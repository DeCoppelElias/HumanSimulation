package io.github.decoppelelias.humansimulation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReplayAcrossProcessesTest {
    @Test
    void runPrintsTheSameBytesInTwoProcesses() throws Exception {
        String first = launch(Main.class, "run", "--seed", "42", "--days", "200");
        assertThat(first).isNotEmpty().isEqualTo(launch(Main.class, "run", "--seed", "42", "--days", "200"));
    }

    @Test
    void aSeededWorldReachesTheSameStatesInTwoProcesses() throws Exception {
        String first = launch(SnapshotProbe.class, "42", "200");
        assertThat(first).isNotEmpty().isEqualTo(launch(SnapshotProbe.class, "42", "200"));
    }

    private static String launch(Class<?> main, String... args) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>(List.of(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp",
                System.getProperty("java.class.path"),
                main.getName()));
        command.addAll(List.of(args));
        Process process = new ProcessBuilder(command)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start();
        byte[] out = process.getInputStream().readAllBytes();
        assertThat(process.waitFor()).isZero();
        return new String(out, StandardCharsets.UTF_8);
    }
}
