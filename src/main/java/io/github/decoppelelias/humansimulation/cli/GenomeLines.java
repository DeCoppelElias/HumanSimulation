package io.github.decoppelelias.humansimulation.cli;

import io.github.decoppelelias.humansimulation.domain.Command;
import io.github.decoppelelias.humansimulation.domain.WorldSnapshot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The export format: one creature per line, with where it came from and its genes by name. */
final class GenomeLines {
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private GenomeLines() {}

    /** A spawn read from a file, with where it came from so the world's refusal can name the line. */
    record Line(String where, Command.Spawn spawn) {}

    /**
     * One spawn of one per line. Only species is required: genes left out come from the baseline, and fields it does
     * not know are ignored.
     */
    static List<Line> read(String name, List<String> lines) {
        List<Line> spawns = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String text = i == 0 ? lines.get(i).replace("﻿", "") : lines.get(i);
            if (text.isBlank()) {
                continue;
            }
            String where = name + " line " + (i + 1);
            JsonNode node;
            try {
                node = JSON.readTree(text);
            } catch (JacksonException e) {
                throw new IllegalArgumentException(where + " is not JSON");
            }
            JsonNode species = node.get("species");
            if (species == null || !species.isString()) {
                throw new IllegalArgumentException(where + " needs a species");
            }
            Map<String, Double> genes = new TreeMap<>();
            JsonNode given = node.get("genes");
            if (given != null) {
                if (!given.isObject()) {
                    throw new IllegalArgumentException(where + ": genes must be an object");
                }
                for (Map.Entry<String, JsonNode> gene : given.properties()) {
                    if (!gene.getValue().isNumber()) {
                        throw new IllegalArgumentException(where + ": gene " + gene.getKey() + " needs a number");
                    }
                    genes.put(gene.getKey(), gene.getValue().doubleValue());
                }
            }
            spawns.add(new Line(where, new Command.Spawn(species.asString(), 1, genes)));
        }
        return spawns;
    }

    static List<String> format(long seed, WorldSnapshot snapshot) {
        List<WorldSnapshot.EntityView> creatures = new ArrayList<>();
        for (WorldSnapshot.TileView tile : snapshot.tiles()) {
            for (WorldSnapshot.EntityView view : tile.entities()) {
                if (!view.genes().isEmpty()) {
                    creatures.add(view);
                }
            }
        }
        creatures.sort(Comparator.comparingInt(WorldSnapshot.EntityView::id));
        List<String> lines = new ArrayList<>();
        for (WorldSnapshot.EntityView creature : creatures) {
            StringBuilder line = new StringBuilder()
                    .append("{\"seed\":")
                    .append(seed)
                    .append(",\"day\":")
                    .append(snapshot.day())
                    .append(",\"species\":")
                    .append(JsonStrings.quote(creature.species()))
                    .append(",\"id\":")
                    .append(creature.id())
                    .append(",\"genes\":{");
            String separator = "";
            for (Map.Entry<String, Double> gene : creature.genes().entrySet()) {
                line.append(separator)
                        .append(JsonStrings.quote(gene.getKey()))
                        .append(':')
                        .append(gene.getValue());
                separator = ",";
            }
            lines.add(line.append("}}").toString());
        }
        return lines;
    }
}
