package game.rules.play.moves.nonDecision.operators.logical;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Then;
import game.rules.play.moves.nonDecision.operator.Operator;
import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;

/**
 * Generates all combinations (i.e. the cross product) between two lists of moves.
 * 
 * @author Cameron Browne
 */
public final class AllCombinations extends Operator
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The first list to cross. */
	private final Moves listA;
	
	/** The second list to cross. */
	private final Moves listB;

	//-------------------------------------------------------------------------

	/**
	 * @param listA The first list.
	 * @param listB The second list.
	 * @param then  The moves applied after that move is applied.
	 * 
	 * @example (allCombinations (add (piece (id "Disc0") state:(mover)) (to
	 *          (site))) (flip (between)) )
	 */
	public AllCombinations
	(
			 final Moves listA,
			 final Moves listB,
		@Opt final Then  then
	)
	{
		super(then);
		this.listA = listA;
		this.listB = listB;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		// Return intersected list of moves
		final Moves moves = new BaseMoves(super.then());
		
		final FastArrayList<Move> ev1 = listA.eval(context).moves();

		final FastArrayList<Move> ev2 = listB.eval(context).moves();

		for (final Move m1 : ev1)
		{
			for (final Move m2 : ev2) 
			{
//				System.out.println("---\n" + m1);
//				System.out.println("---\n" + m2);

				final Move newMove = new Move(m1, m2);
				if (then() != null) 
				{
					newMove.then().add(then().moves());
				}
				moves.moves().add(newMove);
			}
		}

		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = listA.gameFlags(game) | listB.gameFlags(game) | super.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(listB.concepts(game));
		concepts.or(listA.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(listB.writesEvalContextRecursive());
		writeEvalContext.or(listA.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(listB.readsEvalContextRecursive());
		readEvalContext.or(listA.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= listB.missingRequirement(game);
		missingRequirement |= listA.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= listB.willCrash(game);
		willCrash |= listA.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return listA.isStatic() && listB.isStatic();
	}
	
	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);

		listA.preprocess(game);
		listB.preprocess(game);
	}
	
	@Override
	public String toEnglish(final Game game)
	{
		return listA.toEnglish(game) + ", then " + listB.toEnglish(game);
	}
	
}
