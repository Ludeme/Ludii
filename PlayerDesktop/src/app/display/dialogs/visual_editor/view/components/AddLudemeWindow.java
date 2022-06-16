package app.display.dialogs.visual_editor.view.components;

import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import main.grammar.Symbol;

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



    public AddLudemeWindow(List<Symbol> symbolList, IGraphPanel graphPanel, boolean connect){
        this.graphPanel = graphPanel;
        this.connect = connect;

        updateList(symbolList);

    }

    private void drawComponents(){
        removeAll();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        scrollableList.setPreferredSize(new Dimension(scrollableList.getPreferredSize().width, 150));
        searchField.setPreferredSize(new Dimension(scrollableList.getPreferredSize().width, searchField.getPreferredSize().height));


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
                    graphPanel.addNode((Symbol) o, getLocation().x, getLocation().y, connect);
                    searchField.setText("");
                    scrollableList.getVerticalScrollBar().setValue(0);
                }
            }
        };
        list.addMouseListener(mouseListener);

    }

    public void updateList(List<Symbol> symbolList){


        searchField = new JTextField();

        // remove duplicates
        symbolList = symbolList.stream().distinct().collect(java.util.stream.Collectors.toList());

        listModel = new DefaultListModel<Symbol>();
        for (Symbol l : symbolList) {
            listModel.addElement(l);
        }
        list = new JList(listModel);
        scrollableList = new JScrollPane(list);

        List<Symbol> ludemeList_copy = symbolList;

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
                listModel = new DefaultListModel<Symbol>();
                for(Symbol l : ludemeList_copy){
                    // TODO: Improve
                    if(l.name().contains(searchField.getText())){
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
