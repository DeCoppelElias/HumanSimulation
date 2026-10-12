package io.github.decoppelelias.humansimulation.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ServeCommandPageTest {
    @Test
    void clockSeedsFitAJavaScriptNumber() {
        long largest = (1L << 53) - 1;
        assertThat(ServeCommand.seedFromClock(Long.MAX_VALUE)).isEqualTo(largest);
        assertThat(ServeCommand.seedFromClock((1L << 53) + 5)).isEqualTo(5);
        assertThat(ServeCommand.seedFromClock(42)).isEqualTo(42);
        assertThat(ServeCommand.seedFromClock(-1)).isEqualTo(largest);
    }

    @Test
    void theBrowserGetsThePageAddress() {
        List<URI> opened = new ArrayList<>();
        List<String> logged = new ArrayList<>();
        ServeCommand.showPage(URI.create("http://127.0.0.1:7070/"), Optional.of(opened::add), logged::add);
        assertThat(opened).containsExactly(URI.create("http://127.0.0.1:7070/"));
        assertThat(logged).isEmpty();
    }

    @Test
    void withoutADesktopTheAddressIsLogged() {
        List<String> logged = new ArrayList<>();
        ServeCommand.showPage(URI.create("http://127.0.0.1:7070/"), Optional.empty(), logged::add);
        assertThat(logged).singleElement().asString().contains("http://127.0.0.1:7070/");
    }

    @Test
    void anyAddressListensButTheBrowserUsesLoopback() {
        assertThat(ServeCommand.pageAddress("0.0.0.0", 80)).isEqualTo(URI.create("http://127.0.0.1:80/"));
        assertThat(ServeCommand.pageAddress("::", 80)).isEqualTo(URI.create("http://127.0.0.1:80/"));
        assertThat(ServeCommand.pageAddress("::1", 80)).isEqualTo(URI.create("http://[::1]:80/"));
        assertThat(ServeCommand.pageAddress("127.0.0.1", 80)).isEqualTo(URI.create("http://127.0.0.1:80/"));
    }
}
