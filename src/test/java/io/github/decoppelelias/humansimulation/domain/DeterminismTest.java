package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DeterminismTest {
    private static final List<Species> RABBITS = List.of(Rabbit.species());

    private static List<WorldSnapshot> replay(World world, List<LoggedCommand> log, int days) {
        List<WorldSnapshot> snapshots = new ArrayList<>();
        for (int day = 0; day < days; day++) {
            for (LoggedCommand logged : log) {
                if (logged.day() == day) {
                    world.submit(logged.command());
                }
            }
            snapshots.add(world.advance().snapshot());
        }
        return snapshots;
    }

    @Test
    void aSeedAndItsLogReplayExactly() {
        World original = new World(20, 20, 42, RABBITS);
        original.submit(new Command.Spawn("rabbit", 10));
        List<WorldSnapshot> seen = new ArrayList<>();
        for (int day = 0; day < 30; day++) {
            if (day == 10) {
                original.submit(new Command.SpawnAt("rabbit", new GridPosition(3, 4)));
            }
            if (day == 20) {
                original.submit(new Command.Spawn("rabbit", 2));
                original.applyPending();
            }
            seen.add(original.advance().snapshot());
        }

        assertThat(replay(new World(20, 20, 42, RABBITS), original.log(), 30)).isEqualTo(seen);
    }

    @Test
    void aResetStartsALogThatReplaysOnItsOwn() {
        World original = new World(20, 20, 42, RABBITS);
        original.submit(new Command.Spawn("rabbit", 10));
        for (int day = 0; day < 5; day++) {
            original.advance();
        }
        original.submit(new Command.Reset(99));
        original.submit(new Command.Spawn("rabbit", 4));
        List<WorldSnapshot> seen = new ArrayList<>();
        for (int day = 0; day < 20; day++) {
            seen.add(original.advance().snapshot());
        }

        assertThat(original.log().getFirst()).isEqualTo(new LoggedCommand(0, new Command.Reset(99)));
        assertThat(replay(new World(20, 20, 1, RABBITS), original.log(), 20)).isEqualTo(seen);
    }

    @Test
    void differentSeedsGiveDifferentRuns() {
        World one = new World(20, 20, 1, RABBITS);
        World two = new World(20, 20, 2, RABBITS);
        one.submit(new Command.Spawn("rabbit", 10));
        two.submit(new Command.Spawn("rabbit", 10));
        assertThat(WorldTest.occupied(one.advance().snapshot()))
                .isNotEqualTo(WorldTest.occupied(two.advance().snapshot()));
    }
}
