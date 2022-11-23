package game.rules.play.moves.nonDecision.effect;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.container.board.Track;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanConstant.FalseConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastTo;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.NonDecision;
import game.types.board.SiteType;
import game.types.state.GameType;
import main.Constants;
import other.action.Action;
import other.action.move.move.ActionMove;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.context.TempContext;
import other.move.Move;
import other.state.container.ContainerState;

/**
 * Sows counters by removing them from a site then placing them one-by-one at
 * each consecutive site along a track.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks Sowing is used in Mancala games. A track must be defined on the
 *          board in order for sowing to work.
 */
public final class Sow extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Start loc. */
	private final IntFunction startLoc;

	/** How many components to sow. */
	private final IntFunction countFn;

	/** How many components to place in each hole. */
	private final IntFunction numPerHoleFn;

	/** Track to follow. */
	private final String trackName;

	/** Owner of the track. */
	private final IntFunction ownerFn;

	/** Start position included. */
	private final boolean includeSelf;

	/** To put a bean in the original hole before to sow. */
	private final BooleanFunction origin;

	/** Exception in the sow move **/
	private final BooleanFunction skipFn;

	/** Capture rule. */
	private final BooleanFunction captureRule;

	/** Backward capture. */
	private final BooleanFunction backtracking;

	/** Forward capture. */
	private final BooleanFunction forward;

	/** Capture effect. */
	private final Moves captureEffect;
	
	/** Sow effect. */
	private final Moves sowEffect;

	/** Add on Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/** Pre-computed tracks in keeping only the track with the name on them. */
	private List<Track> preComputedTracks = new ArrayList<Track>();

	//-------------------------------------------------------------------------
	
	/**
	 * @param type         The graph element type [default SiteType of the board].
	 * @param start        The origin of the sowing [(lastTo)].
	 * @param count        The number of pieces to sow [(count (lastTo))].
	 * @param numPerHole   The number of pieces to sow in each hole [1].
	 * @param trackName    The name of the track to sow [The first track if it
	 *                     exists].
	 * @param owner        The owner of the track.
	 * @param If           The condition to capture some counters after sowing
	 *                     [True].
	 * @param apply        The move to apply if the condition is satisfied.
	 * @param sowEffect    The effect to apply to each hole sowed.
	 * @param includeSelf  True if the origin is included in the sowing [True].
	 * @param origin       True to place a counter in the origin at the start of
	 *                     sowing [False].
	 * @param skipIf       The condition to skip a hole during the sowing.
	 * @param backtracking Whether to apply the capture backwards from the ``to''
	 *                     site.
	 * @param forward      Whether to apply the capture forwards from the ``to''
	 *                     site.
	 * @param then         The moves applied after that move is applied.
	 * 
	 * @example (sow if:(and (is In (to) (sites Next)) (or (= (count at:(to)) 2) (=
	 *          (count at:(to)) 3)) ) apply:(fromTo (from (to)) (to (mapEntry
	 *          (mover))) count:(count at:(to))) includeSelf:False
	 *          backtracking:True)
	 */
	public Sow
	(
		    @Opt       final SiteType         type,
		    @Opt 	   final IntFunction      start,
		    @Opt @Name final IntFunction      count,
		    @Opt @Name final IntFunction      numPerHole,
		    @Opt 	   final String           trackName,
		    @Opt @Name final IntFunction      owner,
		    @Opt @Name final BooleanFunction  If,
		    @Opt @Name final Moves            sowEffect,
		    @Opt @Name final NonDecision      apply,
		    @Opt @Name final Boolean          includeSelf,
	   	    @Opt @Name final BooleanFunction  origin,
		    @Opt @Name final BooleanFunction  skipIf,
		@Or @Opt @Name final BooleanFunction  backtracking,
		@Or @Opt @Name final BooleanFunction  forward,
		    @Opt 	   final Then             then
	)
	{ 
		super(then);
		startLoc = (start == null) ? new LastTo(null) : start;
		this.includeSelf = (includeSelf == null) ? true : includeSelf.booleanValue();
		countFn = (count == null)
				? new game.functions.ints.count.site.CountNumber(null, null, startLoc)
				: count;
		this.trackName = trackName;
		captureRule = (If == null) ? new BooleanConstant(true) : If;
		captureEffect = apply;
		skipFn = skipIf;
		this.backtracking = backtracking;
		this.forward = forward;
		this.origin = (origin == null) ? new BooleanConstant(false) : origin;
		ownerFn = owner;
		this.type = type;
		numPerHoleFn = (numPerHole == null) ? new IntConstant(1) : numPerHole;
		this.sowEffect = sowEffect;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final int start = startLoc.eval(context);
		final int count = countFn.eval(context);
		int numPerHole = numPerHoleFn.eval(context);
		final Moves moves = new BaseMoves(super.then());
		final Move move = new Move(new ArrayList<Action>());
		int numSeedSowed = 0;
		final ContainerState cs = context.containerState(0);
		final int origFrom = context.from();
		final int origTo = context.to();
		final int origValue = context.value();
		final int owner = (ownerFn == null) ? Constants.OFF : ownerFn.eval(context);
		
		Track track = null;
		for (final Track t : preComputedTracks)
		{
			if (trackName == null || (owner == Constants.OFF && t.name().equals(trackName)
					|| (owner != Constants.OFF && t.owner() == owner && t.name().contains(trackName))))
			{
				track = t;
				break;
			}
		}

		// If the track does exist we return an empty list of moves.
		if (track == null)
			return moves;

		int i;
		for (i = 0; i < track.elems().length; i++)
			if (track.elems()[i].site == start)
				break;
		
		// If we include the origin we remove one piece to sow and we sow on the origin
		// location.
		context.setFrom(start);
		if (origin.eval(context))
		{
			if(sowEffect != null)
			{
				final Moves effect = sowEffect.eval(context);
				for(final Move moveEffect : effect.moves())
					for(final Action actionEffect : moveEffect.actions())
						move.actions().add(actionEffect);
			}
			
			int numDone = 0;
			while(numDone != numPerHole)
			{
				if(numSeedSowed < count)
					move.actions()
							.add(ActionMove.construct(type, start, Constants.UNDEFINED, type, start, Constants.OFF, Constants.UNDEFINED, Constants.OFF, Constants.OFF, false));
				numDone++;
				numSeedSowed++;
			}
			context.setTo(start);
		}
		context.setFrom(origFrom);

		// Computation of the sowing moves.
		if(numSeedSowed < count)
		{
			int numSkipped = 0;
			for (int index = 0; index < count; index++)
			{
				context.setValue(count - index);
				if(i >= track.elems().length)
					return moves;
				int to = track.elems()[i].next;
				context.setTo(to);
				
				// Check if that site should be skip.
				if (skipFn != null && skipFn.eval(context) && numSkipped < Constants.MAX_NUM_ITERATION)
				{
					index--;
					numSkipped++;
					i = track.elems()[i].nextIndex;
					to = track.elems()[i].next;
					continue;
				}
				else
					numSkipped = 0;
				
				// Check if we include the origin in the sowing movement.
				if (!includeSelf && to == start)
				{
					i = track.elems()[i].nextIndex;
					to = track.elems()[i].next;
				}
				
				// The sowing action for each location.
				final int startState = cs.state(start, type);
				final int startRotation = cs.rotation(start, type);
				final int startValue = cs.value(start, type);
				final int toState = cs.state(to, type);
				final int toRotation = cs.rotation(to, type);
				final int toValue = cs.value(to, type);
				numPerHole = numPerHoleFn.eval(context);
				int numDone = 0;
				
				if(sowEffect != null)
				{
					final Moves effect = sowEffect.eval(context);
					for(final Move moveEffect : effect.moves())
						for(final Action actionEffect : moveEffect.actions())
							move.actions().add(actionEffect);
				}
				
				while(numDone != numPerHole)
				{
					if(numSeedSowed < count)
						move.actions().add(ActionMove.construct(type, start, Constants.UNDEFINED, type, to, Constants.OFF,
								startState != toState ? toState : Constants.UNDEFINED,
								startRotation != toRotation ? toRotation : Constants.UNDEFINED,
								startValue != toValue ? toValue : Constants.UNDEFINED, false));
					numDone++;
					numSeedSowed++;
				}
				
				i = track.elems()[i].nextIndex;
				
				if(numSeedSowed >= count)
					break;
			}
		}
		moves.moves().add(move);
		
		// We apply each sow move and we check the condition of capture to apply the
		// effect with the count value after sowing.
		if (captureRule != null && captureEffect != null)
			for (final Move sowMove : moves.moves())
			{
				final Context newContext = new TempContext(context);
				sowMove.apply(newContext, false);

				int numCapture = 0;
				
				// If true we have to capture the pieces
				while (captureRule.eval(newContext))
				{
					// We compute the capturing moves.
					newContext.setFrom(start);
					final Moves capturingMoves = captureEffect.eval(newContext);
					for (final Move m : capturingMoves.moves())
						sowMove.actions().addAll(m.getActionsWithConsequences(newContext));

					if(backtracking == null && forward == null)
						break;
					
					if(backtracking != null)
					{
						if (!backtracking.eval(newContext))
							break;
	
						final int to = track.elems()[i].prev;
						i = track.elems()[i].prevIndex;
						newContext.setTo(to);
	
						if (!backtracking.eval(newContext))
							break;
						
						if (to == start)
							break;
					}
					
					if(forward != null)
					{
						if (!forward.eval(newContext))
							break;
	
						if(!track.islooped() && track.elems()[i].next == Constants.OFF)
							break;
						
						final int to = track.elems()[i].next;
						i = track.elems()[i].nextIndex;
						newContext.setTo(to);
	
						if (!forward.eval(newContext))
							break;
					}
					numCapture++;
					
					if(numCapture >= track.elems().length)
						break;
				}
			}

		context.setTo(origTo);
		context.setFrom(origFrom);
		context.setValue(origValue);
		
		// The subsequents to add to the moves
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
		long gameFlags = GameType.Count | super.gameFlags(game);
		gameFlags |= GameType.UsesFromPositions;
		
		gameFlags |= startLoc.gameFlags(game);
		gameFlags |= countFn.gameFlags(game);
		gameFlags |= numPerHoleFn.gameFlags(game);
		
		if (captureEffect != null)
		{
			gameFlags |= captureEffect.gameFlags(game);
			if (captureEffect.then() != null)
				gameFlags |= captureEffect.then().gameFlags(game);
		}
		
		gameFlags |= SiteType.gameFlags(type);

		if (ownerFn != null)
			gameFlags |= ownerFn.gameFlags(game);

		if (captureRule != null)
			gameFlags |= captureRule.gameFlags(game);
		
		if (sowEffect != null)
			gameFlags |= sowEffect.gameFlags(game);

		if (origin != null)
			gameFlags |= origin.gameFlags(game);

		if (skipFn != null)
			gameFlags |= skipFn.gameFlags(game);

		if (backtracking != null)
			gameFlags |= backtracking.gameFlags(game);
		
		if (forward != null)
			gameFlags |= forward.gameFlags(game);
		
		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		if (captureRule != null && captureEffect != null)
			concepts.set(Concept.CopyContext.id(), true);
		concepts.set(Concept.Sow.id(), true);

		concepts.or(SiteType.concepts(type));

		concepts.or(startLoc.concepts(game));
		concepts.or(countFn.concepts(game));
		concepts.or(numPerHoleFn.concepts(game));

		if (captureEffect != null)
		{
			concepts.or(captureEffect.concepts(game));
			if (captureEffect.then() != null)
				concepts.or(captureEffect.then().concepts(game));
			concepts.set(Concept.SowWithEffect.id(), true);

			if (captureEffect.concepts(game).get(Concept.RemoveEffect.id()))
				concepts.set(Concept.SowRemove.id(), true);

			if (captureEffect.concepts(game).get(Concept.FromToEffect.id()))
				concepts.set(Concept.SowCapture.id(), true);
		}

		if (ownerFn != null)
			concepts.or(ownerFn.concepts(game));

		if (captureRule != null)
			concepts.or(captureRule.concepts(game));
		
		if (sowEffect != null)
			concepts.or(sowEffect.concepts(game));

		if (origin != null)
		{
			if (!(origin instanceof FalseConstant))
				concepts.set(Concept.SowOriginFirst.id(), true);

			concepts.or(origin.concepts(game));
		}

		if (skipFn != null)
		{
			concepts.set(Concept.SowSkip.id(), true);
			concepts.or(skipFn.concepts(game));
		}

		if (!includeSelf)
			concepts.set(Concept.SowSkip.id(), true);

		if (backtracking != null)
		{
			concepts.or(backtracking.concepts(game));
			concepts.set(Concept.SowBacktracking.id(), true);
		}
		
		if (forward != null)
			concepts.or(forward.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));
		
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(startLoc.writesEvalContextRecursive());
		writeEvalContext.or(countFn.writesEvalContextRecursive());
		writeEvalContext.or(numPerHoleFn.writesEvalContextRecursive());

		if (captureEffect != null)
		{
			writeEvalContext.or(captureEffect.writesEvalContextRecursive());
			if (captureEffect.then() != null)
				writeEvalContext.or(captureEffect.then().writesEvalContextRecursive());
		}

		if (ownerFn != null)
			writeEvalContext.or(ownerFn.writesEvalContextRecursive());

		if (captureRule != null)
			writeEvalContext.or(captureRule.writesEvalContextRecursive());
		
		if (sowEffect != null)
			writeEvalContext.or(sowEffect.writesEvalContextRecursive());

		if (origin != null)
			writeEvalContext.or(origin.writesEvalContextRecursive());

		if (skipFn != null)
			writeEvalContext.or(skipFn.writesEvalContextRecursive());

		if (backtracking != null)
			writeEvalContext.or(backtracking.writesEvalContextRecursive());
		
		if (forward != null)
			writeEvalContext.or(forward.writesEvalContextRecursive());

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
		readEvalContext.or(startLoc.readsEvalContextRecursive());
		readEvalContext.or(countFn.readsEvalContextRecursive());
		readEvalContext.or(numPerHoleFn.readsEvalContextRecursive());

		if (captureEffect != null)
		{
			readEvalContext.or(captureEffect.readsEvalContextRecursive());
			if (captureEffect.then() != null)
				readEvalContext.or(captureEffect.then().readsEvalContextRecursive());
		}

		if (ownerFn != null)
			readEvalContext.or(ownerFn.readsEvalContextRecursive());

		if (captureRule != null)
			readEvalContext.or(captureRule.readsEvalContextRecursive());
		
		if (sowEffect != null)
			readEvalContext.or(sowEffect.readsEvalContextRecursive());

		if (origin != null)
			readEvalContext.or(origin.readsEvalContextRecursive());

		if (skipFn != null)
			readEvalContext.or(skipFn.readsEvalContextRecursive());

		if (backtracking != null)
			readEvalContext.or(backtracking.readsEvalContextRecursive());

		if (forward != null)
			readEvalContext.or(forward.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		if (!game.hasTrack())
		{
			game.addRequirementToReport("The ludeme (sow ...) is used but the board has no tracks.");
			missingRequirement = true;
		}

		if (ownerFn != null)
		{
			if (ownerFn instanceof IntConstant)
			{
				final int ownerValue = ((IntConstant) ownerFn).eval(null);
				if (ownerValue < 1 || ownerValue > game.players().count())
				{
					game.addRequirementToReport("A wrong player index is used in (sow ... owner:... ...).");
					missingRequirement = true;
				}

			}
		}

		missingRequirement |= startLoc.missingRequirement(game);
		missingRequirement |= countFn.missingRequirement(game);
		missingRequirement |= numPerHoleFn.missingRequirement(game);

		if (captureEffect != null)
		{
			missingRequirement |= captureEffect.missingRequirement(game);
			if (captureEffect.then() != null)
				missingRequirement |= captureEffect.then().missingRequirement(game);
		}

		if (ownerFn != null)
			missingRequirement |= ownerFn.missingRequirement(game);

		if (captureRule != null)
			missingRequirement |= captureRule.missingRequirement(game);
		
		if (sowEffect != null)
			missingRequirement |= sowEffect.missingRequirement(game);

		if (origin != null)
			missingRequirement |= origin.missingRequirement(game);

		if (skipFn != null)
			missingRequirement |= skipFn.missingRequirement(game);

		if (backtracking != null)
			missingRequirement |= backtracking.missingRequirement(game);

		if (forward != null)
			missingRequirement |= forward.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		willCrash |= startLoc.willCrash(game);
		willCrash |= countFn.willCrash(game);
		willCrash |= numPerHoleFn.willCrash(game);

		if (captureEffect != null)
		{
			willCrash |= captureEffect.willCrash(game);
			if (captureEffect.then() != null)
				willCrash |= captureEffect.then().willCrash(game);
		}

		if (ownerFn != null)
			willCrash |= ownerFn.willCrash(game);

		if (captureRule != null)
			willCrash |= captureRule.willCrash(game);
		
		if (sowEffect != null)
			willCrash |= sowEffect.willCrash(game);

		if (origin != null)
			willCrash |= origin.willCrash(game);

		if (skipFn != null)
			willCrash |= skipFn.willCrash(game);

		if (backtracking != null)
			willCrash |= backtracking.willCrash(game);

		if (forward != null)
			willCrash |= forward.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		// we look at "count" of specific context, so cannot be static
		return false;
	}
	
	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);

		super.preprocess(game);
		
		startLoc.preprocess(game);
		countFn.preprocess(game);
		numPerHoleFn.preprocess(game);
		origin.preprocess(game);
		
		if (captureEffect != null)
			captureEffect.preprocess(game);
		
		if (ownerFn != null)
			ownerFn.preprocess(game);

		if (captureRule != null)
			captureRule.preprocess(game);
		
		if (sowEffect != null)
			sowEffect.preprocess(game);

		if (skipFn != null)
			skipFn.preprocess(game);

		if (backtracking != null)
			backtracking.preprocess(game);

		if (forward != null)
			forward.preprocess(game);

		if (captureEffect != null)
			captureEffect.preprocess(game);

		preComputedTracks = new ArrayList<Track>();
		for (final Track t : game.board().tracks())
			if (trackName == null || t.name().contains(trackName))
				preComputedTracks.add(t);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game)
	{
		String englishString = "Sow with the following rules, ";
		
		if (startLoc != null)
			englishString += "startLoc: " + startLoc.toEnglish(game) + ", ";
		if (countFn != null)
			englishString += "countFn: " + countFn.toEnglish(game) + ", ";
		if (numPerHoleFn != null)
			englishString += "numPerHoleFn: " + numPerHoleFn.toEnglish(game) + ", ";
		if (trackName != null)
			englishString += "trackName: " + trackName + ", ";
		if (ownerFn != null)
			englishString += "ownerFn: " + ownerFn.toEnglish(game) + ", ";
		if (origin != null)
			englishString += "origin: " + origin.toEnglish(game) + ", ";
		if (skipFn != null)
			englishString += "skipFn: " + skipFn.toEnglish(game) + ", ";
		if (captureRule != null)
			englishString += "captureRule: " + captureRule.toEnglish(game) + ", ";
		if (backtracking != null)
			englishString += "backtracking: " + backtracking.toEnglish(game) + ", ";
		if (forward != null)
			englishString += "forward: " + forward.toEnglish(game) + ", ";
		if (captureEffect != null)
			englishString += "captureEffect: " + captureEffect.toEnglish(game) + ", ";
		if (sowEffect != null)
			englishString += "sowEffect: " + sowEffect.toEnglish(game) + ", ";
		if (type != null)
			englishString += "type: " + type.name() + ", ";

		englishString += "includeSelf: " + includeSelf;
		
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);

		return englishString + thenString;
	}
	
	//-------------------------------------------------------------------------

}
