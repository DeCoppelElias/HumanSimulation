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
        return line.append("}}").toString();
    }
}
