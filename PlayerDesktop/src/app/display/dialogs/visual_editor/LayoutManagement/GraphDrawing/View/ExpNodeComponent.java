package app.display.dialogs.visual_editor.LayoutManagement.GraphDrawing.View;

import app.display.dialogs.visual_editor.LayoutManagement.GraphDrawing.DrawingFrame;
import app.display.dialogs.visual_editor.model.MetaGraph.ExpNode;

import java.awt.*;
import java.awt.geom.Ellipse2D;

import static app.display.dialogs.visual_editor.LayoutManagement.LayoutConfigs.NODE_SIZE;


public class ExpNodeComponent
{

    private ExpNode node;
    private String label;

    public ExpNodeComponent(ExpNode node)
    {
        this.node = node;
        label = node.getLabel();
    }

    public void drawNode(Graphics g)
    {
        drawInner((Graphics2D) g);
        drawOuter((Graphics2D) g);
        g.drawString(label,
                (int)(node.pos().getX() + DrawingFrame.getWIDTH() / 2 - node.width() /2 ),
                (int)(node.pos().getY() + DrawingFrame.getHEIGHT() / 2 + node.height()/2));
    }

    private void drawInner(Graphics2D g2)
    {
        Shape inner = new Ellipse2D.Double(
                node.pos().getScreenTransX(),
                node.pos().getScreenTransY(),
                NODE_SIZE, NODE_SIZE);
        g2.setColor(Color.WHITE);
        g2.fill(inner);
        g2.draw(inner);
    }

    private void drawOuter(Graphics2D g2)
    {
        Shape outer = new Ellipse2D.Double(
                node.pos().getScreenTransX(),
                node.pos().getScreenTransY(),
                NODE_SIZE, NODE_SIZE);
        g2.setColor(Color.BLACK);
        g2.draw(outer);
    }

}
