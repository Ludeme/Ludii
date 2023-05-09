package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;
import java.util.List;

import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.is.in.IsIn;
import game.functions.directions.Directions;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.Between;
import game.functions.ints.last.LastTo;
import game.functions.region.sites.Sites;
import game.functions.region.sites.SitesIndexType;
import game.functions.region.sites.index.SitesEmpty;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Radial;
import game.util.moves.From;
import game.util.moves.Piece;
import game.util.moves.To;
import main.Constants;
import main.collections.FastTIntArrayList;
import other.action.Action;
import other.action.move.ActionAdd;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.Move;
import other.move.MoveUtilities;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Is used to shoot an item from one site to another with a specific direction.
 * 
 * @author cambolbro
 * 
 * @remarks This ludeme is used for games including Amazons.
 */
public final class Shoot extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** Location of the piece. */
	private final IntFunction startLocationFn;

	/** Direction chosen. */
	private final Directions dirnChoice;

	/** The rule to continue the move. */
	private final BooleanFunction goRule;
	
	/** If not null, the site has to go has to follow this condition. */
	private final BooleanFunction toRule;

	/** The piece shot.  */
	private final IntFunction pieceFn;

	/** Add on Cell/Edge/Vertex. */
	protected SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param what    The data about the piece to shoot.
	 * @param from    The ``from'' location [(lastTo)].
	 * @param dirn    The direction to follow [Adjacent].
	 * @param between The location(s) between ``from'' and ``to''.
	 * @param to      The condition on the ``to'' location to allow shooting [(to
	 *                if:(in (to) (sites Empty)))].
	 * @param then    The moves applied after that move is applied.
	 * 
	 * @example (shoot (piece "Dot0"))
	 */
	public Shoot
	(
			 final Piece                   what,
		@Opt final From 		           from,
		@Opt final AbsoluteDirection       dirn,
		@Opt final game.util.moves.Between between,
		@Opt final To 	                   to,
		@Opt final Then 			       then
	)
	{
		super(then);
		startLocationFn = (from == null) ? new LastTo(null) : from.loc();
		goRule = (between == null || between.condition() == null) ? IsIn.construct(null, new IntFunction[]
		{ Between.instance() }, SitesEmpty.construct(null, null), null) : between.condition();
		toRule = (to == null)
						? 	IsIn.construct
							(
								null, 
								new IntFunction[]
								{ 
									game.functions.ints.iterator.To.instance() 
								},
						Sites.construct(SitesIndexType.Empty, SiteType.Cell, null), null
							)
						: to.cond();
		dirnChoice = 	(dirn == null) 
							? new Directions(AbsoluteDirection.Adjacent, null)
							: new Directions(dirn, null);
		pieceFn = what.component();
		type = (from == null) ? null : from.type();
	}

	//-------------------------------------------------------------------------

	@Override
	public final Moves eval(final Context context)
	{
		final BaseMoves moves = new BaseMoves(super.then());

		final int from = startLocationFn.eval(context);
		if (from == Constants.OFF)
			return moves;

		final int origTo = context.to();
		final int origBetween = context.between();

		final Topology graph = context.topology();

		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final TopologyElement fromV = graph.getGraphElements(realType).get(from);

		final int pieceType = pieceFn.eval(context);

		if (pieceType < 0)
			return moves;

		final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(realType, fromV, null, null, null,
				context);

		for (final AbsoluteDirection direction : directions)
		{
			final List<Radial> radialList = graph.trajectories().radials(type, fromV.index(), direction);
			for (final Radial radial : radialList)
			{
				final FastTIntArrayList betweenSites = new FastTIntArrayList();
				context.setBetween(origBetween);
				for (int toIdx = 1; toIdx < radial.steps().length; toIdx++)
				{
					final int to = radial.steps()[toIdx].id();
					context.setTo(to);
					if (!toRule.eval(context))
						break;

					context.setBetween(to);
					final Action actionAdd = new ActionAdd(type, to, pieceType, 1, Constants.UNDEFINED,
							Constants.UNDEFINED, Constants.UNDEFINED, null);
					if (isDecision())
						actionAdd.setDecision(true);
					final Move move = new Move(actionAdd);
					MoveUtilities.chainRuleCrossProduct(context, moves, null, move, false);
					move.setFromNonDecision(to);
					move.setBetweenNonDecision(new FastTIntArrayList(betweenSites));
					move.setToNonDecision(to);

					betweenSites.add(to);
				}
			}
		}

		context.setTo(origTo);
		context.setBetween(origBetween);
		
		MoveUtilities.setGeneratedMovesData(moves.moves(), this, context.state().mover());

		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game);
		gameFlags |= GameType.UsesFromPositions;
		
		if (startLocationFn != null)
			gameFlags |= startLocationFn.gameFlags(game);
		
		if (goRule!= null)
			gameFlags |= goRule.gameFlags(game);
		
		if (toRule != null)
			gameFlags |= toRule.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

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
		concepts.set(Concept.LineOfSight.id(), true); // shooting involves the line of sight.

		if (startLocationFn != null)
			concepts.or(startLocationFn.concepts(game));

		if (goRule != null)
			concepts.or(goRule.concepts(game));

		if (toRule != null)
			concepts.or(toRule.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		if (isDecision())
		{
			concepts.set(Concept.ShootDecision.id(), true);
			concepts.set(Concept.AddDecision.id(), true); // shooting is adding pieces.
		}
		else
		{
			concepts.set(Concept.ShootEffect.id(), true);
			concepts.set(Concept.AddEffect.id(), true); // shooting is adding pieces.
		}

		if (dirnChoice != null)
			concepts.or(dirnChoice.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (startLocationFn != null)
			writeEvalContext.or(startLocationFn.writesEvalContextRecursive());

		if (goRule != null)
			writeEvalContext.or(goRule.writesEvalContextRecursive());

		if (toRule != null)
			writeEvalContext.or(toRule.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());

		if (dirnChoice != null)
			writeEvalContext.or(dirnChoice.writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.To.id(), true);
		writeEvalContext.set(EvalContextData.Between.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		if (startLocationFn != null)
			readEvalContext.or(startLocationFn.readsEvalContextRecursive());

		if (goRule != null)
			readEvalContext.or(goRule.readsEvalContextRecursive());

		if (toRule != null)
			readEvalContext.or(toRule.readsEvalContextRecursive());

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

		if (startLocationFn != null)
			missingRequirement |= startLocationFn.missingRequirement(game);

		if (goRule != null)
			missingRequirement |= goRule.missingRequirement(game);

		if (toRule != null)
			missingRequirement |= toRule.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		if (startLocationFn != null)
			willCrash |= startLocationFn.willCrash(game);

		if (goRule != null)
			willCrash |= goRule.willCrash(game);

		if (toRule != null)
			willCrash |= toRule.willCrash(game);

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
		
		if (startLocationFn != null)
			startLocationFn.preprocess(game);
		
		if (goRule != null)
			goRule.preprocess(game);

		if (toRule != null)
			toRule.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Our go-rule
	 */
	public BooleanFunction goRule()
	{
		return goRule;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "shoot the piece " + pieceFn.toEnglish(game) + thenString;
	}
}
