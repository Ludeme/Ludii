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
 * Global State for a container item using only edges.
 *
 * @author Eric.Piette
 */
public class ContainerFlatEdgeState extends BaseContainerState
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Owner of an edge. */
	private final HashedChunkSet whoEdge;

	/** Type of Item on an edge. */
	private final HashedChunkSet whatEdge;

	/** Count of an edge. */
	private final HashedChunkSet countEdge;

	/** State of an edge. */
	private final HashedChunkSet stateEdge;

	/** Rotation of an edge. */
	private final HashedChunkSet rotationEdge;

	/** Value of the piece on an edge. */
	private final HashedChunkSet valueEdge;

	/** Which edge has some hidden properties for each player. */
	private final HashedBitSet[] hiddenEdge;

	/** Which edge has the what information hidden for each player. */
	private final HashedBitSet[] hiddenWhatEdge;

	/** Which edge has the who information hidden for each player. */
	private final HashedBitSet[] hiddenWhoEdge;

	/** Which edge has the count information hidden for each player. */
	private final HashedBitSet[] hiddenCountEdge;

	/** Which edge has the state information hidden for each player. */
	private final HashedBitSet[] hiddenStateEdge;

	/** Which edge has the rotation information hidden for each player. */
	private final HashedBitSet[] hiddenRotationEdge;

	/** Which edge has the value information hidden for each player. */
	private final HashedBitSet[] hiddenValueEdge;

	/** Which edge slots are empty. */
	private final Region emptyEdge;

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
	public ContainerFlatEdgeState
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
		final int numEdges = game.board().topology().edges().size();

		if ((game.gameFlags() & GameType.HiddenInfo) == 0L)
		{
			hiddenEdge = null;
			hiddenWhatEdge = null;
			hiddenWhoEdge = null;
			hiddenCountEdge = null;
			hiddenStateEdge = null;
			hiddenRotationEdge = null;
			hiddenValueEdge = null;
		}
		else
		{
			hiddenEdge = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenEdge[i] = new HashedBitSet(generator, numEdges);
			hiddenWhatEdge = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenWhatEdge[i] = new HashedBitSet(generator, numEdges);
			hiddenWhoEdge = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenWhoEdge[i] = new HashedBitSet(generator, numEdges);
			hiddenCountEdge = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenCountEdge[i] = new HashedBitSet(generator, numEdges);
			hiddenStateEdge = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenStateEdge[i] = new HashedBitSet(generator, numEdges);
			hiddenRotationEdge = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenRotationEdge[i] = new HashedBitSet(generator, numEdges);
			hiddenValueEdge = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenValueEdge[i] = new HashedBitSet(generator, numEdges);
		}

		this.whoEdge = new HashedChunkSet(generator, numPlayers + 1, numEdges);
		whatEdge = maxWhatVal > 0 ? new HashedChunkSet(generator, maxWhatVal, numEdges) : null;
		countEdge = maxCountVal > 0 ? new HashedChunkSet(generator, maxCountVal, numEdges) : null;
		stateEdge = maxStateVal > 0 ? new HashedChunkSet(generator, maxStateVal, numEdges) : null;
		rotationEdge = maxRotationVal > 0 ? new HashedChunkSet(generator, maxRotationVal, numEdges) : null;
		valueEdge = maxPieceValue > 0 ? new HashedChunkSet(generator, maxPieceValue, numEdges) : null;

		this.emptyEdge = new Region(numEdges);
	}

	/**
	 * Copy constructor.
	 *
	 * @param other
	 */
	public ContainerFlatEdgeState(final ContainerFlatEdgeState other)
	{
		super(other);

		if (other.hiddenEdge != null)
		{
			hiddenEdge = new HashedBitSet[other.hiddenEdge.length];
			for (int i = 1; i < other.hiddenEdge.length; i++)
				hiddenEdge[i] = (other.hiddenEdge[i] == null) ? null : other.hiddenEdge[i].clone();

			hiddenWhatEdge = new HashedBitSet[other.hiddenWhatEdge.length];
			for (int i = 1; i < other.hiddenWhatEdge.length; i++)
				hiddenWhatEdge[i] = (other.hiddenWhatEdge[i] == null) ? null : other.hiddenWhatEdge[i].clone();

			hiddenWhoEdge = new HashedBitSet[other.hiddenWhoEdge.length];
			for (int i = 1; i < other.hiddenWhoEdge.length; i++)
				hiddenWhoEdge[i] = (other.hiddenWhoEdge[i] == null) ? null : other.hiddenWhoEdge[i].clone();

			hiddenCountEdge = new HashedBitSet[other.hiddenCountEdge.length];
			for (int i = 1; i < other.hiddenCountEdge.length; i++)
				hiddenCountEdge[i] = (other.hiddenCountEdge[i] == null) ? null : other.hiddenCountEdge[i].clone();

			hiddenStateEdge = new HashedBitSet[other.hiddenStateEdge.length];
			for (int i = 1; i < other.hiddenStateEdge.length; i++)
				hiddenStateEdge[i] = (other.hiddenStateEdge[i] == null) ? null : other.hiddenStateEdge[i].clone();

			hiddenRotationEdge = new HashedBitSet[other.hiddenRotationEdge.length];
			for (int i = 1; i < other.hiddenRotationEdge.length; i++)
				hiddenRotationEdge[i] = (other.hiddenRotationEdge[i] == null) ? null
						: other.hiddenRotationEdge[i].clone();

			hiddenValueEdge = new HashedBitSet[other.hiddenValueEdge.length];
			for (int i = 1; i < other.hiddenValueEdge.length; i++)
				hiddenValueEdge[i] = (other.hiddenValueEdge[i] == null) ? null : other.hiddenValueEdge[i].clone();
		}
		else
		{
			hiddenEdge = null;
			hiddenWhatEdge = null;
			hiddenWhoEdge = null;
			hiddenCountEdge = null;
			hiddenRotationEdge = null;
			hiddenValueEdge = null;
			hiddenStateEdge = null;
		}

		this.whoEdge = (other.whoEdge == null) ? null : other.whoEdge.clone();
		this.whatEdge = (other.whatEdge == null) ? null : other.whatEdge.clone();
		this.countEdge = (other.countEdge == null) ? null : other.countEdge.clone();
		this.stateEdge = (other.stateEdge == null) ? null : other.stateEdge.clone();
		this.rotationEdge = (other.rotationEdge == null) ? null : other.rotationEdge.clone();
		this.valueEdge = (other.valueEdge == null) ? null : other.valueEdge.clone();
		this.emptyEdge = (other.emptyEdge == null) ? null : new Region(other.emptyEdge);
	}

	@Override
	public ContainerFlatEdgeState deepClone()
	{
		return new ContainerFlatEdgeState(this);
	}

	//-------------------------------------------------------------------------

	@Override
	protected long calcCanonicalHash(final int[] siteRemap, final int[] edgeRemap, final int[] vertexRemap,
			final int[] playerRemap, final boolean whoOnly)
	{
		long hash = 0;

		if (vertexRemap != null && vertexRemap.length > 0)
		{
			if (whoEdge != null)
				hash ^= whoEdge.calculateHashAfterRemap(vertexRemap, playerRemap);
			if (!whoOnly)
			{
				if (whatEdge != null)
					hash ^= whatEdge.calculateHashAfterRemap(vertexRemap, null);
				if (countEdge != null)
					hash ^= countEdge.calculateHashAfterRemap(vertexRemap, null);
				if (stateEdge != null)
					hash ^= stateEdge.calculateHashAfterRemap(vertexRemap, null);
				if (rotationEdge != null)
					hash ^= rotationEdge.calculateHashAfterRemap(vertexRemap, null);
				if (hiddenEdge != null)
				{
					for (int i = 1; i < hiddenEdge.length; i++)
						hash ^= hiddenEdge[i].calculateHashAfterRemap(siteRemap, false);
				}
				if (hiddenWhatEdge != null)
				{
					for (int i = 1; i < hiddenWhatEdge.length; i++)
						hash ^= hiddenWhatEdge[i].calculateHashAfterRemap(siteRemap, false);
				}
				if (hiddenWhoEdge != null)
				{
					for (int i = 1; i < hiddenWhoEdge.length; i++)
						hash ^= hiddenWhoEdge[i].calculateHashAfterRemap(siteRemap, false);
				}
				if (hiddenCountEdge != null)
				{
					for (int i = 1; i < hiddenCountEdge.length; i++)
						hash ^= hiddenCountEdge[i].calculateHashAfterRemap(siteRemap, false);
				}
				if (hiddenRotationEdge != null)
				{
					for (int i = 1; i < hiddenRotationEdge.length; i++)
						hash ^= hiddenRotationEdge[i].calculateHashAfterRemap(siteRemap, false);
				}
				if (hiddenValueEdge != null)
				{
					for (int i = 1; i < hiddenValueEdge.length; i++)
						hash ^= hiddenValueEdge[i].calculateHashAfterRemap(siteRemap, false);
				}
				if (hiddenStateEdge != null)
				{
					for (int i = 1; i < hiddenStateEdge.length; i++)
						hash ^= hiddenStateEdge[i].calculateHashAfterRemap(siteRemap, false);
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

		final int numEdge = game.board().topology().edges().size();
		super.reset(trialState, game);

		if (whoEdge != null)
			whoEdge.clear(trialState);

		if (whatEdge != null)
			whatEdge.clear(trialState);

		if (countEdge != null)
			countEdge.clear(trialState);

		if (stateEdge != null)
			stateEdge.clear(trialState);

		if (rotationEdge != null)
			rotationEdge.clear(trialState);

		if (valueEdge != null)
			valueEdge.clear(trialState);

		if (hiddenEdge != null)
			for (int i = 1; i < hiddenEdge.length; i++)
				hiddenEdge[i].clear(trialState);
		if (hiddenWhatEdge != null)
			for (int i = 1; i < hiddenWhatEdge.length; i++)
				hiddenWhatEdge[i].clear(trialState);
		if (hiddenWhoEdge != null)
			for (int i = 1; i < hiddenWhoEdge.length; i++)
				hiddenWhoEdge[i].clear(trialState);
		if (hiddenCountEdge != null)
			for (int i = 1; i < hiddenCountEdge.length; i++)
				hiddenCountEdge[i].clear(trialState);
		if (hiddenRotationEdge != null)
			for (int i = 1; i < hiddenRotationEdge.length; i++)
				hiddenRotationEdge[i].clear(trialState);
		if (hiddenValueEdge != null)
			for (int i = 1; i < hiddenValueEdge.length; i++)
				hiddenValueEdge[i].clear(trialState);
		if (hiddenStateEdge != null)
			for (int i = 1; i < hiddenStateEdge.length; i++)
				hiddenStateEdge[i].clear(trialState);

		if (emptyEdge != null)
			emptyEdge.set(numEdge);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isHidden(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenEdge == null)
			return false;

		if (player < 1 || player > (hiddenEdge.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (hidden ...) in the containerState. Player =  "
							+ player);

		return this.hiddenEdge[player].get(site);
	}

	@Override
	public boolean isHiddenWhat(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenWhatEdge == null)
			return false;

		if (player < 1 || player > (hiddenWhatEdge.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (hiddenWhat ...) in the containerState. Player =  "
							+ player);

		return this.hiddenWhatEdge[player].get(site);
	}

	@Override
	public boolean isHiddenWho(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenWhoEdge == null)
			return false;

		if (player < 1 || player > (hiddenWhoEdge.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (hiddenWho ...) in the containerState. Player =  "
							+ player);

		return this.hiddenWhoEdge[player].get(site);
	}

	@Override
	public boolean isHiddenState(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenStateEdge == null)
			return false;

		if (player < 1 || player > (hiddenStateEdge.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (hiddenState ...) in the containerState. Player =  "
							+ player);

		return this.hiddenStateEdge[player].get(site);
	}

	@Override
	public boolean isHiddenRotation(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenRotationEdge == null)
			return false;

		if (player < 1 || player > (hiddenRotationEdge.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (hiddenRotation ...) in the containerState. Player =  "
							+ player);

		return this.hiddenRotationEdge[player].get(site);
	}

	@Override
	public boolean isHiddenValue(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenValueEdge == null)
			return false;

		if (player < 1 || player > (hiddenValueEdge.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (hiddenValue ...) in the containerState. Player =  "
							+ player);

		return this.hiddenValueEdge[player].get(site);
	}

	@Override
	public boolean isHiddenCount(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenCountEdge == null)
			return false;

		if (player < 1 || player > (hiddenCountEdge.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (hiddenCount ...) in the containerState. Player =  "
							+ player);

		return this.hiddenCountEdge[player].get(site);
	}

	@Override
	public void setHidden(final State state, final int player, final int site, final int level, final SiteType type,
			final boolean on)
	{
		if (hiddenEdge == null)
			throw new UnsupportedOperationException("No Hidden information, but the method (setHidden ...) was called");

		if (player < 1 || player > (hiddenEdge.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHidden ...) in the containerState. Player =  "
							+ player);

		this.hiddenEdge[player].set(state, site, on);
	}

	@Override
	public void setHiddenWhat(final State state, final int player, final int site, final int level, final SiteType type,
			final boolean on)
	{
		if (hiddenWhatEdge == null)
			throw new UnsupportedOperationException(
					"No Hidden information, but the method (setHiddenWhat ...) was called");

		if (player < 1 || player > (hiddenWhatEdge.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHiddenWhat ...) in the containerState. Player =  "
							+ player);

		this.hiddenWhatEdge[player].set(state, site, on);
	}

	@Override
	public void setHiddenWho(final State state, final int player, final int site, final int level, final SiteType type,
			final boolean on)
	{
		if (hiddenWhoEdge == null)
			throw new UnsupportedOperationException(
					"No Hidden information, but the method (setHiddenWho ...) was called");

		if (player < 1 || player > (hiddenWhoEdge.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHiddenWho ...) in the containerState. Player =  "
							+ player);

		this.hiddenWhoEdge[player].set(state, site, on);
	}

	@Override
	public void setHiddenState(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (hiddenStateEdge == null)
			throw new UnsupportedOperationException(
					"No Hidden information, but the method (setHiddenState ...) was called");

		if (player < 1 || player > (hiddenStateEdge.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHiddenState ...) in the containerState. Player =  "
							+ player);

		this.hiddenStateEdge[player].set(state, site, on);
	}

	@Override
	public void setHiddenRotation(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (hiddenRotationEdge == null)
			throw new UnsupportedOperationException(
					"No Hidden information, but the method (setHiddenRotation ...) was called");

		if (player < 1 || player > (hiddenRotationEdge.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHiddenRotation ...) in the containerState. Player =  "
							+ player);

		this.hiddenRotationEdge[player].set(state, site, on);
	}

	@Override
	public void setHiddenValue(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (hiddenValueEdge == null)
			throw new UnsupportedOperationException(
					"No Hidden information, but the method (setHiddenValue ...) was called");

		if (player < 1 || player > (hiddenValueEdge.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHiddenValue ...) in the containerState. Player =  "
							+ player);

		this.hiddenValueEdge[player].set(state, site, on);
	}

	@Override
	public void setHiddenCount(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (hiddenCountEdge == null)
			throw new UnsupportedOperationException(
					"No Hidden information, but the method (setHiddenCount ...) was called");

		if (player < 1 || player > (hiddenCountEdge.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHiddenCount ...) in the containerState. Player =  "
							+ player);

		this.hiddenCountEdge[player].set(state, site, on);
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
		return countEdge(site) != 0;
	}

	//-------------------------------------------------------------------------
	@Override
	public void setSite(final State trialState, final int site, final int whoVal, final int whatVal, final int countVal,
			final int stateVal, final int rotationVal, final int valueVal, final SiteType type)
	{
		final boolean wasEmpty = !isOccupied(site);

		if (whoVal != Constants.UNDEFINED)
			whoEdge.setChunk(trialState, site, whoVal);

		if (whatVal != Constants.UNDEFINED)
			defaultIfNull(whatEdge).setChunk(trialState, site, whatVal);

		if (countVal != Constants.UNDEFINED)
		{
			if (countEdge != null)
				countEdge.setChunk(trialState, site, (countVal < 0 ? 0 : countVal));
			else if (countEdge == null && countVal > 1)
				throw new UnsupportedOperationException(
						"This game does not support counts, but a count > 1 has been set. countVal=" + countVal);
		}

		if (stateVal != Constants.UNDEFINED)
		{
			if (stateEdge != null)
				stateEdge.setChunk(trialState, site, stateVal);
			else if (stateVal != 0)
				throw new UnsupportedOperationException(
						"This game does not support states, but a state has been set. stateVal=" + stateVal);
		}

		if (rotationVal != Constants.UNDEFINED)
		{
			if (rotationEdge != null)
				rotationEdge.setChunk(trialState, site, rotationVal);
			else if (rotationVal != 0)
				throw new UnsupportedOperationException(
						"This game does not support rotations, but a rotation has been set. rotationVal="
								+ rotationVal);
		}

		if (valueVal != Constants.UNDEFINED)
		{
			if (valueEdge != null)
				valueEdge.setChunk(trialState, site, valueVal);
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
		result = prime * result + ((hiddenEdge == null) ? 0 : hiddenEdge.hashCode());
		result = prime * result + ((hiddenWhatEdge == null) ? 0 : hiddenWhatEdge.hashCode());
		result = prime * result + ((hiddenWhoEdge == null) ? 0 : hiddenWhoEdge.hashCode());
		result = prime * result + ((hiddenStateEdge == null) ? 0 : hiddenStateEdge.hashCode());
		result = prime * result + ((hiddenRotationEdge == null) ? 0 : hiddenRotationEdge.hashCode());
		result = prime * result + ((hiddenValueEdge == null) ? 0 : hiddenValueEdge.hashCode());
		result = prime * result + ((hiddenStateEdge == null) ? 0 : hiddenStateEdge.hashCode());
		result = prime * result + ((whoEdge == null) ? 0 : whoEdge.hashCode());
		result = prime * result + ((countEdge == null) ? 0 : countEdge.hashCode());
		result = prime * result + ((whatEdge == null) ? 0 : whatEdge.hashCode());
		result = prime * result + ((stateEdge == null) ? 0 : stateEdge.hashCode());
		result = prime * result + ((rotationEdge == null) ? 0 : rotationEdge.hashCode());
		result = prime * result + ((valueEdge == null) ? 0 : valueEdge.hashCode());

		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (!(obj instanceof ContainerFlatEdgeState))
			return false;

		if (!super.equals(obj))
			return false;

		final ContainerFlatEdgeState other = (ContainerFlatEdgeState) obj;

		if (hiddenEdge != null)
		{
			for (int i = 1; i < hiddenEdge.length; i++)
				if (!bitSetsEqual(hiddenEdge[i], other.hiddenEdge[i]))
					return false;
		}
		if (hiddenWhatEdge != null)
		{
			for (int i = 1; i < hiddenWhatEdge.length; i++)
				if (!bitSetsEqual(hiddenWhatEdge[i], other.hiddenWhatEdge[i]))
					return false;
		}
		if (hiddenWhoEdge != null)
		{
			for (int i = 1; i < hiddenWhoEdge.length; i++)
				if (!bitSetsEqual(hiddenWhoEdge[i], other.hiddenWhoEdge[i]))
					return false;
		}
		if (hiddenRotationEdge != null)
		{
			for (int i = 1; i < hiddenRotationEdge.length; i++)
				if (!bitSetsEqual(hiddenRotationEdge[i], other.hiddenRotationEdge[i]))
					return false;
		}
		if (hiddenStateEdge != null)
		{
			for (int i = 1; i < hiddenStateEdge.length; i++)
				if (!bitSetsEqual(hiddenStateEdge[i], other.hiddenStateEdge[i]))
					return false;
		}
		if (hiddenValueEdge != null)
		{
			for (int i = 1; i < hiddenValueEdge.length; i++)
				if (!bitSetsEqual(hiddenValueEdge[i], other.hiddenValueEdge[i]))
					return false;
		}
		if (hiddenValueEdge != null)
		{
			for (int i = 1; i < hiddenValueEdge.length; i++)
				if (!bitSetsEqual(hiddenValueEdge[i], other.hiddenValueEdge[i]))
					return false;
		}
		if (hiddenCountEdge != null)
		{
			for (int i = 1; i < hiddenCountEdge.length; i++)
				if (!bitSetsEqual(hiddenCountEdge[i], other.hiddenCountEdge[i]))
					return false;
		}

		if (!chunkSetsEqual(whoEdge, other.whoEdge))
			return false;
		if (!chunkSetsEqual(countEdge, other.countEdge))
			return false;
		if (!chunkSetsEqual(whatEdge, other.whatEdge))
			return false;
		if (!chunkSetsEqual(stateEdge, other.stateEdge))
			return false;
		if (!chunkSetsEqual(rotationEdge, other.rotationEdge))
			return false;
		if (!chunkSetsEqual(valueEdge, other.valueEdge))
			return false;

		return true;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("ContainerState type = " + this.getClass() + "\n");

		if (emptyChunkSetEdge() != null)
			sb.append("Empty = " + emptyChunkSetEdge().toChunkString() + "\n");

		if (whoEdge != null)
			sb.append("Who = " + cloneWhoEdge().toChunkString() + "\n");

		if (whatEdge != null)
			sb.append("What" + cloneWhatEdge().toChunkString() + "\n");

		if (stateEdge != null)
			sb.append("State = " + stateEdge.internalStateCopy().toChunkString() + "\n");

		if (rotationEdge != null)
			sb.append("Rotation = " + rotationEdge.internalStateCopy().toChunkString() + "\n");

		if (valueEdge != null)
			sb.append("value = " + valueEdge.internalStateCopy().toChunkString() + "\n");

		if (countEdge != null)
			sb.append("Count = " + countEdge.internalStateCopy().toChunkString() + "\n");

		if (hiddenEdge != null)
		{
			for (int i = 1; i < hiddenEdge.length; i++)
				sb.append(
						"Hidden for " + " player " + i + " = " + hiddenEdge[i].internalStateCopy().toString() + "\n");
		}
		if (hiddenWhatEdge != null)
		{
			for (int i = 1; i < hiddenWhatEdge.length; i++)
				sb.append("Hidden What for " + " player " + i + " = "
						+ hiddenWhatEdge[i].internalStateCopy().toString() + "\n");
		}
		if (hiddenWhoEdge != null)
		{
			for (int i = 1; i < hiddenWhoEdge.length; i++)
				sb.append("Hidden Who for " + " player " + i + " = " + hiddenWhoEdge[i].internalStateCopy().toString()
						+ "\n");
		}
		if (hiddenCountEdge != null)
		{
			for (int i = 1; i < hiddenCountEdge.length; i++)
				sb.append("Hidden Count for " + " player " + i + " = "
						+ hiddenCountEdge[i].internalStateCopy().toString() + "\n");
		}
		if (hiddenValueEdge != null)
		{
			for (int i = 1; i < hiddenValueEdge.length; i++)
				sb.append("Hidden Value for " + " player " + i + " = "
						+ hiddenValueEdge[i].internalStateCopy().toString() + "\n");
		}
		if (hiddenStateEdge != null)
		{
			for (int i = 1; i < hiddenStateEdge.length; i++)
				sb.append("Hidden State for " + " player " + i + " = "
						+ hiddenStateEdge[i].internalStateCopy().toString() + "\n");
		}
		if (hiddenRotationEdge != null)
		{
			for (int i = 1; i < hiddenRotationEdge.length; i++)
				sb.append("Hidden Rotation for " + " player " + i + " = "
						+ hiddenRotationEdge[i].internalStateCopy().toString() + "\n");
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
		return whoEdge.getChunk(edge);
	}

	@Override
	public int whoVertex(final int vertex)
	{
		return 0;
	}

	@Override
	public int whatEdge(int site)
	{
		return whatEdge.getChunk(site);
	}

	@Override
	public int countEdge(int site)
	{
		if (countEdge != null)
			return countEdge.getChunk(site);

		if (whoEdge.getChunk(site) != 0 || whatEdge != null && whatEdge.getChunk(site) != 0)
			return 1;

		return 0;
	}

	@Override
	public int stateEdge(int site)
	{
		if (stateEdge == null)
			return 0;

		return stateEdge.getChunk(site);
	}

	@Override
	public int rotationEdge(int site)
	{
		if (rotationEdge == null)
			return 0;

		return rotationEdge.getChunk(site);
	}

	@Override
	public int whatVertex(int site)
	{
		return 0;
	}

	@Override
	public int countVertex(int site)
	{
		return 0;
	}

	@Override
	public int stateVertex(int site)
	{
		return 0;
	}

	@Override
	public int rotationVertex(int site)
	{
		return 0;
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
		return remove(trialState, site, SiteType.Edge);
	}

	@Override
	public void setSite(State trialState, int site, int level, int whoVal, int whatVal, int countVal, int stateVal,
			int rotationVal, int valueVal)
	{
		setSite(trialState, site, whoVal, whatVal, countVal, stateVal, rotationVal, valueVal, SiteType.Edge);
	}

	@Override
	public int whoVertex(int site, int level)
	{
		return 0;
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
	public int whoEdge(int site, int level)
	{
		return whoEdge(site);
	}

	@Override
	public int whatEdge(int site, int level)
	{
		return whatEdge(site);
	}

	@Override
	public int stateEdge(int site, int level)
	{
		return stateEdge(site);
	}

	@Override
	public int rotationEdge(int site, int level)
	{
		return rotationEdge(site);
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
		return new Sites(emptyEdge.sites());
	}

	@Override
	public int numEmpty()
	{
		return emptyEdge.count();
	}

	@Override
	public void addToEmpty(final int site, final SiteType graphType)
	{
		emptyEdge.add(site);
	}

	@Override
	public void removeFromEmpty(final int site, final SiteType graphType)
	{
		emptyEdge.remove(site);
	}

	@Override
	public boolean isEmptyVertex(final int vertex)
	{
		return true;
	}

	@Override
	public boolean isEmptyEdge(final int edge)
	{
		return emptyEdge.contains(edge);
	}

	@Override
	public boolean isEmptyCell(final int site)
	{
		return true;
	}

	@Override
	public Region emptyRegion(final SiteType type)
	{
		return emptyEdge;
	}

	@Override
	public void addToEmptyCell(final int site)
	{
		emptyEdge.add(site);
	}

	@Override
	public void removeFromEmptyCell(final int site)
	{
		emptyEdge.remove(site);
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
		return whoEdge;
	}

	@Override
	public ChunkSet emptyChunkSetCell()
	{
		return null;
	}

	@Override
	public ChunkSet emptyChunkSetVertex()
	{
		return null;
	}

	@Override
	public ChunkSet emptyChunkSetEdge()
	{
		return emptyEdge.bitSet();
	}

	@Override
	public int numChunksWhoCell()
	{
		return Constants.UNDEFINED;
	}

	@Override
	public int numChunksWhoVertex()
	{
		return Constants.UNDEFINED;
	}

	@Override
	public int numChunksWhoEdge()
	{
		return whoEdge.numChunks();
	}

	@Override
	public int chunkSizeWhoCell()
	{
		return Constants.UNDEFINED;
	}

	@Override
	public int chunkSizeWhoVertex()
	{
		return Constants.UNDEFINED;
	}

	@Override
	public int chunkSizeWhoEdge()
	{
		return whoEdge.chunkSize();
	}

	@Override
	public int numChunksWhatCell()
	{
		return Constants.UNDEFINED;
	}

	@Override
	public int numChunksWhatVertex()
	{
		return Constants.UNDEFINED;
	}

	@Override
	public int numChunksWhatEdge()
	{
		return defaultIfNull(whatEdge).numChunks();
	}

	@Override
	public int chunkSizeWhatCell()
	{
		return Constants.UNDEFINED;
	}

	@Override
	public int chunkSizeWhatVertex()
	{
		return Constants.UNDEFINED;
	}

	@Override
	public int chunkSizeWhatEdge()
	{
		return defaultIfNull(whatEdge).chunkSize();
	}

	@Override
	public boolean matchesWhoCell(final ChunkSet mask, final ChunkSet pattern)
	{
		return false;
	}

	@Override
	public boolean matchesWhoVertex(final ChunkSet mask, final ChunkSet pattern)
	{
		return false;
	}

	@Override
	public boolean matchesWhoEdge(final ChunkSet mask, final ChunkSet pattern)
	{
		return whoEdge.matches(mask, pattern);
	}

	@Override
	public boolean violatesNotWhoCell(final ChunkSet mask, final ChunkSet pattern)
	{
		return false;
	}

	@Override
	public boolean violatesNotWhoVertex(final ChunkSet mask, final ChunkSet pattern)
	{
		return false;
	}

	@Override
	public boolean violatesNotWhoEdge(final ChunkSet mask, final ChunkSet pattern)
	{
		return whoEdge.violatesNot(mask, pattern);
	}

	@Override
	public boolean violatesNotWhoCell(final ChunkSet mask, final ChunkSet pattern, final int startWord)
	{
		return false;
	}

	@Override
	public boolean violatesNotWhoVertex(final ChunkSet mask, final ChunkSet pattern, final int startWord)
	{
		return false;
	}

	@Override
	public boolean violatesNotWhoEdge(final ChunkSet mask, final ChunkSet pattern, final int startWord)
	{
		return whoEdge.violatesNot(mask, pattern, startWord);
	}

	@Override
	public boolean matchesWhatCell(final ChunkSet mask, final ChunkSet pattern)
	{
		return false;
	}

	@Override
	public boolean matchesWhatVertex(final ChunkSet mask, final ChunkSet pattern)
	{
		return false;
	}

	@Override
	public boolean matchesWhatEdge(final ChunkSet mask, final ChunkSet pattern)
	{
		return defaultIfNull(whatEdge).matches(mask, pattern);
	}
	
	@Override
	public boolean matchesWhoEdge(final int wordIdx, final long mask, final long matchingWord)
	{
		return whoEdge.matches(wordIdx, mask, matchingWord);
	}

	@Override public boolean matchesWhoVertex(final int wordIdx, final long mask, final long matchingWord) { return false; }
	@Override public boolean matchesWhoCell(final int wordIdx, final long mask, final long matchingWord) { return false; }
	
	@Override 
	public boolean matchesWhatEdge(final int wordIdx, final long mask, final long matchingWord) 
	{ 
		return defaultIfNull(whatEdge).matches(wordIdx, mask, matchingWord);
	}

	@Override public boolean matchesWhatVertex(final int wordIdx, final long mask, final long matchingWord) { return false; }
	@Override public boolean matchesWhatCell(final int wordIdx, final long mask, final long matchingWord) { return false; }

	@Override
	public boolean violatesNotWhatCell(final ChunkSet mask, final ChunkSet pattern)
	{
		return false;
	}

	@Override
	public boolean violatesNotWhatVertex(final ChunkSet mask, final ChunkSet pattern)
	{
		return false;
	}

	@Override
	public boolean violatesNotWhatEdge(final ChunkSet mask, final ChunkSet pattern)
	{
		return defaultIfNull(whatEdge).violatesNot(mask, pattern);
	}

	@Override
	public boolean violatesNotWhatCell(final ChunkSet mask, final ChunkSet pattern, final int startWord)
	{
		return false;
	}

	@Override
	public boolean violatesNotWhatVertex(final ChunkSet mask, final ChunkSet pattern, final int startWord)
	{
		return false;
	}

	@Override
	public boolean violatesNotWhatEdge(final ChunkSet mask, final ChunkSet pattern, final int startWord)
	{
		return defaultIfNull(whatEdge).violatesNot(mask, pattern, startWord);
	}

	@Override
	public ChunkSet cloneWhoCell()
	{
		return null;
	}

	@Override
	public ChunkSet cloneWhoVertex()
	{
		return null;
	}

	@Override
	public ChunkSet cloneWhoEdge()
	{
		return whoEdge.internalStateCopy();
	}

	@Override
	public ChunkSet cloneWhatCell()
	{
		return null;
	}

	@Override
	public ChunkSet cloneWhatVertex()
	{
		return null;
	}

	@Override
	public ChunkSet cloneWhatEdge()
	{
		return defaultIfNull(whatEdge).internalStateCopy();
	}

	@Override
	public int valueCell(int site, int level)
	{
		return valueCell(site);
	}

	@Override
	public int valueVertex(int site)
	{
		return 0;
	}

	@Override
	public int valueVertex(int site, int level)
	{
		return 0;
	}

	@Override
	public int valueEdge(int site)
	{
		if (valueEdge == null)
			return 0;

		return valueEdge(site);
	}

	@Override
	public int valueEdge(int site, int level)
	{
		if (valueEdge == null)
			return 0;

		return valueEdge(site);
	}
	
	//-------------------------------------------------------------------------
	// Optimised versions of methods with graph element types; we only support
	// Edge anyway, so just assume we were passed Edge as type
	
	@Override
	public int what(final int site, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Edge)
			return 0;
		
		return whatEdge(site);
	}

	@Override
	public int who(final int site, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Edge)
			return 0;
		
		return whoEdge(site);
	}

	@Override
	public int count(final int site, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Edge)
			return 0;
		
		return countEdge(site);
	}

	@Override
	public int sizeStack(final int site, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Edge)
			return 0;
		
		return sizeStackEdge(site);

	}

	@Override
	public int state(final int site, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Edge)
			return 0;
		
		return stateEdge(site);
	}

	@Override
	public int rotation(final int site, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Edge)
			return 0;
		
		return rotationEdge(site);
	}

	@Override
	public int value(final int site, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Edge)
			return 0;
		
		return valueEdge(site);
	}

	//-------------------Methods with levels---------------------

	@Override
	public int what(final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Edge)
			return 0;
		
		return whatEdge(site, level);
	}

	@Override
	public int who(final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Edge)
			return 0;
		
		return whoEdge(site, level);
	}

	@Override
	public int state(final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Edge)
			return 0;
		
		return stateEdge(site, level);
	}

	@Override
	public int rotation(final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Edge)
			return 0;
		
		return rotationEdge(site, level);
	}
	
	@Override
	public int value(final int site, final int level, final SiteType graphElementType)
	{
		if (graphElementType != SiteType.Edge)
			return 0;
		
		return valueEdge(site, level);
	}
	
	//-------------------------------------------------------------------------
}
