package game.functions.booleans.is.regularGraph;
import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastTo;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.types.state.GameType;
import game.util.moves.Player;
import main.Constants;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Edge;
import other.topology.Topology;

/**
 * Test the induced graph (with all the vertices and some of edges) is a regular
 * graph or not.
 * 
 * @author tahmina and cambolbro and Eric.Piette
 * 
 * @remarks It uses to check the regular graph (or k-regular) to use all the
 *          vertices and also possible to check odd degree or even degree
 *          regular graph.
 */
@Hide
public class IsRegularGraph extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The type of player. **/
	private final IntFunction who;
				
	/** The flag for check G is single or not. */
	private final IntFunction kParameter;
	
	/** The flag for all the degree is odd or not. */
	private final BooleanFunction oddFn;
	
	/** The flag for all the degree is odd or not. */
	private final BooleanFunction evenFn;
	
	//-------------------------------------------------------------------------
			
	/**
	 * @param who  The owner of the tree.
	 * @param role RoleType of the owner of the tree.
	 * @param k    The parameter of k-regular graph.
	 * @param odd  Flag to recognise the k (in k-regular graph) is odd or not.
	 * @param even Flag to recognise the k (in k-regular graph) is even or not.
	 */
	public IsRegularGraph
	(
			 @Or         final Player          who,
			 @Or         final RoleType        role,
		@Opt @Or2  @Name final IntFunction     k,
		@Opt @Or2  @Name final BooleanFunction odd,
		@Opt @Or2  @Name final BooleanFunction even
	)
	{		
		this.who = (who != null) ? who.index() : RoleType.toIntFunction(role);
		this.kParameter = (k == null) ? new IntConstant(0) : k;
		this.oddFn 	= (odd == null) ? new BooleanConstant(false) : odd;
		this.evenFn = (even == null) ? new BooleanConstant(false) : even;
	} 

	//----------------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{	
		final int siteId = new LastTo(null).eval(context); 
		if(siteId == Constants.OFF)
			return false;
		
		final Topology graph 		= context.topology();
		final int cid              	= context.containerId()[0];
		final ContainerState state 	= context.state().containerStates()[cid];
		int whoSiteId 		    	= who.eval(context);
		final boolean oddFlag	    = oddFn.eval(context);
		final boolean evenFlag	    = evenFn.eval(context);
		final int kValue	    	= kParameter.eval(context);
		final int totalVertices		= graph.vertices().size();
		final int totalEdges		= graph.edges().size();	
		final BitSet[] degreeInfo   = new BitSet[totalVertices];
		
		if(whoSiteId == 0)
		{
			if (state.what(siteId, SiteType.Edge) == 0)
				whoSiteId = 1;
			else
				whoSiteId = state.what(siteId, SiteType.Edge);
		}
		
		for (int i = 0; i < totalVertices; i++)
		{
			degreeInfo[i] = new BitSet(totalEdges);
		}
		
		for (int k = 0; k < totalEdges; k++)
		{
			final Edge kEdge = graph.edges().get(k);
			if (state.what(kEdge.index(), SiteType.Edge) == whoSiteId)
			{ 
				final int vA  = kEdge.vA().index();
				final int vB  = kEdge.vB().index();
				degreeInfo[vA].set(vB);
				degreeInfo[vB].set(vA);					
			}
		}		
		int deg = kValue;
		if(kValue == 0)
		{
			for (int i = 0; i < totalVertices; i++)
			{
				if (degreeInfo[i].cardinality() != 0)
				{
					deg = degreeInfo[i].cardinality();
					break;
				}
			}
		}
		
		for (int i = 0; i < totalVertices; i++)
		{
			if (deg != degreeInfo[i].cardinality())
				return false;
		}
		if(oddFlag) 
		{
			if((deg % 2) == 1)	return true;
			else 				return false;
		}
		if(evenFlag) 
		{
			if((deg % 2) == 0)	return true;
			else 				return false;
		}
		return true;			
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		String str = "";
		str += "IsRegularGraph( )";
		return str;
	}

	@Override
	public boolean isStatic()
	{		
		return false;		
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = GameType.Graph;
		if (who != null)
			gameFlags |= who.gameFlags(game);
		if (kParameter != null)
			gameFlags |= kParameter.gameFlags(game);
		if (oddFn != null)
			gameFlags |= oddFn.gameFlags(game);
		if (evenFn != null)
			gameFlags |= evenFn.gameFlags(game);
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (who != null)
			concepts.or(who.concepts(game));
		if (kParameter != null)
			concepts.or(kParameter.concepts(game));
		if (oddFn != null)
			concepts.or(oddFn.concepts(game));
		if (evenFn != null)
			concepts.or(evenFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (who != null)
			writeEvalContext.or(who.writesEvalContextRecursive());
		if (kParameter != null)
			writeEvalContext.or(kParameter.writesEvalContextRecursive());
		if (oddFn != null)
			writeEvalContext.or(oddFn.writesEvalContextRecursive());
		if (evenFn != null)
			writeEvalContext.or(evenFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (who != null)
			readEvalContext.or(who.readsEvalContextRecursive());
		if (kParameter != null)
			readEvalContext.or(kParameter.readsEvalContextRecursive());
		if (oddFn != null)
			readEvalContext.or(oddFn.readsEvalContextRecursive());
		if (evenFn != null)
			readEvalContext.or(evenFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (who != null)
			who.preprocess(game);
		if (kParameter != null)
			kParameter.preprocess(game);
		if (oddFn != null)
			oddFn.preprocess(game);
		if (evenFn != null)
			evenFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (who != null)
			missingRequirement |= who.missingRequirement(game);
		if (kParameter != null)
			missingRequirement |= kParameter.missingRequirement(game);
		if (oddFn != null)
			missingRequirement |= oddFn.missingRequirement(game);
		if (evenFn != null)
			missingRequirement |= evenFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (who != null)
			willCrash |= who.willCrash(game);
		if (kParameter != null)
			willCrash |= kParameter.willCrash(game);
		if (oddFn != null)
			willCrash |= oddFn.willCrash(game);
		if (evenFn != null)
			willCrash |= evenFn.willCrash(game);
		return willCrash;
	}
}
