package app.display.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import app.DesktopApp;
import app.PlayerApp;
import app.display.dialogs.util.DialogUtil;
import app.display.util.DesktopGUIUtil;
import app.display.views.tabs.TabView;
import app.loading.GameLoading;
import game.Game;
import main.FileHandling;
import main.grammar.Description;
import metrics.DistanceMetric;
import metrics.Levenshtein;
import metrics.ZhangShasha;

/**
 * Dialog that is used to display various game distance options
 * 
 * @author Matthew.Stephenson
 */
public class DistanceDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	final JTextField textFieldThinkTime;
	DistanceMetric distanceMetric = new ZhangShasha();
	
	//-------------------------------------------------------------------------

	/**
	 * Show the Dialog.
	 */
	public static void showDialog(final PlayerApp app)
	{
		try
		{
			final DistanceDialog dialog = new DistanceDialog(app);
			DialogUtil.initialiseSingletonDialog(dialog, "Game Distance", null);
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
	public DistanceDialog(final PlayerApp app)
	{
		final JButton okButton;
		
		final JPanel contentPanel = new JPanel();
		setBounds(100, 100, 900, 500);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
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
		contentPanel.setLayout(null);

		final JLabel lblNewLabel = new JLabel("Number of Trials");
		lblNewLabel.setBounds(26, 41, 145, 15);
		contentPanel.add(lblNewLabel);

		final JTextField textFieldNumberTrials = new JTextField();
		textFieldNumberTrials.setBounds(290, 39, 102, 19);
		textFieldNumberTrials.setText("10");
		contentPanel.add(textFieldNumberTrials);
		textFieldNumberTrials.setColumns(10);

		final JLabel lblAiModes = new JLabel("AI Agents");
		lblAiModes.setBounds(26, 169, 91, 15);
		contentPanel.add(lblAiModes);

		final JComboBox<String> comboBoxAIAgents = new JComboBox<String>();
		comboBoxAIAgents.addItem("Random");
		comboBoxAIAgents.addItem("Very weak AI");
		comboBoxAIAgents.addItem("Weak AI");
		comboBoxAIAgents.addItem("Strong AI");
		comboBoxAIAgents.addItem("Very strong AI");
		comboBoxAIAgents.addItem("Custom");
		comboBoxAIAgents.setBounds(220, 164, 162, 24);
		comboBoxAIAgents.setEnabled(true);
		contentPanel.add(comboBoxAIAgents);

		final JButton btnSelectGame = new JButton("Select Game");
		btnSelectGame.setBounds(26, 300, 178, 25);
		contentPanel.add(btnSelectGame);
		
		final JTextField lblSelectedGame = new JTextField("");
		lblSelectedGame.setBounds(26, 350, 300, 25);
		contentPanel.add(lblSelectedGame);
		
		final JLabel labelMaxTurns = new JLabel("Maximum # Turns (per player)");
		labelMaxTurns.setBounds(26, 83, 175, 15);
		contentPanel.add(labelMaxTurns);
		
		final JTextField textFieldMaxTurns = new JTextField();
		textFieldMaxTurns.setBounds(280, 81, 102, 19);
		textFieldMaxTurns.setText("50");
		textFieldMaxTurns.setColumns(10);
		contentPanel.add(textFieldMaxTurns);
		
		final JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setBounds(430, 0, 8, 450);
		contentPanel.add(separator);
		
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
		
		final String[] comboBoxContents = DesktopGUIUtil.getAiStrings(app, false).toArray(new String[DesktopGUIUtil.getAiStrings(app, false).size()]);
		final JComboBox<String> comboBoxAlgorithm = new JComboBox<String>(comboBoxContents);
		comboBoxAlgorithm.setEnabled(false);
		comboBoxAlgorithm.setBounds(220, 207, 162, 24);
		contentPanel.add(comboBoxAlgorithm);
		
		
		// Update these buttons as new distance metrics are added.
		
		final JLabel lblDistanceAlgorihtm = new JLabel("Distance Algorihtm");
		lblDistanceAlgorihtm.setBounds(470, 41, 145, 15);
		contentPanel.add(lblDistanceAlgorihtm);
		
		final JRadioButton ZhangShashaButton = new JRadioButton("Zhang Sasha", true);
		ZhangShashaButton.setBounds(470, 80, 291, 15);
		contentPanel.add(ZhangShashaButton);
		
		final JRadioButton LevenshteinButton = new JRadioButton("Levenshtein", false);
		LevenshteinButton.setBounds(470, 120, 291, 15);
		contentPanel.add(LevenshteinButton);

		final ButtonGroup group = new ButtonGroup();
		group.add(ZhangShashaButton);
		group.add(LevenshteinButton);
		
		ZhangShashaButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{	
				if (ZhangShashaButton.isSelected())
				{
					distanceMetric = new ZhangShasha();
				}
			}
		});
		
		LevenshteinButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{	
				if (LevenshteinButton.isSelected())
				{
					distanceMetric = new Levenshtein();
				}
			}
		});
		

		
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
		
		
		// Ok button for starting the distance analysis.
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
				
				final int maxTurns = Integer.valueOf(textFieldMaxTurns.getText().toString()).intValue();
				final int numberTrials = Integer.valueOf(textFieldNumberTrials.getText().toString()).intValue();
				
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

				final List<Game> allGameB = getAllGamesFromCategories(app, lblSelectedGame.getText().split(";"));
				
				distanceMetric.distance(app.manager().ref().context().game(), allGameB, numberTrials, maxTurns, thinkTime, AIName);
				DesktopApp.view().tabPanel().select(TabView.PanelAnalysis);
			}
		});
		
		btnSelectGame.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				try
				{
		        	final String[] choices = FileHandling.listGames();
	        		String initialChoice = choices[0];
	        		for (final String choice : choices)
	        		{
	        			if (app.manager().savedLudName() != null && app.manager().savedLudName().endsWith(choice.replaceAll(Pattern.quote("\\"), "/")))
	        			{
	        				initialChoice = choice;
	        				break;
	        			}
	        		}
	        		final String choice = GameLoaderDialog.showDialog(DesktopApp.frame(), choices, initialChoice);

	    			final String str = choice.replaceAll(Pattern.quote("\\"), "/");
	    			final String[] parts = str.split("/");
	    			
	    			lblSelectedGame.setText("");
	    			String gameCategoriesString = "";
	    			
	    			for (final String s : parts)
	    			{
	    				if (s.length() > 1)
	    				{
	    					gameCategoriesString += s + "; ";
	    				}
	    			}
	    			
	    			lblSelectedGame.setText(gameCategoriesString);
				}
				catch (final Exception e1)
				{
					// Probably just cancelled the game loader.
				}
			}	
		});
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns a list of all Games in Ludii that include all required category strings in the path.
	 * @param categoriesToMatch
	 * @return
	 */
	static List<Game> getAllGamesFromCategories(final PlayerApp app, final String[] categoriesToMatch)
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
				if (!Arrays.asList(gameCategories).contains(categoryToMatch.trim()))
				{
					validGame = false;
					break;
				}
			}
			if (validGame)
			{
				final String gameDescription = GameLoading.getGameDescriptionRawFromName(app, path);
				validGames.add(new Game(path, new Description(gameDescription)));
				
			}
		}
		return validGames;
	}
}
