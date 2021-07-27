package app.display.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.json.JSONObject;

import app.DesktopApp;
import app.PlayerApp;
import app.display.dialogs.util.DialogUtil;
import app.display.dialogs.util.MaxLengthTextDocument;
import app.display.util.DesktopGUIUtil;
import app.display.views.tabs.TabView;
import app.utils.AnimationVisualsType;
import main.Constants;
import manager.ai.AIDetails;
import manager.ai.AIMenuName;
import manager.ai.AIUtil;
import other.context.Context;

/**
 * Dialog for changing various user settings.
 * Mostly auto-generated code.
 *
 * @author matthew.stephenson
 */
@SuppressWarnings("unchecked")
public class SettingsDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	final JTextField textFieldThinkingTimeAll;
	private static JTabbedPane tabbedPane = null;
	private static JPanel playerPanel = null;
	private static JPanel otherPanel = null;
	
	static SettingsDialog dialog;
	
	static JTextField[] playerNamesArray = new JTextField[Constants.MAX_PLAYERS+1];
	static JComboBox<String>[] playerAgentsArray = new JComboBox[Constants.MAX_PLAYERS+1];
	static JComboBox<String>[] playerThinkTimesArray = new JComboBox[Constants.MAX_PLAYERS+1];

	/** We temporarily set this to true to ignore extra events generated when switching AI for ALL players. */
	static boolean ignorePlayerComboBoxEvents = false;
	JTextField textFieldMaximumNumberOfTurns;
	JTextField textFieldTabFontSize;
	JTextField textFieldEditorFontSize;
	JTextField textFieldTickLength;
	
	//-------------------------------------------------------------------------

	/**
	 * Show the Dialog.
	 */
	public static void createAndShowGUI(final PlayerApp app)
	{
		try
		{
			dialog = new SettingsDialog(app);
			DialogUtil.initialiseSingletonDialog(dialog, "Preferences", new Rectangle(DesktopApp.frame().getX() + DesktopApp.frame().getWidth()/2 - 240, DesktopApp.frame().getY(), 500, DesktopApp.frame().getHeight()));
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
	public SettingsDialog(final PlayerApp app)
	{
		super(null, java.awt.Dialog.ModalityType.DOCUMENT_MODAL);
		setTitle("Settings");
		
		final Context context = app.contextSnapshot().getContext(app);

		//setBounds(100, 100, 468, 900);
		getContentPane().setLayout(new BorderLayout(0, 0));

		tabbedPane = new JTabbedPane(SwingConstants.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
	

		playerPanel = new JPanel();
		//playerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		final JScrollPane scroll = new JScrollPane(playerPanel);
		tabbedPane.addTab("Player", null, scroll, null);

		final int numPlayers = context.game().players().count();
		
		final int playerSectionHeight = numPlayers * 30;
		
		addPlayerDetails(app, playerPanel, 1, context);
		addPlayerDetails(app, playerPanel, 2, context);
		addPlayerDetails(app, playerPanel, 3, context);
		addPlayerDetails(app, playerPanel, 4, context);
		addPlayerDetails(app, playerPanel, 5, context);
		addPlayerDetails(app, playerPanel, 6, context);
		addPlayerDetails(app, playerPanel, 7, context);
		addPlayerDetails(app, playerPanel, 8, context);
		addPlayerDetails(app, playerPanel, 9, context);
		addPlayerDetails(app, playerPanel, 10, context);
		addPlayerDetails(app, playerPanel, 11, context);
		addPlayerDetails(app, playerPanel, 12, context);
		addPlayerDetails(app, playerPanel, 13, context);
		addPlayerDetails(app, playerPanel, 14, context);
		addPlayerDetails(app, playerPanel, 15, context);
		addPlayerDetails(app, playerPanel, 16, context);
		
		textFieldThinkingTimeAll = new JTextField();
		textFieldThinkingTimeAll.setBounds(275, 165 + playerSectionHeight, 103, 20);
		textFieldThinkingTimeAll.setEnabled(false);
		if (numPlayers > 1)
			textFieldThinkingTimeAll.setEnabled(true);
		textFieldThinkingTimeAll.setColumns(10);
		textFieldThinkingTimeAll.setText("-");

		final JLabel lblName = new JLabel("Players");
		lblName.setBounds(39, 32, 137, 21);
		lblName.setFont(new Font("Dialog", Font.BOLD, 16));

		final ArrayList<String> aiStringsBlank = DesktopGUIUtil.getAiStrings(app, true);
		aiStringsBlank.add("-");
		
		final JComboBox<?> comboBoxAgentAll = new JComboBox<>(aiStringsBlank.toArray());
		comboBoxAgentAll.setBounds(118, 165 + playerSectionHeight, 134, 22);
		comboBoxAgentAll.setEnabled(false);
		if (numPlayers > 1)
		{
			comboBoxAgentAll.setEnabled(true);
			comboBoxAgentAll.setSelectedIndex(aiStringsBlank.size() - 1);
		}


		final DocumentListener documentListenerMaxTurns = new DocumentListener()
		{
			@Override
			public void changedUpdate(final DocumentEvent documentEvent)
			{
				update(documentEvent);
			}

			@Override
			public void insertUpdate(final DocumentEvent documentEvent)
			{
				update(documentEvent);
			}

			@Override
			public void removeUpdate(final DocumentEvent documentEvent)
			{
				update(documentEvent);
			}

			private void update(final DocumentEvent documentEvent)
			{
				int numberTurns = Constants.DEFAULT_TURN_LIMIT;
				try
				{
					numberTurns = Integer.parseInt(textFieldMaximumNumberOfTurns.getText());
				}
				catch (final Exception e)
				{
					// not an integer;
				}
				
				context.game().setMaxTurns(numberTurns);
				app.manager().settingsManager().setTurnLimit(context.game().name(),numberTurns);

				app.repaint();
				
			}
		};
		
		
		final DocumentListener documentListenerTickLength = new DocumentListener()
		{
			@Override
			public void changedUpdate(final DocumentEvent documentEvent)
			{
				update(documentEvent);
			}

			@Override
			public void insertUpdate(final DocumentEvent documentEvent)
			{
				update(documentEvent);
			}

			@Override
			public void removeUpdate(final DocumentEvent documentEvent)
			{
				update(documentEvent);
			}

			private void update(final DocumentEvent documentEvent)
			{
				app.manager().settingsManager().setAgentsPaused(app.manager(), true);
				double tickLength = app.manager().settingsManager().tickLength();
				try
				{
					tickLength = Double.parseDouble(textFieldTickLength.getText());
				}
				catch (final Exception e)
				{
					// not an integer;
				}
				
				app.manager().settingsManager().setTickLength(tickLength);
				app.repaint();
			}
		};
		
		
		final DocumentListener documentListenerTabFontSize = new DocumentListener()
		{
			@Override
			public void changedUpdate(final DocumentEvent documentEvent)
			{
				update(documentEvent);
			}

			@Override
			public void insertUpdate(final DocumentEvent documentEvent)
			{
				update(documentEvent);
			}

			@Override
			public void removeUpdate(final DocumentEvent documentEvent)
			{
				update(documentEvent);
			}

			private void update(final DocumentEvent documentEvent)
			{
				int tabFontSize = app.settingsPlayer().tabFontSize();
				try
				{
					tabFontSize = Integer.parseInt(textFieldTabFontSize.getText());
				}
				catch (final Exception e)
				{
					// not an integer;
				}
				
				app.settingsPlayer().setTabFontSize(tabFontSize);
				DesktopApp.view().getPanels().clear();
				app.repaint();
			}
		};
		
		final DocumentListener documentListenerEditorFontSize = new DocumentListener()
		{
			@Override
			public void changedUpdate(final DocumentEvent documentEvent)
			{
				update(documentEvent);
			}

			@Override
			public void insertUpdate(final DocumentEvent documentEvent)
			{
				update(documentEvent);
			}

			@Override
			public void removeUpdate(final DocumentEvent documentEvent)
			{
				update(documentEvent);
			}

			private void update(final DocumentEvent documentEvent)
			{
				int editorFontSize = app.settingsPlayer().editorFontSize();
				try
				{
					editorFontSize = Integer.parseInt(textFieldEditorFontSize.getText());
				}
				catch (final Exception e)
				{
					// not an integer;
				}
				
				app.settingsPlayer().setEditorFontSize(editorFontSize);
				//DesktopApp.view().getPanels().clear();
				//app.repaint();
			}
		};



		final JLabel lblAgent = new JLabel("Agent");
		lblAgent.setBounds(122, 135 + playerSectionHeight, 119, 19);
		lblAgent.setFont(new Font("Dialog", Font.BOLD, 16));

		final JLabel lblAllPlayers = new JLabel("All Players");
		lblAllPlayers.setBounds(29, 170 + playerSectionHeight, 78, 14);

		final JLabel lblThinkingTime = new JLabel("Thinking time");
		lblThinkingTime.setBounds(275, 135 + playerSectionHeight, 123, 19);
		lblThinkingTime.setFont(new Font("Dialog", Font.BOLD, 16));
		
		final JButton btnApply = new JButton("Apply");
		btnApply.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnApply.setBounds(339, 238 + playerSectionHeight, 97, 29);
		
		btnApply.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{

				// AI type for all
				if (comboBoxAgentAll.getSelectedIndex() != aiStringsBlank.size() - 1)
				{
					// set AI name/type
					for (int i = 1; i <= Constants.MAX_PLAYERS; i++)
						playerAgentsArray[i].setSelectedItem(comboBoxAgentAll.getSelectedItem().toString());
				}

				// AI search time for all
				try
				{
					double allSearchTimeValue = Double.valueOf(textFieldThinkingTimeAll.getText()).doubleValue();
					if (allSearchTimeValue <= 0)
						allSearchTimeValue = 1.0;

					for (int i = 1; i <= Constants.MAX_PLAYERS; i++)
						playerThinkTimesArray[i].setSelectedItem(Double.valueOf(allSearchTimeValue));
				}
				catch (final Exception E)
				{
					// invalid think time
				}

				applyPlayerDetails(app, context);
				
				app.manager().settingsNetwork().backupAiPlayers(app.manager());

				// clear and recreate the panels
				EventQueue.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						DesktopApp.view().createPanels();
					}
				});
				dispose();
			}
		});
		playerPanel.setLayout(null);
		playerPanel.setPreferredSize(new Dimension(450, 320 + playerSectionHeight));
		
		final JSeparator separator_3 = new JSeparator();
		separator_3.setBounds(0, 116 + playerSectionHeight, 475, 8);
		playerPanel.add(separator_3);
		playerPanel.add(lblName);
		playerPanel.add(btnApply);
		playerPanel.add(lblAllPlayers);
		playerPanel.add(lblAgent);
		playerPanel.add(comboBoxAgentAll);
		playerPanel.add(textFieldThinkingTimeAll);
		playerPanel.add(lblThinkingTime);
		
		final JSeparator separator_4 = new JSeparator();
		separator_4.setBounds(0, 216 + playerSectionHeight, 475, 8);
		playerPanel.add(separator_4);
		
		final JButton buttonResetPlayerNames = new JButton("");
		buttonResetPlayerNames.setBounds(212, 242 + playerSectionHeight, 26, 23);
		playerPanel.add(buttonResetPlayerNames);
		
		buttonResetPlayerNames.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				for (int i = 1; i < app.manager().aiSelected().length; i++)
				{
					app.manager().aiSelected()[context.state().playerToAgent(i)].setName("Player " + i);
					playerNamesArray[i].setText("Player " + i);
					app.manager().aiSelected()[i].setThinkTime(1.0);
					final JSONObject json = new JSONObject().put("AI",
							new JSONObject()
							.put("algorithm", "Human")
							);
					AIUtil.updateSelectedAI(app.manager(), json, i, AIMenuName.getAIMenuName("Human"));
					applyPlayerDetails(app, context);
				}
				createAndShowGUI(app);
			}
		});
		
		if (app.manager().settingsNetwork().getActiveGameId() != 0)
		{
			comboBoxAgentAll.setEnabled(false);
			textFieldThinkingTimeAll.setEnabled(false);
			buttonResetPlayerNames.setEnabled(false);
			if (!app.manager().settingsNetwork().getOnlineAIAllowed())
				btnApply.setEnabled(false);
		}
		
		final JLabel label = new JLabel("Reset Names to Defaults");
		label.setFont(new Font("Dialog", Font.PLAIN, 14));
		label.setBounds(29, 247 + playerSectionHeight, 192, 17);
		playerPanel.add(label);

		otherPanel = new JPanel();
		//otherPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		final JScrollPane scroll2 = new JScrollPane(otherPanel);
		tabbedPane.addTab("Advanced", null, scroll2, null);

		final JComboBox<String> comboBox = new JComboBox<>();
		comboBox.setBounds(320, 780, 124, 22);

		final JLabel lblPieceStyles = new JLabel("Piece Style");
		lblPieceStyles.setBounds(30, 780, 155, 21);
		lblPieceStyles.setFont(new Font("Dialog", Font.BOLD, 14));

		String[] pieceDesigns = context.game().metadata().graphics().pieceFamilies();
		if (pieceDesigns == null)
			pieceDesigns = new String[0];
		
		for (final String s : pieceDesigns)
			comboBox.addItem(s);

		if (!app.bridge().settingsVC().pieceFamily(context.game().name()).equals(""))
			comboBox.setSelectedItem(app.bridge().settingsVC().pieceFamily(context.game().name()));

		comboBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setPieceFamily(context.game().name(), comboBox.getSelectedItem().toString());
				app.graphicsCache().clearAllCachedImages();
				app.repaint();
				
			}
		});
		
		if (comboBox.getItemCount() == 0)
			comboBox.setEnabled(false);
		else
			comboBox.setEnabled(true);
		
		otherPanel.add(lblPieceStyles);
		otherPanel.add(comboBox);

		final JLabel lblMaximumNumberOfTurns = new JLabel("Maximum turn limit (per player)");
		lblMaximumNumberOfTurns.setBounds(30, 40, 281, 19);
		lblMaximumNumberOfTurns.setFont(new Font("Dialog", Font.BOLD, 14));
		lblMaximumNumberOfTurns.setToolTipText("<html>The maximum number of turns each player is allowed to make.<br>"
				+ "If a player has had more turns than this value, the game is a draw for all remaining active players.</html>");

		textFieldMaximumNumberOfTurns = new JTextField();
		textFieldMaximumNumberOfTurns.setBounds(321, 40, 86, 20);
		textFieldMaximumNumberOfTurns.setColumns(10);

		// AlwaysAutoPass
		final JLabel lblAlwaysAutoPass = new JLabel("Auto Pass Stochastic Game");
		lblAlwaysAutoPass.setFont(new Font("Dialog", Font.BOLD, 14));
		lblAlwaysAutoPass.setBounds(30, 120, 227, 17);
		otherPanel.add(lblAlwaysAutoPass);
		
		final JCheckBox checkBoxAlwaysAutoPass = new JCheckBox("yes");
		checkBoxAlwaysAutoPass.setSelected(false);
		checkBoxAlwaysAutoPass.setBounds(321, 120, 86, 23);
		otherPanel.add(checkBoxAlwaysAutoPass);
		
		// Coordinate outline
		final JLabel lblCoordOutline = new JLabel("Coordinate outline");
		lblCoordOutline.setFont(new Font("Dialog", Font.BOLD, 14));
		lblCoordOutline.setBounds(30, 160, 227, 17);
		otherPanel.add(lblCoordOutline);
		
		final JCheckBox checkBoxCoordOutline = new JCheckBox("yes");
		checkBoxCoordOutline.setSelected(false);
		checkBoxCoordOutline.setBounds(321, 160, 86, 23);
		otherPanel.add(checkBoxCoordOutline);
		
		// Developer options
		final JLabel lblDevMode = new JLabel("Developer options");
		lblDevMode.setBounds(30, 200, 227, 17);
		lblDevMode.setFont(new Font("Dialog", Font.BOLD, 14));
		
		final JCheckBox checkBoxDevMode = new JCheckBox("yes");
		checkBoxDevMode.setBounds(321, 200, 86, 23);
		
		// Hide AI moves
		final JLabel lblHideAiPieces = new JLabel("Hide AI moves");
		lblHideAiPieces.setBounds(30, 240, 227, 17);
		lblHideAiPieces.setFont(new Font("Dialog", Font.BOLD, 14));

		final JCheckBox radioButtonHideAiMoves = new JCheckBox("yes");
		radioButtonHideAiMoves.setBounds(321, 240, 86, 23);
		radioButtonHideAiMoves.setSelected(true);
		
		// Movement animation
		final JLabel lblShowMovementAnimation = new JLabel("Movement animation");
		lblShowMovementAnimation.setBounds(30, 280, 227, 17);
		lblShowMovementAnimation.setFont(new Font("Dialog", Font.BOLD, 14));

		final String[] animationTypes = new String[] { "None", "Single", "All" };
		final JComboBox<String> comboBoxMovementAnimation = new JComboBox<>();
		comboBoxMovementAnimation.setBounds(321, 280, 86, 23);
		for (final String s : animationTypes)
			comboBoxMovementAnimation.addItem(s);
		
		for (int i = 0; i < animationTypes.length; i++)
			if (animationTypes[i].equals(app.settingsPlayer().animationType().name()))
				comboBoxMovementAnimation.setSelectedIndex(i);

		comboBoxMovementAnimation.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.settingsPlayer().setAnimationType(AnimationVisualsType.valueOf(comboBoxMovementAnimation.getSelectedItem().toString()));
			}
		});

		comboBoxMovementAnimation.setEnabled(true);
		
		// Moves use coordinates
		final JLabel lblMovesUseCoordinates = new JLabel("Moves use coordinates");
		lblMovesUseCoordinates.setBounds(30, 320, 227, 17);
		lblMovesUseCoordinates.setFont(new Font("Dialog", Font.BOLD, 14));

		final JCheckBox rdbtnMoveCoord = new JCheckBox("yes");
		rdbtnMoveCoord.setBounds(321, 320, 86, 23);

		// Sound options
		final JLabel lblSoundEffectAfter = new JLabel("<html>Sound effect after AI or<br>network move</html>");
		lblSoundEffectAfter.setBounds(30, 480, 227, 34);
		lblSoundEffectAfter.setFont(new Font("Dialog", Font.BOLD, 14));
		
		final JCheckBox checkBoxSoundEffect = new JCheckBox("yes");
		checkBoxSoundEffect.setBounds(321, 480, 86, 23);
		checkBoxSoundEffect.setSelected(false);
		
		// Move format
		final JLabel lblMoveFormat = new JLabel("Move Format");
		lblMoveFormat.setBounds(30, 530, 227, 17);
		lblMoveFormat.setFont(new Font("Dialog", Font.BOLD, 14));

		final String[] moveFormat = new String[] { "Move", "Short", "Full" };
		final JComboBox<String> comboBoxFormat = new JComboBox<>();
		comboBoxFormat.setBounds(321, 530, 86, 23);
		for (final String s : moveFormat)
			comboBoxFormat.addItem(s);
		
		for (int i = 0; i < moveFormat.length; i++)
			if (moveFormat[i].equals(app.settingsPlayer().moveFormat()))
				comboBoxFormat.setSelectedIndex(i);

		comboBoxFormat.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.settingsPlayer().setMoveFormat(comboBoxFormat.getSelectedItem().toString());
				DesktopApp.view().tabPanel().page(TabView.PanelMoves).updatePage(context);
			}
		});

		comboBoxFormat.setEnabled(true);
		
		final JLabel lblTabFontSize = new JLabel("Tab Font Size");
		lblTabFontSize.setBounds(30, 580, 281, 19);
		lblTabFontSize.setFont(new Font("Dialog", Font.BOLD, 14));
		lblTabFontSize.setToolTipText("<html>The font size for the text displayed in the tabs.<br>");

		textFieldTabFontSize = new JTextField();
		textFieldTabFontSize.setBounds(321, 580, 86, 20);
		textFieldTabFontSize.setColumns(10);
		
		textFieldTabFontSize.setText("" + app.settingsPlayer().tabFontSize());		
		textFieldTabFontSize.getDocument().addDocumentListener(documentListenerTabFontSize);
		
		
		
		final JLabel lblEditorFontSize = new JLabel("Editor Font Size");
		lblEditorFontSize.setBounds(30, 620, 281, 19);
		lblEditorFontSize.setFont(new Font("Dialog", Font.BOLD, 14));
		lblEditorFontSize.setToolTipText("<html>The font size for the text displayed in the editor.<br>");

		textFieldEditorFontSize = new JTextField();
		textFieldEditorFontSize.setBounds(321, 620, 86, 20);
		textFieldEditorFontSize.setColumns(10);
		
		textFieldEditorFontSize.setText("" + app.settingsPlayer().editorFontSize());		
		textFieldEditorFontSize.getDocument().addDocumentListener(documentListenerEditorFontSize);
		
		final JCheckBox checkBoxEditorAutocomplete = new JCheckBox("yes");
		checkBoxEditorAutocomplete.setSelected(false);
		checkBoxEditorAutocomplete.setBounds(321, 660, 86, 23);
		otherPanel.add(checkBoxEditorAutocomplete);

		final JCheckBox checkBoxNetworkPolling = new JCheckBox("yes");
		checkBoxNetworkPolling.setSelected(false);
		checkBoxNetworkPolling.setBounds(321, 700, 86, 23);
		otherPanel.add(checkBoxNetworkPolling);
		
		final JCheckBox checkBoxNetworkRefresh = new JCheckBox("yes");
		checkBoxNetworkRefresh.setSelected(false);
		checkBoxNetworkRefresh.setBounds(321, 740, 86, 23);
		otherPanel.add(checkBoxNetworkRefresh);
		
		final JLabel labelPauseAI = new JLabel("|< Button Pauses AI");
		labelPauseAI.setFont(new Font("Dialog", Font.BOLD, 14));
		labelPauseAI.setBounds(30, 360, 227, 17);
		otherPanel.add(labelPauseAI);
		
		final JCheckBox checkBoxPauseAI = new JCheckBox("yes");
		checkBoxPauseAI.setSelected(true);
		checkBoxPauseAI.setBounds(321, 360, 86, 23);
		otherPanel.add(checkBoxPauseAI);
		
		final JLabel labelSaveTrial = new JLabel("Save Trial After Moves");
		labelSaveTrial.setFont(new Font("Dialog", Font.BOLD, 14));
		labelSaveTrial.setBounds(30, 400, 227, 17);
		otherPanel.add(labelSaveTrial);
		
		final JCheckBox checkBoxSaveTrial = new JCheckBox("yes");
		checkBoxSaveTrial.setSelected(true);
		checkBoxSaveTrial.setBounds(321, 400, 86, 23);
		otherPanel.add(checkBoxSaveTrial);
		
		final JLabel labelFlatBoard = new JLabel("Flat Board");
		labelFlatBoard.setFont(new Font("Dialog", Font.BOLD, 14));
		labelFlatBoard.setBounds(30, 440, 227, 17);
		otherPanel.add(labelFlatBoard);
		
		final JCheckBox checkBoxFlatBoard = new JCheckBox("yes");
		checkBoxFlatBoard.setSelected(true);
		checkBoxFlatBoard.setBounds(321, 440, 86, 23);
		otherPanel.add(checkBoxFlatBoard);
		
		
		checkBoxDevMode.setSelected(app.settingsPlayer().devMode());
		checkBoxDevMode.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.settingsPlayer().setDevMode(checkBoxDevMode.isSelected());
				app.resetMenuGUI();
				app.repaint();
			}
		});
		
		checkBoxSoundEffect.setSelected(app.settingsPlayer().isMoveSoundEffect());
		checkBoxSoundEffect.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.settingsPlayer().setMoveSoundEffect(checkBoxSoundEffect.isSelected());
			}
		});
		
		checkBoxCoordOutline.setSelected(app.bridge().settingsVC().coordWithOutline());
		checkBoxCoordOutline.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setCoordWithOutline(checkBoxCoordOutline.isSelected());
				app.repaint();
			}
		});

		checkBoxAlwaysAutoPass.setSelected(app.manager().settingsManager().alwaysAutoPass());
		checkBoxAlwaysAutoPass.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.manager().settingsManager().setAlwaysAutoPass(checkBoxAlwaysAutoPass.isSelected());
				app.repaint();
			}
		});

		radioButtonHideAiMoves.setSelected(app.settingsPlayer().hideAiMoves());
		radioButtonHideAiMoves.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.settingsPlayer().setHideAiMoves(radioButtonHideAiMoves.isSelected());
			}
		});


		rdbtnMoveCoord.setSelected(app.settingsPlayer().isMoveCoord());
		rdbtnMoveCoord.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.settingsPlayer().setMoveCoord(rdbtnMoveCoord.isSelected());
				app.updateTabs(context);
			}
		});
		
		checkBoxPauseAI.setSelected(app.settingsPlayer().startButtonPausesAI());
		checkBoxPauseAI.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.settingsPlayer().setStartButtonPausesAI(checkBoxPauseAI.isSelected());
			}
		});
		
		checkBoxSaveTrial.setSelected(app.settingsPlayer().saveTrialAfterMove());
		checkBoxSaveTrial.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.settingsPlayer().setSaveTrialAfterMove(checkBoxSaveTrial.isSelected());
			}
		});
		
		checkBoxFlatBoard.setSelected(app.bridge().settingsVC().flatBoard());
		checkBoxFlatBoard.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setFlatBoard(checkBoxFlatBoard.isSelected());
				DesktopApp.view().createPanels();
				app.repaint();
			}
		});
		
		checkBoxEditorAutocomplete.setSelected(app.settingsPlayer().editorAutocomplete());
		checkBoxEditorAutocomplete.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.settingsPlayer().setEditorAutocomplete(checkBoxEditorAutocomplete.isSelected());
			}
		});
		
		checkBoxNetworkPolling.setSelected(app.manager().settingsNetwork().longerNetworkPolling());
		checkBoxNetworkPolling.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.manager().settingsNetwork().setLongerNetworkPolling(checkBoxNetworkPolling.isSelected());
			}
		});
		
		checkBoxNetworkRefresh.setSelected(app.manager().settingsNetwork().noNetworkRefresh());
		checkBoxNetworkRefresh.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.manager().settingsNetwork().setNoNetworkRefresh(checkBoxNetworkRefresh.isSelected());
			}
		});
		
		textFieldTickLength = new JTextField();
		textFieldTickLength.setText("0");
		textFieldTickLength.setColumns(10);
		textFieldTickLength.setBounds(321, 76, 86, 20);
		otherPanel.add(textFieldTickLength);


		textFieldMaximumNumberOfTurns.setText("" + context.game().getMaxTurnLimit());
		textFieldTickLength.setText("" + app.manager().settingsManager().tickLength());
		otherPanel.setLayout(null);
		otherPanel.setPreferredSize(new Dimension(450, 850));
		otherPanel.add(lblShowMovementAnimation);
		otherPanel.add(comboBoxMovementAnimation);
		otherPanel.add(lblDevMode);
		otherPanel.add(checkBoxDevMode);
		otherPanel.add(lblSoundEffectAfter);
		otherPanel.add(checkBoxSoundEffect);
		otherPanel.add(lblHideAiPieces);
		otherPanel.add(radioButtonHideAiMoves);
		otherPanel.add(lblMovesUseCoordinates);
		otherPanel.add(rdbtnMoveCoord);
		otherPanel.add(lblMaximumNumberOfTurns);
		otherPanel.add(textFieldMaximumNumberOfTurns);
		otherPanel.add(lblTabFontSize);
		otherPanel.add(textFieldTabFontSize);
		otherPanel.add(lblEditorFontSize);
		otherPanel.add(textFieldEditorFontSize);
		otherPanel.add(lblMoveFormat);
		otherPanel.add(comboBoxFormat);
		
		final JLabel tickLabel = new JLabel("Tick length (seconds)");
		tickLabel.setToolTipText("<html>The maximum number of turns each player is allowed to make.<br>If a player has had more turns than this value, the game is a draw for all remaining active players.</html>");
		tickLabel.setFont(new Font("Dialog", Font.BOLD, 14));
		tickLabel.setBounds(30, 76, 281, 19);
		otherPanel.add(tickLabel);
		
		final JLabel lblEditorAutocomplete = new JLabel("Editor Autocomplete");
		lblEditorAutocomplete.setToolTipText("<html>The font size for the text displayed in the editor.<br>");
		lblEditorAutocomplete.setFont(new Font("Dialog", Font.BOLD, 14));
		lblEditorAutocomplete.setBounds(30, 660, 281, 19);
		otherPanel.add(lblEditorAutocomplete);
		
		
		final JLabel lblNetworkPolling = new JLabel("Network Less frequent polling");
		lblNetworkPolling.setToolTipText("<html>Poll the Ludii server less often when logged in. Good for poor network connections<br>");
		lblNetworkPolling.setFont(new Font("Dialog", Font.BOLD, 14));
		lblNetworkPolling.setBounds(30, 700, 281, 19);
		otherPanel.add(lblNetworkPolling);
		
		final JLabel lblNetworkRefresh = new JLabel("Network disable auto-refresh");
		lblNetworkRefresh.setToolTipText("<html>Do not auto-refresh the network dialog.<br>");
		lblNetworkRefresh.setFont(new Font("Dialog", Font.BOLD, 14));
		lblNetworkRefresh.setBounds(30, 740, 281, 19);
		otherPanel.add(lblNetworkRefresh);
		
		textFieldMaximumNumberOfTurns.getDocument().addDocumentListener(documentListenerMaxTurns);
		textFieldTickLength.getDocument().addDocumentListener(documentListenerTickLength);

		if (app.manager().settingsNetwork().getActiveGameId() != 0)
		{
			textFieldMaximumNumberOfTurns.setEnabled(false);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Called for every player
	 * 
	 * @param playerJpanel
	 * @param playerId
	 * @param context
	 */
	static void addPlayerDetails(final PlayerApp app, final JPanel playerJpanel, final int playerId, final Context context)
	{
		// Player Labels (fixed)
		final int maxNameLength = 21;
		final JLabel lblPlayer_1 = new JLabel("Player " + playerId);
		lblPlayer_1.setBounds(20, 49 + playerId*30, 74, 14);
		playerJpanel.add(lblPlayer_1);
		
		// Player Names
		final JTextField textFieldPlayerName1 = new JTextField();
		textFieldPlayerName1.setBounds(100, 46 + playerId*30, 130, 20);
		final MaxLengthTextDocument maxLength1 = new MaxLengthTextDocument();
		maxLength1.setMaxChars(maxNameLength);
		textFieldPlayerName1.setDocument(maxLength1);
		textFieldPlayerName1.setEnabled(true);
		textFieldPlayerName1.setText(app.manager().aiSelected()[context.state().playerToAgent(playerId)].name());
		playerJpanel.add(textFieldPlayerName1);
		
		// AI Algorithm
		final AIDetails associatedAI = app.manager().aiSelected()[context.state().playerToAgent(playerId)];
		final String[] comboBoxContents = DesktopGUIUtil.getAiStrings(app, true).toArray(new String[DesktopGUIUtil.getAiStrings(app, true).size()]);
		final JComboBox<String> myComboBox = new JComboBox<String>(comboBoxContents); //comboBoxContents
		myComboBox.setBounds(240, 46 + playerId*30, 100, 20);
		if (!myComboBox.getSelectedItem().equals(associatedAI.menuItemName().label()))
			myComboBox.setSelectedItem(associatedAI.menuItemName().label());
		playerJpanel.add(myComboBox);
		
		// AI think time
		final String[] comboBoxContentsThinkTime = {"1s", "2s", "3s", "5s", "10s", "30s", "60s", "120s", "180s", "240s", "300s"};
		final JComboBox<String> myComboBoxThinkTime = new JComboBox<String>(comboBoxContentsThinkTime); //comboBoxContentsThinkTime
		myComboBoxThinkTime.setBounds(350, 46 + playerId*30, 60, 20);
		myComboBoxThinkTime.setEditable(true);	
		final DecimalFormat format = new DecimalFormat("0.#");
		myComboBoxThinkTime.setSelectedItem(String.valueOf(format.format(associatedAI.thinkTime())) + "s");
		playerJpanel.add(myComboBoxThinkTime);

		// Hide boxes for players not in this game.
		if (context.game().players().count() < playerId)
		{
			textFieldPlayerName1.setVisible(false);
			lblPlayer_1.setVisible(false);
			myComboBoxThinkTime.setVisible(false);
			myComboBox.setVisible(false);
		}
		
		// Hide certain details if this is a network game.
		if (app.manager().settingsNetwork().getActiveGameId() != 0)
		{
			textFieldPlayerName1.setEnabled(false);
			if (!app.manager().settingsNetwork().getOnlineAIAllowed() || playerId != app.manager().settingsNetwork().getNetworkPlayerNumber())
			{
				myComboBoxThinkTime.setEnabled(false);
				myComboBox.setEnabled(false);
			}
		}
		
		// Save selected entries.
		playerNamesArray[playerId] = textFieldPlayerName1;
		playerAgentsArray[playerId] = myComboBox;
		playerThinkTimesArray[playerId] = myComboBoxThinkTime;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Applies the selected details for the players
	 * @param context
	 */
	static void applyPlayerDetails(final PlayerApp app, final Context context)
	{
		for (int i = 1; i < playerNamesArray.length; i++)
		{
			// Player Names
			final String name = playerNamesArray[i].getText();
			if (name != null)
				app.manager().aiSelected()[context.state().playerToAgent(i)].setName(name);
			else
				app.manager().aiSelected()[context.state().playerToAgent(i)].setName("");
			
			// Think time
			String comboBoxThinkTime = playerThinkTimesArray[i].getSelectedItem().toString();
			if (comboBoxThinkTime.toLowerCase().charAt(comboBoxThinkTime.length()-1) == 's')
				comboBoxThinkTime = comboBoxThinkTime.substring(0, comboBoxThinkTime.length()-1);
			double thinkTime = 0.0;
			try
			{
				thinkTime = Double.valueOf(comboBoxThinkTime.toString()).doubleValue();
			}
			catch (final Exception e2)
			{
				System.out.println("Invalid think time: " + comboBoxThinkTime.toString());
			}
			if (thinkTime <= 0)
				thinkTime = 1;
			final int newPlayerIndex = app.contextSnapshot().getContext(app).state().playerToAgent(i);
			app.manager().aiSelected()[newPlayerIndex].setThinkTime(thinkTime);

			// AI agent
			final JSONObject json = new JSONObject().put("AI",
				new JSONObject()
				.put("algorithm", playerAgentsArray[i].getSelectedItem().toString())
				);
			AIUtil.updateSelectedAI(app.manager(), json, newPlayerIndex, AIMenuName.getAIMenuName(playerAgentsArray[i].getSelectedItem().toString()));
			
			// Need to initialise the AI if "Ludii AI" selected, so we can get the algorithm name.
			if (playerAgentsArray[i].getSelectedItem().toString().equals("Ludii AI"))
				app.manager().aiSelected()[newPlayerIndex].ai().initIfNeeded(app.contextSnapshot().getContext(app).game(), newPlayerIndex);
			
			app.manager().settingsNetwork().backupAiPlayers(app.manager());
		}
	}
}
