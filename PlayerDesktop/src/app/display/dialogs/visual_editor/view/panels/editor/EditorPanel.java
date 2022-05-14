package app.display.dialogs.visual_editor.view.panels.editor;


import app.display.dialogs.visual_editor.LayoutManagement.LayoutManager.LayoutHandler;
import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.model.grammar.parser.Parser;
import app.display.dialogs.visual_editor.recs.guiInterfacing.CodeCompletion;
import app.display.dialogs.visual_editor.view.components.AddLudemeWindow;
import app.display.dialogs.visual_editor.view.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.ImmutablePoint;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeConnection;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.InputInformation;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LIngoingConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LInputField;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static app.display.dialogs.visual_editor.handler.Handler.mainPanel;

public class EditorPanel extends JPanel implements IGraphPanel {

    private DescriptionGraph graph = new DescriptionGraph();
    private List<LudemeNodeComponent> nodeComponents = new ArrayList<>();
    private List<LudemeConnection> edges = new ArrayList<>();
    private Point mousePosition;
    private LConnectionComponent selectedConnectionComponent = null;

    private LayoutHandler lm;

    private double zoomFactor = 1.0;
    private double zoomFactor0 = 1.0;
    private boolean zoomed = false;

    // Reads grammar from file and generates all ludemes
    Parser p = new Parser();
    List<Ludeme> ludemes = p.getLudemes();

    // window to add a new ludeme out of all possible ones
    private AddLudemeWindow addLudemeWindow = new AddLudemeWindow(ludemes, this, false);
    // window to add a new ludeme as an input
    private AddLudemeWindow connectLudemeWindow = new AddLudemeWindow(ludemes, this, true);

    private boolean showBackgroundDots = true;

    private static final boolean DEBUG = true;

    public EditorPanel(int width, int height){
        setLayout(null);
        setPreferredSize(new Dimension(width, height));
        setBackground(DesignPalette.BACKGROUND_EDITOR);

        addMouseListener(clickListener);
        addMouseMotionListener(motionListener);
        addMouseWheelListener(wheelListener);

        add(addLudemeWindow);
        add(connectLudemeWindow);

        Ludeme gameLudeme = null;
        for(Ludeme l : p.getLudemes())
            if(l.getName().equals("game")) gameLudeme = l;

        /*
        graph.setRoot(addNode(gameLudeme, 30, 30, false));*/
        Handler.gameDescriptionGraph = graph;

        LudemeNode gameLudemeNode = createLudemeNode(gameLudeme, 30, 30);
        graph.setRoot(gameLudemeNode);
        addLudemeNodeComponent(gameLudemeNode, false);

        lm = new LayoutHandler(graph, graph.getRoot().getId());
    }

    public EditorPanel(){
        setLayout(null);
        //setPreferredSize(DesignPalette.DEFAULT_FRAME_SIZE);
        setBackground(DesignPalette.BACKGROUND_EDITOR);

        addMouseListener(clickListener);
        addMouseMotionListener(motionListener);
        addMouseWheelListener(wheelListener);
        addMouseWheelListener(wheelListener2);

        add(addLudemeWindow);
        add(connectLudemeWindow);

        Ludeme gameLudeme = null;
        for(Ludeme l : p.getLudemes())
            if(l.getName().equals("game")) gameLudeme = l;

        /*
        graph.setRoot(addNode(gameLudeme, 30, 30, false));*/
        Handler.gameDescriptionGraph = graph;

        LudemeNode gameLudemeNode = createLudemeNode(gameLudeme, 30, 30);
        graph.setRoot(gameLudemeNode);
        addLudemeNodeComponent(gameLudemeNode, false);

        lm = new LayoutHandler(graph, graph.getRoot().getId());
    }

    @Override
    public void drawGraph(DescriptionGraph graph) {
        if(DEBUG) System.out.println("\n[EP] Redrawing graph\n");
        this.graph = graph;
        removeAll();
        nodeComponents.clear();
        edges.clear();
        selectedConnectionComponent = null;
        List<LudemeNode> nodes = graph.getNodes();
        for(LudemeNode node : nodes) {
            //add(new LudemeBlock(node, null, 300));
            LudemeNodeComponent lc = new LudemeNodeComponent(node, this);
            nodeComponents.add(lc);
            add(lc);
            lc.revalidate();
        }
        for(LudemeNodeComponent lc : nodeComponents){
            lc.updateProvidedInputs();
            lc.updatePositions();
        }

        add(addLudemeWindow);
        add(connectLudemeWindow);

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
    public DescriptionGraph getGraph() {
        return graph;
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //float scale = 0.5f;
        //int w = getWidth();
        //int h = getHeight();

        //g2.scale(scale, scale);

        // TODO: fix zooming
        // Scaling works but not visible
        if (zoomed) {
            AffineTransform at = new AffineTransform();
            at.scale(zoomFactor, zoomFactor);
            zoomFactor0 = zoomFactor;
            g2.transform(at);
        }

        if(showBackgroundDots) {
            // draw background points
            // every 50 pixel a circle
            int paddingHorizontal = 35;
            int paddingvertical = 15;
            int frequency = DesignPalette.BACKGROUND_DOT_PADDING;
            int diameter = DesignPalette.BACKGROUND_DOT_DIAMETER;

            // to improve performance, only draw points that are in the visible area
            Rectangle viewRect = mainPanel.getPanel().getViewport().getViewRect();
            for (int i = paddingHorizontal; i < getWidth() - paddingHorizontal; i += frequency) {
                for (int j = paddingvertical; j < getHeight() - paddingvertical; j += frequency) {
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
        if(selectedConnectionComponent != null && mousePosition != null) {
            paintNewConnection(g2, mousePosition);
        }

        // draw existing connections
        paintConnections(g2);
    }

    private void paintConnections(Graphics2D g2){
        for(LudemeConnection e : edges){
            ImmutablePoint inputPoint = e.getInputPosition();
            ImmutablePoint targetPoint = e.getTargetPosition();

            int cp_x = inputPoint.x + Math.abs((inputPoint.x-targetPoint.x)/2);
            int cp1_y =inputPoint.y;
            int cp2_y = targetPoint.y;

            Path2D p2d = new Path2D.Double();
            p2d.moveTo(inputPoint.x, inputPoint.y);
            p2d.curveTo(cp_x, cp1_y, cp_x, cp2_y, targetPoint.x, targetPoint.y);
            g2.draw(p2d);
        }
    }

    public void startNewConnection(LConnectionComponent source){
        if(DEBUG) System.out.println("[EP] Start connection: " + source.getConnectionPointPosition() + " , " + source.getRequiredLudemes());

        if(selectedConnectionComponent != null){
            selectedConnectionComponent.setFill(false);
        }
        selectedConnectionComponent = source;
    }

    @Override
    public void cancelNewConnection(){
        if(selectedConnectionComponent != null) {
            selectedConnectionComponent.setFill(false);
            selectedConnectionComponent = null;
        }
    }

    public void finishNewConnection(LudemeNodeComponent target){
        addConnection(selectedConnectionComponent, target.getIngoingConnectionComponent());
        selectedConnectionComponent = null;
    }

    @Override
    public void addConnection(LConnectionComponent source, LIngoingConnectionComponent target) {

        source.updatePosition();
        target.updatePosition();

        if(!source.getInputField().isSingle()){
            source = source.getLudemeNodeComponent().getInputArea().addedConnection(target.getHeader().getLudemeNodeComponent(), source.getInputField()).getConnectionComponent();
        }

        source.updatePosition();
        target.updatePosition();

        source.setFill(true);
        target.setFill(true);
        source.setConnectedTo(target.getHeader().getLudemeNodeComponent());

        // Add an edge
        Handler.addEdge(graph, source.getLudemeNodeComponent().getLudemeNode(), target.getHeader().getLudemeNodeComponent().getLudemeNode());
        LudemeConnection connection = new LudemeConnection(source, target);
        edges.add(connection);

        // Update the provided input in the description graph
        // differentiate between an inputed provided to a collection and otherwise
        if(source.getInputField().getInputInformation().isCollection()){
            LudemeNode sourceNode = source.getLudemeNodeComponent().getLudemeNode();
            InputInformation sourceInput = source.getInputField().getInputInformation();

            // TODO: Perhaps this part should be put into LInputField.java addCollectionItem() method

            LudemeNode[] providedInput = (LudemeNode[]) sourceNode.getProvidedInputs()[sourceInput.getIndex()];

            // get children of collection
            List<LInputField> children;
            int numberOfChildren;
            if(source.getInputField().parent != null){
                children = source.getInputField().parent.children;
                numberOfChildren = source.getInputField().parent.children.size();
            } else {
                children = source.getInputField().children;
                numberOfChildren = source.getInputField().children.size();
            }

            // The provided input class just be an array. If it is null, then create it NOTE!: the first collection inputfield is not counted as a child, therefore numberOfChildren+1
            if(sourceNode.getProvidedInputs()[sourceInput.getIndex()] == null){
                providedInput = new LudemeNode[numberOfChildren+1];
            } else {
                providedInput = (LudemeNode[]) sourceNode.getProvidedInputs()[sourceInput.getIndex()];
            }
            // if the array is not big enough, expand it.
            if(providedInput.length < numberOfChildren+1){
                LudemeNode[] newProvidedInput = new LudemeNode[numberOfChildren+1];
                System.arraycopy(providedInput, 0, newProvidedInput, 0, providedInput.length);
                providedInput = newProvidedInput;
            }
            // get the index of the current input field w.r.t collection field
            int i = children.indexOf(source.getInputField()) + 1; // + 1 because the first input field is not counted as a child
            //if(i==-1) i = 0;
            providedInput[i] = target.getHeader().getLudemeNodeComponent().getLudemeNode();
            // TODO: REMOVE LATER
            System.out.println("\u001B[32m"+"Calling from EP 237"+"\u001B[0m");
            Handler.updateInput(graph, sourceNode, sourceInput.getIndex(), providedInput);
        } else {
            if(DEBUG) System.out.println("[EP] Adding connection: " + source.getLudemeNodeComponent().getLudemeNode().getLudeme().getName() + " , " + target.getHeader().getLudemeNodeComponent().getLudemeNode().getLudeme().getName() + " at index " + source.getInputField().getInputIndex());
            System.out.println("\u001B[32m"+"Calling from EP 241"+"\u001B[0m");
            Handler.updateInput(graph, source.getLudemeNodeComponent().getLudemeNode(), source.getInputField().getInputIndex(), target.getHeader().getLudemeNodeComponent().getLudemeNode());
        }


        repaint();
    }


    @Override
    public LudemeNodeComponent getNodeComponent(LudemeNode node) {
        for(LudemeNodeComponent lc : nodeComponents){
            if(lc.getLudemeNode().equals(node)){
                return lc;
            }
        }
        return null;
    }

    @Override
    public LudemeNode addNode(Ludeme ludeme, int x, int y, boolean connect) {
        LudemeNode node = new LudemeNode(ludeme, x, y);
        //LudemeNodeComponent lc = new LudemeNodeComponent(node, this);
        Handler.addNode(graph, node);

        addLudemeNodeComponent(node, connect);

        //Handler.centerViewport(x+lc.getWidth()/2, y+lc.getHeight()/2);

        repaint();

        if(DEBUG) System.out.println("[EP] Added node: " + node.getLudeme().getName());

        return node;
    }

    public LudemeNode createLudemeNode(Ludeme ludeme, int x, int y) {
        LudemeNode node = new LudemeNode(ludeme, x, y);
        Handler.addNode(graph, node);
        return node;
    }

    private LudemeNodeComponent addLudemeNodeComponent(LudemeNode node, boolean connect) {
        LudemeNodeComponent lc = new LudemeNodeComponent(node, this);
        addLudemeWindow.setVisible(false);
        connectLudemeWindow.setVisible(false);
        nodeComponents.add(lc);
        add(lc);
        lc.updatePositions();

        if(connect){
            finishNewConnection(lc);
        }

        // expand editor
        //expandEditorPanelSize(lc);

        Handler.centerViewport(lc.getX()+lc.getWidth()/2, lc.getY()+lc.getHeight()/2);

        return lc;
    }

    /**
     * Expands the editor panel size if nodes are close to the border
     * TODO: Not working
     * @param lnc
     * @return
     */
    private boolean expandEditorPanelSize(LudemeNodeComponent lnc){

        int expandHeightBy = lnc.getHeight()*4;
        int expandWidthBy = lnc.getWidth()*4;

        int x = lnc.getX(), y = lnc.getY();

        int additionalHeight = expandHeightBy - y;
        int additionalWidth = expandWidthBy - x;

        boolean expanded = false;
        int newHeight = getHeight(), newWidth = getWidth();

        if(additionalHeight > 0){
            newHeight = getHeight() + additionalHeight;
            expanded = true;
        }
        if(additionalWidth > 0){
            newWidth = getWidth() + additionalWidth;
            expanded = true;
        }

        if(expanded){
            System.out.println("Expanding from " + getSize() + " to " + new Dimension(newWidth, newHeight));
        }

        setPreferredSize(new Dimension(newWidth, newHeight));
        setSize(getPreferredSize());

        repaint();
        revalidate();

        return expanded;
    }

    @Override
    public void showAllAvailableLudemes(int x, int y) {
        addLudemeWindow.setVisible(true);
        addLudemeWindow.setLocation(mousePosition);
        addLudemeWindow.searchField.requestFocus();
        revalidate();
        repaint();
    }

    @Override
    public void removeAllConnections(LudemeNode node) {
        removeAllConnections(node, true);
    }

    public void removeAllConnections(LudemeNode node, boolean onlyOutgoingConnections){
        for(LudemeConnection e : new ArrayList<>(edges)){
            if(e.getConnectionComponent().getLudemeNodeComponent().getLudemeNode().equals(node) || (!onlyOutgoingConnections && e.getIngoingConnectionComponent().getHeader().getLudemeNodeComponent().getLudemeNode().equals(node))){
                edges.remove(e);
                e.getIngoingConnectionComponent().setFill(false); // header
                e.getConnectionComponent().setFill(false); // input
                e.getConnectionComponent().setConnectedTo(null);
                e.getConnectionComponent().getInputField().getLudemeNodeComponent().getInputArea().updateComponent(node, null,true);
                System.out.println("\u001B[32m"+"Calling from EP 307"+"\u001B[0m");
                Handler.updateInput(graph, e.getConnectionComponent().getLudemeNodeComponent().getLudemeNode(), e.getConnectionComponent().getInputField().getInputIndex(), null);
            }
        }
        repaint();
    }


    // removes all outgoing conenctions of the node's ("node") connection component "connection"
    @Override
    public void removeConnection(LudemeNode node, LConnectionComponent connection) {
        if(connection.getLudemeNodeComponent().dynamic) connection.getLudemeNodeComponent().getInputArea().removedConnectionDynamic(node, connection.getInputField());
        for(LudemeConnection e : new ArrayList<>(edges)){
            if(e.getConnectionComponent().equals(connection)){
                edges.remove(e);
                e.getIngoingConnectionComponent().setFill(false); // header
                e.getConnectionComponent().setFill(false); // input
                e.getConnectionComponent().setConnectedTo(null);
                e.getConnectionComponent().getInputField().getLudemeNodeComponent().getInputArea().updateComponent(node, null, true);
                // check whether it was the element of a collection
                if(connection.getInputField().isSingle() && connection.getInputField().getInputInformation().isCollection()){
                    // if element of collection udpate the array
                    LudemeNode[] providedInputs = (LudemeNode[]) node.getProvidedInputs()[connection.getInputField().getInputIndex()];
                    // find index which to remove from array
                    int indexToUpdate;
                    if(connection.getInputField().parent != null) {
                        indexToUpdate = connection.getInputField().parent.children.indexOf(connection.getInputField()) + 1;
                    } else {
                        indexToUpdate = 0;
                    }
                    // set to null
                    providedInputs[indexToUpdate] = null;
                    System.out.println("\u001B[32m"+"Calling from EP 339"+"\u001B[0m");
                    Handler.updateInput(graph, e.getConnectionComponent().getLudemeNodeComponent().getLudemeNode(), connection.getInputField().getInputIndex(), providedInputs);
                } else {
                    System.out.println("\u001B[32m"+"Calling from EP 342"+"\u001B[0m");
                    Handler.updateInput(graph, e.getConnectionComponent().getLudemeNodeComponent().getLudemeNode(), e.getConnectionComponent().getInputField().getInputIndex(), null);
                }
            }
        }
        repaint();
    }

    @Override
    public void clickedOnNode(LudemeNodeComponent lnc) {
        LudemeNode node = lnc.getLudemeNode();
        if(selectedConnectionComponent != null){
            if(selectedConnectionComponent.getRequiredLudemes().contains(node.getLudeme()) && !lnc.getIngoingConnectionComponent().isFilled()) {
                finishNewConnection(lnc);
            }
        }
    }

    @Override
    public void removeNode(LudemeNode node) {
        if(DEBUG) System.out.println("[EP] Removing node " + node.getLudeme().getName());
        LudemeNodeComponent lc = getNodeComponent(node);
        nodeComponents.remove(lc);
        removeAllConnections(node, false);
        Handler.removeNode(graph, node);
        remove(lc);
        repaint();
    }

    @Override
    public LayoutHandler getLayoutHandler()
    {
        return lm;
    }

    public void showCurrentlyAvailableLudemes(int x, int y) {
        if(DEBUG) System.out.println("[EP] Show list of connectable ludemes");
        // get game description up to current point
        int upUntilIndex = selectedConnectionComponent.getInputField().getInputInformations().get(0).getIndex();
        for(InputInformation ii : selectedConnectionComponent.getInputField().getInputInformations()){
            if(ii.getIndex() < upUntilIndex) upUntilIndex = ii.getIndex();
        }
        String gameDescription = selectedConnectionComponent.getLudemeNodeComponent().getLudemeNode().getStringRepresentation(upUntilIndex-1);

        connectLudemeWindow.updateList(CodeCompletion.getRecommendations(ludemes, gameDescription, selectedConnectionComponent.getRequiredLudemes()));
        connectLudemeWindow.setVisible(true);
        connectLudemeWindow.setLocation(mousePosition);
        connectLudemeWindow.searchField.requestFocus();
        revalidate();
        repaint();
    }

    MouseListener clickListener = new MouseAdapter() {

        private void openPopupMenu(MouseEvent e){
            JPopupMenu popupMenu = new EditorPopupMenu(EditorPanel.this);
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);

            System.out.println(Arrays.toString(((LudemeNode) graph.getRoot()).getProvidedInputs()));

            if(connectLudemeWindow.isVisible()){
                cancelNewConnection();
            }
            addLudemeWindow.setVisible(false);
            connectLudemeWindow.setVisible(false);
            if(e.getButton() == MouseEvent.BUTTON1) {
                // user is drawing a new connection
                if(selectedConnectionComponent != null) {
                    // if user has no chocie for next ludeme -> automatically add required ludeme
                    if(selectedConnectionComponent.getRequiredLudemes().size() == 1) {
                        addNode(selectedConnectionComponent.getRequiredLudemes().get(0), e.getX(), e.getY(), true);
                    }
                    else if(!connectLudemeWindow.isVisible() && selectedConnectionComponent.getRequiredLudemes().size() > 1) {
                        showCurrentlyAvailableLudemes(e.getX(), e.getY());
                    }
                }
            } else {
                // user is selecting a connection -> cancel new connection
                if(selectedConnectionComponent != null) {
                    cancelNewConnection();
                }
            }
        }

        public void mousePressed(MouseEvent e){
            if(e.getButton() == MouseEvent.BUTTON3){
                cancelNewConnection();
                openPopupMenu(e);
            }
        }

        public void mouseReleased(MouseEvent e){
            if(e.getButton() == MouseEvent.BUTTON3){
                openPopupMenu(e);
            }
        }

    };

    MouseMotionListener motionListener = new MouseAdapter() {
        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            mousePosition = e.getPoint();
            if(selectedConnectionComponent != null){
                repaint();
            }
        }
    };

    private void paintNewConnection(Graphics2D g2, Point mousePosition){
        ImmutablePoint connection_point = selectedConnectionComponent.getConnectionPointPosition();
        Path2D p2d = new Path2D.Double();

        //if(selectedConnectionComponent.isOutgoing()){
            int cp_x = connection_point.x + Math.abs((connection_point.x-mousePosition.x)/2);
            p2d.moveTo(connection_point.x, connection_point.y);
            p2d.curveTo(cp_x, connection_point.y, cp_x, mousePosition.y, mousePosition.x, mousePosition.y);
        //}
        /*
        else {
            int cp_x = mousePosition.x + Math.abs((mousePosition.x-connection_point.x)/2);
            p2d.moveTo(mousePosition.x, mousePosition.y);
            p2d.curveTo(cp_x, mousePosition.y, cp_x, connection_point.y, connection_point.x, connection_point.y);
        }*/
        g2.draw(p2d);
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    MouseWheelListener wheelListener = new MouseAdapter() {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            double amount = Math.pow(1.01, e.getScrollAmount());
            if(e.getWheelRotation() > 0){
                DesignPalette.scale((float) (Math.max(DesignPalette.SCALAR / amount, DesignPalette.MIN_SCALAR)));
            } else {
                DesignPalette.scale((float) (Math.min(DesignPalette.SCALAR * amount, DesignPalette.MAX_SCALAR)));
            }
            repaint();
        }
    };

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


}
