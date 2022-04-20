package app.display.dialogs.visual_editor.view.components.ludemenode.block;

import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.view.components.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenode.CustomPoint;
import app.display.dialogs.visual_editor.view.panels.editor.EditorPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class LudemeConnectionComponent extends JComponent {
    private final boolean OUTGOING;
    private CustomPoint position = new CustomPoint(0,0);
    private ConnectionPointComponent connectionPointComponent;
    private final LudemeBlock LUDEME_BLOCK;

    private final List<Ludeme> REQUIRED_LUDEMES; // if ingoing connection = null

    private final int WIDTH,HEIGHT;

    private final int RADIUS;

    public LudemeConnectionComponent(LudemeBlock ludemeBlock, List<Ludeme> requiredLudemes, int width, int height, int radius, boolean outgoing){
        this.LUDEME_BLOCK = ludemeBlock;
        this.REQUIRED_LUDEMES = requiredLudemes;
        this.OUTGOING = outgoing;
        this.RADIUS = radius;
        this.WIDTH = width;
        this.HEIGHT = height;

        setPreferredSize(new Dimension(WIDTH,HEIGHT));
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setSize(getPreferredSize());
        setLocation(0,0);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        connectionPointComponent = new ConnectionPointComponent();
        connectionPointComponent.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(connectionPointComponent);

        repaint();
        revalidate();
        setVisible(true);

    }


    class ConnectionPointComponent extends JComponent{
        public boolean fill = false;
        public int x,y;

        public ConnectionPointComponent(){
            setClickListener();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            x = (RADIUS/2);
            y = (RADIUS/2);

            // if fill = true, draw a filled circle. otherwise, the contour only
            if(fill) {
                g2.setColor(DesignPalette.LUDEME_CONNECTION_POINT);
                g2.fillOval(x, y, LudemeConnectionComponent.this.RADIUS, LudemeConnectionComponent.this.RADIUS);
            }
            else {
                // fill a new oval with transparent colour (to make the filled out oval disappear)
                g2.setColor(new Color(0,0,0,0));
                g2.fillOval(x, y, LudemeConnectionComponent.this.RADIUS, LudemeConnectionComponent.this.RADIUS);
                // draw unfilled oval
                g2.setColor(DesignPalette.LUDEME_CONNECTION_POINT);
                g2.drawOval(x, y, LudemeConnectionComponent.this.RADIUS, LudemeConnectionComponent.this.RADIUS);
            }
        }

        // if circle is clicked, fill/unfill circle
        private void setClickListener(){
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);

                    if(fill){
                        // TODO: ? remove connection?
                    } else {
                        EditorPanel editor_panel = LudemeConnectionComponent.this.LUDEME_BLOCK.EDITOR_PANEL;
                        fill = true;
                        repaint();
                        // editor_panel: new selected point
                        editor_panel.connectNewConnection(LudemeConnectionComponent.this);
                    }
                }
            });

        }
    }

    public boolean isFilled(){
        return connectionPointComponent.fill;
    }

    public void fill(){
        if(!isFilled()){
            connectionPointComponent.fill = true;
            connectionPointComponent.repaint();
            connectionPointComponent.revalidate();
        }
    }

    public void unfill(){
        if(isFilled()){
            connectionPointComponent.fill = false;
            connectionPointComponent.repaint();
            connectionPointComponent.revalidate();
        }
    }

    public void updatePosition(){
        int x = connectionPointComponent.getX() + this.getX() + this.getParent().getX() + this.getParent().getParent().getX() + RADIUS;// + this.getParent().getParent().getParent().getX() + this.getParent().getParent().getParent().getParent().getX();
        int y = connectionPointComponent.getY() + this.getY() + this.getParent().getY() + this.getParent().getParent().getY() + RADIUS;// + this.getParent().getParent().getParent().getY() + this.getParent().getParent().getParent().getParent().getY();

        Point p = new Point(x,y);
        if(position == null){
            position = new CustomPoint(p);
        }
        position.update(p);
    }

    public CustomPoint getPosition(){
        updatePosition();
        return position;
    }

    public boolean isOutgoing(){
        return OUTGOING;
    }

    public LudemeBlock getLudemeBlock(){
        return LUDEME_BLOCK;
    }

    public List<Ludeme> getRequiredLudemes(){
        return REQUIRED_LUDEMES;
    }

}
