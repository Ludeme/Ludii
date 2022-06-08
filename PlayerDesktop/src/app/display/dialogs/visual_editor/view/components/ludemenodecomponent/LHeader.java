package app.display.dialogs.visual_editor.view.components.ludemenodecomponent;

import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LIngoingConnectionComponent;
import main.grammar.Clause;
import main.grammar.Symbol;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LHeader extends JComponent {

    private LIngoingConnectionComponent ingoingConnectionComponent;
    private LudemeNodeComponent LNC;
    private JPanel connectionAndTitle = new JPanel(new FlowLayout(FlowLayout.LEFT));

    public JLabel title;

    public LHeader(LudemeNodeComponent ludemeNodeComponent) {
        LNC = ludemeNodeComponent;
        LudemeNode LN = LNC.node();

        setLayout(new BorderLayout());

        title = new JLabel(getNodeTitle());


        title.setFont(DesignPalette.LUDEME_TITLE_FONT);
        title.setForeground(DesignPalette.FONT_LUDEME_TITLE_COLOR);
        title.setSize(title.getPreferredSize());

        ingoingConnectionComponent = new LIngoingConnectionComponent(this, title.getHeight(), ((int)(title.getHeight()*0.4)), false);

        if(LNC.getGraphPanel().getGraph().getRoot() == LNC.node()) ingoingConnectionComponent = null;

        System.out.println("Root: " + LNC.getGraphPanel().getGraph().getRoot());

        connectionAndTitle = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if(ingoingConnectionComponent!=null) connectionAndTitle.add(ingoingConnectionComponent);
        connectionAndTitle.add(Box.createHorizontalStrut(5));
        connectionAndTitle.add(title);
        connectionAndTitle.setOpaque(false);

        add(connectionAndTitle, BorderLayout.LINE_START);

        JButton constructorBtn = new JButton(DesignPalette.DOWN_ICON);
        EmptyBorder emptyBorder = new EmptyBorder(0, 0, 0, 10);
        //constructorBtn.setBorder(emptyBorder);
        constructorBtn.setFocusPainted(false);
        constructorBtn.setOpaque(false);
        constructorBtn.setContentAreaFilled(false);
        constructorBtn.setBorderPainted(false);
        constructorBtn.setPreferredSize(new Dimension(title.getHeight(), title.getHeight()));
        constructorBtn.setSize(new Dimension(title.getHeight(), title.getHeight()));

        JPanel constructorPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        constructorPanel.add(constructorBtn);
        constructorPanel.add(Box.createHorizontalStrut(0));
        constructorPanel.setOpaque(false);


        add(constructorPanel, BorderLayout.LINE_END);

        JPopupMenu popup = new JPopupMenu();

        JMenuItem[] items = new JMenuItem[LN.clauses().size()];

        for(int i = 0; i < LN.clauses().size(); i++) {
            Clause clause = LN.clauses().get(i);
            items[i] = new JMenuItem(clause.toString());
            items[i].addActionListener(e1 -> {
                System.out.println("Selected clause: " + clause.toString());
                ludemeNodeComponent.changeClause(clause);
                repaint();
            });
            popup.add(items[i]);
        }

        constructorBtn.addActionListener(e -> {
            popup.show(constructorBtn, 0, constructorBtn.getHeight());
        });


        //int width = title.getPreferredSize().width + ingoingConnectionComponent.getPreferredSize().width;
        //int height = title.getPreferredSize().height;

        // TODO: maybe do this somehwere else?
        setBorder(new EmptyBorder(DesignPalette.HEADER_PADDING_TOP,0,DesignPalette.HEADER_PADDING_BOTTOM,0)); // just space between this and input area and top of LNC

        setSize(getPreferredSize());

        setOpaque(false);

        revalidate();
        repaint();
        setVisible(true);
    }

    public void updatePosition(){
        if(ingoingConnectionComponent != null)
        ingoingConnectionComponent.updatePosition();
    }

    public LIngoingConnectionComponent getIngoingConnectionComponent() {
        return ingoingConnectionComponent;
    }

    public LudemeNodeComponent getLudemeNodeComponent() {
        return LNC;
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        title.setText(getNodeTitle());
        title.setFont(DesignPalette.LUDEME_TITLE_FONT);
        title.setForeground(DesignPalette.FONT_LUDEME_TITLE_COLOR);
        title.setSize(title.getPreferredSize());

        setBorder(DesignPalette.HEADER_PADDING_BORDER);
        //setSize(getPreferredSize());

        //ingoingConnectionComponent = new LIngoingConnectionComponent(this, title.getHeight(), ((int)(title.getHeight()*0.4)), false);
/*
        remove(connectionAndTitle);

        connectionAndTitle = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionAndTitle.add(ingoingConnectionComponent);
        connectionAndTitle.add(Box.createHorizontalStrut(5));
        connectionAndTitle.add(title);
        connectionAndTitle.setOpaque(true);

        add(connectionAndTitle, BorderLayout.LINE_START);


*/

    }

    private String getNodeTitle(){
        LudemeNode LN = LNC.node();
        String title = LN.symbol().name();
        if(LN.selectedClause().args() == null) return title;
        // if selected clause starts with constants, add these to the title
        int index = 0;
        while(LN.selectedClause().args().get(index).symbol().ludemeType().equals(Symbol.LudemeType.Constant)){
            title = title + " " + LN.selectedClause().args().get(index).symbol().name();
            index++;
        }
        return title;
    }

}
