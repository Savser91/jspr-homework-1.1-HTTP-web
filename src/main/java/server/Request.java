package server;

import java.io.*;
import java.util.*;

public class Request {
    private String method;
    private String path;
    private Map<String, String> header;
    private InputStream in;

    public Request(String method, String path, Map<String, String> header, InputStream in) {
        this.method = method;
        this.path = path;
        this.header = header;
        this.in = in;
    }

    public static Request getRequest(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String request = reader.readLine();
        String[] parts = request.split(" ");

        if (parts.length != 3) {
            // just close socket
            return null;
        }

        String method = parts[0];
        String path = parts[1];
        Map<String, String> headers = new HashMap<>();
        String header = reader.readLine();

        String headerName = header.substring(0, header.indexOf(":"));
        String headerValue = header.substring(header.indexOf(":") + 2);
        headers.put(headerName, headerValue);

        return new Request(method, path, headers, in);
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }
}
