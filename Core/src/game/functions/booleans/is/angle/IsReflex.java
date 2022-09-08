package game.functions.booleans.is.angle;

import java.awt.geom.Point2D;
import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import other.context.Context;

/**
 * Returns true if a site and two other sites checking conditions form a reflex angle (> 180 degrees).
 * 
 * @author Eric.Piette
 */
@Hide
public final class IsReflex extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Cell/Edge/Vertex. */
	private SiteType type;

	/** The site. */
	private IntFunction atFn;

	/** The condition on the first site. */
	private BooleanFunction cond1;

	/** The condition on the second site. */
	private BooleanFunction cond2;
	
	//-------------------------------------------------------------------------

	/**
	 * @param type           The graph element type [default of the board].
	 * @param at             The site
	 * @param conditionSite  The condition on the first site.
	 * @param conditionSite2 The condition on the second site.
	 */
	public IsReflex
	(
	    @Opt   final SiteType         type,
	    @Name  final IntFunction      at, 
			   final BooleanFunction  conditionSite, 
			   final BooleanFunction  conditionSite2
	)
	{
		this.atFn = at;
		this.cond1 = conditionSite;
		this.cond2 = conditionSite2;
		this.type = type;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		final SiteType realSiteType = type == null ? context.board().defaultSite() : type;
		final int site = atFn.eval(context);
		final int originSite = context.site();
		
		if (site < 0)
			return false;

		// Always 0 for the container because we look for angle only on the main board.
		final int numSites = context.topology().getGraphElements(realSiteType).size();

		if (site >= numSites)
			return false;

		for(int site1 = 0; site1 < numSites; site1++)
			for(int site2 = site1 + 1; site2 < numSites; site2++)
				if(site1 != site && site2 != site)
				{
					context.setSite(site1);
					final boolean condition1 = cond1.eval(context);
					context.setSite(site2);
					final boolean condition2 = cond2.eval(context);
					
					if(condition1 && condition2)
					{
						final Point2D p1 = context.topology().getGraphElements(realSiteType).get(site1).centroid();
						final Point2D p2 = context.topology().getGraphElements(realSiteType).get(site2).centroid();
						double difX = p2.getX() - p1.getX(); double difY = p2.getY() - p1.getY();
						double angle = Math.toDegrees(Math.atan2(difX,-difY));
						if(angle > 180)
						{
							context.setSite(originSite);
							return true;
						}
					}
					
				}
		
		context.setSite(originSite);
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "IsReflex(" + atFn + "," + cond1  + "," + cond2+ ")";
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;
		gameFlags |= SiteType.gameFlags(type);
		gameFlags |= atFn.gameFlags(game);
		gameFlags |= cond1.gameFlags(game);
		gameFlags |= cond2.gameFlags(game);
		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(SiteType.concepts(type));
		concepts.or(atFn.concepts(game));
		concepts.or(cond1.concepts(game));
		concepts.or(cond2.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(atFn.writesEvalContextRecursive());
		writeEvalContext.or(cond1.writesEvalContextRecursive());
		writeEvalContext.or(cond2.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(atFn.readsEvalContextRecursive());
		readEvalContext.or(cond1.readsEvalContextRecursive());
		readEvalContext.or(cond2.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		atFn.preprocess(game);
		cond1.preprocess(game);
		cond2.preprocess(game);
	}
	
	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= atFn.missingRequirement(game);
		missingRequirement |= cond1.missingRequirement(game);
		missingRequirement |= cond2.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= atFn.willCrash(game);
		willCrash |= cond1.willCrash(game);
		willCrash |= cond2.willCrash(game);
		return willCrash;
	}

	@Override
	public String toEnglish(final Game game) 
	{
		return "is Reflex at " + atFn.toEnglish(game) + " with condition 1 = " + cond1.toEnglish(game) + " and condition 2 = " + cond2.toEnglish(game); 
	}
}
