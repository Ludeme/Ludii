package app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs;

import app.display.dialogs.visual_editor.view.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.ImmutablePoint;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import main.grammar.Symbol;

import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Represents an outgoing connection from a LudemeNodeComponent.
 * A circle which is displayed to the right of a LInputField
 * @author Filipp Dokienko
 */

public class LConnectionComponent extends JComponent
{
    /** InputField this ConnectionComponent is part of */
    private final LInputField INPUT_FIELD;
    /** ConnectionPointComponent of this ConnectionComponent (the graphic of the circle) */
    private final ConnectionPointComponent connectionPointComponent;
    /** Position of the ConnectionPointComponent */
    private ImmutablePoint connectionPointPosition = new ImmutablePoint(0, 0);
    /** The LudemeNodeComponent this ConnectionComponent is connected to */
    private LudemeNodeComponent connectedTo;
    /** Whether the node this ConnectionComponent is connected to is collapsed */
    private boolean connectionIsCollapsed = false;
    /** Whether this ConnectionComponent is filled (connected) */
    private boolean isFilled;

    /**
     * Constructor
     * @param inputField InputField this ConnectionComponent is part of
     * @param fill Whether the circle should be filled or not (is connected or not)
     */
    public LConnectionComponent(LInputField inputField, boolean fill)
    {
        this.INPUT_FIELD = inputField;
        this.isFilled = fill;
        this.connectionPointComponent = new ConnectionPointComponent(fill);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(INPUT_FIELD.label().getPreferredSize().height, INPUT_FIELD.label().getPreferredSize().height));
        setSize(getPreferredSize());

        connectionPointComponent.repaint();
        add(connectionPointComponent);
        setAlignmentX(Component.CENTER_ALIGNMENT);

        addMouseListener(clickListener);

        revalidate();
        repaint();
        setVisible(true);
    }

    /**
     * Listener to create a connection when the user clicks on the circle
     * or remove a connection when the user clicks on the circle again
     */
    private final MouseListener clickListener = new MouseAdapter()
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            super.mouseClicked(e);
            IGraphPanel graphPanel = lnc().graphPanel();
            if(e.getButton() == MouseEvent.BUTTON1)
            {
                if(!filled())
                {
                    // Start drawing connection
                    fill(!filled());
                    graphPanel.connectionHandler().startNewConnection(LConnectionComponent.this);
                }
                else
                {
                    /*if(connectionIsCollapsed)
                    {
                        connectedTo.setCollapsed(false);
                        connectedTo.setVisible(true);
                        graphPanel.repaint();
                        updatePosition();
                        return;
                    }*/
                    // if already connected: remove connection
                    if(connectedTo != null)
                    {
                        graphPanel.connectionHandler().removeConnection(LConnectionComponent.this.lnc().node(), LConnectionComponent.this);
                        setConnectedTo(null);
                    }
                    else
                    {
                        // end drawing connection
                        fill(!filled());
                        graphPanel.connectionHandler().cancelNewConnection();
                    }
                }
            }
        }
    };

    /**
     * Updates the position of the ConnectionPointComponent
     * Updates whether the ConnectionComponent is connected to a collapsed node or not
     */
    public void updatePosition()
    {
        // Update whether the node this ConnectionComponent is connected to is collapsed
        /*if(connectedTo != null)
        {
            if(connectedTo.node().collapsed() != connectionIsCollapsed)
            {
                connectionIsCollapsed = connectedTo.node().collapsed();
                connectionPointComponent.repaint();
                connectionPointComponent.revalidate();
            }
        }*/

        // Update the position of the ConnectionPointComponent
        if(this.getParent() == null || this.getParent().getParent() == null || this.getParent().getParent().getParent() == null) return;
        int x = connectionPointComponent.getX() + this.getX() + this.getParent().getX() + this.getParent().getParent().getX() + this.getParent().getParent().getParent().getX() + radius();
        int y = connectionPointComponent.getY() + this.getY() + this.getParent().getY() + this.getParent().getParent().getY() + this.getParent().getParent().getParent().getY() + radius();
        Point p = new Point(x,y);
        if(connectionPointPosition == null)
            connectionPointPosition = new ImmutablePoint(p);
        connectionPointPosition.update(p);
    }

    /**
     * Updates the position of the ConnectionPointComponent and returns it
     * @return the position of the ConnectionPointComponent
     */
    public ImmutablePoint connectionPointPosition()
    {
        updatePosition();
        return connectionPointPosition;
    }

    /**
     * Fills or unfills the ConnectionComponent and repaints it
     * @param fill Whether the circle should be filled or not (is connected or not)
     */
    public void fill(boolean fill)
    {
        this.isFilled = fill;
        connectionPointComponent.fill = fill;
        connectionPointComponent.repaint();
        connectionPointComponent.revalidate();
    }

    /**
     *
     * @return Whether the circle is filled or not (is connected or not)
     */
    public boolean filled()
    {
        return isFilled;
    }

    /**
     * Sets the LudemeNodeComponent this ConnectionComponent is connected to
     * @param connectedTo LudemeNodeComponent this ConnectionComponent is connected to
     */
    public void setConnectedTo(LudemeNodeComponent connectedTo)
    {
        this.connectedTo = connectedTo;
    }

    /**
     *
     * @return what LudemeNodeComponent this ConnectionComponent is connected to
     */
    public LudemeNodeComponent connectedTo()
    {
        return connectedTo;
    }

    /**
     *
     * @return the InputField this ConnectionComponent is part of
     */
    public LInputField inputField()
    {
        return INPUT_FIELD;
    }

    /**
     *
     * @return The LudemeNodeComponent this ConnectionComponent is part of
     */
    public LudemeNodeComponent lnc()
    {
        return inputField().inputArea().LNC();
    }

    public List<Symbol> possibleSymbolInputs()
    {
        return inputField().possibleSymbolInputs();
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        // TODO: need that?
    }

    /**
     *
     * @return whether the input field is optional
     */
    private boolean optional()
    {
        return INPUT_FIELD.optional();
    }

    /**
     *
     * @return the radius of the connection component
     */
    private int radius()
    {
        return (int)(INPUT_FIELD.label().getPreferredSize().height * 0.4 * (1.0/DesignPalette.SCALAR));
    }

    /**
     * The circle that is drawn on the ConnectionComponent
     */
    class ConnectionPointComponent extends JComponent
    {
        public boolean fill;
        public int x,y;

        public ConnectionPointComponent(boolean fill)
        {
            this.fill = fill;
            setSize(radius()*2,radius()*2);
            revalidate();
            repaint();
            setVisible(true);
        }

        @Override
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // if fill = true, draw a filled circle. otherwise, the contour only
            if(fill)
            {
                g2.setColor(DesignPalette.LUDEME_CONNECTION_POINT);
                g2.fillOval(x, y, radius()*2, radius()*2);
            }
            else
            {
                if(!optional() && !LConnectionComponent.this.inputField().isHybrid())
                {
                    g2.setColor(DesignPalette.LUDEME_CONNECTION_POINT_INACTIVE);
                }
                else
                {
                    g2.setColor(DesignPalette.LUDEME_CONNECTION_POINT);
                }
                g2.fillOval(x, y, radius()*2, radius()*2);
                // make white hole to create stroke effect
                g2.setColor(DesignPalette.BACKGROUND_LUDEME_BODY);
                g2.fillOval(x+radius()/2, y+radius()/2, radius(), radius());

            }
        }
    }

}
