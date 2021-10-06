package processing.visualisation_3d_scatter_plot;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import common.DistanceMatrix;
import common.LudRul;
import processing.kmedoid.LineDrawable;
import processing.kmedoid.LineDrawer;
import processing.visualisation_3d_scatter_plot.ScatterPlotWindow.DrawHelper;


public class WrapperClass
{
	private static final int autoOptimizeRate = 50;
	private static final int TextUpdateCounterRate = 100;
	private static final int MinimumHistory = 200;
	private static final int MaximumHistory = 4000;
	
	boolean force2d = false;
	ScatterPlotWindow parentWindow;
	DistanceMatrix<LudRul, LudRul> distanceMatrix;
	double maxX;
	double minX;
	double maxZ;
	double maxY;
	double minY;
	HashMap<LudRul, Pos> possAsignment = new HashMap<>();
	DistanceError currentError;
	
	
	double[][] dm;
	private double diameter;
	final double divisionFactor = 8.0;
	private final Random random;
	ArrayList<LudRul> nodes;
	boolean anneahling;

	boolean improveSelected;
	boolean showProgressGraph = true;

	
	boolean autoOptimize = true;
	
	
	public WrapperClassData optimisationHistory = new WrapperClassData(10, 1,
			new LinkedList<>(), 0, new LinkedList<>(), 0, new LinkedList<>(), 0, new LinkedList<>(),
			0, 0, 0, 0, 0);
	private boolean useSquareEvaluation = false;
	
	JFrame progressGraph = new JFrame();
	private double spaceDiagonal;
	protected boolean closeProcess = false;
	private final boolean emphasiseCloseOverDistant = false; //gives difference, but not enough to justify button

	public WrapperClass(
			DistanceMatrix<LudRul, LudRul> distanceMatrix,
			DrawHelper drawHelper, ScatterPlotWindow sw
	)
	{
		random = new Random(299999L);
		this.distanceMatrix = distanceMatrix;
		nodes = new ArrayList<LudRul>(
				distanceMatrix.getIndexToCandidate());
		parentWindow = sw;

		dm = distanceMatrix.getDistanceMatrix();
		diameter = 0; // by definition 1
		for (final double[] ds : dm)
		{
			for (final double d : ds)
			{
				if (d > diameter)
					diameter = d;
			}
		}

		maxX = drawHelper.maxX;
		minX = drawHelper.minX;
		minY = drawHelper.minY;
		maxY = drawHelper.maxY;
		maxZ = drawHelper.maxZ;
		spaceDiagonal = Math.sqrt(maxY + maxX + maxZ);
		if (force2d) spaceDiagonal = Math.sqrt(maxZ + maxX);

		initialise(possAsignment, nodes);
		
	}

	public HashMap<LudRul, Pos> getPosAssignment()
	{
		return possAsignment;
	}

	private void initialise(
			HashMap<LudRul, Pos> possAsignment2, ArrayList<LudRul> gameList
	)
	{
		possAsignment2.clear();

		for (final LudRul game : gameList)
		{
			final Pos p = getRandomPos(game);
			possAsignment2.put(game, p);
		}

	}

	private Pos getRandomPos(LudRul nodeState)
	{
		final double zValue = maxZ * random.nextDouble();
		final double xValue = maxX * random.nextDouble();
		double yValue = maxY * random.nextDouble();
		if (force2d) yValue = maxY/2.;
		return new Pos(xValue, yValue, zValue);
	}

	
	public HashMap<LudRul,Double> getCostToRest(LudRul game1, HashMap<LudRul,Pos> pos) {
		final ArrayList<Entry<Double, LudRul>> sorted = distanceMatrix.getSortedDistances(game1);
		final HashMap<LudRul,Double> costMap = new HashMap<>(pos.size());
		final Pos pos1 = pos.get(game1);
		for (final Entry<Double, LudRul> entry : sorted)
		{
			final LudRul game2 = entry.getValue();
			final Pos pos2 = pos.get(game2);
			
			final double cost = costBetween2(entry.getKey().doubleValue(), pos1, pos2);
			costMap.put(game2, Double.valueOf(cost));
		}
		
		return costMap;
	}
	

	public DistanceError calculateErrorCostSimple(
			ArrayList<LudRul> games, HashMap<LudRul, Pos> pa, double[][] dm2
	)
	{
		final int n = games.size();
		final double[] error = new double[n];	
		double totalCost = 0.;
	
		for (int i = 0; i < n; i++)
		{
			final LudRul n1 = games.get(i);
			final Pos p1 = pa.get(n1);

			for (int j = i + 1; j < n; j++)
			{
				final LudRul n2 = games.get(j);
				final Pos p2 = pa.get(n2);
				
				final double distanceInGraph = dm2[i][j];
				
				final double simpleCost = costBetween2(distanceInGraph, 
						 p1, p2);
				
				error[i] += simpleCost / 2;
				error[j] += simpleCost / 2;
				totalCost += (simpleCost);
			}

		}
		return new DistanceError(totalCost, games, error);
	}

	private double costBetween2(
			double distanceInGraph,
			Pos p1, Pos p2
	)
	{
		
		final double distanceReal = distanceInGraph/diameter;
		final double distanceShown = Pos.getDistanceEuclid(p1, p2)
				/ spaceDiagonal; // classic mode
		double simpleCost = 0;
		
		double dif = Math.abs(distanceShown / divisionFactor  - distanceReal); // classicmode
		if (emphasiseCloseOverDistant)
			dif = dif/Math.max(distanceInGraph,0.05);
		if (useSquareEvaluation)
			simpleCost += dif * dif ;
		else
			simpleCost += dif ;
		return simpleCost;
	}

	public static HashMap<LudRul, Pos> copyPossAssignment(
			HashMap<LudRul, Pos> possAsignment2
	)
	{
		final HashMap<LudRul, Pos> hm = new HashMap<LudRul, WrapperClass.Pos>(
				possAsignment2.size());
		final Set<Entry<LudRul, Pos>> entries = possAsignment2.entrySet();
		for (final Entry<LudRul, Pos> entry : entries)
		{
			hm.put(entry.getKey(), entry.getValue().copy());
		}
		return hm;
	}


	public static class Pos implements Serializable
	{
		private static final long serialVersionUID = 1L;
		double x;
		double y;
		double z;

		public Pos(double x2, double y2, double z2)
		{
			x = x2;
			y = y2;
			z = z2;
		}

		public static double getDistanceEuclidWrap(
				Pos p1, Pos p2, double maxX, double maxY, double maxZ
		)
		{

			final double dx = dxMin(p1.x, p2.x, maxX);
			final double dy = dxMin(p1.y, p2.y, maxY);
			final double dz = dxMin(p1.z, p2.z, maxZ);

			return Math.sqrt(dx * dx + dy * dy + dz * dz);
		}

		private static double dxMin(double x1, double x2, double maxX)
		{
			final double val = Math.abs(x1 - x2);
			final double val1 = Math.abs(x1 + maxX - x2);
			final double val2 = Math.abs(x1 - x2 - maxX);
			return Math.min(val, Math.min(val1, val2));
		}

		public static double getDistanceXY(Pos p1, Pos p2, double bigDiameter)
		{
			final double something = Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
			return something / bigDiameter;
		}

		public static double getDistanceManhatten(Pos p1, Pos p2)
		{
			final double something = Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y)
					+ Math.abs(p1.z - p2.z);
			return something;
		}

		public static double getDistanceEuclid(Pos p1, Pos p2)
		{
			final double something = (p1.x - p2.x) * (p1.x - p2.x)
					+ (p1.y - p2.y) * (p1.y - p2.y)
					+ (p1.z - p2.z) * (p1.z - p2.z);

			return Math.sqrt(something);
		}

		public Pos()
		{

		}

		public Pos copy()
		{

			return new Pos(x, y, z);
		}

	}

	public ArrayList<LudRul> getNodes()
	{

		return nodes;
	}

	public void resetPoints()
	{
		initialise(possAsignment, nodes);
		currentError = calculateErrorCostSimple(nodes, optimisationHistory.getBestPossition(), dm);

		overWritePossAssignment(possAsignment, optimisationHistory.getBestPossition());
	}

	public void startAnneahling()
	{
		anneahling = true;
		optimisationHistory.setBestPossition(getPosAssignment());

		final Thread t = new Thread()
		{

			private final Random random = new Random();
			ArrayList<LineDrawable> bests = new ArrayList<>();
			private LineDrawer currentLineDrawer = new LineDrawer(
					new ArrayList<>());
			JTextArea textLabel = new JTextArea("");
			
			protected JFrame setUpProgressGraph()
			{
				
				progressGraph.add(currentLineDrawer, BorderLayout.CENTER);
				progressGraph.setTitle("Progress Graph");
				progressGraph.add(textLabel, BorderLayout.SOUTH);
				progressGraph.setPreferredSize(new Dimension(500, 300));
				progressGraph.pack();
				progressGraph.setVisible(true);
				return progressGraph;
			}
			
			@Override
			public void run()
			{
				setUpProgressGraph();

				currentError = calculateErrorCostSimple(nodes, optimisationHistory.getBestPossition(), dm);

				overWritePossAssignment(possAsignment, optimisationHistory.getBestPossition());
				textLabel.setEditable(false);
				int counter = 0;
				while (!closeProcess )
				{
					try
					{
						Thread.sleep((long) (1.));
					} catch (final InterruptedException e)
					{
						e.printStackTrace();
					}
					if (!anneahling)
					{
						try
						{
							parentWindow.updateTextPanel(counter,currentError);
							Thread.sleep((long) (300.));
						} catch (final InterruptedException e)
						{
							e.printStackTrace();
						}
						continue;
					}

					counter++;

					// copy
					final HashMap<LudRul, Pos> candidate = copyPossAssignment(optimisationHistory.getBestPossition());
					// generateCandidate
					final boolean b = random.nextBoolean();
					if (b)
					{
						modifyReplaceSingle(candidate);
					} else
						modifyShiftAll(candidate);

					final DistanceError errorCost = calculateErrorCostSimple(nodes,
							candidate, dm);
					final double candidateCost = errorCost.totalCost;
					double bestCost = currentError.totalCost;
					final double chance = Math
							.exp(-(candidateCost - bestCost) / optimisationHistory.getTemperature());

					// chance = 0;
					final double r = Math.random();
					final boolean improvement = candidateCost < bestCost;
					final boolean acceptedByChance = r < chance;
					if (improvement || (acceptedByChance && b))
					{
						bestCost = candidateCost;
						overWritePossAssignment(candidate, optimisationHistory.getBestPossition());
						currentError = errorCost;
					}

					final LineDrawable currentBest = LineDrawable
							.getLineDrawable(counter, currentError);
					
					final int maximumHistory = MaximumHistory;
					final int minimumHistory = MinimumHistory;
					if (bests.size() > maximumHistory)
					{
						final ArrayList<LineDrawable> newbest = new ArrayList<>(
								bests);
						newbest.subList(0, newbest.size() - maximumHistory)
								.clear();
						bests = newbest;
					}
					bests.add(currentBest);
					
					
					updateAcceptenceRates(improvement, acceptedByChance, bests,
							maximumHistory, minimumHistory);
					
					if (counter % TextUpdateCounterRate == 0)
						updateProgressText(counter, candidateCost, bestCost,
								chance);

					if (autoOptimize && counter % autoOptimizeRate == 0)
						autoOptimize();
					parentWindow.updateTextPanel(counter,currentError);
				}
				closeProcess();
			}

			private void closeProcess()
			{
				progressGraph.setVisible(false);
				progressGraph.dispose();
				progressGraph=null;
			}

			private void updateProgressText(
					int counter, double candidateCost, double bestCost,
					double chance
			)
			{
				final String text = createOutPutText(counter,bestCost,candidateCost,chance);
				textLabel.setText(text);
				textLabel.repaint();
				if (showProgressGraph)
				{
					progressGraph.remove(currentLineDrawer);

					currentLineDrawer = new LineDrawer(bests);
					progressGraph.setPreferredSize(progressGraph.getSize());
					progressGraph.add(currentLineDrawer);
					progressGraph.pack();
				}
			}


			private void modifyReplaceSingle(
					HashMap<LudRul, Pos> candidate
			)
			{
				final int l = (int) Math.floor(Math.random() * candidate.size());

				final LudRul cl = parentWindow.getClickedAt();

				final ArrayList<Entry<LudRul, Float>> errors = new ArrayList<>(
						currentError.errorMap.entrySet());

				LudRul node;
				if (cl != null && improveSelected)
				{
					node = cl;
				} else {
					if (errors.size()<=l)return; //a load has happened 
					node = errors.get(l).getKey();
				}		

				modifyReplaceSingle(candidate, node);

			}

			private void modifyReplaceSingle(
					HashMap<LudRul, Pos> candidate, LudRul node
			)
			{
				final Pos pos = candidate.get(node);
				pos.x = Math.random() * maxX;
				if (force2d) pos.y = maxY/2.;
				else pos.y = Math.random() * maxY;
				pos.z = Math.random() * maxZ;
			}

			private void modifyShiftAll(
					HashMap<LudRul, Pos> candidate
			)
			{
				for (final LudRul node : nodes)
				{
					final Pos pos = candidate.get(node);
					pos.x += random.nextGaussian() * optimisationHistory.getStep();
					pos.z += random.nextGaussian() * optimisationHistory.getStep();
					pos.x = Math.max(0, pos.x);
					pos.z = Math.max(0, pos.z);
					pos.x = Math.min(maxX, pos.x);
					pos.z = Math.min(maxZ, pos.z);
					
					if (force2d) pos.y = maxY/2.;
					else {
						pos.y += random.nextGaussian() * optimisationHistory.getStep();
						pos.y = Math.max(0, pos.y);
						pos.y = Math.min(maxY, pos.y);
					}
					
				}
			}
		};
		t.start();
	}

	

	

	protected String createOutPutText(int counter, double bestCost, double candidateCost, double chance)
	{
		final NumberFormat formatter = new DecimalFormat("#0.000");
		final String text = counter + " stepsize:" + formatter.format(optimisationHistory.getStep())
				+ " temp:" + formatter.format(optimisationHistory.getTemperature()) + " bestCost: "
				+ formatter.format(bestCost) + " \ncurrentCandidateCost: "
				+ formatter.format(candidateCost) + " Acceptence Chance: "
				+ formatter.format(chance)
				+ "\n LongTerm acceptensRate: "
				+ formatter
						.format(optimisationHistory.getLongTermAcceptensRate())
				+ " improvementRate: "
				+ formatter.format(optimisationHistory.getLongTermImprovementRate())
				+ " avgImprovement: " + formatter.format(optimisationHistory.getLongTermAvgImprovemt())
				+ "\n ShortTerm acceptensRate: "
				+ formatter.format(optimisationHistory.getShortTermAcceptensRate())
				+ " improvementRate: "
				+ formatter.format(optimisationHistory.getShortTermImprovementRate())
				+ " avgImprovement: " + formatter.format(optimisationHistory.getShortTermAvgImprovemt());
		return text;
	}

	protected void autoOptimize()
	{
		if (optimisationHistory.getShortTermImprovementRate() < 0.1)
		{
			optimisationHistory.setStep(optimisationHistory.getStep() * 0.99);
		}
		if (optimisationHistory.getShortTermImprovementRate() > 0.2)
		{
			optimisationHistory.setStep(optimisationHistory.getStep() * 1.01);
		}
		if (optimisationHistory.getShortTermAcceptensRate() > optimisationHistory.getShortTermImprovementRate() * 1.4)
		{
			optimisationHistory
					.setTemperature(optimisationHistory.getTemperature() * 0.99);
		}
		if (optimisationHistory.getShortTermAcceptensRate() < optimisationHistory.getShortTermImprovementRate() * 1.2)
		{
			optimisationHistory
					.setTemperature(optimisationHistory.getTemperature() * 1.01);
		}

	}

	protected void updateAcceptenceRates(
			boolean improvement, boolean chanceAccept,
			ArrayList<LineDrawable> bests, int longTermHistory,
			int shortTermHistory
	)
	{
		final double first = bests.get(0).getY();
		final double last = bests.get(bests.size() - 1).getY();
		optimisationHistory.setLongTermAvgImprovemt((first - last) / longTermHistory);

		optimisationHistory.getImprovementsLongTerm().add(Boolean.valueOf(improvement));
		if (improvement)
			optimisationHistory.setImprovementsLongTermCounter(
					optimisationHistory.getImprovementsLongTermCounter() + 1);
		if (optimisationHistory.getImprovementsLongTerm().size() > longTermHistory)
		{
			if (optimisationHistory.getImprovementsLongTerm().getFirst().booleanValue())
				optimisationHistory.setImprovementsLongTermCounter(
						optimisationHistory.getImprovementsLongTermCounter()
								- 1);
			optimisationHistory.getImprovementsLongTerm().removeFirst();
		}

		optimisationHistory.getAcceptedsLongTerm().add(Boolean.valueOf(chanceAccept));

		if (chanceAccept)
			optimisationHistory.setAcceptedsLongTermCounter(
					optimisationHistory.getAcceptedsLongTermCounter() + 1);
		if (optimisationHistory.getAcceptedsLongTerm().size() > longTermHistory)
		{
			optimisationHistory.getAcceptedsLongTerm().removeFirst();
			if (optimisationHistory.getAcceptedsLongTerm().getFirst().booleanValue())
				optimisationHistory.setAcceptedsLongTermCounter(
						optimisationHistory.getAcceptedsLongTermCounter() - 1);
		}

		final double firstShort = bests
				.get(Math.max(bests.size() - 1 - shortTermHistory, 0)).getY();
		final double lastShort = last;
		optimisationHistory.setShortTermAvgImprovemt((firstShort - lastShort) / shortTermHistory);

		optimisationHistory.getImprovementsShortTerm().add(Boolean.valueOf(improvement));
		if (improvement)
			optimisationHistory.setImprovementsShortTermCounter(
					optimisationHistory.getImprovementsShortTermCounter() + 1);
		if (optimisationHistory.getImprovementsShortTerm().size() > shortTermHistory)
		{
			if (optimisationHistory.getImprovementsShortTerm().getFirst().booleanValue())
				optimisationHistory.setImprovementsShortTermCounter(
						optimisationHistory.getImprovementsShortTermCounter()
								- 1);
			optimisationHistory.getImprovementsShortTerm().removeFirst();
		}

		optimisationHistory.getAcceptedsShortTerm().add(Boolean.valueOf(chanceAccept));

		if (chanceAccept)
			optimisationHistory.setAcceptedsShortTermCounter(
					optimisationHistory.getAcceptedsShortTermCounter() + 1);
		if (optimisationHistory.getAcceptedsShortTerm().size() > shortTermHistory)
		{
			optimisationHistory.getAcceptedsShortTerm().removeFirst();
			if (optimisationHistory.getAcceptedsShortTerm().getFirst().booleanValue())
				optimisationHistory.setAcceptedsShortTermCounter(
						optimisationHistory.getAcceptedsShortTermCounter() - 1);
		}

		optimisationHistory.setLongTermAcceptensRate((optimisationHistory.getAcceptedsLongTermCounter() * 1.0)
				/ longTermHistory);
		optimisationHistory.setLongTermImprovementRate((optimisationHistory.getImprovementsLongTermCounter() * 1.0)
				/ longTermHistory);
		optimisationHistory.setLongTermAvgImprovemt(
				optimisationHistory.getLongTermAvgImprovemt() + 0);

		optimisationHistory.setShortTermAcceptensRate((optimisationHistory.getAcceptedsShortTermCounter() * 1.0)
				/ shortTermHistory);
		optimisationHistory.setShortTermImprovementRate((optimisationHistory.getImprovementsShortTermCounter() * 1.0)
				/ shortTermHistory);
		optimisationHistory.setShortTermAvgImprovemt(
				optimisationHistory.getShortTermAvgImprovemt() + 0);

	}

	void overWritePossAssignment(
			HashMap<LudRul, Pos> candidate, HashMap<LudRul, Pos> toOverwrite
	)
	{
		for (final Entry<LudRul, Pos> is : candidate.entrySet())
		{
			toOverwrite.put(is.getKey(), is.getValue());
		}
	}

	public DistanceError getCurrentError()
	{
		return currentError;
	}

	public void multiplyTemp(double d)
	{
		optimisationHistory
				.setTemperature(optimisationHistory.getTemperature() * d);

	}

	public void multiplyStep(double d)
	{
		optimisationHistory.setStep(optimisationHistory.getStep() * d);

	}

	public void setSimAnnealing(boolean selected)
	{
		anneahling = selected;

	}

	public void setImproveSelected(boolean improveSelected)
	{
		this.improveSelected = improveSelected;

	}

	public void overridePosAssignment(HashMap<LudRul, Pos> posAssignment)
	{
		if (!posAssignment.keySet().containsAll(possAsignment.keySet()))
			return;
		overWritePossAssignment(posAssignment, possAsignment);
		overWritePossAssignment(posAssignment, optimisationHistory.getBestPossition());
		final DistanceError currentErrorNew = calculateErrorCostSimple(nodes, optimisationHistory.getBestPossition(),
				dm);
		currentError = currentErrorNew;
	}

	public void setProgressGraph(boolean selected)
	{
		showProgressGraph = selected;
		progressGraph.setVisible(selected);
	}

	public void setAutoOptimize(boolean selected)
	{
		autoOptimize = selected;

	}

	public void setSimAnnealingSquareEvaluationFunction(boolean selected)
	{
		useSquareEvaluation = selected;
		
	}

	public void setClose(boolean b)
	{
		closeProcess = b;
		
	}

}
