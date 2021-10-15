package game.functions.region.sites.hidden;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.types.state.GameType;
import game.util.equipment.Region;
import game.util.moves.Player;
import gnu.trove.list.array.TIntArrayList;
import other.PlayersIndices;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.TopologyElement;

/**
 * Returns all the sites which the rotation is hidden to a player on the board.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SitesHiddenRotation extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The player to set the hidden information. */
	private final IntFunction whoFn;

	/** The RoleType if used */
	private final RoleType roleType;

	//-------------------------------------------------------------------------

	/**
	 * @param type       The graph element type [default of the board].
	 * @param to         The player with these hidden information.
	 * @param To         The roleType with these hidden information.
	 */
	public SitesHiddenRotation
	(
			       @Opt   final SiteType type,
		  @Name @Or       final Player   to, 
		  @Name @Or       final RoleType To
	)
	{
		this.type = type;	
		this.whoFn = (to == null && To == null) ? null : To != null ? RoleType.toIntFunction(To) : to.originalIndex();
		this.roleType = To;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		final int who = whoFn.eval(context);
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final TIntArrayList sites = new TIntArrayList();
		final ContainerState cs = context.containerState(0);
		final List<? extends TopologyElement> elements = context.topology().getGraphElements(realType);

		if (roleType != null && RoleType.manyIds(roleType))
		{
			final TIntArrayList idPlayers = PlayersIndices.getIdRealPlayers(context, roleType);
			for (int i = 0; i < idPlayers.size(); i++)
			{
				final int pid = idPlayers.get(i);
				for (final TopologyElement element : elements)
					if (cs.isHiddenRotation(pid, element.index(), 0, realType))
						sites.add(element.index());
			}
		}
		else
		{
			for (final TopologyElement element : elements)
				if (cs.isHiddenRotation(who, element.index(), 0, realType))
					sites.add(element.index());
		}

		return new Region(sites.toArray());
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
		long flags = GameType.HiddenInfo;
		flags |= SiteType.gameFlags(type);
		flags |= whoFn.gameFlags(game);
		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(SiteType.concepts(type));
		concepts.or(whoFn.concepts(game));
		concepts.set(Concept.HiddenInformation.id(), true);
		concepts.set(Concept.HidePieceRotation.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(whoFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(whoFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= whoFn.missingRequirement(game);
		if (roleType != null)
		{
			if (RoleType.isTeam(roleType) && !game.requiresTeams())
			{
				game.addRequirementToReport(
						"(sites Hidden ...): A roletype corresponding to a team is used but the game has no team: "
								+ roleType + ".");
				missingRequirement = true;
			}

			final int indexRoleType = roleType.owner();
			if (indexRoleType > game.players().count())
			{
				game.addRequirementToReport(
						"The roletype used in the rule (sites Hidden ...) is wrong: " + roleType + ".");
				missingRequirement = true;
			}
		}
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= whoFn.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		whoFn.preprocess(game);
	}
}
