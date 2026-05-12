package kr.ac.knu.calendar.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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

    public LocalDate getStartDate() {
        try {
            String cleanDate = date.split("~")[0].trim().replaceAll("[^0-9.]", "");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            return LocalDate.parse(cleanDate, formatter);
        } catch (Exception e) {
            return LocalDate.now();
        }
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