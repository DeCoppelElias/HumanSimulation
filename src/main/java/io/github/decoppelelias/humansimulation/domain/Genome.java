package io.github.decoppelelias.humansimulation.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

record Genome(List<GeneSpec> layout, Map<String, Double> scalars) {
    Genome {
        layout = List.copyOf(layout);
        scalars = Collections.unmodifiableSortedMap(new TreeMap<>(scalars));
        Set<String> declared = new TreeSet<>();
        for (GeneSpec spec : layout) {
            if (!declared.add(spec.key())) {
                throw new IllegalArgumentException("gene " + spec.key() + " is declared twice");
            }
            switch (spec) {
                case GeneSpec.Scalar scalar -> checkScalar(scalar, scalars);
            }
        }
        Set<String> undeclared = new TreeSet<>(scalars.keySet());
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

    private static void checkScalar(GeneSpec.Scalar spec, Map<String, Double> scalars) {
        if (!scalars.containsKey(spec.key())) {
            throw new IllegalArgumentException("scalar gene " + spec.key() + " needs a number");
        }
        double value = scalars.get(spec.key());
        if (!(value >= spec.min() && value <= spec.max())) {
            throw new IllegalArgumentException(
                    "gene " + spec.key() + " is " + value + ", outside " + spec.min() + " to " + spec.max());
        }
    }
}
