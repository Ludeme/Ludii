
package gameDistance.metrics.sequence;

import java.util.List;
import java.util.Map;

import game.Game;
import gameDistance.datasets.Dataset;
import gameDistance.metrics.DistanceMetric;

//-----------------------------------------------------------------------------

/**
 * @author Matthew.Stephenson, Markus
 * https://en.wikipedia.org/wiki/Cosine_similarity
 */
public class Levenshtein implements DistanceMetric
{	
	
	@Override
	public double distance(final Dataset dataset, final Map<String, Double> vocabulary, final Game gameA, final Game gameB)
	{
		final List<String> gameAString = dataset.getSequence(gameA);
		final List<String> gameBString = dataset.getSequence(gameB);
		
        final int [] costs = new int [gameBString.size() + 1];
        
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        
        for (int i = 1; i <= gameAString.size(); i++) 
        {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= gameBString.size(); j++) 
            {
                final int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), gameAString.get(i - 1).equals(gameBString.get(j - 1)) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        
        final int edits = costs[gameBString.size()];
		final int maxLength = Math.max(gameAString.size(), gameBString.size());
		final double score = (double) edits / maxLength;
		return score;
	}

}
