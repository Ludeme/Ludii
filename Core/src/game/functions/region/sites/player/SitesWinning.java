package game.functions.region.sites.player;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.NonDecision;
import game.types.play.RoleType;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;

/**
 * Returns the winning positions for a player.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks Useful to avoid the "kingmaker" effect in multiplayer games.
 */
@Hide
public final class SitesWinning extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The index of the player. */
	private final IntFunction indexFn;
	
	/** The generator we use to generate moves for which we check whether they're winning */
	private final NonDecision movesGenerator;

	//-------------------------------------------------------------------------

	/**
	 * @param player The index of the player.
	 * @param role   The roleType of the player.
	 * @param moves  The moves for which to check which ones lead to wins.
	 */
	public SitesWinning
	(
		@Or @Opt final game.util.moves.Player player, 
		@Or @Opt final RoleType               role,
			     final NonDecision            moves
	)
	{
		this.indexFn = (role != null) ? RoleType.toIntFunction(role) : (player != null) ? player.index() : null;
		this.movesGenerator = moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		final TIntArrayList winningPositions = new TIntArrayList();

		if (indexFn == null)
			return new Region();

		final int pid = indexFn.eval(context);

		if (pid == context.state().mover())
		{
			final int mover = context.state().mover();
			final Moves legalMoves = movesGenerator.eval(context);

			for (final Move m : legalMoves.moves())
			{
				if (m.toNonDecision() != -1 && !winningPositions.contains(m.toNonDecision()))
				{
					final Context newContext = new TempContext(context);
					newContext.game().apply(newContext, m);
					if (newContext.winners().contains(mover))
					{
						winningPositions.add(m.toNonDecision());
						newContext.winners().remove(mover);
					}
				}
			}
		}
		else
		{
			// We compute the legal move of this player in this state
			final Context newContext = new Context(context);
			newContext.setMoverAndImpliedPrevAndNext(pid);
			
			final Moves legalMoves = movesGenerator.eval(newContext);

			for (final Move m : legalMoves.moves())
			{
				if (m.toNonDecision() != -1 && !winningPositions.contains(m.toNonDecision()))
				{
					final Context newNewContext = new TempContext(newContext);
					newNewContext.game().apply(newNewContext, m);
					if (newNewContext.winners().contains(pid))
					{
						winningPositions.add(m.toNonDecision());
						newNewContext.winners().remove(pid);
					}
				}
			}
		}

		return new Region(winningPositions.toArray());
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = movesGenerator.gameFlags(game);
		
		if (indexFn != null)
			flags |= indexFn.gameFlags(game);
		
		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(movesGenerator.concepts(game));
		concepts.set(Concept.CopyContext.id(), true);

		if (indexFn != null)
			concepts.or(indexFn.concepts(game));
		return concepts;
	}
	
	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(movesGenerator.writesEvalContextRecursive());

		if (indexFn != null)
			writeEvalContext.or(indexFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(movesGenerator.readsEvalContextRecursive());

		if (indexFn != null)
			readEvalContext.or(indexFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= movesGenerator.missingRequirement(game);
		if (indexFn != null)
			missingRequirement |= indexFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= movesGenerator.willCrash(game);
		if (indexFn != null)
			willCrash |= indexFn.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (indexFn != null)
			indexFn.preprocess(game);
		
		movesGenerator.preprocess(game);
	}
}
