package com.svx.github.utility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtility {

    public static String formatTimestamp(LocalDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return formatter.format(timestamp);
    }
}
