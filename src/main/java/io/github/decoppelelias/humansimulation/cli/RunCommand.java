package io.github.decoppelelias.humansimulation.cli;

import io.github.decoppelelias.humansimulation.domain.Command;
import io.github.decoppelelias.humansimulation.domain.Rabbit;
import io.github.decoppelelias.humansimulation.domain.World;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            description = "Starting population, repeatable. Replaces the default of rabbit=10.")
    private Map<String, Integer> spawn = new TreeMap<>();

    @Override
    public Integer call() {
        if (days < 1) {
            throw usage("--days must be at least 1, got " + days);
        }
        long worldSeed = seed.orElseGet(System::nanoTime);
        World world = start(worldSeed);
        PrintWriter out = spec.commandLine().getOut();
        for (int i = 0; i < days; i++) {
            out.print(CensusLine.format(worldSeed, world.advance().census()) + "\n");
        }
        out.flush();
        return CommandLine.ExitCode.OK;
    }

    private World start(long worldSeed) {
        try {
            World world = new World(width, height, worldSeed, List.of(Rabbit.species()));
            new TreeMap<>(spawn.isEmpty() ? STARTING_POPULATION : spawn)
                    .forEach((name, count) -> world.submit(new Command.Spawn(name, count)));
            return world;
        } catch (IllegalArgumentException e) {
            throw usage(e.getMessage());
        }
    }

    private ParameterException usage(String message) {
        return new ParameterException(spec.commandLine(), message);
    }
}
