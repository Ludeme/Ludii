package app;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.json.JSONObject;
import org.json.JSONTokener;

import app.display.MainWindowDesktop;
import app.display.dialogs.AboutDialog;
import app.display.dialogs.SettingsDialog;
import app.display.dialogs.MoveDialog.PossibleMovesDialog;
import app.display.dialogs.MoveDialog.PuzzleDialog;
import app.display.util.DesktopGUIUtil;
import app.display.views.tabs.TabView;
import app.loading.FileLoading;
import app.loading.GameLoading;
import app.loading.TrialLoading;
import app.menu.MainMenu;
import app.menu.MainMenuFunctions;
import app.util.SettingsDesktop;
import app.util.Sound;
import app.util.UserPreferences;
import app.utils.GameSetup;
import app.utils.SettingsExhibition;
import app.views.View;
import game.Game;
import game.rules.phase.Phase;
import main.Constants;
import main.StringRoutines;
import main.collections.FastArrayList;
import main.options.GameOptions;
import manager.ai.AIDetails;
import manager.ai.AIUtil;
import other.context.Context;
import other.location.Location;
import other.move.Move;
import tournament.Tournament;
import utils.AIFactory;

//-----------------------------------------------------------------------------

/**
 * The main player object.
 *
 * @author Matthew.Stephenson and cambolbro and Eric.Piette
 */
public class DesktopApp extends PlayerApp
{
	/** App name. */
	public static final String AppName = "Ludii Player";
	
	/** Set me to false if we are making a release jar. 
	 * NOTE. In reality this is final, but keeping it non final prevents dead-code warnings.
	 */
	public static boolean devJar = false;

	/** Main frame. */
	protected static JFrameListener frame;

	/** Main view. */
	protected static MainWindowDesktop view;

	/** Current Graphics Device (screen) that is displaying the frame. */
	private static GraphicsDevice currentGraphicsDevice = null;

	/** Minimum resolution of the application. */
	private static final int minimumViewWidth = 400;
	private static final int minimumViewHeight = 400;

	//-------------------------------------------------------------------------

	/**
	 * Reference to file chooser we use for selecting JSON files (containing AI configurations)
	 */
	private static JFileChooser jsonFileChooser;

	/**
	 * Reference to file chooser we use for selecting JAR files (containing third-party AIs)
	 */
	private static JFileChooser jarFileChooser;
	
	/**
	 * Reference to file chooser we use for selecting AI.DEF files (containing AI configurations)
	 */
	private static JFileChooser aiDefFileChooser;
	
	/**
	 * Reference to file chooser we use for selecting LUD files
	 */
	private static JFileChooser gameFileChooser;
	
	//-------------------------------------------------------------------------

	/** Whether the trial should be loaded from a saved file (based on file validity checks). */
	private static boolean shouldLoadTrial = false;	

	//-------------------------------------------------------------------------

	/** Reference to file chooser we use for saving games we have just played */
	private static JFileChooser saveGameFileChooser;

	/** File chooser for loading games. */
	protected static JFileChooser loadGameFileChooser;

	/** File chooser for loading games. */
	private static JFileChooser loadTrialFileChooser;

	/** File chooser for loading games. */
	private static JFileChooser loadTournamentFileChooser;
	
	/** Last selected filepath for JSON file chooser (loaded from preferences) */
	private static String lastSelectedJsonPath;
	
	/** Last selected filepath for JSON file chooser (loaded from preferences) */
	private static String lastSelectedJarPath;
	
	/** Last selected filepath for AI.DEF file chooser (loaded from preferences) */
	private static String lastSelectedAiDefPath;
	
	/** Last selected filepath for Game file chooser (loaded from preferences) */
	private static String lastSelectedGamePath;
	
	/** Last selected filepath for JSON file chooser (loaded from preferences) */
	private static String lastSelectedSaveGamePath;
	
	/** Last selected filepath for JSON file chooser (loaded from preferences) */
	private static String lastSelectedLoadTrialPath;
	
	/** Last selected filepath for JSON file chooser (loaded from preferences) */
	private static String lastSelectedLoadTournamentPath;

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 */
	public DesktopApp()
	{
		// Do nothing.
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Create the main Desktop application.
	 */
	public void createDesktopApp()
	{
		// Invoke UI in the correct thread, otherwise menu may not draw
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				for (int i = 0; i < Constants.MAX_PLAYERS + 1; i++) // one extra for the shared player
				{
					final JSONObject json = new JSONObject()
							.put("AI", new JSONObject()
							.put("algorithm", "Human")
							);
					
					manager().aiSelected()[i] = new AIDetails(manager(), json, i, "Human");
				}
				try
				{
					createFrame();
				}
				catch (final SQLException e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	//-------------------------------------------------------------------------

	/**
	 * Gets the full frame title for displaying at the top of the application.
	 */
	public String getFrameTitle(final Context context)
	{
		final Game game = context.game();
		String frameTitle = AppName + " - " + game.name();
		GameOptions gameOptions = game.description().gameOptions();
		
		if (manager().settingsManager().userSelections().ruleset() != Constants.UNDEFINED && !SettingsExhibition.exhibitionVersion)
		{
			final String rulesetName = game.description().rulesets().get(manager().settingsManager().userSelections().ruleset()).heading();
			frameTitle += " (" + rulesetName + ")";
		}
		else if (gameOptions.numCategories() > 0)
		{
			final List<String> optionHeadings = gameOptions.allOptionStrings(manager().settingsManager().userSelections().selectedOptionStrings());
			
			final boolean defaultOptionsLoaded = optionHeadings.equals(gameOptions.allOptionStrings(new ArrayList<String>()));
			
			if (optionHeadings.size() > 0 && !defaultOptionsLoaded)
			{
				final String appendOptions = " (" + StringRoutines.join(", ", optionHeadings) + ")";
				frameTitle += appendOptions;
			}
		}

		if (context.isAMatch())
		{
			final Context instanceContext = context.currentInstanceContext();
			frameTitle += " - " + instanceContext.game().name();
			gameOptions =  game.description().gameOptions();
			
			if (gameOptions != null && gameOptions.numCategories() > 0)
			{
				String appendOptions = " (";

				int found = 0;
				for (int cat = 0; cat < gameOptions.numCategories(); cat++)
				{
					try
					{
						if (gameOptions.categories().get(cat).options().size() > 0)
						{
							final List<String> optionHeadings = gameOptions.categories().get(cat).options().get(0).menuHeadings();
							String optionSelected = optionHeadings.get(0);
							optionSelected = optionSelected.substring(optionSelected.indexOf('/')+1);
							if (found > 0)
								appendOptions += ", ";
							appendOptions += optionSelected;
							found++;
						}
					}
					catch (final Exception e)
					{
						e.printStackTrace();
						//break;
					}
				}
				appendOptions += ")";

				if (found > 0)
					frameTitle += appendOptions;
			}
			
			frameTitle += " - game #" + (manager().ref().context().completedTrials().size() + 1);
		}
		
		if (manager().settingsNetwork().getActiveGameId() > 0 && manager().settingsNetwork().getTournamentId() > 0)
			frameTitle += " (game " + manager().settingsNetwork().getActiveGameId() + " in tournament " + manager().settingsNetwork().getTournamentId() + ")";
		else if (manager().settingsNetwork().getActiveGameId() > 0)
			frameTitle += " (game " + manager().settingsNetwork().getActiveGameId() + ")";
		
		if (settingsPlayer().showPhaseInTitle() && !context.game().hasSubgames())
		{
			final int mover = context.state().mover();
			final int indexPhase = context.state().currentPhase(mover);
		    final Phase phase = context.game().rules().phases()[indexPhase];
		    frameTitle += " (phase " + phase.name() + ")";
		}
	
		return frameTitle;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Display an error message on the status panel.
	 */
	@Override
	public void reportError(final String text)
	{
		if (view != null)
		{
			if (frame != null)
			{
				frame.setContentPane(view);
				frame.repaint();
				frame.revalidate();
			}
		}
		
		EventQueue.invokeLater(() -> 
		{
			addTextToStatusPanel(text + "\n");
		});
	}

	//-------------------------------------------------------------------------

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		MainMenuFunctions.checkActionsPerformed(this, e);
	}

	@Override
	public void itemStateChanged(final ItemEvent e)
	{
		MainMenuFunctions.checkItemStateChanges(this, e);
	}

	//---------------------------------------------------------------------------

	/** Tasks that are performed when the application is closed. */
	public void appClosedTasks()
	{
		if (SettingsExhibition.exhibitionVersion)
			return;
		
		manager().settingsNetwork().restoreAiPlayers(manager());
		
		// Close all AI objects
		for (final AIDetails ai : manager().aiSelected())
			if (ai.ai() != null)
				ai.ai().closeAI();
		
		if (manager().ref().context().game().equipmentWithStochastic())
			manager().ref().context().trial().reset(manager().ref().context().game());
		
		// Save the current trial
		final File file = new File("." + File.separator + "ludii.trl");
		TrialLoading.saveTrial(this, file);

		// Save the rest of the preferences
		UserPreferences.savePreferences(this);
	}

	//-------------------------------------------------------------------------

	/**
	 * Launch the frame.
	 */
	void createFrame() throws SQLException
	{
		try
		{
			UserPreferences.loadPreferences(this);
		}
		catch (final Exception e)
		{
			System.out.println("Failed to create preferences file.");
			e.printStackTrace();
		}
			
		try
		{
			frame = new JFrameListener(AppName, this);
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			
			// Logo
			try
			{
				final URL resource = this.getClass().getResource("/ludii-logo-100x100.png");
				final BufferedImage image = ImageIO.read(resource);
				frame.setIconImage(image);
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}

			view = new MainWindowDesktop(this);
			frame.setContentPane(view);
			frame.setSize(SettingsDesktop.defaultWidth, SettingsDesktop.defaultHeight);
			
			if (SettingsExhibition.exhibitionVersion)
			{
				frame.setUndecorated(true);
				frame.setResizable(false);
				frame.setSize(SettingsExhibition.exhibitionDisplayWidth, SettingsExhibition.exhibitionDisplayHeight);
			}
			
			try
			{
				if (settingsPlayer().defaultX() == -1 || settingsPlayer().defaultY() == -1)
					frame.setLocationRelativeTo(null);
				else
					frame.setLocation(settingsPlayer().defaultX(), settingsPlayer().defaultY());
							
				if (settingsPlayer().frameMaximised())
					frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
			}
			catch (final Exception e)
			{
				frame.setLocationRelativeTo(null);
			}

			frame.setVisible(true);
			frame.setMinimumSize(new Dimension(minimumViewWidth, minimumViewHeight));

			FileLoading.createFileChoosers();
			setCurrentGraphicsDevice(frame.getGraphicsConfiguration().getDevice());
			
			// gets called when the app is closed (save preferences and trial)
			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				@Override
				public void run()
				{
					appClosedTasks();
				}
			});
			
			loadInitialGame(true);	
		}
		catch (final Exception e)
		{
			System.out.println("Failed to create application frame.");
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------------
		
	/** 
	 * Loads the initial game. 
	 * Either the default game of the previously loaded game when the application was last closed. 
	 */
	protected void loadInitialGame(final boolean firstTry)
	{
		try
		{
			if (SettingsExhibition.exhibitionVersion)
			{
				GameLoading.loadGameFromMemory(this, SettingsExhibition.exhibitionGamePath, false);
				
				if (SettingsExhibition.againstAI)
				{
					final JSONObject json = new JSONObject().put("AI",
							new JSONObject()
							.put("algorithm", "UCT")
							);
					AIUtil.updateSelectedAI(manager(), json, 2, "UCT");
					manager().aiSelected()[2].setThinkTime(SettingsExhibition.thinkingTime);
				}
				
				bridge().settingsVC().setShowPossibleMoves(true);
				
				return;
			}
			
			if (firstTry)
				TrialLoading.loadStartTrial(this);
			
			if (manager().ref().context() == null)
			{
				if (firstTry)
					GameLoading.loadGameFromMemory(this, Constants.DEFAULT_GAME_PATH, false);
				else
				{
					settingsPlayer().setLoadedFromMemory(true);
					GameSetup.compileAndShowGame(this, Constants.FAIL_SAFE_GAME_DESCRIPTION, false);
					EventQueue.invokeLater(() -> 
					{
						setTemporaryMessage("Failed to start game. Loading default game (Tic-Tac-Toe).");
					});
				}
			}
			
			frame.setJMenuBar(new MainMenu(this));
	
			for (int i = 1; i <=  manager().ref().context().game().players().count(); i++)
				if (aiSelected()[i] != null)
					AIUtil.updateSelectedAI(manager(), manager().aiSelected()[i].object(), i, manager().aiSelected()[i].menuItemName());
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			
			if (firstTry)
			{
				// Try and load the default game.
				manager().setSavedLudName(null);
				settingsPlayer().setLoadedFromMemory(true);
				setLoadTrial(false);
				loadInitialGame(false);
			}
			
			if (manager().savedLudName() != null)
				addTextToStatusPanel("Failed to start game: " + manager().savedLudName() + "\n");
			else if (manager().ref().context().game().name() != null)
				addTextToStatusPanel("Failed to start external game description.\n");
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public Tournament tournament()
	{
		return manager().getTournament();
	}

	@Override
	public void setTournament(final Tournament tournament)
	{
		manager().setTournament(tournament);
	}

	public static GraphicsDevice currentGraphicsDevice()
	{
		return currentGraphicsDevice;
	}

	public static void setCurrentGraphicsDevice(final GraphicsDevice currentGraphicsDevice)
	{
		DesktopApp.currentGraphicsDevice = currentGraphicsDevice;
	}

	public AIDetails[] aiSelected()
	{
		return manager().aiSelected();
	}

	public static JFileChooser jsonFileChooser()
	{
		return jsonFileChooser;
	}

	public static void setJsonFileChooser(final JFileChooser jsonFileChooser)
	{
		DesktopApp.jsonFileChooser = jsonFileChooser;
	}

	public static JFileChooser jarFileChooser()
	{
		return jarFileChooser;
	}
	
	public static JFileChooser gameFileChooser()
	{
		return gameFileChooser;
	}

	public static void setJarFileChooser(final JFileChooser jarFileChooser)
	{
		DesktopApp.jarFileChooser = jarFileChooser;
	}
	
	public static void setGameFileChooser(final JFileChooser gameFileChooser)
	{
		DesktopApp.gameFileChooser = gameFileChooser;
	}

	public static JFileChooser saveGameFileChooser()
	{
		return saveGameFileChooser;
	}

	public static void setSaveGameFileChooser(final JFileChooser saveGameFileChooser)
	{
		DesktopApp.saveGameFileChooser = saveGameFileChooser;
	}

	public static JFileChooser loadTrialFileChooser()
	{
		return loadTrialFileChooser;
	}

	public static void setLoadTrialFileChooser(final JFileChooser loadTrialFileChooser)
	{
		DesktopApp.loadTrialFileChooser = loadTrialFileChooser;
	}

	public static JFileChooser loadTournamentFileChooser()
	{
		return loadTournamentFileChooser;
	}

	public static void setLoadTournamentFileChooser(final JFileChooser loadTournamentFileChooser)
	{
		DesktopApp.loadTournamentFileChooser = loadTournamentFileChooser;
	}

	public static void setLoadTrial(final boolean shouldLoadTrial)
	{
		DesktopApp.shouldLoadTrial = shouldLoadTrial;
	}
	
	public static boolean shouldLoadTrial()
	{
		return shouldLoadTrial;
	}

	public static String lastSelectedSaveGamePath()
	{
		return lastSelectedSaveGamePath;
	}

	public static void setLastSelectedSaveGamePath(final String lastSelectedSaveGamePath)
	{
		DesktopApp.lastSelectedSaveGamePath = lastSelectedSaveGamePath;
	}

	public static String lastSelectedJsonPath()
	{
		return lastSelectedJsonPath;
	}

	public static void setLastSelectedJsonPath(final String lastSelectedJsonPath)
	{
		DesktopApp.lastSelectedJsonPath = lastSelectedJsonPath;
	}

	public static String lastSelectedJarPath()
	{
		return lastSelectedJarPath;
	}
	
	public static String lastSelectedGamePath()
	{
		return lastSelectedGamePath;
	}

	public static void setLastSelectedJarPath(final String lastSelectedJarPath)
	{
		DesktopApp.lastSelectedJarPath = lastSelectedJarPath;
	}
	
	public static void setLastSelectedGamePath(final String lastSelectedGamePath)
	{
		DesktopApp.lastSelectedGamePath = lastSelectedGamePath;
	}

	public static String lastSelectedLoadTrialPath()
	{
		return lastSelectedLoadTrialPath;
	}

	public static void setLastSelectedLoadTrialPath(final String lastSelectedLoadTrialPath)
	{
		DesktopApp.lastSelectedLoadTrialPath = lastSelectedLoadTrialPath;
	}

	public static String lastSelectedLoadTournamentPath()
	{
		return lastSelectedLoadTournamentPath;
	}

	public static void setLastSelectedLoadTournamentPath(final String lastSelectedLoadTournamentPath)
	{
		DesktopApp.lastSelectedLoadTournamentPath = lastSelectedLoadTournamentPath;
	}
	
	/**
	 * @return Main view.
	 */
	public static MainWindowDesktop view()
	{
		return view;
	}

	/**
	 * @return Main frame.
	 */
	public static JFrameListener frame()
	{
		return frame;
	}

	@Override
	public void updateFrameTitle(final boolean alsoUpdateMenu)
	{
		frame().setTitle(getFrameTitle(manager().ref().context()));
		
		if (alsoUpdateMenu)
		{
			frame().setJMenuBar(new MainMenu(this));
			view().createPanels();
		}
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void refreshNetworkDialog()
	{
		remoteDialogFunctionsPublic().refreshNetworkDialog();
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void loadGameFromName(final String name, final List<String> options, final boolean debug)
	{
		GameLoading.loadGameFromName(this, name, options, debug);
	}

	@Override
	public JSONObject getNameFromJar()
	{
		// we'll have to go through file chooser
		final JFileChooser fileChooser = DesktopApp.jarFileChooser();
		fileChooser.setDialogTitle("Select JAR file containing AI.");
		final int jarReturnVal = fileChooser.showOpenDialog(DesktopApp.frame());
		final File jarFile;

		if (jarReturnVal == JFileChooser.APPROVE_OPTION)
			jarFile = fileChooser.getSelectedFile();
		else
			jarFile = null;

		if (jarFile != null && jarFile.exists())
		{
			final List<Class<?>> classes = AIFactory.loadThirdPartyAIClasses(jarFile);

			if (classes.size() > 0)
			{
				// show dialog with options
				final URL logoURL = this.getClass().getResource("/ludii-logo-64x64.png");
				final ImageIcon icon = new ImageIcon(logoURL);

				final String[] choices = new String[classes.size()];
				for (int i = 0; i < choices.length; ++i)
				{
					choices[i] = classes.get(i).getName();
				}

				final String choice = (String) JOptionPane.showInputDialog(DesktopApp.frame(), "AI Classes",
						"Choose an AI class to load", JOptionPane.QUESTION_MESSAGE, icon, choices, choices[0]);

				if (choice == null)
				{
					System.err.println("No AI class selected.");
					return null;
				}
				else
				{
					return new JSONObject().put("AI",
							new JSONObject()
							.put("algorithm", "From JAR")
							.put("JAR File", jarFile.getAbsolutePath())
							.put("Class Name", choice)
							);
				}
			}
			else
			{
				System.err.println("Could not find any AI classes.");
				return null;
			}
		}
		else
		{
			System.err.println("Could not find JAR file.");
			return null;
		}
	}
	
	@Override
	public JSONObject getNameFromJson()
	{
		// we'll have to go through file chooser
		final JFileChooser fileChooser = DesktopApp.jsonFileChooser();
		fileChooser.setDialogTitle("Select JSON file containing AI.");
		final int jsonReturnVal = fileChooser.showOpenDialog(DesktopApp.frame());
		final File jsonFile;

		if (jsonReturnVal == JFileChooser.APPROVE_OPTION)
			jsonFile = fileChooser.getSelectedFile();
		else
			jsonFile = null;

		if (jsonFile != null && jsonFile.exists())
		{
			try (final InputStream inputStream = new FileInputStream(jsonFile))
			{
				return new JSONObject(new JSONTokener(inputStream));
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.err.println("Could not find JSON file.");
		}
		
		return null;
	}
	
	@Override
	public JSONObject getNameFromAiDef()
	{
		// we'll have to go through file chooser
		final JFileChooser fileChooser = DesktopApp.aiDefFileChooser();
		fileChooser.setDialogTitle("Select AI.DEF file containing AI.");
		final int aiDefReturnVal = fileChooser.showOpenDialog(DesktopApp.frame());
		final File aiDefFile;

		if (aiDefReturnVal == JFileChooser.APPROVE_OPTION)
			aiDefFile = fileChooser.getSelectedFile();
		else
			aiDefFile = null;

		if (aiDefFile != null && aiDefFile.exists())
		{
			return new JSONObject().put
					(
						"AI",
						new JSONObject()
						.put("algorithm", "From AI.DEF")
						.put("AI.DEF File", aiDefFile.getAbsolutePath())
					);
		}
		else
		{
			System.err.println("Could not find AI.DEF file.");
		}
		
		return null;
	}

	@Override
	public void addTextToStatusPanel(final String text)
	{
		EventQueue.invokeLater(() -> 
		{
			view.tabPanel().page(TabView.PanelStatus).addText(text);
		});
	}
	
	@Override
	public void addTextToAnalysisPanel(final String text)
	{
		EventQueue.invokeLater(() -> 
		{
			view.tabPanel().page(TabView.PanelAnalysis).addText(text);
		});
	}

	@Override
	public void setTemporaryMessage(final String text)
	{
		view.setTemporaryMessage(text);
	}
	
	@Override
	public void setVolatileMessage(final String text)
	{
		MainWindowDesktop.setVolatileMessage(this, text);
	}

	@Override
	public void showPuzzleDialog(final int site)
	{
		PuzzleDialog.createAndShowGUI(this, manager().ref().context(), site);
	}

	@Override
	public void showPossibleMovesDialog(final Context context, final FastArrayList<Move> possibleMoves)
	{
		PossibleMovesDialog.createAndShowGUI(this, context, possibleMoves, false);
	}

	@Override
	public void selectAnalysisTab()
	{
		view.tabPanel().select((TabView.PanelAnalysis));
	}

	@Override
	public void repaint()
	{
		view.isPainting = true;
		view.repaint();
		view.revalidate();
	}

	@Override
	public void reportDrawAgreed()
	{
		//final String lastLine = view.tabPanel().page(TabView.PanelStatus).text().split("\n")[view.tabPanel().page(TabView.PanelStatus).text().split("\n").length-1];
		final String message = "All players have agreed to a draw, for Game " + manager().settingsNetwork().getActiveGameId() + ".\nThe Game is Over.\n";
		if (!view.tabPanel().page(TabView.PanelStatus).text().contains(message))
			addTextToStatusPanel(message);
	}
	
	@Override
	public void reportForfeit(final int playerForfeitNumber)
	{		
		final String message = "Player " + playerForfeitNumber + " has resigned Game " + manager().settingsNetwork().getActiveGameId() + ".\nThe Game is Over.\n";
		if (!view.tabPanel().page(TabView.PanelStatus).text().contains(message))
			addTextToStatusPanel(message);
	}

	@Override
	public void reportTimeout(final int playerForfeitNumber)
	{
		final String message = "Player " + playerForfeitNumber + " has timed out for Game " + manager().settingsNetwork().getActiveGameId() + ".\nThe Game is Over.\n";
		if (!view.tabPanel().page(TabView.PanelStatus).text().contains(message))
			addTextToStatusPanel(message);
	}

	@Override
	public void updateTabs(final Context context)
	{
		EventQueue.invokeLater(() -> 
		{
			view.tabPanel().updateTabs(context);
		});
	}

	@Override
	public void playSound(final String soundName)
	{
		if (settingsPlayer().isMoveSoundEffect())
			Sound.playSound(soundName);
	}
	
	@Override
	public void saveTrial()
	{
		final File file = new File("." + File.separator + "ludii.trl");
		TrialLoading.saveTrial(this, file);
	}

	//-------------------------------------------------------------------------

	@Override
	public void repaintTimerForPlayer(final int playerId)
	{
		if (view.playerNameList[playerId] != null)
			view.repaint(DesktopApp.view.playerNameList[playerId]);		
	}

	@Override
	public void repaintComponentBetweenPoints(final Context context, final Location moveFrom, final Point startPoint, final Point endPoint)
	{
		DesktopGUIUtil.repaintComponentBetweenPoints(this, context, moveFrom, startPoint, endPoint);
	}

	@Override
	public void writeTextToFile(final String fileName, final String log)
	{
		FileLoading.writeTextToFile(fileName, log);
	}

	@Override
	public void resetMenuGUI()
	{
		DesktopApp.frame().setJMenuBar(new MainMenu(this));
	}

	@Override
	public void showSettingsDialog()
	{
		if (!SettingsExhibition.exhibitionVersion)
		{
			SettingsDialog.createAndShowGUI(this);
		}
	}

	@Override
	public void showOtherDialog(final FastArrayList<Move> otherPossibleMoves)
	{
		PossibleMovesDialog.createAndShowGUI(this, contextSnapshot().getContext(this), otherPossibleMoves, true);
	}

	@Override
	public void showInfoDialog()
	{
		AboutDialog.showAboutDialog(this);
	}
	
	
	//-------------------------------------------------------------------------

	@Override
	public int width()
	{
		return view.width();
	}

	@Override
	public int height()
	{
		return view.height();
	}

	@Override
	public Rectangle[] playerSwatchList()
	{
		return view.playerSwatchList;
	}

	@Override
	public Rectangle[] playerNameList()
	{
		return view.playerNameList;
	}

	@Override
	public boolean[] playerSwatchHover()
	{
		return view.playerSwatchHover;
	}

	@Override
	public boolean[] playerNameHover()
	{
		return view.playerNameHover;
	}

	@Override
	public List<View> getPanels()
	{
		return view.getPanels();
	}

	@Override
	public void repaint(final Rectangle rect)
	{
		view.repaint(rect);
	}

	public static JFileChooser aiDefFileChooser()
	{
		return aiDefFileChooser;
	}

	public static void setAiDefFileChooser(final JFileChooser aiDefFileChooser)
	{
		DesktopApp.aiDefFileChooser = aiDefFileChooser;
	}

	public static String lastSelectedAiDefPath()
	{
		return lastSelectedAiDefPath;
	}

	public static void setLastSelectedAiDefPath(final String lastSelectedAiDefPath)
	{
		DesktopApp.lastSelectedAiDefPath = lastSelectedAiDefPath;
	}

}