package game.functions.region.sites.lineOfSight;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.last.Last;
import game.functions.ints.last.LastType;
import game.functions.region.BaseRegionFunction;
import game.functions.region.sites.LineOfSightType;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.equipment.Region;
import game.util.graph.Radial;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.StringRoutines;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Returns the sites along line-of-sight (LoS) from a specified site in specified directions.
 * 
 * @author Eric Piette and cambolbro
 * 
 * Use this ludeme to find all empty sites in LoS, or the farthest 
 * empty site in LoS, or the first piece in LoS, in each direction.
 */
@Hide
public final class SitesLineOfSight extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Type of test. */
	private final LineOfSightType typeLoS;
	
	/** Location. */
	private final IntFunction loc;

	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;

	/** Graph Type of the location */
	private SiteType typeLoc;

	//-------------------------------------------------------------------------

	/**
	 * @param typeLoS    The line-of-sight test to apply [Piece]. 
	 * @param typeLoc    Graph element type [Cell (or Vertex for intersections)].
	 * @param at         The location to check [(last To)].
	 * @param directions The directions of the move [Adjacent].
	 */
	public SitesLineOfSight
	(
		@Opt	   final LineOfSightType                typeLoS,
		@Opt       final SiteType                       typeLoc, 
		@Opt @Name final IntFunction                    at,
		@Opt       final game.util.directions.Direction directions
	)
	{
		this.typeLoS = (typeLoS == null) ? LineOfSightType.Piece : typeLoS;
		loc = (at == null) ? Last.construct(LastType.To, null) : at;
		this.typeLoc = typeLoc;

		// Directions
		dirnChoice = (directions != null) 
							? directions.directionsFunctions()
							: new Directions(AbsoluteDirection.Adjacent, null);
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		final TIntArrayList sitesLineOfSight = new TIntArrayList();

		final int from = loc.eval(context);

		if (from == Constants.OFF)
			return new Region(sitesLineOfSight.toArray());

		final ContainerState cs = context.containerState(context.containerId()[from]);
		
		if(cs.container().index() > 0)
			return new Region(sitesLineOfSight.toArray());
		
		final Topology graph = context.topology();
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final TopologyElement fromV = graph.getGraphElements(realType).get(from);

		final List<AbsoluteDirection> directions = 
				dirnChoice.convertToAbsolute(realType, fromV, null, null, null,	context);

		for (final AbsoluteDirection direction : directions)
		{
			final List<Radial> radials = graph.trajectories().radials(realType, fromV.index(), direction);
			for (final Radial radial : radials)
			{
				int prevTo = -1;
				for (int toIdx = 1; toIdx < radial.steps().length; toIdx++)
				{
					final int to = radial.steps()[toIdx].id();
					final int what = cs.what(to, realType);
					switch (typeLoS)
					{
					case Empty:  // store all empty
						if (what == 0)
							sitesLineOfSight.add(to);
						break;
					case Farthest:  // store last empty
						if (what != 0 && prevTo != -1)
							sitesLineOfSight.add(prevTo);
						else if (toIdx == radial.steps().length - 1 && what == 0)
							sitesLineOfSight.add(to);
						break;
					case Piece:  // store first piece
						if (what != 0)
							sitesLineOfSight.add(to);
						break;
					default:
						System.out.println("** SitesLineOfSight(): Should never reach here.");
					}
					
					if (what != 0)
						break;
					
					prevTo = to;
				}
			}
		}
		return new Region(sitesLineOfSight.toArray());
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
		long flag = loc.gameFlags(game);

		flag |= SiteType.gameFlags(type);

		return flag;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(loc.concepts(game));
		concepts.or(SiteType.concepts(type));
		concepts.set(Concept.LineOfSight.id(), true);

		if (dirnChoice != null)
			concepts.or(dirnChoice.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(loc.writesEvalContextRecursive());

		if (dirnChoice != null)
			writeEvalContext.or(dirnChoice.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(loc.readsEvalContextRecursive());

		if (dirnChoice != null)
			readEvalContext.or(dirnChoice.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= loc.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= loc.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (typeLoc == null)
			typeLoc = game.board().defaultSite();

		loc.preprocess(game);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		String typeString = "";
		if (typeLoc != null)
			typeString = " " + typeLoc.name().toLowerCase() + StringRoutines.getPlural(typeLoc.name());
		
		String directionString = "";
		if (dirnChoice != null)
			directionString = " in the direction " + dirnChoice.toEnglish(game);
		
		return "all " + typeLoS.name().toLowerCase() + " sites along line-of-site from" + typeString + " " + loc.toEnglish(game) + directionString;
	}
	
	//-------------------------------------------------------------------------
	
}
