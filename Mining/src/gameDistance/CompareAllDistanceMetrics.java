package gameDistance;

import java.util.Map;

import game.Game;
import gameDistance.datasets.Dataset;
import gameDistance.datasets.bagOfWords.BooleanConceptDataset;
import gameDistance.datasets.bagOfWords.NGramDataset;
import gameDistance.datasets.sequence.MoveConceptDataset;
import gameDistance.datasets.treeEdit.LudemeDataset;
import gameDistance.metrics.DistanceMetric;
import gameDistance.metrics.bagOfWords.JensenShannonDivergence;
import gameDistance.metrics.sequence.GlobalAlignment;
import gameDistance.metrics.sequence.Levenshtein;
import gameDistance.metrics.sequence.LocalAlignment;
import gameDistance.metrics.sequence.RepeatedLocalAlignment;
import gameDistance.metrics.treeEdit.ZhangShasha;
import other.GameLoader;

public class CompareAllDistanceMetrics
{
	
	//---------------------------------------------------------------------

	final static Dataset ludemeDataset = new LudemeDataset();
	final static Dataset booleanConceptDataset = new BooleanConceptDataset();
	final static Dataset moveConceptDataset = new MoveConceptDataset();
	
//	final static Map<String, Double> ludemeVocabulary = DistanceUtils.fullVocabulary(ludemeDataset);
//	final static Map<String, Double> booleanConceptVocabulary = DistanceUtils.fullVocabulary(booleanConceptDataset);
//	final static Map<String, Double> moveConceptVocabulary = DistanceUtils.fullVocabulary(moveConceptDataset);
	
	final static int nGramLength = 4;
	
	//---------------------------------------------------------------------
	
	public static void main(final String[] args)
	{
		final String[] gamesToCompare = 
			{
				"/lud/board/hunt/Adugo.lud",
				"/lud/board/war/replacement/checkmate/chess/Half Chess.lud",
				"/lud/board/war/replacement/checkmate/chess/Chess.lud"
			};
		
		for (int i = 0; i < gamesToCompare.length; i++)
			for (int j = 0; j < gamesToCompare.length; j++)
				if (i < j)
					compareTwoGames(GameLoader.loadGameFromName(gamesToCompare[i]), GameLoader.loadGameFromName(gamesToCompare[j]));	
	}
	
	//---------------------------------------------------------------------
	
	public static void compareTwoGames(Game gameA, Game gameB)
	{
		ludemeDataset.getTree(gameA);
		System.out.println("\n" + gameA.name() + " v.s. " + gameB.name());
		
		//---------------------------------------------------------------------
		// Store the default vocabularies for each dataset.
		
		final Map<String, Double> defaultLudemeVocabulary = DistanceUtils.defaultVocabulary(ludemeDataset, gameA, gameB);
		final Map<String, Double> defaultLudemeNGramVocabulary = DistanceUtils.defaultVocabulary(new NGramDataset(ludemeDataset, nGramLength), gameA, gameB);
		
		final Map<String, Double> defaultBooleanConceptVocabulary = DistanceUtils.defaultVocabulary(booleanConceptDataset, gameA, gameB);
		
		final Map<String, Double> defaultMoveConceptVocabulary = DistanceUtils.defaultVocabulary(moveConceptDataset, gameA, gameB);
		final Map<String, Double> defaultMoveConceptNGramVocabulary = DistanceUtils.defaultVocabulary(new NGramDataset(moveConceptDataset, nGramLength), gameA, gameB);
		
		//---------------------------------------------------------------------
		// JensenShannonDivergence
		
		final DistanceMetric jensenShannonDivergenceMetric = new JensenShannonDivergence();
		System.out.println("JensenShannonDivergence");
		
		System.out.println(jensenShannonDivergenceMetric.distance(ludemeDataset, defaultLudemeVocabulary, gameA, gameB));
		System.out.println(jensenShannonDivergenceMetric.distance(booleanConceptDataset, defaultBooleanConceptVocabulary, gameA, gameB));
		System.out.println(jensenShannonDivergenceMetric.distance(moveConceptDataset, defaultMoveConceptVocabulary, gameA, gameB));
		
		System.out.println(jensenShannonDivergenceMetric.distance(new NGramDataset(ludemeDataset, nGramLength), defaultLudemeNGramVocabulary, gameA, gameB));
		System.out.println(jensenShannonDivergenceMetric.distance(new NGramDataset(moveConceptDataset, nGramLength), defaultMoveConceptNGramVocabulary, gameA, gameB));
		
//		System.out.println(jensenShannonDivergenceMetric.distance(ludemeDataset, ludemeVocabulary, gameA, gameB));
//		System.out.println(jensenShannonDivergenceMetric.distance(booleanConceptDataset, booleanConceptVocabulary, gameA, gameB));
//		System.out.println(jensenShannonDivergenceMetric.distance(moveConceptDataset, moveConceptVocabulary, gameA, gameB));
//		
//		//---------------------------------------------------------------------
//		// Cosine
//		
//		final DistanceMetric cosineMetric = new Cosine();
//		System.out.println("Cosine");
//		
//		System.out.println(cosineMetric.distance(ludemeDataset, defaultLudemeVocabulary, gameA, gameB));
//		System.out.println(cosineMetric.distance(booleanConceptDataset, defaultBooleanConceptVocabulary, gameA, gameB));
//		System.out.println(cosineMetric.distance(moveConceptDataset, defaultMoveConceptVocabulary, gameA, gameB));
//		
//		System.out.println(cosineMetric.distance(new NGramDataset(ludemeDataset, nGramLength), defaultLudemeNGramVocabulary, gameA, gameB));
//		System.out.println(cosineMetric.distance(new NGramDataset(moveConceptDataset, nGramLength), defaultMoveConceptNGramVocabulary, gameA, gameB));
//		
//		System.out.println(cosineMetric.distance(ludemeDataset, ludemeVocabulary, gameA, gameB));
//		System.out.println(cosineMetric.distance(booleanConceptDataset, booleanConceptVocabulary, gameA, gameB));
//		System.out.println(cosineMetric.distance(moveConceptDataset, moveConceptVocabulary, gameA, gameB));
//		
//		//---------------------------------------------------------------------
//		// Jaccard
//		
//		final DistanceMetric jaccardMetric = new Jaccard();
//		System.out.println("Jaccard");
//		
//		System.out.println(jaccardMetric.distance(ludemeDataset, defaultLudemeVocabulary, gameA, gameB));
//		System.out.println(jaccardMetric.distance(booleanConceptDataset, defaultBooleanConceptVocabulary, gameA, gameB));
//		System.out.println(jaccardMetric.distance(moveConceptDataset, defaultMoveConceptVocabulary, gameA, gameB));
//		
//		System.out.println(jaccardMetric.distance(new NGramDataset(ludemeDataset, nGramLength), defaultLudemeNGramVocabulary, gameA, gameB));
//		System.out.println(jaccardMetric.distance(new NGramDataset(moveConceptDataset, nGramLength), defaultMoveConceptNGramVocabulary, gameA, gameB));
//		
//		System.out.println(jaccardMetric.distance(ludemeDataset, ludemeVocabulary, gameA, gameB));
//		System.out.println(jaccardMetric.distance(booleanConceptDataset, booleanConceptVocabulary, gameA, gameB));
//		System.out.println(jaccardMetric.distance(moveConceptDataset, moveConceptVocabulary, gameA, gameB));
		
		//---------------------------------------------------------------------
		// Levenshtein
		
		final DistanceMetric levenshteinMetric = new Levenshtein();
		System.out.println("Levenshtein");
		
		System.out.println(levenshteinMetric.distance(ludemeDataset, null, gameA, gameB));
		System.out.println(levenshteinMetric.distance(moveConceptDataset, null, gameA, gameB));
		
		//---------------------------------------------------------------------
		// Local Alignment
		
		final DistanceMetric localAlignmentMetric = new LocalAlignment();
		System.out.println("LocalAlignment");
		
		System.out.println(localAlignmentMetric.distance(ludemeDataset, null, gameA, gameB));
		System.out.println(localAlignmentMetric.distance(moveConceptDataset, null, gameA, gameB));
		
		//---------------------------------------------------------------------
		// Repeated Local Alignment
		
		final DistanceMetric repeatedLocalAlignmentMetric = new RepeatedLocalAlignment();
		System.out.println("RepeatedLocalAlignment");
		
		System.out.println(repeatedLocalAlignmentMetric.distance(ludemeDataset, null, gameA, gameB));
		System.out.println(repeatedLocalAlignmentMetric.distance(moveConceptDataset, null, gameA, gameB));
		
		//---------------------------------------------------------------------
		// Global Alignment
		
		final DistanceMetric globalAlignmentMetric = new GlobalAlignment();
		System.out.println("GlobalAlignment");
		
		System.out.println(globalAlignmentMetric.distance(ludemeDataset, null, gameA, gameB));
		System.out.println(globalAlignmentMetric.distance(moveConceptDataset, null, gameA, gameB));
		
		//---------------------------------------------------------------------
		// Zhang Shasha
		
		final DistanceMetric zhangShashaMetric = new ZhangShasha();
		System.out.println("ZhangShasha");
		
		System.out.println(zhangShashaMetric.distance(ludemeDataset, defaultLudemeVocabulary, gameA, gameB));
	}
	
	//---------------------------------------------------------------------
	
}
