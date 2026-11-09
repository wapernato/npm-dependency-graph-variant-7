package ru.mirea.graph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Собственный строгий JSON-парсер метаданных npm, без библиотек. */
public final class Json {
    private final String text;
    private int position;
    private Json(String text) { this.text = text; }

    /** Разобрать единственное JSON-значение. */
    public static Object parse(String text) {
        Json reader = new Json(text);
        Object result = reader.value();
        reader.space();
        if (reader.position != text.length()) { throw reader.error("Лишние символы"); }
        return result;
    }

    /** Проверить объект и получить его строковые ключи. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> object(Object value) {
        if (!(value instanceof Map<?, ?>)) { throw new IllegalArgumentException("Ожидался объект JSON"); }
        return (Map<String, Object>) value;
    }

    private Object value() {
        space();
        if (position == text.length()) { throw error("Неожиданный конец"); }
        return switch (text.charAt(position)) {
            case '{' -> map();
            case '[' -> array();
            case '"' -> string();
            case 't' -> literal("true", Boolean.TRUE);
            case 'f' -> literal("false", Boolean.FALSE);
            case 'n' -> literal("null", null);
            default -> number();
        };
    }

    private Map<String, Object> map() {
        position++;
        Map<String, Object> map = new LinkedHashMap<>();
        if (take('}')) { return map; }
        do {
            space();
            String key = string();
            expect(':');
            if (map.containsKey(key)) { throw error("Повтор ключа " + key); }
            map.put(key, value());
        } while (take(','));
        expect('}');
        return map;
    }

    private List<Object> array() {
        position++;
        List<Object> list = new ArrayList<>();
        if (take(']')) { return list; }
        do { list.add(value()); } while (take(','));
        expect(']');
        return list;
    }

    private String string() {
        expect('"');
        StringBuilder result = new StringBuilder();
        while (position < text.length()) {
            char next = text.charAt(position++);
            if (next == '"') { return result.toString(); }
            if (next < ' ') { throw error("Управляющий символ в строке"); }
            result.append(next == '\\' ? escaped() : next);
        }
        throw error("Незавершённая строка");
    }

    private char escaped() {
        if (position == text.length()) { throw error("Незавершённое экранирование"); }
        char next = text.charAt(position++);
        if ("\"/\\".indexOf(next) >= 0) { return next; }
        if (next == 'u') { return unicode(); }
        return control(next);
    }

    private char control(char next) {
        return switch (next) {
            case 'b' -> '\b';
            case 'f' -> '\f';
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            default -> throw error("Неверное экранирование");
        };
    }

    private char unicode() {
        if (position + 4 > text.length()) { throw error("Короткий Unicode escape"); }
        String digits = text.substring(position, position + 4);
        if (!digits.matches("[0-9a-fA-F]{4}")) { throw error("Неверный Unicode escape"); }
        position += 4;
        return (char) Integer.parseInt(digits, 16);
    }

    private Object number() {
        var matcher = java.util.regex.Pattern.compile("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?")
                .matcher(text.substring(position));
        if (!matcher.lookingAt()) { throw error("Неверное значение"); }
        position += matcher.end();
        return new java.math.BigDecimal(matcher.group());
    }

    private Object literal(String word, Object value) {
        if (!text.startsWith(word, position)) { throw error("Неверный литерал"); }
        position += word.length();
        return value;
    }

    private void space() {
        while (position < text.length() && " \t\r\n".indexOf(text.charAt(position)) >= 0) { position++; }
    }

    private boolean take(char symbol) {
        space();
        if (position < text.length() && text.charAt(position) == symbol) { position++; return true; }
        return false;
    }

    private void expect(char symbol) {
        if (!take(symbol)) { throw error("Ожидался символ " + symbol); }
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + ", JSON позиция " + position);
    }
}
