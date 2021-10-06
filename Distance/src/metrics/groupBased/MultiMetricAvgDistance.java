package metrics.groupBased;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import common.DistanceMatrix;
import common.DistanceUtils;
import common.EvaluatorDistanceMetric;
import common.LudRul;
import common.Score;
import game.Game;
import metrics.DistanceMetric;
import metrics.GroupBased;
import metrics.individual.CosineSimilarity;
import metrics.individual.FeatureDistance;
import metrics.individual.JensenShannonDivergence;
import metrics.support.DistanceProgressListener;

/**
 * Uses the avg distance of different metrics and uses the avg distance
 * 
 * @author Markus
 *
 */
public class MultiMetricAvgDistance implements DistanceMetric, GroupBased
{

	private String name;
	private boolean initialized = false;
	private DistanceMatrix<LudRul, LudRul> combined;
	private ArrayList<DistanceMetric> metrics;

	/**
	 * 
	 * @param candidates the games (with individualRulesets)
	 * @param metrics    The different metrices to be used
	 */
	public MultiMetricAvgDistance(
			final ArrayList<LudRul> candidates,
			final ArrayList<DistanceMetric> metrics
	)
	{
		this.metrics = metrics;
		combined = new DistanceMatrix<LudRul, LudRul>(candidates, candidates);
		this.name = createName(metrics);
		calculate(candidates, null);
	}

	/**
	 * Uses a default combination of metrices, which worked well
	 * 
	 * @param candidates the games (with individualRulesets)
	 */
	public MultiMetricAvgDistance(final ArrayList<LudRul> candidates)
	{
		combined = new DistanceMatrix<LudRul, LudRul>(candidates, candidates);
		this.metrics = getBestMetrics();
		this.name = createName(metrics);
		calculate(candidates, null);

	}

	private ArrayList<DistanceMetric> getBestMetrics()
	{
		ArrayList<DistanceMetric> metricsList = new ArrayList<>();
		metricsList.add(new JensenShannonDivergence());
		metricsList.add(new CosineSimilarity());
		metricsList.add(EndConditionLudemeSuffixTree.createPlaceHolder());
		metricsList.add(new FeatureDistance());
		

		return metricsList;
	}

	private MultiMetricAvgDistance()
	{
		// placeholder which defines the metrices

		metrics = getBestMetrics();
		this.name = createName(metrics);
	}

	public static MultiMetricAvgDistance createInstance(
			ArrayList<DistanceMetric> metricsList
	)
	{
		MultiMetricAvgDistance metric = new MultiMetricAvgDistance();
		metric.metrics.clear();
		metric.metrics.addAll(metricsList);
		metric.name = createName(metric.metrics);

		return metric;
	}

	@Override
	public boolean typeNeedsToBeInitialized()
	{
		return true;
	}

	/**
	 * process the data
	 * 
	 * @param candidates
	 * @param metrics
	 * @param dpl
	 */
	private void calculate(
			final ArrayList<LudRul> candidates, DistanceProgressListener dpl
	)
	{
		final ArrayList<DistanceMatrix<LudRul, LudRul>> matrices = new ArrayList<>();
		for (final DistanceMetric distanceMetric : metrics)
		{
			final DistanceMatrix<LudRul, LudRul> distanceMatrix = EvaluatorDistanceMetric
					.getDistanceMatrix(candidates, distanceMetric, false, dpl);
			matrices.add(distanceMatrix);
		}
		final double[][] sum = new double[candidates.size()][candidates.size()];
		for (final DistanceMatrix<LudRul, LudRul> distanceMatrix : matrices)
		{
			final double[][] dm = distanceMatrix.getDistanceMatrix();
			for (int i = 0; i < dm.length; i++)
			{
				final double[] dmline = dm[i];
				for (int j = 0; j < dmline.length; j++)
				{
					sum[i][j] += dmline[j] / matrices.size();
				}
			}
		}

		// overwrite the raw data of the combined distance matrix for speed
		// reasons
		combined = new DistanceMatrix<>(candidates, candidates);
		final double[][] cDM = combined.getDistanceMatrix();
		for (int i = 0; i < sum.length; i++)
		{
			System.arraycopy(sum[i], 0, cDM[i], 0, sum[i].length);
		}
		initialized = true;
	}

	/**
	 * Name the instance, mainly used for storage reasons
	 * 
	 * @param metrics
	 * @return A string starting with "MultiClassifier" containing the names of
	 *         the different distance metrics
	 */
	private static String createName(final ArrayList<DistanceMetric> metrics)
	{
		String named = "MultiClassifier";
		for (final DistanceMetric metric : metrics)
		{
			named += "_" + metric.getName();
		}
		return named;
	}

	@Override
	public Score distance(final Game gameA, final Game gameB)
	{
		// TODO Auto-generated method stub doesnt really apply
		return null;
	}

	@Override
	public Score distance(final LudRul gameA, final LudRul gameB)
	{
		return new Score(combined.get(gameA, gameB));
	}

	@Override
	public Score distance(
			final Game gameA, final List<Game> gameB, final int numberTrials,
			final int maxTurns, final double thinkTime, final String AIName
	)
	{
		// TODO Auto-generated method stub doesnt really apply
		return null;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Score distance(final String description1, final String description2)
	{
		return null;
	}

	@Override
	public DistanceMetric getDefaultInstance()
	{
		return new MultiMetricAvgDistance();
	}

	@Override
	public boolean hasUserSelectionDialog()
	{
		return true;
	}

	@Override
	public boolean isInitialized(ArrayList<LudRul> candidates)
	{
		if (!initialized)
			return false;
		Set<LudRul> ks = combined.getCandidateToIndex().keySet();
		if (ks.size() != candidates.size())
			return false;
		if (!ks.containsAll(candidates))
			return false;

		return true;
	}

	@Override
	public void init(
			final ArrayList<LudRul> candidates,boolean forceRecalculation,
			final DistanceProgressListener dpl
	)
	{
		for (DistanceMetric metric : metrics)
		{
			if (!metric.isInitialized(candidates)||forceRecalculation)
				metric.init(candidates,forceRecalculation, dpl);
		}
		calculate(candidates, dpl);
	}

	@Override
	public DistanceMetric getPlaceHolder()
	{
		return createPlaceHolder();
	}

	public static DistanceMetric createPlaceHolder()
	{
		return new MultiMetricAvgDistance();
	}

	/**
	 * Many distance metrices can have multiple settings. This returns a
	 * suggestion. (if the metric does not have multiple settings it just
	 * returns the instance).
	 * 
	 * @return a userSelected instance of this distance metric
	 */
	@Override
	public DistanceMetric showUserSelectionDialog()
	{

		JPanel dialogPanel = new JPanel();

		final List<DistanceMetric> metricsWithoutPre = DistanceUtils
				.getAllDistanceMetricesWithoutPreprocessing();
		final List<DistanceMetric> metricsWithPre = DistanceUtils
				.getAllDistanceMetricesWithPreprocessing();
		final List<DistanceMetric> possibleMetrics = new ArrayList<DistanceMetric>();

		possibleMetrics.addAll(metricsWithoutPre);
		possibleMetrics.addAll(metricsWithPre);
		DefaultListModel<DistanceMetric> choiseModel = new DefaultListModel<DistanceMetric>();
		for (DistanceMetric distanceMetric : possibleMetrics)
		{
			choiseModel.addElement(distanceMetric);
		}
		final JList<DistanceMetric> choiseList = new JList<DistanceMetric>(
				choiseModel);
		final JScrollPane scrollPane = new JScrollPane();
		choiseList.setLayoutOrientation(JList.VERTICAL);
		scrollPane.setViewportView(choiseList);
		dialogPanel.add(scrollPane, BorderLayout.WEST);

		DefaultListModel<DistanceMetric> selectedModel = new DefaultListModel<DistanceMetric>();
		final JList<DistanceMetric> selectedList = new JList<DistanceMetric>(
				selectedModel);
		final JScrollPane scrollPaneSelectiong = new JScrollPane();
		selectedList.setLayoutOrientation(JList.VERTICAL);
		scrollPaneSelectiong.setViewportView(selectedList);
		dialogPanel.add(scrollPaneSelectiong, BorderLayout.EAST);

		MetricRenderer lr = new MetricRenderer();
		choiseList.setCellRenderer(lr);
		selectedList.setCellRenderer(lr);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton addButton = new JButton("Add");
		JButton removeButton = new JButton("Remove");
		buttonPanel.add(addButton);
		buttonPanel.add(removeButton);

		addButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				final int[] indices = choiseList.getSelectedIndices();
				Arrays.sort(indices); // probably not necessary, but wont hurt
				for (int index = 0; index < indices.length; index++)
				{
					DistanceMetric seomthing = choiseModel.get(indices[index]);
					if (seomthing.hasUserSelectionDialog())
					{
						DistanceMetric dm = null;
						while (dm == null)
						{
							dm = seomthing.showUserSelectionDialog();
						}
						selectedModel.addElement(dm);
					} else
					{
						selectedModel
								.addElement(seomthing.getDefaultInstance());
					}

				}

			}
		});

		removeButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				final int[] indices = selectedList.getSelectedIndices();
				Arrays.sort(indices); // probably not necessary, but wont hurt
				SwingUtilities.invokeLater(new Runnable()
				{

					@Override
					public void run()
					{
						for (int index = indices.length
								- 1; index >= 0; index--)
						{
							selectedModel.remove(indices[index]);

						}
					}
				});
			}
		});

		final Object[] message = { dialogPanel, buttonPanel };
		int returnValue = JOptionPane.showConfirmDialog(null, message,
				"Select Metrices", JOptionPane.OK_CANCEL_OPTION);
		if (returnValue != JOptionPane.OK_OPTION)
			return null;

		ArrayList<DistanceMetric> metricsList = new ArrayList<>();
		for (int i = 0; i < selectedModel.size(); i++)
		{
			metricsList.add(selectedModel.get(i));
		}

		return MultiMetricAvgDistance.createInstance(metricsList);

	}

	private class MetricRenderer extends DefaultListCellRenderer
	{
		/**
		* 
		*/
		private static final long serialVersionUID = 1L;

		/** Creates a new instance of LocaleRenderer */
		public MetricRenderer()
		{
		}

		@Override
		public Component getListCellRendererComponent(
				JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus
		)
		{
			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			DistanceMetric l = (DistanceMetric) value;
			setText(l.getName());
			return this;
		}
	}
}
