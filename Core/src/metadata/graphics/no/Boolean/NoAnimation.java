package metadata.graphics.no.Boolean;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Indicates whether the movement animation should be disabled.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Should be used in cases where specific BoardStyles or rule combinations 
 *          may cause incorrect animations.
 */
@Hide
public class NoAnimation implements GraphicsItem
{
	/** If animations are disabled. */
	private final boolean noAnimation;
		
	//-------------------------------------------------------------------------

	/**
	 * @param noAnimation  Whether animations are disabled or not [True].
	 * 
	 * @example (noAnimation)
	 */
	public NoAnimation
	(
		@Opt final Boolean noAnimation
	)
	{
		this.noAnimation = (noAnimation == null) ? true : noAnimation.booleanValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return If animations are disabled.
	 */
	public boolean noAnimation()
	{
		return noAnimation;
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
