package metadata.graphics.show.Boolean;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Indicates whether curved edges should be shown.
 * 
 * @author Matthew.Stephenson
 */
@Hide
public class ShowCurvedEdges implements GraphicsItem
{
	/** If the curved edges should be shown. */
	private final boolean showCurvedEdges;
		
	//-------------------------------------------------------------------------

	/**
	 * @param showCurvedEdges  Whether the curved edges should be shown [True].
	 */
	public ShowCurvedEdges
	(
		@Opt final Boolean showCurvedEdges
	)
	{
		this.showCurvedEdges = (showCurvedEdges == null) ? true : showCurvedEdges.booleanValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return If the curved edges should be shown.
	 */
	public boolean showCurvedEdges()
	{
		return showCurvedEdges;
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
