package com.timetracker.tracking;

import java.util.Optional;

public interface BrowserUrlResolver {

    Optional<String> resolveActiveUrl();
}
