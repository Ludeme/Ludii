package gameDistance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.Game;
import gameDistance.datasets.Dataset;
import gameDistance.datasets.bagOfWords.BooleanConceptDataset;
import gameDistance.datasets.bagOfWords.NGramDataset;
import gameDistance.datasets.sequence.MoveConceptDataset;
import gameDistance.datasets.treeEdit.LudemeDataset;
import gameDistance.metrics.DistanceMetric;
import gameDistance.metrics.bagOfWords.Cosine;
import gameDistance.metrics.bagOfWords.Jaccard;
import gameDistance.metrics.bagOfWords.JensenShannonDivergence;
import gameDistance.metrics.sequence.GlobalAlignment;
import gameDistance.metrics.sequence.Levenshtein;
import gameDistance.metrics.sequence.LocalAlignment;
import gameDistance.metrics.sequence.RepeatedLocalAlignment;
import gameDistance.metrics.treeEdit.ZhangShasha;
import main.FileHandling;
import other.GameLoader;

/**
 * Compares all distance metrics for a given set of games.
 * 
 * @author matthew.stephenson
 */
public class CompareAllDistanceMetrics
{
	
	//---------------------------------------------------------------------

	final static String outputPath = "res/gameDistance/";
	
	final static Dataset ludemeDataset = new LudemeDataset();
	final static Dataset booleanConceptDataset = new BooleanConceptDataset();
	final static Dataset moveConceptDataset = new MoveConceptDataset();
	
	static Map<String, Double> fullLudemeVocabulary;
	static Map<String, Double> fullBooleanConceptVocabulary;
	static Map<String, Double> fullMoveConceptVocabulary;
	
	final static int nGramLength = 4;
	
	//---------------------------------------------------------------------
	
	public static void main(final String[] args)
	{
		fullLudemeVocabulary = DistanceUtils.fullVocabulary(ludemeDataset);
		System.out.println("ludemeVocabulary stored");
		
		fullBooleanConceptVocabulary = DistanceUtils.fullVocabulary(booleanConceptDataset);
		System.out.println("booleanConceptVocabulary stored");
		
		fullMoveConceptVocabulary = DistanceUtils.fullVocabulary(moveConceptDataset);
		System.out.println("moveConceptVocabulary stored");
		
//		final String[] gamesToCompare = 
//			{
//				"/lud/board/hunt/Adugo.lud",
//				"/lud/board/war/replacement/checkmate/chess/Half Chess.lud",
//				"/lud/board/war/replacement/checkmate/chess/Chess.lud"
//			};
		
		final String[] allGames = FileHandling.listGames();
		final List<String> allGamesToCompare = new ArrayList<>();
		for (final String gameName : allGames)
			if (!FileHandling.shouldIgnoreLudAnalysis(gameName))
				allGamesToCompare.add(gameName);
		final String[] gamesToCompare = allGamesToCompare.toArray(new String[0]);
		

		for (int i = 0; i < gamesToCompare.length; i++)
		{
			System.out.println(gamesToCompare[i]);
			final File outputFile = new File(outputPath + gamesToCompare[i].replaceAll("[^a-zA-Z0-9]", "_") + ".csv");
			try
			{
				outputFile.createNewFile();
			}
			catch (final IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 

			try (FileWriter myWriter = new FileWriter(outputFile))
			{
				final Map<String, Map<String, Double>> allGameDistances = new HashMap<>();
				
				for (int j = 0; j < gamesToCompare.length; j++)
					allGameDistances.put(gamesToCompare[j], compareTwoGames(GameLoader.loadGameFromName(gamesToCompare[i]), GameLoader.loadGameFromName(gamesToCompare[j])));
				
				// Write the top row of the csv
				String topRow = "GameName";
				@SuppressWarnings("unchecked")
				final List<String> distanceNames = new ArrayList<>(((Map<String, Double>) allGameDistances.values().toArray()[0]).keySet());
				for (final String distance : distanceNames)
				{
					topRow += "," + distance;
				}
				myWriter.write(topRow + "\n");
				
				for (final Map.Entry<String, Map<String, Double>> gameEntry : allGameDistances.entrySet()) 
				{
				    final String gameName = gameEntry.getKey();
				    final List<String> row =  new ArrayList<>(distanceNames);
				    
				    for (final Map.Entry<String, Double> distanceEntry : gameEntry.getValue().entrySet()) 
					{
				    	final String distanceMetric = distanceEntry.getKey();
				    	final Double distaneValue = distanceEntry.getValue();
				    	
				    	row.set(row.indexOf(distanceMetric), String.valueOf(distaneValue));
					}
				    
				    myWriter.write(gameName + "," + String.join(",", row) + "\n");
				}
				
				myWriter.close();
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
		
		
//		for (int i = 0; i < gamesToCompare.length; i++)
//			for (int j = 0; j < gamesToCompare.length; j++)
//				if (i < j)
//					compareTwoGames(GameLoader.loadGameFromName(gamesToCompare[i]), GameLoader.loadGameFromName(gamesToCompare[j]));
	}
	
	//---------------------------------------------------------------------
	
	public static Map<String, Double> compareTwoGames(final Game gameA, final Game gameB)
	{
		final Map<String, Double> allDistances = new HashMap<>();
		
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
		//System.out.println("JensenShannonDivergence");
		
		allDistances.put("JSD_ludeme", jensenShannonDivergenceMetric.distance(ludemeDataset, defaultLudemeVocabulary, gameA, gameB));
		allDistances.put("JSD_booleanConcept", jensenShannonDivergenceMetric.distance(booleanConceptDataset, defaultBooleanConceptVocabulary, gameA, gameB));
		allDistances.put("JSD_moveConcept", jensenShannonDivergenceMetric.distance(moveConceptDataset, defaultMoveConceptVocabulary, gameA, gameB));
		
		allDistances.put("JSD_ludeme_ngram", jensenShannonDivergenceMetric.distance(new NGramDataset(ludemeDataset, nGramLength), defaultLudemeNGramVocabulary, gameA, gameB));
		allDistances.put("JSD_moveCooncept_ngram", jensenShannonDivergenceMetric.distance(new NGramDataset(moveConceptDataset, nGramLength), defaultMoveConceptNGramVocabulary, gameA, gameB));
		
		allDistances.put("JSD_ludeme_TFIDF", jensenShannonDivergenceMetric.distance(ludemeDataset, fullLudemeVocabulary, gameA, gameB));
		allDistances.put("JSD_booleanConcept_TFIDF", jensenShannonDivergenceMetric.distance(booleanConceptDataset, fullBooleanConceptVocabulary, gameA, gameB));
		allDistances.put("JSD_moveConcept_TFIDF", jensenShannonDivergenceMetric.distance(moveConceptDataset, fullMoveConceptVocabulary, gameA, gameB));
		
		//---------------------------------------------------------------------
		// Cosine
		
		final DistanceMetric cosineMetric = new Cosine();
		//System.out.println("Cosine");
		
		allDistances.put("Cosine_ludeme", cosineMetric.distance(ludemeDataset, defaultLudemeVocabulary, gameA, gameB));
		allDistances.put("Cosine_booleanConcept", cosineMetric.distance(booleanConceptDataset, defaultBooleanConceptVocabulary, gameA, gameB));
		allDistances.put("Cosine_moveConcept", cosineMetric.distance(moveConceptDataset, defaultMoveConceptVocabulary, gameA, gameB));
		
		allDistances.put("Cosine_ludeme_ngram", cosineMetric.distance(new NGramDataset(ludemeDataset, nGramLength), defaultLudemeNGramVocabulary, gameA, gameB));
		allDistances.put("Cosine_moveCooncept_ngram", cosineMetric.distance(new NGramDataset(moveConceptDataset, nGramLength), defaultMoveConceptNGramVocabulary, gameA, gameB));
		
		allDistances.put("Cosine_ludeme_TFIDF", cosineMetric.distance(ludemeDataset, fullLudemeVocabulary, gameA, gameB));
		allDistances.put("Cosine_booleanConcept_TFIDF", cosineMetric.distance(booleanConceptDataset, fullBooleanConceptVocabulary, gameA, gameB));
		allDistances.put("Cosine_moveConcept_TFIDF", cosineMetric.distance(moveConceptDataset, fullMoveConceptVocabulary, gameA, gameB));
		
		//---------------------------------------------------------------------
		// Jaccard
		
		final DistanceMetric jaccardMetric = new Jaccard();
		//System.out.println("Jaccard");
		
		allDistances.put("Jaccard_ludeme", jaccardMetric.distance(ludemeDataset, defaultLudemeVocabulary, gameA, gameB));
		allDistances.put("Jaccard_booleanConcept", jaccardMetric.distance(booleanConceptDataset, defaultBooleanConceptVocabulary, gameA, gameB));
		allDistances.put("Jaccard_moveConcept", jaccardMetric.distance(moveConceptDataset, defaultMoveConceptVocabulary, gameA, gameB));
		
		allDistances.put("Jaccard_ludeme_ngram", jaccardMetric.distance(new NGramDataset(ludemeDataset, nGramLength), defaultLudemeNGramVocabulary, gameA, gameB));
		allDistances.put("Jaccard_moveCooncept_ngram", jaccardMetric.distance(new NGramDataset(moveConceptDataset, nGramLength), defaultMoveConceptNGramVocabulary, gameA, gameB));
		
		allDistances.put("Jaccard_ludeme_TFIDF", jaccardMetric.distance(ludemeDataset, fullLudemeVocabulary, gameA, gameB));
		allDistances.put("Jaccard_booleanConcept_TFIDF", jaccardMetric.distance(booleanConceptDataset, fullBooleanConceptVocabulary, gameA, gameB));
		allDistances.put("Jaccard_moveConcept_TFIDF", jaccardMetric.distance(moveConceptDataset, fullMoveConceptVocabulary, gameA, gameB));
		
		//---------------------------------------------------------------------
		// Levenshtein
		
		final DistanceMetric levenshteinMetric = new Levenshtein();
		//System.out.println("Levenshtein");
		
		allDistances.put("Levenshtein_ludeme", levenshteinMetric.distance(ludemeDataset, null, gameA, gameB));
		allDistances.put("Levenshtein_moveConcept", levenshteinMetric.distance(moveConceptDataset, null, gameA, gameB));
		
		//---------------------------------------------------------------------
		// Local Alignment
		
		final DistanceMetric localAlignmentMetric = new LocalAlignment();
		//System.out.println("LocalAlignment");
		
		allDistances.put("LocalAlignment_ludeme", localAlignmentMetric.distance(ludemeDataset, null, gameA, gameB));
		allDistances.put("LocalAlignment_moveConcept", localAlignmentMetric.distance(moveConceptDataset, null, gameA, gameB));
		
		//---------------------------------------------------------------------
		// Repeated Local Alignment
		
		final DistanceMetric repeatedLocalAlignmentMetric = new RepeatedLocalAlignment();
		//System.out.println("RepeatedLocalAlignment");
		
		allDistances.put("RepeatedLocalAlignment_ludeme", repeatedLocalAlignmentMetric.distance(ludemeDataset, null, gameA, gameB));
		allDistances.put("RepeatedLocalAlignment_moveConcept", repeatedLocalAlignmentMetric.distance(moveConceptDataset, null, gameA, gameB));
		
		//---------------------------------------------------------------------
		// Global Alignment
		
		final DistanceMetric globalAlignmentMetric = new GlobalAlignment();
		//System.out.println("GlobalAlignment");
		
		allDistances.put("GlobalAlignment_ludeme", globalAlignmentMetric.distance(ludemeDataset, null, gameA, gameB));
		allDistances.put("GlobalAlignment_moveConcept", globalAlignmentMetric.distance(moveConceptDataset, null, gameA, gameB));
		
		//---------------------------------------------------------------------
		// Zhang Shasha
		
		final DistanceMetric zhangShashaMetric = new ZhangShasha();
		//System.out.println("ZhangShasha");
		
		allDistances.put("ZhangShasha_ludeme", zhangShashaMetric.distance(ludemeDataset, defaultLudemeVocabulary, gameA, gameB));
		
		return allDistances;
	}
	
	//---------------------------------------------------------------------
	
}
