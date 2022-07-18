package app.display.dialogs.visual_editor.view.panels.editor.selections;

import java.awt.*;

/**
 * Creates selection area for editor panel
 * @author nic0gin
 */
public class SelectionBox
{
    private static Point A;
    private static Rectangle rect;
    private static SelectionBox sb;

    public SelectionBox(Point A)
    {
        SelectionBox.A = A;
    }

    public static void drawSelectionArea(Point A1, Point B, Graphics2D g2d)
    {
        if (sb == null) sb = new SelectionBox(A1);

        float[] dashingPattern = {10f, 4f};
        Stroke stroke = new BasicStroke(2f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1.0f, dashingPattern, 0.0f);
        g2d.setColor(new Color(92, 150, 242));
        g2d.setStroke(stroke);

        int x = SelectionBox.A.x;
        int y = SelectionBox.A.y;

        int width = B.x - x;
        int height = B.y - y;

        if (B.getX() < x)
        {
            x = B.x;
            width = SelectionBox.A.x - x;
        }

        if (B.getY() < y)
        {
            y = B.y;
            height = SelectionBox.A.y - y;
        }

        g2d.drawRect(x, y, width, height);

        g2d.setColor(new Color(157, 210, 235, 50));
        g2d.fillRect(x, y, width, height);
        SelectionBox.rect = new Rectangle(x, y, width, height);
    }

    public static void drawSelectionModeIdle(Point m, Graphics2D g2d)
    {
        float[] dashingPattern = {10f, 4f};
        Stroke stroke = new BasicStroke(2f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1.0f, dashingPattern, 0.0f);
        g2d.setColor(new Color(92, 150, 242));
        g2d.setStroke(stroke);
        g2d.drawLine(m.x-10000,m.y,m.x+10000,m.y);
        g2d.drawLine(m.x,m.y+10000,m.x,m.y-10000);
    }

    public static Rectangle endSelection()
    {
        sb = null;
        return rect;
    }


}
