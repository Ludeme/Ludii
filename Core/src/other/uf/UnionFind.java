package other.uf;
import java.io.Serializable;

//-----------------------------------------------------------------------------

/**
 * Main file to create Union tree
 * (with one by one move in each of the game state)
 * 
 * @author tahmina
 */
public class UnionFind implements Serializable
{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor.
	 */
	private UnionFind()
	{
		// Do nothing
	}	
	
//	/**
//	 * @param context      Present Context of the game board.
//	 * @param activeIsLoop loop information is required or not?.
//	 * @param dirnChoice   The direction.
//	 * @param site         The site.
//	 * @return ???
//	 */
//	public static boolean eval
//	(
//		final Context context, 
//		final boolean activeIsLoop, 
//		final AbsoluteDirection dirnChoice,
//		final int site
//	)
//	{
//		final SiteType type;
//		
//		if (context.game().isEdgeGame())  type = SiteType.Edge;
//		else if (context.game().isCellGame()) type = SiteType.Cell;		
//		else type = SiteType.Vertex;
//		
//		final int siteId  		   	= (site == Constants.UNDEFINED) ? context.trial().lastMove().toNonDecision() : site;
//		final ContainerState state 	= context.state().containerStates()[0];
//		final int whoSiteId         = state.who(siteId, type);
//		final int numPlayers	   	= context.game().players().count();
//			
//		if (whoSiteId < 1 || whoSiteId > numPlayers + 1)
//		{
//			//System.out.println("** Bad who in UnionFind.eval(): " + whoSiteId);	
//			return false;
//		}
//		
//		boolean ringflag 		   	= false;
//		final TIntArrayList neighbourList = new TIntArrayList(); 
//		
//		final Topology topology = context.topology();
//		final List<game.util.graph.Step> steps = topology.trajectories().steps(type, site, type, dirnChoice);		// TODO shouldn't we use siteId here?
//
//		for (final game.util.graph.Step step : steps)
//			neighbourList.add(step.to().id());
//		
//		if (activeIsLoop)
//			ringflag = new IsLoopAux(dirnChoice).eval(context);
//		
//		context.setRingFlagCalled(ringflag);
//		
//		union(siteId, state, state.unionInfo()[whoSiteId], whoSiteId, numPlayers, neighbourList, type);
//		union(siteId, state, state.unionInfo()[numPlayers + 1], numPlayers + 1, numPlayers, neighbourList, type);     
//					
//		return ringflag;
//	}
//	
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * @param siteId 	     The last move of the game.
//	 * @param state 		 Each state information.
//	 * @param uf 			 The object of the union-find. 
//	 * @param whoSiteId 	 Player type of last move.
//	 * @param numPlayers 	 The number of players.
//	 * @param neighbourList	 The adjacent list of our last movement.
//	 * 
//	 * Remarks  			 This function will not return any things.
//	 * 						 It able to connect the last movement with the existing union-tree. 
//	 * 						 Basically, the rank base union-find is chosen in this implementation. 
//	 * 						 So, that the height of the union tree will be minimum.
//	 */
//	private static void union
//	(
//		final int siteId, 
//		final ContainerState state, 
//		final UnionInfo uf, 
//		final int whoSiteId,
//		final int numPlayers,
//		final TIntArrayList neighbourList,
//		final SiteType type
//	)
//	{
//		final int numNeighbours = neighbourList.size();
//		
//		uf.setItem(siteId, siteId);
//		uf.setParent(siteId, siteId);
//		
//		for (int i = 0; i < numNeighbours; i++)
//		{	
//			final int ni = neighbourList.getQuick(i);
//			boolean connect  = true;
//			
//			if 
//			(
//				((whoSiteId == numPlayers + 1) && (state.who (ni, type) != 0)) || 
//				((whoSiteId != numPlayers + 1) && (state.who (ni, type) == whoSiteId))
//			)
//			{
//				for (int j = i + 1; j < numNeighbours; j++)
//				{
//					final int nj = neighbourList.getQuick(j);
//					
//					if (connected(ni, nj, uf))
//					{						
//						connect = false; 
//						break;
//					}
//				}
//				
//				if (connect)
//				{
//					final int rootP = find(ni, uf);
//				    final int rootQ = find(siteId, uf);
//				   
//				    if(rootP == rootQ)
//				    	return;	
//					
//					if (uf.getGroupSize(rootP) < uf.getGroupSize(rootQ))
//				   	{
//						uf.setParent(rootP, rootQ);						
//						uf.mergeItemsLists(rootQ, rootP);						
//					}
//					else
//					{
//						uf.setParent(rootQ, rootP);
//						uf.mergeItemsLists(rootP, rootQ);
//					}
//				}		
//			}
//		}			
//	}
//	
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * 
//	 * @param position1 	Integer position.
//	 * @param position2 	Integer position.
//	 * @param uf			Object of union-find.
//	 * @param whoSiteId     The current player type.
//	 * 
//	 * @return check 		Are the position1 and position1 in the same union tree or not?
//	 */
//	private static boolean connected(final int position1, final int position2, final UnionInfo uf)
//	{		
//		final int root1 = find(position1, uf);		
//		return uf.isSameGroup(root1, position2);	
//	}	
//	
//	/**
//	 * 
//	 * @param position   A cell number.
//	 * @param uf    	 Object of union-find. 
//	 * @param whoSiteId  The current player type. 
//	 * 
//	 * @return 			 The root of the position.
//	 */
//	private static int find(final int position, final UnionInfo uf)
//	{
//		final int parent = uf.getParent(position);
//		
//		if (parent == position) 
//			return position;
//		else 			
//			return find(uf.getParent(parent), uf);
//	}	
}
