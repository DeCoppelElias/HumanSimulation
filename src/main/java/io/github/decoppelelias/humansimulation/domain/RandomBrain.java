package io.github.decoppelelias.humansimulation.domain;

import java.util.List;
import java.util.random.RandomGenerator;

final class RandomBrain implements Brain {
    private final List<Double> steps;

    RandomBrain(List<Double> steps) {
        this.steps = List.copyOf(steps);
    }

    @Override
    public Intent decide(Perception perception, Options options, RandomGenerator random) {
        List<Direction> directions = options.directions();
        int pick = random.nextInt(directions.size() + 1);
        if (pick == directions.size()) {
            return new Intent.Idle();
        }
        return new Intent.Move(directions.get(pick), distance(random.nextDouble()));
    }

    private int distance(double roll) {
        double cumulative = 0;
        for (int i = 0; i < steps.size(); i++) {
            cumulative += steps.get(i);
            if (roll < cumulative) {
                return i + 1;
            }
        }
        return steps.size();
    }
}
