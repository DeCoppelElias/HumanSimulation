package io.github.decoppelelias.humansimulation.cli;

import io.github.decoppelelias.humansimulation.domain.CensusRow;
import java.util.Map;

final class CensusLine {
    private CensusLine() {}

    static String format(long seed, CensusRow row) {
        StringBuilder line = new StringBuilder()
                .append("{\"seed\":")
                .append(seed)
                .append(",\"day\":")
                .append(row.day())
                .append(",\"population\":{");
        String separator = "";
        for (Map.Entry<String, Integer> entry : row.population().entrySet()) {
            line.append(separator)
                    .append(JsonStrings.quote(entry.getKey()))
                    .append(':')
                    .append(entry.getValue());
            separator = ",";
        }
        line.append("},\"geneMeans\":");
        nested(line, row.geneMeans());
        line.append(",\"deaths\":");
        nested(line, row.deaths());
        return line.append('}').toString();
    }

    private static void nested(StringBuilder line, Map<String, ? extends Map<String, ?>> groups) {
        line.append('{');
        String separator = "";
        for (Map.Entry<String, ? extends Map<String, ?>> group : groups.entrySet()) {
            line.append(separator).append(JsonStrings.quote(group.getKey())).append(":{");
            String inner = "";
            for (Map.Entry<String, ?> entry : group.getValue().entrySet()) {
                line.append(inner)
                        .append(JsonStrings.quote(entry.getKey()))
                        .append(':')
                        .append(entry.getValue());
                inner = ",";
            }
            line.append('}');
            separator = ",";
        }
        line.append('}');
    }
}
