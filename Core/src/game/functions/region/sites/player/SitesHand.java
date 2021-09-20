package game.functions.region.sites.player;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.container.Container;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.types.play.RoleType;
import game.util.equipment.Region;
import other.context.Context;

/**
 * Returns all the sites in a specific hand.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SitesHand extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** If we can, we'll precompute once and cache. */
	private Region precomputedRegion = null;

	//-------------------------------------------------------------------------

	/**
	 * The index of the hand.
	 */
	private final IntFunction index;

	/**
	 * The role.
	 */
	private final RoleType role;

	/**
	 * @param player Index of the side.
	 * @param role  The Role type corresponding to the index.
	 */
	public SitesHand
	(
		@Or @Opt final game.util.moves.Player player,
		@Or @Opt final RoleType               role
    )
	{
		index = (role != null) ? RoleType.toIntFunction(role) : (player != null) ? player.index() : null;
		this.role = role;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		if (precomputedRegion != null)
			return precomputedRegion;

		if (index == null)
			return new Region();
		
		final int pid = index.eval(context);

		if (pid < 1 || pid > context.game().players().size())
		{
			System.out.println("** Bad player index.");
			return new Region();
		}
		
		for (int id = 0; id < context.containers().length; id++)
		{
			final Container c = context.containers()[id];
			if (c.isHand())
			{
				if 
				(
					role == RoleType.Shared
					||
					c.owner() == pid
				)
				{
					final int[] sites = new int[context.game().equipment().containers()[id].numSites()];

					for (int i = 0; i < sites.length; i++)
						sites[i] = context.game().equipment().sitesFrom()[id] + i;

					return new Region(sites);
				}
			}
		}

		return new Region();
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if (index != null)
			return index.isStatic();
		return true;
	}

	@Override
	public String toString()
	{
		return "Hand()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0L;

		if (index != null)
			flags = index.gameFlags(game);

		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (index != null)
			concepts.or(index.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (index != null)
			writeEvalContext.or(index.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (index != null)
			readEvalContext.or(index.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		boolean gameHasHands = false;
		for (final Container c : game.equipment().containers())
		{
			if (c.isHand())
			{
				gameHasHands = true;
				break;
			}
		}
		if (!gameHasHands)
		{
			game.addRequirementToReport("The ludeme (sites Hand ...) is used but the equipment has no hands.");
			missingRequirement = true;
		}
		if (index != null)
			missingRequirement |= index.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (index != null)
			willCrash |= index.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (index != null)
			index.preprocess(game);
		if (isStatic())
			precomputedRegion = eval(new Context(game, null));
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public boolean isHand()
	{
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "the sites in Player " + index.toEnglish(game) + "'s hand";
	}
	
	//-------------------------------------------------------------------------
}
