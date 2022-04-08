package server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@FunctionalInterface
public interface Handler {
    void handle(Request request, BufferedOutputStream out);

    public static void notFoundHandler(Request request, BufferedOutputStream out) {
        try {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void okHandler(Request request, BufferedOutputStream out) {
        try {
            final Path filePath = Path.of(".", "public", request.getPath());
            final String mimeType = Files.probeContentType(filePath);
            final long length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void badRequestHandler(Request request, BufferedOutputStream out) {
        try {
            out.write((
                    "HTTP/1.1 400 BadRequest\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
