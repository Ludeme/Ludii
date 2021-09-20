package game.rules.play.moves.nonDecision.effect.take.simple;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.equipment.container.Container;
import game.equipment.container.other.Hand;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.board.SiteType;
import game.types.state.GameType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.action.move.ActionAdd;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerState;

/**
 * Takes a domino from those remaining.
 * 
 * @author Eric.Piette and cambolbro
 */
@Hide
public final class TakeDomino extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param then The moves applied after that move is applied.
	 */
	public TakeDomino
	(
		@Opt final Then then
	)
	{
		super(then);
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		final TIntArrayList remainingDominoes = context.state().remainingDominoes();
		if (remainingDominoes.isEmpty())
			return moves;
		int site = Constants.OFF;
		for (final Container container: context.containers())
		{
			if (container.isHand())
			{
				final Hand hand = (Hand) container;
				final ContainerState cs = context.containerState(hand.index());
				if (hand.owner() == context.state().mover())
				{
					final int pid = hand.index();
					final int siteFrom = context.sitesFrom()[pid];
					for (int siteHand = siteFrom; siteHand < siteFrom + hand.numSites(); siteHand++)
						if (cs.whatCell(siteHand) == 0)
						{
							site = siteHand;
							break;
						}
					break;
				}
			}
		}

		if (site == Constants.OFF)
			return moves;
		
		final TIntArrayList available = new TIntArrayList();
		for (int i = 0; i < remainingDominoes.size(); i++)
			available.add(i);

		final int index = context.rng().nextInt(available.size());
		final int what = remainingDominoes.getQuick(index);
		final ActionAdd actionAdd = new ActionAdd(SiteType.Cell, site, what, 1, 0, Constants.UNDEFINED,
				Constants.UNDEFINED,
				null);
		final Move move = new Move(actionAdd);
		moves.moves().add(move);

		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = GameType.LargePiece | GameType.Dominoes | super.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.Domino.id(), true);

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (!game.hasDominoes())
		{
			game.addRequirementToReport("The ludeme (take Domino ...) is used but the equipment has no dominoes.");
			missingRequirement = true;
		}
		missingRequirement |= super.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Nothing todo
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "take a domino" + thenString;
	}

	//-------------------------------------------------------------------------

}
