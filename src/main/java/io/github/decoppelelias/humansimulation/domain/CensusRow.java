package io.github.decoppelelias.humansimulation.domain;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/** One day's population per species, and the mean of each gene over a species' living members. */
public record CensusRow(int day, Map<String, Integer> population, Map<String, Map<String, Double>> geneMeans) {
    public CensusRow {
        population = Collections.unmodifiableSortedMap(new TreeMap<>(population));
        SortedMap<String, Map<String, Double>> means = new TreeMap<>();
        geneMeans.forEach(
                (species, genes) -> means.put(species, Collections.unmodifiableSortedMap(new TreeMap<>(genes))));
        geneMeans = Collections.unmodifiableSortedMap(means);
    }
}
