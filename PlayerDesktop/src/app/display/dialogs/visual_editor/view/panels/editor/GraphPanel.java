package app.display.dialogs.visual_editor.view.panels.editor;

import app.display.dialogs.visual_editor.LayoutManagement.GraphRoutines;
import app.display.dialogs.visual_editor.LayoutManagement.LayoutHandler;
import app.display.dialogs.visual_editor.LayoutManagement.Vector2D;
import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.view.components.AddArgumentPanel;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LIngoingConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LInputField;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.editor.selections.FixedGroupSelection;
import app.display.dialogs.visual_editor.view.panels.editor.selections.SelectionBox;
import app.display.dialogs.visual_editor.view.panels.editor.tabPanels.LayoutSettingsPanel;
import grammar.Grammar;
import main.grammar.Symbol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphPanel extends JPanel implements IGraphPanel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -7514241007809349985L;
	// The graph this panel is displaying
    private final DescriptionGraph GRAPH = new DescriptionGraph();
    // The parent JScrollPane
    private JScrollPane parentScrollPane;
    // The node components in this panel
    private final List<LudemeNodeComponent> NODE_COMPONENTS = new ArrayList<>();
    private final Map<Integer, LudemeNodeComponent> NODE_COMPONENTS_BY_ID = new HashMap<>();
    // The last position of the mouse
    private Point mousePosition;
    // The LayoutHandler
    private final LayoutHandler lm;
    // The ConnectionHandler
    private final ConnectionHandler CONNECTION_HANDLER;
    // window to add a new ludeme out of all possible ones
    private final AddArgumentPanel addLudemePanel;
    // window to add a new ludeme as an input
    private final AddArgumentPanel connectArgumentPanel;
    // List of nodes that are not satisfied/complete
    private List<LudemeNodeComponent> uncompilableNodes = new ArrayList<>();

    // Whether this panel is currently busy
    private boolean busy = false;

    // list of symbols that can be created without connection
    public static List<Symbol> symbolsWithoutConnection;

    // flag to check if select button is active
    private boolean SELECTION_MODE = false;
    // flag to check if user performs selection
    private boolean SELECTING = false;
    // flag to check if selection was performed
    private boolean SELECTED = false;
    // list of selected nodes
    private List<LudemeNodeComponent> selectedLnc = new ArrayList<>();

    // latencies for user testing code completion (Filip)
    private final List<Long> latencies = new ArrayList<>();
    private final List<Integer> selectedCompletion = new ArrayList<>();


    public GraphPanel(int width, int height)
    {
        setLayout(null);
        setPreferredSize(new Dimension(width, height));
        Handler.addGraphPanel(graph(), this);
        GraphPanel.symbolsWithoutConnection = symbolsWithoutConnection();
        this.addLudemePanel = new AddArgumentPanel(symbolsWithoutConnection, this, false);
        // window to add a new ludeme as an input
        this.connectArgumentPanel = new AddArgumentPanel(symbolsWithoutConnection, this, true);
        this.lm = new LayoutHandler(graph());
        this.CONNECTION_HANDLER = new ConnectionHandler(this);
    }

    /**
     * Initializes the graph panel.
     */
    public void initialize(JScrollPane parentScrollPane1)
    {
        this.parentScrollPane = parentScrollPane1;
        addMouseListener(clickListener);
        addMouseMotionListener(motionListener);
        addMouseWheelListener(wheelListener);

        addMouseListener(panelDragListener);
        addMouseMotionListener(panelDragListener);

        addKeyListener(CTRL_listener);

        add(addLudemePanel);
        add(connectArgumentPanel);
    }

    /**
     * Whether this panel is of a define graph.
     */
    @Override
    public boolean isDefineGraph() {
        return false;
    }

    private final MouseAdapter panelDragListener = new MouseAdapter()
    {

        private Point origin;

        @Override
        public void mousePressed(MouseEvent e)
        {
            origin = new Point(e.getPoint());
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            if (origin != null && !isSelectionMode())
            {
                JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, GraphPanel.this);
                if (viewPort != null)
                {
                    int deltaX = origin.x - e.getX();
                    int deltaY = origin.y - e.getY();

                    Rectangle view = viewPort.getViewRect();
                    view.x += deltaX;
                    view.y += deltaY;

                    scrollRectToVisible(view);
                }
            }
        }

    };

    /**
     * Notifies the panel that a node was added to the graph.
     *
     * @param node
     * @param connect
     */
    @Override
    public void notifyNodeAdded(LudemeNode node, boolean connect)
    {
        addLudemeNodeComponent(node, connect);
    }

    /**
     * Notifies the panel that a node was removed from the graph.
     *
     * @param lnc
     */
    @Override
    public void notifyNodeRemoved(LudemeNodeComponent lnc)
    {
        NODE_COMPONENTS.remove(lnc);
        NODE_COMPONENTS_BY_ID.remove(lnc.node().id());
        remove(lnc);
        connectionHandler().removeAllConnections(lnc.node(), false);
        repaint();
    }

    /**
     * Notifies the panel that an edge between two nodes was established, outgoing from a given input field index
     *
     * @param from
     * @param to
     * @param inputFieldIndex
     */
    @Override
    public void notifyEdgeAdded(LudemeNodeComponent from, LudemeNodeComponent to, int inputFieldIndex)
    {
        LConnectionComponent source = from.inputArea().currentInputFields.get(inputFieldIndex).connectionComponent();
        LIngoingConnectionComponent target = to.header().ingoingConnectionComponent();
        connectionHandler().addConnection(source, target);
    }

    private static LInputField findInputField(LudemeNodeComponent lnc, NodeArgument inputFieldArgument)
    {
        for(LInputField ii : lnc.inputArea().currentInputFields)
            if(ii.nodeArguments().contains(inputFieldArgument))
                return ii;
        return null;
    }

    /**
     * Notifies the panel that an edge between two nodes was established, outgoing from an input field with a given NodeArgument
     *
     * @param from
     * @param to
     * @param inputFieldArgument
     */
    @Override
    public void notifyEdgeAdded(LudemeNodeComponent from, LudemeNodeComponent to, NodeArgument inputFieldArgument)
    {
        LInputField inputField = findInputField(from, inputFieldArgument);
        assert inputField != null;
        LConnectionComponent source = inputField.connectionComponent();
        LIngoingConnectionComponent target = to.header().ingoingConnectionComponent();
        connectionHandler().addConnection(source, target);
    }

    /**
     * Notifies the panel that a collection edge between two nodes was removed, outgoing from an input field with a given NodeArgument
     *
     * @param from
     * @param to
     * @param inputFieldArgument
     * @param elementIndex
     */
    @Override
    public void notifyEdgeAdded(LudemeNodeComponent from, LudemeNodeComponent to, NodeArgument inputFieldArgument, int elementIndex)
    {
        LInputField inputField = findInputField(from, inputFieldArgument);

        if(elementIndex > 0)
        {
            assert inputField != null;
            int index = inputField.inputArea().inputFieldIndex(inputField) + elementIndex;
            while(index >= from.inputArea().currentInputFields.size())
                from.inputArea().addCollectionItem(inputField);
            inputField = from.inputArea().currentInputFields.get(index);
        }

        assert inputField != null;
        LConnectionComponent source = inputField.connectionComponent();
        LIngoingConnectionComponent target = to.header().ingoingConnectionComponent();
        connectionHandler().addConnection(source, target);
    }

    /**
     * Notifies the panel that an edge between two nodes was removed
     *
     * @param from
     * @param to
     */
    @Override
    public void notifyEdgeRemoved(LudemeNodeComponent from, LudemeNodeComponent to)
    {
        // find inputfield of from node
        LInputField inputField = findInputField(from, to.node().creatorArgument());
        assert inputField != null;
        connectionHandler().removeConnection(from.node(), inputField.connectionComponent());
    }

    /**
     * Notifies the panel that an edge between two nodes was removed , of a collection
     *
     * @param from
     * @param to
     * @param elementIndex
     */
    @Override
    public void notifyEdgeRemoved(LudemeNodeComponent from, LudemeNodeComponent to, int elementIndex)
    {
        // find inputfield of from node
        LInputField inputField = findInputField(from, to.node().creatorArgument());
        inputField = from.inputArea().currentInputFields.get(from.inputArea().inputFieldIndex(inputField) + elementIndex);
        assert inputField != null;
        connectionHandler().removeConnection(from.node(), inputField.connectionComponent());
    }

    /**
     * Notifies the panel that a node was collapsed/expanded
     *
     * @param lnc
     * @param collapsed
     */
    @Override
    public void notifyCollapsed(LudemeNodeComponent lnc, boolean collapsed)
    {
        lnc.header().inputField().notifyCollapsed();
        if(!collapsed)
            lnc.setVisible(true);
        repaint();
    }

    /**
     * Notifies the panel that a node's inputs were updated
     *
     * @param lnc
     */
    @Override
    public void notifyInputsUpdated(LudemeNodeComponent lnc)
    {
        lnc.updateProvidedInputs();
    }

    /**
     * Notifies the panel that a collection element was added to a node
     *
     * @param lnc
     * @param inputFieldArgument
     * @param elementIndex
     */
    @Override
    public void notifyCollectionAdded(LudemeNodeComponent lnc, NodeArgument inputFieldArgument, int elementIndex)
    {
        // find parent inputfield
        LInputField inputField = findInputField(lnc, inputFieldArgument);
        assert inputField != null;
        inputField.notifyCollectionAdded();
    }

    @Override
    public void notifyCollectionRemoved(LudemeNodeComponent lnc, NodeArgument inputFieldArgument, int elementIndex)
    {
        // find removed inputfield
        LInputField inputField = findInputField(lnc, inputFieldArgument);

        inputField = lnc.inputArea().currentInputFields.get(lnc.inputArea().inputFieldIndex(inputField) + elementIndex);
        inputField.notifyCollectionRemoved();
    }

    /**
     * Notifies the panel that the node's selected clause was changed
     *
     * @param lnc
     */
    @Override
    public void notifySelectedClauseChanged(LudemeNodeComponent lnc)
    {
        lnc.inputArea().changedSelectedClause();
    }

    /**
     * Notifies the panel that a node's optional terminal input was activated/deactivated
     *
     * @param lnc
     * @param inputFieldArgument
     * @param activated
     */
    @Override
    public void notifyTerminalActivated(LudemeNodeComponent lnc, NodeArgument inputFieldArgument, boolean activated)
    {
        LInputField inputField = findInputField(lnc, inputFieldArgument);
        assert inputField != null;
        if(!inputField.isMerged())
        {
            if (activated)
                inputField.notifyActivated();
            else
                inputField.notifyDeactivated();
        }
        else
            lnc.addTerminal(inputFieldArgument, inputField);
    }

    /**
     * Notifies the panel about an updated collapsed-status of a list of nodes
     *
     * @param lncs
     */
    @Override
    public void updateCollapsed(List<LudemeNodeComponent> lncs)
    {
        for(LudemeNodeComponent lnc : lncs)
            if(lnc.node().collapsed())
                if(lnc.header().inputField() != null)
                    lnc.header().inputField().notifyCollapsed();
    }

    /**
     * Notifies the panel which nodes are not compilable
     *
     * @param lncs
     */
    public void notifyUncompilable(List<LudemeNodeComponent> lncs)
    {
        unmarkUncompilableNodes();
        uncompilableNodes = lncs;
        for(LudemeNodeComponent lnc : lncs)
            lnc.markUncompilable(true);
    }

    private void unmarkUncompilableNodes()
    {
        for(LudemeNodeComponent lnc : uncompilableNodes)
            lnc.markUncompilable(false);
        uncompilableNodes.clear();
    }

    /**
     * Returns the ScrollPane this panel is in
     */
    @Override
    public JScrollPane parentScrollPane()
    {
        return parentScrollPane;
    }

    @Override
    public JPanel panel()
    {
        return this;
    }

    /**
     * Creates a LudemeNodeComponent for a given node
     *
     * @param node
     * @param connect
     */
    private void addLudemeNodeComponent(LudemeNode node, boolean connect)
    {
        LudemeNodeComponent lc = new LudemeNodeComponent(node, this);
        hideAllAddArgumentPanels();
        NODE_COMPONENTS.add(lc);
        NODE_COMPONENTS_BY_ID.put(node.id(), lc);
        add(lc);
        lc.updatePositions();

        if(connect)
            connectionHandler().finishNewConnection(lc);
    }

    /**
     * Whether the graph is currently busy (e.g. a node is being added)
     */
    @Override
    public boolean isBusy()
    {
        return busy;
    }

    /**
     * Updates the busy-status
     *
     * @param b
     */
    @Override
    public void setBusy(boolean b)
    {
        this.busy = b;
    }

    /**
     * Returns the DescriptionGraph this Panel represents
     */
    @Override
    public DescriptionGraph graph() {
        return GRAPH;
    }

    /**
     * Returns the ConnectionHandler for this panel
     */
    @Override
    public ConnectionHandler connectionHandler() {
        return CONNECTION_HANDLER;
    }

    /**
     * Finds the LudemeNodeComponent for a given LudemeNode
     *
     * @param node
     */
    @Override
    public LudemeNodeComponent nodeComponent(LudemeNode node)
    {
        LudemeNodeComponent lnc = NODE_COMPONENTS_BY_ID.get(node.id());
        if(lnc != null)
            return lnc;

        for(LudemeNodeComponent ln : NODE_COMPONENTS)
            if(ln.node() == node)
                return ln;

        return null;
    }

    /**
     *
     */
    @Override
    public void enterSelectionMode()
    {
        this.SELECTION_MODE = true;
    }

    /**
     *
     */
    @Override
    public boolean isSelectionMode() {
        return SELECTION_MODE;
    }

    /**
     *
     */
    @Override
    public Rectangle exitSelectionMode()
    {
        SELECTING = false;
        SELECTION_MODE = false;
        Handler.turnOffSelectionBtn();
        repaint();
        revalidate();
        return SelectionBox.endSelection();
    }

    /**
     * Adds a node to the list of selected nodes
     *
     * @param lnc
     */
    @Override
    public void addNodeToSelections(LudemeNodeComponent lnc)
    {
        if (!selectedLnc.contains(lnc))
        {
            SELECTED = true;
            lnc.setSelected(true);
            selectedLnc.add(lnc);

            // add collapsed subtrees too
            for(LudemeNode child : lnc.node().childrenNodes())
            {
                LudemeNodeComponent childLnc = nodeComponent(child);
                if(childLnc.node().collapsed())
                    for(LudemeNodeComponent lncc : subtree(childLnc))
                    {
                        if(selectedLnc.contains(lncc))
                            continue;
                        lncc.setSelected(true);
                        selectedLnc.add(lncc);
                    }
            }
        }
    }

    /**
     * Returns the subtree of a given node
     * @param lnc
     * @return
     */
    private List<LudemeNodeComponent> subtree(LudemeNodeComponent lnc)
    {
        List<LudemeNodeComponent> subtree = new ArrayList<>();
        subtree.add(lnc);
        for(LudemeNode child : lnc.node().childrenNodes())
        {
            LudemeNodeComponent childLnc = nodeComponent(child);
            subtree.add(childLnc);
            subtree.addAll(subtree(childLnc));
        }
        return subtree;
    }

    /**
     * List of selected nodes
     */
    @Override
    public List<iGNode> selectedNodes()
    {
        List<iGNode> nodeList = new ArrayList<>();
        for (LudemeNodeComponent lnc: selectedLnc)
            nodeList.add(lnc.node());
        return nodeList;
    }

    /**
     * List of selected nodes
     */
    @Override
    public List<LudemeNodeComponent> selectedLnc()
    {
        return selectedLnc;
    }

    /**
     * Selects all nodes
     */
    @Override
    public void selectAllNodes()
    {
        for (LudemeNodeComponent lnc: NODE_COMPONENTS)
            addNodeToSelections(lnc);
        repaint();
        revalidate();
    }

    public void showCurrentlyAvailableLudemes()
    {
        // get game description up to current point
        int upUntilIndex = connectionHandler().selectedComponent().inputField().nodeArguments().get(0).index();
        for(NodeArgument ii : connectionHandler().selectedComponent().inputField().nodeArguments())
            if(ii.index() < upUntilIndex)
                upUntilIndex = ii.index();
        long start = System.nanoTime();
        List<Symbol> possibleSymbols = connectionHandler().selectedComponent().possibleSymbolInputs();
        //[UNCOMMENT FILIP] String gameDescription = connectionHandler().selectedComponent().inputField().inputArea().LNC().node().toLudCodeCompletion(connectionHandler().selectedComponent().inputField().nodeArguments());
        List<Symbol> typeMatched = possibleSymbols;
        //[UNCOMMENT FILIP] List<Symbol> typeMatched = TypeMatch.getInstance().typematch(gameDescription, StartVisualEditor.controller(),possibleSymbols);

        connectArgumentPanel.updateList(connectionHandler().selectedComponent().inputField(), typeMatched);
        connectArgumentPanel.setVisible(true);
        connectArgumentPanel.setLocation(mousePosition);
        connectArgumentPanel.searchField.requestFocus();

        revalidate();
        repaint();
    }

    /**
     * Displays all available ludemes that may be created
     */
    @Override
    public void showAllAvailableLudemes()
    {
        addLudemePanel.setVisible(true);
        addLudemePanel.setLocation(mousePosition);
        addLudemePanel.searchField.requestFocus();
        revalidate();
        repaint();
    }

    public Point mousePosition()
    {
        return mousePosition;
    }

    /**
     * Notifies the panel that the user clicked on a node
     */
    @Override
    public void clickedOnNode(LudemeNodeComponent lnc)
    {
        LudemeNode node = lnc.node();
        LConnectionComponent selectedConnectionComponent = connectionHandler().selectedComponent();
        if(selectedConnectionComponent != null)
        {
            if (selectedConnectionComponent.inputField().nodeArgument(0).collection2D() && !lnc.ingoingConnectionComponent().isFilled())
            {
                if (lnc.node().creatorArgument().arg().equals(selectedConnectionComponent.inputField().nodeArgument(0).arg()))
                    connectionHandler().finishNewConnection(lnc);
            }
            else if(selectedConnectionComponent.possibleSymbolInputs().contains(node.symbol()) && !lnc.ingoingConnectionComponent().isFilled())
                connectionHandler().finishNewConnection(lnc);
            else if(node.isDefineNode() && selectedConnectionComponent.possibleSymbolInputs().contains(node.macroNode().symbol()))
                connectionHandler().finishNewConnection(lnc);
        }
    }

    /**
     * The LayoutHandler
     */
    @Override
    public LayoutHandler getLayoutHandler()
    {
        return lm;
    }

    /**
     * Updates the Graph (position of nodes, etc.)
     */
    @Override
    public void updateGraph()
    {
        for (LudemeNodeComponent lc : NODE_COMPONENTS)
        {
            lc.revalidate();
            lc.updateProvidedInputs();
            lc.updatePositions();
        }
        revalidate();
        repaint();
    }

    /**
     *
     */
    @Override
    public void syncNodePositions()
    {
        for (LudemeNodeComponent lc : NODE_COMPONENTS)
            lc.syncPositionsWithLN();
        revalidate();
        repaint();
    }

    /**
     * Unselects all nodes
     */
    @Override
    public void deselectEverything()
    {
        graph().getNodes().forEach(n -> {
            LudemeNodeComponent lnc = nodeComponent(n);
            lnc.setSelected(false);
            lnc.setDoubleSelected(false);
        });
        graph().setSelectedRoot(-1);
        LayoutSettingsPanel.getLayoutSettingsPanel().disableFixButton();
        LayoutSettingsPanel.getLayoutSettingsPanel().disableUnfixButton();
        selectedLnc = new ArrayList<>();
        SELECTED = false;
        repaint();
        revalidate();
    }

    /**
     * @return A list of symbols which can be created without an ingoing connection
     */
    private static List<Symbol> symbolsWithoutConnection()
    {
        List<Symbol> allSymbols = Grammar.grammar().symbols();
        List<Symbol> symbolsWithoutConnection1 = new ArrayList<>();
        for (Symbol symbol : allSymbols)
        {
            if(symbol.ludemeType().equals(Symbol.LudemeType.Constant) ||
                    symbol.ludemeType().equals(Symbol.LudemeType.Predefined) ||
                    symbol.ludemeType().equals(Symbol.LudemeType.Structural) ||
                    symbol.ludemeType().equals(Symbol.LudemeType.Primitive) ||
                    symbol.ludemeType().equals(Symbol.LudemeType.SubLudeme))
                continue;
            symbolsWithoutConnection1.add(symbol);
        }
        return symbolsWithoutConnection1;
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if(getBackground() != DesignPalette.BACKGROUND_EDITOR())
            setBackground(DesignPalette.BACKGROUND_EDITOR());

        // draw background
        if(parentScrollPane!=null) Handler.currentBackground().paint(parentScrollPane().getViewport().getViewRect(), getWidth(), getHeight(), g2);

        // set color for edges
        g2.setColor(DesignPalette.LUDEME_CONNECTION_EDGE());
        // set stroke for edges
        g2.setStroke(DesignPalette.LUDEME_EDGE_STROKE);
        // draw new connection
        connectionHandler().drawNewConnection(g2, mousePosition);
        // draw existing connections
        connectionHandler().paintConnections(g2);

        // Draw selection area
        if (SELECTION_MODE && !SELECTING)
            SelectionBox.drawSelectionModeIdle(mousePosition, g2);
        if (SELECTION_MODE && SELECTING)
            SelectionBox.drawSelectionArea(mousePosition, mousePosition, g2);

        // Draw fixed groups area
        for(LudemeNodeComponent lc : NODE_COMPONENTS)
            if (lc.node().fixed())
            {
                Rectangle subtreeArea = GraphRoutines.getSubtreeArea(graph(), lc.node().id());
                FixedGroupSelection.drawGroupBox(subtreeArea, (Graphics2D) g);
            }
    }


    public void hideAllAddArgumentPanels()
    {
        connectArgumentPanel.setVisible(false);
        addLudemePanel.setVisible(false);
    }


    // LISTENERS

    final MouseListener clickListener = new MouseAdapter()
    {
        private void openPopupMenu(MouseEvent e)
        {
            JPopupMenu popupMenu = new EditorPopupMenu(GraphPanel.this, e.getX(), e.getY());
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
            super.mouseClicked(e);

            if(connectArgumentPanel.isVisible())
                connectionHandler().cancelNewConnection();
            hideAllAddArgumentPanels();
            if(e.getButton() == MouseEvent.BUTTON1)
            {
                // user is drawing a new connection
                if(connectionHandler().selectedComponent() != null)
                {
                    // if its a 2D collection, connect to a 1D collection equivalent
                    if(connectionHandler().selectedComponent().inputField().nodeArgument(0).collection2D())
                        Handler.addNode(graph(), connectionHandler().selectedComponent().inputField().nodeArgument(0), e.getX(), e.getY());
                        // if user has no chocie for next ludeme -> automatically add required ludeme
                    else if(connectionHandler().selectedComponent().possibleSymbolInputs().size() == 1)
                        Handler.addNode(graph(), connectionHandler().selectedComponent().possibleSymbolInputs().get(0), connectionHandler().selectedComponent().inputField().nodeArgument(0), e.getX(), e.getY(), true);
                    else if(!connectArgumentPanel.isVisible() && connectionHandler().selectedComponent().possibleSymbolInputs().size() > 1)
                        showCurrentlyAvailableLudemes();
                }

                // When selection was performed user can clear it out by clicking on blank area
                if (SELECTED)
                {
                    LayoutSettingsPanel.getLayoutSettingsPanel().setSelectedComponent("Empty", false);
                    deselectEverything();
                }
            }
            else
            {
                // user is selecting a connection -> cancel new connection
                if(connectionHandler().selectedComponent() != null)
                    connectionHandler().cancelNewConnection();
            }

            repaint();
            revalidate();
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            super.mouseDragged(e);
        }

        public void mousePressed(MouseEvent e)
        {
            if(e.getButton() == MouseEvent.BUTTON3)
            {
                connectionHandler().cancelNewConnection();
                openPopupMenu(e);
            }

            if (SELECTION_MODE && e.getButton() == MouseEvent.BUTTON1)
            {
                SELECTING = true;
                repaint();
                revalidate();
            }
        }

        /**
         * - Open pop up menu right click on empty space
         * - Select nodes that fall within selection area
         * @param e mouse event
         */
        public void mouseReleased(MouseEvent e)
        {
            if(e.getButton() == MouseEvent.BUTTON3)
                openPopupMenu(e);

            if (SELECTING && e.getButton() == MouseEvent.BUTTON1)
            {
                Rectangle region = exitSelectionMode();
                if (region != null)
                {
                    graph().getNodes().forEach(n -> {
                        LudemeNodeComponent lnc = nodeComponent(n);
                        if (region.intersects(lnc.getBounds()))
                        {
                            addNodeToSelections(lnc);
                            SELECTED = true;
                        }
                    });
                }
            }
        }

    };

    final MouseMotionListener motionListener = new MouseAdapter()
    {
        @Override
        public void mouseMoved(MouseEvent e)
        {
            super.mouseMoved(e);
            mousePosition = e.getPoint();
            if (SELECTION_MODE || connectionHandler().selectedComponent() != null)
                repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            super.mouseDragged(e);
            mousePosition = e.getPoint();
            if (SELECTING)
                repaint();
        }
    };

    // key listener check if ctrl is pressed/released
    final KeyAdapter CTRL_listener = new KeyAdapter()
    {
        @Override
        public void keyTyped(KeyEvent e) {
            super.keyTyped(e);
            if (e.getKeyCode() == 17)
                LudemeNodeComponent.cltrPressed = true;
        }

        @Override
        public void keyPressed(KeyEvent e)
        {
            super.keyPressed(e);
            if (e.getKeyCode() == 17)
                LudemeNodeComponent.cltrPressed = true;
        }

        @Override
        public void keyReleased(KeyEvent e)
        {
            super.keyReleased(e);
            if (e.getKeyCode() == 17)
                LudemeNodeComponent.cltrPressed = false;
        }
    };



    // # Mouse wheel listeners #

    /*
    Notes on proper scaling:
    - node is a box with 4 coords: (x1,y1);(x2,y2);(x3,y3);(x4,y4);
    - s is a scaling factor
    1. translate (x1,y1): (x1-w/2, -1*(y1-h/2)) = (x', y')
    2. scale (x',y') by s: (x'*s, y'*s) = (xs, ys)
    3. scale h, w by s: hs = h*s, ws = w*s
    4. translate (xs, ys) back: (xs+w/2, -1*ys+h/2)
     */

    final MouseWheelListener wheelListener = new MouseAdapter()
    {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            if(e.getWheelRotation() > 0)
            {
                DesignPalette.scale(1.02f);
                if (DesignPalette.SCALAR < 1.9f) scaleNodesPositions(0.98);
            }
            else
            {
                DesignPalette.scale(1.0f/1.02f);
                if (DesignPalette.SCALAR > 0.86f) scaleNodesPositions(1.0/0.98);
            }
            syncNodePositions();
            repaint();
        }
    };

    /**
     * Iterate through nodes on a panel and scale their positions for zooming in/out
     */
    private void scaleNodesPositions(double scalar)
    {
        graph().getNodes().forEach(ludemeNode -> {
            Vector2D scaledPos = getScaledCoords(ludemeNode.pos(), scalar, Handler.getViewPortSize().width, Handler.getViewPortSize().height);
            ludemeNode.setPos(scaledPos);
        });
    }

    /**
     * Scale coordinates of single node.
     * Usage of integer coordinates by java swing graphical components introduces rounding errors that may degrade scaling.
     * @param pos position to be scaled
     * @param scalar scalar
     * @param W screen width
     * @param H screen height
     * @return scaled position
     */
    private Vector2D getScaledCoords(Vector2D pos, double scalar, double W, double H)
    {
        // account for changed viewport
        int viewportX = parentScrollPane.getViewport().getViewRect().x;
        int viewportY = parentScrollPane.getViewport().getViewRect().y;
        // translate into cartesian coordinates
        double xp = pos.x()-viewportX-W/2;
        double yp = (pos.y()-viewportY-H/2)*-1;
        // scale
        xp*=scalar;
        yp*=scalar;
        // return translated back to java swing scaled coordinates
        return new Vector2D(xp+viewportX+W/2, -1*yp+H/2+viewportY);
    }
    
    public List<Integer> selectedCompletion()
    {
        return selectedCompletion;
    }
}
