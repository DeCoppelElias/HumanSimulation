package io.github.decoppelelias.humansimulation.cli;

final class JsonStrings {
    private JsonStrings() {}

    static String quote(String text) {
        return '"' + text.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }
}
