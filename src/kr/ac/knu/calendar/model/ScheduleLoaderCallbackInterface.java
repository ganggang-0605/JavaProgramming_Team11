package kr.ac.knu.calendar.model;

import java.util.List;

public interface ScheduleLoaderCallbackInterface {
    void updateSchedules(List<Schedule> schedules);
    void onError(Exception e);
}