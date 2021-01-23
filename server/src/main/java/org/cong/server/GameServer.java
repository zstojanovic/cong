package org.cong.server;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import org.cong.server.bot.IffyBot;
import org.cong.server.bot.Bot;
import org.cong.server.bot.RandoBot;
import org.cong.server.bot.ZippyBot;
import org.cong.shared.*;
import org.cong.shared.protocol.*;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
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
  boolean botsUsed;

  Runnable[] powerUpCreators = new Runnable[] {
    () -> BallFreeze.create(world),
    () -> ExtraBall.create(world),
    () -> PaddleSlowdown.create(world),
    () -> PaddleGrowth.create(world)
  };
  Supplier<Bot>[] botSuppliers = new Supplier[] {ZippyBot::new, RandoBot::new, IffyBot::new};

  @Override
  public void create() {
    logger.info("Server started");
    preferences = Gdx.app.getPreferences("cong-server");
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
      @Override public void endContact(Contact contact) {}
      @Override public void preSolve(Contact contact, Manifold oldManifold) {}
      @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
    });
  }

  @Override
  public void render() {
    var delta = Gdx.graphics.getDeltaTime();
    world.step(delta, 6, 2);
    Player.repo.stream().forEach(p -> p.step(delta));
    Paddle.repo.stream().forEach(p -> p.step(delta));
    handleBalls(delta);
    handlePowerUps(delta);
    socketManager.broadcast(getState());
    bounce = false;
    drop = false;
    collect = false;
  }

  void handleBalls(float delta) { // Yes, this is how I'm naming this method. No discussion.
    Ball.repo.stream().forEach(b -> b.step(delta));
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
  }

  void handlePowerUps(float delta) {
    if (bounceCount >= 2 && status == Status.PLAY) {
      powerUpCreators[MathUtils.random(powerUpCreators.length - 1)].run();
      bounceCount = 0;
    }
    PowerUp.repo.stream().forEach(p -> p.step(delta));
  }

  void startCountdown() {
    status = Status.COUNTDOWN;
    countdownTimer = 3;
    if (playTimer > recordTime) {
      if (botsUsed) {
        sendServerMessage((int)playTimer + "s without accident with bot help wont be recorded");
      } else {
        recordTime = playTimer;
        recordNames = Player.repo.stream().filter(p -> p.paddle().isPresent()).map(a -> a.name).toArray(String[]::new);
        preferences.putFloat("record.time", recordTime);
        preferences.putString("record.names", String.join(",", recordNames));
        preferences.flush();
        getRecordText(false).ifPresent(this::sendServerMessage);
      }
    }
    botsUsed = Player.repo.stream().anyMatch(p -> p.bot().isPresent());;
    PowerUp.repo.stream().forEach(PowerUp::dispose);
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
      PowerUp.repo.stream().map(PowerUp::getState).toArray(PowerUpState[]::new));
  }

  Optional<RecordStats> getRecordStats() {
    return getRecordText(true).map(RecordStats::new);
  }

  Optional<String> getRecordText(boolean upper) {
    if (recordNames.length == 0) return Optional.empty();
    var firstPart = String.format(upper ? "RECORD OF %s" : "Record of %s", (int)recordTime + "s");
    var secondPartBuilder =
      new StringBuilder(" without accident set by ");
    for (int i = 0; i < (recordNames.length - 1); i++) {
      secondPartBuilder.append(recordNames[i]);
      if (i == recordNames.length - 2) {
        secondPartBuilder.append(" and ");
      } else {
        secondPartBuilder.append(", ");
      }
    }
    secondPartBuilder.append(recordNames[recordNames.length - 1]);
    var secondPart = upper ? secondPartBuilder.toString().toUpperCase() : secondPartBuilder.toString();
    return Optional.of(firstPart + secondPart);
  }

  void handleRecordStatsRequest(WebSocket socket) {
    getRecordStats().ifPresent(stats -> socketManager.send(socket, stats));
  }

  Optional<Player> createNewPlayer(NewPlayerRequest request, WebSocket socket) {
    if (Paddle.repo.count() == Const.MAX_PADDLES) handleRemoveBots(1); // if no free paddles, try to remove one bot
    var paddle = Paddle.create(world);
    if (paddle.isEmpty()) logger.warn("All paddles occupied");
    var player = Player.create(request.name, paddle, Optional.empty());
    if (player.isPresent()) {
      var names = Player.repo.stream().map(a -> a.name).toArray(String[]::new);
      socketManager.send(socket, new NewPlayerResponse(player.get().id, chatQueue.toArray(ChatMessage[]::new)));
      logger.info(player.get() + " created");
      sendServerMessage(player.get().name + (paddle.isPresent() ? " joined the game" : " joined chat only"));
    } else {
      logger.error("Too many players");
    }
    return player;
  }

  void sendServerMessage(String text) {
    handleMessage(new ChatMessage(getTimestamp(), text));
  }

  void handleMessage(Player player, String message) {
    handleMessage(new ChatMessage(getTimestamp(), player.name, message));
  }

  void handleMessage(ChatMessage chatMessage) {
    var name = chatMessage.playerName == null ? "[SERVER]" : chatMessage.playerName;
    logger.info("[Message] " + name + ": " + chatMessage.text);
    chatQueue.add(chatMessage);
    if (chatQueue.size() > Const.MESSAGE_LIMIT) chatQueue.remove();
    socketManager.broadcast(chatMessage);
  }

  private String getTimestamp() {
    return formatter.format(LocalTime.now());
  }

  void disconnectPlayer(Player player) {
    sendServerMessage(player.name + " left the game");
    player.dispose();
  }

  void handleKeyEvent(Player player, KeyEvent event) {
    if (player.paddle().isEmpty()) logger.warn("Player without paddle sent KeyEvent");
    player.paddle().ifPresent(p -> p.handleKeyEvent(event));
  }

  void handleAddBot(String args) {
    if (args.isEmpty()) addBot(MathUtils.random(botSuppliers.length - 1));
    for (char c : args.toCharArray()) {
      var index = c - 49;
      if (index >= 0 && index < botSuppliers.length) addBot(index);
    }
  }

  private void addBot(int index) {
    var paddle = Paddle.create(world);
    if (paddle.isEmpty()) {
      sendServerMessage("Can't create bot, all paddles occupied");
    } else {
      var bot = botSuppliers[index].get();
      bot.setup(paddle.get());
      var player = Player.create(bot.name(), paddle, Optional.of(bot));
      if (player.isEmpty()) {
        logger.error("Too many players"); // This should never happen since we got the paddle and player limit is higher
      } else {
        botsUsed = true;
        logger.info("Bot " + player.get() + " created");
        sendServerMessage(bot.name() + " joined the game");
      }
    }
  }

  void handleRemoveBots(int limit) {
    Player.repo.stream()
      .filter(p -> p.bot().isPresent())
      .limit(limit)
      .forEach(p -> {
        logger.info("Bot" + p + " removed");
        disconnectPlayer(p);
      });
  }

  public static void main(String[] args) {
    var config = new HeadlessApplicationConfiguration();
    config.renderInterval = 1 / 30f;
    new HeadlessApplication(new GameServer(), config);
  }
}
