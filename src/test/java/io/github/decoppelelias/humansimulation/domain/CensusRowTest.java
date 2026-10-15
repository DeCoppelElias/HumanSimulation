package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class CensusRowTest {
    @Test
    void aMeanThatIsNotAFiniteNumberIsRejected() {
        assertThatThrownBy(() -> new CensusRow(1, Map.of("rabbit", 1), Map.of("rabbit", Map.of("whim", Double.NaN))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("whim");
        assertThatThrownBy(() -> new CensusRow(
                        1, Map.of("rabbit", 1), Map.of("rabbit", Map.of("speed", Double.POSITIVE_INFINITY))))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
