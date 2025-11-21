package com.timetracker.tracking;

import java.util.Optional;

public class NoOpActiveAppCollector implements ActiveAppCollector {

    @Override
    public Optional<ActiveAppSnapshot> capture() {
        return Optional.empty();
    }
}
