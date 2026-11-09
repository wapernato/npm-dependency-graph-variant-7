package ru.mirea.graph;

import java.util.List;
import java.util.Map;

/** Строгий JSON и правила диапазонов npm. */
final class ParserChecks {
    private ParserChecks() { }

    static void run() {
        TestSuite.equal("Привет\n", Json.object(Json.parse("{\"x\":\"Привет\\n\"}")).get("x"));
        TestSuite.equal(3, ((List<?>) Json.parse("[true,null,1.5]")).size());
        TestSuite.equal(Map.of(), Json.parse("{}"));
        for (String text : List.of("{", "[1,]", "{\"x\":1,\"x\":2}", "01", "true false", "\"\\q\"")) {
            TestSuite.rejects(() -> Json.parse(text));
        }
        check("^1.2.3", "1.9.0", true);
        check("^1.2.3", "2.0.0", false);
        check("^0.2.3", "0.3.0", false);
        check("^0.0.3", "0.0.4", false);
        check("~1.2", "1.2.99", true);
        check("~1.2", "1.3.0", false);
        check("1.x", "1.9.0", true);
        check(">=1.0 <2", "1.9.0", true);
        check("1.0 - 2.3", "2.3.99", true);
        check("1 || 3", "3.1.0", true);
        check("*", "2.0.0-beta.1", false);
        check(">=2.0.0-beta.1 <3", "2.0.0-beta.2", true);
        check("", "1.0.0", true);
        TestSuite.equal("1.10.0", Ranges.select(List.of("1.2.0", "1.10.0", "2.0.0"), "^1"));
        TestSuite.rejects(() -> Ranges.select(List.of("1.0.0"), "^2"));
        TestSuite.equal(-1, Integer.signum(Version.parse("1.0.0-beta.2").compareTo(Version.parse("1.0.0"))));
        TestSuite.equal("@scope/a", Repository.reference("@scope/a@^1")[0]);
        TestSuite.equal("latest", Repository.reference("@scope/a")[1]);
    }

    private static void check(String range, String version, boolean expected) {
        TestSuite.equal(expected, Ranges.matches(Version.parse(version), range));
    }
}
