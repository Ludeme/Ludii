package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;
import java.util.List;

import annotations.Opt;
import game.Game;
import game.functions.directions.DirectionsFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastTo;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Radial;
import main.Constants;
import other.action.Action;
import other.action.move.ActionAdd;
import other.action.move.remove.ActionRemove;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Pushes all the pieces from a site in one direction.
 * 
 * @author Eric.Piette
 */
public final class Push extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Location of the piece. */
	private final IntFunction startLocationFn;

	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;
	
	/** Add on Cell/Edge/Vertex. */
	protected SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param from       Description of the ``from'' location [(from (last To))].
	 * @param directions The direction to push.
	 * @param then       The moves applied after that move is applied.
	 * 
	 * @example (push (from (last To)) E)
	 */
	public Push
	(
		@Opt final game.util.moves.From           from,
			 final game.util.directions.Direction directions,
		@Opt final Then                           then
	)
	{
		super(then);		
		startLocationFn = (from == null) ? new LastTo(null) : from.loc();
		type = (from == null) ? null : from.type();
		dirnChoice = directions.directionsFunctions();
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		final int from = startLocationFn.eval(context);
		if (from == Constants.OFF)
			return moves;

		final Topology topology = context.topology();

		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final TopologyElement fromV = topology.getGraphElements(realType).get(from);
		final ContainerState cs = context.state().containerStates()[0];
		final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(realType, fromV, null, null, null, context);

		if (directions.isEmpty())
			return moves;

		final List<Radial> radials = topology.trajectories().radials(type, fromV.index(), directions.get(0));

		for (final Radial radial : radials)
		{
			int currentPiece = cs.what(radial.steps()[0].id(), realType);

			final Action removeAction = ActionRemove.construct(realType, from, Constants.UNDEFINED, true);
			moves.moves().add(new Move(removeAction));

			for (int toIdx = 1; toIdx < radial.steps().length; toIdx++)
			{
				final int to = radial.steps()[toIdx].id();
				final int what = cs.what(to, realType);
				if (what != 0)
				{
					final Action removeTo = ActionRemove.construct(realType, to, Constants.UNDEFINED, true);
					moves.moves().add(new Move(removeTo));
					final Action actionAdd = new ActionAdd(realType, to, currentPiece, 1, Constants.UNDEFINED,
							Constants.UNDEFINED, Constants.UNDEFINED, null);
					final Move move = new Move(actionAdd);
					moves.moves().add(move);
					currentPiece = what;
				}
				else
				{
					final Action actionAdd = new ActionAdd(realType, to, currentPiece, 1, Constants.UNDEFINED,
							Constants.UNDEFINED, Constants.UNDEFINED, null);
					final Move move = new Move(actionAdd);
					moves.moves().add(move);
					break;
				}
			}
		}

//		for (final Move m : moves.moves())
//			m.setMover(context.state().mover());

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
		long gameFlags = startLocationFn.gameFlags(game) | super.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(super.concepts(game));
		concepts.or(startLocationFn.concepts(game));
		concepts.set(Concept.PushEffect.id(), true);

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(startLocationFn.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(startLocationFn.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= startLocationFn.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= startLocationFn.willCrash(game);

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
		type = SiteType.use(type, game);
		startLocationFn.preprocess(game);
	}
	
	@Override
	public String toEnglish(final Game game)
	{
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "push " + dirnChoice.toEnglish(game) + thenString;
	}

}
