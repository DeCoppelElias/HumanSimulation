package io.github.decoppelelias.humansimulation.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The settings a species or a world process declares, with their defaults. A world holds the current values. */
record Settings(List<Setting> declared) {
    static final Settings NONE = new Settings(List.of());

    Settings {
        declared = List.copyOf(declared);
        Set<String> keys = new HashSet<>();
        for (Setting setting : declared) {
            if (!keys.add(setting.key())) {
                throw new IllegalArgumentException("setting " + setting.key() + " is declared twice");
            }
        }
    }

    boolean declares(String key) {
        return declared.stream().anyMatch(setting -> setting.key().equals(key));
    }

    Setting.Scalar scalar(String key) {
        return switch (find(key)) {
            case Setting.Scalar scalar -> scalar;
            case Setting.Toggle toggle -> toggle.asScalar();
        };
    }

    Setting.Toggle toggle(String key) {
        if (find(key) instanceof Setting.Toggle toggle) {
            return toggle;
        }
        throw new IllegalArgumentException("setting " + key + " is not a toggle");
    }

    private Setting find(String key) {
        for (Setting setting : declared) {
            if (setting.key().equals(key)) {
                return setting;
            }
        }
        throw new IllegalArgumentException("no setting " + key);
    }
}
