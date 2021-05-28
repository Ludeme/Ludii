package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.designs.board.GoDesign;
import view.container.styles.BoardStyle;

public class GoStyle extends BoardStyle
{
	public GoStyle(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
		containerDesign = new GoDesign(this, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

}
