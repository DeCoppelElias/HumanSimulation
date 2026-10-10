package io.github.decoppelelias.humansimulation.cli;

import io.github.decoppelelias.humansimulation.domain.Rabbit;
import io.github.decoppelelias.humansimulation.web.Api;
import io.github.decoppelelias.humansimulation.web.IdleWatch;
import io.github.decoppelelias.humansimulation.web.Worlds;
import io.javalin.Javalin;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@CommandLine.Command(
        name = "serve",
        mixinStandardHelpOptions = true,
        description = "Serve worlds over HTTP. Prints one ready line on stdout and logs to stderr.")
public final class ServeCommand implements Callable<Integer> {
    private static final Logger LOG = LoggerFactory.getLogger(ServeCommand.class);
    private static final Duration KEEP_ALIVE = Duration.ofSeconds(15);
    private static final long IDLE_CHECK_SECONDS = 30;
    private static final int AGENT_IDLE_MINUTES = 10;
    private static final int PERSON_IDLE_MINUTES = 24 * 60;

    @Spec
    private CommandSpec spec;

    @Option(
            names = "--port",
            defaultValue = "7070",
            description = "Port, or 0 for any free one. Default: ${DEFAULT-VALUE}.")
    private int port;

    @Option(
            names = "--host",
            defaultValue = "127.0.0.1",
            description = "Address to listen on. Default: ${DEFAULT-VALUE}.")
    private String host;

    @Option(names = "--no-browser", description = "For an agent: no page, and quit when idle.")
    private boolean noBrowser;

    @Option(
            names = "--idle-minutes",
            description = "Quiet minutes before quitting with --no-browser (default 10), or before pausing playing"
                    + " worlds without it (default 1440). 0 never.")
    private Optional<Integer> idleMinutes = Optional.empty();

    @Option(
            names = "--max-worlds",
            defaultValue = "32",
            description = "Most worlds held at once. Default: ${DEFAULT-VALUE}.")
    private int maxWorlds;

    @Override
    public Integer call() throws InterruptedException {
        int minutes = idleMinutes(noBrowser, idleMinutes);
        if (minutes < 0) {
            throw new ParameterException(spec.commandLine(), "--idle-minutes cannot be negative, got " + minutes);
        }
        Worlds worlds;
        try {
            worlds = new Worlds(maxWorlds, List.of(Rabbit.species()));
        } catch (IllegalArgumentException e) {
            throw new ParameterException(spec.commandLine(), e.getMessage());
        }
        CountDownLatch quit = new CountDownLatch(1);
        Runnable onIdle = noBrowser ? quit::countDown : worlds::pauseAll;
        IdleWatch idle = new IdleWatch(Duration.ofMinutes(minutes), System::nanoTime, onIdle);
        Javalin app = Api.create(worlds, idle, KEEP_ALIVE, System::nanoTime).start(host, port);
        ScheduledExecutorService checks = Executors.newSingleThreadScheduledExecutor(
                Thread.ofPlatform().daemon().factory());
        checks.scheduleAtFixedRate(idle::check, IDLE_CHECK_SECONDS, IDLE_CHECK_SECONDS, TimeUnit.SECONDS);
        if (!noBrowser) {
            LOG.info("No page yet: the browser view arrives in a later version. Serving the API.");
        }
        PrintWriter out = spec.commandLine().getOut();
        out.print("{\"event\":\"ready\",\"url\":" + JsonStrings.quote("http://" + host + ":" + app.port())
                + ",\"port\":" + app.port() + "}\n");
        out.flush();
        quit.await();
        LOG.info("Quitting after {} idle minutes.", minutes);
        checks.shutdownNow();
        app.stop();
        worlds.closeAll();
        return CommandLine.ExitCode.OK;
    }

    static int idleMinutes(boolean noBrowser, Optional<Integer> given) {
        return given.orElse(noBrowser ? AGENT_IDLE_MINUTES : PERSON_IDLE_MINUTES);
    }
}
