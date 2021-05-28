package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.designs.board.ShibumiDesign;
import view.container.aspects.placement.Board.PyramidalPlacement;
import view.container.styles.BoardStyle;

public class ShibumiStyle extends BoardStyle
{
	public ShibumiStyle(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
		final PyramidalPlacement pyramidalPlacement = new PyramidalPlacement(bridge, this);
		containerPlacement = pyramidalPlacement;
		containerDesign = new ShibumiDesign(this, pyramidalPlacement);
	}
	
	//-------------------------------------------------------------------------

}
