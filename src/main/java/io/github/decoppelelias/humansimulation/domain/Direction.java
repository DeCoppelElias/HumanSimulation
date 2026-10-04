package io.github.decoppelelias.humansimulation.domain;

record Direction(String name, int dx, int dy) {
    Direction {
        if (name.isBlank()) {
            throw new IllegalArgumentException("a direction needs a name");
        }
        if (dx == 0 && dy == 0) {
            throw new IllegalArgumentException("direction " + name + " goes nowhere");
        }
    }
}
