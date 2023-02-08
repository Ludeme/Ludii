package cluster;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generate the percentage of concepts from a list of rulesets.
 * @author Eric.Piette
 *
 */
public class ConceptsFromCluster
{
	final static String listRulesets        = "./res/cluster/input/Cluster.csv";
	
	/**
	 * Main method to call the reconstruction with command lines.
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public static void main(final String[] args) throws FileNotFoundException, IOException
	{
		// Read the CSV line by line.
		final List<String> coordinates = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(listRulesets))) 
		{
			String line = br.readLine();
			while (line != null)
			{
				coordinates.add(line);
				line = br.readLine();
			}
		}
		
//		
//		// init game names list
//		final List<String> gameNames = new ArrayList<String>();
//		try (BufferedReader br = new BufferedReader(new FileReader(gamePath))) 
//		{
//			String line = br.readLine();
//			while (line != null)
//			{
//				gameNames.add(line.substring(1, line.length()-1)); // we remove the quotes.
//				line = br.readLine();
//			}
//		}
//		
//		
//		// init the clusters results;
//		final List<String>[] clusters = new ArrayList[numClusters];
//		for(int i = 0; i < numClusters; i++)
//			clusters[i] = new ArrayList<String>();
//
//
//		
//		for(int i = 0; i < coordinates.size(); i++)
//		{
//			String[] gameAndCoordinates = coordinates.get(i).split(";");
//			final String gameName = gameAndCoordinates[0];
//			final double x = Double.parseDouble(gameAndCoordinates[1]);
//			final double y = Double.parseDouble(gameAndCoordinates[2]);
//			
//			if(x >= xMinCluster1 && x <= xMaxCluster1 && y >= yMinCluster1 && y <= yMaxCluster1)
//				clusters[0].add(gameName);
//			else if(x >= xMinCluster2 && x <= xMaxCluster2 && y >= yMinCluster2 && y <= yMaxCluster2)
//				clusters[1].add(gameName);
//			else if(x >= xMinCluster3 && x <= xMaxCluster3 && y >= yMinCluster3 && y <= yMaxCluster3)
//				clusters[2].add(gameName);
//			else if(x >= xMinCluster4 && x <= xMaxCluster4 && y >= yMinCluster4 && y <= yMaxCluster4)
//				clusters[3].add(gameName);
//			else if(x >= xMinCluster5 && x <= xMaxCluster5 && y >= yMinCluster5 && y <= yMaxCluster5)
//				clusters[4].add(gameName);
//			else if(x >= xMinCluster6 && x <= xMaxCluster6 && y >= yMinCluster6 && y <= yMaxCluster6)
//				clusters[5].add(gameName);
////			else if(x >= xMinCluster7 && x <= xMaxCluster7 && y >= yMinCluster7 && y <= yMaxCluster7)
////				clusters[6].add(gameName);
////			else if(x >= xMinCluster8 && x <= xMaxCluster8 && y >= yMinCluster8 && y <= yMaxCluster8)
////				clusters[7].add(gameName);
////			else if(x >= xMinCluster9 && x <= xMaxCluster9 && y >= yMinCluster9 && y <= yMaxCluster9)
////				clusters[8].add(gameName);
//			else
//				System.err.println(gameName + " does not go to any cluster");
//		}
//
//		for(int i = 0; i < numClusters; i++)
//		{
//			System.out.println("****************** Cluster " + (i + 1) + "  **************************");
//			for(int j = 0; j < clusters[i].size(); j++)
//				System.out.println(clusters[i].get(j));
//			System.out.println("*****Size = " + clusters[i].size());
//			
//			System.out.println();
//		}
//		
//		final String SQLRequest = "SELECT DISTINCT GameRulesets.Id AS GameRulesetsId, GameRulesets.Name AS GameRulesetsName, Games.Id AS GamesId, Games.Name AS GamesName FROM GameRulesets, Games, RulesetConcepts WHERE Games.Id = GameRulesets.GameId AND RulesetConcepts.RulesetId = GameRulesets.Id AND (GameRulesets.Type = 1 OR GameRulesets.Type = 3) AND Games.DLPGame = 1 AND (";
//		String SQLRequestCluster1 = SQLRequest;
//		String SQLRequestCluster2 = SQLRequest;
//		String SQLRequestCluster3 = SQLRequest;
//		String SQLRequestCluster4 = SQLRequest;
//		
//		// Request for Cluster 1.
//		for(int i = 0; i < clusters[0].size() - 1; i++)
//		{
//			String gameName = clusters[0].get(i);
//			//System.out.println("test for " + fullGameName);
//			boolean found = false;
//			while(!found)
//			{
//				String gameNameWithUnderscore = gameName.substring(0, gameName.lastIndexOf('_'));
//				gameName = gameNameWithUnderscore.replace('_', ' ');
//				//System.out.println("Test: " + possibleGameName);
//				for(int j = 0; j < gameNames.size(); j++)
//				{
//					if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
//					{
//						found = true;
//						gameName = gameNames.get(j);
//						SQLRequestCluster1 += "Games.Name = \\\"" + gameName + "\\\" OR ";
//						break;
//					}
//				}
//				gameName = gameNameWithUnderscore;
//				if(!gameName.contains("_")) // If this is reached, the game name is never found.
//				{
//					for(int j = 0; j < gameNames.size(); j++)
//					{
//						if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
//						{
//							found = true;
//							gameName = gameNames.get(j);
//							SQLRequestCluster1 += "Games.Name = \\\"" + gameName + "\\\" OR ";
//							break;
//						}
//					}
//					
//					if(!found)
//					{
//						System.err.println(clusters[0].get(i) + " is never found in the list of game names.");
//						System.exit(1);
//					}
//				}
//			}
//		}
//		String gameName = clusters[0].get(clusters[0].size()-1);
//		//System.out.println("test for " + fullGameName);
//		boolean found = false;
//		while(!found)
//		{
//			String gameNameWithUnderscore = gameName.substring(0, gameName.lastIndexOf('_'));
//			gameName = gameNameWithUnderscore.replace('_', ' ');
//			//System.out.println("Test: " + possibleGameName);
//			for(int j = 0; j < gameNames.size(); j++)
//			{
//				if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
//				{
//					found = true;
//					gameName = gameNames.get(j);
//					SQLRequestCluster1 += "Games.Name = \\\"" + gameName + "\\\")";
//					break;
//				}
//			}
//			if(!found)
//			{
//				gameName = gameNameWithUnderscore;
//				if(!gameName.contains("_")) // If this is reached, the game name is never found.
//				{
//					for(int j = 0; j < gameNames.size(); j++)
//					{
//						if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
//						{
//							found = true;
//							gameName = gameNames.get(j);
//							SQLRequestCluster1 += "Games.Name = \\\"" + gameName + "\\\")";
//							break;
//						}
//					}
//					
//					if(!found)
//					{
//						System.err.println(clusters[0].get(clusters[0].size()-1) + " is never found in the list of game names.");
//						System.exit(1);
//					}
//					
//				}
//			}
//		}
	}
}
