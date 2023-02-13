package cluster;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import main.FileHandling;
import main.options.Ruleset;
import other.GameLoader;
import other.concept.Concept;
import other.concept.ConceptDataType;
import other.concept.ConceptType;
import utils.RulesetNames;

/**
 * Generate the percentage of concepts from a list of rulesets.
 * @author Eric.Piette
 *
 */
public class ConceptsComparaisonClusters
{
	final static String pathFile = "./res/cluster/input/clusters/";
	final static String csv          = ".csv";     
	/**
	 * Main method to call the reconstruction with command lines.
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	@SuppressWarnings("boxing")
	public static void main(final String[] args) throws FileNotFoundException, IOException
	{
		final String[] clusters = {"Cluster1.1", "Cluster1.2", "Cluster1.3", "Cluster1.4", "Cluster1.5", "Cluster1.6", 
				"Cluster2.1", "Cluster2.2", "Cluster2.3", "Cluster2.4", "Cluster2.5", "Cluster2.6", "Cluster2.7", "Cluster2.8", 
				"Cluster3.1", "Cluster3.2", "Cluster3.3", "Cluster3.4", "Cluster3.5", "Cluster3.6", 
				"Cluster3.7", "Cluster3.8", "Cluster3.9", "Cluster3.10", "Cluster3.11", "Cluster3.12",
				"Cluster4"};
	
		for(String clusterName : clusters)
		{
			// Get the list of ruleset names.
			final List<String> rulesetNames = new ArrayList<String>();
			try (BufferedReader br = new BufferedReader(new FileReader(pathFile + clusterName + csv))) 
			{
				String line = br.readLine();
				while (line != null)
				{
					rulesetNames.add(line);
					line = br.readLine();
				}
			}
			
			// Conversion to Game object
			final List<Game> rulesetsCompiled = new ArrayList<Game>();
			final String[] gameNames = FileHandling.listGames();
			for (int index = 0; index < gameNames.length; index++)
			{
				final String gameName = gameNames[index];
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/"))
					continue;
	
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/"))
					continue;
	
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/"))
					continue;
	
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/"))
					continue;
	
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("subgame"))
					continue;
	
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("reconstruction"))
					continue;
	
				final Game game = GameLoader.loadGameFromName(gameName);
				
				final List<Ruleset> rulesetsInGame = game.description().rulesets();
				
				// Get all the rulesets of the game if it has some.
				if (rulesetsInGame != null && !rulesetsInGame.isEmpty())
				{
					for (int rs = 0; rs < rulesetsInGame.size(); rs++)
					{
						final Ruleset ruleset = rulesetsInGame.get(rs);
						if (!ruleset.optionSettings().isEmpty() && !ruleset.heading().contains("Incomplete")) 
						{
							final Game gameRuleset = GameLoader.loadGameFromName(gameName, ruleset.heading());
							final String rulesetName = RulesetNames.gameRulesetName(gameRuleset);
							if(rulesetNames.contains(rulesetName))
								rulesetsCompiled.add(gameRuleset);
						}
					}
				}
				else
				{
					final String rulesetName = RulesetNames.gameRulesetName(game);
					if(rulesetNames.contains(rulesetName))
						rulesetsCompiled.add(game);
				}
			}
			
			System.out.println(clusterName);
			System.out.println("Num compiled rulesets is " + rulesetsCompiled.size());
			System.out.println("*****************************");
		
	//		for(Game gameRuleset: rulesetsCompiled)
	//			System.out.println(RulesetNames.gameRulesetName(gameRuleset));
	
			final List<Concept> concepts = new ArrayList<Concept>();
			for(Concept concept: Concept.values())
			{
				if(concept.type().equals(ConceptType.Start) || concept.type().equals(ConceptType.End) || concept.type().equals(ConceptType.Play)
						|| concept.type().equals(ConceptType.Meta)|| concept.type().equals(ConceptType.Container)|| concept.type().equals(ConceptType.Component)
				)
				{
					concepts.add(concept);
				}
			}
			
			System.out.println("\n\n***Boolean concepts in average***\n");
			final List<ConceptAverageValue> booleanConceptAverageValues = new ArrayList<ConceptAverageValue>();
			// Check boolean concepts
			for(Concept concept: concepts)
			{
				if(concept.dataType().equals(ConceptDataType.BooleanData))
				{
					int count = 0;
					for(Game gameRuleset: rulesetsCompiled)
						if(gameRuleset.booleanConcepts().get(concept.id()))
							count++;
					
					final double average = ((double) count * 100) / rulesetsCompiled.size();
					final ConceptAverageValue conceptAverageValue = new ConceptAverageValue(concept, average);
					booleanConceptAverageValues.add(conceptAverageValue);
				}
			}
			booleanConceptAverageValues.sort((c1, c2) -> { return (c2.value - c1.value) > 0 ? 1 : (c2.value - c1.value) < 0 ? -1 : 0;});
			
			System.out.println("\n\n***Numerical concepts in average***\n");
			final List<ConceptAverageValue> numericalConceptAverageValues = new ArrayList<ConceptAverageValue>();
			// Check numerical concepts
			for(Concept concept: concepts)
			{
				if(concept.dataType().equals(ConceptDataType.IntegerData))
				{
					int count = 0;
					for(Game gameRuleset: rulesetsCompiled)
						if(gameRuleset.nonBooleanConcepts().get(concept.id()) != null)
							count += Double.parseDouble(gameRuleset.nonBooleanConcepts().get(concept.id()));
					final double average = (double) count / (double) rulesetsCompiled.size();
					final ConceptAverageValue conceptAverageValue = new ConceptAverageValue(concept, average);
					numericalConceptAverageValues.add(conceptAverageValue);
				}
				
				if(concept.dataType().equals(ConceptDataType.DoubleData))
				{
					double count = 0;
					for(Game gameRuleset: rulesetsCompiled)
						if(gameRuleset.nonBooleanConcepts().get(concept.id()) != null)
							count += Double.parseDouble(gameRuleset.nonBooleanConcepts().get(concept.id()));
					final double average = count / rulesetsCompiled.size();
					final ConceptAverageValue conceptAverageValue = new ConceptAverageValue(concept, average);
					numericalConceptAverageValues.add(conceptAverageValue);
				}
			}
			
			numericalConceptAverageValues.sort((c1, c2) -> { return (c2.value - c1.value) > 0 ? 1 : (c2.value - c1.value) < 0 ? -1 : 0;});
		
		}
		
	}
}
