package game.rules.play.moves.nonDecision.effect;

import java.util.ArrayList;
import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.To;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.moves.Flips;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.action.BaseAction;
import other.action.move.ActionAdd;
import other.action.state.ActionSetState;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerState;

/**
 * Is used to flip a piece.
 * 
 * @author Eric.Piette
 */
public final class Flip extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** location. */
	protected final IntFunction locFn;

	/** Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type The graph element type [default SiteType of the board].
	 * @param loc  The location to flip the piece [(to)].
	 * @param then The moves applied after that move is applied.
	 * 
	 * @example (flip)
	 * 
	 * @example (flip (last To))
	 */
	public Flip 
	(
		@Opt final SiteType    type,
		@Opt final IntFunction loc,
		@Opt final Then        then
	)
	{
		super(then);
		this.locFn = (loc == null) ? To.instance() : loc;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());
		
		final int loc = locFn.eval(context);
		
		if (loc == Constants.OFF)
			return moves;

		final int cid = loc >= context.containerId().length ? 0 : context.containerId()[loc];
		SiteType realType = type;
		if (cid > 0)
			realType = SiteType.Cell;
		else if (realType == null)
			realType = context.board().defaultSite();
		final ContainerState cs = context.state().containerStates()[cid];
		final int stackSize = cs.sizeStack(loc, realType);

		if (stackSize > 1)
		{
			final Move move = new Move(new ArrayList<other.action.Action>());
			final TIntArrayList whats = new TIntArrayList();
			final TIntArrayList states = new TIntArrayList();
			final TIntArrayList rotations = new TIntArrayList();
			final TIntArrayList values = new TIntArrayList();
			for (int level = 0; level < stackSize; level++)
			{
				whats.add(cs.what(loc, level, realType));
				states.add(cs.state(loc, level, realType));
				rotations.add(cs.rotation(loc, level, realType));
				values.add(cs.value(loc, level, realType));
				move.actions().add(new other.action.move.ActionRemove(realType, loc, level, true));
			}

			for (int level = 0; level < stackSize; level++)
			{
				final int what = whats.get(whats.size() - level - 1);
				final int value = values.get(whats.size() - level - 1);
				final int rotation = rotations.get(whats.size() - level - 1);
				int state = states.get(states.size() - level - 1);
				final Flips flips = context.components()[what].getFlips();
				if (flips != null)
					state = flips.flipState(state);
				move.actions().add(new ActionAdd(realType, loc, what, 1, state, rotation, value, Boolean.TRUE));
			}

			moves.moves().add(move);
		}
		else if (stackSize == 1)
		{
			final int currentState = context.containerState(context.containerId()[loc]).state(loc, realType);
			final int whatValue = context.containerState(context.containerId()[loc]).what(loc, realType);

			if (whatValue == 0)
				return moves;

			final Flips flips = context.components()[whatValue].getFlips();

			if (flips == null)
				return moves;

			final int newState = flips.flipState(currentState);

			final BaseAction action = new ActionSetState(realType, loc, Constants.UNDEFINED, newState);
			final Move m = new Move(action);
			moves.moves().add(m);
		}

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
	public String toString()
	{
		return "Flip(" + locFn + ")";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = GameType.SiteState | locFn.gameFlags(game) | super.gameFlags(game);

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
		concepts.set(Concept.Flip.id(), true);
		concepts.or(locFn.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(locFn.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(locFn.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		boolean gameHasComponentsWithFlips = false;
		for (int i = 1; i < game.equipment().components().length; i++)
		{
			final Component component = game.equipment().components()[i];
			if (component.getFlips() != null)
			{
				gameHasComponentsWithFlips = true;
				break;
			}
		}
		if (!gameHasComponentsWithFlips)
		{
			game.addRequirementToReport("The ludeme (flip ...) is used but no component has flips defined.");
			missingRequirement = true;
		}
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= locFn.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= locFn.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return locFn.isStatic();
	}
	
	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		super.preprocess(game);
		locFn.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "Flip";
	}
}
