package other.state.zhash;

import java.io.Serializable;

import game.types.board.SiteType;
import main.collections.ChunkStack;
import other.state.State;

/**
 * Wrapper around ChunkSet, to make sure it is managed correctly
 * If ChunkSet were an interface, I'd use the decorator pattern...
 * 
 * @author mrraow
 */
public class HashedChunkStack implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	/** The internal state of each site */
	private final ChunkStack internalState;

	private final long[][] whatHash;
	private final long[][] whoHash;
	private final long[][] stateHash;
	private final long[][] rotationHash;
	private final long[][] valueHash;
	private final long[] sizeHash;
	
	private long zhash = 0L;
	
	/**
	 * @param numComponents
	 * @param numPlayers
	 * @param numStates
	 * @param numRotations
	 * @param numValues
	 * @param type
	 * @param hidden
	 * @param whatHash
	 * @param whoHash
	 * @param stateHash
	 * @param rotationHash
	 * @param valueHash
	 * @param sizeHash
	 */
	public HashedChunkStack
	(
			final int numComponents, 
			final int numPlayers,
			final int numStates, 
			final int numRotations,
			final int numValues,
			final int type, 
			final boolean hidden,
			final long[][] whatHash,
			final long[][] whoHash,
			final long[][] stateHash,
			final long[][] rotationHash,
			final long[][] valueHash,
			final long[] sizeHash
	) 
	{
		this.internalState = new ChunkStack(numComponents, numPlayers, numStates, numRotations, numValues, type, hidden);
		
		this.whatHash = whatHash;
		this.whoHash = whoHash;
		this.stateHash = stateHash;
		this.rotationHash = rotationHash;
		this.valueHash = valueHash;
		this.sizeHash = sizeHash;
	}

	/**
	 * Copy constructor, used by clone()
	 * @param that
	 */
	private HashedChunkStack(final HashedChunkStack that) 
	{
		this.internalState = new ChunkStack(that.internalState);
		
		// Safe to just store a reference
		this.whatHash = that.whatHash;
		this.whoHash = that.whoHash;
		this.stateHash = that.stateHash;
		this.rotationHash = that.rotationHash;
		this.valueHash = that.valueHash;
		this.sizeHash = that.sizeHash;

		this.zhash = that.zhash;
	}

	/**
	 * @return The long value of the hash.
	 */
	public long calcHash() 
	{
		return zhash;
	}

	/**
	 * @param newWhatHash
	 * @param newWhoHash
	 * @param newStateHash
	 * @param newRotationHash
	 * @param newValueHash
	 * @param newSizeHash
	 * @param whoOnly
	 * @return the hash of this stack as if it were at a new location
	 */
	public long remapHashTo (
			final long[][] newWhatHash,
			final long[][] newWhoHash,
			final long[][] newStateHash,
			final long[][] newRotationHash,
			final long[][] newValueHash,
			final long[] newSizeHash,
			final boolean whoOnly)
	{
		long hash = newSizeHash[internalState.size()];

		for (int level = 0; level < internalState.size(); level++)
		{
			if (whoOnly ) 
			{
				hash ^= newWhoHash[level][internalState.who(level)];				
			} 
			else 
			{
				hash ^= newStateHash[level][internalState.state(level)];
				hash ^= newRotationHash[level][internalState.rotation(level)];
				hash ^= newValueHash[level][internalState.value(level)];
				hash ^= newWhoHash[level][internalState.who(level)];
				hash ^= newWhatHash[level][internalState.what(level)];
			}
		}
		
		return hash;
	}
	
/* ----------------------------------------------------------------------------------------------------
 * The following methods change state, and therefore need to manage the hash value
 * ---------------------------------------------------------------------------------------------------- */

	/**
	 * Set the state.
	 * 
	 * @param trialState The state.
	 * @param val        The value.
	 * @param level      The level.
	 */
	public void setState(final State trialState, final int val, final int level) 
	{
		if (level >= internalState.size()) return;
		
		long delta = stateHash[level][internalState.state(level)];
		internalState.setState(val, level);
		delta ^= stateHash[level][internalState.state(level)];

		trialState.updateStateHash(delta);
		zhash ^= delta;
	}

	/**
	 * Set the rotation.
	 * 
	 * @param trialState The state.
	 * @param val        The value.
	 * @param level      The level.
	 */
	public void setRotation(final State trialState, final int val, final int level)
	{
		if (level >= internalState.size())
			return;

		long delta = rotationHash[level][internalState.rotation(level)];
		internalState.setRotation(val, level);
		delta ^= rotationHash[level][internalState.rotation(level)];

		trialState.updateStateHash(delta);
		zhash ^= delta;
	}

	/**
	 * Set the value.
	 * 
	 * @param trialState The state.
	 * @param val        The value.
	 * @param level      The level.
	 */
	public void setValue(final State trialState, final int val, final int level)
	{
		if (level >= internalState.size())
			return;

		long delta = valueHash[level][internalState.value(level)];
		internalState.setValue(val, level);
		delta ^= valueHash[level][internalState.value(level)];

		trialState.updateStateHash(delta);
		zhash ^= delta;
	}

	/**
	 * Set the who.
	 * 
	 * @param trialState The state.
	 * @param val        The value.
	 * @param level      The level.
	 */
	public void setWho(final State trialState, final int val, final int level) 
	{
		if (level >= internalState.size()) 
			return;

		long delta = whoHash[level][internalState.whoChunkSet().getAndSetChunk(level, val)];
		delta ^= whoHash[level][internalState.who(level)];
		
		trialState.updateStateHash(delta);
		zhash ^= delta;
	}

	/**
	 * Set the what.
	 * 
	 * @param trialState The state.
	 * @param val        The value.
	 * @param level      The level.
	 */
	public void setWhat(final State trialState, final int val, final int level) 
	{
		if (level >= internalState.size()) 
			return;

		long delta = whatHash[level][internalState.whatChunkSet().getAndSetChunk(level, val)];
		delta ^= whatHash[level][internalState.what(level)];
		
		trialState.updateStateHash(delta);
		zhash ^= delta;
	}

	/**
	 * @param player The index of the player.
	 * @param site   The index of the site.
	 * @param level  The index of the level.
	 * @param type   The graph element type.
	 * @return True if the location is invisible.
	 */
	public boolean isHidden(final int player, final int site, final int level, final SiteType type)
	{
		return internalState.isHidden(player, level);
	}

	/**
	 * @param player The index of the player.
	 * @param site   The index of the site.
	 * @param level  The index of the level.
	 * @param type   The graph element type.
	 * @return True if the what information is not know.
	 */
	public boolean isHiddenWhat(final int player, final int site, final int level, final SiteType type)
	{
		return internalState.isHiddenWhat(player, level);
	}

	/**
	 * @param player The index of the player.
	 * @param site   The index of the site.
	 * @param level  The index of the level.
	 * @param type   The graph element type.
	 * @return True if the who information is not know.
	 */
	public boolean isHiddenWho(final int player, final int site, final int level, final SiteType type)
	{
		return internalState.isHiddenWho(player, level);
	}

	/**
	 * @param player The index of the player.
	 * @param site   The index of the site.
	 * @param level  The index of the level.
	 * @param type   The graph element type.
	 * @return True if the state information is not know.
	 */
	public boolean isHiddenState(final int player, final int site, final int level, final SiteType type)
	{
		return internalState.isHiddenState(player, level);
	}

	/**
	 * @param player The index of the player.
	 * @param site   The index of the site.
	 * @param level  The index of the level.
	 * @param type   The graph element type.
	 * @return True if the rotation information is not know.
	 */
	public boolean isHiddenRotation(final int player, final int site, final int level, final SiteType type)
	{
		return internalState.isHiddenRotation(player, level);
	}

	/**
	 * @param player The index of the player.
	 * @param site   The index of the site.
	 * @param level  The index of the level.
	 * @param type   The graph element type.
	 * @return True if the value information is not know.
	 */
	public boolean isHiddenValue(final int player, final int site, final int level, final SiteType type)
	{
		return internalState.isHiddenValue(player, level);
	}

	/**
	 * @param player The index of the player.
	 * @param site   The index of the site.
	 * @param level  The index of the level.
	 * @param type   The graph element type.
	 * @return True if the count information is not know.
	 */
	public boolean isHiddenCount(final int player, final int site, final int level, final SiteType type)
	{
		return internalState.isHiddenCount(player, level);
	}

	/**
	 * To set the hidden information.
	 * 
	 * @param state  The state.
	 * @param player The index of the player.
	 * @param site   The index of the site.
	 * @param level  The index of the level.
	 * @param type   The graph element type.
	 * @param on     The new value.
	 */
	public void setHidden(final State state, final int player, final int site, final int level, final SiteType type,
			final boolean on)
	{
		if (level >= internalState.size())
			return;

		internalState.setHidden(player, level, on);
	}

	/**
	 * To set the what hidden information.
	 * 
	 * @param state  The state.
	 * @param player The index of the player.
	 * @param site   The index of the site.
	 * @param level  The index of the level.
	 * @param type   The graph element type.
	 * @param on     The new value.
	 */
	public void setHiddenWhat(final State state, final int player, final int site, final int level, final SiteType type,
			final boolean on)
	{
		if (level >= internalState.size())
			return;

		internalState.setHiddenWhat(player, level, on);
	}

	/**
	 * To set the who hidden information.
	 * 
	 * @param state  The state.
	 * @param player The index of the player.
	 * @param site   The index of the site.
	 * @param level  The index of the level.
	 * @param type   The graph element type.
	 * @param on     The new value.
	 */
	public void setHiddenWho(final State state, final int player, final int site, final int level, final SiteType type,
			final boolean on)
	{
		if (level >= internalState.size())
			return;

		internalState.setHiddenWho(player, level, on);
	}

	/**
	 * To set the state hidden information.
	 * 
	 * @param state  The state.
	 * @param player The index of the player.
	 * @param site   The index of the site.
	 * @param level  The index of the level.
	 * @param type   The graph element type.
	 * @param on     The new value.
	 */
	public void setHiddenState(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (level >= internalState.size())
			return;

		internalState.setHiddenState(player, level, on);
	}

	/**
	 * To set the rotation hidden information.
	 * 
	 * @param state  The state.
	 * @param player The index of the player.
	 * @param site   The index of the site.
	 * @param level  The index of the level.
	 * @param type   The graph element type.
	 * @param on     The new value.
	 */
	public void setHiddenRotation(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (level >= internalState.size())
			return;

		internalState.setHiddenRotation(player, level, on);
	}

	/**
	 * To set the piece value hidden information.
	 * 
	 * @param state  The state.
	 * @param player The index of the player.
	 * @param site   The index of the site.
	 * @param level  The index of the level.
	 * @param type   The graph element type.
	 * @param on     The new value.
	 */
	public void setHiddenValue(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (level >= internalState.size())
			return;

		internalState.setHiddenValue(player, level, on);
	}

	/**
	 * To set the count hidden information.
	 * 
	 * @param state  The state.
	 * @param player The index of the player.
	 * @param site   The index of the site.
	 * @param level  The index of the level.
	 * @param type   The graph element type.
	 * @param on     The new value.
	 */
	public void setHiddenCount(final State state, final int player, final int site, final int level,
			final SiteType type, final boolean on)
	{
		if (level >= internalState.size())
			return;

		internalState.setHiddenCount(player, level, on);
	}

	/**
	 * Decrement the size.
	 * 
	 * @param trialState The state.
	 */
	public void decrementSize(final State trialState) 
	{
		long delta = sizeHash[internalState.size()];
		internalState.decrementSize();
		delta ^= sizeHash[internalState.size()];
		
		trialState.updateStateHash(delta);
		zhash ^= delta;
	}

	/**
	 * Increment the size.
	 * 
	 * @param trialState The state.
	 */
	public void incrementSize(final State trialState) 
	{
		long delta = sizeHash[internalState.size()];
		internalState.incrementSize();
		delta ^= sizeHash[internalState.size()];
		
		trialState.updateStateHash(delta);
		zhash ^= delta;
	}

/* ----------------------------------------------------------------------------------------------------
 * The following methods delegate to a method which manages state
 * ---------------------------------------------------------------------------------------------------- */

	/**
	 * Set the rotation.
	 * 
	 * @param trialState The state.
	 * @param val        The value.
	 */
	public void setRotation(final State trialState, final int val)
	{
		final int size = internalState.size();
		if (size <= 0)
			return;
		setRotation(trialState, val, size - 1);
	}

	/**
	 * Set the value.
	 * 
	 * @param trialState The state.
	 * @param val        The value.
	 */
	public void setValue(final State trialState, final int val)
	{
		final int size = internalState.size();
		if (size <= 0)
			return;
		setValue(trialState, val, size - 1);
	}

	/**
	 * Set the state.
	 * 
	 * @param trialState The state.
	 * @param val        The value.
	 */
	public void setState(final State trialState, final int val) {
		final int size = internalState.size();
		if (size <= 0) return;
		setState(trialState, val, size-1);
	}

	/**
	 * Set the who.
	 * 
	 * @param trialState The state.
	 * @param val        The value.
	 */
	public void setWho(final State trialState, final int val) {
		final int size = internalState.size();
		if (size <= 0) 
			return;
		setWho(trialState, val, size-1);
	}

	/**
	 * Set the what.
	 * 
	 * @param trialState The state.
	 * @param val        The value.
	 */
	public void setWhat(final State trialState, final int val) {
		final int size = internalState.size();
		if (size <= 0) return;
		setWhat(trialState, val, size-1);
	}

/* ----------------------------------------------------------------------------------------------------
 * The following methods are read-only, and do not need to manage their internal states
 * ---------------------------------------------------------------------------------------------------- */

	@Override
	public HashedChunkStack clone()
	{
		return new HashedChunkStack(this);
	}

	/**
	 * @return The size.
	 */
	public int size() { return internalState.size(); }

	/**
	 * @return The who value.
	 */
	public int who() {
		return internalState.who();
	}

	/**
	 * @param level The level.
	 * @return The who value.
	 */
	public int who(int level) {
		return internalState.who(level);
	}

	/**
	 * @return The what value.
	 */
	public int what() {
		return internalState.what();
	}

	/**
	 * @param level The level.
	 * @return The what value.
	 */
	public int what(int level)
	{
		return internalState.what(level);
	}

	/**
	 * @return The state value.
	 */
	public int state() {
		return internalState.state();
	}

	/**
	 * @param level The level.
	 * @return The state.
	 */
	public int state(int level) {
		return internalState.state(level);
	}

	/**
	 * @return The rotation.
	 */
	public int rotation()
	{
		return internalState.rotation();
	}

	/**
	 * @param level The level.
	 * @return The rotation.
	 */
	public int rotation(int level)
	{
		return internalState.rotation(level);
	}

	/**
	 * @return The value.
	 */
	public int value()
	{
		return internalState.value();
	}

	/**
	 * @param level The level.
	 * @return The value.
	 */
	public int value(int level)
	{
		return internalState.value(level);
	}

}
