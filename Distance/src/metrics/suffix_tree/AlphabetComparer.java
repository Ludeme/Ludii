package metrics.suffix_tree;

import java.util.Map.Entry;
import java.util.Set;

import metrics.suffix_tree.SuffixTreeCollapsed.N;

public class AlphabetComparer
{

	private final boolean[][] equalMatrix;

	public AlphabetComparer(final Alphabet a1, final Alphabet a2)
	{
		final Set<Entry<String,Letter>> astl = a1.getStringToLetter().entrySet();
		final Set<Entry<String,Letter>> ast2 = a2.getStringToLetter().entrySet();
		equalMatrix = new boolean[astl.size()][ast2.size()];
		for (final Entry<String,Letter> entry1 : astl)
		{
			final String string1 = entry1.getKey();
			final int index1 = entry1.getValue().getIndex();
			for (final Entry<String,Letter> entry2 : ast2)
			{
				final String string2 = entry2.getKey();
				final int index2 = entry2.getValue().getIndex();
				
				if(string1.equals(string2)) {
					equalMatrix[index1][index2] = true;
				}
			}
		}
	}
	
	/**
	 * 
	 * @param l1 letter from first alphabet
	 * @param l2 letter from second alphabet
	 * @return
	 */
	public boolean isEqual(final Letter l1, final Letter l2)
	{
		return equalMatrix[l1.getIndex()][l2.getIndex()];
	}

	public boolean containsEqualLetter(final N childA, final N childB)
	{
		return isEqual(childA.getNthLetter(0),childB.getNthLetter(0));
	}
	
}
