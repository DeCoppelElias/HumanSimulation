package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class CensusRowTest {
    @Test
    void aMeanThatIsNotAFiniteNumberIsRejected() {
        assertThatThrownBy(() ->
                        new CensusRow(1, Map.of("rabbit", 1), Map.of("rabbit", Map.of("whim", Double.NaN)), Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("whim");
        assertThatThrownBy(() -> new CensusRow(
                        1, Map.of("rabbit", 1), Map.of("rabbit", Map.of("speed", Double.POSITIVE_INFINITY)), Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aDeathCountIsPositive() {
        assertThatThrownBy(() -> new CensusRow(1, Map.of(), Map.of(), Map.of("grass", Map.of("aged", 0))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("grass")
                .hasMessageContaining("aged");
    }
}
