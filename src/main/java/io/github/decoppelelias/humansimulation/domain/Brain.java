package io.github.decoppelelias.humansimulation.domain;

import java.util.Optional;
import java.util.random.RandomGenerator;

interface Brain extends Component {
    record NoView() {}

    Intent decide(Perception perception, Options options, RandomGenerator random);

    @Override
    default Class<? extends Component> key() {
        return Brain.class;
    }

    @Override
    default Record ownView() {
        return new NoView();
    }

    @Override
    default Optional<Record> seenView() {
        return Optional.empty();
    }
}
