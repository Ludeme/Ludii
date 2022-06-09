package app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs;

import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.view.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.ImmutablePoint;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import main.grammar.Symbol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

/**
 * Represents an outgoing connection from a LudemeNodeComponent.
 * A circle which is displayed to the right of a LInputField
 */

public class LConnectionComponent extends JComponent {

    private int RADIUS;
    private int height;
    private boolean fill;
    private final LInputField INPUT_FIELD;
    private ConnectionPointComponent connectionPointComponent;
    private ImmutablePoint connectionPointPosition = new ImmutablePoint(0, 0);

    private LudemeNodeComponent connected_to;
    boolean isOptional = false;


    public LConnectionComponent(LInputField inputField, int height, int radius, boolean fill) {
        this.INPUT_FIELD = inputField;
        height = INPUT_FIELD.label.getPreferredSize().height;
        RADIUS = (int) (INPUT_FIELD.label.getPreferredSize().height * 0.4);
        this.fill = fill;
        this.isOptional = INPUT_FIELD.isOptional;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(height, height));
        setSize(getPreferredSize());

        connectionPointComponent = new ConnectionPointComponent(fill);

        connectionPointComponent.repaint();
        add(connectionPointComponent);
        setAlignmentX(Component.CENTER_ALIGNMENT);

        addMouseListener(clickListener);

        connectionPointPosition.update(new Point(inputField.getLudemeNodeComponent().getWidth(), inputField.getPreferredSize().height*inputField.getInputIndex()));

        revalidate();
        repaint();
        setVisible(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        height = INPUT_FIELD.label.getPreferredSize().height;
        RADIUS = (int) (INPUT_FIELD.label.getPreferredSize().height * 0.4);
    }

    public void updatePosition(){
        if(this.getParent() == null || this.getParent().getParent() == null || this.getParent().getParent().getParent() == null) return;
        int x = connectionPointComponent.getX() + this.getX() + this.getParent().getX() + this.getParent().getParent().getX() + this.getParent().getParent().getParent().getX() + RADIUS;
        int y = connectionPointComponent.getY() + this.getY() + this.getParent().getY() + this.getParent().getParent().getY() + this.getParent().getParent().getParent().getY() + RADIUS;
        Point p = new Point(x,y);
        if(connectionPointPosition == null){
            connectionPointPosition = new ImmutablePoint(p);
        }
        connectionPointPosition.update(p);
    }

    public ImmutablePoint getConnectionPointPosition(){
        updatePosition();
        return connectionPointPosition;
    }

    public void setFill(boolean fill){
        this.fill = fill;
        connectionPointComponent.fill = fill;
        connectionPointComponent.repaint();
        connectionPointComponent.revalidate();
    }

    MouseListener clickListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            IGraphPanel graphPanel = INPUT_FIELD.getLudemeNodeComponent().getGraphPanel();
            if(e.getButton() == MouseEvent.BUTTON1){
                if(!fill){
                    // Start drawing connection
                    setFill(!fill);
                    graphPanel.getCh().startNewConnection(LConnectionComponent.this);
                }
                else{
                    // if already connected: remove connection
                    if(getConnectedTo() != null) {
                        graphPanel.getCh().removeConnection(LConnectionComponent.this.getLudemeNodeComponent().node(), LConnectionComponent.this);
                        setConnectedTo(null);
                    }
                    else {
                        // end drawing connection
                        setFill(!fill);
                        graphPanel.getCh().cancelNewConnection();
                    }
                }
            }
        }
    };

    public void setConnectedTo(LudemeNodeComponent connectedTo){
        this.connected_to = connectedTo;
    }

    public LudemeNodeComponent getConnectedTo(){
        return connected_to;
    }

    public LudemeNodeComponent getLudemeNodeComponent(){
        return INPUT_FIELD.getLudemeNodeComponent();
    }

    public LInputField getInputField(){
        return INPUT_FIELD;
    }

    class ConnectionPointComponent extends JComponent{
        public boolean fill;
        public int x,y;

        public ConnectionPointComponent(boolean fill){
            this.fill = fill;
            setSize(getRadius()*2,getRadius()*2);
            revalidate();
            repaint();
            setVisible(true);
        }

        private int getRadius(){
            return RADIUS;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // if fill = true, draw a filled circle. otherwise, the contour only
            if(fill) {
                g2.setColor(DesignPalette.LUDEME_CONNECTION_POINT);
                g2.fillOval(x, y, getRadius()*2, getRadius()*2);
            }
            else {
                // fill a new oval with transparent colour (to make the filled out oval disappear)
                /*g2.setColor(new Color(0,0,0,0));
                g2.fillOval(x, y, radius*2, radius*2);
                // draw unfilled oval
                g2.setColor(DesignPalette.LUDEME_CONNECTION_POINT);
                g2.drawOval(x, y, radius*2, radius*2);*/

                if(!isOptional) {
                    g2.setColor(DesignPalette.LUDEME_CONNECTION_POINT_INACTIVE);
                } else {
                    g2.setColor(DesignPalette.LUDEME_CONNECTION_POINT);
                }
                g2.fillOval(x, y, getRadius()*2, getRadius()*2);
                // make white hole to create stroke effect
                g2.setColor(DesignPalette.BACKGROUND_LUDEME_BODY);
                g2.fillOval(x+getRadius()/2, y+getRadius()/2, getRadius(), getRadius());

            }
        }
    }


    public List<Symbol> getRequiredSymbols(){
        return INPUT_FIELD.getRequiredSymbols();
        //return INPUT_FIELD.getRequiredLudemes(); TODO
    }


}
