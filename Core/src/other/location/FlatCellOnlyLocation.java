package other.location;

import game.types.board.SiteType;

/**
 * A version of Location for games that use only Cells, and no levels
 *
 * @author Dennis Soemers
 */
public final class FlatCellOnlyLocation extends Location
{
	
	//-------------------------------------------------------------------------
	
	/**  */
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/**
	 * The site of the component.
	 */
	private final int site;

	//-------------------------------------------------------------------------

	/**
	 * Constructor for non stacking game.
	 * 
	 * @param site
	 */
	public FlatCellOnlyLocation(final int site)
	{
		this.site = site;
	}

	/**
	 * Copy Constructor.
	 * @param other
	 */
	private FlatCellOnlyLocation(final FlatCellOnlyLocation other)
	{
		this.site = other.site;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public Location copy()
	{
		return new FlatCellOnlyLocation(this);
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
		return 0;
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
		throw new UnsupportedOperationException();
	}

	@Override
	public void incrementLevel()
	{
		throw new UnsupportedOperationException();
	}

	//--------------------------------------------------------------------------

}
