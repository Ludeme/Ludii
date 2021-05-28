package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;
import java.util.List;

import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.is.player.IsEnemy;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.From;
import game.functions.ints.iterator.To;
import game.functions.ints.last.LastFrom;
import game.functions.ints.last.LastTo;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Radial;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.MoveUtilities;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Is used to apply an effect to all the pieces in a direction from a location.
 * 
 * @author Eric.Piette
 * @remarks For example, used in Fanorona.
 */
public final class Directional extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Location of the piece. */
	private final IntFunction startLocationFn;

	/** The kind of piece to capture. */
	private final BooleanFunction targetRule;

	/** Moves to apply. */
	private final Moves effect;

	/** Add on Cell/Edge/Vertex. */
	private SiteType type;

	/** Direction to use. */
	private final DirectionsFunction dirnChoice;

	//-------------------------------------------------------------------------

	/**
	 * @param from       The origin of the move [(from (last To))].
	 * @param directions The directions to use [(directions from:(last From)
	 *                   to:(last To))].
	 * @param to         The condition of the location to apply the effect [(to
	 *                   if:(is Enemy (to)) (apply (remove (from))))].
	 * @param then       The moves applied after that move is applied.
	 * 
	 * @example (directional (from (last To)) (to if:(is Enemy (who at:(to))) (apply
	 *          (remove (to))) ))
	 */
	public Directional
	(
		@Opt final game.util.moves.From           from,
		@Opt final game.util.directions.Direction directions,
		@Opt final game.util.moves.To             to,
		@Opt final Then                           then
	)  
	{
		super(then);
		this.startLocationFn = (from == null) ? new LastTo(null) : from.loc();
		this.type = (from == null) ? null : from.type();
		this.targetRule = (to == null || to.cond() == null)
				? new IsEnemy(To.instance(), null)
				: to.cond();
		this.effect = (to == null || to.effect() == null)
				? new Remove(null, new From(null), null, null, null, null, null)
				: to.effect();

		// The directions
		this.dirnChoice = (directions != null) ? directions.directionsFunctions()
				: null;
	}

	//-------------------------------------------------------------------------

	@Override
	public final Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		final int from = startLocationFn.eval(context);

		final int fromOrig = context.from();
		final int toOrig = context.to();

		final Topology topology = context.topology();
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final TopologyElement fromV = topology.getGraphElements(realType).get(from);

		final List<AbsoluteDirection> directions = (dirnChoice != null)
				? dirnChoice.convertToAbsolute(realType, fromV, null, null, null, context)
				: new Directions(realType, new LastFrom(null), new LastTo(null)).convertToAbsolute(realType, fromV,
						null, null, null, context);

		for (final AbsoluteDirection direction : directions)
		{
			final List<Radial> radials = topology.trajectories().radials(type, fromV.index(), direction);

			for (final Radial radial : radials)
			{
				for (int i = 1; i < radial.steps().length; i++)
				{
					final int locUnderThreat = radial.steps()[i].id();
					if (!isTarget(context, locUnderThreat))
						break;

					final int saveFrom = context.from();
					final int saveTo = context.to();
					context.setFrom(Constants.OFF);
					context.setTo(locUnderThreat);
					MoveUtilities.chainRuleCrossProduct(context, moves, effect, null, false);
					context.setTo(saveTo);
					context.setFrom(saveFrom);
				}
			}
		}

		context.setFrom(fromOrig);
		context.setTo(toOrig);

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	/**
	 * @param context  The context.
	 * @param location The location.
	 * @return True if the target condition is true.
	 */
	private final boolean isTarget
	(
		final Context context,
		final int location
	)
	{
		context.setTo(location);
		return targetRule.eval(context);
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = startLocationFn.gameFlags(game) | targetRule.gameFlags(game)
				| effect.gameFlags(game) | super.gameFlags(game);
		
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
		concepts.or(targetRule.concepts(game));
		concepts.or(effect.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		// We check if that's effectively a capture (remove or fromTo).
		if (effect.concepts(game).get(Concept.Remove.id())
				|| effect.concepts(game).get(Concept.FromTo.id()))
			concepts.set(Concept.DirectionCapture.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(startLocationFn.writesEvalContextRecursive());
		writeEvalContext.or(targetRule.writesEvalContextRecursive());
		writeEvalContext.or(effect.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.To.id(), true);
		writeEvalContext.set(EvalContextData.From.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(startLocationFn.readsEvalContextRecursive());
		readEvalContext.or(targetRule.readsEvalContextRecursive());
		readEvalContext.or(effect.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean isStatic()
	{
		return startLocationFn.isStatic() && targetRule.isStatic() && effect.isStatic();
	}
	
	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		super.preprocess(game);
		
		startLocationFn.preprocess(game);
		targetRule.preprocess(game);
		effect.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= startLocationFn.missingRequirement(game);
		missingRequirement |= targetRule.missingRequirement(game);
		missingRequirement |= effect.missingRequirement(game);

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
		willCrash |= targetRule.willCrash(game);
		willCrash |= effect.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game)
	{
		return "DirectionCapture";
	}
}
