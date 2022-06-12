package app.display.dialogs.visual_editor.view.panels.editor;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.ImmutablePoint;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeConnection;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.model.InputInformation;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LIngoingConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LInputField;


import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class ConnectionHandler
{

    private static final boolean DEBUG = true;
    private final java.util.List<LudemeConnection> edges;
    private LConnectionComponent selectedConnectionComponent = null;

    public ConnectionHandler(java.util.List<LudemeConnection> edges)
    {
        this.edges = edges;
    }

    public void paintConnections(Graphics2D g2)
    {
        for(LudemeConnection e : edges)
        {
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

    public void startNewConnection(LConnectionComponent source)
    {
        if(DEBUG) System.out.println("[EP] Start connection: " + source.getConnectionPointPosition() + " , " + source.getRequiredSymbols());

        if(selectedConnectionComponent != null)
        {
            selectedConnectionComponent.setFill(false);
        }
        selectedConnectionComponent = source;
    }

    public void cancelNewConnection()
    {
        if(selectedConnectionComponent != null)
        {
            selectedConnectionComponent.setFill(false);
            selectedConnectionComponent = null;
        }
    }

    public void finishNewConnection(LudemeNodeComponent target)
    {
        addConnection(selectedConnectionComponent, target.ingoingConnectionComponent());
        selectedConnectionComponent = null;
    }

    public void addConnection(LConnectionComponent source, LIngoingConnectionComponent target)
    {

        source.updatePosition();
        target.updatePosition();

        if(!source.getInputField().isSingle())
        {
            source = source.getLudemeNodeComponent().inputArea().addedConnection(target.getHeader().getLudemeNodeComponent(), source.getInputField()).getConnectionComponent();
        }

        source.updatePosition();
        target.updatePosition();

        source.setFill(true);
        target.setFill(true);
        source.setConnectedTo(target.getHeader().getLudemeNodeComponent());

        // Add an edge
        Handler.addEdge(Handler.editorPanel.getGraph(), source.getLudemeNodeComponent().node(), target.getHeader().getLudemeNodeComponent().node());
        LudemeConnection connection = new LudemeConnection(source, target);
        edges.add(connection);

        // Update the provided input in the description graph
        // differentiate between an inputed provided to a collection and otherwise
        if(source.getInputField().getInputInformation().collection())
        {
            LudemeNode sourceNode = source.getLudemeNodeComponent().node();
            InputInformation sourceInput = source.getInputField().getInputInformation();

            // TODO: Perhaps this part should be put into LInputField.java addCollectionItem() method

            LudemeNode[] providedInput = (LudemeNode[]) sourceNode.providedInputs()[sourceInput.getIndex()];

            // get children of collection
            List<LInputField> children;
            int numberOfChildren;
            if(source.getInputField().parent != null)
            {
                children = source.getInputField().parent.children;
                numberOfChildren = source.getInputField().parent.children.size();
            }
            else
            {
                children = source.getInputField().children;
                numberOfChildren = source.getInputField().children.size();
            }

            // The provided input class just be an array. If it is null, then create it NOTE!: the first collection inputfield is not counted as a child, therefore numberOfChildren+1
            if(sourceNode.providedInputs()[sourceInput.getIndex()] == null)
            {
                providedInput = new LudemeNode[numberOfChildren+1];
            }
            else
            {
                providedInput = (LudemeNode[]) sourceNode.providedInputs()[sourceInput.getIndex()];
            }
            // if the array is not big enough, expand it.
            if(providedInput.length < numberOfChildren+1)
            {
                LudemeNode[] newProvidedInput = new LudemeNode[numberOfChildren+1];
                System.arraycopy(providedInput, 0, newProvidedInput, 0, providedInput.length);
                providedInput = newProvidedInput;
            }
            // get the index of the current input field w.r.t collection field
            int i = children.indexOf(source.getInputField()) + 1; // + 1 because the first input field is not counted as a child
            //if(i==-1) i = 0;
            providedInput[i] = target.getHeader().getLudemeNodeComponent().node();
            // TODO: REMOVE LATER
            System.out.println("\u001B[32m"+"Calling from EP 237"+"\u001B[0m");
            Handler.updateInput(Handler.editorPanel.getGraph(), sourceNode, sourceInput.getIndex(), providedInput);
        }
        else
        {
            if(DEBUG) System.out.println("[EP] Adding connection: " + source.getLudemeNodeComponent().node().symbol().name() + " , " + target.getHeader().getLudemeNodeComponent().node().symbol().name() + " at index " + source.getInputField().getInputIndex());
            System.out.println("\u001B[32m"+"Calling from EP 241"+"\u001B[0m");
            Handler.updateInput(Handler.editorPanel.getGraph(), source.getLudemeNodeComponent().node(), source.getInputField().getInputIndex(), target.getHeader().getLudemeNodeComponent().node());
        }

        Handler.editorPanel.repaint();
    }

    public void removeAllConnections(LudemeNode node) {
        removeAllConnections(node, true);
    }

    public void removeAllConnections(LudemeNode node, boolean onlyOutgoingConnections)
    {
        for(LudemeConnection e : new ArrayList<>(edges))
        {
            if(e.getConnectionComponent().getLudemeNodeComponent().node().equals(node)
                    || (!onlyOutgoingConnections
                    && e.getIngoingConnectionComponent().getHeader().getLudemeNodeComponent().node().equals(node)))
            {
                edges.remove(e);
                e.getIngoingConnectionComponent().setFill(false); // header
                e.getConnectionComponent().setFill(false); // input
                e.getConnectionComponent().setConnectedTo(null);
                e.getConnectionComponent().getInputField().getLudemeNodeComponent().inputArea().updateComponent(node, null,true);
                System.out.println("\u001B[32m"+"Calling from EP 307"+"\u001B[0m");
                Handler.updateInput(Handler.editorPanel.getGraph(), e.getConnectionComponent().getLudemeNodeComponent().node(), e.getConnectionComponent().getInputField().getInputIndex(), null);
            }
        }
        Handler.editorPanel.repaint();
    }

    // removes all outgoing conenctions of the node's ("node") connection component "connection"
    public void removeConnection(LudemeNode node, LConnectionComponent connection)
    {
        if(connection.getLudemeNodeComponent().dynamic()) connection.getLudemeNodeComponent().inputArea().removedConnectionDynamic(node, connection.getInputField());
        for(LudemeConnection e : new ArrayList<>(edges))
        {
            if(e.getConnectionComponent().equals(connection))
            {
                edges.remove(e);
                e.getIngoingConnectionComponent().setFill(false); // header
                e.getConnectionComponent().setFill(false); // input
                e.getConnectionComponent().setConnectedTo(null);
                e.getConnectionComponent().getInputField().getLudemeNodeComponent().inputArea().updateComponent(node, null, true);
                // check whether it was the element of a collection
                if(connection.getInputField().isSingle() && connection.getInputField().getInputInformation().collection())
                {
                    // if element of collection udpate the array
                    LudemeNode[] providedInputs = (LudemeNode[]) node.providedInputs()[connection.getInputField().getInputIndex()];
                    // find index which to remove from array
                    int indexToUpdate;
                    if(connection.getInputField().parent != null)
                    {
                        indexToUpdate = connection.getInputField().parent.children.indexOf(connection.getInputField()) + 1;
                    }
                    else
                    {
                        indexToUpdate = 0;
                    }
                    // set to null
                    providedInputs[indexToUpdate] = null;
                    System.out.println("\u001B[32m"+"Calling from EP 339"+"\u001B[0m");
                    Handler.updateInput(Handler.editorPanel.getGraph(), e.getConnectionComponent().getLudemeNodeComponent().node(), connection.getInputField().getInputIndex(), providedInputs);
                } else
                {
                    System.out.println("\u001B[32m"+"Calling from EP 342"+"\u001B[0m");
                    Handler.updateInput(Handler.editorPanel.getGraph(), e.getConnectionComponent().getLudemeNodeComponent().node(), e.getConnectionComponent().getInputField().getInputIndex(), null);
                }
            }
        }
        Handler.editorPanel.repaint();
    }

    public void paintNewConnection(Graphics2D g2, Point mousePosition)
    {
        ImmutablePoint connection_point = selectedConnectionComponent.getConnectionPointPosition();
        Path2D p2d = new Path2D.Double();

        //if(selectedConnectionComponent.isOutgoing()){
        int cp_x = connection_point.x + Math.abs((connection_point.x-mousePosition.x)/2);
        p2d.moveTo(connection_point.x, connection_point.y);
        p2d.curveTo(cp_x, connection_point.y, cp_x, mousePosition.y, mousePosition.x, mousePosition.y);
        //}
        /*
        else {
            int cp_x = mousePosition.x + Math.abs((mousePosition.x-connection_point.x)/2);
            p2d.moveTo(mousePosition.x, mousePosition.y);
            p2d.curveTo(cp_x, mousePosition.y, cp_x, connection_point.y, connection_point.x, connection_point.y);
        }*/
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
}
