package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.designs.board.SpiralDesign;
import view.container.styles.BoardStyle;

/**
 * Spiral board style that draws cells based on centroid position, e.g. for Mehen. 
 * 
 * @author cambolbro
 */
public class SpiralStyle extends BoardStyle
{
	public SpiralStyle(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
		containerDesign = new SpiralDesign(this);
	}

	//-------------------------------------------------------------------------
	
}
