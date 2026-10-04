package io.github.decoppelelias.humansimulation.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

record Genome(List<GeneSpec> layout, Map<String, Double> scalars, Map<String, List<Double>> simplexes) {
    private static final double SUM_TOLERANCE = 1e-9;

    Genome {
        layout = List.copyOf(layout);
        scalars = Collections.unmodifiableSortedMap(new TreeMap<>(scalars));
        TreeMap<String, List<Double>> copied = new TreeMap<>(simplexes);
        copied.replaceAll((key, weights) -> List.copyOf(weights));
        simplexes = Collections.unmodifiableSortedMap(copied);

        Set<String> declared = new TreeSet<>();
        for (GeneSpec spec : layout) {
            if (!declared.add(spec.key())) {
                throw new IllegalArgumentException("gene " + spec.key() + " is declared twice");
            }
            switch (spec) {
                case GeneSpec.Scalar scalar -> checkScalar(scalar, scalars, simplexes);
                case GeneSpec.Simplex simplex -> checkSimplex(simplex, scalars, simplexes);
            }
        }
        Set<String> undeclared = new TreeSet<>(scalars.keySet());
        undeclared.addAll(simplexes.keySet());
        undeclared.removeAll(declared);
        if (!undeclared.isEmpty()) {
            throw new IllegalArgumentException("genes " + undeclared + " are not in the layout");
        }
    }

    double scalar(String key) {
        if (!scalars.containsKey(key)) {
            throw new IllegalArgumentException("no scalar gene " + key);
        }
        return scalars.get(key);
    }

    List<Double> simplex(String key) {
        if (!simplexes.containsKey(key)) {
            throw new IllegalArgumentException("no simplex gene " + key);
        }
        return simplexes.get(key);
    }

    private static void checkScalar(
            GeneSpec.Scalar spec, Map<String, Double> scalars, Map<String, List<Double>> simplexes) {
        if (!scalars.containsKey(spec.key()) || simplexes.containsKey(spec.key())) {
            throw new IllegalArgumentException("scalar gene " + spec.key() + " needs exactly one number");
        }
        double value = scalars.get(spec.key());
        if (!(value >= spec.min() && value <= spec.max())) {
            throw new IllegalArgumentException(
                    "gene " + spec.key() + " is " + value + ", outside " + spec.min() + " to " + spec.max());
        }
    }

    private static void checkSimplex(
            GeneSpec.Simplex spec, Map<String, Double> scalars, Map<String, List<Double>> simplexes) {
        if (!simplexes.containsKey(spec.key()) || scalars.containsKey(spec.key())) {
            throw new IllegalArgumentException("simplex gene " + spec.key() + " needs exactly one distribution");
        }
        List<Double> weights = simplexes.get(spec.key());
        if (weights.size() < spec.minLength() || weights.size() > spec.maxLength()) {
            throw new IllegalArgumentException("gene " + spec.key() + " has " + weights.size() + " weights, outside "
                    + spec.minLength() + " to " + spec.maxLength());
        }
        double sum = 0;
        for (double weight : weights) {
            if (!(weight >= 0)) {
                throw new IllegalArgumentException("gene " + spec.key() + " has a negative weight " + weight);
            }
            sum += weight;
        }
        if (Math.abs(sum - 1) > SUM_TOLERANCE) {
            throw new IllegalArgumentException("gene " + spec.key() + " sums to " + sum + ", not 1");
        }
    }
}
