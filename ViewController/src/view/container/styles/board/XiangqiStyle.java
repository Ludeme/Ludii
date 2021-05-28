package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.designs.board.XiangqiDesign;
import view.container.styles.BoardStyle;

public class XiangqiStyle extends BoardStyle
{
	public XiangqiStyle(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
		containerDesign = new XiangqiDesign(this, boardPlacement);
	}

	//-------------------------------------------------------------------------

}
