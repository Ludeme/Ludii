package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.designs.board.HoundsAndJackalsDesign;
import view.container.aspects.placement.Board.HoundsAndJackalsPlacement;
import view.container.styles.BoardStyle;

/**
 * Hounds and Jackals (i.e. 58 Holes) board style.
 * @author cambolbro
 */
public class HoundsAndJackalsStyle extends BoardStyle
{
	public HoundsAndJackalsStyle(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
		final HoundsAndJackalsPlacement houndsAndJackalsPlacement = new HoundsAndJackalsPlacement(bridge, this);
		containerPlacement = houndsAndJackalsPlacement;
		containerDesign = new HoundsAndJackalsDesign(this, houndsAndJackalsPlacement);
	} 
	
	//-------------------------------------------------------------------------
	
}
