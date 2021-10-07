package processing.kmedoid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;

import common.DistanceMatrix;
import common.LudRul;

public class KmedoidClustering
{

	
	private ArrayList<Clustering> clusterings;

	public ArrayList<Clustering> getClusterings()
	{
		return clusterings;
	}

	public KmedoidClustering(
	)
	{
	}

	public void generateClusterings(
			final ArrayList<LudRul> candidates,
			final DistanceMatrix<LudRul, LudRul> distanceMatrix, final int minK, final int maxK
	)
	{
		clusterings = new ArrayList<Clustering>();
		for (int k = minK; k <= maxK; k++)
		{
			final Clustering clustering = createKmedoidClustering(candidates,distanceMatrix,k);
			clusterings.add(clustering);
		}
	}

	private static Clustering createKmedoidClustering(final ArrayList<LudRul> candidates, final DistanceMatrix<LudRul,LudRul> distanceMatrix, final int k)
	{
		
		return new Clustering(candidates,distanceMatrix,k);
	}


	public void printKtoSSE()
	{
		if (clusterings==null)return;
		final Stream<Clustering> stream = clusterings.stream().sorted(Comparator.comparingInt(Clustering::getK));
		
		
		stream.forEach(action->System.out.println(action.getK() + " " + action.getSSE()+ " "));
		System.out.println();
		for (final Clustering clustering : clusterings)
		{
			final LudRul[] med = clustering.getMedoid();
			System.out.print(clustering.getK() + ": " );
			for (final LudRul ludRul : med)
			{
				System.out.print(ludRul.getGameNameIncludingOption(false) + " ");
			}
			System.out.println();
		}
	}


	public static String getToolTipText()
	{
		final String ttt = "<html>Creates several K-medoid cluserings of the selected Games<br>"
				+ "The according K to SSE curve shows the sum of the square error to the new cluster centers<br>"
				+ "It is suggested that the best clustering is the one where the derivative of the k to SSE changes from vertical to horizontal"
				+ "</html>";
		return ttt;
	}

	public static KmedoidClustering getInstance(
			ArrayList<LudRul> candidates,
			DistanceMatrix<LudRul, LudRul> distanceMatrix, int minK, int maxK
	)
	{
		KmedoidClustering kc = new KmedoidClustering();
		kc.generateClusterings(candidates, distanceMatrix, minK, maxK);
		return kc;
	}


}
