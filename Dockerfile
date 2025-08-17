# JDK 기반 빌드 이미지
FROM eclipse-temurin:17-jdk

# 작업 디렉토리 생성
WORKDIR /app

# 빌드된 JAR 복사 (예: build/libs/wiseai-dev-0.0.1-SNAPSHOT.jar)
COPY build/libs/*.jar app.jar

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
