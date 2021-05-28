package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.play.WhenType;
import game.types.state.GameType;
import main.Constants;
import other.IntArrayFromRegion;
import other.action.move.ActionRemove;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerState;

/**
 * Removes an item from a site.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks If the site is empty, the move is not applied.
 */
public final class Remove extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** Which sites. */
	private final IntArrayFromRegion regionFunction;

	/** The number of pieces to remove. */
	private final IntFunction countFn;

	/** The level of the piece to remove. */
	private final IntFunction levelFn;

	/** Add on Cell/Edge/Vertex. */
	private SiteType type;

	/** When to apply the removal (immediately unless otherwise specified). */
	private final WhenType when;

	//-------------------------------------------------------------------------

	/**
	 * @param type             The graph element type of the location [Cell (or
	 *                         Vertex if the main board uses this)].
	 * @param locationFunction The location to remove a piece.
	 * @param regionFunction   The locations to remove a piece.
	 * @param level            The level to remove a piece [top level].
	 * @param at               When to perform the removal [immediately].
	 * @param count            The number of pieces to remove [1].
	 * @param then             The moves applied after that move is applied.
	 * 
	 * @example (remove (last To))
	 * 
	 * @example (remove (last To) at:EndOfTurn)
	 */
	public Remove
	(
		@Opt            final SiteType       type,
			 @Or        final IntFunction    locationFunction, 
			 @Or        final RegionFunction regionFunction, 
		@Opt     @Name  final IntFunction    level,
		@Opt     @Name  final WhenType       at,
		@Opt     @Name  final IntFunction    count,
		@Opt 	        final Then           then
	)
	{
		super(then);
		
		int numNonNull = 0;
		if (locationFunction != null)
			numNonNull++;
		if (regionFunction != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Remove(): Only one of locationFunction or regionFunction has to be non-null.");
		
		this.regionFunction = new IntArrayFromRegion(locationFunction, regionFunction);
		this.type = type;
		this.when = at;
		this.countFn = (count == null) ? new IntConstant(1) : count;
		this.levelFn = level;
	}
  
	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		// Return list of legal "to" moves
		final Moves moves = new BaseMoves(super.then());

		final int[] locs = regionFunction.eval(context);

		final int count = countFn.eval(context);

		for (final int loc : locs)
		{
			final int cid = loc >= context.containerId().length ? 0 : context.containerId()[loc];

			SiteType realType = type;
			if (cid > 0)
				realType = SiteType.Cell;
			else if (realType == null)
				realType = context.board().defaultSite();

			int numToRemove = count;

			if (loc < 0)
				continue;

			final ContainerState cs = context.state().containerStates()[cid];

			if (cs.what(loc, realType) <= 0)
				continue;

			final boolean applyNow = (when == WhenType.EndOfTurn) ? false : true;

			int level = (levelFn != null) ? levelFn.eval(context)
					: cs.sizeStack(loc, realType) - 1;
			level = (level < 0) ? 0 : level;
			
			level = (!context.game().isStacking()
					|| cs.sizeStack(loc, realType) == (level + 1)) ? Constants.UNDEFINED
							: level;

			final ActionRemove actionRemove = new other.action.move.ActionRemove(realType, loc, level, applyNow);
			if (isDecision())
				actionRemove.setDecision(true);
			final Move move = new Move(actionRemove);

			numToRemove--;
			while (numToRemove > 0)
			{
				move.actions().add(new other.action.move.ActionRemove(realType, loc, level, applyNow));
				numToRemove--;
			}

			move.setMover(context.state().mover());
			moves.moves().add(move);

			if (then() != null)
				move.then().add(then().moves());
		}

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
		
		if (then() != null)
			gameFlags |= then().gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		if (when != null)
			gameFlags |= GameType.SequenceCapture;
		
		return gameFlags | regionFunction.gameFlags(game) | countFn.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(super.concepts(game));
		concepts.set(Concept.Remove.id(), true);

		if (when != null)
			concepts.set(Concept.CaptureSequence.id(), true);

		if (isDecision())
			concepts.set(Concept.RemoveDecision.id(), true);

		if (then() != null)
			concepts.or(then().concepts(game));

		concepts.or(regionFunction.concepts(game));
		concepts.or(countFn.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());

		writeEvalContext.or(regionFunction.writesEvalContextRecursive());
		writeEvalContext.or(countFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());

		readEvalContext.or(regionFunction.readsEvalContextRecursive());
		readEvalContext.or(countFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);

		missingRequirement |= regionFunction.missingRequirement(game);
		missingRequirement |= countFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);

		willCrash |= regionFunction.willCrash(game);
		willCrash |= countFn.willCrash(game);
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
		type = SiteType.use(type, game);

		super.preprocess(game);
		
		regionFunction.preprocess(game);
		countFn.preprocess(game);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game)
	{
		return "Remove";
	}
}
