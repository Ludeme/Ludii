package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.axes.board.SurakartaAxis;
import view.container.aspects.designs.board.SurakartaDesign;
import view.container.aspects.placement.Board.SurakartaPlacement;
import view.container.styles.BoardStyle;

/**
 * Graphic style for Surakarta boards.
 * 
 * @author cambolbro
 */
public class SurakartaStyle extends BoardStyle
{
	public SurakartaStyle(final Bridge bridge, final Container container)
	{
		super(bridge, container);
		final SurakartaPlacement surakartaPlacement = new SurakartaPlacement(bridge, this);
		containerPlacement = surakartaPlacement;
		containerAxis = new SurakartaAxis(this, surakartaPlacement);
		containerDesign = new SurakartaDesign(this, surakartaPlacement);
	}
	
	//-------------------------------------------------------------------------

}
