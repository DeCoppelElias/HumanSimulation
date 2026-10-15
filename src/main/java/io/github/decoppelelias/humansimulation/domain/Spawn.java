package io.github.decoppelelias.humansimulation.domain;

/** What the world hands a new member's brain and parts: its genome, its species, and its species' values here. */
record Spawn(Genome genome, Species species, SettingValues settings) {}
