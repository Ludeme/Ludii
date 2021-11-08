package app.views.tools.buttons;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import app.PlayerApp;
import app.views.tools.ToolButton;
import app.views.tools.ToolView;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Settings button.
 *
 * @author Matthew.Stephenson and cambolbro
 */
public class ButtonForward extends ToolButton
{
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param cx
	 * @param cy
	 * @param forwardButtonIndex 
	 */
	public ButtonForward(final PlayerApp app, final int cx, final int cy, final int sx, final int sy, final int forwardButtonIndex)
	{
		super(app, "Forward", cx, cy, sx, sy, forwardButtonIndex);
		tooltipMessage = "Forward a Move";
	}

	//-------------------------------------------------------------------------

	@Override
	public void draw(final Graphics2D g2d)
	{
		final double cx = rect.getCenterX();
		final double cy = rect.getCenterY();
		
		g2d.setColor(getButtonColour());
		
		// Determine button scale, so that buttons are scaled up on the mobile version.
		// The desktop version assume a toolbar height of 32 pixels, this should be 64 for mobile version.
		final double scale = scaleForDevice();

		g2d.setStroke(new BasicStroke((float)(3 * scale), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
		
		final GeneralPath path = new GeneralPath();
		path.moveTo(cx - 5 * scale, cy + 7 * scale);
		path.lineTo(cx + 5 * scale, cy);
		path.lineTo(cx - 5 * scale, cy - 7 * scale);
		g2d.draw(path);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	protected boolean isEnabled()
	{
		if 
		(
			app.manager().undoneMoves().size() > 0
			&&
			app.manager().settingsNetwork().getActiveGameId() == 0
		)
			return true;
		
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	// Goes forward a single action in the current trial
	public void press()
	{
		if (isEnabled())
		{
			final Context context = app.manager().ref().context();
			ToolView.jumpToMove(app, context.trial().numMoves() + 1);
		}
	}

	//-------------------------------------------------------------------------

}
