package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.designs.board.BoardlessDesign;
import view.container.aspects.placement.Board.BoardlessPlacement;
import view.container.styles.BoardStyle;

public class BoardlessStyle extends BoardStyle
{
	public BoardlessStyle(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
		final BoardlessPlacement boardlessPlacement = new BoardlessPlacement(bridge, this);
		containerPlacement = boardlessPlacement;
		containerDesign = new BoardlessDesign(this, boardlessPlacement);
	}
	
	//-------------------------------------------------------------------------

}
