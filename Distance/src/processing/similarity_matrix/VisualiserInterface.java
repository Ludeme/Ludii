package processing.similarity_matrix;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

import common.DistanceMatrix;
import common.LudRul;

public interface VisualiserInterface
{

	void clickAt(int x, int y, MouseEvent me);

	void reSort(LudRul ludRul);
	
	
	
	public default JButton[] createSortDistanceButton(final DistanceMatrix<LudRul, LudRul> distanceMatrix)
	{
		final double[][] dm = distanceMatrix.getDistanceMatrix();
		
		double minimum = Integer.MAX_VALUE;
		int minimumIndex = -1;
		double maximum = Integer.MIN_VALUE;
		int maximumIndex = -1;
		
		for (int i = 0; i < dm.length; i++)
		{
			double localMaximum = 0;
			for (int j = 0; j < dm.length; j++)
			{
				if (dm[i][j]>localMaximum)localMaximum=dm[i][j];
			}
			if (localMaximum<minimum) {
				minimum=localMaximum;
				minimumIndex = i;
			}
			if (localMaximum>maximum) {
				maximum=localMaximum;
				maximumIndex = i;
			}
		}
		final int finalMinimumIndex = minimumIndex;
		final int finalMaximumIndex = maximumIndex;
		final JButton sortByMin = new JButton("Sort by smallest Diameter");
		sortByMin.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				
				final LudRul canditX = distanceMatrix.getIndexToCandidate().get(finalMinimumIndex);
				reSort(canditX);
			}
		});
		final JButton sortByMax = new JButton("Sort by biggest Diameter");
		sortByMax.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final LudRul canditX = distanceMatrix.getIndexToCandidate().get(finalMaximumIndex);
				reSort(canditX);
			}
		});
		
		return new JButton[] {sortByMin,sortByMax};
	}

}
