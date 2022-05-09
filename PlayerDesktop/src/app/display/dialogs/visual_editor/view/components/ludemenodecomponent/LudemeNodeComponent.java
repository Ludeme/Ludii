package app.display.dialogs.visual_editor.view.components.ludemenodecomponent;


import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.grammar.Constructor;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LIngoingConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LInputArea;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class LudemeNodeComponent extends JComponent {

    protected int x, y;
    private ImmutablePoint position = new ImmutablePoint(x, y);
    int width;
    private final LudemeNode LUDEME_NODE;
    private final IGraphPanel GRAPH_PANEL;

    public boolean dynamic = true;

    private LHeader header;
    private LInputArea inputArea;

    public LudemeNodeComponent(LudemeNode ludemeNode, int width, IGraphPanel graphPanel){
        this.LUDEME_NODE = ludemeNode;
        this.GRAPH_PANEL = graphPanel;

        this.x = (int) ludemeNode.getPos().getX();
        this.y = (int) ludemeNode.getPos().getY();
        this.width = width;

        setLayout(new BorderLayout());

        // LNC cannot be dynamic if its a terminal node or all constructors have size one
        if(ludemeNode.getLudeme().getConstructors().size() == 1){
            dynamic = false;
        }
        else if (dynamic){
            dynamic = false;
            for(Constructor c : ludemeNode.getLudeme().getConstructors()){
                if(c.getInputs().size() > 1){
                    dynamic = true;
                }
            }
        }

        ludemeNode.setDynamic(dynamic); // TODO: Shouldnt be done here

        header = new LHeader(this);
        inputArea = new LInputArea(this);
        add(header, BorderLayout.NORTH);
        add(inputArea, BorderLayout.CENTER);

        setLocation(x,y);

        int preferredHeight = inputArea.getPreferredSize().height + header.getPreferredSize().height;

        setMinimumSize(new Dimension(width, preferredHeight));
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

    public void changeConstructor(Constructor c){
        Handler.updateCurrentConstructor(getGraphPanel().getGraph(), getLudemeNode(), c);

        inputArea.updateConstructor();

        revalidate();
        repaint();

        int preferredHeight = inputArea.getPreferredSize().height + header.getPreferredSize().height;

        setMinimumSize(new Dimension(width, preferredHeight));
        setPreferredSize(new Dimension(getMinimumSize().width, preferredHeight));
        setSize(new Dimension(getMinimumSize().width, preferredHeight));

        setSize(getPreferredSize());
        LUDEME_NODE.setWidth(getWidth());
        LUDEME_NODE.setHeight(getHeight());

        revalidate();
        repaint();
    }

    public void updatePositions() {
        if(inputArea == null || header == null) return;
        position.update(getLocation());
        inputArea.updatePosition();
        header.updatePosition();
    }

    public void updateLudemePosition()
    {
        LudemeNodeComponent.this.setLocation((int)LUDEME_NODE.getPos().getX(), (int)LUDEME_NODE.getPos().getY());
    }

    public void updateProvidedInputs(){
        inputArea.updateProvidedInputs();
    }

    public void updateComponent(){
        if(inputArea == null) return;
        int preferredHeight = inputArea.getPreferredSize().height + header.getPreferredSize().height;

        setPreferredSize(new Dimension(getMinimumSize().width, preferredHeight));
        setSize(getPreferredSize());

        repaint();
        revalidate();
    }

    public LudemeNode getLudemeNode(){
        return LUDEME_NODE;
    }

    public LInputArea getInputArea(){
        return inputArea;
    }

    public int getWidth(){
        return width;
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
        getLudemeNode().setDynamic(dynamic);
    }

    // Drag Listener

    MouseMotionListener dragListener = new MouseAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            e.translatePoint(e.getComponent().getLocation().x - LudemeNodeComponent.this.x, e.getComponent().getLocation().y -LudemeNodeComponent.this.y);
            LudemeNodeComponent.this.setLocation(e.getX(),e.getY());
            // TODO: fix positions
            // System.out.println(e.getX() + " " + e.getY());
            updatePositions();
        }
    };

    // Mouse Listener
    MouseListener mouseListener = new MouseAdapter() {

        private void openPopupMenu(MouseEvent e){
            JPopupMenu popupMenu = new NodePopupMenu(LudemeNodeComponent.this, LudemeNodeComponent.this.getGraphPanel());
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }

        //TODO: do we need mousePressed listener?

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            LudemeNodeComponent.this.x = e.getX();
            LudemeNodeComponent.this.y = e.getY();
            Handler.updatePosition(getGraphPanel().getGraph(), getLudemeNode(), getX(), getY());

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
            Handler.updatePosition(getGraphPanel().getGraph(), getLudemeNode(), getX(), getY());

            if(e.getButton() == MouseEvent.BUTTON3){
                openPopupMenu(e);
                getGraphPanel().cancelNewConnection();
            }
            else {
                getGraphPanel().clickedOnNode(LudemeNodeComponent.this);
            }
        }
    };

}
