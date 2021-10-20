package gameDistance.datasets.bagOfWords;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.Game;
import gameDistance.datasets.Dataset;
import other.concept.Concept;
import utils.data_structures.support.zhang_shasha.Tree;

public class BooleanConceptDataset implements Dataset
{
	@Override
	public Map<String, Double> getBagOfWords(final Game game) 
	{
		final Map<String, Double> featureMap = new HashMap<>();

		for (int i = 0; i < Concept.values().length; i++)
		{
			final Concept concept = Concept.values()[i];
			if (game.booleanConcepts().get(concept.id()))
				featureMap.put(concept.name(), Double.valueOf(1.0));
		}
		
		return featureMap;
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

}
