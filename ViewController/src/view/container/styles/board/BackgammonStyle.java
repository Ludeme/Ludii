package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.components.board.BackgammonComponents;
import view.container.aspects.designs.board.BackgammonDesign;
import view.container.aspects.placement.Board.BackgammonPlacement;
import view.container.styles.BoardStyle;

/**
 * Custom style for Backgammon boards.
 * @author cambolbro
 */
public class BackgammonStyle extends BoardStyle
{
	public BackgammonStyle(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
		final BackgammonPlacement backgammonPlacement = new BackgammonPlacement(bridge, this);
		containerPlacement = backgammonPlacement;
		containerDesign = new BackgammonDesign(this, backgammonPlacement);
		containerComponents = new BackgammonComponents(bridge, this);
	}
}
