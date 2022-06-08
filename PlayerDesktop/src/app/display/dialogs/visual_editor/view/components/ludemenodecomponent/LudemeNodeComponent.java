package app.display.dialogs.visual_editor.view.components.ludemenodecomponent;


import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LIngoingConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LInputArea;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import main.grammar.Clause;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

public class LudemeNodeComponent extends JPanel {

    protected int x, y; // TODO: figure out this nonsense
    private ImmutablePoint position = new ImmutablePoint(x, y);
    private final LudemeNode LUDEME_NODE;
    private final IGraphPanel GRAPH_PANEL;

    public boolean dynamic;

    private LHeader header;
    private LInputArea inputArea;

    private boolean MOVE_DESCENDANTS = false;

    public LudemeNodeComponent(LudemeNode ludemeNode, IGraphPanel graphPanel){
        this.LUDEME_NODE = ludemeNode;
        this.GRAPH_PANEL = graphPanel;

        this.x = (int) ludemeNode.getPos().getX();
        this.y = (int) ludemeNode.getPos().getY();

        setLayout(new BorderLayout());

        System.out.println("[LNC] Provided Inputs");
        for(int i = 0; i < ludemeNode.providedInputs().length; i++){
            if(ludemeNode.providedInputs()[i] != null)
            System.out.println("["+i+", " +  ludemeNode.providedInputs()[i].getClass().getName() + "] " + ludemeNode.providedInputs()[i]);
            else System.out.println("["+i+", null]");
        }

        this.dynamic = ludemeNode.isDynamic();

        // LNC cannot be dynamic if its a terminal node or all constructors have size one
        if(!ludemeNode.canBeDynamic()) dynamic = false;


        // sync dynamic boolean with model node
        ludemeNode.setDynamic(dynamic); // TODO: Shouldnt be done here

        header = new LHeader(this);
        inputArea = new LInputArea(this);

        add(header, BorderLayout.NORTH);
        add(inputArea, BorderLayout.CENTER);

        setLocation(x,y);

        int preferredHeight = inputArea.getPreferredSize().height + header.getPreferredSize().height;

        setMinimumSize(new Dimension(getWidth(), preferredHeight));
        setPreferredSize(new Dimension(getMinimumSize().width, preferredHeight));
        setSize(new Dimension(getMinimumSize().width, preferredHeight));

        setSize(getPreferredSize());
        ludemeNode.setWidth(getWidth());
        ludemeNode.setHeight(getHeight());

        addMouseMotionListener(dragListener);
        addMouseListener(mouseListener);

        updatePositions();

        revalidate();
        repaint();
        setVisible(true);

    }


    public void changeClause(Clause clause){
        Handler.updateCurrentClause(getGraphPanel().getGraph(), node(), clause);
        inputArea.updateConstructor(); // TODO: update input area
        revalidate();
        repaint();

    }


    public void updatePositions() {
        if(inputArea == null || header == null) {
            return;
        }
        position.update(getLocation());
        inputArea.updatePosition();
        header.updatePosition();
    }

    public void updateLudemePosition()
    {
        LudemeNodeComponent.this.setLocation((int)LUDEME_NODE.getPos().getX(), (int)LUDEME_NODE.getPos().getY());
    }

    /**
     * Method which syncs the Ludeme Node Component with provided inputs (stored in the Ludeme Node).
     * Called when drawing a graph.
     */
    public void updateProvidedInputs(){
        inputArea.updateProvidedInputs();
    }

    public void updateComponent(){
        if(inputArea == null) {
            return;
        }

        int preferredHeight = inputArea.getPreferredSize().height + header.getPreferredSize().height;

        setPreferredSize(new Dimension(getMinimumSize().width, preferredHeight));
        setSize(getPreferredSize());

        repaint();
        revalidate();
    }

    public LudemeNode node(){
        return LUDEME_NODE;
    }

    public LInputArea getInputArea(){
        return inputArea;
    }

    public int getWidth(){
        return DesignPalette.NODE_WIDTH;
    }

    public IGraphPanel getGraphPanel(){
        return GRAPH_PANEL;
    }

    public ImmutablePoint getPosition(){
        return position;
    }

    public LIngoingConnectionComponent getIngoingConnectionComponent(){
        return header.getIngoingConnectionComponent();
    }

    public void changeDynamic(){
        dynamic = !dynamic;
        getInputArea().setDynamic(dynamic);
        node().setDynamic(dynamic);
    }

    private void dragNode(LudemeNodeComponent lnc, MouseEvent e, Point dif1)
    {
        e.translatePoint(e.getComponent().getLocation().x - lnc.x, e.getComponent().getLocation().y - lnc.y);
        Point dif2 = new Point(dif1.x - LudemeNodeComponent.this.x, dif1.y - LudemeNodeComponent.this.y);
        lnc.setLocation(lnc.x+dif2.x, lnc.y+dif2.y);
    }

    // Drag Listener
    // TODO: Implement moving node subtree + movement of selected nodes
    MouseMotionListener dragListener = new MouseAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);

            if (!MOVE_DESCENDANTS)
            {
                LudemeNodeComponent.this.setLocation(e.getX()+e.getComponent().getLocation().x - LudemeNodeComponent.this.x,
                        e.getY()+e.getComponent().getLocation().y - LudemeNodeComponent.this.y);
                updatePositions();
            }
            else
            {
                int initX = position.x;
                int initY = position.y;

                e.translatePoint(e.getComponent().getLocation().x - LudemeNodeComponent.this.x, e.getComponent().getLocation().y - LudemeNodeComponent.this.y);
                LudemeNodeComponent.this.setLocation(e.getX(), e.getY());
                updatePositions();
                Point posDif = new Point(position.x-initX, position.y-initY);

                // iterate children in BFS
                java.util.List<LudemeNodeComponent> Q = new ArrayList<>();
                Q.add(LudemeNodeComponent.this);
                while (!Q.isEmpty())
                {
                    LudemeNodeComponent lnc = Q.remove(0);
                    if (!lnc.equals(LudemeNodeComponent.this)) lnc.setLocation(lnc.getLocation().x+posDif.x, lnc.getLocation().y+posDif.y);
                    lnc.updatePositions();

                    List<Integer> children = lnc.LUDEME_NODE.getChildren();
                    children.forEach(v -> {
                        Q.add(GRAPH_PANEL.getNodeComponent(GRAPH_PANEL.getGraph().getNode(v)));
                    });
                }
            }
            updatePositions();
            getGraphPanel().repaint();
        }
    };

    // Mouse Listener
    MouseListener mouseListener = new MouseAdapter() {

        private void openPopupMenu(MouseEvent e){
            JPopupMenu popupMenu = new NodePopupMenu(LudemeNodeComponent.this, LudemeNodeComponent.this.getGraphPanel());
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
            super.mouseClicked(e);
            if (e.getClickCount() == 1)
            {
                MOVE_DESCENDANTS = false;
            }
            else
            {
                System.out.println("TRUE");
                MOVE_DESCENDANTS = true;
            }
        }

        //TODO: do we need mousePressed listener?

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            LudemeNodeComponent.this.x = e.getX();
            LudemeNodeComponent.this.y = e.getY();
            Handler.updatePosition(getGraphPanel().getGraph(), node(), getX(), getY());

            if(e.getButton() == MouseEvent.BUTTON3){
                openPopupMenu(e);
                getGraphPanel().cancelNewConnection();
            }
            else {
                getGraphPanel().clickedOnNode(LudemeNodeComponent.this);
            }

        }

        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
            LudemeNodeComponent.this.x = e.getX();
            LudemeNodeComponent.this.y = e.getY();
            Handler.updatePosition(getGraphPanel().getGraph(), node(), getX(), getY());

            if(e.getButton() == MouseEvent.BUTTON3){
                openPopupMenu(e);
                getGraphPanel().cancelNewConnection();
            }
            else {
                getGraphPanel().clickedOnNode(LudemeNodeComponent.this);
            }
        }
    };

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int preferredHeight = inputArea.getPreferredSize().height + header.getPreferredSize().height;

        setMinimumSize(new Dimension(getWidth(), preferredHeight));
        setPreferredSize(new Dimension(getMinimumSize().width, preferredHeight));
        setSize(new Dimension(getMinimumSize().width, preferredHeight));

        setSize(getPreferredSize());
        LUDEME_NODE.setWidth(getWidth());
        LUDEME_NODE.setHeight(getHeight());

        setBackground(DesignPalette.BACKGROUND_LUDEME_BODY);
        setBorder(DesignPalette.LUDEME_NODE_BORDER);
    }

}
