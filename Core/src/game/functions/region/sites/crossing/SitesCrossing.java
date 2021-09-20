package game.functions.region.sites.crossing;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.equipment.Region;
import game.util.moves.Player;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.math.MathRoutines;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Edge;
import other.topology.Topology;
import other.topology.Vertex;

/**
 * Is used to return all the sites which are crossing with an edge.
 *
 * @author tahmina
 */
@Hide
public final class SitesCrossing extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The starting location of the Group. */
	private final IntFunction startLocationFn;
	
	/** The owner of the regions */
	private final IntFunction roleFunc;
	
	/**
	 * @param at  The specific starting position needs to crossing check.
	 * @param who The returned group items player type.
	 * @param role The returned group items role type.
	 */
	public SitesCrossing
	(				  
		         @Name final IntFunction at,	
		@Opt @Or       final Player      who,
		@Opt @Or       final RoleType    role
	)
	{ 		
		startLocationFn = at;
		roleFunc = (role != null) ? RoleType.toIntFunction(role) : who.index();
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		final int from = startLocationFn.eval(context);		
		if (from == Constants.OFF)
			return new Region(new TIntArrayList().toArray());		
		
		final Topology graph = context.topology();
		final TIntArrayList groupItems = new TIntArrayList();		
		final ContainerState state = context.state().containerStates()[0];	
		final int numPlayers	   = context.game().players().count();
		final int whoSiteId  = roleFunc.eval(context);
		int player = 0;
		
		if(whoSiteId == 0)
		{
			if (context.game().isGraphGame())
				player = roleFunc.eval(context);		
			else return null;
		}
		else
			player = whoSiteId;
		
		final Edge kEdge = graph.edges().get(from);
		final int vA  = kEdge.vA().index();
		final int vB  = kEdge.vB().index();
		final Vertex a = graph.vertices().get(vA);
		final Vertex b = graph.vertices().get(vB);		
		final double a0x = a.centroid().getX(); 
		final double a0y = a.centroid().getY();
		final double a1x = b.centroid().getX();
		final double a1y = b.centroid().getY();
		for (int k = 0; k < graph.edges().size() ; k++)
		{			
			if (((whoSiteId == numPlayers + 1 ) && (state.what(k, SiteType.Edge) != 0))
					||((player < numPlayers + 1 ) && (state.who(k, SiteType.Edge) == whoSiteId)))
			{	
				if(from != k)
				{
					final Edge kEdgek = graph.edges().get(k);
					final int vAk  = kEdgek.vA().index();
					final int vBk  = kEdgek.vB().index();
					final Vertex c = graph.vertices().get(vAk);
					final Vertex d = graph.vertices().get(vBk);
					final double b0x = c.centroid().getX();
					final double b0y = c.centroid().getY();
					final double b1x = d.centroid().getX();
					final double b1y = d.centroid().getY();
					
					if(MathRoutines.isCrossing(a0x, a0y, a1x, a1y, b0x, b0y, b1x, b1y))
						groupItems.add(k);
				}
			}
		}		
		return new Region(groupItems.toArray());
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
		long gameFlags = 0;

		gameFlags |= SiteType.gameFlags(type);

		gameFlags |= startLocationFn.gameFlags(game);
		if (roleFunc != null)
			gameFlags |= roleFunc.gameFlags(game);

		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(startLocationFn.concepts(game));
		concepts.or(SiteType.concepts(type));

		if (roleFunc != null)
			concepts.or(roleFunc.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(startLocationFn.writesEvalContextRecursive());

		if (roleFunc != null)
			writeEvalContext.or(roleFunc.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(startLocationFn.readsEvalContextRecursive());

		if (roleFunc != null)
			readEvalContext.or(roleFunc.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		startLocationFn.preprocess(game);
		if (roleFunc != null)
			roleFunc.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= startLocationFn.missingRequirement(game);

		if (roleFunc != null)
			missingRequirement |= roleFunc.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= startLocationFn.willCrash(game);

		if (roleFunc != null)
			willCrash |= roleFunc.willCrash(game);
		return willCrash;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "all sites which are crossing edge " + startLocationFn.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
	
}
