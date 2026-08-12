#!/usr/bin/env bash
#
# Ручная smoke-проверка сервиса статистики после `docker compose up stats-server stats-db`.
# Проверяет POST /hit и GET /stats на соответствие ewm-stats-service-spec.json.
#
# Использование:
#   chmod +x scripts/smoke-test.sh
#   ./scripts/smoke-test.sh
#
# Опционально можно переопределить адрес сервиса:
#   BASE_URL=http://localhost:9090 ./scripts/smoke-test.sh

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9090}"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

PASS=0
FAIL=0

check() {
    local description="$1"
    local expected="$2"
    local actual="$3"
    if [ "$expected" = "$actual" ]; then
        echo -e "  ${GREEN}OK${NC}  $description (ожидалось: $expected, получено: $actual)"
        PASS=$((PASS + 1))
    else
        echo -e "  ${RED}FAIL${NC} $description (ожидалось: $expected, получено: $actual)"
        FAIL=$((FAIL + 1))
    fi
}

echo -e "${YELLOW}== Проверка сервиса статистики на $BASE_URL ==${NC}"
echo

# ---------------------------------------------------------------------------
# 1. POST /hit — сохранение обращения (ip #1)
# ---------------------------------------------------------------------------
echo "1) POST /hit — первое обращение (ip=192.163.0.1)"
HIT1_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/hit" \
    -H "Content-Type: application/json" \
    -d '{
          "app": "ewm-main-service",
          "uri": "/events/1",
          "ip": "192.163.0.1",
          "timestamp": "2024-01-01 12:00:00"
        }')
HIT1_STATUS=$(echo "$HIT1_RESPONSE" | tail -n1)
HIT1_BODY=$(echo "$HIT1_RESPONSE" | sed '$d')
echo "  Ответ: $HIT1_BODY"
check "Код ответа 201 (Created)" "201" "$HIT1_STATUS"
echo

# ---------------------------------------------------------------------------
# 2. POST /hit — второе обращение с тем же ip (для проверки unique=false/true)
# ---------------------------------------------------------------------------
echo "2) POST /hit — второе обращение, тот же ip (для проверки unique)"
HIT2_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/hit" \
    -H "Content-Type: application/json" \
    -d '{
          "app": "ewm-main-service",
          "uri": "/events/1",
          "ip": "192.163.0.1",
          "timestamp": "2024-01-01 12:05:00"
        }')
HIT2_STATUS=$(echo "$HIT2_RESPONSE" | tail -n1)
check "Код ответа 201 (Created)" "201" "$HIT2_STATUS"
echo

# ---------------------------------------------------------------------------
# 3. POST /hit — обращение с другим ip и другим uri
# ---------------------------------------------------------------------------
echo "3) POST /hit — обращение к другому uri, другой ip"
HIT3_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/hit" \
    -H "Content-Type: application/json" \
    -d '{
          "app": "ewm-main-service",
          "uri": "/events/2",
          "ip": "10.0.0.5",
          "timestamp": "2024-01-01 13:00:00"
        }')
HIT3_STATUS=$(echo "$HIT3_RESPONSE" | tail -n1)
check "Код ответа 201 (Created)" "201" "$HIT3_STATUS"
echo

# ---------------------------------------------------------------------------
# 4. POST /hit — некорректный запрос (пустой app) должен вернуть 400
# ---------------------------------------------------------------------------
echo "4) POST /hit — некорректный запрос (app пустой), ожидаем 400"
BAD_HIT_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/hit" \
    -H "Content-Type: application/json" \
    -d '{
          "app": "",
          "uri": "/events/1",
          "ip": "10.0.0.5",
          "timestamp": "2024-01-01 13:00:00"
        }')
BAD_HIT_STATUS=$(echo "$BAD_HIT_RESPONSE" | tail -n1)
check "Код ответа 400 (Bad Request)" "400" "$BAD_HIT_STATUS"
echo

# ---------------------------------------------------------------------------
# 5. GET /stats — без uris, unique=false (по умолчанию)
# ---------------------------------------------------------------------------
echo "5) GET /stats — весь диапазон, без фильтра по uris"
STATS_ALL_RESPONSE=$(curl -s -w "\n%{http_code}" -G "$BASE_URL/stats" \
    --data-urlencode "start=2024-01-01 00:00:00" \
    --data-urlencode "end=2024-12-31 23:59:59")
STATS_ALL_STATUS=$(echo "$STATS_ALL_RESPONSE" | tail -n1)
STATS_ALL_BODY=$(echo "$STATS_ALL_RESPONSE" | sed '$d')
echo "  Ответ: $STATS_ALL_BODY"
check "Код ответа 200 (OK)" "200" "$STATS_ALL_STATUS"

# /events/1 получил 2 запроса (оба с ip=192.163.0.1) -> hits=2 при unique=false
HITS_EVENTS_1=$(echo "$STATS_ALL_BODY" | grep -o '"uri":"/events/1","hits":[0-9]*' | grep -o '[0-9]*$' || echo "")
check "У /events/1 hits=2 (без учёта уникальности)" "2" "${HITS_EVENTS_1:-НЕ НАЙДЕНО}"
echo

# ---------------------------------------------------------------------------
# 6. GET /stats — unique=true
# ---------------------------------------------------------------------------
echo "6) GET /stats — unique=true (учитываем только уникальные ip)"
STATS_UNIQUE_RESPONSE=$(curl -s -w "\n%{http_code}" -G "$BASE_URL/stats" \
    --data-urlencode "start=2024-01-01 00:00:00" \
    --data-urlencode "end=2024-12-31 23:59:59" \
    --data-urlencode "unique=true")
STATS_UNIQUE_STATUS=$(echo "$STATS_UNIQUE_RESPONSE" | tail -n1)
STATS_UNIQUE_BODY=$(echo "$STATS_UNIQUE_RESPONSE" | sed '$d')
echo "  Ответ: $STATS_UNIQUE_BODY"
check "Код ответа 200 (OK)" "200" "$STATS_UNIQUE_STATUS"

# у /events/1 оба хита с одним и тем же ip -> при unique=true hits=1
HITS_UNIQUE_EVENTS_1=$(echo "$STATS_UNIQUE_BODY" | grep -o '"uri":"/events/1","hits":[0-9]*' | grep -o '[0-9]*$' || echo "")
check "У /events/1 hits=1 (только уникальные ip)" "1" "${HITS_UNIQUE_EVENTS_1:-НЕ НАЙДЕНО}"
echo

# ---------------------------------------------------------------------------
# 7. GET /stats — фильтр по конкретному uri
# ---------------------------------------------------------------------------
echo "7) GET /stats — фильтр по uris=/events/2"
STATS_URIS_RESPONSE=$(curl -s -w "\n%{http_code}" -G "$BASE_URL/stats" \
    --data-urlencode "start=2024-01-01 00:00:00" \
    --data-urlencode "end=2024-12-31 23:59:59" \
    --data-urlencode "uris=/events/2")
STATS_URIS_STATUS=$(echo "$STATS_URIS_RESPONSE" | tail -n1)
STATS_URIS_BODY=$(echo "$STATS_URIS_RESPONSE" | sed '$d')
echo "  Ответ: $STATS_URIS_BODY"
check "Код ответа 200 (OK)" "200" "$STATS_URIS_STATUS"
check "В ответе нет /events/1" "true" "$(echo "$STATS_URIS_BODY" | grep -q '/events/1' && echo false || echo true)"
echo

# ---------------------------------------------------------------------------
# 8. GET /stats — некорректный диапазон (start > end) должен вернуть 400
# ---------------------------------------------------------------------------
echo "8) GET /stats — start позже end, ожидаем 400"
BAD_STATS_RESPONSE=$(curl -s -w "\n%{http_code}" -G "$BASE_URL/stats" \
    --data-urlencode "start=2024-12-31 23:59:59" \
    --data-urlencode "end=2024-01-01 00:00:00")
BAD_STATS_STATUS=$(echo "$BAD_STATS_RESPONSE" | tail -n1)
check "Код ответа 400 (Bad Request)" "400" "$BAD_STATS_STATUS"
echo

# ---------------------------------------------------------------------------
# 9. Actuator health — обязательная зависимость по требованиям диплома
# ---------------------------------------------------------------------------
echo "9) GET /actuator/health — проверка Spring Boot Actuator"
HEALTH_RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/actuator/health")
HEALTH_STATUS=$(echo "$HEALTH_RESPONSE" | tail -n1)
HEALTH_BODY=$(echo "$HEALTH_RESPONSE" | sed '$d')
echo "  Ответ: $HEALTH_BODY"
check "Код ответа 200 (OK)" "200" "$HEALTH_STATUS"
echo

# ---------------------------------------------------------------------------
# Итог
# ---------------------------------------------------------------------------
echo -e "${YELLOW}== Итог: $PASS пройдено, $FAIL провалено ==${NC}"
if [ "$FAIL" -gt 0 ]; then
    exit 1
fi
