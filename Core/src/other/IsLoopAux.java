package other;

/**
 * Helping file of the isLoop.
 * 
 * @author tahmina
 * 
 */
public final class IsLoopAux 
{
//
//	/** Direction chosen. */
//	private final AbsoluteDirection dirnChoice;
//
//	/**
//	 * @param dirnChoice The absolute direction.
//	 */
//	public IsLoopAux(@Opt final AbsoluteDirection dirnChoice)
//	{
//		this.dirnChoice = (dirnChoice == null) ? AbsoluteDirection.Adjacent : dirnChoice;
//	}
//
//	/**
//	 * It's a boolean function.
//	 * 
//	 * Remarks This function execute before add the last move in Union-tree, which
//	 * helps to set a flag( which is ringflag) at the isLoop.java file.
//	 * 
//	 * @param context The context.
//	 * @param siteId Site ID for which we want to check whether we created a loop.
//	 * 
//	 * @return Are the neighbor's position of the last move is sufficient to create
//	 *         an Open ring?
//	 * 
//	 */
//	public boolean eval(final Context context, final int siteId)
//	{
//		final SiteType type;
//			 					 	
//		if (context.game().isEdgeGame())  	  type = SiteType.Edge;
//		else if (context.game().isCellGame()) type = SiteType.Cell;	
//		else								  type = SiteType.Vertex;
//	
//		if (siteId == Constants.UNDEFINED)
//			return false;
//		final Topology topology = context.topology();
//		
//		final ContainerState state 					= context.state().containerStates()[0];
//		final int whoSiteId 						= state.who(siteId, type);
//		
//		final List<? extends TopologyElement> elements;
//		if (type == SiteType.Vertex)
//		    elements = context.game().board().topology().vertices();
//		else if (type == SiteType.Edge)
//		    elements = context.game().board().topology().edges();
//		else if (type == SiteType.Cell)
//		    elements = context.game().board().topology().cells();
//		else
//		    elements = context.game().graphPlayElements();
//				
//		if (dirnChoice == AbsoluteDirection.Adjacent)
//		{ 
//			final TIntArrayList adjacentItemsList = new TIntArrayList(); 
//			final List<game.util.graph.Step> steps = topology.trajectories().steps(type, siteId, dirnChoice);
//
//			for (final game.util.graph.Step step : steps)
//				if (step.from().siteType() == step.to().siteType())
//					adjacentItemsList.add(step.to().id());			
//					
//			return loop(type, elements, siteId, state, adjacentItemsList, state.unionInfo(AbsoluteDirection.Adjacent)[whoSiteId]);	
//		}
//		else
//		{
//			final TIntArrayList adjacentItemsList = new TIntArrayList(); 
//			final List<game.util.graph.Step> steps = topology.trajectories().steps(type, siteId, type, dirnChoice);
//
//			for (final game.util.graph.Step step : steps)
//					adjacentItemsList.add(step.to().id());
//
//			final TIntArrayList directionItemsList = new TIntArrayList(); 
//			
//			final List<game.util.graph.Step> stepsDirectionsItemsList = topology.trajectories().steps(type, siteId,
//					type,
//					dirnChoice);
//
//			for (final game.util.graph.Step step : stepsDirectionsItemsList)
//				directionItemsList.add(step.to().id());
//
//			int count = 0;
//
//			for (int i = 0; i < directionItemsList.size(); i++)
//			{
//				final int ni = directionItemsList.get(i);
//				if (state.who(ni, type) == whoSiteId)
//				{
//					count++;
//				}
//			}
//			
//			if (count < 2)
//				return false;
//					
//			return loopOthers(context, siteId, state,  adjacentItemsList, directionItemsList, dirnChoice, state.unionInfo(AbsoluteDirection.Orthogonal)[whoSiteId]);
//		}
//	}
//	
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * @param type	 		The graph elements type.
//	 * @param elements 		The list of graph elements.
//	 * @param siteId        The last move of the current game state.
//	 * @param state         The present state of the game board.
//	 * @param nList 		The adjacent list of the last move.
//	 * @param uf            The object of the union-find.
//	 * 
//	 * @return Is it loop or not?
//	 */
//	private static boolean loop
//	(
//		final SiteType type,
//		final List<? extends TopologyElement> elements,
//		final int siteId, 
//		final ContainerState state,
//		final TIntArrayList  nList, 
//		final UnionInfoD uf
//	)
//	{
//		final int whoSiteId 						= state.who(siteId, type);
//		final int numNeighbours 					= nList.size();
//		final int[] localParent 					= new int[numNeighbours];
//		int adjacentSetsNumber 						= 0;
//		
////		System.out.println();
////		System.out.println("siteId = " + siteId);
////		System.out.println("nList = " + nList);
////		System.out.println("whoSiteId = " + whoSiteId);
////		System.out.println();
//
//		Arrays.fill(localParent, Constants.UNDEFINED); 
//
//		for (int i = 0; i < numNeighbours; i++)
//		{
//			final int ni = nList.getQuick(i);
//			if (state.who(ni, type) == whoSiteId)
//			{
//				if (localParent[i] == Constants.UNDEFINED)
//				{
//					localParent[i] = i;
//				}
//				
//				final TIntArrayList kList = elementsToIndices(elements.get(ni).adjacent());
//				final TIntArrayList intersectionList = intersection(nList, kList);
//
//				for (int j = 0; j < intersectionList.size(); j++)
//				{
//					final int nj = intersectionList.getQuick(j);
//
//					if ((state.who(nj, type) == whoSiteId) && (ni != siteId))
//					{
//						for (int m = 0; m < numNeighbours; m++)
//						{
//							if ((m != i) && (nj == nList.getQuick(m)))
//							{
//								if (localParent[m] == -1)
//								{
//									localParent[m] = i;
//									break;
//								}
//								else
//								{
//									int mRoot = m;
//									int iRoot = i;
//
//									while (mRoot != localParent[mRoot])
//										mRoot = localParent[mRoot];
//									while (iRoot != localParent[iRoot])
//										iRoot = localParent[iRoot];
//
//									localParent[iRoot] = localParent[mRoot]; // connect between two union trees
//									break;
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//
//		for (int k = 0; k < numNeighbours; k++)
//		{
//			if (localParent[k] == k)
//			{
//				adjacentSetsNumber++;
//			}
//		}
//		
//		// if the number sets of adjacency list more than one, only then it is required
//		// to check the open-ring
//		if (adjacentSetsNumber > 1) 						
//		{
//			for (int i = 0; i < numNeighbours; i++)
//			{
//				if (localParent[i] == i)
//				{
//					final int rootI = find(nList.getQuick(i), uf);
//					
//					for (int j = i + 1; j < numNeighbours; j++)
//					{
//						if (localParent[j] == j)
//						{ 						
//							if (uf.isSameGroup(rootI, nList.getQuick(j)))
//							{	
//								return true;
//							}
//						}
//					}
//
//				}
//			}
//		}
//		
//		return false;
//	}
//	
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * @param context  				The context of present game state.
//	 * @param siteId        		The last move of the current game state.
//	 * @param state         		The present state of the game board.
//	 * @param nList 				The adjacent list of the last move.
//	 * @param directionItemsList	The Adjacent position according to direction.
//	 * @param dirnChoice			The direction.
//	 * @param uf            		The object of the union-find.
//	 * 
//	 * 
//	 * @return Is it loop or not?
//	 */
//	private static boolean loopOthers
//	(
//		final Context context,
//		final int siteId, 
//		final ContainerState state,
//		final TIntArrayList nList,
//		final TIntArrayList directionItemsList, 
//		final AbsoluteDirection dirnChoice,
//		final UnionInfoD uf
//	)
//	{
//		
//		final SiteType type;
//		 	
//		if (context.game().isEdgeGame())  	  type = SiteType.Edge;
//		else if (context.game().isCellGame()) type = SiteType.Cell;	
//		else								  type = SiteType.Vertex;
//			
//		final int whoSiteId 						= state.who(siteId, type);
//		final int numNeighbours 					= nList.size();
//		final int[] localParent 					= new int[numNeighbours];
//		int adjacentSetsNumber 						= 0;
//		
//		Arrays.fill(localParent, -1); 		
//		
//		for (int i = 0; i < numNeighbours; i++)
//		{
//			final int ni = nList.getQuick(i);
//			
//			if (state.who(ni, type) == whoSiteId)
//			{
//				boolean orthogonalPosition = false;
//				for (int k = 0; k < directionItemsList.size(); k++)
//				{
//					final int oi =  directionItemsList.getQuick(k);
//
//					if (ni == oi)
//					{
//						orthogonalPosition = true;
//						break;
//					}
//				}
//
//				if (orthogonalPosition)
//				{					
//					if (localParent[i] == -1)
//					{
//						localParent[i] = i;
//					}
//					
//					final TIntArrayList kList = new TIntArrayList(); 
//					
//					final Topology topology = context.topology();
//					final List<game.util.graph.Step> steps = topology.trajectories().steps(type, ni, type, dirnChoice);
//
//					for (final game.util.graph.Step step : steps)
//						nList.add(step.to().id());
//
//					final TIntArrayList intersectionList = intersection(nList, kList);
//					
//					for (int j = 0; j < intersectionList.size(); j++)
//					{
//						final int nj = intersectionList.getQuick(j);
//						
//						if ((state.who(nj, type) == whoSiteId) && (ni != siteId))
//						{
//							for (int m = 0; m < numNeighbours; m++)
//							{
//								if ((m != i) && (nj == nList.getQuick(m)))
//								{
//									if (localParent[m] == -1)
//									{
//										localParent[m] = i;
//										break;
//									}
//									else
//									{
//										int mRoot = m;
//										int iRoot = i;
//						
//										while (mRoot != localParent[mRoot])
//											mRoot = localParent[mRoot];
//										while (iRoot != localParent[iRoot])
//											iRoot = localParent[iRoot];
//						
//										localParent[iRoot] = localParent[mRoot]; // connect between two union trees
//										break;
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//		
//		
//		for (int k = 0; k < numNeighbours; k++)
//		{
//				
//			if (localParent[k] == k)
//			{
//				adjacentSetsNumber++;
//			}
//		}
//		
//		if (adjacentSetsNumber > 1) // if the number sets of adjacency list more than one, only then it is required
//									// to check the open-ring
//		{
//			for (int i = 0; i < numNeighbours; i++)
//			{
//				if (localParent[i] == i)
//				{
//
//					final int rootI = find(nList.getQuick(i), uf);
//
//					for (int j = i + 1; j < numNeighbours; j++)
//					{
//						if (localParent[j] == j)
//						{
//							if (uf.isSameGroup(rootI, nList.getQuick(j)))
//							{
//								return true;
//							}
//						}
//					}
//				}
//			}
//		}
//
//		return false;
//	}
//
//	//--------------------------------------------------------------------------	
//	/**
//	 * 
//	 * @param position  	A cell number.
//	 * @param uf        	Object of union-find.
//	 * 
//	 * @return 				The root of the position.
//	 */
//	private static int find(final int position, final UnionInfoD uf)
//	{
//		final int parentId = uf.getParent(position);
//
//		if (parentId == position)
//			return position;
//		else
//			return find(uf.getParent(parentId), uf);
//	}
//	
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * @param elementsList A list of topology elements.
//	 * 
//	 * @return List of indices of the given topology elements.
//	 */
//	public static TIntArrayList elementsToIndices
//	(
//		final List<? extends TopologyElement> elementsList
//	) 
//	{		
//		final int verticesListSz = elementsList.size();
//		final TIntArrayList indicesList  = new TIntArrayList(verticesListSz);              
//
//		for (int i = 0; i < verticesListSz; i++)  
//		{
//			indicesList.add(elementsList.get(i).index());	        	
//		} 		
//		
//		return indicesList;
//	}
//	
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * @param verticesList The specific Adjacent vertices List.
//	 * @param cell         The index of the cell.
//	 * 
//	 * @return Convert Vertex type to integer list type
//	 */
//	public static boolean validDirection
//	(
//		final TIntArrayList verticesList, final int cell
//	) 
//	{		
//		final int verticesListSz = verticesList.size();
//		
//		for (int i = 0; i < verticesListSz; i++)  
//		{
//			if (verticesList.getQuick(i) == cell)
//				return true;
//		} 		
//		return false;
//	}	
//
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * @param verticesList The specific Adjacent vertices List.
//	 * @param cell         The index of the cell.
//	 * 
//	 * @return Convert Vertex type to integer list type
//	 */
//	public static boolean adjacentCells
//	(
//		final TIntArrayList verticesList, final int cell
//	) 
//	{		
//		final int verticesListSz = verticesList.size();
//		
//		for (int i = 0; i < verticesListSz; i++)  
//		{
//			if (verticesList.getQuick(i) == cell)
//				return true;
//		} 		
//		return false;
//	}	
//	//-------------------------------------------------------------------------
//
//	/**
//	 * @param list1 First list of ints.
//	 * @param list2 Second list ints.
//	 * 
//	 * @return List containing all ints that appear in both given lists.
//	 */
//	public static TIntArrayList intersection
//	(
//		final TIntArrayList list1, 
//		final TIntArrayList list2
//	)
//	{
//		final TIntArrayList list = new TIntArrayList();
//	
//		for (int i = 0; i < list1.size(); i++)  
//		{
//			if (list2.contains(list1.getQuick(i)))
//				list.add(list1.getQuick(i));		
//		}		
//		return list;
//	}
//
//	//-----------------------------------------------------------------------------
//
//	@Override
//	public String toString()
//	{
//		String str = "";
//		str += "IsLoopAux( )";
//		return str;
//	}
//
//	//-------------------------------------------------------------------------
//
//	
}
