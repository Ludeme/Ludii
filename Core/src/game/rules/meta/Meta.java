package game.rules.meta;

import java.io.Serializable;

import annotations.Or;
import other.BaseLudeme;
import other.context.Context;

/**
 * Defines a metarule defined before play that supersedes all other rules.
 * 
 * @author cambolbro
 * 
 */
public class Meta extends BaseLudeme implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** The metarules. */
	private final MetaRule[] rules;

	//-------------------------------------------------------------------------
	
	/**
	 * @param rules A collection of metarules.
	 * @param rule  A single metarule.
	 * 
	 * @example (meta (swap))
	 */
	public Meta
	(
		@Or final MetaRule[] rules,
		@Or final MetaRule   rule
	)
	{
		int numNonNull = 0;
		if (rules != null)
			numNonNull++;
		if (rule != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");
		
		if (rules != null)
			this.rules = rules;
		else
		{
			this.rules = new MetaRule[1];
			this.rules[0] = rule;
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The metarules.
	 */
	public MetaRule[] rules()
	{
		return rules;
	}

	//-------------------------------------------------------------------------

	/**
	 * Eval the meta rules.
	 * 
	 * @param context The context.
	 */
	public void eval(final Context context)
	{
		for (final MetaRule rule : rules)
			rule.eval(context);
	}
}