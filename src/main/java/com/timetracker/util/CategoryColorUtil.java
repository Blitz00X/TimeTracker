package com.timetracker.util;

public final class CategoryColorUtil {

    private static final String[] PALETTE = new String[]{
            "#2563EB", // blue
            "#10B981", // green
            "#F59E0B", // amber
            "#EF4444", // red
            "#8B5CF6", // purple
            "#14B8A6", // teal
            "#EC4899", // pink
            "#F97316"  // orange
    };

    private CategoryColorUtil() {
    }

    public static String colorFor(String key) {
        if (key == null || key.isBlank()) {
            return PALETTE[0];
        }
        int idx = Math.abs(key.hashCode()) % PALETTE.length;
        return PALETTE[idx];
    }
}
