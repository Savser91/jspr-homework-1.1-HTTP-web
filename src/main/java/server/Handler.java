package server;

import java.io.BufferedOutputStream;

@FunctionalInterface
public interface Handler {
    void handle(Request request, BufferedOutputStream out);

    static void notFoundHandler(Request request, BufferedOutputStream out) {

    }
}
