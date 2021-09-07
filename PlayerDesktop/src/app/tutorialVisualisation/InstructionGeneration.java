package app.tutorialVisualisation;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import app.DesktopApp;
import app.PlayerApp;
import app.display.screenCapture.ScreenCapture;
import app.utils.AnimationVisualsType;
import app.utils.GameUtil;
import manager.Referee;
import other.action.Action;
import other.move.Move;
import other.trial.Trial;

public class InstructionGeneration
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
	 * Main entry point for running the instruction generation. 
	 */
	public static void instructionGeneration(final PlayerApp app)
	{
		// Check if this game is supported by instruction generation.
		if (!ValueUtils.checkGameValid(app.manager().ref().context().game()))
		{
			System.out.println("Sorry. This game type is not supported yet.");
			return;
		}
		
		final Referee ref = app.manager().ref();
		rootPath = "tutorialVisualisation/" + ref.context().game().name() + "/";
		
		// Set some desired visual settings (recommend resetting preferences beforehand).
		app.settingsPlayer().setPerformingTutorialVisualisation(true);
		app.settingsPlayer().setShowEndingMove(false);
		app.settingsPlayer().setShowLastMove(false);
		app.settingsPlayer().setAnimationType(AnimationVisualsType.Single);
		app.bridge().settingsVC().setShowPossibleMoves(false);
		app.bridge().settingsVC().setFlatBoard(true);
		DesktopApp.frame().setSize(300, 465);
		app.repaint();

		// Generate all trials that will be used.
		final List<Trial> generatedTrials = new ArrayList<>();
		final List<RandomProviderDefaultState> generatedTrialsRNG = new ArrayList<>();
		MoveGeneration.generateTrials(app, generatedTrials, generatedTrialsRNG, numberTrials);
		
		// Merge all similar moves from our generated trials into a condensed moves list.
		final List<MoveCompleteInformation> condensedMoveList = new ArrayList<>();
		final List<String> rankingStrings = new ArrayList<>();
		final List<MoveCompleteInformation> endingMoveList = new ArrayList<>(); 
		MoveGeneration.recordTrialMoves(app, generatedTrials, generatedTrialsRNG, condensedMoveList, rankingStrings, endingMoveList, includeHandMoves);
		
		System.out.println("\nTotal of " + condensedMoveList.size() + " condensed moves found.");
		System.out.println("Total of " + endingMoveList.size() + " ending moves found.");
		
		// Set delays for each process
		final int delay1 = 1000;
		final int delay2 = 3000;
		final int delay3 = 5000 * (condensedMoveList.size()+1);
		final int delay4 = 5000 * (condensedMoveList.size()+1) + 5000 * (endingMoveList.size()+1);
		
		// Run the required processes
//		generateSetupImage(app, delay1);
//		generateMoveImages(app, condensedMoveList, delay2);
//		generateEndImages(app, endingMoveList, delay3);
//		generateWebsite(ref, rankingStrings, condensedMoveList, endingMoveList, delay4);
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
		            		myWriter.write(HtmlFileOutput.htmlEnglishRules(ref.context().game()));
		            		
		            		// Output strategy/heuristics for this game based on metadata (if present).
		            		myWriter.write(HtmlFileOutput.htmlEnglishHeuristics(ref.context()));
		  
		            		// Output board setup
		            		myWriter.write(HtmlFileOutput.htmlBoardSetup());
		 
		            		// Output endings
		            		myWriter.write(HtmlFileOutput.htmlEndings(rankingStrings, endingMoveList));
		            		
		            		// Output all Move images/animations
		            		myWriter.write(HtmlFileOutput.htmlMoves(ref, condensedMoveList));

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
		final Trial trial = moveInformation.trial();
		final RandomProviderDefaultState trialRNG = moveInformation.rng();
		app.manager().setCurrGameStartRngState(trialRNG);
		GameUtil.resetGame(app, true);
		
		// Apply all moves up until the one we want to capture.
		for (int i = trial.numInitialPlacementMoves(); i < moveInformation.moveIndex(); i++)
		{
			final Move move = trial.getMove(i);
			ref.context().game().apply(ref.context(), move);
		}
		
		// Update the GUI
		app.contextSnapshot().setContext(ref.context());
		if (endingMove)
		{
			final List<Move> endingMoveList = new ArrayList<>();
			endingMoveList.add(moveInformation.move());
			app.settingsPlayer().setTutorialVisualisationMoves(endingMoveList);
		}
		else
		{
			app.settingsPlayer().setTutorialVisualisationMoves(moveInformation.similarMoves());
		}
		app.repaint();

		// Determine the label for the gif/image. (mover-componentName-moveDescription-actionDescriptions)
		final String mover = String.valueOf(moveInformation.move().mover());
		final String moveDescription = moveInformation.move().getDescription() + "_";
		String allActionDescriptions = "";
		for (final Action a : moveInformation.move().actions())
			allActionDescriptions += a.getDescription() + "-";
		final String imageLabel = (endingMove ? "END_" : "") + mover + "_" + moveDescription + "_" + moveInformation.pieceName() + "_" + allActionDescriptions;

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
	            	moveInformation.setScreenshotA(filePath + ".png");
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
	            	moveInformation.setGifLocation(filePath + ".gif");
	    			ref.applyHumanMoveToGame(app.manager(), moveInformation.move());
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
	            	moveInformation.setScreenshotB(filePath + ".png");
	            }
	        }, 
	        4000 
		);
	}
	
}