package app.display.dialogs.visual_editor.view.panels.editor;


import app.display.dialogs.visual_editor.LayoutManagement.LayoutManager.LayoutHandler;
import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.model.grammar.parser.Parser;
import app.display.dialogs.visual_editor.view.components.AddLudemeWindow;
import app.display.dialogs.visual_editor.view.components.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.ImmutablePoint;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeConnection;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.InputInformation;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LIngoingConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LInputField;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.MainPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class EditorPanel extends JPanel implements IGraphPanel {

    private DescriptionGraph graph = new DescriptionGraph();
    private List<LudemeNodeComponent> nodeComponents = new ArrayList<>();
    private List<LudemeConnection> edges = new ArrayList<>();
    private Point mousePosition;
    private LConnectionComponent selectedConnectionComponent = null;

    private LayoutHandler lm;

    // Reads grammar from file and generates all ludemes
    Parser p = new Parser();
    List<Ludeme> ludemes = p.getLudemes();

    // window to add a new ludeme out of all possible ones
    private AddLudemeWindow addLudemeWindow = new AddLudemeWindow(ludemes, this, false);
    // window to add a new ludeme as an input
    private AddLudemeWindow connectLudemeWindow = new AddLudemeWindow(ludemes, this, true);

    public EditorPanel(int width, int height){
        setLayout(null);
        setPreferredSize(new Dimension(width, height));
        setBackground(DesignPalette.BACKGROUND_EDITOR);

        addMouseListener(clickListener);
        addMouseMotionListener(motionListener);

        add(addLudemeWindow);
        add(connectLudemeWindow);

        Ludeme gameLudeme = null;
        for(Ludeme l : p.getLudemes())
            if(l.getName().equals("game")) gameLudeme = l;

        graph.setRoot(addNode(gameLudeme, 30, 30, false));
        Handler.gameDescriptionGraph = graph;

        lm = new LayoutHandler(graph, graph.getRoot().getId());
    }

    @Override
    public void drawGraph(DescriptionGraph graph) {
        this.graph = graph;
        //lm = new LayoutHandler(graph, graph.getRoot().getId());
        removeAll();
        nodeComponents.clear();
        edges.clear();
        selectedConnectionComponent = null;
        List<LudemeNode> nodes = graph.getNodes();
        for(LudemeNode node : nodes) {
            //add(new LudemeBlock(node, null, 300));
            LudemeNodeComponent lc = new LudemeNodeComponent(node, 250, this);
            nodeComponents.add(lc);
            add(lc);
            lc.revalidate();
        }
        for(LudemeNodeComponent lc : nodeComponents){
            lc.updateProvidedInputs(); //TODO: FIX. Call of this method duplicates children nodes id in parent's list
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

        // draw new connection
        if(selectedConnectionComponent != null && mousePosition != null) {
            paintNewConnection(g2, mousePosition);
        }

        // draw existing connections
        paintConnections(g2);

        repaint();
        revalidate();
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
        System.out.println(source.getConnectionPointPosition() + " , " + source.getRequiredLudemes());
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

            LudemeNode[] providedInput = null;

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
            Handler.updateInput(graph, sourceNode, sourceInput.getIndex(), providedInput);
        } else {
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
        LudemeNodeComponent lc = new LudemeNodeComponent(node, 250, this);
        Handler.addNode(graph, node);
        nodeComponents.add(lc);
        add(lc);
        lc.updatePositions();

        if(connect){
            finishNewConnection(lc);
        }

        addLudemeWindow.setVisible(false);
        connectLudemeWindow.setVisible(false);

        Handler.centerViewport(x+lc.getWidth()/2, y+lc.getHeight()/2);

        repaint();
        return node;
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
                Handler.updateInput(graph, e.getConnectionComponent().getLudemeNodeComponent().getLudemeNode(), e.getConnectionComponent().getInputField().getInputIndex(), null);
            }
        }
        repaint();
    }


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
                Handler.updateInput(graph, e.getConnectionComponent().getLudemeNodeComponent().getLudemeNode(), e.getConnectionComponent().getInputField().getInputIndex(), null);
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
        for(int i = 0; i < node.getProvidedInputs().length; i++){
            if(node.getProvidedInputs()[i] != null){
                System.out.println(i + ": (" + node.getProvidedInputs()[i].getClass().getName() + ") " + node.getProvidedInputs()[i]);
            }
        }
    }

    @Override
    public void removeNode(LudemeNode node) {
        System.out.println("Removing node");
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
        connectLudemeWindow.updateList(selectedConnectionComponent.getRequiredLudemes());
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


}
