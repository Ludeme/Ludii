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
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Edge;
import other.topology.Topology;

/**
 * Tests whether the selected vertex is the center of the tree (or sub tree).
 * 
 * @author tahmina and cambolbro
 * 
 * @remarks It is used to confirm the last move is a center of the tree or not.
 */
@Hide
public class IsTreeCentre extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** The type of player. **/
	private final IntFunction who;			
	
	/**
	 * @param who  Data about the owner of the tree.
	 * @param role RoleType of the owner of the tree.
	 */
	public IsTreeCentre
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
		final int siteId 			= new LastTo(null).eval(context); 
		final Topology graph 		= context.topology();
		final ContainerState state 	= context.state().containerStates()[0];
		final int whoSiteId 		= who.eval(context);
		final int numPlayers	    = context.game().players().count();
		final int totalVertices		= graph.vertices().size();
		final int totalEdges		= graph.edges().size();
		final int[] localParent    	= new int [totalVertices];
		final BitSet[] itemsList    = new BitSet[totalVertices];
		final BitSet[] adjacencyGraph= new BitSet[totalVertices];
		//System.out.println(" Who : "+whoSiteId);
		for (int i = 0; i < totalVertices; i++)
		{			 			
			localParent[i]     = -1;
			itemsList[i]   	   = new BitSet(totalEdges);
			adjacencyGraph[i]  = new BitSet(totalEdges);
		}
		
		for(int k = 0; k < totalEdges; k++)
		{				
			if (((whoSiteId == numPlayers + 1 ) && (state.what(k, SiteType.Edge) != 0))
					||((whoSiteId < numPlayers + 1 ) && (state.who(k, SiteType.Edge) == whoSiteId)))
			{  //System.out.println(" k : "+k); 
				final Edge kEdge = graph.edges().get(k);		
				final int vA  = kEdge.vA().index();
				final int vB  = kEdge.vB().index();
				adjacencyGraph[vA].set(vB);
				adjacencyGraph[vB].set(vA);
				final int vARoot = find(vA, localParent);
				final int vBRoot = find(vB, localParent);	
				if (vARoot == vBRoot) return false;
				if(localParent[vARoot] == -1) 
				{
					localParent[vARoot] = vARoot;
					itemsList[vARoot].set(vARoot);
				}
				if(localParent[vBRoot] == -1) 
				{
					localParent[vBRoot] = vBRoot;	
					itemsList[vBRoot].set(vBRoot);
				}
				localParent[vARoot] = vBRoot;	
				itemsList[vBRoot].or(itemsList[vARoot]);				
			}
		}
		
		final BitSet siteIdList = new BitSet(totalVertices);	
		int centre = 0;
		final BitSet subTree = new BitSet(totalVertices);
		siteIdList.set(siteId);
		for(int i = 0; i < totalVertices; i++)
		{
			if(localParent[i] == i)
			{
				if(siteIdList.intersects(itemsList[i]))
				{
					centre = build(itemsList[i].nextSetBit(0), -1, adjacencyGraph, totalVertices);	
					subTree.or(itemsList[i]);
					break;
				}
			}
		}
		
		if(siteId == centre)
			return true;
		
		else
		{
			if((adjacencyGraph[centre].cardinality() == adjacencyGraph[siteId].cardinality()) 
					&& (adjacencyGraph[centre].cardinality() == 1))
						return true;
			
			boolean adjcentVertices = false;
			for(int k = 0; k < totalEdges; k++)
			{				
				if (((whoSiteId == numPlayers + 1 ) && (state.what(k, SiteType.Edge) != 0))
						||((whoSiteId < numPlayers + 1 ) && (state.who(k, SiteType.Edge) == whoSiteId)))
				{   
					final Edge kEdge = graph.edges().get(k);		
					final int vA  = kEdge.vA().index();
					final int vB  = kEdge.vB().index();
					if(((vA == siteId) && (vB == centre))||((vB == siteId) && (vA == centre)))
					{
						adjcentVertices = true;
						break;
					}
				}
			}
			
			if(adjcentVertices)
			{					
				final int level1, level2;
				final BitSet subTree1 = (BitSet) subTree.clone(); 
				level1 = depthLimit(siteId, 0, 0, subTree1, adjacencyGraph);
				final BitSet subTree2 = (BitSet) subTree.clone(); 
				level2 = depthLimit(centre, 0, 0, subTree2, adjacencyGraph);
				if(level1 == level2)
					return true;
			}
		}		
		return false;
	}

	//----------------------------------------------------------------------	
	/**
	 * 
	 * @param u  				A vertex.
	 * @param parent    		The parent of u.
	 * @param adjacencyGraph	The general Graph info.
	 * @param totalVertices		The total number of vertex in the global tree.
	 * 
	 * @return 					Returns all possible subTees information.
	 */
	private int build
	(
			final int u, 
			final int parent, 
			final BitSet[] adjacencyGraph, 
			final int totalVertices
	)
	{
		final BitSet[] sub = new BitSet[totalVertices];
		final BitSet n = dfs_subTreesGenerator(u, parent, adjacencyGraph, sub, totalVertices);	
		return dfsCenter(u, parent, n.cardinality(), adjacencyGraph, sub);	
	}
	
	//----------------------------------------------------------------------

	/**
	 * 
	 * @param u  				A vertex.
	 * @param parent    		The parent of u.
	 * @param adjacencyGraph	The general Graph info.
	 * @param sub  				Uses to store all possible subtree information.
	 * @return 					Returns all possible subTees information.
	 */	
	private BitSet dfs_subTreesGenerator
	(
			final int u, 
			final int parent, 
			final BitSet[] adjacenceGraph, 
			final BitSet[] sub, 
			final int totalVertices
	)
	{
		sub[u] = new BitSet(totalVertices);
		sub[u].set(u);
		for (int v = adjacenceGraph[u].nextSetBit(0); v >= 0; v = adjacenceGraph[u].nextSetBit(v + 1)) 
		{
			if(u != v)
			{
				if(v!= parent)
				{
					sub[u].or(dfs_subTreesGenerator(v, u, adjacenceGraph, sub, totalVertices));
				}
			}
		}
		return sub[u];
	}
	//----------------------------------------------------------------------
	
	/**
	 * 
	 * @param u  				A vertex.
	 * @param parent    		The parent of u.
	 * @param totalItems		The total number of Items of the present tree.
	 * @param adjacencyGraph	The general Graph info.
	 * @param sub  				Uses to store all possible subtree information.
	 * @return 					The center of a present tree.
	 */	
	private int dfsCenter
	(
			final int u, 
			final int parent, 
			final int totalItems, 
			final BitSet[] adjacencyGraph, 
			final BitSet[] sub
	)
	{		
		for (int v = adjacencyGraph[u].nextSetBit(0); v >= 0; v = adjacencyGraph[u].nextSetBit(v + 1)) 
		{
			if(u != v)
			{
				if(v!= parent)
				{					
					if(sub[v].cardinality() > totalItems/2)
					{
						return dfsCenter(v, u, totalItems, adjacencyGraph, sub);	
					}
				}
			}
		}
		return u;
	}
	//----------------------------------------------------------------------
	
	/**
	 * 
	 * @param u  				A vertex.
	 * @param index	    		The iterator for depth.
	 * @param maxIndex			The maxdepth.
	 * @param subTree			The Tree or subTree, where we will search the depth.
	 * @param adjacencyGraph	The general Graph info.
	 * @return 					The depth from any specific point.
	 */
	private int depthLimit
	(
			final int u, 
			final int index, 
			final int max,
			final BitSet subTree, 
			final BitSet[] adjacencyGraph
	)
	{
		subTree.clear(u);
		final int newIndex = index + 1;
		int newMax = max;
		if (newIndex > newMax) 
			newMax = newIndex;
		
		if (subTree.cardinality() == 0)
			return newMax;			
		
		for (int v = adjacencyGraph[u].nextSetBit(0); v >= 0; v = adjacencyGraph[u].nextSetBit(v + 1)) 
		{
			if(subTree.get(v))
			{				
				return depthLimit(v, newIndex, newMax, subTree, adjacencyGraph);
			}
		}		
		return newMax;
	}
	
	//----------------------------------------------------------------------
	
	/**
	 * 
	 * @param position  	A  vertex.
	 * @param parent 	    The array with parent id.
	 * 
	 * @return 				The root of the position.
	 */
	private int find(final int position, final int[] parent)
	{
		final int parentId = parent[position];	
		
		if ((parentId ==position) || (parentId == -1)) 
			return position;
		
		return find (parentId, parent);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{		
		return "IsTreeCenter( )";
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
		final BitSet concepts = new BitSet();
		concepts.or(who.concepts(game));
		return concepts;
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
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "Player " + who.toEnglish(game) + "'s last move is the centre of a tree";
	}
	
	//-------------------------------------------------------------------------
		
}