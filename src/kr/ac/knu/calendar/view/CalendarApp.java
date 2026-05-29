package kr.ac.knu.calendar.view;

import kr.ac.knu.calendar.model.*;
import kr.ac.knu.calendar.util.DataLoader;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class CalendarApp extends JFrame {
    private ScheduleManager manager;
    private YearMonth currentMonth;
    private JPanel calendarPanel;
    private JLabel monthLabel;
    private JComboBox<String> filterBox;
    private JTextField dateField;
    private JTextField contentField;

    public CalendarApp() {
        manager = new ScheduleManager();
        manager.fetchSchedules(LocalDate.now().getYear());
        currentMonth = YearMonth.now();

        setTitle("KNU 달력 시스템");
        setSize(900, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initUI();
        updateCalendar();
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new FlowLayout());
        JButton prevBtn = new JButton("<");
        JButton nextBtn = new JButton(">");
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        String[] filters = {"전체 보기", "학사 일정", "학사 일정 (대학원)", "개인 일정"};
        filterBox = new JComboBox<>(filters);

        topPanel.add(prevBtn);
        topPanel.add(monthLabel);
        topPanel.add(nextBtn);
        topPanel.add(new JLabel("  필터: "));
        topPanel.add(filterBox);
        add(topPanel, BorderLayout.NORTH);

        calendarPanel = new JPanel(new GridLayout(0, 7));
        add(calendarPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        dateField = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 10);
        contentField = new JTextField(20);
        JButton addQuickBtn = new JButton("개인일정 등록");

        bottomPanel.add(new JLabel("날짜(yyyy-MM-dd):"));
        bottomPanel.add(dateField);
        bottomPanel.add(new JLabel("내용:"));
        bottomPanel.add(contentField);
        bottomPanel.add(addQuickBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        prevBtn.addActionListener(e -> { currentMonth = currentMonth.minusMonths(1); updateCalendar(); });
        nextBtn.addActionListener(e -> { currentMonth = currentMonth.plusMonths(1); updateCalendar(); });
        filterBox.addActionListener(e -> updateCalendar());

        addQuickBtn.addActionListener(e -> {
            LocalDate date = LocalDate.parse(dateField.getText());
            String c = contentField.getText();
            if (!c.isEmpty()) {
                manager.addSchedule(date, new PersonalSchedule(date, c));
                updateCalendar();
                contentField.setText("");
                JOptionPane.showMessageDialog(this, "개인일정이 등록되었습니다.");
            }
        });
    }

    private boolean isHoliday(String content) {
        String[] holidays = {"부처님", "어린이날", "추석", "설날", "현충일", "광복절", "개천절", "한글날", "삼일절", "공휴일", "대체공휴", "기념일"};
        for (String h : holidays) {
            if (content.contains(h)) return true;
        }
        return false;
    }

    private void updateCalendar() {
        calendarPanel.removeAll();
        monthLabel.setText(currentMonth.getYear() + "년 " + currentMonth.getMonthValue() + "월");

        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        for (String day : days) {
            JLabel dLabel = new JLabel(day, SwingConstants.CENTER);
            dLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            if (day.equals("일")) dLabel.setForeground(Color.RED);
            if (day.equals("토")) dLabel.setForeground(Color.BLUE);
            calendarPanel.add(dLabel);
        }

        LocalDate firstDay = currentMonth.atDay(1);
        int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentMonth.lengthOfMonth();

        for (int i = 0; i < startDayOfWeek; i++) {
            calendarPanel.add(new JLabel(""));
        }

        int filterType = filterBox.getSelectedIndex();
        LocalDate today = LocalDate.now();

        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate currentDate = currentMonth.atDay(i);
            java.util.List<Schedule> daySchedules = manager.getSchedules(currentDate);

            JButton dayBtn = new JButton();
            dayBtn.setLayout(new BorderLayout());
            // macOS 환경에서 setBackground가 정상적으로 동작하지 않는 버그 해결
            // Reference: https://stackoverflow.com/a/9852024
            dayBtn.setOpaque(true);

            if (currentDate.equals(today)) {
                dayBtn.setBackground(new Color(255, 255, 153));
                dayBtn.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            } else {
                dayBtn.setBackground(Color.WHITE);
                dayBtn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            }

            JLabel dayNum = new JLabel(String.valueOf(i));
            dayNum.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
            if (currentDate.getDayOfWeek().getValue() == 7) dayNum.setForeground(Color.RED);
            if (currentDate.getDayOfWeek().getValue() == 6) dayNum.setForeground(Color.BLUE);

            if (currentDate.equals(today)) {
                dayNum.setFont(new Font("맑은 고딕", Font.BOLD, 13));
            }
            dayBtn.add(dayNum, BorderLayout.NORTH);

            if (!daySchedules.isEmpty()) {
                StringBuilder info = new StringBuilder("<html><div style='text-align:left; padding-left:5px; padding-right:5px;'>");
                int count = 0;
                for (Schedule s : daySchedules) {
                    if (count++ > 3) break;

                    if (filterType == 1) {
                        // 학사 일정
                        if (s instanceof AcademicSchedule academicSchedule) {
                            if (academicSchedule.getCategory().equals("대학원"))
                                continue;
                        } else if (s instanceof PersonalSchedule) {
                            continue;
                        }
                    } else if (filterType == 2) {
                        // 학사 일정 (대학원 포함)
                        if (s instanceof PersonalSchedule) {
                            continue;
                        }
                    } else if (filterType == 3) {
                        // 개인 일정
                        if (s instanceof AcademicSchedule) {
                            continue;
                        }
                    }
                    String text = s.getContent();

                    // 색상 판별 로직 적용
                    String color = "black"; // 기본 학사일정: 검은색

                    if (s instanceof AcademicSchedule) {
                        if (isHoliday(text)) {
                            color = "red"; // 공휴일 키워드가 포함되어 있으면 빨간색
                            dayNum.setForeground(Color.RED); // 날짜 텍스트도 빨간색으로 변경
                        }
                    } else if (s instanceof PersonalSchedule) {
                        color = "blue"; // 개인일정은 파란색
                    }

                    info.append("<font color='").append(color).append("'>- ").append(text).append("</font><br>");
                }
                if (daySchedules.size() > 3) {
                    info.append("<font color='gray'>+").append(daySchedules.size() - 3).append(" 더보기</font>");
                }
                info.append("</div></html>");

                JLabel infoLabel = new JLabel(info.toString());
                infoLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
                infoLabel.setVerticalAlignment(SwingConstants.TOP);
                dayBtn.add(infoLabel, BorderLayout.CENTER);
            }

            dayBtn.addActionListener(e -> openDayDialog(currentDate));
            calendarPanel.add(dayBtn);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    public String getDDayString(LocalDate date) {
        long dDay = ChronoUnit.DAYS.between(LocalDate.now(), date);
        String dDayString = "D-Day";
        if (dDay > 0) {
            dDayString = "D-" + dDay;
        } else if (dDay < 0) {
            dDayString = "D+" + Math.abs(dDay);
        }
        return dDayString;
    }

    private void openDayDialog(LocalDate date) {
        JDialog dialog = new JDialog(this, String.format("%s 상세 일정 (%s)", date, getDDayString(date)), true);
        dialog.setSize(500, 400);
        dialog.setLayout(new BorderLayout());

        DefaultListModel<Schedule> listModel = new DefaultListModel<Schedule>();
        JList<Schedule> list = new JList<Schedule>(listModel);
        list.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        dialog.add(new JScrollPane(list), BorderLayout.CENTER);

        Runnable refreshList = () -> {
            listModel.clear();
            java.util.List<Schedule> s = manager.getSchedules(date);
            for (Schedule sch : s) {
                int filterType = filterBox.getSelectedIndex();
                if (filterType == 1) {
                    // 학사 일정
                    if (s instanceof AcademicSchedule academicSchedule) {
                        if (academicSchedule.getCategory().equals("대학원"))
                            continue;
                    } else if (s instanceof PersonalSchedule) {
                        continue;
                    }
                } else if (filterType == 2) {
                    // 학사 일정 (대학원 포함)
                    if (s instanceof PersonalSchedule) {
                        continue;
                    }
                } else if (filterType == 3) {
                    // 개인 일정
                    if (s instanceof AcademicSchedule) {
                        continue;
                    }
                }

                listModel.addElement(sch);
            }
            updateCalendar();
        };
        refreshList.run();

        JPanel btnPanel = new JPanel();
        JButton addPerBtn = new JButton("개인일정 등록");
        JButton editBtn = new JButton("수정");
        JButton delBtn = new JButton("삭제");

        addPerBtn.addActionListener(e -> {
            String content = JOptionPane.showInputDialog(dialog, "개인일정 내용 입력:");
            if (content != null && !content.trim().isEmpty()) {
                manager.addSchedule(date, new PersonalSchedule(date, content));
                refreshList.run();
            }
        });

        editBtn.addActionListener(e -> {
            Schedule sel = list.getSelectedValue();
            if (sel != null) {
                String newContent = JOptionPane.showInputDialog(dialog, "수정할 내용:", sel.getContent());
                if (newContent != null && !newContent.trim().isEmpty()) {
                    sel.setContent(newContent);
                    refreshList.run();
                }
            }
        });

        delBtn.addActionListener(e -> {
            Schedule sel = list.getSelectedValue();
            if (sel != null) {
                manager.removeSchedule(date, sel);
                refreshList.run();
            }
        });

        btnPanel.add(addPerBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CalendarApp().setVisible(true));
    }
}