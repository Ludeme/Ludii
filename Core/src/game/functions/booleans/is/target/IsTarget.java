package game.functions.booleans.is.target;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.container.board.Board;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import other.ContainerId;
import other.context.Context;
import other.state.container.ContainerState;
import other.state.stacking.BaseContainerStateStacking;
import other.topology.Cell;
import other.topology.TopologyElement;

/**
 * Returns true when a specific configuration is on the board.
 * 
 * @author Eric.Piette
 * 
 * @remarks Used in the ending condition when the goal of the game is to place
 *          some pieces in a specific configuration.
 */
@Hide
public class IsTarget extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which container. */
	private final ContainerId containerId;

	/** The configuration. */
	private final int[] configuration;
	
	/** The specific sites of the configuration. */
	private final int[] specificSites;
	
	//-------------------------------------------------------------------------

	/**
	 * @param containerIdFn The index of the container [0].
	 * @param containerName The name of the container ["Board"].
	 * @param configuration The configuration defined by the indices of each piece.
	 * @param specificSite  The specific site of the configuration.
	 * @param specificSites The specific sites of the configuration.
	 */
	public IsTarget
	(
		@Opt @Or final IntFunction containerIdFn,
		@Opt @Or final String      containerName,
			     final Integer[]   configuration,
		@Opt @Or final Integer     specificSite,
		@Opt @Or final Integer[]   specificSites
	)
	{
		this.containerId = new ContainerId(containerIdFn, containerName, null, null, null);
		
		this.configuration = new int[configuration.length];
		for (int i = 0; i < configuration.length; ++i)
			this.configuration[i] = configuration[i].intValue();
		
		if (specificSites != null)
		{
			this.specificSites = new int[specificSites.length];
			for (int i = 0; i < specificSites.length; ++i)
				this.specificSites[i] = specificSites[i].intValue();
		}
		else if (specificSite != null)
		{
			this.specificSites = new int[]
			{ specificSite.intValue() };
		}
		else
		{
			this.specificSites = null;
		}
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (context.game().isStacking())
			return evalStack(context);
		
		final int cid = containerId.eval(context);
		final ContainerState state = context.state().containerStates()[cid];
		final SiteType type = context.board().defaultSite();

		if (specificSites == null) 
		{
			final Board board = ((Board) context.containers()[cid]);
			final other.topology.Topology graph = board.topology();
			
			if (graph.cells().size() != configuration.length)
			{
				return false;
			}
			for (final TopologyElement element : graph.getGraphElements(type))
			{
				if (state.what(element.index(), type) != configuration[element.index()])
				{
					return false;
				}
			}
			return true;
		}
		else if (configuration.length == specificSites.length)
		{
			for (int i = 0; i < specificSites.length; i++) 
			{ 
				final int site = specificSites[i];
				if (state.what(site, type) != configuration[i])
				{
					return false;
				}
			}
			return true;
		}
		else 
		{
			return false;
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * To evaluate the configuration of a stack on each site.
	 * @param context
	 * @return
	 */
	private boolean evalStack(Context context) 
	{
		final int cid = containerId.eval(context);
		final Board board = ((Board) context.containers()[cid]);
		final other.topology.Topology graph = board.topology();
		final BaseContainerStateStacking state = (BaseContainerStateStacking) context.state().containerStates()[cid];

		if (specificSites == null)
		{
			for (final Cell v : graph.cells()) 
			{
				if (configuration.length != state.sizeStackCell(v.index()))
				{
					break;
				}
				else
				{	
					int i;
					for (i = 0; i < configuration.length; i++)
						if (state.whatCell(v.index(), i) != configuration[i])
							break;
					if (i == configuration.length)
						return true;
				}
			}
			return false;
		}
		else 
		{
			for (int j = 0; j < specificSites.length; j++)
			{
				final int site = specificSites[j];
				if (configuration.length != state.sizeStackCell(site))
				{
					continue;
				}
				else
				{	
					int i;
					for (i = 0 ; i < configuration.length ; i++)
					{
						if (state.whatCell(site, i) != configuration[i])
						{
							break;
						}
					}
					if (i == configuration.length)
					{
						return true;
					}
				}
			}
			return false;
		}
	}

	@Override
	public String toString()
	{
		String str = "";
		str += "Configuration(" + containerId + ",";
		for (int i = 0; i < configuration.length; ++i)
		{
			str += configuration[i] + "-";
		}
		str += ")";
		return str;
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
		return 0l;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Do nothing
	}
}
