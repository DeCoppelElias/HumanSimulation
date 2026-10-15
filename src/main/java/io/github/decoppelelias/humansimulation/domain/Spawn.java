package io.github.decoppelelias.humansimulation.domain;

import java.util.Optional;

/**
 * What the world hands a new member's brain and parts: its genome, its species and its species' values here, the day
 * it arrives, and a starting reserve when a parent paid one.
 */
record Spawn(Genome genome, Species species, SettingValues settings, int day, Optional<Double> reserve) {}
