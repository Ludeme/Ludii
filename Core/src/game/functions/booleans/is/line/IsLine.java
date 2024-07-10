package game.functions.booleans.is.line;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.equipment.component.Component;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.directions.Directions;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastTo;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.types.state.GameType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Radial;
import game.util.graph.Step;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.state.container.ContainerState;
import other.state.puzzle.ContainerDeductionPuzzleState;
import other.state.stacking.BaseContainerStateStacking;
import other.topology.TopologyElement;

/**
 * Tests whether a succession of sites are occupied by a specified piece.
 * 
 * @author Eric.Piette
 * 
 * @remarks Used for any line games.
  */
@Hide
public class IsLine extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Minimum length of the line. */
	private final IntFunction length;

	/** The direction category of potential lines. */
	private final Directions dirn;

	/** The location to look for a line. */
	private final IntFunction through;

	/** The locations to look for a line. */
	private final RegionFunction throughAny;

	/** To simulate this kind of piece is on the pivot. (e.g. Dara) */
	private final IntFunction[] whatFn;
	
	/** The owner of the pieces to make a line */
	private final IntFunction whoFn;

	/** To detect exactly the size or not. */
	private final BooleanFunction exactly;

	/** To detect The line only by level in a stack. */
	private final BooleanFunction byLevelFn;

	/** To detect The line only in using the top level in a stack. */
	private final BooleanFunction topFn;

	/** Condition for every piece in the line. */
	private final BooleanFunction condition;
	
	/** True if we look for contiguous lines. */
	private final BooleanFunction contiguousFn;
	
	/** Add on Cell/Edge/Vertex. */
	private SiteType type;
	
	/** The number of minimum component types to compose the line */
	private final IntFunction throughHowMuch;
	
	/** If a line has to be visible from above (and not cut by other components) (used in 3D games) */
	private final BooleanFunction isVisibleFn;
	
	/** Whether to use the opposite radial to try and find line 
	(i.e whether to iterate in both opposite directions of analyzed component to find line */
	private final BooleanFunction useOppositesFn;
	
	

	//-------------------------------------------------------------------------

	/**
	 * @param type       		The graph element type [default SiteType of the board].
	 * @param length     		Minimum length of lines.
	 * @param dirn       		Direction category to which potential lines must belong
	 *                   		[Adjacent].
	 * @param through    		Location through which the line must pass. [(last To)]
	 * @param throughAny 		The line must pass through at least one of these sites.
	 * @param who        		The owner of the pieces making a line.
	 * @param what       		The index of the component composing the line.
	 * @param whats      		The indices of the components composing the line.
	 * @param exact      		If true, then lines cannot exceed minimum length [False].
	 * @param contiguous 		If true, the line has to be contiguous [True].
	 * @param If         		The condition on each site on the line [True].
	 * @param byLevel    		If true, then lines are detected in using the level in a
	 *                   		stack [False].
	 * @param top        		If true, then lines are detected in using only the top level 
	 *                   		in a stack [False].
	 * @param throughHowMuch 	Minimum number of ball IDs the line is made of 
	 * @param isVisible         Whether components composing a line have to be visible or not (used in 3D games)  
	 * @param useOpposites      Whether to use the opposites radial to find line
	 * 
	 */
	
	public IsLine
	(
		@Opt        	final SiteType          type,
			       	    final IntFunction       length,
		@Opt 	     	final AbsoluteDirection dirn,
		@Opt @Or  @Name final IntFunction       through,
		@Opt @Or  @Name final RegionFunction    throughAny,
		@Opt @Or2       final RoleType          who,
		@Opt @Or2 @Name final IntFunction       what,
		@Opt @Or2 @Name final IntFunction[]     whats,
		@Opt      @Name final BooleanFunction   exact,
		@Opt 	  @Name final BooleanFunction   contiguous,
		@Opt  	  @Name final BooleanFunction   If, 
		@Opt  	  @Name final BooleanFunction   byLevel,
		@Opt  	  @Name final BooleanFunction   top,
		@Opt  	  @Name final IntFunction   	throughHowMuch,
		@Opt      @Name final BooleanFunction   isVisible,
		@Opt      @Name final BooleanFunction   useOpposites
	)
	
	
	{
		this.length  = length;
		this.dirn = (dirn == null) ? new Directions(AbsoluteDirection.Adjacent, null) : new Directions(dirn, null);
		this.through   = (through == null) ? new LastTo(null) : through;
		this.throughAny = throughAny;
		exactly = (exact == null) ? new BooleanConstant(false) : exact;
		condition = (If == null) ? new BooleanConstant(true) : If;

		if (whats != null)
		{
			whatFn = whats;
		}
		else if (what != null)
		{
			whatFn = new IntFunction[1];
			whatFn[0] = what;
		}
		else
		{
			whatFn = null;
		}

		whoFn = (who != null) ? RoleType.toIntFunction(who) : null;

		this.type = type;
		byLevelFn = (byLevel == null) ? new BooleanConstant(false) : byLevel;
		topFn = (top == null) ? new BooleanConstant(false) : top;
		contiguousFn = (contiguous == null) ? new BooleanConstant(true) : contiguous;
		this.throughHowMuch = throughHowMuch;
		isVisibleFn = (isVisible == null) ? new BooleanConstant(false) : isVisible;
		useOppositesFn = (useOpposites == null) ? new BooleanConstant(true) : useOpposites;
		
	} 

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (context.game().isStacking())
			return evalStack(context);
		
		if (context.game().isDeductionPuzzle())
			return evalDeductionPuzzle(context);

		final boolean contiguous = contiguousFn.eval(context);
		
		final int throughnum;
		throughnum = (throughHowMuch == null) ? 1 : throughHowMuch.eval(context);
		
		final int[] pivots;
		if (throughAny != null)
		{
			final TIntArrayList listPivots = new TIntArrayList(throughAny.eval(context).sites());
			if (whatFn != null)
			{
				final TIntArrayList whats = new TIntArrayList();
				for (final IntFunction what : whatFn)
					whats.add(what.eval(context));

				for (int i = listPivots.size() - 1; i >= 0; i--)
				{
					final int loc = listPivots.getQuick(i);
					final int contId = context.containerId()[loc];
					final ContainerState state = context.state().containerStates()[contId];
					final int what = state.what(loc, type);
					
					if (!whats.contains(what))
						listPivots.removeAt(i);
				}
			}
			
			pivots = listPivots.toArray();
		}
		else
		{
			pivots = new int[1];
			pivots[0] = through.eval(context);
		}
	
		final other.topology.Topology graph = context.topology();
		final boolean playOnCell = (type != null && type.equals(SiteType.Cell)
				|| (type == null && (context.game().board().defaultSite() != SiteType.Vertex)));
		
		TIntArrayList origlocnUpwards = new TIntArrayList();
		TIntArrayList locnUpwards = new TIntArrayList();
		TIntArrayList indexUpwards = new TIntArrayList();
		TIntArrayList origlocnUpSites = new TIntArrayList();
		TIntArrayList locnUpSites = new TIntArrayList();
		TIntArrayList indexUpSites = new TIntArrayList();
		
		for (int p = 0; p < pivots.length; p++)
		{	
			
			final int locn = pivots[p];
			if (locn < 0)
				return false;

			final int origTo = context.to();
			context.setTo(locn);
			if (!condition.eval(context)) 
			{
				context.setTo(origTo);
				return false;
			}

			if (playOnCell && locn >= graph.cells().size())
				return false;

			if (!playOnCell && locn >= graph.vertices().size())
				return false;
	
			final TopologyElement vertexLoc = playOnCell ? graph.cells().get(locn) : graph.vertices().get(locn);
			final ContainerState state = context.state().containerStates()[context.containerId()[vertexLoc.index()]];
			final TIntArrayList whats = new TIntArrayList(); 
			final int whatLocn = state.what(locn, type);
			
			final List<? extends TopologyElement> sites = context.topology().getGraphElements(type);
			
			/** If components need to be visible in line we first check that there is no other component right above the evaluated one (locn) then
			 * store all other components above (on layer above) */
			if (isVisibleFn.eval(context) == true && !context.equipment().containers()[context.containerId()[locn]].isHand()) {
				boolean covered = false;
				for(final TopologyElement temp_elem : sites) {
					TopologyElement locn_elem = sites.get(locn);
					if (temp_elem.centroid3D().x() == locn_elem.centroid3D().x() && temp_elem.centroid3D().y() == locn_elem.centroid3D().y() && temp_elem.index() > locn_elem.index()) {
						if(state.what(temp_elem.index(), type) != 0)
							covered = true;
					}
				}
				if (covered)
					continue;
				
				final List<game.util.graph.Step> steps = context.game().board().topology().trajectories()
						.steps(type, locn, type, AbsoluteDirection.Upward);
	
				for (final Step step : steps)
				{
					final int toSite = step.to().id();
					locnUpSites.add(toSite);
					if (state.what(toSite, type) != 0) {
						locnUpwards.add(toSite);
					}
				}
				origlocnUpwards = locnUpwards;
				origlocnUpSites = locnUpSites;
			}

			if (whatFn == null)
			{
				if (whoFn == null)
				{
					whats.add(whatLocn);
				}
				else
				{
					if (whoFn != null)
					{
						final int who = whoFn.eval(context);
						for (int i = 1; i < context.components().length; i++)
						{
							final Component component = context.components()[i];
							if (component.owner() == who)
								whats.add(component.index());
						}
					}
				}
			}
			else
			{
				for (final IntFunction what : whatFn)
					whats.add(what.eval(context));
			}
			
			if (!whats.contains(whatLocn))
				continue;

			final int len = length.eval(context);

			final boolean exact = exactly.eval(context);

			final List<Radial> radials = graph.trajectories().radials(type, locn).distinctInDirection(dirn.absoluteDirection());

//			System.out.println("-------------------");
//			System.out.println("Loc: " + locn);
			
			for (final Radial radial : radials)
			{
//				System.out.println("Radial: " + radial);
				final TIntArrayList whoNumber = new TIntArrayList();	
				whoNumber.add(state.what(locn, type));
				
				locnUpwards = origlocnUpwards;
				locnUpSites = origlocnUpSites;
				indexUpwards = new TIntArrayList();
				indexUpSites = new TIntArrayList();
				
				int count = 1;
				
				for (int indexPath = 1; indexPath < radial.steps().length; indexPath++)
				{
					boolean isVisib = true; 
					final int index = radial.steps()[indexPath].id();
					
//					System.out.println("Index: " + index);

					context.setTo(index);
					
					/** If components need to be visible in line we first check that there is no other component right above the evaluated one (index) then
					 * store all other components above (on layer above) and compare them to the ones above the previous studied component */
					if (isVisibleFn.eval(context) == true && !context.equipment().containers()[context.containerId()[index]].isHand()) {
						boolean covered = false;
						for(final TopologyElement temp_elem : sites) {
							TopologyElement index_elem = sites.get(index);
							if (temp_elem.centroid3D().x() == index_elem.centroid3D().x() && temp_elem.centroid3D().y() == index_elem.centroid3D().y() && temp_elem.index() > index_elem.index()) {
								if(state.what(temp_elem.index(), type) != 0)
									covered = true;
							}
						}
						if (covered)
							break;
						
						final List<game.util.graph.Step> steps = context.game().board().topology().trajectories()
								.steps(type, index, type, AbsoluteDirection.Upward);
			
						for (final Step step : steps)
						{
							final int toSite = step.to().id();
							indexUpSites.add(toSite);
							if (state.what(toSite, type) != 0) {
								indexUpwards.add(toSite);
							}
						}
						
						int[] locnUp = locnUpwards.toArray();	
						int[] indexUp = indexUpwards.toArray();	
						int[] locnUpSite = locnUpSites.toArray();	
						int[] indexUpSite = indexUpSites.toArray();	
						
						if (getIntersectionLength(locnUp, indexUp) != 0 && getIntersectionLength(locnUp, indexUp) == getIntersectionLength(locnUpSite, indexUpSite)) { 
							isVisib = false;
						}
						
					}
					
					if (whats.contains(state.what(index, type)) && condition.eval(context) && isVisib)
					{	
						locnUpwards = indexUpwards;
						locnUpSites = indexUpSites;
						indexUpwards = new TIntArrayList(); 
						indexUpSites = new TIntArrayList(); 

						count++;
//						System.out.println("count: " + count);
						
						if (!whoNumber.contains(state.what(index, type))) {	
							whoNumber.add(state.what(index, type));	
						}
						
						if (!exact)
						{
							final int[] whoNum = whoNumber.toArray();	

							if (count == len && throughnum <= whoNum.length)
							{
								
								context.setTo(origTo);
								return true;
							}
						}
					}
					else if (contiguous)
					{
						break;
					}
				}

				final List<Radial> oppositeRadials = radial.opposites();

				if (oppositeRadials != null && useOppositesFn.eval(context))
				{
					for (final Radial oppositeRadial : oppositeRadials)
					{
//						System.out.println("Oppradial: " + oppositeRadials);
						locnUpwards = origlocnUpwards;
						locnUpSites = origlocnUpSites;
						indexUpwards = new TIntArrayList();
						indexUpSites = new TIntArrayList();
						
						int oppositeCount = count;
						
						for (int indexPath = 1; indexPath < oppositeRadial.steps().length; indexPath++)
						{
							boolean isVisib = true;
							final int index = oppositeRadial.steps()[indexPath].id();
							
//							System.out.println("Index: " + index);
							
							context.setTo(index);
							
							/** If components need to be visible in line we first check that there is no other component right above the evaluated one (index) then
							 * store all other components above (on layer above) and compare them to the ones above the previous studied component */
							if (isVisibleFn.eval(context) == true && !context.equipment().containers()[context.containerId()[index]].isHand()) {
								boolean covered = false;
								for(final TopologyElement temp_elem : sites) {
									TopologyElement index_elem = sites.get(index);
									if (temp_elem.centroid3D().x() == index_elem.centroid3D().x() && temp_elem.centroid3D().y() == index_elem.centroid3D().y() && temp_elem.index() > index_elem.index()) {
										if(state.what(temp_elem.index(), type) != 0)
											covered = true;
									}
								}
								if (covered)
									break;
								
								final List<game.util.graph.Step> steps = context.game().board().topology().trajectories()
										.steps(type, index, type, AbsoluteDirection.Upward);
					
								for (final Step step : steps)
								{
									final int toSite = step.to().id();
									if (state.what(toSite, type) != 0) {
										indexUpwards.add(toSite);
									}
								}
									
								int[] locnUp = locnUpwards.toArray();	
								int[] indexUp = indexUpwards.toArray();	
								int[] locnUpSite = locnUpSites.toArray();	
								int[] indexUpSite = indexUpSites.toArray();	
								
								if (getIntersectionLength(locnUp, indexUp) != 0 && getIntersectionLength(locnUp, indexUp) == getIntersectionLength(locnUpSite, indexUpSite)) { 
									isVisib = false;
								}
								
							}
							
							if (whats.contains(state.what(index, type)) && condition.eval(context) && isVisib)
							{
								
								locnUpwards = indexUpwards; 
								locnUpSites = indexUpSites;
								indexUpwards = new TIntArrayList(); 
								indexUpSites = new TIntArrayList(); 
								
								oppositeCount++;
//								System.out.println("oppositeCount: " + oppositeCount);
								
								if (!whoNumber.contains(state.what(index, type))) {	
										whoNumber.add(state.what(index, type));	
								}								
								
								if (!exact)
								{
									final int[] whoNum = whoNumber.toArray();	
									
									if (oppositeCount == len && throughnum <= whoNum.length )	
									{
										context.setTo(origTo);
										return true;
									}
								}
							}
							else if (contiguous)
							{
								break;
							}
						}
						final int[] whoNum = whoNumber.toArray();	
						if (oppositeCount == len && throughnum <= whoNum.length)	
						{
							context.setTo(origTo);
							return true;
						}
					}
				}
				else if (count == len)
				{
					final int[] whoNum = whoNumber.toArray();	
					if (throughnum <= whoNum.length) {	
						context.setTo(origTo);
						return true;
					}
				}
			}
			context.setTo(origTo);
		}

		return false;
	}

	//-------------------------------------------------------------------------

	/**
	 * 
	 * Method for isLine but with a slightly modification for deduction puzzle
	 * 
	 * @param context
	 * @return True if a line exists.
	 */
	public boolean evalDeductionPuzzle(final Context context)
	{
		final boolean contiguous = contiguousFn.eval(context);

		final int[] pivots;
		if (throughAny != null)
		{
			final TIntArrayList listPivots = new TIntArrayList(throughAny.eval(context).sites());
			if (whatFn != null)
			{
				final TIntArrayList whats = new TIntArrayList();
				for (final IntFunction what : whatFn)
					whats.add(what.eval(context));

				for (int i = 0; i < listPivots.size(); i++)
				{
					final int loc = listPivots.getQuick(i);
					final int contId = context.containerId()[loc];
					final ContainerState state = context.state().containerStates()[contId];
					final int what = state.what(loc, type);
					if (!whats.contains(what))
					{
						listPivots.remove(loc);
						i--;
					}
				}
			}
			pivots = listPivots.toArray();
		}
		else
		{
			pivots = new int[1];
			pivots[0] = through.eval(context);
		}

		for (int p = 0; p < pivots.length; p++)
		{
			final int locn = pivots[p];
			if (locn == -1)
				return false;

			final int origTo = context.to();
			context.setTo(locn);
			if (!condition.eval(context))
			{
				context.setTo(origTo);
				return false;
			}

			final int contId = 0;
			final other.topology.Topology graph = context.containers()[contId].topology();
			final boolean playOnCell = (type != null && type.equals(SiteType.Cell)
					|| (type == null && context.game().board().defaultSite() != SiteType.Vertex));

			if (playOnCell && locn >= graph.cells().size())
				return false;

			if (!playOnCell && locn >= graph.vertices().size())
				return false;

			final ContainerDeductionPuzzleState state = (ContainerDeductionPuzzleState) context.state().containerStates()[contId];

			if (!state.isResolved(locn, type))
				return false;

			final TIntArrayList whats = new TIntArrayList();
			final int whatLocn = state.what(locn, type);

			if (whatFn == null)
				whats.add(whatLocn);
			else
				for (final IntFunction what : whatFn)
					whats.add(what.eval(context));

			if (!whats.contains(whatLocn))
				return false;

			final int from = context.from();

			final int len = length.eval(context);
			final boolean exact = exactly.eval(context);

			final List<Radial> radials = graph.trajectories().radials(type, locn)
					.distinctInDirection(dirn.absoluteDirection());
			for (final Radial radial : radials)
			{
				int count = whats.contains(state.what(locn, type)) ? 1 : 0;
				for (int indexPath = 1; indexPath < radial.steps().length; indexPath++)
				{
					final int index = radial.steps()[indexPath].id();

					if (!state.isResolved(index, type))
						break;

					context.setTo(index);
					if (whats.contains(state.what(index, type)) && (whatFn == null || index != from)
							&& condition.eval(context))
					{
						count++;
						if (!exact)
							if (count == len)
							{
								context.setTo(origTo);
								return true;
							}
					}
					else if (contiguous)
					{
						break;
					}
				}

				final List<Radial> oppositeRadials = radial.opposites();
				if (oppositeRadials != null)
				{
					for (final Radial oppositeRadial : oppositeRadials)
					{
						int oppositeCount = count;
						for (int indexPath = 1; indexPath < oppositeRadial.steps().length; indexPath++)
						{
							final int index = oppositeRadial.steps()[indexPath].id();

							if (!state.isResolved(index, type))
								break;

							context.setTo(index);
							if (whats.contains(state.what(index, type)) && (whatFn == null || index != from)
									&& condition.eval(context))
							{	
								
								oppositeCount++;
								if (!exact)
									if (oppositeCount == len)
									{
										context.setTo(origTo);
										return true;
									}
							}
							else if (contiguous)
							{
								break;
							}
						}

						if (oppositeCount == len)
						{
							context.setTo(origTo);
							return true;
						}
					}
				}
				else if (count == len)
				{
					context.setTo(origTo);
					return true;
				}
			}

			context.setTo(origTo);
		}

		return false;
	}

	//-------------------------------------------------------------------------

	/**
	 * Line for stacking game.
	 * 
	 * @param context The context.
	 * @return True if a line is detected.
	 */
	private boolean evalStack(final Context context)
	{
		final int locn = through.eval(context);
		final boolean top = topFn.eval(context);
		if (locn == Constants.UNDEFINED)
			return false;

		SiteType realType = type;
		if (realType == null)
			realType = context.board().defaultSite();
		
		final int contId = context.containerId()[locn];
		final other.topology.Topology graph = context.containers()[contId].topology();
		final BaseContainerStateStacking state = (BaseContainerStateStacking) context.state().containerStates()[contId];
		final TIntArrayList whats = new TIntArrayList();
		if (whatFn == null)
		{
			if (whoFn == null)
			{
				whats.add(state.what(locn, realType));
			}
			else
			{
				if (whoFn != null)
				{
					final int who = whoFn.eval(context);
					for (int i = 1; i < context.components().length; i++)
					{
						final Component component = context.components()[i];
						if (component.owner() == who)
							whats.add(component.index());
					}
				}
			}
		}
		else
		{
			for (final IntFunction what : whatFn)
				whats.add(what.eval(context));
		}

		final int len = length.eval(context);
		if (len == 1)
			return true;
		final boolean exact = exactly.eval(context);

		final boolean byLevel = byLevelFn.eval(context);

		if (byLevel)
		{
			// check a line directly on the stack (hypothesis we add a piece only on the
			// top, e.g. connect-4)
			final int sizeStack = state.sizeStack(locn, realType);
			if (sizeStack >= len)
			{
				final int level = sizeStack - 2;
				int count = 1;
				for (int i = 0; i < len - 1; i++)
				{
					if (!whats.contains(state.what(locn, level - i, realType)))
					{
						break;
					}
					else
					{
						count++;
						if (!exact)
							if (count == len)
								return true;
					}
				}
				if (count == len)
					return true;
			}

			final List<Radial> radials = graph.trajectories().radials(realType, locn)
					.distinctInDirection(dirn.absoluteDirection());

			final int levelOrigin = sizeStack - 1;
			if(levelOrigin < 0)
				return false;
			
			for (final Radial radial : radials)
			{
				final List<Radial> oppositeRadials = radial.opposites();

				// Same Level
				int count = 0;
				for (int indexPath = 0; indexPath < radial.steps().length; indexPath++)
				{
					final int index = radial.steps()[indexPath].id();

					if (state.sizeStack(index, realType) <= levelOrigin)
						break;

					if (whats.contains(state.what(index, levelOrigin, realType)))
					{
						count++;
						if (!exact)
							if (count == len)
								return true;
					}
					else
						break;
				}
				if (oppositeRadials != null)
				{
					for (final Radial oppositeRadial : oppositeRadials)
					{
						int oppositeCount = count;
						for (int indexPath = 1; indexPath < oppositeRadial.steps().length; indexPath++)
						{
							final int index = oppositeRadial.steps()[indexPath].id();
							if (state.sizeStack(index, realType) <= levelOrigin)
								break;

							if (whats.contains(state.what(index, levelOrigin, realType)))
							{
								oppositeCount++;
								if (!exact)
									if (oppositeCount == len)
										return true;
							}
							else
							{
								break;
							}
						}
						if (oppositeCount == len)
							return true;
					}
				}
				else if (count == len)
					return true;

				// level -1 / level +1
				count = 0;
				int diffLevel = 0;
				for (int indexPath = 0; indexPath < radial.steps().length; indexPath++)
				{
					if ((levelOrigin - diffLevel) == -1)
						continue;

					final int index = radial.steps()[indexPath].id();
					if (state.sizeStack(index, realType) <= (levelOrigin - diffLevel))
						break;

					if (whats.contains(state.what(index, levelOrigin - diffLevel, realType)))
					{
						count++;
						diffLevel++;
						if (!exact)
							if (count == len)
								return true;
					}
					else
					{
						break;
					}
				}
				if (oppositeRadials != null)
				{
					diffLevel = 1;
					for (final Radial oppositeRadial : oppositeRadials)
					{
						int oppositeCount = count;
						for (int indexPath = 1; indexPath < oppositeRadial.steps().length; indexPath++)
						{
							final int index = oppositeRadial.steps()[indexPath].id();
							if (state.sizeStack(index, realType) <= (levelOrigin + diffLevel))
								break;

							if (whats.contains(state.what(index, levelOrigin + diffLevel, realType)))
							{
								oppositeCount++;
								diffLevel++;
								if (!exact)
									if (oppositeCount == len)
										return true;
							}
							else
							{
								break;
							}
						}
						if (count == len)
							return true;
					}
				}
				else if (count == len)
					return true;

				// level +1 / level -1
				count = 0;
				diffLevel = 0;
				for (int indexPath = 0; indexPath < radial.steps().length; indexPath++)
				{
					final int index = radial.steps()[indexPath].id();
					if (state.sizeStack(index, realType) <= (levelOrigin + diffLevel))
						break;

					if (whats.contains(state.what(index, levelOrigin + diffLevel, realType)))
					{
						count++;
						diffLevel++;
						if (!exact)
							if (count == len)
								return true;
					}
					else
					{
						break;
					}
				}
				if (oppositeRadials != null)
				{
					diffLevel = 1;
					for (final Radial oppositeRadial : oppositeRadials)
					{
						int oppositeCount = count;
						for (int indexPath = 1; indexPath < oppositeRadial.steps().length; indexPath++)
						{
							if ((levelOrigin - diffLevel) == -1)
								continue;

							final int index = oppositeRadial.steps()[indexPath].id();
							if (state.sizeStack(index, realType) <= (levelOrigin - diffLevel))
								break;

							if (whats.contains(state.what(index, levelOrigin - diffLevel, realType)))
							{
								oppositeCount++;
								diffLevel++;
								if (!exact)
									if (oppositeCount == len)
										return true;
							}
							else
							{
								break;
							}
						}
						if (oppositeCount == len)
							return true;
					}
				}
				else if (count == len)
					return true;
			}

			return false;
		}
		else
		{
			int count = 0;
			if (top)
			{
				if (whats.contains(state.what(locn, realType)))
					count++;
				}
			else
			{
				final int sizeStack = state.sizeStack(locn, realType);
				for (int level = 0; level < sizeStack; level++)
				{
					final int whatLevel = state.what(locn, level, realType);
					if (whats.contains(whatLevel))
					{
						count++;
						break;
					}
				}
			}
			


			if (count == 0)
				return false;

			final List<Radial> radials = graph.trajectories().radials(realType, locn)
					.distinctInDirection(dirn.absoluteDirection());
			for (final Radial radial : radials)
			{
				count = 1;
				for (int indexPath = 1; indexPath < radial.steps().length; indexPath++)
				{
					final int index = radial.steps()[indexPath].id();

					context.setTo(index);

					boolean whatFound = false;
					if (top)
					{
						if (whats.contains(state.what(index, realType)))
							whatFound = true;
					}
					else
					{
						final int sizeStackTo = state.sizeStack(index, realType);
						for (int level = 0; level < sizeStackTo; level++)
						{
							final int whatLevel = state.what(index, level, realType);
							if (whats.contains(whatLevel))
							{
								whatFound = true;
								break;
							}
						}
					}


					if (whatFound && condition.eval(context))
					{
						count++;
						if (!exact)
							if (count == len)
								return true;
					}
					else
					{
						break;
					}
				}

				final List<Radial> oppositeRadials = radial.opposites();
				if (oppositeRadials != null)
				{
					for (final Radial oppositeRadial : oppositeRadials)
					{
						int oppositeCount = count;
						for (int indexPath = 1; indexPath < oppositeRadial.steps().length; indexPath++)
						{
							final int index = oppositeRadial.steps()[indexPath].id();

							boolean whatFound = false;
							if (top)
							{
								if (whats.contains(state.what(index, realType)))
									whatFound = true;
							}
							else
							{
								final int sizeStackTo = state.sizeStack(index, realType);
								for (int level = 0; level < sizeStackTo; level++)
								{
									final int whatLevel = state.what(index, level, realType);
									if (whats.contains(whatLevel))
									{
										whatFound = true;
										break;
									}
								}
							}

							context.setTo(index);
							if (whatFound && condition.eval(context))
							{
								oppositeCount++;
								if (!exact)
									if (oppositeCount == len)
										return true;
							}
							else
							{
								break;
							}
						}

						if (oppositeCount == len)
							return true;
					}
				}
				else if (count == len)
					return true;
			}
		}
		return false;
	}

	
	//-------------------------------------------------------------------------
	
	
	/**
     * function to compute length of intersection of two int arrays
     */
	private static int getIntersectionLength(int[] array1, int[] array2) {
        Set<Integer> set1 = new HashSet<>();
        Set<Integer> intersection = new HashSet<>();
        
        // Add elements of the first array to the set
        for (int num : array1) {
            set1.add(num);
        }
        
        // Check elements of the second array against the set
        for (int num : array2) {
            if (set1.contains(num)) {
                intersection.add(num);
            }
        }
        
        return intersection.size();
    }
	
	
	//-------------------------------------------------------------------------
	
	
	@Override
	public String toString()
	{
		String str = "";
		str += "Line(" + length + ", " + dirn + ", " + through + ", " + exactly + ")";
		return str;
	}

	@Override
	public boolean isStatic()
	{
		// we're always inspecting the "what" ChunkSet of our context, so we're
		// never static
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = length.gameFlags(game);

		if (type != null && (type.equals(SiteType.Edge) || type.equals(SiteType.Vertex)))
			flags |= GameType.Graph;
		
		if (exactly != null)
			flags |= exactly.gameFlags(game);
		
		if (through != null)
			flags |= through.gameFlags(game);
		
		if (whatFn != null)
		{
			for (final IntFunction what : whatFn)
				flags |= what.gameFlags(game);
		}
		
		if (whoFn != null)
			flags |= whoFn.gameFlags(game);
		
		flags |= condition.gameFlags(game);
		flags |= byLevelFn.gameFlags(game);
		flags |= topFn.gameFlags(game);
		
		if (throughAny != null)
			flags |= throughAny.gameFlags(game);
		if (throughHowMuch != null)
			flags |= throughHowMuch.gameFlags(game);
		if (isVisibleFn != null)
			flags |= isVisibleFn.gameFlags(game);
		if (useOppositesFn != null)
			flags |= useOppositesFn.gameFlags(game);

		return flags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Line.id(), true);
		concepts.or(length.concepts(game));

		if (exactly != null)
			concepts.or(exactly.concepts(game));

		if (through != null)
			concepts.or(through.concepts(game));

		if (whatFn != null)
			for (final IntFunction what : whatFn)
				concepts.or(what.concepts(game));

		if (whoFn != null)
			concepts.or(whoFn.concepts(game));

		concepts.or(condition.concepts(game));
		concepts.or(byLevelFn.concepts(game));
		concepts.or(topFn.concepts(game));

		if (throughAny != null)
			concepts.or(throughAny.concepts(game));

		if (dirn != null)
			concepts.or(dirn.concepts(game));
		if (throughHowMuch != null)
			concepts.or(throughHowMuch.concepts(game));
		if (isVisibleFn != null)
			concepts.or(isVisibleFn.concepts(game));
		if (useOppositesFn != null)
			concepts.or(useOppositesFn.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(length.writesEvalContextRecursive());

		if (exactly != null)
			writeEvalContext.or(exactly.writesEvalContextRecursive());

		if (through != null)
			writeEvalContext.or(through.writesEvalContextRecursive());

		if (whatFn != null)
			for (final IntFunction what : whatFn)
				writeEvalContext.or(what.writesEvalContextRecursive());

		if (whoFn != null)
			writeEvalContext.or(whoFn.writesEvalContextRecursive());

		writeEvalContext.or(condition.writesEvalContextRecursive());
		writeEvalContext.or(byLevelFn.writesEvalContextRecursive());
		writeEvalContext.or(topFn.writesEvalContextRecursive());

		if (throughAny != null)
			writeEvalContext.or(throughAny.writesEvalContextRecursive());

		if (dirn != null)
			writeEvalContext.or(dirn.writesEvalContextRecursive());
		if (throughHowMuch != null)
			writeEvalContext.or(throughHowMuch.writesEvalContextRecursive());
		if (isVisibleFn != null)
			writeEvalContext.or(isVisibleFn.writesEvalContextRecursive());
		if (useOppositesFn != null)
			writeEvalContext.or(useOppositesFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(length.readsEvalContextRecursive());

		if (exactly != null)
			readEvalContext.or(exactly.readsEvalContextRecursive());

		if (through != null)
			readEvalContext.or(through.readsEvalContextRecursive());

		if (whatFn != null)
			for (final IntFunction what : whatFn)
				readEvalContext.or(what.readsEvalContextRecursive());

		if (whoFn != null)
			readEvalContext.or(whoFn.readsEvalContextRecursive());

		readEvalContext.or(condition.readsEvalContextRecursive());
		readEvalContext.or(byLevelFn.readsEvalContextRecursive());
		readEvalContext.or(topFn.readsEvalContextRecursive());

		if (throughAny != null)
			readEvalContext.or(throughAny.readsEvalContextRecursive());

		if (dirn != null)
			readEvalContext.or(dirn.readsEvalContextRecursive());
		if (throughHowMuch != null)
			readEvalContext.or(throughHowMuch.readsEvalContextRecursive());
		if (isVisibleFn != null)
			readEvalContext.or(isVisibleFn.readsEvalContextRecursive());
		if (useOppositesFn != null)
			readEvalContext.or(useOppositesFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);

		length.preprocess(game);
		
		if (exactly != null)
			exactly.preprocess(game);
		
		if (through != null)
			through.preprocess(game);
		
		if (whatFn != null)
		{
			for (final IntFunction what : whatFn)
				what.preprocess(game);
		}
		
		if (whoFn != null)
			whoFn.preprocess(game);
		
		byLevelFn.preprocess(game);
		topFn.preprocess(game);

		condition.preprocess(game);
		
		if (throughAny != null)
			throughAny.preprocess(game);
		if (throughHowMuch != null)
			throughHowMuch.preprocess(game);
		if (isVisibleFn != null)			
			isVisibleFn.preprocess(game);
		if (useOppositesFn != null)
			useOppositesFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= length.missingRequirement(game);

		if (exactly != null)
			missingRequirement |= exactly.missingRequirement(game);

		if (through != null)
			missingRequirement |= through.missingRequirement(game);

		if (whatFn != null)
			for (final IntFunction what : whatFn)
				missingRequirement |= what.missingRequirement(game);

		if (whoFn != null)
			missingRequirement |= whoFn.missingRequirement(game);

		missingRequirement |= condition.missingRequirement(game);
		missingRequirement |= byLevelFn.missingRequirement(game);

		if (throughAny != null)
			missingRequirement |= throughAny.missingRequirement(game);
		
		if (byLevelFn != null)
			missingRequirement |= byLevelFn.missingRequirement(game);
		
		if (topFn != null)
			missingRequirement |= topFn.missingRequirement(game);
		if (throughHowMuch != null)
			missingRequirement |= throughHowMuch.missingRequirement(game);
		if (isVisibleFn != null)	
			missingRequirement |= isVisibleFn.missingRequirement(game);
		if (useOppositesFn != null)
			missingRequirement |= useOppositesFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= length.willCrash(game);

		if (exactly != null)
			willCrash |= exactly.willCrash(game);

		if (through != null)
			willCrash |= through.willCrash(game);

		if (whatFn != null)
			for (final IntFunction what : whatFn)
				willCrash |= what.willCrash(game);

		if (whoFn != null)
			willCrash |= whoFn.willCrash(game);

		willCrash |= condition.willCrash(game);
		willCrash |= byLevelFn.willCrash(game);

		if (throughAny != null)
			willCrash |= throughAny.willCrash(game);
		
		if (byLevelFn != null)
			willCrash |= byLevelFn.willCrash(game);
		
		if (topFn != null)
			willCrash |= topFn.willCrash(game);
		if (throughHowMuch != null)
			willCrash |= throughHowMuch.willCrash(game);
		if (isVisibleFn != null)	
			willCrash |= isVisibleFn.willCrash(game);
		if (useOppositesFn != null)
			willCrash |= useOppositesFn.willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return Our target length IntFunction
	 */
	public IntFunction length()
	{
		return length;
	}

	//-------------------------------------------------------------------------

	@Override
	public List<Location> satisfyingSites(final Context context)
	{
		if (!eval(context))
			return new ArrayList<Location>();

		final List<Location> winningSites = new ArrayList<Location>();

		final SiteType realType = type != null ? type : context.board().defaultSite();

		final int[] pivots;
		if (throughAny != null)
		{
			final TIntArrayList listPivots = new TIntArrayList(throughAny.eval(context).sites());
			if (whatFn != null)
			{
				final TIntArrayList whats = new TIntArrayList();
				for (final IntFunction what : whatFn)
					whats.add(what.eval(context));

				for (int i = listPivots.size() - 1; i >= 0; i--)
				{
					final int loc = listPivots.getQuick(i);
					final int contId = context.containerId()[loc];
					final ContainerState state = context.state().containerStates()[contId];
					final int what = state.what(loc, type);

					if (!whats.contains(what))
						listPivots.removeAt(i);
				}
			}
			pivots = listPivots.toArray();
		}
		else
		{
			pivots = new int[1];
			pivots[0] = through.eval(context);
		}

		final other.topology.Topology graph = context.topology();
		final boolean playOnCell = (type != null && type.equals(SiteType.Cell)
				|| (type == null && context.game().board().defaultSite() != SiteType.Vertex));

		for (int p = 0; p < pivots.length; p++)
		{
			final int locn = pivots[p];
			if (locn == -1)
				return new ArrayList<Location>();

			final int origTo = context.to();
			context.setTo(locn);
			if (!condition.eval(context))
			{
				context.setTo(origTo);
				return new ArrayList<Location>();
			}

			if (playOnCell && locn >= graph.cells().size())
				return new ArrayList<Location>();

			if (!playOnCell && locn >= graph.vertices().size())
				return new ArrayList<Location>();

			final TopologyElement vertexLoc = playOnCell ? graph.cells().get(locn) : graph.vertices().get(locn);
			final ContainerState state = context.state().containerStates()[context.containerId()[vertexLoc.index()]];
			final TIntArrayList whats = new TIntArrayList();
			final int whatLocn = state.what(locn, type);

			if (whatFn == null)
			{
				if (whoFn == null)
				{
					whats.add(whatLocn);
				}
				else
				{
					if (whoFn != null)
					{
						final int who = whoFn.eval(context);
						for (int i = 1; i < context.components().length; i++)
						{
							final Component component = context.components()[i];
							if (component.owner() == who)
								whats.add(component.index());
						}
					}
				}
			}
			else
			{
				for (final IntFunction what : whatFn)
					whats.add(what.eval(context));
			}

			if (!whats.contains(whatLocn))
				continue;

			final int len = length.eval(context);
			final boolean exact = exactly.eval(context);


			final List<Radial> radials = graph.trajectories().radials(type, locn)
					.distinctInDirection(dirn.absoluteDirection());
			for (final Radial radial : radials)
			{
				winningSites.clear();
				winningSites.add(new FullLocation(locn, 0, realType));

				int count = whats.contains(whatLocn) ? 1 : 0;
				for (int indexPath = 1; indexPath < radial.steps().length; indexPath++)
				{
					final int index = radial.steps()[indexPath].id();
					context.setTo(index);
					if (whats.contains(state.what(index, type)) && condition.eval(context))
					{
						count++;
						winningSites.add(new FullLocation(index, 0, realType));
						if (!exact)
							if (count == len)
							{
								context.setTo(origTo);
								return winningSites;
							}
					}
					else
					{
						break;
					}
				}

				final List<Radial> oppositeRadials = radial.opposites();
				if (oppositeRadials != null)
				{
					for (final Radial oppositeRadial : oppositeRadials)
					{
						int oppositeCount = count;
						for (int indexPath = 1; indexPath < oppositeRadial.steps().length; indexPath++)
						{
							final int index = oppositeRadial.steps()[indexPath].id();
							context.setTo(index);
							if (whats.contains(state.what(index, type)) && condition.eval(context))
							{
								winningSites.add(new FullLocation(index, 0, realType));
								oppositeCount++;
								if (!exact)
									if (oppositeCount == len)
									{
										context.setTo(origTo);
										return winningSites;
									}
							}
							else
							{
								break;
							}
						}
						if (oppositeCount == len)
						{
							context.setTo(origTo);
							return winningSites;
						}
					}
				}
				else if (count == len)
				{
					context.setTo(origTo);
					return winningSites;
				}
			}
			context.setTo(origTo);
		}

		return new ArrayList<Location>();
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		String whoString = "of their";
		if (whatFn != null)
			whoString = whatFn.toString();
		
		String directionString = dirn.toEnglish(game) + " direction";
		
		return "a player places " + length.toString() + " " + whoString + " pieces in an " + directionString + " line";
	}

}