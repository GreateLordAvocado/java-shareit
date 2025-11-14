# ShareIt

Этот документ описывает архитектуру, сборку и запуск многомодульного проекта ShareIt после разделения на server и gateway, а также примеры использования новых эндпоинтов запросов вещей.

Архитектура

shareit-server (порт 9090) — бизнес-логика, доступ к БД, REST-эндпоинты для приёма запросов от gateway.

shareit-gateway (порт 8080) — контроллеры, валидация входных данных, проксирование запросов к server, (опционально) кэширование.

client → gateway:8080 → server:9090 → DB
Сборка и запуск
# из корня репозитория
mvn clean install


# стартуем сервер, затем гейтвей
cd server && mvn spring-boot:run
cd ../gateway && mvn spring-boot:run
Порты и конфигурация

server/src/main/resources/application.properties

server.port=9090

JDBC spring.datasource.* (по умолчанию Postgres)

gateway/src/main/resources/application.properties

server.port=8080

shareit-server.url=http://localhost:9090

Новые возможности
Запросы вещей (Item Requests)

POST /requests — создать запрос (описание необходимой вещи)

GET /requests — получить свои запросы с ответами

GET /requests/all?from=&size= — получить чужие запросы (постранично)

GET /requests/{id} — получить один запрос с ответами

Привязка вещи к запросу

POST /items поддерживает опциональное поле requestId — отвечает на существующий запрос.

Валидация (gateway)

Требуется заголовок X-Sharer-User-Id.

POST /requests — description @NotBlank.

Пагинация from >= 0, size > 0.

Единый обработчик ошибок: JSON { "error": "..." }.

Примеры запросов (через gateway)

Везде используйте заголовок -H "X-Sharer-User-Id: <id>"

Создать запрос
curl -sS -X POST http://localhost:8080/requests \
-H 'Content-Type: application/json' \
-H 'X-Sharer-User-Id: 1' \
-d '{"description":"Нужна ударная дрель"}'
Свои запросы
curl -sS -H 'X-Sharer-User-Id: 1' http://localhost:8080/requests
Чужие запросы (постранично)
curl -sS -H 'X-Sharer-User-Id: 1' \
'http://localhost:8080/requests/all?from=0&size=10'
Запрос по id
curl -sS -H 'X-Sharer-User-Id: 2' http://localhost:8080/requests/5
Создать вещь в ответ на запрос
curl -sS -X POST http://localhost:8080/items \
-H 'Content-Type: application/json' \
-H 'X-Sharer-User-Id: 10' \
-d '{
"name":"Дрель Makita",
"description":"1200W",
"available":true,
"requestId":5
}'
Тесты

Интеграционные (server): сервисы ItemRequestService, ItemService (поддержка requestId).

MockMvc (gateway): RequestController, ItemController (валидация заголовков и параметров).

@JsonTest: ItemRequestDto.

Запуск всех тестов: