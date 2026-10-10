package io.github.decoppelelias.humansimulation.web;

import io.github.decoppelelias.humansimulation.domain.CensusRow;
import io.github.decoppelelias.humansimulation.domain.WorldSnapshot;

/** What a viewer is sent: a completed day, or a snapshot changed by commands while paused. */
sealed interface StreamEvent {
    WorldSnapshot snapshot();

    record Day(WorldSnapshot snapshot, CensusRow census) implements StreamEvent {}

    record Snapshot(WorldSnapshot snapshot) implements StreamEvent {}
}
