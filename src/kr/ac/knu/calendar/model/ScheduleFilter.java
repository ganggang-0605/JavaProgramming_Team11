package kr.ac.knu.calendar.model;

import java.util.function.Predicate;

public class ScheduleFilter implements Predicate<Schedule> {
    private int filterType;

    public ScheduleFilter(int filterType) {
        this.filterType = filterType;
    }

    public int getFilterType() { return this.filterType; }
    public void setFilterType(int filterType) { this.filterType = filterType; }

    @Override
    public boolean test(Schedule schedule) {
        return switch (this.filterType) {
            case 1 -> { // 학사 일정 (대학원 제외)
                if (schedule instanceof AcademicSchedule academicSchedule) {
                    yield !academicSchedule.getCategory().equals("대학원");
                }
                yield false;
            }
            case 2 -> // 학사 일정 (대학원 포함)
                    schedule instanceof AcademicSchedule;
            case 3 -> // 개인 일정
                    schedule instanceof PersonalSchedule;
            default -> true;
        };
    }
}