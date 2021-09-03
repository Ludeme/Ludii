package app.tutorialVisualisation;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
import app.utils.UpdateTabMessages;
import manager.Referee;
import other.action.Action;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.move.Move;
import other.state.State;
import other.state.container.ContainerState;
import other.trial.Trial;
import util.ContainerUtil;

public class MoveVisualisation
{
	
	/** How many trials to run to provide all the moves for analysis. */
	private final static int numberTrials = 10;
	
	/** Whether or not to include moves that are from the player's hands. */
	private final static boolean includeHandMoves = false;
	
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
		generateTrials(app, ref, generatedTrials, generatedTrialsRNG);
		
		// Merge all similar moves from our generated trials into a condensed moves list.
		final List<MoveCompleteInformation> condensedMoveList = new ArrayList<>();
		final List<String> rankingStrings = new ArrayList<>();
		final List<MoveCompleteInformation> endingMoveList = new ArrayList<>(); 
		recordTrialMoves(app, ref, generatedTrials, generatedTrialsRNG, condensedMoveList, rankingStrings, endingMoveList);
		
		System.out.println("\nTotal of " + condensedMoveList.size() + " condensed moves found.");
		System.out.println("Total of " + endingMoveList.size() + " ending moves found.");
		
		generateSetupImage(app, 1000);
		generateMoveImages(app, endingMoveList, 5000);
		generateEndImages(app, endingMoveList, 15000 * (condensedMoveList.size()+1));
		generateWebsite(ref, rankingStrings, condensedMoveList, endingMoveList);
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
	
	private final static void generateTrials(final PlayerApp app, final Referee ref, final List<Trial> generatedTrials, final List<RandomProviderDefaultState> generatedTrialsRNG)
	{
		while (generatedTrials.size() < numberTrials)
		{
			System.out.print(".");
			app.restartGame();
			ref.randomPlayout(app.manager());
			generatedTrials.add(new Trial(ref.context().trial()));
			generatedTrialsRNG.add(new RandomProviderDefaultState(app.manager().currGameStartRngState().getState()));
		}
		System.out.println("\nTrials Generated.");
	}
	
	//-------------------------------------------------------------------------
	
	private final static void recordTrialMoves(final PlayerApp app, final Referee ref, final List<Trial> generatedTrials, final List<RandomProviderDefaultState> generatedTrialsRNG, final List<MoveCompleteInformation> condensedMoveList, final List<String> rankingStrings, final List<MoveCompleteInformation> endingMoveList)
	{
		for (int trialIndex = 0; trialIndex < generatedTrials.size(); trialIndex++)
		{
			System.out.print(".");
			
			// Reset the game for the new trial.
			final Trial trial = generatedTrials.get(trialIndex);
			final RandomProviderDefaultState trialRNG = generatedTrialsRNG.get(trialIndex);
			app.manager().setCurrGameStartRngState(trialRNG);
			GameUtil.resetGame(app, true);
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				// Get complete information about the selected move
				final Move move = trial.getMove(i);
				final int what = getWhatOfMove(ref.context(), move);
				final List<Move> similarMoves = similarMoves(ref.context(), move);
				final MoveCompleteInformation newMove = new MoveCompleteInformation(trial, trialRNG, move, i, what, similarMoves);
							
				// Record if the move involved hands at all.
				final boolean moveFromBoard = ContainerUtil.getContainerId(ref.context(), move.from(), move.fromType()) == 0;
				final boolean moveToBoard = ContainerUtil.getContainerId(ref.context(), move.to(), move.toType()) == 0;
				final boolean moveInvolvesHands = !moveFromBoard || !moveToBoard;
				
				// Skip moves without an associated component or which move from the hands (if desired).
				if (what != -1 && (includeHandMoves || !moveInvolvesHands))
				{
					// Determine if the move should be added to the condensed list.
					boolean addMove = true;
					for (int j = 0; j < condensedMoveList.size(); j++)
					{
						final MoveCompleteInformation priorMove = condensedMoveList.get(j);
						if (movesCanBeMerged(ref.context(), newMove, priorMove))
						{
							// Check if the new move has a larger number of possible moves, if so replace the old move.
							if (newMove.similarMoves.size() > priorMove.similarMoves.size())
								condensedMoveList.set(j, newMove);
							
							addMove = false;
							break;
						}
					}
					if (addMove)
						condensedMoveList.add(newMove);
					
					// Check if the last move should be stored.
					if (i == trial.numMoves()-1)
					{
						// Check if the last move should be stored.
						final String rankingString = UpdateTabMessages.gameOverMessage(ref.context(), trial);
						
						if (!rankingStrings.contains(rankingString))
						{
							rankingStrings.add(rankingString);
							endingMoveList.add(newMove);
						}
					}
				}
				
				// Apply the move to update the context for the next move.
				ref.context().game().apply(ref.context(), move);
			}
		}
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
		}, delay, 15000);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Take a screenshot/video of every move in the ending move list.
	 */
	private final static void generateEndImages(final PlayerApp app, final List<MoveCompleteInformation> endingMoveList, final int delay)
	{
		app.settingsPlayer().setShowEndingMove(true);
		final Timer endScreenshotTimer = new Timer();
		endScreenshotTimer.scheduleAtFixedRate(new TimerTask()
		{
			int endingMoveIndex = 0;
			
		    @Override
		    public void run()
		    {
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
		}, delay, 15000);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Once the process is complete, combine all the stored images into a complete document.
	 */
	private final static void generateWebsite(final Referee ref, final List<String> rankingStrings, final List<MoveCompleteInformation> condensedMoveList, final List<MoveCompleteInformation> endingMoveList)
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
	            		final String filePath = rootPath + "test.html";
	            		final File outputFile = new File(filePath);
	            		outputFile.getParentFile().mkdirs();
	            		outputFile.createNewFile();
	            		try(final FileWriter myWriter = new FileWriter(filePath))
	            		{
		            		myWriter.write(HtmlFileOutput.htmlHeader);
		            		
		            		// Output toEnglish of the game description
		            		myWriter.write("<h1>Game Rules:</h1>");
		            		myWriter.write(ref.context().game().toEnglish(ref.context().game()).replaceAll("\n", "<br>") + "\n<br>");
		            		
		            		// Output strategy/heuristics for this game.
		            		// TODO check with dennis.
		  
		            		// Output board setup
		            		myWriter.write("<h1>Board Setup:</h1>");
		            		myWriter.write("<img src=\"screenshot/Game_Setup.png\" />\n<br><br>");
		 
		            		// Output ending rankings
		            		myWriter.write("<br><h1>Game Endings:</h1>");
		            		for (int i = 0; i < rankingStrings.size(); i++)
		            		{
		            			final MoveCompleteInformation moveInformation = endingMoveList.get(i);
		            			myWriter.write(rankingStrings.get(i).replaceAll("\n", "<br>") + "\n<br>");
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
		            			allComponents.add(ref.context().game().equipment().components()[moveInformation.what].getNameWithoutNumber());
		            			allMoveActionDescriptions.add(moveInformation.actionDescriptionString());
		            		}
		            		
		            		final String[] storedTitles = {"","",""};
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
		            							ref.context().game().equipment().components()[moveInformation.what].getNameWithoutNumber().equals(componentString)
		            							&&
		            							moveInformation.actionDescriptionString().equals(actionDescriptionString)
		            						)
		            						{
		            							myWriter.write(String.join("", storedTitles));
		            							Arrays.fill(storedTitles, "");
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
	        15000 * condensedMoveList.size() + 15000 * (endingMoveList.size()+1)
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
		
		//System.out.println(moveInformation.similarMoves.size());
		
		// Determine the label for the gif/image. (mover-componentName-moveDescription-actionDescriptions)
		final String mover = String.valueOf(moveInformation.move.mover());
		final String moveComponentName = ref.context().equipment().components()[moveInformation.what].getNameWithoutNumber();
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
	            	final String filePath = "gif/" + imageLabel + moveInformation.toString().hashCode();
	            	ScreenCapture.gameGif(rootPath + filePath);
	            	moveInformation.gifLocation = filePath + ".gif";
	    			ref.applyHumanMoveToGame(app.manager(), moveInformation.move);
	            }
	        }, 
	        6000 
		);	
		
		// Take the after screenshot
		new java.util.Timer().schedule
		( 
	        new java.util.TimerTask() 
	        {
	            @Override
	            public void run() 
	            {
	            	final String filePath = "screenshot/" + imageLabel + "B_" + moveInformation.toString().hashCode();
	            	ScreenCapture.gameScreenshot(rootPath + filePath);
	            	moveInformation.screenshotB = filePath + ".png";
	            }
	        }, 
	        8000 
		);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Determines if two moves can be merged due to them containing the same key information.
	 */
	private final static boolean movesCanBeMerged(final Context context, final MoveCompleteInformation m1, final MoveCompleteInformation m2)
	{
		if (m1.what != m2.what)
			return false;
		
		if (m1.move.mover() != m2.move.mover())
			return false;
		
		if (!m1.move.getDescription().equals(m2.move.getDescription()))
			return false;
		
		if (m1.move.actions().size() != m2.move.actions().size())
			return false;
		
		for (int i = 0; i < m1.move.actions().size(); i++)
		{
			final String m1ActionDescription = m1.move.actions().get(i).getDescription();
			final String m2ActionDescription = m2.move.actions().get(i).getDescription();
			if (!m1ActionDescription.equals(m2ActionDescription))
				return false;
			
//			if (m1ActionDescription != null && m2ActionDescription != null && !m1ActionDescription.equals(m2ActionDescription))
//				return false;
//			else if (m1ActionDescription == null && m2ActionDescription != null || m1ActionDescription != null && m2ActionDescription == null)
//				return false;
		}
		
		// m.direction(ref.context()
		
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns a list off all similar legal moves at the point in the context where the trueMove was applied.
	 * Similar moves are those that can be merged, and are from the same location.
	 */
	private final static List<Move> similarMoves(final Context context, final Move trueMove)
	{
		final int trueMoveWhat = getWhatOfMove(context, trueMove);
		final MoveCompleteInformation trueMoveCompleteInfo = new MoveCompleteInformation(null, null, trueMove, -1, trueMoveWhat, null);
		
		final List<Move> similarMoves = new ArrayList<>();
		for (final Move move : context.moves(context).moves())
		{
			final Move moveWithConsequences = new Move(move.getMoveWithConsequences(context));
			
			final int moveWhat = getWhatOfMove(context, moveWithConsequences);
			final MoveCompleteInformation moveCompleteInfo = new MoveCompleteInformation(null, null, moveWithConsequences, -1, moveWhat, null);
			
			if (movesCanBeMerged(context, trueMoveCompleteInfo, moveCompleteInfo) && moveWithConsequences.getFromLocation().equals(trueMove.getFromLocation()))
				similarMoves.add(new Move(moveWithConsequences));
		}
		
		if (similarMoves.isEmpty())
			System.out.println("ERROR! similarMoves was empty");
		
		return similarMoves;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns the what value of a given move at the current point in the context.
	 */
	private final static int getWhatOfMove(final Context context, final Move move)
	{
		final Location moveFrom = move.getFromLocation();
		final int containerIdFrom = ContainerUtil.getContainerId(context, moveFrom.site(), moveFrom.siteType());
		
		int what = -1;
		
		if (containerIdFrom != -1)
		{
			final State state = context.state();
			final ContainerState cs = state.containerStates()[containerIdFrom];
			
			// Get the what of the component at the move's from location
			what = cs.what(moveFrom.site(), moveFrom.level(), moveFrom.siteType());
			
			// If adding a piece at the site, get the what of the first action that matches the move's from location instead.
			if (what == 0)
			{
				for (final Action a : move.actions())
				{
					final Location actionLocationA = new FullLocation(a.from(), a.levelFrom(), a.fromType());
					final Location actionLocationB = new FullLocation(a.to(), a.levelTo(), a.toType());
					final Location testingLocation = new FullLocation(moveFrom.site(), moveFrom.level(), moveFrom.siteType());
					
					if (actionLocationA.equals(testingLocation) && actionLocationB.equals(testingLocation))
					{
						what = a.what();
						break;
					}
				}
			}
		}
		
		return what;
	}
	
	//-------------------------------------------------------------------------
	
}