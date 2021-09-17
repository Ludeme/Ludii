package game.rules.play.moves.nonDecision.effect.set.nextPlayer;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.intArray.IntArrayConstant;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.util.moves.Player;
import main.Constants;
import other.action.state.ActionSetNextPlayer;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Is used to set the next player.
 * 
 * @author Eric.Piette and cambolbro
  */
@Hide
public final class SetNextPlayer extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The indices of the possible next player. */
	private final IntArrayFunction nextPlayerFn;
	
	/**
	 * @param who         The data of the next player.
	 * @param nextPlayers The indices of the next players.
	 * @param then        The moves applied after that move is applied.
	 */
	public SetNextPlayer
	(
		     @Or final Player           who,
		     @Or final IntArrayFunction nextPlayers,
		@Opt     final Then           	then
	)
	{
		super(then);

		int numNonNull = 0;
		if (who != null)
			numNonNull++;
		if (nextPlayers != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Only one Or parameter can be non-null.");

		if (nextPlayers != null)
			nextPlayerFn = nextPlayers;
		else
			nextPlayerFn = new IntArrayConstant(new IntFunction[]
			{ who.index() });
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		final int[] nextPlayerIds = nextPlayerFn.eval(context);

		for (final int nextPlayerId : nextPlayerIds)
		{
			if (nextPlayerId < 1 || nextPlayerId > context.game().players().count())
			{
				System.err.println("The Player " + nextPlayerId + " can not be set");
				continue;
			}
			
			final ActionSetNextPlayer actionSetNextPlayer = new ActionSetNextPlayer(nextPlayerId);
			if (isDecision())
				actionSetNextPlayer.setDecision(true);
			final Move move = new Move(actionSetNextPlayer);
			move.setFromNonDecision(Constants.OFF);
			move.setToNonDecision(Constants.OFF);
			move.setMover(context.state().mover());
			moves.moves().add(move);
		}

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean canMoveTo(final Context context, final int target)
	{
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game) | nextPlayerFn.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));

		if (isDecision())
			concepts.set(Concept.SetNextPlayer.id(), true);

		concepts.or(nextPlayerFn.concepts(game));
				
		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(nextPlayerFn.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(nextPlayerFn.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= nextPlayerFn.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= nextPlayerFn.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return nextPlayerFn.isStatic();
	}

	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);
		nextPlayerFn.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "set the next mover to Player " + nextPlayerFn.toEnglish(game) + thenString;
	}
	
	//-------------------------------------------------------------------------

}
