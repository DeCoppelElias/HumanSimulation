package io.github.decoppelelias.humansimulation.cli;

import io.github.decoppelelias.humansimulation.domain.Command;
import io.github.decoppelelias.humansimulation.domain.DayReport;
import io.github.decoppelelias.humansimulation.domain.FoodDrop;
import io.github.decoppelelias.humansimulation.domain.Grass;
import io.github.decoppelelias.humansimulation.domain.Rabbit;
import io.github.decoppelelias.humansimulation.domain.World;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
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
            description = "Override a setting once DAY days are complete. Repeatable. On one day, --set comes first,"
                    + " then --set-at in the order given, and the last value for a key wins.")
    private List<String> setAt = List.of();

    @Option(
            names = "--gene",
            paramLabel = "SPECIES.GENE=VALUE",
            description = "Start that species' spawn from its baseline with this gene in place. Repeatable, and the"
                    + " last value for a gene wins.")
    private List<String> gene = List.of();

    @Option(
            names = "--export",
            paramLabel = "FILE",
            description = "Write the creatures alive on each --export-day to FILE, one per line.")
    private Optional<Path> export = Optional.empty();

    @Option(
            names = "--export-day",
            paramLabel = "DAY",
            description = "Export once DAY days are complete, from 1 to --days. Repeatable.")
    private List<Integer> exportDay = List.of();

    @Option(
            names = "--spawn-from",
            paramLabel = "FILE",
            description = "Spawn one creature per line of an exported FILE, after any --spawn. Repeatable, and replaces"
                    + " the default of rabbit=10.")
    private List<Path> spawnFrom = List.of();

    @Override
    public Integer call() {
        if (days < 1) {
            throw usage("--days must be at least 1, got " + days);
        }
        SortedSet<Integer> exportDays = new TreeSet<>(exportDay);
        if (export.isPresent() == exportDays.isEmpty()) {
            throw usage("--export and --export-day go together");
        }
        for (int day : exportDays) {
            if (day < 1 || day > days) {
                throw usage("--export-day must be from 1 to --days, got " + day);
            }
        }
        long worldSeed = seed.orElseGet(System::nanoTime);
        List<Command.SetSetting> dayZero = new ArrayList<>();
        set.forEach(entry -> dayZero.add(setting("--set", "OWNER.KEY", entry)));
        SortedMap<Integer, List<Command.SetSetting>> later = new TreeMap<>();
        for (String entry : setAt) {
            int colon = entry.indexOf(':');
            int day = colon < 1 ? -1 : dayOf(entry.substring(0, colon));
            if (day < 0) {
                throw usage("--set-at takes DAY:OWNER.KEY=VALUE with DAY a whole number from 0, got " + entry);
            }
            Command.SetSetting edit = setting("--set-at", "OWNER.KEY", entry.substring(colon + 1));
            if (day == 0) {
                dayZero.add(edit);
            } else {
                later.computeIfAbsent(day, _ -> new ArrayList<>()).add(edit);
            }
        }
        List<Command.Spawn> fromFiles = new ArrayList<>();
        for (Path file : spawnFrom) {
            List<String> lines;
            try {
                lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw usage("cannot read " + file);
            }
            try {
                fromFiles.addAll(GenomeLines.read(file.getFileName().toString(), lines));
            } catch (IllegalArgumentException e) {
                throw usage(e.getMessage());
            }
        }
        // A world built the same way checks the later overrides, so a bad one fails before the first line prints.
        World scratch = start(worldSeed, dayZero, fromFiles);
        later.values().forEach(edits -> edits.forEach(edit -> submit(scratch, edit)));
        World world = start(worldSeed, dayZero, fromFiles);
        PrintWriter out = spec.commandLine().getOut();
        List<String> exported = new ArrayList<>();
        for (int completed = 0; completed < days; completed++) {
            later.getOrDefault(completed, List.of()).forEach(world::submit);
            DayReport report = world.advance();
            out.print(CensusLine.format(worldSeed, report.census()) + "\n");
            if (exportDays.contains(report.snapshot().day())) {
                exported.addAll(GenomeLines.format(worldSeed, report.snapshot()));
            }
        }
        out.flush();
        if (export.isPresent()) {
            write(export.orElseThrow(), exported);
        }
        return CommandLine.ExitCode.OK;
    }

    private static void write(Path file, List<String> lines) {
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot write " + file, e);
        }
    }

    private static int dayOf(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void submit(World world, Command command) {
        try {
            world.submit(command);
        } catch (IllegalArgumentException e) {
            throw usage(e.getMessage());
        }
    }

    private World start(long worldSeed, List<Command.SetSetting> dayZero, List<Command.Spawn> fromFiles) {
        Map<String, Integer> population =
                new TreeMap<>(spawn.isEmpty() && fromFiles.isEmpty() ? STARTING_POPULATION : startingPopulation());
        Map<String, Map<String, Double>> genes = startingGenes(population);
        try {
            World world = new World(
                    width,
                    height,
                    worldSeed,
                    List.of(Grass.species(), Rabbit.species()),
                    List.of(new FoodDrop("grass")));
            dayZero.forEach(world::submit);
            population.forEach(
                    (name, count) -> world.submit(new Command.Spawn(name, count, genes.getOrDefault(name, Map.of()))));
            fromFiles.forEach(world::submit);
            return world;
        } catch (IllegalArgumentException e) {
            throw usage(e.getMessage());
        }
    }

    private Map<String, Map<String, Double>> startingGenes(Map<String, Integer> population) {
        Map<String, Map<String, Double>> genes = new TreeMap<>();
        for (String entry : gene) {
            Command.SetSetting parsed = setting("--gene", "SPECIES.GENE", entry);
            if (!population.containsKey(parsed.owner())) {
                throw usage("--gene names " + parsed.owner() + ", which run does not spawn");
            }
            genes.computeIfAbsent(parsed.owner(), _ -> new TreeMap<>()).put(parsed.key(), parsed.value());
        }
        return genes;
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

    /** NAME.KEY=VALUE, where true and false read as 1 and 0. A gene comes back in the same shape as a setting. */
    private Command.SetSetting setting(String flag, String shape, String entry) {
        int dot = entry.indexOf('.');
        int equals = entry.indexOf('=');
        if (dot < 1 || equals < dot + 2 || equals == entry.length() - 1) {
            throw usage(flag + " takes " + shape + "=VALUE, got " + entry);
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
