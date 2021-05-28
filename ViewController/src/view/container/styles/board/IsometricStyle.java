package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.designs.board.IsometricDesign;
import view.container.styles.BoardStyle;

public class IsometricStyle extends BoardStyle
{
	public IsometricStyle(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
		containerDesign = new IsometricDesign(this, boardPlacement);
	}
	
	//-------------------------------------------------------------------------
	
}
