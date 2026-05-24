FROM gradle:jdk-21-and-23-graal-jammy AS builder

ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    LANGUAGE=C.UTF-8

WORKDIR /app

COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN gradle dependencies --no-daemon

COPY src src

RUN gradle build --no-daemon -x test

FROM eclipse-temurin:21-jre-jammy

ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    LANGUAGE=C.UTF-8

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]