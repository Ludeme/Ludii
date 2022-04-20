package app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs;

import app.display.dialogs.visual_editor.view.components.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.ImmutablePoint;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LHeader;

import javax.swing.*;
import java.awt.*;

public class LIngoingConnectionComponent extends JComponent {

    private LHeader lHeader;
    private boolean fill;
    private final int RADIUS;
    private ConnectionPointComponent connectionPointComponent;
    private ImmutablePoint connectionPointPosition = new ImmutablePoint(0, 0);

    public LIngoingConnectionComponent(LHeader header, int height, int radius, boolean fill){
            this.lHeader = header;
            this.RADIUS = radius;
            this.fill = fill;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setPreferredSize(new Dimension(height, height));
            setSize(getPreferredSize());

            connectionPointComponent = new ConnectionPointComponent(radius, fill);

            connectionPointComponent.repaint();
            add(connectionPointComponent);
            setAlignmentX(Component.CENTER_ALIGNMENT);

            revalidate();
            repaint();
            setVisible(true);

        }

        public void updatePosition(){
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

        public LHeader getHeader(){
            return lHeader;
        }

        public boolean isFilled(){ return fill; }



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
}
