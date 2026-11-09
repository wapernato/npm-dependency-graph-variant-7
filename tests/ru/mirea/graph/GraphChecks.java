package ru.mirea.graph;

import java.nio.file.Path;
import java.util.List;

/** Проверки BFS, кратчайшей глубины, фильтрации, повторных путей и циклов. */
final class GraphChecks {
    private GraphChecks() { }

    static Graph graph(String file, String... extra) throws Exception {
        var args = new java.util.ArrayList<>(List.of("--package", "A", "--test",
                "--repository", "examples/" + file + ".json"));
        args.addAll(List.of(extra));
        return Graph.build(new TestRepository(Path.of("examples/" + file + ".json")),
                Options.parse(args.toArray(String[]::new)));
    }

    static void run() throws Exception {
        TestSuite.equal(List.of("A", "B", "C", "D"), List.copyOf(graph("diamond").edges().keySet()));
        TestSuite.equal(List.of("B", "C"), graph("diamond").edges().get("A"));
        TestSuite.equal(1, graph("chain", "--depth", "0").edges().size());
        TestSuite.equal(2, graph("chain", "--depth", "1").edges().size());
        TestSuite.equal(true, graph("chain", "--depth", "1").cut().contains("B"));
        TestSuite.equal(List.of("C"), graph("diamond", "--filter", "B").edges().get("A"));
        TestSuite.equal(4, graph("cycle").edges().size());
        TestSuite.equal(List.of("A", "D"), graph("cycle").edges().get("C"));
        TestSuite.equal(true, graph("cycle").ascii().contains("[repeat/cycle]"));
        TestSuite.equal(1, graph("isolated").edges().size());
    }
}
