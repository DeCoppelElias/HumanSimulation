package io.github.decoppelelias.humansimulation.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

/** A world's current setting values by owner: "world" for its processes, else a species' name. */
final class SettingTable {
    static final String WORLD = "world";

    /** In panel order: the world first, then the species in the order given. */
    private final Map<String, SettingValues> owners = new LinkedHashMap<>();

    SettingTable(Collection<Species> species, List<WorldProcess> processes) {
        List<Setting> world = new ArrayList<>();
        for (WorldProcess process : processes) {
            world.addAll(declared(process).declared());
        }
        owners.put(WORLD, new SettingValues(new Settings(world)));
        for (Species one : species) {
            if (one.name().equals(WORLD)) {
                throw new IllegalArgumentException(
                        "no species can be named " + WORLD + ", which names the world's own settings");
            }
            owners.put(one.name(), new SettingValues(one.settings()));
        }
    }

    SettingValues of(String owner) {
        SettingValues values = owners.get(owner);
        if (values == null) {
            throw new IllegalArgumentException("no settings belong to " + owner);
        }
        return values;
    }

    void check(String owner, String key, double value) {
        of(owner).declared().scalar(key).check(value);
    }

    void set(String owner, String key, double value) {
        of(owner).set(key, value);
    }

    /** Throws when the owner, or the key within it, is unknown. */
    void checkScope(Optional<String> owner, Optional<String> key) {
        if (owner.isPresent()) {
            Settings declared = of(owner.orElseThrow()).declared();
            if (key.isPresent()) {
                declared.scalar(key.orElseThrow());
            }
        }
    }

    void restore(Optional<String> owner, Optional<String> key) {
        if (owner.isEmpty()) {
            owners.values().forEach(SettingValues::restoreAll);
            return;
        }
        SettingValues values = of(owner.orElseThrow());
        key.ifPresentOrElse(values::restore, values::restoreAll);
    }

    /** Every value that differs from its default, in panel order, as the commands that set it. */
    List<Command.SetSetting> edits() {
        List<Command.SetSetting> edits = new ArrayList<>();
        owners.forEach((owner, values) -> {
            for (Setting setting : values.declared().declared()) {
                Setting.Scalar scalar = values.declared().scalar(setting.key());
                double value = values.number(scalar.key());
                if (value != scalar.defaultValue()) {
                    edits.add(new Command.SetSetting(owner, scalar.key(), value));
                }
            }
        });
        return edits;
    }

    SortedMap<String, Map<String, Double>> current() {
        SortedMap<String, Map<String, Double>> current = new TreeMap<>();
        owners.forEach((owner, values) -> current.put(owner, values.current()));
        return current;
    }

    List<SettingDeclaration> declarations() {
        List<SettingDeclaration> declarations = new ArrayList<>();
        owners.forEach((owner, values) -> {
            for (Setting setting : values.declared().declared()) {
                Setting.Scalar scalar = values.declared().scalar(setting.key());
                String shape =
                        switch (setting) {
                            case Setting.Scalar _ -> "scalar";
                            case Setting.Toggle _ -> "toggle";
                        };
                declarations.add(new SettingDeclaration(
                        owner,
                        scalar.key(),
                        shape,
                        scalar.description(),
                        scalar.min(),
                        scalar.max(),
                        scalar.step(),
                        scalar.defaultValue()));
            }
        });
        return List.copyOf(declarations);
    }

    private static Settings declared(WorldProcess process) {
        return switch (process) {
            case FoodDrop drop -> drop.settings();
        };
    }
}
