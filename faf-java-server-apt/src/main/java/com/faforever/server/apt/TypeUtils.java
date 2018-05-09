package com.faforever.server.apt;

import lombok.experimental.UtilityClass;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
final class TypeUtils {

  private final Pattern JAVA_UTIL_LIST_PATTERN = Pattern.compile("java\\.util\\.List<(.*)>");
  private final Pattern JAVA_UTIL_SET_PATTERN = Pattern.compile("java\\.util\\.Set<(.*)>");

  private String type(String typeName) {
    if ("java.lang.String".equals(typeName)) {
      return "string";
    }
    if ("java.lang.Integer".equals(typeName)) {
      return forPrimitive(TypeKind.INT);
    }
    if ("java.lang.Long".equals(typeName)) {
      return forPrimitive(TypeKind.LONG);
    }
    if ("java.lang.Double".equals(typeName)) {
      return forPrimitive(TypeKind.DOUBLE);
    }
    if ("java.lang.Boolean".equals(typeName)) {
      return forPrimitive(TypeKind.BOOLEAN);
    }
    if ("java.lang.Byte".equals(typeName)) {
      return forPrimitive(TypeKind.BYTE);
    }
    if ("java.lang.Short".equals(typeName)) {
      return forPrimitive(TypeKind.SHORT);
    }
    if ("java.lang.Char".equals(typeName)) {
      return forPrimitive(TypeKind.CHAR);
    }
    if ("java.lang.Float".equals(typeName)) {
      return forPrimitive(TypeKind.FLOAT);
    }
    if ("java.util.UUID".equals(typeName)) {
      return "string (uuid)";
    }
    if ("java.util.TimeZone".equals(typeName)) {
      return "string (time zone)";
    }
    Matcher matcher = JAVA_UTIL_LIST_PATTERN.matcher(typeName);
    if (matcher.find()) {
      return String.format("%s[]", type(matcher.group(1)));
    }

    matcher = JAVA_UTIL_SET_PATTERN.matcher(typeName);
    if (matcher.find()) {
      return String.format("%s[]", type(matcher.group(1)));
    }

    if (typeName.contains(".")) {
      return typeName.substring(typeName.lastIndexOf(".") + 1, typeName.length());
    }

    return typeName;
  }

  private static String forPrimitive(TypeKind typeKind) {
    switch (typeKind) {
      case BOOLEAN:
        return "boolean";
      case BYTE:
        return "int8";
      case SHORT:
        return "int16";
      case INT:
        return "int32";
      case LONG:
        return "int64";
      case CHAR:
        return "character";
      case FLOAT:
        return "float";
      case DOUBLE:
        return "double";

      default:
        throw new IllegalArgumentException("Not a primitive: " + typeKind);
    }
  }

  String type(Element element) {
    switch (element.asType().getKind()) {
      case BOOLEAN:
      case BYTE:
      case SHORT:
      case INT:
      case LONG:
      case CHAR:
      case FLOAT:
      case DOUBLE:
        return forPrimitive(element.asType().getKind());
      case TYPEVAR:
        return "typevar";
      case WILDCARD:
        return "?";
      case DECLARED:
        return type(element.asType());
      case ARRAY:
        return type(element.asType());

      default:
        throw new IllegalArgumentException("Not meant to serialize: " + element.getKind());
    }
  }

  @SuppressWarnings("unchecked")
  Optional<VariableElement> typeName(Element value) {
    return (Optional<VariableElement>) value.getEnclosedElements().stream()
      .filter(o -> "TYPE_NAME".equals(o.getSimpleName().toString()))
      .findFirst();
  }

  String type(TypeMirror typeMirror) {
    return type(typeMirror.toString());
  }
}
