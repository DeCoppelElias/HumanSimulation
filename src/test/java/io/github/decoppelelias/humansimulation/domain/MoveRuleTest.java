package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class MoveRuleTest {
    private static final Direction SOUTH = new Direction("south", 0, 1);

    private static SettingValues stride(int stride) {
        return new SettingValues(
                new Settings(List.of(new Setting.Scalar(MoveRule.STRIDE, "Farthest walk.", 1, 5, 1, stride))));
    }

    @Test
    void offersEachDirectionOneTileAtATimeUpToTheStrideAndNeverOffTheGrid() {
        assertThat(MoveRule.options(new Grid(5, 5), new GridPosition(0, 0), stride(3)))
                .containsExactly(
                        new Option.Move(TestSpecies.EAST, 1, 1, 0),
                        new Option.Move(TestSpecies.EAST, 2, 2, 0),
                        new Option.Move(TestSpecies.EAST, 3, 3, 0),
                        new Option.Move(SOUTH, 1, 0, 1),
                        new Option.Move(SOUTH, 2, 0, 2),
                        new Option.Move(SOUTH, 3, 0, 3));
    }

    @Test
    void aSpeciesWithNoStrideIsOfferedNoMoves() {
        assertThat(MoveRule.options(new Grid(5, 5), new GridPosition(2, 2), new SettingValues(Settings.NONE)))
                .isEmpty();
    }
}
