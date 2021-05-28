package game.functions.region;

import annotations.Anon;
import annotations.Hide;
import game.Game;
import game.util.equipment.Region;
import other.context.Context;

/**
 * A fixed region of sites that does not change during the game.
 * 
 * @author cambolbro
 */
@Hide
public final class RegionConstant extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;
	
	/** Which region. */
	private final Region region;

	//-------------------------------------------------------------------------

	/**
	 * @param region The constant region to set.
	 */
	public RegionConstant(@Anon final Region region)
	{
		this.region = region;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		return region;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return 0;
	}
	
	@Override
	public void preprocess(final Game game)
	{
		// nothing to do
	}
}
