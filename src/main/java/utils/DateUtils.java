package utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return DATE_FORMATTER.format(date);
    }
    
    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return TIME_FORMATTER.format(dateTime);
    }
    
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return DATETIME_FORMATTER.format(dateTime);
    }
} 