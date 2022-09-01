package game.equipment;

import java.util.BitSet;

import game.Game;
import game.types.play.RoleType;
import main.Constants;
import other.BaseLudeme;
import other.ItemType;

/**
 * Provides a Grammar placeholder for items to go in the equipment collection.
 * 
 * @author Eric and cambolbro
 */
public abstract class Item extends BaseLudeme
{
	/** The owner of the item. */
	private RoleType owner;

	/** The type of the item. */
	private ItemType type;

	/** Unique index in the corresponding item's list of equipment in game. */
	private int index = -1;

	/** Unique name within game. */
	private String name;
	
	/** ID of owner */
	private int ownerID = Constants.UNDEFINED;

	//-------------------------------------------------------------------------

	/**
	 * @param name  The name of the item.
	 * @param index The index of the item.
	 * @param owner  The owner of the item.
	 */
	public Item
	(
		final String   name, 
		final int      index,
		final RoleType owner
	)
	{
		this.name  = name;
		this.index = index;
		this.owner = owner;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Copy constructor.
	 * 
	 * Protected because we do not want the compiler to detect it, this is call only
	 * in Clone method.
	 * 
	 * @param other
	 */
	protected Item(final Item other)
	{
		owner = other.owner;
		index = other.index;
		name  = other.name;
		type  = other.type;
	}

	/**
	 * @return Unique index in master game object's list of equipment.
	 */
	public int index()
	{
		return index;
	}

	/**
	 * To set the index.
	 * 
	 * @param id
	 */
	public void setIndex(final int id)
	{
		index = id;
	}
	
	/**
	 * @return role.
	 */
	public RoleType role() 
	{
		return owner;
	}
	
	/**
	 * To set the owner of the item.
	 * 
	 * @param role
	 */
	public void setRole(final RoleType role) 
	{
		owner = role;
	}
	
	/**
	 * To set the owner of the item according to the id of a player.
	 * 
	 * @param pid
	 */
	public void setRoleFromPlayerId(final int pid)
	{
		owner = RoleType.roleForPlayerId(pid);
	}

	/**
	 * @return owner.
	 */
	public int owner()
	{
		return ownerID;
	}
	
	/**
	 * Makes sure this item is fully created
	 * @param game
	 */
	public void create(final Game game)
	{
		if (owner == RoleType.Shared || owner == RoleType.All)
			ownerID = game.players().count() + 1;
		else		
			ownerID = owner.owner();
	}
	
	/**
	 * @return Unique name within game.
	 */
	public String name()
	{
		return name;
	}

	/**
	 * To set the name of the item.
	 * 
	 * @param name
	 */
	public void setName(final String name)
	{
		this.name = name;
	}

	/**
	 * @return The type of the item.
	 */
	public ItemType type()
	{
		return type;
	}

	/**
	 * To set the type of the item.
	 * 
	 * @param type The type of the item.
	 */
	public void setType(final ItemType type)
	{
		this.type = type;
	}

	/**
	 * @param game The game.
	 * @return Accumulated flags for ludeme.
	 */
	@SuppressWarnings("static-method")
	public long gameFlags(final Game game)
	{
		return 0l;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		return new BitSet();
	}
	
	@Override
	public BitSet writesEvalContextRecursive()
	{
		return new BitSet();
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		return new BitSet();
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return Credit details for images and other resources, else null.
	 * Default behaviour: no credits for this item.
	 */
	@SuppressWarnings("static-method")
	public String credit()
	{
		return null;
	}
}
