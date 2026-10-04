package io.github.decoppelelias.humansimulation.cli;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.decoppelelias.humansimulation.domain.CensusRow;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CensusLineTest {
    @Test
    void writesTheSeedTheDayAndThePopulation() {
        assertThat(CensusLine.format(42, new CensusRow(1, Map.of("rabbit", 10))))
                .isEqualTo("{\"seed\":42,\"day\":1,\"population\":{\"rabbit\":10}}");
    }

    @Test
    void listsSpeciesByName() {
        assertThat(CensusLine.format(-3, new CensusRow(7, Map.of("wolf", 0, "rabbit", 4))))
                .isEqualTo("{\"seed\":-3,\"day\":7,\"population\":{\"rabbit\":4,\"wolf\":0}}");
    }

    @Test
    void escapesQuotesAndBackslashesInNames() {
        assertThat(CensusLine.format(1, new CensusRow(1, Map.of("a\"b\\c", 1))))
                .isEqualTo("{\"seed\":1,\"day\":1,\"population\":{\"a\\\"b\\\\c\":1}}");
    }
}
