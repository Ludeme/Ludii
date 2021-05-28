package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.designs.board.TaflDesign;
import view.container.styles.BoardStyle;

public class TaflStyle extends BoardStyle
{
	public TaflStyle(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
		containerDesign = new TaflDesign(this, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

}
