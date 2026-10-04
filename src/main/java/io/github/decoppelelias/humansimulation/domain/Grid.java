package io.github.decoppelelias.humansimulation.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

final class Grid {
    private static final List<Direction> DIRECTIONS = List.of(
            new Direction("north", 0, -1),
            new Direction("east", 1, 0),
            new Direction("south", 0, 1),
            new Direction("west", -1, 0));

    private final int width;
    private final int height;
    private final List<TreeSet<Integer>> occupants;

    Grid(int width, int height) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("a grid needs at least one tile, got " + width + " by " + height);
        }
        this.width = width;
        this.height = height;
        this.occupants = new ArrayList<>(width * height);
        for (int i = 0; i < width * height; i++) {
            occupants.add(new TreeSet<>());
        }
    }

    List<Direction> directions() {
        return DIRECTIONS;
    }

    boolean contains(GridPosition at) {
        return at.x() >= 0 && at.x() < width && at.y() >= 0 && at.y() < height;
    }

    GridPosition step(GridPosition from, Direction direction) {
        return new GridPosition(from.x() + direction.dx(), from.y() + direction.dy());
    }

    List<GridPosition> within(GridPosition centre, int range) {
        List<GridPosition> found = new ArrayList<>();
        for (int dy = -range; dy <= range; dy++) {
            for (int dx = -range; dx <= range; dx++) {
                GridPosition at = new GridPosition(centre.x() + dx, centre.y() + dy);
                if (dx * dx + dy * dy <= range * range && contains(at)) {
                    found.add(at);
                }
            }
        }
        found.sort(Comparator.comparingInt((GridPosition at) -> squaredDistance(centre, at))
                .thenComparingInt(GridPosition::y)
                .thenComparingInt(GridPosition::x));
        return found;
    }

    List<GridPosition> tiles() {
        List<GridPosition> tiles = new ArrayList<>(width * height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles.add(new GridPosition(x, y));
            }
        }
        return tiles;
    }

    void place(int id, GridPosition at) {
        tile(at).add(id);
    }

    void remove(int id, GridPosition at) {
        tile(at).remove(id);
    }

    List<Integer> occupants(GridPosition at) {
        return List.copyOf(tile(at));
    }

    private TreeSet<Integer> tile(GridPosition at) {
        if (!contains(at)) {
            throw new IllegalArgumentException(at + " is off the " + width + " by " + height + " grid");
        }
        return occupants.get(at.y() * width + at.x());
    }

    private static int squaredDistance(GridPosition a, GridPosition b) {
        int dx = a.x() - b.x();
        int dy = a.y() - b.y();
        return dx * dx + dy * dy;
    }
}
