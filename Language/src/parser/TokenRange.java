package parser; 

/**
 * Specifies a token's range within a String.
 * @author cambolbro
 */
public class TokenRange
{
	final int from;
	final int to;
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param from Range from (inclusive).
	 * @param to   Range to (exclusive).
	 */
	public TokenRange(final int from, final int to)
	{
		this.from = from;
		this.to   = to;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Range from (inclusive).
	 */
	public int from()
	{
		return from;
	}
	
	/**
	 * @return Range to (exclusive).
	 */
	public int to()
	{
		return to;
	}
	
	//-------------------------------------------------------------------------

}
