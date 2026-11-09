package ru.mirea.graph;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/** Реальные HTTP-запросы к локальному реестру: версии, теги, кеш, alias и ошибки. */
final class RegistryChecks {
    private RegistryChecks() { }

    static void run() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            String path = exchange.getRequestURI().getPath();
            String body = path.equals("/broken") ? "{" : metadata();
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(path.equals("/missing") ? 404 : 200, bytes.length);
            try (var output = exchange.getResponseBody()) { output.write(bytes); }
        });
        server.start();
        try { check(server.getAddress().getPort(), requests); }
        finally { server.stop(0); }
    }

    private static void check(int port, AtomicInteger requests) throws Exception {
        Registry registry = new Registry("http://127.0.0.1:" + port);
        TestSuite.equal("foo@1.0.0", registry.resolve("foo", "latest").id());
        TestSuite.equal("foo@1.2.0", registry.resolve("foo", "^1").id());
        TestSuite.equal(1, requests.get());
        TestSuite.equal("bar@1.2.0", registry.resolve("alias", "npm:bar@^1").id());
        TestSuite.equal("@scope/foo@1.0.0", registry.resolve("@scope/foo", "latest").id());
        TestSuite.equal("^1", registry.resolve("foo", "latest").dependencies().get("bar"));
        try { registry.resolve("missing", "latest"); throw new AssertionError(); }
        catch (IllegalArgumentException error) { TestSuite.equal(true, error.getMessage().contains("404")); }
        try { registry.resolve("broken", "latest"); throw new AssertionError(); }
        catch (IllegalArgumentException error) { TestSuite.equal(true, error.getMessage().contains("JSON")); }
    }

    private static String metadata() {
        return "{\"dist-tags\":{\"latest\":\"1.0.0\"},\"versions\":{"
                + "\"1.0.0\":{\"dependencies\":{\"bar\":\"^1\"}},\"1.2.0\":{}}}";
    }
}
