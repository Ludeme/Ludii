package game.rules.meta;

import java.util.BitSet;

import game.Game;
import game.functions.ints.IntConstant;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.state.swap.players.SwapPlayers;
import game.types.state.GameType;
import main.Constants;
import other.MetaRules;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * To activate the swap rule.
 * 
 * @author Eric.Piette
 */
public class Swap extends MetaRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @example (swap)
	 */
	public Swap()
	{
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{
		// Do nothing
	}
	
	/**
	 * @param context The context.
	 * @param legalMoves The original legal moves.
	 */
	public static void apply(final Context context, final Moves legalMoves)
	{
		final Game game = context.game();
		final Trial trial = context.trial();
		final int mover = context.state().mover();
		final MetaRules metaRules = game.metaRules();
		if (metaRules.usesSwapRule() && trial.moveNumber() == context.game().players().count() - 1)
		{
			final int moverLastTurn = context.trial().lastTurnMover(mover);
			if(mover != moverLastTurn && moverLastTurn != Constants.UNDEFINED)
				legalMoves.moves()
						.addAll(new SwapPlayers(new IntConstant(mover), null, new IntConstant(moverLastTurn), null, null)
								.eval(context).moves());
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		return GameType.UsesSwapRule;
	}

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Nothing to do
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.SwapPlayersDecision.id(), true);
		concepts.set(Concept.SwapOption.id(), true);
		return concepts;
	}

	@Override
	public int hashCode()
	{
		final int result = 1;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof Swap))
			return false;

		return true;
	}
}