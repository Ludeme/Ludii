package main.collections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gnu.trove.list.array.TIntArrayList;

/**
 * The three possible lists for each level of each site (for card
 * games).
 * 
 * @author Eric.Piette
 *
 */
public final class ListStack implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/** 'what' */
	public static final int TYPE_DEFAULT_STATE = 0;
	/** 'what' + 'who' */
	public static final int TYPE_PLAYER_STATE = 1;
	/** 'what' + 'who' + 'state' */
	public static final int TYPE_INDEX_STATE = 2;
	/** Just store 'who' */
	public static final int TYPE_INDEX_LOCAL_STATE = 3;
	
	/**
	 * if
	 * 
	 * type == 1 --> Player State
	 * 
	 * type == 2 --> Index State
	 * 
	 * type == 3 --> index local state, rotation, value.
	 */
	private final int type;
	
	/** What ChunkSet. */
	private final TIntArrayList what;

	/** Who ChunkSet. */
	private final TIntArrayList who;

	/** State ChunkSet. */
	private final TIntArrayList state;
	
	/** Rotation ChunkSet. */
	private final TIntArrayList rotation;

	/** Value ChunkSet. */
	private final TIntArrayList value;

	/** Hidden Information. */
	private final List<TIntArrayList> hidden;

	/** Hidden What Information. */
	private final List<TIntArrayList> hiddenWhat;

	/** Hidden Who Information. */
	private final List<TIntArrayList> hiddenWho;

	/** Hidden Count Information. */
	private final List<TIntArrayList> hiddenCount;

	/** Hidden State Information. */
	private final List<TIntArrayList> hiddenState;

	/** Hidden Rotation Information. */
	private final List<TIntArrayList> hiddenRotation;

	/** Hidden Value Information. */
	private final List<TIntArrayList> hiddenValue;
	
	/** The number of components on the stack */
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
	public ListStack
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
		size = 0;

		// What
		what = new TIntArrayList();
		
		// Who
		if (type > 0)
			who = new TIntArrayList();
		else
			who = null;

		// State
		if (type > 1)
			state = new TIntArrayList();
		else
			state = null;

		// Rotation and Value 
		if (type >= 2)
		{
			rotation = new TIntArrayList();
			value = new TIntArrayList();
		}
		else
		{
			rotation = null;
			value = null;
		}
		

		if(hidden)
		{
			this.hidden = new ArrayList<TIntArrayList>();
			this.hidden.add(null);
			for (int i = 1; i < numPlayers + 1; i++)
				this.hidden.add(new TIntArrayList());
			
			this.hiddenWhat = new ArrayList<TIntArrayList>();
			this.hiddenWhat.add(null);
			for (int i = 1; i < numPlayers + 1; i++)
				this.hiddenWhat.add(new TIntArrayList());
			
			if (type > 0)
			{
				this.hiddenWho = new ArrayList<TIntArrayList>();
				this.hiddenWho.add(null);
				for (int i = 1; i < numPlayers + 1; i++)
					this.hiddenWho.add(new TIntArrayList());

				if (type > 1)
				{
					this.hiddenState = new ArrayList<TIntArrayList>();
					this.hiddenState.add(null);
					for (int i = 1; i < numPlayers + 1; i++)
						this.hiddenState.add(new TIntArrayList());

					if (type >= 2)
					{
						this.hiddenCount = new ArrayList<TIntArrayList>();
						this.hiddenCount.add(null);
						for (int i = 1; i < numPlayers + 1; i++)
							this.hiddenCount.add(new TIntArrayList());
						
						this.hiddenRotation = new ArrayList<TIntArrayList>();
						this.hiddenRotation.add(null);
						for (int i = 1; i < numPlayers + 1; i++)
							this.hiddenRotation.add(new TIntArrayList());
						
						this.hiddenValue = new ArrayList<TIntArrayList>();
						this.hiddenValue.add(null);
						for (int i = 1; i < numPlayers + 1; i++)
							this.hiddenValue.add(new TIntArrayList());
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
			this.hiddenCount = null;
			this.hiddenState = null;
			this.hiddenRotation = null;
			this.hiddenValue = null;
		}
	}
	
	/**
	 * Copy constructor
	 * @param other
	 */
	public ListStack(final ListStack other)
	{
		type = other.type;
		size = other.size;
		
		what = (other.what == null) ? null : new TIntArrayList(other.what);
		who = (other.who == null) ? null : new TIntArrayList(other.who);
		state = (other.state == null) ? null : new TIntArrayList(other.state);
		rotation = (other.rotation == null) ? null : new TIntArrayList(other.rotation);
		value = (other.value == null) ? null : new TIntArrayList(other.value);
		
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
			hidden = new ArrayList<TIntArrayList>(other.hidden);
			hiddenWhat = new ArrayList<TIntArrayList>(other.hiddenWhat);
			for (int i = 1; i < this.hidden.size(); ++i)
			{
				this.hidden.set(i, new TIntArrayList(other.hidden.get(i)));
				this.hiddenWhat.set(i, new TIntArrayList(other.hiddenWhat.get(i)));
			}

			if (type > 0)
			{
				hiddenWho = new ArrayList<TIntArrayList>(other.hiddenWho);
				for (int i = 1; i < this.hiddenWho.size(); ++i)
					this.hiddenWho.set(i, new TIntArrayList(other.hiddenWho.get(i)));

				if (type > 1)
				{
					hiddenState = new ArrayList<TIntArrayList>(other.hiddenState);
					for (int i = 1; i < this.hiddenState.size(); ++i)
						this.hiddenState.set(i, new TIntArrayList(other.hiddenState.get(i)));
					
					if (type >= 2)
					{
						hiddenCount = new ArrayList<TIntArrayList>(other.hiddenCount);
						for (int i = 1; i < this.hiddenCount.size(); ++i)
							this.hiddenCount.set(i, new TIntArrayList(other.hiddenCount.get(i)));

						hiddenRotation = new ArrayList<TIntArrayList>(other.hiddenRotation);
						for (int i = 1; i < this.hiddenRotation.size(); ++i)
							this.hiddenRotation.set(i, new TIntArrayList(other.hiddenRotation.get(i)));
						
						hiddenValue = new ArrayList<TIntArrayList>(other.hiddenValue);
						for (int i = 1; i < this.hiddenValue.size(); ++i)
							this.hiddenValue.set(i, new TIntArrayList(other.hiddenValue.get(i)));
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
	 * 
	 * @return what list.
	 */
	public TIntArrayList whatChunkSet()
	{
		return what;
	}

	/**
	 * 
	 * @return who list.
	 */
	public TIntArrayList whoChunkSet()
	{
		return who;
	}

	/**
	 * 
	 * @return local state list.
	 */
	public TIntArrayList stateChunkSet()
	{
		return state;
	}

	/**
	 * 
	 * @return rotation state list.
	 */
	public TIntArrayList rotationChunkSet()
	{
		return rotation;
	}

	/**
	 * 
	 * @return value state list.
	 */
	public TIntArrayList valueChunkSet()
	{
		return value;
	}

	/**
	 * 
	 * @return hidden list for each player.
	 */
	public List<TIntArrayList> hiddenList()
	{
		return hidden;
	}

	/**
	 * 
	 * @return hidden what list for each player.
	 */
	public List<TIntArrayList> hiddenWhatList()
	{
		return hiddenWhat;
	}

	/**
	 * 
	 * @return hidden who list for each player.
	 */
	public List<TIntArrayList> hiddenWhoList()
	{
		return hiddenWho;
	}

	/**
	 * 
	 * @return hidden Count list for each player.
	 */
	public List<TIntArrayList> hiddenCountList()
	{
		return hiddenCount;
	}

	/**
	 * 
	 * @return hidden Rotation list for each player.
	 */
	public List<TIntArrayList> hiddenRotationList()
	{
		return hiddenRotation;
	}

	/**
	 * 
	 * @return hidden State list for each player.
	 */
	public List<TIntArrayList> hiddenStateList()
	{
		return hiddenState;
	}

	/**
	 * 
	 * @return hidden Value list for each player.
	 */
	public List<TIntArrayList> hiddenValueList()
	{
		return hiddenValue;
	}

	
	/**
	 * @return Current size of the stack
	 */
	public int size() 
	{
		return size;
	}
	
	/**
	 * Size ++
	 */
	public void incrementSize() 
	{
		size++;
	}
	
	/**
	 * Size --
	 */
	public void decrementSize() 
	{
		if (size > 0)
			size--;
	}
	
	/**
	 * To remove the top site on the stack.
	 */
	public void remove() 
	{
		if(what != null && what.size() > 0 )
			what.removeAt(what.size()-1);
		if(who != null && who.size() > 0 )
			who.removeAt(who.size()-1);
		if(state != null && state.size() > 0 )
			state.removeAt(state.size()-1);
		if(rotation != null && rotation.size() > 0 )
			rotation.removeAt(rotation.size()-1);
		if(value != null && value.size() > 0 )
			value.removeAt(value.size()-1);
	}
	
	/**
	 * To remove the a site at a specific level.
	 * @param level The level.
	 */
	public void remove(final int level) 
	{
		if(what != null && what.size() > level && what.size() > 0)
			what.removeAt(level);
		if(who != null && who.size() > level && who.size() > 0)
			who.removeAt(level);
		if(state != null && state.size() > level && state.size() > 0)
			state.removeAt(level);
		if(rotation != null && rotation.size() > level && rotation.size() > 0)
			rotation.removeAt(level);
		if(value != null && value.size() > level  && value.size() > 0)
			value.removeAt(level);
	}
	
	//--------------------- State -------------------------
	
	/**
	 * @return state of the top.
	 */
	public int state()
	{
		if (type >= 2 && size > 0 && !state.isEmpty())
			return state.getQuick(state.size() -1);
		return 0;
	}
	
	/**
	 * @param level
	 * @return state.
	 */
	public int state(final int level)
	{
		if (type >= 2 && level < state.size() && level < size)
			return state.getQuick(level);
		return 0;
	}

	/**
	 * Set state.
	 * 
	 * @param val
	 */
	public void setState(final int val)
	{
		if(type >= 2 && size > 0)
			state.add(val);
	}
	
	/**
	 * Set state.
	 * 
	 * @param val
	 * @param level
	 */
	public void setState(final int val, final int level)
	{
		if(type >= 2 && level < state.size() && level < size)
			state.set(level, val);
	}

	//----------------------- Rotation ----------------------------
	
	/**
	 * @return rotation of the top.
	 */
	public int rotation()
	{
		if (type >= 2 && size > 0 && !rotation.isEmpty() && rotation != null)
			return rotation.getQuick(rotation.size() - 1);
		return 0;
	}

	/**
	 * @param level
	 * @return rotation.
	 */
	public int rotation(final int level)
	{
		if (type >= 2 && level < rotation.size() && rotation != null && level < size)
			return rotation.getQuick(level);
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
			rotation.add(val);
	}

	/**
	 * Set rotation.
	 * 
	 * @param val
	 * @param level
	 */
	public void setRotation(final int val, final int level)
	{
		if (type >= 2 && level < rotation.size() && level < size)
			rotation.set(level, val);
	}

	//--------------------------- Value ---------------------------------
	
	/**
	 * @return value of the top.
	 */
	public int value()
	{
		if (type >= 2 && size > 0 && !value.isEmpty() && value != null)
			return value.getQuick(value.size() - 1);
		return 0;
	}

	/**
	 * @param level
	 * @return value.
	 */
	public int value(final int level)
	{
		if (type >= 2 && level < value.size() && value != null && level < size)
			return value.getQuick(level);
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
			value.add(val);
	}

	/**
	 * Set value.
	 * 
	 * @param val
	 * @param level
	 */
	public void setValue(final int val, final int level)
	{
		if (type >= 2 && level < value.size() && level < size)
			value.set(level, val);
	}

	//--------------------------- What ------------------------------
	
	/**
	 * @return what.
	 */
	public int what()
	{
		if (size > 0 && !what.isEmpty())
			return what.getQuick(what.size()-1);
		return 0;
	}
	
	/**
	 * @param level
	 * @return what.
	 */
	public int what(final int level) 
	{
		if (level < size && level < what.size())
			return what.getQuick(level);
		return 0;
	}

	/**
	 * Set what.
	 * 
	 * @param val
	 */
	public void setWhat(final int val)
	{
		what.add(val);
	}
	
	/**
	 * Set what.
	 * 
	 * @param val
	 * @param level
	 */
	public void setWhat(final int val, final int level)
	{
		if(level < size && level < what.size())
			what.set(level, val);
	}

	/**
	 * Set what.
	 * 
	 * @param val
	 * @param level
	 */
	public void insertWhat(final int val, final int level)
	{
		if (level < size && level < what.size())
			what.insert(level, val);
	}

	//--------------------------- Who -----------------------------
	
	/**
	 * @return who.
	 */
	public int who()
	{
		if (size > 0)
		{
			if (type > 0 && !who.isEmpty())
				return who.getQuick(size-1);
			return 0;
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
			if (type > 0 && level < who.size())
				return who.getQuick(level);
			return 0;
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
		if (type > 0)
			who.add(val);
	}
	
	/**
	 * Set who.
	 * 
	 * @param val
	 */

	public void setWho(final int val, final int level)
	{
		if (level < size && level < who.size() && type > 0)
			who.set(level, val);
	}

	//--------------------------- Hidden Info All -----------------------------
	
	/**
	 * @param pid The player id.
	 * @return True if the site has some hidden information for the player.
	 */
	public boolean isHidden(final int pid)
	{
		if (this.hidden != null && size > 0)
			return hidden.get(pid).getQuick(size - 1) == 1;

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
			return hidden.get(pid).getQuick(level) == 1;

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
			hidden.get(pid).set(size - 1, on ? 1 : 0);
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
			hidden.get(pid).set(level, on ? 1 : 0);
	}

	//--------------------------- Hidden Info What -----------------------------
	
	/**
	 * @param pid The player id.
	 * @return True if for the site the what information is hidden for the player.
	 */
	public boolean isHiddenWhat(final int pid)
	{
		if (this.hiddenWhat != null && size > 0)
			return hiddenWhat.get(pid).getQuick(size - 1) == 1;

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
			return hiddenWhat.get(pid).getQuick(level) == 1;

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
			this.hidden.get(pid).setQuick(size - 1, on ? 1 : 0);
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
			this.hiddenWhat.get(pid).setQuick(level, on ? 1 : 0);
	}
	
	//--------------------------- Hidden Info Who -----------------------------
	
	/**
	 * @param pid The player id.
	 * @return True if for the site the who information is hidden for the player.
	 */
	public boolean isHiddenWho(final int pid)
	{
		if (this.hiddenWho != null && size > 0)
			return hiddenWho.get(pid).getQuick(size - 1) == 1;

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
			return hiddenWho.get(pid).getQuick(level) == 1;

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
			this.hiddenWho.get(pid).setQuick(size - 1, on ? 1 : 0);
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
			hiddenWho.get(pid).set(level, on ? 1 : 0);
	}
	
	//--------------------------- Hidden Info State -----------------------------

	/**
	 * @param pid The player id.
	 * @return True if for the site the state information is hidden for the player.
	 */
	public boolean isHiddenState(final int pid)
	{
		if (this.hiddenState != null && size > 0)
			return hiddenState.get(pid).getQuick(size - 1) == 1;

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
			return hiddenState.get(pid).getQuick(level) == 1;

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
			this.hiddenState.get(pid).setQuick(size - 1, on ? 1 : 0);
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
			this.hiddenState.get(pid).setQuick(level, on ? 1 : 0);
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
			return hiddenRotation.get(pid).getQuick(size - 1) == 1;

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
			return hiddenRotation.get(pid).getQuick(level) == 1;

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
			this.hiddenRotation.get(pid).setQuick(size - 1, on ? 1 : 0);
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
			this.hiddenRotation.get(pid).setQuick(level, on ? 1 : 0);
	}

	//--------------------------- Hidden Info Count -----------------------------
	
	/**
	 * @param pid The player id.
	 * @return True if for the site the count information is hidden for the player.
	 */
	public boolean isHiddenCount(final int pid)
	{
		if (this.hiddenCount != null && size > 0)
			return hiddenCount.get(pid).getQuick(size - 1) == 1;

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
			return hiddenCount.get(pid).getQuick(level) == 1;

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
			this.hiddenCount.get(pid).setQuick(size - 1, on ? 1 : 0);
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
			this.hiddenCount.get(pid).setQuick(level, on ? 1 : 0);
	}

	//--------------------------- Hidden Info Value -----------------------------
	
	/**
	 * @param pid The player id.
	 * @return True if for the site the value information is hidden for the player.
	 */
	public boolean isHiddenValue(final int pid)
	{
		if (this.hiddenValue != null && size > 0)
			return hiddenValue.get(pid).getQuick(size - 1) == 1;

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
			return hiddenValue.get(pid).getQuick(level) == 1;

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
			this.hiddenValue.get(pid).setQuick(size - 1, on ? 1 : 0);
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
			hiddenValue.get(pid).set(level, on ? 1 : 0);
	}
}
