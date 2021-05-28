package view.container.styles.board.puzzle;

import bridge.Bridge;
import game.equipment.container.Container;
import other.context.Context;
import view.container.aspects.designs.board.puzzle.FutoshikiDesign;
import view.container.styles.board.graph.GraphStyle;

public class FutoshikiStyle extends GraphStyle
{
	public FutoshikiStyle(final Bridge bridge, final Container container, final Context context) 
	{
		super(bridge, container, context);
		containerDesign = new FutoshikiDesign(this, boardPlacement);
	}

	//-------------------------------------------------------------------------

}
