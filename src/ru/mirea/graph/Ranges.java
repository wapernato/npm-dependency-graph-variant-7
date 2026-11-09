package ru.mirea.graph;

import java.util.Collection;
import java.util.Comparator;

/** Собственное разрешение стандартных npm semver-диапазонов. */
public final class Ranges {
    private Ranges() { }

    /** Выбрать максимальную подходящую версию. */
    public static String select(Collection<String> versions, String range) {
        return versions.stream().filter(Ranges::valid).filter(v -> matches(Version.parse(v), range))
                .max(Comparator.comparing(Version::parse))
                .orElseThrow(() -> new IllegalArgumentException("Нет версии для диапазона: " + range));
    }

    private static boolean valid(String version) {
        try { Version.parse(version); return true; }
        catch (IllegalArgumentException error) { return false; }
    }

    /** Проверить OR-объединение наборов ограничений. */
    public static boolean matches(Version version, String range) {
        for (String part : range.split("\\|\\|", -1)) {
            if (group(version, part.trim())) { return true; }
        }
        return false;
    }

    private static boolean group(Version v, String range) {
        if (!v.pre().isEmpty() && !explicitPre(v, range)) { return false; }
        var hyphen = java.util.regex.Pattern.compile("^(\\S+)\\s+-\\s+(\\S+)$").matcher(range);
        if (hyphen.matches()) {
            return v.compareTo(base(hyphen.group(1))) >= 0 && upper(v, hyphen.group(2));
        }
        range = range.replaceAll("([<>=~^]+)\\s+", "$1");
        for (String token : range.split("\\s+")) {
            if (!token.isEmpty() && !token(v, token)) { return false; }
        }
        return true;
    }

    private static boolean explicitPre(Version v, String range) {
        String tuple = v.major() + "." + v.minor() + "." + v.patch() + "-";
        return range.contains(tuple);
    }

    private static boolean token(Version v, String text) {
        var matcher = java.util.regex.Pattern.compile("^(>=|<=|>|<|=|~|\\^)?(.*)$").matcher(text);
        matcher.matches();
        String op = matcher.group(1) == null ? "" : matcher.group(1);
        String raw = matcher.group(2);
        int parts = precision(raw);
        if (parts == 0) { return !op.equals("<") && !op.equals(">"); }
        Version low = base(raw);
        if (java.util.Set.of(">=", ">", "<", "<=").contains(op)) {
            return comparison(v, low, parts, op, raw);
        }
        return bounded(v, low, parts, op);
    }

    private static boolean comparison(Version v, Version low, int parts, String op, String raw) {
        int order = v.compareTo(low);
        return switch (op) {
            case ">=" -> order >= 0;
            case ">" -> parts == 3 ? order > 0 : v.compareTo(next(low, parts)) >= 0;
            case "<" -> order < 0;
            default -> upper(v, raw);
        };
    }

    private static boolean bounded(Version v, Version low, int parts, String op) {
        if (v.compareTo(low) < 0) { return false; }
        return switch (op) {
            case "~" -> v.compareTo(next(low, Math.min(parts, 2))) < 0;
            case "^" -> v.compareTo(caret(low, parts)) < 0;
            default -> parts == 3 ? v.compareTo(low) == 0 : v.compareTo(next(low, parts)) < 0;
        };
    }

    private static Version caret(Version low, int parts) {
        if (low.major() != 0 || parts == 1) { return next(low, 1); }
        return next(low, low.minor() != 0 || parts == 2 ? 2 : 3);
    }

    private static boolean upper(Version v, String raw) {
        int parts = precision(raw);
        if (parts == 0) { return true; }
        Version low = base(raw);
        return parts == 3 ? v.compareTo(low) <= 0 : v.compareTo(next(low, parts)) < 0;
    }

    private static int precision(String raw) {
        String text = raw.replaceFirst("^[v=]", "");
        String core = text.split("[-+]", 2)[0];
        if (!core.isEmpty() && !core.matches("(0|[1-9][0-9]*|[xX*])(\\.(0|[1-9][0-9]*|[xX*])){0,2}")) {
            throw new IllegalArgumentException("Неподдерживаемый диапазон: " + raw);
        }
        int result = 0;
        for (String part : text.split("[.+-]")) {
            if (!part.matches("[0-9]+")) { break; }
            if (++result == 3) { break; }
        }
        if (result == 0 && !text.matches("[xX*]?")) {
            throw new IllegalArgumentException("Неподдерживаемый диапазон: " + raw);
        }
        return result;
    }

    private static Version base(String raw) {
        String text = raw.replaceFirst("^[v=]", "");
        int parts = precision(raw);
        if (parts == 3) { return Version.parse(text); }
        String[] values = text.split("\\.");
        return new Version(parts > 0 ? Integer.parseInt(values[0]) : 0,
                parts > 1 ? Integer.parseInt(values[1]) : 0, 0, "");
    }

    private static Version next(Version v, int parts) {
        return switch (parts) {
            case 1 -> new Version(v.major() + 1, 0, 0, "");
            case 2 -> new Version(v.major(), v.minor() + 1, 0, "");
            default -> new Version(v.major(), v.minor(), v.patch() + 1, "");
        };
    }
}
