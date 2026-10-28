package io.github.decoppelelias.humansimulation.cli;

import io.github.decoppelelias.humansimulation.domain.WorldSnapshot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** The export format: one creature per line, with where it came from and its genes by name. */
final class GenomeLines {
    private GenomeLines() {}

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
