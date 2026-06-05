package com.example.storyhub.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeAgoHelper {
    public static String getTimeAgo(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date date = null;
        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        };

        for (String fmt : formats) {
            try {
                SimpleDateFormat f = new SimpleDateFormat(fmt, Locale.getDefault());
                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                date = f.parse(dateStr);
                break;
            } catch (ParseException ignored) {}
        }

        if (date == null) return dateStr;

        long diff = System.currentTimeMillis() - date.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;

        if (seconds < 60) return "Baru saja";
        if (minutes < 60) return minutes + " menit yang lalu";
        if (hours < 24) return hours + " jam yang lalu";
        if (days < 7) return days + " hari yang lalu";
        if (weeks < 4) return weeks + " minggu yang lalu";
        if (months < 12) return months + " bulan yang lalu";
        return years + " tahun yang lalu";
    }
}