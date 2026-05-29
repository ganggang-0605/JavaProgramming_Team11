package kr.ac.knu.calendar.view;

import kr.ac.knu.calendar.model.PersonalSchedule;
import kr.ac.knu.calendar.model.Schedule;
import kr.ac.knu.calendar.model.ScheduleManager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DetailDialog extends JDialog {
    private static final String TITLE = "%s 상세 일정 (%s)";
    private final CalendarApp parent;
    private LocalDate date;
    private int filterType;
    private ItemList<Schedule> list;
    private ItemPanel buttonPanel;

    public DetailDialog(CalendarApp parent, LocalDate date, int filterType) {
        this.parent = parent;
        this.date = date;
        this.filterType = filterType;
    }

    public JFrame getParent() { return this.parent; }
    public LocalDate getDate() { return this.date; }
    public void setDate(LocalDate date) { this.date = date; }
    public int getFilterType() { return this.filterType; }
    public void setFilterType(int filterType) { this.filterType = filterType; }
    public ItemList<Schedule> getList() { return this.list; }
    public void setList(ItemList<Schedule> list) { this.list = list; }
    public ItemPanel getButtonPanel() { return this.buttonPanel; }
    public void setButtonPanel(ItemPanel buttonPanel) { this.buttonPanel = buttonPanel; }

    public void init() {
        this.setTitle(String.format(TITLE, date, this.getDDayString()));
        this.setSize(500, 400);
        this.setLayout(new BorderLayout());
        this.setLocationRelativeTo(this.parent);
        this.setModal(true);

        this.addItemList();
        this.addButtonPanel();

        this.setVisible(true);
    }

    private void addItemList() {
        this.list = new ItemList<>();
        this.updateList();
        this.add(new JScrollPane(this.list), BorderLayout.CENTER);
    }

    private void addButtonPanel() {
        this.buttonPanel = new ItemPanel();
        ScheduleManager manager = this.parent.getManager();

        this.buttonPanel.addButton("개인일정 등록", _ -> {
            String content = JOptionPane.showInputDialog(this, "개인일정 내용 입력:");
            if (content != null && !content.trim().isEmpty()) {
                manager.addSchedule(date, new PersonalSchedule(date, content));
                this.updateList();
            }
        });

        this.buttonPanel.addButton("수정", _ -> {
            Schedule schedule = this.list.getSelectedValue();
            if (schedule == null) return;

            String newContent = JOptionPane.showInputDialog(this, "수정할 내용:", schedule.getContent());
            if (newContent != null && !newContent.trim().isEmpty()) {
                schedule.setContent(newContent);
                this.updateList();
            }
        });

        this.buttonPanel.addButton("삭제", _ -> {
            Schedule schedule = list.getSelectedValue();
            if (schedule == null) return;

            this.parent.getManager().removeSchedule(date, schedule);
            this.updateList();
        });

        this.add(this.buttonPanel, BorderLayout.SOUTH);
    }

    private void updateList() {
        this.list.clear();

        ScheduleManager manager = this.parent.getManager();
        java.util.List<Schedule> schedules = manager.getFilteredSchedules(this.date, this.filterType);
        for (Schedule schedule : schedules) {
            this.list.addElement(schedule);
        }

        this.parent.updateCalendar();
    }

    private String getDDayString() {
        long dDay = ChronoUnit.DAYS.between(LocalDate.now(), this.date);
        String dDayString = "D-Day";
        if (dDay > 0) {
            dDayString = "D-" + dDay;
        } else if (dDay < 0) {
            dDayString = "D+" + Math.abs(dDay);
        }
        return dDayString;
    }
}