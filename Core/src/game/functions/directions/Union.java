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
 * Returns the union of two set of directions.
 * 
 * @author Eric.Piette
 */
public class Union extends DirectionsFunction
{
	/** The first set of directions. */
	final DirectionsFunction directionSet1;

	/** The second set of directions. */
	final DirectionsFunction directionSet2;

	//-------------------------------------------------------------------------

	/**
	 * @param directions         The first set of directions.
	 * @param directionsToRemove The second set of directions.
	 * 
	 * @example (union Orthogonal Diagonal)
	 */
	public Union
	(
		final Direction directions,
		final Direction directionsToRemove
	)
	{
		directionSet1 = directions.directionsFunctions();
		directionSet2 = directionsToRemove.directionsFunctions();
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

		gameFlags |= directionSet1.gameFlags(game);
		gameFlags |= directionSet2.gameFlags(game);

		return gameFlags;
	}

	@Override
	public boolean isStatic()
	{
		return directionSet1.isStatic() && directionSet2.isStatic();
	}

	@Override
	public void preprocess(final Game game)
	{
		directionSet1.preprocess(game);
		directionSet2.preprocess(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(directionSet1.concepts(game));
		concepts.or(directionSet2.concepts(game));
		return concepts;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= directionSet1.missingRequirement(game);
		missingRequirement |= directionSet2.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= directionSet1.willCrash(game);
		willCrash |= directionSet2.willCrash(game);
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
		final List<AbsoluteDirection> firstDirecitons = directionSet1.convertToAbsolute(graphType, element,
				newComponent, newFacing, newRotation,
					context);
		final List<AbsoluteDirection> firstDirectionAfterConv = new ArrayList<AbsoluteDirection>();
		final List<AbsoluteDirection> secondDirections = directionSet2.convertToAbsolute(graphType, element,
				newComponent, newFacing, newRotation,
				context);
		final List<AbsoluteDirection> secondDirectionsAfterConv = new ArrayList<AbsoluteDirection>();

		// Convert in AbsoluteDirection with equivalent in FacingDirection
		for (final AbsoluteDirection dir : firstDirecitons)
			{
			final RelationType relation = AbsoluteDirection.converToRelationType(dir);
				if(relation != null)
				{
					final List<DirectionFacing> directionsFacing = element.supportedDirections(relation);
					for(final DirectionFacing dirFacing : directionsFacing)
						if(!firstDirectionAfterConv.contains(dirFacing.toAbsolute()))
							firstDirectionAfterConv.add(dirFacing.toAbsolute());
				}
				else
					firstDirectionAfterConv.add(dir);
			}

		for (final AbsoluteDirection dir : secondDirections)
		{
			final RelationType relation = AbsoluteDirection.converToRelationType(dir);
			if(relation != null)
			{
				final List<DirectionFacing> directionsFacing = element.supportedDirections(relation);
				for(final DirectionFacing dirFacing : directionsFacing)
					if(!secondDirectionsAfterConv.contains(dirFacing.toAbsolute()))
						secondDirectionsAfterConv.add(dirFacing.toAbsolute());
			}
			else
				secondDirectionsAfterConv.add(dir);
		}
		
		directionsReturned.addAll(firstDirectionAfterConv);
		
		for (final AbsoluteDirection dir : secondDirectionsAfterConv)
			if (!directionsReturned.contains(dir))
				directionsReturned.add(dir);

		return directionsReturned;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "the union of " + directionSet1.toEnglish(game) + " and " + directionSet2.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
}
