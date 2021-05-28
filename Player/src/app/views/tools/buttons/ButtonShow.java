package app.views.tools.buttons;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

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
		final int r = 10;
		g2d.fillArc(cx - r, cy - r, 2 * r + 1, 2 * r + 1, 0, 360);
		final int fontSize = 17;
		final int flags = Font.BOLD;
		final Font font = new Font("Arial", flags, fontSize);
		g2d.setFont(font);
		g2d.setColor(Color.white);
		g2d.drawString("?", cx - 3, cy + 6);
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
