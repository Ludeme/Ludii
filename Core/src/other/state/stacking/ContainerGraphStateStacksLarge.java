package other.state.stacking;

import game.Game;
import game.equipment.container.Container;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.equipment.Region;
import main.Constants;
import main.collections.ListStack;
import other.state.State;
import other.state.zhash.ZobristHashGenerator;

/**
 * Container state for large stacks on the vertices or on the edges.
 * 
 * @author Eric.Piette
 */
public class ContainerGraphStateStacksLarge extends ContainerStateStacksLarge
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The stacks on the vertices. */
	private ListStack[] listStacksVertex;

	/** The stack on the edges. */
	private ListStack[] listStacksEdge;

	/** Which edge slots are empty. */
	private final Region emptyEdge;

	/** Which vertex slots are empty. */
	private final Region emptyVertex;

	private final boolean hiddenVertexInfo;
	private final boolean hiddenEdgeInfo;

	/**
	 * Constructor.
	 * 
	 * @param generator
	 * @param game
	 * @param container
	 * @param type
	 */
	public ContainerGraphStateStacksLarge(final ZobristHashGenerator generator, final Game game, final Container container,
			final int type)
	{
		super(generator, game, container, type);

		final int numEdges = game.board().topology().edges().size();
		final int numVertice = game.board().topology().vertices().size();

		listStacksVertex = new ListStack[numVertice];
		listStacksEdge = new ListStack[numEdges];

		if ((game.gameFlags() & GameType.HiddenInfo) == 0L)
		{
			hiddenVertexInfo = false;
			hiddenEdgeInfo = false;
		}
		else
		{
			hiddenVertexInfo = true;
			hiddenEdgeInfo = true;
		}

		this.emptyEdge   = new Region(numEdges);
		this.emptyVertex = new Region(numVertice);

		for (int i = 0 ; i < listStacksVertex.length; i++)
			listStacksVertex[i] = new ListStack(numComponents, numPlayers, numStates, numRotation, numValues, type, hiddenVertexInfo);

		for (int i = 0 ; i < listStacksEdge.length; i++)
			listStacksEdge[i] = new ListStack(numComponents, numPlayers, numStates, numRotation, numValues, type, hiddenVertexInfo);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 */
	public ContainerGraphStateStacksLarge(final ContainerGraphStateStacksLarge other)
	{
		super(other);

		if (other.listStacksVertex == null)
		{
			listStacksVertex = null;
		}
		else
		{
			listStacksVertex = new ListStack[other.listStacksVertex.length];
			for(int i = 0 ; i < listStacksVertex.length; i++)
				listStacksVertex[i] = new ListStack(other.listStacksVertex[i]);
		}

		if (other.listStacksEdge == null)
		{
			listStacksEdge = null;
		}
		else
		{
			listStacksEdge = new ListStack[other.listStacksEdge.length];
			
			for(int i = 0 ; i < listStacksEdge.length; i++)
				listStacksEdge[i] = new ListStack(other.listStacksEdge[i]);
		}

		hiddenVertexInfo = other.hiddenVertexInfo;
		hiddenEdgeInfo = other.hiddenEdgeInfo;

		this.emptyEdge   = new Region(other.emptyEdge);
		this.emptyVertex = new Region(other.emptyVertex);
	}
	
	@Override
	public ContainerStateStacksLarge deepClone()
	{
		return new ContainerGraphStateStacksLarge(this);
	}

	//-------------------------------------------------------------------------

	@Override
	protected long calcCanonicalHash(final int[] siteRemap, final int[] edgeRemap, final int[] vertexRemap, final int[] playerRemap, final boolean whoOnly)
	{
		return 0; // To do.
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void reset(final State trialState, final Game game)
	{
		super.reset(trialState, game);
		final int numEdges = game.board().topology().edges().size();
		final int numVertices = game.board().topology().vertices().size();
		
		listStacksVertex = new ListStack[numVertices];
		for (int i = 0 ; i < listStacksVertex.length; i++)
			listStacksVertex[i] = new ListStack(numComponents, numPlayers, numStates, numRotation, numValues, type, hiddenVertexInfo);

		listStacksEdge = new ListStack[numEdges];
		for (int i = 0 ; i < listStacksEdge.length; i++)
			listStacksEdge[i] = new ListStack(numComponents, numPlayers, numStates, numRotation, numValues, type, hiddenVertexInfo);

		emptyEdge.set(numEdges);
		emptyVertex.set(numVertices);
	}

	private void verifyPresentVertex(final int site)
	{
		if (listStacksVertex[site] != null)
			return;
	}

	private void verifyPresentEdge(final int site)
	{
		if (listStacksEdge[site] != null)
			return;
	}

	/**
	 * @param site The vertex to look.
	 * @return True if the vertex is occupied.
	 */
	public boolean isOccupiedVertex(final int site)
	{
		return listStacksVertex[site] != null && listStacksVertex[site].what() != 0;
	}

	/**
	 * @param site The edge to look.
	 * @return True if the edge is occupied.
	 */
	public boolean isOccupiedEdge(final int site)
	{
		return listStacksEdge[site] != null && listStacksEdge[site].what() != 0;
	}

	//-------------------------------------------------------------------------

	@Override
	public void setSite(final State trialState, final int site, final int whoVal, final int whatVal, final int countVal,
			final int stateVal, final int rotationVal, final int valueVal, final SiteType type)
	{
		if (type == SiteType.Cell)
		{
			super.setSite(trialState, site, whoVal, whatVal, countVal, stateVal, rotationVal, valueVal, type);
		}
		else if (type == SiteType.Vertex)
		{
			verifyPresentVertex(site);

			final boolean wasEmpty = isEmpty(site, SiteType.Vertex);

			if (whoVal != Constants.UNDEFINED)
				listStacksVertex[site].setWho(whoVal);
			if (whatVal != Constants.UNDEFINED)
				listStacksVertex[site].setWhat(whatVal);
			if (stateVal != Constants.UNDEFINED)
				listStacksVertex[site].setState(stateVal);
			if (rotationVal != Constants.UNDEFINED)
				listStacksVertex[site].setRotation(rotationVal);
			if (valueVal != Constants.UNDEFINED)
				listStacksVertex[site].setValue(valueVal);

			final boolean isEmpty = isEmpty(site, SiteType.Vertex);
			if (wasEmpty == isEmpty)
				return;

			if (isEmpty)
				addToEmpty(site, type);
			else
				removeFromEmpty(site, type);
		}
		else if (type == SiteType.Edge)
		{
			verifyPresentEdge(site);

			final boolean wasEmpty = isEmpty(site, SiteType.Edge);

			if (whoVal != Constants.UNDEFINED)
				listStacksEdge[site].setWho(whoVal);
			if (whatVal != Constants.UNDEFINED)
				listStacksEdge[site].setWhat(whatVal);
			if (stateVal != Constants.UNDEFINED)
				listStacksEdge[site].setState(stateVal);
			if (rotationVal != Constants.UNDEFINED)
				listStacksEdge[site].setRotation(rotationVal);
			if (valueVal != Constants.UNDEFINED)
				listStacksEdge[site].setValue(valueVal);

			final boolean isEmpty = isEmpty(site, SiteType.Edge);
			if (wasEmpty == isEmpty)
				return;

			if (isEmpty)
				addToEmpty(site, type);
			else
				removeFromEmpty(site, type);
		}
	}

	@Override
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, Game game)
	{
		verifyPresentVertex(site);
		listStacksVertex[site].incrementSize();
		listStacksVertex[site].setWhat(whatValue);
		listStacksVertex[site].setWho(whoId);

	}

	@Override
	public void insertVertex(State trialState, int site, int level, int what, int who, final int state,
			final int rotation, final int value, Game game)
	{
		verifyPresentVertex(site);
		final int size = listStacksVertex[site - offset].size();
		final boolean wasEmpty = (size == 0);

		if (level == size)
		{
			listStacksVertex[site - offset].incrementSize();
			listStacksVertex[site - offset].setWhat(what);
			listStacksVertex[site - offset].setWho(who);
			listStacksVertex[site - offset].setState((state == Constants.UNDEFINED ? 0 : state));
			listStacksVertex[site - offset].setRotation((rotation == Constants.UNDEFINED ? 0 : rotation));
			listStacksVertex[site - offset].setValue(0, (value == Constants.UNDEFINED ? 0 : value));
		}
		else if (level < size)
		{
			listStacksVertex[site - offset].incrementSize();
			for (int i = size - 1; i >= level; i--)
			{
				final int whatLevel = listStacksVertex[site - offset].what(i);
				listStacksVertex[site - offset].setWhat(whatLevel, i + 1);

				final int whoLevel = listStacksVertex[site - offset].who(i);
				listStacksVertex[site - offset].setWho(whoLevel, i + 1);

				final int rotationLevel = listStacksVertex[site - offset].rotation(i);
				listStacksVertex[site - offset].setRotation(rotationLevel, i + 1);

				final int valueLevel = listStacksVertex[site - offset].value(i);
				listStacksVertex[site - offset].setValue(valueLevel, i + 1);

				final int stateLevel = listStacksVertex[site - offset].state(i);
				listStacksVertex[site - offset].setState(stateLevel, i + 1);
			}
			listStacksVertex[site - offset].setWhat(what, level);
			listStacksVertex[site - offset].setWho(who, level);
			listStacksVertex[site - offset].setState((state == Constants.UNDEFINED ? 0 : state), level);
			listStacksVertex[site - offset].setRotation((rotation == Constants.UNDEFINED ? 0 : rotation),
					level);
			listStacksVertex[site - offset].setValue((value == Constants.UNDEFINED ? 0 : value), level);
		}

		final boolean isEmpty = (listStacksVertex[site - offset].size() == 0);

		if (wasEmpty == isEmpty)
			return;

		if (isEmpty)
			addToEmpty(site, SiteType.Vertex);
		else
			removeFromEmpty(site, SiteType.Vertex);
	}

	@Override
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, int stateVal, int rotationVal,
			int valueVal,
			Game game)
	{
		verifyPresentVertex(site);
		listStacksVertex[site].incrementSize();
		listStacksVertex[site].setWhat(whatValue);
		listStacksVertex[site].setWho(whoId);
		listStacksVertex[site].setState(stateVal);
		listStacksVertex[site].setRotation(rotationVal);
		listStacksVertex[site].setValue(valueVal);
	}

	@Override
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, Game game, boolean[] hiddenValues,
			boolean masked)
	{
		verifyPresentVertex(site);
		listStacksVertex[site].incrementSize();
		listStacksVertex[site].setWhat(whatValue);
		listStacksVertex[site].setWho(whoId);
	}

	@Override
	public void removeStackVertex(State trialState, int site)
	{
		if (listStacksVertex[site] == null) 
			return;
		
		listStacksVertex[site] = null;
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, Game game)
	{
		verifyPresentEdge(site);
		listStacksEdge[site].incrementSize();
		listStacksEdge[site].setWhat(whatValue);
		listStacksEdge[site].setWho(whoId);
	}

	@Override
	public void insertEdge(State trialState, int site, int level, int what, int who, final int state,
			final int rotation, final int value, Game game)
	{
		verifyPresentEdge(site);
		final int size = listStacksEdge[site - offset].size();

		final boolean wasEmpty = (size == 0);

		if (level == size)
		{
			listStacksEdge[site - offset].incrementSize();
			listStacksEdge[site - offset].setWhat(what);
			listStacksEdge[site - offset].setWho(who);
			listStacksEdge[site - offset].setState((state == Constants.UNDEFINED ? 0 : state));
			listStacksEdge[site - offset].setRotation((rotation == Constants.UNDEFINED ? 0 : rotation));
			listStacksEdge[site - offset].setValue(0, (value == Constants.UNDEFINED ? 0 : value));
		}
		else if (level < size)
		{
			listStacksEdge[site - offset].incrementSize();
			for (int i = size - 1; i >= level; i--)
			{
				final int whatLevel = listStacksEdge[site - offset].what(i);
				listStacksEdge[site - offset].setWhat(whatLevel, i + 1);

				final int whoLevel = listStacksEdge[site - offset].who(i);
				listStacksEdge[site - offset].setWho(whoLevel, i + 1);

				final int rotationLevel = listStacksEdge[site - offset].rotation(i);
				listStacksEdge[site - offset].setRotation(rotationLevel, i + 1);

				final int valueLevel = listStacksEdge[site - offset].value(i);
				listStacksEdge[site - offset].setValue(valueLevel, i + 1);

				final int stateLevel = listStacksEdge[site - offset].state(i);
				listStacksEdge[site - offset].setState(stateLevel, i + 1);
			}
			listStacksEdge[site - offset].setWhat(what, level);
			listStacksEdge[site - offset].setWho(who, level);
			listStacksEdge[site - offset].setState((state == Constants.UNDEFINED ? 0 : state), level);
			listStacksEdge[site - offset].setRotation((rotation == Constants.UNDEFINED ? 0 : rotation),
					level);
			listStacksEdge[site - offset].setValue((value == Constants.UNDEFINED ? 0 : value), level);
		}

		final boolean isEmpty = (listStacksEdge[site - offset].size() == 0);

		if (wasEmpty == isEmpty)
			return;

		if (isEmpty)
			addToEmpty(site, SiteType.Edge);
		else
			removeFromEmpty(site, SiteType.Edge);
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, int stateVal, int rotationVal,
			int valueVal, Game game)
	{
		verifyPresentEdge(site);
		listStacksEdge[site].incrementSize();
		listStacksEdge[site].setWhat(whatValue);
		listStacksEdge[site].setWho(whoId);
		listStacksEdge[site].setState(stateVal);
		listStacksEdge[site].setRotation(rotationVal);
		listStacksEdge[site].setValue(valueVal);
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, Game game, boolean[] hiddenValues,
			boolean masked)
	{
		verifyPresentEdge(site);
		listStacksEdge[site].incrementSize();
		listStacksEdge[site].setWhat(whatValue);
		listStacksEdge[site].setWho(whoId);
	}

	@Override
	public void removeStackEdge(State trialState, int site)
	{
		if (listStacksEdge[site] == null) 
			return;
		
		listStacksEdge[site] = null;
	}

	@Override
	public int whoVertex(int site)
	{
		if (listStacksVertex[site] == null)
			return 0;

		return listStacksVertex[site].who();
	}

	@Override
	public int whoVertex(int site, int level)
	{
		if (listStacksVertex[site] == null)
			return 0;

		return listStacksVertex[site].who(level);
	}

	@Override
	public int whatVertex(int site)
	{
		if (listStacksVertex[site] == null)
			return 0;

		return listStacksVertex[site].what();
	}

	@Override
	public int whatVertex(int site, int level)
	{
		if (listStacksVertex[site] == null)
			return 0;

		return listStacksVertex[site].what(level);
	}

	@Override
	public int stateVertex(int site)
	{
		if (listStacksVertex[site] == null)
			return 0;

		return listStacksVertex[site].state();
	}

	@Override
	public int stateVertex(int site, int level)
	{
		if (listStacksVertex[site] == null)
			return 0;

		return listStacksVertex[site].state(level);
	}

	@Override
	public int rotationVertex(int site)
	{
		if (listStacksVertex[site] == null)
			return 0;

		return listStacksVertex[site].rotation();
	}

	@Override
	public int rotationVertex(int site, int level)
	{
		if (listStacksVertex[site] == null)
			return 0;

		return listStacksVertex[site].rotation(level);
	}

	@Override
	public int valueVertex(int site)
	{
		if (listStacksVertex[site] == null)
			return 0;

		return listStacksVertex[site].value();
	}

	@Override
	public int valueVertex(int site, int level)
	{
		if (listStacksVertex[site] == null)
			return 0;

		return listStacksVertex[site].value(level);
	}

	@Override
	public int sizeStackVertex(final int site)
	{
		if (listStacksVertex[site] == null)
			return 0;

		return listStacksVertex[site].size();
	}

	@Override
	public int whoEdge(int site)
	{
		if (listStacksEdge[site] == null)
			return 0;

		return listStacksEdge[site].who();
	}

	@Override
	public int whoEdge(int site, int level)
	{
		if (listStacksEdge[site] == null)
			return 0;

		return listStacksEdge[site].who(level);
	}

	@Override
	public int whatEdge(int site)
	{
		if (listStacksEdge[site] == null)
			return 0;

		return listStacksEdge[site].what();
	}

	@Override
	public int whatEdge(int site, int level)
	{
		if (listStacksEdge[site] == null)
			return 0;

		return listStacksEdge[site].what(level);
	}

	@Override
	public int stateEdge(int site)
	{
		if (listStacksEdge[site] == null)
			return 0;

		return listStacksEdge[site].state();
	}

	@Override
	public int stateEdge(int site, int level)
	{
		if (listStacksEdge[site] == null)
			return 0;

		return listStacksEdge[site].state(level);
	}

	@Override
	public int rotationEdge(int site)
	{
		if (listStacksEdge[site] == null)
			return 0;

		return listStacksEdge[site].rotation();
	}

	@Override
	public int rotationEdge(int site, int level)
	{
		if (listStacksEdge[site] == null)
			return 0;

		return listStacksEdge[site].rotation(level);
	}

	@Override
	public int valueEdge(int site)
	{
		if (listStacksEdge[site] == null)
			return 0;

		return listStacksEdge[site].value();
	}

	@Override
	public int valueEdge(int site, int level)
	{
		if (listStacksEdge[site] == null)
			return 0;

		return listStacksEdge[site].value(level);
	}

	@Override
	public int sizeStackEdge(final int site)
	{
		if (listStacksEdge[site] == null)
			return 0;

		return listStacksEdge[site].size();
	}

	@Override
	public int remove(final State state, final int site, final SiteType graphElement)
	{
		if (graphElement == SiteType.Cell)
			return super.remove(state, site, graphElement);
		else if (graphElement == SiteType.Vertex)
		{
			if (listStacksVertex[site] == null)
				return 0;

			final int componentRemove = listStacksVertex[site].what();
			listStacksVertex[site].setWhat(0);
			listStacksVertex[site].setWho(0);
			listStacksVertex[site].setState(0);
			listStacksVertex[site].setRotation(0);
			listStacksVertex[site].setValue(0);
			listStacksVertex[site].decrementSize();
			return componentRemove;
		}

		// Edge
		if (listStacksEdge[site] == null)
			return 0;

		final int componentRemove = listStacksEdge[site].what();
		listStacksEdge[site].setWhat(0);
		listStacksEdge[site].setWho(0);
		listStacksEdge[site].setState(0);
		listStacksEdge[site].setRotation(0);
		listStacksEdge[site].setValue(0);
		listStacksEdge[site].decrementSize();
		return componentRemove;
	}

	@Override
	public int remove(final State state, final int site, final int level, final SiteType graphElement)
	{
		if (graphElement == SiteType.Cell)
			super.remove(state, site, level, graphElement);
		else if (graphElement == SiteType.Vertex)
		{
			if (listStacksVertex[site] == null)
				return 0;

			final int componentRemove = listStacksVertex[site].what(level);
			for (int i = level; i < sizeStack(site, graphElement) - 1; i++)
			{
				listStacksVertex[site].setWhat(listStacksVertex[site].what(i + 1), i);
				listStacksVertex[site].setWho(listStacksVertex[site].who(i + 1), i);
				listStacksVertex[site].setState(listStacksVertex[site].state(i + 1), i);
				listStacksVertex[site].setRotation(listStacksVertex[site].rotation(i + 1), i);
				listStacksVertex[site].setValue(listStacksVertex[site].value(i + 1), i);
			}
			listStacksVertex[site].setWhat(0);
			listStacksVertex[site].setWho(0);
			listStacksVertex[site].decrementSize();
			return componentRemove;
		}

		// Edge
		if (listStacksEdge[site] == null)
			return 0;

		final int componentRemove = listStacksEdge[site].what(level);
		for (int i = level; i < sizeStack(site, graphElement) - 1; i++)
		{
			listStacksEdge[site].setWhat(listStacksEdge[site].what(i + 1), i);
			listStacksEdge[site].setWho(listStacksEdge[site].who(i + 1), i);
			listStacksEdge[site].setState(listStacksEdge[site].state(i + 1), i);
			listStacksEdge[site].setRotation(listStacksEdge[site].rotation(i + 1), i);
			listStacksEdge[site].setValue(listStacksEdge[site].value(i + 1), i);
		}
		listStacksEdge[site].setWhat(0);
		listStacksEdge[site].setWho(0);
		listStacksEdge[site].decrementSize();
		return componentRemove;
	}

	@Override
	public void addToEmpty(final int site, final SiteType graphType)
	{
		if (graphType.equals(SiteType.Cell))
			empty.add(site - offset);
		else if (graphType.equals(SiteType.Edge))
			emptyEdge.add(site);
		else
			emptyVertex.add(site);
	}

	@Override
	public void removeFromEmpty(final int site, final SiteType graphType)
	{
		if (graphType.equals(SiteType.Cell))
			empty.remove(site - offset);
		else if (graphType.equals(SiteType.Edge))
			emptyEdge.remove(site);
		else
			emptyVertex.remove(site);
	}

	@Override
	public boolean isHidden(final int player, final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return super.isHidden(player, site, level, graphElementType);
		else if (graphElementType == SiteType.Edge)
		{
			if (!hiddenEdgeInfo)
				return false;

			return listStacksEdge[site].isHidden(player, level);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return listStacksVertex[site].isHidden(player, level);
		}
	}

	@Override
	public boolean isHiddenWhat(final int player, final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return super.isHiddenWhat(player, site, level, graphElementType);
		else if (graphElementType == SiteType.Edge)
		{
			if (!hiddenEdgeInfo)
				return false;

			return listStacksEdge[site].isHiddenWhat(player, level);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return listStacksVertex[site].isHiddenWhat(player, level);
		}
	}

	@Override
	public boolean isHiddenWho(final int player, final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return super.isHiddenWho(player, site, level, graphElementType);
		else if (graphElementType == SiteType.Edge)
		{
			if (!hiddenEdgeInfo)
				return false;

			return listStacksEdge[site].isHiddenWho(player, level);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return listStacksVertex[site].isHiddenWho(player, level);
		}
	}

	@Override
	public boolean isHiddenState(final int player, final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return super.isHiddenState(player, site, level, graphElementType);
		else if (graphElementType == SiteType.Edge)
		{
			if (!hiddenEdgeInfo)
				return false;

			return listStacksEdge[site].isHiddenState(player, level);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return listStacksVertex[site].isHiddenState(player, level);
		}
	}

	@Override
	public boolean isHiddenRotation(final int player, final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return super.isHiddenRotation(player, site, level, graphElementType);
		else if (graphElementType == SiteType.Edge)
		{
			if (!hiddenEdgeInfo)
				return false;

			return listStacksEdge[site].isHiddenRotation(player, level);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return listStacksVertex[site].isHiddenRotation(player, level);
		}
	}

	@Override
	public boolean isHiddenValue(final int player, final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return super.isHiddenValue(player, site, level, graphElementType);
		else if (graphElementType == SiteType.Edge)
		{
			if (!hiddenEdgeInfo)
				return false;

			return listStacksEdge[site].isHiddenValue(player, level);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return listStacksVertex[site].isHiddenValue(player, level);
		}
	}

	@Override
	public boolean isHiddenCount(final int player, final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return super.isHiddenCount(player, site, level, graphElementType);
		else if (graphElementType == SiteType.Edge)
		{
			if (!hiddenEdgeInfo)
				return false;

			return listStacksEdge[site].isHiddenCount(player, level);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return listStacksVertex[site].isHiddenCount(player, level);
		}
	}

	@Override
	public void setHidden(final State state, final int player, final int site, final int level,
			final SiteType graphElementType, final boolean on)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			super.setHidden(state, player, site, level, graphElementType, on);
		else if (graphElementType == SiteType.Edge)
		{
			if (!hiddenEdgeInfo)
				return;

			listStacksEdge[site].setHidden(player, level, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			listStacksVertex[site].setHidden(player, level, on);
		}
	}

	@Override
	public void setHiddenWhat(final State state, final int player, final int site, final int level,
			final SiteType graphElementType, final boolean on)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			super.setHiddenWhat(state, player, site, level, graphElementType, on);
		else if (graphElementType == SiteType.Edge)
		{
			if (!hiddenEdgeInfo)
				return;

			listStacksEdge[site].setHiddenWhat(player, level, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			listStacksVertex[site].setHiddenWhat(player, level, on);
		}
	}

	@Override
	public void setHiddenWho(final State state, final int player, final int site, final int level,
			final SiteType graphElementType, final boolean on)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			super.setHiddenWho(state, player, site, level, graphElementType, on);
		else if (graphElementType == SiteType.Edge)
		{
			if (!hiddenEdgeInfo)
				return;

			listStacksEdge[site].setHiddenWho(player, level, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			listStacksVertex[site].setHiddenWho(player, level, on);
		}
	}

	@Override
	public void setHiddenState(final State state, final int player, final int site, final int level,
			final SiteType graphElementType, final boolean on)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			super.setHiddenState(state, player, site, level, graphElementType, on);
		else if (graphElementType == SiteType.Edge)
		{
			if (!hiddenEdgeInfo)
				return;

			listStacksEdge[site].setHiddenState(player, level, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			listStacksVertex[site].setHiddenState(player, level, on);
		}
	}

	@Override
	public void setHiddenRotation(final State state, final int player, final int site, final int level,
			final SiteType graphElementType, final boolean on)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			super.setHiddenRotation(state, player, site, level, graphElementType, on);
		else if (graphElementType == SiteType.Edge)
		{
			if (!hiddenEdgeInfo)
				return;

			listStacksEdge[site].setHiddenRotation(player, level, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			listStacksVertex[site].setHiddenRotation(player, level, on);
		}
	}

	@Override
	public void setHiddenValue(final State state, final int player, final int site, final int level,
			final SiteType graphElementType, final boolean on)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			super.setHiddenValue(state, player, site, level, graphElementType, on);
		else if (graphElementType == SiteType.Edge)
		{
			if (!hiddenEdgeInfo)
				return;

			listStacksEdge[site].setHiddenValue(player, level, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			listStacksVertex[site].setHiddenValue(player, level, on);
		}
	}

	@Override
	public void setHiddenCount(final State state, final int player, final int site, final int level,
			final SiteType graphElementType, final boolean on)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			super.setHiddenCount(state, player, site, level, graphElementType, on);
		else if (graphElementType == SiteType.Edge)
		{
			if (!hiddenEdgeInfo)
				return;

			listStacksEdge[site].setHiddenCount(player, level, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			listStacksVertex[site].setHiddenCount(player, level, on);
		}
	}

	@Override
	public Region emptyRegion(final SiteType graphType)
	{
		if (graphType.equals(SiteType.Cell))
			return empty;
		else if (graphType.equals(SiteType.Edge))
			return emptyEdge;
		else
			return emptyVertex;
	}

	@Override
	public int countVertex(int site)
	{
		return whoVertex(site) == 0 ? 0 : 1;
	}

	@Override
	public int countEdge(int site)
	{
		return whoEdge(site) == 0 ? 0 : 1;
	}

	@Override
	public boolean isEmptyVertex(final int vertex)
	{
		return emptyVertex.contains(vertex);
	}

	@Override
	public boolean isEmptyEdge(final int edge)
	{
		return emptyEdge.contains(edge);
	}
}
