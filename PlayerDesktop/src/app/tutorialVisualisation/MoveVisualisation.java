package app.tutorialVisualisation;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import app.DesktopApp;
import app.PlayerApp;
import app.display.screenCapture.ScreenCapture;
import app.utils.AnimationVisualsType;
import app.utils.GameUtil;
import manager.Referee;
import metadata.ai.heuristics.HeuristicUtil;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.HeuristicTerm;
import other.action.Action;
import other.move.Move;
import other.trial.Trial;

public class MoveVisualisation
{
	
	//-------------------------------------------------------------------------
	// Adjustable Settings
	
	/** How many trials to run to provide all the moves for analysis. */
	private final static int numberTrials = 10;
	
	/** Whether or not to include moves that are from the player's hands. */
	private final static boolean includeHandMoves = false;
	
	//-------------------------------------------------------------------------
	
	/** Root file path for storing game specific files. */
	static String rootPath;
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Main entry point for running the move visualisation. 
	 */
	public static void moveVisualisation(final PlayerApp app)
	{
		final Referee ref = app.manager().ref();
		rootPath = "tutorialVisualisation/" + ref.context().game().name() + "/";
		
		// Set some desired visual settings (recommend resetting preferences beforehand).
		app.settingsPlayer().setPerformingTutorialVisualisation(true);
		app.settingsPlayer().setShowEndingMove(false);
		app.bridge().settingsVC().setFlatBoard(true);
		app.settingsPlayer().setShowLastMove(false);
		app.bridge().settingsVC().setShowPossibleMoves(false);
		app.settingsPlayer().setAnimationType(AnimationVisualsType.Single);
		
		DesktopApp.frame().setSize(300, 465);
		app.repaint();

		// Generate all trials that will be used.
		final List<Trial> generatedTrials = new ArrayList<>();
		final List<RandomProviderDefaultState> generatedTrialsRNG = new ArrayList<>();
		MoveGeneration.generateTrials(app, ref, generatedTrials, generatedTrialsRNG, numberTrials);
		
		// Merge all similar moves from our generated trials into a condensed moves list.
		final List<MoveCompleteInformation> condensedMoveList = new ArrayList<>();
		final List<String> rankingStrings = new ArrayList<>();
		final List<MoveCompleteInformation> endingMoveList = new ArrayList<>(); 
		MoveGeneration.recordTrialMoves(app, ref, generatedTrials, generatedTrialsRNG, condensedMoveList, rankingStrings, endingMoveList, includeHandMoves);
		
		System.out.println("\nTotal of " + condensedMoveList.size() + " condensed moves found.");
		System.out.println("Total of " + endingMoveList.size() + " ending moves found.");
		
		// Set delays for each process
		final int delay1 = 1000;
		final int delay2 = 3000;
		final int delay3 = 5000 * (condensedMoveList.size()+1);
		final int delay4 = 5000 * (condensedMoveList.size()+1) + 5000 * (endingMoveList.size()+1);
		
		// Run the required processes
		generateSetupImage(app, delay1);
		generateMoveImages(app, condensedMoveList, delay2);
		generateEndImages(app, endingMoveList, delay3);
		generateWebsite(ref, rankingStrings, condensedMoveList, endingMoveList, delay4);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Take a screenshot of the game before it begins.
	 */
	private final static void generateSetupImage(final PlayerApp app, final int delay)
	{
		GameUtil.resetGame(app, true);
		new java.util.Timer().schedule
		( 
	        new java.util.TimerTask() 
	        {
	            @Override
	            public void run() 
	            {
	            	final String filePath = "screenshot/Game_Setup";
	            	ScreenCapture.gameScreenshot(rootPath + filePath);
	            }
	        }, 
	        delay 
		);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Take a screenshot/video of every move in the condensed list.
	 */
	private final static void generateMoveImages(final PlayerApp app, final List<MoveCompleteInformation> condensedMoveList, final int delay)
	{
		final Timer moveScreenshotTimer = new Timer();
		moveScreenshotTimer.scheduleAtFixedRate(new TimerTask()
		{
			int condensedMoveIndex = 0;
			
		    @Override
		    public void run()
		    {
		    	if (condensedMoveIndex >= condensedMoveList.size())
		    	{
		    		System.out.println("------------------------");
		    		System.out.println("Move image generation complete.");
		    		moveScreenshotTimer.cancel();
		    		moveScreenshotTimer.purge();
		    	}
		    	else
		    	{
		    		System.out.println("------------------------");
		    		System.out.println("Move " + (condensedMoveIndex+1) + "/" + condensedMoveList.size());
			    	final MoveCompleteInformation moveInformation = condensedMoveList.get(condensedMoveIndex);
					takeMoveImage(app, moveInformation, false);
					condensedMoveIndex++;
		    	}
		    }
		}, delay, 5000);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Take a screenshot/video of every move in the ending move list.
	 */
	private final static void generateEndImages(final PlayerApp app, final List<MoveCompleteInformation> endingMoveList, final int delay)
	{
		final Timer endScreenshotTimer = new Timer();
		endScreenshotTimer.scheduleAtFixedRate(new TimerTask()
		{
			int endingMoveIndex = 0;
			
		    @Override
		    public void run()
		    {
		    	app.settingsPlayer().setShowEndingMove(true);
		    	
		    	if (endingMoveIndex >= endingMoveList.size())
		    	{
		    		System.out.println("------------------------");
		    		System.out.println("Ending image generation complete.");
		    		app.settingsPlayer().setShowEndingMove(false);
		    		endScreenshotTimer.cancel();
		    		endScreenshotTimer.purge();
		    	}
		    	else
		    	{
		    		System.out.println("------------------------");
		    		System.out.println("End " + (endingMoveIndex+1) + "/" + endingMoveList.size());
			    	final MoveCompleteInformation moveInformation = endingMoveList.get(endingMoveIndex);
					takeMoveImage(app, moveInformation, true);
					endingMoveIndex++;
		    	}
		    }
		}, delay, 5000);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Once the process is complete, combine all the stored images into a complete document.
	 */
	private final static void generateWebsite(final Referee ref, final List<String> rankingStrings, final List<MoveCompleteInformation> condensedMoveList, final List<MoveCompleteInformation> endingMoveList, final int delay)
	{
		new java.util.Timer().schedule
		( 
	        new java.util.TimerTask() 
	        {
	            @Override
	            public void run() 
	            {
	            	System.out.println("------------------------");
		    		System.out.println("Generating html file.");
	            	
	            	try 
	            	{
	            		final String filePath = rootPath + "output.html";
	            		final File outputFile = new File(filePath);
	            		outputFile.getParentFile().mkdirs();
	            		outputFile.createNewFile();
	            		try(final FileWriter myWriter = new FileWriter(filePath))
	            		{
		            		myWriter.write(HtmlFileOutput.htmlHeader);
		            		
		            		// Output toEnglish of the game description
		            		myWriter.write("<h1>Game Rules:</h1>");
		            		myWriter.write("<p><pre>" + ref.context().game().toEnglish(ref.context().game()) + "\n</pre></p>");
		            		
		            		// Output strategy/heuristics for this game based on metadata (if present).
		        			final metadata.ai.Ai aiMetadata = ref.context().game().metadata().ai();
		        			if (aiMetadata != null && aiMetadata.heuristics() != null)
		        			{
		        				// Record the heuristic strings that are applicable to each player.
		        				final List<List<String>> allHeuristicStringsPerPlayer = new ArrayList<>();
		        				final Set<String> allHeuristicStrings = new HashSet<>();
		        				allHeuristicStringsPerPlayer.add(new ArrayList<>());
		        				final Heuristics heuristicValueFunction = HeuristicUtil.normaliseHeuristic(Heuristics.copy(aiMetadata.heuristics()));
		        				myWriter.write("<h1>Game Heuristics:</h1>");
		        				for (int i = 1; i <= ref.context().game().players().count(); i++)
			            		{
		        					allHeuristicStringsPerPlayer.add(new ArrayList<>());
			        				for (final HeuristicTerm heuristic : heuristicValueFunction.heuristicTerms())
			        				{
			        					heuristic.init(ref.context().game());
			        					final String heuristicEnglishString = heuristic.toEnglishString(ref.context(), i);
			        					if (heuristicEnglishString.length() > 0)
			        					{
				        					String finalHeuristicString = "<b>" + ValueUtils.splitCamelCase(heuristic.getClass().getSimpleName()) + "</b>\n";
				        					finalHeuristicString += "<i>" + heuristic.description() + "</i>\n";	
				        					finalHeuristicString += heuristicEnglishString + "\n\n";
				        					allHeuristicStringsPerPlayer.get(i).add(finalHeuristicString);
				        					allHeuristicStrings.add(finalHeuristicString);
			        					}
			        				}
			            		}
		        				
		        				// Merge heuristic strings that apply to all players
		        				for (final String heuristicString : allHeuristicStrings)
		        				{
		        					boolean validForAllPlayers = true;
		        					for (int i = 1; i <= ref.context().game().players().count(); i++)
				            		{
		        						final List<String> playerValidHeuristics = allHeuristicStringsPerPlayer.get(i);
		        						if (!playerValidHeuristics.contains(heuristicString))
		        						{
		        							validForAllPlayers = false;
		        							break;
		        						}
		        					}
		        					
		        					if (validForAllPlayers)
		        					{
		        						allHeuristicStringsPerPlayer.get(0).add(heuristicString);
		        						for (int i = 1; i <= ref.context().game().players().count(); i++)
					            		{
		        							final List<String> playerValidHeuristics = allHeuristicStringsPerPlayer.get(i);
		        							playerValidHeuristics.remove(playerValidHeuristics.indexOf(heuristicString));
					            		}
		        					}
		        				}
		        				
		        				// Write the merged heuristic strings
		        				for (int i = 0; i < allHeuristicStringsPerPlayer.size(); i++)
			            		{
		        					if (allHeuristicStringsPerPlayer.get(i).size() > 0)
		        					{
			        					if (i == 0)
			        						myWriter.write("<h2>All Players:</h2>\n");
			        					else
			        						myWriter.write("<h2>Player: " + i + "</h2>\n");
			        					
				        				myWriter.write("<p><pre>");
				        				
				        				for (final String heuristicString : allHeuristicStringsPerPlayer.get(i))
				        					myWriter.write(heuristicString);
				        				
				        				myWriter.write("</pre></p>");
		        					}
			            		}
		        			}
		  
		            		// Output board setup
		            		myWriter.write("<h1>Board Setup:</h1>");
		            		myWriter.write("<img src=\"screenshot/Game_Setup.png\" />\n<br><br>");
		 
		            		// Output ending rankings
		            		myWriter.write("<br><h1>Game Endings:</h1>");
		            		for (int i = 0; i < rankingStrings.size(); i++)
		            		{
		            			final MoveCompleteInformation moveInformation = endingMoveList.get(i);
		            			myWriter.write("<p><pre>" + rankingStrings.get(i) + "</pre></p>");
		            			myWriter.write("<img src=\"" + moveInformation.screenshotA + "\" />\n");
		            			myWriter.write("<img src=\"" + moveInformation.screenshotB + "\" />\n");
		            			myWriter.write("<img src=\"" + moveInformation.gifLocation + "\" />\n<br><br>\n");
		            		}
		            		
		            		// Output all Move images/animations
		            		myWriter.write("<br><h1>Moves:</h1>");
		            		final Set<String> allMovers = new TreeSet<>();
		            		final Set<String> allComponents = new TreeSet<>();
		            		final Set<String> allMoveActionDescriptions = new TreeSet<>();
		            		
		            		for (final MoveCompleteInformation moveInformation : condensedMoveList)
		            		{
		            			allMovers.add(String.valueOf(moveInformation.move.mover()));
		            			final String moveComponentName = ValueUtils.getComponentNameFromIndex(ref, moveInformation.what);
		            			allComponents.add(moveComponentName);
		            			allMoveActionDescriptions.add(moveInformation.move.actionDescriptionStringShort());
		            		}
		            		
		            		final String[] storedTitles = {"", "", ""};
		            		for (final String moverString : allMovers)
		            		{
		            			storedTitles[0] = "<h2>Player: " + moverString + "</h2>\n";
		            			for (final String componentString : allComponents)
			            		{
		            				storedTitles[1] = "<h3>Piece: " + componentString + "</h3>\n";
		            				for (final String actionDescriptionString : allMoveActionDescriptions)
				            		{
		            					storedTitles[2] = "<h4>Actions: " + actionDescriptionString + "</h4>\n";
		            					for (final MoveCompleteInformation moveInformation : condensedMoveList)
		    		            		{
		            						if 
		            						(
		            							String.valueOf(moveInformation.move.mover()).equals(moverString)
		            							&&
		            							ValueUtils.getComponentNameFromIndex(ref, moveInformation.what).equals(componentString)
		            							&&
		            							moveInformation.move.actionDescriptionStringShort().equals(actionDescriptionString)
		            						)
		            						{
		            							myWriter.write(String.join("", storedTitles));
		            							Arrays.fill(storedTitles, "");
		            							myWriter.write(moveInformation.move.actionDescriptionStringLong(ref.context(), true) + "\n<br>");
			    		            			myWriter.write(moveInformation.move.actions().toString() + "\n<br>");
			    		            			myWriter.write("<img src=\"" + moveInformation.screenshotA + "\" />\n");
			    		            			myWriter.write("<img src=\"" + moveInformation.screenshotB + "\" />\n");
			    		            			myWriter.write("<img src=\"" + moveInformation.gifLocation + "\" />\n<br><br>\n");
		            						}
		    		            		}
				            		}
			            		}
		            		}

		            		myWriter.write(HtmlFileOutput.htmlFooter);
		            	    myWriter.close();
		            	    
		            	    System.out.println("Process complete.");
	            		}
	            	}
	            	catch (final Exception e)
	            	{
	            		e.printStackTrace();
	            	}
	            }
	        }, 
	        delay
		);
	}
	
	//-------------------------------------------------------------------------

	/** 
	 * Takes a pair of screenshots and gif animation of the provided move. 
	 */
	protected final static void takeMoveImage(final PlayerApp app, final MoveCompleteInformation moveInformation, final boolean endingMove)
	{
		final Referee ref = app.manager().ref();
		
		// Reset the game for the new trial.
		final Trial trial = moveInformation.trial;
		final RandomProviderDefaultState trialRNG = moveInformation.rng;
		app.manager().setCurrGameStartRngState(trialRNG);
		GameUtil.resetGame(app, true);
		
		// Apply all moves up until the one we want to capture.
		for (int i = trial.numInitialPlacementMoves(); i < moveInformation.moveIndex; i++)
		{
			final Move move = trial.getMove(i);
			ref.context().game().apply(ref.context(), move);
		}
		
		// Update the GUI
		app.contextSnapshot().setContext(ref.context());
		if (endingMove)
		{
			final List<Move> endingMoveList = new ArrayList<>();
			endingMoveList.add(moveInformation.move);
			app.settingsPlayer().setTutorialVisualisationMoves(endingMoveList);
		}
		else
		{
			app.settingsPlayer().setTutorialVisualisationMoves(moveInformation.similarMoves);
		}
		app.repaint();

		// Determine the label for the gif/image. (mover-componentName-moveDescription-actionDescriptions)
		final String mover = String.valueOf(moveInformation.move.mover());
		final String moveComponentName = ValueUtils.getComponentNameFromIndex(ref, moveInformation.what);
		final String moveDescription = moveInformation.move.getDescription() + "_";
		String allActionDescriptions = "";
		for (final Action a : moveInformation.move.actions())
			allActionDescriptions += a.getDescription() + "-";
		final String imageLabel = (endingMove ? "END_" : "") + mover + "_" + moveDescription + "_" + moveComponentName + "_" + allActionDescriptions;

		// Take the before screenshot
		new java.util.Timer().schedule
		( 
	        new java.util.TimerTask() 
	        {
	            @Override
	            public void run() 
	            {
	            	System.out.println("Taking Before Screenshot");
	            	final String filePath = "screenshot/" + imageLabel + "A_" + moveInformation.toString().hashCode();
	            	ScreenCapture.gameScreenshot(rootPath + filePath);
	            	moveInformation.screenshotA = filePath + ".png";
	            	app.settingsPlayer().setTutorialVisualisationMoves(new ArrayList<>());
	            	app.repaint();
	            }
	        }, 
	        1000 
		);
		
		// Start the gif animation recording process, and apply the move.
		new java.util.Timer().schedule
		( 
	        new java.util.TimerTask() 
	        {
	            @Override
	            public void run() 
	            {	            	
	            	System.out.println("Taking Gif Animation");
	            	final String filePath = "gif/" + imageLabel + moveInformation.toString().hashCode();
	            	ScreenCapture.gameGif(rootPath + filePath);
	            	moveInformation.gifLocation = filePath + ".gif";
	    			ref.applyHumanMoveToGame(app.manager(), moveInformation.move);
	            }
	        }, 
	        2000 
		);	
		
		// Take the after screenshot
		new java.util.Timer().schedule
		( 
	        new java.util.TimerTask() 
	        {
	            @Override
	            public void run() 
	            {
	            	System.out.println("Taking After Screenshot");
	            	final String filePath = "screenshot/" + imageLabel + "B_" + moveInformation.toString().hashCode();
	            	ScreenCapture.gameScreenshot(rootPath + filePath);
	            	moveInformation.screenshotB = filePath + ".png";
	            }
	        }, 
	        4000 
		);
	}
	
}