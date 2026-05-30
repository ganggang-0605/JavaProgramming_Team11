package kr.ac.knu.calendar.model;

import kr.ac.knu.calendar.util.DataLoader;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScheduleManager {
    private static final String KNU_SCHEDULE_URL = "https://knu.ac.kr/wbbs/wbbs/user/yearSchedule/index.action?search_year=%d";
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

    public void fetchSchedules(int year) {
        String data = DataLoader.loadData(String.format(KNU_SCHEDULE_URL, year));
        List<Schedule> schedules = parse(data, year);

        for (Schedule schedule : schedules) {
            this.addSchedule(schedule.getDate(), schedule);
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