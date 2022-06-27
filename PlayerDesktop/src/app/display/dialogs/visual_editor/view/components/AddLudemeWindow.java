package app.display.dialogs.visual_editor.view.components;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.recs.utils.HumanReadable;
import app.display.dialogs.visual_editor.recs.utils.Pair;
import app.display.dialogs.visual_editor.recs.utils.ReadableSymbol;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LInputField;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import main.grammar.Clause;
import main.grammar.ClauseArg;
import main.grammar.Symbol;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

public class AddLudemeWindow extends JPanel {

    JList list;
    DefaultListModel listModel;
    public JTextField searchField;
    JScrollPane scrollableList;

    IGraphPanel graphPanel;
    boolean connect;

    private LInputField initiator;



    public AddLudemeWindow(List<Symbol> symbolList, IGraphPanel graphPanel, boolean connect){
        this.graphPanel = graphPanel;
        this.connect = connect;

        updateList(null, symbolList);

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
                graphPanel.addSelectionIndex(index+1); // plus one because we want the position, but indices start at 0

                if (index >= 0) {
                    Object o = theList.getModel().getElementAt(index);
                    if(o != null) {
                        ReadableSymbol rs = (ReadableSymbol) o;
                        // if the symbol is a terminal, dont create a node. Instead create an inputfield
                        if(rs.getSymbol().ludemeType().equals(Symbol.LudemeType.Predefined) || isConstantTerminal(rs.getSymbol()))
                        {
                            // get the node that is being added to
                            LudemeNodeComponent lnc = graphPanel.connectionHandler().getSelectedConnectionComponent().lnc();
                            lnc.addTerminal(rs.getSymbol());
                            graphPanel.connectionHandler().cancelNewConnection();
                            setVisible(false);
                        }
                        else
                        {
                            System.out.println(initiator);
                            // find matching NodeArgument
                            NodeArgument match = null;
                            if(initiator == null)
                            {
                                Handler.addNode(graphPanel.graph(), rs.getSymbol(), null, getLocation().x, getLocation().y, false);
                            }
                            else {
                                for (NodeArgument na : initiator.nodeArguments()) {
                                    if (na.possibleSymbolInputsExpanded().contains(rs.getSymbol())) {
                                        match = na;
                                        break;
                                    }
                                }
                                System.out.println(match);
                                Handler.addNode(graphPanel.graph(), rs.getSymbol(), match, getLocation().x, getLocation().y, connect);
                                //graphPanel.addNode(rs.getSymbol(), getLocation().x, getLocation().y, connect);
                            }
                        }
                        searchField.setText("");
                        scrollableList.getVerticalScrollBar().setValue(0);
                    }
                }
            }
        };
        list.addMouseListener(mouseListener);
    }

    private boolean isConstantTerminal(Symbol s)
    {
        if(s.rule() == null) return false;
        for(Clause c : s.rule().rhs())
        {
            if(c.args() == null) continue;
            for(ClauseArg ca : c.args())
            {
                if(!ca.symbol().ludemeType().equals(Symbol.LudemeType.Constant)) return false;
            }
        }
        return true;
    }

    public void updateList(LInputField initiator, List<Symbol> symbolList){

        this.initiator = initiator;

        searchField = new JTextField();

        // remove duplicates
        symbolList = symbolList.stream().distinct().collect(java.util.stream.Collectors.toList());


        List<ReadableSymbol> ludemeList_copy = new ArrayList<>();

        listModel = new DefaultListModel<ReadableSymbol>();
        for (Symbol s : symbolList) {
            if(s != null) {
                ReadableSymbol rs = new ReadableSymbol(s);
                listModel.addElement(rs);
                ludemeList_copy.add(rs);
            }
        }

        list = new JList(listModel);
        scrollableList = new JScrollPane(list);

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
                for(ReadableSymbol rs : ludemeList_copy){
                    Symbol l = rs.getSymbol();
                    // TODO: Improve
                    if(l.name().contains(searchField.getText())){
                        listModel.addElement(rs);
                    }
                }
                list.setModel(listModel);
                repaint();
            }
        });
        drawComponents();
    }

}
