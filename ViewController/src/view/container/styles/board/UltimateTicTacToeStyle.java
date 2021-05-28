package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.designs.board.UltimateTicTacToeDesign;
import view.container.styles.BoardStyle;

/**
 * Graphic style for Surakarta boards.
 * 
 * @author cambolbro
 */
public class UltimateTicTacToeStyle extends BoardStyle
{
	public UltimateTicTacToeStyle(final Bridge bridge, final Container container)
	{
		super(bridge, container);
		containerDesign = new UltimateTicTacToeDesign(this, boardPlacement);
	}

	//-------------------------------------------------------------------------

}
