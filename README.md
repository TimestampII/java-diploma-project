# ExploreWithMe

Дипломный проект курса Java-разработчик (Yandex Practicum) — афиша для поиска
событий и компании для участия в них. Backend на Spring Boot: сервис
статистики просмотров и основной сервис (пользователи, категории, события,
заявки на участие, подборки), плюс дополнительная функциональность —
рейтинг событий.

Проект собран на основе официального шаблона
[yandex-praktikum/java-explore-with-me](https://github.com/yandex-praktikum/java-explore-with-me):
`groupId` — `ru.practicum`, Spring Boot **3.3.2**, Java **21**.

**Pull request с реализацией дополнительной функциональности (rating_events):**
https://github.com/TimestampII/java-diploma-project/pull/6

## Структура модулей

```
explore-with-me
├── pom.xml                       # родительский pom
├── checkstyle.xml                # из шаблона
├── suppressions.xml              # из шаблона
├── lombok.config                 # из шаблона
├── ewm-stats-service-spec.json   # спецификация сервиса статистики
├── stats                         # РОДИТЕЛЬСКИЙ МОДУЛЬ СТАТИСТИКИ (packaging=pom)
│   ├── dto                       # общие DTO (artifactId: stats-dto)
│   ├── server                    # сервис статистики: REST + JPA + PostgreSQL (artifactId: stats-server)
│   └── client                    # HTTP-клиент сервиса статистики (artifactId: stats-client)
├── ewm-service                   # ОСНОВНОЙ СЕРВИС
│   └── src/main/java/ru/practicum/ewm
│       ├── user            # пользователи (admin)
│       ├── category        # категории (admin + public)
│       ├── event            # события (private/admin/public), интеграция со stats-client
│       ├── request          # заявки на участие
│       ├── compilation      # подборки событий (admin + public)
│       ├── rating           # доп. функциональность: лайки/дизлайки, рейтинг (feature_rating_events)
│       ├── exception        # ApiError, ErrorHandler, кастомные исключения
│       └── util             # OffsetPageRequest, DateTimeConstants
├── postman
│   ├── stats-service.postman_collection.json
│   ├── ewm-service.postman_collection.json
│   └── feature.json          # тесты дополнительной функциональности rating_events
└── docker-compose.yml
```

## Домены основного сервиса

| Домен | Основные эндпоинты |
|---|---|
| Users | `/admin/users` |
| Categories | `/admin/categories`, `/categories` |
| Events | `/users/{userId}/events`, `/admin/events`, `/events` |
| Requests | `/users/{userId}/requests`, `/users/{userId}/events/{eventId}/requests` |
| Compilations | `/admin/compilations`, `/compilations` |
| Rating (доп. функциональность) | `/users/{userId}/events/{eventId}/rating`, `/users/{userId}/rating` |

Полная спецификация основного сервиса:
https://github.com/yandex-praktikum/java-explore-with-me/blob/main/ewm-main-service-spec.json

## Дополнительная функциональность — rating_events

Пользователи могут ставить лайк/дизлайк опубликованным событиям (по одному
голосу на пользователя, повторный голос меняет значение, а не дублируется).
Чистый рейтинг события (`лайки − дизлайки`) отображается в `EventFullDto`/
`EventShortDto`, добавлена сортировка `GET /events?sort=RATING`, и доступен
суммарный рейтинг организатора по всем его событиям через
`GET /users/{userId}/rating`.

Бизнес-правила: нельзя голосовать за своё событие и за неопубликованное
событие (409 в обоих случаях).

## Эндпоинты сервиса статистики

| Метод | Путь     | Описание                                                                     |
| ----- | -------- | ------------------------------------------------------------------------------ |
| POST  | `/hit`   | сохранить информацию об обращении к эндпоинту (201)                          |
| GET   | `/stats` | получить статистику за период (`start`, `end`, опционально `uris`, `unique`) |

`start`/`end` передаются в формате `yyyy-MM-dd HH:mm:ss`.

## Сборка

```bash
mvn clean install
```

## Запуск через Docker

```bash
docker compose up --build stats-server stats-db ewm-service ewm-db
```

- Сервис статистики: `http://localhost:9090`
- Основной сервис: `http://localhost:8080`

## Проверка

Postman-коллекции лежат в `postman/`:
- `stats-service.postman_collection.json` — сервис статистики
- `ewm-service.postman_collection.json` — основной сервис (все 5 доменов)
- `feature.json` — дополнительная функциональность rating_events

Запуск через Newman:
```bash
npm install -g newman
newman run postman/ewm-service.postman_collection.json
newman run postman/feature.json
```

## Ветки и история разработки

| Этап | Ветка | Что реализовано |
|---|---|---|
| 1 | `stat_svc` | Сервис статистики |
| 2 | `main_svc` | Основной сервис: Users, Categories, Events, Requests, Compilations |
| 3 | `feature_rating_events` | Дополнительная функциональность: рейтинг событий |