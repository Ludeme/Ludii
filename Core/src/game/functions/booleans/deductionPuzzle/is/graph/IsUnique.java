package game.functions.booleans.deductionPuzzle.is.graph;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.equipment.other.Regions;
import game.functions.booleans.BaseBooleanFunction;
import game.types.board.RegionTypeStatic;
import game.types.board.SiteType;
import game.types.state.GameType;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;

/**
 * Returns true if each sub region of a static region is different.
 * 
 * @author Eric.Piette
 * 
 * @remarks This works only for deduction puzzles.
 */
@Hide
public class IsUnique extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Graph element type. */
	private final SiteType type;

	/**
	 * @param elementType The graph element type [Cell].
	 */
	public IsUnique
	(
		@Opt final SiteType elementType
	)
	{
		areaConstraint = RegionTypeStatic.Regions;
		type = (elementType == null) ? SiteType.Cell : elementType;
	}

	//--------------------------------------------------------------------------

	@Override
	public boolean eval(Context context)
	{
		final ContainerState ps = context.state().containerStates()[0];
		
		final Regions[] regions = context.game().equipment().regions();
		for (final Regions region : regions)
		{
			if (region.regionTypes() != null)
			{
				final RegionTypeStatic[] regionTypes = region.regionTypes();
				for (final RegionTypeStatic regionType : regionTypes)
				{
					final Integer[][] regionsList = region.convertStaticRegionOnLocs(regionType, context);
					
					for (int i = 0; i < regionsList.length; i++)
						for (int j = i + 1; j < regionsList.length; j++)
						{
							final Integer[] set1 = regionsList[i];
							final Integer[] set2 = regionsList[j];
							if (regionAllAssigned(set1, ps) && regionAllAssigned(set2, ps))
							{
								boolean identical = true;
								for (int index = 0; index < set1.length; index++)
									if (ps.what(set1[index].intValue(), type) != ps.what(set2[index].intValue(), type))
									{
										identical = false;
										break;
									}
										
								if (identical)
									return false;
							}
						}
				}
			}
		}
					
		return true;
	}
	
	/**
	 * @param region
	 * @param ps
	 * @return True if all the vars corresponding to the locs are assigned
	 */
	public boolean regionAllAssigned(final Integer[] region, final ContainerState ps)
	{
		for (final Integer loc : region)
			if (!ps.isResolved(loc.intValue(), type))
				return false;
		return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public void preprocess(final Game game)
	{
		// Do nothing.
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long gameFlags = GameType.DeductionPuzzle;
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.DeductionPuzzle.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (game.players().count() != 1)
		{
			game.addCrashToReport("The ludeme (is Unique ...) is used but the number of players is not 1.");
			willCrash = true;
		}
		return willCrash;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String str = "";
		str += "Unique()";
		return str;
	}
}
