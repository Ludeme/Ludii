package metadata.graphics.show.Boolean;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Indicates whether the directions of the Edges should be shown (only valid for Graph Games).
 * 
 * @author Matthew.Stephenson
 */
@Hide
public class ShowEdgeDirections implements GraphicsItem
{
	/** If the edge directions should be shown. */
	private final boolean showEdgeDirections;
		
	//-------------------------------------------------------------------------

	/**
	 * @param showEdgeDirections  Whether the edge directions should be shown [True].
	 */
	public ShowEdgeDirections
	(
		@Opt final Boolean showEdgeDirections
	)
	{
		this.showEdgeDirections = (showEdgeDirections == null) ? true : showEdgeDirections.booleanValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return If the edge directions should be shown.
	 */
	public boolean showEdgeDirections()
	{
		return showEdgeDirections;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		return concepts;
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long gameFlags = 0l;
		return gameFlags;
	}

	@Override
	public boolean needRedraw()
	{
		return false;
	}

}
