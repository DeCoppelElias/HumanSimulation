package io.github.decoppelelias.humansimulation.domain;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public record CensusRow(int day, Map<String, Integer> population) {
    public CensusRow {
        population = Collections.unmodifiableSortedMap(new TreeMap<>(population));
    }
}
