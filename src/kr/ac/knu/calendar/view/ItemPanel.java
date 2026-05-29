package kr.ac.knu.calendar.view;

import javax.swing.*;
import java.awt.event.ActionListener;

public class ItemPanel extends JPanel {
    public ItemPanel() {
        super();
    }

    public void addButton(String text, ActionListener event) {
        JButton button = new JButton(text);
        button.addActionListener(event);

        this.add(button);
    }
}