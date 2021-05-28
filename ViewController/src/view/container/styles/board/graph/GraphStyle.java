package view.container.styles.board.graph;

import java.awt.Color;

import bridge.Bridge;
import game.equipment.container.Container;
import other.context.Context;
import view.container.aspects.designs.board.graph.GraphDesign;
import view.container.styles.board.puzzle.PuzzleStyle;

public class GraphStyle extends PuzzleStyle
{
	protected final Color baseGraphColour = new Color(200, 200, 200);
	
	protected double baseVertexRadius = 6;
	protected double baseLineWidth    = 0.5 * baseVertexRadius;
	
	//-------------------------------------------------------------------------

	/**
	 * @param container The container to draw (typically the board). 
	 */
	public GraphStyle(final Bridge bridge, final Container container, final Context context) 
	{
		super(bridge, container, context);
		containerDesign = new GraphDesign(this, boardPlacement, true, true);
	}
	
	//-------------------------------------------------------------------------
	
	public Color baseGraphColour()
	{
		return baseGraphColour;
	}
	
	//-------------------------------------------------------------------------
	
	public double baseVertexRadius()
	{
		return baseVertexRadius;
	}
	
	public void setBaseVertexRadius(final double vr)
	{
		baseVertexRadius = vr;
	}

	public double baseLineWidth()
	{
		return baseLineWidth;
	}
		
	public void setBaseLineWidth(final double lw)
	{
		baseLineWidth = lw;
	}

	//-------------------------------------------------------------------------

}
