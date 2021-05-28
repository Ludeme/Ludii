package game.rules.end;

import java.io.Serializable;
import java.util.BitSet;

import annotations.Opt;
import game.Game;
import other.BaseLudeme;
import other.context.Context;

/**
 * Declares a generic end rule.
 * 
 * @author cambolbro
 */
public abstract class EndRule extends BaseLudeme implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/** The result of applying this end rule. */
	private Result result = null;
	
	//-------------------------------------------------------------------------

	/**
	 * @param result The result of the rule.
	 */
	public EndRule
	(
		@Opt final Result result
	)
	{
		this.result = result;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The result of the end rule.
	 */
	public Result result()
	{
		return result;
	}
	
	/**
	 * Set the result of the end rule.
	 * 
	 * @param rslt
	 */
	public void setResult(final Result rslt)
	{
		result = rslt;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param context
	 * @return Must return EndRule object! If return Result, grammar can't chain it
	 *         with an end rule.
	 */
	public abstract EndRule eval(final Context context);
	
	/**
	 * @param game
	 * @return The gameFlags of the end rule.
	 */
	public abstract long gameFlags(final Game game);
	
	/**
	 * Preprocess the end rule.
	 * 
	 * @param game
	 */
	public abstract void preprocess(final Game game);

	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "<EndRule>";
	}

	/**
	 * @param context The context.
	 * @return The concepts related to a specific ending state
	 */
	public abstract BitSet stateConcepts(final Context context);
}
