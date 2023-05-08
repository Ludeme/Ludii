package app.display.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import analysis.Complexity;
import app.DesktopApp;
import app.PlayerApp;
import app.display.dialogs.util.DialogUtil;
import app.display.util.DesktopGUIUtil;
import app.display.views.tabs.TabView;
import app.loading.FileLoading;
import app.utils.AIPlayer;
import app.utils.ReportMessengerGUI;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import main.grammar.Report;
import metrics.Evaluation;
import metrics.Metric;
import metrics.designer.IdealDuration;
import metrics.designer.SkillTrace;

/**
 * Dialog that is used to display various game evaluation options
 * 
 * @author Matthew.Stephenson
 */
public class EvaluationDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	final JTextField textFieldThinkTime;
	final JTextField textFieldMinIdealTurns;
	final JTextField textFieldMaxIdealTurns;
	final JTextField textFieldNumMatches;
	final JTextField textFieldNumTrialsPerMatch;
	final JTextField textFieldHardTimeLimit;
	final JTextField txtcommonresoutput;
	
	//-------------------------------------------------------------------------

	/**
	 * Show the Dialog.
	 */
	public static void showDialog(final PlayerApp app)
	{
		try
		{
			final EvaluationDialog dialog = new EvaluationDialog(app);
			DialogUtil.initialiseSingletonDialog(dialog, "Game Evaluation", null);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Create the dialog.
	 */
	@SuppressWarnings("serial")
	public EvaluationDialog(final PlayerApp app)
	{
		final List<Metric> metrics = new Evaluation().dialogMetrics();
		final ArrayList<Double> weights = new ArrayList<>();

		final JButton okButton;
		setBounds(100, 100, 780, DesktopApp.frame().getHeight());
		getContentPane().setLayout(new BorderLayout());
		
		final JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		final JPanel LeftPanel = new JPanel();
		panel.add(LeftPanel, BorderLayout.WEST);
		LeftPanel.setPreferredSize(new Dimension(460,500));
		//LeftPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		LeftPanel.setLayout(null);
		
		final JLabel lblNewLabel = new JLabel("Number of Trials");
		lblNewLabel.setBounds(26, 41, 145, 15);
		LeftPanel.add(lblNewLabel);
		
		final JTextField textFieldNumberTrials = new JTextField();
		textFieldNumberTrials.setBounds(280, 39, 102, 19);
		textFieldNumberTrials.setText("10");
		LeftPanel.add(textFieldNumberTrials);
		textFieldNumberTrials.setColumns(10);
		
		final JLabel lblAiModes = new JLabel("AI Agents");
		lblAiModes.setBounds(26, 169, 91, 15);
		LeftPanel.add(lblAiModes);
		
		final JComboBox<String> comboBoxAIAgents = new JComboBox<String>();
		comboBoxAIAgents.addItem("Random");
		comboBoxAIAgents.addItem("Very weak AI");
		comboBoxAIAgents.addItem("Weak AI");
		comboBoxAIAgents.addItem("Strong AI");
		comboBoxAIAgents.addItem("Very strong AI");
		comboBoxAIAgents.addItem("Custom");
		comboBoxAIAgents.setBounds(220, 164, 162, 24);
		comboBoxAIAgents.setEnabled(true);
		LeftPanel.add(comboBoxAIAgents);
		
				
		final JLabel labelMaxTurns = new JLabel("Maximum # Turns (per player)");
		labelMaxTurns.setBounds(26, 72, 175, 15);
		LeftPanel.add(labelMaxTurns);
		
		final JTextField textFieldMaxTurns = new JTextField();
		textFieldMaxTurns.setBounds(280, 70, 102, 19);
		textFieldMaxTurns.setText("50");
		textFieldMaxTurns.setColumns(10);
		LeftPanel.add(textFieldMaxTurns);
		
		final JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setBounds(430, 0, 8, 4500);
		LeftPanel.add(separator);
		
		final JLabel labelThinkTime = new JLabel("Agent Think Time");
		labelThinkTime.setBounds(26, 252, 175, 15);
		LeftPanel.add(labelThinkTime);
		
		textFieldThinkTime = new JTextField();
		textFieldThinkTime.setEnabled(false);
		textFieldThinkTime.setText("0.5");
		textFieldThinkTime.setColumns(10);
		textFieldThinkTime.setBounds(220, 250, 162, 19);
		LeftPanel.add(textFieldThinkTime);
		
		final JLabel lblAiAlgorithm = new JLabel("AI Algorithm");
		lblAiAlgorithm.setBounds(26, 212, 91, 15);
		LeftPanel.add(lblAiAlgorithm);
		final String[] comboBoxContents = DesktopGUIUtil.getAIDropdownStrings(app, false).toArray(new String[DesktopGUIUtil.getAIDropdownStrings(app, false).size()]);
		final JComboBox<String> comboBoxAlgorithm = new JComboBox<String>(comboBoxContents); //comboBoxContents
		comboBoxAlgorithm.setEnabled(false);
		comboBoxAlgorithm.setBounds(220, 207, 162, 24);
		LeftPanel.add(comboBoxAlgorithm);
		
		final JLabel lblIdealTurnNumber = new JLabel("Ideal Turn Range");
		lblIdealTurnNumber.setBounds(26, 331, 175, 15);
		LeftPanel.add(lblIdealTurnNumber);
		
		final JLabel lblMinimum = new JLabel("Minimum");
		lblMinimum.setBounds(26, 357, 175, 15);
		LeftPanel.add(lblMinimum);
		
		final JLabel lblMaximum = new JLabel("Maximum");
		lblMaximum.setBounds(26, 383, 175, 15);
		LeftPanel.add(lblMaximum);
		
		textFieldMinIdealTurns = new JTextField();
		textFieldMinIdealTurns.setText("0");
		textFieldMinIdealTurns.setColumns(10);
		textFieldMinIdealTurns.setBounds(220, 354, 162, 19);
		LeftPanel.add(textFieldMinIdealTurns);
		
		textFieldMaxIdealTurns = new JTextField();
		textFieldMaxIdealTurns.setText("1000");
		textFieldMaxIdealTurns.setColumns(10);
		textFieldMaxIdealTurns.setBounds(220, 380, 162, 19);
		LeftPanel.add(textFieldMaxIdealTurns);
		
		
		
		final JLabel lblSkillTrace = new JLabel("Skill Trace");
		lblSkillTrace.setBounds(26, 435, 175, 15);
		LeftPanel.add(lblSkillTrace);
		
		final JLabel lblDirectory = new JLabel("Output Folder");
		lblDirectory.setBounds(26, 461, 175, 15);
		LeftPanel.add(lblDirectory);
		
		final JLabel lblNumMatches = new JLabel("Maximum Levels");
		lblNumMatches.setBounds(26, 487, 175, 15);
		LeftPanel.add(lblNumMatches);
		
		final JLabel lblTrailsPerMatch = new JLabel("Trials Per Level");
		lblTrailsPerMatch.setBounds(26, 513, 175, 15);
		LeftPanel.add(lblTrailsPerMatch);
		
		final JLabel lblHardTimeLimit = new JLabel("Maximum Time(s)");
		lblHardTimeLimit.setBounds(26, 539, 175, 15);
		LeftPanel.add(lblHardTimeLimit);
		
		final JButton skillTraceButton = new JButton("Run Skill Trace Only");  
		skillTraceButton.setBounds(180, 565, 202, 23);  
	    LeftPanel.add(skillTraceButton);  
		
		textFieldNumMatches = new JTextField();
		textFieldNumMatches.setText("8");
		textFieldNumMatches.setColumns(10);
		textFieldNumMatches.setBounds(220, 484, 162, 19);
		LeftPanel.add(textFieldNumMatches);
		
		textFieldNumTrialsPerMatch = new JTextField();
		textFieldNumTrialsPerMatch.setText("100");
		textFieldNumTrialsPerMatch.setColumns(10);
		textFieldNumTrialsPerMatch.setBounds(220, 510, 162, 19);
		LeftPanel.add(textFieldNumTrialsPerMatch);
		
		textFieldHardTimeLimit = new JTextField();
		textFieldHardTimeLimit.setText("60");
		textFieldHardTimeLimit.setColumns(10);
		textFieldHardTimeLimit.setBounds(220, 536, 162, 19);
		LeftPanel.add(textFieldHardTimeLimit);
		
		String tempFilePath = DesktopApp.lastSelectedJsonPath();
		if (tempFilePath == null)
			tempFilePath = System.getProperty("user.dir");
		final String defaultFilePath = tempFilePath;
		
		txtcommonresoutput = new JTextField();
		txtcommonresoutput.setText(defaultFilePath);
		txtcommonresoutput.setBounds(140, 460, 180, 19);
		LeftPanel.add(txtcommonresoutput);
		txtcommonresoutput.setColumns(10);
		
		final JButton buttonSelectDir = new JButton("Select");
		buttonSelectDir.setFont(new Font("Arial", Font.PLAIN, 7));
		buttonSelectDir.setBounds(324, 460, 55, 18);
		final ActionListener buttonListener = new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				final JFileChooser fileChooser = FileLoading.createFileChooser(defaultFilePath, ".txt", "TXT files (.txt)");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setDialogTitle("Select output directory.");
				final int jsonReturnVal = fileChooser.showOpenDialog(DesktopApp.frame());
				final File directory;

				if (jsonReturnVal == JFileChooser.APPROVE_OPTION)
					directory = fileChooser.getSelectedFile();
				else
					directory = null;

				if (directory != null && directory.exists())
				{
					txtcommonresoutput.setText(directory.getPath());
				}
				else
				{
					txtcommonresoutput.setText(defaultFilePath);
				}
			}
		};
		buttonSelectDir.addActionListener(buttonListener);
		LeftPanel.add(buttonSelectDir);
		
		
		final JButton btnCalculateTurnRange = new JButton("Calculate");
		btnCalculateTurnRange.setBounds(220, 323, 162, 23);
		LeftPanel.add(btnCalculateTurnRange);
		
		final JCheckBox useDatabaseTrialsCheckBox = new JCheckBox("Use Database Trials (when available)");
		useDatabaseTrialsCheckBox.setBounds(26, 97, 323, 23);
		useDatabaseTrialsCheckBox.setSelected(true);
		LeftPanel.add(useDatabaseTrialsCheckBox);
		
		
		// If the user has selected Custom agent, then other options are available.
		comboBoxAIAgents.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (comboBoxAIAgents.getSelectedItem().toString().equals("Custom"))
				{
					comboBoxAlgorithm.setEnabled(true);
					textFieldThinkTime.setEnabled(true);
				}
				else
				{
					comboBoxAlgorithm.setEnabled(false);
					textFieldThinkTime.setEnabled(false);
				}
			}
			
		});
		
		
		// Calculate the automatic min/max turn numbers
		btnCalculateTurnRange.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{	
				app.setVolatileMessage("Please Wait a few seconds.");
				app.repaint();
				EventQueue.invokeLater(() ->
				{
					EventQueue.invokeLater(() ->
					{
						final double brachingFactor = estimateBranchingFactor(app, 5);
						if (brachingFactor != 0)
						{
							textFieldMinIdealTurns.setText(String.valueOf(brachingFactor));
							textFieldMaxIdealTurns.setText(String.valueOf(brachingFactor*2));
						}
						else
						{
							app.addTextToAnalysisPanel("Failed to calculate branching factor");
						}
					});
				});
			}
		});
		
		final JPanel RightPanel = new JPanel();
		panel.add(RightPanel);
		RightPanel.setLayout(null);
		{
			final JPanel buttonPane = new JPanel();
			buttonPane.setBorder(new LineBorder(new Color(0, 0, 0)));
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("Evaluate");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
		
		final List<JSlider> allMetricSliders = new ArrayList<>();
		final List<JTextField> allMetricTextFields = new ArrayList<>();
		JTextField textField_1 = new JTextField();
		
		// Ok button for starting the evaluation.
		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{	
				
				if (Double.valueOf(textFieldThinkTime.getText().toString()).doubleValue() <= 0)
				{
					app.addTextToAnalysisPanel("Invalid think time, setting to 0.05");
					textFieldThinkTime.setText("0.05");
				}
				
				try
				{
					if (Integer.valueOf(textFieldMaxTurns.getText().toString()).intValue() <= 0)
					{
						app.addTextToAnalysisPanel("Invalid maximum number of turns, setting to 50");
						textFieldMaxTurns.setText("50");
					}
				}
				catch (final NumberFormatException exception)
				{
					app.addTextToAnalysisPanel("Invalid maximum number of turns, setting to 50");
					textFieldMaxTurns.setText("50");
				}
				
				try
				{
					if (Integer.valueOf(textFieldNumberTrials.getText().toString()).intValue() <= 0)
					{
						app.addTextToAnalysisPanel("Invalid number of trials, setting to 10");
						textFieldNumberTrials.setText("10");
					}
				}
				catch (final NumberFormatException exception)
				{
					app.addTextToAnalysisPanel("Invalid number of trials, setting to 10");
					textFieldNumberTrials.setText("10");
				}
				
				try
				{
					if (Double.valueOf(textFieldMinIdealTurns.getText().toString()).doubleValue() < 0)
					{
						app.addTextToAnalysisPanel("Invalid minimum number of ideal turns, setting to 0");
						textFieldMinIdealTurns.setText("0");
					}
				}
				catch (final NumberFormatException exception)
				{
					app.addTextToAnalysisPanel("Invalid minimum number of ideal turns, setting to 0");
					textFieldMinIdealTurns.setText("0");
				}
				
				try
				{
					if (Double.valueOf(textFieldMaxIdealTurns.getText().toString()).doubleValue() <= 0)
					{
						app.addTextToAnalysisPanel("Invalid maximum number of ideal turns, setting to 1000");
						textFieldMaxIdealTurns.setText("1000");
					}
				}
				catch (final NumberFormatException exception)
				{
					app.addTextToAnalysisPanel("Invalid maximum number of ideal turns, setting to 1000");
					textFieldMaxIdealTurns.setText("1000");
				}
				
				final int maxTurns = Integer.valueOf(textFieldMaxTurns.getText().toString()).intValue();
				final int numberIterations = Integer.valueOf(textFieldNumberTrials.getText().toString()).intValue();
				
				double thinkTime = 0.5;
				String AIName = null;
				
				switch(comboBoxAIAgents.getSelectedItem().toString())
				{
					case "Random": AIName = "Random"; break;
					case "Very weak AI": AIName = "Ludii AI"; thinkTime = 0.1; break;
					case "Weak AI": AIName = "Ludii AI"; thinkTime = 0.5; break;
					case "Strong AI": AIName = "Ludii AI"; thinkTime = 2.0; break;
					case "Very strong AI": AIName = "Ludii AI"; thinkTime = 5.0; break;
					case "Custom": AIName = comboBoxAlgorithm.getSelectedItem().toString(); thinkTime = Double.valueOf(textFieldThinkTime.getText()).doubleValue(); break;
				}
				
				for (final Metric m : metrics)
				{
					if (m instanceof IdealDuration)
					{
						((IdealDuration)m).setMinTurn(Double.valueOf(textFieldMinIdealTurns.getText()).doubleValue());
						((IdealDuration)m).setMaxTurn(Double.valueOf(textFieldMaxIdealTurns.getText()).doubleValue());
					}
					
					if (m instanceof SkillTrace)
					{
						((SkillTrace)m).setNumMatches(Integer.valueOf(textFieldNumMatches.getText()).intValue());
						((SkillTrace)m).setNumTrialsPerMatch(Integer.valueOf(textFieldNumTrialsPerMatch.getText()).intValue());
						((SkillTrace)m).setHardTimeLimit(Integer.valueOf(textFieldHardTimeLimit.getText()).intValue());
						((SkillTrace)m).setOutputPath(txtcommonresoutput.getText() + File.separatorChar);
					}
				}
				
				final Report report = new Report();
				report.setReportMessageFunctions(new ReportMessengerGUI(app));
				
				// Make a deepcopy of the weights to be used.
				final ArrayList<Double> weightsCopy = new ArrayList<>();
				for (final Double d : weights)
					weightsCopy.add(new Double(d.doubleValue()));
				
				AIPlayer.AIEvalution(app, report, numberIterations, maxTurns, thinkTime, AIName, metrics, weightsCopy, useDatabaseTrialsCheckBox.isSelected());
				DesktopApp.view().tabPanel().select(TabView.PanelAnalysis);
			}
		});
		
		// Skill trace only button for starting the evaluation.
		skillTraceButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{	
				for (int i = 0; i < weights.size(); i++)
				{
					double value = 0.0;
					if (metrics.get(i).name().equals("Skill trace"))
						value = 1.0;
						
					weights.set(i, Double.valueOf(value));
				}

				for(final ActionListener a: okButton.getActionListeners()) {
				    a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null) {
				          // Nothing needs go here.
				    });
				}
				
				// Reset weights back afterwards
				for (int i = 0; i < allMetricSliders.size(); i++)
					weights.set(i, Double.valueOf(allMetricTextFields.get(i).getText()));
			}
		});
		
		// Set the branching factor (quickly) when the dialog is loaded.
		final double brachingFactor = estimateBranchingFactor(app, 1);
		if (brachingFactor != 0)
		{
			textFieldMinIdealTurns.setText(String.valueOf(brachingFactor));
			textFieldMaxIdealTurns.setText(String.valueOf(brachingFactor*2));
		}
		
		int currentYDistance = 60;
		int currentXDistance = 0;
		
		// Each Metric has a slider to represent its weight value;
		for (int i = 0; i < metrics.size(); i++)
		{
			final int metricIndex = i;
			final Metric m = metrics.get(metricIndex);
			
			final JLabel metricNameLabel = new JLabel(m.name());
			metricNameLabel.setBounds(currentXDistance+110, currentYDistance-30, 200, 19);
			RightPanel.add(metricNameLabel);
			final String metricInfo = m.notes();
			
			metricNameLabel.addMouseListener(new MouseAdapter() 
			{
		    	@Override
				public void mouseEntered(final MouseEvent evt) 
		        {
		    		metricNameLabel.setToolTipText(metricInfo);
		        }
		      });

			final JSlider slider = new JSlider();
			slider.setMinorTickSpacing(1);
			slider.setMajorTickSpacing(10);
			slider.setValue(100);
			slider.setMinimum(-100);
			slider.setBounds(currentXDistance+110, currentYDistance+30, 162, 16);
			RightPanel.add(slider);

			textField_1 = new JTextField();
			textField_1.setEditable(false);
			textField_1.setBounds(currentXDistance+110, currentYDistance, 162, 19);
			RightPanel.add(textField_1);
			textField_1.setColumns(10);
			
			final JButton zeroButton = new JButton();
			zeroButton.setBounds(currentXDistance+20, currentYDistance, 70, 19);
			zeroButton.setText("Zero");
			RightPanel.add(zeroButton);
			
			// Zero button sets slider value to 0
			zeroButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					slider.setValue(0);
				}
			});

			currentYDistance = currentYDistance + 100;
			final double initialWeightValue = slider.getValue() / 100.0;
			
			if (currentYDistance > getHeight() - 150)
			{
				currentXDistance += 300;
				currentYDistance = 60;
				this.setSize(getWidth()+300, getHeight());
			}

			allMetricTextFields.add(textField_1);
			allMetricSliders.add(slider);

			weights.add(Double.valueOf(initialWeightValue));
			allMetricTextFields.get(metricIndex).setText(Double.toString(initialWeightValue));
			allMetricSliders.get(metricIndex).addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(final ChangeEvent arg0)
				{
					allMetricTextFields.get(metricIndex).setText(Double.toString(allMetricSliders.get(metricIndex).getValue() / 100.0));
					weights.set(metricIndex, Double.valueOf(allMetricTextFields.get(metricIndex).getText()));
				}
			});
		}
		
	}
	
	//-------------------------------------------------------------------------
	
	public static double estimateBranchingFactor(final PlayerApp app, final int numSecs)
	{
		if (!app.manager().ref().context().game().isDeductionPuzzle())
		{
			final TObjectDoubleHashMap<String> results = 
					Complexity.estimateBranchingFactor
					(
						app.manager().savedLudName(), 
						app.manager().settingsManager().userSelections(),
						numSecs
					);
			
			return results.get("Avg Trial Branching Factor");
		}
		return 0;
	}
}
