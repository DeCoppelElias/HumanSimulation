package io.github.decoppelelias.humansimulation.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.decoppelelias.humansimulation.domain.Rabbit;
import io.javalin.Javalin;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
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

    @Test
    void aCommandOnAPausedWorldAppliesAtOnce() {
        String id = newWorld();
        HttpTestClient.Response response = spawn(id, 3);
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.json().findValues("spriteKey")).hasSize(3);
    }

    @Test
    void aBadCommandIs400() {
        String id = newWorld();
        assertThat(command(id, "{\"type\":\"spawn\",\"species\":\"wolf\",\"count\":1}")
                        .status())
                .isEqualTo(400);
        assertThat(command(id, "{\"type\":\"fly\"}").status()).isEqualTo(400);
        assertThat(command(id, "{\"type\":\"spawn\",\"species\":\"rabbit\"}").status())
                .isEqualTo(400);
        assertThat(command(id, "{\"species\":\"rabbit\",\"count\":1}").status()).isEqualTo(400);
        HttpTestClient.Response unreadable = command(id, "not json");
        assertThat(unreadable.status()).isEqualTo(400);
        assertThat(unreadable.json().has("error")).isTrue();
    }

    @Test
    void stepAdvancesManyDaysAndKeepsEveryRow() {
        String id = newWorld();
        spawn(id, 5);
        HttpTestClient.Response report = client.post("/worlds/" + id + "/step", "{\"days\":500}");
        assertThat(report.status()).isEqualTo(200);
        assertThat(report.json().get("census").get("day").asInt()).isEqualTo(500);
        JsonNode census = client.get("/worlds/" + id + "/census").json();
        assertThat(census.size()).isEqualTo(500);
        assertThat(census.get(499).get("day").asInt()).isEqualTo(500);
        assertThat(client.post("/worlds/" + id + "/step", "")
                        .json()
                        .get("census")
                        .get("day")
                        .asInt())
                .isEqualTo(501);
    }

    @Test
    void stepRejectsDaysOutsideItsRange() {
        String id = newWorld();
        assertThat(client.post("/worlds/" + id + "/step", "{\"days\":0}").status())
                .isEqualTo(400);
        assertThat(client.post("/worlds/" + id + "/step", "{\"days\":10001}").status())
                .isEqualTo(400);
    }

    @Test
    void playAdvancesAndPauseStops() throws InterruptedException {
        String id = newWorld();
        client.put("/worlds/" + id + "/rate", "{\"daysPerSecond\":10}");
        assertThat(client.post("/worlds/" + id + "/play", "")
                        .json()
                        .get("playing")
                        .asBoolean())
                .isTrue();
        long deadline = System.nanoTime() + 3_000_000_000L;
        while (day(id) < 3 && System.nanoTime() < deadline) {
            Thread.sleep(50);
        }
        assertThat(client.post("/worlds/" + id + "/pause", "")
                        .json()
                        .get("playing")
                        .asBoolean())
                .isFalse();
        int paused = day(id);
        assertThat(paused).isGreaterThanOrEqualTo(3);
        Thread.sleep(400);
        assertThat(day(id)).isEqualTo(paused);
    }

    @Test
    void aPlayingWorldTakesCommandsForTheNextDayAndRefusesSteps() {
        String id = newWorld();
        client.post("/worlds/" + id + "/play", "");
        assertThat(spawn(id, 2).status()).isEqualTo(202);
        assertThat(client.post("/worlds/" + id + "/step", "").status()).isEqualTo(409);
        client.post("/worlds/" + id + "/pause", "");
    }

    @Test
    void rateOutsideItsRangeIs400() {
        String id = newWorld();
        assertThat(client.put("/worlds/" + id + "/rate", "{\"daysPerSecond\":0.4}")
                        .status())
                .isEqualTo(400);
        assertThat(client.put("/worlds/" + id + "/rate", "{\"daysPerSecond\":10.5}")
                        .status())
                .isEqualTo(400);
        assertThat(client.put("/worlds/" + id + "/rate", "{\"daysPerSecond\":5}")
                        .json()
                        .get("daysPerSecond")
                        .asDouble())
                .isEqualTo(5.0);
    }

    @Test
    void censusFromSkipsEarlierDays() {
        String id = newWorld();
        client.post("/worlds/" + id + "/step", "{\"days\":10}");
        assertThat(days(client.get("/worlds/" + id + "/census?from=8").json())).containsExactly(8, 9, 10);
    }

    @Test
    void resettingTheStatisticsKeepsTheHistoryWorking() {
        String id = newWorld();
        client.post("/worlds/" + id + "/step", "{\"days\":5}");
        assertThat(client.delete("/worlds/" + id + "/census").status()).isEqualTo(204);
        assertThat(client.get("/worlds/" + id + "/census").json().size()).isZero();
        client.post("/worlds/" + id + "/step", "{\"days\":3}");
        assertThat(days(client.get("/worlds/" + id + "/census").json())).containsExactly(6, 7, 8);
    }

    @Test
    void aResetCommandStartsTheHistoryAgain() {
        String id = newWorld();
        client.post("/worlds/" + id + "/step", "{\"days\":5}");
        JsonNode snapshot = command(id, "{\"type\":\"reset\",\"seed\":7}").json();
        assertThat(snapshot.get("day").asInt()).isZero();
        assertThat(snapshot.get("seed").asLong()).isEqualTo(7);
        assertThat(client.get("/worlds/" + id + "/census").json().size()).isZero();
        client.post("/worlds/" + id + "/step", "{\"days\":2}");
        assertThat(days(client.get("/worlds/" + id + "/census").json())).containsExactly(1, 2);
    }

    @Test
    void theLogReplaysOverHttp() {
        String original = newWorld();
        spawn(original, 4);
        client.post("/worlds/" + original + "/step", "{\"days\":7}");
        command(original, "{\"type\":\"spawnAt\",\"species\":\"rabbit\",\"at\":{\"x\":2,\"y\":3}}");
        client.post("/worlds/" + original + "/step", "{\"days\":5}");
        JsonNode log = client.get("/worlds/" + original + "/log").json();

        String copy = client.post(
                        "/worlds",
                        "{\"width\":" + log.get("width").asInt() + ",\"height\":"
                                + log.get("height").asInt() + ",\"seed\":"
                                + log.get("seed").asLong() + "}")
                .json()
                .get("id")
                .asString();
        int day = 0;
        for (JsonNode entry : log.get("commands")) {
            int target = entry.get("day").asInt();
            if (target > day) {
                client.post("/worlds/" + copy + "/step", "{\"days\":" + (target - day) + "}");
                day = target;
            }
            command(copy, entry.get("command").toString());
        }
        client.post("/worlds/" + copy + "/step", "{\"days\":" + (12 - day) + "}");

        assertThat(log.get("commands").size()).isEqualTo(2);
        assertThat(client.get("/worlds/" + copy).json())
                .isEqualTo(client.get("/worlds/" + original).json());
    }

    @Test
    void theStreamSendsSnapshotsDaysAndKeepalives() {
        String id = newWorld();
        List<String> lines = new CopyOnWriteArrayList<>();
        HttpRequest request = HttpRequest.newBuilder(client.uri("/worlds/" + id + "/events"))
                .header("Accept", "text/event-stream")
                .GET()
                .build();
        CompletableFuture<HttpResponse<Stream<String>>> stream =
                client.http().sendAsync(request, HttpResponse.BodyHandlers.ofLines());
        Thread.ofVirtual().start(() -> stream.join().body().forEach(lines::add));
        awaitLine(lines, "event: snapshot");
        client.post("/worlds/" + id + "/step", "{\"days\":3}");
        awaitLine(lines, "event: day");
        client.delete("/worlds/" + id + "/census");
        awaitLine(lines, "event: censusReset");
        awaitLine(lines, ": keepalive");
        stream.cancel(true);
    }

    private static void awaitLine(List<String> lines, String start) {
        long deadline = System.nanoTime() + 3_000_000_000L;
        while (lines.stream().noneMatch(line -> line.startsWith(start))) {
            assertThat(System.nanoTime())
                    .as("a line starting " + start + " in " + lines)
                    .isLessThan(deadline);
            Thread.onSpinWait();
        }
    }

    private static List<Integer> days(JsonNode rows) {
        List<Integer> days = new ArrayList<>();
        rows.forEach(row -> days.add(row.get("day").asInt()));
        return days;
    }

    private int day(String id) {
        return client.get("/worlds/" + id).json().get("day").asInt();
    }

    private HttpTestClient.Response command(String id, String json) {
        return client.post("/worlds/" + id + "/commands", json);
    }

    private HttpTestClient.Response spawn(String id, int count) {
        return command(id, "{\"type\":\"spawn\",\"species\":\"rabbit\",\"count\":" + count + "}");
    }

    private String newWorld() {
        return client.post("/worlds", "{\"seed\":42}").json().get("id").asString();
    }
}
