package io.github.decoppelelias.humansimulation.domain;

import java.util.List;

/** What the world offers a creature when the day starts. The resolver still decides what happens. */
record Options(List<Direction> directions) {
    Options {
        directions = List.copyOf(directions);
    }
}
