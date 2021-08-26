package game.functions.booleans.is.path;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastTo;
import game.functions.range.RangeFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.types.state.GameType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Cell;
import other.topology.Edge;
import other.topology.Topology;
import other.topology.Vertex;

/**
 * Boolean file to test the cycle or path with specific size.
 * 
 * @author tahmina and cambolbro and Eric.Piette
 * 
 * @remarks Used for any GT game.
 */
@Hide
public class IsPath extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
			
	/** Type of the element of the graph. */
	private final SiteType indexType;

	/** The from site. **/
	private final IntFunction from;

	/** The type of player. **/
	private final IntFunction who;
	
	/** The exact size of component. **/
	private final RangeFunction range;
	
	/** Closed path or not. **/
	private final BooleanFunction closedFlagFn;
	
	//-------------------------------------------------------------------------
			
	/**
	 * 
	 * @param type     The graph element type [default SiteType of the board].
	 * @param from     The site to look the path [(last To)].
	 * @param who      The owner of the pieces on the path.
	 * @param role     The role of the player owning the pieces on the path.
	 * @param range    The range size of the path.
	 * @param closed   It use to detect closed component.
	 */
	
	public IsPath
	(
						     final SiteType               type,
			@Opt             final IntFunction            from,
				  		@Or	 final game.util.moves.Player who,
				  		@Or  final RoleType               role,
				             final RangeFunction          range,
			@Opt  @Name      final BooleanFunction        closed
	)
	{
		indexType  	= type;
		this.who 			= (who == null)    ? RoleType.toIntFunction(role) : who.index();
		this.range 	        = range;
		closedFlagFn = (closed == null) ? new BooleanConstant(false) : closed;
		this.from = (from != null) ? from : new LastTo(null);
	} 

	//----------------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{	
		final int siteId = from.eval(context);
		if(siteId == Constants.OFF)
			return false;
		
		switch (indexType)
		{
			case Vertex:				
				return evalVertex(context, siteId);			
			case Edge:				
				return evalEdge(context, siteId);				
			case Cell:				
				return evalCell(context, siteId);				
			default:
				return false;
		}
	}
	
//---------------------------------------------------------------------------

	/**
	 * @param context  	The present context of game board.
	 * @param siteId 	The last move.
	 * @return 
	 */
	
	private boolean evalEdge(final Context context, final int siteId)
	{
		final Topology graph 			= context.topology();		
		final ContainerState state 		= context.state().containerStates()[0];
		final int whoSiteId 		    = who.eval(context);		
		final int totalVertices			= graph.vertices().size();
		final int totalEdges			= graph.edges().size();
		final int minLength             = range.eval(context).min(context);
		final int maxLength             = range.eval(context).max(context);
		final int[] disc    			= new int[totalVertices];
		final int[] low 				= new int[totalVertices];
		final BitSet stackMember		= new BitSet(totalVertices); 
		final Stack<Integer> st 		= new Stack<Integer>(); 
		final Edge kEdge  				= graph.edges().get(siteId);
		final int v1 	  				= kEdge.vA().index();
		final int v2 	  				= kEdge.vB().index();
		final int startingVertex 		= v1;
		final BitSet testBitset			= new BitSet(totalVertices);
		final BitSet edgeBitset			= new BitSet(totalEdges);		
		final BitSet[] adjacencyGraph	= new BitSet[totalVertices];
		
		for (int i = 0; i < totalVertices; i++)			
			adjacencyGraph[i]  = new BitSet(totalEdges);
		
		
		final int strongComponents = strongComponent(context,  startingVertex, -1, disc, low, st, stackMember, testBitset, whoSiteId, 1, totalVertices, v1, v2);
		final boolean closedFlag = closedFlagFn.eval(context);
		
		if (closedFlag)
		{
			for (int i = 0; i < totalEdges; i++)
			{
				if (state.who(i, indexType) ==  whoSiteId)
				{
					final Edge iEdge = graph.edges().get(i);
					final int vA  = iEdge.vA().index();
					final int vB  = iEdge.vB().index();
					adjacencyGraph[vA].set(vB);
					adjacencyGraph[vB].set(vA);
					if ((testBitset.get(vA)) && (testBitset.get(vB)))
						 edgeBitset.set(i);
				}
			}
			
			if (minLength == maxLength) // length
			{					
				if (strongComponents == edgeBitset.cardinality() && (strongComponents == minLength))
					return true;
			}
			else if ((maxLength > 2) && (strongComponents <= maxLength)) // range
				if (strongComponents == edgeBitset.cardinality() && (strongComponents > 2))
					return true;
			
			if(edgeBitset.cardinality() > strongComponents)
			{
				final int[] path = findShortestDistance(graph, adjacencyGraph, v1, v2, new BitSet(totalVertices), totalVertices); 
				int i = v2;
				int minDepth  = 1;
				while(path[i] != i )
				{					
					minDepth++;
					i = path[i];
				}
				
				if (minLength == maxLength)
					if (minDepth == minLength)
						return true;
				
				if (maxLength > 2)
					if (minDepth <= maxLength)
							return true;
			}
		}		
		else
		{
						
			if (strongComponents != 0)
				return false;
			
			for (int i = 0; i < totalEdges; i++)
			{
				final Edge iEdge = graph.edges().get(i);
				if (state.who(iEdge.index(), indexType) ==  whoSiteId)
					 edgeBitset.set(i);			   				
			}
						
			final BitSet depthBitset1 = new BitSet(totalVertices);
			final BitSet depthBitset2 = new BitSet(totalVertices);
			final BitSet visitedEdge  = new BitSet(totalEdges);
			final int componentSz;
			
			if (minLength == maxLength)
				componentSz = minLength;
			else
				componentSz = maxLength;
						
			dfsMinPathEdge(context, graph, kEdge, edgeBitset, visitedEdge, 0, v1, v2, componentSz, depthBitset1, whoSiteId);				
			dfsMinPathEdge(context, graph, kEdge, edgeBitset, visitedEdge, 0, v2, v1, componentSz, depthBitset2, whoSiteId);
			
			final int pathLength  = (depthBitset1.cardinality() - 1) + (depthBitset2.cardinality() - 1) + 1;	
			
			if (minLength == maxLength)
				if ((pathLength == minLength) && (visitedEdge.cardinality() + 1 == pathLength))
				return true;
			
		if (maxLength > minLength)
				if ((pathLength <= maxLength) && (visitedEdge.cardinality() + 1 == pathLength))
				return true;			
		}
			
		return false;
	}
	
	//---------------------------------------------------------------------------
	
	/**
	 * @param context  	The present context of game board.
	 * @param siteId 	The last move.
	 * @return 
	 */
	private boolean evalCell(final Context context, final int siteId)
	{
		final Topology graph 				= context.topology();
		final int cid              		= context.containerId()[0];
		final ContainerState state 		= context.state().containerStates()[cid];
		final int whoSiteId 		    = who.eval(context);		
		final int totalCells			= graph.cells().size();
		final int minLength             = range.eval(context).min(context);
		final int maxLength             = range.eval(context).max(context);
		final int[] disc    			= new int[totalCells];
		final int[] low 				= new int[totalCells];
		final BitSet stackMember		= new BitSet(totalCells); 
		final Stack<Integer> st 		= new Stack<Integer>(); 		
		final Cell kCell  				= graph.cells().get(siteId);
		boolean isolated 				= true;
		final List<Cell> nList 		    = kCell.adjacent();	
		final int v1 	  				= kCell.index();
		int v2 	  						= 0;
		final int startingVertex 		= v1;
		final boolean closedFlag = closedFlagFn.eval(context);
		
		for (int i = 0; i < nList.size(); i++)
		{			
			final Cell iVertex = nList.get(i);
			if(iVertex != kCell)
			{
				if (state.who(iVertex.index(), indexType) ==  whoSiteId)
				{
					v2 = iVertex.index();
					isolated = false;
					break;
				}
			}
		}	
		
		if((isolated) && (closedFlag))
			return false;	
		
		if((isolated) && (!closedFlag))
			if ((minLength == 1) && (maxLength == 1))
				return true;		
							
		final BitSet testBitset	= new BitSet(totalCells);
		final int[] vertexIndex = new int [totalCells];	
		final int[] vertexVisit = new int [totalCells];	
		
		final int strongComponents = strongComponent (context,  startingVertex, -1, disc, low, st, stackMember, testBitset, 
														whoSiteId, 1, totalCells, v1, v2);
			
		if (closedFlag)
		{
			if (minLength == maxLength)
			{	
				if (strongComponents == minLength)
					return true;						
	
				if (minLength < strongComponents)
				{					
					final TIntArrayList nList2 = new TIntArrayList();
					final List<Cell> nList1 	= graph.cells().get(kCell.index()).adjacent();	
		   		    
				    for (int i = 0; i < nList1.size(); i++)
					{
						final Cell iVertex = nList1.get(i);			
						if (state.who(iVertex.index(), indexType) ==  whoSiteId)
						{
							nList2.add(iVertex.index());
						}				
					}
				    
				    for(int i = 0; i < nList2.size(); i++) 
					{
						final int minDepth = dfsMinCycleSzVertexCell(context, graph, vertexVisit, vertexIndex, 1, kCell.index(), nList2.get(i), -1, Constants.INFINITY, whoSiteId);
						
						if ((minDepth == minLength) && (minDepth > 2))
							return true;
					}
					
				}
			}			
			else if (maxLength != 0)
			{
				if((strongComponents <= maxLength) && (strongComponents > 2))
					return true;						
								
				if(strongComponents > maxLength)
				{
					 final TIntArrayList nList2 = new TIntArrayList();
					 final List<Cell> nList1 	= graph.cells().get(kCell.index()).adjacent();	
			   		    
					    for (int i = 0; i < nList1.size(); i++)
						{
							final Cell iVertex = nList1.get(i);			
							if (state.who(iVertex.index(), indexType) ==  whoSiteId)
							{
								nList2.add(iVertex.index());
							}				
						}
					
					for(int i = 0; i < nList2.size(); i++) 
					{
						final int minDepth = dfsMinCycleSzVertexCell(context, graph, vertexVisit, vertexIndex, 1, kCell.index(), nList2.get(i), -1, Constants.INFINITY, whoSiteId);
						
						if((minDepth <= maxLength) && (minDepth > 2))
							return true;
					}
				}
			}			
		}		
		else
		{					
			if (strongComponents != 0)
				return false;
			if(maxLength > 0)	return true; //range	
			
			final List<Cell> nListVertex = graph.cells().get(kCell.index()).adjacent();	
			final TIntArrayList nList1 = new TIntArrayList();			
			
			for(int i = 0; i< nListVertex.size(); i++)
			{
				if(state.whoCell(nListVertex.get(i).index()) == whoSiteId)
						nList1.add(nListVertex.get(i).index());
			}
			
			if((nList1.size() > 2) || (nList1.size() < 1))
				return false;
					
			int pathSize1 = 0 ;
			
			if(nList1.size() == 1)
				pathSize1 = dfsMinPathSzVertexCell(context, 0, kCell.index(), v1, -1, minLength, whoSiteId) + 1;
			
			if (pathSize1 == minLength)
				return true;	
						
			int pathSize2 = 0 ;
			if(nList1.size() == 2)
			{
				pathSize1 = dfsMinPathSzVertexCell(context, 1, kCell.index(), nList1.getQuick(0), -1, minLength,
						whoSiteId);
				pathSize2 = dfsMinPathSzVertexCell(context, 1, kCell.index(), nList1.getQuick(1), nList1.getQuick(0),
						minLength, whoSiteId);
				final int pathSize = pathSize1 + pathSize2 + 1;
				
				if (pathSize == minLength)
					return true;	
			}
			
			return false;
		}			
		return false;
	}	
	
//---------------------------------------------------------------------------

	/**
	 * @param context  	The present context of game board.
	 * @param siteId 	The last move.
	 * @return 
	 */
	private boolean evalVertex(final Context context, final int siteId)
	{
		final Topology graph 				= context.topology();
		final int cid              		= context.containerId()[0];
		final ContainerState state 		= context.state().containerStates()[cid];
		final int whoSiteId 		    = who.eval(context);		
		final int totalVertices			= graph.vertices().size();
		final int totalEdges			= graph.edges().size();
		final int minLength	            = range.eval(context).min(context);
		final int maxLength	            = range.eval(context).max(context);
		final int[] disc    			= new int[totalVertices];
		final int[] low 				= new int[totalVertices];
		final BitSet stackMember		= new BitSet(totalVertices); 
		final Stack<Integer> st 		= new Stack<Integer>(); 		
		final Vertex kVertex  			= graph.vertices().get(siteId);
		boolean isolated 				= true;
		final List<Vertex> nList 		= kVertex.adjacent();	
		final int v1 	  				= kVertex.index();
		int v2 	  						= 0;
		final boolean closedFlag = closedFlagFn.eval(context);
		
		final int startingVertex 		= v1;
		
		for (int i = 0; i < nList.size(); i++)
		{
			final Vertex iVertex = nList.get(i);
			if(iVertex != kVertex)
			{
				if (state.who(iVertex.index(), indexType) ==  whoSiteId)
				{
					v2 = iVertex.index();
					isolated = false;
					break;
				}
			}
		}	
				
		if((isolated) && (closedFlag))
			return false;	
		
		if((isolated) && (!closedFlag))
			if((minLength == 1) ||(maxLength == 1))
				return true;	
				
		final BitSet testBitset	   = new BitSet(totalVertices);
		final BitSet edgeBitset	   = new BitSet(totalEdges);		
		final int[] vertexIndex    = new int [totalVertices];	
		final int[] vertexVisit = new int [totalVertices];	
		
		final int strongComponents = strongComponent(context,  startingVertex, -1, disc, low, st, stackMember, testBitset, whoSiteId, 1, totalVertices, v1, v2);
				
		if (closedFlag)
		{
			if (minLength == maxLength) // length
			{
				if(strongComponents == minLength)
					return true;
			
				if(strongComponents > minLength)
				{
					final TIntArrayList nListVertex = vertexToAdjacentNeighbourVertices1(context, kVertex.index(), whoSiteId);	 
					
					for(int i = 0; i < nListVertex.size(); i++) 
					{
						final int minDepth = dfsMinCycleSzVertexCell(context, graph, vertexVisit, vertexIndex, 1, kVertex.index(), nListVertex.get(i), -1, Constants.INFINITY, whoSiteId);
						
						if((minDepth == minLength) && (minDepth > 2))
							return true;
					}
				}
			}			
			else if (maxLength > minLength) // range
			{
				if((strongComponents <= maxLength) && (strongComponents > 2))
					return true;
			
				if(strongComponents > maxLength)
				{
					final TIntArrayList nListVertex = vertexToAdjacentNeighbourVertices1(context, kVertex.index(), whoSiteId);	 
					
					for(int i = 0; i < nListVertex.size(); i++) 
					{
						final int minDepth = dfsMinCycleSzVertexCell(context, graph, vertexVisit, vertexIndex, 1, kVertex.index(), nListVertex.get(i), -1, Constants.INFINITY, whoSiteId);
						
						if((minDepth <= maxLength) && (minDepth > 2))
							return true;
					}
				}
			}
		}		
		else
		{						
			if (strongComponents != 0)		return false;	
			if (maxLength > minLength)
				return true; // range
			
			final TIntArrayList nListVertex = vertexToAdjacentNeighbourVertices1(context, kVertex.index(), whoSiteId);	
			
			if((nListVertex.size() > 2 ) || (nListVertex.size() < 1 ) )
				return false;
			
			for (int i = 0; i < totalEdges; i++)
			{
				final Edge iEdge = graph.edges().get(i);
				if (state.who(iEdge.index(), indexType) ==  whoSiteId)
					 edgeBitset.set(i);			   				
			}
			
			if (minLength == maxLength)
			{						
				int pathSize = 0;
				if(nListVertex.size() == 1)
					pathSize = dfsMinPathSzVertexCell(context, 0, kVertex.index(), v1, -1, minLength, whoSiteId) + 1;			
				
				if(pathSize == minLength)
					return true;						
			
				int pathSize1 = 0;
				int pathSize2 = 0;
				if(nListVertex.size() == 2)
				{
					pathSize1 = dfsMinPathSzVertexCell(context, 1, kVertex.index(), nListVertex.getQuick(0), -1, minLength, whoSiteId);
					pathSize2 = dfsMinPathSzVertexCell(context, 1, kVertex.index(), nListVertex.getQuick(1), nListVertex.getQuick(0), minLength, whoSiteId);
					final int path = pathSize1 + pathSize2 + 1;
					
					if(path == minLength)
						return true;	
				}				
				return false;
			}
			
			return false;			
		}			
		return false;
	}
	

	//----------------------------------------------------------------------------	
	
	/**
	 * @param context	 	  			The context of present game state.
	 * @param presentVertex   			The present position.
	 * @param parent				 	The parent of the present position.	
	 * @param visit					  	All visited vertices.
	 * @param low	 					It use to calculated the cycle.
	 * @param stackInfo	  				It use to stack the visited information.
	 * @param stackInfoBitset          	use to get the information about stack.
	 * @param testBitset1			 	keep the information about the large cycle of last move.
	 * @param whoSiteId       			The last move player's type.
	 * @param index		   				Dfs iteration counter.
	 * @param totalItems            	The total vertices of G.	
	 * @param v1						A vertex.
	 * @param v2				       	A vertex.
	 * 
	 * @remarks This function uses to find a Strong Connected Component(modified - Tarjan's Algorithm), which contains the last move.
	 *          
	 */
	
	private int strongComponent
	(		
		final Context context, 
		final int presentPosition, 
		final int parent, 
		final int[] visit, 
		final int[] low, 
		final Stack<Integer> stackInfo, 
		final BitSet stackInfoBitset, 
		final BitSet testBitset1,
		final int whoSiteId, 
		final int index, 
		final int totalItems,
		final int v1, 
		final int v2
	) 
	{	
		final Topology graph 			= context.topology();
		final ContainerState state 		= context.state().containerStates()[0];
		
		visit[presentPosition] = index;
		low[presentPosition]  = index; 		
		
		stackInfo.push(Integer.valueOf(presentPosition));
	    stackInfoBitset.set(presentPosition);
	   
	    TIntArrayList nList = new TIntArrayList();
	    
	    if(indexType.equals(SiteType.Cell))
		{
	    	final List<Cell> nList1 	= graph.cells().get(presentPosition).adjacent();	
		   		    
		    for (int i = 0; i < nList1.size(); i++)
			{
				final Cell iVertex = nList1.get(i);			
				if (state.who(iVertex.index(), indexType) ==  whoSiteId)
				{
					nList.add(iVertex.index());
				}				
			}	 
		}
	    
	    else if(indexType.equals(SiteType.Vertex))
		{
	    	 nList = vertexToAdjacentNeighbourVertices1(context, presentPosition, whoSiteId);	    
		}
	    
	    else if(indexType.equals(SiteType.Edge))
		{
	    	 nList = vertexToAdjacentNeighbourVertices(context, presentPosition, whoSiteId);	        
		}	    
	    	    	    
	    for (int i = 0; i != nList.size(); ++i) 
	    { 
	        final int v = nList.get(i);
	        
	        if(v == parent)
	        	continue;
	        
	        if (visit[v] == 0) 
	        { 	   
	        	
	        	strongComponent(context, v, presentPosition, visit, low, stackInfo, stackInfoBitset, testBitset1, whoSiteId, index + 1, totalItems, v1, v2); 	
	            low[presentPosition]  = (low[presentPosition] < low[v]) ? low[presentPosition] : low[v];
	        }
	        	        
	        else 
        	{	
	        	if (stackInfoBitset.get(v)) 
	        	{
	        		low[presentPosition]  = (low[presentPosition] < visit[v]) ? low[presentPosition]  : visit[v]; 
	        	}
        	}
	    } 
	    int w = 0; 
	    final BitSet testBitset	= new BitSet(totalItems);
	    if (low[presentPosition] == visit[presentPosition]) 
	    { 	    	
			while (stackInfo.peek().intValue() != presentPosition)
	        { 	
				w = stackInfo.peek().intValue();
	            stackInfoBitset.clear(w);
	            testBitset.set(w);
	            stackInfo.pop(); 
	        }	       
			w = stackInfo.peek().intValue();
	        stackInfoBitset.clear(w);
	        testBitset.set(w);
	        stackInfo.pop(); 
	    } 
	   
	    if((testBitset.get(v1)) && (testBitset.get(v2)))
	    {
	    	for (int i = testBitset.nextSetBit(0); i >= 0; i = testBitset.nextSetBit(i + 1)) 
			{
	    		testBitset1.set(i);
			}  
	    		return testBitset.cardinality();
	    }	    
		return 0;
	}
	
	
	//---------------------------------------------------------------------------

	/**
	 * 
	 * @param graph  	 		 The board graph.
	 * @param adjacenceGraph 	 The coloured graph.
     * @param from  	 		 The starting vertex.
	 * @param to 	 			 The ending vertex.	
     * @param visited  	 		 If the vertex is visited.
	 * @param totalVertices 	 The total vertices at the board graph.
	 * 
	 * @return 				     All the path array with a list of parent vertices.
	 * 
	 * */
		
  public static int[] findShortestDistance
	(
			final Topology graph,
			final BitSet[] adjacenceGraph, 				
			final int from, 
			final int to, 
			final BitSet visited, 
			final int totalVertices
	) 
	{		    
	    final Queue<Integer> toVisit = new PriorityQueue<Integer>();
	    final int[] dist = new int [totalVertices];
	    final int[] path = new int [totalVertices];	    
	    Arrays.fill(dist, Constants.INFINITY);
	    
	    toVisit.add(Integer.valueOf(from)); 
	    
	    dist[from] = 0;
	    path[from] = from;
	    
	    while (!toVisit.isEmpty()) 
	    {
	        final int u = toVisit.remove().intValue();
	       
	        if (u == to)
	        {
	        	return path;
	        }
	         
	        if (visited.get(u))
	            continue;
	       
	        visited.set(u);
	        
	        final Edge kEdge = graph.findEdge(graph.vertices().get(to), graph.vertices().get(from));
	        for (int v = adjacenceGraph[u].nextSetBit(0); v >= 0; v = adjacenceGraph[u].nextSetBit(v + 1)) 
			{
	        	final Edge uv = graph.findEdge(graph.vertices().get(v), graph.vertices().get(u));
	        	if(uv == kEdge)
	        		continue;
	        	final int weight = 1;
	        	
	        	if(dist[v] > (dist[u] + weight))
	        	{
	        		dist[v] = dist[u] + weight;
	        		path[v] = u;
	        		toVisit.add(Integer.valueOf(v));
	        	}
	        	
	        }
	    }
	    return path;
	}

	
	//---------------------------------------------------------------------------	
	
	/**
	 * @param context	 	  	The context of present game state.
	 * @param graph           	Present status of graph.
	 * @param vertexIndex	  	minimum distance from the starting point.
	 * @param index           	Dfs state counter.
	 * @param startingVertex 	The starting point for a cycle.
	 * @param presentVertex   	The present position.
	 * @param parent          	The parent of the present position.	
	 * @param minDepth			It use to store the information about minimum cycle.
	 * @param whoSiteId       	The last move player's type.
	 * 
	 * @remarks dfsMinCycleSzVertex() uses to find minimum cycle within in a strong component.
	 *          
	 */
	
	private int dfsMinCycleSzVertexCell
	(
			final Context context,
			final Topology graph,
			final int[] vertexVisit,
			final int[] vertexIndex,
			final int index,
			final int startingVertex,
			final int presentVertex,
			final int parent,
			final int minDepth,
			final int whoSiteId
	)
	{	
		final int cid              		= context.containerId()[0];
		final ContainerState state 		= context.state().containerStates()[cid];
		final int presentDegree = graph.vertices().get(presentVertex).adjacent().size();
		int newindex 	= 0;
		int newMinDepth = 0;
		
		vertexVisit[presentVertex]++;	
		
		if(vertexVisit[presentVertex] > presentDegree)
			return index;
		
		if(minDepth == 3) return minDepth;
		
		if(vertexIndex[presentVertex] == 0)
		{
			vertexIndex[presentVertex] = index;	
			newindex = index;
		}
		else
		{
			newindex = vertexIndex[presentVertex];
		}
		
		newMinDepth = minDepth;
		
		if(startingVertex == presentVertex)
		{
			if(minDepth > index)
			{
				newMinDepth = index;
				return newMinDepth + 1;
			}			
		}
		
		if(indexType.equals(SiteType.Cell))
		{
			final List<Cell> nList1 = graph.cells().get(presentVertex).adjacent();
			for (int i = 0; i < nList1.size(); i++)
			{		
				final int iVertex = nList1.get(i).index();
				if(iVertex != parent)	
				{
					if (state.who(iVertex, indexType) ==  whoSiteId)
					{
						dfsMinCycleSzVertexCell(context, graph, vertexVisit, vertexIndex, newindex + 1, startingVertex, iVertex, presentVertex, newMinDepth, whoSiteId);
					}	
				}
			}
		}
		
		else if(indexType.equals(SiteType.Vertex))
		{
			final TIntArrayList nListVertex = vertexToAdjacentNeighbourVertices1(context, presentVertex, whoSiteId);	 
			
			for(int i = 0; i < nListVertex.size(); i++) 
			{
				final int ni = nListVertex.get(i);
				
				if((newindex == 1) && (ni != startingVertex))
					if(presentVertex != ni)
						dfsMinCycleSzVertexCell(context, graph, vertexVisit, vertexIndex, newindex + 1, startingVertex, ni, presentVertex, newMinDepth, whoSiteId);
			}
		}
			
		return newindex + 1;
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
	
	//--------------------------------------------------------------------------------------------

	/**
	 * @param context	 	  The context of present game state.
	 * @param index           Dfs state counter.
	 * @param startingVertex  The starting point for a Path.
	 * @param presentVertex   The present position.
	 * @param parent          The parent of the present position.	
	 * @param mincomponentsz  The desire component size.
	 * @param whoSiteId       The last move player's type.
	 * 
	 * @remarks dfsMinPathSzVertexCell() uses to find the path length.
	 *          
	 */
	
	private int dfsMinPathSzVertexCell
	(
			final Context context,
			final int index,
			final int startingVertex,
			final int presentVertex,
			final int parent,
			final int mincomponentsz,
			final int whoSiteId
	)
	{
		final Topology graph 				= context.topology();
		final int cid              		= context.containerId()[0];
		final ContainerState state 		= context.state().containerStates()[cid];
		
		
		if(index == mincomponentsz * 2)
			return index;
		
		TIntArrayList nListVertex = new TIntArrayList();
		
		if(indexType.equals(SiteType.Cell))
		{
			final List<Cell> nList = graph.cells().get(presentVertex).adjacent();	
								
			for(int i = 0; i< nList.size(); i++)
			{
				if(state.whoCell(nList.get(i).index()) == whoSiteId)
					nListVertex.add(nList.get(i).index());
			}
		}
		
		if(indexType.equals(SiteType.Vertex))		
			nListVertex = vertexToAdjacentNeighbourVertices1(context, presentVertex, whoSiteId);	 
				
		if(nListVertex.size() > 2 ) 
			return Constants.INFINITY; 			
		
		if(nListVertex.size() == 0 ) 
			return index; 
		
		for(int i = 0; i < nListVertex.size(); i++)
		{
			if( nListVertex.getQuick(i) != parent)	
				if( nListVertex.getQuick(i) != startingVertex)	
					return dfsMinPathSzVertexCell(context, index + 1, startingVertex, nListVertex.get(i), presentVertex, mincomponentsz,  whoSiteId);
		}
					
		return index;
	}
	
	//-----------------------------------------------------------------------	

	/**
	 * 
	 * @param context	 	  The context of present game state.
	 * @param v				  A vertex of G.
	 * @param whoSiteId       The last move player's type.
	 * @return 				
	 */
	private  TIntArrayList vertexToAdjacentNeighbourVertices
	(
			final Context context, 
			final int v, 
			final int whoSiteId
	)
	{
		final ContainerState state 		= context.state().containerStates()[0];
		final int totalEdges 			= context.topology().edges().size();		
		final TIntArrayList nList 		= new TIntArrayList();
		
		for(int k = 0; k < totalEdges; k++)
		{
			final Edge kEdge = context.topology().edges().get(k);
			
			if(state.who(kEdge.index(), indexType) ==  whoSiteId)
			{
				final int vA  = kEdge.vA().index();
				final int vB  = kEdge.vB().index();
				if(vA == v)
						nList.add(vB);
				else
					if(vB == v)
						nList.add(vA);
			}
		}
		
		return nList;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param context	 	  The context of present game state.
	 * @param v				  A vertex of G.
	 * @param whoSiteId       The last move player's type.
	 * @return 				
	 */
	private  TIntArrayList vertexToAdjacentNeighbourVertices1
	(
			final Context context, 
			final int v, 
			final int whoSiteId
	)
	{
		final ContainerState state 		= context.state().containerStates()[0];
		final TIntArrayList nList 		= new TIntArrayList();
		final List<Vertex> nList1 		=  context.topology().vertices().get(v).adjacent();
				
		for(int k = 0; k < nList1.size(); k++)
		{
			if(nList1.get(k).index() != v)
			{
				if(state.who(nList1.get(k).index(), indexType) ==  whoSiteId)
				{
					nList.add(nList1.get(k).index());
				}
			}
		}		
		return nList;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		String str = "";
		str += "IsPath( )";
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
		long flags = GameType.Graph;
		flags |= from.gameFlags(game);
		flags |= closedFlagFn.gameFlags(game);
		flags |= range.gameFlags(game);

		if (who != null)
			flags |= who.gameFlags(game);

		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(from.concepts(game));
		concepts.or(closedFlagFn.concepts(game));
		concepts.or(range.concepts(game));

		if (who != null)
			concepts.or(who.concepts(game));

		return concepts;
	}
	
	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(from.writesEvalContextRecursive());
		writeEvalContext.or(closedFlagFn.writesEvalContextRecursive());
		writeEvalContext.or(range.writesEvalContextRecursive());

		if (who != null)
			writeEvalContext.or(who.writesEvalContextRecursive());

		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(from.readsEvalContextRecursive());
		readEvalContext.or(closedFlagFn.readsEvalContextRecursive());
		readEvalContext.or(range.readsEvalContextRecursive());

		if (who != null)
			readEvalContext.or(who.readsEvalContextRecursive());

		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= from.missingRequirement(game);
		missingRequirement |= closedFlagFn.missingRequirement(game);
		missingRequirement |= range.missingRequirement(game);

		if (who != null)
			missingRequirement |= who.missingRequirement(game);

		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= from.willCrash(game);
		willCrash |= closedFlagFn.willCrash(game);
		willCrash |= range.willCrash(game);

		if (who != null)
			willCrash |= who.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		from.preprocess(game);
		closedFlagFn.preprocess(game);
		range.preprocess(game);

		if (who != null)
			who.preprocess(game);
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		String text ="";
		text+= who.toEnglish(game)+ " "+  indexType.name() + " length is "  + range + " and " + "component closed is "+ closedFlagFn;
		return text;

	}
}
