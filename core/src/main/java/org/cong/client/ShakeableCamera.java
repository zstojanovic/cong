package org.cong.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class ShakeableCamera extends OrthographicCamera {
  float duration = 0.2f;
  float timer;
  Vector3 startPosition = new Vector3();

  ShakeableCamera(float width, float height) {
    super(width, height);
  }

  void shake() {
    timer = duration;
    startPosition.set(position);
  }

  @Override
  public void update() {
    if (timer > 0) {
      timer -= Gdx.graphics.getDeltaTime();
      if (timer <= 0) {
        position.set(startPosition);
      } else {
        var currentPower = 0.35f * timer / duration;
        translate((MathUtils.random() - 0.5f) * currentPower, (MathUtils.random() - 0.5f) * currentPower);
      }
    }
    super.update();
  }
}
