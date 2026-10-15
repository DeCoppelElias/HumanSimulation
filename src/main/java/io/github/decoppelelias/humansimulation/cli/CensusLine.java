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
        line.append("},\"geneMeans\":{");
        separator = "";
        for (Map.Entry<String, Map<String, Double>> species : row.geneMeans().entrySet()) {
            line.append(separator).append(JsonStrings.quote(species.getKey())).append(":{");
            String inner = "";
            for (Map.Entry<String, Double> gene : species.getValue().entrySet()) {
                line.append(inner)
                        .append(JsonStrings.quote(gene.getKey()))
                        .append(':')
                        .append(gene.getValue());
                inner = ",";
            }
            line.append('}');
            separator = ",";
        }
        return line.append("}}").toString();
    }
}
