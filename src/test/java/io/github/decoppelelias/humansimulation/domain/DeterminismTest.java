package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    void aRunWithFoodReplaysExactly() {
        List<Species> known = List.of(Grass.species(), Rabbit.species());
        List<WorldProcess> drop = List.of(new FoodDrop("grass"));
        World original = new World(12, 12, 42, known, drop);
        original.submit(new Command.Spawn("rabbit", 15));
        original.submit(new Command.Spawn("grass", 10));
        List<WorldSnapshot> seen = new ArrayList<>();
        for (int day = 0; day < 60; day++) {
            if (day == 25) {
                original.submit(new Command.SpawnAt("grass", new GridPosition(3, 4)));
            }
            seen.add(original.advance().snapshot());
        }
        assertThat(seen.getLast().day()).isEqualTo(60);
        assertThat(replay(new World(12, 12, 42, known, drop), original.log(), 60))
                .isEqualTo(seen);
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
    void aRunWithBreedingAndEditsReplaysExactly() {
        List<Species> known = List.of(Grass.species(), Rabbit.species());
        List<WorldProcess> drop = List.of(new FoodDrop("grass"));
        World original = new World(15, 15, 42, known, drop);
        original.submit(new Command.Spawn("rabbit", 15));
        original.submit(new Command.Spawn("grass", 60));
        List<WorldSnapshot> seen = new ArrayList<>();
        int most = 0;
        for (int day = 0; day < 120; day++) {
            if (day == 40) {
                original.submit(new Command.SetSetting("rabbit", "breedingLoss", 20));
            }
            if (day == 80) {
                original.submit(new Command.RestoreDefaults(Optional.empty(), Optional.empty()));
            }
            DayReport report = original.advance();
            most = Math.max(most, report.census().population().get("rabbit"));
            seen.add(report.snapshot());
        }
        assertThat(most).isGreaterThan(15);
        assertThat(replay(new World(15, 15, 42, known, drop), original.log(), 120))
                .isEqualTo(seen);
    }

    @Test
    void aResetWithEditsReplaysOnItsOwn() {
        World original = new World(20, 20, 42, RABBITS);
        original.submit(new Command.SetSetting("rabbit", "stride", 1));
        original.submit(new Command.Spawn("rabbit", 10));
        original.advance();
        original.submit(new Command.Reset(99));
        original.submit(new Command.Spawn("rabbit", 4));
        List<WorldSnapshot> seen = new ArrayList<>();
        for (int day = 0; day < 20; day++) {
            seen.add(original.advance().snapshot());
        }
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
