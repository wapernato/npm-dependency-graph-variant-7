# Этап 5. Визуализатор npm-зависимостей — вариант 7

Практическая работа 2. Выполнены этапы 1–5 на Java 17+. Данные npm
получаются прямыми HTTP-запросами стандартной библиотекой Java. JSON,
SemVer, BFS и топологическая сортировка реализованы самостоятельно.
D2 используется только для рендеринга готового описания графа в SVG.

## Быстрый запуск

Нужен JDK 17+ (java и javac), macOS/Linux или Windows. На macOS/Linux:

```sh
./scripts/install-d2.sh
./run.sh --package is-odd@3.0.1 --ascii --d2 ./tools/d2 --output output/is-odd.svg
./scripts/demo.sh
./scripts/test.sh
```

При нескольких JDK задайте JAVA_HOME. Сборка: `./scripts/build.sh`,
результат в build/classes. Внешних Java-зависимостей, Maven и npm
для работы приложения не требуется. D2 0.9.0 устанавливается локально
в игнорируемую tools/; скрипт поддерживает Linux/macOS amd64/arm64.
Windows: установите Java 17+ и D2 CLI, затем `run.bat --package is-odd@3.0.1
--ascii --d2 C:\path\d2.exe` (одной строкой). Windows-запуск подготовлен,
проверки выполнены на macOS. Интернет нужен только реальному npm-режиму
и первому скачиванию D2. Тестовые графы работают офлайн.

## Все настройки

| Опция | Значение и поведение |
| --- | --- |
| --package NAME[@VERSION] | Обязательное имя, в т. ч. @scope/name; по умолчанию latest |
| --repository URL/FILE | https://registry.npmjs.org; с --test — путь к JSON-графу |
| --test | Локальный тестовый репозиторий вместо сети |
| --output FILE.svg | output/graph.svg; рядом сохраняется FILE.d2 |
| --ascii | Дополнительно вывести ASCII-дерево |
| --depth N | Максимальная глубина >=0, по умолчанию 8, корень 0 |
| --filter TEXT | Исключить имена с подстрокой, пустая строка выключает фильтр |
| --mode config | Все настройки в формате ключ=значение (этап 1) |
| --mode direct | Прямые зависимости с диапазонами (этап 2) |
| --mode graph | Рёбра графа без рендеринга (этап 3) |
| --mode order | Порядок «зависимость раньше потребителя» (этап 4) |
| --mode render | D2 + SVG; режим по умолчанию (этап 5) |
| --d2 PATH | Исполняемый файл D2, по умолчанию d2 из PATH |
| --help | Справка, код 0 |

Все опции независимы, кроме выбора локального файла через --test.
Повторы, неизвестные опции, отсутствующие/неверные значения, HTTP,
формат JSON, отсутствующие пакеты и ошибки рендеринга дают stderr и код 2.
Пути отсчитываются от текущей папки; пути с пробелами заключайте в кавычки.
При ошибке D2 старый SVG сохраняется, D2 остаётся для диагностики.

## Демонстрации и защита

```sh
./run.sh --package A --test --repository examples/diamond.json --mode graph --ascii
./run.sh --package A --test --repository examples/chain.json --depth 1 --mode graph
./run.sh --package A --test --repository examples/diamond.json --filter B --mode graph
./run.sh --package A --test --repository examples/diamond.json --mode order
./run.sh --package A --test --repository examples/cycle.json --mode order
./run.sh --package A --test --repository examples/cycle.json --d2 ./tools/d2 --ascii
./run.sh --package is-even@1.0.0 --d2 ./tools/d2 --output output/is-even.svg --ascii
./run.sh --package ansi-styles@4.3.0 --d2 ./tools/d2 --output output/ansi-styles.svg
```

Тестовый формат: `{"A":["B","C"],"B":[],"C":[]}`. Все имена — большие
латинские буквы; каждый узел объявлен явно. Проверяются ссылки и повторы.
BFS выполняется без рекурсии, посещение по имени@версии. Глубина считается
по кратчайшему пути. На границе зависимости не раскрываются и ставится
метка depth limit. Фильтр удаляет узел и обход через него; если корень
совпадает с фильтром — ошибка. Циклические рёбра сохраняются, повторные
узлы в ASCII не разворачиваются. Топологическая сортировка Кана сообщает
цикл вместо неверного порядка; для среза по глубине выводит предупреждение.

## Проверки и сравнение

`./scripts/test.sh`: 77 проверок CLI, JSON, SemVer, HTTP-реестра на localhost,
кеша, aliases, BFS, глубины, фильтра, циклов, порядка, D2 и сохранности SVG
при ошибке. Внешний интернет для этих проверок не нужен.

Три реальные демонстрации: is-odd@3.0.1 (2 узла), is-even@1.0.0 (5),
ansi-styles@4.3.0 (3). SVG созданы настоящим D2, просмотрены; все узлы,
версии и рёбра совпали с `npm ls --all --json`. Порядок проверен по каждому
ребру. Отличие порядка размещения npm объяснено в [отчёте](docs/comparison.md).
Воспроизведение независимой проверки: `./scripts/compare-npm.sh`
(только здесь нужны Node.js и npm; установка в отдельной временной папке,
скрипты устанавливаемых пакетов не исполняются).

## Реализация и ограничения

| Класс / функции | Назначение |
| --- | --- |
| Options.parse/get/flag/depth/describe | Настройки и валидация |
| Json.parse/object | Строгий JSON без сторонних библиотек |
| Version.parse/compareTo, Ranges.select/matches | Версии, теги и npm-диапазоны |
| Repository.resolve/reference, Registry.resolve | HTTP-метаданные, кеш и aliases |
| TestRepository.resolve | Локальный проверяемый граф |
| Graph.build/describe/ascii | BFS, список рёбер и ASCII без рекурсии |
| LoadOrder.of | Детерминированный порядок на DAG |
| Diagram.text/render | D2, вызов официального CLI и запись SVG |
| Main.main | Выбор режима и коды ошибок |

Учитывается dependencies, без dev/peer/optional, lifecycle, платформы и
lockfile. Поддержаны точные версии, теги, ^, ~, wildcard, сравнения,
AND/OR, hyphen и prerelease. file/git/workspace-ссылки отклоняются.
Версия выбирается максимально подходящая независимо для ребра;
не реализуются алгоритмы установки/hoisting/deduplication npm.

## Codespaces

[Открыть в GitHub Codespaces](https://codespaces.new/wapernato/npm-dependency-graph-variant-7?quickstart=1).

Конфигурация .devcontainer устанавливает Java 17, D2 и запускает тесты.
В репозитории GitHub: Code → Codespaces → Create.
В терминале: `./scripts/demo.sh`, затем откройте SVG из папки output/.
На локальном компьютере достаточно браузера и доступа к репозиторию.
Облачный запуск ещё не проверен. Репозиторий проекта публичный:
https://github.com/wapernato/npm-dependency-graph-variant-7 .

## Пять отдельных этапов

- [Этап 1](docs/stage1/README.md): настройки.
- [Этап 2](docs/stage2/README.md): npm HTTP и версии.
- [Этап 3](docs/stage3/README.md): BFS и тестовый граф.
- [Этап 4](docs/stage4/README.md): порядок загрузки.
- [Этап 5](docs/stage5/README.md): D2/SVG, тесты, сравнение.

Каждый этап — отдельный Conventional Commit со своим корневым README.
Команды: `git log --oneline --reverse`, `git checkout <хеш>`,
возврат: `git checkout main`. Исходная методичка: ИКБО-15-25-2.pdf,
страницы 26–28. Исходники в src/, проверки в tests/, артефакты игнорируются.
