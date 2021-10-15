package metadata.graphics.region;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;
import metadata.graphics.region.colour.RegionColour;
import metadata.graphics.util.colour.Colour;

/**
 * Sets a graphic element to a region.
 * 
 * @author Eric.Piette
 */
public class Region implements GraphicsItem
{
	//-------------------------------------------------------------------------------

	/**
	 * @param regionType        Expected colour type.
	 * @param region            Region to be coloured.
	 * @param roleType          Player whose index is to be matched (only for Region).
	 * @param graphElementType  The GraphElementType for the specified sites [DefaultBoardType].
	 * @param sites             Sites to be coloured.
	 * @param site              Site to be coloured.
	 * @param regionFunction    RegionFunction to be coloured.
	 * @param regionSiteType 	The SiteType of the region [DefaultBoardType].
	 * @param colour            The assigned colour for the specified boardGraphicsType.
	 * @param scale             The scale for the region graphics (only applies to Edge siteType).
	 * 
	 * @example (region Colour "Home" Edge regionSiteType:Cell (colour Black))
	 * 
	 * @return Appropriate graphics item.
	 */
	public static GraphicsItem construct
	(
		         	final RegionColourType regionType, 
		@Opt     	final String region,
		@Opt     	final RoleType roleType,
		@Opt     	final SiteType graphElementType,
		@Opt @Or 	final Integer[] sites,
		@Opt @Or 	final Integer site,
		@Opt 		final RegionFunction regionFunction,
		@Opt @Name 	final SiteType regionSiteType,
		@Opt     	final Colour colour,
		@Opt @Name 	final Float scale
	)
	{
		switch (regionType)
		{
		case Colour:
			return new RegionColour(region, roleType,graphElementType,sites,site,regionFunction,regionSiteType,colour,scale);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Region(): A RegionColourType is not implemented.");
	}

	//-------------------------------------------------------------------------------

	private Region()
	{
		// Ensure that compiler does not pick up default constructor
	}

	//-------------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		throw new UnsupportedOperationException("Board.concepts(...): Should never be called directly.");
	}

	@Override
	public long gameFlags(final Game game)
	{
		throw new UnsupportedOperationException("Board.gameFlags(...): Should never be called directly.");
	}

	@Override
	public boolean needRedraw()
	{
		throw new UnsupportedOperationException("Board.gameFlags(...): Should never be called directly.");
	}
}
