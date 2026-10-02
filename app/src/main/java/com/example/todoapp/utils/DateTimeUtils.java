package com.example.todoapp.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Tiện ích định dạng và tính toán thời gian cho ứng dụng.
 */
public class DateTimeUtils {

    private static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm";
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final String TIME_FORMAT = "HH:mm";

    /**
     * Định dạng timestamp sang chuỗi "dd/MM/yyyy HH:mm".
     */
    public static String formatDateTime(long timeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    /**
     * Định dạng timestamp sang chuỗi "dd/MM/yyyy".
     */
    public static String formatDateOnly(long timeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    /**
     * Định dạng timestamp sang chuỗi "HH:mm".
     */
    public static String formatTimeOnly(long timeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    /**
     * Lấy mốc thời gian bắt đầu của ngày (00:00:00.000).
     */
    public static long getStartOfDay(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Lấy mốc thời gian kết thúc của ngày (23:59:59.999).
     */
    public static long getEndOfDay(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    /**
     * Kiểm tra công việc có bị quá hạn hay không.
     */
    public static boolean isOverdue(long dueTimeMillis, boolean isCompleted) {
        if (isCompleted) {
            return false;
        }
        return dueTimeMillis < System.currentTimeMillis();
    }
}
