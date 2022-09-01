package game.equipment.other;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.Item;
import game.functions.region.RegionFunction;
import game.types.board.RegionTypeStatic;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.DirectionFacing;
import game.util.equipment.Region;
import game.util.graph.Radial;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.ItemType;
import other.context.Context;
import other.topology.Cell;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Defines a static region on the board.
 * 
 * @author Eric.Piette and cambolbro
 */
public class Regions extends Item
{
	/** If we can, we'll precompute once and cache. */
	private int[] precomputedRegion = null;

	//-------------------------------------------------------------------------

	/** The sites implied. */
	private final int[] sites;
	
	/** The region function implied. */
	private final RegionFunction[] region;

	/** The type of pre-computed static region. */
	private final RegionTypeStatic[] regionType;
	
	/** The name of the hintRegion. */
	private final String hintRegionName;
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param name            The name of the region ["Region" + owner index].
	 * @param role            The owner of the region [P1].
	 * @param sites           The sites included in the region.
	 * @param regionFn        The region function corresponding to the region.
	 * @param regionsFn       The region functions corresponding to the region.
	 * @param staticRegion    Pre-computed static region corresponding to this region.
	 * @param staticRegions   Pre-computed static regions corresponding to this region.
	 * @param hintRegionLabel Name of this hint region (for deduction puzzles).
	 * 
	 * @example (regions P1 { (sites Side NE) (sites Side SW) } )
	 * 
	 * @example (regions "Replay" {14 24 43 53})
	 * 
	 * @example (regions "Traps" (sites {"C3" "C6" "F3" "F6"}))
	 */
	public Regions
	(
	   @Opt     final String             name,
	   @Opt     final RoleType           role,
	        @Or final Integer[]          sites,
	        @Or final RegionFunction     regionFn,
	        @Or final RegionFunction[]   regionsFn,
	        @Or final RegionTypeStatic   staticRegion,
	        @Or final RegionTypeStatic[] staticRegions,
	   @Opt	    final String             hintRegionLabel
	)
	{
		super
		(
			(name == null) ? "Region" + ((role == null) ? RoleType.P1 : role) : name,
			Constants.UNDEFINED, 
			RoleType.Neutral
		);

		int numNonNull = 0;
		if (sites != null)
			numNonNull++;
		if (regionFn != null)
			numNonNull++;
		if (regionsFn != null)
			numNonNull++;
		if (staticRegion != null)
			numNonNull++;
		if (staticRegions != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");

		if (role != null)
		{
			setRole(role);  //this.role = role;
		}
		else
		{
			setRole(RoleType.Neutral); // this.role = RoleType.None;
		}
		
		if (sites != null)
		{
			this.sites = new int[sites.length];
			for (int i = 0; i < sites.length; i++)
				this.sites[i] = sites[i].intValue();
		}
		else
		{
			this.sites = null;
		}
		
		region = (regionFn != null) ? new RegionFunction[] { regionFn } : regionsFn;
		regionType = (staticRegion != null) ? new RegionTypeStatic[] { staticRegion } : staticRegions;
		hintRegionName = hintRegionLabel;
		setType(ItemType.Regions);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		String text = "";	

		if(region!=null) 
		{
			int count=0;
			for (final RegionFunction regionFunction : region())
			{
				text += this.name() + ": ";
				text += regionFunction.toEnglish(game) + " for "+ RoleType.roleForPlayerId(owner()).name();
				count++;
				
	            if(count == region().length-1)
	                text += " and ";
	            else if(count < region().length)
	            	text += ", ";
			}
		}
		else
		{
			text = this.name() + ": contains the sites " + Arrays.toString(sites);
		}
		
		return text;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param type
	 * @param context
	 * @return A list of locations corresponding to the area type.
	 */
	public Integer[][] convertStaticRegionOnLocs(final RegionTypeStatic type, final Context context) 
	{
		Integer[][] regions = null;
		final Topology graph = context.topology();
		final SiteType defaultType = context.board().defaultSite();
		
		switch (type)
		{
		case Corners:
			regions = new Integer[graph.corners(defaultType).size()][1];
			for (int c = 0; c < graph.corners(defaultType).size(); c++)
			{
				final TopologyElement corner = graph.corners(defaultType).get(c);
				regions[c][0] = Integer.valueOf(corner.index());
			}
			break;
		case Sides:
			regions = new Integer[graph.sides(defaultType).size()][];
			int indexSide = 0;
			for (final java.util.Map.Entry<DirectionFacing, List<TopologyElement>> entry : graph.sides(defaultType).entrySet())
			{
				regions[indexSide] = new Integer[entry.getValue().size()];
				for (int j = 0; j < entry.getValue().size(); j++)
				{
					final TopologyElement element = entry.getValue().get(j);
					regions[indexSide][j] = Integer.valueOf(element.index());
				}
				indexSide++;
			}
			break;
		case SidesNoCorners:
			final TIntArrayList corners = new TIntArrayList();
			for (int c = 0; c < graph.corners(defaultType).size(); c++)
			{
				final TopologyElement corner = graph.corners(defaultType).get(c);
				corners.add(corner.index());
			}

			regions = new Integer[graph.sides(defaultType).size()][];
			int indexSideNoCorners = 0;
			for (final java.util.Map.Entry<DirectionFacing, List<TopologyElement>> entry : graph.sides(defaultType).entrySet())
			{
				final List<Integer> sideNoCorner = new ArrayList<Integer>();
				regions[indexSideNoCorners] = new Integer[entry.getValue().size()];
				for (int j = 0; j < entry.getValue().size(); j++)
				{
					final TopologyElement element = entry.getValue().get(j);
					if (!corners.contains(element.index()))
						sideNoCorner.add(Integer.valueOf(element.index()));
				}

				regions[indexSideNoCorners] = new Integer[sideNoCorner.size()];

				for (int j = 0; j < sideNoCorner.size(); j++)
					regions[indexSideNoCorners][j] = sideNoCorner.get(j);

				indexSideNoCorners++;
			}
			break;
		case AllSites:
			regions = new Integer[1][graph.getGraphElements(defaultType).size()];
			for (int i = 0; i < graph.getGraphElements(defaultType).size(); i++)
				regions[0][i] = Integer.valueOf(i);
			break;
		case Columns:
			regions = new Integer[graph.columns(defaultType).size()][];
			for (int i = 0; i < graph.columns(defaultType).size(); i++)
			{
				final List<TopologyElement> col = graph.columns(defaultType).get(i);
				regions[i] = new Integer[col.size()];
				for (int j = 0; j < col.size(); j++) 
					regions[i][j] = Integer.valueOf(col.get(j).index());
			}
			break;
		case Rows:
			regions = new Integer[graph.rows(defaultType).size()][];
			for (int i = 0; i < graph.rows(defaultType).size(); i++)
			{
				final List<TopologyElement> row = graph.rows(defaultType).get(i);
				regions[i] = new Integer[row.size()];
				for (int j = 0 ; j < row.size() ; j++) 
					regions[i][j]= Integer.valueOf(row.get(j).index());
			}
			break;
		case Diagonals:
			regions = new Integer[graph.diagonals(defaultType).size()][];
			for (int i = 0; i < graph.diagonals(defaultType).size(); i++)
			{
				final List<TopologyElement> diag = graph.diagonals(defaultType).get(i);
				regions[i] = new Integer[diag.size()];
				for (int j = 0; j < diag.size(); j++)
					regions[i][j] = Integer.valueOf(diag.get(j).index());
			}
			break;
		case Layers:
			regions = new Integer[graph.layers(defaultType).size()][];
			for (int i = 0; i < graph.layers(defaultType).size(); i++)
			{
				final List<TopologyElement> diag = graph.layers(defaultType).get(i);
				regions[i] = new Integer[diag.size()];
				for (int j = 0 ; j < diag.size() ; j++) 
					regions[i][j] = Integer.valueOf(diag.get(j).index());
			}
			break;
		case HintRegions:
			if (hintRegionName != null) 
			{
				if (context.game().equipment().verticesWithHints().length != 0)
					return context.game().equipment().verticesWithHints();
				else if (context.game().equipment().cellsWithHints().length != 0)
					return context.game().equipment().cellsWithHints();
				else if (context.game().equipment().edgesWithHints().length != 0)
					return context.game().equipment().edgesWithHints();
			}
			else
			{
				return context.game().equipment().cellsWithHints();
			}
			break;
		case AllDirections:
			// list of cell indices in the chosen directions
			regions = new Integer[graph.getGraphElements(defaultType).size()][];
			for (final TopologyElement element : graph.getGraphElements(defaultType))
			{	
				final List<Radial> radials = graph.trajectories().radials(defaultType, element.index(), AbsoluteDirection.All);

				final TIntArrayList locs = new TIntArrayList();
				locs.add(element.index());

				for (final Radial radial : radials)
				{
					for (int toIdx = 1; toIdx < radial.steps().length; toIdx++)
					{
						final int to = radial.steps()[toIdx].id();
						if (!locs.contains(to))
							locs.add(to);
					}
				}
				regions[element.index()] = new Integer[locs.size()];
				for (int index = 0; index < locs.size(); index++)
					regions[element.index()][index] = Integer.valueOf(locs.getQuick(index));
			}
			break;
		case SubGrids:
			final int sizeSubGrids = (int) Math.sqrt(Math.sqrt(graph.cells().size()));
			regions = new Integer[sizeSubGrids*sizeSubGrids][sizeSubGrids*sizeSubGrids];
			int indexRegion = 0; 
			for (int rowSubGrid = 0 ; rowSubGrid < sizeSubGrids ; rowSubGrid++)
				for (int colSubGrid = 0 ; colSubGrid < sizeSubGrids ; colSubGrid++)
				{
					int indexOnTheRegion = 0 ;
					for (final Cell vertex : context.board().topology().cells())
					{
						final int col = vertex.col();
						final int row = vertex.row();
						
						if (row >= rowSubGrid * sizeSubGrids && row < (rowSubGrid + 1) * sizeSubGrids)
							if (col >= colSubGrid * sizeSubGrids && col < (colSubGrid + 1) * sizeSubGrids)
							{
								regions[indexRegion][indexOnTheRegion] = Integer.valueOf(vertex.index());
								indexOnTheRegion++;
							}
					}
					indexRegion++;
				}
			break;
		case Regions:
		case Vertices:
		case Touching:
			final ArrayList<ArrayList<TopologyElement>> touchingRegions = new ArrayList<ArrayList<TopologyElement>>();
			for (final TopologyElement element : graph.getGraphElements(defaultType))
				for (final TopologyElement vElement : element.adjacent())
				{
					final ArrayList<TopologyElement> touchingRegion = new ArrayList<TopologyElement>();
					touchingRegion.add(element);
					touchingRegion.add(vElement);
					touchingRegions.add(touchingRegion);
				}
			regions = new Integer[touchingRegions.size()][2];
			for (int i = 0; i < touchingRegions.size(); i++)
				for (int j = 0; j < 2; j++)
					regions[i][j] = Integer.valueOf(touchingRegions.get(i).get(j).index());

				break;
			default:
				break;
		}
		
		return regions;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return The sites of that region.
	 */
	public int[] sites()
	{
		return sites;
	}
		
	/**
	 * @return The region functions of that region.
	 */
	public RegionFunction[] region() 
	{
		return region;
	}
	
	/**
	 * @return The list of all the static regions.
	 */
	public RegionTypeStatic[] regionTypes() 
	{
		return regionType;
	}
	
	/**
	 * @param context The context.
	 * @return Array of site indices for given context.
	 */
	public int[] eval(final Context context)
	{
		if (precomputedRegion != null)
			return precomputedRegion;

		if (region != null)
		{
			final List<TIntArrayList> siteLists = new ArrayList<TIntArrayList>();
			
			int totalNumSites = 0;
			for (final RegionFunction regionFn : region)
			{
				final TIntArrayList wrapped = TIntArrayList.wrap(regionFn.eval(context).sites());
				siteLists.add(wrapped);
				totalNumSites += wrapped.size();
			}
			
			final int[] toReturn = new int[totalNumSites];
			int startIdx = 0;
			
			for (final TIntArrayList wrapped : siteLists)
			{
				wrapped.toArray(toReturn, 0, startIdx, wrapped.size());
				startIdx += wrapped.size();
			}
			
			if (siteLists.size() > 1)
			{
				// We might have duplicates, so just to be sure we'll wrap 
				// into Region and unwrap again to get rid of duplicates
				return new Region(toReturn).sites();
			}
			
			return toReturn;
		}
		else
		{
			return sites;
		}
	}
	
	/**
	 * @param context  The context.
	 * @param location The location.
	 * @return True if the given location is in this Regions.
	 */
	public boolean contains(final Context context, final int location)
	{
		if (region != null)
		{
			for (final RegionFunction regionFn : region)
			{
				if (regionFn.contains(context, location))
					return true;
			}
			
			return false;
		}
		else
		{
			for (final int site : sites)
			{
				if (site == location)
					return true;
			}
			
			return false;
		}
	}
	
	/**
	 * @return True if this Region is static (always evals to the same region
	 * regardless of context).
	 */
	@SuppressWarnings("static-method")
	public boolean isStatic()
	{
//		if (region != null)
//		{
//			for (final RegionFunction regionFn : region)
//			{
//				if (!regionFn.isStatic())
//					return false;
//			}
//		}
		
		return true;
	}
	
	/**
	 * Does preprocessing for region functions in this region
	 * @param game
	 */
	public void preprocess(final Game game)
	{
		if (region() != null)
		{
			for (final RegionFunction regionFunction : region())
			{
				regionFunction.preprocess(game);
			}
		}

		if (isStatic())
			precomputedRegion = eval(new Context(game, null));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		if (region != null)
			for (final RegionFunction r : region)
				missingRequirement |= r.missingRequirement(game);

		return missingRequirement;
	}

	@Override
	public String toString()
	{
		return "Regions in Equipment named = " + name();
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();

		if (region != null)
			for (final RegionFunction reg : region)
				concepts.or(reg.concepts(game));

		return concepts;
	}
	
	//-------------------------------------------------------------------------
}
