package io.github.decoppelelias.humansimulation.domain;

import java.util.List;

/** Drops members of a species on tiles drawn from the whole grid, every so many days. */
public record FoodDrop(String species) implements WorldProcess {
    public FoodDrop {
        if (species.isBlank()) {
            throw new IllegalArgumentException("a food drop names its species");
        }
    }

    Settings settings() {
        return new Settings(List.of(
                new Setting.Scalar(perDrop(), "How many " + species + " land in each drop.", 1, 100, 1, 7),
                new Setting.Scalar(interval(), "Days between two drops of " + species + ".", 1, 30, 1, 7)));
    }

    /** How many land at the end of the given day, counted from one. */
    int dropsOn(int completedDay, SettingValues world) {
        return completedDay % world.whole(interval()) == 0 ? world.whole(perDrop()) : 0;
    }

    private String perDrop() {
        return species + "PerDrop";
    }

    private String interval() {
        return species + "DropInterval";
    }
}
