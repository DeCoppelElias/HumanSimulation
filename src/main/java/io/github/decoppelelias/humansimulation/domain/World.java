package io.github.decoppelelias.humansimulation.domain;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.IntFunction;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public final class World {
    private static final String ALGORITHM = "L64X128MixRandom";
    static final String SPEED = "speed";

    private final int width;
    private final int height;
    private final SortedMap<String, Species> species;
    private final List<WorldProcess> processes;
    private final SettingTable table;
    private final ArrayDeque<Command> pending = new ArrayDeque<>();
    private final List<LoggedCommand> log = new ArrayList<>();
    private final TreeMap<Integer, Entity> entities = new TreeMap<>();
    private Grid grid;
    private long seed;
    private RandomGenerator random;
    private int nextId;
    private int day;

    public World(int width, int height, long seed, List<Species> species) {
        this(width, height, seed, species, List.of());
    }

    public World(int width, int height, long seed, List<Species> species, List<WorldProcess> processes) {
        this.width = width;
        this.height = height;
        this.grid = new Grid(width, height);
        this.species = byName(species);
        this.processes = List.copyOf(processes);
        for (WorldProcess process : this.processes) {
            switch (process) {
                case FoodDrop drop -> speciesNamed(drop.species());
            }
        }
        this.table = new SettingTable(this.species.values(), this.processes);
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

    public DayReport advance() {
        drain();
        List<Decision> order = dayOrder(decide(), this::speedOf, random);
        resolve(order);
        feed(order);
        metabolise();
        runProcesses();
        day++;
        return new DayReport(snapshot(), census());
    }

    /** One entity, for tests that read its state. */
    Optional<Entity> entity(int id) {
        return Optional.ofNullable(entities.get(id));
    }

    /** Every declared setting in panel order. They never change for a world. */
    public List<SettingDeclaration> declarations() {
        return table.declarations();
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
                        id,
                        entity.species().name(),
                        entity.species().spriteKey(),
                        entity.brain().isPresent(),
                        Map.of()));
            }
            tiles.add(new WorldSnapshot.TileView(at, Map.of(), standing));
        }
        return new WorldSnapshot(seed, day, width, height, List.copyOf(species.keySet()), tiles, table.current());
    }

    private List<Decision> decide() {
        List<Decision> decisions = new ArrayList<>();
        for (int id : entityIds()) {
            Entity entity = entities.get(id);
            entity.brain()
                    .ifPresent(brain -> decisions.add(new Decision(
                            id,
                            brain.decide(CircularSense.perceive(entity, grid, entities), options(entity), random))));
        }
        return decisions;
    }

    /** Idling, then each rule's offer in a fixed order. Every action a creature can take is listed here. */
    private Options options(Entity entity) {
        List<Option> offered = new ArrayList<>();
        offered.add(new Option.Idle());
        offered.addAll(MoveRule.options(
                grid, entity.position(), table.of(entity.species().name())));
        return new Options(grid.directions(), offered);
    }

    /**
     * Shuffled from id order with the world's generator, so the draw replays, then sorted fastest first. The sort is
     * stable, so equal speeds keep the shuffle, and anything without a speed gene goes last.
     */
    static List<Decision> dayOrder(
            List<Decision> decisions, IntFunction<OptionalDouble> speedOf, RandomGenerator random) {
        List<Decision> order = new ArrayList<>(decisions);
        Collections.shuffle(order, random);
        order.sort(Comparator.comparingDouble(decision -> {
            OptionalDouble speed = speedOf.apply(decision.entityId());
            return speed.isPresent() ? -speed.getAsDouble() : Double.POSITIVE_INFINITY;
        }));
        return order;
    }

    private OptionalDouble speedOf(int id) {
        Genome genome = entities.get(id).genome();
        return genome.declares(SPEED) ? OptionalDouble.of(genome.scalar(SPEED)) : OptionalDouble.empty();
    }

    private void resolve(List<Decision> order) {
        for (Decision decision : order) {
            Entity entity = entities.get(decision.entityId());
            if (entity == null) {
                continue;
            }
            switch (decision.intent()) {
                case Intent.Move move -> MoveRule.resolve(grid, entity, move);
                case Intent.Breed _ -> {}
                case Intent.Idle _ -> {}
            }
        }
    }

    /** Walks the day's order, per 0016: a creature takes every edible on its tile. */
    void feed(List<Decision> order) {
        for (Decision decision : order) {
            Entity eater = entities.get(decision.entityId());
            if (eater == null) {
                continue;
            }
            eater.part(Metabolism.class).ifPresent(metabolism -> {
                for (int id : grid.occupants(eater.position())) {
                    Entity other = entities.get(id);
                    if (id != eater.id() && other != null) {
                        other.part(Edible.class).ifPresent(edible -> {
                            metabolism.credit(edible.amount());
                            remove(id);
                        });
                    }
                }
            });
        }
    }

    /** Charges every reserve in id order and removes anything below zero. */
    private void metabolise() {
        for (int id : entityIds()) {
            Entity entity = entities.get(id);
            if (entity != null
                    && entity.part(Metabolism.class).map(m -> !m.tick()).orElse(false)) {
                remove(id);
            }
        }
    }

    private void runProcesses() {
        for (WorldProcess process : processes) {
            switch (process) {
                case FoodDrop drop -> {
                    Species kind = speciesNamed(drop.species());
                    for (int i = drop.dropsOn(day + 1, table.of(SettingTable.WORLD)); i > 0; i--) {
                        GridPosition at = randomTile();
                        place(kind, at, kind.baseline().mutated(random));
                    }
                }
            }
        }
    }

    private GridPosition randomTile() {
        return new GridPosition(random.nextInt(width), random.nextInt(height));
    }

    private CensusRow census() {
        Map<String, Integer> population = new TreeMap<>();
        for (String name : species.keySet()) {
            population.put(name, 0);
        }
        for (int id : entityIds()) {
            population.merge(entities.get(id).species().name(), 1, Integer::sum);
        }
        return new CensusRow(day, population);
    }

    private List<Integer> entityIds() {
        return List.copyOf(entities.keySet());
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
            case Command.SetSetting set -> table.check(set.owner(), set.key(), set.value());
            case Command.RestoreDefaults restore -> table.checkScope(restore.owner(), restore.key());
        }
    }

    private void drain() {
        while (!pending.isEmpty()) {
            Command command = pending.poll();
            apply(command);
            log.add(new LoggedCommand(day, command));
            if (command instanceof Command.Reset) {
                // A reset keeps edited settings, so the new log carries them and the run still replays from it.
                table.edits().forEach(edit -> log.add(new LoggedCommand(day, edit)));
            }
        }
    }

    private void apply(Command command) {
        switch (command) {
            case Command.Spawn spawn -> {
                Species kind = speciesNamed(spawn.species());
                for (int i = 0; i < spawn.count(); i++) {
                    GridPosition at = randomTile();
                    place(kind, at, kind.baseline().mutated(random));
                }
            }
            case Command.SpawnAt spawnAt -> {
                Species kind = speciesNamed(spawnAt.species());
                place(kind, spawnAt.at(), kind.baseline().mutated(random));
            }
            case Command.Reset reset -> start(reset.seed());
            case Command.SetSetting set -> table.set(set.owner(), set.key(), set.value());
            case Command.RestoreDefaults restore -> table.restore(restore.owner(), restore.key());
        }
    }

    private void place(Species kind, GridPosition at, Genome genome) {
        Spawn spawn = new Spawn(genome, kind, table.of(kind.name()));
        Entity entity = new Entity(
                nextId++,
                kind,
                genome,
                at,
                kind.brain().map(brain -> brain.apply(spawn)),
                kind.parts().stream().map(part -> part.apply(spawn)).toList());
        entities.put(entity.id(), entity);
        grid.place(entity.id(), at);
    }

    /** Checks everything before changing anything, so a failed removal leaves the world whole. */
    void remove(int id) {
        Entity entity = entities.get(id);
        if (entity == null) {
            throw new IllegalArgumentException("there is no entity " + id + " to remove");
        }
        if (!grid.occupants(entity.position()).contains(id)) {
            throw new IllegalStateException("entity " + id + " is not on its tile " + entity.position());
        }
        entities.remove(id);
        grid.remove(id, entity.position());
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
