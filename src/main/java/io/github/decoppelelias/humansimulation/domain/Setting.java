package io.github.decoppelelias.humansimulation.domain;

/** A number a rule reads that is not a gene, declared with what an editor needs to show it. */
sealed interface Setting {
    String key();

    record Scalar(String key, String description, double min, double max, double step, double defaultValue)
            implements Setting {
        private static final double STEP_TOLERANCE = 1e-9;

        public Scalar {
            if (key.isBlank()) {
                throw new IllegalArgumentException("a setting needs a key");
            }
            if (description.isBlank()) {
                throw new IllegalArgumentException("setting " + key + " needs a description");
            }
            if (!(min <= max)) {
                throw new IllegalArgumentException("setting " + key + " has min " + min + " above max " + max);
            }
            if (!(step > 0)) {
                throw new IllegalArgumentException("setting " + key + " needs a positive step, got " + step);
            }
            reject(key, min, max, step, defaultValue);
        }

        void check(double value) {
            reject(key, min, max, step, value);
        }

        /** Whole when every value it allows is a whole number. */
        boolean whole() {
            return min == Math.rint(min) && step == Math.rint(step);
        }

        private static void reject(String key, double min, double max, double step, double value) {
            if (!(value >= min && value <= max)) {
                throw new IllegalArgumentException(
                        "setting " + key + " is " + value + ", outside " + min + " to " + max);
            }
            double steps = (value - min) / step;
            if (Math.abs(steps - Math.rint(steps)) > STEP_TOLERANCE) {
                throw new IllegalArgumentException(
                        "setting " + key + " is " + value + ", off its step of " + step + " from " + min);
            }
        }
    }
}
