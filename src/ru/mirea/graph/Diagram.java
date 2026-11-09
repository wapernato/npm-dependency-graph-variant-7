package ru.mirea.graph;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Экспорт D2 и рендер SVG официальным CLI D2, отдельно от получения зависимостей. */
public final class Diagram {
    private Diagram() { }

    /** Создать D2 со стабильными идентификаторами и безопасными строковыми метками. */
    public static String text(Graph graph) {
        Map<String, String> ids = new LinkedHashMap<>();
        StringBuilder result = new StringBuilder("direction: down\n");
        for (String node : graph.edges().keySet()) {
            String id = "n" + ids.size();
            ids.put(node, id);
            String label = node + (graph.cut().contains(node) ? " [depth limit]" : "");
            result.append(id).append(": ").append(quote(label))
                    .append(" {style.fill: \"#ffffff\"; style.stroke: \"#333333\"; style.font-color: \"#222222\"}\n");
        }
        graph.edges().forEach((from, children) -> children.stream().distinct().forEach(to ->
                result.append(ids.get(from)).append(" -> ").append(ids.get(to))
                        .append(" {style.stroke: \"#444444\"}\n")));
        return result.toString();
    }

    private static String quote(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
    }

    /** Записать D2 и SVG. Не заменять старый SVG при ошибке внешнего инструмента. */
    public static void render(Graph graph, Options options) throws Exception {
        Path output = Path.of(options.get("--output")).toAbsolutePath();
        Files.createDirectories(output.getParent());
        String filename = output.getFileName().toString();
        Path source = output.resolveSibling(filename.substring(0, filename.length() - 4) + ".d2");
        Files.writeString(source, text(graph));
        Path temporary = Files.createTempFile(output.getParent(), ".graph-", ".svg");
        Path log = Files.createTempFile(output.getParent(), ".d2-", ".log");
        try {
            invoke(options.get("--d2"), source, temporary, log);
            if (!Files.readString(temporary).contains("<svg")) {
                throw new IllegalArgumentException("D2 не создал SVG");
            }
            Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("D2: " + source + "\nSVG: " + output);
        } finally {
            Files.deleteIfExists(temporary);
            Files.deleteIfExists(log);
        }
    }

    private static void invoke(String executable, Path source, Path target, Path log) throws Exception {
        Process process;
        try {
            process = new ProcessBuilder(executable, "--layout", "dagre", "--theme", "0",
                    source.toString(), target.toString()).redirectErrorStream(true)
                    .redirectOutput(log.toFile()).start();
        } catch (java.io.IOException error) {
            throw new IllegalArgumentException("D2 не найден: запустите ./scripts/install-d2.sh или задайте --d2 PATH");
        }
        if (!process.waitFor(60, TimeUnit.SECONDS)) {
            process.destroyForcibly().waitFor();
            throw new IllegalArgumentException("Превышено время рендеринга D2 (60 секунд)");
        }
        if (process.exitValue() != 0) {
            throw new IllegalArgumentException("Ошибка D2: " + Files.readString(log));
        }
    }
}
