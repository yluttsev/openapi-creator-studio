# Архитектура `studio-openapi`

Язык: **Русский** · [English](../../en/architecture/openapi.md)

Статус: публичные контракты, граница version adapter, syntax layer для
YAML/JSON, определение версии и структурная валидация OpenAPI 3.1
реализованы на 2026-07-31. Decoder OpenAPI 3.1 реализован, включая корневые
поля, paths, operations, callbacks, webhooks, schemas, payload, security
schemes и links. Фундамент encoder-а OpenAPI 3.1 реализован; предметные
encoder-ы ещё в работе.

Исходный код находится в
`studio-openapi/src/main/java/ru/luttsev/studio/openapi`.

## Назначение

`studio-openapi` — граница между внешним OpenAPI YAML/JSON и
нормализованной моделью `studio-core`. Модуль понимает синтаксис и
version-specific представление, а core — смысл документа и связи внутри него.

Модуль отвечает за:

- parsing и serialization YAML/JSON;
- определение версии OpenAPI;
- структурную валидацию по зафиксированной официальной схеме;
- преобразование version-specific представления в core и обратно;
- сохранение неизвестных полей и extensions;
- структурированные diagnostics импорта и экспорта.

Он не отвечает за persistence, HTTP, авторизацию, логирование запросов и UI.

## Конвейер обработки

Строгий импорт:

```text
content
-> parse
-> detect version
-> select adapter
-> structural validation
-> decode into core
-> core semantic validation
-> ImportSuccess or ImportFailure
```

Любая ошибка приводит к `ImportFailure` без документа. Warnings операцию не
блокируют.

Экспорт проходит в обратном направлении. Перед serialization проверяются
core-документ и совместимость с целевой версией. Неподдержанные данные нельзя
молча отбрасывать.

Требуется семантический round-trip. Исходные комментарии, YAML anchors,
кавычки, пробелы и форматирование полей не сохраняются.

## Публичные контракты

`OpenApiImporter` принимает текст и `ImportOptions`. Формат можно указать
явно или определить автоматически.

`OpenApiExporter` принимает `OpenApiDocument` и `ExportOptions` с целевым
форматом и версией OpenAPI.

Sealed results:

- `ImportSuccess` или `ImportFailure`;
- `ExportSuccess` или `ExportFailure`;
- `SyntaxSuccess<T>` или `SyntaxFailure<T>` для внутренней границы синтаксиса;
- `DetectedVersion` или `VersionDetectionFailure` для определения версии;
- `StructuralValidationSuccess` или `StructuralValidationFailure` для
  структурной валидации;
- `AdapterSuccess<T>` или `AdapterFailure<T>` для внутренней границы
  адаптера.

Успешный результат может содержать warnings, но не errors. Неуспешный
результат обязан содержать хотя бы одну error.

## Diagnostics

`OpenApiDiagnostic` содержит:

- стабильный `DiagnosticCode`;
- severity `ERROR` или `WARNING`;
- фазу обработки;
- понятное пользователю сообщение;
- `DocumentPath`;
- необязательные номера строки и столбца, начиная с единицы.

Фазы охватывают parsing, определение версии, структурную валидацию, mapping,
семантическую валидацию, совместимость с целевой версией и serialization.

Ожидаемые проблемы документа возвращаются как diagnostics. Неожиданные
программные или инфраструктурные сбои могут подниматься как исключения и
логируются в `studio-app`, а не в этом модуле.

## Граница синтаксического дерева

Parser-specific типы, например Jackson `JsonNode`, не должны попадать в
публичные контракты или version adapters. Общее синтаксическое дерево,
передаваемое адаптерам, — core `ObjectValue` с вложенными `DocumentValue`.

Это позволяет заменить parser и переносить неизвестные значения в
`additionalFields` или `additionalKeywords` без потери их структуры.

`OpenApiSyntaxCodec` объединяет контракты parser и writer. Текущая реализация
`JacksonOpenApiSyntaxCodec` использует Jackson 3.2.1 для строгой обработки
JSON и YAML. Типы Jackson остаются внутренними деталями реализации.

Явный format hint из `ImportOptions` имеет приоритет. Без него содержимое,
которое после необязательного byte-order mark и пробелов начинается с `{` или
`[`, считается JSON; остальное считается YAML. Невалидный JSON не
переинтерпретируется как YAML.

Syntax layer отклоняет пустой ввод, корень не-объект, повторяющиеся поля,
лишнее содержимое и несколько YAML-документов. Числа разбираются без
промежуточного преобразования через `double`. Serialization создаёт читаемый
и предсказуемый JSON или YAML, но не сохраняет исходное форматирование.

## Определение версии

`OpenApiVersionDetector` читает обязательное корневое поле `openapi` из
`ObjectValue`. Реализация по умолчанию принимает строку в формате
`major.minor.patch` и сохраняет точное значение в `OpenApiVersion`.

Отсутствующее поле, значение не строкового типа или неверный формат дают
структурированную error diagnostic по пути `/openapi`. Детектор не решает,
поддерживается ли версия. Это ответственность `OpenApiVersionAdapterRegistry`,
поэтому корректно записанную будущую версию можно определить, а затем сообщить,
что адаптера для неё нет.

## Структурная валидация

`OpenApiStructuralValidator` проверяет синтаксическое дерево до
version-specific маппинга. `OpenApiStructuralSchemaRegistry` выбирает ровно
один schema provider для определённой версии, поэтому поддержка OpenAPI 3.0 и
3.2 будет добавляться отдельно.

Текущий provider поддерживает все patch-версии OpenAPI 3.1. Он использует
зафиксированный официальный ресурс `schema-base` от `2025-11-23` и базовый
диалект OpenAPI 3.1, поэтому проверяются и Schema Objects, и остальная
структура документа. Пользовательские значения `jsonSchemaDialect` не входят
в первый релиз.

Все схемы и их транзитивные OpenAPI-зависимости упакованы в приложение.
Валидация детерминирована и не выполняет сетевых запросов во время работы.
Скомпилированные схемы кешируются по ID корневой схемы. Ожидаемые нарушения
возвращаются как стабильные diagnostics `openapi.structure.*` с
`DocumentPath`; неподдерживаемая версия указывается по пути `/openapi`.

## Version adapters

`OpenApiVersionAdapter` — стратегия для одного семейства версий. Она
определяет поддержку версии и предоставляет:

- `decode(ObjectValue, OpenApiVersion)`;
- `encode(OpenApiDocument, OpenApiVersion)`.

`OpenApiVersionAdapterRegistry` выбирает ровно один подходящий адаптер.
Отсутствие поддержки — ожидаемая diagnostic импорта или экспорта. Несколько
подходящих адаптеров означают ошибку конфигурации.

Первая реализация будет предназначена для OpenAPI 3.1.2. OpenAPI 3.0 и 3.2
добавляются отдельными адаптерами, а не условными ветвлениями по всему модулю.

## Decoder OpenAPI 3.1

`OpenApi31Decoder` преобразует структурно валидный `ObjectValue` в
нормализованный core `OpenApiDocument`. JSON Schema повторно не запускается.
При прямом вызове decoder безопасное типизированное чтение всё равно вернёт
mapping diagnostics вместо ошибки приведения типа.

`DecodeContext` хранит текущий `DocumentPath` и использует общий сборщик
diagnostics для всех дочерних контекстов. `ObjectValueReader` централизует
типизированное чтение полей. Небольшие предметные decoders преобразуют
отдельные семейства моделей, а `AdditionalFieldsMapper` сохраняет каждое поле,
которое ещё не было обработано decoder.

Текущий вертикальный срез охватывает корневую версию, `jsonSchemaDialect`,
`Info`, `Contact`, `License`, корневой `paths`, Path Item Objects, стандартные
HTTP operations, а также schema-, payload-, security scheme- и link-секции
`components`. `SchemaDecoder` рекурсивно преобразует логические и объектные
схемы, включая properties, items, ограничения, композицию, discriminator,
ссылки и неизвестные ключевые слова JSON Schema. Одиночный `example` и массив
`examples` нормализуются в список examples модели core.

Payload-слой преобразует Example, Parameter, Header, Request Body, Response,
Media Type и Encoding Objects. Общий `ReferenceOrDecoder` различает inline-
значения и Reference Objects, а `PayloadDecoder` координирует рекурсивный граф
Media Type, Encoding и Header без циклических зависимостей конструкторов.
`LinkDecoder` преобразует как links внутри responses, так и общие links из
components. Переиспользуемые декодеры преобразуют Server, Server Variable,
External Documentation и Security Requirement Objects на path- и
operation-уровнях. `SecuritySchemeDecoder` поддерживает API key, HTTP, mutual
TLS, OAuth 2.0 и OpenID Connect вместе со всеми OAuth flows из OpenAPI 3.1.
Ответы операции преобразуются в расширяемую core-модель `Responses`, поэтому
extensions объекта responses сохраняются рядом с типизированными status-code
записями. В OpenAPI 3.1 media types декодируются внутри `content`;
`components.mediaTypes` намеренно не принимается, потому что это поле более
новой версии OpenAPI.

Callbacks операций, корневые webhooks и секции callbacks и pathItems в
components преобразуются в типизированные core-модели. Callback expressions
рекурсивно переиспользуют `PathItemDecoder`, а callback-ссылки — общий
`ReferenceOrDecoder`. Расширения Callback Object сохраняются в
`Callback.additionalFields`. Все стандартные корневые поля OpenAPI 3.1
преобразуются в типизированные core-модели. Неизвестные корневые поля и
extensions остаются в `OpenApiDocument.additionalFields`. Поля Tag из OpenAPI
3.2 — `summary`, `parent` и `kind` — адаптер 3.1 не заполняет; при прямом вызове
decoder они сохраняются как дополнительные поля.

## Encoder OpenAPI 3.1

Encoder преобразует нормализованную core-модель обратно в общее
синтаксическое дерево `ObjectValue`. `EncodeContext` хранит целевую версию и
текущий `DocumentPath`, а также разделяет diagnostics совместимости между
дочерними контекстами. `ObjectValueBuilder` обеспечивает детерминированную
сборку объектов и пропускает отсутствующие необязательные значения.

Неизвестные поля и extensions переносятся из `additionalFields` без изменения
их представления `DocumentValue`. Если ключ одновременно является стандартным
полем OpenAPI 3.1 и дополнительным полем, это ошибка совместимости:
типизированное свойство core остаётся источником истины, а конфликтующее
дополнительное значение не подставляется молча. Предметные encoder-ы schemas,
payload, paths, operations и корневого документа будут добавлены следующими
срезами.

## План реализации

1. Syntax layer для YAML/JSON. Реализован.
2. Version detector. Реализован.
3. Зафиксированная структурная схема OpenAPI 3.1 и validator. Реализованы.
4. OpenAPI 3.1 decoder. Реализован.
5. OpenAPI 3.1 encoder. Фундамент реализован; предметные encoder-ы в работе.
6. Orchestration импорта/экспорта.
7. Интеграция строгой семантической валидации.
8. Round-trip и интеграционные тесты на fixtures.

Официальные схемы будут храниться как версионированные resources. Во время
работы приложения они не скачиваются.

## Тестирование

Тесты фиксируют инварианты options, diagnostics, results, registry, строгого
parsing, определения версии, структурной валидации, точности чисел и round-trip
JSON/YAML. Каждый следующий слой должен добавлять valid, invalid и round-trip
fixtures.

Запуск:

```powershell
.\gradlew.bat :studio-openapi:test
```
