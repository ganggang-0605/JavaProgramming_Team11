package kr.ac.knu.calendar.view;

import kr.ac.knu.calendar.model.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DetailDialog extends JDialog {
    private static final String TITLE = "%s 상세 일정 (%s)";
    private final CalendarApp parent;
    private LocalDate date;
    private ItemList<Schedule> list;
    private ItemPanel buttonPanel;
    private JButton editButton;
    private JButton deleteButton;

    public DetailDialog(CalendarApp parent, LocalDate date) {
        super(parent, true);
        this.parent = parent;
        this.date = date;
    }

    public JFrame getParent() { return this.parent; }
    public LocalDate getDate() { return this.date; }
    public void setDate(LocalDate date) { this.date = date; }
    public ItemList<Schedule> getList() { return this.list; }
    public void setList(ItemList<Schedule> list) { this.list = list; }
    public ItemPanel getButtonPanel() { return this.buttonPanel; }
    public void setButtonPanel(ItemPanel buttonPanel) { this.buttonPanel = buttonPanel; }

    public void init() {
        this.setTitle(String.format(TITLE, date, this.getDDayString()));
        this.setSize(500, 400);
        this.setLayout(new BorderLayout());
        this.setLocationRelativeTo(this.parent);

        this.addItemList();
        this.addButtonPanel();

        this.setVisible(true);
    }

    private void addItemList() {
        this.list = new ItemList<>();
        this.list.addListSelectionListener(e -> {
            // 개인일정만 수정 또는 삭제 가능
            Schedule selected = this.list.getSelectedValue();
            this.editButton.setEnabled(selected instanceof PersonalSchedule);
            this.deleteButton.setEnabled(selected instanceof PersonalSchedule);
        });

        SwingUtilities.invokeLater(this::updateList);
        this.add(new JScrollPane(this.list), BorderLayout.CENTER);
    }

    private void addButtonPanel() {
        this.buttonPanel = new ItemPanel();
        ScheduleManager manager = this.parent.getManager();

        this.buttonPanel.addButton("개인일정 등록", _ -> {
            String content = JOptionPane.showInputDialog(
                    this,
                    "개인일정 내용:",
                    "일정 등록",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (content != null && !content.trim().isEmpty()) {
                Schedule schedule = new PersonalSchedule(this.date, content);
                manager.addSchedule(schedule);
                SwingUtilities.invokeLater(this::updateList);
                this.list.setSelectedValue(schedule, true);
            }
        });

        this.editButton = this.buttonPanel.addButton("수정", _ -> {
            Schedule schedule = this.list.getSelectedValue();
            if (schedule == null) return;

            if (schedule instanceof AcademicSchedule) {
                JOptionPane.showMessageDialog(
                        this,
                        "학사일정은 수정할 수 없습니다.",
                        "일정 수정",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            Object response = JOptionPane.showInputDialog(
                    this,
                    "수정할 내용:",
                    "일정 수정",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    schedule.getContent()
            );
            if (response == null) return;

            String content = response.toString().trim();
            if (!content.isEmpty()) {
                schedule.setContent(content);
                SwingUtilities.invokeLater(this::updateList);
                this.list.setSelectedValue(schedule, true);
            }
        });
        this.editButton.setEnabled(false);

        this.deleteButton = this.buttonPanel.addButton("삭제", _ -> {
            Schedule schedule = this.list.getSelectedValue();
            if (schedule == null) return;

            if (schedule instanceof AcademicSchedule) {
                JOptionPane.showMessageDialog(
                        this,
                        "학사일정은 삭제할 수 없습니다.",
                        "일정 삭제",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            int response = JOptionPane.showConfirmDialog(
                    this,
                    schedule.getContent() + "\n일정을 정말로 삭제하시겠습니까?",
                    "일정 삭제",
                    JOptionPane.OK_CANCEL_OPTION
            );
            if (response == 0) {
                manager.removeSchedule(schedule);
                SwingUtilities.invokeLater(this::updateList);
            }
        });
        this.deleteButton.setEnabled(false);

        this.add(this.buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 일정 목록을 다시 렌더링합니다.
     * 일정 목록의 요소가 업데이트될 때마다 호출하여야 합니다.
     */
    private void updateList() {
        this.list.clear();

        ScheduleManager manager = this.parent.getManager();
        ScheduleFilter filter = this.parent.getFilter();
        java.util.List<Schedule> schedules = manager.getFilteredSchedules(this.date, filter);
        for (Schedule schedule : schedules) {
            this.list.addElement(schedule);
        }

        SwingUtilities.invokeLater(this.parent::updateCalendar);
    }

    /**
     * 오늘까지 남은/지난 시간을 나타냅니다.
     * @return "D-Day" | "D-{dDay}" | "D+{dDay}"
     */
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