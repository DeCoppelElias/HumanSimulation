package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

class WolfTest {
    /** Rejects a wolf that keeps the rabbit's pulls toward grass, or drifts from the rabbit anywhere else. */
    @Test
    void isARabbitExceptWhereItIsAPredator() {
        Species wolf = Wolf.species();
        Species rabbit = Rabbit.species();
        assertThat(wolf.name()).isEqualTo("wolf");
        assertThat(wolf.spriteKey()).isEqualTo("wolf");
        assertThat(wolf.geneLayout()).isEqualTo(rabbit.geneLayout());
        Map<String, Double> differs = new TreeMap<>(wolf.baseline().scalars());
        differs.entrySet()
                .removeIf(gene -> gene.getValue().equals(rabbit.baseline().scalar(gene.getKey())));
        assertThat(differs)
                .containsOnly(
                        Map.entry("near.Plant", 0.0),
                        Map.entry("hungry.near.Plant", 0.0),
                        Map.entry("sum.Meat", 0.0),
                        Map.entry("near.Meat", 1.0),
                        Map.entry("hungry.near.Meat", 2.0),
                        Map.entry("sum.takes.Meat", -0.2));
    }

    @Test
    void takesMeatAndCarriesNone() {
        Species wolf = Wolf.species();
        SettingValues settings = new SettingValues(wolf.settings());
        Spawn spawn = new Spawn(wolf.baseline(), wolf, settings, 0, Optional.empty());
        List<Component> parts =
                wolf.parts().stream().map(part -> part.apply(spawn)).toList();
        assertThat(parts).noneMatch(Meat.class::isInstance);
        assertThat(parts)
                .anySatisfy(part -> assertThat(part.ownView()).isEqualTo(new Eater.Takes(List.of(Meat.class))));
        assertThat(settings.declared().declares("nourishment")).isFalse();
        assertThat(settings.whole("stride")).isEqualTo(3);
    }
}
