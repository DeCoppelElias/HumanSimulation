package io.github.decoppelelias.humansimulation.web;

import io.github.decoppelelias.humansimulation.domain.Command;
import io.github.decoppelelias.humansimulation.domain.GridPosition;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongSupplier;

/** A command's JSON: a {@code type} and the fields that type takes. Written in the shape it is read. */
final class CommandJson {
    record Body(
            Optional<String> type,
            Optional<String> species,
            Optional<Integer> count,
            Optional<GridPosition> at,
            Optional<Long> seed) {}

    private CommandJson() {}

    static Command toCommand(Body body, LongSupplier freshSeed) {
        String type = required(body.type(), "a type");
        return switch (type) {
            case "spawn" -> new Command.Spawn(required(body.species(), "species"), required(body.count(), "count"));
            case "spawnAt" -> new Command.SpawnAt(required(body.species(), "species"), required(body.at(), "at"));
            case "reset" -> new Command.Reset(body.seed().orElseGet(freshSeed::getAsLong));
            default -> throw new IllegalArgumentException("unknown command type " + type);
        };
    }

    static Map<String, Object> toJson(Command command) {
        Map<String, Object> json = new LinkedHashMap<>();
        switch (command) {
            case Command.Spawn spawn -> {
                json.put("type", "spawn");
                json.put("species", spawn.species());
                json.put("count", spawn.count());
            }
            case Command.SpawnAt spawnAt -> {
                json.put("type", "spawnAt");
                json.put("species", spawnAt.species());
                json.put("at", spawnAt.at());
            }
            case Command.Reset reset -> {
                json.put("type", "reset");
                json.put("seed", reset.seed());
            }
        }
        return json;
    }

    private static <T> T required(Optional<T> value, String field) {
        return value.orElseThrow(() -> new IllegalArgumentException("this command needs " + field));
    }
}
