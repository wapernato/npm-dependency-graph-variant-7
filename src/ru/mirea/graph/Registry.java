package ru.mirea.graph;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/** Прямое чтение HTTP JSON npm registry без npm и сторонних библиотек. */
public final class Registry implements Repository {
    private final String base;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL).build();
    private final Map<String, Map<String, Object>> cache = new HashMap<>();

    /** Настроить URL реестра (включая совместимые частные зеркала). */
    public Registry(String base) { this.base = base.replaceAll("/+$", ""); }

    @Override
    public Package resolve(String name, String range) throws Exception {
        if (range.startsWith("npm:")) {
            String[] alias = Repository.reference(range.substring(4));
            return resolve(alias[0], alias[1]);
        }
        Map<String, Object> metadata = metadata(name);
        Map<String, Object> versions = Json.object(metadata.get("versions"));
        Map<String, Object> tags = Json.object(metadata.getOrDefault("dist-tags", Map.of()));
        String version = tags.containsKey(range) ? tags.get(range).toString() : Ranges.select(versions.keySet(), range);
        Map<String, Object> manifest = Json.object(versions.get(version));
        Map<String, String> dependencies = new TreeMap<>();
        for (var entry : Json.object(manifest.getOrDefault("dependencies", Map.of())).entrySet()) {
            if (!(entry.getValue() instanceof String)) {
                throw new IllegalArgumentException("Неверная зависимость: " + entry.getKey());
            }
            dependencies.put(entry.getKey(), entry.getValue().toString());
        }
        return new Package(name, version, Map.copyOf(dependencies));
    }

    private Map<String, Object> metadata(String name) throws Exception {
        if (cache.containsKey(name)) { return cache.get(name); }
        String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/" + encoded))
                .timeout(Duration.ofSeconds(30)).header("Accept", "application/vnd.npm.install-v1+json").GET().build();
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IllegalArgumentException("npm HTTP " + response.statusCode() + ": " + name);
        }
        Map<String, Object> result = Json.object(Json.parse(response.body()));
        cache.put(name, result);
        return result;
    }
}
