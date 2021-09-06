package game.rules.play.moves.nonDecision.operators.logical;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.NonDecision;
import game.rules.play.moves.nonDecision.effect.Then;
import game.rules.play.moves.nonDecision.operator.Operator;
import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;

/**
 * Appends a list of moves to each move in a list.
 * 
 * @author Eric.Piette and cambolbro
 */
public final class Append extends Operator
{
	private static final long serialVersionUID = 1L;

	/** The list of moves to append. */
	private final Moves list;

	//-------------------------------------------------------------------------

	/**
	 * @param list The moves to merge.
	 * @param then The moves applied after that move is applied.
	 * 
	 * @example (append (custodial (between if:(is Enemy (state at:(between)))
	 *          (apply (allCombinations (add (piece "Disc0" state:(mover)) (to
	 *          (site))) (flip (between)) ) ) ) (to if:(is Friend (state at:(to))))
	 *          ) (then (and (set Score P1 (count Sites in:(sites State 1)) ) (set
	 *          Score P2 (count Sites in:(sites State 2)) ) ) ) )
	 */
	public Append
	(
			 final NonDecision list,
		@Opt final Then        then
	)
	{
		super(then);
		this.list = list;
	} 

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());
		
		final FastArrayList<Move> evaluated = list.eval(context).moves();		

		for (final Move m : evaluated)
			m.setDecision(true);

		if (evaluated.size() == 0) 
			return moves;
		
		final Move newMove = new Move(evaluated);
		newMove.setMover(context.state().mover());

		moves.moves().add(newMove);
		
		if (then() != null) 
			newMove.then().add(then().moves());

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);
		
		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game) | list.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(list.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(list.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(list.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= list.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= list.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return super.isStatic() && list.isStatic();
	}
	
	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);
		list.preprocess(game);
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		String text = list.toEnglish(game);
		
		if(then() != null)
			text+=", then "+ then().toEnglish(game);
		
		return text;
	}
	
}
