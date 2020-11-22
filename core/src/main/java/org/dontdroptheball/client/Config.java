package org.dontdroptheball.client;

import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Map;

public class Config {
  private Map<String, String> map = new HashMap<>();

  void load(FileHandle handle) {
    var lines = handle.readString().split("\\r?\\n");
    for (var l: lines) {
      var line = l.trim();
      if (line.length() > 0 && line.charAt(0) != '#') {
        var i = line.indexOf("=");
        if (i > 0) {
          var key = line.substring(0, i).trim();
          var value = line.substring(i + 1).trim();
          map.put(key, value);
        } else {
          throw new RuntimeException("Illegal config line: " + l);
        }
      }
    }
  }

  String get(String key) {
    return map.get(key);
  }
}
