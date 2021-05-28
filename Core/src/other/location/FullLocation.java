package other.location;

import game.types.board.SiteType;

/**
 * A "Full" version of Location, with all the data we could ever need (no optimisations)
 *
 * @author Dennis Soemers
 */
public final class FullLocation extends Location
{
	
	//-------------------------------------------------------------------------
	
	/**  */
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/**
	 * The site of the component.
	 */
	private final int site;

	/**
	 * The level of the component in case of stack.
	 */
	private int level = 0;
	
	/**
	 * The graph element type.
	 */
	private final SiteType siteType;

	//-------------------------------------------------------------------------
	
	/**
	 * Constructor for stacking game with the site type.
	 * 
	 * @param site
	 * @param level
	 * @param siteType
	 */
	public FullLocation(final int site, final int level, final SiteType siteType)
	{
		this.site = site;
		this.level = level;
		this.siteType = siteType;
	}

	/**
	 * Constructor for stacking game.
	 * 
	 * @param site
	 * @param level
	 */
	public FullLocation(final int site, final int level)
	{
		this.site = site;
		this.level = level;
		this.siteType = SiteType.Cell;
	}

	/**
	 * Constructor for non stacking game.
	 * 
	 * @param site
	 */
	public FullLocation(final int site)
	{
		this.site = site;
		this.level = 0;
		this.siteType = SiteType.Cell;
	}

	/**
	 * Constructor for non stacking game.
	 * 
	 * @param site
	 * @param siteType
	 */
	public FullLocation(final int site, final SiteType siteType)
	{
		this.site = site;
		this.level = 0;
		this.siteType = siteType;
	}

	/**
	 * Copy Constructor.
	 * @param other
	 */
	private FullLocation(final FullLocation other)
	{
		this.site = other.site;
		this.level = other.level;
		this.siteType = other.siteType;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public Location copy()
	{
		return new FullLocation(this);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int site() 
	{
		return site;
	}

	@Override
	public int level()
	{
		return level;
	}
	
	@Override
	public SiteType siteType()
	{
		return siteType;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void decrementLevel()
	{
		this.level = this.level - 1;
	}

	@Override
	public void incrementLevel()
	{
		this.level = this.level + 1;
	}

	//--------------------------------------------------------------------------

}
