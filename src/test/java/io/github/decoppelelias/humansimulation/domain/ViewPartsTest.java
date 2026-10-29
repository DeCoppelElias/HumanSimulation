package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ViewPartsTest {
    @Test
    void othersSeeOnlyTheFeaturesOfVisibleParts() {
        List<Component> parts = List.of(new Plant(() -> 15), new Fertility(0), new HiddenFeature());
        assertThat(ViewParts.seen(parts).features()).containsExactly(Features.PLANT);
    }

    /** A part that names a feature and offers no seen view, so a builder that skips the view check shows it. */
    private static final class HiddenFeature implements Component {
        @Override
        public Class<? extends Component> key() {
            return HiddenFeature.class;
        }

        @Override
        public Record ownView() {
            return new Edible.Amount(0);
        }

        @Override
        public Optional<Record> seenView() {
            return Optional.empty();
        }

        @Override
        public List<Feature> features() {
            return List.of(Features.TAKES_PLANT);
        }
    }
}
