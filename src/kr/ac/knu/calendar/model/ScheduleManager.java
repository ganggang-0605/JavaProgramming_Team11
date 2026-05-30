package kr.ac.knu.calendar.model;

import java.time.LocalDate;
import java.util.*;

public class ScheduleManager {
    private final Map<LocalDate, List<Schedule>> schedules;

    public ScheduleManager() {
        this.schedules = new HashMap<>();
    }

    public void addSchedule(LocalDate date, Schedule schedule) {
        this.schedules.computeIfAbsent(date, _ -> new ArrayList<>())
                .add(schedule);
    }

    public void removeSchedule(LocalDate date, Schedule schedule) {
        if (!this.schedules.containsKey(date)) return;

        List<Schedule> daySchedules = this.schedules.get(date);
        daySchedules.remove(schedule);

        if (daySchedules.isEmpty()) this.schedules.remove(date);
    }

    public List<Schedule> getSchedules(LocalDate date) {
        return this.schedules.getOrDefault(date, new ArrayList<>());
    }

    public List<Schedule> getFilteredSchedules(LocalDate date, ScheduleFilter filter) {
        if (!this.schedules.containsKey(date)) return new ArrayList<>();

        List<Schedule> list = new ArrayList<>(this.schedules.get(date));
        list.removeIf(schedule -> !filter.test(schedule));

        return list;
    }
}