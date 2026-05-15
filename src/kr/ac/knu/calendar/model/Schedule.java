package kr.ac.knu.calendar.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Schedule implements Comparable<Schedule> {
    protected String date;
    protected String content;

    public Schedule(String date, String content) {
        this.date = date;
        this.content = content;
    }

    public String getDate() { return date; }
    public String getContent() { return content; }
    public void setDate(String date) { this.date = date; }
    public void setContent(String content) { this.content = content; }
    public abstract String getScheduleType();

    private LocalDate[] extractDates() {
        String combined = date + " " + content;
        Pattern p = Pattern.compile("(?:[0-9]{4}\\s*\\.\\s*)?([0-9]{1,2})\\s*\\.\\s*([0-9]{1,2})");
        Matcher m = p.matcher(combined);

        LocalDate start = null;
        LocalDate end = null;
        int currentYear = LocalDate.now().getYear();

        while (m.find()) {
            try {
                int month = Integer.parseInt(m.group(1));
                int day = Integer.parseInt(m.group(2));
                if (month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                    LocalDate d = LocalDate.of(currentYear, month, day);
                    if (start == null) start = d;
                    end = d;
                }
            } catch (Exception e) {}
        }
        return new LocalDate[]{start, end};
    }

    public LocalDate getStartDate() {
        LocalDate[] dates = extractDates();
        return dates[0] != null ? dates[0] : LocalDate.now();
    }

    public boolean isOccurringOn(LocalDate targetDate) {
        LocalDate[] dates = extractDates();
        LocalDate start = dates[0];
        LocalDate end = dates[1];
        String combined = date + " " + content;

        if (start != null && end != null) {
            if (!combined.contains("~") && !combined.contains("-")) {
                end = start;
            } else if (end.isBefore(start)) {
                end = end.plusYears(1);
            }
            return !targetDate.isBefore(start) && !targetDate.isAfter(end);
        }

        String t1 = String.format("%02d.%02d", targetDate.getMonthValue(), targetDate.getDayOfMonth());
        String t2 = String.format("%d.%d", targetDate.getMonthValue(), targetDate.getDayOfMonth());
        return combined.contains(t1) || combined.contains(t2);
    }

    public long getDDay() {
        return ChronoUnit.DAYS.between(LocalDate.now(), getStartDate());
    }

    @Override
    public int compareTo(Schedule o) {
        return this.getStartDate().compareTo(o.getStartDate());
    }

    @Override
    public String toString() {
        long d = getDDay();
        String dStr = d > 0 ? "D-" + d : (d == 0 ? "D-Day" : "D+" + Math.abs(d));
        return String.format("[%s] %s : %s (%s)", getScheduleType(), date, content, dStr);
    }
}