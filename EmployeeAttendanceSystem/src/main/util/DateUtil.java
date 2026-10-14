package main.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date / time formatting helpers used throughout the application.
 */
public class DateUtil {

    public static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter TIME_FMT     = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    public static final DateTimeFormatter MONTH_FMT    = DateTimeFormatter.ofPattern("MMMM yyyy");
    public static final DateTimeFormatter ISO_DATE     = DateTimeFormatter.ISO_LOCAL_DATE;

    private DateUtil() {}

    public static String formatDate(LocalDate d) {
        return d == null ? "" : d.format(DATE_FMT);
    }

    public static String formatDateTime(LocalDateTime dt) {
        return dt == null ? "" : dt.format(DATETIME_FMT);
    }

    public static String formatTime(LocalDateTime dt) {
        return dt == null ? "--:--" : dt.format(TIME_FMT);
    }

    public static String formatMonth(LocalDate d) {
        return d == null ? "" : d.format(MONTH_FMT);
    }

    public static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s, DATE_FMT); } catch (Exception ignored) {}
        try { return LocalDate.parse(s, ISO_DATE); } catch (Exception ignored) {}
        return null;
    }

    /** Number of calendar days between two dates (inclusive). */
    public static long daysBetween(LocalDate from, LocalDate to) {
        return java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1;
    }

    /** Format working hours as "H h MM m" – e.g. "8 h 30 m". */
    public static String formatHours(double hours) {
        int h = (int) hours;
        int m = (int) Math.round((hours - h) * 60);
        return h + " h " + m + " m";
    }
}
