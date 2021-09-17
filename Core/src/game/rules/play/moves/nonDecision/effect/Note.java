package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.directions.DirectionsFunction;
import game.functions.floats.FloatFunction;
import game.functions.graph.GraphFunction;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.board.Id;
import game.functions.range.RangeFunction;
import game.functions.region.RegionFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.play.RoleType;
import game.types.state.GameType;
import game.util.directions.Direction;
import gnu.trove.list.array.TIntArrayList;
import other.action.others.ActionNote;
import other.context.Context;
import other.move.Move;

/**
 * Makes a note to a player or to all the players.
 * 
 * @author Eric.Piette and cambolbro
 */
public final class Note extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The index of the player. */
	final IntFunction playerMessage;

	/** The index of the player. */
	final IntFunction playerFn;

	/** The role in the message. */
	final RoleType roleMessage;

	/** The role. */
	final RoleType role;

	/** The message. */
	final String message;

	/** The int message. */
	final IntFunction messageInt;

	/** The int array message. */
	final IntArrayFunction messageIntArray;

	/** The float message. */
	final FloatFunction messageFloat;

	/** The boolean message. */
	final BooleanFunction messageBoolean;

	/** The region message. */
	final RegionFunction messageRegion;

	/** The range message. */
	final RangeFunction messageRange;

	/** The directions message. */
	final DirectionsFunction messageDirection;

	/** The graph message. */
	final GraphFunction messageGraph;

	//-------------------------------------------------------------------------

	/**
	 * @param player           The index of the player to add at the beginning of
	 *                         the message.
	 * @param Player           The role of the player to add at the beginning of the
	 *                         message.
	 * @param message          The message as a string.
	 * @param messageInt       The message as an integer.
	 * @param messageIntArray  The message as an array of integer.
	 * @param messageFloat     The message as a float.
	 * @param messageBoolean   The message as a boolean.
	 * @param messageRegion    The message as a region.
	 * @param messageRange     The message as a range.
	 * @param messageDirection The message as a set of directions.
	 * @param messageGraph     The message as a graph.
	 * @param to               The index of the player.
	 * @param To               The role of the player [ALL].
	 * 
	 * @example (note "Opponent has played")
	 */
	public Note
	(
		@Opt @Or @Name  final IntFunction            player,
		@Opt @Or @Name  final RoleType               Player,
		     @Or2       final String                 message,
		     @Or2       final IntFunction            messageInt,
		     @Or2       final IntArrayFunction       messageIntArray,
		     @Or2       final FloatFunction          messageFloat,
		     @Or2       final BooleanFunction        messageBoolean,
		     @Or2       final RegionFunction         messageRegion,
		     @Or2       final RangeFunction          messageRange,
		     @Or2       final Direction              messageDirection,
		     @Or2       final GraphFunction          messageGraph,
		@Opt @Or @Name  final game.util.moves.Player to,
		@Opt @Or @Name  final RoleType               To
	)
	{
		super(null);

		int numNonNull = 0;
		if (Player != null)
			numNonNull++;
		if (player != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Note(): Only one 'playerMessage' or 'roleMessage' parameters can be non-null.");
		
		numNonNull = 0;
		if (to != null)
			numNonNull++;
		if (To != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Note(): Only one 'to' or 'role' parameters can be non-null.");

		numNonNull = 0;
		if (message != null)
			numNonNull++;
		if (messageInt != null)
			numNonNull++;
		if (messageIntArray != null)
			numNonNull++;
		if (messageFloat != null)
			numNonNull++;
		if (messageBoolean != null)
			numNonNull++;
		if (messageRegion != null)
			numNonNull++;
		if (messageRange != null)
			numNonNull++;
		if (messageDirection != null)
			numNonNull++;
		if (messageGraph != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Note(): One 'message', 'messageInt', 'messageIntArray', messageFloat', 'messageBoolean', 'messageRegion', 'messageRange',"
							+ " 'messageDirection' or 'messageGraph' parameters must be non-null.");

		playerFn = (to != null) ? to.index() : (To != null) ? RoleType.toIntFunction(To) : new Id(null, RoleType.All);
		role = (to == null && To == null) ? RoleType.All : To;
		this.message = message;
		this.messageInt = messageInt;
		this.messageIntArray = messageIntArray;
		this.messageFloat = messageFloat;
		this.messageBoolean = messageBoolean;
		this.messageRegion = messageRegion;
		this.messageRange = messageRange;
		this.messageDirection = (messageDirection != null) ? messageDirection.directionsFunctions() : null;
		this.messageGraph = messageGraph;
		playerMessage = (Player != null) ? new Id(null, Player) : player;
		roleMessage = Player;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		// Return list of legal "to" moves
		final BaseMoves moves = new BaseMoves(super.then());
		
//		final String msg = 
//				(message != null) 
//				? message 
//				: 	(messageInt != null) 
//					? messageInt.eval(context) + "" 
//					: 	(messageIntArray != null) 
//						? messageIntArray.eval(context) + "" 
//						:	(messageFloat != null) 
//							? messageFloat.eval(context) + "" 
//							: 	(messageBoolean != null) 
//								? messageBoolean.eval(context) + "" 
//								:	(messageRegion != null) 
//									? new TIntArrayList(messageRegion.eval(context).sites()) + "" 
//									:	(messageRange != null)
//										? "[" + messageRange.eval(context).min(context) + ";" + messageRange.eval(context).max(context) + "]"
//										:
//											// For the directions we look the result for the first centre site of the default SiteType on the board.
//											(messageDirection != null) 
//											? messageDirection.convertToAbsolute(context.board().defaultSite(),context.topology().centre(context.board().defaultSite()).get(0),null, null, null, context) + ""
//											: 	(messageGraph != null) 
//												? messageGraph.eval(context, context.board().defaultSite()) + ""
//												: null;

		final String msg;
		if (      message != null) 
			msg = message;
		else if ( messageInt != null) 
			msg = messageInt.eval(context) + ""; 
		else if ( messageIntArray != null) 
			msg = messageIntArray.eval(context) + ""; 
		else if ( messageFloat != null) 
			msg = messageFloat.eval(context) + ""; 
		else if ( messageBoolean != null) 
			msg = messageBoolean.eval(context) + "";
		else if ( messageRegion != null) 
			msg = new TIntArrayList(messageRegion.eval(context).sites()) + "";
		else if ( messageRange != null)
			msg = "[" + messageRange.eval(context).min(context) + ";" + messageRange.eval(context).max(context) + "]";
		else if ( messageDirection != null)  // For the directions we look the result for the first centre site of the default SiteType on the board.
			msg = messageDirection.convertToAbsolute
				  (
				  	  context.board().defaultSite(),context.topology().centre(context.board().defaultSite()).get(0), null, null, null, context
				  ) + "";
		else if ( messageGraph != null) 
			msg = messageGraph.eval(context, context.board().defaultSite()) + "";
		else
			msg = null;

		final String messageToSend = (playerMessage == null) ? msg : "P" + playerMessage.eval(context) + " " + msg;

		if (role == RoleType.All)
		{
			for(int i = 1; i < context.game().players().size();i++)
			{
				final Move move = new Move(new ActionNote(messageToSend, i));
				moves.moves().add(move);
			}
		}
		else
		{
			final int pid = playerFn.eval(context);

			if (pid > 0 && pid < context.game().players().size())
			{
				final Move move = new Move(new ActionNote(messageToSend, pid));
				moves.moves().add(move);
			}
		}

		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean canMove(final Context context)
	{
		return false;
	}

	@Override
	public boolean canMoveTo(final Context context, final int target)
	{
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game) | GameType.Note | playerFn.gameFlags(game);
		
		if (playerMessage != null)
			gameFlags |= playerMessage.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		if (messageInt != null)
			gameFlags |= messageInt.gameFlags(game);
		if (messageIntArray != null)
			gameFlags |= messageIntArray.gameFlags(game);
		if (messageFloat != null)
			gameFlags |= messageFloat.gameFlags(game);
		if (messageBoolean != null)
			gameFlags |= messageBoolean.gameFlags(game);
		if (messageRange != null)
			gameFlags |= messageRange.gameFlags(game);
		if (messageRegion != null)
			gameFlags |= messageRegion.gameFlags(game);
		if (messageGraph != null)
			gameFlags |= messageGraph.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(playerFn.concepts(game));
		concepts.or(super.concepts(game));

		if (playerMessage != null)
			concepts.or(playerMessage.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		if (messageInt != null)
			concepts.or(messageInt.concepts(game));
		if (messageIntArray != null)
			concepts.or(messageIntArray.concepts(game));
		if (messageFloat != null)
			concepts.or(messageFloat.concepts(game));
		if (messageBoolean != null)
			concepts.or(messageBoolean.concepts(game));
		if (messageRange != null)
			concepts.or(messageRange.concepts(game));
		if (messageRegion != null)
			concepts.or(messageRegion.concepts(game));
		if (messageGraph != null)
			concepts.or(messageGraph.concepts(game));

		return concepts;
	}
	
	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(playerFn.writesEvalContextRecursive());
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (playerMessage != null)
			writeEvalContext.or(playerMessage.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());

		if (messageInt != null)
			writeEvalContext.or(messageInt.writesEvalContextRecursive());
		if (messageIntArray != null)
			writeEvalContext.or(messageIntArray.writesEvalContextRecursive());
		if (messageBoolean != null)
			writeEvalContext.or(messageBoolean.writesEvalContextRecursive());
		if (messageRange != null)
			writeEvalContext.or(messageRange.writesEvalContextRecursive());
		if (messageRegion != null)
			writeEvalContext.or(messageRegion.writesEvalContextRecursive());
		if (messageGraph != null)
			writeEvalContext.or(messageGraph.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(playerFn.readsEvalContextRecursive());
		readEvalContext.or(super.readsEvalContextRecursive());

		if (playerMessage != null)
			readEvalContext.or(playerMessage.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());

		if (messageInt != null)
			readEvalContext.or(messageInt.readsEvalContextRecursive());
		if (messageIntArray != null)
			readEvalContext.or(messageIntArray.readsEvalContextRecursive());
		if (messageBoolean != null)
			readEvalContext.or(messageBoolean.readsEvalContextRecursive());
		if (messageRange != null)
			readEvalContext.or(messageRange.readsEvalContextRecursive());
		if (messageRegion != null)
			readEvalContext.or(messageRegion.readsEvalContextRecursive());
		if (messageGraph != null)
			readEvalContext.or(messageGraph.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= playerFn.missingRequirement(game);
		missingRequirement |= super.missingRequirement(game);

		// We check if the role is correct.
		if (role != null)
		{
			final int indexOwnerPhase = role.owner();
			if ((indexOwnerPhase < 1 && !role.equals(RoleType.All) && !role.equals(RoleType.Mover)
					&& !role.equals(RoleType.Prev) && !role.equals(RoleType.Next))
					|| indexOwnerPhase > game.players().count())
			{
				game.addRequirementToReport("A (note ...) ludeme is used with an incorrect RoleType " + role + ".");
				missingRequirement = true;
			}
		}
		
		// We check if the role is correct.
		if (roleMessage != null)
		{
			final int indexOwnerPhase = roleMessage.owner();
			if ((indexOwnerPhase < 1 && !roleMessage.equals(RoleType.Mover) && !roleMessage.equals(RoleType.Prev)
					&& !roleMessage.equals(RoleType.Next)) || indexOwnerPhase > game.players().count())
			{
				game.addRequirementToReport("A (note ...) ludeme is used with an incorrect RoleType " + roleMessage + ".");
				missingRequirement = true;
			}
		}

		if (playerMessage != null)
		missingRequirement |= playerMessage.missingRequirement(game);

		if (messageInt != null)
			missingRequirement |= messageInt.missingRequirement(game);
		if (messageIntArray != null)
			missingRequirement |= messageIntArray.missingRequirement(game);
		if (messageFloat != null)
			missingRequirement |= messageFloat.missingRequirement(game);
		if (messageBoolean != null)
			missingRequirement |= messageBoolean.missingRequirement(game);
		if (messageRange != null)
			missingRequirement |= messageRange.missingRequirement(game);
		if (messageRegion != null)
			missingRequirement |= messageRegion.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= playerFn.willCrash(game);
		willCrash |= super.willCrash(game);

		if (playerMessage != null)
			willCrash |= playerMessage.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		if (messageInt != null)
			willCrash |= messageInt.willCrash(game);
		if (messageIntArray != null)
			willCrash |= messageIntArray.willCrash(game);
		if (messageFloat != null)
			willCrash |= messageFloat.willCrash(game);
		if (messageBoolean != null)
			willCrash |= messageBoolean.willCrash(game);
		if (messageRange != null)
			willCrash |= messageRange.willCrash(game);
		if (messageRegion != null)
			willCrash |= messageRegion.willCrash(game);

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
		playerFn.preprocess(game);
		
		if (playerMessage != null)
			playerMessage.preprocess(game);

		if (messageInt != null)
			messageInt.preprocess(game);
		if (messageIntArray != null)
			messageIntArray.preprocess(game);
		if (messageFloat != null)
			messageFloat.preprocess(game);
		if (messageBoolean != null)
			messageBoolean.preprocess(game);
		if (messageRange != null)
			messageRange.preprocess(game);
		if (messageRegion != null)
			messageRegion.preprocess(game);
		if (messageGraph != null)
			messageGraph.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String msg = "No Message";
		if (      message != null) 
			msg = message;
		else if ( messageInt != null) 
			msg = messageInt.toEnglish(game); 
		else if ( messageIntArray != null) 
			msg = messageIntArray.toEnglish(game); 
		else if ( messageFloat != null) 
			msg = messageFloat.toEnglish(game); 
		else if ( messageBoolean != null) 
			msg = messageBoolean.toEnglish(game);
		else if ( messageRegion != null) 
			msg = messageRegion.toEnglish(game);
		else if ( messageRange != null)
			msg = messageRange.toEnglish(game);
		else if ( messageDirection != null)  // For the directions we look the result for the first centre site of the default SiteType on the board.
			msg = messageDirection.toEnglish(game);
		else if ( messageGraph != null) 
			msg = messageGraph.toEnglish(game);
		
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);

		return "send message " + msg + thenString;
	}
	
	//-------------------------------------------------------------------------

}
