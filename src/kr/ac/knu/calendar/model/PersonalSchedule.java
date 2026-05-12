package kr.ac.knu.calendar.model;

public class PersonalSchedule extends Schedule {
    private String priority;

    public PersonalSchedule(String date, String content, String priority) {
        super(date, content);
        this.priority = priority;
    }

    @Override
    public String getScheduleType() {
        return "개인";
    }
}