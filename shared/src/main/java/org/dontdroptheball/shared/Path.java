package org.dontdroptheball.shared;

public class Path {
  static public final float OFFSET = 1;
  static public final float WIDTH = Arena.WIDTH - (2 * OFFSET);
  static public final float HEIGHT = Arena.HEIGHT - (2 * OFFSET);
  static public final float POINT1 = WIDTH;
  static public final float POINT2 = POINT1 + HEIGHT;
  static public final float POINT3 = POINT2 + WIDTH;
  static public final float LENGTH = WIDTH * 2 + HEIGHT * 2;
}
