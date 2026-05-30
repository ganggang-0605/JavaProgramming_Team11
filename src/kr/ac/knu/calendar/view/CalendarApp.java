package kr.ac.knu.calendar.view;

import kr.ac.knu.calendar.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CalendarApp extends JFrame {
    private static final String TITLE = "KNU 달력 시스템";
    private static final String MONTH_TITLE = "%d년 %d월";
    private static final String[] FILTERS = {"전체 보기", "학사 일정", "학사 일정 (대학원)", "개인 일정"};
    private static final String[] DAYS = {"일", "월", "화", "수", "목", "금", "토"};
    private final ScheduleManager manager;
    private final ScheduleFilter filter;
    private final List<Year> loadedYears;
    private ScheduleLoaderThread scheduleLoader;
    private YearMonth currentMonth;
    private JPanel calendarPanel;
    private JLabel monthLabel;

    public CalendarApp() {
        this.manager = new ScheduleManager();
        this.filter = new ScheduleFilter(0);
        this.loadedYears = new ArrayList<>();

        this.scheduleLoader = new ScheduleLoaderThread(this.manager, Year.now(), null);
        this.scheduleLoader.start();

        this.currentMonth = YearMonth.now();
    }

    public ScheduleManager getManager() { return this.manager; }
    public ScheduleFilter getFilter() { return this.filter; }
    public YearMonth getCurrentMonth() { return this.currentMonth; }
    public void setCurrentMonth(YearMonth month) { this.currentMonth = month; }
    public JPanel getCalendarPanel() { return this.calendarPanel; }
    public void setCalendarPanel(JPanel panel) { this.calendarPanel = panel; }
    public JLabel getMonthLabel() { return this.monthLabel; }
    public void setMonthLabel(JLabel label) { this.monthLabel = label; }

    private void init() {
        this.setTitle(TITLE);
        this.setSize(900, 800);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());

        this.addTopPanel();
        this.addCalendarPanel();
        this.addBottomPanel();

        this.setVisible(true);

        try {
            this.scheduleLoader.join();
            this.loadedYears.add(Year.now());
        } catch (InterruptedException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "일정을 불러오는 도중 오류가 발생하였습니다.",
                    TITLE,
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            SwingUtilities.invokeLater(this::updateCalendar);
        }
    }

    public void updateCalendar() {
        this.calendarPanel.removeAll();
        this.monthLabel.setText(String.format(
                MONTH_TITLE,
                this.currentMonth.getYear(),
                this.currentMonth.getMonthValue()
        ));

        for (String day : DAYS) {
            JLabel dLabel = new JLabel(day, SwingConstants.CENTER);
            dLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            if (day.equals("일")) dLabel.setForeground(Color.RED);
            else if (day.equals("토")) dLabel.setForeground(Color.BLUE);
            this.calendarPanel.add(dLabel);
        }

        // 1일 시작 전 빈칸 추가
        int startDayOfWeek = this.currentMonth.atDay(1).getDayOfWeek().getValue() % 7;
        for (int i = 0; i < startDayOfWeek; i++) {
            this.calendarPanel.add(new JLabel(""));
        }

        for (int day = 1; day <= this.currentMonth.lengthOfMonth(); day++) {
            this.addDayButton(this.currentMonth.atDay(day));
        }

        this.calendarPanel.revalidate();
        this.calendarPanel.repaint();
    }

    private void addTopPanel() {
        ItemPanel topPanel = new ItemPanel();

        topPanel.addButton("<", _ -> {
            // 이전연도 학사일정 로딩
            fetchSchedules(this.currentMonth.minusMonths(1));
        });

        this.monthLabel = topPanel.addLabel("", SwingConstants.CENTER);
        this.monthLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        topPanel.addButton(">", _ -> {
            // 다음연도 학사일정 로딩
            fetchSchedules(this.currentMonth.plusMonths(1));
        });

        topPanel.addLabel("  필터: ");

        topPanel.addComboBox(FILTERS, e -> {
            @SuppressWarnings("unchecked")
            JComboBox<String> filterBox = (JComboBox<String>) e.getSource();
            this.filter.setFilterType(filterBox.getSelectedIndex());
            SwingUtilities.invokeLater(this::updateCalendar);
        });

        this.add(topPanel, BorderLayout.NORTH);
    }

    private void fetchSchedules(YearMonth newMonth) {
        Year newYear = Year.of(newMonth.getYear());
        if (this.currentMonth.getYear() != newYear.getValue() && !this.loadedYears.contains(newYear)) {
            ScheduleLoaderThread scheduleLoader = new ScheduleLoaderThread(
                    this.manager,
                    Year.of(newMonth.getYear()),
                    success -> {
                        if (success) {
                            this.loadedYears.add(newYear);
                            SwingUtilities.invokeLater(this::updateCalendar);
                        }
                        else {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "일정을 불러오는 도중 오류가 발생하였습니다.",
                                    TITLE,
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
            );
            scheduleLoader.start();
        }

        this.currentMonth = newMonth;
        SwingUtilities.invokeLater(this::updateCalendar);
    }

    private void addCalendarPanel() {
        this.calendarPanel = new JPanel(new GridLayout(0, 7));
        SwingUtilities.invokeLater(this::updateCalendar);
        this.add(this.calendarPanel, BorderLayout.CENTER);
    }

    private void addBottomPanel() {
        ItemPanel bottomPanel = new ItemPanel();

        bottomPanel.addLabel("날짜 (yyyy-MM-dd):");
        JTextField dateField = bottomPanel.addField(
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                10
        );

        bottomPanel.addLabel("내용:");
        JTextField contentField = bottomPanel.addField("", 20);

        ActionListener listener = _ -> {
            try {
                LocalDate date = LocalDate.parse(dateField.getText());
                String c = contentField.getText().trim();
                if (!c.isEmpty()) {
                    contentField.setText("");
                    this.manager.addSchedule(date, new PersonalSchedule(date, c));
                    SwingUtilities.invokeLater(this::updateCalendar);
                    JOptionPane.showMessageDialog(
                            this,
                            "개인일정이 등록되었습니다.",
                            "일정 등록",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            } catch (DateTimeParseException _) {
                JOptionPane.showMessageDialog(
                        this,
                        "날짜 입력이 올바르지 않습니다.",
                        "일정 등록",
                        JOptionPane.ERROR_MESSAGE
                );
            } finally {
                // macOS 환경에서 팝업을 띄운 이후 focus를 다른 곳으로 빼주지 않으면
                // 앱 최초 실행 후 개인일정 내용에 한글을 입력하고 개인일정 등록 버튼을 눌렀을 때
                // 스페이스, 탭 등을 눌러서 focus를 주지 않으면 아무 입력도 받아들이지 않는 버그 해결
                contentField.requestFocusInWindow();
            }
        };

        bottomPanel.addButton("개인일정 등록", listener);
        contentField.addActionListener(listener);

        this.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addDayButton(LocalDate date) {
        JButton dayButton = new JButton();
        dayButton.setLayout(new BorderLayout());
        // macOS 환경에서 setBackground가 정상적으로 동작하지 않는 버그 해결
        // Reference: https://stackoverflow.com/a/9852024
        dayButton.setOpaque(true);

        if (date.equals(LocalDate.now())) {
            dayButton.setBackground(new Color(255, 255, 153));
            dayButton.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        } else {
            dayButton.setBackground(Color.WHITE);
            dayButton.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        }

        dayButton.addActionListener(_ -> {
            DetailDialog dialog = new DetailDialog(
                    this,
                    date
            );
            dialog.init();
        });

        JLabel dayNum = new JLabel(Integer.toString(date.getDayOfMonth()));
        dayNum.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        if (date.getDayOfWeek().getValue() == 7) dayNum.setForeground(Color.RED);
        if (date.getDayOfWeek().getValue() == 6) dayNum.setForeground(Color.BLUE);

        if (date.equals(LocalDate.now())) {
            dayNum.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        }
        dayButton.add(dayNum, BorderLayout.NORTH);

        List<Schedule> daySchedules = this.manager.getFilteredSchedules(date, this.filter);
        if (!daySchedules.isEmpty()) {
            StringBuilder info = new StringBuilder("<html><div style='padding-left:5px; padding-right:5px;'>");
            for (int i = 0; i < Math.min(daySchedules.size(), 3); i++) {
                Schedule schedule = daySchedules.get(i);

                String color = "black"; // 기본 학사일정: 검은색
                if (schedule instanceof AcademicSchedule) {
                    if (schedule.isHoliday()) {
                        color = "red"; // 공휴일: 빨간색
                        dayNum.setForeground(Color.RED); // 날짜 텍스트도 빨간색으로 변경
                    }
                } else if (schedule instanceof PersonalSchedule) {
                    color = "blue"; // 개인일정: 파란색
                }

                info.append("<font color='").append(color).append("'>- ").append(schedule.getContent()).append("</font><br/>");
            }
            if (daySchedules.size() > 3) {
                info.append("<font color='gray'>+").append(daySchedules.size() - 3).append(" 더보기</font>");
            }
            info.append("</div></html>");

            JLabel infoLabel = new JLabel(info.toString());
            infoLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            infoLabel.setVerticalAlignment(SwingConstants.TOP);
            dayButton.add(infoLabel, BorderLayout.CENTER);
        }

        this.calendarPanel.add(dayButton);
    }

    public static void main(String[] args) {
        CalendarApp app = new CalendarApp();
        app.init();
    }
}