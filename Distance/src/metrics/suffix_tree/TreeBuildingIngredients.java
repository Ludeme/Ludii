package metrics.suffix_tree;

import java.util.ArrayList;

public class TreeBuildingIngredients 
{

	private final Alphabet a;
	private final ArrayList<ArrayList<Letter>> convertedTrials;
	public ArrayList<ArrayList<Letter>> getConvertedTrials(){
		return convertedTrials;
	}

	public TreeBuildingIngredients(
			final Alphabet a, final ArrayList<ArrayList<Letter>> convertedTrials
	)
	{
		this.a =a;
		this.convertedTrials = convertedTrials;
	}

	public Alphabet getAlphabet()
	{
		return a;
	}
	public int getNumLetters() {
		int sum = 0;
		for (final ArrayList<Letter> arrayList : convertedTrials)
		{
			sum += arrayList.size();
		}
		return sum;
	}

}
