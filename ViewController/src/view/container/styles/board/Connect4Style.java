package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.components.board.Connect4Components;
import view.container.aspects.designs.board.Connect4Design;
import view.container.aspects.placement.Board.Connect4Placement;
import view.container.styles.BoardStyle;

public class Connect4Style extends BoardStyle
{	
	public Connect4Style(final Bridge bridge, final Container container) 
	{
		super(bridge, container);

		final Connect4Placement connect4Placement = new Connect4Placement(bridge, this);
		containerPlacement = connect4Placement;
		containerDesign = new Connect4Design(this, connect4Placement);
		containerComponents = new Connect4Components(bridge, this, connect4Placement);
	}

	//-------------------------------------------------------------------------

}
