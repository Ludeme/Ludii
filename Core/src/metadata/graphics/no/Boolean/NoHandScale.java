package metadata.graphics.no.Boolean;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Indicates whether pieces drawn in the hand should be scaled or not.
 * 
 * @author Matthew.Stephenson
 * 
 */
@Hide
public class NoHandScale implements GraphicsItem
{
	/** If pieces drawn in the hand should not be scaled. */
	private final boolean noHandScale;
		
	//-------------------------------------------------------------------------

	/**
	 * @param noHandScale  Whether pieces drawn in the hand should not be scaled. [True].
	 */
	public NoHandScale
	(
		@Opt final Boolean noHandScale
	)
	{
		this.noHandScale = (noHandScale == null) ? true : noHandScale.booleanValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return If pieces drawn in the hand should not be scaled.
	 */
	public boolean noHandScale()
	{
		return noHandScale;
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
