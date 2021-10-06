package metrics.suffix_tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * collection of letters which a suffixTree is made of
 * A letter, which could also be seen as a token, is a representation of an atomic building block.
 * @author Markus
 *
 */
public class Alphabet implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final HashMap<Letter,String> letterToString;
	private final HashMap<String,Letter> stringToLetter;
	private final TreeMap<Integer,Seperator> seperators = new TreeMap<>();

	public static boolean equals(final Alphabet gameA, final Letter a, final Alphabet gameB, final Letter b) {
		return a.getString().equals(b.getString());
	}
	
	public Alphabet(final HashSet<String> wordList)
	{	
		this.letterToString = new HashMap<>(wordList.size()+1);
		this.stringToLetter = new HashMap<>(wordList.size()+1);
		//letterToString.put(Letter.emptySuffix, "");
		
		int counter = 0;
		for (final String word : wordList)
		{
			final Letter a = new Letter(counter,word);
			letterToString.put(a,word);
			stringToLetter.put(word, a);
			counter++;
		}
	}
	
	public <T> void overwritteSuffixes(final ArrayList<T> container) {
		
		seperators.clear();
		for (int i = 0; i < container.size(); i++)
		{
			final T t = container.get(i);
			final ContainerSeperator<T> sep = new ContainerSeperator<T>(i,t, "");
			seperators.put(Integer.valueOf(i), sep);
			letterToString.put(sep, "");
		}
	}
	
	
	public ArrayList<Letter> encode(final String[] sequence){
		final ArrayList<Letter> converted = new ArrayList<>(sequence.length);
		for (int i = 0; i < sequence.length; i++)
		{
			final String s = sequence[i];
			final Letter l = stringToLetter.get(s);
			if (l==null) {
				System.out.println("what");
			}
			converted.add(l);
		}
		return converted;
	}
	public ArrayList<Letter> encode(final ArrayList<String> sequence){
		final ArrayList<Letter> converted = new ArrayList<>(sequence.size());

		for (int i = 0; i < sequence.size(); i++)
		{
			converted.add(stringToLetter.get(sequence.get(i)));
		}
		return converted;
	}
	
	public ArrayList<String> decode(final ArrayList<Letter> letters){
		final ArrayList<String> converted = new ArrayList<>(letters.size());

		for (final Letter letter : letters)
		{
			converted.add(letter.getString());
		}
		return converted;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (final Letter letter : letterToString.keySet())
		{
			sb.append(letter.afterDot() + " ");
		}
		return sb.toString();
	}

	public HashMap<String,Letter> getStringToLetter()
	{
		return stringToLetter;
	}

	public ArrayList<ArrayList<Letter>> encodeAllTrials(
			final ArrayList<String[]> actionInTrials
	)
	{
		final ArrayList<ArrayList<Letter>> liste = new ArrayList<>(actionInTrials.size());
		for (final String[] strings : actionInTrials)
		{
			liste.add(encode(strings));
		}
		return liste;
	}

	public Seperator getSeperator(final int n)
	{
		Seperator sep = seperators.get(Integer.valueOf(n));
		if (sep==null) {
			sep = new Seperator(n, "");
			seperators.put(Integer.valueOf(n), sep);
			letterToString.put(sep, "");
		}
		return sep;
	}

	/**
	 * 
	 * @return list of the at
	 */
	public Set<String> getWordList()
	{
		return stringToLetter.keySet();
	}

	/**
	 * 
	 * @return a map of the seperators used between the different insertions
	 */
	public TreeMap<Integer,Seperator> getSeperator()
	{
		return seperators;
	}

	

}
