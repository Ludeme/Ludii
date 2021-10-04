package processing.similarity_matrix;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import common.DistanceMatrix;
import common.LudRul;
import common.SimilarityMatrix;

public class DistanceMatrixDrawable implements Drawable
{

	private final ArrayList<LudRul> candidates;
	private final String name;
	private final DistanceMatrix<LudRul, LudRul> distanceMatrix;
	
	private BufferedImage image;
	
	private final VisualiserInterface visualiser;

	
	public DistanceMatrixDrawable(final String name,
			final ArrayList<LudRul> sortedCandidates,
			final DistanceMatrix<LudRul, LudRul> distanceMatrix, final VisualiserInterface visualiser
	)
	{
		this.name = name;
		this.candidates = sortedCandidates;
		this.distanceMatrix = distanceMatrix;
		this.visualiser = visualiser;
		
		image = SimilarityMatrix
				.getSimilarityImage(distanceMatrix, candidates);
	}

	public void setCandidatesAndPaint(final ArrayList<LudRul> newSorting)
	{
		image = SimilarityMatrix
		.getSimilarityImage(distanceMatrix, newSorting);
		
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
