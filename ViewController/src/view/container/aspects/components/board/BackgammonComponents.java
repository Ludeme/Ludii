package view.container.aspects.components.board;

import bridge.Bridge;
import view.container.BaseContainerStyle;
import view.container.aspects.components.ContainerComponents;

/**
 * Backgammon components properties.
 * 
 * @author Matthew.Stephenson
 */
public class BackgammonComponents extends ContainerComponents
{
	public BackgammonComponents(final Bridge bridge, final BaseContainerStyle containerStyle) 
	{
		super(bridge, containerStyle);
		setPieceScale(1.1);
	}
	
	//-------------------------------------------------------------------------

}
