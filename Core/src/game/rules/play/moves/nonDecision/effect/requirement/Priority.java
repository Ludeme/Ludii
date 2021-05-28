package game.rules.play.moves.nonDecision.effect.requirement;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns the first list of moves with a non-empty set of moves.
 * 
 * @author Eric.Piette
 * @remarks To prioritise a list of legal moves over another. For example in
 *          some draughts games, if you can capture, you must capture, if not
 *          you can move normally.
 */
public final class Priority extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** the list of Moves to eval. */
	private final Moves[] list;

	//-------------------------------------------------------------------------

	/**
	 * For selecting the first set of moves with a legal move between many set of
	 * moves.
	 * 
	 * @param list The list of moves.
	 * @param then The moves applied after that move is applied.
	 * 
	 * @example (priority { (forEach Piece "Leopard" (step (to if:(is Enemy (who
	 *          at:(to)))))) (forEach Piece "Leopard" (step (to if:(is In (to)
	 *          (sites Empty))))) })
	 */
	public Priority
	(
			 final Moves[] list,
		@Opt final Then    then
	)
	{
		super(then);
		this.list = list;
	}
	
	/**
	 * For selecting the first set of moves with a legal move between two moves.
	 * 
	 * @param list1 The first set of moves.
	 * @param list2 The second set of moves.
	 * @param then  The moves applied after that move is applied.
	 * 
	 * @example (priority (forEach Piece "Leopard" (step (to if:(is Enemy (who
	 *          at:(to)))))) (forEach Piece "Leopard" (step (to if:(is In (to)
	 *          (sites Empty)))) ))
	 */
	public Priority
	(
			 final Moves list1,
			 final Moves list2,
		@Opt final Then  then
	)
	{
		super(then);
		this.list = new Moves[] {list1, list2};
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		for (final Moves move : list)
		{
			final Moves l = move.eval(context);
			if (!l.moves().isEmpty())
			{
				if (then() != null)
					for (int j = 0; j < l.moves().size(); j++)
						l.moves().get(j).then().add(then().moves());

				// Store the Moves in the computed moves.
				for (int j = 0; j < l.moves().size(); j++)
					l.moves().get(j).setMovesLudeme(move);

				return l;
			}
		}

		return new BaseMoves(super.then());
	}

	//-------------------------------------------------------------------------
	
	@Override
	public boolean canMove(final Context context)
	{
		for (final Moves moves : list)
		{
			if (moves.canMove(context))
				return true;
		}
		
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game);

		for (final Moves moves : list)
			gameFlags |= moves.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.Priority.id(), true);
		for (final Moves moves : list)
			concepts.or(moves.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		for (final Moves moves : list)
			writeEvalContext.or(moves.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		for (final Moves moves : list)
			readEvalContext.or(moves.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		for (final Moves moves : list)
			missingRequirement |= moves.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		for (final Moves moves : list)
			willCrash |= moves.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		boolean isStatic = true;

		for (final Moves moves : list)
			isStatic = isStatic && moves.isStatic();

		return isStatic;
	}

	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);
		
		for (final Moves moves : list)
			moves.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Array of Moves
	 */
	public Moves[] list()
	{
		return list;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "Priority";
	}
}
