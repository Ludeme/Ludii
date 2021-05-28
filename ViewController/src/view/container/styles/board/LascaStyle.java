package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.designs.board.LascaDesign;
import view.container.styles.BoardStyle;

public class LascaStyle extends BoardStyle
{
	public LascaStyle(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
		containerDesign = new LascaDesign(this, boardPlacement);
	}

	//-------------------------------------------------------------------------

}
