# Этап 2. Сбор npm-данных — вариант 7

```sh
./run.sh --package is-odd@3.0.1 --mode direct
./run.sh --package @babel/core --mode direct
./scripts/test.sh
```

Java 17+, настройка JDK через JAVA_HOME. См. [этап 1](docs/stage1/README.md)
для всех параметров CLI. `--mode config` сохраняет вывод конфигурации.
`--mode direct` показывает корень и прямые runtime dependencies с диапазонами.
`Registry` получает JSON по HTTP(S), проверяет статус, ограничивает ожидание
и кеширует метаданные на время запуска. Менеджеры пакетов и библиотеки
получения зависимостей не используются. `Json` — собственный строгий парсер.
`Version` и `Ranges` разрешают latest/теги, точные версии, ^, ~, wildcard,
сравнения, AND, OR, hyphen и prerelease. Узел определяется именем и версией.
Поддержаны npm aliases; file/git/workspace URL отклоняются с ошибкой.
Учитывается поле dependencies; dev/peer/optional не входят в анализ.
HTTP/JSON/версии и сетевые ошибки дают stderr и ненулевой код 2.

Запуск и сборка не требуют npm. Тесты: CLI, JSON, сравнение версий и диапазоны.
На этом этапе выводятся только прямые зависимости; транзитивный обход — этап 3.
