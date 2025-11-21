package com.timetracker.tracking;

import java.util.Optional;

public interface ActiveAppCollector {

    Optional<ActiveAppSnapshot> capture();
}
