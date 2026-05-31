package kr.ac.knu.calendar.view;

import javax.swing.*;
import java.awt.*;

public class ItemList<T> extends JList<T> {
    private final DefaultListModel<T> listModel;

    public ItemList() {
        DefaultListModel<T> listModel = new DefaultListModel<>();
        super(listModel);

        this.listModel = listModel;
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