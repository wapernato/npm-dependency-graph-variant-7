package ru.mirea.graph;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Отдельные JVM: коды ошибок, stdout/stderr и атомарность SVG при ошибке D2. */
final class CliChecks {
    private CliChecks() { }

    static void run() throws Exception {
        TestSuite.equal(0, code("--help"));
        TestSuite.equal(2, code("--package", "foo", "--depth", "-1"));
        TestSuite.equal(0, code("--package", "foo", "--mode", "config"));
        TestSuite.equal(2, code("--package", "A", "--test", "--repository", "examples/missing.json"));
        TestSuite.equal(0, code("--package", "A", "--test", "--repository", "examples/cycle.json",
                "--mode", "graph", "--ascii"));
        TestSuite.equal(2, code("--package", "A", "--test", "--repository", "examples/cycle.json",
                "--mode", "order"));
        atomicOutput();
        invalidFixtures();
    }

    private static int code(String... args) throws Exception {
        List<String> command = new ArrayList<>(List.of(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("java.class.path"), "ru.mirea.graph.Main"));
        command.addAll(List.of(args));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        if (!process.waitFor(10, TimeUnit.SECONDS)) { process.destroyForcibly(); throw new AssertionError("Timeout"); }
        process.getInputStream().readAllBytes();
        return process.exitValue();
    }

    private static void atomicOutput() throws Exception {
        Path folder = Files.createTempDirectory("graph output with spaces");
        Path svg = folder.resolve("previous.svg");
        Files.writeString(svg, "previous result");
        Options options = Options.parse("--package", "A", "--output", svg.toString(), "--d2", "missing-d2-cli");
        try {
            Diagram.render(GraphChecks.graph("isolated"), options);
            throw new AssertionError("Expected renderer error");
        } catch (IllegalArgumentException expected) {
            TestSuite.equal("previous result", Files.readString(svg));
            TestSuite.equal(true, Files.exists(folder.resolve("previous.d2")));
        } finally {
            try (var files = Files.list(folder)) { for (Path file : files.toList()) { Files.delete(file); } }
            Files.delete(folder);
        }
    }

    private static void invalidFixtures() throws Exception {
        Path file = Files.createTempFile("bad-graph", ".json");
        try {
            for (String body : List.of("{\"a\":[]}", "{\"A\":[\"B\"]}", "{\"A\":{}}")) {
                Files.writeString(file, body);
                try { new TestRepository(file); throw new AssertionError("Invalid fixture accepted"); }
                catch (IllegalArgumentException error) { TestSuite.equal(true, !error.getMessage().isBlank()); }
            }
        } finally { Files.delete(file); }
    }
}
