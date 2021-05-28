package game.util.equipment;

import annotations.Opt;
import other.BaseLudeme;

/**
 * Defines a hint value to a region or a specific site.
 * 
 * @author Eric.Piette
 * @remarks This is used only for deduction puzzles.
 */
public class Hint extends BaseLudeme
{
	/** The hint to put. */
	final private Integer hint;

	/** The place to put the hints. */
	final private Integer[] region;
	
	//-------------------------------------------------------------------------

	/**
	 * For creating hints in a region.
	 * 
	 * @param region The locations.
	 * @param hint   The value of the hint [0].
	 * @example (hint {0 1 2 3 4 5} 1)
	 */
	public Hint
	(
			 final Integer[] region, 
		@Opt final Integer   hint
	) 
	{
		this.region = region;
		this.hint = (hint == null) ? Integer.valueOf(0) : hint;
	}
	
	/**
	 * For creating hint in a site.
	 * 
	 * @param site The location.
	 * @param hint The value of the hint [0].
	 * @example (hint 1 1)
	 */
	public Hint
	(
		     final Integer site, 
		@Opt final Integer hint
	) 
	{
		this.hint = (hint == null) ? Integer.valueOf(0) : hint;
		this.region = new Integer[1];
		this.region[0] = site;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The hint.
	 */
	public Integer hint() 
	{
		return hint;
	}
	
	/**
	 * @return The list of the site in the region.
	 */
	public Integer[] region()
	{
		return region;
	}
}
