package io.github.decoppelelias.humansimulation.domain;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/** One owner's current setting values in one world, read live by whatever holds it. */
final class SettingValues {
    private final Settings declared;
    private final SortedMap<String, Double> current = new TreeMap<>();

    SettingValues(Settings declared) {
        this.declared = declared;
        restoreAll();
    }

    Settings declared() {
        return declared;
    }

    boolean declares(String key) {
        return declared.declares(key);
    }

    double number(String key) {
        return current.get(declared.scalar(key).key());
    }

    int whole(String key) {
        Setting.Scalar scalar = declared.scalar(key);
        if (!scalar.whole()) {
            throw new IllegalArgumentException("setting " + key + " is not a whole number");
        }
        return (int) Math.round(current.get(key));
    }

    /**
     * Checks before it changes anything, so a refused value leaves the old one. A value the check let through within
     * its tolerance is stored exactly on its step, so it never reads as edited when it is the default.
     */
    void set(String key, double value) {
        Setting.Scalar scalar = declared.scalar(key);
        scalar.check(value);
        long steps = Math.round((value - scalar.min()) / scalar.step());
        // Decimal arithmetic, so 35 steps of 0.01 store as 0.35 rather than 0.35000000000000003.
        current.put(
                key,
                BigDecimal.valueOf(scalar.min())
                        .add(BigDecimal.valueOf(scalar.step()).multiply(BigDecimal.valueOf(steps)))
                        .doubleValue());
    }

    void restore(String key) {
        current.put(key, declared.scalar(key).defaultValue());
    }

    void restoreAll() {
        for (Setting setting : declared.declared()) {
            switch (setting) {
                case Setting.Scalar scalar -> current.put(scalar.key(), scalar.defaultValue());
            }
        }
    }

    SortedMap<String, Double> current() {
        return Collections.unmodifiableSortedMap(new TreeMap<>(current));
    }
}
