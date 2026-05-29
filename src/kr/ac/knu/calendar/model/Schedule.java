package kr.ac.knu.calendar.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public abstract class Schedule implements Comparable<Schedule> {
    private static final String[] HOLIDAYS = {"부처님", "어린이날", "추석", "설날", "현충일", "광복절", "개천절", "한글날", "삼일절", "공휴일", "대체공휴", "기념일"};
    protected LocalDate date;
    protected String content;

    public Schedule(LocalDate date, String content) {
        this.date = date;
        this.content = content;
    }

    public LocalDate getDate() { return this.date; }
    public String getContent() { return this.content; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setContent(String content) { this.content = content; }
    public abstract String getScheduleType();

    public boolean isHoliday() {
        for (String holiday : HOLIDAYS) {
            if (this.content.contains(holiday)) return true;
        }
        return false;
    }

    @Override
    public int compareTo(Schedule schedule) {
        return this.date.compareTo(schedule.getDate());
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", this.getScheduleType(), this.content);
    }

    @Override
    public int hashCode() {
        return this.date.hashCode() +
                this.content.hashCode() +
                this.getScheduleType().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Schedule schedule) {
            return this.date.equals(schedule.getDate()) &&
                    this.content.equals(schedule.getContent()) &&
                    this.getScheduleType().equals(schedule.getScheduleType());
        }
        return false;
    }
}