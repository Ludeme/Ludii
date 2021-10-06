package metrics.suffix_tree;

import java.io.Serializable;

public class Letter implements Serializable, Comparable<Letter>
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final Letter emptySuffix = new Letter(-1, "$");
	private final String word;
	private final String afterDot;
	private final int index;

	public Letter(final int index, final String word)
	{
		this.word = word;
		this.index = index;
		final String[] split = word.split("\\.");
		if (split.length>0)afterDot = split[split.length-1];
		else afterDot = word;
		
	}
	
	@Override
	public boolean equals(final Object l) {
		if (!(l instanceof Letter)) return false;
		return ((Letter)l).index==this.index;
		
	}
	
	public boolean equals(
			final Letter other, final boolean ignoreQuotationMarks
	)
	{
		if (ignoreQuotationMarks&&this.word.startsWith("\"")&&other.word.startsWith("\"")){
			return true;
		}
		return equals(other);
	}
	
	@Override
	public String toString() {
		return afterDot;
	}

	public String afterDot()
	{
		return afterDot;
		
	}

	public String getString()
	{
		return word;
	}

	public int getIndex()
	{
		return index;
	}

	@Override
	public int hashCode()
	{
		return word.hashCode();
	}

	public int compare(final Letter l2) {
		if (this==l2)return 0;
		return this.word.compareTo(l2.word);
		
	}

	@Override
	public int compareTo(final Letter l2)
	{
		if (this==l2)return 0;
		return this.word.compareTo(l2.word);
	}

	
}
