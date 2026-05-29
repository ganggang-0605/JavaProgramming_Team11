package kr.ac.knu.calendar.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public abstract class Schedule implements Comparable<Schedule> {
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

    public long getDDay() {
        return ChronoUnit.DAYS.between(LocalDate.now(), this.date);
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