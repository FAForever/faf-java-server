package com.faforever.server.apt;

import com.faforever.server.annotations.V2ClientNotification;
import com.faforever.server.annotations.V2ClientRequest;
import com.faforever.server.annotations.V2ServerResponse;
import com.faforever.server.annotations.ValidV2Message;
import com.faforever.server.apt.Type.EnumValue;
import com.faforever.server.apt.Type.Field;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.auto.service.AutoService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import lombok.SneakyThrows;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Locale.US;

@SupportedAnnotationTypes("com.faforever.server.annotations.*")
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_10)
public class V2ProtocolDocGenerator extends AbstractProcessor {

  private static final Pattern CODE_PATTERN = Pattern.compile("\\{@code (.*?)}", Pattern.DOTALL);
  private static final Pattern DEPRECATED_PATTERN = Pattern.compile("@deprecated (.*)", Pattern.DOTALL);
  private static final Pattern TYPE_IDENTIFIER_EXTRACTOR = Pattern.compile("[^A-Z]+(.*)");
  private final Configuration freeMarkerConfig;
  private ObjectMapper objectMapper;

  public V2ProtocolDocGenerator() {
    freeMarkerConfig = new Configuration(new Version(2, 3, 20));
    freeMarkerConfig.setClassForTemplateLoading(V2ProtocolDocGenerator.class, "/");
    freeMarkerConfig.setDefaultEncoding("UTF-8");
    freeMarkerConfig.setLocale(US);
    freeMarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
  }

  @Override
  @SneakyThrows
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (annotations.isEmpty()) {
      return false;
    }

    Set<? extends Element> v2Messages = roundEnv.getElementsAnnotatedWith(ValidV2Message.class);
    checkAnnotations(v2Messages);
    checkTypeNameDeclaration(v2Messages);

    objectMapper = createObjectMapper();

    Map<String, Object> input = ImmutableMap.of("messageCategories", buildMessageCategories(roundEnv));

    FileObject fileObject = getOutputFile();
    try (Writer writer = fileObject.openWriter()) {
      freeMarkerConfig.getTemplate("/templates/index.ftl").process(input, writer);
    }

    return true;
  }

  private void checkTypeNameDeclaration(Set<? extends Element> elementsAnnotatedWith) {
    List<? extends Element> classesWithMissingTypeName = elementsAnnotatedWith.stream()
      .filter(o -> !TypeUtils.typeName(o).isPresent())
      .collect(Collectors.toList());

    if (!classesWithMissingTypeName.isEmpty()) {
      return;
    }

    throw new IllegalStateException("The following classes are missing the constant TYPE_NAME: "
      + Joiner.on("\n  - ").join(classesWithMissingTypeName));
  }

  private ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

    SimpleModule module = new SimpleModule();
    module.addSerializer(TypeElement.class, new ElementSerializer(processingEnv));
    objectMapper.registerModule(module);
    return objectMapper;
  }

  private FileObject getOutputFile() throws IOException {
    return processingEnv.getFiler()
      .createResource(StandardLocation.CLASS_OUTPUT, "com.faforever.server.doc", "v2-protocol.html");
  }

  private List<MessageCategory> buildMessageCategories(RoundEnvironment roundEnv) {
    List<Type> clientTypes = new ArrayList<>();
    clientTypes.addAll(getMessages(roundEnv.getElementsAnnotatedWith(V2ClientNotification.class)));
    clientTypes.addAll(getMessages(roundEnv.getElementsAnnotatedWith(V2ClientRequest.class)));

    MessageCategory clientMessageCategory = new MessageCategory("V2 Client Messages", clientTypes);
    MessageCategory serverMessageCategory = new MessageCategory("V2 Server Messages", getMessages(roundEnv.getElementsAnnotatedWith(V2ServerResponse.class)));

    return Arrays.asList(clientMessageCategory, serverMessageCategory);
  }

  private void checkAnnotations(Set<? extends Element> elementsAnnotatedWith) {
    List<? extends Element> unannotatedClasses = elementsAnnotatedWith.stream()
      .filter(o -> o.getAnnotation(V2ClientRequest.class) == null
        && o.getAnnotation(V2ClientNotification.class) == null
        && o.getAnnotation(V2ServerResponse.class) == null
        && o.getAnnotation(ValidV2Message.class) == null)
      .collect(Collectors.toList());

    if (unannotatedClasses.isEmpty()) {
      return;
    }

    processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "The following classes are missing V2 protocol type annotation: "
      + Joiner.on("\n  - ").join(unannotatedClasses)
      + "\n Documentation for these classes will not be generated.");
  }

  private List<Type> getMessages(Iterable<? extends Element> elements) {
    return StreamSupport.stream(elements.spliterator(), false)
      .map(this::createType)
      .collect(Collectors.toList());
  }

  private Type createType(Element element) {
    Collection<Field> fields = getFields(element);
    return new Type(typeIdentifier(element), getTitle(element), getDescription(element), fields, getJsonFormat(element), enumValues(element));
  }

  private Type createType(TypeMirror typeMirror) {
    switch (typeMirror.getKind()) {
      case BOOLEAN:
      case BYTE:
      case SHORT:
      case INT:
      case LONG:
      case CHAR:
      case FLOAT:
      case DOUBLE:
        return new Type(null, TypeUtils.forPrimitive(typeMirror.getKind()), null, Collections.emptyList(), null, Collections.emptyList());

      case DECLARED:
        DeclaredType declaredType = (DeclaredType) typeMirror;
        if (isCollection(declaredType)) {
          return createType(processingEnv.getTypeUtils().asElement(declaredType.getTypeArguments().get(0)));
        }

        return createType(processingEnv.getTypeUtils().asElement(typeMirror));

      case ARRAY:
        return createType(processingEnv.getTypeUtils().asElement(((ArrayType) typeMirror).getComponentType()));

      default:
        throw new UnsupportedOperationException("Not implemented: " + typeMirror);
    }
  }

  private boolean isCollection(DeclaredType declaredType) {
    switch (declaredType.asElement().toString()) {
      case "java.util.Collection":
      case "java.util.List":
      case "java.util.Set":
        return true;
      default:
        return false;
    }
  }

  private String typeIdentifier(Element element) {
    if (element.toString().startsWith("java.")
      || element.getKind() == ElementKind.ENUM) {
      return null;
    }
    String className = element.asType().toString();
    Matcher matcher = TYPE_IDENTIFIER_EXTRACTOR.matcher(className);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Could not extract type identifier from: " + className);
    }
    return matcher.group(1);
  }

  private Collection<Field> getFields(Element element) {
    if (element == null
      || element.getKind() == ElementKind.ENUM
      || element.asType().toString().startsWith("java.")) {
      return Collections.emptySet();
    }

    return element.getEnclosedElements().stream()
      .filter(enclosedElement -> enclosedElement.getKind() == ElementKind.FIELD
        && !enclosedElement.getModifiers().contains(Modifier.STATIC))
      .map(e -> {
        TypeMirror typeMirror = e.asType();
        Type type = createType(typeMirror);
        return new Field(e.getSimpleName().toString(), type, getDescription(e), isRequired(e), typeMirror.getKind() == TypeKind.ARRAY);
      })
      .collect(Collectors.toSet());
  }

  private List<EnumValue> enumValues(Element e) {
    if (e.getKind() == ElementKind.FIELD) {
      return enumValues(processingEnv.getTypeUtils().asElement(e.asType()));
    }
    if (e.getKind() != ElementKind.ENUM) {
      return Collections.emptyList();
    }

    return e.getEnclosedElements().stream()
      .filter(o -> o.getKind() == ElementKind.ENUM_CONSTANT)
      .map(o -> new EnumValue(o.getSimpleName().toString(), getDescription(o)))
      .collect(Collectors.toList());
  }

  /**
   * Accessing Class values in annotations isn't straight forward because the classes aren't loaded. This method allows
   * to get Class attributes from annotations.
   * <p>
   * See <a href="https://area-51.blog/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/">https://area-51.blog/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/</a>.
   */
  private Optional<? extends AnnotationValue> getAnnotationAttributeValue(Element element, String attributeName) {
    TypeMirror requestElementType = processingEnv.getElementUtils().getTypeElement(V2ClientRequest.class.getName()).asType();

    for (ExecutableElement annotationAttribute : ElementFilter.methodsIn(element.getEnclosedElements())) {
      Optional<? extends AnnotationValue> annotationValue = annotationAttribute.getAnnotationMirrors().stream()
        .filter(o -> processingEnv.getTypeUtils().isSameType(o.getAnnotationType(), requestElementType))
        .flatMap(o -> o.getElementValues().entrySet().stream())
        .filter(entry -> attributeName.equals(entry.getKey().getSimpleName().toString()))
        .findFirst()
        .map(Entry::getValue);

      if (annotationValue.isPresent()) {
        return annotationValue;
      }
    }
    return Optional.empty();
  }

  private String getJsonFormat(Element element) {
    try {
      return objectMapper.writeValueAsString(element);
    } catch (JsonProcessingException e) {
      processingEnv.getMessager().printMessage(Kind.WARNING, "Error while generating JSON (" + e + ")", element);
      e.printStackTrace();
    }
    return "Error";
  }

  private boolean isRequired(Element o) {
    if (o.getKind() != ElementKind.FIELD) {
      return true;
    }
    return o.asType().getKind() != TypeKind.DECLARED
      || o.getAnnotation(NotNull.class) != null;
  }

  private String getTitle(Element element) {
    String title = null;
    if (element.getAnnotation(V2ClientRequest.class) != null) {
      title = element.getAnnotation(V2ClientRequest.class).title();
    } else if (element.getAnnotation(V2ClientNotification.class) != null) {
      title = element.getAnnotation(V2ClientNotification.class).title();
    } else if (element.getAnnotation(V2ServerResponse.class) != null) {
      title = element.getAnnotation(V2ServerResponse.class).title();
    }
    if (Strings.isNullOrEmpty(title)) {
      // Dirty but effective code ahead
      return TypeUtils.type(element)
        .replace("ClientMessage", "")
        .replace("ServerMessage", "")
        .replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2");
    }
    return title;
  }

  private String getDescription(Element element) {
    String description = null;
    if (element.getAnnotation(V2ClientRequest.class) != null) {
      description = element.getAnnotation(V2ClientRequest.class).description();
    } else if (element.getAnnotation(V2ClientNotification.class) != null) {
      description = element.getAnnotation(V2ClientNotification.class).description();
    } else if (element.getAnnotation(V2ServerResponse.class) != null) {
      description = element.getAnnotation(V2ServerResponse.class).description();
    }
    if (Strings.isNullOrEmpty(description)) {
      description = processingEnv.getElementUtils().getDocComment(element);
    }
    if (description == null) {
      return null;
    }

    // TODO use freemarker templates
    Matcher matcher = CODE_PATTERN.matcher(description);
    if (matcher.find()) {
      description = matcher.replaceAll("<code>$1</code>");
    }
    matcher = DEPRECATED_PATTERN.matcher(description);
    if (matcher.find()) {
      description = matcher.replaceAll("<div class=\"alert alert-warning\" role=\"alert\"><strong>Deprecated:</strong> $1</div>");
    }
    return description;
  }
}
