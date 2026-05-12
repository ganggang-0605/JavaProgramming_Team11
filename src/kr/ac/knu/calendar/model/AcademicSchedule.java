package kr.ac.knu.calendar.model;

public class AcademicSchedule extends Schedule {
    private String category;

    public AcademicSchedule(String date, String content, String category) {
        super(date, content);
        this.category = category;
    }

    @Override
    public String getScheduleType() {
        return "학사";
    }
}