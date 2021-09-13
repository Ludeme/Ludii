package game.rules.end;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import other.context.Context;

/**
 * Dual role object; links end rules in the grammar and contains result afterwards. 
 * 
 * @author cambolbro
 */
@Hide
public class BaseEndRule extends EndRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * The base end rule.
	 * 
	 * @param result The result.
	 */
	public BaseEndRule
	(
		@Opt final Result result
	)
	{
		super(result);
	}

	//-------------------------------------------------------------------------	

	/**
	 * @return Must return EndRule object! 
	 *         If return Result, grammar can't chain it with an end rule. 
	 */
	@Override
	public EndRule eval(final Context context)
	{
		return null;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return 0;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Nothing to do.
	}

	@Override
	public BitSet stateConcepts(final Context context)
	{
		final BitSet concepts = new BitSet();

		return concepts;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "";
	}
}
