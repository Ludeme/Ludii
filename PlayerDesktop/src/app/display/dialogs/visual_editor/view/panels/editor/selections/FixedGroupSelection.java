package app.display.dialogs.visual_editor.view.panels.editor.selections;

import java.awt.*;

public class FixedGroupSelection
{
    public static void drawGroupBox(int x, int y, int width, int height, Graphics2D g2d)
    {
        float[] dashingPattern = {10f, 4f};
        Stroke stroke = new BasicStroke(2f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1.0f, dashingPattern, 0.0f);

        g2d.setColor(new Color(51, 51, 51));
        g2d.setStroke(stroke);

        g2d.drawRect(x, y, width, height);

        g2d.setColor(new Color(236, 221, 144, 50));
        g2d.fillRect(x, y, width, height);

    }
}
