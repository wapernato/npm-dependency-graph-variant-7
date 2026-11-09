package ru.mirea.graph;

/** Точка входа CLI. Ошибки настроек возвращают код 2. */
public final class Main {
    private Main() { }

    /** Запустить выбранный этап. */
    public static void main(String[] args) {
        try {
            Options options = Options.parse(args);
            if (options.flag("--help")) {
                System.out.println("./run.sh --package NAME[@VERSION] [--repository URL|FILE] [--test]\n"
                        + "[--output graph.svg] [--ascii] [--depth N] [--filter TEXT]\n"
                        + "[--mode config|direct|graph|order|render] [--d2 PATH]");
            } else { execute(options); }
        } catch (Exception error) {
            System.err.println("Ошибка: " + error.getMessage());
            System.exit(2);
        }
    }

    private static void execute(Options options) throws Exception {
        if (options.get("--mode").equals("config")) { System.out.print(options.describe()); return; }
        String[] reference = Repository.reference(options.get("--package"));
        Repository.Package root = new Registry(options.get("--repository")).resolve(reference[0], reference[1]);
        System.out.println(root.id());
        new java.util.TreeMap<>(root.dependencies()).forEach((name, range) -> System.out.println(name + " " + range));
    }
}
