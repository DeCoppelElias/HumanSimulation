package io.github.decoppelelias.humansimulation.web;

/** A request the adapter refuses, with the HTTP status that says why. */
final class ApiException extends RuntimeException {
    private final int status;

    ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    int status() {
        return status;
    }
}
