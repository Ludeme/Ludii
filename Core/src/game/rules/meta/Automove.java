package game.rules.meta;

import java.util.BitSet;

import game.Game;
import game.rules.phase.Phase;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import main.Constants;
import other.MetaRules;
import other.concept.Concept;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;
import other.move.MoveUtilities;

/**
 * To apply automatically to the game all the legal moves only applicable to a
 * single site.
 * 
 * @author Eric.Piette
 */
public class Automove extends MetaRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @example (automove)
	 */
	public Automove()
	{
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{
		context.game().metaRules().setAutomove(true);
	}
	
	/**
	 * @param context The context.
	 * @param move The original move.
	 */
	public static void apply(final Context context, final Move move)
	{
		final Game game = context.game();
		final MetaRules metaRules = game.metaRules();
		if (metaRules.automove())
		{
			boolean repeatAutoMove = true;

			while (repeatAutoMove)
			{
				repeatAutoMove = false;

				if (context.state().isDecided() != Constants.UNDEFINED)
					context.state().setIsDecided(Constants.UNDEFINED);

				final Context newContext = new TempContext(context);
				game.applyInternal(newContext, move, false);

				final int mover = newContext.state().mover();
				final int indexPhase = newContext.state().currentPhase(mover); 
				final Phase phase = newContext.game().rules().phases()[indexPhase];
				final Moves newLegalMoves = phase.play().moves().eval(newContext);

				for (int j = 0; j < newLegalMoves.moves().size() - 1; j++)
				{
					final Move newMove = newLegalMoves.get(j);
					final int site = newMove.toNonDecision();
					int cpt = 1;
					for (int k = j + 1; k < newLegalMoves.moves().size(); k++)
					{
						if (site == newLegalMoves.moves().get(k).toNonDecision())
							cpt++;
					}

					if (cpt != 1)
					{
						for (int k = 0; k < newLegalMoves.moves().size(); k++)
						{
							if (newLegalMoves.moves().get(k).toNonDecision() == site)
							{
								newLegalMoves.moves().remove(k);
								k--;
							}
						}
						j--;
					}
				}

				if (!newLegalMoves.moves().isEmpty())
				{
					final Moves forcedMoves = new BaseMoves(null);
					for (int j = 0; j < newLegalMoves.moves().size(); j++)
						MoveUtilities.chainRuleCrossProduct(newContext, forcedMoves, null,
								newLegalMoves.moves().get(j), false);
					move.then().add(forcedMoves);

					repeatAutoMove = true;
				}
			}
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		return 0;
	}

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Nothing to do.
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.CopyContext.id(), true);
		concepts.set(Concept.AutoMove.id(), true);
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

		if (!(obj instanceof Automove))
			return false;

		return true;
	}
}