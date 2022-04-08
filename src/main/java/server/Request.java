package server;

import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {
    public static final String GET = "GET";
    public static final String POST = "POST";
    final static List<String> allowedMethods = List.of(GET, POST);

    private String method;
    private String path;
    private Map<String, String> header;
    private Map<String, String> queryParams = new HashMap<>();
    private String body;
    private InputStream in;

    public Request(String method, String path, Map<String, String> header, String body, InputStream in) {
        this.method = method;
        this.path = path;
        this.header = header;
        var pairs = URLEncodedUtils.parse(URI.create(path), StandardCharsets.UTF_8);
        for (var pair: pairs) {
            queryParams.put(pair.getName(), pair.getValue());
        }
        this.body = body;
        this.in = in;
    }

    public static Request getRequest(InputStream in) throws IOException {
        var reader = new BufferedInputStream(in);

        // лимит на request line + заголовки
        final var limit = 4096;

        reader.mark(limit);
        final var buffer = new byte[limit];
        final var read = reader.read(buffer);

        // ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            throw new IOException();
        }

        // читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            throw new IOException();
        }

        final var method = requestLine[0];
        if (!allowedMethods.contains(method)) {
            throw new IOException();
        }
        System.out.println(method);

        final var path = requestLine[1];
        if (!path.startsWith("/")) {
            throw new IOException();
        }
        System.out.println(path);

        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            throw new IOException();
        }

        // отматываем на начало буфера
        reader.reset();
        // пропускаем requestLine
        reader.skip(headersStart);

        final var headersBytes = reader.readNBytes(headersEnd - headersStart);
        final var headerList = Arrays.asList(new String(headersBytes).split("\r\n"));
        System.out.println(headerList);

        Map<String, String> headers = new HashMap<>();

        for (String header : headerList) {
            int index = header.indexOf(":");
            var headerName = header.substring(0, index);
            var headerValue = header.substring(index + 2);
            headers.put(headerName, headerValue);
        }

        // для GET тела нет
        String body = null;
        if (!method.equals(GET)) {
            reader.skip(headersDelimiter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = Optional.ofNullable(headers.get("Content-Length"));
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = reader.readNBytes(length);

                body = new String(bodyBytes);
                System.out.println(body);
            }
        }

        return new Request(method, path, headers, body, in);
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String string) {
        return queryParams.get(string);
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", header=" + header +
                ", queryParams='" + queryParams + '\'' +
                '}';
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
