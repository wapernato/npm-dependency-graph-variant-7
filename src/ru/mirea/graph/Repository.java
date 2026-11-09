package ru.mirea.graph;

import java.util.Map;

/** Источник прямых зависимостей, независимый от обхода графа. */
public interface Repository {
    /** Конкретный пакет с зафиксированной версией и диапазонами зависимостей. */
    record Package(String name, String version, Map<String, String> dependencies) {
        /** Уникальный ключ узла: одинаковые имена разных версий не сливаются. */
        public String id() { return version.isEmpty() ? name : name + "@" + version; }
    }

    /** Разрешить имя и диапазон версии в конкретный пакет. */
    Package resolve(String name, String range) throws Exception;

    /** Отделить версию от имени, сохраняя scoped-пакеты. */
    static String[] reference(String reference) {
        int separator = reference.lastIndexOf('@');
        return separator > 0 ? new String[]{reference.substring(0, separator), reference.substring(separator + 1)}
                : new String[]{reference, "latest"};
    }
}
