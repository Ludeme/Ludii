package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;
import java.util.List;

import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.is.player.IsEnemy;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
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
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.MoveUtilities;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Is used to apply an effect to all the sites flanking a site.
 * 
 * @author Eric.Piette
 */
public final class Intervene extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Location of the piece. */
	private final IntFunction startLocationFn;

	/** Direction choosen. */
	private final AbsoluteDirection dirnChoice;

	/** Limit to intervene. */
	private final IntFunction limit;

	/** Min to intervene. */
	private final IntFunction min;

	/** The piece to intervene. */
	private final BooleanFunction targetRule;

	/** The effect to apply on the pieces flanked. */
	private final Moves targetEffect;

	/** Add on Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param from       The data about the sites to intervene [(from (last To))].
	 * @param dirnChoice The direction to compute the flanking [Adjacent].
	 * @param between    The condition on the pieces flanked [(between (exact 1))].
	 * @param to         The condition and effect on the pieces flanking [(to if:(is
	 *                   Enemy (who at:(to))) (apply (remove (to))))].
	 * @param then       The moves applied after that move is applied.
	 * 
	 * @example (intervene (from (last To)) (to if:(is Enemy (who at:(to))) (apply
	 *          (remove (to))) ) )
	 */
	public Intervene
	(
			@Opt final game.util.moves.From    from, 
			@Opt final AbsoluteDirection       dirnChoice,
			@Opt final game.util.moves.Between between, 
			@Opt final game.util.moves.To      to,
			@Opt final Then                    then
	)
	{
		super(then);
		startLocationFn = (from == null) ? new LastTo(null) : from.loc();
		type = (from == null) ? null : from.type();
		limit = (between == null || between.range() == null) ? new IntConstant(1)
				: between.range().maxFn();
		min = (between == null || between.range() == null) ? new IntConstant(1)
				: between.range().minFn();
		this.dirnChoice = (dirnChoice == null) ? AbsoluteDirection.Adjacent : dirnChoice;
		targetRule = (to == null || to.cond() == null) ? new IsEnemy(To.instance(), null) : to.cond();
		targetEffect = (to == null || to.effect() == null)
				? new Remove(null, To.instance(), null, null, null, null, null)
				: to.effect();
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		final int from = startLocationFn.eval(context);

		final int fromOrig = context.from();
		final int toOrig = context.to();

		final Topology graph = context.topology();

		if (from == Constants.UNDEFINED)
			return new BaseMoves(null);

		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final TopologyElement fromV = graph.getGraphElements(realType).get(from);

		final List<Radial> radialList = graph.trajectories().radials(type, fromV.index())
				.distinctInDirection(dirnChoice);

		final int minPathLength = min.eval(context);
		final int maxPathLength = limit.eval(context);

		if (maxPathLength == 1 && minPathLength == 1)
			shortSandwich(context, moves, fromV, radialList);
		else
			longSandwich(context, moves, fromV, radialList, maxPathLength, minPathLength);

		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

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
	 * @param actions
	 * @param fromV
	 * @param directionIndices
	 */
	private void shortSandwich(final Context context, final Moves actions, final TopologyElement fromV,
			final List<Radial> radials)
	{
		for (final Radial radial : radials)
		{
			if (radial.steps().length < 2 || !isTarget(context, radial.steps()[1].id()))
				continue;

			final List<Radial> oppositeRadials = radial.opposites();
			if (oppositeRadials != null)
			{
				boolean oppositeFound = false;
				for (final Radial oppositeRadial : oppositeRadials)
				{
					if (oppositeRadial.steps().length < 2 || !isTarget(context, oppositeRadial.steps()[1].id()))
						continue;

					context.setTo(oppositeRadial.steps()[1].id());
					MoveUtilities.chainRuleCrossProduct(context, actions, targetEffect, null, false);
					oppositeFound = true;
				}
				if (oppositeFound)
				{
					context.setTo(radial.steps()[1].id());
					MoveUtilities.chainRuleCrossProduct(context, actions, targetEffect, null, false);
				}
			}

		}
	}

	private boolean isTarget(final Context context, final int location)
	{
		context.setTo(location);
		return targetRule.eval(context);
	}

	/**
	 * To compute a longer flank (> 1).
	 * 
	 * @param context
	 * @param actions
	 * @param fromV
	 * @param directionIndices
	 * @param maxPathLength
	 */
	private void longSandwich(final Context context, final Moves actions, final TopologyElement fromV,
			final List<Radial> radials, final int maxPathLength, final int minPathLength)
	{
		for (final Radial radial : radials)
		{
			final TIntArrayList sitesToIntervene = new TIntArrayList();
			int posIdx = 1;
			while (posIdx < radial.steps().length && posIdx <= maxPathLength)
			{
				if (!isTarget(context, radial.steps()[posIdx].id()))
					break;
				sitesToIntervene.add(radial.steps()[posIdx].id());
				posIdx++;
			}

			if (sitesToIntervene.size() < minPathLength || sitesToIntervene.size() > maxPathLength)
				continue;

			final List<Radial> oppositeRadials = radial.opposites();
			if (oppositeRadials != null)
			{
				final TIntArrayList sitesOppositeToIntervene = new TIntArrayList();
				boolean oppositeFound = false;
				for (final Radial oppositeRadial : oppositeRadials)
				{
					int posOppositeIdx = 1;
					while (posOppositeIdx < oppositeRadial.steps().length && posOppositeIdx <= maxPathLength)
					{
						if (!isTarget(context, oppositeRadial.steps()[posOppositeIdx].id()))
							break;
						sitesOppositeToIntervene.add(oppositeRadial.steps()[posOppositeIdx].id());
						posOppositeIdx++;
					}

					if (sitesOppositeToIntervene.size() < minPathLength
							|| sitesOppositeToIntervene.size() > maxPathLength)
						continue;

					for (int i = 0; i < sitesOppositeToIntervene.size(); i++)
					{
						final int oppositeSite = sitesOppositeToIntervene.get(i);
						context.setTo(oppositeSite);
						MoveUtilities.chainRuleCrossProduct(context, actions, targetEffect, null, false);
					}
					oppositeFound = true;
				}

				if (oppositeFound)
				{
					for (int i = 0; i < sitesToIntervene.size(); i++)
					{
						final int oppositeSite = sitesToIntervene.get(i);
						context.setTo(oppositeSite);
						MoveUtilities.chainRuleCrossProduct(context, actions, targetEffect, null, false);
					}
				}
			}
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game) | startLocationFn.gameFlags(game) | limit.gameFlags(game)
				| min.gameFlags(game) | targetRule.gameFlags(game) | targetEffect.gameFlags(game);

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
		concepts.or(min.concepts(game));
		concepts.or(targetRule.concepts(game));
		concepts.or(targetEffect.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		// We check if that's effectively a capture (remove or fromTo).
		if (targetEffect.concepts(game).get(Concept.RemoveEffect.id())
				|| targetEffect.concepts(game).get(Concept.FromToEffect.id()))
			concepts.set(Concept.InterveneCapture.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(startLocationFn.writesEvalContextRecursive());
		writeEvalContext.or(limit.writesEvalContextRecursive());
		writeEvalContext.or(min.writesEvalContextRecursive());
		writeEvalContext.or(targetRule.writesEvalContextRecursive());
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
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(startLocationFn.readsEvalContextRecursive());
		readEvalContext.or(limit.readsEvalContextRecursive());
		readEvalContext.or(min.readsEvalContextRecursive());
		readEvalContext.or(targetRule.readsEvalContextRecursive());
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
		missingRequirement |= min.missingRequirement(game);
		missingRequirement |= targetRule.missingRequirement(game);
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
		willCrash |= min.willCrash(game);
		willCrash |= targetRule.willCrash(game);
		willCrash |= targetEffect.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return startLocationFn.isStatic() && limit.isStatic() && targetRule.isStatic() 
				&& targetEffect.isStatic() && min.isStatic();
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);

		super.preprocess(game);

		startLocationFn.preprocess(game);
		min.preprocess(game);
		limit.preprocess(game);
		targetRule.preprocess(game);
		targetEffect.preprocess(game);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{		
		String directionString = "";
		if (dirnChoice != null)
			directionString = " in dirrection " + dirnChoice.toString();
		
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		final SiteType realType = (type != null) ? type : game.board().defaultSite();
		
		return "apply " + targetEffect.toEnglish(game) + " to all sites flanking " + realType.name() + StringRoutines.getPlural(realType.name()) + " " + startLocationFn.toEnglish(game) + directionString + thenString;
	}
	
	//-------------------------------------------------------------------------

}
