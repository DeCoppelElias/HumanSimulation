package io.github.decoppelelias.humansimulation.domain;

/** A gene's range and its species' baseline value, which a page measures a genome against. */
public record GeneRange(String key, double min, double max, double baseline) {}
