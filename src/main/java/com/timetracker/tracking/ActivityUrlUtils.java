package com.timetracker.tracking;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public final class ActivityUrlUtils {

    private ActivityUrlUtils() {
    }

    public static Optional<String> extractDomain(String url) {
        if (url == null || url.isBlank()) {
            return Optional.empty();
        }
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) {
                return Optional.empty();
            }
            return Optional.of(host);
        } catch (URISyntaxException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<String> redactQuery(String url, boolean redactQueries) {
        if (!redactQueries || url == null || url.isBlank()) {
            return Optional.ofNullable(url);
        }
        try {
            URI uri = new URI(url);
            URI sanitized = new URI(uri.getScheme(),
                    uri.getAuthority(),
                    uri.getPath(),
                    null,
                    uri.getFragment());
            return Optional.of(sanitized.toString());
        } catch (URISyntaxException ignored) {
            return Optional.ofNullable(url);
        }
    }
}
