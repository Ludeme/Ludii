package game.rules.play.moves.nonDecision.operators.logical;

import java.util.BitSet;
import java.util.function.BiPredicate;

import annotations.Opt;
import game.Game;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Then;
import game.rules.play.moves.nonDecision.operator.Operator;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.move.MovesIterator;

/**
 * Moves all the moves in the list if used in a consequence else only one move in the list.
 * 
 * @author Eric.Piette
 */
public final class And extends Operator
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	final Moves[] list;

	//-------------------------------------------------------------------------

	/**
	 * For making a move between two sets of moves.
	 * 
	 * @param movesA The first move.
	 * @param movesB The second move.
	 * @param then   The moves applied after that move is applied.
	 * 
	 * @example (and (set Score P1 100) (set Score P2 100))
	 */
	public And
	(
			 final Moves movesA,
			 final Moves movesB,
		@Opt final Then  then
	)
	{
		super(then);
		list = new Moves[2];
		list[0] = movesA;
		list[1] = movesB;
	}
	
	/**
	 * For making a move between many sets of moves.
	 * 
	 * @param list The list of moves.
	 * @param then The moves applied after that move is applied.
	 * 
	 * @example (and { (set Score P1 100) (set Score P2 100) (set Score P3 100) })
	 */
	public And
	( 
			 final Moves[] list,
		@Opt final Then    then
	)
	{
		super(then);
		this.list = list;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public MovesIterator movesIterator(final Context context)
	{
		return new MovesIterator()
				{
			
					protected int listIdx = 0;
					protected MovesIterator itr = computeNextItr();

					@Override
					public boolean hasNext()
					{
						return (itr != null);
					}

					@Override
					public Move next()
					{
						final Move next = itr.next();
						
						if (!itr.hasNext())
						{
							itr = computeNextItr();
						}
						
						if (then() != null)
							next.then().add(then().moves());
						
						return next;
					}
					
					/**
					 * @return Computes and returns our next moves iterator
					 */
					private MovesIterator computeNextItr()
					{
						while (true)
						{
							if (list.length <= listIdx)
								return null;
							
							final MovesIterator nextItr = list[listIdx++].movesIterator(context);
							
							if (nextItr.hasNext())
								return nextItr;
						}
					}
					
					@Override
					public boolean canMoveConditionally(final BiPredicate<Context, Move> predicate)
					{
						if (itr == null)
							return false;
						
						while (true)
						{
							if (itr.canMoveConditionally(predicate))
								return true;
							
							if (list.length <= listIdx)
								return false;
							
							itr = list[listIdx++].movesIterator(context);
						}
					}
			
				};
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());
		
		for (int i = 0; i < list.length; ++i)
		{
			moves.moves().addAll(list[i].eval(context).moves());
		}

		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		return moves;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public boolean canMoveTo(final Context context, final int target)
	{
		for (final Moves moves : list)
		{
			if (moves.canMoveTo(context, target))
				return true;
		}
		
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "And(" + list + ")";
	}

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

		for (final Moves moves : list)
			concepts.or(moves.concepts(game));
		
		if (then() != null)
			concepts.or(then().concepts(game));

		concepts.set(Concept.Union.id(), true);
		
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
		for (final Moves moves : list)
			if (!moves.isStatic())
				return false;
		return true;
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
	 * @return Array of moves
	 */
	public Moves[] list()
	{
		return list;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String text = "";

		for (final Moves move : list) 
			text += move.toEnglish(game) + " and ";
		
		text = text.substring(0, text.length()-5);
		
		if(then() != null) 
			text += " " + then().moves().toEnglish(game);
		
		return text;
	}
}
