package org.dontdroptheball.server;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import org.dontdroptheball.shared.*;
import org.dontdroptheball.shared.protocol.*;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class GameServer extends ApplicationAdapter {
	Logger logger = LoggerFactory.getLogger(GameServer.class);
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
	ServerConnectionManager socketManager;
	World world;
	Ball ball;
	Queue<ChatMessage> chatQueue = new LinkedList<>();

	@Override
	public void create() {
		logger.info("Server started");
		socketManager = new ServerConnectionManager(this);
		world = new World(Vector2.Zero, true);
		ball = new Ball(this);
	}

	@Override
	public void render() {
		var delta = Gdx.graphics.getDeltaTime();
		world.step(delta, 6, 2);
		ball.step(delta);
		Paddle.all().forEach(p -> p.step(delta));
		socketManager.broadcast(getState());
	}

	private GameState getState() {
		return new GameState(
			ball.getState(),
			Paddle.all().stream().map(Paddle::getState).collect(Collectors.toList()));
	}

	Player createNewPlayer(NewPlayerRequest request, WebSocket socket) {
		var paddle = Paddle.create(world);
		if (paddle.isEmpty()) logger.warn("All paddles occupied");
		var player = Player.create(request.name, paddle);
		var names = Player.all().stream().map(a -> a.name).toArray(String[]::new);
		socketManager.broadcast(new PlayerNames(names));
		socketManager.send(socket, new NewPlayerResponse(player.id, chatQueue.toArray(ChatMessage[]::new)));
		handleMessage(new ChatMessage(getTimestamp(), player.name + " joined the game\n")); // TODO change wording if no paddle?
		return player;
	}

	void handleMessage(Player player, String message) {
		handleMessage(new ChatMessage(getTimestamp(), player.id, message));
	}

	void handleMessage(ChatMessage chatMessage) {
		chatQueue.add(chatMessage);
		if (chatQueue.size() > Const.MESSAGE_LIMIT) chatQueue.remove();
		socketManager.broadcast(chatMessage);
	}

	private String getTimestamp() {
		return formatter.format(LocalTime.now());
	}

	void disconnectPlayer(Player player) {
		socketManager.broadcast(new ChatMessage(getTimestamp(), player.name + " left the game\n"));
		player.dispose();
	}

	void handleKeyEvent(Player player, KeyEvent event) {
		if (player.paddle.isEmpty()) logger.warn("Player without paddle sent KeyEvent");
		player.paddle.ifPresent(p -> p.handleKeyEvent(event));
	}

	Optional<Paddle> getRandomPaddle() {
		var paddles = Paddle.all();
		if (paddles.size() == 0) return Optional.empty();
		return Optional.of(paddles.get(MathUtils.random(paddles.size() - 1)));
	}

	public static void main(String[] args) {
		var config = new HeadlessApplicationConfiguration();
		config.renderInterval = 1 / 30f;
		new HeadlessApplication(new GameServer(), config);
	}
}
