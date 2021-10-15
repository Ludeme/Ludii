package game.functions.booleans.is.tree;

import java.util.BitSet;

import annotations.Hide;
import annotations.Or;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
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
 * Test the induced graph (by adding or deleting edges) is the largest
 * caterpillarTree tree or not.
 * 
 * @author tahmina and cambolbro and Eric.Piette
 * 
 * @remarks It is used to check the induced graph (by adding or deleting edges)
 *          is the largest caterpillarTree tree or not.
 */
@Hide
public class IsCaterpillarTree extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The type of player. **/
	private final IntFunction who;	

	//-------------------------------------------------------------------------
				
	/**
	 * @param who Data about the owner of the tree.
	 * @param role RoleType of the owner of the tree.
	 */
	public IsCaterpillarTree
	(	 			
		@Or  final Player   who,
		@Or  final RoleType role
	)
	{		
		this.who = (role != null) ? RoleType.toIntFunction(role) : who.index();
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
		int whoSiteId 		        = who.eval(context);		
		final int totalVertices	    = graph.vertices().size();
		final int totalEdges		= graph.edges().size();
		final int[] localParent    	= new int [totalVertices];	
		
		int totalExistingedges 		= 0;
		
		if(whoSiteId == 0)
		{
			if (state.what(siteId, SiteType.Edge) == 0)
				whoSiteId = 1;// for neutral player
			else
				whoSiteId = state.what(siteId, SiteType.Edge);
		}		
		for (int i = 0; i < totalVertices; i++)
		{			 			
			localParent[i] = i;				
		}		
		for(int k = 0; k < totalEdges; k++)
		{						
			if(state.what(k, SiteType.Edge) ==  whoSiteId)
			{
				final Edge kEdge = graph.edges().get(k);
				final int vARoot = find(kEdge.vA().index(), localParent);
				final int vBRoot = find(kEdge.vB().index(), localParent);		
				if (vARoot == vBRoot)	
					return false;
				localParent[vARoot] = vBRoot;	
				totalExistingedges++;
			}
		}	
		if(totalExistingedges != (totalVertices	- 1))
			return false;
		
		int count = 0;
		for(int i = 0; i < totalVertices; i++)
		{
			if(localParent[i] == i)
				count++;				
		}
		
		if(count != 1)
			return false;
		
		final BitSet caterpillarBackbone = new BitSet(totalEdges);		
		for(int k = 0; k < totalEdges; k++)
		{
			final Edge kEdge = graph.edges().get(k);
			
			if(state.what(k, SiteType.Edge) ==  whoSiteId)
			{
				final int kEdgevA  = kEdge.vA().index();
				int degree1 = 0;
				
				for(int ka = 0; ka < totalEdges; ka++)
				{
					final Edge kaEdge = graph.edges().get(ka);						
					
					if (state.what(ka, SiteType.Edge) ==  whoSiteId)
					{							
						if((kEdgevA == kaEdge.vA().index())||(kEdgevA == kaEdge.vB().index()))
						{
							degree1++;
							if(degree1 > 1)
								break;
						}							
					}						
				}				
				if(degree1 < 2)
					continue;
				
				final int kEdgevB  = kEdge.vB().index();
				int degree2 = 0;				
				for(int kb = 0; kb < totalEdges; kb++)
				{
					final Edge kbEdge = graph.edges().get(kb);						
					
					if (state.what(kb, SiteType.Edge) ==  whoSiteId)
					{													
						if((kEdgevB == kbEdge.vA().index())||(kEdgevB == kbEdge.vB().index()))
						{
							degree2++;
							if(degree2 > 1)
								break;
						}							
					}
				}										
				if((degree1 > 1) && (degree2 > 1))
				{
					caterpillarBackbone.set(kEdge.index());	
				}
			}
		}			
		final Edge kEdge = graph.edges().get(caterpillarBackbone.nextSetBit(0));
		final int v1 = kEdge.vA().index();
		final int v2 = kEdge.vB().index();
		final BitSet depthBitset1 = new BitSet(totalVertices);
		final BitSet depthBitset2 = new BitSet(totalVertices);
		final BitSet visitedEdge  = new BitSet(totalEdges);
		final int componentSz = totalVertices;
					
		dfsMinPathEdge(context, graph, kEdge, caterpillarBackbone, visitedEdge, 0, v1, v2, componentSz, depthBitset1, whoSiteId);				
		dfsMinPathEdge(context, graph, kEdge, caterpillarBackbone, visitedEdge, 0, v2, v1, componentSz, depthBitset2, whoSiteId);
		
		final int pathLength  = (depthBitset1.cardinality() - 1) + (depthBitset2.cardinality() - 1) + 1;
		return (pathLength == caterpillarBackbone.cardinality()); // If only one path then caterpillar, otherwise not
	
	}
	
	//--------------------------------------------------------------------------------------------

	/**
	 * @param context	 	  The context of present game state.
	 * @param graph           Present status of graph.
	 * @param kEdge 		  The last move.
	 * @param edgeBitset	  All the edge of the present player.
	 * @param visitedEdge	  All the visited edge of the present player.
	 * @param index           Dfs state counter.
	 * @param presentVertex   The present position.
	 * @param parent          The parent of the present position.	
	 * @param mincomponentsz  The desire component size.
	 * @param depthBitset	  Store the depth of path.	
	 * @param whoSiteId       The last move player's type.
	 * 
	 * @remarks dfsMinPathEdge() uses to find the path length.
	 *          
	 */
	
	private int dfsMinPathEdge
	(
			final Context context,
			final Topology graph,
			final Edge kEdge,
			final BitSet edgeBitset,
			final BitSet visitedEdge,
			final int index,
			final int presentVertex,
			final int parent,
			final int mincomponentsz,
			final BitSet depthBitset,
			final int whoSiteId
	)
	{			
		if(index == mincomponentsz * 2)
			return index;
				
		for (int i = edgeBitset.nextSetBit(0); i >= 0; i = edgeBitset.nextSetBit(i + 1)) 
		{
    		final Edge nEdge = graph.edges().get(i);
    		    		
    		if(nEdge != kEdge) 
    		{
    			final int nVA = nEdge.vA().index();
	    		final int nVB = nEdge.vB().index();	    			    		
	    		
	    		if(nVA == presentVertex)
	    		{    	
	    			visitedEdge.set(i);
	    			dfsMinPathEdge(context, graph, nEdge, edgeBitset, visitedEdge, index + 1, nVB, nVA, mincomponentsz,  depthBitset, whoSiteId);
	    		}
	    		else if(nVB == presentVertex)
	    		{	
	    			visitedEdge.set(i);
	    			dfsMinPathEdge(context, graph, nEdge, edgeBitset, visitedEdge, index + 1, nVA, nVB, mincomponentsz,  depthBitset,  whoSiteId);
	    		}
    		}
		} 				
		depthBitset.set(index);
		return index;
	}
	
	//----------------------------------------------------------------------
	
	/**
	 * 
	 * @param position  	A vertex.
	 * @param parent 	    The array with parent id.
	 * 
	 * @return 				The root of the position.
	 */
	private int find(final int position, final int[] parent)
	{
		final int parentId = parent[position];			
		if (parentId == position) 
			return position;
		
		return find (parentId, parent);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{		
		return "IsCaterpillarTree( )";
	}

	@Override
	public boolean isStatic()
	{		
		return false;		
	}

	@Override
	public long gameFlags(final Game game)
	{
		return GameType.Graph | who.gameFlags(game);
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		return who.concepts(game);
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(who.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(who.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		who.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= who.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= who.willCrash(game);
		return willCrash;
	}
}
