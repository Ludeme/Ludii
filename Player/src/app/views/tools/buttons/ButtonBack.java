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
public class ButtonBack extends ToolButton
{
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param cx
	 * @param cy
	 * @param backButtonIndex 
	 */
	public ButtonBack(final PlayerApp app, final int cx, final int cy, final int sx, final int sy, final int backButtonIndex)
	{
		super(app, "Back", cx, cy, sx, sy, backButtonIndex);
		tooltipMessage = "Back a Move";
	}

	//-------------------------------------------------------------------------

	@Override
	public void draw(final Graphics2D g2d)
	{
		final double cx = rect.getCenterX();
		final double cy = rect.getCenterY();
		
		g2d.setColor(getButtonColour());
		g2d.setStroke(new BasicStroke((3 ), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
		final GeneralPath path = new GeneralPath();
		path.moveTo(cx + 5 , cy + 7 );
		path.lineTo(cx - 5 , cy + 0 );
		path.lineTo(cx + 5 , cy - 7 );
		g2d.draw(path);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	protected boolean isEnabled()
	{
		final Context context = app.manager().ref().context();
		final int numInitialPlacementMoves = context.currentInstanceContext().trial().numInitialPlacementMoves();
		if 
		(
			(
				context.currentSubgameIdx() > 1
				||
				context.trial().numMoves() > numInitialPlacementMoves
			)
			&& 
			app.manager().settingsNetwork().getActiveGameId() == 0
		)
			return true;
		
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	// Goes back a single action in the trial
	public void press()
	{
		if (isEnabled())
		{
			final Context context = app.manager().ref().context();
			//context.game().undo(context);
			ToolView.jumpToMove(app, context.trial().numMoves() - 1);
		}
	}

	//-------------------------------------------------------------------------

}
