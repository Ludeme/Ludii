package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.designs.board.SnakesAndLaddersDesign;
import view.container.styles.BoardStyle;

public class SnakesAndLaddersStyle extends BoardStyle
{
	public SnakesAndLaddersStyle(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
		containerDesign = new SnakesAndLaddersDesign(this, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

}
