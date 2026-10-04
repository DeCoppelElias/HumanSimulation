package io.github.decoppelelias.humansimulation.domain;

import java.util.Optional;

interface Parts {
    boolean has(Class<? extends Component> type);

    <V extends Record> Optional<V> view(Class<V> viewType);
}
