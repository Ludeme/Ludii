package view.container.styles;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.BaseContainerStyle;
import view.container.aspects.axes.BoardAxis;
import view.container.aspects.designs.BoardDesign;
import view.container.aspects.placement.BoardPlacement;

/**
 * @author Matthew.Stephenson and cambolbro
 */
public class BoardStyle extends BaseContainerStyle
{
	protected BoardPlacement boardPlacement;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param container
	 */
	public BoardStyle(final Bridge bridge, final Container container)
	{
		super(bridge, container);
		boardPlacement = new BoardPlacement(bridge, this);
		containerPlacement = boardPlacement;
		containerAxis = new BoardAxis(this, boardPlacement);
		containerDesign = new BoardDesign(this, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

}
