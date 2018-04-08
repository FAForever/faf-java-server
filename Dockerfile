FROM openjdk:9-jdk-slim
VOLUME /tmp
ADD build/libs/faf-java-server-*.jar app.jar
ENTRYPOINT ["java", "-server", "-Djava.security.egd=file:/dev/./urandom", "-XX:+CompactStrings", "-XX:MaxMetaspaceSize=196m", "-XX:MinHeapFreeRatio=25", "-XX:MaxHeapFreeRatio=40", "-jar", "app.jar"]
