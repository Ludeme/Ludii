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

	/** 'what' + 'who' + 'state' + 'rotation' */
	public static final int TYPE_INDEX_ROTATION = 4;

	/** Just store 'who' */
	public static final int TYPE_INDEX_LOCAL_STATE = 3;
	
	/**
	 * if
	 * 
	 * type == 1 --> Player State
	 * 
	 * type == 2 --> Index State
	 * 
	 * type == 3 --> index local state.
	 * 
	 * type == 4 --> rotation local state.
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
	 * @param type
	 */
	public ListStack(final int type)
	{
		this.type = type;
		size = 0;

		// What
		what = new TIntArrayList();
		hidden = new ArrayList<TIntArrayList>();
		hiddenWhat = new ArrayList<TIntArrayList>();
		hiddenWho = new ArrayList<TIntArrayList>();
		hiddenCount = new ArrayList<TIntArrayList>();
		hiddenState = new ArrayList<TIntArrayList>();
		hiddenRotation = new ArrayList<TIntArrayList>();
		hiddenValue = new ArrayList<TIntArrayList>();
		
		// Who
		if (type > 1)
		{
			who = new TIntArrayList();
		}
		else
			who = null;

		// State
		if (type > 2)
		{
			state = new TIntArrayList();
		}
		else
			state = null;

		// State
		if (type > 3)
		{
			rotation = new TIntArrayList();
			value = new TIntArrayList();
		}
		else
		{
			rotation = null;
			value = null;
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
		hidden = (other.hidden == null) ? null : new ArrayList<TIntArrayList>(other.hidden);
		hiddenWhat = (other.hiddenWhat == null) ? null : new ArrayList<TIntArrayList>(other.hiddenWhat);
		hiddenWho = (other.hiddenWho == null) ? null : new ArrayList<TIntArrayList>(other.hiddenWho);
		hiddenCount = (other.hiddenCount == null) ? null : new ArrayList<TIntArrayList>(other.hiddenCount);
		hiddenState = (other.hiddenState == null) ? null : new ArrayList<TIntArrayList>(other.hiddenState);
		hiddenRotation = (other.hiddenRotation == null) ? null : new ArrayList<TIntArrayList>(other.hiddenRotation);
		hiddenValue = (other.hiddenValue == null) ? null : new ArrayList<TIntArrayList>(other.hiddenValue);
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
		if (hidden == null)
			return null;
		return hidden;
	}

	/**
	 * 
	 * @return hidden what list for each player.
	 */
	public List<TIntArrayList> hiddenWhatList()
	{
		if (hiddenWhat == null)
			return null;
		return hiddenWhat;
	}

	/**
	 * 
	 * @return hidden who list for each player.
	 */
	public List<TIntArrayList> hiddenWhoList()
	{
		if (hiddenWho == null)
			return null;
		return hiddenWho;
	}

	/**
	 * 
	 * @return hidden Count list for each player.
	 */
	public List<TIntArrayList> hiddenCountList()
	{
		if (hiddenCount == null)
			return null;
		return hiddenCount;
	}

	/**
	 * 
	 * @return hidden Rotation list for each player.
	 */
	public List<TIntArrayList> hiddenRotationList()
	{
		if (hiddenRotation == null)
			return null;
		return hiddenRotation;
	}

	/**
	 * 
	 * @return hidden State list for each player.
	 */
	public List<TIntArrayList> hiddenStateList()
	{
		if (hiddenState == null)
			return null;
		return hiddenState;
	}

	/**
	 * 
	 * @return hidden Value list for each player.
	 */
	public List<TIntArrayList> hiddenValueList()
	{
		if (hiddenValue == null)
			return null;
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
		{
			size--;
			what.removeAt(what.size() - 1);
			if (who != null)
			{
				who.removeAt(who.size() - 1);
				if (state != null)
					state.removeAt(state.size() - 1);
			}
			hidden.remove(hidden.size() - 1);
		}
	}
	
	/**
	 * @return state of the top.
	 */
	public int state()
	{
		if (type > 2 && size > 0)
			return state.getQuick(size-1);
		return 0;
	}
	
	/**
	 * @param level
	 * @return state.
	 */
	public int state(final int level)
	{
		if (type > 2 && level < size && (level < state.size()))
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
		if(type > 2)
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
		if(type > 2 && level < size)
			state.set(level, val);
	}

	/**
	 * @return rotation of the top.
	 */
	public int rotation()
	{
		if (type > 3 && size > 0)
			return rotation.getQuick(size - 1);
		return 0;
	}

	/**
	 * @param level
	 * @return rotation.
	 */
	public int rotation(final int level)
	{
		if (type > 3 && level < size && (level < rotation.size()))
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
		if (type > 3)
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
		if (type > 3 && level < size)
			rotation.set(level, val);
	}

	/**
	 * @return value of the top.
	 */
	public int value()
	{
		if (type > 3 && size > 0)
			return value.getQuick(size - 1);
		return 0;
	}

	/**
	 * @param level
	 * @return value.
	 */
	public int value(final int level)
	{
		if (type > 3 && level < size && (level < value.size()))
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
		if (type > 3)
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
		if (type > 3 && level < size)
			value.set(level, val);
	}

	/**
	 * @return what.
	 */
	public int what()
	{
		if (size > 0 && ((size-1) < what.size()))
			return what.getQuick(size-1);
		return 0;
	}
	
	/**
	 * @param level
	 * @return what.
	 */
	public int what(final int level) 
	{
		if (level < size && (level < what.size()))
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
		if(level < size)
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
		if (level < size)
			what.insert(level, val);
	}

	/**
	 * @return who.
	 */
	public int who()
	{
		if (size > 0  && ((size-1) < who.size())) 
		{
			if (type > 0)
				return who.getQuick(size-1);
			return what.getQuick(size-1);
		}
		return 0;
	}

	/**
	 * @return who.
	 */
	public int who(final int level)
	{
		if (level < size  && ((level) < what.size())) 
		{
			if (type > 0)
				return who.getQuick(level);
			return what.getQuick(level);
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
		if (level < size)
		{
			if(type > 0)
				who.set(level, val);
		}
	}

	/**
	 * Set who.
	 * 
	 * @param val
	 */

	public void insertWho(final int val, final int level)
	{
		if (level < size)
		{
			if (type > 0)
				who.insert(level, val);
		}
	}

	/**
	 * @param pid The player id.
	 * @return True if the site has some hidden information for the player.
	 */
	public boolean isHidden(final int pid)
	{
		if (this.hidden != null && size > 0)
			return hidden.get(size - 1).getQuick(pid) == 1;

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
			return hidden.get(level).getQuick(pid) == 1;

		return false;
	}

	/**
	 * Set Hidden for a player at a specific level.
	 * 
	 * @param pid   The player id.
	 * @param level The level.
	 */
	public void setHidden(final int pid, final int level, final boolean on)
	{
		if (level < size && (level < hidden.size()))
			hidden.get(level).set(pid, on ? 1 : 0);
	}

	/**
	 * @param pid The player id.
	 * @return True if for the site the what information is hidden for the player.
	 */
	public boolean isHiddenWhat(final int pid)
	{
		if (this.hiddenWhat != null && size > 0)
			return hiddenWhat.get(size - 1).getQuick(pid) == 1;

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
			return hiddenWhat.get(level).getQuick(pid) == 1;

		return false;
	}

	/**
	 * Set What Hidden for a player at a specific level.
	 * 
	 * @param pid   The player id.
	 * @param level The level.
	 */
	public void setHiddenWhat(final int pid, final int level, final boolean on)
	{
		if (level < size && (level < hiddenWhat.size()))
			hiddenWhat.get(level).set(pid, on ? 1 : 0);
	}

	/**
	 * @param pid The player id.
	 * @return True if for the site the who information is hidden for the player.
	 */
	public boolean isHiddenWho(final int pid)
	{
		if (this.hiddenWho != null && size > 0)
			return hiddenWho.get(size - 1).getQuick(pid) == 1;

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
			return hiddenWho.get(level).getQuick(pid) == 1;

		return false;
	}

	/**
	 * Set Who Hidden for a player at a specific level.
	 * 
	 * @param pid   The player id.
	 * @param level The level.
	 */
	public void setHiddenWho(final int pid, final int level, final boolean on)
	{
		if (level < size && (level < hiddenWho.size()))
			hiddenWho.get(level).set(pid, on ? 1 : 0);
	}

	/**
	 * @param pid The player id.
	 * @return True if for the site the state information is hidden for the player.
	 */
	public boolean isHiddenState(final int pid)
	{
		if (this.hiddenState != null && size > 0)
			return hiddenState.get(size - 1).getQuick(pid) == 1;

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
			return hiddenState.get(level).getQuick(pid) == 1;

		return false;
	}

	/**
	 * Set State Hidden for a player at a specific level.
	 * 
	 * @param pid   The player id.
	 * @param level The level.
	 */
	public void setHiddenState(final int pid, final int level, final boolean on)
	{
		if (level < size && (level < hiddenState.size()))
			hiddenState.get(level).set(pid, on ? 1 : 0);
	}

	/**
	 * @param pid The player id.
	 * @return True if for the site the rotation information is hidden for the
	 *         player.
	 */
	public boolean isHiddenRotation(final int pid)
	{
		if (this.hiddenRotation != null && size > 0)
			return hiddenRotation.get(size - 1).getQuick(pid) == 1;

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
			return hiddenRotation.get(level).getQuick(pid) == 1;

		return false;
	}

	/**
	 * Set Rotation Hidden for a player at a specific level.
	 * 
	 * @param pid   The player id.
	 * @param level The level.
	 */
	public void setHiddenRotation(final int pid, final int level, final boolean on)
	{
		if (level < size && (level < hiddenRotation.size()))
			hiddenRotation.get(level).set(pid, on ? 1 : 0);
	}

	/**
	 * @param pid The player id.
	 * @return True if for the site the count information is hidden for the player.
	 */
	public boolean isHiddenCount(final int pid)
	{
		if (this.hiddenCount != null && size > 0)
			return hiddenCount.get(size - 1).getQuick(pid) == 1;

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
			return hiddenCount.get(level).getQuick(pid) == 1;

		return false;
	}

	/**
	 * Set Count Hidden for a player at a specific level.
	 * 
	 * @param pid   The player id.
	 * @param level The level.
	 */
	public void setHiddenCount(final int pid, final int level, final boolean on)
	{
		if (level < size && (level < hiddenCount.size()))
			hiddenCount.get(level).set(pid, on ? 1 : 0);
	}

	/**
	 * @param pid The player id.
	 * @return True if for the site the value information is hidden for the player.
	 */
	public boolean isHiddenValue(final int pid)
	{
		if (this.hiddenValue != null && size > 0)
			return hiddenValue.get(size - 1).getQuick(pid) == 1;

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
			return hiddenValue.get(level).getQuick(pid) == 1;

		return false;
	}


	/**
	 * Set Value Hidden for a player at a specific level.
	 * 
	 * @param pid   The player id.
	 * @param level The level.
	 */
	public void setHiddenValue(final int pid, final int level, final boolean on)
	{
		if (level < size && (level < hiddenValue.size()))
			hiddenValue.get(level).set(pid, on ? 1 : 0);
	}
}
