package io.github.decoppelelias.humansimulation.domain;

/** One declared setting as an editor shows it, with the owner whose group it sits in and its shape. */
public record SettingDeclaration(
        String owner,
        String key,
        String shape,
        String description,
        double min,
        double max,
        double step,
        double defaultValue) {}
