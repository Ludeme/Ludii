package other.state.container;

import game.Game;
import game.equipment.container.Container;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.equipment.Region;
import main.Constants;
import main.collections.ChunkSet;
import other.state.State;
import other.state.zhash.HashedBitSet;
import other.state.zhash.HashedChunkSet;
import other.state.zhash.ZobristHashGenerator;

/**
 * The state of a container corresponding to a graph.
 * 
 * @author Eric.Piette
 */
public class ContainerGraphState extends ContainerFlatState
{
	private static final long serialVersionUID = 1L;

	//-----------------------------------------------------------------------------

	/** Owner of an edge. */
	private final HashedChunkSet whoEdge;

	/** Owner of a vertex. */
	private final HashedChunkSet whoVertex;

	/** Type of Item on an edge. */
	private final HashedChunkSet whatEdge;

	/** Type of Item on a vertex. */
	private final HashedChunkSet whatVertex;

	/** Count on an edge. */
	private final HashedChunkSet countEdge;

	/** Count of a vertex. */
	private final HashedChunkSet countVertex;

	/** State of an edge. */
	private final HashedChunkSet stateEdge;

	/** State of a vertex. */
	private final HashedChunkSet stateVertex;

	/** Rotation of an edge. */
	private final HashedChunkSet rotationEdge;

	/** Rotation of a vertex. */
	private final HashedChunkSet rotationVertex;

	/** Value of the piece on a vertex. */
	private final HashedChunkSet valueVertex;

	/** Value of the piece on an edge. */
	private final HashedChunkSet valueEdge;

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

	/** Which vertex slots are empty. */
	private final Region emptyVertex;

	/**
	 * @param generator      The generator.
	 * @param game           The game.
	 * @param container      The container.
	 * @param maxWhatVal     The max what value.
	 * @param maxStateVal    The max state value.
	 * @param maxCountVal    The max count value.
	 * @param maxRotationVal The max rotation value.
	 * @param maxPieceValue  The max piece value.
	 */
	public ContainerGraphState
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
		super(generator, game, container, container.numSites(), maxWhatVal, maxStateVal, maxCountVal, maxRotationVal,
				maxPieceValue);

		final int numEdges = game.board().topology().edges().size();
		final int numVertices = game.board().topology().vertices().size();
		final int numPlayers = game.players().count();

		if ((game.gameFlags() & GameType.HiddenInfo) == 0L)
		{
			hiddenEdge = null;
			hiddenWhatEdge = null;
			hiddenWhoEdge = null;
			hiddenRotationEdge = null;
			hiddenCountEdge = null;
			hiddenValueEdge = null;
			hiddenStateEdge = null;
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

			this.whoVertex = new HashedChunkSet(generator, numPlayers + 1, numVertices);
			whatVertex = maxWhatVal > 0 ? new HashedChunkSet(generator, maxWhatVal, numVertices) : null;
			countVertex = maxCountVal > 0 ? new HashedChunkSet(generator, maxCountVal, numVertices) : null;
			stateVertex = maxStateVal > 0 ? new HashedChunkSet(generator, maxStateVal, numVertices) : null;
			rotationVertex = maxRotationVal > 0 ? new HashedChunkSet(generator, maxRotationVal, numVertices) : null;
			valueVertex = maxPieceValue > 0 ? new HashedChunkSet(generator, maxPieceValue, numVertices) : null;

			this.emptyVertex = new Region(numVertices);

			this.whoEdge = new HashedChunkSet(generator, numPlayers + 1, numEdges);
			whatEdge = maxWhatVal > 0 ? new HashedChunkSet(generator, maxWhatVal, numEdges) : null;
			rotationEdge = maxRotationVal > 0 ? new HashedChunkSet(generator, maxRotationVal, numEdges) : null;
			countEdge = maxCountVal > 0 ? new HashedChunkSet(generator, maxCountVal, numEdges) : null;
			stateEdge = maxStateVal > 0 ? new HashedChunkSet(generator, maxStateVal, numEdges) : null;
			valueEdge = maxPieceValue > 0 ? new HashedChunkSet(generator, maxPieceValue, numEdges) : null;
			this.emptyEdge = new Region(numEdges);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 */
	private ContainerGraphState(final ContainerGraphState other)
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

		this.whoEdge   = (other.whoEdge == null) ? null : other.whoEdge.clone();
		this.whoVertex = (other.whoVertex == null) ? null : other.whoVertex.clone();

		this.whatEdge   = (other.whatEdge == null) ? null : other.whatEdge.clone();
		this.whatVertex = (other.whatVertex == null) ? null : other.whatVertex.clone();

		this.countEdge   = (other.countEdge == null) ? null : other.countEdge.clone();
		this.countVertex = (other.countVertex == null) ? null : other.countVertex.clone();

		this.stateEdge   = (other.stateEdge == null) ? null : other.stateEdge.clone();
		this.stateVertex = (other.stateVertex == null) ? null : other.stateVertex.clone();

		this.rotationEdge   = (other.rotationEdge == null) ? null : other.rotationEdge.clone();
		this.rotationVertex = (other.rotationVertex == null) ? null : other.rotationVertex.clone();

		this.valueVertex = (other.valueVertex == null) ? null : other.valueVertex.clone();
		this.valueEdge = (other.valueEdge == null) ? null : other.valueEdge.clone();

		this.emptyEdge = (other.emptyEdge == null) ? null : new Region(other.emptyEdge);
		this.emptyVertex = (other.emptyVertex == null) ? null : new Region(other.emptyVertex);
	}

	@Override
	public ContainerGraphState deepClone()
	{
		return new ContainerGraphState(this);
	}

	@Override
	public void reset(final State trialState, final Game game)
	{
		final int numEdges = game.board().topology().edges().size();
		final int numVertices = game.board().topology().vertices().size();
		super.reset(trialState, game);

		if (whoEdge != null)
			whoEdge.clear(trialState);
		if (whoVertex != null)
			whoVertex.clear(trialState);

		if (whatEdge != null)
			whatEdge.clear(trialState);
		if (whatVertex != null)
			whatVertex.clear(trialState);

		if (countEdge != null)
			countEdge.clear(trialState);
		if (countVertex != null)
			countVertex.clear(trialState);

		if (stateEdge != null)
			stateEdge.clear(trialState);
		if (stateVertex != null)
			stateVertex.clear(trialState);

		if (rotationEdge != null)
			rotationEdge.clear(trialState);
		if (rotationVertex != null)
			rotationVertex.clear(trialState);

		if (valueVertex != null)
			valueVertex.clear(trialState);
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

		if (emptyEdge != null)
			emptyEdge.set(numEdges);
		if (emptyVertex != null)
			emptyVertex.set(numVertices);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isHidden(final int player, final int site, final int level, final SiteType type)
	{
		if (type == SiteType.Cell || container().index() != 0 || type == null)
			return super.isHidden(player, site, level, type);
		else if (type == SiteType.Edge)
		{
			if (hiddenEdge == null)
				return false;

			if (player < 1 || player > (hiddenEdge.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (hidden ...) in the containerState. Player =  "
								+ player);

			return this.hiddenEdge[player].get(site - offset);
		}
		else
		{
			if (hiddenVertex == null)
				return false;

			if (player < 1 || player > (hiddenVertex.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (hidden ...) in the containerState. Player =  "
								+ player);

			return this.hiddenVertex[player].get(site - offset);
		}
	}

	@Override
	public boolean isHiddenWhat(final int player, final int site, final int level, final SiteType type)
	{
		if (type == SiteType.Cell || container().index() != 0 || type == null)
			return super.isHiddenWhat(player, site, level, type);
		else if (type == SiteType.Edge)
		{
			if (hiddenWhatEdge == null)
				return false;

			if (player < 1 || player > (hiddenWhatEdge.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (hiddenWhat ...) in the containerState. Player =  "
								+ player);

			return this.hiddenWhatEdge[player].get(site - offset);
		}
		else
		{
			if (hiddenWhatVertex == null)
				return false;

			if (player < 1 || player > (hiddenWhatVertex.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (hiddenWhat ...) in the containerState. Player =  "
								+ player);

			return this.hiddenWhatVertex[player].get(site - offset);
		}
	}

	@Override
	public boolean isHiddenWho(final int player, final int site, final int level, final SiteType type)
	{
		if (type == SiteType.Cell || container().index() != 0 || type == null)
			return super.isHiddenWho(player, site, level, type);
		else if (type == SiteType.Edge)
		{
			if (hiddenWhoEdge == null)
				return false;

			if (player < 1 || player > (hiddenWhoEdge.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (hiddenWho ...) in the containerState. Player =  "
								+ player);

			return this.hiddenWhoEdge[player].get(site - offset);
		}
		else
		{
			if (hiddenWhoVertex == null)
				return false;

			if (player < 1 || player > (hiddenWhoVertex.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (hiddenWho ...) in the containerState. Player =  "
								+ player);

			return this.hiddenWhoVertex[player].get(site - offset);
		}
	}

	@Override
	public boolean isHiddenState(final int player, final int site, final int level, final SiteType type)
	{
		if (type == SiteType.Cell || container().index() != 0 || type == null)
			return super.isHiddenState(player, site, level, type);
		else if (type == SiteType.Edge)
		{
			if (hiddenStateEdge == null)
				return false;

			if (player < 1 || player > (hiddenStateEdge.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (hiddenState ...) in the containerState. Player =  "
								+ player);

			return this.hiddenStateEdge[player].get(site - offset);
		}
		else
		{
			if (hiddenStateVertex == null)
				return false;

			if (player < 1 || player > (hiddenStateVertex.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (hiddenState ...) in the containerState. Player =  "
								+ player);

			return this.hiddenStateVertex[player].get(site - offset);
		}
	}

	@Override
	public boolean isHiddenRotation(final int player, final int site, final int level, final SiteType type)
	{
		if (type == SiteType.Cell || container().index() != 0 || type == null)
			return super.isHiddenRotation(player, site, level, type);
		else if (type == SiteType.Edge)
		{
			if (hiddenRotationEdge == null)
				return false;

			if (player < 1 || player > (hiddenRotationEdge.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (hiddenRotation ...) in the containerState. Player =  "
								+ player);

			return this.hiddenRotationEdge[player].get(site - offset);
		}
		else
		{
			if (hiddenRotationVertex == null)
				return false;

			if (player < 1 || player > (hiddenRotationVertex.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (hiddenRotation ...) in the containerState. Player =  "
								+ player);

			return this.hiddenRotationVertex[player].get(site - offset);
		}
	}

	@Override
	public boolean isHiddenValue(final int player, final int site, final int level, final SiteType type)
	{
		if (type == SiteType.Cell || container().index() != 0 || type == null)
			return super.isHiddenValue(player, site, level, type);
		else if (type == SiteType.Edge)
		{
			if (hiddenValueEdge == null)
				return false;

			if (player < 1 || player > (hiddenValueEdge.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (hiddenValue ...) in the containerState. Player =  "
								+ player);

			return this.hiddenValueEdge[player].get(site - offset);
		}
		else
		{
			if (hiddenValueVertex == null)
				return false;

			if (player < 1 || player > (hiddenValueVertex.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (hiddenValue ...) in the containerState. Player =  "
								+ player);

			return this.hiddenValueVertex[player].get(site - offset);
		}
	}

	@Override
	public boolean isHiddenCount(final int player, final int site, final int level, final SiteType type)
	{
		if (type == SiteType.Cell || container().index() != 0 || type == null)
			return super.isHiddenCount(player, site, level, type);
		else if (type == SiteType.Edge)
		{
			if (hiddenCountEdge == null)
				return false;

			if (player < 1 || player > (hiddenCountEdge.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (hiddenCount ...) in the containerState. Player =  "
								+ player);

			return this.hiddenCountEdge[player].get(site - offset);
		}
		else
		{
			if (hiddenCountVertex == null)
				return false;

			if (player < 1 || player > (hiddenCountVertex.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (hiddenCount ...) in the containerState. Player =  "
								+ player);

			return this.hiddenCountVertex[player].get(site - offset);
		}
	}

	@Override
	public void setHidden(final State state, final int player, final int site, final int level, final SiteType type,
			final boolean on)
	{
		if (type == SiteType.Cell || container().index() != 0 || type == null)
			super.setHidden(state, player, site, level, type, on);
		else if (type == SiteType.Edge)
		{
			if (hiddenEdge == null)
				throw new UnsupportedOperationException(
						"No Hidden information, but the method (setHidden ...) was called");

			if (player < 1 || player > (hiddenEdge.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (setHidden ...) in the containerState. Player =  "
								+ player);

			this.hiddenEdge[player].set(state, site - offset, on);
		}
		else
		{
			if (hiddenVertex == null)
				throw new UnsupportedOperationException(
						"No Hidden information, but the method (setHidden ...) was called");

			if (player < 1 || player > (hiddenVertex.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (setHidden ...) in the containerState. Player =  "
								+ player);

			this.hiddenVertex[player].set(state, site - offset, on);
		}
	}

	@Override
	public void setHiddenWhat(final State state, final int player, final int site, final int level, final SiteType type,
			final boolean on)
	{
		if (type == SiteType.Cell || container().index() != 0 || type == null)
			super.setHiddenWhat(state, player, site, level, type, on);
		else if (type == SiteType.Edge)
		{
			if (hiddenWhatEdge == null)
				throw new UnsupportedOperationException(
						"No Hidden information, but the method (setHiddenWhat ...) was called");

			if (player < 1 || player > (hiddenWhatEdge.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (setHiddenWhat ...) in the containerState. Player =  "
								+ player);

			this.hiddenWhatEdge[player].set(state, site - offset, on);
		}
		else
		{
			if (hiddenWhatVertex == null)
				throw new UnsupportedOperationException(
						"No Hidden information, but the method (setHiddenWhat ...) was called");

			if (player < 1 || player > (hiddenWhatVertex.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (setHiddenWhat ...) in the containerState. Player =  "
								+ player);

			this.hiddenWhatVertex[player].set(state, site - offset, on);
		}
	}

	@Override
	public void setHiddenWho(final State state, final int player, final int site, final int level, final SiteType type,
			final boolean on)
	{
		if (type == SiteType.Cell || container().index() != 0 || type == null)
			super.setHiddenWho(state, player, site, level, type, on);
		else if (type == SiteType.Edge)
		{
			if (hiddenWhoEdge == null)
				throw new UnsupportedOperationException(
						"No Hidden information, but the method (setHiddenWho ...) was called");

			if (player < 1 || player > (hiddenWhoEdge.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (setHiddenWho ...) in the containerState. Player =  "
								+ player);

			this.hiddenWhoEdge[player].set(state, site - offset, on);
		}
		else
		{
			if (hiddenWhoVertex == null)
				throw new UnsupportedOperationException(
						"No Hidden information, but the method (setHiddenWho ...) was called");

			if (player < 1 || player > (hiddenWhoVertex.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (setHiddenWho ...) in the containerState. Player =  "
								+ player);

			this.hiddenWhoVertex[player].set(state, site - offset, on);
		}
	}

	@Override
	public void setHiddenState(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (type == SiteType.Cell || container().index() != 0 || type == null)
			super.setHiddenState(state, player, site, level, type, on);
		else if (type == SiteType.Edge)
		{
			if (hiddenStateEdge == null)
				throw new UnsupportedOperationException(
						"No Hidden information, but the method (setHiddenState ...) was called");

			if (player < 1 || player > (hiddenStateEdge.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (setHiddenState ...) in the containerState. Player =  "
								+ player);

			this.hiddenStateEdge[player].set(state, site - offset, on);
		}
		else
		{
			if (hiddenStateVertex == null)
				throw new UnsupportedOperationException(
						"No Hidden information, but the method (setHiddenState ...) was called");

			if (player < 1 || player > (hiddenStateVertex.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (setHiddenState ...) in the containerState. Player =  "
								+ player);

			this.hiddenStateVertex[player].set(state, site - offset, on);
		}
	}

	@Override
	public void setHiddenRotation(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (type == SiteType.Cell || container().index() != 0 || type == null)
			super.setHiddenRotation(state, player, site, level, type, on);
		else if (type == SiteType.Edge)
		{
			if (hiddenRotationEdge == null)
				throw new UnsupportedOperationException(
						"No Hidden information, but the method (setHiddenRotation ...) was called");

			if (player < 1 || player > (hiddenRotationEdge.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (setHiddenRotation ...) in the containerState. Player =  "
								+ player);

			this.hiddenRotationEdge[player].set(state, site - offset, on);
		}
		else
		{
			if (hiddenRotationVertex == null)
				throw new UnsupportedOperationException(
						"No Hidden information, but the method (setHiddenRotation ...) was called");

			if (player < 1 || player > (hiddenRotationVertex.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (setHiddenRotation ...) in the containerState. Player =  "
								+ player);

			this.hiddenRotationVertex[player].set(state, site - offset, on);
		}
	}

	@Override
	public void setHiddenValue(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (type == SiteType.Cell || container().index() != 0 || type == null)
			super.setHiddenValue(state, player, site, level, type, on);
		else if (type == SiteType.Edge)
		{
			if (hiddenValueEdge == null)
				throw new UnsupportedOperationException(
						"No Hidden information, but the method (setHiddenValue ...) was called");

			if (player < 1 || player > (hiddenValueEdge.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (setHiddenValue ...) in the containerState. Player =  "
								+ player);

			this.hiddenValueEdge[player].set(state, site - offset, on);
		}
		else
		{
			if (hiddenValueVertex == null)
				throw new UnsupportedOperationException(
						"No Hidden information, but the method (setHiddenValue ...) was called");

			if (player < 1 || player > (hiddenValueVertex.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (setHiddenValue ...) in the containerState. Player =  "
								+ player);

			this.hiddenValueVertex[player].set(state, site - offset, on);
		}
	}

	@Override
	public void setHiddenCount(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (type == SiteType.Cell || container().index() != 0 || type == null)
			super.setHiddenCount(state, player, site, level, type, on);
		else if (type == SiteType.Edge)
		{
			if (hiddenCountEdge == null)
				throw new UnsupportedOperationException(
						"No Hidden information, but the method (setHiddenCount ...) was called");

			if (player < 1 || player > (hiddenCountEdge.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (setHiddenCount ...) in the containerState. Player =  "
								+ player);

			this.hiddenCountEdge[player].set(state, site - offset, on);
		}
		else
		{
			if (hiddenCountVertex == null)
				throw new UnsupportedOperationException(
						"No Hidden information, but the method (setHiddenCount ...) was called");

			if (player < 1 || player > (hiddenCountVertex.length - 1))
				throw new UnsupportedOperationException(
						"A wrong player is set in calling the method (setHiddenCount ...) in the containerState. Player =  "
								+ player);

			this.hiddenCountVertex[player].set(state, site - offset, on);
		}
	}

	@Override
	protected long calcCanonicalHash(int[] siteRemap, int[] edgeRemap, int[] vertexRemap, int[] playerRemap, final boolean whoOnly)
	{
		long hash = 0;

		if (siteRemap != null && siteRemap.length > 0) 
		{
			if (who != null) hash ^= who.calculateHashAfterRemap(siteRemap, playerRemap);
			if (!whoOnly)
			{
				if (what != null) hash ^= what.calculateHashAfterRemap(siteRemap, null);
				if (playable != null) hash ^= playable.calculateHashAfterRemap(siteRemap, false);
				if (count != null) hash ^= count.calculateHashAfterRemap(siteRemap, null);
				if (state != null) hash ^= state.calculateHashAfterRemap(siteRemap, null);
				if (rotation != null) hash ^= rotation.calculateHashAfterRemap(siteRemap, null);
				if (value != null) hash ^= value.calculateHashAfterRemap(siteRemap, null);
				if (hidden != null)
				{
					for (int i = 1; i < hidden.length; i++)
						hash ^= hidden[i].calculateHashAfterRemap(siteRemap, false);
				}
				if (hiddenWhat != null)
				{
					for (int i = 1; i < hiddenWhat.length; i++)
						hash ^= hiddenWhat[i].calculateHashAfterRemap(siteRemap, false);
				}
				if (hiddenWho != null)
				{
					for (int i = 1; i < hiddenWho.length; i++)
						hash ^= hiddenWho[i].calculateHashAfterRemap(siteRemap, false);
				}
				if (hiddenCount != null)
				{
					for (int i = 1; i < hiddenCount.length; i++)
						hash ^= hiddenCount[i].calculateHashAfterRemap(siteRemap, false);
				}
				if (hiddenRotation != null)
				{
					for (int i = 1; i < hiddenRotation.length; i++)
						hash ^= hiddenRotation[i].calculateHashAfterRemap(siteRemap, false);
				}
				if (hiddenValue != null)
				{
					for (int i = 1; i < hiddenValue.length; i++)
						hash ^= hiddenValue[i].calculateHashAfterRemap(siteRemap, false);
				}
				if (hiddenState != null)
				{
					for (int i = 1; i < hiddenState.length; i++)
						hash ^= hiddenState[i].calculateHashAfterRemap(siteRemap, false);
				}
			}
		}

		if (edgeRemap != null && edgeRemap.length > 0) 
		{
			if (whoEdge != null) hash ^= whoEdge.calculateHashAfterRemap(edgeRemap, playerRemap);
			if (!whoOnly)
			{
				if (whatEdge != null) hash ^= whatEdge.calculateHashAfterRemap(edgeRemap, null);
				if (countEdge != null) hash ^= countEdge.calculateHashAfterRemap(edgeRemap, null);
				if (stateEdge != null) hash ^= stateEdge.calculateHashAfterRemap(edgeRemap, null);
				if (rotationEdge != null) hash ^= rotationEdge.calculateHashAfterRemap(edgeRemap, null);
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
		
		if (vertexRemap != null && vertexRemap.length > 0) 
		{
			if (whoVertex != null) hash ^= whoVertex.calculateHashAfterRemap(vertexRemap, playerRemap);
			if (!whoOnly)
			{
				if (whatVertex != null) hash ^= whatVertex.calculateHashAfterRemap(vertexRemap, null);
				if (countVertex != null) hash ^= countVertex.calculateHashAfterRemap(vertexRemap, null);
				if (stateVertex != null) hash ^= stateVertex.calculateHashAfterRemap(vertexRemap, null);
				if (rotationVertex != null) hash ^= rotationVertex.calculateHashAfterRemap(vertexRemap, null);
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


	@Override
	public int whoEdge(final int edge)
	{
		return whoEdge.getChunk(edge);
	}

	@Override
	public int whoVertex(final int vertex)
	{
		return whoVertex.getChunk(vertex);
	}

	//-------------------------------------------------------------------------

	@Override
	public int whatEdge(final int edge)
	{
		if (whatEdge == null)
			return whoEdge(edge);

		return whatEdge.getChunk(edge);
	}

	//-------------------------------------------------------------------------

	@Override
	public int stateEdge(final int edge)
	{
		if (stateEdge == null)
			return 0;

		return stateEdge.getChunk(edge);
	}

	@Override
	public int rotationEdge(final int edge)
	{
		if (rotationEdge == null)
			return 0;

		return rotationEdge.getChunk(edge);
	}

	//-------------------------------------------------------------------------

	@Override
	public int countEdge(final int edge)
	{
		if (countEdge != null)
			return countEdge.getChunk(edge);

		if (whoEdge.getChunk(edge) != 0 || whatEdge != null && whatEdge.getChunk(edge) != 0)
			return 1;

		return 0;
	}

	//-------------------------------------------------------------------------

	@Override
	public int whatVertex(final int vertex)
	{
		if (whatVertex == null)
			return whoVertex(vertex);

		return whatVertex.getChunk(vertex);
	}

	//-------------------------------------------------------------------------

	@Override
	public int stateVertex(final int vertex)
	{
		if (stateVertex == null)
			return 0;

		return stateVertex.getChunk(vertex);
	}

	@Override
	public int rotationVertex(final int vertex)
	{
		if (rotationVertex == null)
			return 0;

		return rotationVertex.getChunk(vertex);
	}

	//-------------------------------------------------------------------------

	@Override
	public int countVertex(final int vertex)
	{
		if (countVertex != null)
			return countVertex.getChunk(vertex);

		if (whoVertex.getChunk(vertex) != 0 || whatVertex != null && whatVertex.getChunk(vertex) != 0)
			return 1;

		return 0;
	}

	/**
	 * @param site The site.
	 * @param type The graph element type.
	 * 
	 * @return True if the site is occupied.
	 */
	public boolean isOccupied(final int site, final SiteType type)
	{
		if (type.equals(SiteType.Cell))
			return countCell(site) != 0;
		else if (type.equals(SiteType.Edge))
			return countEdge(site) != 0;
		else
			return countVertex(site) != 0;
	}

	@Override
	public void addToEmpty(final int site, final SiteType type)
	{
		if (type.equals(SiteType.Cell))
			empty.add(site - offset);
		else if (type.equals(SiteType.Edge))
			emptyEdge.add(site);
		else
			emptyVertex.add(site);
	}

	@Override
	public void removeFromEmpty(final int site, final SiteType type)
	{
		if (type.equals(SiteType.Cell))
			empty.remove(site - offset);
		else if (type.equals(SiteType.Edge))
			emptyEdge.remove(site);
		else
			emptyVertex.remove(site);
	}

	@Override
	public void setSite(final State trialState, final int site, final int whoVal, final int whatVal, final int countVal,
			final int stateVal, final int rotationVal, final int valueVal, final SiteType type)
	{
		if (type.equals(SiteType.Cell) || container().index() != 0)
		{
			super.setSite(trialState, site, whoVal, whatVal, countVal, stateVal, rotationVal, valueVal, type);
		}
		else if (type.equals(SiteType.Edge))
		{
			final boolean wasEmpty = !isOccupied(site, type);

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

			final boolean isEmpty = !isOccupied(site, type);

			if (wasEmpty == isEmpty)
				return;

			if (isEmpty)
				addToEmpty(site, type);
			else
				removeFromEmpty(site, type);
		}
		else if (type.equals(SiteType.Vertex))
		{
			final boolean wasEmpty = !isOccupied(site, type);

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

			final boolean isEmpty = !isOccupied(site, type);

			if (wasEmpty == isEmpty)
				return;

			if (isEmpty)
				addToEmpty(site, type);
			else
				removeFromEmpty(site, type);
		}
	}

	@Override
	public Region emptyRegion(final SiteType type)
	{
		if (type.equals(SiteType.Cell))
			return empty;
		else if (type.equals(SiteType.Edge))
			return emptyEdge;
		else
			return emptyVertex;
	}

	@Override
	public boolean isEmptyVertex(final int vertex)
	{
		return emptyVertex.contains(vertex - offset);
	}

	@Override
	public boolean isEmptyEdge(final int edge)
	{
		return emptyEdge.contains(edge - offset);
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
	public int valueVertex(final int site)
	{
		if (valueVertex == null)
			return 0;

		return valueVertex.getChunk(site);
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
	public int valueEdge(final int site)
	{
		if (valueEdge == null)
			return 0;

		return valueEdge.getChunk(site);
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("ContainerState type = " + this.getClass() + "\n");
		
		if (empty != null)
			sb.append("empty = " + empty.bitSet().toChunkString() + "\n");
		
		if (emptyEdge != null)
			sb.append("emptyEdge = " + emptyEdge.bitSet().toChunkString() + "\n");
		
		if (emptyVertex != null)
			sb.append("emptyVertex = " + emptyVertex.bitSet().toChunkString() + "\n");
		
		if (who != null)
			sb.append("Who = " + who.internalStateCopy().toChunkString() + "\n");
		
		if (whoEdge != null)
			sb.append("whoEdge = " + whoEdge.internalStateCopy().toChunkString() + "\n");
		
		if (whoVertex != null)
			sb.append("whoVertex = " + whoVertex.internalStateCopy().toChunkString() + "\n");
		
		if (what != null)
			sb.append("What" + what.internalStateCopy().toChunkString() + "\n");
		
		if (whatEdge != null)
			sb.append("whatEdge = " + whatEdge.internalStateCopy().toChunkString() + "\n");
		
		if (whatVertex != null)
			sb.append("whatVertex = " + whatVertex.internalStateCopy().toChunkString() + "\n");
		
		if (state != null)
			sb.append("State = " + state.internalStateCopy().toChunkString() + "\n");
		
		if (rotation != null)
			sb.append("Rotation = " + rotation.internalStateCopy().toChunkString() + "\n");

		if (value != null)
			sb.append("value = " + value.internalStateCopy().toChunkString() + "\n");

		if (count != null)
			sb.append("Count = " + count.internalStateCopy().toChunkString() + "\n");
		
		if (playable != null)
			sb.append("Playable = " + playable.internalStateCopy().toString() + "\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	@Override public ChunkSet emptyChunkSetVertex() { return emptyVertex.bitSet(); }
	@Override public ChunkSet emptyChunkSetEdge() { return emptyEdge.bitSet(); }

	@Override public int numChunksWhoVertex() { return whoVertex.numChunks(); }
	@Override public int numChunksWhoEdge() { return whoEdge.numChunks(); }
	
	@Override public int chunkSizeWhoVertex() { return whoVertex.chunkSize(); }
	@Override public int chunkSizeWhoEdge() { return whoEdge.chunkSize(); }

	@Override 
	public int numChunksWhatVertex() 
	{ 
		return whatVertex != null ? whatVertex.numChunks() : whoVertex.numChunks(); 
	}
	
	@Override 
	public int numChunksWhatEdge() 
	{ 
		return whatEdge != null ? whatEdge.numChunks() : whoEdge.numChunks(); 
	}
	
	@Override 
	public int chunkSizeWhatVertex() 
	{ 
		return whatVertex != null ? whatVertex.chunkSize() : whoVertex.chunkSize();
	}
	
	@Override 
	public int chunkSizeWhatEdge() 
	{ 
		return whatEdge != null ? whatEdge.chunkSize() : whoEdge.chunkSize();
	}
	
	@Override public boolean matchesWhoVertex(final ChunkSet mask, final ChunkSet pattern) { return whoVertex.matches(mask, pattern); }
	@Override public boolean matchesWhoEdge(final ChunkSet mask, final ChunkSet pattern) { return whoEdge.matches(mask, pattern); }
	
	@Override public boolean violatesNotWhoVertex(final ChunkSet mask, final ChunkSet pattern) { return whoVertex.violatesNot(mask, pattern); }
	@Override public boolean violatesNotWhoEdge(final ChunkSet mask, final ChunkSet pattern) { return whoEdge.violatesNot(mask, pattern); }
	
	@Override public boolean violatesNotWhoVertex(final ChunkSet mask, final ChunkSet pattern, final int startWord) 
	{ return whoVertex.violatesNot(mask, pattern, startWord); }
	
	@Override public boolean violatesNotWhoEdge(final ChunkSet mask, final ChunkSet pattern, final int startWord) 
	{ return whoEdge.violatesNot(mask, pattern, startWord); }
	
	@Override 
	public boolean matchesWhatVertex(final ChunkSet mask, final ChunkSet pattern) 
	{ 
		return whatVertex != null ? whatVertex.matches(mask, pattern) : whoVertex.matches(mask, pattern); 
	}
	
	@Override 
	public boolean matchesWhatEdge(final ChunkSet mask, final ChunkSet pattern) 
	{ 
		return whatEdge != null ? whatEdge.matches(mask, pattern) : whoEdge.matches(mask, pattern); 
	}
	
	@Override 
	public boolean violatesNotWhatVertex(final ChunkSet mask, final ChunkSet pattern) 
	{ 
		return whatVertex != null ? whatVertex.violatesNot(mask, pattern) : whoVertex.violatesNot(mask, pattern); 
	}
	
	@Override 
	public boolean violatesNotWhatEdge(final ChunkSet mask, final ChunkSet pattern) 
	{ 
		return whatEdge != null ? whatEdge.violatesNot(mask, pattern) : whoEdge.violatesNot(mask, pattern); 
	}
	
	@Override 
	public boolean violatesNotWhatVertex(final ChunkSet mask, final ChunkSet pattern, final int startWord) 
	{ 
		return whatVertex != null ? whatVertex.violatesNot(mask, pattern, startWord) : whoVertex.violatesNot(mask, pattern, startWord); 
	}
	
	@Override 
	public boolean violatesNotWhatEdge(final ChunkSet mask, final ChunkSet pattern, final int startWord) 
	{ 
		return whatEdge != null ? whatEdge.violatesNot(mask, pattern, startWord) : whoEdge.violatesNot(mask, pattern, startWord); 
	}
	
	@Override
	public boolean matchesWhoVertex(final int wordIdx, final long mask, final long matchingWord)
	{
		return whoVertex.matches(wordIdx, mask, matchingWord);
	}

	@Override 
	public boolean matchesWhoEdge(final int wordIdx, final long mask, final long matchingWord) 
	{ 
		return whoEdge.matches(wordIdx, mask, matchingWord);
	}
	
	@Override 
	public boolean matchesWhatVertex(final int wordIdx, final long mask, final long matchingWord) 
	{ 
		return whatVertex != null ? whatVertex.matches(wordIdx, mask, matchingWord) : whoVertex.matches(wordIdx, mask, matchingWord);
	}

	@Override 
	public boolean matchesWhatEdge(final int wordIdx, final long mask, final long matchingWord) 
	{ 
		return whatEdge != null ? whatEdge.matches(wordIdx, mask, matchingWord) : whoEdge.matches(wordIdx, mask, matchingWord);
	}
	
	@Override public ChunkSet cloneWhoVertex() { return whoVertex.internalStateCopy(); }
	@Override public ChunkSet cloneWhoEdge() { return whoEdge.internalStateCopy(); }
	
	@Override public ChunkSet cloneWhatVertex() { return whatVertex != null ? whatVertex.internalStateCopy() : whoVertex.internalStateCopy(); }
	@Override public ChunkSet cloneWhatEdge() { return whatEdge != null ? whatEdge.internalStateCopy() : whoEdge.internalStateCopy(); }
}
