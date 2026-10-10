package io.github.decoppelelias.humansimulation.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ServeCommandIdleTest {
    @Test
    void anAgentServerIdlesAfterTenMinutesAndAPersonsAfterADay() {
        assertThat(ServeCommand.idleMinutes(true, Optional.empty())).isEqualTo(10);
        assertThat(ServeCommand.idleMinutes(false, Optional.empty())).isEqualTo(24 * 60);
    }

    @Test
    void theFlagOverridesEitherDefault() {
        assertThat(ServeCommand.idleMinutes(true, Optional.of(0))).isZero();
        assertThat(ServeCommand.idleMinutes(false, Optional.of(30))).isEqualTo(30);
    }
}
