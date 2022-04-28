package app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs;

import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.view.components.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.ImmutablePoint;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

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

    private final int RADIUS;
    private boolean fill;
    private final LInputField INPUT_FIELD;
    private ConnectionPointComponent connectionPointComponent;
    private ImmutablePoint connectionPointPosition = new ImmutablePoint(0, 0);

    private LudemeNodeComponent connected_to;


    public LConnectionComponent(LInputField inputField, int height, int radius, boolean fill) {
        this.INPUT_FIELD = inputField;
        this.RADIUS = radius;
        this.fill = fill;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(height, height));
        setSize(getPreferredSize());

        connectionPointComponent = new ConnectionPointComponent(radius, fill);

        connectionPointComponent.repaint();
        add(connectionPointComponent);
        setAlignmentX(Component.CENTER_ALIGNMENT);

        addMouseListener(clickListener);

        connectionPointPosition.update(new Point(inputField.getLudemeNodeComponent().getWidth(), inputField.getPreferredSize().height*inputField.getInputIndex()));

        revalidate();
        repaint();
        setVisible(true);
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
                    graphPanel.startNewConnection(LConnectionComponent.this);
                }
                else{
                    // if already connected: remove connection
                    if(getConnectedTo() != null) {
                        graphPanel.removeConnection(LConnectionComponent.this.getLudemeNodeComponent().getLudemeNode(), LConnectionComponent.this);
                        setConnectedTo(null);
                    }
                    else {
                        // end drawing connection
                        setFill(!fill);
                        graphPanel.cancelNewConnection();
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
        public int radius;
        public boolean fill;
        public int x,y;

        public ConnectionPointComponent(int radius, boolean fill){
            this.radius = radius;
            this.fill = fill;
            setSize(radius*2,radius*2);
            revalidate();
            repaint();
            setVisible(true);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            // if fill = true, draw a filled circle. otherwise, the contour only
            if(fill) {
                g2.setColor(DesignPalette.LUDEME_CONNECTION_POINT);
                g2.fillOval(x, y, radius*2, radius*2);
            }
            else {
                // fill a new oval with transparent colour (to make the filled out oval disappear)
                g2.setColor(new Color(0,0,0,0));
                g2.fillOval(x, y, radius*2, radius*2);
                // draw unfilled oval
                g2.setColor(DesignPalette.LUDEME_CONNECTION_POINT);
                g2.drawOval(x, y, radius*2, radius*2);
            }
        }
    }

    public List<Ludeme> getRequiredLudemes(){
        return INPUT_FIELD.getRequiredLudemes();
    }


}
