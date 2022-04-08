package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;

public class Server {
    private final ExecutorService executorService;
    Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();

    public Server(int threadPoolSize) throws IOException {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void addHandler(String method, String path, Handler handler) {
        if (handlers.get(method) == null) {
            handlers.put(method, new ConcurrentHashMap<>());
        }

        handlers.get(method).put(path, handler);
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
             final var in = socket.getInputStream();
             final var out = new BufferedOutputStream(socket.getOutputStream());) {
            var request = Request.getRequest(in);
            System.out.println(request);
            Map<String, Handler> headerMap = handlers.get(request.getMethod());

            if (headerMap == null) {
                Handler.notFoundHandler(request, out);
                return;
            }

            Handler handler = headerMap.get(request.getPath());

            if (handler == null) {
                Handler.notFoundHandler(request, out);
                return;
            }

            handler.handle(request, out);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}










