package metadata.graphics.show.sites;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.types.board.SiteType;
import metadata.graphics.GraphicsItem;

/**
 * Indicates whether the sites of the board should have their index displayed.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Only used by a specific number of games (e.g. Game of the Goose).
 */
@Hide
public class ShowSitesIndex implements GraphicsItem
{
	/** Site Type. */
	private final SiteType type;

	/** Additional value to add to the index. */
	private final Integer additionalValue;
	
	//-------------------------------------------------------------------------

	/**
	 * @param type 				Site Type [Cell]
	 * @param additionalValue   Additional value to add to the index [0]
	 */
	public ShowSitesIndex
	(
		@Opt final SiteType type,
		@Opt final Integer additionalValue
	)
	{
		this.type = type == null ? SiteType.Cell : type;
		this.additionalValue = additionalValue == null ? Integer.valueOf(0) : additionalValue;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Site Type.
	 */
	public SiteType type()
	{
		return type;
	}

	/**
	 * @return Additional value to add to the index
	 */
	public Integer additionalValue()
	{
		return additionalValue;
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
