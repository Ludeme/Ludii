package app.views.tools.buttons;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import app.PlayerApp;
import app.move.MoveHandler;
import app.views.tools.ToolButton;
import game.rules.play.moves.Moves;
import other.context.Context;
import other.move.Move;

//-----------------------------------------------------------------------------

/**
 * Pass button.
 * 
 * @author Matthew.Stephenson and cambolbro
 */
public class ButtonPass extends ToolButton
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 * @param passButtonIndex 
	 */
	public ButtonPass(final PlayerApp app, final int cx, final int cy, final int sx, final int sy, final int passButtonIndex)
	{
		super(app, "Pass", cx, cy, sx, sy, passButtonIndex);
		tooltipMessage = "Pass/End Move";
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

		//g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
		
		final GeneralPath path = new GeneralPath();
		path.moveTo( cx - 15 * scale, cy + 10 * scale);
		path.curveTo(cx - 15 * scale, cy, cx - 8 * scale, cy - 7 * scale, cx + 2 * scale, cy - 7 * scale);
		path.lineTo( cx,              cy - 12 * scale);
		path.lineTo( cx + 15 * scale, cy - 5 * scale);
		path.lineTo( cx,              cy + 2 * scale);
		path.lineTo( cx +  2 * scale, cy - 3 * scale);
		path.curveTo(cx -  7 * scale, cy - 3 * scale, cx - 13 * scale, cy + 6 * scale, cx - 15 * scale, cy + 10 * scale);
		g2d.fill(path);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	protected boolean isEnabled()
	{
		boolean canPass = false;
		final Context context = app.contextSnapshot().getContext(app);
		
		final Moves legal = context.moves(context);
		for (final Move m : legal.moves())
		{
			if (m.isPass() && (app.manager().settingsNetwork().getNetworkPlayerNumber() == m.mover() || app.manager().settingsNetwork().getNetworkPlayerNumber() == 0))
				canPass = true;
			
			// If going from one game to the next in a match, use the pass button to trigger this.
			if (m.containsNextInstance())
				canPass = true;
		}
		
		// If going backwards in a trial and have no moves, then need to show the pass button.
//		if (legal.moves().size() == 0 && !app.contextSnapshot().getContext(app).trial().over() && app.manager().undoneMoves().size() > 0)
//			canPass = true;
		
		if (canPass)
		{
			showPossibleMovesTemporaryMessage();
			return true;
		}
		return false;
	}

	//-------------------------------------------------------------------------

	// The user wants to either pass or indicate "end of turn".
	@Override
	public void press()
	{
		if (isEnabled())
		{
			MoveHandler.tryGameMove(app, null, null, true, -1);
		}
	}

	//-------------------------------------------------------------------------

}
