package io.github.decoppelelias.humansimulation.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class RunCommandTest {
    private record Result(int exitCode, String out, String err) {}

    private static Result run(String... args) {
        StringWriter out = new StringWriter();
        StringWriter err = new StringWriter();
        int exitCode = new CommandLine(new RunCommand())
                .setOut(new PrintWriter(out))
                .setErr(new PrintWriter(err))
                .execute(args);
        return new Result(exitCode, out.toString(), err.toString());
    }

    @Test
    void printsOneLinePerDayFromDayOne() {
        Result result = run("--seed", "42", "--days", "3");
        assertThat(result.exitCode()).isZero();
        assertThat(result.out().split("\n"))
                .hasSize(3)
                .satisfiesExactly(
                        line -> assertThat(line)
                                .startsWith("{\"seed\":42,\"day\":1,\"population\":{\"grass\":0,\"rabbit\":10},"
                                        + "\"geneMeans\":{\"rabbit\":{\"breedPull\":"),
                        line -> assertThat(line)
                                .startsWith("{\"seed\":42,\"day\":2,\"population\":{\"grass\":0,\"rabbit\":10},"
                                        + "\"geneMeans\":{\"rabbit\":{\"breedPull\":"),
                        line -> assertThat(line)
                                .startsWith("{\"seed\":42,\"day\":3,\"population\":{\"grass\":0,\"rabbit\":10},"
                                        + "\"geneMeans\":{\"rabbit\":{\"breedPull\":"));
    }

    @Test
    void theSameSeedPrintsTheSameLines() {
        assertThat(run("--seed", "7", "--days", "20").out())
                .isEqualTo(run("--seed", "7", "--days", "20").out());
    }

    @Test
    void aSpawnReplacesTheDefaultStartingPopulation() {
        assertThat(run("--seed", "1", "--days", "1", "--spawn", "rabbit=3").out())
                .startsWith("{\"seed\":1,\"day\":1,\"population\":{\"grass\":0,\"rabbit\":3},"
                        + "\"geneMeans\":{\"rabbit\":{");
    }

    @Test
    void spawnsGrassAndCountsIt() {
        assertThat(run("--seed", "1", "--days", "1", "--spawn", "grass=3").out())
                .isEqualTo(
                        "{\"seed\":1,\"day\":1,\"population\":{\"grass\":3,\"rabbit\":0},\"geneMeans\":{},\"deaths\":{}}\n");
    }

    @Test
    void theSeedDefaultsToTheClockAndIsPrinted() {
        assertThat(run("--days", "1").out())
                .matches("\\{\"seed\":-?\\d+,\"day\":1,\"population\":\\{\"grass\":0,\"rabbit\":10},"
                        + "\"geneMeans\":\\{\"rabbit\":\\{.*}},\"deaths\":\\{.*}}\n");
    }

    @Test
    void daysIsRequired() {
        assertThat(run("--seed", "1").exitCode()).isEqualTo(CommandLine.ExitCode.USAGE);
    }

    @Test
    void daysBelowOneIsAUsageError() {
        Result result = run("--days", "0");
        assertThat(result.exitCode()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(result.err()).contains("--days");
    }

    @Test
    void anUnknownSpeciesIsAUsageError() {
        Result result = run("--days", "1", "--spawn", "wolf=2");
        assertThat(result.exitCode()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(result.err()).contains("wolf");
    }

    @Test
    void aGridWithNoTilesIsAUsageError() {
        assertThat(run("--days", "1", "--width", "0").exitCode()).isEqualTo(CommandLine.ExitCode.USAGE);
    }

    @Test
    void aGridTooLargeToCountIsAUsageError() {
        assertThat(run("--days", "1", "--width", "65536", "--height", "65536").exitCode())
                .isEqualTo(CommandLine.ExitCode.USAGE);
    }

    @Test
    void aSpeciesSpawnedTwiceIsAUsageError() {
        Result result = run("--days", "1", "--spawn", "rabbit=3", "--spawn", "rabbit=5");
        assertThat(result.exitCode()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(result.err()).contains("rabbit");
    }

    @Test
    void aSpawnThatIsNotSpeciesEqualsCountIsAUsageError() {
        assertThat(run("--days", "1", "--spawn", "rabbit").exitCode()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(run("--days", "1", "--spawn", "rabbit=many").exitCode()).isEqualTo(CommandLine.ExitCode.USAGE);
    }

    @Test
    void aSpawnOfZeroIsAUsageError() {
        assertThat(run("--days", "1", "--spawn", "rabbit=0").exitCode()).isEqualTo(CommandLine.ExitCode.USAGE);
    }

    @Test
    void aSetReachesTheStartingPopulation() {
        assertThat(run("--seed", "1", "--days", "1", "--set", "rabbit.startingReserve=0")
                        .out())
                .startsWith("{\"seed\":1,\"day\":1,\"population\":{\"grass\":0,\"rabbit\":0},");
    }

    @Test
    void aToggleTakesTrueOrFalse() {
        Result words = run("--seed", "1", "--days", "3", "--set", "world.mutation=false");
        assertThat(words.exitCode()).isZero();
        assertThat(words.out())
                .isEqualTo(run("--seed", "1", "--days", "3", "--set", "world.mutation=0")
                        .out())
                .isNotEqualTo(run("--seed", "1", "--days", "3").out());
    }

    @Test
    void anUnknownSettingIsAUsageError() {
        Result result = run("--seed", "1", "--days", "1", "--set", "rabbit.wings=1");
        assertThat(result.exitCode()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(result.err()).contains("no setting wings");
        assertThat(result.out()).isEmpty();
    }

    @Test
    void aValueOutOfBoundsIsAUsageError() {
        Result result = run("--seed", "1", "--days", "1", "--set", "rabbit.stride=9");
        assertThat(result.exitCode()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(result.err()).contains("stride", "outside");
    }

    @Test
    void aMalformedSetIsAUsageError() {
        for (String bad : List.of("stride=3", "rabbit.stride", "rabbit.stride=", ".stride=3", "rabbit.stride=fast")) {
            Result result = run("--seed", "1", "--days", "1", "--set", bad);
            assertThat(result.exitCode()).as(bad).isEqualTo(CommandLine.ExitCode.USAGE);
            assertThat(result.err()).as(bad).containsAnyOf("--set takes OWNER.KEY=VALUE", "--set needs a number");
        }
    }

    @Test
    void aGeneStartsTheSpawnFromIt() {
        Result result =
                run("--seed", "1", "--days", "1", "--set", "world.mutation=0", "--gene", "rabbit.grassPull=0.2");
        assertThat(result.exitCode()).isZero();
        assertThat(result.out()).contains("\"grassPull\":0.2,");
    }

    @Test
    void aGeneForASpeciesRunDoesNotSpawnIsAUsageError() {
        Result result = run("--seed", "1", "--days", "1", "--spawn", "grass=1", "--gene", "rabbit.grassPull=0.2");
        assertThat(result.exitCode()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(result.err()).contains("--gene names rabbit");
    }

    @Test
    void anUnknownGeneIsAUsageError() {
        Result result = run("--seed", "1", "--days", "1", "--gene", "rabbit.wings=1");
        assertThat(result.exitCode()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(result.err()).contains("[wings] are not in the layout");
        assertThat(result.out()).isEmpty();
    }

    @Test
    void aMalformedGeneIsAUsageError() {
        Result result = run("--seed", "1", "--days", "1", "--gene", "grassPull=0.2");
        assertThat(result.exitCode()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(result.err()).contains("--gene takes SPECIES.GENE=VALUE");
    }

    @Test
    void aSetAtTakesEffectAfterItsDay() {
        Result result = run(
                "--seed",
                "1",
                "--days",
                "2",
                "--spawn",
                "grass=1",
                "--set",
                "world.grassPerDrop=3",
                "--set-at",
                "1:world.grassDropInterval=1");
        assertThat(result.exitCode()).isZero();
        String[] lines = result.out().split("\n");
        assertThat(lines[0]).contains("\"grass\":1,");
        assertThat(lines[1]).contains("\"grass\":4,");
    }

    @Test
    void aSetAtZeroIsASet() {
        assertThat(run("--seed", "1", "--days", "1", "--set-at", "0:rabbit.startingReserve=0")
                        .out())
                .startsWith("{\"seed\":1,\"day\":1,\"population\":{\"grass\":0,\"rabbit\":0},");
    }

    @Test
    void aBadSetAtFailsBeforeTheFirstLine() {
        Result result = run("--seed", "1", "--days", "10", "--set-at", "5:rabbit.stride=9");
        assertThat(result.exitCode()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(result.out()).isEmpty();
        assertThat(result.err()).contains("stride", "outside");
    }

    @Test
    void aMalformedSetAtIsAUsageError() {
        for (String bad : List.of("rabbit.stride=3", "-1:rabbit.stride=3", "x:rabbit.stride=3", "1:stride=3")) {
            Result result = run("--seed", "1", "--days", "1", "--set-at", bad);
            assertThat(result.exitCode()).as(bad).isEqualTo(CommandLine.ExitCode.USAGE);
            assertThat(result.err()).as(bad).containsAnyOf("--set-at takes DAY:", "--set-at takes OWNER.KEY=VALUE");
        }
    }
}
