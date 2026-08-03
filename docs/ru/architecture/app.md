# Архитектура `studio-app`

Язык: **Русский** · [English](../../en/architecture/app.md)

`studio-app` — Spring Boot composition root и прикладной слой. Он публикует REST API,
координирует `studio-core` и `studio-openapi` и подключает заменяемые инфраструктурные
адаптеры.

## REST-контракт

REST API проектируется contract-first. Его документ OpenAPI 3.1 находится в
`studio-app/src/main/resources/static/openapi/studio-api.yaml`. OpenAPI Generator создаёт
API-интерфейсы и транспортные модели в `build/generated/openapi`; это артефакты сборки,
которые не коммитятся. Ручные контроллеры реализуют сгенерированные интерфейсы.

Сгенерированные транспортные типы не используются в `studio-core`. Прикладной слой явно
преобразует generated DTO в core-типы и обратно.

## Workspace документов

`DocumentWorkspace` — прикладной порт, владеющий открытыми сессиями редактора. Сессия
содержит UUID, неотрицательную revision и один изменяемый граф `OpenApiDocument` из core.
Начальная revision равна `0`.

В первом релизе используется `InMemoryDocumentWorkspace`. Порт создаёт, находит и удаляет
сессии, а также атомарно выполняет `DocumentCommand` над документом. Для команд применяется
optimistic concurrency:

- переданная revision должна совпадать с текущей;
- успешная core-команда увеличивает revision ровно на один;
- отклонённая core-команда не изменяет revision;
- устаревшая revision приводит к `RevisionConflictException` до выполнения команды.

Команды каждого сохранённого документа выполняются последовательно и независимо от других
документов. Concurrent map защищает только коллекцию сессий. Workspace получает владение
документом, переданным в `create`; вызывающий код не должен изменять этот граф в обход
workspace.

Порт не привязан к способу хранения. В будущем persistent-адаптер сможет реализовать те же
операции через транзакции БД или распределённый optimistic locking без изменения REST-
контракта и core-модели.

## Следующие прикладные слои

Далее будут добавлены document lifecycle services, REST/core-мапперы, типизированные
command handlers, ручная валидация и экспорт, контроллеры и преобразование ошибок в
Problem Details.
