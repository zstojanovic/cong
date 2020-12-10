package org.dontdroptheball.shared;

public class Const {
  public static final byte MAX_PLAYERS = 100;
  public static final byte MAX_PADDLES = 9;
  public static final byte MAX_BALLS = 10;
  public static final float WIDTH = 11;
  public static final float HEIGHT = 9;
  public static final int MESSAGE_LIMIT = 50;

  public static class Path {
    static public final float OFFSET = 1;
    static public final float WIDTH = Const.WIDTH - (2 * OFFSET);
    static public final float HEIGHT = Const.HEIGHT - (2 * OFFSET);
    static public final float POINT1 = WIDTH;
    static public final float POINT2 = POINT1 + HEIGHT;
    static public final float POINT3 = POINT2 + WIDTH;
    static public final float LENGTH = WIDTH * 2 + HEIGHT * 2;
  }
}
