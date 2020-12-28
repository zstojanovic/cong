package org.dontdroptheball.server;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
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
	enum Status { COUNTDOWN, PLAY }

	Status status = Status.COUNTDOWN;
	Preferences preferences;
	Logger logger = LoggerFactory.getLogger(GameServer.class);
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
	ServerConnectionManager socketManager;
	World world;
	float countdownTimer;
	float playTimer;
	float recordTime;
	String[] recordNames;
	Queue<ChatMessage> chatQueue = new LinkedList<>();
	int bounceCount = 0;
	boolean bounce;
	boolean drop;
	boolean collect;

	Runnable[] powerUpRunnables = new Runnable[] {
		() -> BallFreeze.create(world),
		() -> ExtraBall.create(world),
		() -> PaddleSlowdown.create(world),
		() -> PaddleGrowth.create(world)
	};

	@Override
	public void create() {
		logger.info("Server started");
		preferences = Gdx.app.getPreferences("dontdroptheball-server");
		recordTime = preferences.getFloat("record.time");
		recordNames = preferences.getString("record.names").split(",");
		socketManager = new ServerConnectionManager(this);
		world = new World(Vector2.Zero, true);
		Ball.create(world);
		startCountdown();

		world.setContactListener(new ContactListener() {
			@Override
			public void beginContact(Contact contact) {
				var objectA = contact.getFixtureA().getBody().getUserData();
				var objectB = contact.getFixtureB().getBody().getUserData();
				if (objectA instanceof Paddle && objectB instanceof PowerUp) {
					((PowerUp)objectB).collect((Paddle)objectA);
					collect = true;
				}
				if (objectA instanceof Ball || objectB instanceof Ball) {
					bounceCount++;
					bounce = true;
				}
			}
			@Override	public void endContact(Contact contact) {}
			@Override	public void preSolve(Contact contact, Manifold oldManifold) {}
			@Override	public void postSolve(Contact contact, ContactImpulse impulse) {}
		});
	}

	@Override
	public void render() {
		var delta = Gdx.graphics.getDeltaTime();
		world.step(delta, 6, 2);
		Paddle.repo.stream().forEach(p -> p.step(delta));
		handleBalls(delta);
		handlePowerUps(delta);
		socketManager.broadcast(getState());
		bounce = false;
		drop = false;
		collect = false;
	}

	void handleBalls(float delta) { // Yes, this is how I'm naming this method. No discussion.
		var dropped = Ball.repo.stream().filter(Ball::dropped).collect(Collectors.toList());
		var skip = Ball.repo.count() - dropped.size() == 0 ? 1 : 0;
		dropped.stream().skip(skip).forEach(Ball::dispose);
		if (skip == 1) {
			drop = true;
			startCountdown();
		}
		if (status == Status.COUNTDOWN) {
			countdownTimer -= delta;
			if (countdownTimer <= 0) startPlaying();
		} else {
			playTimer += delta;
		}
		Ball.repo.stream().forEach(b -> {
			var diff = Math.abs(b.body.getLinearVelocity().len() - b.velocity);
			if (diff > 0.05f) {
				b.body.setLinearVelocity(b.body.getLinearVelocity().setLength(b.velocity));
			}
		});
	}

	void handlePowerUps(float delta) {
		if (bounceCount >= 2 && status == Status.PLAY) {
			powerUpRunnables[MathUtils.random(powerUpRunnables.length - 1)].run();
			bounceCount = 0;
		}
		PowerUp.repo.stream().forEach(p -> p.step(delta));
	}

	void startCountdown() {
		status = Status.COUNTDOWN;
		countdownTimer = 3;
		if (playTimer > recordTime) {
			recordTime = playTimer;
			recordNames = Player.repo.stream().map(a -> a.name).toArray(String[]::new);
			preferences.putFloat("record.time", recordTime);
			preferences.putString("record.names", String.join(",", recordNames));
			preferences.flush();
		}
		PowerUp.withBodies().forEach(PowerUp::dispose);
		Ball.repo.first().startCountdown();
	}

	void startPlaying() {
		status = Status.PLAY;
		playTimer = 0;
		bounceCount = 0;
		var paddle = Paddle.repo.random();
		if (paddle.isPresent()) {
			Ball.repo.first().startPlaying(paddle.get().body.getPosition());
		} else {
			Ball.repo.first().startPlaying(MathUtils.random() * MathUtils.PI2);
		}
	}

	private GameState getState() {
		return new GameState(
			bounce, drop, collect,
			playTimer, recordTime,
			Ball.repo.stream().map(Ball::getState).toArray(BallState[]::new),
			Paddle.repo.stream().map(Paddle::getState).toArray(PaddleState[]::new),
			PowerUp.withBodies().map(PowerUp::getState).toArray(PowerUpState[]::new));
	}

	RecordStats getRecordStats() {
		if (recordNames.length == 0) return new RecordStats("");
		var builder =
			new StringBuilder("RECORD OF ").append((int)recordTime).append("s WITHOUT ACCIDENT SET BY ");
		for (int i = 0; i < (recordNames.length - 1); i++) {
			builder.append(recordNames[i].toUpperCase());
			if (i == recordNames.length - 2) {
				builder.append(" AND ");
			} else {
				builder.append(", ");
			}
		}
		builder.append(recordNames[recordNames.length - 1].toUpperCase());
		return new RecordStats(builder.toString());
	}

	Optional<Player> createNewPlayer(NewPlayerRequest request, WebSocket socket) {
		var paddle = Paddle.create(world);
		if (paddle.isEmpty()) logger.warn("All paddles occupied");
		var player = Player.create(request.name, paddle);
		player.ifPresent(p -> {
			var names = Player.repo.stream().map(a -> a.name).toArray(String[]::new);
			socketManager.broadcast(new PlayerNames(names));
			socketManager.send(socket, new NewPlayerResponse(p.id, chatQueue.toArray(ChatMessage[]::new)));
			handleMessage(new ChatMessage(getTimestamp(), p.name + " joined the game\n"));
		});
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

	public static void main(String[] args) {
		var config = new HeadlessApplicationConfiguration();
		config.renderInterval = 1 / 30f;
		new HeadlessApplication(new GameServer(), config);
	}
}
