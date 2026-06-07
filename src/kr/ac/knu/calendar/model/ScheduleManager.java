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

        // 추가한부분
        loadSchedulesFromFile();

        // 추가한부분
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                saveSchedulesToFile();
            }
        }));
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
        if (this.loadedYears.contains(year)) return null;

        ScheduleLoaderCallbackInterface callback = new ScheduleLoaderCallbackInterface() {
            @Override
            public void updateSchedules(List<Schedule> schedules) {
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

    // 파일에 일정을 저장하는 기능
    public void saveSchedulesToFile() {
        String filePath = "schedules.txt";
        java.io.BufferedWriter bw = null;
        try {
            bw = new java.io.BufferedWriter(new java.io.FileWriter(filePath));
  
            for (LocalDate date : this.schedules.keySet()) {
                List<Schedule> list = this.schedules.get(date);
                for (int i = 0; i < list.size(); i++) {
                    Schedule schedule = list.get(i);
                    
                    if (schedule instanceof PersonalSchedule) {
                        bw.write(schedule.getDate() + "|" + schedule.getContent());
                        bw.newLine();
                    }
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void loadSchedulesFromFile() {
        String filePath = "schedules.txt";
        java.io.File file = new java.io.File(filePath);
        
        if (!file.exists()) {
            return;
        }

        java.io.BufferedReader br = null;
        try {
            br = new java.io.BufferedReader(new java.io.FileReader(file));
            String line = "";
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 2) {
                    LocalDate date = LocalDate.parse(parts[0]);
                    String content = parts[1];
                    
                    PersonalSchedule schedule = new PersonalSchedule(date, content);
                    
                    if (!this.schedules.containsKey(date)) {
                        this.schedules.put(date, new ArrayList<>());
                    }
                    this.schedules.get(date).add(schedule);
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}