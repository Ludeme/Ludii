package game.functions.ints.board;

import java.util.BitSet;

import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.container.Container;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.types.play.RoleType;
import game.types.state.GameType;
import main.Constants;
import other.context.Context;

/**
 * Returns one site of one hand.
 * 
 * @author Eric Piette
 * 
 * @remarks To check a specific site of a specific hand.
 */
public final class HandSite extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which player. */ 
	private final IntFunction playerId;
	
	/** Which site. */
	private final IntFunction siteFn;
	
	/** Precomputed value if possible. */
	private int precomputedValue = Constants.OFF;

	//-------------------------------------------------------------------------

	/**
	 * Return one site of one hand.
	 * 
	 * @param indexPlayer The index of the owner of the hand.
	 * @param role        The roleType of the owner of the hand.
	 * @param site        The site on the hand.
	 * @example (handSite Mover)
	 */
	public HandSite
	(
		@Or 	 final IntFunction indexPlayer,
		@Or 	 final RoleType    role,
			@Opt final IntFunction site
	)
	{
		int numNonNull = 0;
		if (indexPlayer != null)
			numNonNull++;
		if (role != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");

		if (indexPlayer != null)
			playerId = indexPlayer;
		else
			playerId = RoleType.toIntFunction(role);		
		
		siteFn = (site == null) ? new IntConstant(0) : site;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (precomputedValue != Constants.OFF)
			return precomputedValue;
		
		final int player = playerId.eval(context);
		final int index  = siteFn.eval(context);
		
		for (final Container c : context.containers())
		{
			if (c.isHand())
			{
				if (c.owner() == player) 
					return context.sitesFrom()[c.index()] + index;
			}
		}
		
		return Constants.OFF;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return siteFn.isStatic() && playerId.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return GameType.Count | siteFn.gameFlags(game) | playerId.gameFlags(game);
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(siteFn.concepts(game));
		concepts.or(playerId.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(siteFn.writesEvalContextRecursive());
		writeEvalContext.or(playerId.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(siteFn.readsEvalContextRecursive());
		readEvalContext.or(playerId.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		siteFn.preprocess(game);
		playerId.preprocess(game);
		
		if (isStatic())
			precomputedValue = eval(new Context(game, null));
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
			game.addRequirementToReport("The ludeme (handSite ...) is used but the equipment has no hands.");
			missingRequirement = true;
		}

		missingRequirement |= siteFn.missingRequirement(game);
		missingRequirement |= playerId.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= siteFn.willCrash(game);
		willCrash |= playerId.willCrash(game);
		return willCrash;
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
		return "Player " + playerId.toEnglish(game) + "'s hand site " + siteFn.toEnglish(game);
	}

	//-------------------------------------------------------------------------
		
}
