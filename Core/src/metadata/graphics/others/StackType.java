package metadata.graphics.others;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.types.board.SiteType;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;
import metadata.graphics.util.PieceStackType;
import other.concept.Concept;

//-----------------------------------------------------------------------------

/**
 * Sets the stack design for a container.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Different stack types that can be specified are defined in PieceStackType.
 *          For games such as Snakes and Ladders, Backgammon, Tower of Hanoi, card games, etc.
 */
public class StackType implements GraphicsItem
{
	/** RoleType condition. */
	private final RoleType roleType;
	
	/** Container name condition. */
	private final String name;
	
	/** Container index condition. */
	private final Integer index;
	
	/** GraphElementType for the specified location(s). */
	private final SiteType graphElementType;
	
	/** Set of locations to apply stack design onto. */
	private final Integer[] sites;
	
	/** Stack type to apply. */
	private final PieceStackType stackType;
	
	/** Scale to apply. */
	private final float scale;
	
	/** state condition. */
	private final Integer state;
	
	/** limit for stack, applies only to some stack types (e.g. backgammon). */
	private final int limit;
		
	//-------------------------------------------------------------------------

	/**
	 * @param roleType  		Player whose index we want to match.
	 * @param name      		Container name to match.
	 * @param index     		Container index to match.
	 * @param sites 			Draw image on all specified sites.
	 * @param site 				Draw image on this site.
	 * @param graphElementType  The GraphElementType for the specified sites [Cell].
	 * @param state				Local state to match.
	 * @param stackType 		Stack type for this piece.
	 * @param scale				Scaling factor [1.0].
	 * @param limit				Stack limit [5].
	 * 
	 * @example (stackType Ground)
	 */
	public StackType
	(
		@Opt 			final RoleType roleType,
		@Opt 			final String name,
		@Opt 			final Integer index,
		@Opt        	final SiteType graphElementType,
		@Opt @Or @Name  final Integer[] sites,
		@Opt @Or @Name  final Integer site,
		@Opt 	 @Name  final Integer state,
						final PieceStackType stackType,
		@Opt	 		final Float scale,
		@Opt     @Name  final Integer limit
	)
	{
		this.roleType = roleType;
		this.name = name;
		this.index = index;
		this.graphElementType = graphElementType;
		this.sites = ((sites != null) ? sites : ((site != null) ? (new Integer[]{ site }) : null));
		this.state = state;
		this.stackType = stackType;
		this.scale = (scale == null) ? (float)1.0 : scale.floatValue();
		this.limit = (limit == null) ? 5 : limit.intValue();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return RoleType condition to check.
	 */
	public RoleType roleType()
	{
		return roleType;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Container name condition to check.
	 */
	public String name()
	{
		return name;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return GraphElementType for the specified location(s).
	 */
	public SiteType graphElementType()
	{
		return graphElementType;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Container index condition to check.
	 */
	public Integer index()
	{
		return index;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Sites to apply stack design onto.
	 */
	public Integer[] sites()
	{
		return sites;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Stack type to apply.
	 */
	public PieceStackType stackType()
	{
		return stackType;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Scale for the stack.
	 */
	public float scale()
	{
		return scale;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return State condition to check.
	 */
	public Integer state()
	{
		return state;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Stack limit
	 */
	public int limit()
	{
		return limit;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.StackType.id(), true);
		if (stackType.equals(PieceStackType.Backgammon) || stackType.equals(PieceStackType.Default)
				|| stackType.equals(PieceStackType.None) || stackType.equals(PieceStackType.Reverse))
			concepts.set(Concept.Stack.id(), true);
		return concepts;
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long gameFlags = 0l;
		return gameFlags;
	}

	@Override
	public boolean needRedraw()
	{
		return false;
	}

}
