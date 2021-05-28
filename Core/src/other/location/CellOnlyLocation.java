package other.location;

import game.types.board.SiteType;

/**
 * A version of Location for games that use only Cells (no other site types)
 *
 * @author Dennis Soemers
 */
public final class CellOnlyLocation extends Location
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

	//-------------------------------------------------------------------------
	
	/**
	 * Constructor for stacking game.
	 * 
	 * @param site
	 * @param level
	 */
	public CellOnlyLocation(final int site, final int level)
	{
		this.site = site;
		this.level = level;
	}


	/**
	 * Constructor for non stacking game.
	 * 
	 * @param site
	 */
	public CellOnlyLocation(final int site)
	{
		this.site = site;
		this.level = 0;
	}

	/**
	 * Copy Constructor.
	 * @param other
	 */
	private CellOnlyLocation(final CellOnlyLocation other)
	{
		this.site = other.site;
		this.level = other.level;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public Location copy()
	{
		return new CellOnlyLocation(this);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int site() 
	{
		return this.site;
	}

	@Override
	public int level()
	{
		return this.level;
	}
	
	@Override
	public SiteType siteType()
	{
		return SiteType.Cell;
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
