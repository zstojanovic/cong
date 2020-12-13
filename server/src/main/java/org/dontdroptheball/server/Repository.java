package org.dontdroptheball.server;

import com.badlogic.gdx.math.MathUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Repository<Type extends Identifiable> {
  private final Type[] elements;

  Repository(Type[] elements) {
    this.elements = elements;
  }

  Optional<Type> create(Function<Byte, Type> creator) {
    byte newId = 0;
    while (newId < elements.length && elements[newId] != null) newId++;
    if (newId == elements.length) return Optional.empty();
    elements[newId] = creator.apply(newId);
    return Optional.of(elements[newId]);
  }

  void remove(Type element) {
    elements[element.id] = null;
  }

  Stream<Type> stream() {
    return Arrays.stream(elements).filter(Objects::nonNull);
  }

  List<Type> list() {
    return stream().collect(Collectors.toList());
  }

  Type first() {
    return stream().findFirst().get();
  }

  long count() {
    return stream().count();
  }

  Optional<Type> random() {
    var list = list();
    if (list.isEmpty()) return Optional.empty();
    return Optional.of(list.get(MathUtils.random(list.size() - 1)));
  }
}
