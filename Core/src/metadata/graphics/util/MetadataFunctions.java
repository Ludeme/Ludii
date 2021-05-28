package metadata.graphics.util;

import java.util.ArrayList;

import game.equipment.other.Regions;
import game.functions.ints.board.Id;
import game.types.play.RoleType;
import other.context.Context;

/**
 * The metadata functions.
 */
public class MetadataFunctions 
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Takes in the name of a region and returns an array of all sites in it.
	 * 
	 * @param context    The context.
	 * @param regionName The name of the region.
	 * @param roleType   The role of the owner.
	 * @return The converting regions into integers sites.
	 */
	public static ArrayList<ArrayList<Integer>> convertRegionToSiteArray(final Context context, final String regionName, final RoleType roleType)
	{
		final ArrayList<ArrayList<Integer>> allRegionSites = new ArrayList<>();
		
		for(final Regions region : context.equipment().regions())
			if (region.name().equals((regionName)))
				if (roleType == null || roleType.equals(region.role()))
				{
					allRegionSites.add(new ArrayList<>());
					region.preprocess(context.game()); 
					
					for (final int site : region.eval(context))
						allRegionSites.get(allRegionSites.size()-1).add(Integer.valueOf(site));	
				}
		
		return allRegionSites;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context  The context.
	 * @param roletype The roletype.
	 * @return The real owner.
	 */
	public static int getRealOwner(final Context context, final RoleType roletype)
	{
		return new Id(null, roletype).eval(context.currentInstanceContext());
	}

}
