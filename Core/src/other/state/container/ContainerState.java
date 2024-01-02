package other.state.container;

import java.io.Serializable;
import java.util.BitSet;

import game.Game;
import game.equipment.container.Container;
import game.types.board.SiteType;
import game.util.equipment.Region;
import main.collections.ChunkSet;
import other.Sites;
import other.state.State;
import other.state.symmetry.SymmetryValidator;

/**
 * Common ContainerState methods.
 * 
 * @author mrraow, cambolbro, Eric.Piette and Dennis Soemers
 */
public interface ContainerState extends Serializable
{
	/**
	 * Reset this state. Manages hashes, if any
	 * @param trialState 
	 * @param game 
	 */
	public void reset(final State trialState, final Game game);

	/**
	 * Removes the item(s) at site, performs all necessary operations to clean up
	 * afterwards
	 * 
	 * @param state
	 * @param site
	 * @param type
	 * @return the index of the component removed, or 0 if none
	 */
	public int remove(final State state, final int site, final SiteType type);
	
	/**
	 * Removes the item(s) at site and level, performs all necessary operations to
	 * clean up afterwards
	 * 
	 * @param state
	 * @param site
	 * @param level
	 * @param type
	 * @return the index of the component removed, or 0 if none
	 */
	public int remove(final State state, final int site, final int level, final SiteType type);
	
	//-------------------------------------------------------------------------

	/**
	 * @return Deep copy of self.
	 */
	public ContainerState deepClone();

	//-------------------------------------------------------------------------

	/**
	 * Iterates through the allowed symmetries, returns the lowest hash value
	 * @param validator validates a given symmetry
	 * @param state provides extra information for symmetries, e.g. number of players
	 * @param whoOnly if true, hash only the 'who' values (for games with undifferentiated pieces)
	 * @return canonical (lowest) hash value from the allowed symmetries 
	 */
	public long canonicalHash(final SymmetryValidator validator, final State state, final boolean whoOnly);

	/**
	 * @return Collection of empty sites.
	 */
	public Sites emptySites();
	
	/**
	 * @return Number of empty sites.
	 */
	public int numEmpty();

	/**
	 * @param site
	 * @param type
	 * 
	 * @return Whether the specified site is empty.
	 */
	public boolean isEmpty(final int site, final SiteType type);
	
	/**
	 * @param cell
	 * 
	 * @return Whether the specified cell is empty.
	 */
	public boolean isEmptyCell(final int cell);

	/**
	 * @param edge
	 * 
	 * @return Whether the specified edge is empty.
	 */
	public boolean isEmptyEdge(final int edge);

	/**
	 * @param vertex
	 * 
	 * @return Whether the specified vertex is empty.
	 */
	public boolean isEmptyVertex(final int vertex);
	
	/**
	 * NOTE: Do NOT modify the Region!
	 * 
	 * @param type
	 * 
	 * @return Reference to the empty Region.
	 */
	public Region emptyRegion(final SiteType type);
	
	/**
	 * Add a site to the empty record.
	 * @param site
	 */
	public void addToEmptyCell(final int site);
	
	/**
	 * Add a site to the empty record for a specific graph element type.
	 * 
	 * @param site
	 * @param type
	 */
	public void addToEmpty(final int site, final SiteType type);

	/**
	 * Remove a site from the empty record for a specific graph element type.
	 * 
	 * @param site
	 * @param type
	 */
	public void removeFromEmpty(final int site, final SiteType type);

	/**
	 * Remove a site from the empty record.
	 * @param site
	 */
	public void removeFromEmptyCell(final int site); 

	/**
	 * Add a vertex to the empty record.
	 * 
	 * @param site
	 */
	public void addToEmptyVertex(final int site);

	/**
	 * Remove a vertex from the empty record.
	 * 
	 * @param site
	 */
	public void removeFromEmptyVertex(final int site);

	/**
	 * Add an edge to the empty record.
	 * 
	 * @param site
	 */
	public void addToEmptyEdge(final int site);

	/**
	 * Remove an edge from the empty record.
	 * 
	 * @param site
	 */
	public void removeFromEmptyEdge(final int site);

	//-------------------------------------------------------------------------

	/**
	 * @return Reference to corresponding source container.
	 */
	public Container container();

	/**
	 * Set the container reference.
	 * @param cont
	 */
	public void setContainer(final Container cont);

	/**
	 * @return Container name extracted from file
	 */
	public String nameFromFile();

	/**
	 * Set playable a site.
	 * 
	 * @param trialState
	 * @param site
	 * @param on
	 */
	public void setPlayable(final State trialState, final int site, final boolean on);
	
	//-------------------------------------------------------------------------

	/**
	 * Grand unified setter
	 * 
	 * @param trialState
	 * @param site
	 * @param who
	 * @param what
	 * @param count
	 * @param state
	 * @param rotation
	 * @param value
	 * @param type
	 */
	public void setSite
	(
		final State trialState, final int site, final int who, final int what, final int count,
		final int state, final int rotation, final int value, final SiteType type
	);

	//-------------------------------------------------------------------------
	
	/**
	 * @param site
	 * @return Index of owner at the specified site.
	 */
	public int whoCell(final int site);

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @return Index of item at the specified site.
	 */
	public int whatCell(final int site);

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @return Item count at the specified site.
	 */
	public int countCell(final int site);

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @return Item state at the specified site.
	 */
	public int stateCell(final int site);

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @return Item rotation at the specified site.
	 */
	public int rotationCell(final int site);

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @return Item value at the specified site.
	 */
	public int valueCell(final int site);

	/**
	 * @param site
	 * @param level
	 * @return Item value at the specified site.
	 */
	public int valueCell(final int site, final int level);

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @return can we play here?
	 */
	public boolean isPlayable(final int site);
	
	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * 
	 * @return is this site occupied.
	 */
	public boolean isOccupied(final int site);
	
	/**
	 * @param site
	 * 
	 * @return the size of the stack
	 */
	public int sizeStackCell(final int site);

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @return Index of item at the specified edge.
	 */
	public int whatEdge(final int site);

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @return Index of the owner of a specified edge.
	 */
	public int whoEdge(final int site);

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @return Item count at the specified edge.
	 */
	public int countEdge(final int site);

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @return Item state at the specified edge.
	 */
	public int stateEdge(final int site);

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @return Item rotation at the specified edge.
	 */
	public int rotationEdge(final int site);

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * 
	 * @return the size of the stack for edge
	 */
	public int sizeStackEdge(final int site);

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @return Index of item at the specified vertex.
	 */
	public int whatVertex(final int site);

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @return Index of item at the specified vertex.
	 */
	public int whoVertex(final int site);

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @return Item count at the specified vertex.
	 */
	public int countVertex(final int site);

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @return Item state at the specified vertex.
	 */
	public int stateVertex(final int site);

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @return Item rotation at the specified vertex.
	 */
	public int rotationVertex(final int site);

	/**
	 * @param site
	 * @return Item value at the specified vertex.
	 */
	public int valueVertex(final int site);

	/**
	 * @param site
	 * @param level
	 * @return Item value at the specified vertex.
	 */
	public int valueVertex(final int site, final int level);

	/**
	 * @param site
	 * @return the size of the stack for vertex.
	 */
	public int sizeStackVertex(final int site);

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @param graphElementType
	 * @return The index of the component on that site.
	 */
	public abstract int what(final int site,
			final SiteType graphElementType);
	
	/**
	 * @param site
	 * @param graphElementType
	 * @return The who data of the site.
	 */
	public abstract int who(final int site,
			final SiteType graphElementType);

	/**
	 * @param site
	 * @param graphElementType
	 * @return The count data of the site.
	 */
	public abstract int count(final int site,
			final SiteType graphElementType);
	
	/**
	 * @param site
	 * @param graphElementType
	 * @return The size stack of the site.
	 */
	public abstract int sizeStack(final int site,
			final SiteType graphElementType);

	/**
	 * 
	 * @param site
	 * @param graphElementType
	 * @return The local state value of the site.
	 */
	public abstract int state(final int site,
			final SiteType graphElementType);

	/**
	 * @param site
	 * @param graphElementType
	 * @return The rotation value of the site.
	 */
	public abstract int rotation(final int site,
			final SiteType graphElementType);
	
	/**
	 * @param site
	 * @param graphElementType
	 * @return The piece value of the site.
	 */
	public abstract int value(final int site,
			final SiteType graphElementType);

	/**
	 * @param site
	 * @param level
	 * @param graphElementType
	 * @return The what data of the site.
	 */
	public abstract int what(final int site, final int level,
			final SiteType graphElementType);
	
	/**
	 * @param site
	 * @param level
	 * @param graphElementType
	 * @return The who data of the site.
	 */
	public abstract int who(final int site, final int level,
			final SiteType graphElementType);

	/**
	 * @param site
	 * @param level
	 * @param graphElementType
	 * @return The state value of the site.
	 */
	public abstract int state(final int site, final int level,
			final SiteType graphElementType);
	
	/**
	 * @param site
	 * @param level
	 * @param graphElementType
	 * @return The rotation value of the site.
	 */
	public abstract int rotation(final int site, final int level,
			final SiteType graphElementType);
	
	/**
	 * @param site
	 * @param level
	 * @param graphElementType
	 * @return The piece value of the site.
	 */
	public abstract int value(final int site, final int level,
			final SiteType graphElementType);

	/**
	 * To set the value of a site in case of dominoes games.
	 * 
	 * @param state
	 * @param site
	 * @param value
	 */
	public void setValueCell(final State state, final int site, final int value);

	/**
	 * Add an item.
	 * 
	 * @param trialState
	 * @param site
	 * @param what
	 * @param who
	 * @param game
	 * @param graphElementType
	 */
	public void addItemGeneric(final State trialState, final int site, final int what, final int who, final Game game,
			final SiteType graphElementType);

	/**
	 * Add an item.
	 * 
	 * @param trialState
	 * @param site
	 * @param what
	 * @param who
	 * @param stateVal
	 * @param rotationVal
	 * @param value
	 * @param game
	 * @param graphElementType
	 */
	public void addItemGeneric(final State trialState, final int site, final int what, final int who,
			final int stateVal, final int rotationVal, final int value, final Game game,
			final SiteType graphElementType);

	/**
	 * Add an item.
	 * 
	 * @param trialState
	 * @param site
	 * @param what
	 * @param who
	 * @param game
	 * @param hidden
	 * @param masked
	 * @param graphElementType
	 */
	public void addItemGeneric(final State trialState, final int site, final int what, final int who, final Game game,
			final boolean[] hidden, final boolean masked, final SiteType graphElementType);

	/**
	 * Remove a stack.
	 * 
	 * @param state
	 * @param site
	 * @param graphElementType
	 */
	public void removeStackGeneric(final State state, final int site, final SiteType graphElementType);

	/**
	 * Set the count.
	 * 
	 * @param state
	 * @param site
	 * @param count
	 */
	public void setCount(final State state, final int site, final int count);

	/**
	 * Add an item.
	 * 
	 * @param trialState
	 * @param site
	 * @param what
	 * @param who
	 * @param game
	 */
	public void addItem(final State trialState, final int site, final int what, final int who, final Game game);

	/**
	 * Insert an element in a stack of any type.
	 * 
	 * @param trialState
	 * @param type
	 * @param site
	 * @param level
	 * @param what
	 * @param who
	 * @param state
	 * @param rotation
	 * @param value
	 * @param game
	 */
	public void insert(final State trialState, final SiteType type, final int site, final int level, final int what,
			final int who, final int state, final int rotation, final int value, final Game game);

	/**
	 * Insert an element in a stack.
	 * 
	 * @param trialState
	 * @param site
	 * @param level
	 * @param what
	 * @param who
	 * @param state
	 * @param rotation
	 * @param value
	 * @param game
	 */
	public void insertCell(final State trialState, final int site, final int level, final int what, final int who,
			final int state, final int rotation, final int value, final Game game);

	/**
	 * Add an item.
	 * 
	 * @param trialState
	 * @param site
	 * @param what
	 * @param who
	 * @param stateVal
	 * @param rotationVal
	 * @param value
	 * @param game
	 */
	public void addItem(final State trialState, final int site, final int what, final int who, final int stateVal,
			final int rotationVal, final int value, final Game game);

	/**
	 * Add an item.
	 * 
	 * @param trialState
	 * @param site
	 * @param what
	 * @param who
	 * @param game
	 * @param hidden
	 * @param masked
	 */
	public void addItem(final State trialState, final int site, final int what, final int who, final Game game, final boolean[] hidden, final boolean masked);

	/**
	 * Remove a stack.
	 * 
	 * @param state
	 * @param site
	 */
	public void removeStack(final State state, final int site);

	/**
	 * @param site
	 * @param level
	 * @return The owner of a cell.
	 */
	public int whoCell(final int site, final int level);

	/**
	 * @param site
	 * @param level
	 * @return the index of the element in the site.
	 */
	public int whatCell(final int site, final int level);

	/**
	 * @param site
	 * @param level
	 * @return The state of the cell.
	 */
	public int stateCell(final int site, final int level);

	/**
	 * @param site
	 * @param level
	 * @return The rotation of the cell.
	 */
	public int rotationCell(final int site, final int level);

	/**
	 * @param state
	 * @param site
	 * @param level
	 * @return The value removed.
	 */
	public int remove(final State state, final int site, final int level);

	/**
	 * Set data on a site.
	 * 
	 * @param trialState
	 * @param site
	 * @param level
	 * @param whoVal
	 * @param whatVal
	 * @param countVal
	 * @param stateVal
	 * @param rotationVal
	 * @param valueVal
	 */
	public void setSite(final State trialState, final int site, final int level, final int whoVal, final int whatVal, final int countVal, final int stateVal, final int rotationVal, final int valueVal);
	
	/**
	 * Add an item to a vertex.
	 * 
	 * @param trialState
	 * @param site
	 * @param what
	 * @param who
	 * @param game
	 */
	public void addItemVertex(final State trialState, final int site, final int what, final int who, final Game game);

	/**
	 * Insert an element to a vertex.
	 * 
	 * @param trialState
	 * @param site
	 * @param level
	 * @param what
	 * @param who
	 * @param state
	 * @param rotation
	 * @param value
	 * @param game
	 */
	public void insertVertex(final State trialState, final int site, final int level, final int what, final int who,
			final int state, final int rotation, final int value,
			final Game game);

	/**
	 * Insert an element to a vertex.
	 * 
	 * @param trialState
	 * @param site
	 * @param what
	 * @param who
	 * @param stateVal
	 * @param rotationVal
	 * @param value
	 * @param game
	 */
	public void addItemVertex(final State trialState, final int site, final int what, final int who, final int stateVal,
			final int rotationVal, final int value, final Game game);

	/**
	 * Add an element to a vertex.
	 * 
	 * @param trialState
	 * @param site
	 * @param what
	 * @param who
	 * @param game
	 * @param hidden
	 * @param masked
	 */
	public void addItemVertex(final State trialState, final int site, final int what, final int who, final Game game, final boolean[] hidden, final boolean masked);

	/**
	 * Remove a stack from a vertex.
	 * 
	 * @param state
	 * @param site
	 */
	public void removeStackVertex(final State state, final int site);

	/**
	 * @param site
	 * @param level
	 * @return The owner of a vertex.
	 */
	public int whoVertex(final int site, final int level);

	/**
	 * @param site
	 * @param level
	 * @return The index of the element on a vertex.
	 */
	public int whatVertex(final int site, final int level);

	/**
	 * @param site
	 * @param level
	 * @return The state value of a vertex.
	 */
	public int stateVertex(final int site, final int level);

	/**
	 * @param site
	 * @param level
	 * @return The rotation value of a vertex.
	 */
	public int rotationVertex(final int site, final int level);
	
	/**
	 * Add an item to an edge.
	 * 
	 * @param trialState
	 * @param site
	 * @param what
	 * @param who
	 * @param game
	 */
	public void addItemEdge(final State trialState, final int site, final int what, final int who, final Game game);

	/**
	 * Insert an element to an edge.
	 * 
	 * @param trialState
	 * @param site
	 * @param level
	 * @param what
	 * @param who
	 * @param state
	 * @param rotation
	 * @param value
	 * @param game
	 */
	public void insertEdge(final State trialState, final int site, final int level, final int what, final int who,
			final int state, final int rotation, final int value,
			final Game game);

	/**
	 * Add an element to an edge.
	 * 
	 * @param trialState
	 * @param site
	 * @param what
	 * @param who
	 * @param stateVal
	 * @param rotationVal
	 * @param value
	 * @param game
	 */
	public void addItemEdge(final State trialState, final int site, final int what, final int who, final int stateVal,
			final int rotationVal, final int value, final Game game);

	/**
	 * Add an element to an edge.
	 * 
	 * @param trialState
	 * @param site
	 * @param what
	 * @param who
	 * @param game
	 * @param hidden
	 * @param masked
	 */
	public void addItemEdge(final State trialState, final int site, final int what, final int who, final Game game, final boolean[] hidden, final boolean masked);

	/**
	 * Remove a stack from an edge.
	 * 
	 * @param state
	 * @param site
	 */
	public void removeStackEdge(final State state, final int site);

	/**
	 * @param site
	 * @param level
	 * @return The owner of an edge.
	 */
	public int whoEdge(final int site, final int level);

	/**
	 * @param site
	 * @param level
	 * @return The index of the component on an edge.
	 */
	public int whatEdge(final int site, final int level);

	/**
	 * @param site
	 * @param level
	 * @return The state value of an edge.
	 */
	public int stateEdge(final int site, final int level);

	/**
	 * @param site
	 * @param level
	 * @return The rotation value of an edge.
	 */
	public int rotationEdge(final int site, final int level);

	/**
	 * @param site
	 * @return Item value at the specified edge.
	 */
	public int valueEdge(final int site);

	/**
	 * @param site
	 * @param level
	 * @return Item value at the specified edge.
	 */
	public int valueEdge(final int site, final int level);

	// ------ Hidden Info--------------------------------------------

	/**
	 * @param who   The index of the player.
	 * @param site  The index of the site.
	 * @param level The index of the level.
	 * @param type  The graph element type.
	 * @return True if the site is invisible.
	 */
	public boolean isHidden(final int who, final int site, final int level, final SiteType type);

	/**
	 * @param who   The index of the player.
	 * @param site  The index of the site.
	 * @param level The index of the level.
	 * @param type  The graph element type.
	 * @return True if the what information is not know.
	 */
	public boolean isHiddenWhat(final int who, final int site, final int level, final SiteType type);

	/**
	 * @param who   The index of the player.
	 * @param site  The index of the site.
	 * @param level The index of the level.
	 * @param type  The graph element type.
	 * @return True if the who information is not know.
	 */
	public boolean isHiddenWho(final int who, final int site, final int level, final SiteType type);

	/**
	 * @param who   The index of the player.
	 * @param site  The index of the site.
	 * @param level The index of the level.
	 * @param type  The graph element type.
	 * @return True if the local state info is not know.
	 */
	public boolean isHiddenState(final int who, final int site, final int level, final SiteType type);

	/**
	 * @param who   The index of the player.
	 * @param site  The index of the site.
	 * @param level The index of the level.
	 * @param type  The graph element type.
	 * @return True if the piece value is not know.
	 */
	public boolean isHiddenValue(final int who, final int site, final int level, final SiteType type);
	
	/**
	 * @param who   The index of the player.
	 * @param site  The index of the site.
	 * @param level The index of the level.
	 * @param type  The graph element type.
	 * @return True if the rotation information is not know.
	 */
	public boolean isHiddenRotation(final int who, final int site, final int level, final SiteType type);

	/**
	 * @param who   The index of the player.
	 * @param site  The index of the site.
	 * @param level The index of the level.
	 * @param type  The graph element type.
	 * @return True if the count information is not know.
	 */
	public boolean isHiddenCount(final int who, final int site, final int level, final SiteType type);

	/**
	 * To set the hidden information.
	 * 
	 * @param state The state.
	 * @param who   The index of the player.
	 * @param site  The index of the site.
	 * @param level The index of the level.
	 * @param type  The graph element type.
	 * @param on    The new value.
	 */
	public void setHidden(final State state, final int who, final int site, final int level, final SiteType type,
			final boolean on);

	/**
	 * To set the What hidden information.
	 * 
	 * @param state The state.
	 * @param who   The index of the player.
	 * @param site  The index of the site.
	 * @param level The index of the level.
	 * @param type  The graph element type.
	 * @param on    The new value.
	 */
	public void setHiddenWhat(final State state, final int who, final int site, final int level, final SiteType type,
			final boolean on);

	/**
	 * To set the Who hidden information.
	 * 
	 * @param state The state.
	 * @param who   The index of the player.
	 * @param site  The index of the site.
	 * @param level The index of the level.
	 * @param type  The graph element type.
	 * @param on    The new value.
	 */
	public void setHiddenWho(final State state, final int who, final int site, final int level, final SiteType type,
			final boolean on);

	/**
	 * To set the state hidden information.
	 * 
	 * @param state The state.
	 * @param who   The index of the player.
	 * @param site  The index of the site.
	 * @param level The index of the level.
	 * @param type  The graph element type.
	 * @param on    The new value.
	 */
	public void setHiddenState(final State state, final int who, final int site, final int level, final SiteType type,
			final boolean on);

	/**
	 * To set the piece value hidden information.
	 * 
	 * @param state The state.
	 * @param who   The index of the player.
	 * @param site  The index of the site.
	 * @param level The index of the level.
	 * @param type  The graph element type.
	 * @param on    The new value.
	 */
	public void setHiddenValue(final State state, final int who, final int site, final int level, final SiteType type,
			final boolean on);

	/**
	 * To set the rotation hidden information.
	 * 
	 * @param state The state.
	 * @param who   The index of the player.
	 * @param site  The index of the site.
	 * @param level The index of the level.
	 * @param type  The graph element type.
	 * @param on    The new value.
	 */
	public void setHiddenRotation(final State state, final int who, final int site, final int level,
			final SiteType type,
			final boolean on);

	/**
	 * To set the count hidden information.
	 * 
	 * @param state The state.
	 * @param who   The index of the player.
	 * @param site  The index of the site.
	 * @param level The index of the level.
	 * @param type  The graph element type.
	 * @param on    The new value.
	 */
	public void setHiddenCount(final State state, final int who, final int site, final int level, final SiteType type,
			final boolean on);

	//------ Deduction Puzzle--------------------------------------------
	
	/**
	 * @param var   The site corresponding to the var.
	 * @param value The value to check.
	 * @param type  The graph element type.
	 * @return if the value for the element is possible.
	 */
	public boolean bit(final int var, final int value, final SiteType type);

	/**
	 * @param var  The site corresponding to the var.
	 * @param type The graph element type.
	 * @return True if the variable is solved.
	 */
	public boolean isResolved(final int var, final SiteType type);

	/**
	 * @param var The site corresponding to the var.
	 * @return True if the variable is solved.
	 */
	public boolean isResolvedEdges(final int var);

	/**
	 * @param var The site corresponding to the var.
	 * @return True if the variable is solved.
	 */
	public boolean isResolvedCell(final int var);

	/**
	 * @param var The site corresponding to the var.
	 * @return True if the variable is solved.
	 */
	public boolean isResolvedVerts(final int var);

	/**
	 * Set the corresponding variable.
	 * 
	 * @param var   The site corresponding to the var.
	 * @param value The value to check.
	 * @param type  The graph element type.
	 */
	public void set(final int var, final int value, final SiteType type);

	/**
	 * @param type
	 * @param var
	 * @return Current values for the specified variable.
	 */
	public abstract BitSet values(final SiteType type, final int var);

	//-------------------------------------------------------------------------
	

	/** @return Reference to the empty ChunkSet for vertices. */
	public ChunkSet emptyChunkSetVertex();
	/** @return Reference to the empty ChunkSet for cells. */
	public ChunkSet emptyChunkSetCell();
	/** @return Reference to the empty ChunkSet for edges. */
	public ChunkSet emptyChunkSetEdge();
	
	/**  @return The number of chunks that we have in the "who" ChunkSet for vertices */
	public int numChunksWhoVertex();
	/** @return The number of chunks that we have in the "who" ChunkSet for cells */
	public int numChunksWhoCell();
	/** @return The number of chunks that we have in the "who" ChunkSet for edges */
	public int numChunksWhoEdge();
	
	/** @return The size per chunk for our "who" ChunkSet for vertices */
	public int chunkSizeWhoVertex();
	/** @return The size per chunk for our "who" ChunkSet for cells */
	public int chunkSizeWhoCell();
	/** @return The size per chunk for our "who" ChunkSet for edges */
	public int chunkSizeWhoEdge();
	
	/** @return The number of chunks that we have in the "what" ChunkSet for vertices */
	public int numChunksWhatVertex();
	/** @return The number of chunks that we have in the "what" ChunkSet for cells */
	public int numChunksWhatCell();
	/** @return The number of chunks that we have in the "what" ChunkSet for edges */
	public int numChunksWhatEdge();
	
	/** @return The size per chunk for our "what" ChunkSet for vertices */
	public int chunkSizeWhatVertex();
	/** @return The size per chunk for our "what" ChunkSet for cells */
	public int chunkSizeWhatCell();
	/** @return The size per chunk for our "what" ChunkSet for edges */
	public int chunkSizeWhatEdge();
	
	/**
	 * Tests whether the parts of the given pattern that are covered by the given mask
	 * are identical in our "who" ChunkSet for vertices.
	 * 
	 * @param mask Relevant parts of ChunkSet
	 * @param pattern Pattern we wish to match
	 * @return True if and only if match
	 */
	public boolean matchesWhoVertex(final ChunkSet mask, final ChunkSet pattern);

	/**
	 * Same as above, but for cells
	 * 
	 * @param mask    Relevant parts of ChunkSet
	 * @param pattern Pattern we wish to match
	 * 
	 * @return True if and only if match
	 */
	public boolean matchesWhoCell(final ChunkSet mask, final ChunkSet pattern);

	/**
	 * Same as above, but for edges
	 * 
	 * @param mask    Relevant parts of ChunkSet
	 * @param pattern Pattern we wish to match
	 * 
	 * @return True if and only if match
	 */
	public boolean matchesWhoEdge(final ChunkSet mask, final ChunkSet pattern);
	
	/**
	 * Tests whether the parts of the given pattern that are covered by the given mask
	 * are identical in our "what" ChunkSet for vertices.
	 * 
	 * @param mask Relevant parts of ChunkSet
	 * @param pattern Pattern we wish to match
	 * @return True if and only if match
	 */
	public boolean matchesWhatVertex(final ChunkSet mask, final ChunkSet pattern);

	/**
	 * Same as above, but for cells
	 * 
	 * @param mask    Relevant parts of ChunkSet
	 * @param pattern Pattern we wish to match
	 * 
	 * @return True if and only if match
	 */
	public boolean matchesWhatCell(final ChunkSet mask, final ChunkSet pattern);

	/**
	 * Same as above, but for edges
	 * 
	 * @param mask    Relevant parts of ChunkSet
	 * @param pattern Pattern we wish to match
	 * 
	 * @return True if and only if match
	 */
	public boolean matchesWhatEdge(final ChunkSet mask, final ChunkSet pattern);
	
	/**
	 * Tests whether we match a single specific who-value of a vertex.
	 * 
	 * @param wordIdx Index of the word we want to match
	 * @param mask Mask to apply
	 * @param matchingWord The word that we must match after masking
	 * @return True if a match
	 */
	public boolean matchesWhoVertex(final int wordIdx, final long mask, final long matchingWord);

	/**
	 * Tests whether we match a single specific who-value of a cell.
	 * 
	 * @param wordIdx Index of the word we want to match
	 * @param mask Mask to apply
	 * @param matchingWord The word that we must match after masking
	 * @return True if a match
	 */
	public boolean matchesWhoCell(final int wordIdx, final long mask, final long matchingWord);

	/**
	 * Tests whether we match a single specific who-value of an edge.
	 * 
	 * @param wordIdx Index of the word we want to match
	 * @param mask Mask to apply
	 * @param matchingWord The word that we must match after masking
	 * @return True if a match
	 */
	public boolean matchesWhoEdge(final int wordIdx, final long mask, final long matchingWord);
	
	/**
	 * Tests whether we match a single specific what-value of a vertex.
	 * 
	 * @param wordIdx Index of the word we want to match
	 * @param mask Mask to apply
	 * @param matchingWord The word that we must match after masking
	 * @return True if a match
	 */
	public boolean matchesWhatVertex(final int wordIdx, final long mask, final long matchingWord);

	/**
	 * Tests whether we match a single specific what-value of a cell.
	 * 
	 * @param wordIdx Index of the word we want to match
	 * @param mask Mask to apply
	 * @param matchingWord The word that we must match after masking
	 * @return True if a match
	 */
	public boolean matchesWhatCell(final int wordIdx, final long mask, final long matchingWord);

	/**
	 * Tests whether we match a single specific what-value of an edge.
	 * 
	 * @param wordIdx Index of the word we want to match
	 * @param mask Mask to apply
	 * @param matchingWord The word that we must match after masking
	 * @return True if a match
	 */
	public boolean matchesWhatEdge(final int wordIdx, final long mask, final long matchingWord);
	
	/**
	 * Tests whether at least one of the chunks in our "who" ChunkSet for vertices 
	 * is equal to one of the chunks in the given pattern that is covered by the 
	 * given mask.
	 * 
	 * @param mask Relevant parts of ChunkSet
	 * @param pattern Values that our masked chunks may not have
	 * @return True if and only if we violate at least one "must-not" condition
	 */
	public boolean violatesNotWhoVertex(final ChunkSet mask, final ChunkSet pattern);

	/**
	 * Same as above, but for cells
	 * 
	 * @param mask    Relevant parts of ChunkSet
	 * @param pattern Values that our masked chunks may not have
	 * @return True if and only if we violate at least one "must-not" condition
	 */
	public boolean violatesNotWhoCell(final ChunkSet mask, final ChunkSet pattern);

	/**
	 * Same as above, but for edges
	 * 
	 * @param mask    Relevant parts of ChunkSet
	 * @param pattern Values that our masked chunks may not have
	 * @return True if and only if we violate at least one "must-not" condition
	 */
	public boolean violatesNotWhoEdge(final ChunkSet mask, final ChunkSet pattern);
	
	/**
	 * Tests whether at least one of the chunks in our "what" ChunkSet for vertices 
	 * is equal to one of the chunks in the given pattern that is covered by the 
	 * given mask.
	 * 
	 * @param mask Relevant parts of ChunkSet
	 * @param pattern Values that our masked chunks may not have
	 * @return True if and only if we violate at least one "must-not" condition
	 */
	public boolean violatesNotWhatVertex(final ChunkSet mask, final ChunkSet pattern);

	/**
	 * Same as above, but for cells
	 * 
	 * @param mask    Relevant parts of ChunkSet
	 * @param pattern Values that our masked chunks may not have
	 * @return True if and only if we violate at least one "must-not" condition
	 */
	public boolean violatesNotWhatCell(final ChunkSet mask, final ChunkSet pattern);

	/**
	 * Same as above, but for edges
	 * 
	 * @param mask    Relevant parts of ChunkSet
	 * @param pattern Values that our masked chunks may not have
	 * @return True if and only if we violate at least one "must-not" condition
	 */
	public boolean violatesNotWhatEdge(final ChunkSet mask, final ChunkSet pattern);
	
	/**
	 * Tests whether at least one of the chunks in our "who" ChunkSet for vertices 
	 * is equal to one of the chunks in the given pattern that is covered by the 
	 * given mask.
	 * 
	 * @param mask Relevant parts of ChunkSet
	 * @param pattern Values that our masked chunks may not have
	 * @param startWord The first word of our array of longs in which to start checking
	 * @return True if and only if we violate at least one "must-not" condition
	 */
	public boolean violatesNotWhoVertex(final ChunkSet mask, final ChunkSet pattern, final int startWord);

	/**
	 * Same as above, but for cells
	 * 
	 * @param mask      Relevant parts of ChunkSet
	 * @param pattern   Values that our masked chunks may not have
	 * @param startWord The first word of our array of longs in which to start
	 *                  checking
	 * @return True if and only if we violate at least one "must-not" condition
	 */
	public boolean violatesNotWhoCell(final ChunkSet mask, final ChunkSet pattern, final int startWord);

	/**
	 * Same as above, but for edges
	 * 
	 * @param mask      Relevant parts of ChunkSet
	 * @param pattern   Values that our masked chunks may not have
	 * @param startWord The first word of our array of longs in which to start
	 *                  checking
	 * @return True if and only if we violate at least one "must-not" condition
	 */
	public boolean violatesNotWhoEdge(final ChunkSet mask, final ChunkSet pattern, final int startWord);
	
	/**
	 * Tests whether at least one of the chunks in our "what" ChunkSet for vertices 
	 * is equal to one of the chunks in the given pattern that is covered by the given mask.
	 * 
	 * @param mask Relevant parts of ChunkSet
	 * @param pattern Values that our masked chunks may not have
	 * @param startWord The first word of our array of longs in which to start checking
	 * @return True if and only if we violate at least one "must-not" condition
	 */
	public boolean violatesNotWhatVertex(final ChunkSet mask, final ChunkSet pattern, final int startWord);

	/**
	 * Same as above, but for cells
	 * 
	 * @param mask      Relevant parts of ChunkSet
	 * @param pattern   Values that our masked chunks may not have
	 * @param startWord The first word of our array of longs in which to start
	 *                  checking
	 * @return True if and only if we violate at least one "must-not" condition
	 */
	public boolean violatesNotWhatCell(final ChunkSet mask, final ChunkSet pattern, final int startWord);

	/**
	 * Same as above, but for edges
	 * 
	 * @param mask      Relevant parts of ChunkSet
	 * @param pattern   Values that our masked chunks may not have
	 * @param startWord The first word of our array of longs in which to start
	 *                  checking
	 * @return True if and only if we violate at least one "must-not" condition
	 */
	public boolean violatesNotWhatEdge(final ChunkSet mask, final ChunkSet pattern, final int startWord);
	
	/** @return Clone of the "who" ChunkSet for vertices */
	public ChunkSet cloneWhoVertex();
	/** @return Clone of the "who" ChunkSet for cells */
	public ChunkSet cloneWhoCell();
	/** @return Clone of the "who" ChunkSet for edges */
	public ChunkSet cloneWhoEdge();
	
	/** @return Clone of the "what" ChunkSet for vertices */
	public ChunkSet cloneWhatVertex();
	/** @return Clone of the "what" ChunkSet for cells */
	public ChunkSet cloneWhatCell();
	/** @return Clone of the "what" ChunkSet for edges */
	public ChunkSet cloneWhatEdge();
}
