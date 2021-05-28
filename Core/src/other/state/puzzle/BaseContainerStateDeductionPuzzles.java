package other.state.puzzle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import game.Game;
import game.equipment.container.Container;
import game.types.board.SiteType;
import game.util.equipment.Region;
import main.collections.ChunkSet;
import other.Sites;
import other.state.State;
import other.state.container.ContainerState;

/**
 * Global State for a container item.
 *
 * @author cambolbro and mrraow
 */
public abstract class BaseContainerStateDeductionPuzzles implements ContainerState
{
	private static final long serialVersionUID = 1L;

	/** Reference to corresponding source container. */
	private transient Container container;

	/**
	 * Name of container. Often actually left at null, only need it when reading
	 * item states from files.
	 */
	private transient String nameFromFile = null;
	
	/** Offset for this state's container */
	protected final int offset;

	//-------------------------------------------------------------------------

	/** Which slots are empty. */
	private final Region empty;

	/**
	 * Constructor.
	 * @param game
	 * @param container
	 * @param numSites 
	 */
	public BaseContainerStateDeductionPuzzles(final Game game, final Container container, final int numSites)
	{
		this.container = container;
		this.empty = new Region(numSites);
		this.offset = game.equipment().sitesFrom()[container.index()];
	}

	/**
	 * Copy constructor.
	 *
	 * @param other
	 */
	public BaseContainerStateDeductionPuzzles(final BaseContainerStateDeductionPuzzles other)
	{
		container = other.container;
		empty = new Region(other.empty);
		this.offset = other.offset;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Reset this state.
	 */
	@Override
	public void reset (final State trialState, final Game game)
	{
		final int numSites = container.numSites();
		empty.set(numSites);
	}

	//-------------------------------------------------------------------------

	@Override
	public String nameFromFile()
	{
		return nameFromFile;
	}

	@Override
	public Container container()
	{
		return container;
	}

	@Override
	public void setContainer(final Container cont)
	{
		container = cont;
	}

	//-------------------------------------------------------------------------

	@Override
	public Sites emptySites()
	{
		return new Sites(empty.sites());
	}

	@Override
	public int numEmpty()
	{
		return empty.count();
	}

	@Override
	public Region emptyRegion(final SiteType type)
	{
		return empty;
	}

	@Override
	public void addToEmptyCell (final int site) 
	{
		empty.add(site - offset);
	}

	@Override
	public void removeFromEmptyCell (final int site) 
	{
		empty.remove(site - offset);
	}

	//-------------------------------------------------------------------------

	/**
	 * Serializes the ItemState
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(final ObjectOutputStream out) throws IOException
	{
		// Use default writer to write all fields of subclasses, like ChunkSets
		// this will not include our container, because it's transient
		out.defaultWriteObject();

		// now write just the name of the container
		out.writeUTF(container.name());
	}

	/**
	 * Deserializes the ItemState
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException
	{
		// Use default reader to read all fields of subclasses, like ChunkSets
		// This will not include our container, because it's transient
		in.defaultReadObject();

		// now read the name of our container
		nameFromFile = in.readUTF();
	}

	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (empty.hashCode());

		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (!(obj instanceof BaseContainerStateDeductionPuzzles))
			return false;

		final BaseContainerStateDeductionPuzzles other = (BaseContainerStateDeductionPuzzles) obj;

		if (!empty.equals(other.empty))
			return false;

		return true;
	}
	
	@Override
	public int stateCell(final int site)
	{
		return 0;
	}

	@Override
	public int rotationCell(final int site)
	{
		return 0;
	}

	@Override
	public boolean isPlayable(final int site)
	{
		throw new UnsupportedOperationException ("Not supported for puzzles");
	}

	@Override
	public boolean isOccupied(final int site)
	{
		throw new UnsupportedOperationException ("Not supported for puzzles");
	}

	@Override
	public void setSite(State trialState, int site, int who, int what, int count, int state, int rotation,
			final int valueVal,
			final SiteType type)
	{
		throw new UnsupportedOperationException ("Not supported for puzzles");
	}

	//-------------------------------------------------------------------------

	/**
	 * @param var
	 * @return Number of edges for this "edge" on the graph.
	 */
	public abstract int numberEdge(final int var);

	/**
	 * Sets all values for the specified variable to "true".
	 * @param type
	 * @param var
	 * @param numValues
	 */
	public abstract void resetVariable(final SiteType type, final int var, final int numValues);

	/**
	 * @param var
	 * @param type
	 * @return The correct resolved method according to the type.
	 */
	@Override
	public boolean isResolved(final int var, final SiteType type)
	{
		if (type == SiteType.Cell)
			return isResolvedCell(var);
		else if (type == SiteType.Vertex)
			return isResolvedVerts(var);
		else
			return isResolvedEdges(var);
	}

	//----------------------------Vertex--------------------------------------

	@Override
	public boolean bit(final int var, final int value, final SiteType type)
	{
		if (type == SiteType.Cell)
			return bitCell(var, value);
		else if (type == SiteType.Vertex)
			return bitVert(var, value);
		else
			return bitEdge(var, value);
	}

	@Override
	public void set(final int var, final int value, final SiteType type)
	{
		if (type == SiteType.Cell)
			setCell(var, value);
		else if (type == SiteType.Vertex)
			setVert(var, value);
		else
			setEdge(var, value);
	}

	/**
	 *
	 * @param var
	 * @param value
	 * @return if the value for the vert is possible
	 */
	public abstract boolean bitVert(final int var, final int value);

	/**
	 * @param var
	 * @param value
	 * Set a value to the variable for the vert.
	 */
	public abstract void setVert(final int var, final int value);

	/**
	 * @param var
	 * @param value
	 * Toggle a value to the variable for the cell.
	 */
	public abstract void toggleVerts(final int var, final int value);

	/**
	 * @param var
	 * @return the assigned value for vertex
	 */
	@Override
	public abstract int whatVertex(final int var);

	//-------------------Edges----------------------------------------

	/**
	 * @param var
	 * @return the assigned value for edge
	 */
	@Override
	public abstract int whatEdge(final int var);

	/**
	 *
	 * @param var
	 * @param value
	 * @return if the value for the edge is possible
	 */
	public abstract boolean bitEdge(final int var, final int value);

	/**
	 * @param var
	 * @param value
	 * Set a value to the variable for the edge.
	 */
	public abstract void setEdge(final int var, final int value);

	/**
	 * @param var
	 * @param value
	 * Toggle a value to the variable for the edge.
	 */
	public abstract void toggleEdges(final int var, final int value);

	//---------------------Cells-----------------------------------------

	/**
	 *
	 * @param var
	 * @param value
	 * @return if the value for the cell is possible
	 */
	public abstract boolean bitCell(final int var, final int value);

	/**
	 * @param var
	 * @param value Set a value to the variable for the cell.
	 */
	public abstract void setCell(final int var, final int value);

	/**
	 * @param var
	 * @param value Toggle a value to the variable for the cell.
	 */
	public abstract void toggleCells(final int var, final int value);

	//-------------------------------------------------------------------------

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
			return whatEdge(site) == 0 ? 0 : 1;
		else
			return whatVertex(site) == 0 ? 0 : 1;

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
	public int what(final int site, final int level,
			final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return whatCell(site, level);
		else if (graphElementType == SiteType.Edge)
			return whatEdge(site);
		else
			return whatVertex(site);
	}

	@Override
	public int who(final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return whoCell(site, level);
		else if (graphElementType == SiteType.Edge)
			return whoEdge(site);
		else
			return whoVertex(site);
	}

	@Override
	public int state(final int site, final int level,
			final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return stateCell(site, level);
		else if (graphElementType == SiteType.Edge)
			return stateEdge(site);
		else
			return stateVertex(site);
	}

	@Override
	public int rotation(final int site, final int level,
			final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return rotationCell(site, level);
		else if (graphElementType == SiteType.Edge)
			return rotationEdge(site);
		else
			return rotationVertex(site);
	}

	@Override
	public int whatVertex(int site, int level)
	{
		return 0;
	}

	@Override
	public int stateVertex(int site, int level)
	{
		return 0;
	}

	@Override
	public int rotationVertex(int site, int level)
	{
		return 0;
	}

	@Override
	public int whatEdge(int site, int level)
	{
		return 0;
	}

	@Override
	public int stateEdge(int site, int level)
	{
		return 0;
	}

	@Override
	public int rotationEdge(int site, int level)
	{
		return 0;
	}

	@Override
	public boolean isHidden(final int player, final int site, final int level, final SiteType type)
	{
		return false;
	}

	@Override
	public boolean isHiddenWhat(final int player, final int site, final int level, final SiteType type)
	{
		return false;
	}

	@Override
	public boolean isHiddenWho(final int player, final int site, final int level, final SiteType type)
	{
		return false;
	}

	@Override
	public boolean isHiddenState(final int player, final int site, final int level, final SiteType type)
	{
		return false;
	}

	@Override
	public boolean isHiddenRotation(final int player, final int site, final int level, final SiteType type)
	{
		return false;
	}

	@Override
	public boolean isHiddenValue(final int player, final int site, final int level, final SiteType type)
	{
		return false;
	}

	@Override
	public boolean isHiddenCount(final int player, final int site, final int level, final SiteType type)
	{
		return false;
	}

	@Override
	public void setHidden(final State state, final int player, final int site, final int level, final SiteType type,
			final boolean on)
	{
		// Nothing to do.
	}

	@Override
	public void setHiddenWhat(final State state, final int player, final int site, final int level, final SiteType type,
			final boolean on)
	{
		// Nothing to do.
	}

	@Override
	public void setHiddenWho(final State state, final int player, final int site, final int level, final SiteType type,
			final boolean on)
	{
		// Nothing to do.
	}

	@Override
	public void setHiddenState(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		// Nothing to do.
	}

	@Override
	public void setHiddenRotation(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		// Nothing to do.
	}

	@Override
	public void setHiddenValue(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		// Nothing to do.
	}

	@Override
	public void setHiddenCount(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		// Nothing to do.
	}

	@Override
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, Game game)
	{
		// nothing to do.
	}

	@Override
	public void insertVertex(State trialState, int site, int level, int whatValue, int whoId, final int state,
			final int rotation, final int value, Game game)
	{
		// nothing to do.
	}

	@Override
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, int stateVal, int rotationVal,
			int valueVal,
			Game game)
	{
		// nothing to do.
	}

	@Override
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, Game game, boolean[] hiddenValues,
			boolean masked)
	{
		// nothing to do.
	}

	@Override
	public void removeStackVertex(State trialState, int site)
	{
		// nothing to do.
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, Game game)
	{
		// nothing to do.
	}

	@Override
	public void insertEdge(State trialState, int site, int level, int whatValue, int whoId, final int state,
			final int rotation, final int value, Game game)
	{
		// nothing to do.
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, int stateVal, int rotationVal,
			int valueVal,
			Game game)
	{
		// nothing to do.
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, Game game, boolean[] hiddenValues,
			boolean masked)
	{
		// nothing to do.
	}

	@Override
	public void removeStackEdge(State trialState, int site)
	{
		// nothing to do.
	}

	@Override
	public void addItemGeneric(State trialState, int site, int whatValue, int whoId, Game game,
			SiteType graphElementType)
	{
		// Do nothing.
	}

	@Override
	public void addItemGeneric(State trialState, int site, int whatValue, int whoId, int stateVal, int rotationVal,
			int valueVal,
			Game game, SiteType graphElementType)
	{
		// Do nothing.
	}

	@Override
	public void addItemGeneric(State trialState, int site, int whatValue, int whoId, Game game, boolean[] hiddenValues,
			boolean masked, SiteType graphElementType)
	{
		// Do nothing.
	}

	@Override
	public void removeStackGeneric(State trialState, int site, SiteType graphElementType)
	{
		// Do nothing.
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

	@Override
	public void addToEmpty(final int site, final SiteType graphType)
	{
		addToEmptyCell(site);
	}

	@Override
	public void removeFromEmpty(final int site, final SiteType graphType)
	{
		removeFromEmptyCell(site);
	}

	@Override
	public void addToEmptyVertex(final int site)
	{
		// Nothing to do.
	}

	@Override
	public void removeFromEmptyVertex(final int site)
	{
		// Nothing to do.
	}

	@Override
	public void addToEmptyEdge(final int site)
	{
		// Nothing to do.
	}

	@Override
	public void removeFromEmptyEdge(final int site)
	{
		// Nothing to do.
	}

	// -------------------------------------------------------------------------

	@Override
	public int whoEdge(int site, int level)
	{
		return whoEdge(site);
	}

	@Override
	public int whoEdge(int site)
	{
		return (whatEdge(site) != 0) ? 1 : 0;
	}

	@Override
	public int countEdge(int site)
	{
		return (whatEdge(site) != 0) ? 1 : 0;
	}

	@Override
	public int whoVertex(int site, int level)
	{
		return whoVertex(site);
	}

	@Override
	public int whoVertex(int site)
	{
		return (whatVertex(site) != 0) ? 1 : 0;
	}

	@Override
	public int countVertex(int site)
	{
		return (whatVertex(site) != 0) ? 1 : 0;
	}

	@Override
	public int whoCell(int site, int level)
	{
		return whoCell(site);
	}

	@Override
	public int whoCell(final int site)
	{
		return (whatCell(site) != 0) ? 1 : 0;
	}

	@Override
	public int countCell(int site)
	{
		return (whatCell(site) != 0) ? 1 : 0;
	}

	//-------------------------------------------------------------------------
	
	@Override public ChunkSet emptyChunkSetCell() { throw new UnsupportedOperationException("TODO"); }
	@Override public ChunkSet emptyChunkSetVertex() { throw new UnsupportedOperationException("TODO"); }
	@Override public ChunkSet emptyChunkSetEdge() { throw new UnsupportedOperationException("TODO"); }
	@Override public int numChunksWhoVertex() { throw new UnsupportedOperationException("TODO"); }
	@Override public int numChunksWhoCell() { throw new UnsupportedOperationException("TODO"); }
	@Override public int numChunksWhoEdge() { throw new UnsupportedOperationException("TODO"); }
	@Override public int chunkSizeWhoVertex() { throw new UnsupportedOperationException("TODO"); }
	@Override public int chunkSizeWhoCell() { throw new UnsupportedOperationException("TODO"); }
	@Override public int chunkSizeWhoEdge() { throw new UnsupportedOperationException("TODO"); }
	@Override public int numChunksWhatVertex() { throw new UnsupportedOperationException("TODO"); }
	@Override public int numChunksWhatCell() { throw new UnsupportedOperationException("TODO"); }
	@Override public int numChunksWhatEdge() { throw new UnsupportedOperationException("TODO"); }
	@Override public int chunkSizeWhatVertex() { throw new UnsupportedOperationException("TODO"); }
	@Override public int chunkSizeWhatCell() { throw new UnsupportedOperationException("TODO"); }
	@Override public int chunkSizeWhatEdge() { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhoVertex(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhoCell(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhoEdge(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhatVertex(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhatCell(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhatEdge(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhoVertex(final int wordIdx, final long mask, final long matchingWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhoCell(final int wordIdx, final long mask, final long matchingWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhoEdge(final int wordIdx, final long mask, final long matchingWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhatVertex(final int wordIdx, final long mask, final long matchingWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhatCell(final int wordIdx, final long mask, final long matchingWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhatEdge(final int wordIdx, final long mask, final long matchingWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhoVertex(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhoCell(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhoEdge(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhatVertex(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhatCell(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhatEdge(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhoVertex(ChunkSet mask, ChunkSet pattern, int startWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhoCell(ChunkSet mask, ChunkSet pattern, int startWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhoEdge(ChunkSet mask, ChunkSet pattern, int startWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhatVertex(ChunkSet mask, ChunkSet pattern, int startWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhatCell(ChunkSet mask, ChunkSet pattern, int startWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhatEdge(ChunkSet mask, ChunkSet pattern, int startWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public ChunkSet cloneWhoVertex() { throw new UnsupportedOperationException("TODO"); }
	@Override public ChunkSet cloneWhoCell() { throw new UnsupportedOperationException("TODO"); }
	@Override public ChunkSet cloneWhoEdge() { throw new UnsupportedOperationException("TODO"); }
	@Override public ChunkSet cloneWhatVertex() { throw new UnsupportedOperationException("TODO"); }
	@Override public ChunkSet cloneWhatCell() { throw new UnsupportedOperationException("TODO"); }
	@Override public ChunkSet cloneWhatEdge() { throw new UnsupportedOperationException("TODO"); }
}
