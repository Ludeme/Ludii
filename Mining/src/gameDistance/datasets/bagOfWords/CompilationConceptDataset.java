package gameDistance.datasets.bagOfWords;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.Game;
import gameDistance.datasets.Dataset;
import other.concept.Concept;
import other.concept.ConceptComputationType;
import other.concept.ConceptDataType;
import utils.data_structures.support.zhang_shasha.Tree;

/**
 * Dataset containing compilation concepts.
 * - BagOfWords
 * 
 * @author matthew.stephenson
 */
public class CompilationConceptDataset implements Dataset
{
	@Override
	public Map<String, Double> getBagOfWords(final Game game) 
	{
		final Map<String, Double> featureMap = new HashMap<>();
		
		for (int i = 0; i < Concept.values().length; i++)
		{
			final Concept concept = Concept.values()[i];
			
			if(concept.computationType().equals(ConceptComputationType.Compilation)) 
			{
				if (concept.dataType().equals(ConceptDataType.BooleanData))
				{
					if (game.booleanConcepts().get(concept.id()))
						featureMap.put(concept.name(), Double.valueOf(1.0));
					else
						featureMap.put(concept.name(), Double.valueOf(0.0));
				}
				else if (concept.dataType().equals(ConceptDataType.DoubleData) || concept.dataType().equals(ConceptDataType.IntegerData))
				{
					featureMap.put(concept.name(), Double.valueOf(game.nonBooleanConcepts().get(Integer.valueOf(concept.id()))));
				}
				else
				{
					System.out.println("ERROR, the following concept has an invalid type " + concept.toString());
				}
			}
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
