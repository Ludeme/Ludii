package app.display.dialogs.visual_editor.view.panels.editor;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
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
        addConnection(selectedConnectionComponent, target.ingoingConnectionComponent());
        target.ingoingConnectionComponent().setInputField(selectedConnectionComponent.inputField());
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
        else
        {
            source.lnc().inputArea().addedConnection(target.getHeader().ludemeNodeComponent(), source.inputField());
        }

        // update the positions of the connection components
        source.updatePosition();
        target.updatePosition();
        // fill the connection components
        source.fill(true);
        target.setFill(true);
        // update the connection in the source
        source.setConnectedTo(target.getHeader().ludemeNodeComponent());

        // Add an edge
        Handler.addEdge(graphPanel.graph(), source.lnc().node(), target.getHeader().ludemeNodeComponent().node());
        LudemeConnection connection = new LudemeConnection(source, target);
        edges.add(connection);

        // Update the provided input in the description graph
        // differentiate between an inputed provided to a collection and otherwise

        if(source.inputField().nodeArgument(0).collection())
        {
            LudemeNode sourceNode = source.lnc().node();
            int collectionElementIndex = 0;
            if(source.inputField().parent() != null) collectionElementIndex = source.inputField().parent().children().indexOf(source.inputField())+1;
            Handler.setCollectionInput(graphPanel.graph(), sourceNode, source.inputField().inputIndexFirst(), target.getHeader().ludemeNodeComponent().node(), collectionElementIndex);
        }
        else
        {
            if(DEBUG) System.out.println("[EP] Adding connection: " + source.lnc().node().symbol().name() + " , " + target.getHeader().ludemeNodeComponent().node().symbol().name() + " at index " + source.inputField().inputIndexFirst());
            System.out.println("\u001B[32m"+"Calling from EP 241"+"\u001B[0m");
            Handler.updateInput(graphPanel.graph(), source.lnc().node(), source.inputField().inputIndexFirst(), target.getHeader().ludemeNodeComponent().node());
        }

        /* TODO if(source.inputField().nodeArgument(0).collection())
        {
            LudemeNode sourceNode = source.lnc().node();
            NodeArgument sourceInput = source.inputField().nodeArgument(0);

            // TODO: Perhaps this part should be put into LInputField.java addCollectionItem() method

            LudemeNode[] providedInput = (LudemeNode[]) sourceNode.providedInputs()[sourceInput.indexFirst()];

            // get children of collection
            List<LInputField> children;
            int numberOfChildren;
            if(source.inputField().parent != null)
            {
                children = source.inputField().parent.children;
                numberOfChildren = source.inputField().parent.children.size();
            }
            else
            {
                children = source.inputField().children;
                numberOfChildren = source.inputField().children.size();
            }

            // The provided input class just be an array. If it is null, then create it NOTE!: the first collection inputfield is not counted as a child, therefore numberOfChildren+1
            if(sourceNode.providedInputs()[sourceInput.indexFirst()] == null)
            {
                providedInput = new LudemeNode[numberOfChildren+1];
            }
            else
            {
                providedInput = (LudemeNode[]) sourceNode.providedInputs()[sourceInput.indexFirst()];
            }
            // if the array is not big enough, expand it.
            if(providedInput.length < numberOfChildren+1)
            {
                LudemeNode[] newProvidedInput = new LudemeNode[numberOfChildren+1];
                System.arraycopy(providedInput, 0, newProvidedInput, 0, providedInput.length);
                providedInput = newProvidedInput;
            }
            // get the index of the current input field w.r.t collection field
            int i = children.indexOf(source.inputField()) + 1; // + 1 because the first input field is not counted as a child
            //if(i==-1) i = 0;
            providedInput[i] = target.getHeader().ludemeNodeComponent().node();
            // TODO: REMOVE LATER
            System.out.println("\u001B[32m"+"Calling from EP 237"+"\u001B[0m");
            Handler.updateInput(Handler.editorPanel.graph(), sourceNode, sourceInput.indexFirst(), providedInput);
        }
        else
        {
            if(DEBUG) System.out.println("[EP] Adding connection: " + source.lnc().node().symbol().name() + " , " + target.getHeader().ludemeNodeComponent().node().symbol().name() + " at index " + source.inputField().inputIndexFirst());
            System.out.println("\u001B[32m"+"Calling from EP 241"+"\u001B[0m");
            Handler.updateInput(graphPanel.graph(), source.lnc().node(), source.inputField().inputIndexFirst(), target.getHeader().ludemeNodeComponent().node());
        } */

        graphPanel.repaint();
    }

    public void removeAllConnections(LudemeNode node) {
        removeAllConnections(node, true);
    }

    public void removeAllConnections(LudemeNode node, boolean onlyOutgoingConnections)
    {
        for(LudemeConnection e : new ArrayList<>(edges))
        {
            if(e.getConnectionComponent().lnc().node().equals(node)
                    || (!onlyOutgoingConnections
                    && e.getIngoingConnectionComponent().getHeader().ludemeNodeComponent().node().equals(node)))
            {
                edges.remove(e);
                e.getIngoingConnectionComponent().setFill(false); // header
                e.getConnectionComponent().fill(false); // input
                e.getConnectionComponent().setConnectedTo(null);
                // TODO: e.getConnectionComponent().inputField().inputArea().updateComponent(node, null,true);
                System.out.println("\u001B[32m"+"Calling from EP 307"+"\u001B[0m");
                Handler.updateInput(Handler.editorPanel.graph(), e.getConnectionComponent().lnc().node(), e.getConnectionComponent().inputField().inputIndexFirst(), null);
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
                edges.remove(e);
                e.getIngoingConnectionComponent().setFill(false); // header
                e.getConnectionComponent().fill(false); // input
                e.getConnectionComponent().setConnectedTo(null);
                e.getConnectionComponent().inputField().inputArea().removedConnection(e.getConnectionComponent().inputField());

                if(connection.inputField().nodeArgument(0).collection())
                {
                    if(connection.inputField().nodeArgument(0).collection())
                    {
                        LudemeNode sourceNode = connection.lnc().node();
                        int collectionElementIndex = 0;
                        if(connection.inputField().parent() != null) collectionElementIndex = connection.inputField().children().indexOf(connection.inputField())+1;
                        // public static void setCollectionInput(DescriptionGraph graph, LudemeNode node, int inputIndex, Object input, int elementIndex)
                        Handler.setCollectionInput(graphPanel.graph(), sourceNode, connection.inputField().inputIndexFirst(), null, collectionElementIndex);
                    }
                }
                else
                {
                    System.out.println("\u001B[32m"+"Calling from EP 342"+"\u001B[0m");
                    Handler.updateInput(Handler.editorPanel.graph(), e.getConnectionComponent().lnc().node(), e.getConnectionComponent().inputField().inputIndexFirst(), null);
                }

                // TODO: e.getConnectionComponent().inputField().inputArea().updateComponent(node, null, true);
                // check whether it was the element of a collection
                /* TODO
                if(!connection.inputField().isMerged() && connection.inputField().nodeArgument(0).collection())
                {
                    // if element of collection udpate the array
                    LudemeNode[] providedInputs = (LudemeNode[]) node.providedInputs()[connection.inputField().inputIndexFirst()];
                    // find index which to remove from array
                    int indexToUpdate;
                    if(connection.inputField().parent != null)
                    {
                        indexToUpdate = connection.inputField().parent.children.indexOf(connection.inputField()) + 1;
                    }
                    else
                    {
                        indexToUpdate = 0;
                    }
                    // set to null
                    providedInputs[indexToUpdate] = null;
                    System.out.println("\u001B[32m"+"Calling from EP 339"+"\u001B[0m");
                    Handler.updateInput(Handler.editorPanel.graph(), e.getConnectionComponent().lnc().node(), connection.inputField().inputIndexFirst(), providedInputs);
                } else
                {
                    System.out.println("\u001B[32m"+"Calling from EP 342"+"\u001B[0m");
                    Handler.updateInput(Handler.editorPanel.graph(), e.getConnectionComponent().lnc().node(), e.getConnectionComponent().inputField().inputIndexFirst(), null);
                } */

            }
        }
        Handler.editorPanel.repaint();
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
