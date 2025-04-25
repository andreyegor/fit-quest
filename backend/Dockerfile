FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0

WORKDIR /app
COPY . .
RUN sbt stage


FROM postgres:17.4

ENTRYPOINT ["./target/universal/stage/bin/t-health-backend"]
