package view.container.styles.board.puzzle;

import bridge.Bridge;
import game.equipment.container.Container;
import other.context.Context;
import view.container.aspects.designs.board.puzzle.SudokuDesign;

public class SudokuStyle extends PuzzleStyle
{
	public SudokuStyle(final Bridge bridge, final Container container, final Context context) 
	{
		super(bridge, container, context);
		
		final SudokuDesign sudokuDesign = new SudokuDesign(this, boardPlacement);
		containerDesign = sudokuDesign;
	}

	//-------------------------------------------------------------------------
	
}
