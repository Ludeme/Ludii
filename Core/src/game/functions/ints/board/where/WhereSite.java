package game.functions.ints.board.where;

import java.util.ArrayList;
import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.component.Component;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.types.state.GameType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.context.Context;
import other.state.container.ContainerState;

/**
 * Returns the site of a piece if it is on the board, else OFF (-1).
 * 
 * @author Eric.Piette
 * @remarks The name of the piece can be specific without the number on it
 *          because the owner is also specified in the ludeme.
 */
@Hide
public final class WhereSite extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The name of the piece. */
	private final String namePiece;

	/** The index of the owner. */
	private final IntFunction playerFn;

	/** The index of the piece. */
	private final IntFunction whatFn;

	/** The local state of the piece. */
	private final IntFunction localStateFn;

	/** Cell/Edge/Vertex. */
	private SiteType type;
	
	/** List of components that match namePiece (if it is not null) */
	private final ArrayList<Component> matchingNameComponents;

	//-------------------------------------------------------------------------

	/**
	 * If a piece is on the board, return its site else Off.
	 * 
	 * @param namePiece   The name of the piece (without the number at the end).
	 * @param indexPlayer The index of the owner.
	 * @param role        The roleType of the owner.
	 * @param state       The local state of the piece.
	 * @param type        The graph element type [default SiteType of the board].
	 * 
	 * @example (where "Pawn" Mover)
	 */
	public WhereSite
	(
			           final String      namePiece, 
			 @Or       final IntFunction indexPlayer,
			 @Or       final RoleType    role,
		@Opt     @Name final IntFunction state,
		@Opt           final SiteType    type
	)
	{
		this.namePiece = namePiece;
		int numNonNull = 0;
		if (indexPlayer != null)
			numNonNull++;
		if (role != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");

		if (indexPlayer != null)
			playerFn = indexPlayer;
		else
			playerFn = RoleType.toIntFunction(role);

		this.type = type;
		whatFn = null;
		localStateFn = state;
		
		matchingNameComponents = new ArrayList<Component>();
	}
	
	/**
	 * If a piece is on the board, return its site else -1.
	 * 
	 * @param what The index of the piece.
	 * @param type The graph element type [default SiteType of the board].
	 * 
	 * @example (where (what at:(last To)))
	 */
	public WhereSite
	(
			 final IntFunction what,
		@Opt final SiteType    type
	)
	{
		playerFn = null;
		this.type = type;
		whatFn = what;
		namePiece = null;
		localStateFn = null;
		
		matchingNameComponents = null;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int numSite = context.board().numSites();
		final ContainerState cs = context.containerState(0);
		final int localState = (localStateFn != null) ? localStateFn.eval(context) : Constants.UNDEFINED;

		int what = Constants.OFF;
		if (whatFn != null)
		{
			what = whatFn.eval(context);
			
			if (what <= Constants.NO_PIECE)
				return Constants.OFF;

			if (context.game().isStacking())
			{
				for (int site = 0; site < numSite; site++)
				{
					final int stackSize = cs.sizeStack(site, type);
					for (int level = 0; level < stackSize; level++)
						if (cs.what(site, level, type) == what)
							if (localState == Constants.UNDEFINED || cs.state(site, level, type) == localState)
								return site;
				}
			}
			else
			{
				for (int site = 0; site < numSite; site++)
					if (cs.what(site, type) == what)
						if (localState == Constants.UNDEFINED || cs.state(site, type) == localState)
							return site;
			}
		}
		else
		{
			final int playerId = playerFn.eval(context);
			for (final Component c : matchingNameComponents)
			{
				if (c.owner() == playerId)
				{
					what = c.index();
					break;
				}
			}
			
			if (what <= Constants.OFF)
				return Constants.OFF;
			
			final TIntArrayList sites = context.state().owned().sites(playerId, what);
			
			if (context.game().isStacking())
			{
				for (int i = 0; i < sites.size(); ++i)
				{
					final int site = sites.getQuick(i);
					if (site < numSite)
					{
						final int stackSize = cs.sizeStack(site, type);
						for (int level = 0; level < stackSize; level++)
							if (cs.what(site, level, type) == what)
								if (localState == Constants.UNDEFINED || cs.state(site, level, type) == localState)
									return site;
					}
				}
			}
			else
			{
				for (int i = 0; i < sites.size(); ++i)
				{
					final int site = sites.getQuick(i);
					if (site < numSite && cs.what(site, type) == what)
						if (localState == Constants.UNDEFINED || cs.state(site, type) == localState)
							return site;
				}
			}
		}

		return Constants.OFF;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0L;

		if (playerFn != null)
			gameFlags |= playerFn.gameFlags(game);

		if (whatFn != null)
			gameFlags |= whatFn.gameFlags(game);

		if (localStateFn != null)
			gameFlags |= localStateFn.gameFlags(game) | GameType.SiteState;
		gameFlags |= SiteType.gameFlags(type);
		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();

		concepts.or(SiteType.concepts(type));

		if (playerFn != null)
			concepts.or(playerFn.concepts(game));

		if (whatFn != null)
			concepts.or(whatFn.concepts(game));

		if (localStateFn != null)
			concepts.or(localStateFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();

		if (playerFn != null)
			writeEvalContext.or(playerFn.writesEvalContextRecursive());

		if (whatFn != null)
			writeEvalContext.or(whatFn.writesEvalContextRecursive());

		if (localStateFn != null)
			writeEvalContext.or(localStateFn.writesEvalContextRecursive());

		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();

		if (playerFn != null)
			readEvalContext.or(playerFn.readsEvalContextRecursive());

		if (whatFn != null)
			readEvalContext.or(whatFn.readsEvalContextRecursive());

		if (localStateFn != null)
			readEvalContext.or(localStateFn.readsEvalContextRecursive());

		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (playerFn != null)
			missingRequirement |= playerFn.missingRequirement(game);

		if (whatFn != null)
			missingRequirement |= whatFn.missingRequirement(game);

		if (localStateFn != null)
			missingRequirement |= localStateFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (playerFn != null)
			willCrash |= playerFn.willCrash(game);

		if (whatFn != null)
			willCrash |= whatFn.willCrash(game);

		if (localStateFn != null)
			willCrash |= localStateFn.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		if (playerFn != null)
			playerFn.preprocess(game);
		if (whatFn != null)
			whatFn.preprocess(game);
		if (localStateFn != null)
			localStateFn.preprocess(game);
		
		if (namePiece != null)
		{
			// Precompute list of components for which name matches
			for (final Component c : game.equipment().components())
			{
				if (c != null && c.name().contains(namePiece))
					matchingNameComponents.add(c);
			}
			
			matchingNameComponents.trimToSize();
		}
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		String playerString = "";
		if (playerFn != null)
			playerString = " of " + playerFn.toEnglish(game);
		
		return namePiece + playerString + " is in";
	}
}
