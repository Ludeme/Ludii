package app.menu;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import agentPrediction.external.AgentPredictionExternal;
import agentPrediction.external.MetricPredictionExternal;
import agentPrediction.internal.AgentPredictionInternal;
import agentPrediction.internal.models.LinearRegression;
import app.DesktopApp;
import app.PlayerApp;
import app.display.dialogs.AboutDialog;
import app.display.dialogs.DeveloperDialog;
import app.display.dialogs.EvaluationDialog;
import app.display.dialogs.GameLoaderDialog;
import app.display.dialogs.ReconstructionDialog;
import app.display.dialogs.SVGViewerDialog;
import app.display.dialogs.TestLudemeDialog;
import app.display.dialogs.MoveDialog.PossibleMovesDialog;
import app.display.dialogs.editor.EditorDialog;
import app.display.dialogs.visual_editor.StartVisualEditor;
import app.display.screenCapture.ScreenCapture;
import app.display.util.Thumbnails;
import app.display.views.tabs.TabView;
import app.loading.GameLoading;
import app.loading.MiscLoading;
import app.loading.TrialLoading;
import app.manualGeneration.ManualGeneration;
import app.utils.GameSetup;
import app.utils.GameUtil;
import app.utils.PuzzleSelectionType;
import app.utils.QrCodeGeneration;
import app.views.tools.ToolView;
import approaches.random.Generator;
import contextualiser.ContextualSimilarity;
import features.feature_sets.BaseFeatureSet;
import game.Game;
import game.rules.phase.Phase;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.play.RepetitionType;
import gnu.trove.list.array.TIntArrayList;
import grammar.Grammar;
import graphics.svg.SVGLoader;
import main.Constants;
import main.FileHandling;
import main.StringRoutines;
import main.collections.FastArrayList;
import main.grammar.Call;
import main.grammar.Description;
import main.grammar.Report;
import main.options.GameOptions;
import main.options.Option;
import main.options.Ruleset;
import manager.ai.AIDetails;
import manager.ai.AIUtil;
import manager.network.local.LocalFunctions;
import metadata.ai.features.Features;
import metadata.ai.heuristics.Heuristics;
import other.AI;
import other.GameLoader;
import other.action.Action;
import other.action.move.remove.ActionRemove;
import other.action.state.ActionSetNextPlayer;
import other.concept.Concept;
import other.concept.ConceptComputationType;
import other.concept.ConceptDataType;
import other.concept.ConceptType;
import other.context.Context;
import other.location.FullLocation;
import other.model.Model;
import other.move.Move;
import other.trial.Trial;
import parser.Parser;
import policies.softmax.SoftmaxPolicyLinear;
import search.pns.ProofNumberSearch.ProofGoals;
import supplementary.EvalUtil;
import supplementary.experiments.EvalAIsThread;
import supplementary.experiments.EvalGamesSet;
import supplementary.experiments.ludemes.CountLudemes;
import util.StringUtil;

//--------------------------------------------------------

/**
 * The app's main menu.
 *
 * @author cambolbro and Matthew.Stephenson and Eric.Piette
 */
public class MainMenuFunctions extends JMenuBar
{
	private static final long serialVersionUID = 1L;

	/** Thread in which we're timing random playouts. */
	private static Thread timeRandomPlayoutsThread = null;
	
	/** Thread in which we're comparing agents. */
	private static Thread agentComparisonThread = null;

	/** The visual editor. */
	private static StartVisualEditor startVisualEditor;

	//-------------------------------------------------------------------------
	
	public static void checkActionsPerformed(final DesktopApp app, final ActionEvent e)
	{
		app.bridge().settingsVC().setSelectedFromLocation(new FullLocation(Constants.UNDEFINED));
		final JMenuItem source = (JMenuItem) (e.getSource());
		final Context context = app.manager().ref().context();
		final Game game = context.game();

		if (source.getText().equals("About"))
		{
			AboutDialog.showAboutDialog(app);
		}
		else if (source.getText().equals("Count Ludemes"))
		{
			final CountLudemes ludemeCounter = new CountLudemes();
			app.addTextToStatusPanel("\nChar count (including spaces): " + context.game().description().raw().length());
			app.addTextToStatusPanel("\nChar count (excluding spaces): " + context.game().description().raw().replaceAll("\\s+","").length());
			app.addTextToStatusPanel(ludemeCounter.result());
			System.out.println(ludemeCounter.result());
		}
		else if (source.getText().equals("Game Description Length"))
		{
			final String allOutputs = game.name() + ", Raw: " + game.description().raw().replaceAll("\\s+","").length() + ", Expanded: " + game.description().expanded().replaceAll("\\s+","").length() + ", Tokens: " + game.description().tokenForest().tokenTree().countKeywords() + ", ludemes: " + game.description().callTree().countClasses();
			app.addTextToStatusPanel(allOutputs + "\n");
		}
		else if (source.getText().equals("Game Description Length (All Games)"))
		{
			final String[] choices = FileHandling.listGames();
			for (final String s : choices)
			{
				if (!FileHandling.shouldIgnoreLudRelease(s))
				{
					final Game gameTemp = GameLoader.loadGameFromName(s.split("/")[s.split("/").length-1]);
					
					// Skip the Match game luds
					if (gameTemp.hasSubgames())
						continue;
					
					final String allOutputs = gameTemp.name() + "," + gameTemp.description().raw().replaceAll("\\s+","").length() + "," + gameTemp.description().expanded().replaceAll("\\s+","").length() + "," + gameTemp.description().tokenForest().tokenTree().countKeywords() + "," + gameTemp.description().callTree().countClasses();
					System.out.println(allOutputs);
				}
			}
			System.out.println("Done");
		}
		else if (source.getText().equals("Load Game"))
		{
			if (!app.manager().settingsManager().agentsPaused())
			{
				app.manager().settingsManager().setAgentsPaused(app.manager(), true);
			}
			GameLoading.loadGameFromMemory(app, false);
		}
		else if (source.getText().equals("Load Game from File"))
		{
			if (!app.manager().settingsManager().agentsPaused())
			{
				app.manager().settingsManager().setAgentsPaused(app.manager(), true);
			}
			GameLoading.loadGameFromFile(app);
		}
		else if (source.getText().equals("Load Random Game"))
		{
			if (!app.manager().settingsManager().agentsPaused())
				app.manager().settingsManager().setAgentsPaused(app.manager(), true);

			GameLoading.loadRandomGame(app);
		}
		else if (source.getText().equals("Save Trial"))
		{
			TrialLoading.saveTrial(app);
		}
		else if (source.getText().equals("Test Ludeme"))
		{
			TestLudemeDialog.showDialog(app);
		}
		else if (source.getText().equals("Create Game"))
		{
			final String savedPath = EditorDialog.saveGameDescription(app, Constants.BASIC_GAME_DESCRIPTION);
			GameLoading.loadGameFromFilePath(app, savedPath + ".lud");
			EditorDialog.createAndShowGUI(app, true, true, true);
		}
		else if (source.getText().equals("Load Trial"))
		{
			if (!app.manager().settingsManager().agentsPaused())
			{
				app.manager().settingsManager().setAgentsPaused(app.manager(), true);
			}
			TrialLoading.loadTrial(app, false);
		}
		else if (source.getText().equals("Load Tournament File"))
		{
			if (!app.manager().settingsManager().agentsPaused())
			{
				app.manager().settingsManager().setAgentsPaused(app.manager(), true);
			}
			MiscLoading.loadTournamentFile(app);
		}
		else if (source.getText().equals("Editor (Packed)"))
		{
			EditorDialog.createAndShowGUI(app, false, true, true);
		}
		else if (source.getText().equals("Editor (Expanded)"))
		{
			EditorDialog.createAndShowGUI(app, true, true, true);
		}
		else if (source.getText().equals("Visual Editor (Beta)"))
		{
			// Create and lauch an instance of the visual editor
			setStartVisualEditor(new StartVisualEditor(app));
		}
		// IMPORTANT These next four menu functions are just for us, not the user
		else if (source.getText().equals("Export Thumbnails"))
		{
			// DesktopApp.frame().setSize(462, 462); Eric Resolution
			DesktopApp.frame().setSize(464, 464);
			EventQueue.invokeLater(() -> 
	    	{
		    	Thumbnails.generateThumbnails(app, false);
		    	QrCodeGeneration.makeQRCode(game, 5, 2, false);
	    	});
		}
		else if (source.getText().equals("Export Thumbnails (ruleset)"))
		{
			DesktopApp.frame().setSize(464, 464);
			EventQueue.invokeLater(() -> 
	    	{
		    	Thumbnails.generateThumbnails(app, true);
	    	});
		}
		else if (source.getText().equals("Export Thumbnails (complete rulesets)"))
		{
			DesktopApp.frame().setSize(464, 464);
			final ArrayList<List<String>> gameOptions = new ArrayList<>();
			System.out.println("Getting rulesets from game:");
			final List<Ruleset> rulesets = game.description().rulesets();
			if (rulesets != null && !rulesets.isEmpty())
				for (int rs = 0; rs < rulesets.size(); rs++)
					if (!rulesets.get(rs).optionSettings().isEmpty() && !rulesets.get(rs).heading().contains("Incomplete"))
						gameOptions.add(rulesets.get(rs).optionSettings());
			
			final Timer t = new Timer( );
			t.scheduleAtFixedRate(new TimerTask()
			{
				int gameChoice = 0;
			    @Override
			    public void run()
			    {
			    	if (gameChoice >= gameOptions.size()) 
			    	{
			    		System.out.println("Thumbnails Done.");
			            t.cancel();
			            t.purge();
			            return;
			        }
			    	
			    	EventQueue.invokeLater(() -> 
			    	{
			    		GameLoading.loadGameFromName(app, game.name(), gameOptions.get(gameChoice), false);
			    		gameChoice++;
			    	});
			    }
			}, 1000,50000);

			final Timer t2 = new Timer( );
			t2.scheduleAtFixedRate(new TimerTask()
			{
			    @Override
			    public void run()
			    {
			    	Thumbnails.generateThumbnails(app, true);
			    }
			}, 24000,50000);
		}
		else if (source.getText().equals("Export All Thumbnails"))
		{			
			DesktopApp.frame().setSize(464, 464);
			final String[] choices = FileHandling.listGames();
			final ArrayList<String> validChoices = new ArrayList<>();
			for (final String s : choices)
			{
				if (!FileHandling.shouldIgnoreLudThumbnails(s))
				{
					validChoices.add(s);
				}
			}
			
			final Timer t = new Timer( );
			t.scheduleAtFixedRate(new TimerTask()
			{
				int gameChoice = 0;
			    @Override
			    public void run()
			    {
			    	if (gameChoice >= validChoices.size()) 
			    	{
			            t.cancel();
			            t.purge();
			            return;
			        }
			    	
			    	EventQueue.invokeLater(() -> 
			    	{
			    		GameLoading.loadGameFromName(app, validChoices.get(gameChoice), new ArrayList<String>(), false);
			    		gameChoice++;
			    	});
			    }
			}, 1000,50000);

			final Timer t2 = new Timer( );
			t2.scheduleAtFixedRate(new TimerTask()
			{
			    @Override
			    public void run()
			    {
			    	Thumbnails.generateThumbnails(app, false);
			    }
			}, 24000,50000);
		}
		else if (source.getText().equals("Export All Thumbnails (rulesets)"))
		{			
			DesktopApp.frame().setSize(464, 464);
			final String[] choices = FileHandling.listGames();
			final ArrayList<String> validChoices = new ArrayList<>();
			final ArrayList<List<String>> gameOptions = new ArrayList<>();
			System.out.println("Getting rulesets from games:");
			for (final String s : choices)
			{
				if (!FileHandling.shouldIgnoreLudThumbnails(s))
				{
					System.out.println(s);
					final Game tempGame = GameLoader.loadGameFromName(s.split("\\/")[s.split("\\/").length-1]);
					final List<Ruleset> rulesets = tempGame.description().rulesets();
					if (rulesets != null && !rulesets.isEmpty())
					{
						for (int rs = 0; rs < rulesets.size(); rs++)
						{
							if (!rulesets.get(rs).optionSettings().isEmpty())
							{
								validChoices.add(s);
								gameOptions.add(rulesets.get(rs).optionSettings());
							}
						}
					}
				}
			}

			final Timer t = new Timer( );
			t.scheduleAtFixedRate(new TimerTask()
			{
				int gameChoice = 0;
			    @Override
			    public void run()
			    {
			    	if (gameChoice >= validChoices.size()) 
			    	{
			            t.cancel();
			            t.purge();
			            return;
			        }
			    	
			    	EventQueue.invokeLater(() -> 
			    	{
			    		GameLoading.loadGameFromName(app, validChoices.get(gameChoice), gameOptions.get(gameChoice), false);
			    		gameChoice++;
			    	});
			    }
			}, 1000,50000);

			final Timer t2 = new Timer( );
			t2.scheduleAtFixedRate(new TimerTask()
			{
			    @Override
			    public void run()
			    {
			    	Thumbnails.generateThumbnails(app, true);
			    }
			}, 24000,50000);

		}
		else if (source.getText().equals("Export Board Thumbnail"))
		{
			Thumbnails.generateBoardThumbnail(app);
		}
		else if (source.getText().equals("Export All Board Thumbnails"))
		{			
			//PlayerApp.frame.setSize(464, 464);
			final String[] choices = FileHandling.listGames();
			final ArrayList<String> validChoices = new ArrayList<>();
			for (final String s : choices)
				if (!FileHandling.shouldIgnoreLudThumbnails(s))
					validChoices.add(s);

			final Timer t = new Timer( );
			t.scheduleAtFixedRate(new TimerTask()
			{
				int gameChoice = 0;
			    @Override
			    public void run()
			    {
			    	if (gameChoice >= validChoices.size()) 
			    	{
			            t.cancel();
			            t.purge();
			            return;
			        }
			    	
			    	GameLoading.loadGameFromName(app, validChoices.get(gameChoice), new ArrayList<String>(), false);
			    	gameChoice++;
			    }
			}, 1000,20000);

			final Timer t2 = new Timer( );
			t2.scheduleAtFixedRate(new TimerTask()
			{
			    @Override
			    public void run()
			    {
			    	Thumbnails.generateBoardThumbnail(app);
			    }
			}, 11000,20000);

		}
		else if (source.getText().equals("Linear Regression (internal)"))
		{
			AgentPredictionInternal.predictAI(app.manager(), new LinearRegression());
		}
		else if (source.getText().equals("Restart"))
		{
			app.addTextToStatusPanel("-------------------------------------------------\n");
			app.addTextToStatusPanel("Game Restarted.\n");
			
			GameUtil.resetGame(app, false);
		}
		else if (source.getText().equals("Random Move"))
		{
			app.manager().ref().randomMove(app.manager());
		}
		else if (source.getText().equals("Random Playout"))
		{
			if (!game.isDeductionPuzzle())
				app.manager().ref().randomPlayout(app.manager());
			
//			System.out.println("Num Moves: " + app.manager().ref().context().trial().numMoves());
//			System.out.println("Num Turns: " + app.manager().ref().context().trial().numTurns());
//			System.out.println("Num Decisions: " + app.manager().ref().context().trial().numLogicalDecisions(game));
//			System.out.println("Num Forced Passes: " + app.manager().ref().context().trial().numForcedPasses());
		}
		else if (source.getText().equals("Time Random Playouts"))
		{
			if (!game.isDeductionPuzzle())
			{
        		app.setVolatileMessage("This will take about 40 seconds, during which time the UI will not respond.\n");
        		
        		// Just use a series of EventQueues to ensure this gets run after the GUI has properly updated.
        		EventQueue.invokeLater(() ->
				{
					EventQueue.invokeLater(() ->
					{
						EventQueue.invokeLater(() ->
						{
							EventQueue.invokeLater(() ->
							{
								app.manager().ref().interruptAI(app.manager());
								DesktopApp.frame().setContentPane(DesktopApp.view());
								final double rate = app.manager().ref().timeRandomPlayouts();
								app.addTextToStatusPanel("" + String.format(Locale.US, "%.2f", Double.valueOf(rate)) + " random playouts/s.\n");
								app.setTemporaryMessage("");
								app.setTemporaryMessage("Analysis Complete.\n");
							});
						});
					});
				});
			}
			else
			{
				app.setVolatileMessage("Time Random Playouts is disabled for deduction puzzles.\n");
			}
		}
		else if (source.getText().equals("Time Random Playouts in Background"))
		{
			if (timeRandomPlayoutsThread != null)
			{
				app.addTextToStatusPanel("Time Random Playouts is already in progress!\n");
				return;
			}

			if (!game.isDeductionPuzzle())
			{
				timeRandomPlayoutsThread = new Thread(() ->
				{
					final double rate = app.manager().ref().timeRandomPlayouts();
					EventQueue.invokeLater(() ->
					{
						app.addTextToStatusPanel("" + String.format(Locale.US, "%.2f", Double.valueOf(rate)) + " random playouts/s.\n");
						app.setTemporaryMessage("");
    					app.setTemporaryMessage("Analysis Complete.\n");
						timeRandomPlayoutsThread = null;
					});
				});
				app.setTemporaryMessage("Time Random Playouts is starting. This will take about 40 seconds.\n");
				timeRandomPlayoutsThread.setDaemon(true);
				timeRandomPlayoutsThread.start();
			}
			else
			{
				app.setVolatileMessage("Time Random Playouts is disabled for deduction puzzles.\n");
			}
		}
		else if (source.getText().equals("Show Compilation Concepts"))
		{
			final List<List<String>> conceptsPerCategories = new ArrayList<List<String>>();
			for (int i = 0; i < ConceptType.values().length; i++)
				conceptsPerCategories.add(new ArrayList<String>());

			for (int i = 0; i < Concept.values().length; i++)
			{
				final Concept concept = Concept.values()[i];
				final ConceptType type = concept.type();
				if (game.booleanConcepts().get(concept.id()))
					conceptsPerCategories.get(type.ordinal()).add(concept.name());
			}

			final StringBuffer properties = new StringBuffer("The boolean concepts of the game are: \n\n");

			for (int i = 0; i < conceptsPerCategories.size(); i++)
			{
				if(!conceptsPerCategories.get(i).isEmpty())
				{
					final ConceptType type = ConceptType.values()[i];
					properties.append("******* " + type.name() + " concepts *******\n");
					for(int j = 0; j < conceptsPerCategories.get(i).size();j++)
					{
						final String concept = conceptsPerCategories.get(i).get(j);
						properties.append(concept + "\n");
					}
					properties.append("\n");
				}
			}

			properties.append("\nThe non boolean concepts of the game are: \n\n");
			for (int i = 0; i < Concept.values().length; i++)
			{
				final Concept concept = Concept.values()[i];
				final String name = concept.name();
				final Integer idConcept = Integer.valueOf(concept.id());
				if (!concept.dataType().equals(ConceptDataType.BooleanData) && concept.computationType().equals(ConceptComputationType.Compilation))
					properties.append(name + ": " + game.nonBooleanConcepts().get(idConcept) + "\n");
			}
			
			app.manager().getPlayerInterface().addTextToAnalysisPanel(properties.toString());
			app.selectAnalysisTab();
		}
		else if (source.getText().equals("Duplicates Moves Test"))
		{
			final StringBuffer results = new StringBuffer();
			final int numPlayouts = 100;
			boolean duplicateMove = false;

			for (int i = 0; i < numPlayouts; i++)
			{
				final List<AI> ais = new ArrayList<AI>();
				ais.add(null);
				for (int p = 1; p <= game.players().count(); ++p)
					ais.add(new utils.RandomAI());

				final Context tempContext = new Context(game, new Trial(game));
				final Trial trial = tempContext.trial();
				game.start(tempContext);

				for (int p = 1; p <= game.players().count(); ++p)
					ais.get(p).initAI(game, p);

				final Model model = tempContext.model();

				while (!trial.over())
				{
					final int mover = tempContext.state().mover();
					final Phase currPhase = game.rules().phases()[tempContext.state().currentPhase(mover)];
					final Moves legal = currPhase.play().moves().eval(tempContext);

					for (int j = 0; j < legal.moves().size(); j++)
					{
						final Move m1 = legal.moves().get(j);

						for (int k = j + 1; k < legal.moves().size(); k++)
						{
							final Move m2 = legal.moves().get(k);

							if (Model.movesEqual(m1, m2, context))
							{
								duplicateMove = true;
								results.append("Move num " + trial.moveNumber() + " with Move = " + m1 + "\n");
								break;
							}
						}
						if (duplicateMove)
							break;
					}

					if (duplicateMove)
						break;

					model.startNewStep(tempContext, ais, 1.0);
				}
			}

			if (!duplicateMove)
				results.append("No duplicate moves detected.\n");
			else
				results.append("DUPLICATE MOVES DETECTED!\n");

			app.addTextToAnalysisPanel(results.toString());
		}
		else if (source.getText().equals("Resign Game"))
		{
			if (app.manager().settingsNetwork().getNetworkPlayerNumber() > Constants.MAX_PLAYERS)
			{
				app.addTextToStatusPanel("You are just a spectator, leave the game alone!\n");
			}
			else if (!app.manager().settingsNetwork().activePlayers()[app.manager().settingsNetwork().getNetworkPlayerNumber()])
			{
				app.addTextToStatusPanel("The game is already over for you.");
			}
			else if (app.manager().settingsNetwork().getActiveGameId() != 0)
			{
				final URL resource = app.getClass().getResource("/ludii-logo-64x64.png");
				BufferedImage image = null;
				try
				{
					image = ImageIO.read(resource);
				}
				catch (final IOException e1)
				{
					//couldn't read logo
				}
				final int dialogResult = JOptionPane.showConfirmDialog (DesktopApp.frame(), "Do you really want to resign this game?\nIf the game has already started then this will be considered a loss.", "Last Chance!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, new ImageIcon(image));
				if(dialogResult == JOptionPane.YES_OPTION)
				{
					app.manager().databaseFunctionsPublic().sendForfeitToDatabase(app.manager());
				}
			}
		}
		else if (source.getText().equals("Leave Game"))
		{
			app.remoteDialogFunctionsPublic().leaveGameUpdateGui(app.manager());
		}
		else if (source.getText().equals("Propose/Accept a Draw"))
		{
			if (app.manager().settingsNetwork().getNetworkPlayerNumber() > Constants.MAX_PLAYERS)
				app.addTextToStatusPanel("You are just a spectator, leave the game alone!\n");
			else if (!app.manager().settingsNetwork().activePlayers()[app.manager().settingsNetwork().getNetworkPlayerNumber()])
				app.addTextToStatusPanel("The game is already over for you.");
			else if (app.manager().settingsNetwork().getActiveGameId() != 0)
				app.manager().databaseFunctionsPublic().sendProposeDraw(app.manager());
		}
		else if (source.getText().equals("List Legal Moves"))
		{
			final Moves legal = game.moves(context);
			app.addTextToStatusPanel("Legal Moves: " + "\n");
			for (int i = 0; i < legal.moves().size(); i++)
				app.addTextToStatusPanel(i + " - " + legal.moves().get(i).getActionsWithConsequences(context) + "\n");
		}
		else if (source.getText().equals("Game Screenshot"))
		{
			ScreenCapture.gameScreenshot("Image " + new Date().getTime());
		}
		else if (source.getText().equals("Game Gif"))
		{
			final String numFramesString = JOptionPane.showInputDialog("How many frames? (1 frame ≈ 100ms)");
			int numFrames = 10;
			try
			{
				numFrames = Integer.parseInt(numFramesString);
			}
			catch (final Exception e2)
			{
				app.addTextToStatusPanel("Invalid entry, defaulting to 10 frames.");
			}
			final int finalNumFrames = numFrames;
			
			// Delay the gif animation a little bit to allow all dialogs to close.
			new java.util.Timer().schedule
			( 
		        new java.util.TimerTask() 
		        {
		            @Override
		            public void run() 
		            {	            	
		            	ScreenCapture.gameGif("Image " + new Date().getTime(), finalNumFrames);
		            }
		        }, 
		        500 
			);	
		}
		else if (source.getText().equals("Make QR Code"))
		{
			QrCodeGeneration.makeQRCode(game);
		}
		else if (source.getText().equals("Play/Pause"))
		{
			DesktopApp.view().toolPanel().buttons.get(ToolView.PLAY_BUTTON_INDEX).press();
		}
		else if (source.getText().equals("Previous Move"))
		{
			DesktopApp.view().toolPanel().buttons.get(ToolView.BACK_BUTTON_INDEX).press();
		}
		else if (source.getText().equals("Next Move"))
		{
			DesktopApp.view().toolPanel().buttons.get(ToolView.FORWARD_BUTTON_INDEX).press();
		}
		else if (source.getText().equals("Go To Start"))
		{
			DesktopApp.view().toolPanel().buttons.get(ToolView.START_BUTTON_INDEX).press();
		}
		else if (source.getText().equals("Go To End"))
		{
			DesktopApp.view().toolPanel().buttons.get(ToolView.END_BUTTON_INDEX).press();
		}
		else if (source.getText().equals("Pass"))
		{
			DesktopApp.view().toolPanel().buttons.get(ToolView.PASS_BUTTON_INDEX).press();
		}
		else if (source.getText().equals("Random Playout Instance"))
		{
			app.manager().ref().randomPlayoutSingleInstance(app.manager());
		}
		else if (source.getText().equals("Clear Board"))
		{
			final Moves csq = new BaseMoves(null);
    		final Move nextMove = new Move(new ActionSetNextPlayer(context.state().mover()));
    		csq.moves().add(nextMove);
    		
			for (int i = 0; i < context.board().numSites(); i++)
			{
				for (int j = 0; j < Constants.MAX_STACK_HEIGHT; j++)
				{ 
					final Action actionRemove = ActionRemove.construct(context.board().defaultSite(), i, j, true);
					final Move moveToApply = new Move(actionRemove);
					moveToApply.then().add(csq);
					
					for (final Action a : moveToApply.actions())
		    			a.apply(context, false);
					
					final int currentMover = context.state().mover();
					final int nextMover = context.state().next();
					final int previousMover = context.state().prev();
	
					context.state().setMover(currentMover);
					context.state().setNext(nextMover);
					context.state().setPrev(previousMover);
				}
			}
			app.updateTabs(context);
		}
		else if (source.getText().equals("Next Player"))
		{
			final ActionSetNextPlayer actionSetNextPlayer = new ActionSetNextPlayer(context.state().next());
			final Move moveToApply = new Move(actionSetNextPlayer);

    		for (final Action a : moveToApply.actions())
    			a.apply(context, false);

			final int currentMover = context.state().mover();
			final int nextMover = context.state().next();
			final int previousMover = context.state().prev();

			context.state().setMover(currentMover);
			context.state().setNext(nextMover);
			context.state().setPrev(previousMover);

			app.updateTabs(context);
		}
		else if (source.getText().equals("Cycle Players"))
		{	
			AIUtil.cycleAgents(app.manager());
		}
		else if (source.getText().equals("Generate Grammar"))
		{
			app.addTextToStatusPanel(Grammar.grammar().toString());
			System.out.print(Grammar.grammar());
			
			System.out.println("Aliases in grammar:\n" + Grammar.grammar().aliases());
			
			try
			{
				Grammar.grammar().export("ludii-grammar-" + Constants.LUDEME_VERSION + ".txt");
			}
			catch (final IOException e1)
			{
				e1.printStackTrace();
			}
		}
		else if (source.getText().equals("Generate Symbols"))
		{
			app.addTextToStatusPanel(Grammar.grammar().getFormattedSymbols());
			System.out.print(Grammar.grammar().getFormattedSymbols());
			System.out.print(Grammar.grammar().symbolDetails());
		}
		else if (source.getText().equals("Rules in English"))
		{
			final String rules = game.toEnglish(game);
			app.addTextToStatusPanel(rules);
			System.out.print(rules);
		}
		else if (source.getText().equals("Game Manual Generation (Beta)"))
		{
			ManualGeneration.manualGeneration(app);
		}
		else if (source.getText().equals("Estimate Branching Factor"))
		{
			EvalUtil.estimateBranchingFactor(app);
		}
		else if (source.getText().equals("Estimate Game Length"))
		{
			EvalUtil.estimateGameLength(app);
		}
		else if (source.getText().equals("Estimate Game Tree Complexity"))
		{
			EvalUtil.estimateGameTreeComplexity(app, false);
		}
		else if (source.getText().equals("Estimate Game Tree Complexity (No State Repetition)"))
		{
			EvalUtil.estimateGameTreeComplexity(app, true);
		}
		else if (source.getText().equals("Compare Agents"))
		{
			final List<AI> aiList = new ArrayList<>();
			for (int i = 1; i <= game.players().count(); i++) 
			{
				final AI agent = app.manager().aiSelected()[i].ai();
				if (agent == null)
					app.addTextToStatusPanel("Player " + i + " should be set to an AI player.");
				else
					aiList.add(agent);
			}
			
			if (aiList.size() == game.players().count())
			{
				final String playoutNumberString = JOptionPane.showInputDialog("How many playouts?");
				try 
				{
					final int playoutNumber = Integer.parseInt(playoutNumberString);
	
					agentComparisonThread = new Thread(() ->
					{
						final EvalGamesSet gamesSet = 
								new EvalGamesSet()
								.setGameName(game.name() + ".lud")
								.setAgents(aiList)
								.setMaxSeconds(AIDetails.convertToThinkTimeArray(app.manager().aiSelected()))
								.setWarmingUpSecs(0)
								.setNumGames(playoutNumber)
								.setRoundToNextPermutationsDivisor(false)
								.setRotateAgents(true);
						
						gamesSet.startGames(game);
						app.addTextToAnalysisPanel(gamesSet.resultsSummary().generateIntermediateSummary());
						app.selectAnalysisTab();
						app.setTemporaryMessage("");
						app.setTemporaryMessage("Comparions have finished.\n");
					});
					
					app.setTemporaryMessage("");
					app.setTemporaryMessage("Comparions have started.\n");
					agentComparisonThread.setDaemon(true);
					agentComparisonThread.start();				
				}
				catch (final NumberFormatException numberException)
				{
					app.addTextToStatusPanel("Invalid number of playouts");
				}		
			}
		}
		else if (source.getText().equals("Prove Win"))
		{
			EvalUtil.proveState(app, ProofGoals.PROVE_WIN);
		}
		else if (source.getText().equals("Prove Loss"))
		{
			EvalUtil.proveState(app, ProofGoals.PROVE_LOSS);
		}
		else if (source.getText().equals("Evaluate AI vs. AI"))
		{
			if (!app.manager().settingsManager().agentsPaused())
			{
				DesktopApp.view().tabPanel().page(TabView.PanelAnalysis).addText("Cannot start evaluation of AIs when agents are not paused!");
				return;
			}

			final EvalAIsThread evalThread = EvalAIsThread.construct(app.manager().ref(),
					AIDetails.convertToAIList(app.manager().aiSelected()), app.manager());
			app.manager().settingsManager().setAgentsPaused(app.manager(), false);
			evalThread.start();
			
		}
		else if (source.getText().startsWith("Evaluation Dialog"))
		{
			EvaluationDialog.showDialog(app);
		}
		else if (source.getText().equals("Clear Status Panel"))
		{
			DesktopApp.view().tabPanel().page(TabView.PanelStatus).clear();
		}
		else if (source.getText().startsWith("Compile Game (Debug)"))
		{
			if (!app.manager().settingsManager().agentsPaused())
			{
				app.manager().settingsManager().setAgentsPaused(app.manager(), true);
			}
			GameLoading.loadGameFromMemory(app, true);
		}
		else if (source.getText().startsWith("Recompile Current Game"))
		{
			GameSetup.compileAndShowGame(app, context.game().description().raw(), false);
		}
		else if (source.getText().startsWith("Show Call Tree"))
		{
			final Call callTree = game.description().callTree();
			System.out.println(callTree);
			callTree.export("call-tree-" + game.name() + ".txt");
		}
		else if (source.getText().startsWith("Expanded Description"))
		{
			// Get the user to choose a .lud
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
			if (choice == null)
				return;

			// Get game description from resource
			String path = choice.replaceAll(Pattern.quote("\\"), "/");
			path = path.substring(path.indexOf("/lud/"));

			final InputStream in = GameLoader.class.getResourceAsStream(path);

			String desc = "";
			try (final BufferedReader rdr = new BufferedReader(new InputStreamReader(in, "UTF-8")))
			{
				String line;
				while ((line = rdr.readLine()) != null)
					desc += line + "\n";
			}
			catch (final IOException e2)
			{
				e2.printStackTrace();
			}
				
			final Description gameDescription = new Description(desc);
			final boolean didParse = Parser.expandAndParse
									 (
										 gameDescription,
										 app.manager().settingsManager().userSelections(), 
										 new Report(),
										 false  //true
									 ); 
			
			final String report =  "Expanded game description:\n" +
			                		gameDescription.expanded() +
			                		"\nGame description " + (didParse ? "parsed." : "did not parse. ");
			System.out.println(report);
			app.addTextToStatusPanel(report);
		}
		else if (source.getText().startsWith("Metadata Description"))
		{
			// Get the user to choose a .lud
			final String[] choices = FileHandling.listGames();

			String initialChoice = choices[0];
			for (final String choice : choices)
				if (app.manager().savedLudName() != null && app.manager().savedLudName().endsWith(choice.replaceAll(Pattern.quote("\\"), "/")))
				{
					initialChoice = choice;
					break;
				}

			final String choice = GameLoaderDialog.showDialog(DesktopApp.frame(), choices, initialChoice);
			if (choice == null)
				return;

			// Get game description from resource
			String path = choice.replaceAll(Pattern.quote("\\"), "/");
			path = path.substring(path.indexOf("/lud/"));

			final InputStream in = GameLoader.class.getResourceAsStream(path);

			String desc = "";
			try (final BufferedReader rdr = new BufferedReader(new InputStreamReader(in, "UTF-8")))
			{
				String line;
				while ((line = rdr.readLine()) != null)
					desc += line + "\n";
			}
			catch (final IOException e2)
			{
				e2.printStackTrace();
			}
				
			final Description gameDescription = new Description(desc);
			Parser.expandAndParse
			(
				gameDescription, 
				app.manager().settingsManager().userSelections(), 
				new Report(), 
				false
			); 
			
			final String report = "Metadata:\n" + gameDescription.metadata();
			System.out.println(report);
			app.addTextToStatusPanel(report);
		}
		else if (source.getText().startsWith("Jump to Move"))
		{
			try
			{
				final int moveToJumpTo = Integer.parseInt(JOptionPane.showInputDialog(DesktopApp.frame(), "Which move to jump to?"));
				if (app.manager().settingsNetwork().getActiveGameId() == 0)
					ToolView.jumpToMove(app, moveToJumpTo + app.contextSnapshot().getContext(app).trial().numInitialPlacementMoves());
			}
			catch (final NumberFormatException exception)
			{
				// Probably just closed the dialog.
			}
		}
		else if (source.getText().startsWith("Serialise Game Object"))
		{
			System.out.println("Game object not serialisable yet.");
			app.addTextToStatusPanel("Game object not serialisable yet.");
			
//			// Choose a default file path
//			String tempFilePath = DesktopApp.lastSelectedJsonPath();
//			if (tempFilePath == null)
//				tempFilePath = System.getProperty("user.dir");
//			final String defaultFilePath = tempFilePath;
//			
//			// Get the destination folder
//			final JFileChooser fileChooser = FileLoading.createFileChooser(defaultFilePath, ".*", "All files");  //".txt", "TXT files (.txt)");
//			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//			fileChooser.setDialogTitle("Select output directory.");
//
//			final int choice = fileChooser.showOpenDialog(DesktopApp.frame());
//			final File directory = (choice == JFileChooser.APPROVE_OPTION) ? fileChooser.getSelectedFile() : null;
//
//			if (directory != null && directory.exists())
//			{
//				System.out.println("Storing to folder: " + directory);
//				
//				final String outFileName = "game.ser";
//				try 
//				(
//					final FileOutputStream file = new FileOutputStream(outFileName); 
//					final ObjectOutputStream out = new ObjectOutputStream(file)
//				)
//				{
//					out.writeObject(game);
//				}
//				catch(final Exception e1)
//				{
//					e1.printStackTrace();
//				}
//				System.out.println("Game has been serialised.");
//			}
//			else
//			{
//				System.out.println("Something went wrong.");
//			}
		}
		else if (source.getText().startsWith("Print Working Directory"))
		{
			final File file = new File(".");
			try
			{
				app.addTextToStatusPanel("Current working directory: " + file.getCanonicalPath());
			}
			catch (final IOException e1)
			{
				e1.printStackTrace();
			}
		}
		else if (source.getText().startsWith("Evaluate Heuristic"))
		{
			final String heuristicStr = JOptionPane.showInputDialog(DesktopApp.frame(), "Enter heuristic.");
			
			if (app.manager().settingsNetwork().getActiveGameId() == 0)
			{
				final Heuristics heuristics = 
						(Heuristics)compiler.Compiler.compileObject
						(
							"(heuristics " + heuristicStr + ")", 
							"metadata.ai.heuristics.Heuristics",
							new Report()
						);
				heuristics.init(game);
				
				for (int p = 1; p <= game.players().count(); ++p)
				{
					app.addTextToAnalysisPanel("Heuristic score for Player " + p + " = " + heuristics.computeValue(context, p, 0.f) + "\n");
				}
			}
		}
		else if (source.getText().startsWith("Evaluate Features"))
		{
			final String featuresStr = JOptionPane.showInputDialog(DesktopApp.frame(), "Enter features.");
			
			if (app.manager().settingsNetwork().getActiveGameId() == 0)
			{
				final Features features = 
						(Features)compiler.Compiler.compileObject
						(
							featuresStr, 
							"metadata.ai.features.Features",
							new Report()
						);
				final SoftmaxPolicyLinear softmax = SoftmaxPolicyLinear.constructSelectionPolicy(features, 0.0);
				softmax.initAI(game, context.state().mover());
				
				final BaseFeatureSet[] featureSets = softmax.featureSets();
				final BaseFeatureSet featureSet;
				if (featureSets.length == 1)
					featureSet = featureSets[0];
				else
					featureSet = featureSets[context.state().mover()];
				
				final FastArrayList<Move> legalMoves = game.moves(context).moves();
				final TIntArrayList[] sparseFeatureVectors = 
						featureSet.computeSparseSpatialFeatureVectors
						(
							context, 
							legalMoves, 
							false
						);
				
				@SuppressWarnings("unchecked")
				final List<String>[] moveStrings = new List[featureSet.getNumSpatialFeatures()];
				for (int moveIdx = 0; moveIdx < legalMoves.size(); ++moveIdx)
				{
					final TIntArrayList activeFeatures = sparseFeatureVectors[moveIdx];
					for (int i = 0; i < activeFeatures.size(); ++i)
					{
						final int featureIdx = activeFeatures.getQuick(i);
						
						if (moveStrings[featureIdx] == null)
							moveStrings[featureIdx] = new ArrayList<String>();
						
						moveStrings[featureIdx].add(legalMoves.get(moveIdx).toString());
					}
				}
				
				System.out.println("------------------------------------------------");
				System.out.println("Printing moves for which features are active...");
				
				for (int featureIdx = 0; featureIdx < moveStrings.length; ++featureIdx)
				{
					final List<String> moves = moveStrings[featureIdx];
					
					if (moves != null && moves.size() > 0)
					{
						System.out.println("Feature: " + featureSet.spatialFeatures()[featureIdx].toString());
						
						for (final String moveStr : moves)
						{
							System.out.println("active for move: " + moveStr);
						}
					}
				}
				
				System.out.println("------------------------------------------------");
				
				app.addTextToAnalysisPanel("Active features are only printed in the console.\n");
			}
		}
		else if (source.getText().startsWith("Game Hashcode"))
		{
			System.out.println("File encoding: " + System.getProperty("file.encoding"));
			app.addTextToStatusPanel("Game Hashcode: " + StringUtil.hashCode(game.description().raw()) + "\n");
		}
		else if (source.getText().startsWith("Print Board Graph"))
		{
			System.out.println(context.board().graph());
		}
//		else if (source.getText().startsWith("Advanced Distance Dialog"))
//		{
//			AdvancedDistanceDialog.showDialog();
//		}
		else if (source.getText().startsWith("Print Trajectories"))
		{
			context.board().graph().trajectories().report(context.board().graph());
		}
		else if (source.getText().startsWith("Preferences"))
		{
			app.showSettingsDialog();
		}
		else if (source.getText().equals("Load SVG"))
		{
			MiscLoading.loadSVG(app, DesktopApp.view());
		}
		else if (source.getText().equals("View SVG"))
		{
			SVGViewerDialog.showDialog(app, DesktopApp.frame(), SVGLoader.listSVGs());
		}
		else if (source.getText().equals("Remote Play"))
		{
			app.manager().settingsNetwork().setRemoteDialogPosition(null);
			app.remoteDialogFunctionsPublic().showRemoteDialog(app);
		}
		else if (source.getText().equals("Initialise Server Socket"))
		{
			final String port = JOptionPane.showInputDialog("Port Number (4 digits)");
			int portNumber = 0;
			try
			{
				if (port.length() != 4)
					throw new Exception("Port number must be four digits long.");
				portNumber = Integer.parseInt(port);
			}
			catch (final Exception E)
			{
				app.addTextToStatusPanel("Please enter a valid four digit port number.\n");
				return;
			}
			LocalFunctions.initialiseServerSocket(app.manager(), portNumber);
		}
		else if (source.getText().equals("Test Message Socket"))
		{
			final String port = JOptionPane.showInputDialog("Port Number (4 digits)");
			int portNumber = 0;
			try
			{
				if (port.length() != 4)
					throw new Exception("Port number must be four digits long.");
				portNumber = Integer.parseInt(port);
			}
			catch (final Exception E)
			{
				app.addTextToStatusPanel("Please enter a valid four digit port number.\n");
				return;
			}
			final String message = JOptionPane.showInputDialog("Message");
			LocalFunctions.initialiseClientSocket(portNumber, message);
		}
		else if (source.getText().equals("Select Move from String"))
		{
			final FastArrayList<Move> substringMatchingMoves = new FastArrayList<>();
			boolean exactMatchFound = false;
			final String moveString = JOptionPane.showInputDialog("Enter desired move in Trial, Turn or Move format.");
			for (final Move m : context.game().moves(context).moves())
			{
				// Check for exact match first
				if (
						m.toTrialFormat(context).equals(moveString)
						||
						m.toTurnFormat(context, true).equals(moveString)
						||
						m.toTurnFormat(context, false).equals(moveString)
						||
						m.toMoveFormat(context, true).equals(moveString)
						||
						m.toMoveFormat(context, false).equals(moveString)
						||
						m.toString().equals(moveString)
					)
				{
					exactMatchFound = true;
					app.manager().ref().applyHumanMoveToGame(app.manager(), m);
					break;
				}
				else
				{
					// Check for substring match
					if (m.toTrialFormat(context).contains(moveString))
						substringMatchingMoves.add(m);
					else if (m.toTurnFormat(context, true).contains(moveString))
						substringMatchingMoves.add(m);
					else if (m.toTurnFormat(context, false).contains(moveString))
						substringMatchingMoves.add(m);
					else if (m.toMoveFormat(context, true).contains(moveString))
						substringMatchingMoves.add(m);
					else if (m.toMoveFormat(context, false).contains(moveString))
						substringMatchingMoves.add(m);
					else if (m.toString().contains(moveString))
						substringMatchingMoves.add(m);
				}
			}
			if (!exactMatchFound)
			{
				if (substringMatchingMoves.size() == 1)
					app.manager().ref().applyHumanMoveToGame(app.manager(), substringMatchingMoves.get(0));
				else if (substringMatchingMoves.size() > 1)
					PossibleMovesDialog.createAndShowGUI(app, context, substringMatchingMoves, true);
				else
					System.out.println("No matching move found.");
			}
		}
		else if (source.getText().equals("Quit"))
		{
			System.exit(0);
		}
		else if (source.getText().equals("More Developer Options"))
		{
			DeveloperDialog.showDialog(app);
		}
		else if (source.getText().equals("Generate Random Game"))
		{
			boolean validGameFound = false;
			while (!validGameFound)
			{
				final String gameDescription = Generator.testGames(1, true, true, false, true);
				if (gameDescription != null)
				{
					GameSetup.compileAndShowGame(app, gameDescription, false);
					validGameFound = true;
				}
			}
		}
		else if (source.getText().equals("Generate 1000 Random Games"))
		{
			Generator.testGames
			(
				1000,   // num games 
				true,   // random
				true,   // valid
				false,  // boardless
				true    // save
			);
		}
		else if (source.getText().equals("Generate 1 Game with Restrictions (dev)"))
		{
			Generator.testGamesEric(1, true, false);
		}
		else if (source.getText().equals("Contextual Distance"))
		{
			final Map<String, Double> rulesetSimilaritiesCultural = ContextualSimilarity.getRulesetSimilarities(game, false);
	        final Map<String, Double> rulesetSimilaritiesConcept = ContextualSimilarity.getRulesetSimilarities(game, true);
	        final Map<String, Double> rulesetSimilaritiesGeographical = ContextualSimilarity.getRulesetGeographicSimilarities(game);
	        final Map<String, Double> rulesetSimilaritiesYears = ContextualSimilarity.getRulesetYearSimilarities(game);

			final int NUM_TO_PRINT = 10;
			
			System.out.println("------------------");
			System.out.println("Closest cultural rulesets:");
			if (new HashSet<Double>(rulesetSimilaritiesCultural.values()).size() > 1)
			{
				for(int i = 0; i < NUM_TO_PRINT; i++)
				{
					final double maxValueInMapCultural = (Collections.max(rulesetSimilaritiesCultural.values()).doubleValue());
					if (maxValueInMapCultural > 0)
				        for (final Entry<String, Double> entry : rulesetSimilaritiesCultural.entrySet()) 
				            if (entry.getValue().doubleValue() == maxValueInMapCultural) 
				                System.out.println(entry.getKey() + " (" + maxValueInMapCultural + ")");     // Print the rulesets with max cultural similarity
	
					rulesetSimilaritiesCultural.values().removeIf(value -> (value.doubleValue() == maxValueInMapCultural));
				}
			}
			
			System.out.println();
			System.out.println("Closest concept rulesets:");
			if (new HashSet<Double>(rulesetSimilaritiesConcept.values()).size() > 1)
			{
				for(int i = 0; i < NUM_TO_PRINT; i++)
				{
					final double maxValueInMapConcept = (Collections.max(rulesetSimilaritiesConcept.values()).doubleValue());
					if (maxValueInMapConcept > 0)
				        for (final Entry<String, Double> entry : rulesetSimilaritiesConcept.entrySet()) 
				            if (entry.getValue().doubleValue() == maxValueInMapConcept) 
				                System.out.println(entry.getKey() + " (" + maxValueInMapConcept + ")");     // Print the rulesets with max concept similarity
	
					rulesetSimilaritiesConcept.values().removeIf(value -> (value.doubleValue() == maxValueInMapConcept));
				}
			}
			
			System.out.println();
			System.out.println("Closest geographical rulesets:");
			if (new HashSet<Double>(rulesetSimilaritiesGeographical.values()).size() > 1)
			{
				for(int i = 0; i < NUM_TO_PRINT; i++)
				{
					final double maxValueInMapConcept = (Collections.max(rulesetSimilaritiesGeographical.values()).doubleValue());
					if (maxValueInMapConcept > 0)
				        for (final Entry<String, Double> entry : rulesetSimilaritiesGeographical.entrySet()) 
				            if (entry.getValue().doubleValue() == maxValueInMapConcept) 
				                System.out.println(entry.getKey() + " (" + maxValueInMapConcept + ")");     // Print the rulesets with max geographic similarity
	
					rulesetSimilaritiesGeographical.values().removeIf(value -> (value.doubleValue() == maxValueInMapConcept));
				}
			}
			
			System.out.println();
			System.out.println("Closest year rulesets:");
			if (new HashSet<Double>(rulesetSimilaritiesYears.values()).size() > 1)
			{
				for(int i = 0; i < NUM_TO_PRINT; i++)
				{
					final double maxValueInMapConcept = (Collections.max(rulesetSimilaritiesYears.values()).doubleValue());
					if (maxValueInMapConcept > 0)
				        for (final Entry<String, Double> entry : rulesetSimilaritiesYears.entrySet()) 
				            if (entry.getValue().doubleValue() == maxValueInMapConcept) 
				                System.out.println(entry.getKey() + " (" + maxValueInMapConcept + ")");     // Print the rulesets with max year similarity
	
					rulesetSimilaritiesYears.values().removeIf(value -> (value.doubleValue() == maxValueInMapConcept));
				}
			}
		}
		else if (source.getText().equals("Reconstruction Dialog"))
		{
			ReconstructionDialog.createAndShowGUI(app);
		}
		else if (((JMenu)((JPopupMenu) source.getParent()).getInvoker()).getText().equals("Load Recent"))
		{
			// Check if a recent game has been selected
			try
			{
				if (source.getText().contains(".lud")) 		// load game from external file
					GameLoading.loadGameFromFilePath(app, source.getText());
				else
					GameLoading.loadGameFromName(app, source.getText() + ".lud", new ArrayList<String>(), false);
			}
			catch (final Exception E)
			{
				System.out.println("This game no longer exists");
			}
		}
		else if (source.getText().equals("Portfolio Parameters (external)"))
		{
			final Map<String, Map<String, Double>> portfolioParameterPredictions = AgentPredictionExternal.predictPortfolioParameters(game);
			System.out.println("\n" + portfolioParameterPredictions + "\n");
			for (final String parameterName : portfolioParameterPredictions.keySet())
			{
				double highestPredictionProb = -1.0;
				String bestPredictionLabel = "";
				for (final String paramValue : portfolioParameterPredictions.get(parameterName).keySet())
				{ 
					if (portfolioParameterPredictions.get(parameterName).get(paramValue).doubleValue() > highestPredictionProb)
					{
						highestPredictionProb = portfolioParameterPredictions.get(parameterName).get(paramValue).doubleValue();
						bestPredictionLabel = paramValue;
					}
				}
				System.out.println(parameterName + ": " + bestPredictionLabel + " (" + highestPredictionProb + ")");
			}
		}
		else if (getParentTitle(source, 2).equals("Predict Metrics (external)"))
		{
			final boolean useCompilationOnly = getParentTitle(source, 1).equals("Compilation");
			final String modelFilePath = source.getText() + "-Regression" + "-Metrics" + "-" + (useCompilationOnly ? "True" : "False");
			final Map<String, Double> metricPredictions = MetricPredictionExternal.predictMetrics(game, modelFilePath, useCompilationOnly);
			displayPredictionResults(app, metricPredictions, false, false);
		}
		else if (getParentTitle(source, 3).equals("Predict Best Agent (external)"))
		{
			// Agent prediction
			final boolean useHeuristics = false;
			final boolean useClassifier = getParentTitle(source, 2).equals("Classification");
			final boolean useCompilationOnly = getParentTitle(source, 1).equals("Compilation");
			
			// Determine the file path for the model
			final String modelFilePath = AgentPredictionExternal.getModelPath(source.getText(), useClassifier, useHeuristics, useCompilationOnly);
			
			System.out.println("Predicting...\n");
			final Map<String, Double> agentPredictions = AgentPredictionExternal.predictBestAgent(game, modelFilePath, useClassifier, useHeuristics, useCompilationOnly);
			displayPredictionResults(app, agentPredictions, useClassifier, true);

		}
		else if (getParentTitle(source, 3).equals("Predict Best Heuristic (external)"))
		{
			// Heuristic prediction
			final boolean useHeuristics = true;
			final boolean useClassifier = getParentTitle(source, 2).equals("Classification");
			final boolean useCompilationOnly = getParentTitle(source, 1).equals("Compilation");
			
			// Determine the file path for the model
			final String modelFilePath = AgentPredictionExternal.getModelPath(source.getText(), useClassifier, useHeuristics, useCompilationOnly);
			
			System.out.println("Predicting...\n");
			final Map<String, Double> heuristicPredictions = AgentPredictionExternal.predictBestAgent(game, modelFilePath, useClassifier, useHeuristics, useCompilationOnly);
			displayPredictionResults(app, heuristicPredictions, useClassifier, true);
		}
		
		EventQueue.invokeLater(() ->
		{
			app.resetMenuGUI();
			app.repaint();
		});
	}
	
	private static void displayPredictionResults(final PlayerApp app, final Map<String, Double> agentPredictions, final boolean useClassifier, final boolean printHighestValue)
	{
		app.manager().getPlayerInterface().selectAnalysisTab();
		
		String bestPredictedAgentName = "None";
		double bestPredictedValue = -99999;
		for (final String agentName : agentPredictions.keySet())
		{
			final double score = agentPredictions.get(agentName).doubleValue();
			
			if (useClassifier)
				app.manager().getPlayerInterface().addTextToAnalysisPanel("Predicted probability for " + agentName + ": " + score + "\n");
			else
				app.manager().getPlayerInterface().addTextToAnalysisPanel("Predicted value for " + agentName + ": " + score + "\n");
			
			if (score > bestPredictedValue)
			{
				bestPredictedValue = score;
				bestPredictedAgentName = agentName;
			}
		}

		if (printHighestValue)
			app.manager().getPlayerInterface().addTextToAnalysisPanel("Best Predicted Agent/Heuristic is " + bestPredictedAgentName + "\n");
		
		app.manager().getPlayerInterface().addTextToAnalysisPanel("//-------------------------------------------------------------------------\n");
		System.out.println("Prediction complete.\n");
	}
	
	//-------------------------------------------------------------------------
	
	public static void checkItemStateChanges(final PlayerApp app, final ItemEvent e)
	{
		final JMenuItem source = (JMenuItem) (e.getSource());
		final Context context = app.contextSnapshot().getContext(app);

		JMenuItem rootParent = (JMenu)((JPopupMenu)source.getParent()).getInvoker();
		while (rootParent.getParent() instanceof JPopupMenu)
			rootParent = (JMenu)((JPopupMenu)rootParent.getParent()).getInvoker();
		
		if (rootParent.getText().equals("Options"))
		{
			// Check if an in-game option or ruleset has been selected
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				final Game game = context.game();
				final GameOptions gameOptions = game.description().gameOptions();
				
				// First, check if a predefined ruleset has been selected. Also check parent in case default ruleset variation selected.
				final JMenu sourceParent = (JMenu)((JPopupMenu)source.getParent()).getInvoker();
				final boolean rulesetSelected = GameUtil.checkMatchingRulesets(app, game, source.getText()) || GameUtil.checkMatchingRulesets(app, game, sourceParent.getText());
	
				// Second, check if an option has been selected
				if (!rulesetSelected && gameOptions.numCategories() > 0 && source.getParent() != null)
				{
					final JMenu parent = (JMenu)((JPopupMenu)source.getParent()).getInvoker();
					
					final List<String> currentOptions = app.manager().settingsManager().userSelections().selectedOptionStrings();
	
					for (int cat = 0; cat < gameOptions.numCategories(); cat++)
					{
						final List<Option> options = gameOptions.categories().get(cat).options();
						if (options.isEmpty())
							continue; // no options in this group
	
						if (!options.get(0).menuHeadings().get(0).equals(parent.getText()))
							continue; // not this option group
	
						for (final Option option : options)
						{
							if (option.menuHeadings().get(1).equals(source.getText()))
							{
								// Match!
								final String selectedOptString = StringRoutines.join("/", option.menuHeadings());
								
								// Remove any other selected options in the same category
								for (int i = 0; i < currentOptions.size(); ++i)
								{
									final String currOption = currentOptions.get(i);
									
									if 
									(
										currOption.substring(0, currOption.lastIndexOf("/")).equals(
												selectedOptString.substring(0, selectedOptString.lastIndexOf("/"))
												)
									)
									{
										// Found one in same category, so remove it
										currentOptions.remove(i);
										break;	// Should be no more than just this one
									}
								}
								
								// Now add the option we newly selected
								currentOptions.add(selectedOptString);
								app.manager().settingsManager().userSelections().setSelectOptionStrings(currentOptions);
								gameOptions.setOptionsLoaded(true);
								
								// Since we selected an option, we should try to auto-select ruleset
								app.manager().settingsManager().userSelections().setRuleset
								(
									game.description().autoSelectRuleset
									(
										app.manager().settingsManager().userSelections().selectedOptionStrings()
									)
								);
								
								try
								{
									GameSetup.compileAndShowGame(app, game.description().raw(), false);
								}
								catch (final Exception exception)
								{
									GameUtil.resetGame(app, false);
								}
								break;
							}
						}
					}
				}
			}
		}
		else if (source.getText().equals("Show Legal Moves"))
		{
			app.bridge().settingsVC().setShowPossibleMoves(!app.bridge().settingsVC().showPossibleMoves());
		}
		else if (source.getText().equals("Show Board"))
		{
			app.settingsPlayer().setShowBoard(!app.settingsPlayer().showBoard());
		}
		else if (source.getText().equals("Show dev tooltip"))
		{
			app.settingsPlayer().setCursorTooltipDev(!app.settingsPlayer().cursorTooltipDev());
		}
		else if (source.getText().equals("Show Pieces"))
		{
			app.settingsPlayer().setShowPieces(!app.settingsPlayer().showPieces());
		}
		else if (source.getText().equals("Show Graph"))
		{
			app.settingsPlayer().setShowGraph(!app.settingsPlayer().showGraph());
		}
		else if (source.getText().equals("Show Cell Connections"))
		{
			app.settingsPlayer().setShowConnections(!app.settingsPlayer().showConnections());
		}
		else if (source.getText().equals("Show Axes"))
		{
			app.settingsPlayer().setShowAxes(!app.settingsPlayer().showAxes());
		}
		else if (source.getText().equals("Show Container Indices"))
		{
			app.bridge().settingsVC().setShowContainerIndices(!app.bridge().settingsVC().showContainerIndices());
		}
		else if (source.getText().equals("Sandbox"))
		{
			app.settingsPlayer().setSandboxMode(!app.settingsPlayer().sandboxMode());
			app.addTextToStatusPanel("Warning! Using sandbox mode may result in illegal game states.\n");
		}
		else if (source.getText().equals("Show Indices"))
		{
			app.bridge().settingsVC().setShowIndices(!app.bridge().settingsVC().showIndices());
			if (app.bridge().settingsVC().showCoordinates())
				app.bridge().settingsVC().setShowCoordinates(false);
			if (app.bridge().settingsVC().showIndices())
			{
				app.bridge().settingsVC().setShowVertexIndices(false);
				app.bridge().settingsVC().setShowEdgeIndices(false);
				app.bridge().settingsVC().setShowCellIndices(false);
			}
		}
		else if (source.getText().equals("Show Coordinates"))
		{
			app.bridge().settingsVC().setShowCoordinates(!app.bridge().settingsVC().showCoordinates());
			if (app.bridge().settingsVC().showIndices())
				app.bridge().settingsVC().setShowIndices(false);
			if (app.bridge().settingsVC().showCoordinates())
			{
				app.bridge().settingsVC().setShowVertexCoordinates(false);
				app.bridge().settingsVC().setShowEdgeCoordinates(false);
				app.bridge().settingsVC().setShowCellCoordinates(false);
			}
		}
		else if (source.getText().equals("Show Cell Indices"))
		{
			app.bridge().settingsVC().setShowCellIndices(!app.bridge().settingsVC().showCellIndices());
			if (app.bridge().settingsVC().showCellCoordinates())
				app.bridge().settingsVC().setShowCellCoordinates(false);
			if (app.bridge().settingsVC().showCellIndices())
				app.bridge().settingsVC().setShowIndices(false);
		}
		else if (source.getText().equals("Show Edge Indices"))
		{
			app.bridge().settingsVC().setShowEdgeIndices(!app.bridge().settingsVC().showEdgeIndices());
			if (app.bridge().settingsVC().showEdgeCoordinates())
				app.bridge().settingsVC().setShowEdgeCoordinates(false);
			if (app.bridge().settingsVC().showEdgeIndices())
				app.bridge().settingsVC().setShowIndices(false);
		}
		else if (source.getText().equals("Show Vertex Indices"))
		{
			app.bridge().settingsVC().setShowVertexIndices(!app.bridge().settingsVC().showVertexIndices());
			if (app.bridge().settingsVC().showVertexCoordinates())
				app.bridge().settingsVC().setShowVertexCoordinates(false);
			if (app.bridge().settingsVC().showVertexIndices())
				app.bridge().settingsVC().setShowIndices(false);
		}
		else if (source.getText().equals("Show Cell Coordinates"))
		{
			app.bridge().settingsVC().setShowCellCoordinates(!app.bridge().settingsVC().showCellCoordinates());
			if (app.bridge().settingsVC().showCellIndices())
				app.bridge().settingsVC().setShowCellIndices(false);
			if (app.bridge().settingsVC().showCellCoordinates())
				app.bridge().settingsVC().setShowCoordinates(false);
		}
		else if (source.getText().equals("Show Edge Coordinates"))
		{
			app.bridge().settingsVC().setShowEdgeCoordinates(!app.bridge().settingsVC().showEdgeCoordinates());
			if (app.bridge().settingsVC().showEdgeIndices())
				app.bridge().settingsVC().setShowEdgeIndices(false);
			if (app.bridge().settingsVC().showEdgeCoordinates())
				app.bridge().settingsVC().setShowCoordinates(false);
		}
		else if (source.getText().equals("Show Vertex Coordinates"))
		{
			app.bridge().settingsVC().setShowVertexCoordinates(!app.bridge().settingsVC().showVertexCoordinates());
			if (app.bridge().settingsVC().showVertexIndices())
				app.bridge().settingsVC().setShowVertexIndices(false);
			if (app.bridge().settingsVC().showVertexCoordinates())
				app.bridge().settingsVC().setShowCoordinates(false);
		}
		else if (source.getText().equals("Show Magnifying Glass"))
		{
			app.settingsPlayer().setShowZoomBox(!app.settingsPlayer().showZoomBox());
		}
		else if (source.getText().equals("Show AI Distribution"))
		{
			app.settingsPlayer().setShowAIDistribution(!app.settingsPlayer().showAIDistribution());
		}
		else if (source.getText().equals("Show Last Move"))
		{
			app.settingsPlayer().setShowLastMove(!app.settingsPlayer().showLastMove());
		}
		else if (source.getText().equals("Show Repetitions"))
		{
			app.manager().settingsManager().setShowRepetitions(!app.manager().settingsManager().showRepetitions());
			if (app.manager().settingsManager().showRepetitions())
				app.addTextToStatusPanel("Please restart the game to display repetitions correctly.\n");
		}
		else if (source.getText().equals("Show Ending Moves"))
		{
			app.settingsPlayer().setShowEndingMove(!app.settingsPlayer().showEndingMove());
		}
		else if (source.getText().contains("Show Track"))
		{
			for (int i = 0; i < app.bridge().settingsVC().trackNames().size(); i++)
				if (source.getText().equals(app.bridge().settingsVC().trackNames().get(i)))
					app.bridge().settingsVC().trackShown().set(i, Boolean.valueOf(!app.bridge().settingsVC().trackShown().get(i).booleanValue()));
		}
		else if (source.getText().equals("Swap Rule"))
		{
			app.settingsPlayer().setSwapRule(!app.settingsPlayer().swapRule());
			context.game().metaRules().setUsesSwapRule(app.settingsPlayer().swapRule());
			GameUtil.resetGame(app, false);
		}
		else if (source.getText().equals("No Repetition Of Game State"))
		{
			app.settingsPlayer().setNoRepetition(!app.settingsPlayer().noRepetition());
			if (app.settingsPlayer().noRepetition())
				context.game().metaRules().setRepetitionType(RepetitionType.Positional);
			GameUtil.resetGame(app, false);
		}
		else if (source.getText().equals("No Repetition Within A Turn"))
		{
			app.settingsPlayer().setNoRepetitionWithinTurn(!app.settingsPlayer().noRepetitionWithinTurn());
			if (app.settingsPlayer().noRepetition())
				context.game().metaRules().setRepetitionType(RepetitionType.PositionalInTurn);
			GameUtil.resetGame(app, false);
		}
		else if (source.getText().equals("Save Heuristics"))
		{
			app.settingsPlayer().setSaveHeuristics(!app.settingsPlayer().saveHeuristics());
		}
		else if (source.getText().equals("Print Move Features"))
		{
			app.settingsPlayer().setPrintMoveFeatures(!app.settingsPlayer().printMoveFeatures());
		}
		else if (source.getText().equals("Print Move Feature Instances"))
		{
			app.settingsPlayer().setPrintMoveFeatureInstances(!app.settingsPlayer().printMoveFeatureInstances());
		}
		else if (source.getText().equals("Automatic"))
		{
			app.settingsPlayer().setPuzzleDialogOption(PuzzleSelectionType.Automatic);
		}
		else if (source.getText().equals("Dialog"))
		{
			app.settingsPlayer().setPuzzleDialogOption(PuzzleSelectionType.Dialog);
		}
		else if (source.getText().equals("Cycle"))
		{
			app.settingsPlayer().setPuzzleDialogOption(PuzzleSelectionType.Cycle);
		}
		else if (source.getText().equals("Illegal Moves Allowed"))
		{
			app.settingsPlayer().setIllegalMovesValid(!app.settingsPlayer().illegalMovesValid());
		}
		else if (source.getText().equals("Show Possible Values"))
		{
			app.bridge().settingsVC().setShowCandidateValues(!app.bridge().settingsVC().showCandidateValues());
		}
		else
		{
			System.out.println("NO MATCHING MENU OPTION FOUND");
		}

		EventQueue.invokeLater(() ->
		{
			app.resetMenuGUI();
			app.repaint();
		});
	}
	
	//---------------------------------------------------------------------

	// Returns the parent menu title of a JMenuItem a certain number of steps back (depth).
	private static String getParentTitle(final JMenuItem source, final int depth)
	{
		if (depth <= 0)
			return source.getText();
		
		Container currentContainer = source;
		for (int i = 0; i < depth; i++)
			currentContainer = ((JMenu)((JPopupMenu) currentContainer.getParent()).getInvoker());
		
		return ((JMenu)currentContainer).getText();
	}

	public static StartVisualEditor getStartVisualEditor()
	{
		return startVisualEditor;
	}

	public static void setStartVisualEditor(final StartVisualEditor startVisualEditor)
	{
		MainMenuFunctions.startVisualEditor = startVisualEditor;
	}
	
	//---------------------------------------------------------------------

}
