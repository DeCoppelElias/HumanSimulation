package io.github.decoppelelias.humansimulation.domain;

/** Something a creature may choose today. Each case returns the intent of its name. */
sealed interface Option {
    Intent intent();

    /** A walk the grid has checked, with the tile it lands on relative to the walker. */
    record Move(Direction direction, int distance, int dx, int dy) implements Option {
        @Override
        public Intent intent() {
            return new Intent.Move(direction, distance);
        }
    }

    record Idle() implements Option {
        @Override
        public Intent intent() {
            return new Intent.Idle();
        }
    }

    record Breed() implements Option {
        @Override
        public Intent intent() {
            return new Intent.Breed();
        }
    }
}
