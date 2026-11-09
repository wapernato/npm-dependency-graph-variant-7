# Этап 3. BFS и тестовый репозиторий — вариант 7

```sh
./run.sh --package A --test --repository examples/diamond.json --mode graph --ascii
./run.sh --package A --test --repository examples/cycle.json --mode graph --ascii
./run.sh --package A --test --repository examples/chain.json --depth 1 --mode graph
./run.sh --package A --test --repository examples/diamond.json --filter B --mode graph
./run.sh --package is-odd@3.0.1 --mode graph
./scripts/test.sh
```

Java 17+. Все настройки: [этап 1](docs/stage1/README.md).
`Graph.build` использует очередь BFS без рекурсивных вызовов. Корень имеет
глубину 0; узлы на границе включены, но их зависимости не раскрываются
и помечаются depth limit. При нескольких путях используется кратчайшее
расстояние. Ключ узла — имя@версия. Циклические рёбра сохраняются, повторные
вершины не ставятся в очередь. Фильтр исключает узел и обход через него;
пустой фильтр выключен, совпавший корень даёт ошибку.

`TestRepository` читает JSON вида `{"A":["B"],"B":[]}`. Все вершины
объявляются явно, имена состоят из больших латинских букв. Повторы ключей,
рёбер и отсутствующие ссылки отклоняются. Примеры: цепочка, ромб, цикл,
изолированная вершина. `Graph.describe` печатает рёбра в порядке BFS;
`Graph.ascii` строит дерево итеративно, отмечает повторы/циклы и глубину.
`--mode config/direct` продолжают работать. HTTP/npm: [этап 2](docs/stage2/README.md).
