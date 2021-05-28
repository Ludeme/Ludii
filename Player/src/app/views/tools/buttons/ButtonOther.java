package app.views.tools.buttons;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import app.PlayerApp;
import app.views.tools.ToolButton;
import game.rules.play.moves.Moves;
import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;

//-----------------------------------------------------------------------------

/**
 * Generic button for "other" operations that don't have a button.
 * 
 * @author Matthew.Stephenson and cambolbro
 */
public class ButtonOther extends ToolButton
{
	FastArrayList<Move> otherPossibleMoves = new FastArrayList<>();
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 * @param otherButtonIndex 
	 */
	public ButtonOther(final PlayerApp app, final int cx, final int cy, final int sx, final int sy, final int otherButtonIndex)
	{
		super(app, "Other", cx, cy, sx, sy, otherButtonIndex);
		tooltipMessage = "Miscellaneous";
	}

	//-------------------------------------------------------------------------

	@Override
	public void draw(final Graphics2D g2d)
	{
		final double cx = rect.getCenterX();
		final double cy = rect.getCenterY();
		
		g2d.setColor(getButtonColour());
		final double r = 2.75;
		g2d.fill(new Ellipse2D.Double(cx-r, cy-r-9, 2*r, 2*r));
		g2d.fill(new Ellipse2D.Double(cx-r, cy-r,   2*r, 2*r));
		g2d.fill(new Ellipse2D.Double(cx-r, cy-r+9, 2*r, 2*r));
		
		if (otherPossibleMoves.size() > 0)
			showPossibleMovesTemporaryMessage();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	protected boolean isEnabled()
	{
		otherPossibleMoves.clear();
		final Context context = app.contextSnapshot().getContext(app);
		
		final Moves legal = context.moves(context);
		for (final Move m : legal.moves())
			if (m.isOtherMove())
				otherPossibleMoves.add(m);
		
		if (otherPossibleMoves.size() > 0)
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
			app.showOtherDialog(otherPossibleMoves);
	}

	//-------------------------------------------------------------------------

}
