package io.github.decoppelelias.humansimulation.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

record ViewParts(List<Class<? extends Component>> keys, List<Record> views) implements Parts {
    ViewParts {
        keys = List.copyOf(keys);
        views = List.copyOf(views);
    }

    static ViewParts own(List<Component> components) {
        return new ViewParts(
                components.stream()
                        .<Class<? extends Component>>map(Component::key)
                        .toList(),
                components.stream().map(Component::ownView).toList());
    }

    static ViewParts seen(List<Component> components) {
        List<Class<? extends Component>> keys = new ArrayList<>();
        List<Record> views = new ArrayList<>();
        for (Component component : components) {
            component.seenView().ifPresent(view -> {
                keys.add(component.key());
                views.add(view);
            });
        }
        return new ViewParts(keys, views);
    }

    @Override
    public boolean has(Class<? extends Component> type) {
        return keys.contains(type);
    }

    @Override
    public <V extends Record> Optional<V> view(Class<V> viewType) {
        return views.stream().filter(viewType::isInstance).map(viewType::cast).findFirst();
    }
}
