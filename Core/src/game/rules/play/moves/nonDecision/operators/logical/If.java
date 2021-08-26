package game.rules.play.moves.nonDecision.operators.logical;

import java.util.BitSet;
import java.util.function.BiPredicate;

import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Then;
import game.rules.play.moves.nonDecision.operator.Operator;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.move.MovesIterator;

/**
 * Returns, depending on the condition, a list of legal moves or an other list.
 * 
 * @author Eric.Piette and cambolbro
 */
public final class If extends Operator
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which condition. */
	final BooleanFunction cond;

	/** If the condition is true. */
	final Moves list;

	/** If the condition if false. */
	final Moves elseList;

	//-------------------------------------------------------------------------

	/**
	 * @param cond     The condition to satisfy to get the first list of legal
	 *                 moves.
	 * @param list     The first list of legal moves.
	 * @param elseList The other list of legal moves if the condition is not
	 *                 satisfied.
	 * @param then     The moves applied after that move is applied.
	 * 
	 * @example (if (is Mover P1) (moveAgain))
	 * 
	 * @example (if (is Mover P1) (moveAgain) (remove (last To)))
	 */
	public If
	(
			 final BooleanFunction cond,
			 final Moves           list,
		@Opt final Moves           elseList,
		@Opt final Then            then
	)
	{
		super(then);
		this.cond = cond;
		this.list = list;
		this.elseList = elseList;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public MovesIterator movesIterator(final Context context)
	{
		return new MovesIterator()
				{
			
					protected MovesIterator itr = computeItr();

					@Override
					public boolean hasNext()
					{
						return itr != null && itr.hasNext();
					}

					@Override
					public Move next()
					{
						final Move next = itr.next();
						
						if (then() != null)
							next.then().add(then().moves());
						
						return next;
					}
					
					/**
					 * Computes which iterator to use for given context
					 * @return Moves iterator
					 */
					private MovesIterator computeItr()
					{
						if (cond.eval(context)) 
							return list.movesIterator(context);
						else if (elseList != null)
							return elseList.movesIterator(context);
						else
							return null;
					}

					@Override
					public boolean canMoveConditionally(final BiPredicate<Context, Move> predicate)
					{
						return itr.canMoveConditionally(predicate);
					}
				};
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		if (cond.eval(context)) 
		{
			final Moves moves = list.eval(context);
			if (then() != null)
				for (int j = 0; j < moves.moves().size(); j++)
					moves.moves().get(j).then().add(then().moves());

			return moves;
		}
		else if (elseList != null) 
		{
			final Moves moves = elseList.eval(context);
			if (then() != null)
				for (int j = 0; j < moves.moves().size(); j++)
					moves.moves().get(j).then().add(then().moves());

			return moves;
		}

		final Moves moves = new BaseMoves(super.then());
		
		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());
		
		return new BaseMoves(super.then());
	}

	//-------------------------------------------------------------------------
	
	@Override
	public boolean canMove(final Context context)
	{
		if (cond.eval(context)) 
			return list.canMove(context);
		else if (elseList != null)
			return elseList.canMove(context);
		
		return false;
	}
	
	@Override
	public boolean canMoveTo(final Context context, final int target)
	{
		if (cond.eval(context))
			return list.canMoveTo(context, target);
		else if (elseList != null)
			return elseList.canMoveTo(context, target);
		else
			return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game);

		if (cond != null)
			gameFlags |= cond.gameFlags(game);

		if (list != null)
			gameFlags |= list.gameFlags(game);

		if (elseList != null)
			gameFlags |= elseList.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));

		if (cond != null)
			concepts.or(cond.concepts(game));

		if (list != null)
			concepts.or(list.concepts(game));

		if (elseList != null)
			concepts.or(elseList.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		concepts.set(Concept.ConditionalStatement.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (cond != null)
			writeEvalContext.or(cond.writesEvalContextRecursive());

		if (list != null)
			writeEvalContext.or(list.writesEvalContextRecursive());

		if (elseList != null)
			writeEvalContext.or(elseList.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		if (cond != null)
			readEvalContext.or(cond.readsEvalContextRecursive());

		if (list != null)
			readEvalContext.or(list.readsEvalContextRecursive());

		if (elseList != null)
			readEvalContext.or(elseList.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		if (cond != null)
			missingRequirement |= cond.missingRequirement(game);

		if (list != null)
			missingRequirement |= list.missingRequirement(game);

		if (elseList != null)
			missingRequirement |= elseList.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		if (cond != null)
			willCrash |= cond.willCrash(game);

		if (list != null)
			willCrash |= list.willCrash(game);

		if (elseList != null)
			willCrash |= elseList.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		if (cond != null && !cond.isStatic())
			return false;

		if (list != null && !list.isStatic())
			return false;

		if (elseList != null && !elseList.isStatic())
			return false;

		return true;
	}
	
	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);
		
		if (cond != null)
			cond.preprocess(game);
		
		if (list != null)
			list.preprocess(game);
		
		if (elseList != null)
			elseList.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Our condition
	 */
	public BooleanFunction cond()
	{
		return cond;
	}
	
	/**
	 * @return Move generator if condition holds
	 */
	public Moves list()
	{
		return list;
	}
	
	/**
	 * @return Move generator if condition does not hold
	 */
	public Moves elseList()
	{
		return elseList;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String text="";
		text+= "if " + cond.toEnglish(game);
		if(list != null) {
			if(!list.toEnglish(game).equals(""))
				text+= ", "+ list.toEnglish(game);
		}
		if(elseList != null)
			if(!elseList.toEnglish(game).equals(""))
				text+= ", else "+ elseList.toEnglish(game);
		return text;
	}
}
