package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class WorldTest {
    private static final List<Species> RABBITS = List.of(Rabbit.species());

    static List<GridPosition> occupied(WorldSnapshot snapshot) {
        return snapshot.tiles().stream()
                .flatMap(tile -> tile.entities().stream().map(entity -> tile.at()))
                .toList();
    }

    @Test
    void aNewWorldIsEmptyOnDayZero() {
        WorldSnapshot snapshot = new World(4, 3, 42, RABBITS).snapshot();
        assertThat(snapshot.seed()).isEqualTo(42);
        assertThat(snapshot.day()).isZero();
        assertThat(snapshot.width()).isEqualTo(4);
        assertThat(snapshot.height()).isEqualTo(3);
        assertThat(snapshot.tiles()).hasSize(12);
        assertThat(occupied(snapshot)).isEmpty();
    }

    @Test
    void aWorldNeedsAtLeastOneTile() {
        assertThatThrownBy(() -> new World(0, 3, 42, RABBITS)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void twoSpeciesCannotShareAName() {
        assertThatThrownBy(() -> new World(3, 3, 42, List.of(Rabbit.species(), Rabbit.species())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rabbit");
    }

    @Test
    void aSpawnWaitsForPendingCommandsToApply() {
        World world = new World(5, 5, 1, RABBITS);
        world.submit(new Command.Spawn("rabbit", 3));
        assertThat(occupied(world.snapshot())).isEmpty();
        assertThat(occupied(world.applyPending())).hasSize(3);
    }

    @Test
    void applyPendingShowsCommandsAndKeepsTheDay() {
        World world = new World(5, 5, 1, RABBITS);
        world.submit(new Command.Spawn("rabbit", 2));
        WorldSnapshot snapshot = world.applyPending();
        assertThat(snapshot.day()).isZero();
        assertThat(occupied(snapshot)).hasSize(2);
    }

    @Test
    void spawnAtPlacesOneOnThatTile() {
        World world = new World(5, 5, 1, RABBITS);
        world.submit(new Command.SpawnAt("rabbit", new GridPosition(3, 4)));
        assertThat(occupied(world.applyPending())).containsExactly(new GridPosition(3, 4));
    }

    @Test
    void aSpawnedCountMayShareTiles() {
        World world = new World(1, 1, 1, RABBITS);
        world.submit(new Command.Spawn("rabbit", 3));
        WorldSnapshot snapshot = world.applyPending();
        assertThat(snapshot.tiles().getFirst().entities())
                .extracting(WorldSnapshot.EntityView::id)
                .containsExactly(0, 1, 2);
    }

    @Test
    void aRejectedCommandChangesNothing() {
        World world = new World(5, 5, 1, RABBITS);
        world.submit(new Command.Spawn("rabbit", 2));
        WorldSnapshot before = world.applyPending();
        List<LoggedCommand> logBefore = world.log();

        assertThatThrownBy(() -> world.submit(new Command.Spawn("wolf", 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("wolf");
        assertThatThrownBy(() -> world.submit(new Command.SpawnAt("rabbit", new GridPosition(5, 0))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Command.Spawn("rabbit", 0)).isInstanceOf(IllegalArgumentException.class);

        assertThat(world.applyPending()).isEqualTo(before);
        assertThat(world.log()).isEqualTo(logBefore);
    }

    @Test
    void theLogRecordsEachCommandWithTheDayCounterWhenItDrained() {
        World world = new World(5, 5, 1, RABBITS);
        Command spawn = new Command.Spawn("rabbit", 2);
        world.submit(spawn);
        world.applyPending();
        assertThat(world.log()).containsExactly(new LoggedCommand(0, spawn));
    }
}
