package game.functions.region.sites.occupied;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.equipment.component.Component;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.equipment.Region;
import game.util.moves.Player;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.ContainerId;
import other.PlayersIndices;
import other.context.Context;
import other.location.Location;
import other.state.container.ContainerState;

/**
 * Returns sites occupied by a player (or many players) in a container.
 * 
 * @author Eric Piette
 */
@Hide
public final class SitesOccupied extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The owner(s). */
	private final IntFunction who;

	/** The roleType. */
	private final RoleType role;

	/** Which container. */
	private final ContainerId containerId;

	/** Name of the container. */
	private final String containerName;

	/** Name of the container. */
	private final IntFunction containerFn;

	/** To only return that kind of component. */
	private final String[] kindComponents;

	/** To only return that component. */
	private final IntFunction component;
	
	/** To get the owned pieces at the top of the stacks only. */
	private final boolean top;
	
	/** Precomputed list of component indices that match given component name(s) */
	private final TIntArrayList matchingComponentIds;

	//-------------------------------------------------------------------------

	/**
	 * @param by            The index of the owner.
	 * @param By            The roleType of the owner.
	 * @param container     The index of the container.
	 * @param containerName The name of the container.
	 * @param component     The index of the component.
	 * @param Component     The name of the component.
	 * @param components    The names of the component.
	 * @param top           True to look only the top of the stack [True].
	 * @param on            The type of the graph element.
	 */
	public SitesOccupied
	(
			@Or   				final Player      by,
			@Or   				final RoleType    By,
			@Opt @Or 	@Name	final IntFunction container, 
			@Opt @Or    	    final String      containerName,
			@Opt @Or2   @Name   final IntFunction component,
			@Opt @Or2   @Name   final String      Component,
			@Opt @Or2   @Name 	final String[]    components,
			@Opt        @Name 	final Boolean     top,
			@Opt        @Name   final SiteType    on
	)
	{
		who = (by == null) ? RoleType.toIntFunction(By) : by.index();
		containerId = new ContainerId(container, containerName,
				(containerName != null && containerName.contains("Hand")) ? By : null, null, null);
		this.containerName = containerName;
		containerFn = container;
		kindComponents = (components != null) ? components : (Component == null) ? new String[0] : new String[]
		{ Component };
		this.component = component;
		type = on;
		this.top = (top == null) ? true : top.booleanValue();
		role = By;
		
		if (kindComponents.length > 0)
			matchingComponentIds = new TIntArrayList();
		else
			matchingComponentIds = null;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		final TIntArrayList sitesOccupied = new TIntArrayList();

		final int cid = (containerName == null && containerFn == null) ? Constants.UNDEFINED
				: containerId.eval(context);

		if (cid > 0)
			type = SiteType.Cell;
		
		final int whoId = who.eval(context);

		// Code to handle specific components.
		final TIntArrayList idSpecificComponents = getSpecificComponents(context, component, matchingComponentIds, role, whoId);

		// Code to handle specific roleType.
		final TIntArrayList idPlayers = PlayersIndices.getIdPlayers(context, role, whoId);

		// To filter the specific components according to the ids of the players.
		if (idSpecificComponents != null)
		{
			for (int i = idSpecificComponents.size() - 1; i >= 0; i--)
			{
				final int componentId = idSpecificComponents.get(i);
				final Component componentObject = context.components()[componentId];
				if (!idPlayers.contains(componentObject.owner()))
					idSpecificComponents.removeAt(i);
			}
		}

		// No specific components
		if (idSpecificComponents == null)
		{
			for (int i = 0; i < idPlayers.size(); i++)
			{
				final int pid = idPlayers.get(i);

				if (type != null)
				{
					final List<? extends Location>[] positions = context.state().owned().positions(pid);
					for (final List<? extends Location> locs : positions)
						for (final Location loc : locs)
							if (loc.siteType().equals(type))
								sitesOccupied.add(loc.site());
				}
				else
					sitesOccupied.addAll(context.state().owned().sites(pid));
			}
		}
		else // specific components
		{
			for (int i = 0; i < idSpecificComponents.size(); i++)
			{
				final int componentId = idSpecificComponents.get(i);
				final Component componentObject = context.components()[componentId];

				if (type != null)
				{
					final List<? extends Location> positions = context.state().owned()
							.positions(componentObject.owner(), componentId);
					for (final Location loc : positions)
						if (loc.siteType().equals(type))
							sitesOccupied.add(loc.site());
				}
				else
					sitesOccupied.addAll(context.state().owned().sites(componentObject.owner(), componentId));
			}
		}

		// Code to handle specific containers.

		final int sitesFrom = (cid == Constants.UNDEFINED) ? 0 : context.sitesFrom()[cid];
		final int sitesTo = (cid == Constants.UNDEFINED) ? Constants.INFINITY
				: sitesFrom + context.containers()[cid].numSites();

		// Filter the containers.
		if (cid != Constants.UNDEFINED)
			for (int i = sitesOccupied.size() - 1; i >= 0; i--)
			{
				final int site = sitesOccupied.getQuick(i);
				if (site < sitesFrom || site >= sitesTo)
					sitesOccupied.removeAt(i);
			}

		// Specific case for large pieces.
		if (context.game().hasLargePiece() && cid == 0)
		{
			final TIntArrayList sitesToReturn = new TIntArrayList(sitesOccupied);
			final ContainerState cs = context.containerState(0);
			for (int i = 0; i < sitesOccupied.size(); i++)
			{
				final int site = sitesOccupied.get(i);
				final int what = cs.what(site, type);
				if (what != 0)
				{
					final Component piece = context.equipment().components()[what];
					if (piece.isLargePiece())
					{
						final int localState = cs.state(site, type);
						final TIntArrayList locs = piece.locs(context, site, localState, context.topology());
						for (int j = 0; j < locs.size(); j++)
							if (!sitesToReturn.contains(locs.get(j)))
								sitesToReturn.add(locs.get(j));
					}
				}
			}

			return new Region(sitesToReturn.toArray());
		}
		
		// Specific case for stacking.
		if (top && context.game().isStacking())
			// we keep only the owned pieces at the top of each stack
			for (int i = sitesOccupied.size() - 1; i >= 0; i--)
			{
				final int site = sitesOccupied.getQuick(i);
				final int cidSite =  type == SiteType.Cell ? context.containerId()[site] : 0;
				final ContainerState cs = context.containerState(cidSite);
				final int owner = cs.who(site, type);
				if (!idPlayers.contains(owner))
					sitesOccupied.removeAt(i);
			}

		return new Region(sitesOccupied.toArray());

	}

	//-------------------------------------------------------------------------

	/**
	 * @param context           The context.
	 * @param specificComponent The specific component to check.
	 * @param preComputeIds     The pre-computed components Ids (not complete for
	 *                          some roleType).
	 * @param occupiedByRole    The role of the player.
	 * @param occupiedbyId      The specific player in entry.
	 * 
	 * @return The ids of the components to check.
	 */
	public static TIntArrayList getSpecificComponents
	(
		final Context context, 
		final IntFunction specificComponent,
		final TIntArrayList preComputeIds, 
		final RoleType occupiedByRole, 
		final int occupiedbyId
	)
	{
		final TIntArrayList idSpecificComponents = new TIntArrayList();

		if (specificComponent != null)
		{
			idSpecificComponents.add(specificComponent.eval(context));

			return idSpecificComponents;
		}
		else if (preComputeIds != null)
		{
			switch (occupiedByRole)
			{
			case All:
				for (int i = 0; i < preComputeIds.size(); ++i)
				{
					final Component comp = context.components()[preComputeIds.getQuick(i)];
					idSpecificComponents.add(comp.index());
				}
				break;
			case Enemy:
				for (int i = 0; i < preComputeIds.size(); ++i)
				{
					final Component comp = context.components()[preComputeIds.getQuick(i)];
					final int owner = comp.owner();
					if (owner != context.state().mover() && owner != 0 && owner < context.game().players().size())
						idSpecificComponents.add(comp.index());
				}
				break;
			case NonMover:
				for (int i = 0; i < preComputeIds.size(); ++i)
				{
					final Component comp = context.components()[preComputeIds.getQuick(i)];
					final int owner = comp.owner();
					if (owner != context.state().mover())
						idSpecificComponents.add(comp.index());
				}
				break;
			default:
				for (int i = 0; i < preComputeIds.size(); ++i)
				{
					final Component comp = context.components()[preComputeIds.getQuick(i)];
					if (comp.owner() == occupiedbyId)
						idSpecificComponents.add(comp.index());
				}
				break;
			}
			return idSpecificComponents;
		}
		else
			return null;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		// we're returning region containing all owned sites in a specific
		// context, so not static
		return false;
		
//		if (component != null)
//			return component.isStatic();
//		else
//			return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0;
		
		flags |= SiteType.gameFlags(type);
		flags |= who.gameFlags(game);
		
		if (component != null)
			flags |= component.gameFlags(game);
		
		if (containerFn != null)
			flags |= containerFn.gameFlags(game);
		
		return flags;
	}
	
	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= who.missingRequirement(game);

		if (component != null)
			missingRequirement |= component.missingRequirement(game);

		if (containerFn != null)
			missingRequirement |= containerFn.missingRequirement(game);

		if (role != null && !game.requiresTeams())
		{
			if (RoleType.isTeam(role) && !game.requiresTeams())
			{
				game.addRequirementToReport(
						"(sites Occupied ...): A roletype corresponding to a team is used but the game has no team: "
								+ role + ".");
				missingRequirement = true;
			}
			
			final int numPlayers = game.players().count();
			if(numPlayers < role.ordinal())
			{
				game.addRequirementToReport(
						"(sites Occupied ...): A roletype corresponding to a player not existed is used: "
								+ role + ".");
				missingRequirement = true;
			}
		}

		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= who.willCrash(game);

		if (component != null)
			willCrash |= component.willCrash(game);

		if (containerFn != null)
			willCrash |= containerFn.willCrash(game);
		return willCrash;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(who.concepts(game));

		if (component != null)
			concepts.or(component.concepts(game));

		if (containerFn != null)
			concepts.or(containerFn.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(who.writesEvalContextRecursive());

		if (component != null)
			writeEvalContext.or(component.writesEvalContextRecursive());

		if (containerFn != null)
			writeEvalContext.or(containerFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(who.readsEvalContextRecursive());

		if (component != null)
			readEvalContext.or(component.readsEvalContextRecursive());

		if (containerFn != null)
			readEvalContext.or(containerFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);

		who.preprocess(game);
		if (component != null)
			component.preprocess(game);
		if (containerFn != null)
			containerFn.preprocess(game);
		
		if (kindComponents.length != 0)
		{
			for (int indexComponent = 1; indexComponent < game.equipment().components().length; indexComponent++)
			{
				final Component comp = game.equipment().components()[indexComponent];
				for (final String kindComponent : kindComponents)
					if(comp.getNameWithoutNumber()!= null)
						if (comp.getNameWithoutNumber().equals(kindComponent))
							matchingComponentIds.add(comp.index());
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Our who function
	 */
	public IntFunction who()
	{
		return who;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game) 
	{
		final String text = "sites occupied by " + (component == null ? "any component" : component.toEnglish(game)) +
							" owned by " + (role == null ? who : role.toString()) +
							(containerName == null ? "" : " in " + containerName);
		
		return text;
	}
	
	//-------------------------------------------------------------------------

}
