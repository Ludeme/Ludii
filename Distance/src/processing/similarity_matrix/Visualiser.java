package processing.similarity_matrix;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import common.DistanceMatrix;
import common.EvaluatorDistanceMetric;
import common.LudRul;
import common.SimilarityMatrix;

public class Visualiser extends JFrame implements VisualiserInterface 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final ArrayList<DistanceMatrixDrawable> drawableMatrices = new ArrayList<>();
	ArrayList<LudRul> sortedCandidates;
	DistanceMatrix<LudRul, LudRul> distanceMatrix;
	private HashMap<LudRul, String> classAssignment;
	private JLabel textLabel;
	private DistanceMatrix<LudRul, LudRul> correctAssigned;
	private DistanceMatrix<LudRul, LudRul> classAssigned;
	private String name;
	private DistanceMatrix<LudRul, LudRul> lineAdjustedDistance;

	

	public Visualiser(
			final String name, final ArrayList<LudRul> candidates,
			final DistanceMatrix<LudRul, LudRul> distanceMatrix, final AssignmentSettings ass
	)
	{
		init(name,candidates, distanceMatrix,ass);
	}

	private void init(
			final String nameParameter, final ArrayList<LudRul> candidates,
			final DistanceMatrix<LudRul, LudRul> dm,
			final AssignmentSettings ass
	)
	{
		this.name = nameParameter;
		sortedCandidates = SimilarityMatrix.sortCandidates(candidates);
		this.distanceMatrix = dm;
		final DistanceMatrix<LudRul, LudRul> cm = SimilarityMatrix
				.getClusterMatrix(sortedCandidates); 
		this.classAssignment = EvaluatorDistanceMetric.getClassAssignment(
				candidates, distanceMatrix, ass);
		
		
		final DistanceMatrix<LudRul, LudRul> assigned = EvaluatorDistanceMetric
				.getAssigned(candidates, classAssignment);
		
		this.correctAssigned = EvaluatorDistanceMetric
				.getCorrectlyAssigned(candidates, cm, assigned, classAssignment);
		this.classAssigned = EvaluatorDistanceMetric
				.getClassAssigned(candidates, cm, assigned, classAssignment);
		this.lineAdjustedDistance = EvaluatorDistanceMetric
				.getLineAdjustedDistance(candidates, dm);
		textLabel = createTextLabel();
		
		final DistanceMatrixDrawable d1 = new DistanceMatrixDrawable("Distance",sortedCandidates,distanceMatrix,this);
		final DistanceMatrixDrawable d2 = new DistanceMatrixDrawable("Ground Truth",sortedCandidates,cm,this);
		final DistanceMatrixDrawable d3 = new DistanceMatrixDrawable("SameAssigned",sortedCandidates,assigned,this);
		final DistanceMatrixDrawable d4 = new DistanceMatrixDrawable("Correct",sortedCandidates,correctAssigned,this);
		final DistanceMatrixDrawable d5 = new DistanceMatrixDrawable("ClassAssigned",sortedCandidates,classAssigned,this);
		final DistanceMatrixDrawable d6 = new DistanceMatrixDrawable("LineAdjusted",sortedCandidates,lineAdjustedDistance,this);
		
		final DrawPanel distPanel = new DrawPanel(d1);
		final DrawPanel distPanel2 = new DrawPanel(d2);
		final DrawPanel distPanel3 = new DrawPanel(d3);
		final DrawPanel distPanel4 = new DrawPanel(d4);
		final DrawPanel distPanel5 = new DrawPanel(d5);
		final DrawPanel distPanel6 = new DrawPanel(d6);
		
		drawableMatrices.add(d1);
		drawableMatrices.add(d2);
		drawableMatrices.add(d3);
		drawableMatrices.add(d4);
		drawableMatrices.add(d5);
		drawableMatrices.add(d6);
		
		shareScalingAcrossPanels(new DrawPanel[] {distPanel,distPanel2,distPanel3,distPanel4,distPanel5,distPanel6});
		final JPanel mainPanel = new JPanel(new BorderLayout());
		
		final JTabbedPane jtp = new JTabbedPane();
		jtp.addTab(distPanel.getName(), null, distPanel,
				"Does nothing");
		jtp.addTab(distPanel2.getName(), null, distPanel2,
				"Does nothing");
		jtp.addTab(distPanel3.getName(), null, distPanel3,
				"Does nothing");
		jtp.addTab(distPanel4.getName(), null, distPanel4,
				"Does nothing");
		jtp.addTab(distPanel5.getName(), null, distPanel5,
				"Does nothing");
		jtp.addTab(distPanel6.getName(), null, distPanel6,
				"Does nothing");
		mainPanel.add(jtp, BorderLayout.CENTER);
		mainPanel.add(textLabel, BorderLayout.SOUTH);
		
		final JToolBar jtb = createToolbar();
		this.add(jtb, BorderLayout.PAGE_START);
		this.add(mainPanel);
		this.pack();
		
		// https://stackoverflow.com/questions/9342233/zoom-in-and-out-of-images-in-java

	}
	

	private static void shareScalingAcrossPanels(final DrawPanel[] drawPanels)
	{
		for (final DrawPanel drawPanel : drawPanels)
		{
			for (final DrawPanel drawPanel2 : drawPanels)
			{
				if (drawPanel2.equals(drawPanel))continue;
				drawPanel2.addSharedScaling(drawPanel);
			}
		}
		
	}

	protected void updateText(final MouseEvent e)
	{
		final int x = e.getX();
		final int y = e.getY();
		if (x < 0 || x >= this.getWidth() || y < 0 || y >= this.getHeight())
			return;
		

	}
	private JLabel createTextLabel()
	{
		final JLabel jl = new JLabel();
		jl.setText("<html>"+name + "<br/><br/><br/><br/><br/><br/><br/><br/><br/><br/></html>");
		return jl;
	}

	private JToolBar createToolbar()
	{
		final JToolBar jtb = new JToolBar("Tools", SwingConstants.HORIZONTAL);
		final JToggleButton toggel = createTravelButton();
		jtb.add(toggel);
		final JButton[] jb = createSortDistanceButton(distanceMatrix);
		final JButton sortMinDistance = jb[0];
		final JButton sortMaxDistance = jb[1];
		jtb.add(sortMinDistance);
		jtb.add(sortMaxDistance);
		JButton testTriangleInequality = new JButton("Test Triangle Inequality");
		testTriangleInequality.addActionListener(new ActionListener()
		{	
			@Override
			public void actionPerformed(ActionEvent e)
			{
				boolean holds = distanceMatrix.doesTriangleInequalityHold();
				JOptionPane.showMessageDialog(null, "Triangle Inequality holds: " + holds, "Triangle Inequality", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		jtb.add(testTriangleInequality);
		return jtb;
	}

	private JToggleButton createTravelButton()
	{
		final JToggleButton toggel = new JToggleButton("Travel");
		toggel.setSelected(false);
		
		final TravelThread tt = new TravelThread(this);
		toggel.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(final ItemEvent arg0)
			{
				tt.checkThread(toggel.isSelected(),sortedCandidates);
				
			}
		});
		return toggel;
	}

	public void reSort() {
		sortedCandidates = SimilarityMatrix.sortCandidates(sortedCandidates);
		resort(sortedCandidates);
		
		
	}
	
	@Override
	public void reSort(final LudRul cand)
	{	
		sortedCandidates = SimilarityMatrix.sortCandidates(cand,sortedCandidates,distanceMatrix);
		resort(sortedCandidates);
	}
	
	private void resort(final ArrayList<LudRul> newSorting)
	{
		for (final DistanceMatrixDrawable drawMatrix : drawableMatrices)
		{
			drawMatrix.setCandidatesAndPaint(newSorting);
		}
		repaint();
	}

	@Override
	public void clickAt(final int x, final int y, final MouseEvent me)
	{
		final LudRul canditX = sortedCandidates.get(x);
		final LudRul canditY = sortedCandidates.get(y);
		
		final String assignedX = classAssignment.get(canditX);
		final String assignedY = classAssignment.get(canditY);
		final String text = "<html><nobr>" + name + "<br/>"+ "row:" + "<br/>&emsp;"+ y+ "<br/>&emsp;" + canditY + "<br/>&emsp;"
				+ canditY.getCurrentClassName() + " assigned as: "+ assignedY + "<br/>"+ "column:" + "<br/>&emsp;"+ x+"<br/>&emsp;" + canditX + "<br/>&emsp;"
				+ canditX.getCurrentClassName() + " assigned as: "+ assignedX + "<br/>&emsp;&emsp;"
				+ distanceMatrix.get(canditX, canditY) +"   corect assigned assignment:" + correctAssigned.get(canditX, canditY) +  "</nobr></html>";
		textLabel.setText(text);
		
		
		final boolean shiftDown = (me.isShiftDown());
		final boolean ctrlDown = me.isControlDown();
		
		if (shiftDown&&!ctrlDown) 
			reSort(canditX);
		if (!shiftDown&&ctrlDown) 
			reSort(canditY);
		if (shiftDown&&ctrlDown) 
			reSort();
	}

}
