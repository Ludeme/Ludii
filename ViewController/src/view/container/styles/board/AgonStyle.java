package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.designs.board.AgonDesign;
import view.container.styles.BoardStyle;

public class AgonStyle extends BoardStyle
{
	public AgonStyle(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
		containerDesign = new AgonDesign(this, boardPlacement);
	}
	
	//-------------------------------------------------------------------------
	
}
