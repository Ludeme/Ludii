package app.display.dialogs.visual_editor.view.panels.editor;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.view.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.ImmutablePoint;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeConnection;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LIngoingConnectionComponent;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import java.util.List;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;

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

    private static final boolean DEBUG = true;

    public ConnectionHandler(IGraphPanel graphPanel)
    {
        this.graphPanel = graphPanel;
        this.edges = new ArrayList<>();
    }

    /**
     * Paints the edges
     * @param g2
     */
    public void paintConnections(Graphics2D g2)
    {
        // set color for edges
        g2.setColor(DesignPalette.LUDEME_CONNECTION_EDGE);
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

    /**
     * Starts a new connection
     * @param source the LudemeNodeComponent to connect from
     */
    public void startNewConnection(LConnectionComponent source)
    {
        if(DEBUG) System.out.println("[EP] Start connection: " + source.connectionPointPosition() + " , " + source.possibleSymbolInputs());
        // if a previous connection component was selected, deselect it
        cancelNewConnection();
        // set the selected connection component
        selectedConnectionComponent = source;
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
     * Ends the currently created connection
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

        if(DEBUG) System.out.println("adding connection from " + source.inputField() + " to " + target.getHeader().title().getText());

        // update the positions of the connection components
        source.updatePosition();
        target.updatePosition();

        // If the InputField was merged, notify the InputArea to remove the NodeArgument from the merged list
        if(source.inputField().isMerged())
        {
            source.fill(false);
            source = source.lnc().inputArea().addedConnection(target.getHeader().ludemeNodeComponent(), source.inputField()).connectionComponent();
        }
        else
        {
            source.lnc().inputArea().addedConnection(target.getHeader().ludemeNodeComponent(), source.inputField());
        }

        // update creator argument
        target.getHeader().ludemeNodeComponent().node().setCreatorArgument(source.inputField().nodeArgument(0)); // TODO

        // update the positions of the connection components
        source.updatePosition();
        target.updatePosition();
        // fill the connection components
        source.fill(true);
        target.setFill(true);
        // update the connection in the source
        source.setConnectedTo(target.getHeader().ludemeNodeComponent());
        target.setInputField(source.inputField());

        // Add an edge
        LudemeConnection connection = new LudemeConnection(source, target);
        edges.add(connection);

        // Update the provided input in the description graph
        // differentiate between an inputed provided to a collection and otherwise

        if(source.inputField().nodeArgument(0).collection())
        {
            LudemeNode sourceNode = source.lnc().node();
            int collectionElementIndex = 0;
            if(source.inputField().parent() != null) collectionElementIndex = source.inputField().parent().children().indexOf(source.inputField())+1;
            Handler.updateCollectionInput(graphPanel.graph(), sourceNode, source.inputField().nodeArgument(0), target.getHeader().ludemeNodeComponent().node(), collectionElementIndex);
        }
        else
        {
            if(DEBUG) System.out.println("[EP] Adding connection: " + source.lnc().node().symbol().name() + " , " + target.getHeader().ludemeNodeComponent().node().symbol().name() + " at index " + source.inputField().inputIndexFirst());
            // update creator argument
            System.out.println("\u001B[32m"+"Calling from EP 241"+"\u001B[0m");
            //Handler.updateInput(graphPanel.graph(), source.lnc().node(), source.inputField().inputIndexFirst(), target.getHeader().ludemeNodeComponent().node());
            Handler.updateInput(graphPanel.graph(), source.lnc().node(), source.inputField().nodeArgument(0), target.getHeader().ludemeNodeComponent().node());
        }
        
        graphPanel.repaint();
    }

    public void removeAllConnections(LudemeNode node) {
        removeAllConnections(node, true);
    }

    public void removeAllConnections(LudemeNode node, boolean onlyOutgoingConnections)
    {

        for(LudemeConnection e : new ArrayList<>(edges))
        {
            if(e.outgoingNode() != node && !(!onlyOutgoingConnections && e.ingoingNode() == node)) continue;

            edges.remove(e);
            LudemeNodeComponent source = e.getConnectionComponent().inputField().inputArea().LNC();
            int collectionElementIndex = -1;
            if(e.getConnectionComponent().inputField().parent() != null)
            {
                collectionElementIndex = e.getConnectionComponent().inputField().parent().children().indexOf(e.getConnectionComponent().inputField())+1;
            }
            source.inputArea().removedConnection(e.getConnectionComponent().inputField());
            e.getIngoingConnectionComponent().setFill(false); // the node source was connected to is not connected anymore
            e.getConnectionComponent().fill(false); // the node source is not connected anymore
            if(e.getConnectionComponent().inputField().nodeArgument(0).collection())
            {
                if(collectionElementIndex == -1) collectionElementIndex = 0;
                Handler.updateCollectionInput(graphPanel.graph(), source.node(), e.getConnectionComponent().inputField().nodeArgument(0), null, collectionElementIndex);
                //Handler.updateCollectionInput(graphPanel.graph(), source.node(), e.getConnectionComponent().inputField().inputIndexFirst(), null, collectionElementIndex);
            }
            else
            {
                Handler.updateInput(graphPanel.graph(), source.node(), e.getConnectionComponent().inputField().nodeArgument(0), null);
                //Handler.updateInput(graphPanel.graph(), source.node(), e.getConnectionComponent().inputField().inputIndexFirst(), null);
            }
        }

        graphPanel.repaint();
    }

    // removes all outgoing conenctions of the node's ("node") connection component "connection"
    public void removeConnection(LudemeNode node, LConnectionComponent connection)
    {
        // TODO if(connection.lnc().dynamic()) connection.lnc().inputArea().removedConnectionDynamic(node, connection.inputField());
        for(LudemeConnection e : new ArrayList<>(edges))
        {
            if(e.getConnectionComponent().equals(connection))
            {
                System.out.println("[CH] removing connection from " + node.symbol().name() + " to " + e.ingoingNode().symbol().name());
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
                    Handler.removeEdge(graphPanel.graph(), node, e.ingoingNode(), elementIndex);
                }
                else {
                    Handler.removeEdge(graphPanel.graph(), node, e.ingoingNode());
                }
                // TODO: Below should happen in notifyEdgeRemoved()
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
                        // public static void setCollectionInput(DescriptionGraph graph, LudemeNode node, int inputIndex, Object input, int elementIndex)
                        //Handler.updateCollectionInput(graphPanel.graph(), sourceNode, connection.inputField().inputIndexFirst(), null, collectionElementIndex);
                    Handler.updateCollectionInput(graphPanel.graph(), sourceNode, connection.inputField().nodeArgument(0), null, collectionElementIndex);
                }
                else
                {
                    System.out.println("\u001B[32m"+"Calling from EP 342"+"\u001B[0m");
                    //Handler.updateInput(graphPanel.graph(), e.getConnectionComponent().lnc().node(), e.getConnectionComponent().inputField().inputIndexFirst(), null);
                    Handler.updateInput(graphPanel.graph(), e.getConnectionComponent().lnc().node(), e.getConnectionComponent().inputField().nodeArgument(0), null);
                }
            }
        }
        graphPanel.repaint();
    }

    public void paintNewConnection(Graphics2D g2, Point mousePosition)
    {
        ImmutablePoint connection_point = selectedConnectionComponent.connectionPointPosition();
        Path2D p2d = new Path2D.Double();

        int cp_x = connection_point.x + Math.abs((connection_point.x-mousePosition.x)/2);
        p2d.moveTo(connection_point.x, connection_point.y);
        p2d.curveTo(cp_x, connection_point.y, cp_x, mousePosition.y, mousePosition.x, mousePosition.y);
        g2.draw(p2d);
    }

    public void drawNewConnection(Graphics2D g2, Point mousePosition)
    {
        if(selectedConnectionComponent != null && mousePosition != null) paintNewConnection(g2, mousePosition);
    }

    public LConnectionComponent getSelectedConnectionComponent()
    {
        return selectedConnectionComponent;
    }

    public void setSelectedConnectionComponent(LConnectionComponent selectedConnectionComponent)
    {
        this.selectedConnectionComponent = selectedConnectionComponent;
    }

    public void clearEdges()
    {
        edges.clear();
    }
}
