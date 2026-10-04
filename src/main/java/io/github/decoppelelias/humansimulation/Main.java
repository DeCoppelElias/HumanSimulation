package io.github.decoppelelias.humansimulation;

import io.github.decoppelelias.humansimulation.cli.RunCommand;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@CommandLine.Command(
        name = "humansimulation",
        mixinStandardHelpOptions = true,
        subcommands = RunCommand.class,
        description = "A grid world you watch evolve.")
public final class Main implements Runnable {
    @Spec
    private CommandSpec spec;

    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }

    @Override
    public void run() {
        spec.commandLine().usage(spec.commandLine().getOut());
    }
}
