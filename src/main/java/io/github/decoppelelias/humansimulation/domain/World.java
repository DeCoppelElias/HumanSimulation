package io.github.decoppelelias.humansimulation.domain;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public final class World {
    private static final String ALGORITHM = "L64X128MixRandom";

    private final int width;
    private final int height;
    private final SortedMap<String, Species> species;
    private final ArrayDeque<Command> pending = new ArrayDeque<>();
    private final List<LoggedCommand> log = new ArrayList<>();
    private final TreeMap<Integer, Entity> entities = new TreeMap<>();
    private Grid grid;
    private long seed;
    private RandomGenerator random;
    private int nextId;
    private int day;

    public World(int width, int height, long seed, List<Species> species) {
        this.width = width;
        this.height = height;
        this.grid = new Grid(width, height);
        this.species = byName(species);
        start(seed);
    }

    public void submit(Command command) {
        validate(command);
        pending.add(command);
    }

    public WorldSnapshot applyPending() {
        drain();
        return snapshot();
    }

    public List<LoggedCommand> log() {
        return List.copyOf(log);
    }

    public WorldSnapshot snapshot() {
        List<WorldSnapshot.TileView> tiles = new ArrayList<>();
        for (GridPosition at : grid.tiles()) {
            List<WorldSnapshot.EntityView> standing = new ArrayList<>();
            for (int id : grid.occupants(at)) {
                Entity entity = entities.get(id);
                standing.add(new WorldSnapshot.EntityView(
                        id, entity.species().name(), entity.species().spriteKey(), Map.of()));
            }
            tiles.add(new WorldSnapshot.TileView(at, Map.of(), standing));
        }
        return new WorldSnapshot(seed, day, width, height, tiles);
    }

    private void validate(Command command) {
        switch (command) {
            case Command.Spawn spawn -> speciesNamed(spawn.species());
            case Command.SpawnAt spawnAt -> {
                speciesNamed(spawnAt.species());
                if (!grid.contains(spawnAt.at())) {
                    throw new IllegalArgumentException(
                            spawnAt.at() + " is off the " + width + " by " + height + " grid");
                }
            }
            case Command.Reset _ -> {}
        }
    }

    private void drain() {
        while (!pending.isEmpty()) {
            Command command = pending.poll();
            apply(command);
            log.add(new LoggedCommand(day, command));
        }
    }

    private void apply(Command command) {
        switch (command) {
            case Command.Spawn spawn -> {
                Species kind = speciesNamed(spawn.species());
                for (int i = 0; i < spawn.count(); i++) {
                    place(kind, new GridPosition(random.nextInt(width), random.nextInt(height)));
                }
            }
            case Command.SpawnAt spawnAt -> place(speciesNamed(spawnAt.species()), spawnAt.at());
            case Command.Reset reset -> start(reset.seed());
        }
    }

    private void place(Species kind, GridPosition at) {
        Spawn spawn = new Spawn(kind.baseline(), kind);
        Entity entity = new Entity(
                nextId++,
                kind,
                kind.baseline(),
                at,
                kind.brain().map(brain -> brain.apply(spawn)),
                kind.parts().stream().map(part -> part.apply(spawn)).toList());
        entities.put(entity.id(), entity);
        grid.place(entity.id(), at);
    }

    private Species speciesNamed(String name) {
        Species found = species.get(name);
        if (found == null) {
            throw new IllegalArgumentException("this world knows no species named " + name);
        }
        return found;
    }

    private void start(long newSeed) {
        seed = newSeed;
        random = RandomGeneratorFactory.of(ALGORITHM).create(newSeed);
        grid = new Grid(width, height);
        entities.clear();
        log.clear();
        nextId = 0;
        day = 0;
    }

    private static SortedMap<String, Species> byName(List<Species> species) {
        SortedMap<String, Species> named = new TreeMap<>();
        for (Species one : species) {
            if (named.putIfAbsent(one.name(), one) != null) {
                throw new IllegalArgumentException("two species are named " + one.name());
            }
        }
        return Collections.unmodifiableSortedMap(named);
    }
}
