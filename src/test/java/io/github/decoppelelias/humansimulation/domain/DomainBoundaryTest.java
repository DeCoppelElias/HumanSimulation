package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.spi.ToolProvider;
import org.junit.jupiter.api.Test;

class DomainBoundaryTest {
    private static final String DOMAIN = "io.github.decoppelelias.humansimulation.domain";

    @Test
    void theDomainDependsOnlyOnItselfAndJavaBase() {
        ToolProvider jdeps = ToolProvider.findFirst("jdeps").orElseThrow();
        StringWriter output = new StringWriter();
        Path classes = Path.of("target", "classes").resolve(DOMAIN.replace('.', '/'));
        int exitCode =
                jdeps.run(new PrintWriter(output), new PrintWriter(output), "-verbose:class", classes.toString());
        assertThat(exitCode).as(output.toString()).isZero();

        List<String> violations = output.toString()
                .lines()
                .map(String::trim)
                .filter(line -> line.startsWith(DOMAIN) && line.contains("->"))
                .filter(line -> {
                    String[] fields = line.split("\s+");
                    String target = fields[2];
                    String module = fields[fields.length - 1];
                    return !target.startsWith(DOMAIN + ".") && !module.equals("java.base");
                })
                .toList();
        assertThat(violations).isEmpty();
    }
}
