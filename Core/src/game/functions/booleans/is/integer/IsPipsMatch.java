package game.functions.booleans.is.integer;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastTo;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Radial;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Topology;

/**
 * Detects whether the pips of a domino match its neighbours.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks Used for domino games to detect if the pips of the dominoes
 *          match on a specific site with its neighbours.
 */
@Hide
public final class IsPipsMatch extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Site to check. */
	private final IntFunction siteFn;

	//-------------------------------------------------------------------------

	/**
	 * To detect if the pips of the dominoes match with his neighbors.
	 * 
	 * @param site The site to check [(lastTo)].
	 */
	public IsPipsMatch
	(
		@Opt final IntFunction site
	)
	{
		this.siteFn = (site == null) ? new LastTo(null) : site;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		final int site = siteFn.eval(context);
		final ContainerState cs = context.containerState(context.containerId()[site]);
		final int what = cs.what(site, SiteType.Cell);
		
		if(what == 0)
			return true;
		
		final Component component = context.components()[what];
		final Topology topology = context.topology();

		if (!component.isDomino())
			return true;

		final int state = cs.state(site, SiteType.Cell);
		final TIntArrayList locs = component.locs(context, site, state, context.topology());
		final TIntArrayList locsAroundOccupied = new TIntArrayList();

		for (int i = 0; i < locs.size(); i++)
		{
			final int loc = locs.getQuick(i);
			final int value = cs.valueCell(loc);
			final other.topology.Cell cell = context.topology().cells().get(loc);

			final List<game.util.graph.Step> steps = topology.trajectories().steps(SiteType.Cell, cell.index(),
					SiteType.Cell, AbsoluteDirection.Orthogonal);

			for (final game.util.graph.Step step : steps)
			{
				final int to = step.to().id();
				if (!locs.contains(to)) // We do not check in the domino itself
				{
					final int valueTo = cs.valueCell(to);
					final boolean empty = cs.isEmpty(to, SiteType.Cell);
					if (!empty && valueTo != value)
						return false;

					if (cs.isOccupied(to))
						locsAroundOccupied.add(to);
				}
			}
		}

		if (context.trial().moveNumber() > 1) // Not for the first domino
		{
			boolean okMatch = false;
			for (int i = 0; i < locsAroundOccupied.size(); i++)
			{
				final other.topology.Cell cellI = context.topology().cells().get(locsAroundOccupied.getQuick(i));
				final List<Radial> radials = topology.trajectories().radials(SiteType.Cell, cellI.index(), AbsoluteDirection.Orthogonal);
				
				final TIntArrayList nbors = new TIntArrayList();
				nbors.add(cellI.index());
				
				for(final Radial radial : radials)
				{
					for (int j = 1; j < radial.steps().length; j++)
					{
						final int to = radial.steps()[j].id();
						if (locsAroundOccupied.contains(to))
						{
							nbors.add(to);
						}
					}
					if (nbors.size() > 2)
						return false;
					if (nbors.size() == 2)
						okMatch = (cs.countCell(nbors.getQuick(0)) == cs.countCell(nbors.getQuick(1)));

				}
			}
			return okMatch;
		}
		else
			return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		final boolean isStatic = siteFn.isStatic();

		return isStatic;
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long stateFlag = siteFn.gameFlags(game) | GameType.Dominoes | GameType.LargePiece;

		return stateFlag;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(siteFn.concepts(game));
		concepts.set(Concept.Domino.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(siteFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(siteFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		siteFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (!game.hasDominoes())
		{
			game.addRequirementToReport(
					"The ludeme (is PipsMatch ...) is used but the equipment has no dominoes.");
			missingRequirement = true;
		}
		missingRequirement |= siteFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= siteFn.willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[MatchPips ");
		sb.append("]");

		return sb.toString();
	}
}
