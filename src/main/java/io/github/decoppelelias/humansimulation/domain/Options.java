package io.github.decoppelelias.humansimulation.domain;

import java.util.List;

/** What the world offers a creature when the day starts, in a fixed order. The resolver still decides what happens. */
record Options(List<Direction> directions, List<Option> offered) {
    Options {
        directions = List.copyOf(directions);
        offered = List.copyOf(offered);
    }
}
