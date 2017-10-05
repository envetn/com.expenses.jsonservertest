package databaseUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

/**
 * Created by Foten on 1/21/2017.
 */
public class DateUtils
{
    public static Map<String, LocalDate> getFirstAndLastDayOf(int givenMonth, int yeah)
    {
        LocalDateTime now = LocalDateTime
                .now()
                .withYear(yeah)
                .withMonth(givenMonth);

        Map<String, LocalDate> minMax = new HashMap<>();
        int month = now.getMonthValue();
        int year = now.getYear();

        LocalDate initial = LocalDate.of(year, month, 1);
        LocalDate start = initial.with(firstDayOfMonth());
        LocalDate end = initial.with(lastDayOfMonth());

        minMax.put("first", start);
        minMax.put("last", end);

        return  minMax;
    }

    public static Map<String, LocalDate> getFirstAndLastDayOfWeek()
    {
        Map<String, LocalDate> minMax = new HashMap<>();

        LocalDate now = LocalDate
                .now()
                .withYear(2017)
                .withMonth(2)
                .withDayOfMonth(1);

        TemporalField fieldISO = WeekFields.of(Locale.FRENCH).dayOfWeek();

        LocalDate monday = now.with(fieldISO, 1); // 2016-12-12 (Monday)
        LocalDate sunday = now.with(fieldISO, 7);

        minMax.put("first", monday);
        minMax.put("last", sunday);

        return minMax;
    }

    public static Map<String , LocalDate> getSingleDayBy(int year, int month, int day)
    {
        Map<String, LocalDate> minMax = new HashMap<>();
        LocalDate now = LocalDate
                .now()
                .withYear(year)
                .withMonth(month)
                .withDayOfMonth(day);

        minMax.put("first", now);
        minMax.put("last", now);

        return minMax;
    }

    public static Map<String, LocalDate> getSingleDay()
    {
        Map<String, LocalDate> minMax = new HashMap<>();
        LocalDate now = LocalDate
                .now()
                .withYear(2017)
                .withMonth(3)
                .withDayOfMonth(12);

        minMax.put("first", now);
        minMax.put("last", now);

        return minMax;
    }

    public static Map<String, LocalDate> getBoTEoT()
    {
        Map<String, LocalDate> minMax = new HashMap<>();

        LocalDate bot = LocalDate
                .now()
                .withYear(1970)
                .withMonth(1)
                .withDayOfMonth(1);

        LocalDate eot = LocalDate
                .now()
                .withYear(9999)
                .withMonth(12)
                .withDayOfMonth(31);

        minMax.put("first", bot);
        minMax.put("last", eot);

        return minMax;
    }
}
