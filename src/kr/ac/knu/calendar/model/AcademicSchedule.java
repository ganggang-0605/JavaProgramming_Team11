package kr.ac.knu.calendar.model;

import java.time.LocalDate;
import java.util.Objects;

public class AcademicSchedule extends Schedule {
    private String category;
    private final String scheduleType = "학사";

    public AcademicSchedule(LocalDate date, String content, String category) {
        super(date, content);
        this.category = category;
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
    public boolean equals(Object o) {
        if (!(o instanceof AcademicSchedule schedule)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(this.category, schedule.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.category, this.scheduleType);
    }
}