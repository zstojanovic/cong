package org.cong.server;

public abstract class Identifiable {
  protected byte id;

  Identifiable(byte id) {
    this.id = id;
  }
}
