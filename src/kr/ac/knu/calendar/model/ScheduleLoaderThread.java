package kr.ac.knu.calendar.model;

import kr.ac.knu.calendar.util.DataLoader;

import javax.swing.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScheduleLoaderThread extends Thread {
    private static final String KNU_SCHEDULE_URL = "https://knu.ac.kr/wbbs/wbbs/user/yearSchedule/index.action?search_year=%d";
    private final ScheduleManager manager;
    private final Year year;
    private final Consumer<Boolean> callback;

    public ScheduleLoaderThread(ScheduleManager manager, Year year, Consumer<Boolean> callback) {
        super();
        this.manager = manager;
        this.year = year;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            String data = DataLoader.loadData(String.format(KNU_SCHEDULE_URL, this.year.getValue()));
            List<Schedule> schedules = parse(data, this.year.getValue());

            for (Schedule schedule : schedules) {
                this.manager.addSchedule(schedule.getDate(), schedule);
            }
            if (this.callback != null) this.callback.accept(true);
        } catch (Exception e) {
            e.printStackTrace();
            if (this.callback != null) this.callback.accept(false);
            this.interrupt();
        }
    }

    private static List<Schedule> parse(String data, int year) {
        List<Schedule> schedules = new ArrayList<>();

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
                            LocalDate.of(year, month, day),
                            content.replace("[대학원]", "").trim(),
                            "대학원"
                    );
                } else {
                    schedule = new AcademicSchedule(
                            LocalDate.of(year, month, day),
                            content,
                            "학사"
                    );
                }
                schedules.add(schedule);

                // 일정 내용에 날짜가 추가적으로 입력되는 경우
                Set<LocalDate> dates = parseDateRange(schedule);
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

    private static Set<LocalDate> parseDateRange(Schedule schedule) {
        Set<LocalDate> dates = new HashSet<>();
        String content = schedule.getContent();

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