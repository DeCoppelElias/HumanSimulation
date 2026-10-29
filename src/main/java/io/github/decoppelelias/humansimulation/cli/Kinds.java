package io.github.decoppelelias.humansimulation.cli;

import io.github.decoppelelias.humansimulation.domain.Grass;
import io.github.decoppelelias.humansimulation.domain.Rabbit;
import io.github.decoppelelias.humansimulation.domain.Species;
import io.github.decoppelelias.humansimulation.domain.Wolf;
import java.util.List;

/** The species every world built by run or serve knows. */
final class Kinds {
    private Kinds() {}

    static List<Species> all() {
        return List.of(Grass.species(), Rabbit.species(), Wolf.species());
    }
}
