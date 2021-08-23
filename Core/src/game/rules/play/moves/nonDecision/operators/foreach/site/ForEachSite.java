package game.rules.play.moves.nonDecision.operators.foreach.site;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.region.RegionFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.util.equipment.Region;
import main.collections.FastArrayList;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.Move;

/**
 * Applies a move for each site in a region.
 * 
 * @author mrraow and cambolbro and Eric.Piette
 * 
 * @remarks Useful when a move has to be applied to all sites of a region
 *          according to some conditions.
 */
@Hide
public final class ForEachSite extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Location of the piece. */
	private final RegionFunction regionFn;

	/** The moves to apply. */
	private final Moves generator;

	/**
	 * The moves to apply if the list of moves resulting from the generator is
	 * empty.
	 */
	private final Moves elseMoves;

	/**
	 * @param regionFn  The region used.
	 * @param generator The move to apply.
	 * @param noMoveYet The moves to apply if the list of moves resulting from the
	 *                  generator is empty.
	 * @param then      The moves applied after that move is applied.
	 */
	public ForEachSite
	(
			 	   final RegionFunction regionFn,
			 	   final Moves          generator,
		@Opt @Name final Moves          noMoveYet,
		@Opt 	   final Then           then
	)
	{
		super(then);
		this.regionFn = regionFn;
		this.generator = generator;
		elseMoves = noMoveYet;
	}

	@Override
	public Moves eval(final Context context)
	{
		final Region sites = regionFn.eval(context);
		final Moves moves = new BaseMoves(super.then());
		
		final int savedTo = context.to();
		final int originSiteValue = context.site();

		for (int site = sites.bitSet().nextSetBit(0); site >= 0; site = sites.bitSet().nextSetBit(site + 1))
		{
			context.setTo(site);
			context.setSite(site);
			final FastArrayList<Move> generatedMoves = generator.eval(context).moves();
			moves.moves().addAll(generatedMoves);
		}

		if (moves.moves().isEmpty() && elseMoves != null)
			return elseMoves.eval(context);

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
		long gameFlags = regionFn.gameFlags(game) | generator.gameFlags(game) | super.gameFlags(game);

		if (elseMoves != null)
			gameFlags |= elseMoves.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(regionFn.concepts(game));
		concepts.or(generator.concepts(game));

		if (elseMoves != null)
			concepts.or(elseMoves.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		concepts.set(Concept.ControlFlowStatement.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(regionFn.writesEvalContextRecursive());
		writeEvalContext.or(generator.writesEvalContextRecursive());

		if (elseMoves != null)
			writeEvalContext.or(elseMoves.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.To.id(), true);
		writeEvalContext.set(EvalContextData.Site.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(regionFn.readsEvalContextRecursive());
		readEvalContext.or(generator.readsEvalContextRecursive());

		if (elseMoves != null)
			readEvalContext.or(elseMoves.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= regionFn.missingRequirement(game);
		missingRequirement |= generator.missingRequirement(game);

		if (elseMoves != null)
			missingRequirement |= elseMoves.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= regionFn.willCrash(game);
		willCrash |= generator.willCrash(game);

		if (elseMoves != null)
			willCrash |= elseMoves.willCrash(game);

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
		regionFn.preprocess(game);
		generator.preprocess(game);
		if (elseMoves != null)
			elseMoves.preprocess(game);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game)
	{
		//return getClass().getSimpleName();
		
		String text="";
		if(regionFn != null) {
			text+="each turn, where the site is "+ regionFn.toEnglish(game);
			text+=generator.toEnglish(game);
		}
		return text;
	}
}
