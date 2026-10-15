package io.github.decoppelelias.humansimulation.domain;

import java.util.Optional;

/** Breeding: when a creature may breed, and what a breed costs it. The world places the child. */
final class BreedRule {
    static final String BREED_INTERVAL = "breedInterval";
    static final String CHILD_RESERVE = "childReserve";
    static final String BREEDING_LOSS = "breedingLoss";
    static final String PARENT_MARGIN = "parentMargin";

    private BreedRule() {}

    static Optional<Option.Breed> options(Entity entity, SettingValues settings, int today) {
        return legal(entity, settings, today) ? Optional.of(new Option.Breed()) : Optional.empty();
    }

    /** Pays for a child and restarts the window, returning the child's reserve; empty when it may not breed. */
    static Optional<Double> resolve(Entity parent, SettingValues settings, int today) {
        if (!legal(parent, settings, today)) {
            return Optional.empty();
        }
        double child = settings.number(CHILD_RESERVE);
        parent.part(Metabolism.class).ifPresent(metabolism -> metabolism.spend(child + settings.number(BREEDING_LOSS)));
        parent.part(Fertility.class).ifPresent(fertility -> fertility.bred(today));
        return Optional.of(child);
    }

    /** The window is open, and the reserve covers the child, the loss and the margin the parent keeps. */
    private static boolean legal(Entity entity, SettingValues settings, int today) {
        Optional<Fertility> fertility = entity.part(Fertility.class);
        Optional<Metabolism> metabolism = entity.part(Metabolism.class);
        if (fertility.isEmpty() || metabolism.isEmpty()) {
            return false;
        }
        double needed =
                settings.number(CHILD_RESERVE) + settings.number(BREEDING_LOSS) + settings.number(PARENT_MARGIN);
        return fertility.orElseThrow().open(today, settings.whole(BREED_INTERVAL))
                && metabolism.orElseThrow().reserve() >= needed;
    }
}
