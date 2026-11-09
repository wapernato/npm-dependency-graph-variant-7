package ru.mirea.graph;

import java.net.URI;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Проверяемая конфигурация CLI варианта 7. */
public record Options(Map<String, String> values) {
    private static final Set<String> FLAGS = Set.of("--test", "--ascii", "--help");
    private static final Set<String> KEYS = Set.of("--package", "--repository", "--output",
            "--depth", "--filter", "--mode", "--d2");

    /** Разобрать независимые опции и отклонить дубликаты. */
    public static Options parse(String... args) {
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            String key = args[i];
            if ((!FLAGS.contains(key) && !KEYS.contains(key)) || values.containsKey(key)) {
                throw new IllegalArgumentException("Неизвестный или повторный параметр: " + key);
            }
            String value = "true";
            if (!FLAGS.contains(key)) {
                if (++i == args.length || args[i].startsWith("--")) {
                    throw new IllegalArgumentException("Нет значения: " + key);
                }
                value = args[i];
            }
            values.put(key, value);
        }
        Options options = new Options(Map.copyOf(values));
        if (!options.flag("--help")) { options.validate(); }
        return options;
    }

    private void validate() {
        if (get("--package").isBlank() || !get("--package").matches("[@A-Za-z0-9_./+*^~<>=| -]+")) {
            throw new IllegalArgumentException("Требуется корректный --package");
        }
        if (depth() < 0) { throw new IllegalArgumentException("--depth должен быть >= 0"); }
        if (!Set.of("config", "direct", "graph", "order", "render").contains(get("--mode"))) {
            throw new IllegalArgumentException("--mode: config/direct/graph/order/render");
        }
        if (!get("--output").endsWith(".svg")) {
            throw new IllegalArgumentException("--output должен иметь расширение .svg");
        }
        Path.of(get("--output"));
        if (get("--repository").isBlank()) { throw new IllegalArgumentException("Пустой --repository"); }
        if (flag("--test")) { Path.of(get("--repository")); } else { validateUrl(); }
        if (get("--d2").isBlank()) { throw new IllegalArgumentException("Пустой --d2"); }
    }

    private void validateUrl() {
        URI uri = URI.create(get("--repository"));
        if (uri.getScheme() == null || !Set.of("http", "https").contains(uri.getScheme()) || uri.getHost() == null
                || uri.getQuery() != null || uri.getFragment() != null || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("--repository: требуется URL HTTP(S) без query/fragment/пароля");
        }
    }

    /** Получить значение с документированным значением по умолчанию. */
    public String get(String key) {
        return values.getOrDefault(key, switch (key) {
            case "--repository" -> "https://registry.npmjs.org";
            case "--output" -> "output/graph.svg";
            case "--depth" -> "8";
            case "--mode" -> "render";
            case "--d2" -> "d2";
            default -> "";
        });
    }

    /** Присутствует ли переключатель. */
    public boolean flag(String key) { return values.containsKey(key); }

    /** Ограничение глубины: корень находится на уровне 0. */
    public int depth() {
        try { return Integer.parseInt(get("--depth")); }
        catch (NumberFormatException error) { throw new IllegalArgumentException("--depth: требуется целое число"); }
    }

    /** Показать все пользовательские настройки. */
    public String describe() {
        StringBuilder text = new StringBuilder();
        for (String key : new String[]{"--package", "--repository", "--test", "--output", "--ascii",
                "--depth", "--filter", "--mode", "--d2"}) {
            text.append(key.substring(2)).append("=").append(FLAGS.contains(key) ? flag(key) : get(key)).append('\n');
        }
        return text.toString();
    }
}
