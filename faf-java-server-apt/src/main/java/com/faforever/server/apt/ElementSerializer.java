package com.faforever.server.apt;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.Value;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ElementSerializer extends StdSerializer<TypeElement> {
  private final ProcessingEnvironment processingEnv;
  private final TypeMirror byteTypeMirror;
  private final TypeMirror charTypeMirror;
  private final TypeMirror shortTypeMirror;
  private TypeMirror stringTypeMirror;
  private TypeMirror integerTypeMirror;
  private TypeMirror floatTypeMirror;
  private TypeMirror booleanTypeMirror;
  private TypeMirror doubleTypeMirror;

  public ElementSerializer(ProcessingEnvironment processingEnv) {
    super((Class<TypeElement>) null);
    this.processingEnv = processingEnv;

    stringTypeMirror = processingEnv.getElementUtils().getTypeElement(String.class.getName()).asType();
    integerTypeMirror = processingEnv.getElementUtils().getTypeElement(Integer.class.getName()).asType();
    floatTypeMirror = processingEnv.getElementUtils().getTypeElement(Float.class.getName()).asType();
    doubleTypeMirror = processingEnv.getElementUtils().getTypeElement(Double.class.getName()).asType();
    booleanTypeMirror = processingEnv.getElementUtils().getTypeElement(Boolean.class.getName()).asType();
    byteTypeMirror = processingEnv.getElementUtils().getTypeElement(Byte.class.getName()).asType();
    charTypeMirror = processingEnv.getElementUtils().getTypeElement(Character.class.getName()).asType();
    shortTypeMirror = processingEnv.getElementUtils().getTypeElement(Short.class.getName()).asType();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void serialize(TypeElement value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    boolean isComplexType = isComplexType(value);

    if (isComplexType) {
      writeComplexType(value, gen);
    } else {
      gen.writeRawValue(TypeUtils.type(value));
    }
  }

  private void writeComplexType(Element value, JsonGenerator gen) throws IOException {
    gen.writeStartObject();

    Optional<VariableElement> typeNameElement = TypeUtils.typeName(value);

    if (typeNameElement.isPresent()) {
      gen.writeFieldName("type");
      gen.writeString((String) typeNameElement.get().getConstantValue());
      gen.writeFieldName("data");
      gen.writeStartObject();
    }

    Map<Name, Element> fields = value.getEnclosedElements().stream()
      .filter(enclosedElement -> enclosedElement.getKind() == ElementKind.FIELD
        && !enclosedElement.getModifiers().contains(Modifier.STATIC))
      .collect(Collectors.toMap(Element::getSimpleName, Function.identity()));

    for (Entry<Name, Element> entry : fields.entrySet()) {
      gen.writeFieldName(String.valueOf(entry.getKey()));

      Element element = entry.getValue();
      switch (element.asType().getKind()) {
        case BOOLEAN:
        case BYTE:
        case SHORT:
        case INT:
        case LONG:
        case CHAR:
        case FLOAT:
        case DOUBLE:
        case TYPEVAR:
        case WILDCARD:
          gen.writeRawValue(TypeUtils.type(element));
          break;

        case DECLARED:
          DeclaredType declaredType = (DeclaredType) element.asType();
          TypeElement typeElement = (TypeElement) processingEnv.getTypeUtils().asElement(declaredType);
          if (isString(typeElement) || isWrapper(typeElement)) {
            gen.writeRawValue(TypeUtils.type(typeElement));
          } else if (isClass("java.lang.Object", typeElement)) {
            gen.writeRawValue("any");
          } else if (isClass("java.time.Instant", typeElement) || isClass("java.time.OffsetDateTime", typeElement)) {
            gen.writeRawValue("ISO 8601 string");
          } else if (isClass("java.util.UUID", typeElement)) {
            gen.writeRawValue("uuid");
          } else if (isClass("java.net.URI", typeElement)) {
            gen.writeRawValue("uri");
          } else if (isClass("java.net.URL", typeElement)) {
            gen.writeRawValue("url");
          } else if (isClass("java.util.TimeZone", typeElement)) {
            gen.writeRawValue("timeZone");
          } else if (isComplexType(typeElement)) {
            gen.writeObject(typeElement);
          } else if (isEnum(typeElement)) {
            gen.writeRawValue(typeElement.getSimpleName().toString());
          } else if (isCollection(typeElement)) {
            gen.writeStartArray();
            gen.writeObject(processingEnv.getTypeUtils().asElement(declaredType.getTypeArguments().get(0)));
            gen.writeEndArray();
          } else {
            throw new IllegalStateException("Uncovered: " + typeElement);
          }
          break;

        case ARRAY:
          gen.writeStartArray();
          gen.writeObject(processingEnv.getTypeUtils().asElement(((ArrayType) element.asType()).getComponentType()));
          gen.writeEndArray();
          break;

        default:
          throw new IllegalArgumentException("Not meant to serialize: " + value.getKind());
      }
    }

    if (typeNameElement.isPresent()) {
      gen.writeEndObject();
    }

    gen.writeEndObject();
  }

  private boolean isClass(String className, TypeElement typeElement) {
    return (typeElement.getKind() == ElementKind.CLASS)
      && className.equals(typeElement.toString());
  }

  private boolean isCollection(TypeElement typeElement) {
    return (typeElement.getKind() == ElementKind.CLASS || typeElement.getKind() == ElementKind.INTERFACE)
      // TODO use "isSubtype" against Collection instead
      && ("java.util.List".equals(typeElement.toString()) || "java.util.Set".equals(typeElement.toString()));
  }

  private boolean isString(TypeElement element) {
    return isString(element.asType());
  }

  private boolean isString(TypeMirror typeMirror) {
    Types typeUtils = processingEnv.getTypeUtils();
    return typeUtils.isSameType(typeMirror, stringTypeMirror);
  }

  private boolean isWrapper(TypeElement element) {
    Types typeUtils = processingEnv.getTypeUtils();
    TypeMirror typeMirror = element.asType();

    return typeUtils.isSameType(typeMirror, integerTypeMirror)
      || typeUtils.isSameType(typeMirror, floatTypeMirror)
      || typeUtils.isSameType(typeMirror, doubleTypeMirror)
      || typeUtils.isSameType(typeMirror, shortTypeMirror)
      || typeUtils.isSameType(typeMirror, byteTypeMirror)
      || typeUtils.isSameType(typeMirror, charTypeMirror)
      || typeUtils.isSameType(typeMirror, booleanTypeMirror);
  }

  private boolean isComplexType(TypeElement element) {
    TypeMirror typeMirror = element.asType();

    return element.getKind() == ElementKind.CLASS
      && !isString(typeMirror)
      && !isWrapper(element);
  }

  private boolean isEnum(TypeElement element) {
    return element.getKind() == ElementKind.ENUM;
  }

  private void log(String message) {
    System.out.println(message);
  }

  @Value
  private static class ListType {
    TypeMirror typeArgument;
  }
}
