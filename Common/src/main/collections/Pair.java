package main.collections;

/**
 * String pair (key, value) for metadata.
 * @author Michel--Delétie Cyprien
 */
public class Pair<A,B>
{
	private final A key;
	private final B value;
	
	/**
	 * Constructor
	 * @param key
	 * @param value
	 */
	public Pair(final A key, final B value)
	{
		this.key   = key;
		this.value = value;
	}

	/**
	 * @return Pair's first element (the key)
	 */
	public A key()
	{
		return key;
	}
	
	/**
	 * @return Pair's second element (the value)
	 */
	public B value()
	{
		return value;
	}
}
