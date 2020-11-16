package org.dontdroptheball.server;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import org.dontdroptheball.shared.*;
import org.dontdroptheball.shared.protocol.GameState;
import org.dontdroptheball.shared.protocol.KeyEvent;
import org.dontdroptheball.shared.protocol.NewPlayerRequest;
import org.dontdroptheball.shared.protocol.PlayerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GameServer extends ApplicationAdapter {
	Logger logger = LoggerFactory.getLogger(GameServer.class);
	ServerConnectionManager socketManager;
	World world;
	Ball ball;
	Player[] players = new Player[Arena.MAX_PLAYERS];
	Random random = new Random();

	@Override
	public void create() {
		logger.info("Server started");
		socketManager = new ServerConnectionManager(this);
		world = new World(Vector2.Zero, true);
		ball = new Ball(world);
	}

	@Override
	public void render() {
		var delta = Gdx.graphics.getDeltaTime();
		world.step(delta, 6, 2);
		ball.step(delta);
		for (Player player: players) {
			if (player != null) player.step(delta);
		}
		socketManager.broadcastState(getState());
	}

	private GameState getState() {
		var playerStates = new ArrayList<PlayerState>(Arena.MAX_PLAYERS);
		for (Player player: players) {
			if (player != null) playerStates.add(player.getState());
		}
		return new GameState(ball.getState(), playerStates);
	}

	Optional<Player> createNewPlayer(NewPlayerRequest request) {
		byte newIndex = 0;
		while (newIndex < Arena.MAX_PLAYERS && players[newIndex] != null) newIndex++;
		if (newIndex == Arena.MAX_PLAYERS) {
			logger.error("Too many players");
			return Optional.empty();
		}
		var name = request.name.substring(0, Math.min(request.name.length(), 10));
		var newLocation = random.nextInt(100) * Path.LENGTH / 100;
		var player = new Player(newIndex, name, newLocation, world);
		players[newIndex] = player;
		return Optional.of(player);
	}

	void disconnectPlayer(Player player) {
		players[player.index] = null;
		player.dispose();
	}

	void handleKeyEvent(Player player, KeyEvent event) {
		player.handleKeyEvent(event);
	}

	public static void main(String[] args) {
		new HeadlessApplication(new GameServer());
	}
}
