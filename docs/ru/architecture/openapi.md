# Архитектура `studio-openapi`

Язык: **Русский** · [English](../../en/architecture/openapi.md)

Статус: публичные контракты, граница version adapter и syntax layer для
YAML/JSON реализованы на 2026-07-30. Определение версии, структурная
валидация и маппинг OpenAPI 3.1 запланированы.

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

## План реализации

1. Syntax layer для YAML/JSON. Реализован.
2. Version detector и orchestration импорта/экспорта.
3. Зафиксированная структурная схема OpenAPI 3.1 и validator.
4. OpenAPI 3.1 decoder.
5. OpenAPI 3.1 encoder.
6. Интеграция строгой семантической валидации.
7. Round-trip и интеграционные тесты на fixtures.

Официальные схемы будут храниться как версионированные resources. Во время
работы приложения они не скачиваются.

## Тестирование

Тесты фиксируют инварианты options, diagnostics, results, registry, строгого
parsing, точности чисел и round-trip JSON/YAML. Каждый следующий слой должен
добавлять valid, invalid и round-trip fixtures.

Запуск:

```powershell
.\gradlew.bat :studio-openapi:test
```
