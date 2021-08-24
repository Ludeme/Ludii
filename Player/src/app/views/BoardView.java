package app.views;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import app.PlayerApp;
import app.move.MoveVisuals;
import other.context.Context;
import util.PlaneType;

//-----------------------------------------------------------------------------

/**
 * Panel showing the gmae board.
 *
 * @author Matthew.Stephenson and cambolbro
 */
public final class BoardView extends View
{
	
	// maximum percentage of application display width that board can take up
	private final double boardToSizeRatio = 1.0;
	
	/** Size of the board. */
	private final int boardSize;
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 */
	public BoardView(final PlayerApp app)
	{
		super(app);
		boardSize = Math.min(app.height(), (int) (app.width() * boardToSizeRatio));
		placement = new Rectangle(0, 0, boardSize, boardSize);
	}

	//-------------------------------------------------------------------------

	@Override
	public void paint(final Graphics2D g2d)
	{
		final Context context = app.contextSnapshot().getContext(app);
		
		app.bridge().getContainerStyle(context.board().index()).setPlacement(context, placement);

		if (app.settingsPlayer().showBoard() || context.board().isBoardless())
			app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.BOARD, context);

		if (app.settingsPlayer().showGraph())
			app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.GRAPH, context);
		
		if (app.settingsPlayer().showConnections())
			app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.CONNECTIONS, context);

		if (app.settingsPlayer().showAxes())
			app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.AXES, context);

		if (app.settingsPlayer().showPieces())
			app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.COMPONENTS, context);
		
		if (context.game().isDeductionPuzzle())
			app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.HINTS, context);
		
		if (app.bridge().settingsVC().showCandidateValues() && context.game().isDeductionPuzzle())
			app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.CANDIDATES, context);

		app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.TRACK, context);
		app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.PREGENERATION, context);
		app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.INDICES, context);
		app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.COSTS, context);
		app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.POSSIBLEMOVES, context);
		
		// Originally in the overlay view, but moved here to work with web app.
		if (
				app.settingsPlayer().showEndingMove() 
				&& 
				context.currentInstanceContext().trial().moveNumber() > 0 
				&& 
				context.game().endRules() != null 
				&& 
				!app.settingsPlayer().sandboxMode()
			)
			MoveVisuals.drawEndingMove(app, g2d, context);

		paintDebug(g2d, Color.CYAN);
	}

	//-------------------------------------------------------------------------
    
    @Override
    public int containerIndex()
	{
    	return app.contextSnapshot().getContext(app).board().index();
	}

	public int boardSize()
	{
		return boardSize;
	}

}
