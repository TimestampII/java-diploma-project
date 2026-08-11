# ExploreWithMe — Этап 1: сервис статистики

Проект собран на основе официального шаблона
[yandex-praktikum/java-explore-with-me](https://github.com/yandex-praktikum/java-explore-with-me):
тот же `groupId` (`ru.practicum`), Spring Boot **3.3.2**, Java **21**,
`checkstyle.xml` / `suppressions.xml` / `lombok.config` — скопированы из шаблона без изменений.

На этом этапе реализован **сервис статистики** (`stats-server`), общий модуль
DTO (`stats-dto`) и HTTP-клиент (`stats-client`). Модуль `ewm-service`
(основной сервис) пока содержит только заготовку `pom.xml` — как и требуется
на первом этапе; его реализация будет добавлена на следующем этапе.

## Структура модулей

Структура повторяет схему из урока «Этап 1. Сервис статистики»: родительский
модуль статистики (`stats`, packaging `pom`) объединяет три подмодуля — сервис,
клиент и общие DTO; модуль основного сервиса (`ewm-service`) — сосед `stats`
на верхнем уровне и использует `stats-client` напрямую.

```
explore-with-me
├── pom.xml                     # родительский pom (как в шаблоне) + <modules>
├── checkstyle.xml               # из шаблона, без изменений
├── suppressions.xml             # из шаблона, без изменений
├── lombok.config                 # из шаблона, без изменений
├── ewm-stats-service-spec.json   # спецификация сервиса статистики (из шаблона)
├── stats                        # РОДИТЕЛЬСКИЙ МОДУЛЬ СТАТИСТИКИ (packaging=pom)
│   ├── pom.xml
│   ├── dto                      # общие DTO (artifactId: stats-dto)
│   ├── server                   # сервис статистики: REST + JPA + PostgreSQL (artifactId: stats-server)
│   └── client                   # HTTP-клиент сервиса статистики (artifactId: stats-client)
├── ewm-service                   # МОДУЛЬ ОСНОВНОГО СЕРВИСА — заготовка (этап 2), использует stats-client
└── docker-compose.yml
```

> Спецификацию основного сервиса (`ewm-main-service-spec.json`) возьмите
> напрямую из шаблона — она очень объёмная, поэтому не копировалась сюда:
> https://github.com/yandex-praktikum/java-explore-with-me/blob/main/ewm-main-service-spec.json

## Эндпоинты сервиса статистики

| Метод | Путь          | Описание                                             |
|-------|---------------|-------------------------------------------------------|
| POST  | `/hit`        | сохранить информацию об обращении к эндпоинту (201)    |
| GET   | `/stats`      | получить статистику за период (`start`, `end`, опционально `uris`, `unique`) |

`start`/`end` передаются в формате `yyyy-MM-dd HH:mm:ss` и должны быть
URL-encoded на стороне клиента.

## Сборка

```bash
mvn clean package
```

`checkstyle.xml`/`suppressions.xml`/`lombok.config` в корне оставлены как в
официальном шаблоне (на случай, если вы захотите вернуть эти проверки), но
сами плагины `checkstyle`/`spotbugs`/`jacoco` в `pom.xml` не подключены —
в некоторых сетевых окружениях (ограниченный доступ к Maven Central) их
подключение мешает даже обычной сборке, а для функциональности диплома они
не обязательны.

## Запуск через Docker

```bash
mvn clean package -pl stats/dto,stats/server -am
docker compose up stats-server stats-db
```

Сервис статистики будет доступен на `http://localhost:9090`.
Сервисы `ewm-service`/`ewm-db` заработают после реализации основного
сервиса на следующем этапе.

## Проверка вручную

```bash
curl -X POST http://localhost:9090/hit \
  -H "Content-Type: application/json" \
  -d '{"app":"ewm-main-service","uri":"/events/1","ip":"192.163.0.1","timestamp":"2024-01-01 12:00:00"}'

curl "http://localhost:9090/stats?start=2024-01-01%2000:00:00&end=2024-12-31%2023:59:59"
```

## Дальнейшие шаги

1. Использовать `StatsClient` из модуля `stats-client` в основном сервисе,
   чтобы логировать обращения к публичным эндпоинтам `/events` и `/events/{id}`
   (см. `HttpServletRequest.getRemoteAddr()` / `getRequestURI()`).
2. Реализовать основной сервис (`ewm-service`) согласно
   `ewm-main-service-spec.json`.
