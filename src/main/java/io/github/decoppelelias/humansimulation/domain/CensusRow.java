package io.github.decoppelelias.humansimulation.domain;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * One day's population per species, the mean of each gene over a species' living members, and the day's deaths per
 * species by cause.
 */
public record CensusRow(
        int day,
        Map<String, Integer> population,
        Map<String, Map<String, Double>> geneMeans,
        Map<String, Map<String, Integer>> deaths) {
    public CensusRow {
        population = Collections.unmodifiableSortedMap(new TreeMap<>(population));
        SortedMap<String, Map<String, Double>> means = new TreeMap<>();
        geneMeans.forEach((species, genes) -> {
            genes.forEach((gene, mean) -> {
                if (!Double.isFinite(mean)) {
                    throw new IllegalArgumentException("the mean of " + species + "'s " + gene + " is " + mean);
                }
            });
            means.put(species, Collections.unmodifiableSortedMap(new TreeMap<>(genes)));
        });
        geneMeans = Collections.unmodifiableSortedMap(means);
        SortedMap<String, Map<String, Integer>> died = new TreeMap<>();
        deaths.forEach((species, causes) -> {
            causes.forEach((cause, count) -> {
                if (count < 1) {
                    throw new IllegalArgumentException(species + " died of " + cause + " " + count + " times");
                }
            });
            died.put(species, Collections.unmodifiableSortedMap(new TreeMap<>(causes)));
        });
        deaths = Collections.unmodifiableSortedMap(died);
    }
}
