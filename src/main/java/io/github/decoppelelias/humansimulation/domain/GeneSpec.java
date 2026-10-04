package io.github.decoppelelias.humansimulation.domain;

sealed interface GeneSpec {
    String key();

    record Scalar(String key, double min, double max, double mutationSize) implements GeneSpec {
        public Scalar {
            if (key.isBlank()) {
                throw new IllegalArgumentException("a gene needs a key");
            }
            if (!(min <= max)) {
                throw new IllegalArgumentException("gene " + key + " has min " + min + " above max " + max);
            }
            if (!(mutationSize >= 0)) {
                throw new IllegalArgumentException("gene " + key + " has a negative mutation size " + mutationSize);
            }
        }
    }

    record Simplex(String key, int minLength, int maxLength, double mutationSize) implements GeneSpec {
        public Simplex {
            if (key.isBlank()) {
                throw new IllegalArgumentException("a gene needs a key");
            }
            if (minLength < 1 || minLength > maxLength) {
                throw new IllegalArgumentException(
                        "gene " + key + " has lengths " + minLength + " to " + maxLength + ", not a range from one up");
            }
            if (!(mutationSize >= 0)) {
                throw new IllegalArgumentException("gene " + key + " has a negative mutation size " + mutationSize);
            }
        }
    }
}
