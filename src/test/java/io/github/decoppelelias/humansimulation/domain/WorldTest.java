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
}
