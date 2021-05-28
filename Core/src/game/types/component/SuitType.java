package game.types.component;

import java.util.BitSet;

import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Defines the possible suit types of cards.
 * 
 * @author Eric.Piette and cambolbro
 */
public enum SuitType implements GraphicsItem
{
	/** Club suit. */
	Clubs(1),
	
	/** Spade suit. */
	Spades(2),
	
	/** Diamond suit. */
	Diamonds(3),
	
	/** Heart suit. */
	Hearts(4),
	;
	
	/**
	 * The corresponding value.
	 */
	public final int value;
	
	private SuitType(final int value) 
	{
        this.value = value;
    }

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
