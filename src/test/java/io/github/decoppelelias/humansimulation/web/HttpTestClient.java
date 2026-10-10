package io.github.decoppelelias.humansimulation.web;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

final class HttpTestClient {
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    record Response(int status, String body) {
        JsonNode json() {
            return MAPPER.readTree(body);
        }
    }

    private final HttpClient http = HttpClient.newHttpClient();
    private final String base;

    HttpTestClient(int port) {
        this.base = "http://127.0.0.1:" + port;
    }

    Response get(String path) {
        return send(HttpRequest.newBuilder(uri(path)).GET());
    }

    Response delete(String path) {
        return send(HttpRequest.newBuilder(uri(path)).DELETE());
    }

    Response post(String path, String json) {
        return send(HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)));
    }

    Response put(String path, String json) {
        return send(HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json)));
    }

    URI uri(String path) {
        return URI.create(base + path);
    }

    HttpClient http() {
        return http;
    }

    private Response send(HttpRequest.Builder request) {
        try {
            HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
            return new Response(response.statusCode(), response.body());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
