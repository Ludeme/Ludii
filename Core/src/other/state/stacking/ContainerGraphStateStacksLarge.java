package other.state.stacking;

import java.util.Arrays;

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
public class ContainerGraphStateStacksLarge extends ContainerStateStacks
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The stacks on the vertices. */
	private final ListStack[] chunkStacksVertex;

	/** The stack on the edges. */
	private final ListStack[] chunkStacksEdge;

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

		chunkStacksVertex = new ListStack[numVertice];
		chunkStacksEdge = new ListStack[numEdges];

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
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 */
	public ContainerGraphStateStacksLarge(final ContainerGraphStateStacksLarge other)
	{
		super(other);

		if (other.chunkStacksVertex == null)
		{
			chunkStacksVertex = null;
		}
		else
		{
			chunkStacksVertex = new ListStack[other.chunkStacksVertex.length];
			
			for(int i = 0 ; i < chunkStacksVertex.length; i++)
				chunkStacksVertex[i] = new ListStack(other.chunkStacksVertex[i]);
		}

		if (other.chunkStacksEdge == null)
		{
			chunkStacksEdge = null;
		}
		else
		{
			chunkStacksEdge = new ListStack[other.chunkStacksEdge.length];
			
			for(int i = 0 ; i < chunkStacksEdge.length; i++)
				chunkStacksEdge[i] = new ListStack(other.chunkStacksEdge[i]);
		}

		hiddenVertexInfo = other.hiddenVertexInfo;
		hiddenEdgeInfo = other.hiddenEdgeInfo;

		this.emptyEdge   = new Region(other.emptyEdge);
		this.emptyVertex = new Region(other.emptyVertex);
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
		Arrays.fill(chunkStacksVertex, null);
		Arrays.fill(chunkStacksEdge, null);

		emptyEdge.set(numEdges);
		emptyVertex.set(numVertices);
	}

	private void verifyPresentVertex(final int site)
	{
		if (chunkStacksVertex[site] != null)
			return;
	}

	private void verifyPresentEdge(final int site)
	{
		if (chunkStacksEdge[site] != null)
			return;
	}

	/**
	 * @param site The vertex to look.
	 * @return True if the vertex is occupied.
	 */
	public boolean isOccupiedVertex(final int site)
	{
		return chunkStacksVertex[site] != null && chunkStacksVertex[site].what() != 0;
	}

	/**
	 * @param site The edge to look.
	 * @return True if the edge is occupied.
	 */
	public boolean isOccupiedEdge(final int site)
	{
		return chunkStacksEdge[site] != null && chunkStacksEdge[site].what() != 0;
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
				chunkStacksVertex[site].setWho(whoVal);
			if (whatVal != Constants.UNDEFINED)
				chunkStacksVertex[site].setWhat(whatVal);
			if (stateVal != Constants.UNDEFINED)
				chunkStacksVertex[site].setState(stateVal);
			if (rotationVal != Constants.UNDEFINED)
				chunkStacksVertex[site].setRotation(rotationVal);
			if (valueVal != Constants.UNDEFINED)
				chunkStacksVertex[site].setValue(valueVal);

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
				chunkStacksEdge[site].setWho(whoVal);
			if (whatVal != Constants.UNDEFINED)
				chunkStacksEdge[site].setWhat(whatVal);
			if (stateVal != Constants.UNDEFINED)
				chunkStacksEdge[site].setState(stateVal);
			if (rotationVal != Constants.UNDEFINED)
				chunkStacksEdge[site].setRotation(rotationVal);
			if (valueVal != Constants.UNDEFINED)
				chunkStacksEdge[site].setValue(valueVal);

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
		chunkStacksVertex[site].incrementSize();
		chunkStacksVertex[site].setWhat(whatValue);
		chunkStacksVertex[site].setWho(whoId);

	}

	@Override
	public void insertVertex(State trialState, int site, int level, int what, int who, final int state,
			final int rotation, final int value, Game game)
	{
		verifyPresentVertex(site);
		final int size = chunkStacksVertex[site - offset].size();
		final boolean wasEmpty = (size == 0);

		if (level == size)
		{
			chunkStacksVertex[site - offset].incrementSize();
			chunkStacksVertex[site - offset].setWhat(what);
			chunkStacksVertex[site - offset].setWho(who);
			chunkStacksVertex[site - offset].setState((state == Constants.UNDEFINED ? 0 : state));
			chunkStacksVertex[site - offset].setRotation((rotation == Constants.UNDEFINED ? 0 : rotation));
			chunkStacksVertex[site - offset].setValue(0, (value == Constants.UNDEFINED ? 0 : value));
		}
		else if (level < size)
		{
			chunkStacksVertex[site - offset].incrementSize();
			for (int i = size - 1; i >= level; i--)
			{
				final int whatLevel = chunkStacksVertex[site - offset].what(i);
				chunkStacksVertex[site - offset].setWhat(whatLevel, i + 1);

				final int whoLevel = chunkStacksVertex[site - offset].who(i);
				chunkStacksVertex[site - offset].setWho(whoLevel, i + 1);

				final int rotationLevel = chunkStacksVertex[site - offset].rotation(i);
				chunkStacksVertex[site - offset].setRotation(rotationLevel, i + 1);

				final int valueLevel = chunkStacksVertex[site - offset].value(i);
				chunkStacksVertex[site - offset].setValue(valueLevel, i + 1);

				final int stateLevel = chunkStacksVertex[site - offset].state(i);
				chunkStacksVertex[site - offset].setState(stateLevel, i + 1);
			}
			chunkStacksVertex[site - offset].setWhat(what, level);
			chunkStacksVertex[site - offset].setWho(who, level);
			chunkStacksVertex[site - offset].setState((state == Constants.UNDEFINED ? 0 : state), level);
			chunkStacksVertex[site - offset].setRotation((rotation == Constants.UNDEFINED ? 0 : rotation),
					level);
			chunkStacksVertex[site - offset].setValue((value == Constants.UNDEFINED ? 0 : value), level);
		}

		final boolean isEmpty = (chunkStacksVertex[site - offset].size() == 0);

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
		chunkStacksVertex[site].incrementSize();
		chunkStacksVertex[site].setWhat(whatValue);
		chunkStacksVertex[site].setWho(whoId);
		chunkStacksVertex[site].setState(stateVal);
		chunkStacksVertex[site].setRotation(rotationVal);
		chunkStacksVertex[site].setValue(valueVal);
	}

	@Override
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, Game game, boolean[] hiddenValues,
			boolean masked)
	{
		verifyPresentVertex(site);
		chunkStacksVertex[site].incrementSize();
		chunkStacksVertex[site].setWhat(whatValue);
		chunkStacksVertex[site].setWho(whoId);
	}

	@Override
	public void removeStackVertex(State trialState, int site)
	{
		if (chunkStacksVertex[site] == null) 
			return;
		
		chunkStacksVertex[site] = null;
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, Game game)
	{
		verifyPresentEdge(site);
		chunkStacksEdge[site].incrementSize();
		chunkStacksEdge[site].setWhat(whatValue);
		chunkStacksEdge[site].setWho(whoId);
	}

	@Override
	public void insertEdge(State trialState, int site, int level, int what, int who, final int state,
			final int rotation, final int value, Game game)
	{
		verifyPresentEdge(site);
		final int size = chunkStacksEdge[site - offset].size();

		final boolean wasEmpty = (size == 0);

		if (level == size)
		{
			chunkStacksEdge[site - offset].incrementSize();
			chunkStacksEdge[site - offset].setWhat(what);
			chunkStacksEdge[site - offset].setWho(who);
			chunkStacksEdge[site - offset].setState((state == Constants.UNDEFINED ? 0 : state));
			chunkStacksEdge[site - offset].setRotation((rotation == Constants.UNDEFINED ? 0 : rotation));
			chunkStacksEdge[site - offset].setValue(0, (value == Constants.UNDEFINED ? 0 : value));
		}
		else if (level < size)
		{
			chunkStacksEdge[site - offset].incrementSize();
			for (int i = size - 1; i >= level; i--)
			{
				final int whatLevel = chunkStacksEdge[site - offset].what(i);
				chunkStacksEdge[site - offset].setWhat(whatLevel, i + 1);

				final int whoLevel = chunkStacksEdge[site - offset].who(i);
				chunkStacksEdge[site - offset].setWho(whoLevel, i + 1);

				final int rotationLevel = chunkStacksEdge[site - offset].rotation(i);
				chunkStacksEdge[site - offset].setRotation(rotationLevel, i + 1);

				final int valueLevel = chunkStacksEdge[site - offset].value(i);
				chunkStacksEdge[site - offset].setValue(valueLevel, i + 1);

				final int stateLevel = chunkStacksEdge[site - offset].state(i);
				chunkStacksEdge[site - offset].setState(stateLevel, i + 1);
			}
			chunkStacksEdge[site - offset].setWhat(what, level);
			chunkStacksEdge[site - offset].setWho(who, level);
			chunkStacksEdge[site - offset].setState((state == Constants.UNDEFINED ? 0 : state), level);
			chunkStacksEdge[site - offset].setRotation((rotation == Constants.UNDEFINED ? 0 : rotation),
					level);
			chunkStacksEdge[site - offset].setValue((value == Constants.UNDEFINED ? 0 : value), level);
		}

		final boolean isEmpty = (chunkStacksEdge[site - offset].size() == 0);

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
		chunkStacksEdge[site].incrementSize();
		chunkStacksEdge[site].setWhat(whatValue);
		chunkStacksEdge[site].setWho(whoId);
		chunkStacksEdge[site].setState(stateVal);
		chunkStacksEdge[site].setRotation(rotationVal);
		chunkStacksEdge[site].setValue(valueVal);
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, Game game, boolean[] hiddenValues,
			boolean masked)
	{
		verifyPresentEdge(site);
		chunkStacksEdge[site].incrementSize();
		chunkStacksEdge[site].setWhat(whatValue);
		chunkStacksEdge[site].setWho(whoId);
	}

	@Override
	public void removeStackEdge(State trialState, int site)
	{
		if (chunkStacksEdge[site] == null) 
			return;
		
		chunkStacksEdge[site] = null;
	}

	@Override
	public int whoVertex(int site)
	{
		if (chunkStacksVertex[site] == null)
			return 0;

		return chunkStacksVertex[site].who();
	}

	@Override
	public int whoVertex(int site, int level)
	{
		if (chunkStacksVertex[site] == null)
			return 0;

		return chunkStacksVertex[site].who(level);
	}

	@Override
	public int whatVertex(int site)
	{
		if (chunkStacksVertex[site] == null)
			return 0;

		return chunkStacksVertex[site].what();
	}

	@Override
	public int whatVertex(int site, int level)
	{
		if (chunkStacksVertex[site] == null)
			return 0;

		return chunkStacksVertex[site].what(level);
	}

	@Override
	public int stateVertex(int site)
	{
		if (chunkStacksVertex[site] == null)
			return 0;

		return chunkStacksVertex[site].state();
	}

	@Override
	public int stateVertex(int site, int level)
	{
		if (chunkStacksVertex[site] == null)
			return 0;

		return chunkStacksVertex[site].state(level);
	}

	@Override
	public int rotationVertex(int site)
	{
		if (chunkStacksVertex[site] == null)
			return 0;

		return chunkStacksVertex[site].rotation();
	}

	@Override
	public int rotationVertex(int site, int level)
	{
		if (chunkStacksVertex[site] == null)
			return 0;

		return chunkStacksVertex[site].rotation(level);
	}

	@Override
	public int valueVertex(int site)
	{
		if (chunkStacksVertex[site] == null)
			return 0;

		return chunkStacksVertex[site].value();
	}

	@Override
	public int valueVertex(int site, int level)
	{
		if (chunkStacksVertex[site] == null)
			return 0;

		return chunkStacksVertex[site].value(level);
	}

	@Override
	public int sizeStackVertex(final int site)
	{
		if (chunkStacksVertex[site] == null)
			return 0;

		return chunkStacksVertex[site].size();
	}

	@Override
	public int whoEdge(int site)
	{
		if (chunkStacksEdge[site] == null)
			return 0;

		return chunkStacksEdge[site].who();
	}

	@Override
	public int whoEdge(int site, int level)
	{
		if (chunkStacksEdge[site] == null)
			return 0;

		return chunkStacksEdge[site].who(level);
	}

	@Override
	public int whatEdge(int site)
	{
		if (chunkStacksEdge[site] == null)
			return 0;

		return chunkStacksEdge[site].what();
	}

	@Override
	public int whatEdge(int site, int level)
	{
		if (chunkStacksEdge[site] == null)
			return 0;

		return chunkStacksEdge[site].what(level);
	}

	@Override
	public int stateEdge(int site)
	{
		if (chunkStacksEdge[site] == null)
			return 0;

		return chunkStacksEdge[site].state();
	}

	@Override
	public int stateEdge(int site, int level)
	{
		if (chunkStacksEdge[site] == null)
			return 0;

		return chunkStacksEdge[site].state(level);
	}

	@Override
	public int rotationEdge(int site)
	{
		if (chunkStacksEdge[site] == null)
			return 0;

		return chunkStacksEdge[site].rotation();
	}

	@Override
	public int rotationEdge(int site, int level)
	{
		if (chunkStacksEdge[site] == null)
			return 0;

		return chunkStacksEdge[site].rotation(level);
	}

	@Override
	public int valueEdge(int site)
	{
		if (chunkStacksEdge[site] == null)
			return 0;

		return chunkStacksEdge[site].value();
	}

	@Override
	public int valueEdge(int site, int level)
	{
		if (chunkStacksEdge[site] == null)
			return 0;

		return chunkStacksEdge[site].value(level);
	}

	@Override
	public int sizeStackEdge(final int site)
	{
		if (chunkStacksEdge[site] == null)
			return 0;

		return chunkStacksEdge[site].size();
	}

	@Override
	public int remove(final State state, final int site, final SiteType graphElement)
	{
		if (graphElement == SiteType.Cell)
			return super.remove(state, site, graphElement);
		else if (graphElement == SiteType.Vertex)
		{
			if (chunkStacksVertex[site] == null)
				return 0;

			final int componentRemove = chunkStacksVertex[site].what();
			chunkStacksVertex[site].setWhat(0);
			chunkStacksVertex[site].setWho(0);
			chunkStacksVertex[site].setState(0);
			chunkStacksVertex[site].setRotation(0);
			chunkStacksVertex[site].setValue(0);
			chunkStacksVertex[site].decrementSize();
			return componentRemove;
		}

		// Edge
		if (chunkStacksEdge[site] == null)
			return 0;

		final int componentRemove = chunkStacksEdge[site].what();
		chunkStacksEdge[site].setWhat(0);
		chunkStacksEdge[site].setWho(0);
		chunkStacksEdge[site].setState(0);
		chunkStacksEdge[site].setRotation(0);
		chunkStacksEdge[site].setValue(0);
		chunkStacksEdge[site].decrementSize();
		return componentRemove;
	}

	@Override
	public int remove(final State state, final int site, final int level, final SiteType graphElement)
	{
		if (graphElement == SiteType.Cell)
			super.remove(state, site, level, graphElement);
		else if (graphElement == SiteType.Vertex)
		{
			if (chunkStacksVertex[site] == null)
				return 0;

			final int componentRemove = chunkStacksVertex[site].what(level);
			for (int i = level; i < sizeStack(site, graphElement) - 1; i++)
			{
				chunkStacksVertex[site].setWhat(chunkStacksVertex[site].what(i + 1), i);
				chunkStacksVertex[site].setWho(chunkStacksVertex[site].who(i + 1), i);
				chunkStacksVertex[site].setState(chunkStacksVertex[site].state(i + 1), i);
				chunkStacksVertex[site].setRotation(chunkStacksVertex[site].rotation(i + 1), i);
				chunkStacksVertex[site].setValue(chunkStacksVertex[site].value(i + 1), i);
			}
			chunkStacksVertex[site].setWhat(0);
			chunkStacksVertex[site].setWho(0);
			chunkStacksVertex[site].decrementSize();
			return componentRemove;
		}

		// Edge
		if (chunkStacksEdge[site] == null)
			return 0;

		final int componentRemove = chunkStacksEdge[site].what(level);
		for (int i = level; i < sizeStack(site, graphElement) - 1; i++)
		{
			chunkStacksEdge[site].setWhat(chunkStacksEdge[site].what(i + 1), i);
			chunkStacksEdge[site].setWho(chunkStacksEdge[site].who(i + 1), i);
			chunkStacksEdge[site].setState(chunkStacksEdge[site].state(i + 1), i);
			chunkStacksEdge[site].setRotation(chunkStacksEdge[site].rotation(i + 1), i);
			chunkStacksEdge[site].setValue(chunkStacksEdge[site].value(i + 1), i);
		}
		chunkStacksEdge[site].setWhat(0);
		chunkStacksEdge[site].setWho(0);
		chunkStacksEdge[site].decrementSize();
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
	public ContainerStateStacks deepClone()
	{
		return new ContainerGraphStateStacksLarge(this);
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

			return chunkStacksEdge[site].isHidden(player, level);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return chunkStacksVertex[site].isHidden(player, level);
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

			return chunkStacksEdge[site].isHiddenWhat(player, level);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return chunkStacksVertex[site].isHiddenWhat(player, level);
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

			return chunkStacksEdge[site].isHiddenWho(player, level);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return chunkStacksVertex[site].isHiddenWho(player, level);
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

			return chunkStacksEdge[site].isHiddenState(player, level);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return chunkStacksVertex[site].isHiddenState(player, level);
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

			return chunkStacksEdge[site].isHiddenRotation(player, level);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return chunkStacksVertex[site].isHiddenRotation(player, level);
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

			return chunkStacksEdge[site].isHiddenValue(player, level);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return chunkStacksVertex[site].isHiddenValue(player, level);
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

			return chunkStacksEdge[site].isHiddenCount(player, level);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return chunkStacksVertex[site].isHiddenCount(player, level);
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

			chunkStacksEdge[site].setHidden(player, level, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			chunkStacksVertex[site].setHidden(player, level, on);
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

			chunkStacksEdge[site].setHiddenWhat(player, level, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			chunkStacksVertex[site].setHiddenWhat(player, level, on);
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

			chunkStacksEdge[site].setHiddenWho(player, level, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			chunkStacksVertex[site].setHiddenWho(player, level, on);
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

			chunkStacksEdge[site].setHiddenState(player, level, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			chunkStacksVertex[site].setHiddenState(player, level, on);
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

			chunkStacksEdge[site].setHiddenRotation(player, level, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			chunkStacksVertex[site].setHiddenRotation(player, level, on);
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

			chunkStacksEdge[site].setHiddenValue(player, level, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			chunkStacksVertex[site].setHiddenValue(player, level, on);
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

			chunkStacksEdge[site].setHiddenCount(player, level, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			chunkStacksVertex[site].setHiddenCount(player, level, on);
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
