package game.functions.booleans.no.pieces;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.component.Component;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import gnu.trove.list.array.TIntArrayList;
import other.PlayersIndices;
import other.concept.Concept;
import other.context.Context;
import other.location.Location;
import other.state.container.ContainerState;

/**
 * To check if one specific piece type or all are not placed.
 * 
 * @author Eric.Piette
 */
@Hide
public final class NoPieces extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Cell/Edge/Vertex. */
	private final SiteType type;

	/** The index of the player. */
	private final IntFunction whoFn;

	/** The name of the item (Container or Component) to count. */
	private final String name;

	/** The RoleType of the player. */
	private final RoleType role;
	
	/** The region to count the pieces. */
	private final RegionFunction whereFn;

	//-------------------------------------------------------------------------
	
	/**
	 * @param type The graph element type [default SiteType of the board].
	 * @param role The role of the player [All].
	 * @param of   The index of the player.
	 * @param name The name of the piece to count only these pieces.
	 * @param in   The region where to count the pieces.
	 */
	public NoPieces
	(
			@Opt           final SiteType        type,
			@Opt @Or       final RoleType        role,
			@Opt @Or @Name final IntFunction     of,
			@Opt           final String          name,
			@Opt     @Name final RegionFunction  in		
	)
	{
		this.type = type;
		this.role = (role != null) ? role : (of == null ? RoleType.All : null);
		whoFn = (of != null) ? of : RoleType.toIntFunction(this.role);
		this.name = name;
		whereFn = in;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		final int whoId = whoFn.eval(context);
		
		// Get the player condition.
		final TIntArrayList idPlayers = PlayersIndices.getIdPlayers(context, role, whoId);
		
		// Get the region condition.
		final TIntArrayList whereSites = (whereFn != null) ? new TIntArrayList(whereFn.eval(context).sites()) : null;
		
		// Get the component condition.
		TIntArrayList componentIds = null;
		
		if (name != null)
		{
			componentIds = new TIntArrayList();
			for (int compId = 1; compId < context.components().length; compId++)
			{
				final Component component = context.components()[compId];
				if (component.name().contains(name))
					componentIds.add(compId);
			}
		}
		
		for (int index = 0; index < idPlayers.size(); index++)
		{
			final int pid = idPlayers.get(index);
			final BitSet alreadyLooked = new BitSet();
			
			final List<? extends Location>[] positions = context.state().owned().positions(pid);
			for (final List<? extends Location> locs : positions)
			{
				for (final Location loc : locs)
				{
					if (type == null || type != null && type.equals(loc.siteType()))
					{
						final int site = loc.site();
						if (!alreadyLooked.get(site))
						{
							alreadyLooked.set(site);
							
							// Check region condition
							if (whereSites != null && !whereSites.contains(site))
								continue;
			
							SiteType realType = type;
							int cid = 0;
							if (type == null)
							{
								cid = site >= context.containerId().length ? 0 : context.containerId()[site];
								if (cid > 0)
									realType = SiteType.Cell;
								else
									realType = context.board().defaultSite();
							}
								
							final ContainerState cs = context.containerState(cid);
							if (context.game().isStacking())
							{
								for (int level = 0 ; level < cs.sizeStack(site, realType); level++)
								{
									final int who = cs.who(site, level, realType);
										
									if (!idPlayers.contains(who))
										continue;
										
									// Check component condition
									if (componentIds != null)
									{
										final int what = cs.what(site, level, realType);
										if (!componentIds.contains(what))
											continue;
									}
									
									return false;
								}
							}
							else
							{
								final int who = cs.who(site, realType);
									
								if (!idPlayers.contains(who))
									continue;
			
								// Check component condition
								if (componentIds != null)
								{
									final int what = cs.what(site, realType);
									if (!componentIds.contains(what))
										continue;
								}
								return false;
							}
						}
					}
				}
			}
		}

		return true;
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
		long gameFlags = whoFn.gameFlags(game);

		if (whereFn != null)
			gameFlags |= whereFn.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.set(Concept.NoPiece.id(), true);
		concepts.or(whoFn.concepts(game));

		if(role != null)
		{
			if(role.equals(RoleType.Mover))
				concepts.set(Concept.NoPieceMover.id(), true);
			else if(role.equals(RoleType.Next))
				concepts.set(Concept.NoPieceNext.id(), true);
		}
		
		if (whereFn != null)
			concepts.or(whereFn.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(whoFn.writesEvalContextRecursive());

		if (whereFn != null)
			writeEvalContext.or(whereFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(whoFn.readsEvalContextRecursive());

		if (whereFn != null)
			readEvalContext.or(whereFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		missingRequirement |= whoFn.missingRequirement(game);

		if (whereFn != null)
			missingRequirement |= whereFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= whoFn.willCrash(game);

		if (whereFn != null)
			willCrash |= whereFn.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		whoFn.preprocess(game);
		if (whereFn != null)
			whereFn.preprocess(game);
	}	
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		String pieceName = "piece";
		if (name != null)
			pieceName = name;
		
		String who = "";
		if (whoFn != null)
			who = " owned by Player " + whoFn.toEnglish(game);
		else if (role != null)
			who = " owned by " + role.name();
		
		String typeString = "";
		if (type != null)
			typeString = " on the " + type.name().toLowerCase() + "s";
		
		String whereString = "";
		if (whereFn != null)
			whereString = " of " + whereFn.toEnglish(game);
		
		return "there are no " + pieceName + "s" + who + typeString + whereString;
	}
		
		//-------------------------------------------------------------------------
}
