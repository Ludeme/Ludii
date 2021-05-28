package view.container.styles.board.graph;

import bridge.Bridge;
import game.equipment.container.Container;
import other.context.Context;
import view.container.aspects.components.board.PenAndPaperComponents;
import view.container.aspects.designs.board.graph.PenAndPaperDesign;

public class PenAndPaperStyle extends GraphStyle
{
	public PenAndPaperStyle(final Bridge bridge, final Container container, final Context context) 
	{
		super(bridge, container, context);
		final PenAndPaperDesign boardDesign = new PenAndPaperDesign(bridge, this, boardPlacement);
		containerDesign = boardDesign;
		containerComponents = new PenAndPaperComponents(bridge, this, boardDesign);
	}

	//-------------------------------------------------------------------------

}
