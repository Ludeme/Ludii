package app.display.dialogs.visual_editor.view.panels.editor;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.ImmutablePoint;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeConnection;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LIngoingConnectionComponent;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles connections between LudemeNodeComponents on a iGraphPanel
 */

public class ConnectionHandler
{

    /** The graph panel */
    private final IGraphPanel graphPanel;
    /** Stores the Edges */
    private final List<LudemeConnection> edges;
    /** The currently selected LudemeNodeComponent's Connection Component */
    private LConnectionComponent selectedConnectionComponent;

    public ConnectionHandler(IGraphPanel graphPanel)
    {
        this.graphPanel = graphPanel;
        this.edges = new ArrayList<>();
    }

    /**
     * Starts a new connection
     * @param source the LudemeNodeComponent to connect from
     */
    public void startNewConnection(LConnectionComponent source)
    {
        // if a previous connection component was selected, deselect it
        cancelNewConnection();
        // set the selected connection component
        selectedConnectionComponent = source;
        if(Handler.autoplacement)
            ((GraphPanel)graphPanel).showCurrentlyAvailableLudemes();
    }

    /**
     * Cancels the currently created connection
     */
    public void cancelNewConnection()
    {
        if(selectedConnectionComponent != null)
        {
            selectedConnectionComponent.fill(false);
            selectedConnectionComponent = null;
        }
    }

    /**
     * Finalises the currently created connection
     * @param target the LudemeNodeComponent to connect to
     */
    public void finishNewConnection(LudemeNodeComponent target)
    {
        Handler.addEdge(graphPanel.graph(), selectedConnectionComponent.inputField().inputArea().LNC().node(), target.node(), selectedConnectionComponent.inputField().inputArea().inputFieldIndex(selectedConnectionComponent.inputField()));
        selectedConnectionComponent = null;
    }

    /**
     * Creates a connection between two Connection Components
     * @param source the LudemeNodeComponent's Connection Component to connect from
     * @param target the LudemeNodeComponent's Connection Component to connect to
     */
    public void addConnection(LConnectionComponent source, LIngoingConnectionComponent target)
    {
        // update the positions of the connection components
        source.updatePosition();
        target.updatePosition();

        // If the InputField was merged, notify the InputArea to remove the NodeArgument from the merged list
        if(source.inputField().isMerged())
        {
            source.fill(false);
            source = source.lnc().inputArea().addedConnection(target.getHeader().ludemeNodeComponent(), source.inputField()).connectionComponent();
        }
        // Otherwise notify the InputArea about the added connection
        else
        {
            source.lnc().inputArea().addedConnection(target.getHeader().ludemeNodeComponent(), source.inputField());
        }

        // update creator argument of the target node
        target.getHeader().ludemeNodeComponent().node().setCreatorArgument(source.inputField().nodeArgument(0));

        // update the positions of the source connection component again (for the case that the InputField was merged)
        source.updatePosition();

        // fill the connection components
        source.fill(true);
        target.setFill(true);

        // update the connection in the source
        source.setConnectedTo(target.getHeader().ludemeNodeComponent());
        target.setInputField(source.inputField());

        // Add an edge
        LudemeConnection connection = new LudemeConnection(source, target);
        edges.add(connection);

        // Update the provided input via the Handler
        // differentiate between an input provided to a collection and otherwise
        if(source.inputField().nodeArgument(0).collection())
            Handler.updateCollectionInput(graphPanel.graph(), source.lnc().node(), source.inputField().nodeArgument(0), target.getHeader().ludemeNodeComponent().node(), source.inputField().elementIndex());
        else
            Handler.updateInput(graphPanel.graph(), source.lnc().node(), source.inputField().nodeArgument(0), target.getHeader().ludemeNodeComponent().node());
        
        graphPanel.repaint();
    }

    /**
     * Removes all connections of a node
     * @param node
     */
    public void removeAllConnections(LudemeNode node)
    {
        removeAllConnections(node, true);
    }

    /**
     * Removes all connections of a node
     * @param node
     * @param onlyOutgoingConnections Whether only outgoing connections of the given node should be removed
     */
    public void removeAllConnections(LudemeNode node, boolean onlyOutgoingConnections)
    {

        for(LudemeConnection e : new ArrayList<>(edges))
        {
            if(e.outgoingNode() != node && !(!onlyOutgoingConnections && e.ingoingNode() == node))
            {
                continue;
            }

            edges.remove(e);

            LudemeNodeComponent source = e.getConnectionComponent().inputField().inputArea().LNC();
            source.inputArea().removedConnection(e.getConnectionComponent().inputField());

            e.getIngoingConnectionComponent().setFill(false); // the node source was connected to is not connected anymore
            e.getConnectionComponent().fill(false); // the node source is not connected anymore

            // notify handler (different for collection and non-collection inputs)
            if(e.getConnectionComponent().inputField().nodeArgument(0).collection())
                Handler.updateCollectionInput(graphPanel.graph(), source.node(), e.getConnectionComponent().inputField().nodeArgument(0), null, e.getConnectionComponent().inputField().elementIndex());
            else
                Handler.updateInput(graphPanel.graph(), source.node(), e.getConnectionComponent().inputField().nodeArgument(0), null);
        }

        graphPanel.repaint();
    }

    /**
     * removes all outgoing connections of a given node's connection component
     * @param node
     * @param connection
     */
    public void removeConnection(LudemeNode node, LConnectionComponent connection)
    {
        for(LudemeConnection e : new ArrayList<>(edges))
        {
            if(e.getConnectionComponent().equals(connection))
            {
                edges.remove(e);
                if(e.ingoingNode().creatorArgument() != null && e.ingoingNode().creatorArgument().collection())
                {
                    // find index in collection
                    int elementIndex = -1;
                    for(Object input : e.outgoingNode().providedInputsMap().values())
                    {
                        if(!(input instanceof Object[])) continue;
                        Object[] currentCollection = (Object[]) input;
                        for(int i = 0; i < currentCollection.length; i++)
                        {
                            if(currentCollection[i] == e.ingoingNode())
                            {
                                elementIndex = i;
                                break;
                            }
                        }
                    }
                    Handler.removeEdge(graphPanel.graph(), node, e.ingoingNode(), elementIndex, false);
                }
                else
                    Handler.removeEdge(graphPanel.graph(), node, e.ingoingNode(), false);

                e.getIngoingConnectionComponent().setFill(false); // header
                e.getConnectionComponent().fill(false); // input
                e.getConnectionComponent().setConnectedTo(null);
                e.getConnectionComponent().inputField().inputArea().removedConnection(e.getConnectionComponent().inputField());

                if(connection.inputField().nodeArgument(0).collection())
                {
                        LudemeNode sourceNode = connection.lnc().node();
                        int collectionElementIndex = 0;
                        if(connection.inputField().parent() != null)
                            collectionElementIndex = connection.inputField().parent().children().indexOf(connection.inputField())+1;
                        Handler.updateCollectionInput(graphPanel.graph(), sourceNode, connection.inputField().nodeArgument(0), null, collectionElementIndex);
                        connection.inputField().updateUserInputs();
                }
                else
                {
                    Handler.updateInput(graphPanel.graph(), e.getConnectionComponent().lnc().node(), e.getConnectionComponent().inputField().nodeArgument(0), null);
                }
            }
        }
        graphPanel.repaint();
    }

    /**
     * Draws an edge between an argument field and the mouse pointer (while establishing a connection)
     * @param g2
     * @param mousePosition
     */
    public void drawNewConnection(Graphics2D g2, Point mousePosition)
    {
        if(selectedConnectionComponent != null && mousePosition != null)
        {
            ImmutablePoint connection_point = selectedConnectionComponent.connectionPointPosition();
            Path2D p2d = new Path2D.Double();

            int cp_x = connection_point.x + Math.abs((connection_point.x-mousePosition.x)/2);
            p2d.moveTo(connection_point.x, connection_point.y);
            p2d.curveTo(cp_x, connection_point.y, cp_x, mousePosition.y, mousePosition.x, mousePosition.y);
            g2.draw(p2d);
        }
    }

    /**
     *
     * @return The currently selected LConnectionComponent
     */
    public LConnectionComponent selectedComponent()
    {
        return selectedConnectionComponent;
    }

    /**
     * Paints the edges
     * @param g2
     */
    public void paintConnections(Graphics2D g2)
    {
        // set color for edges
        g2.setColor(Handler.currentPalette().LUDEME_CONNECTION_EDGE());
        // set stroke for edges
        g2.setStroke(DesignPalette.LUDEME_EDGE_STROKE);

        for(LudemeConnection e : edges)
        {
            if(!(e.outgoingNode().visible() && e.ingoingNode().visible()))
            {
                continue;
            }

            ImmutablePoint inputPoint = e.getInputPosition();
            ImmutablePoint targetPoint = e.getTargetPosition();

            int cp_x = inputPoint.x + Math.abs((inputPoint.x-targetPoint.x)/2);
            int cp1_y = inputPoint.y;
            int cp2_y = targetPoint.y;

            Path2D p2d = new Path2D.Double();
            p2d.moveTo(inputPoint.x, inputPoint.y);
            p2d.curveTo(cp_x, cp1_y, cp_x, cp2_y, targetPoint.x, targetPoint.y);
            g2.draw(p2d);
        }
    }

}
