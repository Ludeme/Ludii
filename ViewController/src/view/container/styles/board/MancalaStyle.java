package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.components.board.MancalaComponents;
import view.container.aspects.designs.board.MancalaDesign;
import view.container.styles.BoardStyle;

/**
 * Basic style for generic Mancala boards.
 * 
 * @author cambolbro
 */
public class MancalaStyle extends BoardStyle
{
	public MancalaStyle(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
		containerDesign = new MancalaDesign(this);
		containerComponents = new MancalaComponents(bridge, this);
	}

	//-------------------------------------------------------------------------

}
