package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.designs.board.JanggiDesign;
import view.container.styles.BoardStyle;

public class JanggiStyle extends BoardStyle
{
	public JanggiStyle(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
		containerDesign = new JanggiDesign(this, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

}
