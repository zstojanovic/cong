package org.cong.server;

import com.badlogic.gdx.math.MathUtils;
import org.cong.shared.protocol.KeyEvent;

import java.util.Optional;

public abstract class Bot {
  String name;
  abstract Optional<KeyEvent> think(float delta);
}

class TrivialBot extends Bot {
  boolean started;

  TrivialBot() {
    name = "TrivialBot_" + MathUtils.random(999);
  }

  @Override
  public Optional<KeyEvent> think(float delta) {
    if (!started) {
      started = true;
      if (MathUtils.random() > 0.5) {
        return Optional.of(new KeyEvent(KeyEvent.Code.LEFT_PRESSED));
      } else {
        return Optional.of(new KeyEvent(KeyEvent.Code.RIGHT_PRESSED));
      }
    }
    return Optional.empty();
  }
}

class DumbBot extends Bot {
  Optional<KeyEvent.Code>[] codes = new Optional[] {
    Optional.of(KeyEvent.Code.LEFT_PRESSED),
    Optional.of(KeyEvent.Code.LEFT_RELEASED),
    Optional.of(KeyEvent.Code.RIGHT_PRESSED),
    Optional.of(KeyEvent.Code.RIGHT_RELEASED),
    Optional.empty()
  };

  DumbBot() {
    name = "DumbBot_" + MathUtils.random(999);
  }

  @Override
  public Optional<KeyEvent> think(float delta) {
    if (MathUtils.random() < 0.01) {
      return codes[MathUtils.random(4)].map(KeyEvent::new);
    }
    return Optional.empty();
  }
}