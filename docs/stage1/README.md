# Этап 1. Настройки CLI — вариант 7 (работа 2)

Java 17+, только стандартная библиотека. Запуск из корня проекта:

```sh
./run.sh --package express --mode config
./run.sh --help
./scripts/test.sh
```

Параметры: `--package NAME[@VERSION]` обязателен; `--repository` — URL npm
(по умолчанию https://registry.npmjs.org), с `--test` — путь к тестовому графу;
`--output` — SVG (output/graph.svg); `--ascii` — дерево; `--depth` — целое >=0
(8 по умолчанию); `--filter` — исключаемая подстрока (пустая по умолчанию).
Дополнительно `--mode config|direct|graph|order|render` и `--d2 PATH`.
В этом коммите выводятся все настройки как ключ=значение. Неизвестные,
повторные опции, пропущенные значения, неверные URL, глубина, режим и
расширение дают понятную ошибку и код 2. `--help` даёт код 0.

`Options.parse/get/flag/depth/describe` разбирают и проверяют настройки.
`Main.main` обрабатывает ошибки. Сборка — `scripts/build.sh`, тесты —
`tests/ru/mirea/graph/TestSuite.java`, запуск — `run.sh`. Задайте `JAVA_HOME`,
если JDK 17+ не выбран в PATH. Зависимости и менеджеры пакетов не нужны.
