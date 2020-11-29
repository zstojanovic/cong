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
		Player.all().forEach(p -> p.step(delta));
		socketManager.broadcast(getState());
	}

	private GameState getState() {
		return new GameState(
			ball.getState(),
			Player.all().stream().map(Player::getState).collect(Collectors.toList()));
	}

	Optional<Player> createNewPlayer(NewPlayerRequest request, WebSocket socket) {
		var player = Player.create(request.name, world);
		if (player.isEmpty()) logger.error("Too many players");
		player.ifPresent(p -> {
			var names = Player.all().stream().map(a -> a.name).toArray(String[]::new);
			socketManager.broadcast(new PlayerNames(names));
			socketManager.send(socket, new NewPlayerResponse(p.index, chatQueue.toArray(ChatMessage[]::new)));
			handleMessage(new ChatMessage(getTimestamp(), p.name + " joined the game\n"));
		});
		return player;
	}

	void handleMessage(Player player, String message) {
		handleMessage(new ChatMessage(getTimestamp(), player.index, message));
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
		player.handleKeyEvent(event);
	}

	Optional<Player> getRandomPlayer() {
		var players = Player.all();
		if (players.size() == 0) return Optional.empty();
		return Optional.of(players.get(MathUtils.random(players.size() - 1)));
	}

	public static void main(String[] args) {
		var config = new HeadlessApplicationConfiguration();
		config.renderInterval = 1 / 30f;
		new HeadlessApplication(new GameServer(), config);
	}
}
