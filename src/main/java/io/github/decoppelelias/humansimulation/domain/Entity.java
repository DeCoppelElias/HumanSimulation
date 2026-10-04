package io.github.decoppelelias.humansimulation.domain;

import java.util.List;
import java.util.Optional;

final class Entity {
    private final int id;
    private final Species species;
    private final Genome genome;
    private final Optional<Brain> brain;
    private final List<Component> parts;
    private GridPosition position;

    Entity(
            int id,
            Species species,
            Genome genome,
            GridPosition position,
            Optional<Brain> brain,
            List<Component> parts) {
        this.id = id;
        this.species = species;
        this.genome = genome;
        this.position = position;
        this.brain = brain;
        this.parts = List.copyOf(parts);
    }

    int id() {
        return id;
    }

    Species species() {
        return species;
    }

    Genome genome() {
        return genome;
    }

    Optional<Brain> brain() {
        return brain;
    }

    List<Component> parts() {
        return parts;
    }

    GridPosition position() {
        return position;
    }

    void moveTo(GridPosition to) {
        position = to;
    }
}
