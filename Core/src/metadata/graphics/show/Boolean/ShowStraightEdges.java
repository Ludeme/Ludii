package metadata.graphics.show.Boolean;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Indicates whether straight edges should be shown.
 * 
 * @author Matthew.Stephenson
 */
@Hide
public class ShowStraightEdges implements GraphicsItem
{
	/** If the curved edges should be shown. */
	private final boolean showStraightEdges;
		
	//-------------------------------------------------------------------------

	/**
	 * @param showStraightEdges  Whether the straight edges should be shown [True].
	 */
	public ShowStraightEdges
	(
		@Opt final Boolean showStraightEdges
	)
	{
		this.showStraightEdges = (showStraightEdges == null) ? true : showStraightEdges.booleanValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return If the straight edges should be shown.
	 */
	public boolean showStraightEdges()
	{
		return showStraightEdges;
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
