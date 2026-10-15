package io.github.decoppelelias.humansimulation.domain;

import java.util.ArrayList;
import java.util.List;

/** Moving: what a creature may walk to today, and how a chosen walk is applied. */
final class MoveRule {
    static final String STRIDE = "stride";

    private MoveRule() {}

    /** Each direction one tile at a time up to the stride, stopping at the edge. None without a stride. */
    static List<Option.Move> options(Grid grid, GridPosition from, SettingValues settings) {
        List<Option.Move> moves = new ArrayList<>();
        int stride = settings.declares(STRIDE) ? settings.whole(STRIDE) : 0;
        for (Direction direction : grid.directions()) {
            GridPosition at = from;
            for (int distance = 1; distance <= stride; distance++) {
                at = grid.step(at, direction);
                if (!grid.contains(at)) {
                    break;
                }
                moves.add(new Option.Move(direction, distance, at.x() - from.x(), at.y() - from.y()));
            }
        }
        return moves;
    }

    /** Walks one tile at a time and stops where the world says it must. */
    static void resolve(Grid grid, Entity entity, Intent.Move move) {
        for (int i = 0; i < move.distance(); i++) {
            GridPosition next = grid.step(entity.position(), move.direction());
            if (!grid.contains(next)) {
                return;
            }
            grid.remove(entity.id(), entity.position());
            grid.place(entity.id(), next);
            entity.moveTo(next);
        }
    }
}
