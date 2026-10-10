package io.github.decoppelelias.humansimulation.web;

import io.github.decoppelelias.humansimulation.domain.Species;
import java.util.List;

/** The worlds a server holds, in the order they were created. */
public final class Worlds {
    public Worlds(int cap, List<Species> species) {}

    List<WorldHost> all() {
        return List.of();
    }
}
