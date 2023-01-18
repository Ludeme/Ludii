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
	final static int    numClusters = 4;
	
	// Cluster 1 area
	final static int    xMinCluster1 = -50;
	final static int    xMaxCluster1 = 0;
	final static int    yMinCluster1 = -40;
	final static int    yMaxCluster1 = 20;
	
	// Cluster 2 area
	final static int    xMinCluster2 = -30;
	final static int    xMaxCluster2 = 10;
	final static int    yMinCluster2 = 20;
	final static int    yMaxCluster2 = 50;
	
	// Cluster 3 area
	final static int    xMinCluster3 = 0;
	final static int    xMaxCluster3 = 40;
	final static int    yMinCluster3 = -30;
	final static int    yMaxCluster3 = 40;
	
	// Cluster 4 area
	final static int    xMinCluster4 = 40;
	final static int    xMaxCluster4 = 70;
	final static int    yMinCluster4 = -40;
	final static int    yMaxCluster4 = -10;
	
	final static String coordinatesPath        = "./res/cluster/input/coordinates.csv";
	
	/**
	 * Main method to call the reconstruction with command lines.
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public static void main(final String[] args) throws FileNotFoundException, IOException
	{
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
			String[] gameAndCoordinates = coordinates.get(i).split(",");
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
		
	}
}
