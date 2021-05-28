package metadata.graphics.others;

import java.util.BitSet;

import game.Game;
import game.types.component.SuitType;
import metadata.graphics.GraphicsItem;

/**
 * Indicates the ranking for card suits (lowest to highest).
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Should be used only for card games.
 */
public class SuitRanking implements GraphicsItem
{
	/** Ranking for the card suits. */
	private final SuitType[] suitRanking;
		
	//-------------------------------------------------------------------------

	/**
	 * @param suitRanking Ranking for card suits.
	 * 
	 * @example (suitRanking {Spades Hearts Diamonds Clubs})
	 */
	public SuitRanking
	(
		final SuitType[] suitRanking
	)
	{
		this.suitRanking = suitRanking;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Ranking for the card suits.
	 */
	public SuitType[] suitRanking()
	{
		return suitRanking;
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
