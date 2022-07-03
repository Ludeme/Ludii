package app.display.dialogs.visual_editor.view.panels.editor.backgrounds;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;

import java.awt.*;
public class DotGridBackground implements IBackground

{
    public void paint(Rectangle viewRectangle, int width, int height, Graphics2D g2)
    {
        // draw background points
        // every 50 pixel a circle
        int paddingHorizontal = 35;
        int paddingvertical = 15;
        int frequency = Handler.currentPalette().BACKGROUND_DOT_PADDING;
        int diameter = Handler.currentPalette().BACKGROUND_DOT_DIAMETER;

        // to improve performance, only draw points that are in the visible area
        for (int i = paddingHorizontal; i < width - paddingHorizontal; i += frequency)
        {
            for (int j = paddingvertical; j < height - paddingvertical; j += frequency)
            {
                if(i < viewRectangle.x || i > viewRectangle.x + viewRectangle.width || j < viewRectangle.y || j > viewRectangle.y + viewRectangle.height)
                    continue;
                g2.setColor(Handler.currentPalette().BACKGROUND_VISUAL_HELPER());
                g2.fillOval(i, j, diameter, diameter);
            }
        }
    }
}
