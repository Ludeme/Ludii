package other;

import java.io.Serializable;

import game.equipment.container.Container;
import game.equipment.container.other.Hand;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.board.Id;
import game.types.play.RoleType;
import main.Constants;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Get index of a container from its index, name with or without (role type and
 * playerId) or with a site in a container.
 * 
 * @author cambolbro and Eric Piette
 */
public final class ContainerId implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	protected final IntFunction index;
	protected final String      name;
	protected final RoleType    role;
	protected final IntFunction playerId;
	protected final IntFunction site;
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 * 
	 * @param index    The index of the container.
	 * @param name     The name of the container.
	 * @param role     The roleType of the owner of the container.
	 * @param playerId The index of the owner of the container.
	 * @param site     The site.
	 */
	public ContainerId
	(
		final IntFunction    index, 
		final String         name,
		final RoleType       role,
		final IntFunction    playerId,
		final IntFunction    site
	)
	{
		if (index == null && name == null && role == null && playerId == null && site == null)
		{
			this.index = new IntConstant(0);
			this.name  = null;
			this.role  = null;
			this.playerId  = null;
			this.site  = null;
		}
		else if (index != null && name == null && role == null && playerId == null && site == null)
		{
			this.index = index;
			this.name  = null;
			this.role  = null;
			this.playerId  = null;
			this.site  = null;
		}
		else if (index == null && name != null && role == null && playerId == null && site == null)
		{
			this.index = null;
			this.name  = name;
			this.role  = null;
			this.playerId  = null;
			this.site  = null;
		}
		else if (index == null && name != null && role != null && playerId == null && site == null)
		{
			this.index = null;
			this.name  = name;
			this.role  = role;
			this.playerId  = null;
			this.site  = null;
		}
		else if (index == null && name != null && role == null && playerId != null && site == null)
		{
			this.index = null;
			this.name  = name;
			this.role  = null;
			this.playerId  = playerId;
			this.site  = null;
		}
		else if (index == null && name == null && role == null && playerId == null && site != null)
		{
			this.index = null;
			this.name  = null;
			this.role  = null;
			this.playerId  = null;
			this.site  = site;
		}
		else
		{
			throw new IllegalArgumentException("Unexpected parameter combination.");
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @param context The context.
	 * 
	 * @return The corresponding index of the container.
	 */
	public int eval(final Context context)
	{
		if (index != null)
			return index.eval(context);
		
		if (site != null)
		{
			final int indexSite = site.eval(context);
			if(indexSite == Constants.UNDEFINED)
				return 0;
			return context.containerId()[indexSite];
		}
		
		if (role == null && playerId == null)
			return context.game().mapContainer().get(name).index();
		
		final int pid = (role != null) ? new Id(null, role).eval(context) : playerId.eval(context);
		
		for (int cid = 0; cid < context.containers().length; cid++)
		{
			final Container container = context.containers()[cid];
			if 
			(
				container.isHand() 
				&& 
				container.name().contains(name)
				&&
				((Hand)container).owner() == pid
			)
				return cid;
		}
		
		throw new RuntimeException("Could not find specified container.");
	}

	//-------------------------------------------------------------------------
	
}
