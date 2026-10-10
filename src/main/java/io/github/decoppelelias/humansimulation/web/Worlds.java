package io.github.decoppelelias.humansimulation.web;

import io.github.decoppelelias.humansimulation.domain.Species;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The worlds a server holds, in the order they were created. */
public final class Worlds {
    static final int MAX_SIDE = 25;

    private final int cap;
    private final List<Species> species;
    private final SecureRandom ids = new SecureRandom();
    private final Map<String, WorldHost> byId = new LinkedHashMap<>();

    public Worlds(int cap, List<Species> species) {
        if (cap < 1) {
            throw new IllegalArgumentException("a server holds at least one world, got a cap of " + cap);
        }
        this.cap = cap;
        this.species = List.copyOf(species);
    }

    synchronized WorldHost create(int width, int height, long seed) {
        if (width > MAX_SIDE || height > MAX_SIDE) {
            throw new IllegalArgumentException("a world served over HTTP is at most " + MAX_SIDE + " tiles a side, got "
                    + width + " by " + height);
        }
        if (byId.size() >= cap) {
            throw new ApiException(409, "this server holds its cap of " + cap + " worlds; delete one first");
        }
        byte[] bytes = new byte[16];
        ids.nextBytes(bytes);
        WorldHost host = new WorldHost(HexFormat.of().formatHex(bytes), width, height, seed, species);
        byId.put(host.id(), host);
        return host;
    }

    synchronized WorldHost get(String id) {
        WorldHost host = byId.get(id);
        if (host == null) {
            throw new ApiException(404, "no world " + id);
        }
        return host;
    }

    /** Closes outside the lock, since closing waits for the world's thread, which may be busy stepping. */
    void delete(String id) {
        WorldHost host;
        synchronized (this) {
            host = get(id);
            byId.remove(id);
        }
        host.close();
    }

    synchronized List<WorldHost> all() {
        return new ArrayList<>(byId.values());
    }

    public void pauseAll() {
        for (WorldHost host : all()) {
            try {
                host.pause();
            } catch (ApiException closedMeanwhile) {
                // Deleted after the list was taken; there is nothing left to pause.
            }
        }
    }

    public void closeAll() {
        all().forEach(WorldHost::close);
    }
}
