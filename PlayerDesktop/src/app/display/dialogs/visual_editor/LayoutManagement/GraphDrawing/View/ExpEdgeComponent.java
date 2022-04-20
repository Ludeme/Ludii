package app.display.dialogs.visual_editor.LayoutManagement.GraphDrawing.View;


import app.display.dialogs.visual_editor.model.Edge;
import app.display.dialogs.visual_editor.model.MetaGraph.ExpNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;

import java.awt.*;

import static app.display.dialogs.visual_editor.LayoutManagement.VisualEditor.LayoutConfigs.NODE_SIZE;

public class ExpEdgeComponent
{

    private ExpNode nodeA;
    private ExpNode nodeB;

    public ExpEdgeComponent(iGraph graph, Edge edge)
    {
        nodeA = (ExpNode) graph.getNode(edge.getNodeA());
        nodeB = (ExpNode) graph.getNode(edge.getNodeB());
    }

    public void drawEdge(Graphics2D g2d)
    {
        g2d.setColor(Color.GRAY);
        g2d.drawLine((int)(nodeA.getPos().getScreenTransX()+(NODE_SIZE*0.5)),
                (int)(nodeA.getPos().getScreenTransY()+(NODE_SIZE*0.5)),
                (int)(nodeB.getPos().getScreenTransX()+(NODE_SIZE*0.5)),
                (int)(nodeB.getPos().getScreenTransY()+(NODE_SIZE*0.5)));
    }

}
