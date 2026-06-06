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

        // 프로그램이 켜질 때 자동으로 저장된 파일 읽어오기
        loadSchedulesFromFile();

        // 프로그램이 꺼질 때 자동으로 saveSchedulesToFile() 호출하도록 시스템에 예약
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
            
            // Map의 모든 날짜 키들을 꺼내서 반복문 돌리기
            for (LocalDate date : this.schedules.keySet()) {
                List<Schedule> list = this.schedules.get(date);
                for (int i = 0; i < list.size(); i++) {
                    Schedule schedule = list.get(i);
                    
                    // 내가 직접 입력한 개인 일정(PersonalSchedule)만 파일에 저장
                    if (schedule instanceof PersonalSchedule) {
                        // 날짜와 내용을 | 기호로 구분해서 한 줄씩 쓰기
                        bw.write(schedule.getDate() + "|" + schedule.getContent());
                        bw.newLine();
                    }
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
            // 열어둔 파일 스트림 안전하게 닫기
            if (bw != null) {
                try {
                    bw.close();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 파일에서 일정을 불러오는 기능
    public void loadSchedulesFromFile() {
        String filePath = "schedules.txt";
        java.io.File file = new java.io.File(filePath);
        
        // 읽어올 파일이 아직 없으면 에러가 나므로 함수를 종료시킴
        if (!file.exists()) {
            return;
        }

        java.io.BufferedReader br = null;
        try {
            br = new java.io.BufferedReader(new java.io.FileReader(file));
            String line = "";
            
            // 파일 내용을 한 줄씩 끝까지 읽어오기
            while ((line = br.readLine()) != null) {
                // | 기호를 기준으로 데이터를 날짜와 내용으로 쪼갬
                String[] parts = line.split("\\|");
                if (parts.length >= 2) {
                    LocalDate date = LocalDate.parse(parts[0]);
                    String content = parts[1];
                    
                    // 쪼갠 데이터로 개인 일정 객체 새로 만들기
                    PersonalSchedule schedule = new PersonalSchedule(date, content);
                    
                    // 기존 schedules 지도에 해당 날짜 공간이 없으면 새로 방을 만들어줌
                    if (!this.schedules.containsKey(date)) {
                        this.schedules.put(date, new ArrayList<>());
                    }
                    // 해당 날짜 리스트에 일정 추가하기
                    this.schedules.get(date).add(schedule);
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
            // 열어둔 파일 리더 안전하게 닫기
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