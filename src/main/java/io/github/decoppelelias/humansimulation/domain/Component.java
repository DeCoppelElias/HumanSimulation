package io.github.decoppelelias.humansimulation.domain;

import java.util.Optional;

interface Component {
    Class<? extends Component> key();

    Record ownView();

    /** Empty when creatures in range cannot see it. */
    Optional<Record> seenView();
}
