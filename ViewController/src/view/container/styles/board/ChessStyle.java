package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import other.context.Context;
import view.container.aspects.designs.board.ChessDesign;
import view.container.styles.board.puzzle.PuzzleStyle;

public class ChessStyle extends PuzzleStyle
{
	public ChessStyle(final Bridge bridge, final Container container, final Context context) 
	{
		super(bridge, container, context);
		containerDesign = new ChessDesign(this, boardPlacement);
	}

	//-------------------------------------------------------------------------
	
}
