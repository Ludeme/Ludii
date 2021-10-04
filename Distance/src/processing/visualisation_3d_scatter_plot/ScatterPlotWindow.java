package processing.visualisation_3d_scatter_plot;

import javax.swing.*;
import javax.swing.border.LineBorder;

import common.DistanceMatrix;
import common.DistanceUtils;
import common.EvaluatorDistanceMetric;
import common.FolderLocations;
import common.LudRul;
import common.WrapLayout;
import metrics.DistanceMetric;
import metrics.individual.JensenShannonDivergence;
import processing.kmedoid.Clustering;
import processing.kmedoid.KmedoidClustering;
import processing.similarity_matrix.AssignmentSettings;
import processing.similarity_matrix.ColorScheme;
import processing.visualisation_3d_scatter_plot.WrapperClass.Pos;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by Niebisch Markus on 05.02.2020.
 */
public class ScatterPlotWindow extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static final Logger logger = Logger.getLogger(ScatterPlotWindow.class.getName());
	final JFrame frame;

	boolean animation = true;
	boolean rotate = true;
	int cycleLength;
	private BufferedImage backGroundImage;
	private int storedWidth;
	private int storedHeight;
	WrapperClass wc;

	protected boolean showGTV = false;
	protected boolean flowanimation;
	protected int flowCycleLength = 20000;
	ColorAssignment colorAssignment1;
	ColorAssignment colorAssignment2;

	private ArrayList<ColorAssignment> colorAssignments;
	double manualAngleZRotation;
	double manualAngleXRotation;

	protected boolean blnShowClass = false;
	protected boolean blnShowRelationToSelected = false;
	protected boolean blnShowDistanceToSelected = false;
	protected boolean blnShowBright = false;
	private String selectedKlass;
	private ColorAssignment selectedKlassColorAssignment;

	LudRul clickedAt;

	private JPanel legendPanel;
	private int legendWidth = 30;
	JTextArea textPane;
	protected boolean showTwoClasses;
	protected boolean showTopClassInside;
	protected boolean rotateAlongXAchsis;
	JComboBox<ColorAssignment> combo2;
	JToggleButton autoRotate;

	public static void main(String[] args)
	{
		/*
		 * final ArrayList<LudRul> candidates = DistanceUtils
		 * .getAllLudiiGameFilesAndRulesetCombination(false, new
		 * File(FolderLocations.boardFolder.getAbsolutePath()+"/sow/"), null);
		 */
		final ArrayList<LudRul> candidates = DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false,
						new File(FolderLocations.boardFolder.getAbsolutePath()),
						null);

		final boolean recalc = false;
		final DistanceMetric metric = new JensenShannonDivergence();
		// final DistanceMetric metric = new CosineSimilarity();
		

		final DistanceMatrix<LudRul, LudRul> distanceMatrix;

		distanceMatrix = EvaluatorDistanceMetric.getDistanceMatrix(candidates,
				metric, recalc, null);
		final AssignmentSettings ass = new AssignmentSettings(3, 3);
		KmedoidClustering kc = KmedoidClustering.getInstance(candidates, distanceMatrix, 2, 6);
		 
		
		ArrayList<ColorAssignment> cas = createColorAssignments(candidates, kc,
				distanceMatrix, ass);

		ScatterPlotWindow w = new ScatterPlotWindow(cas);

		w.setVisible(true);

		DrawHelper drawHelper = w.new DrawHelper().invoke();

		WrapperClass wc = new WrapperClass(distanceMatrix, drawHelper, w);
		w.wc = wc;
		w.repaint();
		w.startAnimation();

		wc.startAnneahling();
		w.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		w.frame.setTitle(metric.getName() + "_" + candidates.size());
	}

	private static ArrayList<ColorAssignment> createColorAssignments(
			final ArrayList<LudRul> candidates, KmedoidClustering kc,
			final DistanceMatrix<LudRul, LudRul> distanceMatrix,
			final AssignmentSettings ass
	)
	{
		ArrayList<ColorAssignment> cas = new ArrayList<>();
		
		
		ColorAssignment ca = ColorAssignment.getAssignmentByFolder(candidates);
		cas.add(ca);
		HashMap<LudRul, String> classAssignment = EvaluatorDistanceMetric
				.getClassAssignment(candidates, distanceMatrix, ass);
		ColorAssignment ccorrect = ColorAssignment
				.getAssignmentByCorrectness(candidates, classAssignment);
		cas.add(ccorrect);
		ColorAssignment cClassified = ColorAssignment
				.getAssignmentByClassifier(candidates, classAssignment, ca);
		cas.add(cClassified);
		ColorAssignment cm = ColorAssignment.getAssignmentByMathewFolder(candidates);
		cas.add(cm);
		for (Clustering clustering : kc.getClusterings())
		{
			ColorAssignment ca1 = ColorAssignment.getAssignmentByClustering(
					candidates, clustering, distanceMatrix);
			cas.add(ca1);
		}
		return cas;
	}

	public ScatterPlotWindow(
			ArrayList<LudRul> candidates, AssignmentSettings ass,DistanceMetric dm,
			DistanceMatrix<LudRul,LudRul> distanceMatrix, int minK, int maxK
	)
	{
		this(createColorAssignments(candidates, KmedoidClustering.getInstance(candidates, distanceMatrix, minK, maxK),
				distanceMatrix, ass));
		this.setVisible(true);

		DrawHelper drawHelper = this.new DrawHelper().invoke();

		WrapperClass wrapperClass = new WrapperClass(distanceMatrix, drawHelper, this);
		this.wc = wrapperClass;
		this.repaint();
		this.startAnimation();

		wc.startAnneahling();
		frame.setTitle(dm.getName() + "_" + candidates.size());
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	wc.setClose(true);
		    }
		});
	}
	
	public ScatterPlotWindow(
			ArrayList<ColorAssignment> cas
	)
	{
		DrawHelper dh = new DrawHelper();
		dh.invoke();
		this.colorAssignment1 = cas.get(0);
		this.colorAssignment2 = cas.get(Math.min(1, cas.size() - 1));
		this.colorAssignments = cas;
		frame = new JFrame();
		
		this.setSize(800, 400);
		this.setPreferredSize(new Dimension(800, 600));
		frame.setSize(820, 420);
		frame.setPreferredSize(new Dimension(820, 420));
		frame.add(this, BorderLayout.CENTER);

		addKeyListener();

		legendPanel = new JPanel();
		legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
		replaceLegend();
		// JPanel helperLegend = new JPanel(BoarderLay)

		frame.add(legendPanel, BorderLayout.EAST);
		final JPanel buttonPane = createButtonPanel();
		frame.add(buttonPane, BorderLayout.SOUTH);

		JPanel leftPane = createTextPanel();
		frame.add(leftPane, BorderLayout.WEST);

		JToolBar menuBar = new JToolBar();
		menuBar.setFloatable(false);
		frame.add(menuBar, BorderLayout.PAGE_START);
		JToggleButton jtbtnTwoAssignment = new JToggleButton("Two Class");
		jtbtnTwoAssignment.setToolTipText("Enables to show two classes at the same time");
		JToggleButton jtbtnMarkErrorOutside = new JToggleButton();
		jtbtnMarkErrorOutside.setToolTipText("Decides if the top of the two selected classifications is shown at the center or the fringe of the ball");
		menuBar.add(jtbtnTwoAssignment);
		jtbtnTwoAssignment.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				showTwoClasses = jtbtnTwoAssignment.isSelected();
				combo2.setEnabled(jtbtnTwoAssignment.isSelected());
				jtbtnMarkErrorOutside
						.setEnabled(jtbtnTwoAssignment.isSelected());
				replaceLegend();
			}
		});

		menuBar.add(jtbtnMarkErrorOutside);
		jtbtnMarkErrorOutside.setEnabled(false);
		jtbtnMarkErrorOutside.setText("Top Class Inside");
		jtbtnMarkErrorOutside.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				showTopClassInside = jtbtnMarkErrorOutside.isSelected();
				if (!jtbtnMarkErrorOutside.isSelected()) {
					jtbtnMarkErrorOutside.setText("Top Class Inside");
				}else
					jtbtnMarkErrorOutside.setText("Top Class OutSide");
			}
		});

		JButton jbResetAngle = new JButton("Reset Angle");
		menuBar.add(jbResetAngle);
		jbResetAngle.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				manualAngleXRotation = 0;
				manualAngleZRotation = 0;
			}
		});

		JToggleButton jtbtnRotateAlongXaxsis = new JToggleButton(
				"Lock X-axsis");
		jtbtnRotateAlongXaxsis.setSelected(true);
		menuBar.add(jtbtnRotateAlongXaxsis);
		jtbtnRotateAlongXaxsis.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				rotateAlongXAchsis = !jtbtnRotateAlongXaxsis.isSelected();
			}
		});

		autoRotate = new JToggleButton("AutoRotate");
		autoRotate.setSelected(true);
		menuBar.add(autoRotate);
		autoRotate.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				rotate = autoRotate.isSelected();

			}
		});
		JToggleButton menu3 = new JToggleButton("AutoRotate Speed");
		menuBar.add(menu3);
		cycleLength = 180000;
		menu3.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (!menu3.isSelected())
					cycleLength = (int) (180000 * Math.signum(cycleLength));
				else
					cycleLength = (int) (20000 * Math.signum(-cycleLength));

			}

		});

		JToggleButton showClass = new JToggleButton("Show Class");
		showClass.setSelected(true);
		menuBar.add(showClass);
		blnShowClass = true;
		showClass.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				blnShowClass = showClass.isSelected();

			}
		});

		JToggleButton showBright = new JToggleButton("Placement Error");
		menuBar.add(showBright);
		showBright.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				blnShowBright = showBright.isSelected();
				if (blnShowBright) {
					blnShowClass=false;
					showClass.setSelected(false);
				}
			}
		});
		JToggleButton showIndividualError = new JToggleButton("Placement Error Individual");
		menuBar.add(showIndividualError);
		showIndividualError.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				blnShowRelationToSelected = showIndividualError.isSelected();
				if (blnShowRelationToSelected) {
					blnShowBright = true;
					showBright.setSelected(true);
					blnShowClass=false;
					showClass.setSelected(false);
				}
				
			}
		});
		JToggleButton showIndividualDistance = new JToggleButton("Individual Distance");
		menuBar.add(showIndividualDistance);
		showIndividualDistance.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				blnShowDistanceToSelected = showIndividualDistance.isSelected();
				if (blnShowDistanceToSelected) {
					blnShowBright = true;
					showBright.setSelected(true);
					blnShowClass=false;
					showClass.setSelected(false);
				}
				
			}
		});
		
		JButton saveState = new JButton("Save State");
		menuBar.add(saveState);
		saveState.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				SaveStateHelper.saveDialoge(wc);
			}
		});

		JButton loadState = new JButton("Load State");
		menuBar.add(loadState);
		loadState.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				SaveStateHelper.loadDialoge(wc);

			}
		});

	}

	

	private JPanel createTextPanel()
	{
		JTextArea panel = new JTextArea();
		panel.setEditable(false);
		panel.setPreferredSize(new Dimension(200, 400));
		panel.setLineWrap(true);
		this.textPane = panel;
		

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.add(panel);
		container.add(Box.createVerticalGlue());
		return container;
	}

	private JPanel createLegend(ColorAssignment ca)
	{
		JPanel panel = new JPanel();
		panel.setOpaque(true);
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		Set<Entry<String, Color>> ccc = ca.getLegend().entrySet();
		for (Entry<String, Color> entry : ccc)
		{
			String legendLabelText = getLegendLabelText(
					entry.getKey().toString());
			JLabel legendLabel = new JLabel(legendLabelText,
					SwingConstants.CENTER);
			legendLabel.setForeground(Color.BLACK);
			legendLabel.setOpaque(true);
			legendLabel.setBackground(entry.getValue());

			legendLabel.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent me)
				{
					if (SwingUtilities.isLeftMouseButton(me))
						setSelectedClass(entry.getKey(), ca);
					if (SwingUtilities.isRightMouseButton(me))
						setSelectedClass(null, null);
					if (SwingUtilities.isMiddleMouseButton(me))
					{
						Color newColor = JColorChooser.showDialog(null,
								"Choose Class Color", entry.getValue());
						if (newColor != null)
						{
							ca.overwrite(entry.getKey(), newColor);
							replaceLegend();
						}
					}
				}
			});

			panel.add(legendLabel);
		}

		return panel;

	}

	protected void drag(Point previous, Point point)
	{
		double delta = point.x - previous.x;
		double sss = delta / this.getWidth() * 3 * 2 * Math.PI;
		manualAngleZRotation += sss;
		if (rotateAlongXAchsis)
		{
			double deltaY = point.y - previous.y;
			double sssY = deltaY / this.getHeight() * 3 * 2 * Math.PI;
			manualAngleXRotation += sssY;
		}

	}

	private String getLegendLabelText(String string)
	{

		StringBuilder sb = new StringBuilder(string);
		for (int w = legendWidth; w < string.length(); w += legendWidth)
		{
			sb.insert(w, "<br/>");
		}
		sb.insert(0, "<html>");
		sb.append("</html>");
		return sb.toString();
	}

	protected void setSelectedClass(String key, ColorAssignment ca)
	{
		selectedKlass = key;
		selectedKlassColorAssignment = ca;

	}

	private JPanel createButtonPanel()
	{

		final JPanel buttonPane = new JPanel();

		buttonPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		buttonPane.setLayout(new WrapLayout(FlowLayout.LEFT));
		// buttonPane.setLayout(new FlowLayout(FlowLayout.LEFT));

		JToggleButton progressGraph = new JToggleButton("Progress Graph");
		progressGraph.setToolTipText("Shows/hides the progress graph");
		progressGraph.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				wc.setProgressGraph(progressGraph.isSelected());

			}
		});
		progressGraph.setSelected(true);

		JToggleButton autoOptimize = new JToggleButton("Auto Optimize");
		autoOptimize.setToolTipText("Auto adjusts the temp and step in Simulated Annealing");
		JToggleButton force2d = new JToggleButton("Force 2d");
		autoOptimize.setToolTipText("The games are arranged in the 2d plane");
		JToggleButton simAnnealing = new JToggleButton("Sim Annealing");
		simAnnealing.setToolTipText("Uses Simulated Anneahling to arrange the games like their distance matrix proposes");
		JToggleButton improveSelected = new JToggleButton("Improve Selected");
		improveSelected.setToolTipText("Every second iteration a new place is tried for the selected game. Helps to deal with outliers");
		JToggleButton simAnnealingEvaluationFunction = new JToggleButton(
				"Evaluation^2");
		simAnnealingEvaluationFunction.setToolTipText("Uses the sum of square distances instead of absolute distance. So far no real difference has been seen");
		JButton increaseTemp = new JButton("Increase Temp");
		increaseTemp.setToolTipText("Increases the temp (acceptance of worse solution) by 10%. One can use shift + mouse wheel up instead");
		JButton decreaseTemp = new JButton("Decrease Temp");
		decreaseTemp.setToolTipText("Decreases the temp (acceptance of worse solution) by 10%. One can use shift + mouse wheel down instead");
		JButton increaseStep = new JButton("Increase Step");
		increaseStep.setToolTipText("Increases the stepsize (variety of new solutions) by 10%. One can use ctrl + mouse wheel up instead");
		JButton decreaseStep = new JButton("Decrease Step");
		decreaseStep.setToolTipText("Decreases the stepsize (variety of new solutions) by 10%. One can use ctrl + mouse wheel down instead");
		JButton resetPoints = new JButton("Reset Points");
		resetPoints.setToolTipText("Resets the positioning");
		
		force2d.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				wc.force2d = force2d.isSelected();
				manualAngleXRotation = 0;
				manualAngleZRotation = 0;
				rotate = false;
				autoRotate.setSelected(false);
			}
		});
		
		simAnnealing.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				wc.setSimAnnealing(simAnnealing.isSelected());
				autoOptimize.setEnabled(simAnnealing.isSelected());

				improveSelected.setEnabled(simAnnealing.isSelected());
				simAnnealingEvaluationFunction
						.setEnabled(simAnnealing.isSelected());
				increaseTemp.setEnabled(simAnnealing.isSelected());
				decreaseTemp.setEnabled(simAnnealing.isSelected());
				increaseStep.setEnabled(simAnnealing.isSelected());
				decreaseStep.setEnabled(simAnnealing.isSelected());
				
			}
		});
		simAnnealing.setSelected(true);

		simAnnealingEvaluationFunction.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				wc.setSimAnnealingSquareEvaluationFunction(
						simAnnealingEvaluationFunction.isSelected());

			}
		});

		autoOptimize.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				wc.setAutoOptimize(autoOptimize.isSelected());

			}
		});
		autoOptimize.setSelected(true);

		improveSelected.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				wc.setImproveSelected(improveSelected.isSelected());

			}
		});

		increaseTemp.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				wc.multiplyTemp(1.1);
			}
		});

		decreaseTemp.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				wc.multiplyTemp(0.9);
			}
		});

		increaseStep.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				wc.multiplyStep(1.1);
			}
		});

		decreaseStep.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				wc.multiplyStep(0.9);
			}
		});
		
		resetPoints.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				wc.resetPoints();
			}
		});

		ColorAssignment[] caArray = colorAssignments
				.toArray(new ColorAssignment[colorAssignments.size()]);
		final JComboBox<ColorAssignment> combo = new JComboBox<ColorAssignment>(
				caArray);
		combo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				colorAssignment1 = (ColorAssignment) combo.getSelectedItem();
				replaceLegend();
			}
		});
		combo2 = new JComboBox<ColorAssignment>(caArray);
		combo2.setEnabled(false);
		combo2.setSelectedItem(colorAssignment2);
		combo2.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				colorAssignment2 = (ColorAssignment) combo2.getSelectedItem();
				replaceLegend();
			}
		});
		buttonPane.add(combo);
		buttonPane.add(combo2);

		buttonPane.add(increaseStep);
		buttonPane.add(decreaseStep);
		buttonPane.add(increaseTemp);
		buttonPane.add(decreaseTemp);
		buttonPane.add(improveSelected);
		buttonPane.add(progressGraph);
		buttonPane.add(simAnnealing);
		buttonPane.add(simAnnealingEvaluationFunction);
		buttonPane.add(autoOptimize);
		buttonPane.add(force2d);
		buttonPane.add(resetPoints);
		return buttonPane;

	}

	protected void replaceLegend()
	{
		Dimension dim = new Dimension(frame.getSize());
		legendPanel.removeAll();

		if (colorAssignment1 != null)
		{
			JLabel label1 = new JLabel(colorAssignment1.toString());
			JPanel legendNew = createLegend(colorAssignment1);
			legendPanel.add(label1);
			legendPanel.add(legendNew);
		}

		if (colorAssignment2 != null && combo2 != null && combo2.isEnabled())
		{
			JLabel label2 = new JLabel(colorAssignment2.toString());
			JPanel legendNew = createLegend(colorAssignment2);
			legendPanel.add(label2);
			legendPanel.add(legendNew);
		}

		frame.setPreferredSize(dim);
		this.setPreferredSize(this.getSize());
		frame.pack();
		// frame.setSize(dim);
		// frame.setResizable(true);
		// frame.pack();

	}

	private void addKeyListener()
	{
		this.addMouseWheelListener(new MouseWheelListener()
		{

			@Override
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				if (e.isShiftDown())
				{
					if (e.getWheelRotation() < 0)
					{
						wc.multiplyTemp(1.1);
					} else
					{
						wc.multiplyTemp(0.9);
					}
				}
				if (e.isControlDown())
				{
					if (e.getWheelRotation() < 0)
					{
						wc.multiplyStep(1.1);
					} else
					{
						wc.multiplyStep(0.9);
					}
				}

			}
		});
		this.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{

				if (SwingUtilities.isLeftMouseButton(e))
				{
					if (e.isControlDown())
					{
						manualAngleZRotation += 2 * Math.PI / 32;
					} else if (e.isShiftDown())
					{
						wc.multiplyTemp(1.1);
					} else
					{
						clickAt(e.getPoint(), wc.getPosAssignment());
					}

				}

				if (SwingUtilities.isRightMouseButton(e))
				{
					if (e.isControlDown())
					{
						manualAngleZRotation -= 2 * Math.PI / 32;
					} else if (e.isShiftDown())
					{
						wc.multiplyTemp(0.9);
					} else
					{
						clickedAt = null;
						//updateTextPanel(null);
					}

				}

			}
		});
		this.addMouseMotionListener(new MouseMotionListener()
		{

			private Point previous;

			@Override
			public void mouseMoved(MouseEvent e)
			{
				this.previous = e.getPoint();
			}

			@Override
			public void mouseDragged(MouseEvent e)
			{
				if (SwingUtilities.isRightMouseButton(e))
				{
					if (previous != null)
						drag(previous, e.getPoint());
					this.previous = e.getPoint();
				}

			}

		});
	}

	protected void clickAt(Point point, HashMap<LudRul, Pos> posAssignment)
	{
		HashMap<LudRul, Pos> copy = WrapperClass
				.copyPossAssignment(wc.getPosAssignment());

		turnEveryArroundXAchsis(wc, manualAngleXRotation, copy);
		turnEveryPosAroundZAchsis(wc, manualAngleZRotation, copy);

		Set<Entry<LudRul, Pos>> entries = copy.entrySet();
		ArrayList<LudRul> clickCandidate = new ArrayList<>();
		DrawHelper drawHelper = new DrawHelper().invoke();
		for (Entry<LudRul, Pos> entry : entries)
		{
			Pos p = entry.getValue();
			squeezeP(p);

			double[] screenPos = drawHelper.getScreenPos(p.x, p.y, p.z);
			double dist = (point.getY() - screenPos[1])
					* (point.getY() - screenPos[1])
					+ (point.getX() - screenPos[0])
							* (point.getX() - screenPos[0]);
			dist = Math.sqrt(dist);

			if (dist < 10)
			{
				clickCandidate.add(entry.getKey());
			}
		}
		sortRespectiveToScreen(copy, clickCandidate);
		if (clickCandidate.size() != 0)
		{
			LudRul ss = clickCandidate.get(clickCandidate.size() - 1);
			clickedAt = ss;
			//updateTextPanel(null);
		}

	}

	private void startAnimation()
	{

		Thread t = new Thread()
		{
			@Override
			public void run()
			{
				long tlast = System.currentTimeMillis();
				while (animation)
				{
					long current = System.currentTimeMillis();
					long delta = current - tlast;
					tlast = current;
					if (rotate)
					{
						manualAngleZRotation += ((double) delta) / cycleLength
								* Math.PI * 2;
					}
					try
					{
						Thread.sleep((long) (16.));
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					frame.repaint();

				}
			}
		};
		t.start();

	}

	

	public void updateTextPanel(int counter, DistanceError ce)
	{
		NumberFormat formatter = new DecimalFormat("#0.000");
		StringBuilder sb = new StringBuilder();

		
		if (ce != null)
		{
			sb.append("Iterrations: " + counter + "\n");
			sb.append(
					"Total cost: " + formatter.format(ce.totalCost) + "\n");
			sb.append("Avg cost: "
					+ formatter.format(ce.totalCost / ce.individualError.length)
					+ "\n");
			sb.append("temp: " + formatter.format(wc.optimisationHistory.getTemperature()) + "\n");
			sb.append("step: " + formatter.format(wc.optimisationHistory.getStep()) + "\n");

		}
		sb.append("\n");
		sb.append("\n");
		if (selectedKlass != null)
		{
			sb.append("Class selected: " + selectedKlass + "\n");
			sb.append("\n");
		}
		if (clickedAt != null)
		{
			sb.append("Game selected: \n"
					+ clickedAt.getGameNameIncludingOption(true) + "\n");
			sb.append("Folder: " + clickedAt.getCurrentClassName() + "\n");
			sb.append("Classed Top as: " + colorAssignment1.getClass(clickedAt) + "\n");
			if (showTwoClasses)sb.append("Classed Bottom as: " + colorAssignment2.getClass(clickedAt)
					+ "\n");
			if (ce != null)
				sb.append("local error: "
						+ formatter.format(ce.errorMap.get(clickedAt)));
			
			sb.append("\n");
		}


		
		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				textPane.setText(sb.toString());
			}
		});

	}
	
	@Override
	public void paint(Graphics g)
	{
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		boolean breakB = false;
		if (breakB)
			return;

		HashMap<LudRul, Pos> copy = WrapperClass
				.copyPossAssignment(wc.getPosAssignment());
		DistanceError error = wc.getCurrentError();

		turnEveryArroundXAchsis(wc, manualAngleXRotation, copy);
		turnEveryPosAroundZAchsis(wc, manualAngleZRotation, copy);

		// if (output==null) return;
		int width = this.getWidth();
		int height = this.getHeight();
		DrawHelper drawHelper = new DrawHelper().invoke();
		// int totalValuesX = drawHelper.getTotalValuesX();
		// int totalValuesY = drawHelper.getTotalValuesY();
		if (backGroundImage == null || width != storedWidth
				|| height != storedHeight)
		{
			this.backGroundImage = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);
			storedWidth = width;
			storedHeight = height;

		}

		g.drawImage(backGroundImage, 0, 0, null);

		drawGrids(g, drawHelper, wc);

		double maxRecomendedZ = drawHelper.maxRecomendedZ();
		ArrayList<LudRul> list = new ArrayList<LudRul>(wc.getNodes());

		sortRespectiveToScreen(copy, list);
		for (int i = 0; i < list.size(); i++)
		{
			LudRul ns = list.get(i);
			Pos p = copy.get(ns);
			squeezeP(p);
			double[] screenPos = drawHelper.getScreenPos(p.x, p.y, 0);
			double shadowRatio = 1 - Math.min(1, p.z / maxRecomendedZ);
			g.setColor(new Color(0, 0, 0, 128));
			// if p[2]/drawHelper.m
			g.fillOval((int) screenPos[0] - 5, (int) screenPos[1] - 5,
					(int) (shadowRatio * 5) + 5, (int) (shadowRatio * 5) + 5);
		}

		HashSet<LudRul> selectedInstances = null;
		if (selectedKlass != null)
			selectedInstances = selectedKlassColorAssignment.getClassifiedAs()
					.get(selectedKlass);
		
		HashMap<LudRul,Double> individualError = null;
		Double minIndividualError = null;
		Double maxIndividualError = null;
		if (blnShowRelationToSelected&&clickedAt!=null) {
			individualError = wc.getCostToRest(clickedAt,copy);
			Double[] minMax = getMinMax(individualError);
			minIndividualError = minMax[0];
			maxIndividualError = minMax[1];
		}
		
		for (int i = 0; i < list.size(); i++)
		{
			LudRul ns = list.get(i);
			Pos p = copy.get(ns);

			double[] screenPos = drawHelper.getScreenPos(p.x, p.y, p.z);
			double distanceRatio = 1 - Math.min(1, p.y / drawHelper.maxY);
			distanceRatio = Math.max(0, distanceRatio);
			double radius = (distanceRatio * 3 + 5);
			int diameter = (int) Math.round(radius * 2);
			float brightness = (float) (0.8 + 0.2 * distanceRatio);
			brightness = Math.max(0.f, brightness);
			brightness = Math.min(1f, brightness);

			// if (!showGTV) hsb=
			// gml.getFillColorFromMoverColor(ns.getPlayerToMove(), ns,
			// wc.gameGraph);
			// else hsb = new float[]
			// {getHueFromGTV(ns.getPlayerToMove(),ns.getGameTheoreticValue(),ns),0f,0f};

			g.setColor(Color.BLACK);
			if (clickedAt == ns)
			{
				g.fillOval((int) (screenPos[0] - radius - 4),
						(int) (screenPos[1] - radius - 4), diameter + 8,
						diameter + 8);
				g.setColor(Color.getHSBColor(3f / 4f, 1f, 1f));
				g.fillOval((int) (screenPos[0] - radius - 2),
						(int) (screenPos[1] - radius - 2), diameter + 4,
						diameter + 4);
				g.setColor(Color.BLACK);
				g.fillOval((int) (screenPos[0] - radius),
						(int) (screenPos[1] - radius), diameter, diameter);
			} else if ((selectedInstances != null
					&& selectedInstances.contains(ns)))
				if (showTwoClasses)
					g.fillOval((int) (screenPos[0] - radius - 3),
							(int) (screenPos[1] - radius - 3), diameter + 6,
							diameter + 6);
				else
					g.fillOval((int) (screenPos[0] - radius - 2),
							(int) (screenPos[1] - radius - 2), diameter + 4,
							diameter + 4);
			else
				g.fillOval((int) (screenPos[0] - radius),
						(int) (screenPos[1] - radius), diameter, diameter);

			
			float ie = 0;
			if (blnShowRelationToSelected&&individualError!=null) {
				ie = DistanceError.getBrightness(individualError,ns,minIndividualError,maxIndividualError);
				
			}else {
				if (blnShowBright)
					ie = error.getBrightness(ns);
				else 
					ie = 1f;
			}
				
			
			float combined = brightness * ie;

			if (showTwoClasses)
			{
				g.setColor(Color.BLACK);
				g.fillOval((int) (screenPos[0] - radius - 1),
						(int) (screenPos[1] - radius - 1), diameter + 2,
						diameter + 2);

				if (showTopClassInside)
					g.setColor(colorAssignment1.getColor(ns, combined));
				else
					g.setColor(colorAssignment2.getColor(ns, combined));

				g.fillOval((int) (screenPos[0] - radius),
						(int) (screenPos[1] - radius), diameter, diameter);
				if (!showTopClassInside)
					g.setColor(colorAssignment1.getColor(ns, combined));
				else
					g.setColor(colorAssignment2.getColor(ns, combined));
				g.fillOval((int) (screenPos[0] - radius + 3),
						(int) (screenPos[1] - radius + 3), diameter - 6,
						diameter - 6);
			} else
			{
				Color c;
				if (!(blnShowDistanceToSelected&&clickedAt!=null)) {
					if (!blnShowClass)
						c = Color.getHSBColor(0.5f, 1f, combined);
					else
						c = colorAssignment1.getColor(ns, combined);
				}
				else {
					double d = wc.distanceMatrix.get(clickedAt, ns);
					c = ColorScheme.getDefault().getColorFromDistance(d);
					float[] hsb;
					hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
					
					c = Color.getHSBColor(hsb[0], hsb[1], hsb[2] * combined);
					
				}
				g.setColor(c);
				g.fillOval((int) (screenPos[0] - radius + 2),
						(int) (screenPos[1] - radius + 2), diameter - 4,
						diameter - 4);
			}
		}

	}

	private Double[] getMinMax(HashMap<LudRul, Double> individualError)
	{
		Double min = Double.valueOf(Integer.MAX_VALUE);
		Double max = Double.valueOf(Integer.MIN_VALUE);
		for (Double entry : individualError.values())
		{
			if (min.compareTo(entry)>0)min = entry;
			if (max.compareTo(entry)<0)max = entry;
		}
		return new Double[] {min,max};
	}

	private void squeezeP(Pos p)
	{
		if (p.y < 0)
			p.y = 0;
		if (p.x < 0)
			p.x = 0;
		if (p.z < 0)
			p.z = 0;

		if (p.y > wc.maxY)
			p.y = wc.maxY;
		if (p.x > wc.maxX)
			p.x = wc.maxX;
		if (p.z > wc.maxZ)
			p.z = wc.maxZ;
	}

	private void turnEveryPosAroundZAchsis(
			WrapperClass wc2, double angle, HashMap<LudRul, Pos> copy
	)
	{
		double centerX = wc2.maxX / 2.;
		double centerY = wc2.maxY / 2.;
		Collection<Pos> vals = copy.values();
		for (Pos pos : vals)
		{
			double rotatedX = Math.cos(angle) * (pos.x - centerX)
					- Math.sin(angle) * (pos.y - centerY) + centerX;
			double rotatedY = Math.sin(angle) * (pos.x - centerX)
					+ Math.cos(angle) * (pos.y - centerY) + centerY;
			pos.x = rotatedX;
			pos.y = rotatedY;
		}

	}

	private void turnEveryArroundXAchsis(
			WrapperClass wc2, double angle, HashMap<LudRul, Pos> copy
	)
	{
		double centerY = wc2.maxY / 2.;
		double centerZ = wc2.maxZ / 2.;
		Collection<Pos> vals = copy.values();
		for (Pos pos : vals)
		{
			double rotatedY = Math.cos(angle) * (pos.y - centerY)
					- Math.sin(angle) * (pos.z - centerZ) + centerY;
			double rotatedZ = Math.sin(angle) * (pos.y - centerY)
					+ Math.cos(angle) * (pos.z - centerZ) + centerZ;

			pos.y = rotatedY;
			pos.z = rotatedZ;
		}

	}

	private void drawGrids(Graphics g, DrawHelper drawHelper, WrapperClass wc2)
	{
		int layers = 10;
		int gridWidth = 80;
		double maxX = wc2.maxX;
		double maxY = wc2.maxY;
		double maxZ = wc2.maxZ;
		// drawLine(0,0,0,0,0,maxZ,g,Color.black,2,drawHelper);
		// drawLine(maxX,0,0,maxX,0,maxZ,g,Color.black,2,drawHelper);
		drawLine(0, maxY, 0, 0, maxY, maxZ, g, Color.black, 2, drawHelper);
		drawLine(maxX, maxY, 0, maxX, maxY, maxZ, g, Color.black, 2,
				drawHelper);
		for (int y = 0; y <= wc2.maxY; y = y + gridWidth)
		{
			drawLine(0, y, 0, 0, y, maxZ, g, Color.black, 2, drawHelper);
		}
		for (int x = 0; x <= wc2.maxX; x = x + gridWidth)
		{
			drawLine(x, maxY, 0, x, maxY, maxZ, g, Color.black, 2, drawHelper);
		}
		// drawLine(x,0,zValue,x,drawHelper.maxY,zValue,g,Color.black,2,drawHelper);
		// drawLine(x,0,zValue,x,drawHelper.maxY,zValue,g,Color.black,2,drawHelper);

		for (int i = layers; i >= layers; i = i - 1)
		{
			double zValue = maxZ - (maxZ * i) / layers;
			for (int x = 0; x <= wc2.maxX; x = x + gridWidth)
			{
				drawLine(x, 0, zValue, x, wc2.maxY, zValue, g, Color.black, 2,
						drawHelper);
			}
			for (int y = 0; y <= wc2.maxY; y = y + gridWidth)
			{
				drawLine(0, y, zValue, wc2.maxX, y, zValue, g, Color.black, 2,
						drawHelper);
			}
		}

	}

	private void drawLine(
			double x1, double y1, double z1, double x2, double y2, double z2,
			Graphics g, Color black, int width, DrawHelper drawHelper
	)
	{
		g.setColor(black);
		double[] s1 = drawHelper.getScreenPos(x1, y1, z1);
		double[] s2 = drawHelper.getScreenPos(x2, y2, z2);
		g.drawLine((int) s1[0], (int) s1[1], (int) s2[0], (int) s2[1]);

	}

	private void sortRespectiveToScreen(
			HashMap<LudRul, Pos> copy, ArrayList<LudRul> list
	)
	{
		Collections.sort(list, new Comparator<LudRul>()
		{

			@Override
			public int compare(LudRul o1, LudRul o2)
			{
				Pos p1 = copy.get(o1);
				Pos p2 = copy.get(o2);
				if (p1.y > p2.y)
					return -1;
				if (p1.y < p2.y)
					return 1;
				if (p1.z < p2.z)
					return -1;
				if (p1.z > p2.z)
					return 1;

				if (p1.x < p2.x)
					return -1;
				if (p1.x > p2.x)
					return 1;
				return 0;
			}
		});
	}

	public float getHue(Color c)
	{

		float red = c.getRed();
		float green = c.getGreen();
		float blue = c.getBlue();
		float min = Math.min(Math.min(red, green), blue);
		float max = Math.max(Math.max(red, green), blue);

		if (min == max)
		{
			return 0;
		}

		float hue = 0f;
		if (max == red)
		{
			hue = (green - blue) / (max - min);

		} else if (max == green)
		{
			hue = 2f + (blue - red) / (max - min);

		} else
		{
			hue = 4f + (red - green) / (max - min);
		}

		hue = hue * 60;
		if (hue < 0)
			hue = hue + 360;

		return Math.round(hue);
	}

	@Override
	public void setVisible(boolean bool)
	{
		frame.setVisible(bool);
	}

	public class DrawHelper
	{
		public double minX;
		int maxX;
		int maxY;
		int screenWidht;
		int screenHeight;
		private double angle;
		private double widthRatio;
		private double heightRatio;
		double minY;
		int maxZ;
		private double zRatio;

		// public int getTotalValuesX() {
		// return totalValuesX;
		// }

		// public int getTotalValuesY() {
		// return totalValuesY;
		// }

		public int getScreenWidht()
		{
			return screenWidht;
		}

		public int getScreenHeight()
		{
			return screenHeight;
		}

		public double getAngle()
		{
			return angle;
		}

		public double getWidthRatio()
		{
			return widthRatio;
		}

		public double getHeightRatio()
		{
			return heightRatio;
		}

		public DrawHelper invoke()
		{
			minX = 0;// boundsForTestFunction[0][0];
			minY = 0;// boundsForTestFunction[0][1];
			maxX = 400;// output.length;
			maxY = 400;// output[0].length;
			maxZ = 400;
			// double[] extrema = getExtrema(output);
			// double zDif = extrema[1]-extrema[0];
			screenWidht = ScatterPlotWindow.this.getWidth() - 20;
			screenHeight = ScatterPlotWindow.this.getHeight() - 20;
			angle = Math.asin(ScatterPlotWindow.this.getHeight() / 2. / maxY);// 1. /
																			// 4.
																			// *
			if (Double.isNaN(angle)||angle > Math.PI/2.0*0.95)
				angle = Math.PI/2.0*0.95;
			
			double something = maxY * Math.cos(angle);
			Math.sin(angle);
			widthRatio = (screenWidht * 1.) / (maxX + something);
			heightRatio = (screenHeight / 4.) / (maxY);
			zRatio = 3. * screenHeight / 4. / maxZ;
			return this;
		}

		public double maxRecomendedZ()
		{
			double zUp = getScreenPos(maxX, maxY, 0)[1];
			return zUp - 100;
		}

		double[] getScreenPos(double x, double y, double z)
		{
			double xScreen = (x) * widthRatio
					+ Math.cos(angle) * y * widthRatio;
			double yScreen = screenHeight - Math.sin(angle) * (y) * heightRatio;
			yScreen -= z * zRatio;
			return new double[] { xScreen + 10, yScreen - 10 };
		}
	}

	public class BiHashMap<K1, K2, V>
	{

		private final Map<K1, Map<K2, V>> mMap;

		public BiHashMap()
		{
			mMap = new HashMap<K1, Map<K2, V>>();
		}

		/**
		 * Associates the specified value with the specified keys in this map
		 * (optional operation). If the map previously contained a mapping for
		 * the key, the old value is replaced by the specified value.
		 *
		 * @param key1  the first key
		 * @param key2  the second key
		 * @param value the value to be set
		 * @return the value previously associated with (key1,key2), or
		 *         <code>null</code> if none
		 * @see Map#put(Object, Object)
		 */
		public V put(K1 key1, K2 key2, V value)
		{
			Map<K2, V> map;
			if (mMap.containsKey(key1))
			{
				map = mMap.get(key1);
			} else
			{
				map = new HashMap<K2, V>();
				mMap.put(key1, map);
			}

			return map.put(key2, value);
		}

		/**
		 * Returns the value to which the specified key is mapped, or
		 * <code>null</code> if this map contains no mapping for the key.
		 *
		 * @param key1 the first key whose associated value is to be returned
		 * @param key2 the second key whose associated value is to be returned
		 * @return the value to which the specified key is mapped, or
		 *         <code>null</code> if this map contains no mapping for the key
		 * @see Map#get(Object)
		 */
		public V get(K1 key1, K2 key2)
		{
			if (mMap.containsKey(key1))
			{
				return mMap.get(key1).get(key2);
			} else
			{
				return null;
			}
		}

		/**
		 * Returns <code>true</code> if this map contains a mapping for the
		 * specified key
		 *
		 * @param key1 the first key whose presence in this map is to be tested
		 * @param key2 the second key whose presence in this map is to be tested
		 * @return Returns true if this map contains a mapping for the specified
		 *         key
		 * @see Map#containsKey(Object)
		 */
		public boolean containsKeys(K1 key1, K2 key2)
		{
			return mMap.containsKey(key1) && mMap.get(key1).containsKey(key2);
		}

		public void clear()
		{
			mMap.clear();
		}

	}

	public LudRul getClickedAt()
	{
		return clickedAt;
	}
}
