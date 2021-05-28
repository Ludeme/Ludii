package other.location;

import java.io.Serializable;

import game.types.board.SiteType;

/**
 * A position is the site of a component and the level on it (in case of stack).
 * This is used for the cache Owned on the trial.
 * 
 * @author Eric.Piette and Matthew.Stephenson and Dennis Soemers
 *
 */
public abstract class Location implements Serializable
{
	
	//-------------------------------------------------------------------------
	
	/**  */
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/**
	 * @return A deep copy of this Location object.
	 */
	public abstract Location copy();
	
	//-------------------------------------------------------------------------

	/**
	 * @return The site of this location
	 */
	public abstract int site();

	/**
	 * @return The level of this location
	 */
	public abstract int level();
	
	/**
	 * @return The site type of this location
	 */
	public abstract SiteType siteType();
	
	//-------------------------------------------------------------------------

	/**
	 * Decrements the level of this location
	 */
	public abstract void decrementLevel();

	/**
	 * Increments the level of this location
	 */
	public abstract void incrementLevel();
	
	//--------------------------------------------------------------------------
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + site();
		result = prime * result + level();
		result = prime * result + siteType().hashCode();

		return result;
	}
	
	//--------------------------------------------------------------------------

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof Location))
			return false;

		final Location other = (Location) obj;

		return (site() == other.site() &&
				level() == other.level()  &&
				siteType() == other.siteType());
	}
	
	//--------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "Location(site:" + site() + " level: " + level() + " siteType: " + siteType() + ")";
	}
	
	//-------------------------------------------------------------------------
}
