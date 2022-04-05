package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

public class Server {
    private final List<String> validPaths;
    private final ExecutorService executorService;

    public Server(int threadPoolSize, List<String> validPaths) throws IOException {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.validPaths = validPaths;
    }

    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                executorService.submit(() -> handle(socket));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void handle(Socket socket) {
        try (socket;
             final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream());) {
            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");
            if (parts.length != 3) {
                // just close socket
                return;
            }
            final var path = parts[1];
            if (!validPaths.contains(path)) {

                return;
            }

            // special case for classic
            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
                return;
            }

            final var length = Files.size(filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}










