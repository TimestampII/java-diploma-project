FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY stats/pom.xml stats/pom.xml
COPY stats/dto stats/dto
COPY stats/server stats/server
COPY stats/client stats/client
COPY ewm-service ewm-service

RUN mvn -q -B -pl ewm-service -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/ewm-service/target/ewm-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]