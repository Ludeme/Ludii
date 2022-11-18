package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;
import java.util.List;

import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.is.player.IsEnemy;
import game.functions.booleans.is.player.IsFriend;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.Between;
import game.functions.ints.iterator.To;
import game.functions.ints.last.LastTo;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Radial;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.StringRoutines;
import main.collections.FastTIntArrayList;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.MoveUtilities;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Is used to apply an effect to all the sites flanked between two sites.
 * 
 * @author mrraow and cambolbro and Eric.Piette
 * 
 * @remarks Used for example in all the Tafl games.
 */
public final class Custodial extends Effect
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/** Location of the piece. */
	private final IntFunction startLocationFn;
	
	/** Direction choosen. */
	private final AbsoluteDirection dirnChoice;
	
	/** Limit to flank. */
	private final IntFunction limit;
	
	/** The piece to flank. */
	private final BooleanFunction targetRule;
	
	/** The rule to detect the friend to flank. */
	private final BooleanFunction friendRule;
	
	/** The effect to apply on the pieces flanked. */
	private final Moves targetEffect;
	
	/** Add on Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param from       The data about the sites used as an origin to flank [(from
	 *                   (last To))].
	 * @param dirnChoice The direction to compute the flanking [Adjacent].
	 * @param between    The condition and effect on the pieces flanked [(between
	 *                   if:(is Enemy (between)) (apply (remove (between))))].
	 * @param to         The condition on the pieces surrounding [(to if:(is Friend
	 *                   (to)))].
	 * @param then       The moves applied after that move is applied.
	 * 
	 * @example (custodial (from (last To)) Orthogonal (between (max 1) if:(is Enemy
	 *          (who at:(between))) (apply (remove (between))) ) (to if:(is Friend
	 *          (who at:(to)))) )
	 */
	public Custodial
	(
		@Opt  final game.util.moves.From    from,
	    @Opt  final AbsoluteDirection       dirnChoice,
		@Opt  final game.util.moves.Between between,
		@Opt  final game.util.moves.To      to,
		@Opt  final Then                    then
	)
	{
		super(then);
		startLocationFn = (from == null) ? new LastTo(null) : from.loc();
		type = (from == null) ? null : from.type();
		
		limit = (between == null || between.range() == null) ? new IntConstant(Constants.MAX_DISTANCE)
				: between.range().maxFn();
		this.dirnChoice = (dirnChoice == null) ? AbsoluteDirection.Adjacent : dirnChoice;
		targetRule = (between == null || between.condition() == null)
				? new IsEnemy(Between.instance(), null)
				: between.condition();
		friendRule = (to == null || to.cond() == null)
				? new IsFriend(To.instance(), null)
				: to.cond();
		targetEffect = (between == null || between.effect() == null)
				? new Remove(null, Between.instance(), null, null, null, null, null)
				: between.effect();
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		final int from = startLocationFn.eval(context);
		
		final int fromOrig = context.from();
		final int toOrig = context.to();
		final int betweenOrig = context.between();

		final Topology graph = context.topology();

		if (from == Constants.UNDEFINED)
			return new BaseMoves(null);

		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final TopologyElement fromV = graph.getGraphElements(realType).get(from);

		final List<Radial> radialList = graph.trajectories().radials(type, fromV.index(), dirnChoice);

		final int maxPathLength = limit.eval(context);

		if (maxPathLength == 1)
			shortSandwich(context, moves, fromV, radialList);
		else
			longSandwich(context, moves, fromV, radialList, maxPathLength);

		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		context.setBetween(betweenOrig);
		context.setTo(toOrig);
		context.setFrom(fromOrig);

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	/**
	 * To compute a short flank (= 1).
	 * 
	 * @param context
	 * @param moves
	 * @param fromV
	 * @param directionIndices
	 */
	private void shortSandwich
	(
		final Context context,
		final Moves moves,
		final TopologyElement fromV,
		final List<Radial> radials
	)
	{
		for (final Radial radial : radials)
		{
			final int between = radial.steps()[1].id();
			if (radial.steps().length < 3 || !isTarget(context, between)
					|| !isFriend(context, radial.steps()[2].id()))
					continue;
			context.setBetween(between);
			MoveUtilities.chainRuleCrossProduct(context, moves, targetEffect, null, false);
			final TIntArrayList betweenSites = new TIntArrayList(1);
			betweenSites.add(between);
			moves.moves().get(moves.moves().size() - 1).setBetweenNonDecision(betweenSites);
		}
	}

	private boolean isFriend
	(
		final Context context,
		final int location
	)
	{
		context.setTo(location);
		return friendRule.eval(context);
	}

	private boolean isTarget
	(
		final Context context,
		final int location
	)
	{
		context.setBetween(location);
		return targetRule.eval(context);
	}

	/**
	 * To compute a longer flank (> 1).
	 * 
	 * @param context
	 * @param moves
	 * @param fromV
	 * @param directionIndices
	 * @param maxPathLength
	 */
	private void longSandwich
	(
		final Context context,
		final Moves moves,
		final TopologyElement fromV,
		final List<Radial> radials,
		final int maxPathLength
	)
	{
		for (final Radial radial : radials)
		{
			final FastTIntArrayList betweenSites = new FastTIntArrayList();
			boolean foundEnemy = false;
			int posIdx = 1;
			while (posIdx < radial.steps().length && posIdx <= maxPathLength)
			{
				if (!isTarget(context, radial.steps()[posIdx].id()))
					break;
				foundEnemy = true;
				posIdx++;
			}

			if (!foundEnemy)
				continue;

			final int friendPos = posIdx < radial.steps().length ? radial.steps()[posIdx].id() : Constants.OFF;
			if (isFriend(context, friendPos))
			{
				for (int i = 1; i < posIdx; i++)
				{
					final int between = radial.steps()[i].id();
					betweenSites.add(between);
					context.setBetween(between);
					MoveUtilities.chainRuleCrossProduct(context, moves, targetEffect, null, false);
				}
				if(!moves.moves().isEmpty())
					moves.moves().get(moves.moves().size() - 1).setBetweenNonDecision(new FastTIntArrayList(betweenSites));
			}
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game) | startLocationFn.gameFlags(game)
				| 
				limit.gameFlags(game) 
				| 
				targetRule.gameFlags(game) 
				| 
				friendRule.gameFlags(game) 
				| 
				targetEffect.gameFlags(game);
		
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
		concepts.or(limit.concepts(game));
		concepts.or(targetRule.concepts(game));
		concepts.or(friendRule.concepts(game));
		concepts.or(targetEffect.concepts(game));
		
		if (then() != null)
			concepts.or(then().concepts(game));

		// We check if that's effectively a capture (remove or fromTo).
		if (targetEffect.concepts(game).get(Concept.RemoveEffect.id())
				|| targetEffect.concepts(game).get(Concept.FromToEffect.id()))
			concepts.set(Concept.CustodialCapture.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext =  writesEvalContextFlat();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(startLocationFn.writesEvalContextRecursive());
		writeEvalContext.or(limit.writesEvalContextRecursive());
		writeEvalContext.or(targetRule.writesEvalContextRecursive());
		writeEvalContext.or(friendRule.writesEvalContextRecursive());
		writeEvalContext.or(targetEffect.writesEvalContextRecursive());

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
		writeEvalContext.set(EvalContextData.Between.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(startLocationFn.readsEvalContextRecursive());
		readEvalContext.or(limit.readsEvalContextRecursive());
		readEvalContext.or(targetRule.readsEvalContextRecursive());
		readEvalContext.or(friendRule.readsEvalContextRecursive());
		readEvalContext.or(targetEffect.readsEvalContextRecursive());

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
		missingRequirement |= limit.missingRequirement(game);
		missingRequirement |= targetRule.missingRequirement(game);
		missingRequirement |= friendRule.missingRequirement(game);
		missingRequirement |= targetEffect.missingRequirement(game);

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
		willCrash |= limit.willCrash(game);
		willCrash |= targetRule.willCrash(game);
		willCrash |= friendRule.willCrash(game);
		willCrash |= targetEffect.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return startLocationFn.isStatic() && limit.isStatic() && targetRule.isStatic() && friendRule.isStatic()
				&& targetEffect.isStatic();
	}
	
	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);

		super.preprocess(game);
		
		startLocationFn.preprocess(game);
		limit.preprocess(game);
		targetRule.preprocess(game);
		friendRule.preprocess(game);
		targetEffect.preprocess(game);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game)
	{		
		String targetString = "";
		if (targetRule != null)
			targetString = " if the target is " + targetRule.toEnglish(game);
		
		String friendString = "";
		if (friendRule != null)
			friendString = " if the friend is " + friendRule.toEnglish(game);
		
		String directionString = "";
		if (dirnChoice != null)
			directionString += " with "+ dirnChoice.name()+ " direction";
		
		String fromString = "";
		if (startLocationFn != null)
			fromString = " starting from " + startLocationFn.toEnglish(game);
		
		String limitString = "";
		if (limit != null)
			limitString = " with a limit of " + limit.toEnglish(game) + " pieces";

		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		final SiteType realType = (type != null) ? type : game.board().defaultSite();
		
		return "for all flanked pieces on " + realType.name() + StringRoutines.getPlural(realType.name()) + fromString + directionString + limitString + targetString + friendString + targetEffect.toEnglish(game) + thenString;
	}

	//-------------------------------------------------------------------------
}
