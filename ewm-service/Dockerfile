FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# На этапе 1 у ewm-service ещё нет кода и репакованного Spring Boot jar —
# этот образ появится реально рабочим только на этапе 2. До тех пор сервис
# не запускается через `docker compose up` по умолчанию (см. profiles в
# docker-compose.yml — используйте `docker compose --profile main up`).
COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
