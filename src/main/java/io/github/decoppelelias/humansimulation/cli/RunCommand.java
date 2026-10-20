package io.github.decoppelelias.humansimulation.cli;

import io.github.decoppelelias.humansimulation.domain.Command;
import io.github.decoppelelias.humansimulation.domain.FoodDrop;
import io.github.decoppelelias.humansimulation.domain.Grass;
import io.github.decoppelelias.humansimulation.domain.Rabbit;
import io.github.decoppelelias.humansimulation.domain.World;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@CommandLine.Command(
        name = "run",
        mixinStandardHelpOptions = true,
        description = "Advance one seeded world and print its census as JSON Lines, one line per day.")
public final class RunCommand implements Callable<Integer> {
    private static final Map<String, Integer> STARTING_POPULATION = Map.of("rabbit", 10);

    @Spec
    private CommandSpec spec;

    @Option(names = "--days", required = true, description = "How many days to advance.")
    private int days;

    @Option(names = "--seed", description = "The world's seed. Drawn from the clock when left out.")
    private Optional<Long> seed = Optional.empty();

    @Option(names = "--width", defaultValue = "20", description = "Grid width in tiles. Default: ${DEFAULT-VALUE}.")
    private int width;

    @Option(names = "--height", defaultValue = "20", description = "Grid height in tiles. Default: ${DEFAULT-VALUE}.")
    private int height;

    @Option(
            names = "--spawn",
            paramLabel = "SPECIES=COUNT",
            description = "Starting population, once per species. Replaces the default of rabbit=10.")
    private List<String> spawn = List.of();

    @Option(
            names = "--set",
            paramLabel = "OWNER.KEY=VALUE",
            description = "Override a setting from day 0, before the starting population spawns. Repeatable.")
    private List<String> set = List.of();

    @Option(
            names = "--set-at",
            paramLabel = "DAY:OWNER.KEY=VALUE",
            description = "Override a setting once DAY days are complete. Repeatable. 0 is the same as --set.")
    private List<String> setAt = List.of();

    @Override
    public Integer call() {
        if (days < 1) {
            throw usage("--days must be at least 1, got " + days);
        }
        long worldSeed = seed.orElseGet(System::nanoTime);
        List<Command.SetSetting> dayZero = new ArrayList<>();
        set.forEach(entry -> dayZero.add(setting("--set", entry)));
        SortedMap<Integer, List<Command.SetSetting>> later = new TreeMap<>();
        for (String entry : setAt) {
            int colon = entry.indexOf(':');
            int day = colon < 1 ? -1 : dayOf(entry.substring(0, colon));
            if (day < 0) {
                throw usage("--set-at takes DAY:OWNER.KEY=VALUE with DAY 0 or more, got " + entry);
            }
            Command.SetSetting edit = setting("--set-at", entry.substring(colon + 1));
            if (day == 0) {
                dayZero.add(edit);
            } else {
                later.computeIfAbsent(day, _ -> new ArrayList<>()).add(edit);
            }
        }
        World scratch = start(worldSeed, dayZero);
        later.values().forEach(edits -> edits.forEach(edit -> submit(scratch, edit)));
        World world = start(worldSeed, dayZero);
        PrintWriter out = spec.commandLine().getOut();
        for (int completed = 0; completed < days; completed++) {
            later.getOrDefault(completed, List.of()).forEach(world::submit);
            out.print(CensusLine.format(worldSeed, world.advance().census()) + "\n");
        }
        out.flush();
        return CommandLine.ExitCode.OK;
    }

    private static int dayOf(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Checked against a world built the same way, so a bad later override fails before the first line prints. */
    private void submit(World world, Command command) {
        try {
            world.submit(command);
        } catch (IllegalArgumentException e) {
            throw usage(e.getMessage());
        }
    }

    private World start(long worldSeed, List<Command.SetSetting> dayZero) {
        try {
            World world = new World(
                    width,
                    height,
                    worldSeed,
                    List.of(Grass.species(), Rabbit.species()),
                    List.of(new FoodDrop("grass")));
            dayZero.forEach(world::submit);
            new TreeMap<>(spawn.isEmpty() ? STARTING_POPULATION : startingPopulation())
                    .forEach((name, count) -> world.submit(new Command.Spawn(name, count)));
            return world;
        } catch (IllegalArgumentException e) {
            throw usage(e.getMessage());
        }
    }

    private Map<String, Integer> startingPopulation() {
        Map<String, Integer> population = new TreeMap<>();
        for (String entry : spawn) {
            String[] parts = entry.split("=", -1);
            if (parts.length != 2 || parts[0].isBlank()) {
                throw usage("--spawn takes SPECIES=COUNT, got " + entry);
            }
            int count;
            try {
                count = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                throw usage("--spawn needs a whole number of " + parts[0] + ", got " + parts[1]);
            }
            if (population.putIfAbsent(parts[0], count) != null) {
                throw usage("--spawn names " + parts[0] + " more than once");
            }
        }
        return population;
    }

    /** OWNER.KEY=VALUE, where a toggle also takes true or false. */
    private Command.SetSetting setting(String flag, String entry) {
        int dot = entry.indexOf('.');
        int equals = entry.indexOf('=');
        if (dot < 1 || equals < dot + 2 || equals == entry.length() - 1) {
            throw usage(flag + " takes OWNER.KEY=VALUE, got " + entry);
        }
        String raw = entry.substring(equals + 1);
        double value =
                switch (raw) {
                    case "true" -> 1;
                    case "false" -> 0;
                    default -> {
                        try {
                            yield Double.parseDouble(raw);
                        } catch (NumberFormatException e) {
                            throw usage(flag + " needs a number, true or false, got " + raw);
                        }
                    }
                };
        return new Command.SetSetting(entry.substring(0, dot), entry.substring(dot + 1, equals), value);
    }

    private ParameterException usage(String message) {
        return new ParameterException(spec.commandLine(), message);
    }
}
