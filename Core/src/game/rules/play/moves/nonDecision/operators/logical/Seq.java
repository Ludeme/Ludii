package game.rules.play.moves.nonDecision.operators.logical;

import java.util.BitSet;

import game.Game;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import other.concept.Concept;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;

/**
 * Applies a sequence of moves one by one.
 * 
 * @author Eric.Piette
 */
public final class Seq extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The sequence of moves. */
	final Moves[] moves;

	//-------------------------------------------------------------------------

	/**
	 * @param moves   Moves to apply one by one.
	 * 
	 * @example (seq {(remove 1) (remove 2)})
	 */
	public Seq
	(
		final Moves[] moves
	)
	{
		super(null);
		this.moves = moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		// Return intersected list of moves
		final Moves result = new BaseMoves(super.then());

		if(moves.length == 0)
			return result;

		Context tempContext = new TempContext(context);
		for(int i = 0; i < moves.length; i++)
		{
			final Moves movesToApply = moves[i];
			for (final Move m : movesToApply.eval(tempContext).moves())
			{
				final Move appliedMove = (Move) m.apply(tempContext, true);
				result.moves().add(appliedMove);
			}
		}
		
		// End result of the previous prior code
//		if (then() != null)
//			for (int j = 0; j < result.moves().size(); j++)
//				result.moves().get(j).then().add(then().moves());

		return result;
	}
	
	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game);

		for(int i = 0; i < moves.length; i++)
			gameFlags |= moves[i].gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.CopyContext.id(), true);
		
		for(int i = 0; i < moves.length; i++)
			concepts.or(moves[i].concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());

		for(int i = 0; i < moves.length; i++)
			writeEvalContext.or(moves[i].writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		for(int i = 0; i < moves.length; i++)
			readEvalContext.or(moves[i].readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		for(int i = 0; i < moves.length; i++)
			missingRequirement |= moves[i].missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		
		for(int i = 0; i < moves.length; i++)
			willCrash |= moves[i].willCrash(game);

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
		super.preprocess(game);
		
		for(int i = 0; i < moves.length; i++)
			moves[i].preprocess(game);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String nextString = "";

		for(int i = 0; i < moves.length-1; i++)
			nextString += moves[i].toEnglish(game) + " ,";
		
		if(moves.length != 0)
			nextString += moves[moves.length-1].toEnglish(game);
		
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
			
		return nextString + thenString;
	}
	
	//-------------------------------------------------------------------------
	
}
