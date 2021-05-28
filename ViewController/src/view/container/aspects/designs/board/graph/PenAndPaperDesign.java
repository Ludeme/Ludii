package view.container.aspects.designs.board.graph;

import bridge.Bridge;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class PenAndPaperDesign extends GraphDesign
{
	public PenAndPaperDesign(final Bridge bridge, final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement, false, false);
		
		bridge.settingsVC().setNoAnimation(true);
	}
	
	//-------------------------------------------------------------------------

}
