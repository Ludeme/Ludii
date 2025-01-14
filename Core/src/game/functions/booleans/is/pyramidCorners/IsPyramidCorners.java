package game.functions.booleans.is.pyramidCorners;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.region.RegionFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastTo;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;
import other.location.Location;
import other.state.container.ContainerState;
import other.topology.TopologyElement;

/**
 * For detecting a pyramid corners from a site.
 * 
 * @author Cedric.Antoine
 */
@Hide
public final class IsPyramidCorners extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/** The sites from to check pyramid corners. */
	private final RegionFunction   fromsFn;
	
	/** The sites from to check pyramid corners. */
	private final IntFunction   fromFn;
	
	/** The type of the site from. */
	private SiteType type;


	//-------------------------------------------------------------------------
 
	/**
	 * @param type  The type of the site from to detect the pyramid corners.
	 * @param from  The site from to detect the pyramid corner pattern [(from (last To))].
	 * @param froms  The sites from to detect the pyramid corner pattern.
	 */
	public IsPyramidCorners
	(
					   final SiteType      type,
		    @Opt @Or   final IntFunction   from,
		    @Opt @Or   final RegionFunction froms
	)
	{	
		this.fromFn   = (from == null) ? new LastTo(null) : from;
		this.fromsFn = froms;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		
		final int[] pivots;
		if (fromsFn != null)
		{
			final TIntArrayList listPivots = new TIntArrayList(fromsFn.eval(context).sites());
			pivots = listPivots.toArray();
		}
		else
		{
			pivots = new int[1];
			pivots[0] = fromFn.eval(context);
		}
		
		boolean found = false;
		
		for (int from:pivots) {
		
			final other.topology.Topology graph = context.topology();
	
			if (from >= graph.vertices().size() || from < 0)
				return false;
			
			final TopologyElement vertexLoc = graph.vertices().get(from);
			final ContainerState state = context.state().containerStates()[context.containerId()[vertexLoc.index()]];
			
			final List<AbsoluteDirection> updirectionList = Arrays.asList(
		            AbsoluteDirection.DNW, 
		            AbsoluteDirection.DNE, 
		            AbsoluteDirection.DSW, 
		            AbsoluteDirection.DSE
		        );
			
			int[] sites_encountered = {from, from, from, from};
			int[] id_encountered = {-1, -1, -1, -1};
			
			boolean downsearching = true;
			while(downsearching) {
				
				for (int i = 0; i < 4; i++) {
					List<game.util.graph.Step> steps = context.game().board().topology().trajectories()
							.steps(type, sites_encountered[i], type, updirectionList.get(i));
					if (steps.size() != 1) {
						downsearching = false;
						break;
					}
					sites_encountered[i] = steps.get(0).to().id();
					id_encountered[i] = state.what(steps.get(0).to().id(), type);
				}
					
				if (id_encountered[0] == id_encountered[1] && id_encountered[1] == id_encountered[2] && id_encountered[2] == id_encountered[3] && id_encountered[0] != -1 && downsearching == true && id_encountered[3] == state.what(from, type)) {
				    found = true;
				    downsearching = false;
				}
				
			}
	
			if (found)
				return true;
			
			final List<AbsoluteDirection> downdirectionList = Arrays.asList(
		            AbsoluteDirection.UNW, 
		            AbsoluteDirection.UNE, 
		            AbsoluteDirection.USW, 
		            AbsoluteDirection.USE
		        );
			
			Arrays.fill(sites_encountered, from);
			Arrays.fill(id_encountered, -1);
			
			boolean upsearching = true;
			while(upsearching) {
				
				for (int i = 0; i < 4; i++) {
					List<game.util.graph.Step> steps = context.game().board().topology().trajectories()
							.steps(type, sites_encountered[i], type, downdirectionList.get(i));
					if (steps.size() != 1) {
						upsearching = false;
						break;
					}
					sites_encountered[i] = steps.get(0).to().id();
					id_encountered[i] = state.what(steps.get(0).to().id(), type);
				}
					
				if (id_encountered[0] == id_encountered[1] && id_encountered[1] == id_encountered[2] && id_encountered[2] == id_encountered[3] && id_encountered[0] != -1 && upsearching == true && id_encountered[3] == state.what(from, type)) {
				    found = true;
				    upsearching = false;
				}
				
			}
	
			if (found)
				return true;
		}

		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flag = 0l;

		if (fromFn != null)
			flag |= fromFn.gameFlags(game);
		
		if (fromsFn != null)
			flag |= fromsFn.gameFlags(game);

		flag |= SiteType.gameFlags(type);

		return flag;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Pattern.id(), true);
		concepts.or(SiteType.concepts(type));
		if (fromFn != null)
			concepts.or(fromFn.concepts(game));
		if (fromsFn != null)
			concepts.or(fromsFn.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (fromFn != null)
			writeEvalContext.or(fromFn.writesEvalContextRecursive());
		
		if (fromsFn != null)
			writeEvalContext.or(fromsFn.writesEvalContextRecursive());

		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (fromFn != null)
			readEvalContext.or(fromFn.readsEvalContextRecursive());
		if (fromsFn != null)
			readEvalContext.or(fromsFn.readsEvalContextRecursive());

		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		if (fromFn != null)
			fromFn.preprocess(game);
		if (fromsFn != null)
			fromsFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (fromFn != null)
			missingRequirement |= fromFn.missingRequirement(game);
		if (fromsFn != null)
			missingRequirement |= fromsFn.missingRequirement(game);

		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (fromFn != null)
			willCrash |= fromFn.willCrash(game);
		if (fromsFn != null)
			willCrash |= fromsFn.willCrash(game);

		return willCrash;
	}

	//-------------------------------------------------------------------------

	@Override
	public List<Location> satisfyingSites(final Context context)
	{
		if (!eval(context))
			return new ArrayList<Location>();

		return new ArrayList<Location>();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{		
		if (fromFn != null)
			return "the pyramid corners " + " from " + type.name().toLowerCase() + " " +  fromFn.toEnglish(game);
		else
			return "the pyramid corners " + " from " + type.name().toLowerCase() + " " +  fromsFn.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
}
