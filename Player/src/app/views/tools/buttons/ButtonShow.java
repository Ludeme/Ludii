package app.views.tools.buttons;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import app.PlayerApp;
import app.views.tools.ToolButton;

//-----------------------------------------------------------------------------

/**
 * Show button.
 *
 * @author cambolbro and Matthew.Stephenson
 */
public class ButtonShow extends ToolButton
{
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param cx
	 * @param cy
	 * @param infoButtonIndex 
	 */
	public ButtonShow(final PlayerApp app, final int cx, final int cy, final int sx, final int sy, final int infoButtonIndex)
	{
		super(app, "Show", cx, cy, sx, sy, infoButtonIndex);
		tooltipMessage = "Show moves";
	}

	//-------------------------------------------------------------------------

	@Override
	public void draw(final Graphics2D g2d)
	{
		final int cx = (int) rect.getCenterX();
		final int cy = (int) rect.getCenterY();
		
		g2d.setColor(getButtonColour());
		
		final Font oldFont = g2d.getFont();
		
		// Determine button scale, so that buttons are scaled up on the mobile version.
		// The desktop version assume a toolbar height of 32 pixels, this should be 64 for mobile version.
		final double scale = scaleForDevice();

		final int r = (int)(10 * scale);
		g2d.fillArc(cx - r, cy - r, 2 * r + 1, 2 * r + 1, 0, 360);
		
		final int fontSize = (int)(17 * scale);
		final int flags = Font.BOLD;
		final Font font = new Font("Arial", flags, fontSize);
		g2d.setFont(font);
		
		g2d.setColor(Color.white);
		
		final String str = "?";
		final Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(str, g2d);
		
		final int tx = (int)(cx - bounds.getWidth()  / 2 + 0 * scale);
		final int ty = (int)(cy + bounds.getHeight() / 2 - 3 * scale);
		
		g2d.drawString(str, tx, ty);
		
		g2d.setFont(oldFont);
	}

	//-------------------------------------------------------------------------

	// Toggles the "Show Legal Moves" setting on/off.
	@Override
	public void press()
	{
		app.bridge().settingsVC().setShowPossibleMoves(!app.bridge().settingsVC().showPossibleMoves());
		app.resetMenuGUI();
	}

	//-------------------------------------------------------------------------

}
