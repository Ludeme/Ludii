package metadata.graphics.region.colour;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;
import metadata.graphics.util.colour.Colour;
import other.context.Context;

/**
 * Sets the colour of a specified region.
 * 
 * @author Matthew.Stephenson
 */
@Hide
public class RegionColour implements GraphicsItem
{
	/** Region to colour. */
	private final String region;
	
	/** Sites to colour. */
	private final Integer[] sites;
	
	/** Region to colour. */
	private final RegionFunction regionFunction;
	
	/** SiteType to be coloured. */
	private final SiteType graphElementType;
	
	/** Colour to apply. */
	private final Colour colour;
	
	/** RoleType condition. */
	private final RoleType roleType;
	
	/** The SiteType of the region. */
	private final SiteType regionSiteType;
	
	/** The scale for the region graphics (only applies to Edge siteType). */
	private final float scale;
		
	//-------------------------------------------------------------------------

	/**
	 * @param region            Region to be coloured.
	 * @param roleType          Player whose index is to be matched.
	 * @param graphElementType  SiteType to be coloured [DefaultBoardType].
	 * @param sites             Sites to be coloured.
	 * @param site              Site to be coloured.
	 * @param regionFunction    RegionFunction to be coloured.
	 * @param regionSiteType 	The SiteType of the region [DefaultBoardType].
	 * @param colour            The assigned colour for the specified boardGraphicsType.
	 * @param scale             The scale for the region graphics (only applies to Edge siteType) [1.0].
	 * 
	 * @example (region Colour "Home" Edge regionSiteType:Cell (colour Black) scale:2.0)
	 */
	public RegionColour
	(
		@Opt 		final String region,
		@Opt 		final RoleType roleType,
		@Opt 		final SiteType graphElementType,
		@Opt @Or 	final Integer[] sites,
		@Opt @Or 	final Integer site,
		@Opt 		final RegionFunction regionFunction,
		@Opt @Name 	final SiteType regionSiteType,
		@Opt 		final Colour colour,
		@Opt @Name	final Float scale
	)
	{
		int numNonNull = 0;
		if (sites != null)
			numNonNull++;
		if (site != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Only one of @Or should be different to null");
		
		this.region = region;
		
		this.sites = ((sites != null) ? sites : ((site != null) ? (new Integer[]{ site }) : null));
		this.regionFunction = regionFunction;
		this.colour = colour;
		this.roleType = roleType;
		this.regionSiteType = regionSiteType;
		this.graphElementType = graphElementType;
		this.scale = (scale == null) ? 1.0f : scale.floatValue();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param context 
	 * @return SiteType to be coloured.
	 */
	public SiteType graphElementType(final Context context)
	{
		if (graphElementType == null)
			return context.game().board().defaultSite();
		return graphElementType;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Region to Colour.
	 */
	public String region()
	{
		return region;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Sites to Colour.
	 */
	public Integer[] sites()
	{
		return sites;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Colour to apply.
	 */
	public Colour colour()
	{
		return colour;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Region to Colour.
	 */
	public RegionFunction regionFunction()
	{
		return regionFunction;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return RoleType condition to check.
	 */
	public RoleType roleType()
	{
		return roleType;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context 
	 * @return The SiteType of the region.
	 */
	public SiteType regionSiteType(final Context context)
	{
		if (regionSiteType == null)
			return context.game().board().defaultSite();
		return regionSiteType;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The scale for the region graphics (only applies to Edge siteType).
	 */
	public float getScale() 
	{
		return scale;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		return concepts;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;
		if (regionFunction != null)
			gameFlags |= regionFunction.gameFlags(game);
		return gameFlags;
	}

	@Override
	public boolean needRedraw()
	{
		if (regionFunction != null)
			return !regionFunction.isStatic();
		return false;
	}

}
