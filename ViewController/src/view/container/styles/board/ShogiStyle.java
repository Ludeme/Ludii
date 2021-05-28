package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.designs.board.ShogiDesign;
import view.container.styles.BoardStyle;

public class ShogiStyle extends BoardStyle
{
	public ShogiStyle(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
		containerDesign = new ShogiDesign(this, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

}
