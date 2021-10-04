package common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import game.Game;
import main.FileHandling;
import metrics.DistanceMetric;
import metrics.individual.ZhangShasha;
import metrics.support.DistanceProgressListener;
import processing.grammarGMLComposer.GraphComposerGrammarToken;
import processing.kmedoid.ClusteringVisualiser;
import processing.kmedoid.KmedoidClustering;
import processing.similarity_matrix.AssignmentSettings;
import processing.similarity_matrix.Visualiser;
import processing.visualisation_3d_scatter_plot.ScatterPlotWindow;

/**
 * Dialog that is used to display various game distance options Copy of
 * PlayerDesctop/src/app/display/dialog/DistanceDialog.java and then modified
 * 
 * @author Matthew.Stephenson
 * @author Markus
 */
public class DistanceDialog extends JDialog
{
	final int defaultDismissTimeout = ToolTipManager.sharedInstance()
			.getDismissDelay();
	private static final long serialVersionUID = 1L;
	protected final int knearestNeighbourDefault = 3;
	protected final int knearestNeighbourNoiseDefault = 3;

	protected final int minKDefault = 1;
	protected final int maxKDefault = 12;

	JTextField textFieldThinkTime;
	DistanceMetric distanceMetric = new ZhangShasha();
	final DefaultListModel<LudRul> model;
	final JProgressBar jProgressBar;
	private Cursor defaultCursor;
	ArrayList<AbstractButton> allButtons = new ArrayList<>();

	ArrayList<SwingWorker<Void, Void>> sw = new ArrayList<>();
	final JCheckBox forceRecalculationDistanceMatrixCheckbox;
	final JCheckBox forceRecalculationGamesCheckbox;
	final JLabel lblChosenGames; // chosen games label
	private final JButton btnInstantiateMetric;
	private final JButton btnAlternativeMetric;
	private final JLabel lblDistanceAlgorihtm;

	public static void main(final String[] args)
	{
		showDialog();
	}

	// -------------------------------------------------------------------------

	/**
	 * Show the Dialog.
	 */
	public static void showDialog()
	{
		try
		{
			final DistanceDialog dialog = new DistanceDialog();

			// DialogUtil.initialiseSingletonDialog(dialog, "Game Distance",
			// null);
			dialog.setVisible(true);
			// System.out.println(dialog.isVisible());
			dialog.addLudFolderGames();
		} catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	private void addLudFolderGames()
	{
		addToSelectedFiles(new File[] { FolderLocations.ludFolder }, model);

		this.repaint();
		revalidate();
	}

	// -------------------------------------------------------------------------

	/**
	 * Create the dialog.
	 */
	public DistanceDialog()
	{
		setTitle("Distance Metric Comparision");
		final JPanel contentPanel = new JPanel();
		setBounds(100, 100, 950, 500);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		final JPanel buttonPane = createButtonPanel(getContentPane());
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		contentPanel.setLayout(null);

		lblChosenGames = new JLabel("Choosen Games: 0");
		lblChosenGames.setBounds(26, 41, 145, 15);
		contentPanel.add(lblChosenGames);

		model = new DefaultListModel<LudRul>();
		final JList<LudRul> list = new JList<LudRul>(model);
		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(26, 41 + 30, 178, 200);
		list.setLayoutOrientation(JList.VERTICAL);
		scrollPane.setViewportView(list);
		contentPanel.add(scrollPane);

		final JButton btnAddFolder = getAddGamesOrFolderButton(
				getContentPane());
		btnAddFolder.setBounds(26 + 185, 41 + 30, 178, 25);
		allButtons.add(btnAddFolder);
		contentPanel.add(btnAddFolder);

		final JButton btnRemoveFolder = getRemoveFolderButton(getContentPane());
		btnRemoveFolder.setBounds(26 + 185, 41 + 30 + 30, 178, 25);
		allButtons.add(btnRemoveFolder);
		contentPanel.add(btnRemoveFolder);

		final JButton btnRemoveSelected = getRemoveSelectedGamesButton(
				getContentPane(), list);
		btnRemoveSelected.setBounds(26 + 185, 41 + 30 + 30 + 30, 178, 25);
		allButtons.add(btnRemoveSelected);
		contentPanel.add(btnRemoveSelected);

		final JButton btnRemoveAll = getRemoveAllButton(getContentPane());
		btnRemoveAll.setBounds(26 + 185, 41 + 30 + 30 + 30 + 30, 178, 25);
		allButtons.add(btnRemoveAll);
		contentPanel.add(btnRemoveAll);

		forceRecalculationGamesCheckbox = new JCheckBox("force recalculation");
		forceRecalculationGamesCheckbox.setToolTipText(
				"<html>Adding games needs to compile them and check for all rulesets. As this takes time, helper files are created the first time. <br>(Warning only works if all .lud files within the selected folders are compilable) <br>To force recalculating check on. </html>");
		forceRecalculationGamesCheckbox.setSelected(false);
		forceRecalculationGamesCheckbox.setBounds(26 + 185,
				41 + 30 + 30 + 30 + 30 + 30, 178, 25);
		contentPanel.add(forceRecalculationGamesCheckbox);

		jProgressBar = new JProgressBar();

		jProgressBar.setBounds(26, 380, 385, 25);
		contentPanel.add(jProgressBar);

		addAllTheStuffIDontNeed(contentPanel, false);

		final JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setBounds(430, 0, 8, 450);
		contentPanel.add(separator);

		btnInstantiateMetric = getInstantiateMetricButton(getContentPane());
		btnInstantiateMetric.setBounds(470 + 60, 380, 178, 25);
		allButtons.add(btnInstantiateMetric);
		contentPanel.add(btnInstantiateMetric);

		btnAlternativeMetric = getAlternativeMetricButton(getContentPane());
		btnAlternativeMetric.setBounds(470 + 60, 380 - 30, 178, 25);
		allButtons.add(btnAlternativeMetric);
		contentPanel.add(btnAlternativeMetric);
		// Update these buttons as new distance metrics are added.

		lblDistanceAlgorihtm = new JLabel("Chosen Distance Algorihtm: ");
		lblDistanceAlgorihtm.setBounds(470, 41, 450, 15);
		contentPanel.add(lblDistanceAlgorihtm);

		createMetricSelectionGroup(470, 41 + 30, 291, 15, 20, contentPanel);

		forceRecalculationDistanceMatrixCheckbox = new JCheckBox(
				"force recalculation");
		forceRecalculationDistanceMatrixCheckbox.setToolTipText(
				"<html>As creating the distance matrixes takes time, helper files are created the first time a distance metric is created together with a set of games. <br>To force recalculating check on. </html>");
		forceRecalculationDistanceMatrixCheckbox.setSelected(false);
		forceRecalculationDistanceMatrixCheckbox.setBounds(470 + 60 + 185, 380,
				180, 25);
		contentPanel.add(forceRecalculationDistanceMatrixCheckbox);

		// addBoardFolderGames();
		addJustMouseListener(contentPanel, buttonPane);

	}

	private JButton getAlternativeMetricButton(final Container contentPane)
	{
		final JButton jb = new JButton("Select Metric Variant");
		jb.setEnabled(false);
		jb.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				userSelectAlternativeMetric();
			}
		});
		return jb;
	}

	protected void userSelectAlternativeMetric()
	{
		final DistanceMetric d = distanceMetric.showUserSelectionDialog();
		if (d != null)
			setDistanceMetric(d);
	}

	private JButton getInstantiateMetricButton(final Container contentPane)
	{
		final JButton jb = new JButton("Instantiate Metric");
		jb.setEnabled(false);
		jb.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
				{
					@Override
					public Void doInBackground()
					{
						setBlockFrame(true);

						instantiateMetric();
						return null;
					}

					@Override
					public void done()
					{
						setBlockFrame(false);

					}
				};

				worker.execute();

			}
		});

		return jb;
	}

	protected void instantiateMetric()
	{
		final DistanceProgressListener dpl = getDistanceProgressListener();
		final ArrayList<LudRul> candidates = retrieveGamesFromModel();

		distanceMetric.init(candidates,forceRecalculationDistanceMatrixCheckbox.isSelected(), dpl);

	}

	private void addJustMouseListener(
			final JPanel contentPanel, final JPanel buttonPane
	)
	{
		final Component[] c1 = contentPanel.getComponents();
		final Component[] c2 = buttonPane.getComponents();
		final ArrayList<Component> c3 = new ArrayList<>();
		for (final Component component : c1)
		{
			c3.add(component);
		}
		for (final Component component : c2)
		{
			c3.add(component);
		}
		for (final Component component : c3)
		{
			if (component instanceof AbstractButton)
			{
				component.addMouseListener(new MouseAdapter()
				{

					@Override
					public void mouseEntered(final MouseEvent me)
					{
						ToolTipManager.sharedInstance().setDismissDelay(60000);
					}

					@Override
					public void mouseExited(final MouseEvent me)
					{
						ToolTipManager.sharedInstance()
								.setDismissDelay(defaultDismissTimeout);
					}
				});
			}
		}

	}

	private void addAllTheStuffIDontNeed(
			final JPanel contentPanel, final boolean b
	)
	{
		if (!b)
			return;
		final JLabel lblNewLabel = new JLabel("Number of Trials");
		lblNewLabel.setBounds(26, 200 + 41, 145, 15);
		contentPanel.add(lblNewLabel);

		final JTextField textFieldNumberTrials = new JTextField();
		textFieldNumberTrials.setBounds(220, 39, 162, 19);
		textFieldNumberTrials.setText("10");
		contentPanel.add(textFieldNumberTrials);
		textFieldNumberTrials.setColumns(10);

		final JLabel lblAiModes = new JLabel("AI Agents");
		lblAiModes.setBounds(26, 300 + 169, 91, 15);
		contentPanel.add(lblAiModes);

		final JComboBox<String> comboBoxAIAgents = new JComboBox<String>();
		comboBoxAIAgents.addItem("Random");
		comboBoxAIAgents.addItem("Very weak AI");
		comboBoxAIAgents.addItem("Weak AI");
		comboBoxAIAgents.addItem("Strong AI");
		comboBoxAIAgents.addItem("Very strong AI");
		comboBoxAIAgents.addItem("Custom");
		comboBoxAIAgents.setBounds(220, 300 + 164, 162, 24);
		comboBoxAIAgents.setEnabled(true);
		contentPanel.add(comboBoxAIAgents);

		final JButton btnSelectGame = new JButton("Select Game");
		btnSelectGame.setBounds(26, 300 + 300, 178, 25);
		contentPanel.add(btnSelectGame);

		final JTextField lblSelectedGame = new JTextField("");
		lblSelectedGame.setBounds(26, 350, 300, 25);
		contentPanel.add(lblSelectedGame);

		final JLabel labelMaxTurns = new JLabel("Maximum # Turns");
		labelMaxTurns.setBounds(26, 83, 175, 15);
		contentPanel.add(labelMaxTurns);

		final JTextField textFieldMaxTurns = new JTextField();
		textFieldMaxTurns.setBounds(220, 81, 162, 19);
		textFieldMaxTurns.setText("50");
		textFieldMaxTurns.setColumns(10);
		contentPanel.add(textFieldMaxTurns);

		final JLabel labelThinkTime = new JLabel("Agent Think Time");
		labelThinkTime.setBounds(26, 252, 175, 15);
		contentPanel.add(labelThinkTime);

		textFieldThinkTime = new JTextField();
		textFieldThinkTime.setEnabled(false);
		textFieldThinkTime.setText("0.5");
		textFieldThinkTime.setColumns(10);
		textFieldThinkTime.setBounds(220, 250, 162, 19);
		contentPanel.add(textFieldThinkTime);

		final JLabel lblAiAlgorithm = new JLabel("AI Algorithm");
		lblAiAlgorithm.setBounds(26, 212, 91, 15);
		contentPanel.add(lblAiAlgorithm);

		final String[] comboBoxContents = new String[0]; // GUIUtil.getAiStrings(false).toArray(new
															// String[GUIUtil.getAiStrings(false).size()]);
		final JComboBox<String> comboBoxAlgorithm = new JComboBox<String>(
				comboBoxContents);
		comboBoxAlgorithm.setEnabled(false);
		comboBoxAlgorithm.setBounds(220, 207, 162, 24);
		contentPanel.add(comboBoxAlgorithm);

		// If the user has selected Custom agent, then other options are
		// available.
		comboBoxAIAgents.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (comboBoxAIAgents.getSelectedItem().toString()
						.equals("Custom"))
				{
					comboBoxAlgorithm.setEnabled(true);
					textFieldThinkTime.setEnabled(true);
				} else
				{
					comboBoxAlgorithm.setEnabled(false);
					textFieldThinkTime.setEnabled(false);
				}
			}

		});

		btnSelectGame.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				try
				{
					/*
					 * final String[] choices = FileHandling.listGames(); String
					 * initialChoice = choices[0]; for (final String choice :
					 * choices) { if (Manager.savedLudName() != null &&
					 * Manager.savedLudName().endsWith(choice.replaceAll(Pattern
					 * .quote("\\"), "/"))) { initialChoice = choice; break; } }
					 * final String choice =
					 * GameLoaderDialog.showDialog(DesktopApp.frame(), choices,
					 * initialChoice);
					 * 
					 * final String str =
					 * choice.replaceAll(Pattern.quote("\\"), "/"); final
					 * String[] parts = str.split("/");
					 * 
					 * lblSelectedGame.setText(""); String gameCategoriesString
					 * = "";
					 * 
					 * for (final String s : parts) { if (s.length() > 1) {
					 * gameCategoriesString += s + "; "; } }
					 * 
					 * lblSelectedGame.setText(gameCategoriesString);
					 */
				} catch (final Exception e1)
				{
					// Probably just cancelled the game loader.
				}
			}
		});

	}

	private JButton getRemoveAllButton(final Container contentPane)
	{
		final JButton jbRemove = new JButton("Remove All");

		jbRemove.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						model.removeAllElements();
						setChosenGamesLabel(lblChosenGames, model);
					}
				});

			}
		});

		return jbRemove;
	}

	void setChosenGamesLabel(
			final JLabel lblChosenGames, final DefaultListModel<LudRul> model
	)
	{
		lblChosenGames.setText("Chosen Games: " + model.size());
	}

	private JButton getRemoveSelectedGamesButton(
			final Container parent, final JList<LudRul> list
	)
	{
		final JButton jbRemove = new JButton("Remove Selected");

		jbRemove.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				final int[] indices = list.getSelectedIndices();
				Arrays.sort(indices); // probably not necessary, but wont hurt
				SwingUtilities.invokeLater(new Runnable()
				{

					@Override
					public void run()
					{
						for (int index = indices.length
								- 1; index >= 0; index--)
						{
							model.remove(indices[index]);

						}
						setChosenGamesLabel(lblChosenGames, model);
					}
				});

			}
		});

		return jbRemove;
	}

	private JButton getRemoveFolderButton(final Container parent)
	{
		final JButton jbRemove = new JButton("Remove Games");

		jbRemove.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				final File[] f = showFileAndFolderChooser(
						"Specify the folder or games to remove", parent);
				removeFromSelectedFiles(f, model);

			}
		});

		return jbRemove;
	}

	private JButton getAddGamesOrFolderButton(final Container parent)
	{
		final JButton jbAdd = new JButton("Add Games");

		jbAdd.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				final File[] f = showFileAndFolderChooser(
						"Specify the folder or games to add", parent);
				addToSelectedFiles(f, model);
			}
		});

		return jbAdd;
	}

	protected void addToSelectedFiles(
			final File[] f, final DefaultListModel<LudRul> modelOfList
	)
	{

		final DistanceProgressListener dpl = getDistanceProgressListener();
		final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{
			@Override
			public Void doInBackground()
			{
				setBlockFrame(true);
				final ArrayList<LudRul> newOnes;
				newOnes = DistanceUtils.getContainingLudRuls(f,
						forceRecalculationGamesCheckbox.isSelected(), dpl);
				Collections.reverse(newOnes);
				SwingUtilities.invokeLater(new Runnable()
				{

					@Override
					public void run()
					{
						for (int i = 0; i < newOnes.size(); i++)
						{
							final LudRul ludRul = newOnes.get(i);
							if (!modelOfList.contains(ludRul))

								modelOfList.add(0, ludRul);
						}
						setChosenGamesLabel(lblChosenGames, model);
						setBlockFrame(false);
						dpl.update(0, 1);
					}
				});
				return null;
			}
		};
		worker.execute();

	}

	protected void removeFromSelectedFiles(
			final File[] f, final DefaultListModel<LudRul> modelLocal
	)
	{
		final DistanceProgressListener dpl = getDistanceProgressListener();
		final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{
			@Override
			public Void doInBackground()
			{
				setBlockFrame(true);
				final ArrayList<LudRul> newOnes;
				newOnes = DistanceUtils.getContainingLudRuls(f,
						forceRecalculationGamesCheckbox.isSelected(), dpl);
				Collections.reverse(newOnes);
				SwingUtilities.invokeLater(new Runnable()
				{

					@Override
					public void run()
					{
						for (final LudRul ludRul : newOnes)
						{
							if (modelLocal.contains(ludRul))
								modelLocal.removeElement(ludRul);
						}
						setChosenGamesLabel(lblChosenGames, model);
						dpl.update(0, 1);
						setBlockFrame(false);
					}
				});
				return null;
			}
		};
		worker.execute();

	}

	protected File[] showFileAndFolderChooser(
			final String titel, final Component parent
	)
	{
		final JFileChooser f = new JFileChooser(FolderLocations.ludFolder);
		f.setDialogTitle(titel);
		f.setMultiSelectionEnabled(true);
		f.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		final int userSelection = f.showDialog(parent, "select");

		if (userSelection == JFileChooser.APPROVE_OPTION)
		{
			final File[] files = f.getSelectedFiles();
			for (final File file : files)
			{
				System.out.println(file);
			}
			return files;

		}
		return new File[] {};
	}

	private JPanel createButtonPanel(final Component parent)
	{
		final JPanel buttonPane = new JPanel();

		buttonPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// final JButton btnCancelThread = getCancelThreatButton(parent);
		// buttonPane.add(btnCancelThread);

		final JButton exportSplitsTree = getSplitsTreeExportButton(parent);

		buttonPane.add(exportSplitsTree);

		final JButton exportLudemeFrequenciesToWekka = getWekaExportButton(
				parent);
		buttonPane.add(exportLudemeFrequenciesToWekka);

		final JButton exportRuleSetConnectionToYed = exportRuleSetConnectionToYed(
				parent);
		buttonPane.add(exportRuleSetConnectionToYed);

		final JButton generateKmedoidButton = getGenerateKmedoidButton(parent);
		buttonPane.add(generateKmedoidButton);

		final JButton distanceMatrix = getCreateDistanceMatrixButton(parent);
		buttonPane.add(distanceMatrix);

		final JButton btnAssignmentEvaluation = getAssignmentEvaluationButton(
				parent);
		buttonPane.add(btnAssignmentEvaluation);

		final JButton btn3dScatterPlot = get3dScatterPlotButton(parent);
		buttonPane.add(btn3dScatterPlot);

		getRootPane().setDefaultButton(distanceMatrix);

		synchronized (buttonPane.getTreeLock())
		{
			final Component[] c = buttonPane.getComponents();
			for (final Component component : c)
			{
				if (component instanceof JButton)
				{
					allButtons.add((JButton) component);
				}
			}
		}

		return buttonPane;
	}

	private JButton getAssignmentEvaluationButton(Component parent)
	{
		final JButton jb = new JButton("Evaluation");

		jb.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int[] userInput = askUserForAssignmentSettings(
						knearestNeighbourDefault, knearestNeighbourNoiseDefault,
						parent);
				final int knearestNeighbour = userInput[0];
				final int knearestNeighbourNoise = userInput[1];

				final ArrayList<LudRul> candidates = retrieveGamesFromModel();

				final AssignmentSettings ass = new AssignmentSettings(
						knearestNeighbour, knearestNeighbourNoise);
				final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
				{
					@Override
					public Void doInBackground()
					{
						final long t1 = System.currentTimeMillis();
						setBlockFrame(true);
						final DistanceProgressListener pl = getDistanceProgressListener();
						final DistanceMetric metric = retrieveDistanceMetric(
								candidates);
						final AssignmentStats as = EvaluatorDistanceMetric
								.evaluateDistanceMeasureNearestNeighbours(
										metric, candidates, ass, pl,
										forceRecalculationDistanceMatrixCheckbox
												.isSelected());

						final JFrame jf = new JFrame(
								"Assignment stats: " + metric.getName()
										+ " knear: " + knearestNeighbour
										+ " knoise: " + knearestNeighbourNoise);
						jf.setSize(400, 300);
						final JTextArea jta = new JTextArea(as.toString());
						jta.setSize(400, 300);

						jta.setFont(new Font("Courier New", Font.PLAIN, 12));

						jf.add(jta, BorderLayout.CENTER);
						final JButton btnExport = new JButton("Export CSV");
						btnExport.addActionListener(new ActionListener()
						{
							@Override
							public void actionPerformed(ActionEvent e2)
							{
								final JFileChooser f = new JFileChooser();
								f.setDialogTitle("Specify the Csv file");

								final int userSelection = f
										.showSaveDialog(parent);

								if (userSelection == JFileChooser.APPROVE_OPTION)
								{
									System.out.println(f.getSelectedFile());
									final File file = f.getSelectedFile();
									final File folder = file.getParentFile();
									String fileName = file.getName();
									if (fileName != null
											&& fileName.contains("."))
										fileName = fileName.substring(0,
												fileName.lastIndexOf('.'));
									final String finalFileName = fileName;

									as.exportToCSV(folder, finalFileName);
								}

							}
						});
						final JTextArea legend = new JTextArea();
						legend.setFont(new Font("Courier New", Font.PLAIN, 12));
						legend.setBorder(new LineBorder(new Color(0, 0, 0)));
						legend.setText(AssignmentStats.getLegend());
						legend.setVisible(false);
						final JButton btnLegend = new JButton("Show Legend");
						btnLegend.addActionListener(new ActionListener()
						{

							@Override
							public void actionPerformed(ActionEvent e1)
							{
								legend.setVisible(!legend.isVisible());
								jf.pack();
							}
						});
						final JButton btnConfusionMatrix = new JButton(
								"Show Confusion Matrix");
						btnConfusionMatrix
								.addActionListener(new ActionListener()
								{
									@Override
									public void actionPerformed(ActionEvent e2)
									{
										final JFrame cf = new JFrame(
												"Confusion Matrix. Row is n times assigned as column");
										final JTextArea jtac = new JTextArea(
												as.getConfusionMatrixString());
										jtac.setFont(new Font("Courier New",
												Font.PLAIN, 12));
										cf.add(jtac);
										cf.pack();
										cf.setVisible(true);
									}
								});
						jf.add(legend, BorderLayout.EAST);
						final JPanel buttonList = new JPanel(
								new FlowLayout(FlowLayout.RIGHT));
						buttonList.add(btnExport);
						buttonList.add(btnLegend);
						buttonList.add(btnConfusionMatrix);
						jf.add(buttonList, BorderLayout.NORTH);
						jf.pack();
						jf.setLocation(parent.getLocation());
						jf.setVisible(true);

						System.out.println(jta.getText());
						final long t2 = System.currentTimeMillis();
						System.out.println("Proccessing time: " + (t2-t1) + "ms");
						return null;
					}

					@Override
					public void done()
					{
						setBlockFrame(false);

					}
				};

				worker.execute();

			}
		});

		return jb;
	}

	private JButton get3dScatterPlotButton(Component parent)
	{
		final JButton jb = new JButton("3d Scatter Plot");

		jb.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int[] userInput = askUserForAssignmentSettings(
						knearestNeighbourDefault, knearestNeighbourNoiseDefault,
						parent);
				final int knearestNeighbour = userInput[0];
				final int knearestNeighbourNoise = userInput[1];

				final int[] userInput2 = askUserForKmedioidSettings(minKDefault,
						maxKDefault);
				final int minK = userInput2[0];
				final int maxK = userInput2[1];

				final ArrayList<LudRul> candidates = retrieveGamesFromModel();

				final AssignmentSettings ass = new AssignmentSettings(
						knearestNeighbour, knearestNeighbourNoise);

				final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
				{
					@Override
					public Void doInBackground()
					{
						setBlockFrame(true);

						final DistanceMetric metric = retrieveDistanceMetric(
								candidates);
						final DistanceMatrix<LudRul, LudRul> distanceMatrix;
						distanceMatrix = createDistanceMatrix(candidates,
								metric);
						final ScatterPlotWindow scatterWindow = new ScatterPlotWindow(
								candidates, ass, metric, distanceMatrix, minK,
								maxK);
						scatterWindow.setVisible(true);
						return null;
					}

					@Override
					public void done()
					{
						setBlockFrame(false);

					}
				};

				worker.execute();

			}
		});

		return jb;
	}

	/*
	 * private JButton getCancelThreatButton(final Component parent) { final
	 * JButton jb = new JButton("Cancel Execution"); jb.addActionListener(new
	 * ActionListener() {
	 * 
	 * @Override public void actionPerformed(final ActionEvent e) { for (final
	 * SwingWorker<Void,Void> swingWorker : sw) { swingWorker.can(true); }
	 * sw.clear(); } }); return jb; }
	 */

	private JButton getSplitsTreeExportButton(final Component parent)
	{
		final JButton jb = new JButton("Export to Splitstree");
		jb.setToolTipText(
				"<html>Exports the distance matrix, such that it can be imported<br>"
						+ "into Splitstree 5 (4 should also work) using the import editor<br>"
						+ "</html>");
		jb.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final JFileChooser f = new JFileChooser();
				f.setDialogTitle("Specify the txt file");

				final int userSelection = f.showSaveDialog(parent);

				if (userSelection == JFileChooser.APPROVE_OPTION)
				{
					System.out.println(f.getSelectedFile());
					final File file = f.getSelectedFile();
					final File folder = file.getParentFile();
					String fileName = file.getName();
					if (fileName != null && fileName.contains("."))
						fileName = fileName.substring(0,
								fileName.lastIndexOf('.'));
					final String finalFileName = fileName;

					final ArrayList<LudRul> candidates = retrieveGamesFromModel();

					final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
					{
						@Override
						public Void doInBackground()
						{
							setBlockFrame(true);
							final DistanceMetric metric = retrieveDistanceMetric(
									candidates);
							final DistanceMatrix<LudRul, LudRul> distanceMatrix;
							distanceMatrix = createDistanceMatrix(candidates,
									metric);

							distanceMatrix.generateSplitstreeFile(folder,
									finalFileName);
							return null;
						}

						@Override
						public void done()
						{
							setBlockFrame(false);

						}
					};

					worker.execute();

				}

			}
		});

		return jb;
	}

	private JButton getCreateDistanceMatrixButton(final Component parent)
	{
		final JButton jb = new JButton("Distance Matrix");

		jb.setActionCommand("OK");

		jb.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int[] userInput = askUserForAssignmentSettings(
						knearestNeighbourDefault, knearestNeighbourNoiseDefault,
						parent);
				final int knearestNeighbour = userInput[0];
				final int knearestNeighbourNoise = userInput[1];

				final ArrayList<LudRul> candidates = retrieveGamesFromModel();

				final AssignmentSettings ass = new AssignmentSettings(
						knearestNeighbour, knearestNeighbourNoise);

				final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
				{
					@Override
					public Void doInBackground()
					{
						setBlockFrame(true);

						final DistanceMetric metric = retrieveDistanceMetric(
								candidates);
						final DistanceMatrix<LudRul, LudRul> distanceMatrix;
						distanceMatrix = createDistanceMatrix(candidates,
								metric);
						final Visualiser v = new Visualiser(metric.getName(),
								candidates, distanceMatrix, ass);
						v.setVisible(true);
						return null;
					}

					@Override
					public void done()
					{
						setBlockFrame(false);

					}
				};

				worker.execute();

			}
		});

		return jb;
	}

	protected DistanceMetric retrieveDistanceMetric(
			final ArrayList<LudRul> candidates
	)
	{
		if (distanceMetric.typeNeedsToBeInitialized()
				&& (!distanceMetric.isInitialized(candidates)||forceRecalculationDistanceMatrixCheckbox.isSelected()))
		{
			instantiateMetric();
		}
		return distanceMetric;
	}

	void setBlockFrame(final boolean shallBlock)
	{
		if (shallBlock)
		{
			defaultCursor = getCursor();
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			for (final AbstractButton jButton : allButtons)
			{
				jButton.setEnabled(false);
			}
		} else
		{
			setCursor(defaultCursor);
			for (final AbstractButton jButton : allButtons)
			{
				jButton.setEnabled(true);
			}
		}

	}

	protected int[] askUserForAssignmentSettings(
			final int knearestNeighbourStartValue,
			final int knearestNeighbourNoiseStartValue,
			final Component parentComponent
	)
	{
		int knearestNeighbour = knearestNeighbourStartValue;
		int knearestNeighbourNoise = knearestNeighbourNoiseStartValue;

		final JTextField knearestNeighbourField = new JTextField();
		knearestNeighbourField.setText("" + knearestNeighbour);
		final JTextField knearestNoiseField = new JTextField();
		knearestNoiseField.setText("" + knearestNeighbourNoise);
		final Object[] message = { "k nearest Neighbour:",
				knearestNeighbourField, "k nearest Noise:",
				knearestNoiseField };

		final int option = JOptionPane.showConfirmDialog(null, message,
				"Classifier Settings", JOptionPane.OK_CANCEL_OPTION);

		if (option == JOptionPane.OK_OPTION)
		{
			try
			{
				knearestNeighbour = Integer
						.parseInt(knearestNeighbourField.getText());
			} catch (final Exception e2)
			{
				System.out.println(
						"Not a number, using k nearest neigbours = 3 instead");
			}
			try
			{
				knearestNeighbourNoise = Integer
						.parseInt(knearestNoiseField.getText());
			} catch (final Exception e2)
			{
				System.out.println(
						"Not a number, using noise exclusion = 3 instead");
			}

		}
		return new int[] { knearestNeighbour, knearestNeighbourNoise };
	}

	protected ArrayList<LudRul> retrieveGamesFromModel()
	{
		final ArrayList<LudRul> candidates = new ArrayList<>();
		for (int i = 0; i < model.getSize(); i++)
		{
			candidates.add(model.elementAt(i));
		}
		return candidates;
	}

	protected DistanceMatrix<LudRul, LudRul> createDistanceMatrix(
			final ArrayList<LudRul> candidates, final DistanceMetric metric
	)
	{

		final DistanceProgressListener dpl = getDistanceProgressListener();
		DistanceMatrix<LudRul, LudRul> dm;

		if (!metric.isInitialized(candidates))
		{
			metric.init(candidates,forceRecalculationDistanceMatrixCheckbox.isSelected(), dpl);
		}

		dm = EvaluatorDistanceMetric.getDistanceMatrix(candidates, metric,
				forceRecalculationDistanceMatrixCheckbox.isSelected(), dpl);

		return dm;

	}

	DistanceProgressListener getDistanceProgressListener()
	{

		final DistanceProgressListener dpl = new DistanceProgressListener()
		{
			@Override
			public void update(
					final int completedComparisions, final int totalComparisions
			)
			{
				jProgressBar.setMaximum(totalComparisions);
				jProgressBar.setMinimum(0);
				jProgressBar.setValue(completedComparisions);
			}

			@Override
			public void update(
					final boolean assumeFirstLineTakesLonger,
					final int lengthFirstLine, final double percentage,
					final int completedComparisions, final int totalComparisions
			)
			{
				if (!assumeFirstLineTakesLonger)
				{
					jProgressBar.setMaximum(totalComparisions);
					jProgressBar.setMinimum(0);
					jProgressBar.setValue(completedComparisions);
				} else
				{
					if (completedComparisions < lengthFirstLine)
					{
						jProgressBar
								.setMaximum(lengthFirstLine + lengthFirstLine);
						jProgressBar.setMinimum(0);
						jProgressBar.setValue(completedComparisions);
					} else
					{
						final int remainingComparisionsAfterFirstLine = totalComparisions
								- lengthFirstLine;
						jProgressBar
								.setMaximum(remainingComparisionsAfterFirstLine
										+ remainingComparisionsAfterFirstLine);
						jProgressBar.setMinimum(0);
						jProgressBar.setValue(
								completedComparisions - lengthFirstLine
										+ remainingComparisionsAfterFirstLine);
					}
				}

			}
		};
		return dpl;
	}

	private JButton exportRuleSetConnectionToYed(final Component parentFrame)
	{
		final JButton jb = new JButton("Export rules");
		jb.setToolTipText(
				"<html>Exports the rule connections of the grammar to an gml file<br>"
						+ "to be opened with yEd<br>"
						+ "Currently it shows that the connections are too complex to easely untangle in 2d space"
						+ "</html>");
		jb.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final JFileChooser f = new JFileChooser();
				f.setDialogTitle("Specify the gml file");

				final int userSelection = f.showSaveDialog(parentFrame);

				if (userSelection == JFileChooser.APPROVE_OPTION)
				{
					System.out.println(f.getSelectedFile());
					final File file = f.getSelectedFile();
					final File folder = file.getParentFile();
					String fileName = file.getName();
					if (fileName != null && fileName.contains("."))
						fileName = fileName.substring(0,
								fileName.lastIndexOf('.'));
					final GraphComposerGrammarToken gcgt = new GraphComposerGrammarToken();
					gcgt.compose(folder, fileName);
				}

			}
		});

		return jb;
	}

	private JButton getGenerateKmedoidButton(final Component parent)
	{
		final JButton kmedioid = new JButton("Generate k-mediod");
		kmedioid.setToolTipText(KmedoidClustering.getToolTipText());
		kmedioid.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{

				final ArrayList<LudRul> candidates = retrieveGamesFromModel();

				final int[] userInput = askUserForKmedioidSettings(minKDefault,
						maxKDefault);
				final int minK = userInput[0];
				final int maxK = userInput[1];

				final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
				{
					@Override
					public Void doInBackground()
					{
						setBlockFrame(true);
						final DistanceMetric metric = retrieveDistanceMetric(
								candidates);
						final DistanceMatrix<LudRul, LudRul> distanceMatrix;
						distanceMatrix = createDistanceMatrix(candidates,
								metric);
						final KmedoidClustering km = new KmedoidClustering();
						km.generateClusterings(candidates, distanceMatrix, minK,
								maxK);

						km.printKtoSSE();

						final ClusteringVisualiser cv = new ClusteringVisualiser(
								candidates, metric, km);
						cv.setVisible(true);
						return null;
					}

					@Override
					public void done()
					{
						setBlockFrame(false);

					}
				};

				worker.execute();
			}
		});

		return kmedioid;
	}

	protected int[] askUserForKmedioidSettings(
			final int minKStartValue, final int maxKStartValue
	)
	{
		int minK = minKStartValue;
		int maxK = maxKStartValue;

		final JTextField minKField = new JTextField();
		minKField.setText(minK + "");
		final JTextField maxKField = new JTextField();
		maxKField.setText(maxK + "");
		final Object[] message = { "minK:", minKField, "maxK:", maxKField };

		final int option = JOptionPane.showConfirmDialog(null, message,
				"Enter Clusters", JOptionPane.OK_CANCEL_OPTION);

		if (option == JOptionPane.OK_OPTION)
		{
			try
			{
				minK = Integer.parseInt(minKField.getText());
			} catch (final Exception e2)
			{
				System.out.println("Not a number, using kmin = 1 instead");
			}
			try
			{
				maxK = Integer.parseInt(maxKField.getText());
			} catch (final Exception e2)
			{
				System.out.println("Not a number, using kmax = 12 instead");
			}

		}
		return new int[] { minK, maxK };
	}

	private JButton getWekaExportButton(final Component parentFrame)
	{
		final JButton jb = new JButton("ExportLudemeForWeka");
		jb.setToolTipText(
				"<html>Exports the absolute occurences and relative frequencies of all Ludeme Keywords within the game descriptions to .csv<br>"
						+ "Both versions are stored twice. One time including the game name and one time without it. <br>The last column denotes the type of the game<br>"
						+ "Games wich contain several rulesets are listed several times"
						+ "</html>");
		jb.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				final JFileChooser f = new JFileChooser();
				f.setDialogTitle("Specify the folder to save the Frequecies");
				final ArrayList<LudRul> candidates = retrieveGamesFromModel();
				f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				final int userSelection = f.showSaveDialog(parentFrame);
				final DistanceProgressListener pl = getDistanceProgressListener();
				if (userSelection != JFileChooser.APPROVE_OPTION)
				{
					return;

				}
				final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
				{
					@Override
					public Void doInBackground()
					{
						setBlockFrame(true);

						System.out.println(f.getSelectedFile());
						DistanceUtils.exportLudiiFrequenciesForWeka(candidates,
								f.getSelectedFile(), pl);
						return null;
					}

					@Override
					public void done()
					{
						setBlockFrame(false);
					}
				};
				worker.execute();
			}

		});

		return jb;
	}

	@SuppressWarnings("unused")
	private void pressEvaluate(
			final AbstractButton textFieldMaxTurns,
			final AbstractButton textFieldNumberTrials,
			final JComboBox<String> comboBoxAIAgents,
			final JComboBox<String> comboBoxAlgorithm,
			final AbstractButton lblSelectedGame
	)
	{

		if (Double.valueOf(textFieldThinkTime.getText().toString())
				.doubleValue() <= 0)
		{
			// TOSETBACK DesktopApp.playerApp().addTextToAnalysisPanel("Invalid
			// think time, setting to 0.05");
			System.out.println("Invalid think time, setting to 0.05");
			textFieldThinkTime.setText("0.05");
		}

		try
		{
			if (Integer.valueOf(textFieldMaxTurns.getText().toString())
					.intValue() <= 0)
			{
				// TOSETBACK
				// DesktopApp.playerApp().addTextToAnalysisPanel("Invalid
				// maximum number of turns, setting to 50");
				System.out.println(
						"Invalid maximum number of turns, setting to 50");
				textFieldMaxTurns.setText("50");
			}
		} catch (final NumberFormatException exception)
		{
			// TOTURNON
			// DesktopApp.playerApp().addTextToAnalysisPanel("Invalid maximum
			// number of turns, setting to 50");
			textFieldMaxTurns.setText("50");
		}

		try
		{
			if (Integer.valueOf(textFieldNumberTrials.getText().toString())
					.intValue() <= 0)
			{
				// TOTURNON
				// DesktopApp.playerApp().addTextToAnalysisPanel("Invalid number
				// of trials, setting to 10");
				textFieldNumberTrials.setText("10");
			}
		} catch (final NumberFormatException exception)
		{
			// TOTURNON
			// DesktopApp.playerApp().addTextToAnalysisPanel("Invalid number of
			// trials, setting to 10");
			textFieldNumberTrials.setText("10");
		}

		final int maxTurns = Integer
				.valueOf(textFieldMaxTurns.getText().toString()).intValue();
		final int numberTrials = Integer
				.valueOf(textFieldNumberTrials.getText().toString()).intValue();

		double thinkTime = 0.5;
		String AIName = null;

		switch (comboBoxAIAgents.getSelectedItem().toString())
		{
		case "Random":
			AIName = "Random";
			break;
		case "Very weak AI":
			AIName = "Ludii AI";
			thinkTime = 0.1;
			break;
		case "Weak AI":
			AIName = "Ludii AI";
			thinkTime = 0.5;
			break;
		case "Strong AI":
			AIName = "Ludii AI";
			thinkTime = 2.0;
			break;
		case "very strong AI":
			AIName = "Ludii AI";
			thinkTime = 5.0;
			break;
		case "Custom":
			AIName = comboBoxAlgorithm.getSelectedItem().toString();
			thinkTime = Double.valueOf(textFieldThinkTime.getText())
					.doubleValue();
			break;
		}

		final List<Game> allGameB = getAllGamesFromCategories(
				lblSelectedGame.getText().split(";"));

		// distanceMetric.distance(Manager.ref().context().game(), allGameB,
		// numberTrials, maxTurns, thinkTime, AIName);
		// MainWindow.tabPanel().select(TabView.PanelAnalysis);

	}

	private void createMetricSelectionGroup(
			final int x, final int y, final int width, final int height,
			final int lineHeight, final JPanel contentPanel
	)
	{
		final List<DistanceMetric> metricsWithoutPre = DistanceUtils
				.getAllDistanceMetricesWithoutPreprocessing();
		final List<DistanceMetric> metricsWithPre = DistanceUtils
				.getAllDistanceMetricesWithPreprocessing();
		final List<DistanceMetric> metrics = new ArrayList<DistanceMetric>();
		metrics.addAll(metricsWithoutPre);
		metrics.addAll(metricsWithPre);

		boolean onlyFirstTimeTrue = true;
		final ButtonGroup group = new ButtonGroup();
		int yshift = 0;
		for (final DistanceMetric dm : metrics)
		{
			final JRadioButton button = new JRadioButton(dm.getName(),
					onlyFirstTimeTrue);
			if (onlyFirstTimeTrue)
				metricRadioButtonClicked(dm);
			button.setBounds(x, y + yshift, width, height);
			contentPanel.add(button);
			group.add(button);
			button.addActionListener(e -> metricRadioButtonClicked(dm));
			button.setToolTipText(dm.getToolTipText());
			yshift += lineHeight;
			onlyFirstTimeTrue = false;
			allButtons.add(button);

		}

	}

	private void setDistanceMetric(final DistanceMetric dm)
	{
		distanceMetric = dm;
		lblDistanceAlgorihtm.setText("DistanceMetric: " + dm.getName());
		lblDistanceAlgorihtm.setToolTipText(lblDistanceAlgorihtm.getText());

	}

	private void metricRadioButtonClicked(
			final DistanceMetric distanceMetricSelected
	)
	{
		setDistanceMetric(distanceMetricSelected);

		if (distanceMetricSelected.typeNeedsToBeInitialized())
			btnInstantiateMetric.setVisible(true);
		else
			btnInstantiateMetric.setVisible(false);

		if (distanceMetricSelected.hasUserSelectionDialog())
			btnAlternativeMetric.setVisible(true);
		else
			btnAlternativeMetric.setVisible(false);

	}

	// -------------------------------------------------------------------------

	/**
	 * Returns a list of all Games in Ludii that include all required category
	 * strings in the path.
	 * 
	 * @param categoriesToMatch
	 * @return
	 */
	static List<Game> getAllGamesFromCategories(
			final String[] categoriesToMatch
	)
	{

		for (int i = 0; i < categoriesToMatch.length; i++)
			categoriesToMatch[i] = categoriesToMatch[i].trim();

		final List<Game> validGames = new ArrayList<>();

		final String[] gamePaths = FileHandling.listGames();
		for (final String path : gamePaths)
		{
			boolean validGame = true;
			final String[] gameCategories = path.split("/");
			for (final String categoryToMatch : categoriesToMatch)
			{
				if (!Arrays.asList(gameCategories)
						.contains(categoryToMatch.trim()))
				{
					validGame = false;
					break;
				}
			}
			if (validGame)
			{
				// final String gameDescription =
				// GameLoading.getGameDescriptionRawFromName(path);
				// validGames.add(new Game(path, new
				// Description(gameDescription)));

			}
		}
		return validGames;
	}
}
