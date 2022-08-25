package game.functions.directions;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import game.Game;
import game.equipment.component.Component;
import game.types.board.RelationType;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.Direction;
import game.util.directions.DirectionFacing;
import other.context.Context;
import other.topology.TopologyElement;

/**
 * Returns the difference of two set of directions.
 * 
 * @author Eric.Piette
 */
public class Difference extends DirectionsFunction
{
	/** The original set of directions. */
	final DirectionsFunction originalDirection;

	/** The directions to remove from the original set of directions. */
	final DirectionsFunction removedDirection;

	//-------------------------------------------------------------------------

	/**
	 * @param directions         The original directions.
	 * @param directionsToRemove The directions to remove.
	 * 
	 * @example (difference Orthogonal N)
	 */
	public Difference
	(
		final Direction directions,
		final Direction directionsToRemove
	)
	{
		originalDirection = directions.directionsFunctions();
		removedDirection = directionsToRemove.directionsFunctions();
	}

	//-------------------------------------------------------------------------

	/**
	 * Trick ludeme into joining the grammar.
	 * @param context Current game context.
	 * @return Nuthin'! Absolutely nuthin'.
	 */
	@SuppressWarnings("static-method")
	public Direction eval(final Context context)
	{
		return null;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0;

		gameFlags |= originalDirection.gameFlags(game);
		gameFlags |= removedDirection.gameFlags(game);

		return gameFlags;
	}

	@Override
	public boolean isStatic()
	{
		return originalDirection.isStatic() && removedDirection.isStatic();
	}

	@Override
	public void preprocess(final Game game)
	{
		originalDirection.preprocess(game);
		removedDirection.preprocess(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(originalDirection.concepts(game));
		concepts.or(removedDirection.concepts(game));
		return concepts;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= originalDirection.missingRequirement(game);
		missingRequirement |= removedDirection.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= originalDirection.willCrash(game);
		willCrash |= removedDirection.willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "";
	}

	//-------------------------------------------------------------------------

	@Override
	public List<AbsoluteDirection> convertToAbsolute(final SiteType graphType, final TopologyElement element,
			final Component newComponent, final DirectionFacing newFacing, final Integer newRotation,
			final Context context)
	{
		final List<AbsoluteDirection> directionsReturned = new ArrayList<AbsoluteDirection>();
		final List<AbsoluteDirection> originalDir = originalDirection.convertToAbsolute(graphType, element,
				newComponent, newFacing, newRotation,
					context);
		final List<AbsoluteDirection> originalAfterConv = new ArrayList<AbsoluteDirection>();
		final List<AbsoluteDirection> directionsToRemove = removedDirection.convertToAbsolute(graphType, element,
				newComponent, newFacing, newRotation,
				context);
		final List<AbsoluteDirection> removedAfterConv = new ArrayList<AbsoluteDirection>();

		// Convert in AbsoluteDirection with equivalent in FacingDirection
		for (final AbsoluteDirection dir : originalDir)
			{
			final RelationType relation = AbsoluteDirection.converToRelationType(dir);
				if(relation != null)
				{
					final List<DirectionFacing> directionsFacing = element.supportedDirections(relation);
					for(final DirectionFacing dirFacing : directionsFacing)
						if(!originalAfterConv.contains(dirFacing.toAbsolute()))
							originalAfterConv.add(dirFacing.toAbsolute());
				}
				else
					originalAfterConv.add(dir);
			}

		for (final AbsoluteDirection dir : directionsToRemove)
		{
			final RelationType relation = AbsoluteDirection.converToRelationType(dir);
			if(relation != null)
			{
				final List<DirectionFacing> directionsFacing = element.supportedDirections(relation);
				for(final DirectionFacing dirFacing : directionsFacing)
					if(!removedAfterConv.contains(dirFacing.toAbsolute()))
						removedAfterConv.add(dirFacing.toAbsolute());
			}
			else
				removedAfterConv.add(dir);
		}
		
		for (final AbsoluteDirection dir : originalAfterConv)
			if (!removedAfterConv.contains(dir))
				directionsReturned.add(dir);

		return directionsReturned;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "the difference between directions " + originalDirection.toEnglish(game) + " and " + removedDirection.directionsFunctions();
	}
	
	//-------------------------------------------------------------------------
}
