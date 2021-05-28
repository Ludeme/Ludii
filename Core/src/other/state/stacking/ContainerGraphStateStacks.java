package other.state.stacking;

import java.util.Arrays;

import game.Game;
import game.equipment.container.Container;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.equipment.Region;
import main.Constants;
import other.state.State;
import other.state.zhash.HashedBitSet;
import other.state.zhash.HashedChunkStack;
import other.state.zhash.ZobristHashGenerator;
import other.state.zhash.ZobristHashUtilities;
import other.topology.Vertex;

/**
 * Container state for stacks in the vertices or in the edges.
 * 
 * @author Eric.Piette
 */
public class ContainerGraphStateStacks extends ContainerStateStacks
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The stacks on the vertices. */
	private final HashedChunkStack[] chunkStacksVertex;

	/** Playable vertices (for boardless games). */
	private final HashedBitSet playableVertex;

	/** The stack on the edges. */
	private final HashedChunkStack[] chunkStacksEdge;

	/** Playable edges (for boardless games). */
	private final HashedBitSet playableEdge;

	/** Which edge slots are empty. */
	private final Region emptyEdge;

	/** Which vertex slots are empty. */
	private final Region emptyVertex;

	private final long[][][] chunkStacksWhatVertexHash;
	private final long[][][] chunkStacksWhoVertexHash;
	private final long[][][] chunkStacksStateVertexHash;
	private final long[][][] chunkStacksRotationVertexHash;
	private final long[][][] chunkStacksValueVertexHash;
	private final long[][] chunkStacksSizeVertexHash;
	private final long[][][] chunkStacksHiddenVertexHash;
	private final long[][][] chunkStacksHiddenWhatVertexHash;
	private final long[][][] chunkStacksHiddenWhoVertexHash;
	private final long[][][] chunkStacksHiddenStateVertexHash;
	private final long[][][] chunkStacksHiddenRotationVertexHash;
	private final long[][][] chunkStacksHiddenValueVertexHash;
	private final long[][][] chunkStacksHiddenCountVertexHash;
	

	private final long[][][] chunkStacksWhatEdgeHash;
	private final long[][][] chunkStacksWhoEdgeHash;
	private final long[][][] chunkStacksStateEdgeHash;
	private final long[][][] chunkStacksRotationEdgeHash;
	private final long[][][] chunkStacksValueEdgeHash;
	private final long[][] chunkStacksSizeEdgeHash;
	private final long[][][] chunkStacksHiddenEdgeHash;
	private final long[][][] chunkStacksHiddenWhatEdgeHash;
	private final long[][][] chunkStacksHiddenWhoEdgeHash;
	private final long[][][] chunkStacksHiddenStateEdgeHash;
	private final long[][][] chunkStacksHiddenRotationEdgeHash;
	private final long[][][] chunkStacksHiddenValueEdgeHash;
	private final long[][][] chunkStacksHiddenCountEdgeHash;

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
	public ContainerGraphStateStacks(final ZobristHashGenerator generator, final Game game, final Container container,
			final int type)
	{
		super(generator, game, container, type);

		final int numEdges = game.board().topology().edges().size();
		final int numVertice = game.board().topology().vertices().size();

		chunkStacksVertex = new HashedChunkStack[numVertice];
		chunkStacksEdge = new HashedChunkStack[numEdges];

		final int maxValWhat = numComponents;
		final int maxValWho = numPlayers + 1;
		final int maxValState = numStates;
		final int maxValRotation = numRotation;
		final int maxPieceValue = Constants.MAX_VALUE_PIECE;

		chunkStacksWhatVertexHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT,
				maxValWhat + 1);
		chunkStacksWhoVertexHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT,
				maxValWho + 1);
		chunkStacksStateVertexHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT,
				maxValState + 1);
		chunkStacksRotationVertexHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT, maxValRotation + 1);
		chunkStacksValueVertexHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT, maxPieceValue + 1);
		chunkStacksSizeVertexHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT);

		chunkStacksWhatEdgeHash = ZobristHashUtilities.getSequence(generator, numEdges, Constants.MAX_STACK_HEIGHT,
				maxValWhat + 1);
		chunkStacksWhoEdgeHash = ZobristHashUtilities.getSequence(generator, numEdges, Constants.MAX_STACK_HEIGHT,
				maxValWho + 1);
		chunkStacksStateEdgeHash = ZobristHashUtilities.getSequence(generator, numEdges, Constants.MAX_STACK_HEIGHT,
				maxValState + 1);
		chunkStacksRotationEdgeHash = ZobristHashUtilities.getSequence(generator, numEdges, Constants.MAX_STACK_HEIGHT, maxValRotation + 1);
		chunkStacksValueEdgeHash = ZobristHashUtilities.getSequence(generator, numEdges, Constants.MAX_STACK_HEIGHT, maxPieceValue + 1);
		chunkStacksSizeEdgeHash = ZobristHashUtilities.getSequence(generator, numEdges, Constants.MAX_STACK_HEIGHT);

		if ((game.gameFlags() & GameType.HiddenInfo) == 0L)
		{
			chunkStacksHiddenVertexHash = null;
			chunkStacksHiddenWhatVertexHash = null;
			chunkStacksHiddenWhoVertexHash = null;
			chunkStacksHiddenStateVertexHash = null;
			chunkStacksHiddenRotationVertexHash = null;
			chunkStacksHiddenValueVertexHash = null;
			chunkStacksHiddenCountVertexHash = null;
			hiddenVertexInfo = false;
			chunkStacksHiddenEdgeHash = null;
			chunkStacksHiddenWhatEdgeHash = null;
			chunkStacksHiddenWhoEdgeHash = null;
			chunkStacksHiddenStateEdgeHash = null;
			chunkStacksHiddenRotationEdgeHash = null;
			chunkStacksHiddenValueEdgeHash = null;
			chunkStacksHiddenCountEdgeHash = null;
			hiddenEdgeInfo = false;
		}
		else
		{
			chunkStacksHiddenVertexHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT, 2);
			chunkStacksHiddenWhatVertexHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT, 2);
			chunkStacksHiddenWhoVertexHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT, 2);
			chunkStacksHiddenStateVertexHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT, 2);
			chunkStacksHiddenRotationVertexHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT, 2);
			chunkStacksHiddenValueVertexHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT, 2);
			chunkStacksHiddenCountVertexHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT, 2);
			hiddenVertexInfo = true;
			chunkStacksHiddenEdgeHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT, 2);
			chunkStacksHiddenWhatEdgeHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT, 2);
			chunkStacksHiddenWhoEdgeHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT, 2);
			chunkStacksHiddenStateEdgeHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT, 2);
			chunkStacksHiddenRotationEdgeHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT, 2);
			chunkStacksHiddenValueEdgeHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT, 2);
			chunkStacksHiddenCountEdgeHash = ZobristHashUtilities.getSequence(generator, numVertice, Constants.MAX_STACK_HEIGHT, 2);
			hiddenEdgeInfo = true;
		}

		if (!game.isBoardless())
		{
			playableVertex = null;
			playableEdge = null;
		}
		else
		{
			playableVertex = new HashedBitSet(generator, numVertice);
			playableEdge   = new HashedBitSet(generator, numEdges);
		}

		this.emptyEdge   = new Region(numEdges);
		this.emptyVertex = new Region(numVertice);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 */
	public ContainerGraphStateStacks(final ContainerGraphStateStacks other)
	{
		super(other);

		playableVertex = (other.playableVertex == null) ? null : other.playableVertex.clone();
		playableEdge = (other.playableEdge == null) ? null : other.playableEdge.clone();

		if (other.chunkStacksVertex == null)
		{
			chunkStacksVertex = null;
		}
		else
		{
			chunkStacksVertex = new HashedChunkStack[other.chunkStacksVertex.length];

			for (int i = 0; i < other.chunkStacksVertex.length; ++i)
			{
				final HashedChunkStack otherChunkStack = other.chunkStacksVertex[i];

				if (otherChunkStack != null)
				{
					chunkStacksVertex[i] = otherChunkStack.clone();
				}
			}
		}

		if (other.chunkStacksEdge == null)
		{
			chunkStacksEdge = null;
		}
		else
		{
			chunkStacksEdge = new HashedChunkStack[other.chunkStacksEdge.length];

			for (int i = 0; i < other.chunkStacksEdge.length; ++i)
			{
				final HashedChunkStack otherChunkStack = other.chunkStacksEdge[i];

				if (otherChunkStack != null)
				{
					chunkStacksEdge[i] = otherChunkStack.clone();
				}
			}
		}

		chunkStacksWhatVertexHash = other.chunkStacksWhatVertexHash;
		chunkStacksWhoVertexHash = other.chunkStacksWhoVertexHash;
		chunkStacksStateVertexHash = other.chunkStacksStateVertexHash;
		chunkStacksRotationVertexHash = other.chunkStacksRotationVertexHash;
		chunkStacksValueVertexHash = other.chunkStacksValueVertexHash;
		chunkStacksSizeVertexHash = other.chunkStacksSizeVertexHash;
		hiddenVertexInfo = other.hiddenVertexInfo;

		chunkStacksWhatEdgeHash = other.chunkStacksWhatEdgeHash;
		chunkStacksWhoEdgeHash = other.chunkStacksWhoEdgeHash;
		chunkStacksStateEdgeHash = other.chunkStacksStateEdgeHash;
		chunkStacksRotationEdgeHash = other.chunkStacksRotationEdgeHash;
		chunkStacksValueEdgeHash = other.chunkStacksValueEdgeHash;
		chunkStacksSizeEdgeHash = other.chunkStacksSizeEdgeHash;
		hiddenEdgeInfo = other.hiddenEdgeInfo;

		if (other.chunkStacksHiddenVertexHash == null)
		{
			chunkStacksHiddenVertexHash = null;
			chunkStacksHiddenWhatVertexHash = null;
			chunkStacksHiddenWhoVertexHash = null;
			chunkStacksHiddenStateVertexHash = null;
			chunkStacksHiddenRotationVertexHash = null;
			chunkStacksHiddenValueVertexHash = null;
			chunkStacksHiddenCountVertexHash = null;
		}
		else
		{
			chunkStacksHiddenVertexHash = other.chunkStacksHiddenVertexHash;
			chunkStacksHiddenWhatVertexHash = other.chunkStacksHiddenWhatVertexHash;
			chunkStacksHiddenWhoVertexHash = other.chunkStacksHiddenWhoVertexHash;
			chunkStacksHiddenStateVertexHash = other.chunkStacksHiddenStateVertexHash;
			chunkStacksHiddenRotationVertexHash = other.chunkStacksHiddenRotationVertexHash;
			chunkStacksHiddenValueVertexHash = other.chunkStacksHiddenValueVertexHash;
			chunkStacksHiddenCountVertexHash = other.chunkStacksHiddenCountVertexHash;
		}

		if (other.chunkStacksHiddenEdgeHash == null)
		{
			chunkStacksHiddenEdgeHash = null;
			chunkStacksHiddenWhatEdgeHash = null;
			chunkStacksHiddenWhoEdgeHash = null;
			chunkStacksHiddenStateEdgeHash = null;
			chunkStacksHiddenRotationEdgeHash = null;
			chunkStacksHiddenValueEdgeHash = null;
			chunkStacksHiddenCountEdgeHash = null;
		}
		else
		{
			chunkStacksHiddenEdgeHash = other.chunkStacksHiddenEdgeHash;
			chunkStacksHiddenWhatEdgeHash = other.chunkStacksHiddenWhatEdgeHash;
			chunkStacksHiddenWhoEdgeHash = other.chunkStacksHiddenWhoEdgeHash;
			chunkStacksHiddenStateEdgeHash = other.chunkStacksHiddenStateEdgeHash;
			chunkStacksHiddenRotationEdgeHash = other.chunkStacksHiddenRotationEdgeHash;
			chunkStacksHiddenValueEdgeHash = other.chunkStacksHiddenValueEdgeHash;
			chunkStacksHiddenCountEdgeHash = other.chunkStacksHiddenCountEdgeHash;
		}

		this.emptyEdge   = new Region(other.emptyEdge);
		this.emptyVertex = new Region(other.emptyVertex);
	}

	//-------------------------------------------------------------------------

	@Override
	protected long calcCanonicalHash(final int[] siteRemap, final int[] edgeRemap, final int[] vertexRemap,
			final int[] playerRemap, final boolean whoOnly)
	{
		if (offset != 0)
			return 0; // Not the board!

		long hash = super.calcCanonicalHash(siteRemap, edgeRemap, vertexRemap, playerRemap, whoOnly);

		hash ^= calcCanonicalHashOverSites(chunkStacksVertex, siteRemap, chunkStacksWhatVertexHash,
				chunkStacksWhoVertexHash, chunkStacksStateVertexHash, chunkStacksRotationVertexHash,
				chunkStacksValueVertexHash, chunkStacksSizeVertexHash, whoOnly);

		hash ^= calcCanonicalHashOverSites(chunkStacksEdge, siteRemap, chunkStacksWhatEdgeHash, chunkStacksWhoEdgeHash,
				chunkStacksStateEdgeHash, chunkStacksRotationEdgeHash, chunkStacksValueEdgeHash,
				chunkStacksSizeEdgeHash, whoOnly);

		return hash;
	}

	private static long calcCanonicalHashOverSites(final HashedChunkStack[] stack, final int[] siteRemap,
			final long[][][] whatHash, final long[][][] whoHash, final long[][][] stateHash,
			final long[][][] rotationHash, final long[][][] valueHash, final long[][] sizeHash, final boolean whoOnly)
	{
		long hash = 0;

		for (int pos = 0; pos < stack.length && pos < siteRemap.length; pos++)
		{
			final int newPos = siteRemap[pos];
			if (stack[pos] == null)
				continue;

			hash ^= stack[pos].remapHashTo(whatHash[newPos], whoHash[newPos], stateHash[newPos], rotationHash[newPos],
					valueHash[newPos],
					sizeHash[newPos], whoOnly);
		}

		return hash;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void reset(final State trialState, final Game game)
	{
		super.reset(trialState, game);
		final int numEdges = (game.board().defaultSite() == SiteType.Cell) ? game.board().topology().edges().size()
				: game.board().topology().edges().size();
		final int numVertices = (game.board().defaultSite() == SiteType.Cell)
				? game.board().topology().vertices().size()
				: game.board().topology().vertices().size();
		for (final HashedChunkStack set : chunkStacksVertex)
		{
			if (set == null)
				continue;
			trialState.updateStateHash(set.calcHash());
		}
		Arrays.fill(chunkStacksVertex, null);

		super.reset(trialState, game);
		for (final HashedChunkStack set : chunkStacksEdge)
		{
			if (set == null)
				continue;
			trialState.updateStateHash(set.calcHash());
		}
		Arrays.fill(chunkStacksEdge, null);

		emptyEdge.set(numEdges);
		emptyVertex.set(numVertices);
	}

	private void verifyPresentVertex(final int site)
	{
		if (chunkStacksVertex[site] != null)
			return;

		chunkStacksVertex[site] = new HashedChunkStack(numComponents, numPlayers, numStates, numRotation, numValues,
				type,
				hiddenVertexInfo, chunkStacksWhatVertexHash[site], chunkStacksWhoVertexHash[site],
				chunkStacksStateVertexHash[site], chunkStacksRotationVertexHash[site], chunkStacksValueVertexHash[site],
				chunkStacksSizeVertexHash[site]);
	}

	private void verifyPresentEdge(final int site)
	{
		if (chunkStacksEdge[site] != null)
			return;

		chunkStacksEdge[site] = new HashedChunkStack(numComponents, numPlayers, numStates, numRotation, numValues, type,
				hiddenEdgeInfo, chunkStacksWhatEdgeHash[site], chunkStacksWhoEdgeHash[site],
				chunkStacksStateEdgeHash[site], chunkStacksRotationEdgeHash[site], chunkStacksValueEdgeHash[site],
				chunkStacksSizeEdgeHash[site]);
	}

	private void checkPlayableVertex(final State trialState, final int site)
	{
		if (isOccupiedVertex(site))
		{
			setPlayableVertex(trialState, site, false);
			return;
		}

		final Vertex v = container().topology().vertices().get(site);
		for (final Vertex vNbors : v.adjacent())
			if (isOccupiedVertex(vNbors.index()))
			{
				setPlayableVertex(trialState, site, true);
				return;
			}

		setPlayable(trialState, site, false);
	}

	/**
	 * Make a vertex playable.
	 * 
	 * @param trialState The state of the game.
	 * @param site       The vertex.
	 * @param on         The value to set.
	 */
	public void setPlayableVertex(final State trialState, final int site, final boolean on)
	{
		playableVertex.set(trialState, site, on);
	}

	/**
	 * @param site The vertex to look.
	 * @return True if the vertex is occupied.
	 */
	public boolean isOccupiedVertex(final int site)
	{
		return chunkStacksVertex[site] != null && chunkStacksVertex[site].what() != 0;
	}

	private void checkPlayableEdge(final State trialState, final int site)
	{
		if (isOccupiedEdge(site))
		{
			setPlayableEdge(trialState, site, false);
			return;
		}

		setPlayable(trialState, site, false);
	}

	/**
	 * Make an edge playable.
	 * 
	 * @param trialState The state of the game.
	 * @param site       The edge.
	 * @param on         The value to set.
	 */
	public void setPlayableEdge(final State trialState, final int site, final boolean on)
	{
		playableEdge.set(trialState, site, on);
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
				chunkStacksVertex[site].setWho(trialState, whoVal);
			if (whatVal != Constants.UNDEFINED)
				chunkStacksVertex[site].setWhat(trialState, whatVal);
			if (stateVal != Constants.UNDEFINED)
				chunkStacksVertex[site].setState(trialState, stateVal);
			if (rotationVal != Constants.UNDEFINED)
				chunkStacksVertex[site].setRotation(trialState, rotationVal);
			if (valueVal != Constants.UNDEFINED)
				chunkStacksVertex[site].setValue(trialState, valueVal);

			final boolean isEmpty = isEmpty(site, SiteType.Vertex);
			if (wasEmpty == isEmpty)
				return;

			if (isEmpty)
			{
				addToEmpty(site, type);

				if (playableVertex != null)
				{
					checkPlayableVertex(trialState, site - offset);

					final Vertex v = container().topology().vertices().get(site);
					for (final Vertex vNbors : v.adjacent())
						checkPlayableVertex(trialState, vNbors.index());
				}

			}
			else
			{
				removeFromEmpty(site, type);

				if (playableVertex != null)
				{
					setPlayableVertex(trialState, site, false);

					final Vertex v = container().topology().vertices().get(site);
					for (final Vertex vNbors : v.adjacent())
						if (!isOccupiedVertex(vNbors.index()))
							setPlayableVertex(trialState, vNbors.index(), true);
				}
			}
		}
		else if (type == SiteType.Edge)
		{
			verifyPresentEdge(site);

			final boolean wasEmpty = isEmpty(site, SiteType.Edge);

			if (whoVal != Constants.UNDEFINED)
				chunkStacksEdge[site].setWho(trialState, whoVal);
			if (whatVal != Constants.UNDEFINED)
				chunkStacksEdge[site].setWhat(trialState, whatVal);
			if (stateVal != Constants.UNDEFINED)
				chunkStacksEdge[site].setState(trialState, stateVal);
			if (rotationVal != Constants.UNDEFINED)
				chunkStacksEdge[site].setRotation(trialState, rotationVal);
			if (valueVal != Constants.UNDEFINED)
				chunkStacksEdge[site].setValue(trialState, valueVal);

			final boolean isEmpty = isEmpty(site, SiteType.Edge);
			if (wasEmpty == isEmpty)
				return;

			if (isEmpty)
			{
				addToEmpty(site, type);

				if (playableEdge != null)
				{
					checkPlayableEdge(trialState, site - offset);
				}

			}
			else
			{
				removeFromEmpty(site, type);

				if (playableEdge != null)
				{
					setPlayableEdge(trialState, site, false);
				}
			}
		}
	}

	@Override
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, Game game)
	{
		verifyPresentVertex(site);
		chunkStacksVertex[site].incrementSize(trialState);
		chunkStacksVertex[site].setWhat(trialState, whatValue);
		chunkStacksVertex[site].setWho(trialState, whoId);

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
			chunkStacksVertex[site - offset].incrementSize(trialState);
			chunkStacksVertex[site - offset].setWhat(trialState, what);
			chunkStacksVertex[site - offset].setWho(trialState, who);
			chunkStacksVertex[site - offset].setState(trialState, (state == Constants.UNDEFINED ? 0 : state));
			chunkStacksVertex[site - offset].setRotation(trialState, (rotation == Constants.UNDEFINED ? 0 : rotation));
			chunkStacksVertex[site - offset].setValue(trialState, 0, (value == Constants.UNDEFINED ? 0 : value));
		}
		else if (level < size)
		{
			chunkStacksVertex[site - offset].incrementSize(trialState);
			for (int i = size - 1; i >= level; i--)
			{
				final int whatLevel = chunkStacksVertex[site - offset].what(i);
				chunkStacksVertex[site - offset].setWhat(trialState, whatLevel, i + 1);

				final int whoLevel = chunkStacksVertex[site - offset].who(i);
				chunkStacksVertex[site - offset].setWho(trialState, whoLevel, i + 1);

				final int rotationLevel = chunkStacksVertex[site - offset].rotation(i);
				chunkStacksVertex[site - offset].setRotation(trialState, rotationLevel, i + 1);

				final int valueLevel = chunkStacksVertex[site - offset].value(i);
				chunkStacksVertex[site - offset].setValue(trialState, valueLevel, i + 1);

				final int stateLevel = chunkStacksVertex[site - offset].state(i);
				chunkStacksVertex[site - offset].setState(trialState, stateLevel, i + 1);
			}
			chunkStacksVertex[site - offset].setWhat(trialState, what, level);
			chunkStacksVertex[site - offset].setWho(trialState, who, level);
			chunkStacksVertex[site - offset].setState(trialState, (state == Constants.UNDEFINED ? 0 : state), level);
			chunkStacksVertex[site - offset].setRotation(trialState, (rotation == Constants.UNDEFINED ? 0 : rotation),
					level);
			chunkStacksVertex[site - offset].setValue(trialState, (value == Constants.UNDEFINED ? 0 : value), level);
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
		chunkStacksVertex[site].incrementSize(trialState);
		chunkStacksVertex[site].setWhat(trialState, whatValue);
		chunkStacksVertex[site].setWho(trialState, whoId);
		chunkStacksVertex[site].setState(trialState, stateVal);
		chunkStacksVertex[site].setRotation(trialState, rotationVal);
		chunkStacksVertex[site].setValue(trialState, valueVal);
	}

	@Override
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, Game game, boolean[] hiddenValues,
			boolean masked)
	{
		verifyPresentVertex(site);
		chunkStacksVertex[site].incrementSize(trialState);
		chunkStacksVertex[site].setWhat(trialState, whatValue);
		chunkStacksVertex[site].setWho(trialState, whoId);
	}

	@Override
	public void removeStackVertex(State trialState, int site)
	{
		if (chunkStacksVertex[site] == null)
			return;

		trialState.updateStateHash(chunkStacksVertex[site].calcHash());
		chunkStacksVertex[site] = null;
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, Game game)
	{
		verifyPresentEdge(site);
		chunkStacksEdge[site].incrementSize(trialState);
		chunkStacksEdge[site].setWhat(trialState, whatValue);
		chunkStacksEdge[site].setWho(trialState, whoId);
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
			chunkStacksEdge[site - offset].incrementSize(trialState);
			chunkStacksEdge[site - offset].setWhat(trialState, what);
			chunkStacksEdge[site - offset].setWho(trialState, who);
			chunkStacksEdge[site - offset].setState(trialState, (state == Constants.UNDEFINED ? 0 : state));
			chunkStacksEdge[site - offset].setRotation(trialState, (rotation == Constants.UNDEFINED ? 0 : rotation));
			chunkStacksEdge[site - offset].setValue(trialState, 0, (value == Constants.UNDEFINED ? 0 : value));
		}
		else if (level < size)
		{
			chunkStacksEdge[site - offset].incrementSize(trialState);
			for (int i = size - 1; i >= level; i--)
			{
				final int whatLevel = chunkStacksEdge[site - offset].what(i);
				chunkStacksEdge[site - offset].setWhat(trialState, whatLevel, i + 1);

				final int whoLevel = chunkStacksEdge[site - offset].who(i);
				chunkStacksEdge[site - offset].setWho(trialState, whoLevel, i + 1);

				final int rotationLevel = chunkStacksEdge[site - offset].rotation(i);
				chunkStacksEdge[site - offset].setRotation(trialState, rotationLevel, i + 1);

				final int valueLevel = chunkStacksEdge[site - offset].value(i);
				chunkStacksEdge[site - offset].setValue(trialState, valueLevel, i + 1);

				final int stateLevel = chunkStacksEdge[site - offset].state(i);
				chunkStacksEdge[site - offset].setState(trialState, stateLevel, i + 1);
			}
			chunkStacksEdge[site - offset].setWhat(trialState, what, level);
			chunkStacksEdge[site - offset].setWho(trialState, who, level);
			chunkStacksEdge[site - offset].setState(trialState, (state == Constants.UNDEFINED ? 0 : state), level);
			chunkStacksEdge[site - offset].setRotation(trialState, (rotation == Constants.UNDEFINED ? 0 : rotation),
					level);
			chunkStacksEdge[site - offset].setValue(trialState, (value == Constants.UNDEFINED ? 0 : value), level);
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
		chunkStacksEdge[site].incrementSize(trialState);
		chunkStacksEdge[site].setWhat(trialState, whatValue);
		chunkStacksEdge[site].setWho(trialState, whoId);
		chunkStacksEdge[site].setState(trialState, stateVal);
		chunkStacksEdge[site].setRotation(trialState, rotationVal);
		chunkStacksEdge[site].setValue(trialState, valueVal);
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, Game game, boolean[] hiddenValues,
			boolean masked)
	{
		verifyPresentEdge(site);
		chunkStacksEdge[site].incrementSize(trialState);
		chunkStacksEdge[site].setWhat(trialState, whatValue);
		chunkStacksEdge[site].setWho(trialState, whoId);
	}

	@Override
	public void removeStackEdge(State trialState, int site)
	{
		if (chunkStacksEdge[site] == null)
			return;

		trialState.updateStateHash(chunkStacksEdge[site].calcHash());
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
			super.remove(state, site, graphElement);
		else if (graphElement == SiteType.Vertex)
		{
			if (chunkStacksVertex[site] == null)
				return 0;

			final int componentRemove = chunkStacksVertex[site].what();
			chunkStacksVertex[site].setWhat(state, 0);
			chunkStacksVertex[site].setWho(state, 0);
			chunkStacksVertex[site].setState(state, 0);
			chunkStacksVertex[site].setRotation(state, 0);
			chunkStacksVertex[site].setValue(state, 0);
			chunkStacksVertex[site].decrementSize(state);
			return componentRemove;
		}

		// Edge
		if (chunkStacksEdge[site] == null)
			return 0;

		final int componentRemove = chunkStacksEdge[site].what();
		chunkStacksEdge[site].setWhat(state, 0);
		chunkStacksEdge[site].setWho(state, 0);
		chunkStacksEdge[site].setState(state, 0);
		chunkStacksEdge[site].setRotation(state, 0);
		chunkStacksEdge[site].setValue(state, 0);
		chunkStacksEdge[site].decrementSize(state);
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
				chunkStacksVertex[site].setWhat(state, chunkStacksVertex[site].what(i + 1), i);
				chunkStacksVertex[site].setWho(state, chunkStacksVertex[site].who(i + 1), i);
				chunkStacksVertex[site].setState(state, chunkStacksVertex[site].state(i + 1), i);
				chunkStacksVertex[site].setRotation(state, chunkStacksVertex[site].rotation(i + 1), i);
				chunkStacksVertex[site].setValue(state, chunkStacksVertex[site].value(i + 1), i);
			}
			chunkStacksVertex[site].setWhat(state, 0);
			chunkStacksVertex[site].setWho(state, 0);
			chunkStacksVertex[site].decrementSize(state);
			return componentRemove;
		}

		// Edge
		if (chunkStacksEdge[site] == null)
			return 0;

		final int componentRemove = chunkStacksEdge[site].what(level);
		for (int i = level; i < sizeStack(site, graphElement) - 1; i++)
		{
			chunkStacksEdge[site].setWhat(state, chunkStacksEdge[site].what(i + 1), i);
			chunkStacksEdge[site].setWho(state, chunkStacksEdge[site].who(i + 1), i);
			chunkStacksEdge[site].setState(state, chunkStacksEdge[site].state(i + 1), i);
			chunkStacksEdge[site].setRotation(state, chunkStacksEdge[site].rotation(i + 1), i);
			chunkStacksEdge[site].setValue(state, chunkStacksEdge[site].value(i + 1), i);
		}
		chunkStacksEdge[site].setWhat(state, 0);
		chunkStacksEdge[site].setWho(state, 0);
		chunkStacksEdge[site].decrementSize(state);
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
		return new ContainerGraphStateStacks(this);
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

			return chunkStacksEdge[site - offset].isHidden(player, site, level, graphElementType);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return chunkStacksVertex[site - offset].isHidden(player, site, level, graphElementType);
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

			return chunkStacksEdge[site - offset].isHiddenWhat(player, site, level, graphElementType);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return chunkStacksVertex[site - offset].isHiddenWhat(player, site, level, graphElementType);
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

			return chunkStacksEdge[site - offset].isHiddenWho(player, site, level, graphElementType);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return chunkStacksVertex[site - offset].isHiddenWho(player, site, level, graphElementType);
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

			return chunkStacksEdge[site - offset].isHiddenState(player, site, level, graphElementType);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return chunkStacksVertex[site - offset].isHiddenState(player, site, level, graphElementType);
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

			return chunkStacksEdge[site - offset].isHiddenRotation(player, site, level, graphElementType);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return chunkStacksVertex[site - offset].isHiddenRotation(player, site, level, graphElementType);
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

			return chunkStacksEdge[site - offset].isHiddenValue(player, site, level, graphElementType);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return chunkStacksVertex[site - offset].isHiddenValue(player, site, level, graphElementType);
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

			return chunkStacksEdge[site - offset].isHiddenCount(player, site, level, graphElementType);
		}
		else
		{
			if (!hiddenVertexInfo)
				return false;

			return chunkStacksVertex[site - offset].isHiddenCount(player, site, level, graphElementType);
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

			chunkStacksEdge[site - offset].setHidden(state, player, site, level, graphElementType, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			chunkStacksVertex[site - offset].setHidden(state, player, site, level, graphElementType, on);
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

			chunkStacksEdge[site - offset].setHiddenWhat(state, player, site, level, graphElementType, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			chunkStacksVertex[site - offset].setHiddenWhat(state, player, site, level, graphElementType, on);
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

			chunkStacksEdge[site - offset].setHiddenWho(state, player, site, level, graphElementType, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			chunkStacksVertex[site - offset].setHiddenWho(state, player, site, level, graphElementType, on);
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

			chunkStacksEdge[site - offset].setHiddenState(state, player, site, level, graphElementType, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			chunkStacksVertex[site - offset].setHiddenState(state, player, site, level, graphElementType, on);
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

			chunkStacksEdge[site - offset].setHiddenRotation(state, player, site, level, graphElementType, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			chunkStacksVertex[site - offset].setHiddenRotation(state, player, site, level, graphElementType, on);
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

			chunkStacksEdge[site - offset].setHiddenValue(state, player, site, level, graphElementType, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			chunkStacksVertex[site - offset].setHiddenValue(state, player, site, level, graphElementType, on);
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

			chunkStacksEdge[site - offset].setHiddenCount(state, player, site, level, graphElementType, on);
		}
		else
		{
			if (!hiddenVertexInfo)
				return;

			chunkStacksVertex[site - offset].setHiddenCount(state, player, site, level, graphElementType, on);
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
