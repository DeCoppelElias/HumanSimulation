package io.github.decoppelelias.humansimulation.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

/** Scores every option by pulls on the features it sees, nearest and summed, and picks one by softmax, per 0023. */
final class ScoringBrain implements Brain {
    static final String WHIM = "whim";
    static final String BREED_PULL = "breedPull";

    /** One feature's pulls: on the nearest one, its hunger term, and on closeness to all of them. */
    record Pull(double near, double hungryNear, double sum) {}

    private final Map<Feature, Pull> pulls;
    private final double whim;
    private final double breedPull;

    ScoringBrain(Map<Feature, Pull> pulls, double whim, double breedPull) {
        this.pulls = Collections.unmodifiableMap(new LinkedHashMap<>(pulls));
        this.whim = whim;
        this.breedPull = breedPull;
    }

    static ScoringBrain of(Spawn spawn) {
        return from(spawn.genome());
    }

    static ScoringBrain from(Genome genome) {
        Map<Feature, Pull> pulls = new LinkedHashMap<>();
        for (Feature feature : Features.ALL) {
            pulls.put(
                    feature,
                    new Pull(
                            genome.scalar(near(feature)),
                            genome.scalar(hungryNear(feature)),
                            genome.scalar(sum(feature))));
        }
        return new ScoringBrain(pulls, genome.scalar(WHIM), genome.scalar(BREED_PULL));
    }

    static String near(Feature feature) {
        return "near." + feature.name();
    }

    static String hungryNear(Feature feature) {
        return "hungry.near." + feature.name();
    }

    static String sum(Feature feature) {
        return "sum." + feature.name();
    }

    /** One gene of each shape for every feature in the catalog. */
    static List<GeneSpec> pullLayout() {
        List<GeneSpec> layout = new ArrayList<>();
        for (Feature feature : Features.ALL) {
            layout.add(new GeneSpec.Scalar(near(feature), -5, 5, 0.2));
            layout.add(new GeneSpec.Scalar(hungryNear(feature), -10, 10, 0.4));
            layout.add(new GeneSpec.Scalar(sum(feature), -2, 2, 0.1));
        }
        return layout;
    }

    @Override
    public Intent decide(Perception perception, Options options, RandomGenerator random) {
        if (options.offered().isEmpty()) {
            return new Intent.Idle();
        }
        return options.offered().get(pick(scores(perception, options), random)).intent();
    }

    /** Each offered option's score, in offer order. */
    double[] scores(Perception perception, Options options) {
        Map<Feature, List<Perception.TileView>> showing = new LinkedHashMap<>();
        pulls.keySet().forEach(feature -> showing.put(feature, new ArrayList<>()));
        for (Perception.TileView tile : perception.tiles()) {
            for (Perception.EntityView entity : tile.entities()) {
                for (Feature feature : entity.parts().features()) {
                    List<Perception.TileView> tiles = showing.get(feature);
                    if (tiles != null) {
                        tiles.add(tile);
                    }
                }
            }
        }
        double urgency = urgency(perception);
        List<Option> offered = options.offered();
        double[] scores = new double[offered.size()];
        for (int i = 0; i < scores.length; i++) {
            scores[i] = switch (offered.get(i)) {
                case Option.Move move -> score(perception, showing, urgency, move.dx(), move.dy());
                case Option.Idle _ -> score(perception, showing, urgency, 0, 0);
                case Option.Breed _ -> score(perception, showing, urgency, 0, 0) + breedPull;
            };
        }
        return scores;
    }

    /** The pull of everything in view on the tile a choice ends the day on. */
    private double score(
            Perception perception, Map<Feature, List<Perception.TileView>> showing, double urgency, int dx, int dy) {
        double score = 0;
        for (Map.Entry<Feature, Pull> entry : pulls.entrySet()) {
            List<Perception.TileView> tiles = showing.get(entry.getKey());
            if (tiles.isEmpty()) {
                continue;
            }
            Pull pull = entry.getValue();
            double nearest = Double.MAX_VALUE;
            for (Perception.TileView tile : tiles) {
                nearest = Math.min(nearest, perception.distance(tile.dx() - dx, tile.dy() - dy));
            }
            score -= (pull.near() + pull.hungryNear() * urgency) * nearest;
            for (Perception.TileView tile : tiles) {
                score += pull.sum() / (1 + perception.distance(tile.dx() - dx, tile.dy() - dy));
            }
        }
        return score;
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
        // Rounding can leave a roll past the last bucket; an option that underflowed to zero is never it.
        return lastWeighted;
    }
}
