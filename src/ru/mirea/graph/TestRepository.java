package ru.mirea.graph;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/** Строгий локальный граф: JSON-объект вида {"A":["B"],"B":[]}. */
public final class TestRepository implements Repository {
    private final Map<String, Package> packages = new TreeMap<>();

    /** Прочитать и проверить все вершины и ссылки до начала обхода. */
    public TestRepository(Path file) throws Exception {
        Map<String, Object> graph = Json.object(Json.parse(Files.readString(file)));
        for (var entry : graph.entrySet()) {
            validName(entry.getKey());
            if (!(entry.getValue() instanceof java.util.List<?> list)) {
                throw new IllegalArgumentException("Ожидался массив зависимостей: " + entry.getKey());
            }
            Map<String, String> dependencies = new TreeMap<>();
            for (Object dependency : list) {
                if (!(dependency instanceof String name)) { throw new IllegalArgumentException("Имя не строка"); }
                validName(name);
                if (!graph.containsKey(name)) { throw new IllegalArgumentException("Неизвестная вершина: " + name); }
                if (dependencies.put(name, "") != null) { throw new IllegalArgumentException("Повтор ребра: " + name); }
            }
            packages.put(entry.getKey(), new Package(entry.getKey(), "", Map.copyOf(dependencies)));
        }
    }

    private static void validName(String name) {
        if (!name.matches("[A-Z]+")) { throw new IllegalArgumentException("Тестовые имена: большие латинские буквы"); }
    }

    @Override
    public Package resolve(String name, String range) {
        Package result = packages.get(name);
        if (result == null) { throw new IllegalArgumentException("Неизвестный пакет: " + name); }
        return result;
    }
}
