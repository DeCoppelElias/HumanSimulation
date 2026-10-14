package io.github.decoppelelias.humansimulation.domain;

import java.util.List;

/** What the world offers a creature when the day starts. The resolver still decides what happens. */
record Options(List<Direction> directions, List<Landing> landings) {
    /** A tile a creature could end the day on, relative to it, and the intent that walks there. */
    record Landing(Intent intent, int dx, int dy) {}

    Options {
        directions = List.copyOf(directions);
        landings = List.copyOf(landings);
    }
}
