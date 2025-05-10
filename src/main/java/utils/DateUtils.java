package utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {
    // Define formatters
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    /**
     * Format a LocalDateTime to a date string
     */
    public static String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? DATE_FORMATTER.format(dateTime) : "";
    }
    
    /**
     * Format a LocalDate to a date string
     */
    public static String formatDate(LocalDate date) {
        return date != null ? DATE_FORMATTER.format(date) : "";
    }
    
    /**
     * Format a LocalDateTime to a time string
     */
    public static String formatTime(LocalDateTime dateTime) {
        return dateTime != null ? TIME_FORMATTER.format(dateTime) : "";
    }
    
    /**
     * Format a LocalDateTime to a full date and time string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? DATETIME_FORMATTER.format(dateTime) : "";
    }
    
    /**
     * Calculate the number of days between two dates
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }
    
    /**
     * Calculate the number of days between two date-times
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
    }
    
    /**
     * Parse a string to a LocalDate
     */
    public static LocalDate parseDate(String dateString) {
        return dateString != null && !dateString.isEmpty() 
            ? LocalDate.parse(dateString, DATE_FORMATTER) 
            : null;
    }
    
    /**
     * Parse a string to a LocalDateTime using date and time strings
     */
    public static LocalDateTime parseDateTime(String dateString, String timeString) {
        if (dateString == null || dateString.isEmpty() || timeString == null || timeString.isEmpty()) {
            return null;
        }
        
        LocalDate date = LocalDate.parse(dateString, DATE_FORMATTER);
        String[] timeParts = timeString.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        
        return date.atTime(hour, minute);
    }
} 