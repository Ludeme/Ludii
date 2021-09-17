package game.rules.play.moves.nonDecision.effect.set.site;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.board.SiteType;
import game.types.state.GameType;
import main.Constants;
import other.action.BaseAction;
import other.action.state.ActionSetValue;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Sets the piece value of a location.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SetValue extends Effect
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/** The site. */
	protected final IntFunction siteFn;

	/** The level. */
	private final IntFunction levelFn;

	/** The value. */
	protected final IntFunction value;

	/** Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type  The graph element type [default SiteType of the board].
	 * @param site  The site to modify the local state.
	 * @param level The level to modify the local state.
	 * @param value The new piece value.
	 * @param then  The moves applied after that move is applied.
	 */
	public SetValue
	(
		      @Opt  final SiteType    type,
		@Name       final IntFunction site,
			  @Opt  final IntFunction level,
			        final IntFunction value,
		      @Opt  final Then        then
	)
	{
		super(then);
		siteFn = site;
		this.value = value;
		this.type = type;
		levelFn = level;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		final int level = levelFn == null ? Constants.UNDEFINED : levelFn.eval(context);
		final int valueInt = value.eval(context);

		if (valueInt < 0 || level < Constants.UNDEFINED)
			return moves;

		final BaseAction action = new ActionSetValue(type, siteFn.eval(context), level, valueInt);
		final Move move = new Move(action);
		moves.moves().add(move);

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
		long gameFlags = GameType.Value | siteFn.gameFlags(game) | value.gameFlags(game) | super.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		if (levelFn != null)
			gameFlags |= levelFn.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);
		
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(siteFn.concepts(game));
		if (levelFn != null)
			concepts.or(levelFn.concepts(game));
		concepts.set(Concept.PieceValue.id(), true);
		concepts.set(Concept.SetValue.id(), true);
		concepts.or(SiteType.concepts(type));
		concepts.or(value.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(siteFn.writesEvalContextRecursive());
		writeEvalContext.or(value.writesEvalContextRecursive());
		if (levelFn != null)
			writeEvalContext.or(levelFn.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(siteFn.readsEvalContextRecursive());
		readEvalContext.or(value.readsEvalContextRecursive());
		if (levelFn != null)
			readEvalContext.or(levelFn.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (levelFn != null)
			missingRequirement |= levelFn.missingRequirement(game);
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= siteFn.missingRequirement(game);
		missingRequirement |= value.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (levelFn != null)
			willCrash |= levelFn.willCrash(game);
		willCrash |= super.willCrash(game);
		willCrash |= siteFn.willCrash(game);
		willCrash |= value.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		if (levelFn != null && !levelFn.isStatic())
			return false;

		return siteFn.isStatic() && value.isStatic();
	}
	
	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		super.preprocess(game);
		if (levelFn != null)
			levelFn.preprocess(game);
		siteFn.preprocess(game);
		value.preprocess(game);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString() 
	{
		return "SetValue [siteFn=" + siteFn + ", value=" + value + "then=" + then() + "]";
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{		
		String levelString = "";
		if (levelFn != null)
			levelString = " at " + levelFn.toString();
		
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "set the count of " + type.name().toLowerCase() + " " + siteFn.toEnglish(game) + levelString + " to " + value.toEnglish(game) + thenString;
	}
	
	//-------------------------------------------------------------------------
		
}

