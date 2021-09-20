package manualGeneration;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import app.DesktopApp;
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
	private final static boolean includeHandMoves = true;
	
	/** Whether or not to include moves that have no corresponding piece. */
	private final static boolean includeNoWhatMoves = false;
	
	//-------------------------------------------------------------------------
	// Variables for coordinating various functions.
	
	static boolean setupImageTimerComplete;
	static boolean generateMoveImagesTimerComplete;
	static boolean generateEndImagesTimerComplete;
	static boolean generateWebsiteTimerComplete;
	
	//-------------------------------------------------------------------------
	
	/** Root file path for storing game specific files. */
	static String rootPath;
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Main entry point for running the instruction generation. 
	 */
	public static void instructionGeneration(final DesktopApp app)
	{
		setupImageTimerComplete = false;
		generateMoveImagesTimerComplete = false;
		generateEndImagesTimerComplete = false;
		generateWebsiteTimerComplete = false;
		
		// Check if this game is supported by instruction generation.
		if (!InstructionGenerationUtils.checkGameValid(app.manager().ref().context().game()))
		{
			System.out.println("Sorry. This game type is not supported yet.");
			InstructionGeneration.generateWebsiteTimerComplete = true;
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
		
		// Determine how wide to make the frame, based on what needs to be displayed.
		if (includeHandMoves && app.manager().ref().context().game().requiresHand())
			DesktopApp.frame().setSize(800, 465);
		else
			DesktopApp.frame().setSize(300, 465);

		// Generate all trials that will be used.
		final List<Trial> generatedTrials = new ArrayList<>();
		final List<RandomProviderDefaultState> generatedTrialsRNG = new ArrayList<>();
		MoveGeneration.generateTrials(app, generatedTrials, generatedTrialsRNG, numberTrials);
		
		// Merge all similar moves from our generated trials into a condensed moves list.
		final List<MoveCompleteInformation> condensedMoveList = new ArrayList<>();
		final List<String> rankingStrings = new ArrayList<>();
		final List<MoveCompleteInformation> endingMoveList = new ArrayList<>(); 
		MoveGeneration.recordTrialMoves(app, generatedTrials, generatedTrialsRNG, condensedMoveList, rankingStrings, endingMoveList, includeHandMoves, includeNoWhatMoves);
		
		System.out.println("\nTotal of " + condensedMoveList.size() + " condensed moves found.");
		System.out.println("Total of " + endingMoveList.size() + " ending moves found.");
		
		// Run the required processes
		generateSetupImage(app);
		generateMoveImages(app, condensedMoveList);
		generateEndImages(app, endingMoveList);
		generateWebsite(app, rankingStrings, condensedMoveList, endingMoveList);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Take a screenshot of the game before it begins.
	 */
	private final static void generateSetupImage(final DesktopApp app)
	{
		GameUtil.resetGame(app, true);

		final Timer setupScreenshotTimer = new Timer();
		setupScreenshotTimer.scheduleAtFixedRate(new TimerTask()
		{
		    @Override
		    public void run()
		    {
		    	if (ScreenCapture.screenshotComplete() && ScreenCapture.gifAnimationComplete() && !DesktopApp.view().isPainting)
		    	{
		    		ScreenCapture.resetScreenshotVariables();
		    		final String filePath = "screenshot/Game_Setup";
	            	ScreenCapture.gameScreenshot(rootPath + filePath);
			    	setupImageTimerComplete = true;
			    	setupScreenshotTimer.cancel();
			    	setupScreenshotTimer.purge();
		    	}
		    }
		}, 0, 100);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Take a screenshot/video of every move in the condensed list.
	 */
	private final static void generateMoveImages(final DesktopApp app, final List<MoveCompleteInformation> condensedMoveList)
	{
		final Timer moveScreenshotTimer = new Timer();
		moveScreenshotTimer.scheduleAtFixedRate(new TimerTask()
		{
			int condensedMoveIndex = -1;
			
		    @Override
		    public void run()
		    {
		    	if (setupImageTimerComplete && ScreenCapture.screenshotComplete() && ScreenCapture.gifAnimationComplete() && !DesktopApp.view().isPainting)
		    	{
	    			condensedMoveIndex++;
	    			
	    			if (condensedMoveIndex >= condensedMoveList.size())
			    	{
			    		System.out.println("------------------------");
			    		System.out.println("Move image generation complete.");
			    		generateMoveImagesTimerComplete = true;
			    		moveScreenshotTimer.cancel();
			    		moveScreenshotTimer.purge();
			    	}
			    	else
			    	{
			    		System.out.println("------------------------");
			    		System.out.println("Move " + (condensedMoveIndex+1) + "/" + condensedMoveList.size());
				    	final MoveCompleteInformation moveInformation = condensedMoveList.get(condensedMoveIndex);
						takeMoveImage(app, moveInformation, false);
			    	}
		    	}
		    }
		}, 0, 100);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Take a screenshot/video of every move in the ending move list.
	 */
	private final static void generateEndImages(final DesktopApp app, final List<MoveCompleteInformation> endingMoveList)
	{
		final Timer endScreenshotTimer = new Timer();
		endScreenshotTimer.scheduleAtFixedRate(new TimerTask()
		{
			int endingMoveIndex = -1;
			
		    @Override
		    public void run()
		    {	
		    	if (generateMoveImagesTimerComplete && ScreenCapture.screenshotComplete() && ScreenCapture.gifAnimationComplete() && !DesktopApp.view().isPainting)
		    	{
		    		app.settingsPlayer().setShowEndingMove(true);

	    			endingMoveIndex++;
	    			
	    			if (endingMoveIndex >= endingMoveList.size())
			    	{
			    		System.out.println("------------------------");
			    		System.out.println("Ending image generation complete.");
			    		app.settingsPlayer().setShowEndingMove(false);
			    		generateEndImagesTimerComplete = true;
			    		endScreenshotTimer.cancel();
			    		endScreenshotTimer.purge();
			    	}
			    	else
			    	{
			    		System.out.println("------------------------");
			    		System.out.println("End " + (endingMoveIndex+1) + "/" + endingMoveList.size());
				    	final MoveCompleteInformation moveInformation = endingMoveList.get(endingMoveIndex);
						takeMoveImage(app, moveInformation, true);
			    	}
		    	}
		    }
		}, 0, 100);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Once the process is complete, combine all the stored images into a complete document.
	 */
	private final static void generateWebsite(final DesktopApp app, final List<String> rankingStrings, final List<MoveCompleteInformation> condensedMoveList, final List<MoveCompleteInformation> endingMoveList)
	{
		final Referee ref = app.manager().ref();
		
		final Timer generateWebsiteTimer = new Timer();
		generateWebsiteTimer.scheduleAtFixedRate(new TimerTask()
		{
		    @Override
		    public void run()
		    {	
		    	if (generateEndImagesTimerComplete && !DesktopApp.view().isPainting && ScreenCapture.screenshotComplete() && ScreenCapture.gifAnimationComplete())
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
		            	    generateWebsiteTimerComplete = true;
		            	    
		            	    generateWebsiteTimer.cancel();
		            	    generateWebsiteTimer.purge();
	            		}
	            	}
	            	catch (final Exception e)
	            	{
	            		e.printStackTrace();
	            	}
		    	}
		    }
		}, 0, 100);
	}
	
	//-------------------------------------------------------------------------

	/** 
	 * Takes a pair of screenshots and gif animation of the provided move. 
	 */
	protected final static void takeMoveImage(final DesktopApp app, final MoveCompleteInformation moveInformation, final boolean endingMove)
	{
		ScreenCapture.resetGifAnimationVariables();
		ScreenCapture.resetScreenshotVariables();
		
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
		final String imageLabel = (endingMove ? "END_" : "") + mover + "_" + moveDescription.toString().hashCode() + "_" + moveInformation.pieceName() + "_" + allActionDescriptions.toString().hashCode();
		
		// Take the before screenshot
		final Timer beforeScreenshotTimer = new Timer();
		beforeScreenshotTimer.scheduleAtFixedRate(new TimerTask()
		{
		    @Override
		    public void run()
		    {
		    	if (!DesktopApp.view().isPainting)
		    	{
		    		ScreenCapture.resetScreenshotVariables();
	            	System.out.println("Taking Before Screenshot");
	            	final String filePath = "screenshot/" + imageLabel + "A_" + moveInformation.toString().hashCode();
	            	ScreenCapture.gameScreenshot(rootPath + filePath);
	            	moveInformation.setScreenshotA(filePath + ".png");
	            	app.settingsPlayer().setTutorialVisualisationMoves(new ArrayList<>());
	            	app.repaint();
			    	beforeScreenshotTimer.cancel();
			    	beforeScreenshotTimer.purge();
		    	}
		    }
		}, 0, 100);
		
		// Start the gif animation recording process, and apply the move.
		final Timer gifAnimationTimer = new Timer();
		gifAnimationTimer.scheduleAtFixedRate(new TimerTask()
		{
		    @Override
		    public void run()
		    {
		    	if (!DesktopApp.view().isPainting && ScreenCapture.screenshotComplete())
		    	{
		    		ScreenCapture.resetGifAnimationVariables();
	        		ScreenCapture.resetScreenshotVariables();
	            	System.out.println("Taking Gif Animation");
	            	final String filePath = "gif/" + imageLabel + moveInformation.toString().hashCode();
	            	ScreenCapture.gameGif(rootPath + filePath, 10);
	            	moveInformation.setGifLocation(filePath + ".gif");
	    			ref.applyHumanMoveToGame(app.manager(), moveInformation.move());
	            	gifAnimationTimer.cancel();
	            	gifAnimationTimer.purge();
		    	}
		    }
		}, 0, 100);

		// Take the after screenshot
		final Timer afterScreenShotTimer = new Timer();
		afterScreenShotTimer.scheduleAtFixedRate(new TimerTask()
		{
		    @Override
		    public void run()
		    {
		    	if (!DesktopApp.view().isPainting && ScreenCapture.gifAnimationComplete())
		    	{
		    		ScreenCapture.resetScreenshotVariables();
	            	System.out.println("Taking After Screenshot");
	            	final String filePath = "screenshot/" + imageLabel + "B_" + moveInformation.toString().hashCode();
	            	ScreenCapture.gameScreenshot(rootPath + filePath);
	            	moveInformation.setScreenshotB(filePath + ".png");
	            	afterScreenShotTimer.cancel();
	            	afterScreenShotTimer.purge();
		    	}
		    }
		}, 0, 100);
	}
	
	//-------------------------------------------------------------------------

	public static boolean isProcessComplete()
	{
		return generateWebsiteTimerComplete;
	}
	
	//-------------------------------------------------------------------------
	
}