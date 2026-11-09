package ru.mirea.graph;

/** Проверки без внешних зависимостей, работают без -ea. */
public final class TestSuite {
    private static int count;
    private TestSuite() { }

    /** Запуск всех проверок этапа. */
    public static void main(String[] args) throws Exception {
        equal(8, Options.parse("--package", "foo").depth());
        equal(0, Options.parse("--package", "A", "--depth", "0").depth());
        for (String[] bad : new String[][]{{}, {"--package"}, {"--bad"},
                {"--package", "A", "--depth", "-1"}, {"--package", "A", "--depth", "x"},
                {"--package", "A", "--output", "x.png"}, {"--package", "A", "--mode", "x"},
                {"--package", "A", "--repository", "ftp://host"}, {"--package", "A", "--ascii", "--ascii"}}) {
            rejects(() -> Options.parse(bad));
        }
        System.out.println("PASS: " + count);
    }

    static void equal(Object expected, Object actual) {
        count++;
        if (!expected.equals(actual)) { throw new AssertionError(expected + " != " + actual); }
    }

    static void rejects(Runnable action) {
        count++;
        try { action.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Expected error");
    }
}
