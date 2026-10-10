package io.github.decoppelelias.humansimulation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class ServeCommandTest {
    private static final Pattern READY =
            Pattern.compile("\\{\"event\":\"ready\",\"url\":\"http://127\\.0\\.0\\.1:(\\d+)\",\"port\":(\\d+)}");

    @Test
    @Timeout(30)
    void printsAReadyLineOnStdoutAndServesTheApi() throws Exception {
        Process process = new ProcessBuilder(List.of(
                        Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                        "-cp",
                        System.getProperty("java.class.path"),
                        Main.class.getName(),
                        "serve",
                        "--no-browser",
                        "--port",
                        "0"))
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
        try (BufferedReader out =
                new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String ready = out.readLine();
            assertThat(ready).isNotNull();
            Matcher matcher = READY.matcher(ready);
            assertThat(matcher.matches()).as(ready).isTrue();
            assertThat(matcher.group(1)).isEqualTo(matcher.group(2));
            HttpResponse<String> worlds = HttpClient.newHttpClient()
                    .send(
                            HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + matcher.group(1) + "/worlds"))
                                    .build(),
                            HttpResponse.BodyHandlers.ofString());
            assertThat(worlds.statusCode()).isEqualTo(200);
            assertThat(worlds.body()).isEqualTo("[]");
        } finally {
            process.destroy();
        }
    }
}
