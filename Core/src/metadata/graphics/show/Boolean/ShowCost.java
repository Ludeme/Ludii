package metadata.graphics.show.Boolean;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;

//-----------------------------------------------------------------------------

/**
 * Indicates whether the cost of the different graph element should be shown.
 * 
 * @author Eric.Piette
 */
@Hide
public class ShowCost implements GraphicsItem
{
	/** If the cost should be shown. */
	private final boolean showCost;
		
	//-------------------------------------------------------------------------

	/**
	 * @param showCost  Whether the cost should be shown [True].
	 */
	public ShowCost
	(
		@Opt final Boolean showCost
	)
	{
		this.showCost = (showCost == null) ? true : showCost.booleanValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return If the cost should be shown.
	 */
	public boolean showCost()
	{
		return showCost;
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
