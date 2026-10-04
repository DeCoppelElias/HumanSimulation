package io.github.decoppelelias.humansimulation.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
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
        assertThat(result.out())
                .isEqualTo("{\"seed\":42,\"day\":1,\"population\":{\"rabbit\":10}}\n"
                        + "{\"seed\":42,\"day\":2,\"population\":{\"rabbit\":10}}\n"
                        + "{\"seed\":42,\"day\":3,\"population\":{\"rabbit\":10}}\n");
    }

    @Test
    void aSpawnReplacesTheDefaultStartingPopulation() {
        assertThat(run("--seed", "1", "--days", "1", "--spawn", "rabbit=3").out())
                .isEqualTo("{\"seed\":1,\"day\":1,\"population\":{\"rabbit\":3}}\n");
    }

    @Test
    void theSeedDefaultsToTheClockAndIsPrinted() {
        assertThat(run("--days", "1").out())
                .matches("\\{\"seed\":-?\\d+,\"day\":1,\"population\":\\{\"rabbit\":10}}\n");
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
}
