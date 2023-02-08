package cluster;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generate the clusters based on the coordinates obtained with Sklearn.
 * @author Eric.Piette
 *
 */
public class GenerateClusters
{
	// Coordinates of the 4 main clusters

//	final static int    numClusters = 4;
	
//	// Cluster 1 area
//	final static int    xMinCluster1 = -50;
//	final static int    xMaxCluster1 = 0;
//	final static int    yMinCluster1 = -40;
//	final static int    yMaxCluster1 = 20;
//	
//	// Cluster 2 area
//	final static int    xMinCluster2 = -30;
//	final static int    xMaxCluster2 = 10;
//	final static int    yMinCluster2 = 20;
//	final static int    yMaxCluster2 = 50;
//	
//	// Cluster 3 area
//	final static int    xMinCluster3 = 0;
//	final static int    xMaxCluster3 = 40;
//	final static int    yMinCluster3 = -30;
//	final static int    yMaxCluster3 = 20;
//	
//	// Cluster 4 area
//	final static int    xMinCluster4 = 40;
//	final static int    xMaxCluster4 = 70;
//	final static int    yMinCluster4 = -40;
//	final static int    yMaxCluster4 = 10;
	
	// Coordinates of the 4 sub-clusters of Cluster 1

//	final static String coordinatesPath        = "./res/cluster/input/coordinatesCluster1.csv";
//	final static int    numClusters = 6;
//	
//	// Cluster 1.1 area
//	final static int    xMinCluster1 = -15;
//	final static double xMaxCluster1 = -2.4;
//	final static int    yMinCluster1 = -15;
//	final static int    yMaxCluster1 = 0;
//	
//	// Cluster 1.2 area
//	final static int    xMinCluster2 = -1;
//	final static double xMaxCluster2 = 7.5;
//	final static int    yMinCluster2 = -15;
//	final static int    yMaxCluster2 = -4;
//	
//	// Cluster 1.3 area
//	final static double xMinCluster3 = -2.45;
//	final static int    xMaxCluster3 = 5;
//	final static int    yMinCluster3 = -3;
//	final static double yMaxCluster3 = 5.44;
//	
//	// Cluster 1.4 area
//	final static int    xMinCluster4 = 3;
//	final static int    xMaxCluster4 = 17;
//	final static int    yMinCluster4 = 7;
//	final static int    yMaxCluster4 = 16;
//	
//	// Cluster 1.5 area
//	final static double xMinCluster5 = -4.5;
//	final static int    xMaxCluster5 = 1;
//	final static double yMinCluster5 = 3.6;
//	final static int    yMaxCluster5 = 9;
//	
//	// Cluster 1.6 area
//	final static int    xMinCluster6 = 11;
//	final static double xMaxCluster6 = 12.25;
//	final static int    yMinCluster6 = 4;
//	final static double yMaxCluster6 = 6.5;
	
	// Coordinates of the 5 sub-clusters of Cluster 2

	final static String coordinatesPath        = "./res/cluster/input/coordinatesCluster2.csv";
	final static int    numClusters = 5;
	
	// Cluster 2.1 area
	final static int    xMinCluster1 = -10;
	final static int    xMaxCluster1 = -3;
	final static int    yMinCluster1 = -8;
	final static int    yMaxCluster1 = -2;
	
	// Cluster 2.2 area
	final static int    xMinCluster2 = -15;
	final static int    xMaxCluster2 = -7;
	final static int    yMinCluster2 = -2;
	final static int    yMaxCluster2 = 4;
	
	// Cluster 2.3 area
	final static int    xMinCluster3 = -3;
	final static int    xMaxCluster3 = 10;
	final static int    yMinCluster3 = -8;
	final static int    yMaxCluster3 = -1;
	
	// Cluster 2.4 area
	final static int    xMinCluster4 = -7;
	final static double xMaxCluster4 = -0.85;
	final static int    yMinCluster4 = 2;
	final static int    yMaxCluster4 = 8;
	
	// Cluster 2.5 area
	final static double xMinCluster5 = -0.85;
	final static int    xMaxCluster5 = 10;
	final static int    yMinCluster5 = -1;
	final static int    yMaxCluster5 = 10;
	
	// Coordinates of the 9 sub-clusters of Cluster 3

//	final static String coordinatesPath        = "./res/cluster/input/coordinatesCluster3.csv";
//	final static int    numClusters = 9;
//	
//	// Cluster 3.1 area
//	final static int    xMinCluster1 = -20;
//	final static double xMaxCluster1 = -10.5;
//	final static int    yMinCluster1 = -3;
//	final static int    yMaxCluster1 = 2;
//	
//	// Cluster 3.2 area
//	final static int    xMinCluster2 = -4;
//	final static int    xMaxCluster2 = 6;
//	final static int    yMinCluster2 = 11;
//	final static int    yMaxCluster2 = 20;
//	
//	// Cluster 3.3 area
//	final static double xMinCluster3 = 5.4;
//	final static int    xMaxCluster3 = 13;
//	final static double yMinCluster3 = -2.2;
//	final static int    yMaxCluster3 = 12;
//	
//	// Cluster 3.4 area
//	final static int    xMinCluster4 = -3;
//	final static double xMaxCluster4 = 2;
//	final static double yMinCluster4 = 0.5;
//	final static int    yMaxCluster4 = 10;
//	
//	// Cluster 3.5 area
//	final static double xMinCluster5 = -12;
//	final static double xMaxCluster5 = -1.5;
//	final static double yMinCluster5 = -6.5;
//	final static double yMaxCluster5 = -3.8;
//	
//	// Cluster 3.6 area
//	final static double xMinCluster6 = -4;
//	final static int    xMaxCluster6 = 8;
//	final static int    yMinCluster6 = -17;
//	final static double yMaxCluster6 = -7.25;
//	
//	// Cluster 3.7 area
//	final static double xMinCluster7 = 6.5;
//	final static int    xMaxCluster7 = 11;
//	final static double yMinCluster7 = -6.7;
//	final static double yMaxCluster7 = -3.5;
//	
//	// Cluster 3.8 area
//	final static double xMinCluster8 = -10.5;
//	final static double xMaxCluster8 = -3.1;
//	final static double yMinCluster8 = -0.45;
//	final static int    yMaxCluster8 = 10;
//	
//	// Cluster 3.9 area
//	final static double xMinCluster9 = -10.4;
//	final static int    xMaxCluster9 = 3;
//	final static double yMinCluster9 = -3.3;
//	final static double yMaxCluster9 = -0.5;
	
	final static String gamePath        	   = "./res/cluster/input/Games.csv";
	
	/**
	 * Main method to call the reconstruction with command lines.
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public static void main(final String[] args) throws FileNotFoundException, IOException
	{
		// init game names list
		final List<String> gameNames = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(gamePath))) 
		{
			String line = br.readLine();
			while (line != null)
			{
				gameNames.add(line.substring(1, line.length()-1)); // we remove the quotes.
				line = br.readLine();
			}
		}
		
		
		// init the clusters results;
		final List<String>[] clusters = new ArrayList[numClusters];
		for(int i = 0; i < numClusters; i++)
			clusters[i] = new ArrayList<String>();

		// Read the CSV line by line.
		final List<String> coordinates = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(coordinatesPath))) 
		{
			String line = br.readLine();
			while (line != null)
			{
				coordinates.add(line);
				line = br.readLine();
			}
		}
		
		for(int i = 0; i < coordinates.size(); i++)
		{
			String[] gameAndCoordinates = coordinates.get(i).split(";");
			final String gameName = gameAndCoordinates[0];
			final double x = Double.parseDouble(gameAndCoordinates[1]);
			final double y = Double.parseDouble(gameAndCoordinates[2]);
			
			if(x >= xMinCluster1 && x <= xMaxCluster1 && y >= yMinCluster1 && y <= yMaxCluster1)
				clusters[0].add(gameName);
			else if(x >= xMinCluster2 && x <= xMaxCluster2 && y >= yMinCluster2 && y <= yMaxCluster2)
				clusters[1].add(gameName);
			else if(x >= xMinCluster3 && x <= xMaxCluster3 && y >= yMinCluster3 && y <= yMaxCluster3)
				clusters[2].add(gameName);
			else if(x >= xMinCluster4 && x <= xMaxCluster4 && y >= yMinCluster4 && y <= yMaxCluster4)
				clusters[3].add(gameName);
			else if(x >= xMinCluster5 && x <= xMaxCluster5 && y >= yMinCluster5 && y <= yMaxCluster5)
				clusters[4].add(gameName);
//			else if(x >= xMinCluster6 && x <= xMaxCluster6 && y >= yMinCluster6 && y <= yMaxCluster6)
//				clusters[5].add(gameName);
//			else if(x >= xMinCluster7 && x <= xMaxCluster7 && y >= yMinCluster7 && y <= yMaxCluster7)
//				clusters[6].add(gameName);
//			else if(x >= xMinCluster8 && x <= xMaxCluster8 && y >= yMinCluster8 && y <= yMaxCluster8)
//				clusters[7].add(gameName);
//			else if(x >= xMinCluster9 && x <= xMaxCluster9 && y >= yMinCluster9 && y <= yMaxCluster9)
//				clusters[8].add(gameName);
			else
				System.err.println(gameName + " does not go to any cluster");
		}

		for(int i = 0; i < numClusters; i++)
		{
			System.out.println("****************** Cluster " + (i + 1) + "  **************************");
			for(int j = 0; j < clusters[i].size(); j++)
				System.out.println(clusters[i].get(j));
			System.out.println("*****Size = " + clusters[i].size());
			
			System.out.println();
		}
		
		final String SQLRequest = "SELECT DISTINCT GameRulesets.Id AS GameRulesetsId, GameRulesets.Name AS GameRulesetsName, Games.Id AS GamesId, Games.Name AS GamesName FROM GameRulesets, Games, RulesetConcepts WHERE Games.Id = GameRulesets.GameId AND RulesetConcepts.RulesetId = GameRulesets.Id AND (GameRulesets.Type = 1 OR GameRulesets.Type = 3) AND Games.DLPGame = 1 AND (";
		String SQLRequestCluster1 = SQLRequest;
		String SQLRequestCluster2 = SQLRequest;
		String SQLRequestCluster3 = SQLRequest;
		String SQLRequestCluster4 = SQLRequest;
		
		// Request for Cluster 1.
		for(int i = 0; i < clusters[0].size() - 1; i++)
		{
			String gameName = clusters[0].get(i);
			//System.out.println("test for " + fullGameName);
			boolean found = false;
			while(!found)
			{
				String gameNameWithUnderscore = gameName.substring(0, gameName.lastIndexOf('_'));
				gameName = gameNameWithUnderscore.replace('_', ' ');
				//System.out.println("Test: " + possibleGameName);
				for(int j = 0; j < gameNames.size(); j++)
				{
					if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
					{
						found = true;
						gameName = gameNames.get(j);
						SQLRequestCluster1 += "Games.Name = \\\"" + gameName + "\\\" OR ";
						break;
					}
				}
				gameName = gameNameWithUnderscore;
				if(!gameName.contains("_")) // If this is reached, the game name is never found.
				{
					for(int j = 0; j < gameNames.size(); j++)
					{
						if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
						{
							found = true;
							gameName = gameNames.get(j);
							SQLRequestCluster1 += "Games.Name = \\\"" + gameName + "\\\" OR ";
							break;
						}
					}
					
					if(!found)
					{
						System.err.println(clusters[0].get(i) + " is never found in the list of game names.");
						System.exit(1);
					}
				}
			}
		}
		String gameName = clusters[0].get(clusters[0].size()-1);
		//System.out.println("test for " + fullGameName);
		boolean found = false;
		while(!found)
		{
			String gameNameWithUnderscore = gameName.substring(0, gameName.lastIndexOf('_'));
			gameName = gameNameWithUnderscore.replace('_', ' ');
			//System.out.println("Test: " + possibleGameName);
			for(int j = 0; j < gameNames.size(); j++)
			{
				if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
				{
					found = true;
					gameName = gameNames.get(j);
					SQLRequestCluster1 += "Games.Name = \\\"" + gameName + "\\\")";
					break;
				}
			}
			if(!found)
			{
				gameName = gameNameWithUnderscore;
				if(!gameName.contains("_")) // If this is reached, the game name is never found.
				{
					for(int j = 0; j < gameNames.size(); j++)
					{
						if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
						{
							found = true;
							gameName = gameNames.get(j);
							SQLRequestCluster1 += "Games.Name = \\\"" + gameName + "\\\")";
							break;
						}
					}
					
					if(!found)
					{
						System.err.println(clusters[0].get(clusters[0].size()-1) + " is never found in the list of game names.");
						System.exit(1);
					}
					
				}
			}
		}
		
		
		
		
		
		
		// Request for Cluster 2.
		for(int i = 0; i < clusters[1].size() - 1; i++)
		{
			gameName = clusters[1].get(i);
			//System.out.println("test for " + fullGameName);
			found = false;
			while(!found)
			{
				String gameNameWithUnderscore = gameName.substring(0, gameName.lastIndexOf('_'));
				gameName = gameNameWithUnderscore.replace('_', ' ');
				//System.out.println("Test: " + possibleGameName);
				for(int j = 0; j < gameNames.size(); j++)
				{
					if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
					{
						found = true;
						gameName = gameNames.get(j);
						SQLRequestCluster2 += "Games.Name = \\\"" + gameName + "\\\" OR ";
						break;
					}
				}
				gameName = gameNameWithUnderscore;
				if(!gameName.contains("_")) // If this is reached, the game name is never found.
				{
					for(int j = 0; j < gameNames.size(); j++)
					{
						if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
						{
							found = true;
							gameName = gameNames.get(j);
							SQLRequestCluster2 += "Games.Name = \\\"" + gameName + "\\\" OR ";
							break;
						}
					}
					
					if(!found)
					{
						System.err.println(clusters[1].get(i) + " is never found in the list of game names.");
						System.exit(1);
					}
				}
			}
		}
		gameName = clusters[1].get(clusters[1].size()-1);
		//System.out.println("test for " + fullGameName);
		found = false;
		while(!found)
		{
			String gameNameWithUnderscore = gameName.substring(0, gameName.lastIndexOf('_'));
			gameName = gameNameWithUnderscore.replace('_', ' ');
			//System.out.println("Test: " + possibleGameName);
			for(int j = 0; j < gameNames.size(); j++)
			{
				if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
				{
					found = true;
					gameName = gameNames.get(j);
					SQLRequestCluster2 += "Games.Name = \\\"" + gameName + "\\\")";
					break;
				}
			}
			if(!found)
			{
				gameName = gameNameWithUnderscore;
				if(!gameName.contains("_")) // If this is reached, the game name is never found.
				{
					for(int j = 0; j < gameNames.size(); j++)
					{
						if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
						{
							found = true;
							gameName = gameNames.get(j);
							SQLRequestCluster2 += "Games.Name = \\\"" + gameName + "\\\")";
							break;
						}
					}
					
					if(!found)
					{
						System.err.println(clusters[1].get(clusters[1].size()-1) + " is never found in the list of game names.");
						System.exit(1);
					}
				}
			}
		}
		
		
		
		

		// Request for Cluster 3.
		for(int i = 0; i < clusters[2].size() - 1; i++)
		{
			gameName = clusters[2].get(i);
			//System.out.println("test for " + fullGameName);
			found = false;
			while(!found)
			{
				String gameNameWithUnderscore = gameName.substring(0, gameName.lastIndexOf('_'));
				gameName = gameNameWithUnderscore.replace('_', ' ');
				for(int j = 0; j < gameNames.size(); j++)
				{
					if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
					{
						found = true;
						gameName = gameNames.get(j);
						SQLRequestCluster3 += "Games.Name = \\\"" + gameName + "\\\" OR ";
						break;
					}
				}
				gameName = gameNameWithUnderscore;
				if(!gameName.contains("_")) // If this is reached, the game name is never found.
				{
					for(int j = 0; j < gameNames.size(); j++)
					{
						if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
						{
							found = true;
							gameName = gameNames.get(j);
							SQLRequestCluster3 += "Games.Name = \\\"" + gameName + "\\\" OR ";
							break;
						}
					}
					
					if(!found)
					{
						System.err.println(clusters[2].get(i) + " is never found in the list of game names.");
						System.exit(1);
					}
				}
			}
		}
		gameName = clusters[2].get(clusters[2].size()-1);
		//System.out.println("test for " + fullGameName);
		found = false;
		while(!found)
		{
			String gameNameWithUnderscore = gameName.substring(0, gameName.lastIndexOf('_'));
			gameName = gameNameWithUnderscore.replace('_', ' ');
			//System.out.println("Test: " + possibleGameName);
			for(int j = 0; j < gameNames.size(); j++)
			{
				if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
				{
					found = true;
					gameName = gameNames.get(j);
					SQLRequestCluster3 += "Games.Name = \\\"" + gameName + "\\\")";
					break;
				}
			}
			if(!found)
			{
				gameName = gameNameWithUnderscore;
				if(!gameName.contains("_")) // If this is reached, the game name is never found.
				{
					for(int j = 0; j < gameNames.size(); j++)
					{
						if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
						{
							found = true;
							gameName = gameNames.get(j);
							SQLRequestCluster3 += "Games.Name = \\\"" + gameName + "\\\")";
							break;
						}
					}
					
					if(!found)
					{
						System.err.println(clusters[2].get(clusters[2].size()-1) + " is never found in the list of game names.");
						System.exit(1);
					}
					
				}
			}
		}
		

		
		
		// Request for Cluster 4.
		for(int i = 0; i < clusters[3].size() - 1; i++)
		{
			gameName = clusters[3].get(i);
			//System.out.println("test for " + fullGameName);
			found = false;
			while(!found)
			{
				String gameNameWithUnderscore = gameName.substring(0, gameName.lastIndexOf('_'));
				gameName = gameNameWithUnderscore.replace('_', ' ');
				//System.out.println("Test: " + possibleGameName);
				for(int j = 0; j < gameNames.size(); j++)
				{
					if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
					{
						found = true;
						gameName = gameNames.get(j);
						SQLRequestCluster4 += "Games.Name = \\\"" + gameName + "\\\" OR ";
						break;
					}
				}
				gameName = gameNameWithUnderscore;
				if(!gameName.contains("_")) // If this is reached, the game name is never found.
				{
					for(int j = 0; j < gameNames.size(); j++)
					{
						if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
						{
							found = true;
							gameName = gameNames.get(j);
							SQLRequestCluster4 += "Games.Name = \\\"" + gameName + "\\\" OR ";
							break;
						}
					}
					
					if(!found)
					{
						System.err.println(clusters[3].get(i) + " is never found in the list of game names.");
						System.exit(1);
					}
				}
			}
		}
		gameName = clusters[3].get(clusters[3].size()-1);
		//System.out.println("test for " + fullGameName);
		found = false;
		while(!found)
		{
			String gameNameWithUnderscore = gameName.substring(0, gameName.lastIndexOf('_'));
			gameName = gameNameWithUnderscore.replace('_', ' ');
			//System.out.println("Test: " + possibleGameName);
			for(int j = 0; j < gameNames.size(); j++)
			{
				if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
				{
					found = true;
					gameName = gameNames.get(j);
					SQLRequestCluster4 += "Games.Name = \\\"" + gameName + "\\\")";
					break;
				}
			}
			if(!found)
			{
				gameName = gameNameWithUnderscore;
				if(!gameName.contains("_")) // If this is reached, the game name is never found.
				{
					for(int j = 0; j < gameNames.size(); j++)
					{
						if(gameNames.get(j).replace("'","").replace("(","").replace(")","").equals(gameName))
						{
							found = true;
							gameName = gameNames.get(j);
							SQLRequestCluster4 += "Games.Name = \\\"" + gameName + "\\\")";
							break;
						}
					}
					
					if(!found)
					{
						System.err.println(clusters[3].get(clusters[3].size()-1) + " is never found in the list of game names.");
						System.exit(1);
					}
					
				}
			}
		}
		
		
		System.out.println(SQLRequestCluster1);
		System.out.println("********************");
		System.out.println(SQLRequestCluster2);
		System.out.println("********************");
		System.out.println(SQLRequestCluster3);
		System.out.println("********************");
		System.out.println(SQLRequestCluster4);
		
	}
}
