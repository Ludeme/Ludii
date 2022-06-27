package app.display.dialogs.visual_editor.view.panels.editor;


import app.display.dialogs.visual_editor.LayoutManagement.LayoutHandler;
import app.display.dialogs.visual_editor.LayoutManagement.Vector2D;
import app.display.dialogs.visual_editor.Main;
import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.recs.codecompletion.controller.NGramController;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.TypeMatch;
import app.display.dialogs.visual_editor.view.components.AddLudemeWindow;
import app.display.dialogs.visual_editor.view.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeConnection;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LIngoingConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LInputField;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.editor.selections.SelectionBox;
import app.display.dialogs.visual_editor.view.panels.editor.tabPanels.LayoutSettingsPanel;
import grammar.Grammar;
import main.grammar.Clause;
import main.grammar.Symbol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import static app.display.dialogs.visual_editor.handler.Handler.mainPanel;
import static app.display.dialogs.visual_editor.recs.codecompletion.domain.model.Preprocessing.COMPLETION_WILDCARD;

public class EditorPanel extends JPanel implements IGraphPanel
{

    private DescriptionGraph graph = new DescriptionGraph();
    private final List<LudemeNodeComponent> nodeComponents = new ArrayList<>();
    private Point mousePosition;

    private final LayoutHandler lm;
    private ConnectionHandler ch;

    private double zoomFactor = 1.0;
    private double zoomScalar = 1.0;
    private boolean zoomed = false;
    private boolean busy = false;

    List<Symbol> symbols = Grammar.grammar().symbols();

    // flag to check if select button is active
    private boolean SELECTION_MODE = false;
    // flag to check if user performs selection
    private boolean SELECTING = false;
    // flag to check if selection was performed
    private boolean SELECTED = false;
    // list of selected nodes
    private List<LudemeNodeComponent> selectedLnc = new ArrayList<>();

    // node autoplacement
    private boolean autoplacement = false;

    // window to add a new ludeme out of all possible ones
    private final AddLudemeWindow addLudemeWindow = new AddLudemeWindow(symbols, this, false);
    // window to add a new ludeme as an input
    private final AddLudemeWindow connectLudemeWindow = new AddLudemeWindow(symbols, this, true);

    private static final boolean DEBUG = true;

    public static JLabel listener = new JLabel();

    // Recommendations
    private NGramController controller;
    private int N;
    private List<Long> latencies;
    private List<Integer> selectedCompletion;

    public EditorPanel(int width, int height)
    {
        setLayout(null);
        setPreferredSize(new Dimension(width, height));
        setBackground(DesignPalette.BACKGROUND_EDITOR);

        addMouseListener(clickListener);
        addMouseMotionListener(motionListener);
        addMouseWheelListener(wheelListener);
        //addMouseWheelListener(wheelListener2);

        add(addLudemeWindow);
        add(connectLudemeWindow);


        /*
        graph.setRoot(addNode(gameLudeme, 30, 30, false));*/
        Handler.gameDescriptionGraph = graph;
        Handler.editorPanel = this;
        Handler.addGraphPanel(graph, this);

        Handler.recordUserActions = false;
        LudemeNode gameLudemeNode = Handler.addNode(graph, Grammar.grammar().symbolsByName("Game").get(0), null, 30, 30, false);

        //LudemeNode gameLudemeNode = createLudemeNode(Grammar.grammar().symbolsByName("Game").get(0), 30, 30);
        graph.setRoot(gameLudemeNode);
        //addLudemeNodeComponent(gameLudemeNode, false);

        lm = new LayoutHandler(graph, graph.getRoot().id());
        ch = new ConnectionHandler(this);


        this.controller = Main.controller(); // this is done this way because controller.close() must be called before closing the editor, found in MainFrame.java
        this.N = controller.getN();
        latencies = new ArrayList<>();
        selectedCompletion = new ArrayList<>();
        Handler.recordUserActions = true;
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean showBackgroundDots = true;
        if(showBackgroundDots)
        {
            // draw background points
            // every 50 pixel a circle
            int paddingHorizontal = 35;
            int paddingvertical = 15;
            int frequency = DesignPalette.BACKGROUND_DOT_PADDING;
            int diameter = DesignPalette.BACKGROUND_DOT_DIAMETER;

            // to improve performance, only draw points that are in the visible area
            Rectangle viewRect = mainPanel.getPanel().getViewport().getViewRect();
            for (int i = paddingHorizontal; i < getWidth() - paddingHorizontal; i += frequency)
            {
                for (int j = paddingvertical; j < getHeight() - paddingvertical; j += frequency)
                {
                    if(i < viewRect.x || i > viewRect.x + viewRect.width || j < viewRect.y || j > viewRect.y + viewRect.height) continue; // TODO this can be optimized by a lot by adding this offset to i and j
                    g2.setColor(DesignPalette.BACKGROUND_VISUAL_HELPER);
                    g2.fillOval(i, j, diameter, diameter);
                }
            }
        }

        // set color for edges
        g2.setColor(DesignPalette.LUDEME_CONNECTION_EDGE);
        // set stroke for edges
        g2.setStroke(DesignPalette.LUDEME_EDGE_STROKE);

        // draw new connection
        ch.drawNewConnection(g2, mousePosition);

        // draw existing connections
        ch.paintConnections(g2);

        // paint ludeme nodes somewhere here...


        // Draw selection area
        if (SELECTION_MODE && !SELECTING) SelectionBox.drawSelectionModeIdle(mousePosition, g2);
        if (SELECTION_MODE && SELECTING) SelectionBox.drawSelectionArea(mousePosition, mousePosition, g2);
    }

    public void showCurrentlyAvailableLudemes(int x, int y)
    {
        if(DEBUG) System.out.println("[EP] Show list of connectable ludemes");
        // get game description up to current point
        int upUntilIndex = ch.getSelectedConnectionComponent().inputField().nodeArguments().get(0).index();
        for(NodeArgument ii : ch.getSelectedConnectionComponent().inputField().nodeArguments())
        {
            if(ii.index() < upUntilIndex) upUntilIndex = ii.index();
        }
        long start = System.nanoTime();
        List<Symbol> possibleSymbols = ch.getSelectedConnectionComponent().possibleSymbolInputs();
        String gameDescription = graph().toLudCodeCompletion(ch.getSelectedConnectionComponent().lnc().node(),  ch.getSelectedConnectionComponent().inputField().inputIndexFirst(), COMPLETION_WILDCARD);
        List<Symbol> typeMatched = TypeMatch.getInstance().typematch(gameDescription,controller,possibleSymbols);
        long finish = System.nanoTime();
        long latency = finish - start;
        latencies.add(latency);
        connectLudemeWindow.updateList(ch.getSelectedConnectionComponent().inputField(), typeMatched);
        connectLudemeWindow.setVisible(true);
        connectLudemeWindow.setLocation(mousePosition);
        connectLudemeWindow.searchField.requestFocus();
        revalidate();
        repaint();
    }

    public void addLudemeNodeComponent(LudemeNode node, boolean connect)
    {
        LudemeNodeComponent lc = new LudemeNodeComponent(node, this);
        addLudemeWindow.setVisible(false);
        connectLudemeWindow.setVisible(false);
        nodeComponents.add(lc);
        add(lc);
        lc.updatePositions();

        if(connect){ch.finishNewConnection(lc);}

        // expand editor
        //expandEditorPanelSize(lc);

        Handler.centerViewport(lc.getX()+lc.width()/2, lc.getY()+lc.getHeight()/2);
    }


    public ConnectionHandler connectionHandler()
    {
        return ch;
    }

    // # Methods to handle selection #

    /**
     * Enter selection mode
     */
    public void enterSelectionMode()
    {
        this.SELECTION_MODE = true;
    }

    /**
     * Exit selection mode
     * @return boundary box of the selection area
     */
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
     * Clear selection list and deselects all nodes
     */
    public void deselectEverything()
    {
        graph.getNodes().forEach(n -> {
            LudemeNodeComponent lnc = nodeComponent(n);
            lnc.setSelected(false);
            lnc.setDoubleSelected(false);
        });
        selectedLnc = new ArrayList<>();
        SELECTED = false;
        repaint();
        revalidate();
    }

    @Override
    public void addSelectionIndex(int index) {
        selectedCompletion.add(index);
    }

    /**
     * Select node and add it into selection list
     * @param lnc ludeme node component
     */
    public void addNodeToSelections(LudemeNodeComponent lnc)
    {
        if (!selectedLnc.contains(lnc))
        {
            SELECTED = true;
            lnc.setSelected(true);
            selectedLnc.add(lnc);
        }
    }

    public boolean isSELECTION_MODE()
    {
        return SELECTION_MODE;
    }

    public List<LudemeNodeComponent> selectedLnc()
    {
        return selectedLnc;
    }

    public void setAutoplacement(boolean autoplacement)
    {
        this.autoplacement = autoplacement;
    }



    // # Implementation of IGraphPanel interface methods #

    @Override
    public void notifyNodeRemoved(LudemeNodeComponent lnc)
    {
        nodeComponents.remove(lnc);
        remove(lnc);
        ch.removeAllConnections(lnc.node(), false);
        repaint();
        if (LayoutSettingsPanel.getLayoutSettingsPanel().isAutoPlacementOn())
            LayoutHandler.applyOnPanel(EditorPanel.this);
    }

    @Override
    public void notifyEdgeAdded(LudemeNodeComponent from, LudemeNodeComponent to, int inputFieldIndex)
    {
        LConnectionComponent source = from.inputArea().currentInputFields.get(inputFieldIndex).connectionComponent();
        LIngoingConnectionComponent target = to.header().ingoingConnectionComponent();
        ch.addConnection(source, target);
    }

    public void notifyEdgeAdded(LudemeNodeComponent from, LudemeNodeComponent to, NodeArgument inputFieldArgument)
    {
        LInputField inputField = null;
        for(LInputField ii : from.inputArea().currentInputFields)
        {
            if(ii.nodeArguments().contains(inputFieldArgument))
            {
                inputField = ii;
                break;
            }
        }
        assert inputField != null;
        LConnectionComponent source = inputField.connectionComponent();
        LIngoingConnectionComponent target = to.header().ingoingConnectionComponent();
        ch.addConnection(source, target);
    }

    public void notifyEdgeAdded(LudemeNodeComponent from, LudemeNodeComponent to, NodeArgument inputFieldArgument, int elementIndex)
    {
        LInputField inputField = null;
        for(LInputField ii : from.inputArea().currentInputFields)
        {
            if(ii.nodeArguments().contains(inputFieldArgument))
            {
                inputField = ii;
                break;
            }
        }

        if(elementIndex > 0)
        {
            assert inputField != null;
            int index = inputField.inputArea().inputFieldIndex(inputField) + elementIndex;
            while(index >= from.inputArea().currentInputFields.size()) from.inputArea().addCollectionItem(inputField);
            inputField = from.inputArea().currentInputFields.get(index);
        }

        assert inputField != null;
        LConnectionComponent source = inputField.connectionComponent();
        LIngoingConnectionComponent target = to.header().ingoingConnectionComponent();
        ch.addConnection(source, target);
    }

    @Override
    public void notifyEdgeRemoved(LudemeNodeComponent from, LudemeNodeComponent to) {
        // find inputfield of from node
        LInputField inputField = null;
        for(LInputField ii : from.inputArea().currentInputFields)
        {
            if(ii.nodeArguments().contains(to.node().creatorArgument()))
            {
                inputField = ii;
                break;
            }
        }
        assert inputField != null;
        ch.removeConnection(from.node(), inputField.connectionComponent());
    }

    @Override
    public void notifyEdgeRemoved(LudemeNodeComponent from, LudemeNodeComponent to, int elementIndex) {
        // find inputfield of from node
        LInputField inputField = null;
        for(LInputField ii : from.inputArea().currentInputFields)
        {
            if(ii.nodeArguments().contains(to.node().creatorArgument()))
            {
                inputField = ii;
                break;
            }
        }
        inputField = from.inputArea().currentInputFields.get(from.inputArea().inputFieldIndex(inputField) + elementIndex);
        assert inputField != null;
        ch.removeConnection(from.node(), inputField.connectionComponent());
    }

    @Override
    public void notifyCollapsed(LudemeNodeComponent lnc, boolean collapsed) {
        lnc.header().inputField().notifyCollapsed();
        if(!collapsed) lnc.setVisible(true);
        repaint();
    }

    @Override
    public void notifyTerminalInputUpdated(LudemeNodeComponent lnc, NodeArgument inputFieldArgument, Object input) {
        lnc.updateProvidedInputs();
    }

    @Override
    public void notifyInputsUpdated(LudemeNodeComponent lnc)
    {
        lnc.updateProvidedInputs();
    }

    @Override
    public void notifyCollectionAdded(LudemeNodeComponent lnc, NodeArgument inputFieldArgument, int elementIndex) {
        // find parent inputfield
        LInputField inputField = null;
        for(LInputField ii : lnc.inputArea().currentInputFields)
        {
            if(ii.nodeArguments().contains(inputFieldArgument))
            {
                inputField = ii;
                break;
            }
        }
        assert inputField != null;
        inputField.notifyCollectionAdded();
    }

    @Override
    public void notifyCollectionRemoved(LudemeNodeComponent lnc, NodeArgument inputFieldArgument, int elementIndex) {
        // find removed inputfield
        LInputField inputField = null;
        for(LInputField ii : lnc.inputArea().currentInputFields)
        {
            if(ii.nodeArguments().contains(inputFieldArgument))
            {
                inputField = ii;
                break;
            }
        }
        inputField = lnc.inputArea().currentInputFields.get(lnc.inputArea().inputFieldIndex(inputField) + elementIndex);
        inputField.notifyCollectionRemoved();
    }

    @Override
    public void notifyCollectionInputUpdated(LudemeNodeComponent lnc, NodeArgument inputFieldArgument, int elementIndex, Object input) {
        // find parent inputfield
        /*LInputField inputField = null;
        for(LInputField ii : lnc.inputArea().currentInputFields)
        {
            if(ii.nodeArguments().contains(inputFieldArgument))
            {
                inputField = ii;
                break;
            }
        }
        if(elementIndex > 0) inputField = inputField.parent().children().get(elementIndex-1);
        inputField.setUserInput(input);*/
    }

    @Override
    public void notifySelectedClauseChanged(LudemeNodeComponent lnc, Clause clause) {
        lnc.changeCurrentClause(clause);
    }

    @Override
    public void notifyTerminalActivated(LudemeNodeComponent lnc, NodeArgument inputFieldArgument, boolean activated) {
        LInputField inputField = null;
        for(LInputField ii : lnc.inputArea().currentInputFields)
        {
            if(ii.nodeArguments().contains(inputFieldArgument))
            {
                inputField = ii;
                break;
            }
        }

        assert inputField != null;
        if(!inputField.isMerged())
        {
            if (activated) inputField.notifyActivated();
            else inputField.notifyDeactivated();
        }
        else
        {
            lnc.addTerminal(inputFieldArgument, inputField);
        }
    }

    @Override
    public JPanel panel() {
        return this;
    }

    @Override
    public void notifyNodeAdded(LudemeNode node, boolean connect) {
        addLudemeNodeComponent(node, connect);
    }

    @Override
    public boolean isBusy() {
        return busy;
    }

    @Override
    public void setBusy(boolean b)
    {
     busy = b;
    }

    @Override
    public void drawGraph(DescriptionGraph graph)
    {
        busy = true;
        if(DEBUG) System.out.println("\n[EP] Redrawing graph\n");
        this.graph = graph;
        removeAll();
        nodeComponents.clear();
        ch.clearEdges();
        ch.setSelectedConnectionComponent(null);
        List<LudemeNode> nodes = graph.getNodes();
        for(LudemeNode node : nodes)
        {
            //add(new LudemeBlock(node, null, 300));
            LudemeNodeComponent lc = new LudemeNodeComponent(node, this);
            nodeComponents.add(lc);
            add(lc);
            lc.revalidate();
        }
        for(LudemeNodeComponent lc : nodeComponents)
        {
            lc.updateProvidedInputs();
            lc.updatePositions();
        }

        add(addLudemeWindow);
        add(connectLudemeWindow);

        busy = false;

        revalidate();
        repaint();
    }

    @Override
    public void updateGraph()
    {
        if(DEBUG) System.out.println("\n[EP] Updating graph\n");
        for (LudemeNodeComponent lc : nodeComponents)
        {
            lc.revalidate();
            lc.updateProvidedInputs();
            lc.updateLudemePosition();
        }
        revalidate();
        repaint();
    }

    @Override
    public void updateNodePositions()
    {
        for (LudemeNodeComponent lc : nodeComponents)
        {
            lc.updateLudemePosition();
        }
        revalidate();
        repaint();
    }

    public void syncNodePositions()
    {
        for (LudemeNodeComponent lc : nodeComponents)
        {
            lc.syncPositionsWithLN();
        }
        revalidate();
        repaint();
    }

    @Override
    public DescriptionGraph graph() {
        return graph;
    }

    @Override
    public LudemeNodeComponent nodeComponent(LudemeNode node)
    {
        for(LudemeNodeComponent lc : nodeComponents)
        {
            if(lc.node().equals(node))
            {
                return lc;
            }
        }
        return null;
    }

    @Override
    public void showAllAvailableLudemes(int x, int y)
    {
        addLudemeWindow.setVisible(true);
        addLudemeWindow.setLocation(mousePosition);
        addLudemeWindow.searchField.requestFocus();
        revalidate();
        repaint();
    }

    @Override
    public void clickedOnNode(LudemeNodeComponent lnc)
    {
        LudemeNode node = lnc.node();
        if(ch.getSelectedConnectionComponent() != null)
        {
            if(ch.getSelectedConnectionComponent().possibleSymbolInputs().contains(node.symbol()) && !lnc.ingoingConnectionComponent().isFilled())
            {
                ch.finishNewConnection(lnc);
            }
        }
    }

    @Override
    public LayoutHandler getLayoutHandler()
    {
        return lm;
    }

    @Override
    public int selectedRootId() {
        if (!selectedLnc.isEmpty())
        {
            LudemeNodeComponent rootLnc = selectedLnc.get(0);
            // TODO: implemented awfully, if performance is bad, refactor how the selection list is implemented
            for (LudemeNode n:
                    graph.getNodes()) {
                if (nodeComponent(n).getBounds().intersects(rootLnc.getBounds())) return n.id();
            }
        }
        return graph.getRoot().id();
    }

    @Override
    public List<iGNode> selectedNodes()
    {
        List<iGNode> nodeList = new ArrayList<>();
        for (LudemeNodeComponent lnc:
             selectedLnc) {
            nodeList.add(lnc.node());
        }
        return nodeList;
    }

    public void selectAllNodes()
    {
        for (LudemeNodeComponent lnc: nodeComponents)
        {
            addNodeToSelections(lnc);
        }
        repaint();
        revalidate();
    }

    // # Mouse listeners #

    MouseListener clickListener = new MouseAdapter()
    {
        private void openPopupMenu(MouseEvent e)
        {
            JPopupMenu popupMenu = new EditorPopupMenu(EditorPanel.this, e.getX(), e.getY());
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
            super.mouseClicked(e);

            if(connectLudemeWindow.isVisible())
            {
                ch.cancelNewConnection();
            }
            addLudemeWindow.setVisible(false);
            connectLudemeWindow.setVisible(false);
            if(e.getButton() == MouseEvent.BUTTON1)
            {
                // user is drawing a new connection
                if(ch.getSelectedConnectionComponent() != null)
                {
                    // if user has no chocie for next ludeme -> automatically add required ludeme
                    if(ch.getSelectedConnectionComponent().possibleSymbolInputs().size() == 1)
                    {
                        Handler.addNode(graph, ch.getSelectedConnectionComponent().possibleSymbolInputs().get(0), ch.getSelectedConnectionComponent().inputField().nodeArgument(0), e.getX(), e.getY(), true);
                        //addNode(ch.getSelectedConnectionComponent().possibleSymbolInputs().get(0), e.getX(), e.getY(), true);
                    }
                    else if(!connectLudemeWindow.isVisible() && ch.getSelectedConnectionComponent().possibleSymbolInputs().size() > 1)
                    {
                        showCurrentlyAvailableLudemes(e.getX(), e.getY());
                    }
                    if (LayoutSettingsPanel.getLayoutSettingsPanel().isAutoPlacementOn())
                        LayoutHandler.applyOnPanel(EditorPanel.this);
                }
            }
            else
            {
                // user is selecting a connection -> cancel new connection
                if(ch.getSelectedConnectionComponent() != null)
                {
                    ch.cancelNewConnection();
                }
            }

            // When selection was performed user can clear it out by clicking on blank area
            if (SELECTED)
            {
                LayoutSettingsPanel.getLayoutSettingsPanel().setSelectedComponent("Empty", false);
                deselectEverything();
            }

            repaint();
            revalidate();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
        }

        public void mousePressed(MouseEvent e)
        {
            if(e.getButton() == MouseEvent.BUTTON3)
            {
                ch.cancelNewConnection();
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
            {
                openPopupMenu(e);
            }

            if (SELECTING && e.getButton() == MouseEvent.BUTTON1)
            {
                Rectangle region = exitSelectionMode();
                if (region != null)
                {
                    graph.getNodes().forEach(n -> {
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

    MouseMotionListener motionListener = new MouseAdapter()
    {
        @Override
        public void mouseMoved(MouseEvent e)
        {
            super.mouseMoved(e);
            mousePosition = e.getPoint();
            if (SELECTION_MODE) repaint();
            if(ch.getSelectedConnectionComponent() != null) repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            super.mouseDragged(e);
            mousePosition = e.getPoint();
            if (SELECTING) repaint();
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

    MouseWheelListener wheelListener = new MouseAdapter()
    {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            double amount = Math.pow(1.01, e.getScrollAmount());
            if(e.getWheelRotation() > 0)
            {
                float scalar = (float) (Math.min(DesignPalette.SCALAR * amount, DesignPalette.MAX_SCALAR));
                //float scalar = 0.95f;
                //zoomScalar *= 1.05f;
                DesignPalette.scale(1.02f);
                scaleNodes(0.98);
            }
            else
            {
                float scalar = (float) (Math.max(DesignPalette.SCALAR / amount, DesignPalette.MIN_SCALAR));
                //float scalar = 1.05f;
                //zoomScalar *= 0.95f;
                DesignPalette.scale(1.0f/1.02f);
                scaleNodes(1.0/0.98);
            }
            //updateNodePositions();
            syncNodePositions();
            repaint();
        }
    };

    private void scaleNodes(double scalar)
    {
        graph.getNodes().forEach(ludemeNode -> {
            Vector2D scaledPos = getScaledCoords(ludemeNode.pos(), scalar, Handler.getViewPortSize().width, Handler.getViewPortSize().height);
            ludemeNode.setPos(scaledPos);
            // ludemeNode.setWidth((int) (ludemeNode.width()*scalar));
        });
    }

    private Vector2D getScaledCoords(Vector2D pos, double scalar, double W, double H)
    {
        double xp = pos.x()-W/2;
        double yp = (pos.y()-H/2)*-1;
        xp*=scalar;
        yp*=scalar;
        return new Vector2D(xp+W/2, -1*yp+H/2);
    }

    MouseWheelListener wheelListener2 = new MouseAdapter() {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            zoomed = true;
            //Zoom in
            if (e.getWheelRotation() < 0) {
                zoomFactor *= 1.1;
                repaint();
                revalidate();
            }
            //Zoom out
            if (e.getWheelRotation() > 0) {
                zoomFactor /= 1.1;
                repaint();
                revalidate();
            }
        }
    };

    public List<Long> getLatencies() {
        return latencies;
    }

    public List<Integer> getSelectedCompletion() {
        return selectedCompletion;
    }

}
