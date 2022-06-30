package app.display.dialogs.visual_editor.view.panels.editor.selections;

import java.awt.*;

public class FixedGroupSelection
{

    /**
     * Padding for the fixed node group selection
     */
    private static final int PADDING = 10;

    public static void drawGroupBox(Rectangle rect, Graphics2D g2d)
    {
        float[] dashingPattern = {10f, 4f};
        Stroke stroke = new BasicStroke(2f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1.0f, dashingPattern, 0.0f);

        g2d.setColor(new Color(51, 51, 51));
        g2d.setStroke(stroke);

        g2d.drawRect(rect.x-PADDING, rect.y-PADDING, rect.width+PADDING, rect.height+PADDING);

        g2d.setColor(new Color(236, 221, 144, 50));
        g2d.fillRect(rect.x+PADDING, rect.y+PADDING, rect.width+PADDING, rect.height+PADDING);
    }
}
