package io.github.decoppelelias.humansimulation.web;

import io.github.decoppelelias.humansimulation.domain.Command;
import io.github.decoppelelias.humansimulation.domain.WorldSnapshot;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson3;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongSupplier;
import tools.jackson.core.JacksonException;

public final class Api {
    private static final int DEFAULT_SIDE = 20;

    private record CreateRequest(Optional<Integer> width, Optional<Integer> height, Optional<Long> seed) {}

    private record Created(String id, WorldSnapshot snapshot) {}

    private record StepRequest(Optional<Integer> days) {}

    private record RateRequest(double daysPerSecond) {}

    private Api() {}

    public static Javalin create(Worlds worlds, IdleWatch idle, Duration keepAlive, LongSupplier freshSeed) {
        return Javalin.create(config -> {
            config.startup.showJavalinBanner = false;
            config.jsonMapper(new JavalinJackson3());
            config.routes.before(ctx -> idle.touch());

            config.routes.post("/worlds", ctx -> {
                CreateRequest request = ctx.body().isBlank()
                        ? new CreateRequest(Optional.empty(), Optional.empty(), Optional.empty())
                        : ctx.bodyAsClass(CreateRequest.class);
                WorldHost host = worlds.create(
                        request.width().orElse(DEFAULT_SIDE),
                        request.height().orElse(DEFAULT_SIDE),
                        request.seed().orElseGet(freshSeed::getAsLong));
                ctx.status(201).json(new Created(host.id(), host.snapshot()));
            });
            config.routes.get(
                    "/worlds",
                    ctx -> ctx.json(
                            worlds.all().stream().map(WorldHost::summary).toList()));
            config.routes.get(
                    "/worlds/{id}",
                    ctx -> ctx.json(worlds.get(ctx.pathParam("id")).snapshot()));
            config.routes.delete("/worlds/{id}", ctx -> {
                worlds.delete(ctx.pathParam("id"));
                ctx.status(204);
            });

            config.routes.post("/worlds/{id}/commands", ctx -> {
                Command command = CommandJson.toCommand(ctx.bodyAsClass(CommandJson.Body.class), freshSeed);
                worlds.get(ctx.pathParam("id")).submit(command).ifPresentOrElse(ctx::json, () -> ctx.status(202));
            });
            config.routes.post("/worlds/{id}/step", ctx -> {
                int days = ctx.body().isBlank()
                        ? 1
                        : ctx.bodyAsClass(StepRequest.class).days().orElse(1);
                ctx.json(worlds.get(ctx.pathParam("id")).step(days));
            });
            config.routes.post(
                    "/worlds/{id}/play",
                    ctx -> ctx.json(worlds.get(ctx.pathParam("id")).play()));
            config.routes.post(
                    "/worlds/{id}/pause",
                    ctx -> ctx.json(worlds.get(ctx.pathParam("id")).pause()));
            config.routes.put(
                    "/worlds/{id}/rate",
                    ctx -> ctx.json(worlds.get(ctx.pathParam("id"))
                            .rate(ctx.bodyAsClass(RateRequest.class).daysPerSecond())));
            config.routes.get("/worlds/{id}/census", ctx -> {
                int from = Optional.ofNullable(ctx.queryParam("from"))
                        .map(Integer::parseInt)
                        .orElse(1);
                ctx.json(worlds.get(ctx.pathParam("id")).census(from));
            });
            config.routes.delete("/worlds/{id}/census", ctx -> {
                worlds.get(ctx.pathParam("id")).resetCensus();
                ctx.status(204);
            });
            config.routes.get(
                    "/worlds/{id}/log",
                    ctx -> ctx.json(worlds.get(ctx.pathParam("id")).log()));
            config.routes.sse("/worlds/{id}/events", sse -> {
                WorldHost host = worlds.get(sse.ctx().pathParam("id"));
                sse.keepAlive();
                Viewer viewer = new Viewer(sse, keepAlive);
                idle.streamOpened();
                sse.onClose(() -> {
                    host.detach(viewer);
                    idle.streamClosed();
                });
                host.attach(viewer);
                Thread.ofVirtual().name("viewer-" + host.id()).start(viewer::run);
            });

            config.routes.exception(
                    JacksonException.class,
                    (e, ctx) -> ctx.status(400).json(Map.of("error", "unreadable JSON: " + e.getOriginalMessage())));
            config.routes.exception(
                    NumberFormatException.class,
                    (e, ctx) -> ctx.status(400).json(Map.of("error", "not a whole number: " + e.getMessage())));
            config.routes.exception(
                    ApiException.class, (e, ctx) -> ctx.status(e.status()).json(Map.of("error", e.getMessage())));
            config.routes.exception(
                    IllegalArgumentException.class, (e, ctx) -> ctx.status(400).json(Map.of("error", e.getMessage())));
        });
    }
}
