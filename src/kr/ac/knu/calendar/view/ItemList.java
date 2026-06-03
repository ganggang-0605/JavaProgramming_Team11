package kr.ac.knu.calendar.view;

import javax.swing.*;
import java.awt.*;

public class ItemList<T> extends JList<T> {
    private final DefaultListModel<T> listModel;

    public ItemList() {
        super(new DefaultListModel<>());

        this.listModel = (DefaultListModel<T>) super.getModel();
        this.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
    }

    public DefaultListModel<T> getListModel() { return this.listModel; }

    public T addElement(T element) {
        this.listModel.addElement(element);
        return element;
    }

    public void clear() {
        this.listModel.clear();
    }
}