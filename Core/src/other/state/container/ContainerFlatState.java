package other.state.container;

import java.util.ArrayList;
import java.util.List;

import game.Game;
import game.equipment.container.Container;
import game.types.board.SiteType;
import game.types.state.GameType;
import main.Constants;
import main.collections.ChunkSet;
import other.state.State;
import other.state.zhash.HashedBitSet;
import other.state.zhash.HashedChunkSet;
import other.state.zhash.ZobristHashGenerator;
import other.topology.Cell;

/**
 * Global State for a container item.
 *
 * @author cambolbro and Eric.Piette and mrraow
 */
public class ContainerFlatState extends BaseContainerState
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Playable sites (for boardless games). */
	protected final HashedBitSet playable;

	/** Type of Item on the site. */
	protected final HashedChunkSet who;

	/** Type of Item on the site. */
	protected final HashedChunkSet what;

	/** Count of Item on the site. */
	protected final HashedChunkSet count;

	/** State of Item on the site. */
	protected final HashedChunkSet state;
	
	/** Rotation state of Item on the site. */
	protected final HashedChunkSet rotation;

	/** Value of the piece on a site. */
	protected final HashedChunkSet value;

	/** Which site has some hidden properties for each player. */
	protected final HashedBitSet[] hidden;

	/** Which site has the what information hidden for each player. */
	protected final HashedBitSet[] hiddenWhat;

	/** Which site has the who information hidden for each player. */
	protected final HashedBitSet[] hiddenWho;

	/** Which site has the count information hidden for each player. */
	protected final HashedBitSet[] hiddenCount;

	/** Which site has the state information hidden for each player. */
	protected final HashedBitSet[] hiddenState;

	/** Which site has the rotation information hidden for each player. */
	protected final HashedBitSet[] hiddenRotation;

	/** Which site has the value information hidden for each player. */
	protected final HashedBitSet[] hiddenValue;

	//-------------------------------------------------------------------------

	/**
	 * Constructor of a flat container.
	 * 
	 * @param generator
	 * @param game
	 * @param container
	 * @param numSites
	 * @param maxWhatVal
	 * @param maxStateVal
	 * @param maxCountVal
	 * @param maxRotationVal
	 * @param maxPieceValue
	 */
	public ContainerFlatState
	(
		final ZobristHashGenerator generator, 
		final Game game, 
		final Container container, 
		final int numSites,
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
			numSites
		);
		final int numPlayers = game.players().count();

		if ((game.gameFlags() & GameType.HiddenInfo) == 0L)
		{
			hidden = null;
			hiddenWhat = null;
			hiddenWho = null;
			hiddenCount = null;
			hiddenRotation = null;
			hiddenValue = null;
			hiddenState = null;
		}
		else
		{
			hidden = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hidden[i] = new HashedBitSet(generator, numSites);
			hiddenWhat = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenWhat[i] = new HashedBitSet(generator, numSites);
			hiddenWho = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenWho[i] = new HashedBitSet(generator, numSites);
			hiddenCount = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenCount[i] = new HashedBitSet(generator, numSites);
			hiddenState = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenState[i] = new HashedBitSet(generator, numSites);
			hiddenRotation = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenRotation[i] = new HashedBitSet(generator, numSites);
			hiddenValue = new HashedBitSet[numPlayers + 1];
			for (int i = 1; i < (numPlayers + 1); i++)
				hiddenValue[i] = new HashedBitSet(generator, numSites);
		}

		if (!game.isBoardless())
			playable = null;
		else
			playable = new HashedBitSet(generator, numSites);

		who = new HashedChunkSet(generator, numPlayers + 1, numSites);
		what = maxWhatVal > 0 ? new HashedChunkSet(generator, maxWhatVal, numSites) : null;
		count = maxCountVal > 0 ? new HashedChunkSet(generator, maxCountVal, numSites) : null;
		state = maxStateVal > 0 ? new HashedChunkSet(generator, maxStateVal, numSites) : null;
		rotation = maxRotationVal > 0 ? new HashedChunkSet(generator, maxRotationVal, numSites) : null;
		value = maxPieceValue > 0 ? new HashedChunkSet(generator, maxPieceValue, numSites) : null;
	}

	/**
	 * Copy constructor.
	 *
	 * @param other
	 */
	public ContainerFlatState(final ContainerFlatState other)
	{
		super(other);

		who = (other.who == null) ? null : other.who.clone();

		if (other.hidden != null)
		{
			hidden = new HashedBitSet[other.hidden.length];
			for (int i = 1; i < other.hidden.length; i++)
				hidden[i] = (other.hidden[i] == null) ? null : other.hidden[i].clone();

			hiddenWhat = new HashedBitSet[other.hiddenWhat.length];
			for (int i = 1; i < other.hiddenWhat.length; i++)
				hiddenWhat[i] = (other.hiddenWhat[i] == null) ? null : other.hiddenWhat[i].clone();

			hiddenWho = new HashedBitSet[other.hiddenWho.length];
			for (int i = 1; i < other.hiddenWho.length; i++)
				hiddenWho[i] = (other.hiddenWho[i] == null) ? null : other.hiddenWho[i].clone();

			hiddenCount = new HashedBitSet[other.hiddenCount.length];
			for (int i = 1; i < other.hiddenCount.length; i++)
				hiddenCount[i] = (other.hiddenCount[i] == null) ? null : other.hiddenCount[i].clone();

			hiddenState = new HashedBitSet[other.hiddenState.length];
			for (int i = 1; i < other.hiddenState.length; i++)
				hiddenState[i] = (other.hiddenState[i] == null) ? null : other.hiddenState[i].clone();

			hiddenRotation = new HashedBitSet[other.hiddenRotation.length];
			for (int i = 1; i < other.hiddenRotation.length; i++)
				hiddenRotation[i] = (other.hiddenRotation[i] == null) ? null : other.hiddenRotation[i].clone();

			hiddenValue = new HashedBitSet[other.hiddenValue.length];
			for (int i = 1; i < other.hiddenValue.length; i++)
				hiddenValue[i] = (other.hiddenValue[i] == null) ? null : other.hiddenValue[i].clone();
		}
		else
		{
			hidden = null;
			hiddenWhat = null;
			hiddenWho = null;
			hiddenCount = null;
			hiddenRotation = null;
			hiddenValue = null;
			hiddenState = null;
		}

		playable = (other.playable == null) ? null : other.playable.clone();
		what = (other.what == null) ? null : other.what.clone();
		count = (other.count == null) ? null : other.count.clone();
		state = (other.state == null) ? null : other.state.clone();
		rotation = (other.rotation == null) ? null : other.rotation.clone();
		value = (other.value == null) ? null : other.value.clone();
	}

	@Override
	public ContainerFlatState deepClone()
	{
		return new ContainerFlatState(this);
	}
	
	//-------------------------------------------------------------------------

	@Override
	protected long calcCanonicalHash(final int[] siteRemap, final int[] edgeRemap, final int[] vertexRemap, final int[] playerRemap, final boolean whoOnly)	
	{
		long hash = 0;

		// Key fact: who owns the piece
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

		if (who != null)
			who.clear(trialState);
		if (what != null) what.clear(trialState);
		if (count != null)
			count.clear(trialState);
		if (state != null) state.clear(trialState);
		if (rotation != null)
			rotation.clear(trialState);
		if (value != null)
			value.clear(trialState);

		if (hidden != null)
			for (int i = 1; i < hidden.length; i++)
				hidden[i].clear(trialState);
		if (hiddenWhat != null)
			for (int i = 1; i < hiddenWhat.length; i++)
				hiddenWhat[i].clear(trialState);
		if (hiddenWho != null)
			for (int i = 1; i < hiddenWho.length; i++)
				hiddenWho[i].clear(trialState);
		if (hiddenCount != null)
			for (int i = 1; i < hiddenCount.length; i++)
				hiddenCount[i].clear(trialState);
		if (hiddenRotation != null)
			for (int i = 1; i < hiddenRotation.length; i++)
				hiddenRotation[i].clear(trialState);
		if (hiddenValue != null)
			for (int i = 1; i < hiddenValue.length; i++)
				hiddenValue[i].clear(trialState);
		if (hiddenState != null)
			for (int i = 1; i < hiddenState.length; i++)
				hiddenState[i].clear(trialState);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isHidden(final int player, final int site, final int level, final SiteType type)
	{
		if (hidden == null)
			return false;

		if(player < 1 || player > (hidden.length-1))		
			throw new UnsupportedOperationException("A wrong player is set in calling the method (hidden ...) in the containerState. Player =  " + player);
		
		return this.hidden[player].get(site - offset);
	}
	
	@Override
	public boolean isHiddenWhat(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenWhat == null)
			return false;

		if(player < 1 || player > (hiddenWhat.length-1))		
			throw new UnsupportedOperationException("A wrong player is set in calling the method (hiddenWhat ...) in the containerState. Player =  " + player);
		
		return this.hiddenWhat[player].get(site - offset);
	}
	
	@Override
	public boolean isHiddenWho(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenWho == null)
			return false;

		if(player < 1 || player > (hiddenWho.length-1))		
			throw new UnsupportedOperationException("A wrong player is set in calling the method (hiddenWho ...) in the containerState. Player =  " + player);
		
		return this.hiddenWho[player].get(site - offset);
	}
	
	@Override
	public boolean isHiddenState(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenState == null)
			return false;

		if(player < 1 || player > (hiddenState.length-1))		
			throw new UnsupportedOperationException("A wrong player is set in calling the method (hiddenState ...) in the containerState. Player =  " + player);
		
		return this.hiddenState[player].get(site - offset);
	}
	
	@Override
	public boolean isHiddenRotation(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenRotation == null)
			return false;

		if(player < 1 || player > (hiddenRotation.length-1))		
			throw new UnsupportedOperationException("A wrong player is set in calling the method (hiddenRotation ...) in the containerState. Player =  " + player);
		
		return this.hiddenRotation[player].get(site - offset);
	}
	
	@Override
	public boolean isHiddenValue(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenValue == null)
			return false;

		if(player < 1 || player > (hiddenValue.length-1))		
			throw new UnsupportedOperationException("A wrong player is set in calling the method (hiddenValue ...) in the containerState. Player =  " + player);
		
		return this.hiddenValue[player].get(site - offset);
	}
	
	@Override
	public boolean isHiddenCount(final int player, final int site, final int level, final SiteType type)
	{
		if (hiddenCount == null)
			return false;

		if(player < 1 || player > (hiddenCount.length-1))		
			throw new UnsupportedOperationException("A wrong player is set in calling the method (hiddenCount ...) in the containerState. Player =  " + player);
		
		return this.hiddenCount[player].get(site - offset);
	}
	
	@Override
	public void setHidden(final State state, final int player, final int site, final int level, final SiteType type,
			final boolean on)
	{
		if (hidden == null)
			throw new UnsupportedOperationException("No Hidden information, but the method (setHidden ...) was called");

		if(player < 1 || player > (hidden.length-1))		
			throw new UnsupportedOperationException("A wrong player is set in calling the method (setHidden ...) in the containerState. Player =  " + player);
		
		this.hidden[player].set(state, site - offset, on);
	}
	
	@Override
	public void setHiddenWhat(final State state, final int player, final int site, final int level, final SiteType type,
			final boolean on)
	{
		if (hiddenWhat == null)
			throw new UnsupportedOperationException(
					"No Hidden information, but the method (setHiddenWhat ...) was called");

		if (player < 1 || player > (hiddenWhat.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHiddenWhat ...) in the containerState. Player =  "
							+ player);

		this.hiddenWhat[player].set(state, site - offset, on);
	}

	@Override
	public void setHiddenWho(final State state, final int player, final int site, final int level, final SiteType type,
			final boolean on)
	{
		if (hiddenWho == null)
			throw new UnsupportedOperationException(
					"No Hidden information, but the method (setHiddenWho ...) was called");

		if (player < 1 || player > (hiddenWho.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHiddenWho ...) in the containerState. Player =  "
							+ player);

		this.hiddenWho[player].set(state, site - offset, on);
	}

	@Override
	public void setHiddenState(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (hiddenState == null)
			throw new UnsupportedOperationException(
					"No Hidden information, but the method (setHiddenState ...) was called");

		if (player < 1 || player > (hiddenState.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHiddenState ...) in the containerState. Player =  "
							+ player);

		this.hiddenState[player].set(state, site - offset, on);
	}

	@Override
	public void setHiddenRotation(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (hiddenRotation == null)
			throw new UnsupportedOperationException(
					"No Hidden information, but the method (setHiddenRotation ...) was called");

		if (player < 1 || player > (hiddenRotation.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHiddenRotation ...) in the containerState. Player =  "
							+ player);

		this.hiddenRotation[player].set(state, site - offset, on);
	}

	@Override
	public void setHiddenValue(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (hiddenValue == null)
			throw new UnsupportedOperationException(
					"No Hidden information, but the method (setHiddenValue ...) was called");

		if (player < 1 || player > (hiddenValue.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHiddenValue ...) in the containerState. Player =  "
							+ player);

		this.hiddenValue[player].set(state, site - offset, on);
	}

	@Override
	public void setHiddenCount(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (hiddenCount == null)
			throw new UnsupportedOperationException(
					"No Hidden information, but the method (setHiddenCount ...) was called");

		if (player < 1 || player > (hiddenCount.length - 1))
			throw new UnsupportedOperationException(
					"A wrong player is set in calling the method (setHiddenCount ...) in the containerState. Player =  "
							+ player);

		this.hiddenCount[player].set(state, site - offset, on);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isPlayable(final int site)
	{
		if (playable == null)
			return true;

		return playable.get(site - offset);
	}

	@Override
	public void setPlayable(final State trialState, final int site, final boolean on)
	{
		if (playable != null)
			playable.set(trialState, site, on);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isOccupied(final int site)
	{
		return countCell(site) != 0;
	}

	//-------------------------------------------------------------------------
	@Override
	public void setSite
	(
		final State trialState, final int site, final int whoVal, final int whatVal, 
		final int countVal, final int stateVal, final int rotationVal, final int valueVal, final SiteType type
	)
	{
		final boolean wasEmpty = !isOccupied(site);
		
		if (whoVal != Constants.UNDEFINED) 
			who.setChunk(trialState, site - offset, whoVal);

		if (whatVal != Constants.UNDEFINED)
			defaultIfNull(what).setChunk(trialState, site - offset, whatVal);

		if (countVal != Constants.UNDEFINED) 
		{
			if (count != null)
				count.setChunk(trialState, site - offset, (countVal < 0 ? 0 : countVal));
			else if (count == null && countVal > 1)
				throw new UnsupportedOperationException("This game does not support counts, but a count > 1 has been set. countVal="+countVal);
		}
		
		if (stateVal != Constants.UNDEFINED) 
		{
			if (state != null) 
				state.setChunk(trialState, site - offset, stateVal);
			else if (stateVal != 0)
				throw new UnsupportedOperationException("This game does not support states, but a state has been set. stateVal="+stateVal);
		}
		
		if (rotationVal != Constants.UNDEFINED)
		{
			if (rotation != null)
				rotation.setChunk(trialState, site - offset, rotationVal);
			else if (rotationVal != 0)
				throw new UnsupportedOperationException(
						"This game does not support rotations, but a rotation has been set. rotationVal="
								+ rotationVal);
		}

		if (valueVal != Constants.UNDEFINED)
		{
			if (value != null)
				value.setChunk(trialState, site - offset, valueVal);
			else if (valueVal != 0)
				throw new UnsupportedOperationException(
						"This game does not support piece values, but a value has been set. valueVal="
								+ valueVal);
		}

		final boolean isEmpty = !isOccupied(site);

		if (wasEmpty == isEmpty) 
			return;

		if (isEmpty)
		{
			addToEmptyCell(site);
			if (playable != null && valueVal == Constants.OFF)
			{
				checkPlayable(trialState, site);

				final Cell v = container().topology().cells().get(site - offset);
				for (final Cell vNbors : v.adjacent())
					checkPlayable(trialState, vNbors.index());
			}
		}
		else
		{
			removeFromEmptyCell(site);
			if (playable != null && valueVal == Constants.OFF)
			{
				setPlayable(trialState, site - offset, false);

				final Cell v = container().topology().cells().get(site - offset);
				for (final Cell vNbors : v.adjacent())
					if (!isOccupied(vNbors.index()))
						setPlayable(trialState, vNbors.index(), true);
			}
		}
		
		if (playable != null && whatVal == 0 && !wasEmpty)
		{
			final Cell cell = container().topology().cells().get(site - offset);
			final List<Cell> cells = new ArrayList<Cell>();
			cells.add(cell);
			cells.addAll(cell.adjacent());
			
			for (final Cell cellToCheck : cells)
				if (!isOccupied(cellToCheck.index()))
				{
					boolean findOccupiedNbor = false;
					for (final Cell cellNbors : cellToCheck.adjacent())
						if (isOccupied(cellNbors.index()))
						{
							findOccupiedNbor = true;
							break;
						}
					setPlayable(trialState, cellToCheck.index(), findOccupiedNbor);
				}
		}
	}

	/**
	 * To check the playable site in case of modification for boardless.
	 * 
	 * @param trialState
	 * @param site
	 */
	private void checkPlayable(final State trialState, final int site) 
	{
		if (isOccupied(site))
		{
			setPlayable(trialState, site - offset, false);
			return;
		}
				
		final Cell v = container().topology().cells().get(site - offset);
		for (final Cell vNbors : v.adjacent())
			if (isOccupied(vNbors.index()))
			{
				setPlayable(trialState, site - offset, true);
				return;
			}
		
		setPlayable(trialState, site - offset, false);
	}

	//-------------------------------------------------------------------------

	@Override
	public int whoCell(final int site)
	{
		return who.getChunk(site - offset);
	}

	//-------------------------------------------------------------------------

	@Override
	public int whatCell(final int site)
	{
		if (what == null) 
			return whoCell(site);
		
		return what.getChunk(site - offset);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int stateCell(final int site)
	{
		if (state == null) 
			return 0;
		
		return state.getChunk(site - offset);
	}

	@Override
	public int rotationCell(final int site)
	{
		if (rotation == null)
			return 0;

		return rotation.getChunk(site - offset);
	}

	@Override
	public int valueCell(final int site)
	{
		if (value == null)
			return 0;

		return value.getChunk(site - offset);
	}

	//-------------------------------------------------------------------------

	@Override
	public int countCell(final int site)
	{
		if (count != null)
			return count.getChunk(site - offset);
		
		if (who.getChunk(site - offset) != 0 || what != null && what.getChunk(site - offset) != 0)
			return 1;
		
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
		result = prime * result + ((hidden == null) ? 0 : hidden.hashCode());
		result = prime * result + ((hiddenWhat == null) ? 0 : hiddenWhat.hashCode());
		result = prime * result + ((hiddenWho == null) ? 0 : hiddenWho.hashCode());
		result = prime * result + ((hiddenState == null) ? 0 : hiddenState.hashCode());
		result = prime * result + ((hiddenRotation == null) ? 0 : hiddenRotation.hashCode());
		result = prime * result + ((hiddenValue == null) ? 0 : hiddenValue.hashCode());
		result = prime * result + ((hiddenState == null) ? 0 : hiddenState.hashCode());
		result = prime * result + ((who == null) ? 0 : who.hashCode());
		result = prime * result + ((playable == null) ? 0 : playable.hashCode());
		result = prime * result + ((count == null) ? 0 : count.hashCode());
		result = prime * result + ((what == null) ? 0 : what.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((rotation == null) ? 0 : rotation.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());

		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (!(obj instanceof ContainerFlatState))
			return false;

		if (!super.equals(obj))
			return false;

		final ContainerFlatState other = (ContainerFlatState) obj;

		if (hidden != null)
		{
			for (int i = 1; i < hidden.length; i++)
				if (!bitSetsEqual(hidden[i], other.hidden[i]))
					return false;
		}
		if (hiddenWhat != null)
		{
			for (int i = 1; i < hiddenWhat.length; i++)
				if (!bitSetsEqual(hiddenWhat[i], other.hiddenWhat[i]))
					return false;
		}
		if (hiddenWho != null)
		{
			for (int i = 1; i < hiddenWho.length; i++)
				if (!bitSetsEqual(hiddenWho[i], other.hiddenWho[i]))
					return false;
		}
		if (hiddenRotation != null)
		{
			for (int i = 1; i < hiddenRotation.length; i++)
				if (!bitSetsEqual(hiddenRotation[i], other.hiddenRotation[i]))
					return false;
		}
		if (hiddenState != null)
		{
			for (int i = 1; i < hiddenState.length; i++)
				if (!bitSetsEqual(hiddenState[i], other.hiddenState[i]))
					return false;
		}
		if (hiddenValue != null)
		{
			for (int i = 1; i < hiddenValue.length; i++)
				if (!bitSetsEqual(hiddenValue[i], other.hiddenValue[i]))
					return false;
		}
		if (hiddenValue != null)
		{
			for (int i = 1; i < hiddenValue.length; i++)
				if (!bitSetsEqual(hiddenValue[i], other.hiddenValue[i]))
					return false;
		}
		if (hiddenCount != null)
		{
			for (int i = 1; i < hiddenCount.length; i++)
				if (!bitSetsEqual(hiddenCount[i], other.hiddenCount[i]))
					return false;
		}

		if (!chunkSetsEqual(who, other.who)) return false;
		if (!bitSetsEqual(playable, other.playable)) return false;
		if (!chunkSetsEqual(count, other.count)) return false;
		if (!chunkSetsEqual(what, other.what)) return false;
		if (!chunkSetsEqual(state, other.state)) return false;
		if (!chunkSetsEqual(rotation, other.rotation)) return false;
		if (!chunkSetsEqual(value, other.value)) return false;

		return true;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("ContainerState type = " + this.getClass() + "\n");
		
		if (emptyChunkSetCell() != null)
			sb.append("Empty = " + emptyChunkSetCell().toChunkString() + "\n");
		
		if (who != null)
			sb.append("Who = " + cloneWhoCell().toChunkString() + "\n");
		
		if (what != null)
			sb.append("What" + cloneWhatCell().toChunkString() + "\n");
		
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
		
		if (hidden != null)
		{
			for (int i = 1; i < hidden.length; i++)
				sb.append(
						"Hidden for " + " player " + i + " = " + hidden[i].internalStateCopy().toString() + "\n");
		}
		if (hiddenWhat != null)
		{
			for (int i = 1; i < hiddenWhat.length; i++)
				sb.append("Hidden What for " + " player " + i + " = " + hiddenWhat[i].internalStateCopy().toString()
						+ "\n");
		}
		if (hiddenWho != null)
		{
			for (int i = 1; i < hiddenWho.length; i++)
				sb.append("Hidden Who for " + " player " + i + " = " + hiddenWho[i].internalStateCopy().toString()
						+ "\n");
		}
		if (hiddenCount != null)
		{
			for (int i = 1; i < hiddenCount.length; i++)
				sb.append("Hidden Count for " + " player " + i + " = " + hiddenCount[i].internalStateCopy().toString()
						+ "\n");
		}
		if (hiddenValue != null)
		{
			for (int i = 1; i < hiddenValue.length; i++)
				sb.append("Hidden Value for " + " player " + i + " = " + hiddenValue[i].internalStateCopy().toString()
						+ "\n");
		}
		if (hiddenState != null)
		{
			for (int i = 1; i < hiddenState.length; i++)
				sb.append("Hidden State for " + " player " + i + " = " + hiddenState[i].internalStateCopy().toString()
						+ "\n");
		}
		if (hiddenRotation != null)
		{
			for (int i = 1; i < hiddenRotation.length; i++)
				sb.append("Hidden Rotation for " + " player " + i + " = "
						+ hiddenRotation[i].internalStateCopy().toString() + "\n");
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
		if (!isEmptyCell(site))
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
		if (!isEmptyEdge(site))
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
		if (!isEmptyVertex(site))
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
		return 0;
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
		if (valueVal != Constants.UNDEFINED)
		{
			if (value != null)
				value.setChunk(trialState, site - offset, valueVal);
			else if (valueVal != 0)
				throw new UnsupportedOperationException(
						"This game does not support piece values, but a value has been set. valueVal=" + valueVal);
		}
	}

	@Override
	public void setCount(final State trialState, final int site, final int countVal)
	{
		if (countVal != Constants.UNDEFINED)
		{
			if (count != null)
				count.setChunk(trialState, site - offset, (countVal < 0 ? 0 : countVal));
			else if (count == null && countVal > 1)
				throw new UnsupportedOperationException(
						"This game does not support counts, but a count > 1 has been set. countVal=" + countVal);
		}
	}

	@Override
	public void addItem(State trialState, int site, int whatItem, int whoItem, Game game)
	{
		// do nothing
	}

	@Override
	public void insert(State trialState, SiteType type, int site, int level, int whatItem, int whoItem,
			final int stateVal,
			final int rotationVal, final int valueVal, Game game)
	{
		// do nothing
	}

	@Override
	public void insertCell(State trialState, int site, int level, int whatItem, int whoItem, final int stateVal,
			final int rotationVal, final int valueVal, Game game)
	{
		// do nothing
	}

	@Override
	public void addItem(State trialState, int site, int whatItem, int whoItem, int stateVal, int rotationVal,
			int valueval,
			Game game)
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
		return remove(trialState, site, SiteType.Cell);
	}

	@Override
	public void setSite(State trialState, int site, int level, int whoVal, int whatVal, int countVal, int stateVal,
			int rotationVal, int valueVal)
	{
		setSite(trialState, site, whoVal, whatVal, countVal, stateVal, rotationVal, valueVal, SiteType.Cell);
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
	public void insertVertex(State trialState, int site, int level, int whatValue, int whoId, final int stateVal,
			final int rotationVal, final int valueVal, Game game)
	{
		// Nothing to do.
	}

	@Override
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, int stateVal, int rotationVal,
			int valueVal,
			Game game)
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
	public void insertEdge(State trialState, int site, int level, int whatValue, int whoId, final int stateVal,
			final int rotationVal, final int valueVal, Game game)
	{
		// Nothing to do.
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, int stateVal, int rotationVal,
			int valueVal,
			Game game)
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
			int valueVal,
			Game game,
			SiteType graphElementType)
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
	public void addToEmpty(final int site, final SiteType graphType)
	{
		addToEmptyCell(site);
	}

	@Override
	public void removeFromEmpty(final int site, final SiteType graphType)
	{
		removeFromEmptyCell(site);
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
		return who;
	}
	
	@Override
	public ChunkSet emptyChunkSetCell()
	{
		return empty.bitSet();
	}
	@Override public ChunkSet emptyChunkSetVertex() { return null; }
	@Override public ChunkSet emptyChunkSetEdge() { return null; }
	
	@Override 
	public int numChunksWhoCell() 
	{ 
		return who.numChunks(); 
	}
	@Override public int numChunksWhoVertex() { return Constants.UNDEFINED; }
	@Override public int numChunksWhoEdge() { return Constants.UNDEFINED; }
	
	@Override 
	public int chunkSizeWhoCell() 
	{ 
		return who.chunkSize(); 
	}
	@Override public int chunkSizeWhoVertex() { return Constants.UNDEFINED; }
	@Override public int chunkSizeWhoEdge() { return Constants.UNDEFINED; }
	
	@Override 
	public int numChunksWhatCell() 
	{ 
		return defaultIfNull(what).numChunks(); 
	}
	@Override public int numChunksWhatVertex() { return Constants.UNDEFINED; }
	@Override public int numChunksWhatEdge() { return Constants.UNDEFINED; }
	
	@Override 
	public int chunkSizeWhatCell() 
	{ 
		return defaultIfNull(what).chunkSize(); 
	}
	@Override public int chunkSizeWhatVertex() { return Constants.UNDEFINED; }
	@Override public int chunkSizeWhatEdge() { return Constants.UNDEFINED; }
	
	@Override 
	public boolean matchesWhoCell(final ChunkSet mask, final ChunkSet pattern) 
	{ 
		return who.matches(mask, pattern); 
	}
	@Override public boolean matchesWhoVertex(final ChunkSet mask, final ChunkSet pattern) { return false; }
	@Override public boolean matchesWhoEdge(final ChunkSet mask, final ChunkSet pattern) { return false; }
	
	@Override
	public boolean matchesWhoCell(final int wordIdx, final long mask, final long matchingWord)
	{
		return who.matches(wordIdx, mask, matchingWord);
	}

	@Override public boolean matchesWhoVertex(final int wordIdx, final long mask, final long matchingWord) { return false; }
	@Override public boolean matchesWhoEdge(final int wordIdx, final long mask, final long matchingWord) { return false; }
	
	@Override 
	public boolean matchesWhatCell(final int wordIdx, final long mask, final long matchingWord) 
	{ 
		return defaultIfNull(what).matches(wordIdx, mask, matchingWord);
	}

	@Override public boolean matchesWhatVertex(final int wordIdx, final long mask, final long matchingWord) { return false; }
	@Override public boolean matchesWhatEdge(final int wordIdx, final long mask, final long matchingWord) { return false; }
	
	@Override 
	public boolean violatesNotWhoCell(final ChunkSet mask, final ChunkSet pattern) 
	{ 
		return who.violatesNot(mask, pattern); 
	}
	@Override public boolean violatesNotWhoVertex(final ChunkSet mask, final ChunkSet pattern) { return false; }
	@Override public boolean violatesNotWhoEdge(final ChunkSet mask, final ChunkSet pattern) { return false; }
	
	@Override 
	public boolean violatesNotWhoCell(final ChunkSet mask, final ChunkSet pattern, final int startWord) 
	{ 
		return who.violatesNot(mask, pattern, startWord); 
	}
	@Override public boolean violatesNotWhoVertex(final ChunkSet mask, final ChunkSet pattern, final int startWord) { return false; }
	@Override public boolean violatesNotWhoEdge(final ChunkSet mask, final ChunkSet pattern, final int startWord) { return false; }
	
	@Override 
	public boolean matchesWhatCell(final ChunkSet mask, final ChunkSet pattern) 
	{ 
		return defaultIfNull(what).matches(mask, pattern); 
	}
	@Override public boolean matchesWhatVertex(final ChunkSet mask, final ChunkSet pattern) { return false; }
	@Override public boolean matchesWhatEdge(final ChunkSet mask, final ChunkSet pattern) { return false; }
	
	@Override public boolean violatesNotWhatCell(final ChunkSet mask, final ChunkSet pattern) 
	{ 
		return defaultIfNull(what).violatesNot(mask, pattern); 
	}
	@Override public boolean violatesNotWhatVertex(final ChunkSet mask, final ChunkSet pattern) { return false; }
	@Override public boolean violatesNotWhatEdge(final ChunkSet mask, final ChunkSet pattern) { return false; }
	
	@Override public boolean violatesNotWhatCell(final ChunkSet mask, final ChunkSet pattern, final int startWord) 
	{ 
		return defaultIfNull(what).violatesNot(mask, pattern, startWord); 
	}
	@Override public boolean violatesNotWhatVertex(final ChunkSet mask, final ChunkSet pattern, final int startWord) { return false; }
	@Override public boolean violatesNotWhatEdge(final ChunkSet mask, final ChunkSet pattern, final int startWord) { return false; }
	
	@Override public ChunkSet cloneWhoCell() 
	{ 
		return who.internalStateCopy(); 
	}
	@Override public ChunkSet cloneWhoVertex() { return null; }
	@Override public ChunkSet cloneWhoEdge() { return null; }
	
	@Override public ChunkSet cloneWhatCell() 
	{ 
		return defaultIfNull(what).internalStateCopy(); 
	}
	@Override public ChunkSet cloneWhatVertex() { return null; }
	@Override public ChunkSet cloneWhatEdge() { return null; }

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
}
