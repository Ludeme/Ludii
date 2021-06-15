package app.views;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import app.PlayerApp;
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
			drawBoardState(g2d, context);
		
		if (context.game().isDeductionPuzzle())
			app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.HINTS, context);
		
		if (app.bridge().settingsVC().showCandidateValues() && context.game().isDeductionPuzzle())
			app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.CANDIDATES, context);

		app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.TRACK, context);
		app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.PREGENERATION, context);
		app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.INDICES, context);
		app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.COSTS, context);
		app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.POSSIBLEMOVES, context);

		paintDebug(g2d, Color.CYAN);
	}

	//-------------------------------------------------------------------------

	/**
	 * Determine whether we need to draw the dummy (when selecting consequence) or real context.
	 * @param g2d
	 */
	void drawBoardState(final Graphics2D g2d, final Context context)
	{
		Context stateContext = context;
		
		if 
		(
			app.bridge().settingsVC().selectingConsequenceMove()
			&& 
			app.manager().ref().intermediaryContext() != null
		)
			stateContext = app.manager().ref().intermediaryContext();
		
		app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.COMPONENTS, stateContext);
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
