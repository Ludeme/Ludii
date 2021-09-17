package game.rules.play.moves.nonDecision.effect.set.site;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.board.SiteType;
import other.action.state.ActionSetCount;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerState;

/**
 * Sets the count of a site.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SetCount extends Effect
{
	private static final long serialVersionUID = 1L;

	/** Which site. */
	private final IntFunction locationFunction;

	/** New count. */
	private final IntFunction newCount;

	/** Add on Cell/Edge/Vertex. */
	protected SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * To set the count of a site.
	 * 
	 * @param type             The graph element type of the location [Default =
	 *                         Cell (or Vertex if the main board uses
	 *                         intersections)].
	 * @param locationFunction The site to modify the count.
	 * @param newCount         The new count.
	 * @param then             The moves applied after that move is applied.
	 */
	public SetCount
	(
		@Opt final SiteType    type,
			 final IntFunction locationFunction, 
			 final IntFunction newCount,
		@Opt final Then        then
	)
	{
		super(then);
		this.locationFunction = locationFunction;
		this.newCount = newCount;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		// Return list of legal "to" moves
		final Moves moves = new BaseMoves(super.then());

		final int loc = locationFunction.eval(context);
		final int count = newCount.eval(context);
		final ContainerState cs = context.containerState(context.containerId()[loc]);
		final int what = cs.what(loc, type);
		final ActionSetCount action = new ActionSetCount(type, loc, what, count);
		final Move move = new Move(action);
		moves.moves().add(move);

		if (then() != null)
			move.then().add(then().moves());

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		gameFlags |= locationFunction.gameFlags(game);
		gameFlags |= newCount.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(SiteType.concepts(type));
		concepts.or(locationFunction.concepts(game));
		concepts.or(newCount.concepts(game));
		concepts.set(Concept.PieceCount.id(), true);
		concepts.set(Concept.SetCount.id(), true);

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(locationFunction.writesEvalContextRecursive());
		writeEvalContext.or(newCount.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(locationFunction.readsEvalContextRecursive());
		readEvalContext.or(newCount.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		missingRequirement |= locationFunction.missingRequirement(game);
		missingRequirement |= newCount.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		willCrash |= locationFunction.willCrash(game);
		willCrash |= newCount.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return locationFunction.isStatic() && newCount.isStatic();
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);

		super.preprocess(game);
		locationFunction.preprocess(game);
		newCount.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{		
		return "set the count of " + type.name().toLowerCase() + " " + locationFunction.toEnglish(game) + " to " + newCount.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------

}
