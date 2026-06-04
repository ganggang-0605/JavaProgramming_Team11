package kr.ac.knu.calendar.model;

import kr.ac.knu.calendar.view.CalendarApp;

import javax.swing.*;
import java.time.LocalDate;
import java.util.*;

public class ScheduleManager {
    private final CalendarApp app;
    private final Map<LocalDate, List<Schedule>> schedules;
    private final Set<Integer> loadedYears;

    public ScheduleManager(CalendarApp app) {
        this.app = app;
        this.schedules = new HashMap<>();
        this.loadedYears = new TreeSet<>();
    }

    public CalendarApp getApp() { return this.app; }
    public Set<Integer> getLoadedYears() { return this.loadedYears; }

    /**
     * 일정을 일정 목록에 추가합니다.
     * @param schedule 일정
     */
    public void addSchedule(Schedule schedule) {
        this.schedules.computeIfAbsent(schedule.getDate(), _ -> new ArrayList<>())
                .add(schedule);
    }

    /**
     * 일정을 일정 목록에서 삭제합니다.
     * @param schedule 일정
     */
    public void removeSchedule(Schedule schedule) {
        if (!this.schedules.containsKey(schedule.getDate())) return;

        List<Schedule> daySchedules = this.schedules.get(schedule.getDate());
        daySchedules.remove(schedule);

        if (daySchedules.isEmpty()) this.schedules.remove(schedule.getDate());
    }

    /**
     * 날짜의 전체 일정 목록을 반환합니다.
     * @param date 날짜
     * @return 일정 목록
     */
    public List<Schedule> getSchedules(LocalDate date) {
        return this.schedules.getOrDefault(date, new ArrayList<>());
    }

    /**
     * 날짜의 조건별로 필터링된 일정 목록을 반환합니다.
     * @param date 날짜
     * @param filter 일정 조건
     * @return 필터링된 일정 목록
     */
    public List<Schedule> getFilteredSchedules(LocalDate date, ScheduleFilter filter) {
        if (!this.schedules.containsKey(date)) return new ArrayList<>();

        List<Schedule> list = new ArrayList<>(this.schedules.get(date));
        list.removeIf(schedule -> !filter.test(schedule));

        return list;
    }

    /**
     * 학사일정을 홈페이지에서 불러와서 저장합니다.
     * 이미 불러온 연도의 학사일정은 다시 불러오지 않습니다.
     * Thread를 생성하여 다른 스레드에서 불러오므로 non-blocking 합니다.
     * @param year 학사연도
     * @return ScheduleLoaderThread | null
     */
    public ScheduleLoaderThread fetchSchedules(int year) {
        // 이미 불러온 연도라면 null 반환
        if (this.loadedYears.contains(year)) return null;

        ScheduleLoaderCallbackInterface callback = new ScheduleLoaderCallbackInterface() {
            @Override
            public void updateSchedules(List<Schedule> schedules) {
                // 이미 불러온 연도라면 null 반환
                if (loadedYears.contains(year)) return;
                loadedYears.add(year);

                for (Schedule schedule : schedules) {
                    addSchedule(schedule);
                }
                SwingUtilities.invokeLater(app::updateCalendar);
            }

            @Override
            public void onError(Exception e) {
                JOptionPane.showMessageDialog(
                        app,
                        "학사일정을 불러오는 도중 오류가 발생하였습니다:\n" + e.getMessage(),
                        "학사일정 불러오기",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        };
        ScheduleLoaderThread scheduleLoader = new ScheduleLoaderThread(year);
        scheduleLoader.setCallback(callback);
        scheduleLoader.start();

        return scheduleLoader;
    }
}