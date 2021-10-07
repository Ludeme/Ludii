package processing.kmedoid;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import common.DistanceMatrix;
import common.EvaluatorDistanceMetric;
import common.LudRul;
import common.SimilarityMatrix;
import metrics.DistanceMetric;
import processing.similarity_matrix.DrawPanel;
import processing.similarity_matrix.TravelThread;
import processing.similarity_matrix.VisualiserInterface;

public class ClusteringVisualiser extends JFrame implements VisualiserInterface
{
	private static final long serialVersionUID = 1L;
	
	private final ArrayList<KmeadioidMatrixDrawable> drawableMatrices = new ArrayList<>();
	ArrayList<LudRul> sortedCandidates;
	DistanceMatrix<LudRul, LudRul> distanceMatrix;
	
	private JLabel textLabel;
	private String name;
	@SuppressWarnings("unused")
	private KmedoidClustering kmedioidClustering;
	private JTabbedPane jtp;
	private final HashMap<DrawPanel,Clustering> componentToClustering = new HashMap<>();
	int[] lastXYClicked;
	

	public ClusteringVisualiser(
			final ArrayList<LudRul> candidates, final DistanceMetric metric,final KmedoidClustering kmc
	)
	{
		final DistanceMatrix<LudRul, LudRul> matrix = EvaluatorDistanceMetric.getDistanceMatrix(candidates,
				metric, false, null);
		init(metric.getName(),candidates, matrix,kmc);

	}

	private void init(
			final String nameParameter, final ArrayList<LudRul> candidates,
			final DistanceMatrix<LudRul, LudRul> dm,
			final KmedoidClustering kmc
	)
	{
		this.name = nameParameter;
		sortedCandidates = SimilarityMatrix.sortCandidates(candidates);
		this.distanceMatrix = dm;
		this.kmedioidClustering = kmc;
		jtp = new JTabbedPane();
		ArrayList<LineDrawable> liste = new ArrayList<>(kmc.getClusterings());
		final LineDrawer gp = new LineDrawer(liste);
		jtp.addTab("k to sse curve", null, gp,
				"Does nothing");
		final ArrayList<DrawPanel> sharedPanels = new ArrayList<>();
		
		final ArrayList<Clustering> clusterings = kmc.getClusterings();
		
		for (final Clustering clustering : clusterings)
		{
			final KmeadioidMatrixDrawable d = new KmeadioidMatrixDrawable("ClassAssigned",sortedCandidates,clustering,distanceMatrix,this);
			drawableMatrices.add(d);
			
			final DrawPanel distPanel = new DrawPanel(d);
			componentToClustering.put(distPanel, clustering);
			sharedPanels.add(distPanel);
			jtp.addTab("Clusters: " + clustering.getK(), null, distPanel,
					"Does nothing");
		}
		jtp.addChangeListener(new ChangeListener()
		{
			
			@Override
			public void stateChanged(final ChangeEvent e)
			{
				final Clustering c = getCurentVisualClustering();
				if (lastXYClicked==null||c==null)return;
				setTextField(lastXYClicked[0],lastXYClicked[1]);
			}
		});
		
		
		DrawPanel[] sharedArray = new DrawPanel[sharedPanels.size()]; 
        sharedArray = sharedPanels.toArray(sharedArray); 
		
		shareScalingAcrossPanels(sharedArray);
		final JPanel mainPanel = new JPanel(new BorderLayout());
		
		textLabel = createTextLabel();
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
		for (final KmeadioidMatrixDrawable drawMatrix : drawableMatrices)
		{
			drawMatrix.setCandidatesAndPaint(newSorting);
		}
		repaint();
	}

	@Override
	public void clickAt(final int x, final int y, final MouseEvent me)
	{
		
		
		setTextField(x, y);
		
		final LudRul canditX = sortedCandidates.get(x);
		final LudRul canditY = sortedCandidates.get(y);
		//final boolean altDown = me.isAltDown(); confuses with middle mouse
		final boolean shiftDown = (me.isShiftDown());
		final boolean ctrlDown = me.isControlDown();
		
		if (shiftDown&&!ctrlDown) 
			reSort(canditX);
		if (!shiftDown&&ctrlDown) 
			reSort(canditY);
		if (shiftDown&&ctrlDown) 
			reSort();
	}
	void setTextField(
			final int x, final int y
	)
	{
		final LudRul canditX = sortedCandidates.get(x);
		final LudRul canditY = sortedCandidates.get(y);
		
		final Clustering clustering = getCurentVisualClustering();
		
		final LudRul closestY = clustering.closest(distanceMatrix, canditY);
		final LudRul closestX = clustering.closest(distanceMatrix, canditX);
		lastXYClicked = new int[] {x,y};
		final String text = "<html><nobr>" + "Clustering: "  + clustering.getK()  + " Sse: "+clustering.getSSE() + "<br/>"
		+ name + "<br/>"+ "row:" + "<br/>&emsp;"+ y+ "<br/>&emsp;" + canditY + "<br/>&emsp;"
				+ canditY.getCurrentClassName() + "<br/>&emsp;"+ 
		 "closest medoid: " + closestY +"<br/><br/>" +
		"column:" + "<br/>&emsp;"+ x+"<br/>&emsp;" + canditX + "<br/>&emsp;"
				+ canditX.getCurrentClassName() + "<br/>&emsp;"+ "closest medoid: " + closestX  
		+" <br/>" + "Distance: "+ distanceMatrix.get(canditX, canditY)+  "</nobr></html>";
		//textLabel.
		textLabel.setText(text);
	}
	Clustering getCurentVisualClustering()
	{
		final Component something = jtp.getSelectedComponent();
		final Clustering c = componentToClustering.get(something);
		
		return c;
	}

}
