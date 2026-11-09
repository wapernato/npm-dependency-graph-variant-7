package ru.mirea.graph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/** Детерминированная топологическая сортировка: зависимости раньше потребителя. */
public final class LoadOrder {
    private LoadOrder() { }

    /** Алгоритм Кана без рекурсии. Для цикла корректного линейного порядка нет. */
    public static List<String> of(Graph graph) {
        Map<String, Integer> remaining = new LinkedHashMap<>();
        Map<String, List<String>> consumers = new LinkedHashMap<>();
        graph.edges().keySet().forEach(node -> consumers.put(node, new ArrayList<>()));
        graph.edges().forEach((node, children) -> {
            List<String> unique = children.stream().distinct().toList();
            remaining.put(node, unique.size());
            unique.forEach(child -> consumers.get(child).add(node));
        });
        PriorityQueue<String> ready = new PriorityQueue<>();
        remaining.forEach((node, count) -> { if (count == 0) { ready.add(node); } });
        List<String> order = new ArrayList<>();
        while (!ready.isEmpty()) {
            String node = ready.remove();
            order.add(node);
            for (String consumer : consumers.get(node)) {
                int count = remaining.compute(consumer, (key, value) -> value - 1);
                if (count == 0) { ready.add(consumer); }
            }
        }
        if (order.size() != remaining.size()) {
            List<String> blocked = remaining.keySet().stream().filter(n -> !order.contains(n)).sorted().toList();
            throw new IllegalArgumentException("Цикл: линейный порядок невозможен; блокированы " + blocked);
        }
        return List.copyOf(order);
    }
}
