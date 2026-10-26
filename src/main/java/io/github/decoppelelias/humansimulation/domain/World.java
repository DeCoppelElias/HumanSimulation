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
    static final String MUTATION = "mutation";
    static final String EATEN = "eaten";

    /** Settings the world declares itself, ahead of its processes' in the World group. */
    static final Settings OWN = new Settings(List.of(new Setting.Toggle(
            MUTATION,
            "Children and spawns vary from their parent or baseline. Off, they are exact copies and the world stops"
                    + " evolving.",
            true)));

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
        tick();
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
        SettingValues settings = table.of(entity.species().name());
        List<Option> offered = new ArrayList<>();
        offered.add(new Option.Idle());
        offered.addAll(MoveRule.options(grid, entity.position(), settings));
        BreedRule.options(entity, settings, day).ifPresent(offered::add);
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
                case Intent.Breed _ ->
                    BreedRule.resolve(entity, table.of(entity.species().name()), day)
                            .ifPresent(reserve -> place(
                                    entity.species(),
                                    entity.position(),
                                    inherit(entity.genome()),
                                    Optional.of(reserve)));
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
                            remove(id, EATEN);
                        });
                    }
                }
            });
        }
    }

    /** Ticks each entity's parts in id order. The stream is lazy, so the first cause ends it and later parts wait. */
    private void tick() {
        for (int id : entityIds()) {
            Entity entity = entities.get(id);
            if (entity != null) {
                entity.parts().stream()
                        .map(Component::tick)
                        .flatMap(Optional::stream)
                        .findFirst()
                        .ifPresent(cause -> remove(id, cause));
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
                        place(kind, at, inherit(kind.baseline()), Optional.empty());
                    }
                }
            }
        }
    }

    /** A genome passed on, to a child from its parent or to a spawn from its species' baseline. */
    private Genome inherit(Genome genome) {
        return table.of(SettingTable.WORLD).on(MUTATION) ? genome.mutated(random) : genome;
    }

    private GridPosition randomTile() {
        return new GridPosition(random.nextInt(width), random.nextInt(height));
    }

    /** A species with no members or no genes has no means, since a zero would read as a real one. */
    private CensusRow census() {
        Map<String, Integer> population = new TreeMap<>();
        Map<String, List<Genome>> genomes = new TreeMap<>();
        for (String name : species.keySet()) {
            population.put(name, 0);
            genomes.put(name, new ArrayList<>());
        }
        for (int id : entityIds()) {
            Entity entity = entities.get(id);
            population.merge(entity.species().name(), 1, Integer::sum);
            genomes.get(entity.species().name()).add(entity.genome());
        }
        Map<String, Map<String, Double>> means = new TreeMap<>();
        genomes.forEach((name, members) -> {
            List<GeneSpec> layout = species.get(name).geneLayout();
            if (members.isEmpty() || layout.isEmpty()) {
                return;
            }
            Map<String, Double> mean = new TreeMap<>();
            for (GeneSpec spec : layout) {
                switch (spec) {
                    case GeneSpec.Scalar scalar ->
                        mean.put(
                                scalar.key(),
                                members.stream()
                                        .mapToDouble(genome -> genome.scalar(scalar.key()))
                                        .average()
                                        .orElseThrow());
                }
            }
            means.put(name, mean);
        });
        return new CensusRow(day, population, means);
    }

    private List<Integer> entityIds() {
        return List.copyOf(entities.keySet());
    }

    private void validate(Command command) {
        switch (command) {
            case Command.Spawn spawn -> speciesNamed(spawn.species()).baseline().with(spawn.genes());
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
                Genome start = kind.baseline().with(spawn.genes());
                for (int i = 0; i < spawn.count(); i++) {
                    GridPosition at = randomTile();
                    place(kind, at, inherit(start), Optional.empty());
                }
            }
            case Command.SpawnAt spawnAt -> {
                Species kind = speciesNamed(spawnAt.species());
                place(kind, spawnAt.at(), inherit(kind.baseline()), Optional.empty());
            }
            case Command.Reset reset -> start(reset.seed());
            case Command.SetSetting set -> table.set(set.owner(), set.key(), set.value());
            case Command.RestoreDefaults restore -> table.restore(restore.owner(), restore.key());
        }
    }

    private void place(Species kind, GridPosition at, Genome genome, Optional<Double> reserve) {
        Spawn spawn = new Spawn(genome, kind, table.of(kind.name()), day, reserve);
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
    void remove(int id, String cause) {
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
