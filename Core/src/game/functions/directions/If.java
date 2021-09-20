package game.functions.directions;

import java.util.BitSet;
import java.util.List;

import game.Game;
import game.equipment.component.Component;
import game.functions.booleans.BooleanConstant.FalseConstant;
import game.functions.booleans.BooleanFunction;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.Direction;
import game.util.directions.DirectionFacing;
import other.context.Context;
import other.topology.TopologyElement;

/**
 * Returns whether the specified directions are satisfied for the current game.
 * 
 * @author Eric.Piette
 */
public class If extends DirectionsFunction
{
	/** Direction function if the condition is verified. */
	private final DirectionsFunction directionFunctionOk;

	/** Direction function if the condition is not verified. */
	private final DirectionsFunction directionFunctionNotOk;

	/** The condition. */
	private final BooleanFunction condition;

	//-------------------------------------------------------------------------

	/**
	 * @param condition       The condition to verify.
	 * @param directionsOk    The directions if the condition is verified.
	 * @param directionsNotOk The directions if the condition is not verified.
	 * 
	 * @example (if (is Mover P1) Orthogonal Diagonal)
	 */
	public If
	(
		final BooleanFunction condition,
		final Direction       directionsOk,
		final Direction       directionsNotOk
	)
	{
		directionFunctionOk = directionsOk.directionsFunctions();
		directionFunctionNotOk = directionsNotOk.directionsFunctions();
		this.condition = condition;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0;

		gameFlags |= condition.gameFlags(game);
		gameFlags |= directionFunctionOk.gameFlags(game);
		gameFlags |= directionFunctionNotOk.gameFlags(game);

		return gameFlags;
	}

	@Override
	public boolean isStatic()
	{
		return condition.isStatic() && directionFunctionOk.isStatic() && directionFunctionNotOk.isStatic();
	}

	@Override
	public void preprocess(final Game game)
	{
		condition.preprocess(game);
		directionFunctionOk.preprocess(game);
		directionFunctionNotOk.preprocess(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(directionFunctionOk.concepts(game));
		concepts.or(directionFunctionNotOk.concepts(game));
		concepts.or(condition.concepts(game));
		return concepts;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		if (condition instanceof FalseConstant)
		{
			game.addRequirementToReport("One of the condition of a (if ...) ludeme is \"false\" which is wrong.");
			missingRequirement = true;
		}

		missingRequirement |= directionFunctionOk.missingRequirement(game);
		missingRequirement |= directionFunctionNotOk.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= directionFunctionOk.willCrash(game);
		willCrash |= directionFunctionNotOk.willCrash(game);
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
	public List<AbsoluteDirection> convertToAbsolute
	(
			final SiteType graphType, 
			final TopologyElement element,
			final Component newComponent, 
			final DirectionFacing newFacing,
			final Integer newRotation,
			final Context context
	)
	{
		if (condition.eval(context))
			return directionFunctionOk.convertToAbsolute(graphType, element, newComponent, newFacing, newRotation,
					context);

		return directionFunctionNotOk.convertToAbsolute(graphType, element, newComponent, newFacing, newRotation,
				context);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "if " + condition.toEnglish(game) + " then " + directionFunctionOk.toEnglish(game) + " else " + directionFunctionNotOk.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
}
