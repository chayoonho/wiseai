# 1단계: 빌드 스테이지
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar

# 2단계: 최종 실행 스테이지
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]