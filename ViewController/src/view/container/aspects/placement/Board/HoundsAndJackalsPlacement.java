package view.container.aspects.placement.Board;

import java.awt.Rectangle;
import java.awt.geom.Point2D;

import bridge.Bridge;
import other.context.Context;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class HoundsAndJackalsPlacement extends BoardPlacement
{
	public HoundsAndJackalsPlacement(final Bridge bridge, final BoardStyle containerStyle) 
	{
		super(bridge, containerStyle);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void setPlacement(final Context context, final Rectangle placement)
	{
		setCustomPlacement(context, placement, new Point2D.Double(0.5, 0.6), 0.7);
	}
	
	//-------------------------------------------------------------------------

}
