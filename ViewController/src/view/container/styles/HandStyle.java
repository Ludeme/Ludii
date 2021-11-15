package view.container.styles;

import bridge.Bridge;
import game.equipment.container.Container;
import view.container.BaseContainerStyle;
import view.container.aspects.placement.HandPlacement;

/**
 * Implementation of hand container style.
 * @author matthew.stephenson
 */
public class HandStyle extends BaseContainerStyle
{
	public HandStyle(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
		containerPlacement = new HandPlacement(bridge, this);
	}

	@Override
	public void setDefaultBoardScale(final double scale) 
	{
		// do nothing
	}
	
	//-------------------------------------------------------------------------

}
