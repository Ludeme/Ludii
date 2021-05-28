package view.container.styles.board;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.aspects.components.board.TableComponents;
import view.container.aspects.designs.board.TableDesign;
import view.container.aspects.placement.Board.TablePlacement;
import view.container.styles.BoardStyle;

/**
 * Custom style for Table boards.
 * 
 * @author Eric.Piette
 */
public class TableStyle extends BoardStyle
{
	public TableStyle(final Bridge bridge, final Container container)
	{
		super(bridge, container);
		final TablePlacement backgammonPlacement = new TablePlacement(bridge, this);
		containerPlacement = backgammonPlacement;
		containerDesign = new TableDesign(this, backgammonPlacement);
		containerComponents = new TableComponents(bridge, this);
	}
}
