package app.display.dialogs.visual_editor.view.panels.editor;

/*
TODO:
        - Ludeme ingoing connection component not centered
        - Zoom in/out
        - connection by dragging
        - header background
        - when updating constructor: remove all connections
 */


import app.display.dialogs.visual_editor.LayoutManagement.LayoutManager.LayoutHandler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.model.grammar.parser.Parser;
import app.display.dialogs.visual_editor.view.components.AddLudemeWindow;
import app.display.dialogs.visual_editor.view.components.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenode.CustomPoint;
import app.display.dialogs.visual_editor.view.components.ludemenode.LudemeConnection;
import app.display.dialogs.visual_editor.view.components.ludemenode.block.LudemeBlock;
import app.display.dialogs.visual_editor.view.components.ludemenode.block.LudemeConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenode.interfaces.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LIngoingConnectionComponent;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class EditorPanel extends JPanel implements IGraphPanel {

    // graph of all ludeme nodes
    private DescriptionGraph graph = new DescriptionGraph();

    // read model.grammar
    Parser p = new Parser();
    List<Ludeme> ludemes = p.getLudemes();

    // window to add a new ludeme out of all possible ones
    private AddLudemeWindow addLudemeWindow = new AddLudemeWindow(ludemes, this, false);
    // window to add a new ludeme as an input
    private AddLudemeWindow connectLudemeWindow = new AddLudemeWindow(ludemes, this, true);

    // List of all edges, TODO:
    public List<LudemeConnection> list_edges = new ArrayList<>();

    public EditorPanel(int width, int height){
        setLayout(null);
        setBackground(DesignPalette.BACKGROUND_EDITOR);
        setPreferredSize(new Dimension(width, height));
        addMouseListener(new SpawnNodePanelListener());

        Ludeme game = findLudeme("game");
        LudemeNode gameNode = new LudemeNode(game, 0, 0);
        gameNode.setCurrentConstructor(game.getConstructors().get(1));
        LudemeNodeComponent gameBlock = new LudemeBlock(gameNode, this, 300);
        add(gameBlock);

        graph.addNode(gameNode);
        graph.setRoot(gameNode);



        // this listener continuously udpates the "currentMousePoint" variable
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                currentMousePoint = e.getPoint();
                if(selectedConnectionComponent != null){
                    repaint();
                }
            }
        });

        // this listener affects the linking of nodes.
        // if the user is currently establishing a link, and clicks BUTTON1: -> (a) Open Window with possible ludeme inputs, (b) Create only possible ludeme and link it
        //                                                          BUTTON3: -> Cancel linking
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == MouseEvent.BUTTON1) {
                    connectLudemeWindow.setVisible(false);
                    if(selectedConnectionComponent != null && selectedConnectionComponent.getRequiredLudemes() != null){
                        System.out.println(selectedConnectionComponent.getRequiredLudemes());
                        if(selectedConnectionComponent.getRequiredLudemes().size() == 1){
                            addLudemeNode(selectedConnectionComponent.getRequiredLudemes().get(0), e.getPoint(), true);
                        } else if(selectedConnectionComponent.getRequiredLudemes().size() > 1){
                            System.out.println("hmm");
                            connectLudemeWindow.updateList(selectedConnectionComponent.getRequiredLudemes());
                            //connectLudemeWindow = new AddLudemeWindow(selectedConnectionComponent.getRequiredLudemes(), EditorPanel.this, true);
                            connectLudemeWindow.setVisible(true);
                            connectLudemeWindow.setLocation(e.getX(), e.getY());
                            connectLudemeWindow.searchField.requestFocus();
                            System.out.println(connectLudemeWindow.getSize());
                            revalidate();
                            repaint();
                        }
                    }
                }
                if (e.getButton() == MouseEvent.BUTTON3) {
                    // cancels creation of new connection
                    connectLudemeWindow.setVisible(false);
                    connectNewConnection(null);
                }
            }
        });


        add(addLudemeWindow);
        add(connectLudemeWindow);

        revalidate();
        repaint();

    }


    /**
     * Finds a ludeme with a given name in the model.grammar
     * @param name Name of the ludeme
     * @return Ludeme object
     */
    private Ludeme findLudeme(String name){
        for(Ludeme l : ludemes){
            if(l.getName().equals(name)) return l;
        }
        return null;
    }


    /*
            METHODS TO CREATE & ADD A NEW LUDEME NODE
     */

    private LudemeNode createLudemeNode(Ludeme l){
        return new LudemeNode(l, 0, 0);
    }

    private LudemeBlock createLudemeBlock(LudemeNode ln){
        LudemeBlock lb = new LudemeBlock(ln, this, 300);
        return lb;
    }

    public LudemeBlock addLudemeNode(Ludeme l, Point location){
        LudemeNode ln = createLudemeNode(l);
        ln.setPos(location.x, location.y);
        LudemeBlock lb = createLudemeBlock(ln);
        graph.addNode(ln);
        add(lb);
        addLudemeWindow.setVisible(false);
        connectLudemeWindow.setVisible(false);

        revalidate();
        repaint();
        return lb;
    }

    public LudemeBlock addLudemeNode(Ludeme l, Point location, boolean connect){
        LudemeBlock lb = addLudemeNode(l, location);
        if(connect){
            connectNewConnection(lb.getIngoingConnectionComponent());
        }
        return lb;
    }

    /*
            END: METHODS TO CREATE & ADD A NEW LUDEME NODE
     */


    /*
            METHODS TO LINK
     */


    private Point currentMousePoint;
    private LudemeConnectionComponent selectedConnectionComponent;



    private void addConnection(CustomPoint p1, CustomPoint p2){
        list_edges.add(new LudemeConnection(p1, p2));
    }
    private void addConnection(LudemeConnectionComponent outgoingConnectionComponent, LudemeConnectionComponent ingoingConnectionComponent){
        CustomPoint ingoingPosition = ingoingConnectionComponent.getPosition();
        CustomPoint outgoingPosition = outgoingConnectionComponent.getPosition();
        addConnection(outgoingPosition, ingoingPosition);

        LudemeNode ln_in = ingoingConnectionComponent.getLudemeBlock().getLudemeNode();
        LudemeNode ln_out = outgoingConnectionComponent.getLudemeBlock().getLudemeNode();

        ln_in.setParent(ln_out);
        ln_out.addChildren(ln_in);

        ln_out.setProvidedInput(1, ln_in);

        outgoingConnectionComponent.getLudemeBlock().addedConnection(ln_in, outgoingConnectionComponent);

        ingoingConnectionComponent.fill();
        outgoingConnectionComponent.fill();

        selectedConnectionComponent = null;
        currentMousePoint = null;
        repaint();
    }



    public void connectNewConnection(LudemeConnectionComponent connectionComponent){
        if(selectedConnectionComponent != null && connectionComponent != null){
            if(selectedConnectionComponent.isOutgoing() && !connectionComponent.isOutgoing() && selectedConnectionComponent.getLudemeBlock() != connectionComponent.getLudemeBlock()){
                addConnection(selectedConnectionComponent, connectionComponent);
            }
            else {
                selectedConnectionComponent.unfill();
                this.selectedConnectionComponent = connectionComponent;
            }
        } else if (selectedConnectionComponent != null){
            selectedConnectionComponent.unfill();
            this.selectedConnectionComponent = connectionComponent;
        }
        else {
            this.selectedConnectionComponent = connectionComponent;
        }

    }

    public void ludemeBlockClicked(LudemeNodeComponent ludemeNode){
        if(selectedConnectionComponent != null && selectedConnectionComponent.isOutgoing() && ludemeNode != selectedConnectionComponent.getLudemeBlock()){
            LudemeConnectionComponent ingoingConnectionComponent = ((LudemeBlock) ludemeNode).getIngoingConnectionComponent();
            addConnection(selectedConnectionComponent, ingoingConnectionComponent);
        }
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // draw new connection
        if(selectedConnectionComponent != null && currentMousePoint != null) {
            paintNewConnection(g2, currentMousePoint);
        }

        // draw existing connections
        paintConnections(g2);

        repaint();
        revalidate();
    }

    private void paintConnections(Graphics2D g2){
        for(LudemeConnection e : list_edges){
            int cp_x = e.p1.x + Math.abs((e.p1.x-e.p2.x)/2);
            int cp1_y = e.p1.y;
            int cp2_y = e.p2.y;

            Path2D p2d = new Path2D.Double();
            p2d.moveTo(e.p1.x, e.p1.y);
            p2d.curveTo(cp_x, cp1_y, cp_x, cp2_y, e.p2.x, e.p2.y);
            g2.draw(p2d);
        }
    }

    private void paintNewConnection(Graphics2D g2, Point mousePosition){
        CustomPoint connection_point = selectedConnectionComponent.getPosition();
        Path2D p2d = new Path2D.Double();

        if(selectedConnectionComponent.isOutgoing()){
            int cp_x = connection_point.x + Math.abs((connection_point.x-mousePosition.x)/2);
            p2d.moveTo(connection_point.x, connection_point.y);
            p2d.curveTo(cp_x, connection_point.y, cp_x, mousePosition.y, mousePosition.x, mousePosition.y);
        }
        else {
            int cp_x = mousePosition.x + Math.abs((mousePosition.x-connection_point.x)/2);
            p2d.moveTo(mousePosition.x, mousePosition.y);
            p2d.curveTo(cp_x, mousePosition.y, cp_x, connection_point.y, connection_point.x, connection_point.y);
        }
        g2.draw(p2d);
    }

    @Override
    public void drawGraph(DescriptionGraph graph) {

    }

    @Override
    public DescriptionGraph getGraph() {
        return null;
    }

    @Override
    public void startNewConnection(LConnectionComponent source) {

    }

    @Override
    public void cancelNewConnection() {

    }


    @Override
    public void addConnection(LConnectionComponent source, LIngoingConnectionComponent target) {

    }

    @Override
    public app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent getNodeComponent(LudemeNode node) {
        return null;
    }

    @Override
    public LudemeNode addNode(Ludeme ludeme, int x, int y, boolean connect) {
        return addLudemeNode(ludeme, new Point(x,y), connect).getLudemeNode();
    }

    @Override
    public void showAllAvailableLudemes(int x, int y) {

    }

    @Override
    public void removeAllConnections(LudemeNode node) {

    }

    @Override
    public void removeConnection(LudemeNode node, LConnectionComponent connection) {

    }

    @Override
    public void clickedOnNode(LudemeNode node) {

    }

    @Override
    public void removeNode(LudemeNode node) {

    }

    @Override
    public LayoutHandler getLayoutHandler() {
        return null;
    }


    private class SpawnNodePanelListener extends MouseAdapter {

        private void openPopupMenu(MouseEvent e){
            JPopupMenu popupMenu = new EditorPopupMenu(EditorPanel.this);
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }

        public void mousePressed(MouseEvent e){
            if(e.getButton() == MouseEvent.BUTTON3){
                openPopupMenu(e);
            }
        }

        public void mouseReleased(MouseEvent e){
            if(e.getButton() == MouseEvent.BUTTON3){
                openPopupMenu(e);
            }
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON2) {

                addLudemeWindow.setVisible(true);
                addLudemeWindow.setLocation(e.getX(), e.getY());
                addLudemeWindow.searchField.requestFocus();

                revalidate();
                repaint();
            }
            else if (e.getButton() == MouseEvent.BUTTON3 || e.getButton() == MouseEvent.BUTTON1) {
                addLudemeWindow.setVisible(false);

                revalidate();
                repaint();
            }

        }
    }




}
