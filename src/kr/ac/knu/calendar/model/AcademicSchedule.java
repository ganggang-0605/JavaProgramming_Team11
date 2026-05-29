package kr.ac.knu.calendar.model;

import java.time.LocalDate;

public class AcademicSchedule extends Schedule {
    private String category;
    private final String scheduleType;

    public AcademicSchedule(LocalDate date, String content, String category) {
        super(date, content);
        this.category = category;
        this.scheduleType = "학사";
    }

    public String getCategory() { return this.category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String getScheduleType() {
        return this.scheduleType;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", this.category, this.content);
    }

    @Override
    public int hashCode() {
        return this.date.hashCode() +
                this.content.hashCode() +
                this.category.hashCode() +
                this.scheduleType.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AcademicSchedule schedule) {
            return this.date.equals(schedule.getDate()) &&
                    this.content.equals(schedule.getContent()) &&
                    this.category.equals(schedule.getCategory());
        }
        return false;
    }
}