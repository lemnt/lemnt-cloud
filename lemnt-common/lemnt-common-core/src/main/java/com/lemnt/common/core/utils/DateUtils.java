package com.lemnt.common.core.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 时间工具类
 *
 * @author wangxx
 */
public class DateUtils {
    public static final String YYYY = "yyyy";
    public static final String YYYY_MM = "yyyy-MM";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static LocalDateTime getNowDate() {
        return LocalDateTime.now();
    }

    public static String getDate() {
        return format(LocalDateTime.now(), YYYY_MM_DD);
    }

    public static String getTime() {
        return format(LocalDateTime.now(), YYYY_MM_DD_HH_MM_SS);
    }

    public static String dateTimeNow() {
        return format(LocalDateTime.now(), YYYYMMDDHHMMSS);
    }

    public static String format(LocalDateTime time, String pattern) {
        return time.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(YYYY_MM_DD));
    }

    public static Date toDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr,
                DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS));
    }

    public static LocalDateTime toLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(
                date.toInstant(), ZoneId.systemDefault());
    }

    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    public static LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    public static LocalDateTime startOfDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().atStartOfDay();
    }

    public static LocalDateTime endOfDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().atTime(LocalTime.MAX);
    }

    public static LocalDateTime addDays(LocalDateTime dateTime, int days) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusDays(days);
    }

    public static LocalDate addDays(LocalDate dateTime, int days) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusDays(days);
    }

    public static LocalDateTime addHours(LocalDateTime dateTime, int hours) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusHours(hours);
    }

    public static LocalDateTime addMinutes(LocalDateTime dateTime, int minutes) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusMinutes(minutes);
    }

    public static long betweenMillis(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return Duration.between(start, end).toMillis();
    }

    public static long betweenSeconds(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return Duration.between(start, end).getSeconds();
    }

    public static long betweenMinutes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return Duration.between(start, end).toMinutes();
    }

    public static long betweenDays(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
    }

    public static long betweenDays(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * @deprecated 请使用 {@link #betweenDays(LocalDateTime, LocalDateTime)}，两者功能重复
     */
    @Deprecated
    public static long differentDaysByMillisecond(LocalDateTime startTime,
                                                  LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        long startMillis = startTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        long endMillis = endTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        long diffMillis = Math.abs(endMillis - startMillis);
        return diffMillis / (24 * 60 * 60 * 1000);
    }

    /**
     * 时间区间生成
     */
    public static List<String> generateDateTimeList(
            LocalDate startTime, LocalDate endTime, String type) {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }
        List<String> result = new ArrayList<>();
        switch (type) {
            case "HOUR":
                return generate24HoursPeriodList(startTime);
            case "DAY":
                for (LocalDate d = startTime; !d.isAfter(endTime); d = d.plusDays(1)) {
                    result.add(d.toString());
                }
                break;
            case "MONTH":
                for (LocalDate d = startTime.withDayOfMonth(1);
                     !d.isAfter(endTime);
                     d = d.plusMonths(1)) {
                    result.add(d.format(DateTimeFormatter.ofPattern(YYYY_MM)));
                }
                break;
            default:
                throw new IllegalArgumentException("不支持的时间类型: " + type);
        }
        return result;
    }

    /**
     * 小时段列表
     */
    public static List<String> generate24HoursPeriodList(LocalDate date) {
        List<String> hours = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int maxHour = date.equals(today)
                ? LocalTime.now().getHour()
                : 23;
        for (int i = 0; i <= maxHour; i++) {
            hours.add(String.format("%02d", i));
        }
        return hours;
    }
}
