package app.views.tools.buttons;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import app.PlayerApp;
import app.views.tools.ToolButton;
import main.Constants;
import other.location.FullLocation;

//-----------------------------------------------------------------------------

/**
 * Settings button.
 *
 * @author Matthew.Stephenson and cambolbro
 */
public class ButtonInfo extends ToolButton
{
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param cx
	 * @param cy
	 * @param infoButtonIndex 
	 */
	public ButtonInfo(final PlayerApp app, final int cx, final int cy, final int sx, final int sy, final int infoButtonIndex)
	{
		super(app, "Info", cx, cy, sx, sy, infoButtonIndex);
		tooltipMessage = "Info";
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
		final int flags = Font.ITALIC | Font.BOLD;
		final Font font = new Font("Arial", flags, fontSize);
		g2d.setFont(font);
		g2d.setColor(Color.white);
		g2d.drawString("i", cx - 3, cy + 6);
		g2d.setFont(oldFont);
	}

	//-------------------------------------------------------------------------

	@Override
	// displays the information popup
	public void press()
	{
		app.showInfoDialog();
		app.bridge().settingsVC().setSelectedFromLocation(new FullLocation(Constants.UNDEFINED));
	}

	//-------------------------------------------------------------------------

}
