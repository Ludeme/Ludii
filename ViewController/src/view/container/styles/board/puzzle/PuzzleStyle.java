package view.container.styles.board.puzzle;

import bridge.Bridge;
import game.equipment.container.Container;
import other.context.Context;
import view.container.aspects.components.board.PuzzleComponents;
import view.container.aspects.designs.board.puzzle.PuzzleDesign;
import view.container.styles.BoardStyle;

public class PuzzleStyle extends BoardStyle
{
	public PuzzleStyle(final Bridge bridge, final Container container, final Context context) 
	{
		super(bridge, container);
				
		final PuzzleDesign puzzleDesign = new PuzzleDesign(this, boardPlacement);
		containerDesign = puzzleDesign;
		
		if (context.game().isDeductionPuzzle())
			containerComponents = new PuzzleComponents(bridge, this, puzzleDesign);
	}

	//-------------------------------------------------------------------------

}
