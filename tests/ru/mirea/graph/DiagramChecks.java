package ru.mirea.graph;

/** Проверки D2: стабильные узлы, общие зависимости, цикл и глубина. */
final class DiagramChecks {
    private DiagramChecks() { }

    static void run() throws Exception {
        String text = Diagram.text(GraphChecks.graph("diamond"));
        TestSuite.equal(true, text.contains("n0: \"A\""));
        TestSuite.equal(true, text.contains("n1 -> n3"));
        TestSuite.equal(true, text.contains("n2 -> n3"));
        TestSuite.equal(true, Diagram.text(GraphChecks.graph("cycle")).contains("n2 -> n0"));
        TestSuite.equal(true, Diagram.text(GraphChecks.graph("chain", "--depth", "0")).contains("depth limit"));
    }
}
