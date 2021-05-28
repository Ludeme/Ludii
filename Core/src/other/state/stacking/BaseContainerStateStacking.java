package other.state.stacking;

import game.Game;
import game.equipment.container.Container;
import game.types.board.SiteType;
import other.state.State;
import other.state.container.BaseContainerState;

/**
 * Global State for a stacking container item.
 * 
 * @author Eric.Piette
 */
public abstract class BaseContainerStateStacking extends BaseContainerState
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * The base container stack for a stacking container.
	 * 
	 * @param game                    The game.
	 * @param container               The container.
	 * @param numSites                The number of sites.
	 */
	public BaseContainerStateStacking(final Game game, final Container container, final int numSites)
	{
		super(game, container, numSites);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other The object to copy.
	 */
	public BaseContainerStateStacking(final BaseContainerStateStacking other)
	{
		super(other);
	}

	@Override
	public int what(final int site, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return whatCell(site);
		else if (graphElementType == SiteType.Edge)
			return whatEdge(site);
		else
			return whatVertex(site);
	}

	@Override
	public int who(final int site, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return whoCell(site);
		else if (graphElementType == SiteType.Edge)
			return whoEdge(site);
		else
			return whoVertex(site);
	}

	@Override
	public int count(final int site, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return countCell(site);
		else if (graphElementType == SiteType.Edge)
			return countEdge(site);
		else
			return countVertex(site);
	}

	@Override
	public int sizeStack(final int site, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return sizeStackCell(site);
		else if (graphElementType == SiteType.Edge)
			return sizeStackEdge(site);
		else
			return sizeStackVertex(site);
	}

	@Override
	public int state(final int site, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return stateCell(site);
		else if (graphElementType == SiteType.Edge)
			return stateEdge(site);
		else
			return stateVertex(site);
	}

	//-----------------METHODS USING LEVEL--------------------------------------

	@Override
	public int what(final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return whatCell(site, level);
		else if (graphElementType == SiteType.Edge)
			return whatEdge(site, level);
		else
			return whatVertex(site, level);
	}

	@Override
	public int who(final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return whoCell(site, level);
		else if (graphElementType == SiteType.Edge)
			return whoEdge(site, level);
		else
			return whoVertex(site, level);
	}

	@Override
	public int state(final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return stateCell(site, level);
		else if (graphElementType == SiteType.Edge)
			return stateEdge(site, level);
		else
			return stateVertex(site, level);
	}

	@Override
	public int rotation(final int site, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return rotationCell(site);
		else if (graphElementType == SiteType.Edge)
			return rotationEdge(site);
		else
			return rotationVertex(site);
	}

	@Override
	public int rotation(final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return rotationCell(site, level);
		else if (graphElementType == SiteType.Edge)
			return rotationEdge(site, level);
		else
			return rotationVertex(site, level);
	}

	@Override
	public boolean isEmpty(final int site, final SiteType type)
	{
		if (type == SiteType.Cell || container().index() != 0 || type == null)
			return isEmptyCell(site);
		else if (type.equals(SiteType.Edge))
			return isEmptyEdge(site);
		else
			return isEmptyVertex(site);
	}

	@Override
	public void addItemGeneric(State trialState, int site, int whatValue, int whoId, Game game,
			final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			addItem(trialState, site, whatValue, whoId, game);
		else if (graphElementType == SiteType.Vertex)
			addItemVertex(trialState, site, whatValue, whoId, game);
		else
			addItemEdge(trialState, site, whatValue, whoId, game);
	}

	@Override
	public void addItemGeneric(final State trialState, final int site, final int what, final int who,
			final int stateVal, final int rotationVal, int valueVal, final Game game, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			addItem(trialState, site, what, who, stateVal, rotationVal, valueVal, game);
		else if (graphElementType == SiteType.Vertex)
			addItemVertex(trialState, site, what, who, stateVal, rotationVal, valueVal, game);
		else
			addItemEdge(trialState, site, what, who, stateVal, rotationVal, valueVal, game);
	}

	@Override
	public void addItemGeneric(State trialState, int site, int whatValue, int whoId, Game game, boolean[] hiddenValues,
			boolean masked, SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			addItem(trialState, site, whatValue, whoId, game, hiddenValues, masked);
		else if (graphElementType == SiteType.Vertex)
			addItemVertex(trialState, site, whatValue, whoId, game, hiddenValues, masked);
		else
			addItemEdge(trialState, site, whatValue, whoId, game, hiddenValues, masked);
	}

	@Override
	public void removeStackGeneric(State trialState, int site, SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			removeStack(trialState, site);
		else if (graphElementType == SiteType.Vertex)
			removeStackVertex(trialState, site);
		else
			removeStackEdge(trialState, site);
	}

	@Override
	public boolean isEmptyVertex(final int vertex)
	{
		return true;
	}

	@Override
	public boolean isEmptyEdge(final int edge)
	{
		return true;
	}

	@Override
	public boolean isEmptyCell(final int site)
	{
		return empty.contains(site - offset);
	}
}
