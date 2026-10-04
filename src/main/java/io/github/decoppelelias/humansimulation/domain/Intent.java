package io.github.decoppelelias.humansimulation.domain;

sealed interface Intent {
    record Move(Direction direction, int distance) implements Intent {
        public Move {
            if (distance < 1) {
                throw new IllegalArgumentException("a move covers at least one tile, got " + distance);
            }
        }
    }

    record Idle() implements Intent {}
}
