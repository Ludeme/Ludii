package app.display.dialogs.visual_editor.view.components;


import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.view.panels.editor.EditorPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Comparator;
import java.util.List;

public class ConnectLudemeWindow extends JPanel {

    DefaultListModel listModel;
    public JTextField searchField;
    JScrollPane scrollableList;

    public ConnectLudemeWindow(List<Ludeme> ludemeList, EditorPanel editorPanel) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        searchField = new JTextField();

        ludemeList.sort(Comparator.comparing(Object::toString));

        listModel = new DefaultListModel<Ludeme>();
        for (Ludeme l : ludemeList) {
            listModel.addElement(l);
        }
        JList list = new JList(listModel);
        scrollableList = new JScrollPane(list);

        scrollableList.setPreferredSize(new Dimension(scrollableList.getPreferredSize().width, 150));
        searchField.setPreferredSize(new Dimension(scrollableList.getPreferredSize().width, searchField.getPreferredSize().height));


        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                listModel = new DefaultListModel<Ludeme>();
                for (Ludeme l : ludemeList) {
                    // TODO: Improve
                    if (l.getName().contains(searchField.getText())) {
                        listModel.addElement(l);
                    }
                }
                list.setModel(listModel);
                repaint();
            }
        });


        add(searchField);
        add(scrollableList);

        setPreferredSize(new Dimension(getPreferredSize()));
        setSize(getPreferredSize());
        setVisible(false);


        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList theList = (JList) mouseEvent.getSource();
                int index = theList.locationToIndex(mouseEvent.getPoint());
                if (index >= 0) {
                    Object o = theList.getModel().getElementAt(index);
                    editorPanel.addLudemeNode((Ludeme) o, getLocation());
                    searchField.setText("");
                    scrollableList.getVerticalScrollBar().setValue(0);
                }
            }
        };
        list.addMouseListener(mouseListener);


    }
}
