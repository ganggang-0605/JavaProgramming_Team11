package kr.ac.knu.calendar.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ItemPanel extends JPanel {
    public ItemPanel() {
        super(new FlowLayout());
    }

    public JButton addButton(String text, ActionListener event) {
        JButton button = new JButton(text);
        button.addActionListener(event);

        this.add(button);
        return button;
    }

    public <T> JComboBox<T> addComboBox(T[] items, ActionListener event) {
        JComboBox<T> comboBox = new JComboBox<>(items);
        comboBox.addActionListener(event);

        this.add(comboBox);
        return comboBox;
    }

    public JTextField addField(String text, int columns) {
        JTextField field = new JTextField(text, columns);

        this.add(field);
        return field;
    }

    public JLabel addLabel(String text) {
        JLabel label = new JLabel(text);

        this.add(label);
        return label;
    }

    public JLabel addLabel(String text, int horizontalAlignment) {
        JLabel label = new JLabel(text, horizontalAlignment);

        this.add(label);
        return label;
    }
}