package io.github.decoppelelias.humansimulation;

import io.github.decoppelelias.humansimulation.domain.Command;
import io.github.decoppelelias.humansimulation.domain.Rabbit;
import io.github.decoppelelias.humansimulation.domain.World;
import java.util.List;

/** Prints every day's whole snapshot, which a census of rabbit counts alone cannot tell apart. */
public final class SnapshotProbe {
    private SnapshotProbe() {}

    public static void main(String[] args) {
        World world = new World(20, 20, Long.parseLong(args[0]), List.of(Rabbit.species()));
        world.submit(new Command.Spawn("rabbit", 10));
        StringBuilder out = new StringBuilder();
        for (int day = 0; day < Integer.parseInt(args[1]); day++) {
            out.append(world.advance().snapshot()).append('\n');
        }
        System.out.print(out);
    }
}
