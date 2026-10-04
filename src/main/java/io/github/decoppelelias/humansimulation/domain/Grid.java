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
        int tiles;
        try {
            tiles = Math.multiplyExact(width, height);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("a " + width + " by " + height + " grid has too many tiles", e);
        }
        this.width = width;
        this.height = height;
        this.occupants = new ArrayList<>(tiles);
        for (int i = 0; i < tiles; i++) {
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
        if (range < 0) {
            throw new IllegalArgumentException("a range cannot be negative, got " + range);
        }
        long reach = (long) range * range;
        List<GridPosition> found = new ArrayList<>();
        for (long y = Math.max(0, (long) centre.y() - range);
                y <= Math.min(height - 1, (long) centre.y() + range);
                y++) {
            for (long x = Math.max(0, (long) centre.x() - range);
                    x <= Math.min(width - 1, (long) centre.x() + range);
                    x++) {
                GridPosition at = new GridPosition((int) x, (int) y);
                if (squaredDistance(centre, at) <= reach) {
                    found.add(at);
                }
            }
        }
        found.sort(Comparator.comparingLong((GridPosition at) -> squaredDistance(centre, at))
                .thenComparingInt(GridPosition::y)
                .thenComparingInt(GridPosition::x));
        return found;
    }

    List<GridPosition> tiles() {
        List<GridPosition> tiles = new ArrayList<>(occupants.size());
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

    private static long squaredDistance(GridPosition a, GridPosition b) {
        long dx = (long) a.x() - b.x();
        long dy = (long) a.y() - b.y();
        return dx * dx + dy * dy;
    }
}
