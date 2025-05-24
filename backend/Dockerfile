FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 AS builder

WORKDIR /app
COPY . .

RUN sbt clean assembly

FROM eclipse-temurin:24-jdk-alpine

WORKDIR /app
COPY --from=builder /app/target/scala-3.*/t-health-backend-assembly-*.jar ./app.jar
COPY docker-init/ ./docker-init/
COPY compose.yml ./compose.yml

ENTRYPOINT ["java", "-jar", "app.jar"]
