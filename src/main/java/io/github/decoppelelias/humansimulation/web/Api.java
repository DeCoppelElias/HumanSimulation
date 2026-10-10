package io.github.decoppelelias.humansimulation.web;

import io.github.decoppelelias.humansimulation.domain.WorldSnapshot;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson3;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongSupplier;

public final class Api {
    private static final int DEFAULT_SIDE = 20;

    private record CreateRequest(Optional<Integer> width, Optional<Integer> height, Optional<Long> seed) {}

    private record Created(String id, WorldSnapshot snapshot) {}

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

            config.routes.exception(
                    ApiException.class, (e, ctx) -> ctx.status(e.status()).json(Map.of("error", e.getMessage())));
            config.routes.exception(
                    IllegalArgumentException.class, (e, ctx) -> ctx.status(400).json(Map.of("error", e.getMessage())));
        });
    }
}
