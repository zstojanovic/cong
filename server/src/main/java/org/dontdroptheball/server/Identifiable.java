package org.dontdroptheball.server;

public abstract class Identifiable {
  byte id;

  Identifiable(byte id) {
    this.id = id;
  }
}