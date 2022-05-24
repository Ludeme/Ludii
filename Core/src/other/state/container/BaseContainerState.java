package other.state.container;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.Game;
import game.equipment.container.Container;
import game.types.board.SiteType;
import game.util.equipment.Region;
import other.Sites;
import other.state.State;
import other.state.symmetry.SymmetryType;
import other.state.symmetry.SymmetryUtils;
import other.state.symmetry.SymmetryValidator;

/**
 * Global State for a container item.
 *
 * @author cambolbro, mrraow and tahmina(UF)
 */
public abstract class BaseContainerState implements ContainerState
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Reference to corresponding source container. */
	private transient Container container;

	/**
	 * Name of container. Often actually left at null, only need it when reading
	 * item states from files.
	 */
	private transient String nameFromFile = null;
	
	/** Lookup containing all symmetry hashes */
	private final Map<Long, Long> canonicalHashLookup;
	
	/** Which slots are empty. */
	protected final Region empty;
	
	/** Offset for this state's container */
	protected final int offset;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor.
	 * @param game
	 * @param container
	 * @param numSites 
	 */
	public BaseContainerState
	(
		final Game game, 
		final Container container, 
		final int numSites
	)
	{
		this.container = container;
		final int realNumsites = container.index() == 0 ? game.board().topology().cells().size() : numSites;
		this.empty = new Region(realNumsites);
		this.offset = game.equipment().sitesFrom()[container.index()];
		canonicalHashLookup = new HashMap<>();
	}

	/**
	 * Copy constructor.
	 *
	 * @param other
	 */
	public BaseContainerState(final BaseContainerState other)
	{
		container = other.container;
		empty = new Region(other.empty);
		this.offset = other.offset;
		this.canonicalHashLookup = other.canonicalHashLookup;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param trialState The state.
	 * @param other      The containerState to copy.
	 */
	public void deepCopy(final State trialState, final BaseContainerState other)
	{
		this.container = other.container;
		empty.set(other.empty);
	}
	
	/**
	 * Reset this state.
	 */
	@Override
	public void reset(final State trialState, final Game game)
	{
		final int numSites = container.numSites();
		final int realNumsites = container.index() == 0 ? game.board().topology().cells().size() : numSites;
		empty.set(realNumsites);
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
	public boolean isEmpty(final int site, final SiteType type)
	{
		if (type == null || type == SiteType.Cell || container().index() != 0)
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
	public Region emptyRegion(final SiteType type)
	{
		return empty;
	}
	
	@Override
	public void addToEmptyCell(final int site) 
	{
		empty.add(site - offset);
	}

	@Override
	public void removeFromEmptyCell(final int site) 
	{
		empty.remove(site - offset);
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

	@Override
	public void addToEmpty(final int site, final SiteType graphType)
	{
		if (graphType == null || graphType == SiteType.Cell || container().index() != 0)
			addToEmptyCell(site);
		else if (graphType.equals(SiteType.Edge))
			addToEmptyEdge(site);
		else
			addToEmptyVertex(site);
	}

	@Override
	public void removeFromEmpty(final int site, final SiteType graphType)
	{
		if (graphType == null || graphType == SiteType.Cell || container().index() != 0)
			removeFromEmptyCell(site);
		else if (graphType.equals(SiteType.Edge))
			removeFromEmptyEdge(site);
		else
			removeFromEmptyVertex(site);
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
	
	@Override
	public void setPlayable(final State trialState, final int site, final boolean on)
	{
		// Nothing to do.
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
		if (!(obj instanceof BaseContainerState))
			return false;

		final BaseContainerState other = (BaseContainerState) obj;

		if (!empty.equals(other.empty))
			return false;

		return true;
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
	public int value(final int site, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return valueCell(site);
		else if (graphElementType == SiteType.Edge)
			return valueEdge(site);
		else
			return valueVertex(site);
	}

	//-------------------Methods with levels---------------------

	@Override
	public int what(final int site, final int level,
			final SiteType graphElementType)
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
	public int state(final int site, final int level,
			final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return stateCell(site, level);
		else if (graphElementType == SiteType.Edge)
			return stateEdge(site, level);
		else
			return stateVertex(site, level);
	}

	@Override
	public int rotation(final int site, final int level,
			final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return rotationCell(site, level);
		else if (graphElementType == SiteType.Edge)
			return rotationEdge(site, level);
		else
			return rotationVertex(site, level);
	}
	
	@Override
	public int value(final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell || container().index() != 0 || graphElementType == null)
			return valueCell(site, level);
		else if (graphElementType == SiteType.Edge)
			return valueEdge(site, level);
		else
			return valueVertex(site, level);
	}

	@Override
	public void set(final int var, final int value, final SiteType type)
	{
		// Nothing to do.
	}

	@Override
	public boolean bit(final int index, final int value, final SiteType type)
	{
		return true;
	}

	@Override
	public boolean isResolvedEdges(final int site)
	{
		return false;
	}

	@Override
	public boolean isResolvedCell(final int site)
	{
		return false;
	}

	@Override
	public boolean isResolvedVerts(final int site)
	{
		return false;
	}

	@Override
	public boolean isResolved(final int site, final SiteType type)
	{
		return false;
	}
	
	@Override
	public final long canonicalHash(final SymmetryValidator validator, final State gameState, final boolean whoOnly) 
	{
		// Lazy initialisation
		if (container().topology().cellRotationSymmetries()==null)
			Container.createSymmetries(container().topology());

		final List<Long> allHashes = new ArrayList<>();
		
		final int[][] cellRotates = container().topology().cellRotationSymmetries();
		final int[][] cellReflects = container().topology().cellReflectionSymmetries();
		final int[][] edgeRotates = container().topology().edgeRotationSymmetries();
		final int[][] edgeReflects = container().topology().edgeReflectionSymmetries();
		final int[][] vertexRotates = container().topology().vertexRotationSymmetries();
		final int[][] vertexReflects = container().topology().vertexReflectionSymmetries();
		final int[][] playerPermutations = SymmetryUtils.playerPermutations(gameState.numPlayers());
		
		long canonicalHash = Long.MAX_VALUE;

		// Note that the permutations include the identity mapping
		for (int playerIdx = 0; playerIdx < playerPermutations.length; playerIdx++)
		{
			if (!validator.isValid(SymmetryType.SUBSTITUTIONS, playerIdx, playerPermutations.length)) continue;
			
			for (int rotateIdx = 0; rotateIdx < cellRotates.length; rotateIdx++)
			{
				if (!validator.isValid(SymmetryType.ROTATIONS, rotateIdx, cellRotates.length)) continue;

				// Rotate without reflection...
				{
					final long hash = calcCanonicalHash(cellRotates[rotateIdx], edgeRotates[rotateIdx], vertexRotates[rotateIdx], playerPermutations[playerIdx], whoOnly);
					canonicalHash = Math.min(canonicalHash,hash);

					final Long key = Long.valueOf(hash);

					// Try a shortcut on the first pass only; we may already have cached this state
//					if (playerIdx == 0 && rotateIdx==0) 
//					{
//						final Long smallest = canonicalHashLookup.get(key);
//						if (smallest != null) return smallest.longValue();
//					}
					
					allHashes.add(key);
				}
				
				// --- then combination of rotates and reflections
				// Note that the first rotation is always the identity (0 degrees), so no need for reflects without rotates
				for (int reflectIdx = 0; reflectIdx < cellReflects.length; reflectIdx++)
				{
					if (!validator.isValid(SymmetryType.REFLECTIONS, reflectIdx, cellReflects.length)) continue;

					final int[] siteRemap = SymmetryUtils.combine(cellReflects[reflectIdx], cellRotates[rotateIdx]);				
					final int[] edgeRemap = SymmetryUtils.combine(edgeReflects[reflectIdx], edgeRotates[rotateIdx]);
					final int[] vertexRemap = SymmetryUtils.combine(vertexReflects[reflectIdx], vertexRotates[rotateIdx]);

					final long hash = calcCanonicalHash(siteRemap, edgeRemap, vertexRemap, playerPermutations[playerIdx], whoOnly);
					canonicalHash = Math.min(canonicalHash,hash);
					allHashes.add(Long.valueOf(hash));
				}
			}
		}
		
		// Store the hashes, to save time next time...
		final Long smallest = Long.valueOf(canonicalHash);
		for (final Long key : allHashes)
			canonicalHashLookup.put(key, smallest);

		return canonicalHash;
	}

	protected abstract long calcCanonicalHash(int[] siteRemap, int[] edgeRemap, int[] vertexRemap, int[] playerRemap, boolean whoOnly);

	@Override
	public BitSet values(final SiteType type, final int var)
	{
		return new BitSet();
	}
}
