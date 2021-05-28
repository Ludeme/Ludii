package metadata.graphics.show.Boolean;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Indicates whether the owner of each region should be shown.
 * 
 * @author cambolbro
 * 
 * @remarks This is useful for graph games to indicate special sites. 
 */
@Hide
public class ShowRegionOwner implements GraphicsItem
{
	private final boolean show;
		
	//-------------------------------------------------------------------------

	/**
	 * @param show  Whether to show the owner of each region [True].
	 */
	public ShowRegionOwner
	(
		@Opt final Boolean show
	)
	{
		this.show = (show == null) ? true : show.booleanValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return If the board should be hidden.
	 */
	public boolean show()
	{
		return show;
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
