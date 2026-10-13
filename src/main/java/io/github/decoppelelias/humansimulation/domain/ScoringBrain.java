package io.github.decoppelelias.humansimulation.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/** Scores every landing by its pull toward grass and other rabbits, and picks one by softmax, per 0023. */
final class ScoringBrain implements Brain {
    private final String species;
    private final double grassPull;
    private final double hungerDrive;
    private final double rabbitPull;
    private final double whim;

    ScoringBrain(String species, double grassPull, double hungerDrive, double rabbitPull, double whim) {
        this.species = species;
        this.grassPull = grassPull;
        this.hungerDrive = hungerDrive;
        this.rabbitPull = rabbitPull;
        this.whim = whim;
    }

    static ScoringBrain of(Spawn spawn) {
        Genome genome = spawn.genome();
        return new ScoringBrain(
                spawn.species().name(),
                genome.scalar("grassPull"),
                genome.scalar("hungerDrive"),
                genome.scalar("rabbitPull"),
                genome.scalar("whim"));
    }

    @Override
    public Intent decide(Perception perception, Options options, RandomGenerator random) {
        List<Options.Landing> landings = options.landings();
        if (landings.isEmpty()) {
            return new Intent.Idle();
        }
        List<Perception.TileView> grass = new ArrayList<>();
        List<Perception.TileView> rabbits = new ArrayList<>();
        for (Perception.TileView tile : perception.tiles()) {
            for (Perception.EntityView entity : tile.entities()) {
                if (entity.parts().has(Edible.class)) {
                    grass.add(tile);
                }
                if (entity.species().equals(species)) {
                    rabbits.add(tile);
                }
            }
        }
        double weight = grassPull + hungerDrive * urgency(perception);
        double[] scores = new double[landings.size()];
        for (int i = 0; i < scores.length; i++) {
            Options.Landing landing = landings.get(i);
            double score = 0;
            if (!grass.isEmpty()) {
                double nearest = Double.MAX_VALUE;
                for (Perception.TileView tile : grass) {
                    nearest =
                            Math.min(nearest, perception.distance(tile.dx() - landing.dx(), tile.dy() - landing.dy()));
                }
                score -= weight * nearest;
            }
            for (Perception.TileView tile : rabbits) {
                score += rabbitPull / (1 + perception.distance(tile.dx() - landing.dx(), tile.dy() - landing.dy()));
            }
            scores[i] = score;
        }
        return landings.get(pick(scores, random)).intent();
    }

    private static double urgency(Perception perception) {
        return perception
                .self()
                .parts()
                .view(Metabolism.Own.class)
                .map(own -> 1.0 / (1 + own.daysLeft()))
                .orElse(0.0);
    }

    /** One draw; scores are shifted by their maximum first, so a large score cannot overflow. */
    private int pick(double[] scores, RandomGenerator random) {
        double best = Double.NEGATIVE_INFINITY;
        for (double score : scores) {
            best = Math.max(best, score);
        }
        double[] weights = new double[scores.length];
        double total = 0;
        int lastWeighted = 0;
        for (int i = 0; i < scores.length; i++) {
            weights[i] = Math.exp((scores[i] - best) / whim);
            total += weights[i];
            if (weights[i] > 0) {
                lastWeighted = i;
            }
        }
        double roll = random.nextDouble() * total;
        for (int i = 0; i < weights.length; i++) {
            roll -= weights[i];
            if (roll < 0) {
                return i;
            }
        }
        // Rounding can leave a roll past the last bucket; a landing that underflowed to zero is never it.
        return lastWeighted;
    }
}
