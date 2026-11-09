package ru.mirea.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** BFS без рекурсии: посещённые версии, кратчайшая глубина и сохранённые рёбра циклов. */
public record Graph(String root, Map<String, List<String>> edges, Set<String> cut) {
    /** Получить граф до заданной глубины, исключая совпадающие поддеревья. */
    public static Graph build(Repository repository, Options options) throws Exception {
        String[] ref = Repository.reference(options.get("--package"));
        Repository.Package root = repository.resolve(ref[0], ref[1]);
        if (excluded(root.name(), options)) { throw new IllegalArgumentException("Корень исключён фильтром"); }
        Map<String, List<String>> edges = new LinkedHashMap<>();
        Set<String> cut = new LinkedHashSet<>();
        Map<String, Integer> depths = new LinkedHashMap<>();
        ArrayDeque<Repository.Package> queue = new ArrayDeque<>();
        queue.add(root);
        depths.put(root.id(), 0);
        while (!queue.isEmpty()) {
            Repository.Package current = queue.remove();
            List<String> children = new ArrayList<>();
            edges.put(current.id(), children);
            int depth = depths.get(current.id());
            if (depth == options.depth()) {
                if (!current.dependencies().isEmpty()) { cut.add(current.id()); }
                continue;
            }
            for (var dependency : new TreeMap<>(current.dependencies()).entrySet()) {
                if (excluded(dependency.getKey(), options)) { continue; }
                Repository.Package child = repository.resolve(dependency.getKey(), dependency.getValue());
                if (excluded(child.name(), options)) { continue; }
                children.add(child.id());
                if (depths.putIfAbsent(child.id(), depth + 1) == null) { queue.add(child); }
            }
        }
        return new Graph(root.id(), edges, cut);
    }

    private static boolean excluded(String name, Options options) {
        String filter = options.get("--filter");
        return !filter.isEmpty() && name.contains(filter);
    }

    /** Детерминированный список рёбер; листья тоже выводятся. */
    public String describe() {
        StringBuilder text = new StringBuilder();
        edges.forEach((node, children) -> text.append(node).append(" -> ")
                .append(String.join(", ", children)).append(cut.contains(node) ? " [depth limit]" : "").append('\n'));
        return text.toString();
    }

    /** ASCII-дерево без рекурсии; повторные узлы отмечаются и не разворачиваются снова. */
    public String ascii() {
        record Item(String name, String indent) { }
        ArrayDeque<Item> stack = new ArrayDeque<>();
        Set<String> shown = new LinkedHashSet<>();
        StringBuilder text = new StringBuilder();
        stack.push(new Item(root, ""));
        while (!stack.isEmpty()) {
            Item item = stack.pop();
            boolean fresh = shown.add(item.name());
            text.append(item.indent()).append(item.name());
            if (!fresh) { text.append(" [repeat/cycle]"); }
            if (cut.contains(item.name())) { text.append(" [depth limit]"); }
            text.append('\n');
            if (!fresh) { continue; }
            List<String> children = edges.get(item.name());
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.push(new Item(children.get(i), item.indent() + "  +- "));
            }
        }
        return text.toString();
    }
}
