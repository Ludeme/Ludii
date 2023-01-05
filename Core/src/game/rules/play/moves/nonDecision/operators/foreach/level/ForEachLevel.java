package game.rules.play.moves.nonDecision.operators.foreach.level;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.board.SiteType;
import game.util.directions.StackDirection;
import main.collections.FastArrayList;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.Move;
import other.state.container.ContainerState;

/**
 * Applies a move for each level of a site.
 * 
 * @author Eric.Piette
 */
@Hide
public final class ForEachLevel extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Location of the piece. */
	private final IntFunction siteFn;

	/** The moves to apply. */
	private final Moves generator;

	/** The siteType to look */
	private SiteType type;

	/** To start from the bottom or the top of the stack. */
	private final StackDirection stackDirection;

	/**
	 * @param type           The type of the graph elements of the group [default SiteType of the board].
	 * @param siteFn         The site to iterate through.
	 * @param stackDirection The direction to count in the stack [FromTop].
	 * @param generator      The move to apply.
	 * @param then           The moves applied after that move is applied.
	 */
	public ForEachLevel
	(
		@Opt final SiteType       type,
			 final IntFunction    siteFn,
		@Opt final StackDirection stackDirection,
			 final Moves          generator,
		@Opt final Then           then
	)
	{
		super(then);
		this.siteFn = siteFn;
		this.generator = generator;
		this.type = type;
		this.stackDirection = (stackDirection == null) ? StackDirection.FromTop : stackDirection;
	}

	@Override
	public Moves eval(final Context context)
	{
		final int site = siteFn.eval(context);
		final Moves moves = new BaseMoves(super.then());
		
		final int savedTo = context.to();
		final int originSiteValue = context.site();

		final int cid = site >= context.containerId().length ? 0 : context.containerId()[site];
		SiteType realType = type;
		if (cid > 0)
			realType = SiteType.Cell;
		else if (realType == null)
			realType = context.board().defaultSite();
		final ContainerState cs = context.state().containerStates()[cid];
		final int stackSize = cs.sizeStack(site, realType);

		if(stackDirection.equals(StackDirection.FromBottom))
		{
			for (int level = 0; level < stackSize; level++)
			{
				context.setLevel(level);
				final FastArrayList<Move> generatedMoves = generator.eval(context).moves();
				moves.moves().addAll(generatedMoves);
			}
		}
		else
		{
			for (int level = stackSize-1; level >= 0; level--)
			{
				context.setLevel(level);
				final FastArrayList<Move> generatedMoves = generator.eval(context).moves();
				moves.moves().addAll(generatedMoves);
			}
		}

		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		context.setTo(savedTo);
		context.setSite(originSiteValue);

		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = siteFn.gameFlags(game) | generator.gameFlags(game) | super.gameFlags(game);
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
		concepts.or(siteFn.concepts(game));
		concepts.or(generator.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		concepts.set(Concept.ControlFlowStatement.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(siteFn.writesEvalContextRecursive());
		writeEvalContext.or(generator.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.Level.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(siteFn.readsEvalContextRecursive());
		readEvalContext.or(generator.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= siteFn.missingRequirement(game);
		missingRequirement |= generator.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= siteFn.willCrash(game);
		willCrash |= generator.willCrash(game);

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
		if (type == null)
			type = game.board().defaultSite();

		super.preprocess(game);
		siteFn.preprocess(game);
		generator.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{		
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "for each level at " + ((type == null) ? game.board().defaultSite().name() :  type.name()) + " " + siteFn.toEnglish(game) + " (" + stackDirection.name() + ") " + generator.toEnglish(game) + thenString;
	}
	
	//-------------------------------------------------------------------------

}
