package kr.ac.knu.calendar.model;

import java.time.LocalDate;
import java.util.Arrays;

public class ScheduleManager {
    private Schedule[] schedules;
    private int count;

    public ScheduleManager() {
        this.schedules = new Schedule[1000];
        this.count = 0;
    }

    public void addSchedule(Schedule s) {
        if (count < schedules.length) {
            schedules[count++] = s;
        }
    }

    public void removeSchedule(Schedule s) {
        for (int i = 0; i < count; i++) {
            if (schedules[i] == s) {
                for (int j = i; j < count - 1; j++) {
                    schedules[j] = schedules[j + 1];
                }
                schedules[--count] = null;
                break;
            }
        }
    }

    public Schedule[] getFilteredSchedules(int filterType, LocalDate targetDate) {
        Schedule[] temp = new Schedule[count];
        int mCount = 0;

        for (int i = 0; i < count; i++) {
            Schedule s = schedules[i];
            boolean typeMatch = (filterType == 0) ||
                    (filterType == 1 && s instanceof AcademicSchedule) ||
                    (filterType == 2 && s instanceof PersonalSchedule);

            boolean dateMatch = true;
            if (targetDate != null) {
                dateMatch = s.isOccurringOn(targetDate);
            }

            if (typeMatch && dateMatch) {
                temp[mCount++] = s;
            }
        }

        Schedule[] result = new Schedule[mCount];
        System.arraycopy(temp, 0, result, 0, mCount);
        Arrays.sort(result);
        return result;
    }
}