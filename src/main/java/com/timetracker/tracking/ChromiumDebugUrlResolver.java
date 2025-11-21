package com.timetracker.tracking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the active tab URL from a Chromium-based browser started with --remote-debugging-port.
 * Falls back silently if the endpoint is not reachable.
 */
public class ChromiumDebugUrlResolver implements BrowserUrlResolver {

    private static final Pattern URL_PATTERN = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"");
    private final int port;
    private final Duration timeout;

    public ChromiumDebugUrlResolver(int port, Duration timeout) {
        this.port = port;
        this.timeout = timeout;
    }

    public ChromiumDebugUrlResolver() {
        this(9222, Duration.ofMillis(500));
    }

    @Override
    public Optional<String> resolveActiveUrl() {
        try {
            URL endpoint = URI.create("http://localhost:" + port + "/json/active").toURL();
            HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();
            conn.setConnectTimeout((int) timeout.toMillis());
            conn.setReadTimeout((int) timeout.toMillis());
            conn.setRequestMethod("GET");
            int status = conn.getResponseCode();
            if (status != 200) {
                return Optional.empty();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                Matcher matcher = URL_PATTERN.matcher(sb.toString());
                if (matcher.find()) {
                    String url = matcher.group(1);
                    return Optional.ofNullable(url);
                }
            }
        } catch (IOException ignored) {
            // Browser may not expose remote debugging; ignore.
        }
        return Optional.empty();
    }
}
