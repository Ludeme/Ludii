package game.functions.ints.size.connection;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.directions.AbsoluteDirection;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Returns the total number of sites enclosed by a specific Player.
 *
 * @author tahmina
 * 
 * @remarks This ludeme is used in territory counting games such as Go.
 */
@Hide
public final class SizeTerritory extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The index of player. **/
	private final IntFunction indexPlayer;
	
	/** Direction of the connection. */
	private final AbsoluteDirection direction;
	

	/** Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type      The graph element type [default SiteType of the board].
	 * @param role      The roleType of the player owning the components in the
	 *                  territory.
	 * @param player    The index of the player owning the components in the
	 *                  territory.
	 * @param direction The type of directions from the site to compute the group
	 *                  [Adjacent].
	 */
	public SizeTerritory
	(
			@Opt final SiteType               type,
		@Or      final RoleType               role, 
		@Or      final game.util.moves.Player player,
			@Opt final AbsoluteDirection      direction
	)
	{
		this.direction 	= (direction == null) ? AbsoluteDirection.Orthogonal : direction;
		indexPlayer = (player != null) ? player.index() : RoleType.toIntFunction(role);
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final List<? extends TopologyElement> elements = context.game().graphPlayElements();
		final ContainerState state = context.state().containerStates()[0];
		final int whoSiteId        = indexPlayer.eval(context);
		final int totalElements    = elements.size();		
		final int[] localParent    = new int [totalElements];
		final int[] rank           = new int [totalElements];		
		final BitSet[] localItemWithOrth = new BitSet[totalElements]; 
		int sizeTerritory 		   = 0;
		final TIntArrayList nList = new TIntArrayList();
		final Topology topology = context.topology();
		
		for (int i = 0; i < totalElements; i++)
		{	
			localItemWithOrth[i] = new BitSet(totalElements); 			
			localParent[i] = Constants.UNUSED;
			rank[i] = 0;				
		}	
		
		for (int k = 0; k < totalElements; k++)
		{
			if (state.who(k, type) == 0)
			{
				localParent[k] = k;
				localItemWithOrth[k].set(k);				
				final List<game.util.graph.Step> steps = topology.trajectories().steps(type, k, type, direction);
				
				for (final game.util.graph.Step step : steps)
					nList.add(step.to().id());

				for (int i = 0; i < nList.size(); i++)					
					localItemWithOrth[k].set(nList.getQuick(i));					
				
				for (int i = 0; i < nList.size(); i++)
				{	
					final int ni = nList.getQuick(i);
					boolean connect = true;
					
					if ((state.who(ni, type) == 0) && (ni < k))
					{		
						for (int j = i + 1; j < nList.size(); j++)
						{
							final int nj = nList.getQuick(j);
							if (state.who(nj, type) == 0)
							{	
								if (connected(ni, nj, localParent))
								{						
									connect = false; 
									break;
								}
							}
						}
						if (connect)
						{							
							final int rootP = find(ni, localParent);
						    final int rootQ = find(k, localParent);
						   
							if (rank[rootP] < rank[rootQ])
							{
								localParent[rootP] = rootQ;						
								localItemWithOrth[rootQ].or(localItemWithOrth[rootP]);				           
							}
							else
							{
								localParent[rootQ] = rootP;	
								localItemWithOrth[rootP].or(localItemWithOrth[rootQ]);	
								
								if (rank[rootP] == rank[rootQ])
									rank[rootP]++;					
							}
						}		
					}
				}
			}
		}
		
		for (int i = 0; i < totalElements; i++)
		{	
			if (i == localParent[i])
			{
				boolean flagTerritory = true;
				int count = 0;

				for (int j = localItemWithOrth[i].nextSetBit(0); j >= 0; j = localItemWithOrth[i].nextSetBit(j + 1)) 
				{
					if (state.who(j, type) == 0)
						count++;
					
					if (state.who(j, type) != whoSiteId && state.who(j, type) != 0)
						flagTerritory = false;					
				}
				
				if (flagTerritory)
					sizeTerritory += count;				
			}
		}	
		
		return sizeTerritory;
	}
			
	//------------------------------------------------------------------------
	/**
	 * 
	 * @param position1 	Integer position.
	 * @param position2 	Integer position.
	 * @param parent 	    The array with parent id.
	 * 
	 * @return check 		Are the position1 and position2 in the same union tree or not?
	 */
	private boolean connected(final int position1, final int position2, final int[] parent)
	{		
		final int root1 = find(position1, parent);		
		final int root2 = find(position2, parent);
		
		return root1 == root2;
	}	
	
	/**
	 * 
	 * @param position  	A cell number.
	 * @param parent 	    The array with parent id.
	 * 
	 * @return 				The root of the position.
	 */
	private int find(final int position, final int[] parent)
	{
		final int parentId = parent[position];
		
		if (parentId == Constants.UNUSED) 
			return position;
		
		if (parentId == position) 
			return position;
		
		return find (parentId, parent);
	}
	
	//-------------------------------------------------------------------------
	/**
	 * 
	 * @param verticesList		All the adjacent vertices list of the present position.
	 * @return 					return  a list of all direction adjacent list.
	 * 							
	 */
	public static TIntArrayList validPositionAll(final List<? extends TopologyElement> verticesList) 
	{	
		final int verticesListSz = verticesList.size();
		final TIntArrayList integerVerticesList = new TIntArrayList(verticesListSz);              

		for (int i = 0; i < verticesListSz; i++)  
		{
			integerVerticesList.add(verticesList.get(i).index());	        	
		} 
		
		return integerVerticesList;
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
		long flags = indexPlayer.gameFlags(game);

		flags |= SiteType.gameFlags(type);

		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(indexPlayer.concepts(game));
		concepts.or(SiteType.concepts(type));
		concepts.set(Concept.Territory.id(), true);
		return concepts;
	}
	
	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(indexPlayer.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(indexPlayer.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= indexPlayer.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= indexPlayer.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		indexPlayer.preprocess(game);
		type = SiteType.use(type, game);
	}
}
