package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.designs.board.ConnectiveGoalDesign;
import view.container.styles.BoardStyle;

public class ConnectiveGoalStyle extends BoardStyle
{
	public ConnectiveGoalStyle(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
		containerDesign = new ConnectiveGoalDesign(this, boardPlacement);
	}
	
	//-------------------------------------------------------------------------
	
}
