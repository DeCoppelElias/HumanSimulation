package io.github.decoppelelias.humansimulation.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ServeCommandIdleTest {
    @Test
    void anAgentServerIdlesAfterTenMinutesAndAPersonsAfterADay() {
        assertThat(ServeCommand.idleMinutes(true, Optional.empty())).isEqualTo(10);
        assertThat(ServeCommand.idleMinutes(false, Optional.empty())).isEqualTo(24 * 60);
    }

    @Test
    void anAgentServerQuitsWhenIdleAndAPersonsPauses() {
        List<String> called = new ArrayList<>();
        ServeCommand.onIdle(true, () -> called.add("quit"), () -> called.add("pause"))
                .run();
        ServeCommand.onIdle(false, () -> called.add("quit"), () -> called.add("pause"))
                .run();
        assertThat(called).containsExactly("quit", "pause");
    }

    @Test
    void theReadyLineBracketsAnIpv6Host() {
        assertThat(ServeCommand.readyLine("127.0.0.1", 7070))
                .isEqualTo("{\"event\":\"ready\",\"url\":\"http://127.0.0.1:7070\",\"port\":7070}");
        assertThat(ServeCommand.readyLine("::1", 7070))
                .isEqualTo("{\"event\":\"ready\",\"url\":\"http://[::1]:7070\",\"port\":7070}");
    }

    @Test
    void theFlagOverridesEitherDefault() {
        assertThat(ServeCommand.idleMinutes(true, Optional.of(0))).isZero();
        assertThat(ServeCommand.idleMinutes(false, Optional.of(30))).isEqualTo(30);
    }
}
