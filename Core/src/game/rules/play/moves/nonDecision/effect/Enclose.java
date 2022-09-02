package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.is.player.IsEnemy;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.Between;
import game.functions.ints.last.LastTo;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.Direction;
import gnu.trove.list.array.TIntArrayList;
import main.StringRoutines;
import main.collections.FastTIntArrayList;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.MoveUtilities;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Applies a move to an enclosed group.
 * 
 * @author Eric Piette
 * 
 * @remarks A group of components is 'enclosed' if it has no adjacent empty
 *          sites, where board sides count as boundaries. This ludeme is used
 *          for surround capture games such as Go.
 */
public final class Enclose extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The starting point of the loop. */
	private final IntFunction startFn;

	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;

	/** The piece to surround. */
	private final BooleanFunction targetRule;

	/** The number of liberties allowed in the group to enclose. */
	private final IntFunction numEmptySitesInGroupEnclosed;

	/** Effect to apply to each site inside the group. */
	private final Moves effect;
	
	/** The graph element type. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type         The graph element type [default site type of the board].
	 * @param from         The origin of the enclosed group [(from (last To))].
	 * @param directions   The direction to use [Adjacent].
	 * @param between      The condition and effect on the pieces enclosed [(between
	 *                     if:(is Enemy (between)) (apply (remove (between))))].
	 * @param numException The number of liberties allowed in the group to enclose
	 *                     [0].
	 * @param then         The moves applied after that move is applied.
	 * 
	 * @example (enclose (from (last To)) Orthogonal (between if:(is Enemy (who
	 *          at:(between))) (apply (remove (between) ) ) ))
	 */
	public Enclose
	(
		@Opt       final SiteType                type,
		@Opt       final game.util.moves.From    from,
		@Opt       final Direction               directions,
	    @Opt       final game.util.moves.Between between,
	    @Opt @Name final IntFunction             numException,
		@Opt       final Then                    then
	)
	{
		super(then);
		
		startFn = (from == null) ? new LastTo(null) : (from.loc() == null) ? new LastTo(null) : from.loc();
		dirnChoice = (directions != null) ? directions.directionsFunctions()
				: new Directions(AbsoluteDirection.Adjacent, null);
	
		targetRule = (between == null || between.condition() == null)
				? new IsEnemy(Between.instance(), null)
				: between.condition();
		
		effect = (between == null || between.effect() == null)
				? new Remove(null, Between.instance(), null, null, null, null, null)
				: between.effect();
		this.type = type;
		numEmptySitesInGroupEnclosed = (numException == null) ? new IntConstant(0) : numException;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());
		
		final int from = startFn.eval(context);
		
		final int originBetween = context.between();
		final int originTo = context.to();
		
		// Check if this is a site.
		if (from < 0)
			return moves;

		final Topology topology = context.topology();
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final List<? extends TopologyElement> graphElements = topology.getGraphElements(realType);

		// Check if the site is in the board.
		if (from >= graphElements.size())
			return moves;
		
		final ContainerState cs = context.containerState(0);
		final int what = cs.what(from, realType);

		// Check if the site is not empty.
		if (what <= 0)
			return moves;

		final int numException = numEmptySitesInGroupEnclosed.eval(context);
		final boolean atLeastAnEmpty = targetRule.concepts(context.game()).get(Concept.IsEmpty.id());

		// We get all the sites around the starting positions satisfying the target
		// rule.
		final TIntArrayList aroundTarget = new TIntArrayList();
		final TopologyElement element = graphElements.get(from);
		final List<AbsoluteDirection> directionsElement = dirnChoice.convertToAbsolute(type, element, null, null, null,
				context);
		for (final AbsoluteDirection direction : directionsElement)
		{
			final List<game.util.graph.Step> steps = topology.trajectories().steps(type, element.index(), type,
					direction);

			for (final game.util.graph.Step step : steps)
			{
				final int between = step.to().id();
				if (!aroundTarget.contains(between))
				{
					if (isTarget(context, between))
						aroundTarget.add(between);
					else if (numException > 0 && cs.what(between, realType) == 0)
						aroundTarget.add(between);
				}
			}
		}
		
		// We look the group of each possible target, if no liberties, we apply the
		// effect.
		final boolean[] sitesChecked = new boolean[graphElements.size()];
		
		aroundTargetLoop:
		for (int indexEnclosed = 0; indexEnclosed < aroundTarget.size(); indexEnclosed++)
		{
			final int target = aroundTarget.get(indexEnclosed);
			
			// If already checked we continue;
			if (sitesChecked[target])
				continue;
			
			int numExceptionToUse = numException;

			if (numExceptionToUse > 0 && cs.what(target, realType) == 0)
				numExceptionToUse--;

			// We get the group of sites satisfying the target rule.
			final boolean[] enclosedGroup = new boolean[graphElements.size()];
			final FastTIntArrayList enclosedGroupList = new FastTIntArrayList();
			enclosedGroup[target] = true;
			enclosedGroupList.add(target);
			int i = 0;
			while (i != enclosedGroupList.size())
			{
				final int site = enclosedGroupList.getQuick(i);
				final TopologyElement siteElement = graphElements.get(site);
				final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(type, siteElement, null, null,
						null, context);

				for (final AbsoluteDirection direction : directions)
				{
					final List<game.util.graph.Step> steps = topology.trajectories().steps(type, site,
							type, direction);

					for (final game.util.graph.Step step : steps)
					{
						final int between = step.to().id();

						// If we already have it we continue to look the others.
						if (enclosedGroup[between])
							continue;

						if (isTarget(context, between))
						{
							enclosedGroup[between] = true;
							enclosedGroupList.add(between);
						}
						else if (cs.what(between, realType) == 0)
						{
							if (numExceptionToUse > 0)
							{
								enclosedGroup[between] = true;
								enclosedGroupList.add(between);
								numExceptionToUse--;
							}
							else
							{
								// It's a liberty, so move on
								continue aroundTargetLoop;
							}
						}
					}
				}

				sitesChecked[site] = true;
				i++;
			}
			
			final TIntArrayList enclosingGroup = new TIntArrayList();
			
			// We check whether we have liberties
			for (int indexGroup = 0; indexGroup < enclosedGroupList.size(); indexGroup++)
			{
				final int siteGroup = enclosedGroupList.getQuick(indexGroup);
				final TopologyElement elem = graphElements.get(siteGroup);
				final List<AbsoluteDirection> directionsElem = dirnChoice.convertToAbsolute(type, elem, null,
						null, null, context);
				for (final AbsoluteDirection direction : directionsElem)
				{
					final List<game.util.graph.Step> steps = topology.trajectories().steps(type, siteGroup, type,
							direction);

					for (final game.util.graph.Step step : steps)
					{
						final int to = step.to().id();
						if (!enclosedGroup[to] && !enclosingGroup.contains(to))
							if (atLeastAnEmpty ? isTarget(context, to) : cs.what(to, type) == 0)
								continue aroundTargetLoop;	// At least one liberty, so move on
							else enclosingGroup.add(to);
					}
				}
			}
			
			// If the enclosed site can be empty.
			if(atLeastAnEmpty)
			{
				// Check if that's a single group.
				boolean aSingleGroup = true;
				int siteGroup = enclosingGroup.get(0);
				while(enclosingGroup.size() != 1)
				{
					final List<game.util.graph.Step> steps = topology.trajectories().steps(type, siteGroup, type,
							AbsoluteDirection.All);
	
					boolean inSameGroup = false;
					for (final game.util.graph.Step step : steps)
					{
						final int to = step.to().id();
						if(enclosingGroup.contains(to))
						{
							enclosingGroup.remove(siteGroup);
							siteGroup = to;
							inSameGroup = true;
							break;
						}
					}
					
					if(!inSameGroup)
					{
						aSingleGroup = false;
						break;
					}
				}
				
				if(!aSingleGroup)
					continue aroundTargetLoop;
			}
			
			// If we reach this point and didn't continue the "aroundTargetLoop" above,
			// this means that we have no liberties.
			// If no liberties, this group is enclosed and we apply the effect.
			for (int indexBetween = 0; indexBetween < enclosedGroupList.size(); indexBetween++)
			{
				final int between = enclosedGroupList.getQuick(indexBetween);
				context.setBetween(between);
				MoveUtilities.chainRuleCrossProduct(context, moves, effect, null, false);
			}
			moves.moves().get(moves.moves().size() - 1).setBetweenNonDecision(new FastTIntArrayList(enclosedGroupList));
		}

		context.setTo(originTo);
		context.setBetween(originBetween);

		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	/**
	 * @param context  The context.
	 * @param location The site.
	 * @return True if the target rule is true.
	 */
	private boolean isTarget(final Context context, final int location)
	{
		context.setBetween(location);
		return targetRule.eval(context);
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game);
		gameFlags |= SiteType.gameFlags(type);

		if (then() != null)
			gameFlags |= then().gameFlags(game);
		
		return startFn.gameFlags(game)
				| 
				targetRule.gameFlags(game)
				| 
				effect.gameFlags(game)
				|
				numEmptySitesInGroupEnclosed.gameFlags(game)
				|
				gameFlags;

	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(super.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		// We check if that's effectively a capture (remove or fromTo).
		if (effect.concepts(game).get(Concept.RemoveEffect.id())
				|| effect.concepts(game).get(Concept.FromToEffect.id()))
			concepts.set(Concept.EncloseCapture.id(), true);


		concepts.or(startFn.concepts(game));
		concepts.or(targetRule.concepts(game));
		concepts.or(effect.concepts(game));
		concepts.or(numEmptySitesInGroupEnclosed.concepts(game));

		if (dirnChoice != null)
			concepts.or(dirnChoice.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());

		writeEvalContext.or(startFn.writesEvalContextRecursive());
		writeEvalContext.or(targetRule.writesEvalContextRecursive());
		writeEvalContext.or(effect.writesEvalContextRecursive());
		writeEvalContext.or(numEmptySitesInGroupEnclosed.writesEvalContextRecursive());

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

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());

		readEvalContext.or(startFn.readsEvalContextRecursive());
		readEvalContext.or(targetRule.readsEvalContextRecursive());
		readEvalContext.or(effect.readsEvalContextRecursive());
		readEvalContext.or(numEmptySitesInGroupEnclosed.readsEvalContextRecursive());

		if (dirnChoice != null)
			readEvalContext.or(dirnChoice.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);

		missingRequirement |= startFn.missingRequirement(game);
		missingRequirement |= targetRule.missingRequirement(game);
		missingRequirement |= effect.missingRequirement(game);
		missingRequirement |= numEmptySitesInGroupEnclosed.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);

		willCrash |= startFn.willCrash(game);
		willCrash |= targetRule.willCrash(game);
		willCrash |= effect.willCrash(game);
		willCrash |= numEmptySitesInGroupEnclosed.willCrash(game);
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

		startFn.preprocess(game);
		targetRule.preprocess(game);
		effect.preprocess(game);
		numEmptySitesInGroupEnclosed.preprocess(game);
		type = SiteType.use(type, game);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game)
	{		
		String targetString = "";
		if (targetRule != null)
			targetString = " if the target is " + targetRule.toEnglish(game);
		
		String directionString = "";
		if (dirnChoice != null)
			directionString += " with "+ dirnChoice.toEnglish(game)+ " direction";
		
		String fromString = "";
		if (startFn != null)
			fromString = " starting from " + startFn.toEnglish(game);
		
		String limitString = "";
		if (numEmptySitesInGroupEnclosed != null)
			limitString = " if the number of liberties is less than or equal to " + numEmptySitesInGroupEnclosed.toEnglish(game);

		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "for all enclosed pieces on " + type.name() + StringRoutines.getPlural(type.name()) + fromString + directionString + limitString + targetString + effect.toEnglish(game) + thenString;
	}

	//-------------------------------------------------------------------------

}
