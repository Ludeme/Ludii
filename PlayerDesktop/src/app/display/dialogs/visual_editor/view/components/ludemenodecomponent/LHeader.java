package app.display.dialogs.visual_editor.view.components.ludemenodecomponent;

import app.display.dialogs.visual_editor.documentation.DocumentationReader;
import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LIngoingConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LInputField;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;
import main.grammar.Clause;
import main.grammar.Symbol;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Header component of a LudemeNodeComponent
 * Contains a connection component for the incoming connection and a label for the symbol
 * The users can select the clause to be used for this node here
 * @author Filipp Dokienko
 */
public class LHeader extends JComponent
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 6153442303344307027L;
	/** LudemeNodeComponent this header belongs to */
    private final LudemeNodeComponent LNC;
    /** Ludeme Node */
    private final LudemeNode LN;
    /** Ingoing connection component */
    private LIngoingConnectionComponent ingoingConnectionComponent;
    /** Label for the title */
    private final JLabel title;
    private final JButton clauseBtn;
    private final JPanel constructorPanel;

    /**
     * Constructor for a new LHeader
     * @param ludemeNodeComponent LudemeNodeComponent this header belongs to
     */
    public LHeader(LudemeNodeComponent ludemeNodeComponent)
    {
        LNC = ludemeNodeComponent;
        this.LN = LNC.node();

        setLayout(new BorderLayout());
        // initialize title
        title = new JLabel(LNC.node().title());
        title.setFont(DesignPalette.LUDEME_TITLE_FONT);
        title.setForeground(DesignPalette.FONT_LUDEME_TITLE_COLOR());
        title.setSize(title.getPreferredSize());
        // initialize connection component
        ingoingConnectionComponent = new LIngoingConnectionComponent(this, false);
        // root nodes have no ingoing connection
        if(LNC.graphPanel().graph().getRoot() == LNC.node()) ingoingConnectionComponent = null;
        // Panel containing the label and the connection component
        JPanel connectionAndTitle = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if(ingoingConnectionComponent!=null) connectionAndTitle.add(ingoingConnectionComponent);
        // Empty space between the connection component and the label
        connectionAndTitle.add(Box.createHorizontalStrut(DesignPalette.HEADER_TITLE_CONNECTION_SPACE));
        connectionAndTitle.add(title);
        connectionAndTitle.setOpaque(false);

        // button for selecting the clause
        int iconHeight = (int)(title.getPreferredSize().getHeight());
        ImageIcon icon = new ImageIcon(DesignPalette.DOWN_ICON().getImage().getScaledInstance(iconHeight, iconHeight, Image.SCALE_SMOOTH));

        clauseBtn = new JButton(icon);
        clauseBtn.setFocusPainted(false);
        clauseBtn.setOpaque(false);
        clauseBtn.setContentAreaFilled(false);
        clauseBtn.setBorderPainted(false);
        clauseBtn.setPreferredSize(new Dimension(title.getHeight(), title.getHeight()));
        clauseBtn.setSize(new Dimension(title.getHeight(), title.getHeight()));

        // menu showing available clauses
        constructorPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        constructorPanel.add(clauseBtn);
        constructorPanel.add(Box.createHorizontalStrut(0));
        constructorPanel.setOpaque(false);

        add(connectionAndTitle, BorderLayout.LINE_START);

        if(LN.clauses() != null && LN.clauses().size() > 1) {
            JPopupMenu popup = constructClausePopup();

            clauseBtn.addActionListener(e -> popup.show(clauseBtn, 0, clauseBtn.getHeight()));

            add(constructorPanel, BorderLayout.LINE_END);

        }


        // space between this and input area and top of LNC
        setBorder(new EmptyBorder(DesignPalette.HEADER_PADDING_TOP,0, DesignPalette.HEADER_PADDING_BOTTOM,0));
        setSize(getPreferredSize());

        setOpaque(false);

        revalidate();
        repaint();
        setVisible(true);

        title.setToolTipText(ludemeNodeComponent().node().description());
    }

    public JPopupMenu constructClausePopup()
    {
        JPopupMenu popup = new JPopupMenu();

        // if a symbol encompasses multiple clauses, create a menu for this symbol
        // otherwise just show the clause
        // when clicking on a clause, the current selected clause is repainted
        LinkedHashMap<Symbol, List<Clause>> symbolClauseMap = LN.symbolClauseMap();

        for(Symbol s : symbolClauseMap.keySet())
        {
            if(symbolClauseMap.keySet().size() == 1)
            {
                List<Clause> clauses = symbolClauseMap.get(s);
                for(Clause c : clauses)
                {
                    JMenuItem item = new JMenuItem(clauseTitle(c));
                    item.addActionListener(e -> {
                        Handler.updateCurrentClause(ludemeNodeComponent().graphPanel().graph(), ludemeNodeComponent().node(), c);
                        repaint();
                    });
                    popup.add(item);
                }
                break;
            }

            List<Clause> clauses = symbolClauseMap.get(s);
            if(clauses.size() == 1)
            {
                JMenuItem item = new JMenuItem(clauses.get(0).symbol().name());
                item.setToolTipText(DocumentationReader.instance().documentation().get(s).description());
                item.addActionListener(e -> {
                    Handler.updateCurrentClause(ludemeNodeComponent().graphPanel().graph(), ludemeNodeComponent().node(), clauses.get(0));
                    repaint();
                });
                popup.add(item);
            }
            else
            {
                JMenu menu = new JMenu(s.name());
                menu.setToolTipText(DocumentationReader.instance().documentation().get(s).description());
                JMenuItem[] subitems = new JMenuItem[clauses.size()];
                for(int j = 0; j < clauses.size(); j++)
                {
                    subitems[j] = new JMenuItem(clauseTitle(clauses.get(j)));
                    int finalJ = j;
                    subitems[j].addActionListener(e -> {
                        Handler.updateCurrentClause(ludemeNodeComponent().graphPanel().graph(), ludemeNodeComponent().node(), clauses.get(finalJ));
                        repaint();
                    });
                    menu.add(subitems[j]);
                }
                popup.add(menu);
            }
        }
        return popup;
    }

    private static String clauseTitle(Clause c)
    {
        /*
        if(c.args() == null || c.args().size() == 0)
            return c.toString();
        if(c.args().get(0).symbol().ludemeType().equals(Symbol.LudemeType.Constant))
        {
            String s = "("+c.symbol().token();
            for(ClauseArg ca : c.args())
            {
                if(ca.symbol().ludemeType().equals(Symbol.LudemeType.Constant))
                    s += " "+ca.symbol().token();
                else
                    return s+=")";
            }
        } */
        return c.toString();
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

    public LInputField inputField()
    {
        return ingoingConnectionComponent().inputField();
    }

    /**
     * Paints the header
     * @param g the <code>Graphics</code> object to protect
     */
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        title.setText(LNC.node().title());
        if(title.getFont().getSize() != DesignPalette.LUDEME_TITLE_FONT_SIZE)
        {
            title.setFont(DesignPalette.LUDEME_TITLE_FONT);
            int iconHeight = DesignPalette.LUDEME_TITLE_FONT_SIZE;
            ImageIcon icon = new ImageIcon(DesignPalette.DOWN_ICON().getImage().getScaledInstance(iconHeight, iconHeight, Image.SCALE_SMOOTH));
            clauseBtn.setIcon(null);
            clauseBtn.setIcon(icon);
            clauseBtn.setPreferredSize(new Dimension(iconHeight, iconHeight));
            clauseBtn.setSize(new Dimension(iconHeight, iconHeight));
            clauseBtn.repaint();
            clauseBtn.revalidate();
        }
        if(title.getForeground() != DesignPalette.FONT_LUDEME_TITLE_COLOR())
        {
            title.setForeground(DesignPalette.FONT_LUDEME_TITLE_COLOR());
        }



        title.setSize(title.getPreferredSize());

        if(ludemeNodeComponent().node().description() != null)
        {
            FontMetrics fontMetrics = title.getFontMetrics(title.getFont());

            String toolTipHtml = "<html>" + ludemeNodeComponent().node().description().replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "<br>";
            if(ludemeNodeComponent().node().remark() != null && !ludemeNodeComponent().node().remark().equals(ludemeNodeComponent().node().description()))
            {
                int length = fontMetrics.stringWidth(ludemeNodeComponent().node().remark());
                toolTipHtml += "<p width=\"" + (Math.min(length, 350)) + "px\">" + ludemeNodeComponent().node().remark().replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</p>";

            }
            toolTipHtml += "</html>";
            title.setToolTipText(toolTipHtml);
        }

        if(!clauseBtn.getIcon().equals(DesignPalette.DOWN_ICON()))
            clauseBtn.setIcon(DesignPalette.DOWN_ICON());

        setBorder(DesignPalette.HEADER_PADDING_BORDER);
    }
}
