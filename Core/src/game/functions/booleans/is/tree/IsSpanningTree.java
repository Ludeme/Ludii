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
 * Test the induced graph (by adding or deleting edges) is a spanning tree or
 * not.
 * 
 * @author tahmina and cambolbro and Eric.Piette
 * 
 * @remarks It is used for test the induced graph (by adding or deleting edges)
 *          is a spanning tree or not.
 */
@Hide
public class IsSpanningTree extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	
	/** The type of player. **/
	private final IntFunction who;

	//------------------------------------------------------------------
			
	/**
	 * @param who  Data about the owner of the tree.
	 * @param role RoleType of the owner of the tree.
	 */
	public IsSpanningTree
	(	 			
		@Or  final Player   who,
		@Or  final RoleType role
	)
	{		
		this.who = (role != null) ? RoleType.toIntFunction(role) : who.index();	
	} 

	//-----------------------------------------------------------------

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
		final int totalVertices		= graph.vertices().size();
		final int[] localParent    	= new int [totalVertices];		
		int totalExistingedges = 0;	
		
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
		for(int k = graph.edges().size() - 1; k >= 0; k--)
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
		
		//int count = 0;
		//for(int i = 0; i < totalVertices; i++)
		//{
		//	if(localParent[i] == i)
		//		count++;				
		//}				
		//return (count == 1);
		return true;
	}
	
	//------------------------------------------------------------------------	
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
	//------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "IsSpanningTree( )";
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
