package io.github.decoppelelias.humansimulation.cli;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ServeCommandPageTest {
    @Test
    void clockSeedsFitAJavaScriptNumber() {
        long largest = (1L << 53) - 1;
        assertThat(ServeCommand.seedFromClock(Long.MAX_VALUE)).isEqualTo(largest);
        assertThat(ServeCommand.seedFromClock((1L << 53) + 5)).isEqualTo(5);
        assertThat(ServeCommand.seedFromClock(42)).isEqualTo(42);
        assertThat(ServeCommand.seedFromClock(-1)).isEqualTo(largest);
    }
}
