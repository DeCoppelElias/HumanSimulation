package io.github.decoppelelias.humansimulation.web;

import io.javalin.Javalin;
import io.javalin.json.JavalinJackson3;
import java.time.Duration;
import java.util.Map;
import java.util.function.LongSupplier;

public final class Api {
    private Api() {}

    public static Javalin create(Worlds worlds, IdleWatch idle, Duration keepAlive, LongSupplier freshSeed) {
        return Javalin.create(config -> {
            config.startup.showJavalinBanner = false;
            config.jsonMapper(new JavalinJackson3());
            config.routes.before(ctx -> idle.touch());
            config.routes.get(
                    "/worlds",
                    ctx -> ctx.json(
                            worlds.all().stream().map(WorldHost::summary).toList()));
            config.routes.exception(
                    ApiException.class, (e, ctx) -> ctx.status(e.status()).json(Map.of("error", e.getMessage())));
            config.routes.exception(
                    IllegalArgumentException.class, (e, ctx) -> ctx.status(400).json(Map.of("error", e.getMessage())));
        });
    }
}
