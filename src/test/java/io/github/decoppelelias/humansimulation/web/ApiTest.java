package io.github.decoppelelias.humansimulation.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.decoppelelias.humansimulation.domain.Rabbit;
import io.javalin.Javalin;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

class ApiTest {
    private Javalin app;
    private HttpTestClient client;

    @BeforeEach
    void start() {
        Worlds worlds = new Worlds(3, List.of(Rabbit.species()));
        IdleWatch idle = new IdleWatch(Duration.ZERO, System::nanoTime, () -> {});
        app = Api.create(worlds, idle, Duration.ofMillis(200), System::nanoTime).start("127.0.0.1", 0);
        client = new HttpTestClient(app.port());
    }

    @AfterEach
    void stop() {
        app.stop();
    }

    @Test
    void anEmptyServerListsNoWorlds() {
        HttpTestClient.Response response = client.get("/worlds");
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.json().isArray()).isTrue();
        assertThat(response.json().size()).isZero();
    }

    @Test
    void createsAnEmptyPausedWorldWithDefaults() {
        HttpTestClient.Response created = client.post("/worlds", "");
        assertThat(created.status()).isEqualTo(201);
        JsonNode body = created.json();
        String id = body.get("id").asString();
        assertThat(id).matches("[0-9a-f]{32}");
        assertThat(body.get("snapshot").get("width").asInt()).isEqualTo(20);
        assertThat(body.get("snapshot").get("height").asInt()).isEqualTo(20);
        assertThat(body.get("snapshot").get("day").asInt()).isZero();
        assertThat(client.get("/worlds/" + id).json().findValues("spriteKey")).isEmpty();
        assertThat(client.get("/worlds").json().get(0).get("playing").asBoolean())
                .isFalse();
    }

    @Test
    void createTakesASizeAndASeed() {
        JsonNode snapshot = client.post("/worlds", "{\"width\":5,\"height\":4,\"seed\":42}")
                .json()
                .get("snapshot");
        assertThat(snapshot.get("width").asInt()).isEqualTo(5);
        assertThat(snapshot.get("height").asInt()).isEqualTo(4);
        assertThat(snapshot.get("seed").asLong()).isEqualTo(42);
    }

    @Test
    void rejectsASideAbove25() {
        HttpTestClient.Response response = client.post("/worlds", "{\"width\":26}");
        assertThat(response.status()).isEqualTo(400);
        assertThat(response.json().get("error").asString()).contains("25");
    }

    @Test
    void refusesPastTheCap() {
        for (int i = 0; i < 3; i++) {
            assertThat(client.post("/worlds", "").status()).isEqualTo(201);
        }
        HttpTestClient.Response refused = client.post("/worlds", "");
        assertThat(refused.status()).isEqualTo(409);
        assertThat(refused.json().get("error").asString()).contains("3");
    }

    @Test
    void listsWorldsInTheOrderTheyWereCreated() {
        String first = newWorld();
        String second = newWorld();
        assertThat(client.get("/worlds").json().findValuesAsString("id")).containsExactly(first, second);
    }

    @Test
    void unknownWorldIs404() {
        HttpTestClient.Response response = client.get("/worlds/nope");
        assertThat(response.status()).isEqualTo(404);
        assertThat(response.json().get("error").asString()).contains("nope");
    }

    @Test
    void deleteClosesIt() {
        String id = newWorld();
        assertThat(client.delete("/worlds/" + id).status()).isEqualTo(204);
        assertThat(client.get("/worlds/" + id).status()).isEqualTo(404);
    }

    private String newWorld() {
        return client.post("/worlds", "{\"seed\":42}").json().get("id").asString();
    }
}
