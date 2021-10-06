package processing.kmedoid;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import common.DistanceMatrix;
import common.LudRul;
import processing.similarity_matrix.Drawable;
import processing.similarity_matrix.VisualiserInterface;

public class KmeadioidMatrixDrawable implements Drawable
{
	private final ArrayList<LudRul> candidates;
	private final String name;
	private final DistanceMatrix<LudRul, LudRul> distanceMatrix;
	private final Clustering clustering;
	private BufferedImage image;
	
	private final VisualiserInterface visualiser;
	

	
	public KmeadioidMatrixDrawable(final String name,
			final ArrayList<LudRul> sortedCandidates,
			final Clustering clustering, final DistanceMatrix<LudRul, LudRul> distanceMatrix, final VisualiserInterface visualiser
	)
	{
		this.name = name;
		this.candidates = sortedCandidates;
		this.clustering = clustering;
		this.visualiser = visualiser;
		this.distanceMatrix = distanceMatrix;
		image = Clustering
				.getSimilarityImage(clustering,distanceMatrix, candidates);
	}

	public void setCandidatesAndPaint(final ArrayList<LudRul> newSorting)
	{
		image = Clustering
		.getSimilarityImage(clustering,distanceMatrix, newSorting);
		
	}
	
	@Override
	public BufferedImage getBufferedImage()
	{
		return image;
	}

	@Override
	public void clickAt(final Point p, final MouseEvent me)
	{
		final int x = p.x;
		final int y = p.y;
		if (x<0||y<0)return;
		if (x>=candidates.size()||y>=candidates.size())return;
		this.visualiser.clickAt(x,y, me);
		
	}

	@Override
	public String getName()
	{
		return this.name;
	}
}
