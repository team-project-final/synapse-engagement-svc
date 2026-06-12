FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY src src
RUN ./gradlew build --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd -g 101 -r app && useradd -u 101 -r -g app app
COPY --from=build /workspace/build/libs/*.jar app.jar
RUN chown app:app app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
