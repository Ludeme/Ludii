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

//	final static String coordinatesPath        = "./res/cluster/input/coordinatesCluster2.csv";
//	final static int    numClusters = 8;
//	
//	// Cluster 2.1 area
//	final static int    xMinCluster1 = -10;
//	final static int    xMaxCluster1 = -6;
//	final static double yMinCluster1 = -7.5;
//	final static double yMaxCluster1 = -3.6;
//	
//	// Cluster 2.2 area
//	final static int    xMinCluster2 = -14;
//	final static int    xMaxCluster2 = -8;
//	final static double yMinCluster2 = -0.75;
//	final static int    yMaxCluster2 = 4;
//	
//	final static double secondxMinCluster2 = -8.2;
//	final static double secondxMaxCluster2 = -6.8;
//	final static double secondyMinCluster2 = 2.2;
//	final static int    secondyMaxCluster2 = 3;
//	
//	// Cluster 2.3 area
//	final static double xMinCluster3 = -1.2;
//	final static int    xMaxCluster3 = 5;
//	final static int    yMinCluster3 = -7;
//	final static double yMaxCluster3 = -2.8;
//	
//	final static double secondxMinCluster3 = 5.3;
//	final static double secondxMaxCluster3 = 7;
//	final static double secondyMinCluster3 = -2.75;
//	final static int    secondyMaxCluster3 = -1;
//	
//	// Cluster 2.4 area
//	final static double xMinCluster4 = -5.8;
//	final static double xMaxCluster4 = -0.94;
//	final static int    yMinCluster4 = 2;
//	final static double yMaxCluster4 = 5.5;
//	
//	// Cluster 2.5 area
//	final static double xMinCluster5 = -0.77;
//	final static int    xMaxCluster5 = 2;
//	final static double yMinCluster5 = 1.75;
//	final static double yMaxCluster5 = 4.5;
//	
//	// Cluster 2.6 area
//	final static double xMinCluster6 = 4.25;
//	final static int    xMaxCluster6 = 6;
//	final static double yMinCluster6 = 5.5;
//	final static double yMaxCluster6 = 7.0;
//	
//	// Cluster 2.7 area
//	final static double xMinCluster7 = 1;
//	final static double xMaxCluster7 = 4.5;
//	final static double yMinCluster7 = 0;
//	final static double yMaxCluster7 = 1.25;
//	
//	// Cluster 2.8 area
//	final static double xMinCluster8 = -1.7;
//	final static double xMaxCluster8 = 0;
//	final static double yMinCluster8 = -2.5;
//	final static double yMaxCluster8 = 0.8;
	
	// Coordinates of the 9 sub-clusters of Cluster 3

	final static String coordinatesPath        = "./res/cluster/input/coordinatesCluster3.csv";
	final static int    numClusters = 12;
	
	// Cluster 3.1 area
	final static int    xMinCluster1 = -4;
	final static double xMaxCluster1 = 3;
	final static double yMinCluster1 = 10.8;
	final static int    yMaxCluster1 = 19;
	
	// Cluster 3.2 area
	final static double xMinCluster2 = 6.5;
	final static int    xMaxCluster2 = 12;
	final static double yMinCluster2 = 5.4;
	final static int    yMaxCluster2 = 12;
	
	// Cluster 3.3 area
	final static double xMinCluster3 = 5.4;
	final static int    xMaxCluster3 = 13;
	final static double yMinCluster3 = -2.3;
	final static int    yMaxCluster3 = 3;
	
	// Cluster 3.4 area
	final static double xMinCluster4 = 6.5;
	final static double xMaxCluster4 = 11;
	final static double yMinCluster4 = -7;
	final static int    yMaxCluster4 = -3;
	
	// Cluster 3.5 area
	final static double xMinCluster5 = 4.8;
	final static double xMaxCluster5 = 8;
	final static double yMinCluster5 = -10;
	final static double yMaxCluster5 = -7.5;
	
	// Cluster 3.6 area
	final static double xMinCluster6 = -1.5;
	final static double xMaxCluster6 = 5;
	final static double yMinCluster6 = -13.4;
	final static double yMaxCluster6 = -9.8;
	
	final static double secondxMinCluster6 = -3.5;
	final static double secondxMaxCluster6 = 3;
	final static double secondyMinCluster6 = -11.58;
	final static double secondyMaxCluster6 = -8.3;
	
	final static double thirdxMinCluster6 = -1.6;
	final static double thirdxMaxCluster6 = 2;
	final static double thirdyMinCluster6 = -9.45;
	final static double thirdyMaxCluster6 = -7.28;
	
	// Cluster 3.7 area
	final static double xMinCluster7 = -11.66;
	final static double xMaxCluster7 = -5.22;
	final static double yMinCluster7 = -6.30;
	final static double yMaxCluster7 = 0.25;
	
	final static double secondxMinCluster7 = -7.35;
	final static double secondxMaxCluster7 = -4;
	final static double secondyMinCluster7 = 1.0;
	final static double secondyMaxCluster7 = 2.25;
	
	// Cluster 3.8 area
	final static double xMinCluster8 = -18;
	final static double xMaxCluster8 = -11;
	final static double yMinCluster8 = -2.3;
	final static double yMaxCluster8 = 1.3;
	
	// Cluster 3.9 area
	final static double xMinCluster9 = -10.9;
	final static double xMaxCluster9 = -6;
	final static double yMinCluster9 = 4.4;
	final static double yMaxCluster9 = 8.1;
	
	// Cluster 3.10 area
	final static double xMinCluster10 = -3;
	final static double xMaxCluster10 = 0.8;
	final static double yMinCluster10 = 1.31;
	final static double yMaxCluster10 = 9;

	// Cluster 3.11 area
	final static double xMinCluster11 = 1.43;
	final static double xMaxCluster11 = 6;
	final static double yMinCluster11 = 6;
	final static double yMaxCluster11 = 10;
	
	// Cluster 3.12 area
	final static double xMinCluster12 = -5.2;
	final static double xMaxCluster12 = -0.55;
	final static double yMinCluster12 = -5.45;
	final static double yMaxCluster12 = -0.9;
	
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
			else if(x >= xMinCluster6 && x <= xMaxCluster6 && y >= yMinCluster6 && y <= yMaxCluster6)
				clusters[5].add(gameName);
			else if(x >= secondxMinCluster6 && x <= secondxMaxCluster6 && y >= secondyMinCluster6 && y <= secondyMaxCluster6)
				clusters[5].add(gameName);
			else if(x >= thirdxMinCluster6 && x <= thirdxMaxCluster6 && y >= thirdyMinCluster6 && y <= thirdyMaxCluster6)
				clusters[5].add(gameName);
			else if(x >= xMinCluster7 && x <= xMaxCluster7 && y >= yMinCluster7 && y <= yMaxCluster7)
				clusters[6].add(gameName);
			else if(x >= secondxMinCluster7 && x <= secondxMaxCluster7 && y >= secondyMinCluster7 && y <= secondyMaxCluster7)
				clusters[6].add(gameName);
			else if(x >= xMinCluster8 && x <= xMaxCluster8 && y >= yMinCluster8 && y <= yMaxCluster8)
				clusters[7].add(gameName);
			else if(x >= xMinCluster9 && x <= xMaxCluster9 && y >= yMinCluster9 && y <= yMaxCluster9)
				clusters[8].add(gameName);
			else if(x >= xMinCluster10 && x <= xMaxCluster10 && y >= yMinCluster10 && y <= yMaxCluster10)
				clusters[9].add(gameName);
			else if(x >= xMinCluster11 && x <= xMaxCluster11 && y >= yMinCluster11 && y <= yMaxCluster11)
				clusters[10].add(gameName);
			else if(x >= xMinCluster12 && x <= xMaxCluster12 && y >= yMinCluster12 && y <= yMaxCluster12)
				clusters[11].add(gameName);
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
