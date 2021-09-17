package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;

import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.component.Component;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.To;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.moves.Piece;
import game.util.moves.Player;
import other.action.BaseAction;
import other.action.move.ActionPromote;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Is used for promotion into another item.
 * 
 * @author Eric.Piette and cambolbro
 */
public final class Promote extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Site to promote. */
	private final IntFunction locationFn;

	/** Owner of the promoted piece. */
	private final IntFunction owner;

	/** Name of the new component. */
	private final String[] itemNames;

	/** Promotion to what piece. */
	private final IntFunction toWhat;
	
	/** Pieces To Promote */
	private final IntFunction[] toWhats;

	/** Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type       The graph element type [default SiteType of the board].
	 * @param locationFn The location of the piece to promote [(to)].
	 * @param what       The data about the promoted pieces.
	 * @param who        Index of the owner of the promoted piece.
	 * @param role       RoleType of the owner of the promoted piece.
	 * @param then       The moves applied after that move is applied.
	 * 
	 * @example (promote (last To) (piece {"Queen" "Knight" "Bishop" "Rook"}) Mover)
	 */
	public Promote
	(
		@Opt     final SiteType    type,
		@Opt     final IntFunction locationFn,
			     final Piece       what,
		@Opt @Or final Player      who,
		@Opt @Or final RoleType    role, 
		@Opt     final Then        then
	)
	{
		super(then);

		int numNonNull = 0;
		if (who != null)
			numNonNull++;
		if (role != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"ForEach(): With ForEachPieceType zero or one who, role parameter must be non-null.");

		this.locationFn = (locationFn == null) ? To.instance() : locationFn;

		if (what.nameComponent() != null)
		{
			itemNames = new String[1];
			itemNames[0] = what.nameComponent();
			toWhat = null;
			toWhats = null;
		}
		else if (what.nameComponents() != null)
		{
			itemNames = what.nameComponents();
			toWhat = null;
			toWhats = null;
		}
		else if (what.components() != null)
		{
			toWhats = what.components();
			toWhat = null;
			itemNames = null;
		}
		else
		{
			itemNames = null;
			toWhat = what.component();
			toWhats = null;
		}

		owner = (who == null && role == null) ? null : role != null ? RoleType.toIntFunction(role) : who.originalIndex();
		this.type = type;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final int location = locationFn.eval(context);

		final Moves moves = new BaseMoves(super.then());

		if (location < 0)
			return moves;

		final String[] evalItemNames;
		
		if (toWhat != null)
		{
			evalItemNames = new String[1];
			final int id = toWhat.eval(context);
			if (id < 1)
				return new BaseMoves(super.then());
			evalItemNames[0] = context.components()[id].name();
		}
		else
		{
			evalItemNames = itemNames;
		}

		final int[] whats;
		if (toWhats != null)
		{
			whats = new int[toWhats.length];
			for (int i = 0; i < toWhats.length; i++)
				whats[i] = toWhats[i].eval(context);
		}
		else
		{
			whats = new int[evalItemNames.length];
			for (int i = 0; i < evalItemNames.length; i++)
			{
				if (owner == null)
				{
					final Component component = context.game().getComponent(evalItemNames[i]);
					if (component == null)
						throw new RuntimeException("Component " + evalItemNames[i] + " is not defined.");

					whats[i] = component.index();
				}
				else
				{
					final int ownerId = owner.eval(context);
					for (int j = 1; j < context.components().length; j++)
					{
						final Component component = context.components()[j];
						if (component.name().contains(evalItemNames[i]) && component.owner() == ownerId)
						{
							whats[i] = component.index();
							break;
						}
					}
				}
			}
		}

		for (final int what : whats)
		{
			final BaseAction actionPromote = new ActionPromote(type, location, what);
			if (isDecision())
				actionPromote.setDecision(true);
			final Move move = new Move(actionPromote);
			move.setFromNonDecision(location);
			move.setToNonDecision(location);
			move.setMover(context.state().mover());
			moves.moves().add(move);
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
	public boolean canMoveTo(final Context context, final int target)
	{
		return locationFn.eval(context) == target;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game) | locationFn.gameFlags(game);
		
		if (toWhat != null)
			gameFlags |= toWhat.gameFlags(game);
		
		if (then() != null)
			gameFlags |= then().gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(super.concepts(game));
		concepts.or(locationFn.concepts(game));
		if (isDecision())
			concepts.set(Concept.PromotionDecision.id(), true);
		else
			concepts.set(Concept.PromotionEffect.id(), true);

		if (toWhat != null)
			concepts.or(toWhat.concepts(game));


		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(locationFn.writesEvalContextRecursive());

		if (toWhat != null)
			writeEvalContext.or(toWhat.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(locationFn.readsEvalContextRecursive());

		if (toWhat != null)
			readEvalContext.or(toWhat.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= locationFn.missingRequirement(game);

		if (toWhat != null)
			missingRequirement |= toWhat.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= locationFn.willCrash(game);

		if (toWhat != null)
			willCrash |= toWhat.willCrash(game);

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
		
		locationFn.preprocess(game);
		
		if (toWhat != null)
			toWhat.preprocess(game);
	}
	
	@Override
	public String toEnglish(final Game game)
	{
		String items="";
		int count=0;
		if(itemNames != null) 
		{
			for (final String item : itemNames) 
			{
				items+=item;
	            count++;
	            
	            if(count == itemNames.length-1)
	            	items+=" or ";
	            else if(count < itemNames.length)
	            	items+=", ";
			}
		}

		String ownerEnglish = "a player";
		if (owner != null)
			ownerEnglish = owner.toEnglish(game);
		
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "a piece of " + ownerEnglish + " " + locationFn.toEnglish(game) + ", this piece can promote into " + items + thenString;
	}
	
}
