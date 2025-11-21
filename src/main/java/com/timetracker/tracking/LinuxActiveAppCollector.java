package com.timetracker.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Lightweight X11 collector that shells out to xprop to resolve the active window.
 * Falls back silently if xprop is missing or parsing fails.
 */
public class LinuxActiveAppCollector implements ActiveAppCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinuxActiveAppCollector.class);

    private final boolean captureUrls;
    private final BrowserUrlResolver browserUrlResolver;

    public LinuxActiveAppCollector(boolean captureUrls, BrowserUrlResolver browserUrlResolver) {
        this.captureUrls = captureUrls;
        this.browserUrlResolver = browserUrlResolver;
    }

    public LinuxActiveAppCollector() {
        this(false, null);
    }

    @Override
    public Optional<ActiveAppSnapshot> capture() {
        try {
            Optional<String> windowId = queryActiveWindowId();
            if (windowId.isEmpty()) {
                return Optional.empty();
            }
            WindowInfo info = queryWindowInfo(windowId.get());
            if (info == null) {
                return Optional.empty();
            }
            String url = null;
            if (captureUrls && isBrowser(info.appId) && browserUrlResolver != null) {
                url = browserUrlResolver.resolveActiveUrl()
                        .or(() -> new WindowTitleUrlResolver(info.title).resolveActiveUrl())
                        .orElse(null);
            }
            Instant now = Instant.now();
            return Optional.of(new ActiveAppSnapshot(info.appId, info.title, url, now));
        } catch (Exception e) {
            LOGGER.debug("Failed to capture active X11 window", e);
            return Optional.empty();
        }
    }

    private Optional<String> queryActiveWindowId() throws IOException, InterruptedException {
        String command = "xprop -root _NET_ACTIVE_WINDOW";
        String output = runCommand(command);
        if (output == null) {
            return Optional.empty();
        }
        int idx = output.lastIndexOf("0x");
        if (idx == -1) {
            return Optional.empty();
        }
        String id = output.substring(idx).trim();
        return Optional.of(id);
    }

    private WindowInfo queryWindowInfo(String windowId) throws IOException, InterruptedException {
        String command = "xprop -id " + windowId + " WM_CLASS _NET_WM_NAME";
        String output = runCommand(command);
        if (output == null) {
            return null;
        }
        String[] lines = output.split("\n");
        String appId = null;
        String title = null;
        for (String line : lines) {
            if (line.contains("WM_CLASS")) {
                appId = extractFirstQuoted(line).map(String::toLowerCase).orElse(null);
            } else if (line.contains("_NET_WM_NAME")) {
                title = extractFirstQuoted(line).orElse(null);
            }
        }
        if (appId == null && title == null) {
            return null;
        }
        return new WindowInfo(appId, title);
    }

    private boolean isBrowser(String appId) {
        if (appId == null) {
            return false;
        }
        String lower = appId.toLowerCase();
        return lower.contains("chrome") || lower.contains("brave") || lower.contains("chromium") || lower.contains("edge");
    }

    private Optional<String> extractFirstQuoted(String line) {
        int firstQuote = line.indexOf('"');
        int secondQuote = firstQuote >= 0 ? line.indexOf('"', firstQuote + 1) : -1;
        if (firstQuote >= 0 && secondQuote > firstQuote) {
            return Optional.of(line.substring(firstQuote + 1, secondQuote));
        }
        return Optional.empty();
    }

    private String runCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("bash", "-lc", command);
        Process process = builder.start();
        boolean finished = process.waitFor(1, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            return null;
        }
        if (process.exitValue() != 0) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString().trim();
        }
    }

    private record WindowInfo(String appId, String title) {
    }
}
