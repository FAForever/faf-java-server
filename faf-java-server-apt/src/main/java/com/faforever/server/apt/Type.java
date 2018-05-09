package com.faforever.server.apt;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@EqualsAndHashCode(of = "identifier")
public class Type {
  String identifier;
  String title;
  String description;
  Collection<Field> fields;
  String jsonFormat;
  List<EnumValue> enumValues;

  /** Used in index.ftl. */
  @SuppressWarnings("unused")
  public Set<Type> getRelatedTypes() {
    return fields.stream()
      .map(Field::getType)
      .filter(type -> !type.getFields().isEmpty() && type.enumValues.isEmpty())
      .collect(Collectors.toSet());
  }

  @Value
  public static class Field {
    String name;
    Type type;
    String description;
    boolean required;
    boolean array;
  }

  @Value
  public static class EnumValue {
    String name;
    String description;
  }
}
