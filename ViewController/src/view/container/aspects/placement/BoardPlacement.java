package view.container.aspects.placement;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import bridge.Bridge;
import game.types.board.SiteType;
import other.context.Context;
import util.ContainerUtil;
import view.container.styles.BoardStyle;

public class BoardPlacement extends ContainerPlacement
{
	protected BoardStyle boardStyle;
	
	//-------------------------------------------------------------------------

	/** Scale of the board relative to the original placement size (10% margins either side). */
	protected double defaultBoardScale = 0.8;
	
	//-------------------------------------------------------------------------
	
	public BoardPlacement(final Bridge bridge, final BoardStyle containerStyle) 
	{
		super(bridge, containerStyle);
		containerScale = defaultBoardScale;
		boardStyle = containerStyle;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Customise graph element locations, if needed.
	 */
	public void customiseGraphElementLocations(final Context context)
	{
		// Note: Do not customise graph element locations based on their current
		//       locations, otherwise multiple calls will produce cumulative changes. 
		
		// Customise graph element locations here:
		// ...

		// Then call the following:
		ContainerUtil.normaliseGraphElements(topology());
		ContainerUtil.centerGraphElements(topology());
		calculateCellRadius();
		resetPlacement(context);
	}	
	
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param placement
	 * @param boardCenter
	 * @param scale			The scale of the board by default.
	 * @param context
	 */
	public void setCustomPlacement(final Context context, final Rectangle placement, final Point2D boardCenter, final double scale)
	{
		final Rectangle unscaledPlacement = new Rectangle(placement.x, placement.y, placement.width + placement.x, placement.height);
		setUnscaledPlacement(unscaledPlacement);
		containerScale = scale;
		
		this.placement = new Rectangle(
										(int)(placement.getX() + placement.getWidth() * (1.0-scale) * boardCenter.getX()), 
										(int)(placement.getY() + placement.getHeight() * (1.0-scale) * boardCenter.getY()), 
										(int)(placement.getWidth() * (scale)), 
										(int)(placement.getHeight() * (scale))
										);
		
		setCellRadiusPixels((int) (cellRadius() * this.placement.width));
	}
	
	@Override
	public void setPlacement(final Context context, final Rectangle placement)
	{
		final Point2D.Double boardCenter = new Point2D.Double(0.5, 0.5);
		
		if (context.board().defaultSite() == SiteType.Vertex)
			containerScale = defaultBoardScale - cellRadius();
		else
			containerScale = defaultBoardScale;
		
		if (context.game().metadata().graphics().boardPlacement() != null)
		{
			final Rectangle2D metadataPlacement = context.game().metadata().graphics().boardPlacement();
			boardCenter.x += metadataPlacement.getX();
			boardCenter.y += metadataPlacement.getY();
			containerScale *= metadataPlacement.getWidth();
		}

		setCustomPlacement(context, placement, boardCenter, containerScale);
	}
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Resets the placement of the container.
	 * Needs to be called if the position of any vertices in the graph are shifted.
	 */
	public void resetPlacement(final Context context)
	{
		setPlacement(context, unscaledPlacement());
	}
	//-------------------------------------------------------------------------
	
	public void setDefaultBoardScale(final double scale)
	{
		defaultBoardScale = scale;
	}

	//-------------------------------------------------------------------------
	
}
