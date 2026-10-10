package io.github.decoppelelias.humansimulation.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.decoppelelias.humansimulation.domain.Rabbit;
import io.javalin.Javalin;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
