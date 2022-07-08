package app.display.dialogs.visual_editor.view.panels.editor.backgrounds;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;

import java.awt.*;
import java.awt.geom.Line2D;

public class CartesianGridBackground implements IBackground
{
    @Override
    public void paint(Rectangle viewRectangle, int width, int height, Graphics2D g2) {

        int lineWidth = DesignPalette.BACKGROUND_LINE_WIDTH;
        int frequency = DesignPalette.BACKGROUND_LINE_PADDING;

        g2.setColor(DesignPalette.BACKGROUND_VISUAL_HELPER());
        g2.setStroke(new BasicStroke(lineWidth));

        // draw vertical lines
        for (int x = 0; x < width; x += frequency)
        {
            Line2D line = new Line2D.Double(x, viewRectangle.y, x, viewRectangle.y+height);
            g2.draw(line);
        }
        // draw horizontal lines
        for (int y = 0; y < height; y += frequency)
        {
            Line2D line = new Line2D.Double(viewRectangle.x, y, viewRectangle.x+width, y);
            g2.draw(line);
        }

    }
}
