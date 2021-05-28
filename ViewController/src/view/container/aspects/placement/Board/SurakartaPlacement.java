package view.container.aspects.placement.Board;

import java.awt.Rectangle;

import bridge.Bridge;
import game.types.board.SiteType;
import other.context.Context;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class SurakartaPlacement extends BoardPlacement
{
	public SurakartaPlacement(final Bridge bridge, final BoardStyle containerStyle) 
	{
		super(bridge, containerStyle);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void setPlacement(final Context context, final Rectangle placement)
	{
		final int rows = boardStyle.container().topology().rows(SiteType.Vertex).size() - 1;
		final int cols = boardStyle.container().topology().columns(SiteType.Vertex).size() - 1;

		final double maxDim = Math.max(rows, cols);
		int extra = Math.min(rows, cols) / 2;
		final int numLoops = container().tracks().size() / 2;
		if (numLoops >= extra)
			extra += 1;
		final double fullDim = maxDim + extra * 2;
		
		// Enlarge scale to reduce outer margin
		switch (topology().graph().basis())
		{
		case Square:  	 containerScale = 1.1 * (maxDim) / fullDim; break;
		case Triangular: containerScale = 0.9 * (maxDim) / fullDim; break;
		//$CASES-OMITTED$
		default: System.out.println("** Board type " + topology().graph().basis() + " not supported for Surkarta.");
		}
		
		setUnscaledPlacement(placement);
		this.placement = new Rectangle(
										(int)(placement.getX() + placement.getWidth() * (1.0-containerScale)/2), 
										(int)(placement.getY() + placement.getHeight() * (1.0-containerScale)/2), 
										(int)(placement.getWidth() * (containerScale)), 
										(int)(placement.getHeight() * (containerScale))
										);
		
		setCellRadiusPixels((int) (cellRadius() * placement.width * containerScale));
	}
	
	//-------------------------------------------------------------------------

}
