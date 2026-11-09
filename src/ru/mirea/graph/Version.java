package ru.mirea.graph;

import java.math.BigInteger;
import java.util.regex.Pattern;

/** SemVer: числовые компоненты, prerelease и игнорируемый build metadata. */
public record Version(int major, int minor, int patch, String pre) implements Comparable<Version> {
    private static final Pattern FORMAT = Pattern.compile(
            "[v=]?(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)"
            + "(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z.-]+)?");

    /** Проверить и разобрать конкретную версию. */
    public static Version parse(String text) {
        var match = FORMAT.matcher(text);
        if (!match.matches()) { throw new IllegalArgumentException("Неверная версия: " + text); }
        String pre = match.group(4) == null ? "" : match.group(4);
        for (String part : pre.split("\\.")) {
            if (part.matches("0[0-9]+")) { throw new IllegalArgumentException("Ведущий ноль prerelease"); }
        }
        return new Version(Integer.parseInt(match.group(1)), Integer.parseInt(match.group(2)),
                Integer.parseInt(match.group(3)), pre);
    }

    /** Сравнение по SemVer, не по строковому порядку. */
    @Override
    public int compareTo(Version other) {
        int order = Integer.compare(major, other.major);
        if (order == 0) { order = Integer.compare(minor, other.minor); }
        if (order == 0) { order = Integer.compare(patch, other.patch); }
        return order != 0 ? order : comparePre(pre, other.pre);
    }

    private static int comparePre(String left, String right) {
        if (left.equals(right)) { return 0; }
        if (left.isEmpty()) { return 1; }
        if (right.isEmpty()) { return -1; }
        String[] a = left.split("\\."), b = right.split("\\.");
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            int order = comparePart(a[i], b[i]);
            if (order != 0) { return order; }
        }
        return Integer.compare(a.length, b.length);
    }

    private static int comparePart(String a, String b) {
        boolean an = a.matches("[0-9]+"), bn = b.matches("[0-9]+");
        if (an && bn) { return new BigInteger(a).compareTo(new BigInteger(b)); }
        if (an != bn) { return an ? -1 : 1; }
        return a.compareTo(b);
    }
}
