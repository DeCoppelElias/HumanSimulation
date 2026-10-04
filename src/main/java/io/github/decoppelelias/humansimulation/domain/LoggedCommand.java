package io.github.decoppelelias.humansimulation.domain;

/** A command and the value of the day counter when it drained. */
public record LoggedCommand(int day, Command command) {}
