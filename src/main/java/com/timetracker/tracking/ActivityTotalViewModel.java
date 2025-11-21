package com.timetracker.tracking;

public record ActivityTotalViewModel(String appOrSite,
                                     String url,
                                     long totalSeconds,
                                     String formattedDuration) {
}
