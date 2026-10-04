package io.github.decoppelelias.humansimulation.domain;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record Species(
        String name,
        String spriteKey,
        List<GeneSpec> geneLayout,
        Genome baseline,
        Optional<Function<Spawn, Brain>> brain,
        List<Function<Spawn, Component>> parts) {
    public Species {
        if (name.isBlank()) {
            throw new IllegalArgumentException("a species needs a name");
        }
        if (spriteKey.isBlank()) {
            throw new IllegalArgumentException("species " + name + " needs a sprite key");
        }
        geneLayout = List.copyOf(geneLayout);
        parts = List.copyOf(parts);
        if (!baseline.layout().equals(geneLayout)) {
            throw new IllegalArgumentException("species " + name + " has a baseline genome of another layout");
        }
    }
}
