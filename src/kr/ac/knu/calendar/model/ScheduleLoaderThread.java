package kr.ac.knu.calendar.model;

import kr.ac.knu.calendar.util.DataLoader;

import javax.swing.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScheduleLoaderThread extends Thread {
    private static final String KNU_SCHEDULE_URL = "https://knu.ac.kr/wbbs/wbbs/user/yearSchedule/index.action?search_year=%d";
    private final int year;
    private ScheduleLoaderCallbackInterface callback;

    public ScheduleLoaderThread(int year) {
        this.year = year;
    }

    public int getYear() { return this.year; }
    public ScheduleLoaderCallbackInterface getCallback() { return this.callback; }
    public void setCallback(ScheduleLoaderCallbackInterface callback) { this.callback = callback; }

    @Override
    public void run() {
        try {
            String data = DataLoader.loadData(String.format(KNU_SCHEDULE_URL, this.year));
            List<Schedule> schedules = this.parse(data);

            // 불러온 학사일정 목록을 callback으로 전달
            if (this.callback != null) this.callback.updateSchedules(schedules);
        } catch (Exception e) {
            if (this.callback != null) this.callback.onError(e);
            this.interrupt();
        }
    }

    /**
     * 학사일정 페이지의 HTML 응답에서 학사일정을 파싱합니다.
     * @param data HTML 응답
     * @return 학사일정 목록이 담긴 배열
     */
    private List<Schedule> parse(String data) {
        List<Schedule> schedules = new ArrayList<>();

        // Regex를 이용해 학사일정 페이지의 HTML에서 일정 목록을 가져옴
        Pattern p = Pattern.compile("<li><span class=\"day\">(\\d+)\\.(\\d+)\\(.\\)</span>(.+)</li>");
        Matcher m = p.matcher(data);
        while (m.find()) {
            try {
                // 01.01(월) -> month: 1, day: 1
                int month = Integer.parseInt(m.group(1));
                int day = Integer.parseInt(m.group(2));
                String content = m.group(3);

                AcademicSchedule schedule;
                if (content.startsWith("[대학원]")) {
                    schedule = new AcademicSchedule(
                            LocalDate.of(this.year, month, day),
                            content.replace("[대학원]", "").trim(),
                            "대학원"
                    );
                } else {
                    schedule = new AcademicSchedule(
                            LocalDate.of(this.year, month, day),
                            content,
                            "학사"
                    );
                }
                schedules.add(schedule);

                // 일정 내용에 날짜가 추가적으로 입력되는 경우
                Set<LocalDate> dates = this.parseDateRange(schedule);
                for (LocalDate date : dates) {
                    AcademicSchedule extraSchedule = new AcademicSchedule(
                            date,
                            schedule.getContent(),
                            schedule.getCategory()
                    );

                    if (!schedule.equals(extraSchedule))
                        schedules.add(extraSchedule);
                }
            } catch (NumberFormatException | DateTimeException _) { }
        }

        return schedules;
    }

    /**
     * 일정의 내용에서 일정 범위를 파싱하고, 범위를 일정 내용에서 삭제합니다.<br/>
     * Ex: 1학기 수강꾸러미 신청(1.20.~1.22.) -><br/>
     * [YYYY-01-20, YYYY-01-21, YYYY-01-22]
     * @param schedule 일정
     * @return 일정 범위
     */
    private Set<LocalDate> parseDateRange(Schedule schedule) {
        Set<LocalDate> dates = new HashSet<>();
        String content = schedule.getContent();

        // 일정 내용에서 Regex로 날짜 범위가 추가적으로 주어진 경우 처리 (ex: 1학기 수강꾸러미 신청(1.20.~1.22.))
        Pattern p = Pattern.compile(":?\\s?\\(?(\\d+\\.\\d+\\.?\\s?~?){1,2}/?\\)?");
        Matcher m = p.matcher(schedule.getContent());
        while (m.find()) {
            String extras = m.group();
            content = content.replace(extras, "").trim();

            LocalDate start = null;
            LocalDate end = null;
            String[] extraDates = extras.replaceAll("[()/:]", "").trim().split("~");
            for (String extraDate : extraDates) {
                // 12.23. -> month: 12, day: 23
                StringTokenizer st = new StringTokenizer(extraDate, ".");
                int month = Integer.parseInt(st.nextToken());
                int day = Integer.parseInt(st.nextToken());

                LocalDate localDate = LocalDate.of(schedule.getDate().getYear(), month, day);
                if (start == null) start = localDate;
                end = localDate;
            }

            // 날짜가 정상적으로 입력되지 않았을 경우 스킵
            if (start == null) continue;

            // 다음 해로 넘어가는 것 처리 (ex: 12.25.~1.1.)
            if (end.getMonthValue() < start.getMonthValue())
                end = end.plusYears(1);

            while (!start.isAfter(end)) {
                dates.add(start);
                start = start.plusDays(1);
            }
        }

        // 날짜 범위를 지운 일정 내용으로 덮어쓰기
        schedule.setContent(content);

        return dates;
    }
}