package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.is.player.IsEnemy;
import game.functions.booleans.is.player.IsFriend;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.Between;
import game.functions.ints.iterator.From;
import game.functions.ints.iterator.To;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.RelationType;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Radial;
import game.util.moves.Piece;
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
 * Is used to apply an effect to all the sites surrounded in a specific
 * direction.
 * 
 * @author mrraow and cambolbro and Eric.Piette
 */
public final class Surround extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Location of the piece. */
	private final IntFunction startLocationFn;

	/** Direction chosen. */
	private final AbsoluteDirection dirnChoice;

	/** The piece to surround. */
	private final BooleanFunction targetRule;

	/** The rule to detect the friend to surround. */
	private final BooleanFunction friendRule;

	/** The number of exception to surround. */
	private final IntFunction exception;
	
	/** The number of exception to surround. */
	private final IntFunction withAtLeastPiece;

	/** Moves applied after that one. */
	private final Moves effect;

	/** Add on Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param from     The origin to surround [(from)].
	 * @param relation The way to surround [Adjacent].
	 * @param between  The condition and effect on the pieces to surround [(between
	 *                 if:(is Enemy (to)) (apply (remove (to))))].
	 * @param to       The condition on the pieces surrounding [(to if:(isFriend
	 *                 (between)))].
	 * @param except   The number of exceptions allowed to apply the effect [0].
	 * @param with     The piece which should at least be in the surrounded pieces.
	 * @param then     The moves applied after that move is applied.
	 * 
	 * @example (surround (from (last To)) Orthogonal (between if:(is Friend (who
	 *          at:(between))) (apply (trigger "Lost" (mover))) ) (to if:(not
	 *          (is In (to) (sites Empty))) ) )
	 */
	public Surround
	(
		@Opt       final game.util.moves.From    from,
		@Opt       final RelationType            relation,
		@Opt       final game.util.moves.Between between,
		@Opt       final game.util.moves.To      to,
	    @Opt @Name final IntFunction             except,
		@Opt @Name final Piece                   with,
		@Opt       final Then                    then
	)
	{
		super(then);
		startLocationFn = (from == null) ? new From(null) : from.loc();
		type = (from == null) ? null : from.type();

		dirnChoice = (relation == null) ? AbsoluteDirection.Adjacent : RelationType.convert(relation);
		targetRule = (between == null || between.condition() == null)
				? new IsEnemy(Between.instance(), null)
				: between.condition();
		friendRule = (to == null || to.cond() == null)
				? new IsFriend(To.instance(), null)
				: to.cond();
		effect = (between == null || between.effect() == null)
				? new Remove(null, Between.instance(), null, null, null, null, null)
				: between.effect();
		exception = (except == null) ? new IntConstant(0) : except;
		withAtLeastPiece = (with == null) ? null : with.component();
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
		
		final int nbExcept = exception.eval(context);
		final int withPiece = (withAtLeastPiece == null) ? Constants.UNDEFINED : withAtLeastPiece.eval(context);

		final Topology graph = context.topology();

		if (from == Constants.UNDEFINED)
			return new BaseMoves(null);

		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final TopologyElement fromV = graph.getGraphElements(realType).get(from);

		final List<Radial> radialsFrom = graph.trajectories().radials(realType, fromV.index(), dirnChoice);

		for (final Radial radialFrom : radialsFrom)
		{
			final FastTIntArrayList betweenSites = new FastTIntArrayList();
			if (radialFrom.steps().length < 2)
				continue;

			final int locationUnderThreat = radialFrom.steps()[1].id();
			if (!isTarget(context, locationUnderThreat))
				continue;

			// Check neighbours of threatened pieces
			int except = 0;

			// Check if at least one piece surrounding the target is the correct piece.
			boolean withPieceOk = false;

			final List<Radial> radialsUnderThreat = graph.trajectories().radials(type, locationUnderThreat, dirnChoice);


			for (final Radial radialUnderThreat : radialsUnderThreat)
			{
				final int friendPieceSite = radialUnderThreat.steps()[1].id();
				final boolean isThreat = radialUnderThreat.steps().length < 2 || friendPieceSite == from
						|| isFriend(context, friendPieceSite);

				if (!isThreat)
					except++;

				final int whatFriend = context.containerState(context.containerId()[friendPieceSite])
						.what(friendPieceSite, realType);

				if (withPiece == Constants.UNDEFINED || withPiece == whatFriend)
					withPieceOk = true;
				
				if (except > nbExcept)
					break;
			}

			if (except <= nbExcept && withPieceOk)
			{
				context.setBetween(locationUnderThreat);
				betweenSites.add(locationUnderThreat);
				MoveUtilities.chainRuleCrossProduct(context, moves, effect, null, false);
				moves.moves().get(moves.moves().size() - 1).setBetweenNonDecision(new FastTIntArrayList(betweenSites));

			}
		}

		context.setBetween(betweenOrig);
		context.setFrom(fromOrig);
		context.setTo(toOrig);

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	private boolean isFriend(
			final Context context,
			final int location
	)
	{
		context.setTo(location);
		return friendRule.eval(context);
	}

	private boolean isTarget(
			final Context context,
			final int location
	)
	{
		context.setBetween(location);
		return targetRule.eval(context);
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = startLocationFn.gameFlags(game)
				| 
				targetRule.gameFlags(game) 
				| 
				friendRule.gameFlags(game)
				| 
				effect.gameFlags(game) 
				| 
				exception.gameFlags(game) | super.gameFlags(game);

		if (withAtLeastPiece != null)
			gameFlags |= withAtLeastPiece.gameFlags(game);

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
		concepts.or(friendRule.concepts(game));
		concepts.or(effect.concepts(game));
		concepts.or(exception.concepts(game));

		if (withAtLeastPiece != null)
			concepts.or(withAtLeastPiece.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		// We check if that's effectively a capture (remove or fromTo).
		if (effect.concepts(game).get(Concept.RemoveEffect.id())
				|| effect.concepts(game).get(Concept.FromToEffect.id()))
			concepts.set(Concept.SurroundCapture.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(startLocationFn.writesEvalContextRecursive());
		writeEvalContext.or(targetRule.writesEvalContextRecursive());
		writeEvalContext.or(friendRule.writesEvalContextRecursive());
		writeEvalContext.or(effect.writesEvalContextRecursive());
		writeEvalContext.or(exception.writesEvalContextRecursive());

		if (withAtLeastPiece != null)
			writeEvalContext.or(withAtLeastPiece.writesEvalContextRecursive());

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
		readEvalContext.or(targetRule.readsEvalContextRecursive());
		readEvalContext.or(friendRule.readsEvalContextRecursive());
		readEvalContext.or(effect.readsEvalContextRecursive());
		readEvalContext.or(exception.readsEvalContextRecursive());

		if (withAtLeastPiece != null)
			readEvalContext.or(withAtLeastPiece.readsEvalContextRecursive());

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
		missingRequirement |= targetRule.missingRequirement(game);
		missingRequirement |= friendRule.missingRequirement(game);
		missingRequirement |= effect.missingRequirement(game);
		missingRequirement |= exception.missingRequirement(game);

		if (withAtLeastPiece != null)
			missingRequirement |= withAtLeastPiece.missingRequirement(game);

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
		willCrash |= friendRule.willCrash(game);
		willCrash |= effect.willCrash(game);
		willCrash |= exception.willCrash(game);

		if (withAtLeastPiece != null)
			willCrash |= withAtLeastPiece.willCrash(game);

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
		type = SiteType.use(type, game);

		super.preprocess(game);
		
		startLocationFn.preprocess(game);
		targetRule.preprocess(game);
		friendRule.preprocess(game);
		effect.preprocess(game);
		exception.preprocess(game);
		if (withAtLeastPiece != null)
			withAtLeastPiece.preprocess(game);
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
		
		String exceptString = "";
		if (exception != null)
			exceptString = " except if " + exception.toEnglish(game);
		
		String directionString = "";
		if (dirnChoice != null)
			directionString += " with "+ dirnChoice.name()+ " direction";
		
		String fromString = "";
		if (startLocationFn != null)
			fromString = " starting from " + startLocationFn.toEnglish(game);
		
		String limitString = "";
		if (withAtLeastPiece != null)
			limitString = " with at least " + withAtLeastPiece.toEnglish(game) + " pieces";

		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "for all surrounded pieces on " + type.name() + StringRoutines.getPlural(type.name()) + fromString + directionString + limitString + targetString + friendString + exceptString + effect.toEnglish(game) + thenString;
	}

	//-------------------------------------------------------------------------

}
