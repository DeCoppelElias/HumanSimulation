package io.github.decoppelelias.humansimulation.cli;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.decoppelelias.humansimulation.domain.CensusRow;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CensusLineTest {
    @Test
    void writesTheSeedTheDayAndThePopulation() {
        assertThat(CensusLine.format(42, new CensusRow(1, Map.of("rabbit", 10), Map.of(), Map.of())))
                .isEqualTo("{\"seed\":42,\"day\":1,\"population\":{\"rabbit\":10},\"geneMeans\":{},\"deaths\":{}}");
    }

    @Test
    void listsSpeciesByName() {
        assertThat(CensusLine.format(-3, new CensusRow(7, Map.of("wolf", 0, "rabbit", 4), Map.of(), Map.of())))
                .isEqualTo("{\"seed\":-3,\"day\":7,\"population\":{\"rabbit\":4,\"wolf\":0},\"geneMeans\":{},"
                        + "\"deaths\":{}}");
    }

    @Test
    void escapesQuotesAndBackslashesInNames() {
        assertThat(CensusLine.format(
                        1, new CensusRow(1, Map.of("a\"b\\c", 1), Map.of("a\"b\\c", Map.of("x", 1.0)), Map.of())))
                .isEqualTo("{\"seed\":1,\"day\":1,\"population\":{\"a\\\"b\\\\c\":1},"
                        + "\"geneMeans\":{\"a\\\"b\\\\c\":{\"x\":1.0}},\"deaths\":{}}");
    }

    @Test
    void writesGeneMeansPerSpecies() {
        CensusRow row = new CensusRow(
                2, Map.of("grass", 4, "rabbit", 2), Map.of("rabbit", Map.of("whim", 0.5, "speed", 0.25)), Map.of());
        assertThat(CensusLine.format(42, row))
                .isEqualTo("{\"seed\":42,\"day\":2,\"population\":{\"grass\":4,\"rabbit\":2},"
                        + "\"geneMeans\":{\"rabbit\":{\"speed\":0.25,\"whim\":0.5}},\"deaths\":{}}");
    }

    @Test
    void printsDeathsBySpeciesAndCause() {
        CensusRow row = new CensusRow(3, Map.of("grass", 2), Map.of(), Map.of("grass", Map.of("eaten", 4, "aged", 1)));
        assertThat(CensusLine.format(1, row))
                .isEqualTo("{\"seed\":1,\"day\":3,\"population\":{\"grass\":2},\"geneMeans\":{},"
                        + "\"deaths\":{\"grass\":{\"aged\":1,\"eaten\":4}}}");
    }
}
