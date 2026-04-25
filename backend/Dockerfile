#dockerfile# 첫 번째 스테이지: 빌드 스테이지
FROM gradle:jdk-21-and-23-graal-jammy AS builder

ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    LANGUAGE=C.UTF-8

# 작업 디렉토리 설정
WORKDIR /app

# 소스 코드와 Gradle 래퍼 복사
COPY build.gradle.kts .
COPY settings.gradle.kts .

# 종속성 설치
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY ../.env .
COPY src src

# 애플리케이션 빌드
RUN gradle build --no-daemon -x test

# 두 번째 스테이지: 실행 스테이지
FROM container-registry.oracle.com/graalvm/jdk:21

# 런타임 로케일 설정 추가
ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    LANGUAGE=C.UTF-8

# 작업 디렉토리 설정
WORKDIR /app

# 첫 번째 스테이지에서 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar
COPY --from=builder /app/.env .env

# 실행할 JAR 파일 지정
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]