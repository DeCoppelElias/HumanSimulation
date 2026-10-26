package io.github.decoppelelias.humansimulation.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.decoppelelias.humansimulation.domain.CensusRow;
import io.github.decoppelelias.humansimulation.domain.WorldSnapshot;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class ViewerTest {
    /** Ends the stream once it has sent as many events as the test expects. */
    private static final class RecordingSink implements Viewer.Sink {
        private final int expected;
        final List<String> sent = new CopyOnWriteArrayList<>();

        RecordingSink(int expected) {
            this.expected = expected;
        }

        @Override
        public void sendEvent(String name, Object data) {
            sent.add(name + " " + data);
        }

        @Override
        public void sendComment(String comment) {}

        @Override
        public boolean terminated() {
            return sent.size() >= expected;
        }

        @Override
        public void close() {}
    }

    private static StreamEvent.Day day(int number) {
        return new StreamEvent.Day(
                new WorldSnapshot(1, number, 1, 1, List.of(), List.of(), Map.of()),
                new CensusRow(number, Map.of(), Map.of(), Map.of()));
    }

    @Test
    @Timeout(5)
    void aDayDoesNotOverwriteAWaitingPlayState() {
        RecordingSink sink = new RecordingSink(2);
        Viewer viewer = new Viewer(sink, Duration.ofSeconds(10));
        viewer.playState(new WorldHost.PlayState(false, 7));
        viewer.offer(day(1));
        viewer.offer(day(2));
        viewer.offer(day(3));
        viewer.run();
        assertThat(sink.sent).hasSize(2);
        assertThat(sink.sent.get(0)).startsWith("playState PlayState[playing=false, daysPerSecond=7.0]");
        assertThat(sink.sent.get(1)).startsWith("day Day[snapshot=WorldSnapshot[seed=1, day=3");
    }

    @Test
    @Timeout(5)
    void aStatisticsResetKeepsTheWaitingPlayState() {
        RecordingSink sink = new RecordingSink(2);
        Viewer viewer = new Viewer(sink, Duration.ofSeconds(10));
        viewer.playState(new WorldHost.PlayState(true, 2));
        viewer.censusReset();
        viewer.run();
        assertThat(sink.sent).containsExactly("censusReset {}", "playState PlayState[playing=true, daysPerSecond=2.0]");
    }

    @Test
    @Timeout(5)
    void aStatisticsResetKeepsTheWaitingDaysSnapshotButNotItsCensusRow() {
        RecordingSink sink = new RecordingSink(2);
        Viewer viewer = new Viewer(sink, Duration.ofSeconds(10));
        viewer.offer(day(4));
        viewer.censusReset();
        viewer.run();
        assertThat(sink.sent).hasSize(2);
        assertThat(sink.sent.get(0)).isEqualTo("censusReset {}");
        assertThat(sink.sent.get(1)).startsWith("snapshot Snapshot[snapshot=WorldSnapshot[seed=1, day=4");
    }
}
