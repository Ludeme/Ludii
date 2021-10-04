package processing.similarity_matrix;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import common.DistanceMatrix;
import common.LudRul;
import common.SimilarityMatrix;

public class SimilarityPanelSavetyCopy extends JPanel
{

	private final JLabel label;
	private final String name;
	private final DistanceMatrix<LudRul, LudRul> distanceMatrix;
	private ArrayList<LudRul> candidates;
	private final Visualiser visualiser;
	private final JLabel imageLabel;

	public SimilarityPanelSavetyCopy(
			final Visualiser visualiser, final String name,
			final ArrayList<LudRul> sortedCandidates,
			final DistanceMatrix<LudRul, LudRul> distanceMatrix,
			final JLabel label
	)
	{
		this.visualiser = visualiser;
		this.name = name;
		this.label = label;

		this.distanceMatrix = distanceMatrix;
		this.candidates = sortedCandidates;
		this.addMouseListener(createMouseListener());
		this.addMouseMotionListener(createMouseMotionListener());
		final BufferedImage image = SimilarityMatrix
				.getSimilarityImage(distanceMatrix, sortedCandidates);
		this.imageLabel = new JLabel(new ImageIcon(image));
		this.add(imageLabel);
	}

	private MouseMotionListener createMouseMotionListener()
	{
		final SimilarityPanelSavetyCopy panel = this;
		final MouseMotionListener mml = new MouseMotionListener()
		{

			@Override
			public void mouseMoved(final MouseEvent e)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDragged(final MouseEvent e)
			{
				panel.updateText(e);

			}
		};
		return mml;
	}

	protected void updateText(final MouseEvent e)
	{
		final int x = e.getX();
		final int y = e.getY();
		if (x < 0 || x >= this.getWidth() || y < 0 || y >= this.getHeight())
			return;
		final LudRul canditX = candidates.get(x);
		final LudRul canditY = candidates.get(y);
		final String text = "<html>" + name + "<br/>"+ "row:" + "<br/>&emsp;" + canditY + "<br/>&emsp;"
				+ canditY.getCurrentClassName() + "<br/>"+ "column:" + "<br/>&emsp;" + canditX + "<br/>&emsp;"
				+ canditX.getCurrentClassName() + "<br/>&emsp;&emsp;"
				+ distanceMatrix.get(canditX, canditY) + "</html>";
		label.setText(text);

	}

	private MouseListener createMouseListener()
	{
		final SimilarityPanelSavetyCopy panel = this;
		final MouseListener ml = new MouseListener()
		{

			@Override
			public void mouseReleased(final MouseEvent arg0)
			{
			}

			@Override
			public void mousePressed(final MouseEvent arg0)
			{
				panel.updateText(arg0);
			}

			@Override
			public void mouseExited(final MouseEvent arg0)
			{
			}

			@Override
			public void mouseEntered(final MouseEvent arg0)
			{
			}

			@Override
			public void mouseClicked(final MouseEvent arg0)
			{
				panel.clicked(arg0);
			}
		};
		return ml;
	}

	protected void clicked(final MouseEvent arg0)
	{
		if (arg0.getButton() == MouseEvent.BUTTON2)
		{
			visualiser.reSort();
		}
		if (arg0.getButton() == MouseEvent.BUTTON3)
		{
			final int x = arg0.getX();
			final int y = arg0.getY();
			LudRul cand;
			if (arg0.isAltDown())
				cand = getCandidate(y);
			else
				cand = getCandidate(x);

			visualiser.reSort(cand);
		}
	}

	private LudRul getCandidate(final int x)
	{

		return candidates.get(x);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void setCandidatesAndPaint(final ArrayList<LudRul> sortedCandidates)
	{
		this.candidates = sortedCandidates;
		final BufferedImage image = SimilarityMatrix
				.getSimilarityImage(distanceMatrix, candidates);
		imageLabel.setIcon(new ImageIcon(image));

	}

}
