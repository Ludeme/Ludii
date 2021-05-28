package game.functions.region;

import annotations.Hide;
import game.Game;
import game.types.board.SiteType;
import other.BaseLudeme;
import other.context.Context;

/**
 * Default implementations of region functions - override where necessary.
 * 
 * @author mrraow
 */
@Hide
public abstract class BaseRegionFunction extends BaseLudeme implements RegionFunction
{
	private static final long serialVersionUID = 1L;

	/** Cell, Edge or Vertex. */
	protected SiteType type;

	//-------------------------------------------------------------------------
	
	@Override
	public boolean contains(final Context context, final int location)
	{
		return eval(context).contains(location);
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public SiteType type(final Game game)
	{
		return (type != null) ? type : game.board().defaultSite();
	}
}
