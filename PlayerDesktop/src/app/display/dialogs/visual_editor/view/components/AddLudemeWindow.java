package app.display.dialogs.visual_editor.view.components;

import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.view.DesignPalette;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Comparator;
import java.util.List;

public class AddLudemeWindow extends JPanel {

    JList list;
    DefaultListModel listModel;
    public JTextField searchField;
    JScrollPane scrollableList;

    IGraphPanel graphPanel;
    boolean connect;



    public AddLudemeWindow(List<Ludeme> ludemeList, IGraphPanel graphPanel, boolean connect){
        this.graphPanel = graphPanel;
        this.connect = connect;

        updateList(ludemeList);

    }

    private void drawComponents(){
        removeAll();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        scrollableList.setPreferredSize(new Dimension(scrollableList.getPreferredSize().width, 150));
        searchField.setPreferredSize(new Dimension(scrollableList.getPreferredSize().width, searchField.getPreferredSize().height));

/*
        int widthOfPanels = (int) (searchField.getPreferredSize().width * 1.3);
        int heightOfTopPanel = (int) (searchField.getPreferredSize().height * 1.2);

        EmptyBorder searchFieldBorder = new EmptyBorder(heightOfTopPanel-searchField.getPreferredSize().height, widthOfPanels-searchField.getPreferredSize().width, heightOfTopPanel-searchField.getPreferredSize().height, widthOfPanels-searchField.getPreferredSize().width);

        int heightOfBottomPanel = (int) (scrollableList.getPreferredSize().height * 1.15);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));


        searchField.setBorder(searchFieldBorder);
        top.setBackground(DesignPalette.BACKGROUND_LUDEME_BODY);
        top.setBackground(Color.RED);
        top.add(searchField);
        top.setSize(widthOfPanels, heightOfTopPanel);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.setSize(widthOfPanels, scrollableList.getPreferredSize().height);


        bottom.add(scrollableList);

        add(top);add(bottom);*/

        add(searchField); add(scrollableList);

        repaint();


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

    public void updateList(List<Ludeme> ludemeList){


        searchField = new JTextField();

        // remove duplicates
        ludemeList = ludemeList.stream().distinct().sorted(Comparator.comparing(Ludeme::getName)).collect(java.util.stream.Collectors.toList());

        //TODO: List of ludemes is sorted here RECS
        // TODO: get list of ludemes and connections from editorpanel
        //ludemeList.sort(Comparator.comparing(Object::toString));

        listModel = new DefaultListModel<Ludeme>();
        for (Ludeme l : ludemeList) {
            listModel.addElement(l);
        }
        list = new JList(listModel);
        scrollableList = new JScrollPane(list);

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
        drawComponents();
    }

}
