package io.github.decoppelelias.humansimulation.domain;

import java.math.BigDecimal;

/** A number a rule reads that is not a gene, declared with what an editor needs to show it. */
sealed interface Setting {
    String key();

    record Scalar(String key, String description, double min, double max, double step, double defaultValue)
            implements Setting {
        private static final double STEP_TOLERANCE = 1e-9;

        /** Past this many steps the step check's own rounding error exceeds its tolerance. */
        private static final double MAX_STEPS = 1e6;

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
            if (!((max - min) / step <= MAX_STEPS)) {
                throw new IllegalArgumentException(
                        "setting " + key + " has more than " + (long) MAX_STEPS + " steps from " + min + " to " + max);
            }
            reject(key, min, max, step, max);
            reject(key, min, max, step, defaultValue);
            defaultValue = onStep(min, step, defaultValue);
        }

        void check(double value) {
            reject(key, min, max, step, value);
        }

        /** A value the check let through, stored exactly on its step, so it never reads as edited when it is not. */
        double onStep(double value) {
            return onStep(min, step, value);
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

        /** Decimal arithmetic, so 35 steps of 0.01 come out as 0.35 rather than 0.35000000000000003. */
        private static double onStep(double min, double step, double value) {
            long steps = Math.round((value - min) / step);
            return BigDecimal.valueOf(min)
                    .add(BigDecimal.valueOf(step).multiply(BigDecimal.valueOf(steps)))
                    .doubleValue();
        }
    }

    record Toggle(String key, String description, boolean defaultOn) implements Setting {
        public Toggle {
            if (key.isBlank()) {
                throw new IllegalArgumentException("a setting needs a key");
            }
            if (description.isBlank()) {
                throw new IllegalArgumentException("setting " + key + " needs a description");
            }
        }

        /** Stored as 0 or 1, so a toggle is checked, logged and restored like any number. */
        Scalar asScalar() {
            return new Scalar(key, description, 0, 1, 1, defaultOn ? 1 : 0);
        }
    }
}
