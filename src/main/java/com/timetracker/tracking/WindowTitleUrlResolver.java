package com.timetracker.tracking;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Attempts to pull a plausible URL/domain out of a browser window title.
 * This is heuristic and used as a fallback when remote debugging is unavailable.
 */
public class WindowTitleUrlResolver implements BrowserUrlResolver {

    private static final Pattern DOMAIN_PATTERN = Pattern.compile("([\\w.-]+\\.[a-zA-Z]{2,})(?:[/\\s-].*)?");
    private final String windowTitle;

    public WindowTitleUrlResolver(String windowTitle) {
        this.windowTitle = windowTitle;
    }

    @Override
    public Optional<String> resolveActiveUrl() {
        if (windowTitle == null) {
            return Optional.empty();
        }
        Matcher matcher = DOMAIN_PATTERN.matcher(windowTitle);
        if (matcher.find()) {
            return Optional.of("https://" + matcher.group(1));
        }
        return Optional.empty();
    }
}
