package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.designs.board.GooseDesign;
import view.container.styles.BoardStyle;

public class GooseStyle extends BoardStyle
{
	public GooseStyle(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
		containerDesign = new GooseDesign(this, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

}
