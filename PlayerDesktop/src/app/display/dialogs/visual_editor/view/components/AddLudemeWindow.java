package app.display.dialogs.visual_editor.view.components;

import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Comparator;
import java.util.List;

public class AddLudemeWindow extends JPanel {

    DefaultListModel listModel;
    public JTextField searchField;
    JScrollPane scrollableList;

    IGraphPanel graphPanel;
    boolean connect;

    public void updateList(List<Ludeme> ludemeList){
        removeAll();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.GREEN);

        searchField = new JTextField();

        // remove duplicates
        ludemeList = ludemeList.stream().distinct().sorted(Comparator.comparing(Ludeme::getName)).collect(java.util.stream.Collectors.toList());

        //TODO: List of ludemes is sorted here RECS
        // TODO: get list of ludemes and connections from editorpanel
        ludemeList.sort(Comparator.comparing(Object::toString));

        listModel = new DefaultListModel<Ludeme>();
        for (Ludeme l : ludemeList) {
            listModel.addElement(l);
        }
        JList list = new JList(listModel);
        scrollableList = new JScrollPane(list);

        scrollableList.setPreferredSize(new Dimension(scrollableList.getPreferredSize().width, 150));
        searchField.setPreferredSize(new Dimension(scrollableList.getPreferredSize().width, searchField.getPreferredSize().height));


        List<Ludeme> ludemeList_copy = ludemeList;
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
                for(Ludeme l : ludemeList_copy){
                    // TODO: Improve
                    if(l.getName().contains(searchField.getText())){
                        listModel.addElement(l);
                    }
                }
                list.setModel(listModel);
                repaint();
            }
        });



        add(searchField); add(scrollableList);

        setPreferredSize(new Dimension(getPreferredSize()));
        setSize(getPreferredSize());
        setVisible(false);


        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList theList = (JList) mouseEvent.getSource();
                int index = theList.locationToIndex(mouseEvent.getPoint());
                if (index >= 0) {
                    Object o = theList.getModel().getElementAt(index);
                    graphPanel.addNode((Ludeme) o, getLocation().x, getLocation().y, connect);
                    searchField.setText("");
                    scrollableList.getVerticalScrollBar().setValue(0);
                }
            }
        };
        list.addMouseListener(mouseListener);
    }

    public AddLudemeWindow(List<Ludeme> ludemeList, IGraphPanel graphPanel, boolean connect){
        this.graphPanel = graphPanel;
        this.connect = connect;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.GREEN);

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
                for(Ludeme l : ludemeList){
                    // TODO: Improve
                    if(l.getName().contains(searchField.getText())){
                        listModel.addElement(l);
                    }
                }
                list.setModel(listModel);
                repaint();
            }
        });



        add(searchField); add(scrollableList);

        setPreferredSize(new Dimension(getPreferredSize()));
        setSize(getPreferredSize());
        setVisible(false);


        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList theList = (JList) mouseEvent.getSource();
                int index = theList.locationToIndex(mouseEvent.getPoint());
                if (index >= 0) {
                    Object o = theList.getModel().getElementAt(index);
                    graphPanel.addNode((Ludeme) o, getLocation().x, getLocation().y, connect);
                    searchField.setText("");
                    scrollableList.getVerticalScrollBar().setValue(0);
                }
            }
        };
        list.addMouseListener(mouseListener);


    }
}
