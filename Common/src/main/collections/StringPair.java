package main.collections;

import java.util.regex.Pattern;

import main.StringRoutines;

/**
 * String pair (key, value) for metadata.
 * @author cambolbro, Dennis Soemers
 */
public class StringPair
{
	private final String key;
	private final String value;
	
	/**
	 * Constructor
	 * @param key
	 * @param value
	 */
	public StringPair(final String key, final String value)
	{
		this.key   = key;
		this.value = value;
	}

	/**
	 * @return Pair's first element (the key)
	 */
	public String key()
	{
		return key;
	}
	
	/**
	 * @return Pair's second element (the value)
	 */
	public String value()
	{
		return value;
	}
	
	@Override
	public String toString()
	{
		return "{ " + StringRoutines.quote(key) + " " + StringRoutines.quote(value) + " }";
	}
	
	/**
	 * @param str
	 * @return StringPair generated from given string (in { "key" "value" } format)
	 */
	public static StringPair fromString(final String str)
	{
		String s = str.replaceAll(Pattern.quote("{"), "");
		s = s.replaceAll(Pattern.quote("}"), "");
		s = s.replaceAll(Pattern.quote("\""), "");
		s = s.trim();
		
		final String[] split = s.split(Pattern.quote(" "));
		return new StringPair(split[0], split[1]);
	}
}
