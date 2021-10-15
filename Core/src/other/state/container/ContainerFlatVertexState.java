package other.state.container;

import game.Game;
import game.equipment.container.Container;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.equipment.Region;
import main.Constants;
import main.collections.ChunkSet;
import other.Sites;
import other.state.State;
import other.state.zhash.HashedBitSet;
import other.state.zhash.HashedChunkSet;
import other.state.zhash.ZobristHashGenerator;

/**
 * Global State for a container item using only vertices.
 *
 * @author Eric.Piette
 */
public class ContainerFlatVertexState extends BaseContainerState
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Owner of a vertex. */
	private final HashedChunkSet whoVertex;

	/** Type of Item on a vertex. */
	private final HashedChunkSet whatVertex;

	/** Count of a vertex. */
	private final HashedChunkSet countVertex;

	/** State of a vertex. */
	private final HashedChunkSet stateVertex;

	/** Rotation of a vertex. */
	private final HashedChunkSet rotationVertex;

	/** Value of the piece on a vertex. */
	private final HashedChunkSet valueVertex;

	/** Which vertex has some hidden properties for each player. */
	private final HashedBitSet[] hiddenVertex;

	/** Which vertex has the what information hidden for each player. */
	private final HashedBitSet[] hiddenWhatVertex;

	/** Which vertex has the who information hidden for each player. */
	private final HashedBitSet[] hiddenWhoVertex;

	/** Which vertex has the count information hidden for each player. */
	private final HashedBitSet[] hiddenCountVertex;

	/** Which vertex has the state information hidden for each player. */
	private final HashedBitSet[] hiddenStateVertex;

	/** Which vertex has the rotation information hidden for each player. */
	private final HashedBitSet[] hiddenRotationVertex;

	/** Which vertex has the value information hidden for each player. */
	private final HashedBitSet[] hiddenValueVertex;

	/** Which vertex slots are empty. */		//Improvement: it's not great that we have this, but also still empty (for cells) from parent class...
	private final Region emptyVertex;

	//-------------------------------------------------------------------------

	/**
	 * Constructor of a flat container.
	 * 
	 * @param generator
	 * @param game
	 * @param container
	 * @param maxWhatVal
	 * @param maxStateVal
	 * @param maxCountVal
	 * @param maxRotationVal
	 * @param maxPieceValue
	 */
	public ContainerFlatVertexState
	(
		final ZobristHashGenerator generator, 
		final Game game, 
		final Container container, 
		final int maxWhatVal, 
		final int maxStateVal,
		final int maxCountVal,
		final int maxRotationVal,
		final int maxPieceValue
	)
	{
		super
		(
			game, 
			container, 
			container.numSites()
		);
		final int numPlayers = game.players().count();
		final int numVertices = game.board().topology().vertices().size();

		if ((game.gameFlags() & GameType.HiddenInfo) == 0L)
		{
			hiddenVertex = null;
			hiddenWhatVertex = null;
			hiddenWhoVertex = null;
			hiddenCountVertex = null;
			hiddenStateVertex = null;
			hiddenRotationVertex = null;
			hiddenValueVertex = null;
		}
		else
		{
			hiddenVertex = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenVertex[i] = new HashedBitSet(generator, numVertices);
			hiddenWhatVertex = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenWhatVertex[i] = new HashedBitSet(generator, numVertices);
			hiddenWhoVertex = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenWhoVertex[i] = new HashedBitSet(generator, numVertices);
			hiddenCountVertex = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenCountVertex[i] = new HashedBitSet(generator, numVertices);
			hiddenStateVertex = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenStateVertex[i] = new HashedBitSet(generator, numVertices);
			hiddenRotationVertex = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenRotationVertex[i] = new HashedBitSet(generator, numVertices);
			hiddenValueVertex = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenValueVertex[i] = new HashedBitSet(generator, numVertices);
		}

		this.whoVertex = new HashedChunkSet(generator, numPlayers + 1, numVertices);
		whatVertex = maxWhatVal > 0 ? new HashedChunkSet(generator, maxWhatVal, numVertices) : null;
		countVertex = maxCountVal > 0 ? new HashedChunkSet(generator, maxCountVal, numVertices) : null;
		stateVertex = maxStateVal > 0 ? new HashedChunkSet(generator, maxStateVal, numVertices) : null;
		rotationVertex = maxRotationVal > 0 ? new HashedChunkSet(generator, maxRotationVal, numVertices) : null;
		valueVertex = maxPieceValue > 0 ? new HashedChunkSet(generator, maxPieceValue, numVertices) : null;

		this.emptyVertex = new Region(numVertices);
	}

	/**
	 * Copy constructor.
	 *
	 * @param other
	 */
	public ContainerFlatVertexState(final ContainerFlatVertexState other)
	{
		super(other);

		if (other.hiddenVertex != null)
		{
			hiddenVertex = new HashedBitSet[other.hiddenVertex.length];
			for (int i = 1; i < other.hiddenVertex.length; i++)
				hiddenVertex[i] = (other.hiddenVertex[i] == null) ? null : other.hiddenVertex[i].clone();

			hiddenWhatVertex = new HashedBitSet[other.hiddenWhatVertex.length];
			for (int i = 1; i < other.hiddenWhatVertex.length; i++)
				hiddenWhatVertex[i] = (other.hiddenWhatVertex[i] == null) ? null : other.hiddenWhatVertex[i].clone();

			hiddenWhoVertex = new HashedBitSet[other.hiddenWhoVertex.length];
			for (int i = 1; i < other.hiddenWhoVertex.length; i++)
				hiddenWhoVertex[i] = (other.hiddenWhoVertex[i] == null) ? null : other.hiddenWhoVertex[i].clone();

			hiddenCountVertex = new HashedBitSet[other.hiddenCountVertex.length];
			for (int i = 1; i < other.hiddenCountVertex.length; i++)
				hiddenCountVertex[i] = (other.hiddenCountVertex[i] == null) ? null : other.hiddenCountVertex[i].clone();

			hiddenStateVertex = new HashedBitSet[other.hiddenStateVertex.length];
			for (int i = 1; i < other.hiddenStateVertex.length; i++)
				hiddenStateVertex[i] = (other.hiddenStateVertex[i] == null) ? null : other.hiddenStateVertex[i].clone();

			hiddenRotationVertex = new HashedBitSet[other.hiddenRotationVertex.length];
			for (int i = 1; i < other.hiddenRotationVertex.length; i++)
				hiddenRotationVertex[i] = (other.hiddenRotationVertex[i] == null) ? null
						: other.hiddenRotationVertex[i].clone();

			hiddenValueVertex = new HashedBitSet[other.hiddenValueVertex.length];
			for (int i = 1; i < other.hiddenValueVertex.length; i++)
				hiddenValueVertex[i] = (other.hiddenValueVertex[i] == null) ? null : other.hiddenValueVertex[i].clone();
		}
		else
		{
			hiddenVertex = null;
			hiddenWhatVertex = null;
			hiddenWhoVertex = null;
			hiddenCountVertex = null;
			hiddenRotationVertex = null;
			hiddenValueVertex = null;
			hiddenStateVertex = null;
		}

		this.whoVertex = (other.whoVertex == null) ? null : other.whoVertex.clone();
		this.whatVertex = (other.whatVertex == null) ? null : other.whatVertex.clone();
		this.countVertex = (other.countVertex == null) ? null : other.countVertex.clone();
		this.stateVertex = (other.stateVertex == null) ? null : other.stateVertex.clone();
		this.rotationVertex = (other.rotationVertex == null) ? null : other.rotationVertex.clone();
		this.valueVertex = (other.valueVertex == null) ? null : other.valueVertex.clone();
		this.emptyVertex = (other.emptyVertex == null) ? null : new Region(other.emptyVertex);
	}

	@Override
	public ContainerFlatVertexState deepClone()
	{
		return new ContainerFlatVertexState(this);
	}

	//-------------------------------------------------------------------------

	@Override
	protected long calcCanonicalHash(final int[] siteRemap, final int[] edgeRemap, final int[] vertexRemap,
			final int[] playerRemap, final boolean whoOnly)
	{
		long hash = 0;

		if (vertexRemap != null && vertexRemap.length > 0)
		{
			if (whoVertex != null)
				hash ^= whoVertex.calculateHashAfterRemap(vertexRemap, playerRemap);
			if (!whoOnly)
			{
				if (whatVertex != null)
					hash ^= whatVertex.calculateHashAfterRemap(vertexRemap, null);
				if (countVertex != null)
					hash ^= countVertex.calculateHashAfterRemap(vertexRemap, null);
				if (stateVertex != null)
					hash ^= stateVertex.calculateHashAfterRemap(vertexRemap, null);
				if (rotationVertex != null)
					hash ^= rotationVertex.calculateHashAfterRemap(vertexRemap, null);
				if (hiddenVertex != null)
				{
					for (int i = 1; i < hiddenVertex.length; i++)
						hash ^= hiddenVertex[i].calculateHashAfterRemap(siteRemap, false);
				}
				if (hiddenWhatVertex != null)
				{
					for (int i = 1; i < hiddenWhatVertex.length; i++)
						hash ^= hiddenWhatVertex[i].calculateHashAfterRemap(siteRemap, false);
				}
				if (hiddenWhoVertex != null)
				{
					for (int i = 1; i < hiddenWhoVertex.length; i++)
						hash ^= hiddenWhoVertex[i].calculateHashAfterRemap(siteRemap, false);
				}
				if (hiddenCountVertex != null)
				{
					for (int i = 1; i < hiddenCountVertex.length; i++)
						hash ^= hiddenCountVertex[i].calculateHashAfterRemap(siteRemap, false);
				}
				if (hiddenRotationVertex != null)
				{
					for (int i = 1; i < hiddenRotationVertex.length; i++)
						hash ^= hiddenRotationVertex[i].calculateHashAfterRemap(siteRemap, false);
				}
				if (hiddenValueVertex != null)
				{
					for (int i = 1; i < hiddenValueVertex.length; i++)
						hash ^= hiddenValueVertex[i].calculateHashAfterRemap(siteRemap, false);
				}
				if (hiddenStateVertex != null)
				{
					for (int i = 1; i < hiddenStateVertex.length; i++)
						hash ^= hiddenStateVertex[i].calculateHashAfterRemap(siteRemap, false);
				}
			}
		}

		return hash;
	}

	//-------------------------------------------------------------------------

	/**
	 * Reset this state.
	 */
	@Override
	public void reset(final State trialState, final Game game)
	{
		super.reset(trialState, game);

		final int numVertices = game.board().topology().vertices().size();
		super.reset(trialState, game);

		if (whoVertex != null)
			whoVertex.clear(trialState);

		if (whatVertex != null)
			whatVertex.clear(trialState);

		if (countVertex != null)
			countVertex.clear(trialState);

		if (stateVertex != null)
			stateVertex.clear(trialState);

		if (rotationVertex != null)
			rotationVertex.clear(trialState);

		if (valueVertex != null)
			valueVertex.clear(trialState);

		if (hiddenVertex != null)
			for (int i = 1; i < hiddenVertex.length; i++)
				hiddenVertex[i].clear(trialState);
		if (hiddenWhatVertex != null)
			for (int i = 1; i < hiddenWhatVertex.length; i++)
				hiddenWhatVertex[i].clear(trialState);
		if (hiddenWhoVertex != null)
			for (int i = 1; i < hiddenWhoVertex.length; i++)
				hiddenWhoVertex[i].clear(trialState);
		if (hiddenCountVertex != null)
			for (int i = 1; i < hiddenCountVertex.length; i++)
				hiddenCountVertex[i].clear(trialState);
		if (hiddenRotationVertex != null)
			for (int i = 1; i < hiddenRotationVertex.length; i++)
				hiddenRotationVertex[i].clear(trialState);
		if (hiddenValueVertex != null)
			for (int i = 1; i < hiddenValueVertex.length; i++)
				hiddenValueVertex[i].clear(trialState);
		if (hiddenStateVertex != null)
			for (int i = 1; i < hiddenStateVertex.length; i++)
				hiddenStateVertex[i].clear(trialState);

		if (emptyVertex != null)
			emptyVertex.set(numVertices);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isHidden(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenVertex == null)
			return false;

		if (player < 1 || player > (hiddenVertex.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (hiddenVertex ...) in the containerState. Player =  "
							+ player);

		return this.hiddenVertex[player].get(site);
	}

	@Override
	public boolean isHiddenWhat(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenWhatVertex == null)
			return false;

		if (player < 1 || player > (hiddenWhatVertex.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (hiddenWhat ...) in the containerState. Player =  "
							+ player);

		return this.hiddenWhatVertex[player].get(site);
	}

	@Override
	public boolean isHiddenWho(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenWhoVertex == null)
			return false;

		if (player < 1 || player > (hiddenWhoVertex.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (hiddenWho ...) in the containerState. Player =  "
							+ player);

		return this.hiddenWhoVertex[player].get(site);
	}

	@Override
	public boolean isHiddenState(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenStateVertex == null)
			return false;

		if (player < 1 || player > (hiddenStateVertex.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (hiddenState ...) in the containerState. Player =  "
							+ player);

		return this.hiddenStateVertex[player].get(site);
	}

	@Override
	public boolean isHiddenRotation(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenRotationVertex == null)
			return false;

		if (player < 1 || player > (hiddenRotationVertex.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (hiddenRotation ...) in the containerState. Player =  "
							+ player);

		return this.hiddenRotationVertex[player].get(site);
	}

	@Override
	public boolean isHiddenValue(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenValueVertex == null)
			return false;

		if (player < 1 || player > (hiddenValueVertex.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (hiddenValue ...) in the containerState. Player =  "
							+ player);

		return this.hiddenValueVertex[player].get(site);
	}

	@Override
	public boolean isHiddenCount(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenCountVertex == null)
			return false;

		if (player < 1 || player > (hiddenCountVertex.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (hiddenCount ...) in the containerState. Player =  "
							+ player);

		return this.hiddenCountVertex[player].get(site);
	}

	@Override
	public void setHidden(final State state, final int player, final int site, final int level, final SiteType type,
			final boolean on)
	{
		if (hiddenVertex == null)
			throw new UnsupportedOperationException("No Hidden information, but the method (setHidden ...) was called");

		if (player < 1 || player > (hiddenVertex.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHidden ...) in the containerState. Player =  "
							+ player);

		this.hiddenVertex[player].set(state, site, on);
	}

	@Override
	public void setHiddenWhat(final State state, final int player, final int site, final int level, final SiteType type,
			final boolean on)
	{
		if (hiddenWhatVertex == null)
			throw new UnsupportedOperationException(
					"No Hidden information, but the method (setHiddenWhat ...) was called");

		if (player < 1 || player > (hiddenWhatVertex.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHiddenWhat ...) in the containerState. Player =  "
							+ player);

		this.hiddenWhatVertex[player].set(state, site, on);
	}

	@Override
	public void setHiddenWho(final State state, final int player, final int site, final int level, final SiteType type,
			final boolean on)
	{
		if (hiddenWhoVertex == null)
			throw new UnsupportedOperationException(
					"No Hidden information, but the method (setHiddenWho ...) was called");

		if (player < 1 || player > (hiddenWhoVertex.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHiddenWho ...) in the containerState. Player =  "
							+ player);

		this.hiddenWhoVertex[player].set(state, site, on);
	}

	@Override
	public void setHiddenState(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (hiddenStateVertex == null)
			throw new UnsupportedOperationException(
					"No Hidden information, but the method (setHiddenState ...) was called");

		if (player < 1 || player > (hiddenStateVertex.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHiddenState ...) in the containerState. Player =  "
							+ player);

		this.hiddenStateVertex[player].set(state, site, on);
	}

	@Override
	public void setHiddenRotation(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (hiddenRotationVertex == null)
			throw new UnsupportedOperationException(
					"No Hidden information, but the method (setHiddenRotation ...) was called");

		if (player < 1 || player > (hiddenRotationVertex.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHiddenRotation ...) in the containerState. Player =  "
							+ player);

		this.hiddenRotationVertex[player].set(state, site, on);
	}

	@Override
	public void setHiddenValue(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (hiddenValueVertex == null)
			throw new UnsupportedOperationException(
					"No Hidden information, but the method (setHiddenValue ...) was called");

		if (player < 1 || player > (hiddenValueVertex.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHiddenValue ...) in the containerState. Player =  "
							+ player);

		this.hiddenValueVertex[player].set(state, site, on);
	}

	@Override
	public void setHiddenCount(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (hiddenCountVertex == null)
			throw new UnsupportedOperationException(
					"No Hidden information, but the method (setHiddenCount ...) was called");

		if (player < 1 || player > (hiddenCountVertex.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHiddenCount ...) in the containerState. Player =  "
							+ player);

		this.hiddenCountVertex[player].set(state, site, on);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isPlayable(final int site)
	{
		return true;
	}

	@Override
	public void setPlayable(final State trialState, final int site, final boolean on)
	{
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isOccupied(final int site)
	{
		return countVertex(site) != 0;
	}

	//-------------------------------------------------------------------------
	@Override
	public void setSite(final State trialState, final int site, final int whoVal, final int whatVal, final int countVal,
			final int stateVal, final int rotationVal, final int valueVal, final SiteType type)
	{
		final boolean wasEmpty = !isOccupied(site);

		if (whoVal != Constants.UNDEFINED)
			whoVertex.setChunk(trialState, site, whoVal);

		if (whatVal != Constants.UNDEFINED)
			defaultIfNull(whatVertex).setChunk(trialState, site, whatVal);

		if (countVal != Constants.UNDEFINED)
		{
			if (countVertex != null)
				countVertex.setChunk(trialState, site, (countVal < 0 ? 0 : countVal));
			else if (countVertex == null && countVal > 1)
				throw new UnsupportedOperationException(
						"This game does not support counts, but a count > 1 has been set. countVal=" + countVal);
		}

		if (stateVal != Constants.UNDEFINED)
		{
			if (stateVertex != null)
				stateVertex.setChunk(trialState, site, stateVal);
			else if (stateVal != 0)
				throw new UnsupportedOperationException(
						"This game does not support states, but a state has been set. stateVal=" + stateVal);
		}

		if (rotationVal != Constants.UNDEFINED)
		{
			if (rotationVertex != null)
				rotationVertex.setChunk(trialState, site, rotationVal);
			else if (rotationVal != 0)
				throw new UnsupportedOperationException(
						"This game does not support rotations, but a rotation has been set. rotationVal="
								+ rotationVal);
		}

		if (valueVal != Constants.UNDEFINED)
		{
			if (valueVertex != null)
				valueVertex.setChunk(trialState, site, valueVal);
			else if (valueVal != 0)
				throw new UnsupportedOperationException(
						"This game does not support piece values, but a value has been set. valueVal=" + valueVal);
		}

		final boolean isEmpty = !isOccupied(site);

		if (wasEmpty == isEmpty)
			return;

		if (isEmpty)
		{
			addToEmptyCell(site);
		}
		else
		{
			removeFromEmptyCell(site);
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public int whoCell(final int site)
	{
		return 0;
	}

	//-------------------------------------------------------------------------

	@Override
	public int whatCell(final int site)
	{
		return 0;
	}

	//-------------------------------------------------------------------------

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
	public int valueCell(final int site)
	{
		return 0;
	}

	//-------------------------------------------------------------------------

	@Override
	public int countCell(final int site)
	{
		return 0;
	}

	//-------------------------------------------------------------------------

	@Override
	public int remove(final State trialState, final int site, final SiteType type)
	{
		final int whatIdx = what(site, type);

		setSite(trialState, site, 0, 0, 0, 0, 0, 0, type);

		return whatIdx;
	}

	@Override
	public int remove(final State trialState, final int site, final int level, final SiteType type)
	{
		return remove(trialState, site, type);
	}

	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((hiddenVertex == null) ? 0 : hiddenVertex.hashCode());
		result = prime * result + ((hiddenWhatVertex == null) ? 0 : hiddenWhatVertex.hashCode());
		result = prime * result + ((hiddenWhoVertex == null) ? 0 : hiddenWhoVertex.hashCode());
		result = prime * result + ((hiddenStateVertex == null) ? 0 : hiddenStateVertex.hashCode());
		result = prime * result + ((hiddenRotationVertex == null) ? 0 : hiddenRotationVertex.hashCode());
		result = prime * result + ((hiddenValueVertex == null) ? 0 : hiddenValueVertex.hashCode());
		result = prime * result + ((hiddenStateVertex == null) ? 0 : hiddenStateVertex.hashCode());
		result = prime * result + ((whoVertex == null) ? 0 : whoVertex.hashCode());
		result = prime * result + ((countVertex == null) ? 0 : countVertex.hashCode());
		result = prime * result + ((whatVertex == null) ? 0 : whatVertex.hashCode());
		result = prime * result + ((stateVertex == null) ? 0 : stateVertex.hashCode());
		result = prime * result + ((rotationVertex == null) ? 0 : rotationVertex.hashCode());
		result = prime * result + ((valueVertex == null) ? 0 : valueVertex.hashCode());

		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (!(obj instanceof ContainerFlatVertexState))
			return false;

		if (!super.equals(obj))
			return false;

		final ContainerFlatVertexState other = (ContainerFlatVertexState) obj;

		if (hiddenVertex != null)
		{
			for (int i = 1; i < hiddenVertex.length; i++)
				if (!bitSetsEqual(hiddenVertex[i], other.hiddenVertex[i]))
					return false;
		}
		if (hiddenWhatVertex != null)
		{
			for (int i = 1; i < hiddenWhatVertex.length; i++)
				if (!bitSetsEqual(hiddenWhatVertex[i], other.hiddenWhatVertex[i]))
					return false;
		}
		if (hiddenWhoVertex != null)
		{
			for (int i = 1; i < hiddenWhoVertex.length; i++)
				if (!bitSetsEqual(hiddenWhoVertex[i], other.hiddenWhoVertex[i]))
					return false;
		}
		if (hiddenRotationVertex != null)
		{
			for (int i = 1; i < hiddenRotationVertex.length; i++)
				if (!bitSetsEqual(hiddenRotationVertex[i], other.hiddenRotationVertex[i]))
					return false;
		}
		if (hiddenStateVertex != null)
		{
			for (int i = 1; i < hiddenStateVertex.length; i++)
				if (!bitSetsEqual(hiddenStateVertex[i], other.hiddenStateVertex[i]))
					return false;
		}
		if (hiddenValueVertex != null)
		{
			for (int i = 1; i < hiddenValueVertex.length; i++)
				if (!bitSetsEqual(hiddenValueVertex[i], other.hiddenValueVertex[i]))
					return false;
		}
		if (hiddenValueVertex != null)
		{
			for (int i = 1; i < hiddenValueVertex.length; i++)
				if (!bitSetsEqual(hiddenValueVertex[i], other.hiddenValueVertex[i]))
					return false;
		}
		if (hiddenCountVertex != null)
		{
			for (int i = 1; i < hiddenCountVertex.length; i++)
				if (!bitSetsEqual(hiddenCountVertex[i], other.hiddenCountVertex[i]))
					return false;
		}

		if (!chunkSetsEqual(whoVertex, other.whoVertex))
			return false;
		if (!chunkSetsEqual(countVertex, other.countVertex))
			return false;
		if (!chunkSetsEqual(whatVertex, other.whatVertex))
			return false;
		if (!chunkSetsEqual(stateVertex, other.stateVertex))
			return false;
		if (!chunkSetsEqual(rotationVertex, other.rotationVertex))
			return false;
		if (!chunkSetsEqual(valueVertex, other.valueVertex))
			return false;

		return true;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("ContainerState type = " + this.getClass() + "\n");

		if (emptyChunkSetVertex() != null)
			sb.append("Empty = " + emptyChunkSetVertex().toChunkString() + "\n");

		if (whoVertex != null)
			sb.append("Who = " + cloneWhoVertex().toChunkString() + "\n");

		if (whatVertex != null)
			sb.append("What" + cloneWhatVertex().toChunkString() + "\n");

		if (stateVertex != null)
			sb.append("State = " + stateVertex.internalStateCopy().toChunkString() + "\n");

		if (rotationVertex != null)
			sb.append("Rotation = " + rotationVertex.internalStateCopy().toChunkString() + "\n");

		if (valueVertex != null)
			sb.append("value = " + valueVertex.internalStateCopy().toChunkString() + "\n");

		if (countVertex != null)
			sb.append("Count = " + countVertex.internalStateCopy().toChunkString() + "\n");

		if (hiddenVertex != null)
		{
			for (int i = 1; i < hiddenVertex.length; i++)
				sb.append(
						"Hidden for " + " player " + i + " = " + hiddenVertex[i].internalStateCopy().toString() + "\n");
		}
		if (hiddenWhatVertex != null)
		{
			for (int i = 1; i < hiddenWhatVertex.length; i++)
				sb.append(
						"Hidden What for " + " player " + i + " = " + hiddenWhatVertex[i].internalStateCopy().toString()
						+ "\n");
		}
		if (hiddenWhoVertex != null)
		{
			for (int i = 1; i < hiddenWhoVertex.length; i++)
				sb.append("Hidden Who for " + " player " + i + " = " + hiddenWhoVertex[i].internalStateCopy().toString()
						+ "\n");
		}
		if (hiddenCountVertex != null)
		{
			for (int i = 1; i < hiddenCountVertex.length; i++)
				sb.append("Hidden Count for " + " player " + i + " = "
						+ hiddenCountVertex[i].internalStateCopy().toString()
						+ "\n");
		}
		if (hiddenValueVertex != null)
		{
			for (int i = 1; i < hiddenValueVertex.length; i++)
				sb.append("Hidden Value for " + " player " + i + " = "
						+ hiddenValueVertex[i].internalStateCopy().toString()
						+ "\n");
		}
		if (hiddenStateVertex != null)
		{
			for (int i = 1; i < hiddenStateVertex.length; i++)
				sb.append("Hidden State for " + " player " + i + " = "
						+ hiddenStateVertex[i].internalStateCopy().toString()
						+ "\n");
		}
		if (hiddenRotationVertex != null)
		{
			for (int i = 1; i < hiddenRotationVertex.length; i++)
				sb.append("Hidden Rotation for " + " player " + i + " = "
						+ hiddenRotationVertex[i].internalStateCopy().toString() + "\n");
		}

		return sb.toString();
	}

	private static final boolean chunkSetsEqual(final HashedChunkSet thisSet, final HashedChunkSet otherSet)
	{
		if (thisSet == null)
			return otherSet == null;
		return thisSet.equals(otherSet);
	}

	private static final boolean bitSetsEqual(final HashedBitSet thisSet, final HashedBitSet otherSet)
	{
		if (thisSet == null)
			return otherSet == null;
		return thisSet.equals(otherSet);
	}

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @return Size of stack.
	 */
	@Override
	public int sizeStackCell(final int site)
	{
		if (whatCell(site) != 0)
			return 1;

		return 0;
	}

	/**
	 * @param site
	 * @return Size of stack edge.
	 */
	@Override
	public int sizeStackEdge(final int site)
	{
		if (whatEdge(site) != 0)
			return 1;

		return 0;
	}

	/**
	 * @param site
	 * @return Size of stack vertex.
	 */
	@Override
	public int sizeStackVertex(final int site)
	{
		if (whatVertex(site) != 0)
			return 1;

		return 0;
	}

	@Override
	public int whoEdge(final int edge)
	{
		return 0;
	}

	@Override
	public int whoVertex(final int vertex)
	{
		return whoVertex.getChunk(vertex);
	}

	@Override
	public int whatEdge(int site)
	{
		return 0;
	}

	@Override
	public int countEdge(int site)
	{
		return 0;
	}

	@Override
	public int stateEdge(int site)
	{
		return 0;
	}

	@Override
	public int rotationEdge(int site)
	{
		return 0;
	}

	@Override
	public int whatVertex(int site)
	{
		return whatVertex.getChunk(site);
	}

	@Override
	public int countVertex(int site)
	{
		if (countVertex != null)
			return countVertex.getChunk(site);

		if (whoVertex.getChunk(site) != 0 || whatVertex != null && whatVertex.getChunk(site) != 0)
			return 1;

		return 0;
	}

	@Override
	public int stateVertex(int site)
	{
		if (stateVertex == null)
			return 0;

		return stateVertex.getChunk(site);
	}

	@Override
	public int rotationVertex(int site)
	{
		if (rotationVertex == null)
			return 0;

		return rotationVertex.getChunk(site);
	}

	@Override
	public void setValueCell(final State trialState, final int site, final int valueVal)
	{
		// do nothing
	}

	@Override
	public void setCount(final State trialState, final int site, final int countVal)
	{
		// do nothing
	}

	@Override
	public void addItem(State trialState, int site, int whatItem, int whoItem, Game game)
	{
		// do nothing
	}

	@Override
	public void insert(State trialState, SiteType type, int site, int level, int whatItem, int whoItem, final int state,
			final int rotation, final int value, Game game)
	{
		// do nothing
	}

	@Override
	public void insertCell(State trialState, int site, int level, int whatItem, int whoItem, final int state,
			final int rotation, final int value, Game game)
	{
		// do nothing
	}

	@Override
	public void addItem(State trialState, int site, int whatItem, int whoItem, int stateVal, int rotationVal,
			int valueval, Game game)
	{
		// do nothing
	}

	@Override
	public void addItem(State trialState, int site, int whatItem, int whoItem, Game game, boolean[] hiddenItem,
			final boolean masked)
	{
		// do nothing
	}

	@Override
	public void removeStack(State trialState, int site)
	{
		// do nothing
	}

	@Override
	public int whoCell(int site, int level)
	{
		return whoCell(site);
	}

	@Override
	public int whatCell(int site, int level)
	{
		return whatCell(site);
	}

	@Override
	public int stateCell(int site, int level)
	{
		return stateCell(site);
	}

	@Override
	public int rotationCell(int site, int level)
	{
		return rotationCell(site);
	}

	@Override
	public int remove(State trialState, int site, int level)
	{
		return remove(trialState, site, SiteType.Vertex);
	}

	@Override
	public void setSite(State trialState, int site, int level, int whoVal, int whatVal, int countVal, int stateVal,
			int rotationVal, int valueVal)
	{
		setSite(trialState, site, whoVal, whatVal, countVal, stateVal, rotationVal, valueVal, SiteType.Vertex);
	}

	@Override
	public int whoVertex(int site, int level)
	{
		return whoVertex(site);
	}

	@Override
	public int whatVertex(int site, int level)
	{
		return whatVertex(site);
	}

	@Override
	public int stateVertex(int site, int level)
	{
		return stateVertex(site);
	}

	@Override
	public int rotationVertex(int site, int level)
	{
		return rotationVertex(site);
	}

	@Override
	public int whoEdge(int site, int level)
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
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, Game game)
	{
		// Nothing to do.
	}

	@Override
	public void insertVertex(State trialState, int site, int level, int whatValue, int whoId, final int state,
			final int rotation, final int value, Game game)
	{
		// Nothing to do.
	}

	@Override
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, int stateVal, int rotationVal,
			int valueVal, Game game)
	{
		// Nothing to do.
	}

	@Override
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, Game game, boolean[] hiddenValues,
			boolean masked)
	{
		// Nothing to do.
	}

	@Override
	public void removeStackVertex(State trialState, int site)
	{
		// Nothing to do.
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, Game game)
	{
		// Nothing to do.
	}

	@Override
	public void insertEdge(State trialState, int site, int level, int whatValue, int whoId, final int state,
			final int rotation, final int value, Game game)
	{
		// Nothing to do.
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, int stateVal, int rotationVal,
			int valueVal, Game game)
	{
		// Nothing to do.

	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, Game game, boolean[] hiddenValues,
			boolean masked)
	{
		// Nothing to do.
	}

	@Override
	public void removeStackEdge(State trialState, int site)
	{
		// Nothing to do.
	}

	@Override
	public void addItemGeneric(State trialState, int site, int whatValue, int whoId, Game game,
			SiteType graphElementType)
	{
		// Do nothing.
	}

	@Override
	public void addItemGeneric(State trialState, int site, int whatValue, int whoId, int stateVal, int rotationVal,
			int valueVal, Game game, SiteType graphElementType)
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
	public Sites emptySites()
	{
		return new Sites(emptyVertex.sites());
	}

	@Override
	public int numEmpty()
	{
		return emptyVertex.count();
	}

	@Override
	public void addToEmpty(final int site, final SiteType graphType)
	{
		emptyVertex.add(site);
	}

	@Override
	public void removeFromEmpty(final int site, final SiteType graphType)
	{
		emptyVertex.remove(site);
	}

	@Override
	public boolean isEmptyVertex(final int vertex)
	{
		return emptyVertex.contains(vertex);
	}

	@Override
	public boolean isEmptyEdge(final int edge)
	{
		return true;
	}

	@Override
	public boolean isEmptyCell(final int site)
	{
		return true;
	}

	@Override
	public Region emptyRegion(final SiteType type)
	{
		return emptyVertex;
	}

	@Override
	public void addToEmptyCell(final int site)
	{
		emptyVertex.add(site);
	}

	@Override
	public void removeFromEmptyCell(final int site)
	{
		emptyVertex.remove(site);
	}

	//-------------------------------------------------------------------------

	/**
	 * @param preferred
	 * @return Given HashChunkSet, or who as a replacement if the argument is null
	 */
	protected final HashedChunkSet defaultIfNull(final HashedChunkSet preferred)
	{
		if (preferred != null)
			return preferred;
		return whoVertex;
	}

	@Override
	public ChunkSet emptyChunkSetCell()
	{
		return null;
	}

	@Override
	public ChunkSet emptyChunkSetVertex()
	{
		return emptyVertex.bitSet();
	}

	@Override
	public ChunkSet emptyChunkSetEdge()
	{
		return null;
	}

	@Override
	public int numChunksWhoCell()
	{
		return Constants.UNDEFINED;
	}

	@Override
	public int numChunksWhoVertex()
	{
		return whoVertex.numChunks();
	}

	@Override
	public int numChunksWhoEdge()
	{
		return Constants.UNDEFINED;
	}

	@Override
	public int chunkSizeWhoCell()
	{
		return Constants.UNDEFINED;
	}

	@Override
	public int chunkSizeWhoVertex()
	{
		return whoVertex.chunkSize();
	}

	@Override
	public int chunkSizeWhoEdge()
	{
		return Constants.UNDEFINED;
	}

	@Override
	public int numChunksWhatCell()
	{
		return Constants.UNDEFINED;
	}

	@Override
	public int numChunksWhatVertex()
	{
		return defaultIfNull(whatVertex).numChunks();
	}

	@Override
	public int numChunksWhatEdge()
	{
		return Constants.UNDEFINED;
	}

	@Override
	public int chunkSizeWhatCell()
	{
		return Constants.UNDEFINED;
	}

	@Override
	public int chunkSizeWhatVertex()
	{
		return defaultIfNull(whatVertex).chunkSize();
	}

	@Override
	public int chunkSizeWhatEdge()
	{
		return Constants.UNDEFINED;
	}

	@Override
	public boolean matchesWhoCell(final ChunkSet mask, final ChunkSet pattern)
	{
		return false;
	}

	@Override
	public boolean matchesWhoVertex(final ChunkSet mask, final ChunkSet pattern)
	{
		return whoVertex.matches(mask, pattern);
	}

	@Override
	public boolean matchesWhoEdge(final ChunkSet mask, final ChunkSet pattern)
	{
		return false;
	}

	@Override
	public boolean violatesNotWhoCell(final ChunkSet mask, final ChunkSet pattern)
	{
		return false;
	}

	@Override
	public boolean violatesNotWhoVertex(final ChunkSet mask, final ChunkSet pattern)
	{
		return whoVertex.violatesNot(mask, pattern);
	}

	@Override
	public boolean violatesNotWhoEdge(final ChunkSet mask, final ChunkSet pattern)
	{
		return false;
	}

	@Override
	public boolean violatesNotWhoCell(final ChunkSet mask, final ChunkSet pattern, final int startWord)
	{
		return false;
	}

	@Override
	public boolean violatesNotWhoVertex(final ChunkSet mask, final ChunkSet pattern, final int startWord)
	{
		return whoVertex.violatesNot(mask, pattern, startWord);
	}

	@Override
	public boolean violatesNotWhoEdge(final ChunkSet mask, final ChunkSet pattern, final int startWord)
	{
		return false;
	}

	@Override
	public boolean matchesWhatCell(final ChunkSet mask, final ChunkSet pattern)
	{
		return false;
	}

	@Override
	public boolean matchesWhatVertex(final ChunkSet mask, final ChunkSet pattern)
	{
		return defaultIfNull(whatVertex).matches(mask, pattern);
	}

	@Override
	public boolean matchesWhatEdge(final ChunkSet mask, final ChunkSet pattern)
	{
		return false;
	}

	@Override
	public boolean violatesNotWhatCell(final ChunkSet mask, final ChunkSet pattern)
	{
		return false;
	}

	@Override
	public boolean violatesNotWhatVertex(final ChunkSet mask, final ChunkSet pattern)
	{
		return defaultIfNull(whatVertex).violatesNot(mask, pattern);
	}

	@Override
	public boolean violatesNotWhatEdge(final ChunkSet mask, final ChunkSet pattern)
	{
		return false;
	}

	@Override
	public boolean violatesNotWhatCell(final ChunkSet mask, final ChunkSet pattern, final int startWord)
	{
		return false;
	}

	@Override
	public boolean violatesNotWhatVertex(final ChunkSet mask, final ChunkSet pattern, final int startWord)
	{
		return defaultIfNull(whatVertex).violatesNot(mask, pattern, startWord);
	}

	@Override
	public boolean violatesNotWhatEdge(final ChunkSet mask, final ChunkSet pattern, final int startWord)
	{
		return false;
	}
	
	@Override
	public boolean matchesWhoVertex(final int wordIdx, final long mask, final long matchingWord)
	{
		return whoVertex.matches(wordIdx, mask, matchingWord);
	}

	@Override public boolean matchesWhoEdge(final int wordIdx, final long mask, final long matchingWord) { return false; }
	@Override public boolean matchesWhoCell(final int wordIdx, final long mask, final long matchingWord) { return false; }
	
	@Override 
	public boolean matchesWhatVertex(final int wordIdx, final long mask, final long matchingWord) 
	{ 
		return defaultIfNull(whatVertex).matches(wordIdx, mask, matchingWord);
	}

	@Override public boolean matchesWhatEdge(final int wordIdx, final long mask, final long matchingWord) { return false; }
	@Override public boolean matchesWhatCell(final int wordIdx, final long mask, final long matchingWord) { return false; }

	@Override
	public ChunkSet cloneWhoCell()
	{
		return null;
	}

	@Override
	public ChunkSet cloneWhoVertex()
	{
		return whoVertex.internalStateCopy();
	}

	@Override
	public ChunkSet cloneWhoEdge()
	{
		return null;
	}

	@Override
	public ChunkSet cloneWhatCell()
	{
		return null;
	}

	@Override
	public ChunkSet cloneWhatVertex()
	{
		return defaultIfNull(whatVertex).internalStateCopy();
	}

	@Override
	public ChunkSet cloneWhatEdge()
	{
		return null;
	}

	@Override
	public int valueCell(int site, int level)
	{
		return valueCell(site);
	}

	@Override
	public int valueVertex(int site)
	{
		if (valueVertex == null)
			return 0;

		return valueVertex.getChunk(site);
	}

	@Override
	public int valueVertex(int site, int level)
	{
		if (valueVertex == null)
			return 0;

		return valueVertex(site);
	}

	@Override
	public int valueEdge(int site)
	{
		return 0;
	}

	@Override
	public int valueEdge(int site, int level)
	{
		return valueEdge(site);
	}
	
	//-------------------------------------------------------------------------
	// Optimised versions of methods with graph element types; we only support
	// Vertex anyway, so just assume we were passed Vertex as type
	
	@Override
	public int what(final int site, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Vertex)
			return 0;
		
		return whatVertex(site);
	}

	@Override
	public int who(final int site, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Vertex)
			return 0;
		
		return whoVertex(site);
	}

	@Override
	public int count(final int site, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Vertex)
			return 0;
		
		return countVertex(site);
	}

	@Override
	public int sizeStack(final int site, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Vertex)
			return 0;
		
		return sizeStackVertex(site);

	}

	@Override
	public int state(final int site, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Vertex)
			return 0;
		
		return stateVertex(site);
	}

	@Override
	public int rotation(final int site, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Vertex)
			return 0;
		
		return rotationVertex(site);
	}

	@Override
	public int value(final int site, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Vertex)
			return 0;
		
		return valueVertex(site);
	}

	//-------------------Methods with levels---------------------

	@Override
	public int what(final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Vertex)
			return 0;
		
		return whatVertex(site, level);
	}

	@Override
	public int who(final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Vertex)
			return 0;
		
		return whoVertex(site, level);
	}

	@Override
	public int state(final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Vertex)
			return 0;
		
		return stateVertex(site, level);
	}

	@Override
	public int rotation(final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Vertex)
			return 0;
		
		return rotationVertex(site, level);
	}
	
	@Override
	public int value(final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Vertex)
			return 0;
		
		return valueVertex(site, level);
	}
	
	//-------------------------------------------------------------------------
}
