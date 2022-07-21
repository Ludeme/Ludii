package app.display.dialogs.visual_editor.view.components.ludemenodecomponent;


import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LIngoingConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LInputArea;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LInputField;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.editor.tabPanels.LayoutSettingsPanel;
import main.grammar.Symbol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Graphical Element for a LudemeNode
 * Consists of
 *      - LHeader: Top component of the node displaying the title, the ingoing connection and a button to select a clause
 *      - LInputArea: Center component of the node displaying arguments of the Ludeme.
 *          - Each Argument is represented by a LInputField
 * @author Filipp Dokienko
 */

public class LudemeNodeComponent extends JPanel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -7213640452305410215L;
	/** X, Y coordinates of the node */
    protected int x, y;
    /** Position of the node */
    private final ImmutablePoint position = new ImmutablePoint(x, y);
    /** The Ludeme Node LN this component represents */
    private final LudemeNode LN;
    /** Graph Panel this node is in */
    private final IGraphPanel GRAPH_PANEL;
    /** Whether this node is marked as uncompilable */
    private boolean markedUncompilable = false;
    // Whether the node is "marked"/selected
    private boolean selected = false;
    private boolean doubleSelected = false;
    private boolean subtree = false;
    /** Sub-Components of the node */
    private final LHeader header;
    private final LInputArea inputArea;
    /** Check if ctrl is pressed */
    public static boolean cltrPressed = false;

    /**
     * Constructor for the LudemeNodeComponent
     * @param ludemeNode The LudemeNode this component represents
     * @param graphPanel The GraphPanel this node is in
     */
    public LudemeNodeComponent(LudemeNode ludemeNode, IGraphPanel graphPanel)
    {
        this.LN = ludemeNode;
        this.GRAPH_PANEL = graphPanel;

        this.x = (int) ludemeNode.pos().x();
        this.y = (int) ludemeNode.pos().y();

        // initialize components
        setLayout(new BorderLayout());

        header = new LHeader(this);
        inputArea = new LInputArea(this);

        add(header, BorderLayout.NORTH);
        add(inputArea, BorderLayout.CENTER);
        setLocation(x,y);

        int preferredHeight = preferredHeight();
        setPreferredSize(new Dimension(width(), preferredHeight));
        setSize(getPreferredSize());
        ludemeNode.setWidth(width());
        ludemeNode.setHeight(getHeight());

        addMouseMotionListener(dragListener);
        addMouseListener(mouseListener);

        updatePositions();

        revalidate();
        repaint();
        setVisible(visible());

    }

    /**
     * Adds a terminal inputfield to the node
     * @param symbol the symbol of the terminal inputfield
     */
    public void addTerminal(Symbol symbol)
    {
        inputArea.addedConnection(symbol, graphPanel().connectionHandler().selectedComponent().inputField());
        revalidate();
        repaint();
    }

    /**
     * Adds a terminal inputfield to the node
     * @param nodeArgument the nodeargument of the terminal inputfield
     * @param inputField the inputfield containing the nodeArgument
     */
    public void addTerminal(NodeArgument nodeArgument, LInputField inputField)
    {
        System.out.println(nodeArgument);
        inputArea.addedConnection(nodeArgument, inputField);
        revalidate();
        repaint();
    }

    /**
     * Updates the positions of the components of the node
     */
    public void updatePositions()
    {
        if(inputArea == null || header == null) {return;}
        position.update(getLocation());
        inputArea.updateConnectionPointPositions();
        header.updatePosition();
        LN.setPos(position.x, position.y);
    }

    public void syncPositionsWithLN()
    {
        if(inputArea == null || header == null) {return;}
        setLocation(new Point((int)LN.pos().x(), (int)LN.pos().y()));
        position.update(getLocation());
        inputArea.updateConnectionPointPositions();
        header.updatePosition();
    }

    /**
     * Method which syncs the Ludeme Node Component with provided inputs (stored in the Ludeme Node).
     * Called when drawing a graph.
     */
    public void updateProvidedInputs()
    {
        inputArea.updateProvidedInputs();
    }

    public boolean isPartOfDefine()
    {
        return graphPanel().graph().isDefine();
    }

    /**
     * Updates the component's dimensions
     */
    public void updateComponentDimension()
    {
        if(inputArea == null)
        {
            return;
        }

        int preferredHeight = preferredHeight();

        setPreferredSize(new Dimension(getPreferredSize().width, preferredHeight));
        setSize(getPreferredSize());

        repaint();
        revalidate();
        setVisible(visible());
    }

    /**
     *
     * @return the node this component represents
     */
    public LudemeNode node()
    {
        return LN;
    }

    /**
     *
     * @return the InputArea of the node component
     */
    public LInputArea inputArea()
    {
        return inputArea;
    }

    /**
     *
     * @return the header of the node component
     */
    public LHeader header()
    {
        return header;
    }

    /**
     *
     * @return the width of a node component according to the Design Palette
     */
    public int width()
    {
        return DesignPalette.NODE_WIDTH;
    }

    /**
     *
     * @return the graph panel this node component is in
     */
    public IGraphPanel graphPanel()
    {
        return GRAPH_PANEL;
    }

    /**
     * Sets the node to be selected/unselected
     * @param selected Whether the node is selected
     */
    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    /**
     * Sets the node to be double selected/unselected
     * @param selected Whether the node is selected
     */
    public void setDoubleSelected(boolean selected)
    {
        this.doubleSelected = selected;
    }

    /**
     *
     * @return whether the node is selected
     */
    public boolean selected()
    {
        return selected;
    }

    /**
     * Returned height depends on the height of the header and the input area
     * @return the preferred height of the node component
     */

    private int preferredHeight()
    {
        return inputArea.getPreferredSize().height + header.getPreferredSize().height;
    }

    private List<LudemeNodeComponent> collapsedSubtreeNodes()
    {
        List<LudemeNodeComponent> nodes = new ArrayList<>();
        for(LudemeNode node : node().childrenNodes())
        {
            if(node.collapsed())
            {
                nodes.addAll(subtree(graphPanel().nodeComponent(node)));
            }
        }
        return nodes;
    }

    private List<LudemeNodeComponent> subtree(LudemeNodeComponent root)
    {
        List<LudemeNodeComponent> nodes = new ArrayList<>();
        nodes.add(root);
        for(LudemeNode node : root.node().childrenNodes())
        {
            nodes.addAll(subtree(graphPanel().nodeComponent(node)));
        }
        return nodes;
    }

    /**
     *
     * @return the Ingoing Connection Component of the node component, situated on the top of the node (LHeader)
     */
    public LIngoingConnectionComponent ingoingConnectionComponent()
    {
        return header.ingoingConnectionComponent();
    }

    /**
     *
     * @return whether this node is visible
     */
    public boolean visible()
    {
        return node().visible();
    }

    public void markUncompilable(boolean uncompilable)
    {
        markedUncompilable = uncompilable;
        repaint();
    }

    public boolean isMarkedUncompilable()
    {
        return markedUncompilable;
    }

    /**
     * Drag Listener for the node component
     *  - When the node is dragged, the node is moved
     *  - When the node is selected and dragged, then all the selected nodes are moved according to the drag of this node
     */
    final MouseMotionListener dragListener = new MouseAdapter()
    {

        @Override
        public void mouseDragged(MouseEvent e)
        {
            super.mouseDragged(e);

            int initX = position.x;
            int initY = position.y;

            e.translatePoint(e.getComponent().getLocation().x - LudemeNodeComponent.this.x, e.getComponent().getLocation().y - LudemeNodeComponent.this.y);
            LudemeNodeComponent.this.setLocation(e.getX(), e.getY());
            updatePositions();
            Point posDif = new Point(position.x-initX, position.y-initY);
            // if selection was performed move all others selected nodes with respect to the dragged one
            if (selected)
            {
                List<LudemeNodeComponent> Q = graphPanel().selectedLnc();
                Q.forEach(lnc -> {
                    if (!lnc.equals(LudemeNodeComponent.this)) lnc.setLocation(lnc.getLocation().x+posDif.x, lnc.getLocation().y+posDif.y);
                    lnc.updatePositions();
                });
            }
            else {
                List<LudemeNodeComponent> collapsedChildren = collapsedSubtreeNodes();
                if (collapsedChildren.size() >= 1) {
                    collapsedChildren.forEach(lnc -> {
                        if (!lnc.selected() && !lnc.equals(LudemeNodeComponent.this))
                            lnc.setLocation(lnc.getLocation().x + posDif.x, lnc.getLocation().y + posDif.y);
                        lnc.updatePositions();
                    });
                }
            }

            updatePositions();
            graphPanel().repaint();
        }
    };

    /**
     * Mouse Listener for the node component
     * - When the node is right-clicked, open a popup menu with options
     * - When the node is double-clicked, select it and it's subtrees
     * - When currently connecting and this node is left-clicked, try to connect to this node
     */
    final MouseListener mouseListener = new MouseAdapter()
    {
        private void openPopupMenu(MouseEvent e){
            JPopupMenu popupMenu = new NodePopupMenu(LudemeNodeComponent.this, LudemeNodeComponent.this.graphPanel());
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
            super.mouseClicked(e);
            // when double click is performed on a node add its descendant into selection list
            handleNodeComponentSelection(e);
        }

        // When pressed, update position
        @Override
        public void mousePressed(MouseEvent e)
        {
            super.mousePressed(e);
            LudemeNodeComponent.this.x = e.getX();
            LudemeNodeComponent.this.y = e.getY();
            Handler.updatePosition(node(), getX(), getY());
            handleNodeComponentSelection(e);
        }
        // When released, update position
        // If right click, open popup menu
        // If left click, notify graph panel that the node was clicked
        @Override
        public void mouseReleased(MouseEvent e)
        {
            super.mouseReleased(e);
            LudemeNodeComponent.this.x = e.getX();
            LudemeNodeComponent.this.y = e.getY();
            Handler.updatePosition(node(), getX(), getY());

            if(e.getButton() == MouseEvent.BUTTON3){
                // if(!selected) graphPanel().deselectEverything();
                graphPanel().addNodeToSelections(LudemeNodeComponent.this);
                openPopupMenu(e);
                graphPanel().connectionHandler().cancelNewConnection();
            }
            else {
                graphPanel().clickedOnNode(LudemeNodeComponent.this);
            }
        }
    };

	private void handleNodeComponentSelection(MouseEvent e)
    {
        if (e.getClickCount() == 1 && !selected)
        {
            if (!cltrPressed) graphPanel().deselectEverything();
            Handler.selectNode(LudemeNodeComponent.this);
            subtree = false;
        }
        else if (e.getClickCount() >= 2 && !doubleSelected)
        {
            doubleSelected = true;
            graphPanel().graph().setSelectedRoot(this.LN.id());

            if (this.LN.fixed()) LayoutSettingsPanel.getLayoutSettingsPanel().enableUnfixButton();
            else LayoutSettingsPanel.getLayoutSettingsPanel().enableFixButton();

            List<LudemeNodeComponent> Q = new ArrayList<>();
            Q.add(LudemeNodeComponent.this);
            while (!Q.isEmpty())
            {
                LudemeNodeComponent lnc = Q.remove(0);
                Handler.selectNode(lnc);
                List<Integer> children = lnc.LN.children();
                children.forEach(v -> Q.add(GRAPH_PANEL.nodeComponent(GRAPH_PANEL.graph().getNode(v))));
            }
            subtree = !LudemeNodeComponent.this.LN.children().isEmpty();
            graphPanel().repaint();
            graphPanel().repaint();
        }
        LayoutSettingsPanel.getLayoutSettingsPanel().setSelectedComponent(LudemeNodeComponent.this.header.title().getText(), subtree);
    }

    /**
     * Paints the node component
     * @param g the <code>Graphics</code> object to protect
     */
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        // if was invisible before but now visible, redraw children too
        if(isVisible() != visible())
            setVisible(visible());
        int preferredHeight = preferredHeight();

        setMinimumSize(new Dimension(width(), preferredHeight));
        setPreferredSize(new Dimension(getMinimumSize().width, preferredHeight));
        setSize(getPreferredSize());

        LN.setWidth(getWidth());
        LN.setHeight(getPreferredSize().height);

        setBackground(backgroundColour());
        if (selected)
            setBorder(DesignPalette.LUDEME_NODE_BORDER_SELECTED());
        else if (markedUncompilable)
            setBorder(DesignPalette.LUDEME_NODE_BORDER_UNCOMPILABLE());
        else
            setBorder(DesignPalette.LUDEME_NODE_BORDER());

    }

    @Override
    public void setVisible(boolean visible)
    {
        super.setVisible(visible);
        // make children visible too
        if(!visible) return;
        for(LudemeNode ln : node().childrenNodes())
        {
            LudemeNodeComponent lnc = graphPanel().nodeComponent(ln);
            if(lnc!=null)
                lnc.setVisible(ln.visible());
        }
    }

    /**
     *
     * @return The background colour according to the nodes package
     */
    private Color backgroundColour()
    {
        switch(node().packageName())
        {
            case "game.equipment": return DesignPalette.BACKGROUND_LUDEME_BODY_EQUIPMENT();
            case "game.functions": return DesignPalette.BACKGROUND_LUDEME_BODY_FUNCTIONS();
            case "game.rules": return DesignPalette.BACKGROUND_LUDEME_BODY_RULES();
            case "define": return DesignPalette.BACKGROUND_LUDEME_BODY_DEFINE();
            default: return DesignPalette.BACKGROUND_LUDEME_BODY();
        }
    }

    /**
     * Updates the input area's input fields
     * @param parameters
     */
    public void changedArguments(List<NodeArgument> parameters)
    {
        inputArea().updateCurrentInputFields(parameters);
    }


    // FOR DYNAMIC CONSTRUCTOR
    /*
     // Switches the node to be a dynamic/un-dynamic node
     // Dynamic nodes have no pre-selected clause
    public void changeDynamic()
    {
        if(!LN.dynamicPossible()) return;
        LN.setDynamic(!LN.dynamic());
        node().setDynamic(LN.dynamic());
    }


    // @return whether the node is dynamic
    public boolean dynamic()
    {
        return LN.dynamic();
    }
     */



}
