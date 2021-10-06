package main.collections;

import java.io.Serializable;

import main.math.BitTwiddling;

/**
 * The three possible ChunkSets for each level of each site (for stacking
 * games).
 * 
 * @author Eric.Piette
 *
 */
public final class ChunkStack implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** 'what' */
	public static final int TYPE_DEFAULT_STATE = 0;
	/** 'what' + 'who' */
	public static final int TYPE_PLAYER_STATE = 1;
	/** 'what' + 'who' + 'state' + 'rotation' + 'value' */
	public static final int TYPE_INDEX_STATE = 2;
	
	/**
	 * if
	 * 
	 * type == 1 --> Player State
	 * 
	 * type == 2 --> Index State
	 * 
	 * type == 3 --> index local state, rotation, value.
	 */
	protected final int type;

	/** What ChunkSet. */
	private final ChunkSet what;

	/** Who ChunkSet. */
	private final ChunkSet who;

	/** State ChunkSet. */
	private final ChunkSet state;
	
	/** Rotation ChunkSet. */
	private final ChunkSet rotation;

	/** Value ChunkSet. */
	private final ChunkSet value;
	
	/** Hidden Array ChunkSet. */
	private final ChunkSet[] hidden;

	/** Hidden What Array ChunkSet. */
	private final ChunkSet[] hiddenWhat;

	/** Hidden Who Array ChunkSet. */
	private final ChunkSet[] hiddenWho;

	/** Hidden Count Array ChunkSet. */
	private final ChunkSet[] hiddenCount;

	/** Hidden State Array ChunkSet. */
	private final ChunkSet[] hiddenState;

	/** Hidden Rotation Array ChunkSet. */
	private final ChunkSet[] hiddenRotation;

	/** Hidden Value Array ChunkSet. */
	private final ChunkSet[] hiddenValue;

	/** The number of components on the stack. */
	private int size;

	/**
	 * 
	 * Constructor.
	 * 
	 * @param numComponents The number of components.
	 * @param numPlayers The number of players.
	 * @param numStates The number of states.
	 * @param numRotation The number of rotations.
	 * @param numValues The number of rotations.
	 * @param type The type of the chunkStack.
	 * @param hidden True if the game involves hidden info.
	 */
	public ChunkStack
	(
			final int numComponents, 
			final int numPlayers, 
			final int numStates, 
			final int numRotation,
			final int numValues,
			final int type, 
			final boolean hidden
	)
	{
		this.type = type;
		this.size = 0;

		final int chunkSizeWhat = BitTwiddling.nextPowerOf2(BitTwiddling.bitsRequired(numComponents));

		// What
		this.what = new ChunkSet(chunkSizeWhat, 1);
		final int chunkSizeWho = BitTwiddling.nextPowerOf2(BitTwiddling.bitsRequired(numPlayers + 1));
		
		// Who
		if (type > 0)
			this.who = new ChunkSet(chunkSizeWho, 1);
		else
			this.who = null;

		final int chunkSizeState = BitTwiddling.nextPowerOf2(BitTwiddling.bitsRequired(numStates));

		// State
		if (type > 1)
			this.state = new ChunkSet(chunkSizeState, 1);
		else
			this.state = null;

		final int chunkSizeRotation = BitTwiddling.nextPowerOf2(BitTwiddling.bitsRequired(numRotation));

		// Rotation
		if (type >= 2)
			this.rotation = new ChunkSet(chunkSizeRotation, 1);
		else
			this.rotation = null;

		final int chunkSizeValue = BitTwiddling.nextPowerOf2(BitTwiddling.bitsRequired(numValues));

		// Value
		if (type >= 2)
			this.value = new ChunkSet(chunkSizeValue, 1);
		else
			this.value = null;

		// Hidden info
		if (hidden)
		{
			final int chunkSizeHidden = BitTwiddling.nextPowerOf2(BitTwiddling.bitsRequired(2));

			this.hidden = new ChunkSet[numPlayers + 1];
			for (int i = 1; i < numPlayers + 1; i++)
				this.hidden[i] = new ChunkSet(chunkSizeHidden, 1);
			this.hiddenWhat = new ChunkSet[numPlayers + 1];
			for (int i = 1; i < numPlayers + 1; i++)
				this.hiddenWhat[i] = new ChunkSet(chunkSizeHidden, 1);
			
			if (type > 0)
			{
				this.hiddenWho = new ChunkSet[numPlayers + 1];
				for (int i = 1; i < numPlayers + 1; i++)
					this.hiddenWho[i] = new ChunkSet(chunkSizeHidden, 1);
				
				if (type > 1)
				{
					this.hiddenState = new ChunkSet[numPlayers + 1];
					for (int i = 1; i < numPlayers + 1; i++)
						this.hiddenState[i] = new ChunkSet(chunkSizeHidden, 1);
					
					if (type >= 2)
					{
						this.hiddenRotation = new ChunkSet[numPlayers + 1];
						for (int i = 1; i < numPlayers + 1; i++)
							this.hiddenRotation[i] = new ChunkSet(chunkSizeHidden, 1);
						this.hiddenCount = new ChunkSet[numPlayers + 1];
						for (int i = 1; i < numPlayers + 1; i++)
							this.hiddenCount[i] = new ChunkSet(chunkSizeHidden, 1);
						this.hiddenValue = new ChunkSet[numPlayers + 1];
						for (int i = 1; i < numPlayers + 1; i++)
							this.hiddenValue[i] = new ChunkSet(chunkSizeHidden, 1);
					}
					else
					{
						this.hiddenRotation = null;
						this.hiddenCount = null;
						this.hiddenValue = null;
					}
				}
				else
				{
					this.hiddenState = null;
					this.hiddenRotation = null;
					this.hiddenCount = null;
					this.hiddenValue = null;
				}
			}
			else
			{
				this.hiddenWho = null;
				this.hiddenState = null;
				this.hiddenRotation = null;
				this.hiddenCount = null;
				this.hiddenValue = null;
			}
		}
		else
		{
			this.hidden = null;
			this.hiddenWhat = null;
			this.hiddenWho = null;
			this.hiddenState = null;
			this.hiddenRotation = null;
			this.hiddenCount = null;
			this.hiddenValue = null;
		}
	}
	
	/**
	 * Copy constructor
	 * @param other
	 */
	public ChunkStack(final ChunkStack other)
	{
		this.type = other.type;
		this.size = other.size;
		
		this.what = (other.what == null) ? null : other.what.clone();
		this.who = (other.who == null) ? null : other.who.clone();
		this.state = (other.state == null) ? null : other.state.clone();
		this.rotation = (other.rotation == null) ? null : other.rotation.clone();
		this.value = (other.value == null) ? null : other.value.clone();
		
		if (other.hidden == null)
		{
			this.hidden = null;
			this.hiddenWhat = null;
			this.hiddenWho = null;
			this.hiddenState = null;
			this.hiddenRotation = null;
			this.hiddenCount = null;
			this.hiddenValue = null;
		}
		else
		{
			this.hidden = new ChunkSet[other.hidden.length];
			this.hiddenWhat = new ChunkSet[other.hiddenWhat.length];
			for (int i = 1; i < this.hidden.length; ++i)
			{
				this.hidden[i] = other.hidden[i].clone();
				this.hiddenWhat[i] = other.hiddenWhat[i].clone();
			}
			
			if (type > 0)
			{
				this.hiddenWho = new ChunkSet[other.hiddenWho.length];
				for (int i = 1; i < this.hiddenWho.length; ++i)
				{
					this.hiddenWho[i] = other.hiddenWho[i].clone();
				}
				
				if (type > 1)
				{
					this.hiddenState = new ChunkSet[other.hiddenState.length];
					for (int i = 1; i < this.hiddenState.length; ++i)
					{
						this.hiddenState[i] = other.hiddenState[i].clone();
					}
					
					if (type >= 2)
					{
						this.hiddenRotation = new ChunkSet[other.hiddenRotation.length];
						this.hiddenCount = new ChunkSet[other.hiddenCount.length];
						this.hiddenValue = new ChunkSet[other.hiddenValue.length];
						
						for (int i = 1; i < this.hiddenRotation.length; ++i)
						{
							this.hiddenRotation[i] = other.hiddenRotation[i].clone();
							this.hiddenCount[i] = other.hiddenCount[i].clone();
							this.hiddenValue[i] = other.hiddenValue[i].clone();
						}
					}
					else
					{
						this.hiddenRotation = null;
						this.hiddenCount = null;
						this.hiddenValue = null;
					}
				}
				else
				{
					this.hiddenState = null;
					this.hiddenRotation = null;
					this.hiddenCount = null;
					this.hiddenValue = null;
				}
			}
			else
			{
				this.hiddenWho = null;
				this.hiddenState = null;
				this.hiddenRotation = null;
				this.hiddenCount = null;
				this.hiddenValue = null;
			}
		}
	}

	/**
	 * @return what ChunkSet.
	 */
	public ChunkSet whatChunkSet()
	{
		return what;
	}

	/**
	 * @return who ChunkSet.
	 */
	public ChunkSet whoChunkSet()
	{
		return who;
	}

	/**
	 * @return local state ChunkSet.
	 */
	public ChunkSet stateChunkSet()
	{
		return state;
	}

	/**
	 * @return rotation state ChunkSet.
	 */
	public ChunkSet rotationChunkSet()
	{
		return rotation;
	}

	/**
	 * @return value state ChunkSet.
	 */
	public ChunkSet valueChunkSet()
	{
		return value;
	}

	/**
	 * 
	 * @return hidden info array ChunkSet.
	 */
	public ChunkSet[] hidden()
	{
		return hidden;
	}

	/**
	 * 
	 * @return hidden what info array ChunkSet.
	 */
	public ChunkSet[] hiddenWhat()
	{
		return hiddenWhat;
	}

	/**
	 * 
	 * @return hidden who info array ChunkSet.
	 */
	public ChunkSet[] hiddenWho()
	{
		return hiddenWho;
	}

	/**
	 * 
	 * @return hidden state info array ChunkSet.
	 */
	public ChunkSet[] hiddenState()
	{
		return hiddenState;
	}

	/**
	 * 
	 * @return hidden rotation info array ChunkSet.
	 */
	public ChunkSet[] hiddenRotation()
	{
		return hiddenRotation;
	}

	/**
	 * 
	 * @return hidden count info array ChunkSet.
	 */
	public ChunkSet[] hiddenCount()
	{
		return hiddenCount;
	}

	/**
	 * 
	 * @return hidden piece value info array ChunkSet.
	 */
	public ChunkSet[] hiddenValue()
	{
		return hiddenValue;
	}

	
	/**
	 * @return Current size of the stack
	 */
	public int size() 
	{
		return this.size;
	}
	
	/**
	 * Size ++
	 */
	public void incrementSize() 
	{
		this.size++;
	}
	
	/**
	 * Size --
	 */
	public void decrementSize() 
	{
		if (size > 0)
			this.size--;
	}
	
	//--------------------- State -------------------------
	
	/**
	 * @return state of the top.
	 */
	public int state()
	{
		if (type >= 2 && size > 0)
			return state.getChunk(size-1);
		return 0;
	}
	
	/**
	 * @param level
	 * @return state.
	 */
	public int state(final int level)
	{
		if (type >= 2 && level < size)
			return state.getChunk(level);
		return 0;
	}

	/**
	 * Set state.
	 * 
	 * @param val
	 */
	public void setState(final int val)
	{
		if (type >= 2 && size > 0)
			state.setChunk(size-1, val);
	}
	
	/**
	 * Set state.
	 * 
	 * @param val
	 * @param level
	 */
	public void setState(final int val, final int level)
	{
		if (type >= 2 && level < size)
			state.setChunk(level, val);
	}

	//----------------------- Rotation ----------------------------
	
	/**
	 * @return rotation of the top.
	 */
	public int rotation()
	{
		if (type >= 2 && size > 0)
			return rotation.getChunk(size - 1);
		return 0;
	}

	/**
	 * @param level
	 * @return rotation.
	 */
	public int rotation(final int level)
	{
		if (type >= 2 && level < size)
			return rotation.getChunk(level);
		return 0;
	}

	/**
	 * Set rotation.
	 * 
	 * @param val
	 */
	public void setRotation(final int val)
	{
		if (type >= 2 && size > 0)
			rotation.setChunk(size - 1, val);
	}

	/**
	 * Set rotation.
	 * 
	 * @param val
	 * @param level
	 */
	public void setRotation(final int val, final int level)
	{
		if (type >= 2 && level < size)
			rotation.setChunk(level, val);
	}
	
	//--------------------------- Value ---------------------------------

	/**
	 * @return value of the top.
	 */
	public int value()
	{
		if (type >= 2 && size > 0)
			return value.getChunk(size - 1);
		return 0;
	}

	/**
	 * @param level
	 * @return value.
	 */
	public int value(final int level)
	{
		if (type >= 2 && level < size)
			return value.getChunk(level);
		return 0;
	}

	/**
	 * Set value.
	 * 
	 * @param val
	 */
	public void setValue(final int val)
	{
		if (type >= 2 && size > 0)
			value.setChunk(size - 1, val);
	}

	/**
	 * Set value.
	 * 
	 * @param val
	 * @param level
	 */
	public void setValue(final int val, final int level)
	{
		if (type >= 2 && level < size)
			value.setChunk(level, val);
	}

	//--------------------------- What ------------------------------
	
	/**
	 * @return what.
	 */
	public int what()
	{
		if (size > 0)
			return what.getChunk(size-1);
		return 0;
	}
	
	/**
	 * @param level
	 * @return what.
	 */
	public int what(final int level) 
	{
		if (level < size)
			return what.getChunk(level);
		return 0;
	}

	/**
	 * Set what.
	 * 
	 * @param val
	 */
	public void setWhat(final int val)
	{
		if (size > 0)
			what.setChunk(size-1, val);
	}
	
	/**
	 * Set what.
	 * 
	 * @param val
	 * @param level
	 */
	public void setWhat(final int val, final int level)
	{
		if (level < size)
			what.setChunk(level, val);
	}

	//--------------------------- Who -----------------------------
	
	/**
	 * @return who.
	 */
	public int who()
	{
		if (size > 0) 
		{
			if (type > 0)
				return who.getChunk(size-1);
			return what.getChunk(size-1);
		}
		return 0;
	}

	/**
	 * @return who.
	 */
	public int who(final int level)
	{
		if (level < size) 
		{
			if (type > 0)
				return who.getChunk(level);
			return what.getChunk(level);
		}
		return 0;
	}
	
	/**
	 * Set who.
	 * 
	 * @param val
	 */

	public void setWho(final int val)
	{
		if (size > 0)
		{
			if (type > 0)
				who.setChunk(size-1, val);
		}
	}
	
	/**
	 * Set who.
	 * 
	 * @param val
	 */

	public void setWho(final int val, final int level)
	{
		if (level < size)
		{
			if (type > 0)
				who.setChunk(level, val);
		}
	}

	//--------------------------- Hidden Info All -----------------------------
	
	/**
	 * @param pid The player id.
	 * @return True if the site has some hidden information for the player.
	 */
	public boolean isHidden(final int pid)
	{
		if (this.hidden != null && size > 0)
			return this.hidden[pid].getChunk(size - 1) == 1;

		return false;
	}

	/**
	 * @param pid   The player id.
	 * @param level The level.
	 * @return True if the site has some hidden information for the player at that
	 *         level.
	 */
	public boolean isHidden(final int pid, final int level)
	{
		if (this.hidden != null && level < size)
			return this.hidden[pid].getChunk(level) == 1;

		return false;
	}

	/**
	 * Set Hidden for a player.
	 * 
	 * @param pid
	 */
	public void setHidden(final int pid, final boolean on)
	{
		if (this.hidden != null && size > 0)
			this.hidden[pid].setChunk(size - 1, on ? 1 : 0);
	}

	/**
	 * Set Hidden for a player at a specific level.
	 * 
	 * @param pid   The player id.
	 * @param level The level.
	 */
	public void setHidden(final int pid, final int level, final boolean on)
	{
		if (this.hidden != null && level < size)
			this.hidden[pid].setChunk(level, on ? 1 : 0);
	}

	//--------------------------- Hidden Info What -----------------------------
	
	/**
	 * @param pid The player id.
	 * @return True if for the site the what information is hidden for the player.
	 */
	public boolean isHiddenWhat(final int pid)
	{
		if (this.hiddenWhat != null && size > 0)
			return this.hiddenWhat[pid].getChunk(size - 1) == 1;

		return false;
	}

	/**
	 * @param pid   The player id.
	 * @param level The level.
	 * @return True if for the site the what information is hidden for the player at
	 *         that level.
	 */
	public boolean isHiddenWhat(final int pid, final int level)
	{
		if (this.hiddenWhat != null && level < size)
			return this.hiddenWhat[pid].getChunk(level) == 1;

		return false;
	}

	/**
	 * Set what Hidden for a player.
	 * 
	 * @param pid
	 */
	public void setHiddenWhat(final int pid, final boolean on)
	{
		if (this.hiddenWhat != null && size > 0)
			this.hidden[pid].setChunk(size - 1, on ? 1 : 0);
	}

	/**
	 * Set What Hidden for a player at a specific level.
	 * 
	 * @param pid   The player id.
	 * @param level The level.
	 */
	public void setHiddenWhat(final int pid, final int level, final boolean on)
	{
		if (this.hiddenWhat != null && level < size)
			this.hiddenWhat[pid].setChunk(level, on ? 1 : 0);
	}

	//--------------------------- Hidden Info Who -----------------------------
	
	
	/**
	 * @param pid The player id.
	 * @return True if for the site the who information is hidden for the player.
	 */
	public boolean isHiddenWho(final int pid)
	{
		if (this.hiddenWho != null && size > 0)
			return this.hiddenWho[pid].getChunk(size - 1) == 1;

		return false;
	}

	/**
	 * @param pid   The player id.
	 * @param level The level.
	 * @return True if for the site the who information is hidden for the player at
	 *         that level.
	 */
	public boolean isHiddenWho(final int pid, final int level)
	{
		if (this.hiddenWho != null && level < size)
			return this.hiddenWho[pid].getChunk(level) == 1;

		return false;
	}

	/**
	 * Set Who Hidden for a player.
	 * 
	 * @param pid
	 */
	public void setHiddenWho(final int pid, final boolean on)
	{
		if (this.hiddenWho != null && size > 0)
			this.hiddenWho[pid].setChunk(size - 1, on ? 1 : 0);
	}

	/**
	 * Set Who Hidden for a player at a specific level.
	 * 
	 * @param pid   The player id.
	 * @param level The level.
	 */
	public void setHiddenWho(final int pid, final int level, final boolean on)
	{
		if (this.hiddenWho != null && level < size)
			this.hiddenWho[pid].setChunk(level, on ? 1 : 0);
	}

	//--------------------------- Hidden Info State -----------------------------
	
	/**
	 * @param pid The player id.
	 * @return True if for the site the state information is hidden for the player.
	 */
	public boolean isHiddenState(final int pid)
	{
		if (this.hiddenState != null && size > 0)
			return this.hiddenState[pid].getChunk(size - 1) == 1;

		return false;
	}

	/**
	 * @param pid   The player id.
	 * @param level The level.
	 * @return True if for the site the state information is hidden for the player
	 *         at that level.
	 */
	public boolean isHiddenState(final int pid, final int level)
	{
		if (this.hiddenState != null && level < size)
			return this.hiddenState[pid].getChunk(level) == 1;

		return false;
	}

	/**
	 * Set State Hidden for a player.
	 * 
	 * @param pid
	 */
	public void setHiddenState(final int pid, final boolean on)
	{
		if (this.hiddenState != null && size > 0)
			this.hiddenState[pid].setChunk(size - 1, on ? 1 : 0);
	}

	/**
	 * Set State Hidden for a player at a specific level.
	 * 
	 * @param pid   The player id.
	 * @param level The level.
	 */
	public void setHiddenState(final int pid, final int level, final boolean on)
	{
		if (this.hiddenState != null && level < size)
			this.hiddenState[pid].setChunk(level, on ? 1 : 0);
	}

	//--------------------------- Hidden Info Rotation -----------------------------
	
	/**
	 * @param pid The player id.
	 * @return True if for the site the rotation information is hidden for the
	 *         player.
	 */
	public boolean isHiddenRotation(final int pid)
	{
		if (this.hiddenRotation != null && size > 0)
			return this.hiddenRotation[pid].getChunk(size - 1) == 1;

		return false;
	}

	/**
	 * @param pid   The player id.
	 * @param level The level.
	 * @return True if for the site the rotation information is hidden for the
	 *         player at that level.
	 */
	public boolean isHiddenRotation(final int pid, final int level)
	{
		if (this.hiddenRotation != null && level < size)
			return this.hiddenRotation[pid].getChunk(level) == 1;

		return false;
	}

	/**
	 * Set Rotation Hidden for a player.
	 * 
	 * @param pid
	 */
	public void setHiddenRotation(final int pid, final boolean on)
	{
		if (this.hiddenRotation != null && size > 0)
			this.hiddenRotation[pid].setChunk(size - 1, on ? 1 : 0);
	}

	/**
	 * Set Rotation Hidden for a player at a specific level.
	 * 
	 * @param pid   The player id.
	 * @param level The level.
	 */
	public void setHiddenRotation(final int pid, final int level, final boolean on)
	{
		if (this.hiddenRotation != null && level < size)
			this.hiddenRotation[pid].setChunk(level, on ? 1 : 0);
	}

	//--------------------------- Hidden Info Count -----------------------------
	
	/**
	 * @param pid The player id.
	 * @return True if for the site the count information is hidden for the player.
	 */
	public boolean isHiddenCount(final int pid)
	{
		if (this.hiddenCount != null && size > 0)
			return this.hiddenCount[pid].getChunk(size - 1) == 1;

		return false;
	}

	/**
	 * @param pid   The player id.
	 * @param level The level.
	 * @return True if for the site the count information is hidden for the player
	 *         at that level.
	 */
	public boolean isHiddenCount(final int pid, final int level)
	{
		if (this.hiddenCount != null && level < size)
			return this.hiddenCount[pid].getChunk(level) == 1;

		return false;
	}

	/**
	 * Set Count Hidden for a player.
	 * 
	 * @param pid
	 */
	public void setHiddenCount(final int pid, final boolean on)
	{
		if (this.hiddenCount != null && size > 0)
			this.hiddenCount[pid].setChunk(size - 1, on ? 1 : 0);
	}

	/**
	 * Set Count Hidden for a player at a specific level.
	 * 
	 * @param pid   The player id.
	 * @param level The level.
	 */
	public void setHiddenCount(final int pid, final int level, final boolean on)
	{
		if (this.hiddenCount != null && level < size)
			this.hiddenCount[pid].setChunk(level, on ? 1 : 0);
	}

	//--------------------------- Hidden Info Value -----------------------------
	
	/**
	 * @param pid The player id.
	 * @return True if for the site the value information is hidden for the player.
	 */
	public boolean isHiddenValue(final int pid)
	{
		if (this.hiddenValue != null && size > 0)
			return this.hiddenValue[pid].getChunk(size - 1) == 1;

		return false;
	}

	/**
	 * @param pid   The player id.
	 * @param level The level.
	 * @return True if for the site the value information is hidden for the player
	 *         at that level.
	 */
	public boolean isHiddenValue(final int pid, final int level)
	{
		if (this.hiddenValue != null && level < size)
			return this.hiddenValue[pid].getChunk(level) == 1;

		return false;
	}

	/**
	 * Set Value Hidden for a player.
	 * 
	 * @param pid
	 */
	public void setHiddenValue(final int pid, final boolean on)
	{
		if (this.hiddenValue != null && size > 0)
			this.hiddenValue[pid].setChunk(size - 1, on ? 1 : 0);
	}

	/**
	 * Set Value Hidden for a player at a specific level.
	 * 
	 * @param pid   The player id.
	 * @param level The level.
	 */
	public void setHiddenValue(final int pid, final int level, final boolean on)
	{
		if (this.hiddenValue != null && level < size)
			this.hiddenValue[pid].setChunk(level, on ? 1 : 0);
	}

}
