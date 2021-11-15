package gameDistance.datasets.bagOfWords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.Game;
import gameDistance.datasets.Dataset;
import utils.data_structures.support.zhang_shasha.Tree;

/**
 * Converts a Sequence-possible dataset into a BagOfWords equivalent based on NGram sets.
 * - BagOfWords
 * 
 * @author matthew.stephenson
 */
public class NGramDataset implements Dataset
{
	
	//---------------------------------------------------------------------
	
	Dataset originalDataset;
	int nGramLength;
	
	//---------------------------------------------------------------------
	
	/**
	 * Make sure to call this constructor with a dataset that provides a sequence output format.
	 * @param originalDataset
	 * @param nGramLength
	 */
	public NGramDataset(final Dataset originalDataset, final int nGramLength)
	{
		this.originalDataset = originalDataset;
		this.nGramLength = nGramLength;
	}
	
	//---------------------------------------------------------------------
	
	@Override
	public Map<String, Double> getBagOfWords(final Game game) 
	{
		return convertSequenceToNGram(originalDataset.getSequence(game), nGramLength);
	}

	/**
	 * Not Supported
	 */
	@Override
	public List<String> getSequence(final Game game) 
	{
		return null;
	}

	/**
	 * Not Supported
	 */
	@Override
	public Tree getTree(final Game game) 
	{
		return null;
	}
	
	//---------------------------------------------------------------------
	
	/**
	 * Converts from a sequence dataset output to an NGrams bagOfWords output.
	 */
	public static Map<String, Double> convertSequenceToNGram(final List<String> sequence, final int n)
	{
		final Map<String, Double> featureMap = new HashMap<>();
		
		final List<List<String>> allNGrams = new ArrayList<>();
		for (int i = 0; i < sequence.size()-n; i++)
			allNGrams.add(sequence.subList(i, i+n));
		
		final List<String> allNGramStrings = new ArrayList<>();
		for (int i = 0; i < allNGrams.size(); i++)
			allNGramStrings.add(String.join("_", allNGrams.get(i)));
		
		for (final String nGramString : allNGramStrings)
		{
			if (featureMap.containsKey(nGramString))
				featureMap.put(nGramString, Double.valueOf(featureMap.get(nGramString).doubleValue()+1.0));
			else
				featureMap.put(nGramString, Double.valueOf(1.0));
		}
		
		return featureMap;
	}
	
	//---------------------------------------------------------------------

}
