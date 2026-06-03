package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;
import java.util.List;

import annotations.Opt;
import game.Game;
import game.functions.directions.Directions;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastTo;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Radial;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.action.Action;
import other.action.BaseAction;
import other.action.move.ActionAdd;
import other.action.move.remove.ActionRemove;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Is used to attract all the pieces by a certain number of steps to a site.
 * 
 * @author Eric.Piette
 */
public final class Attract extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Location of the piece. */
	private final IntFunction startLocationFn;

	/** Direction chosen. */
	private final Directions dirnChoice;

	/** Add on Cell/Edge/Vertex. */
	protected SiteType type;
	
	/** Distance of forced movement. */
	private final Integer distance;

	//-------------------------------------------------------------------------

	/**
	 * @param from The data of the from location [(from (last To))].
	 * @param dirn The specific direction [Adjacent].
	 * @param then The moves applied after that move is applied.
	 * @param dist The distance on which a piece is attracted.
	 * 
	 * @example (attract (from (last To)) Diagonal)
	 */
	public Attract
	(
			@Opt final game.util.moves.From from,
			@Opt final AbsoluteDirection    dirn,
			@Opt final Then                 then,
			@Opt final Integer				dist
	)
	{
		super(then);
		startLocationFn = (from == null) ? new LastTo(null) : from.loc();
		dirnChoice = (dirn == null) ? new Directions(AbsoluteDirection.Adjacent, null) : new Directions(dirn, null);
		type = (from == null) ? null : from.type();
		distance = (dist == null) ? null : dist;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		final int from = startLocationFn.eval(context);
		if (from == Constants.OFF)
			return moves;

		final Topology graph = context.topology();
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final TopologyElement fromV = graph.getGraphElements(realType).get(from);

		// Get piece at 'from'
		final ContainerState containerState = context.state().containerStates()[0];
		final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(realType, fromV,
				null, null, null,
				context);

		for (final AbsoluteDirection direction : directions)
		{	
			final List<Radial> radialList = graph.trajectories().radials(type, fromV.index(), direction);
			for (final Radial radial : radialList)
			{
				final TIntArrayList piecesInThisDirection = new TIntArrayList();
				final TIntArrayList piecesOrigin = new TIntArrayList();

				for (int toIdx = 1; toIdx < radial.steps().length; toIdx++)
				{
					final int to = radial.steps()[toIdx].id();
					final int what = containerState.what(to, realType);
					if (what != 0)
					{
						piecesInThisDirection.add(what);
						piecesOrigin.add(toIdx);
						final BaseAction removeAction = ActionRemove.construct(context.board().defaultSite(), to, Constants.UNDEFINED, true);
						final Move move = new Move(removeAction);
						moves.moves().add(move);
					}
				}
				for (int toIdx = 1, firstFreeCell = 1; toIdx <= piecesInThisDirection.size(); toIdx++)
				{
					final int to;
					if (distance == null || piecesOrigin.getQuick(toIdx - 1) - distance < firstFreeCell) {
						to = radial.steps()[toIdx].id();
					} else {
						to = radial.steps()[piecesOrigin.getQuick(toIdx - 1) - distance].id();
					}
					firstFreeCell = toIdx + 1;
					final int what = piecesInThisDirection.getQuick(toIdx - 1);
					final Action actionAdd = new ActionAdd(type, to, what, 1, Constants.UNDEFINED, Constants.UNDEFINED,
							Constants.UNDEFINED,
							null);
					final Move move = new Move(actionAdd);
					moves.moves().add(move);
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
		long gameFlags = super.gameFlags(game) | startLocationFn.gameFlags(game);
		gameFlags |= GameType.UsesFromPositions;

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

		if (then() != null)
			concepts.or(then().concepts(game));

		if (dirnChoice != null)
			concepts.or(dirnChoice.concepts(game));

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

		if (dirnChoice != null)
			writeEvalContext.or(dirnChoice.writesEvalContextRecursive());
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

		if (dirnChoice != null)
			readEvalContext.or(dirnChoice.readsEvalContextRecursive());
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
		startLocationFn.preprocess(game);
		type = SiteType.use(type, game);
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "attracts all pieces towards " + type.name() + " " + startLocationFn.toEnglish(game) + " by " + distance + " steps " + thenString;
	}

}
