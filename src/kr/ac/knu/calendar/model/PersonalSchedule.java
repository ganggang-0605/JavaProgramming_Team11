package kr.ac.knu.calendar.model;

import java.time.LocalDate;

public class PersonalSchedule extends Schedule {
    private final String scheduleType;

    public PersonalSchedule(LocalDate date, String content) {
        super(date, content);
        this.scheduleType = "개인";
    }

    @Override
    public String getScheduleType() {
        return this.scheduleType;
    }
}