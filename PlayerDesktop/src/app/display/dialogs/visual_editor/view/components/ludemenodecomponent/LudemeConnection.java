package app.display.dialogs.visual_editor.view.components.ludemenodecomponent;

import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LIngoingConnectionComponent;

public class LudemeConnection
{

    private final LConnectionComponent CONNECTION_COMPONENT;
    private final LIngoingConnectionComponent INGOING_CONNECTION_COMPONENT;

    private static int ID_COUNT = 0;
    private final int ID = ID_COUNT++;

    private final ImmutablePoint inputPoint;
    private final ImmutablePoint targetPoint;

    public LudemeConnection(LConnectionComponent connectionComponent, LIngoingConnectionComponent ingoingConnectionComponent)
    {
        this.CONNECTION_COMPONENT = connectionComponent;
        this.INGOING_CONNECTION_COMPONENT = ingoingConnectionComponent;


        this.inputPoint = connectionComponent.connectionPointPosition();
        this.targetPoint = ingoingConnectionComponent.getConnectionPointPosition();
    }

    public ImmutablePoint getInputPosition()
    {
        CONNECTION_COMPONENT.updatePosition();
        return inputPoint;
    }

    public ImmutablePoint getTargetPosition()
    {
        INGOING_CONNECTION_COMPONENT.updatePosition();
        return targetPoint;
    }

    public LIngoingConnectionComponent getIngoingConnectionComponent(){
        return INGOING_CONNECTION_COMPONENT;
    }

    public LConnectionComponent getConnectionComponent(){
        return CONNECTION_COMPONENT;
    }

    public LudemeNode ingoingNode(){
        return INGOING_CONNECTION_COMPONENT.getHeader().ludemeNodeComponent().node();
    }

    public LudemeNode outgoingNode(){
        return CONNECTION_COMPONENT.lnc().node();
    }

    @Override
    public String toString(){
        return "Connection " + ID;
    }

}
