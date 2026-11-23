package com.smartattend.app.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String ISO_FORMAT_WITH_MS = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final String DISPLAY_DATE_FORMAT = "EEE, dd MMM yyyy";
    private static final String DISPLAY_TIME_FORMAT = "hh:mm a";
    private static final String DISPLAY_DATETIME_FORMAT = "dd MMM yyyy, hh:mm a";
    private static final String SHORT_DATE_FORMAT = "dd/MM/yyyy";

    public static String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "N/A";
        try {
            Date date = parseIsoDate(isoDate);
            if (date == null) return isoDate;
            SimpleDateFormat sdf = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return isoDate;
        }
    }

    public static String formatTime(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "N/A";
        try {
            Date date = parseIsoDate(isoDate);
            if (date == null) return isoDate;
            SimpleDateFormat sdf = new SimpleDateFormat(DISPLAY_TIME_FORMAT, Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return isoDate;
        }
    }

    public static String formatDateTime(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "N/A";
        try {
            Date date = parseIsoDate(isoDate);
            if (date == null) return isoDate;
            SimpleDateFormat sdf = new SimpleDateFormat(DISPLAY_DATETIME_FORMAT, Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return isoDate;
        }
    }

    public static String formatShortDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "N/A";
        try {
            Date date = parseIsoDate(isoDate);
            if (date == null) return isoDate;
            SimpleDateFormat sdf = new SimpleDateFormat(SHORT_DATE_FORMAT, Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return isoDate;
        }
    }

    public static String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(DISPLAY_DATETIME_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    private static Date parseIsoDate(String isoDate) {
        String[] formats = {ISO_FORMAT_WITH_MS, ISO_FORMAT, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"};
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                return sdf.parse(isoDate);
            } catch (ParseException ignored) {}
        }
        return null;
    }

    public static String formatTimeOnly(String timeString) {
        if (timeString == null || timeString.isEmpty()) return "N/A";
        // Handle HH:mm:ss format
        if (timeString.matches("\\d{2}:\\d{2}:\\d{2}")) {
            try {
                SimpleDateFormat input = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                SimpleDateFormat output = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                Date date = input.parse(timeString);
                if (date != null) return output.format(date);
            } catch (ParseException ignored) {}
        }
        // Handle HH:mm format
        if (timeString.matches("\\d{2}:\\d{2}")) {
            try {
                SimpleDateFormat input = new SimpleDateFormat("HH:mm", Locale.getDefault());
                SimpleDateFormat output = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                Date date = input.parse(timeString);
                if (date != null) return output.format(date);
            } catch (ParseException ignored) {}
        }
        return timeString;
    }
}
