package other.uf;
import java.io.Serializable;

/**
 *  Main file: Union-find-delete()
 *  
 * @author tahmina 
 */
public class UnionFindD implements Serializable
{
	private static final long serialVersionUID = 1L;
//
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * Constructor
//	 */
//	private UnionFindD()
//	{
//		// Do nothing
//	}
//	
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * @param context    	The current Context of the game board.
//	 * @param activeIsLoop  loop information is required or not?.
//	 * @param siteId        The last move, which we want to add in the union tree
//	 * @param dirnChoice 	The direction.
//	 * @return True if loop detected
//	 */
//	public static boolean eval
//	(
//		final Context context, 
//		final boolean activeIsLoop, 
//		final int siteId, 
//		final AbsoluteDirection dirnChoice
//	)
//	{		
//		final SiteType type  		 = getSiteType(context);//Define the type of graph elements
//		final ContainerState state   = context.state().containerStates()[0];
//		final int whoSiteId          = state.who(siteId, type);//it needs to change
//		final int numPlayers	     = context.game().players().count();
//		final int whoSiteIdNext      = context.state().next();				
//		final TIntArrayList nList 	 = new TIntArrayList(); 
//		final List<? extends TopologyElement> elements = context.game().graphPlayElements();
//		final Topology topology = context.topology();
//		
//		if (whoSiteId == 0)
//		{
//			if (state.what(siteId, type) != 0)	// Why GT in this case????
//				evalUFGT(context, siteId);
//
//			return false;
//		}
//		
//		if (whoSiteId < 1 || whoSiteId > numPlayers)
//		{
//			//System.out.println("** Bad who in UFD.eval(): " + whoSiteId);	
//			return false;
//		}
//		
//		if (siteId < 0 || siteId >= elements.size())
//		{
//			//System.out.println("** Bad id in UFD.eval(): ");	
//			return false;
//		}
//		
//		boolean ringFlag = false;
//		
//		if (state.unionInfo(dirnChoice)[whoSiteIdNext].getParent(siteId) != Constants.UNUSED)
//			evalD(context, siteId, true, dirnChoice);				// If the same location exist any opponent player remove it	
//		
//		final List<game.util.graph.Step> steps = topology.trajectories().steps(type, siteId, type, dirnChoice);
//		for (final game.util.graph.Step step : steps)
//			nList.add(step.to().id());
//		
//		if (activeIsLoop)
//			ringFlag = new IsLoopAux(dirnChoice).eval(context, siteId);
//		
//		context.setRingFlagCalled(ringFlag);
//		
//		// preprocessing need to set the orthogonal neighbour items in UFD and to reduce the valid position, which contains the friendly pieces
//		final TIntArrayList neighbourList = preProcessingLiberties(type, state, siteId, numPlayers, nList, state.unionInfo(dirnChoice)[whoSiteId], whoSiteId);	
//		
//		// Union for the current player or the current stone color	
//		union(siteId, neighbourList, true, state.unionInfo(dirnChoice)[whoSiteId], whoSiteId);	
//		
//		// preprocessing need to set the orthogonal neighbour items in UFD and to reduce the valid position, which contains the friendly pieces
//		final TIntArrayList neighbourListCommon = preProcessingLiberties(type, state, siteId, numPlayers, nList, state.unionInfo(dirnChoice)[numPlayers + 1], numPlayers + 1);
//		
//		// Union for the common player
//		union(siteId,  neighbourListCommon, true, state.unionInfo(dirnChoice)[numPlayers + 1], numPlayers + 1);	
//		
//		return ringFlag;
//	}
//	
//	//-------------------------------------------------------------------------
//
//	/**
//	 * "determineUnionTree" Delete works for capturing movement in Local state.
//	 * 
//	 * @param context    The current Context of the game board.
//	 * @param deleteId   deleteId, which we want to delete from the union tree.
//	 * @param dirnChoice The direction.
//	 */
//	public static void determineUnionTree
//	(
//		final Context context, 
//		final int deleteId, 			
//		final AbsoluteDirection dirnChoice
//	)
//	{	
//		final SiteType type  		= getSiteType(context);
//		final int cid               = context.containerId()[0];
//		final ContainerState state  = context.state().containerStates()[cid];
//		final int deletePlayer      = state.who(deleteId, type);	//it needs to change	
//																	//If the position is empty then delete opponent union tree's
//		if (deletePlayer == 0)
//		{
//			if (context.game().isGraphGame())
//			{
//				evalDeleteGT(context, deleteId);
//				return;
//			}
//		}
//		else if (state.unionInfo(dirnChoice)[deletePlayer].getParent(deleteId) == Constants.UNUSED)
//		{
//			evalD(context, deleteId, true, dirnChoice);	//General for the capturing games		
//		}
//		else
//		{ 												//pieces movement/swapping games
//			evalNoLibertyFriendlyD(context, deleteId, dirnChoice);
//		}		
//	}
//
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * "evalD" Delete works for deleting game stones.
//	 * 
//	 * @param context    The current Context of the game board.
//	 * @param deleteId   deleteId, which we want to delete from the union tree.
//	 * @param enemy      True if this is enemy connection.
//	 * @param dirnChoice The direction.
//	 */
//	public static void evalD
//	(
//		final Context context, 
//		final int deleteId, 
//		final boolean enemy, 
//		final AbsoluteDirection dirnChoice
//	)
//	{	
//		final SiteType type  		= getSiteType(context);
//		//final int cid               = context.containerId()[deleteId];//just change it
//		final ContainerState state  = context.state().containerStates()[0];
//		int deletePlayer        	= state.who(deleteId, type);//it needs to change
//		final int numPlayers	    = context.game().players().count();
//		//System.out.println(" deletePlayer site type 8:"+ state.what(8, SiteType.Edge));
//		//System.out.println(" deletePlayer site type:"+ state.what(deleteId, SiteType.Edge));
//		if (context.game().isGraphGame() && deletePlayer == 0)
//		{
//			evalDeleteGT(context, deleteId);
//			return;
//		}	
//		
//		if (enemy)
//			deletePlayer = context.state().next(); // if we need to deleted enemy, then whoSiteId change, so if group of enemy then it will be a list
//
//		// Deletion from specific union tree
//		deletion(type, context, deleteId, dirnChoice, true, false, state.unionInfo(dirnChoice)[deletePlayer], deletePlayer);
//		
//		// Deletion from common stones union tree
//		deletion(type, context, deleteId, dirnChoice, true, false, state.unionInfo(dirnChoice)[numPlayers + 1], numPlayers + 1);		
//	}
//		
//	//-----------------------------------------------------------------
//	
//	/**
//	 * @param type		 		The specific graph element type.
//	 * @param context	 		The current Context of the game board.
//	 * @param deleteLoc	 		The location to delete game stone from any the union tree.
//	 * @param dirnChoice 		Direction 
//	 * @param libertyFlag 		Because all the games need not to compute liberty.
//	 * @param blockingFlag		For the blocking game, there are some union tree of the free spaces.
//	 * @param uf				The UFD object
//	 * @param whoSiteId	 		The type of union tree.	
//	 * 
//	 * Remarks:			 		This function is used to the general deletion in UFD frameworks. 
//	 * 					
//	 */
//	private static void deletion
//	(
//		final SiteType type,
//		final Context context, 
//		final int deleteLoc, 
//		final AbsoluteDirection dirnChoice,
//		final boolean libertyFlag,
//		final boolean blockingFlag,
//		final UnionInfoD uf, 
//		final int whoSiteId
//	)
//	{	
//		final int cid               	= context.containerId()[deleteLoc];
//		final ContainerState state  	= context.state().containerStates()[cid];	
//		final int numPlayers	     	= context.game().players().count();
//		final int root               	= find(deleteLoc, uf);	//determine the union tree
//		final BitSet bitsetsDeletePlayer  = (BitSet) uf.getItemsList(root).clone(); //get all the items from the selected union tree
//																								//save the list into bitsetsDeletePlayer
//		final Topology topology = context.topology();
//
//		for (int i = bitsetsDeletePlayer.nextSetBit(0); i >= 0; i = bitsetsDeletePlayer.nextSetBit(i + 1)) 
//		{
//			uf.clearParent(i);		// this loop uses to clear all 
//			uf.clearItemsList(i);	// the existing items
//			
//			if (libertyFlag)
//				uf.clearAllitemWithOrthoNeighbors(i);	
//		 }
//		
//		bitsetsDeletePlayer.clear(deleteLoc); // from the saved list disable the deleteId, as it no need to reconstruct
//		
//		for (int i = bitsetsDeletePlayer.nextSetBit(0); i >= 0; i = bitsetsDeletePlayer.nextSetBit(i + 1)) 
//		{
//			final List<game.util.graph.Step> steps = topology.trajectories().steps(type, i, type, dirnChoice);
//			final TIntArrayList nList = new TIntArrayList(steps.size());
//				
//			for (final game.util.graph.Step step : steps)
//				nList.add(step.to().id());
//			
//			if (!blockingFlag)
//			{
//				final int nListSz = nList.size();
//				final TIntArrayList neighbourList = new TIntArrayList(nListSz);
//				
//				for (int j = 0; j < nListSz; j++)
//				{
//					final int ni = nList.getQuick(j);//non-blocking game, create a list of neighbour for reconstruct
//					final int who = state.who(ni, type);
//					
//					if 
//					(
//						((who == whoSiteId) && (whoSiteId != numPlayers + 1))
//						||
//						((who != whoSiteId) && (whoSiteId == numPlayers + 1))
//					)
//					{
//						neighbourList.add(ni);
//					}
//				}
//				
//				union(i, neighbourList, false, uf, whoSiteId);
//			}
//			else
//			{			// For blocking game, no need to create a neighbour list as it contains the empty location
//				union(i, nList, libertyFlag, uf, whoSiteId);
//			}
//		}
//	}
//	
//	/**
//	 * @param context The current Context of the game board.
//	 * @param siteId  The last move, which we want to add in the union tree
//	 * @param role    The roleType.
//	 * @param dir Direction for connectivity
//	 */
//	public static void evalSetGT
//	(
//		final Context context, 
//		final int siteId,
//		final RoleType role,
//		final AbsoluteDirection dir
//	)
//	{		
//		final SiteType type  		 = getSiteType(context);//Define the type of graph elements
//		final ContainerState state   = context.state().containerStates()[0];
//		final int whoSiteId          = state.who(siteId, type);//it needs to change
//		
//		//System.out.println("------------------>>>UFD role:"+ role);
//		if (whoSiteId == 0)
//		{	
//			//System.out.println("state.what(siteId, SiteType.Edge) :"+ state.what(siteId, SiteType.Edge));			
//			unionSetGT(context, siteId, dir);				
//		}
//	}
//	
//	//-----------------------------------------------------------------
//	
//	/**
//	 * @param context The current Context of the game board.
//	 * @param siteId The last move, which we want to add in a union tree
//	 * @param dir Direction for connectivity
//	 */
//	private static void unionSetGT
//	(
//		final Context context,		
//		final int siteId,
//		final AbsoluteDirection dir
//	)
//	{	
//		final Topology graph 	    = context.topology();
//		final ContainerState state 	= context.state().containerStates()[0];
//		//System.out.println("here unionSet");
//		//final SiteType type	= SiteType.Edge;
//		final int numplayers = context.game().players().count();
//		final Edge kEdge 	= graph.edges().get(siteId);			
//		
//		final int vA = kEdge.vA().index();
//		final int vB = kEdge.vB().index();	
//		for (int i = 1; i<= numplayers + 1; i++)
//		{
//			final int player = i;
//			final int rootP = find(vA, state.unionInfo(dir)[player]);
//			final int rootQ = find(vB, state.unionInfo(dir)[player]);
//			
//			if (rootP == rootQ)
//				return;
//			
//			 if (state.unionInfo(dir)[player].getGroupSize(rootP) == 0)
//			 {
//				 state.unionInfo(dir)[player].setParent(vA, vA);
//				 state.unionInfo(dir)[player].setItem(vA, vA);
//			 }
//			 
//			 if (state.unionInfo(dir)[player].getGroupSize(rootQ) == 0)
//			 {
//				 state.unionInfo(dir)[player].setParent(vB, vB);
//				 state.unionInfo(dir)[player].setItem(vB, vB);
//			 }
//		
//			 if (state.unionInfo(dir)[player].getGroupSize(rootP) <  state.unionInfo(dir)[player].getGroupSize(rootQ))
//			 {
//			 	state.unionInfo(dir)[player].setParent (rootP, rootQ);	
//			 	state.unionInfo(dir)[player].mergeItemsLists(rootQ, rootP);
//			 }
//			 else
//			 {
//				state.unionInfo(dir)[player].setParent(rootQ, rootP);		
//				state.unionInfo(dir)[player].mergeItemsLists(rootP, rootQ);
//			 }	
//		}		
//	}
//	
//	//-----------------------------------------------------------------
//	
//	/**
//	 * @param context	 The current Context of the game board.
//	 * @param siteId     The last move, which we want to add in a union tree
//	 * 
//	 */
//	private static void evalUFGT
//	(
//		final Context context,		
//		final int siteId
//	)
//	{	
//		final ContainerState state 	= context.state().containerStates()[0];
//		final int numplayers 		= context.game().players().count();
//		//StringRoutines.stackTrace();
//		
//		if (! (context.game().isGraphGame()))
//			return;
//		
//		final SiteType type	= SiteType.Edge;
//		final int player 	= state.who(siteId, type);
//		//System.out.println(" here UnionGT 425 player  "+player);
//		if (player < 1 || player > numplayers + 1)
//		{
//			//System.out.println(" return from UFGT");
//			return;
//		}
//		//System.out.println(" here UnionGT 425 id "+siteId);		
//		if ((player >= 1) && (player <= numplayers))
//		{
//			unionGT(context, siteId, player);
//			unionGT(context, siteId, numplayers + 1);
//		}
//		else
//		{
//			for(int i = 1; i<= numplayers + 1; i++)
//			{
//				//System.out.println("set at all players: "+ i);
//				unionGT(context, siteId, i);
//			}			
//		}		
//		
//	}
//	
//	//-----------------------------------------------------------------
//	
//	private static void unionGT
//	(
//		final Context context,
//		final int siteId,
//		final int player
//	)
//	{	
//		final Topology graph 	    = context.topology();
//		final ContainerState state 	= context.state().containerStates()[0];
//		final Edge kEdge 			= graph.edges().get(siteId);
//		final int vA = kEdge.vA().index();
//		final int vB = kEdge.vB().index();	
//		
//		final int rootP = find(vA, state.unionInfo(AbsoluteDirection.Adjacent)[player]);
//		final int rootQ = find(vB, state.unionInfo(AbsoluteDirection.Adjacent)[player]);	
//		
//		if (rootP == rootQ)
//			return;
//		
//		 if (state.unionInfo(AbsoluteDirection.Adjacent)[player].getGroupSize(rootP) == 0)
//		 {
//			 state.unionInfo(AbsoluteDirection.Adjacent)[player].setParent(vA, vA);
//			 state.unionInfo(AbsoluteDirection.Adjacent)[player].setItem(vA, vA);
//		 }
//		 
//		 if (state.unionInfo(AbsoluteDirection.Adjacent)[player].getGroupSize(rootQ) == 0)
//		 {
//			 state.unionInfo(AbsoluteDirection.Adjacent)[player].setParent(vB, vB);
//			 state.unionInfo(AbsoluteDirection.Adjacent)[player].setItem(vB, vB);
//		 }
//	
//		 if (state.unionInfo(AbsoluteDirection.Adjacent)[player].getGroupSize(rootP) < state.unionInfo(AbsoluteDirection.Adjacent)[player].getGroupSize(rootQ))
//		 {
//		 	state.unionInfo(AbsoluteDirection.Adjacent)[player].setParent (rootP, rootQ);	
//		 	state.unionInfo(AbsoluteDirection.Adjacent)[player].mergeItemsLists(rootQ, rootP);
//		 }
//		 else
//		 {
//			state.unionInfo(AbsoluteDirection.Adjacent)[player].setParent(rootQ, rootP);		
//			state.unionInfo(AbsoluteDirection.Adjacent)[player].mergeItemsLists(rootP, rootQ);
//		 }	
//	}
//	
//	//-----------------------------------------------------------------
//	
//	/**
//	 * @param context	 The current Context of the game board.
//	 * @param deleteId	 The last move, which we want to delete from union tree
//	 * 
//	 */
//	private static void evalDeleteGT
//	(
//		final Context context,
//		final int deleteId
//	)
//	{	
//		//System.out.println(" +++++++++++++++++++++++++++++++ ");
//		final ContainerState state 	= context.state().containerStates()[0];		
//		final SiteType type			= SiteType.Edge;
//		final int player 		    = state.who(deleteId, type);
//		final int numplayers 		= context.game().players().count();
//		//System.out.println("gt delete player deleteId eval :"+ deleteId);
//		//System.out.println("gt delete player player :"+ player);
//		//System.out.println("gt delete player what :"+ state.what(deleteId, type));
//		
//		if (player < 1 || player > numplayers + 1)
//		{
//			//System.out.println(" return from DeleteGT");
//			return;
//		}
//		
//		if ((player >= 1) && (player <= numplayers))
//		{
//			deleteGT(context, deleteId, player, player);
//			deleteGT(context, deleteId, numplayers + 1, player);
//		}
//		else
//		{
//			for (int i = 1; i<= numplayers + 1; i++)
//			{
//				//System.out.println("gt delete player i :"+ i);
//				deleteGT(context, deleteId, i, player);
//			}			
//		}
//	}
//	
//	//-----------------------------------------------------------------
//	
//	private static void deleteGT
//	(
//		final Context context,
//		final int deleteId,
//		final int playerUF,
//		final int compareId
//	)
//	{	
//		final Topology graph 	    = context.topology();
//		final ContainerState state 	= context.state().containerStates()[0];
//		final int totalEdges		= graph.edges().size();
//		final int totalVertices		= graph.vertices().size();
//		final int numplayers 		= context.game().players().count();
//		//final Edge kEdge 			= graph.edges().get(deleteId);
//
//		for (int i = 0; i < totalVertices; i++)
//		{
//			state.unionInfo(AbsoluteDirection.Adjacent)[playerUF].clearParent(i);		// this loop uses to clear all 
//			state.unionInfo(AbsoluteDirection.Adjacent)[playerUF].clearItemsList(i);
//			state.unionInfo(AbsoluteDirection.Adjacent)[playerUF].setParent(i, i);
//			state.unionInfo(AbsoluteDirection.Adjacent)[playerUF].setItem(i, i);
//		} 
//		//System.out.println(" compareId :"+ compareId +"  numplayers :"+ numplayers);	
//		for (int k = 0; k < totalEdges; k++)
//		{
//			if (k != deleteId)
//			{					
//				if 
//				(
//					(
//						(playerUF <= numplayers) 
//						&& 
//						((state.who(k, SiteType.Edge) == compareId) || (state.who(k, SiteType.Edge) == numplayers + 1))
//					)
//					||
//					(
//						(playerUF == numplayers + 1) 
//						&& 
//						(state.who(k, SiteType.Edge) != 0)
//					)
//				)
//				{
//					final Edge kEdge = graph.edges().get(k);
//					final int vA  = kEdge.vA().index();
//					final int vB  = kEdge.vB().index();					
//					final int rootP = find(vA, state.unionInfo(AbsoluteDirection.Adjacent)[playerUF]);
//					final int rootQ = find(vB, state.unionInfo(AbsoluteDirection.Adjacent)[playerUF]);						
//					
//					if (rootP == rootQ)
//						continue;					
//										
//					 if (state.unionInfo(AbsoluteDirection.Adjacent)[playerUF].getGroupSize(rootP) < state.unionInfo(AbsoluteDirection.Adjacent)[playerUF].getGroupSize(rootQ))
//					 {
//					 	state.unionInfo(AbsoluteDirection.Adjacent)[playerUF].setParent(rootP, rootQ);	
//					 	state.unionInfo(AbsoluteDirection.Adjacent)[playerUF].mergeItemsLists(rootQ, rootP);
//					 }
//					 else
//					 {
//						state.unionInfo(AbsoluteDirection.Adjacent)[playerUF].setParent(rootQ, rootP);	
//						state.unionInfo(AbsoluteDirection.Adjacent)[playerUF].mergeItemsLists(rootP, rootQ);
//					 }		
//				}
//			}
//		}
//	}
//	
//	//-----------------------------------------------------------------
//	
//	/**
//	 * @param siteId 	    	 The last move of the game.
//	 * @param validPosition		 neighbourList.
//	 * @param libertyFlag 		 Because all the games need not to compute liberty.
//	 * @param uf 			 	 The object of the union-find-delete(). 
//	 * @param whoSiteId 	 	 Player type of last move.
//	 * 
//	 * Remarks  			 	This function will not return any things.
//	 * 						 	It able to connect the last movement with the existing union-tree. 
//	 * 						 	Basically, the rank base union-find is chosen in this implementation. 
//	 * 						 	So, that the height of the union tree will be minimum.
//	 */
//	private static void union
//	(
//		final int siteId, 
//		final TIntArrayList validPosition,
//		final boolean libertyFlag,
//		final UnionInfoD uf, 
//		final int whoSiteId
//	)
//	{	
//		final int validPositionSz = validPosition.size();
//		
//		// Start out with a new little group containing just siteId
//		uf.setItem(siteId, siteId);		
//		uf.setParent(siteId, siteId);
//		
//		if (libertyFlag)
//			uf.setItemWithOrthoNeighbors(siteId, siteId);
//		
//		for (int i = 0; i < validPositionSz; i++)
//		{	
//			final int ni = validPosition.getQuick(i);			
//			boolean connectflag  = true;	
//			
//			if (uf.getParent(ni) != Constants.UNUSED) 			
//			{
//				for (int j = i + 1; j < validPositionSz; j++)
//				{
//					final int nj = validPosition.getQuick(j);					
//					if (connected(ni, nj, uf))
//					{						
//						connectflag = false; 
//						break;
//					}
//				}
//				if (connectflag)
//				{
//					final int rootP = find(ni, uf);
//				    final int rootQ = find(siteId, uf);
//				    
//				    if (rootP == rootQ)
//				    	return;
//				    			    
//				    if (uf.getGroupSize(rootP) < uf.getGroupSize(rootQ))
//					{
//						uf.setParent(rootP, rootQ);	
//						uf.mergeItemsLists(rootQ, rootP);
//						
//						if (libertyFlag)
//							uf.mergeItemWithOrthoNeighbors(rootQ, rootP);
//					}
//					else
//					{
//						uf.setParent(rootQ, rootP);		
//						uf.mergeItemsLists(rootP, rootQ);
//						
//						if (libertyFlag)
//							uf.mergeItemWithOrthoNeighbors(rootP, rootQ);	
//					}
//				}		
//			}			
//		}			
//	}
//	
//	//-----------------------------------------------------------------
//		
//	/**
//	 * @param context	 	The current Context of the game board.
//	 * @param deleteId	 	deleteId, which we want to delete from the union tree.
//	 * @param dirnChoice 	Direction 
//	 * 
//	 * Remarks:		 		It uses only delete friendly game stones.
//	 */
//	public static void evalNoLibertyFriendlyD
//	(
//		final Context context, 
//		final int deleteId,
//		final AbsoluteDirection dirnChoice	
//	)
//	{		
//		final SiteType type  		= getSiteType(context);
//		final int cid               = context.containerId()[deleteId];
//		final ContainerState state  = context.state().containerStates()[cid];
//		final int deletePlayer        	= state.who(deleteId, type);//it needs to change
//		
//		deletion(type, context, deleteId, dirnChoice, false, false, state.unionInfo(dirnChoice)[deletePlayer], deletePlayer);		
//	}	
//		
//	//-----------------------------------------------------------------
//	
//	/**
//	 * @param context    The current Context of the game board.
//	 * @param deleteId   deleteId, which we want to delete from the union tree.
//	 * @param dirnChoice The direction.
//	 * 
//	 *                   Remarks: It is used in the blocking game stone delete game
//	 *                   stone from all the opponent union trees.
//	 * 
//	 */
//	public static void evalDeletionForBlocking
//	(
//		final Context context, 
//		final int deleteId,
//		final AbsoluteDirection dirnChoice
//	)
//	{	
//		final SiteType type  		= getSiteType(context);
//		final int cid               = context.containerId()[deleteId];
//		final ContainerState state  = context.state().containerStates()[cid];
//		final int currentPlayer     = state.who(deleteId, type);
//		final int numPlayers	    = context.game().players().count();
//				
//		for (int deletePlayer = 1; deletePlayer <= numPlayers; deletePlayer++)		
//		{
//			if (currentPlayer != deletePlayer)
//			{				
//				deletion(type, context, deleteId, dirnChoice, false, true, state.unionInfoBlocking(dirnChoice)[deletePlayer], deletePlayer);
//			}
//		}		
//	}
//	
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * @param verticesList List of topology elements
//	 * 
//	 * @return List of indices of the given elements
//	 */
//	public static TIntArrayList elementIndices(final List<? extends TopologyElement> verticesList) 
//	{		
//		final int verticesListSz = verticesList.size();
//		final TIntArrayList integerVerticesList = new TIntArrayList(verticesListSz);              
//
//		for (int i = 0; i < verticesListSz; i++)  
//		{
//			integerVerticesList.add(verticesList.get(i).index());	        	
//		} 		
//		return integerVerticesList;
//	}		
//
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * @param type      The specific graph element type.
//	 * @param state     The current state of the game board.
//	 * @param siteId    The location to delete game stone from any the union tree.
//	 * @param numPlayer The total number of players.
//	 * @param nList     The neighbour position of the siteId.
//	 * @param uf        The UFD object
//	 * @param whoSiteId The type of union tree.
//	 * @return The list of the liberties
//	 * 
//	 *         Remarks: This function is used to the general preprocessing of
//	 *         deletion in UFD frameworks.
//	 * 
//	 */	
//	public static TIntArrayList preProcessingLiberties
//	(
//		final SiteType type, 
//		final ContainerState state, 
//		final int siteId,
//		final int numPlayer,
//		final TIntArrayList nList, 
//		final UnionInfoD uf, 
//		final int whoSiteId			
//	) 
//	{		
//		final int nListSz = nList.size();
//		final TIntArrayList neighbourList = new TIntArrayList(nListSz);
//		
//		for (int i = 0; i < nListSz; i++)
//		{
//			final int ni = nList.getQuick(i);	
//			
//			if 
//			(
//				((state.who(ni, type) == whoSiteId) && (whoSiteId != numPlayer + 1))
//				|| 
//				((state.who(ni, type) != 0) && (whoSiteId == numPlayer + 1))
//			)
//			{
//				neighbourList.add(ni);
//			}			
//			else
//			{				
//				uf.setItemWithOrthoNeighbors(siteId, ni);
//			}
//		}
//		
//		return neighbourList;
//	}		
//	
//	//-----------------------------------------------------------------------------
//	
//	/**
//	 * 
//	 * @param context The current game context.
//	 * 
//	 * @return type of graph elements.
//	 */
//	private static SiteType getSiteType(final Context context)
//	{		
//		/*
//		final SiteType type;		
//		if(context.activeGame().isEdgeGame())  type = SiteType.Edge;
//		else if(context.activeGame().isCellGame()) type = SiteType.Cell;		
//		else type = SiteType.Vertex;*/
//		
//		return (context.game().board().defaultSite() == SiteType.Vertex ? SiteType.Vertex : SiteType.Cell);
//	}	
//	
//	//-----------------------------------------------------------------------------
//	
//	/**
//	 * 
//	 * @param position1 	Integer position.
//	 * @param position2 	Integer position.
//	 * @param uf			Object of union-find.
//	 * @param whoSiteId     The current player type.
//	 * 
//	 * @return check 		Are the position1 and position2 in the same union tree or not?
//	 */
//	private static boolean connected
//	(
//		final int position1, 
//		final int position2, 
//		final UnionInfoD uf
//	)
//	{		
//		final int find1 = find(position1, uf);			
//		return uf.isSameGroup(find1, position2);	
//	}	
//	
//	//----------------------------------------------------------------------------
//	
//	/**
//	 * 
//	 * @param position   A cell number.
//	 * @param uf    	 Object of union-find. 
//	 * 
//	 * @return 			 The root of the position.
//	 */
//	private static int find(final int position, final UnionInfoD uf)
//	{
//		final int parentId = uf.getParent(position);
//		
//		if ((parentId == position) || (parentId == Constants.UNUSED)) 
//			return position;
//		else 
//			return find(parentId, uf);
//	}
}
