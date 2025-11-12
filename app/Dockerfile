# 빌드 스테이지
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Gradle 래퍼와 의존성 파일 복사
COPY gradlew .
COPY gradle gradle
COPY app/build.gradle .
COPY settings.gradle .

# gradlew 실행 권한 부여
RUN chmod +x gradlew

# 의존성 다운로드 (캐싱 최적화)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY app/src src

# 애플리케이션 빌드
RUN ./gradlew bootJar --no-daemon


# 실행 스테이지
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 애플리케이션 실행을 위한 사용자 생성 (보안)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]