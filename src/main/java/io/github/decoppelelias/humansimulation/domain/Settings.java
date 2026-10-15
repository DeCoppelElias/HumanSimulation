package io.github.decoppelelias.humansimulation.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The settings a species or a world process declares. Every read goes through here, and reads the default. */
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

    double number(String key) {
        return scalar(key).defaultValue();
    }

    int whole(String key) {
        Setting.Scalar scalar = scalar(key);
        if (!scalar.whole()) {
            throw new IllegalArgumentException("setting " + key + " is not a whole number");
        }
        return (int) scalar.defaultValue();
    }

    Setting.Scalar scalar(String key) {
        for (Setting setting : declared) {
            if (setting.key().equals(key)) {
                return switch (setting) {
                    case Setting.Scalar scalar -> scalar;
                };
            }
        }
        throw new IllegalArgumentException("no setting " + key);
    }
}
