package kr.ac.knu.calendar.model;

import java.time.LocalDate;
import java.util.Objects;

public abstract class Schedule implements Comparable<Schedule> {
    private static final String[] HOLIDAYS = {"신정", "설날", "삼일절", "근로자의 날", "어린이날", "석가탄신일", "현충일", "광복절", "추석", "개천절", "한글날", "성탄절", "기념일", "공휴일", "연휴", "선거"};
    protected LocalDate date;
    protected String content;

    public Schedule(LocalDate date, String content) {
        this.date = date;
        this.content = content;
    }

    public LocalDate getDate() { return this.date; }
    public String getContent() { return this.content; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setContent(String content) { this.content = content; }
    public abstract String getScheduleType();

    /**
     * 일정 날짜의 공휴일 여부를 반환합니다.
     * @return 공휴일 여부
     */
    public boolean isHoliday() {
        for (String holiday : HOLIDAYS) {
            if (this.content.contains(holiday)) return true;
        }
        return false;
    }

    @Override
    public int compareTo(Schedule schedule) {
        return this.date.compareTo(schedule.getDate());
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", this.getScheduleType(), this.content);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Schedule schedule)) return false;
        return Objects.equals(this.date, schedule.date) && Objects.equals(this.content, schedule.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.date, this.content);
    }
}