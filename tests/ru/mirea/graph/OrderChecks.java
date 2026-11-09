package ru.mirea.graph;

import java.util.List;

/** Порядок на цепочке/ромбе/листе/срезе и корректный отказ на цикле. */
final class OrderChecks {
    private OrderChecks() { }

    static void run() throws Exception {
        TestSuite.equal(List.of("D", "B", "C", "A"), LoadOrder.of(GraphChecks.graph("diamond")));
        TestSuite.equal(List.of("D", "C", "B", "A"), LoadOrder.of(GraphChecks.graph("chain")));
        TestSuite.equal(List.of("A"), LoadOrder.of(GraphChecks.graph("isolated")));
        TestSuite.equal(List.of("B", "A"), LoadOrder.of(GraphChecks.graph("chain", "--depth", "1")));
        Graph cycle = GraphChecks.graph("cycle");
        TestSuite.rejects(() -> LoadOrder.of(cycle));
    }
}
