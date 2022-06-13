package app.display.dialogs.visual_editor.view.components.ludemenodecomponent;

import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LIngoingConnectionComponent;
import main.grammar.Clause;
import main.grammar.Symbol;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Header component of a LudemeNodeComponent
 * Contains a connection component for the incoming connection and a label for the symbol
 * The users can select the clause to be used for this node here
 * @author Filipp Dokienko
 */
public class LHeader extends JComponent
{
    /** LudemeNodeComponent this header belongs to */
    private final LudemeNodeComponent LNC;
    /** Ingoing connection component */
    private LIngoingConnectionComponent ingoingConnectionComponent;
    /** Label for the title */
    private final JLabel title;

    /**
     * Constructor for a new LHeader
     * @param ludemeNodeComponent LudemeNodeComponent this header belongs to
     */
    public LHeader(LudemeNodeComponent ludemeNodeComponent)
    {
        LNC = ludemeNodeComponent;
        LudemeNode LN = LNC.node();

        setLayout(new BorderLayout());
        // initialize title
        title = new JLabel(nodeTitle());
        title.setFont(DesignPalette.LUDEME_TITLE_FONT);
        title.setForeground(DesignPalette.FONT_LUDEME_TITLE_COLOR);
        title.setSize(title.getPreferredSize());
        // initialize connection component
        ingoingConnectionComponent = new LIngoingConnectionComponent(this, title.getHeight(), ((int)(title.getHeight()*0.4)), false);
        // root nodes have no ingoing connection
        if(LNC.graphPanel().getGraph().getRoot() == LNC.node()) ingoingConnectionComponent = null;
        // Panel containing the label and the connection component
        JPanel connectionAndTitle = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if(ingoingConnectionComponent!=null) connectionAndTitle.add(ingoingConnectionComponent);
        // Empty space between the connection component and the label
        connectionAndTitle.add(Box.createHorizontalStrut(DesignPalette.HEADER_TITLE_CONNECTION_SPACE));
        connectionAndTitle.add(title);
        connectionAndTitle.setOpaque(false);

        // button for selecting the clause
        JButton clauseBtn = new JButton(DesignPalette.DOWN_ICON);
        clauseBtn.setFocusPainted(false);
        clauseBtn.setOpaque(false);
        clauseBtn.setContentAreaFilled(false);
        clauseBtn.setBorderPainted(false);
        clauseBtn.setPreferredSize(new Dimension(title.getHeight(), title.getHeight()));
        clauseBtn.setSize(new Dimension(title.getHeight(), title.getHeight()));

        // menu showing available clauses
        JPanel constructorPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        constructorPanel.add(clauseBtn);
        constructorPanel.add(Box.createHorizontalStrut(0));
        constructorPanel.setOpaque(false);
        JPopupMenu popup = new JPopupMenu();
        JMenuItem[] items = new JMenuItem[LN.clauses().size()];

        if(LN.clauses() != null) {
            for (int i = 0; i < LN.clauses().size(); i++) {
                Clause clause = LN.clauses().get(i);
                items[i] = new JMenuItem(clause.toString());
                items[i].addActionListener(e1 -> {
                    System.out.println("Selected clause: " + clause.toString());
                    ludemeNodeComponent.changeCurrentClause(clause);
                    repaint();
                });
                popup.add(items[i]);
            }

            clauseBtn.addActionListener(e -> {
                popup.show(clauseBtn, 0, clauseBtn.getHeight());
            });
        }

        add(connectionAndTitle, BorderLayout.LINE_START);
        add(constructorPanel, BorderLayout.LINE_END);

        // space between this and input area and top of LNC
        setBorder(new EmptyBorder(DesignPalette.HEADER_PADDING_TOP,0,DesignPalette.HEADER_PADDING_BOTTOM,0));
        setSize(getPreferredSize());

        setOpaque(false);

        revalidate();
        repaint();
        setVisible(true);
    }

    /**
     * Updates the position of the ingoing connection component
     * Called whenever the node is moved
     */
    public void updatePosition()
    {
        if(ingoingConnectionComponent != null) ingoingConnectionComponent.updatePosition();
    }

    /**
     *
     * @return The ingoing connection component
     */
    public LIngoingConnectionComponent ingoingConnectionComponent()
    {
        return ingoingConnectionComponent;
    }

    /**
     *
     * @return The LudemeNodeComponent this header belongs to
     */
    public LudemeNodeComponent ludemeNodeComponent()
    {
        return LNC;
    }

    /**
     *
     * @return the title label of the node
     */
    public JLabel title()
    {
        return title;
    }

    /**
     * The title consists of the symbol and any Constants followed by the constructor
     * @return The title of this node
     */
    private String nodeTitle()
    {
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

    /**
     * Paints the header
     * @param g the <code>Graphics</code> object to protect
     */
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        title.setText(nodeTitle());
        title.setFont(DesignPalette.LUDEME_TITLE_FONT);
        title.setForeground(DesignPalette.FONT_LUDEME_TITLE_COLOR);
        title.setSize(title.getPreferredSize());

        setBorder(DesignPalette.HEADER_PADDING_BORDER);
    }
}
