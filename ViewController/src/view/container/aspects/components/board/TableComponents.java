package view.container.aspects.components.board;

import bridge.Bridge;
import view.container.BaseContainerStyle;
import view.container.aspects.components.ContainerComponents;

/**
 * Table components properties.
 * 
 * @author Eric.Piette
 */
public class TableComponents extends ContainerComponents
{
	public TableComponents(final Bridge bridge, final BaseContainerStyle containerStyle)
	{
		super(bridge, containerStyle);
		setPieceScale(1.1);
	}
}
