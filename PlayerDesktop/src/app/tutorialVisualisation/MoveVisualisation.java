package app.tutorialVisualisation;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import app.DesktopApp;
import app.PlayerApp;
import app.display.screenCapture.ScreenCapture;
import app.utils.GameUtil;
import manager.Referee;
import other.action.Action;
import other.action.ActionType;
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
	
	//-------------------------------------------------------------------------
	
	/***
	 * All necessary information to recreate (and compare) a specific move from a specific trial.
	 * 
	 * @author Matthew.Stephenson
	 */
	public static class MoveCompleteInformation
	{
		final Trial trial;
		final RandomProviderDefaultState rng;
		final Move move;
		final int moveIndex;
		final int what;
		final List<Move> similarMoves;
		
		MoveCompleteInformation(final Trial trial, final RandomProviderDefaultState rng, final Move move, final int moveIndex, final int what, final List<Move> similarMoves)
		{
			this.trial = trial == null ? null : new Trial(trial);
			this.rng = rng == null ? null : new RandomProviderDefaultState(rng.getState());
			this.move = move == null ? null : new Move(move);
			this.moveIndex = moveIndex;
			this.what = what;
			this.similarMoves = similarMoves;
		}
		
		@Override
		public String toString()
		{
			return move.toString() + "_what_" + what + "_mover_" + move.mover();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Main entry point for running the move visualisation. 
	 */
	public static void moveVisualisation(final PlayerApp app)
	{
		app.settingsPlayer().setPerformingTutorialVisualisation(true);
		DesktopApp.frame().setSize(300, 465);
		app.repaint();
		
		final Referee ref = app.manager().ref();

		// Generate all trials that will be used.
		final List<Trial> generatedTrials = new ArrayList<>();
		final List<RandomProviderDefaultState> generatedTrialsRNG = new ArrayList<>();
		while (generatedTrials.size() < numberTrials)
		{
			System.out.print(".");
			app.restartGame();
			ref.randomPlayout(app.manager());
			generatedTrials.add(new Trial(ref.context().trial()));
			generatedTrialsRNG.add(new RandomProviderDefaultState(app.manager().currGameStartRngState().getState()));
		}
		
		// Merge all similar moves from our generated trials into a condensed moves list.
		final List<MoveCompleteInformation> condensedMoveList = new ArrayList<>();
		for (int trialIndex = 0; trialIndex < generatedTrials.size(); trialIndex++)
		{
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
				
				// Apply the move to update the context for the next move.
				ref.context().game().apply(ref.context(), move);
			}
		}
		
		System.out.println("\nTotal of " + condensedMoveList.size() + " condensed moves found.");
		
		// Take a screenshot/video of every move in the condensed list.
		final Timer screenshotTimer = new Timer();
		screenshotTimer.scheduleAtFixedRate(new TimerTask()
		{
			int condensedMoveIndex = 0;
			
		    @Override
		    public void run()
		    {
		    	if (condensedMoveIndex >= condensedMoveList.size())
		    	{
		    		System.out.println("------------------------");
		    		System.out.println("Process complete.");
		    		screenshotTimer.cancel();
		    		screenshotTimer.purge();
		    	}
		    	else
		    	{
		    		System.out.println("------------------------");
		    		System.out.println("Move " + (condensedMoveIndex+1) + "/" + condensedMoveList.size());
			    	final MoveCompleteInformation moveInformation = condensedMoveList.get(condensedMoveIndex);
					takeMoveImage(app, moveInformation);
					condensedMoveIndex++;
		    	}
		    }
		}, 0, 5000);
		
//		// Check the trials for the state where the most of a specific condensed move are legal.
//		new java.util.Timer().schedule
//		( 
//	        new java.util.TimerTask() 
//	        {
//	            @Override
//	            public void run() 
//	            {
//	            	final Timer possibleMovesTimer = new Timer();
//	            	possibleMovesTimer.scheduleAtFixedRate(new TimerTask()
//	        		{
//	        			int condensedMoveIndex = 0;
//	        			
//	        		    @Override
//	        		    public void run()
//	        		    {
//	        		    	if (condensedMoveIndex >= condensedMoveList.size())
//	        		    	{
//	        		    		System.out.println("------------------------");
//	        		    		System.out.println("Process complete.");
//	        		    		possibleMovesTimer.cancel();
//	        		    		possibleMovesTimer.purge();
//	        		    	}
//	        		    	else
//	        		    	{
//	        		    		System.out.println("------------------------");
//	        		    		System.out.println("Move " + (condensedMoveIndex+1) + "/" + condensedMoveList.size());
//	        			    	final MoveCompleteInformation moveInformation = condensedMoveList.get(condensedMoveIndex);
//	        					takeMoveImage(app, moveInformation);
//	        					condensedMoveIndex++;
//	        		    	}
//	        		    }
//	        		}, 0, 5000);
//	            }
//	        }, 
//	        5000 * (condensedMoveList.size()+1)
//		);
		
		
	}
	
	//-------------------------------------------------------------------------

	/** 
	 * Takes a pair of screenshots and gif animation of the provided move. 
	 */
	protected final static void takeMoveImage(final PlayerApp app, final MoveCompleteInformation moveInformation)
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
		app.settingsPlayer().setTutorialVisualisationMoves(moveInformation.similarMoves);
		app.repaint();
		
		//System.out.println(moveInformation.similarMoves.size());
		
		// Determine the label for the gif/image.
		final String mover = String.valueOf(moveInformation.move.mover());
		final String moveComponentName = ref.context().equipment().components()[moveInformation.what].getNameWithoutNumber();
		//final String moveType = moveInformation.move().actionType().name() + "_";
		String allActionTypes = "";
		for (final Action a : moveInformation.move.actions())
			allActionTypes += a.actionType() + "_";
		final String imageLabel = mover + "_" + moveComponentName + "_" + allActionTypes ;

		// Take the before screenshot
		new java.util.Timer().schedule
		( 
	        new java.util.TimerTask() 
	        {
	            @Override
	            public void run() 
	            {
	            	ScreenCapture.gameScreenshot("tutorialVisualisation/screenshot/" + imageLabel + "A_" + moveInformation.toString());
	            	app.settingsPlayer().tutorialVisualisationMoves().clear();
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
	            	ScreenCapture.gameGif("tutorialVisualisation/gif/" + imageLabel + moveInformation.toString());
	    			ref.applyHumanMoveToGame(app.manager(), moveInformation.move);
	            }
	        }, 
	        3000 
		);	
		
		// Take the after screenshot
		new java.util.Timer().schedule
		( 
	        new java.util.TimerTask() 
	        {
	            @Override
	            public void run() 
	            {
	            	ScreenCapture.gameScreenshot("tutorialVisualisation/screenshot/" + imageLabel + "B_" + moveInformation.toString());
	            }
	        }, 
	        4000 
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
		
		if (!m1.move.actionType().equals(m2.move.actionType()))
			return false;
		
		if (m1.move.actions().size() != m2.move.actions().size())
			return false;
		
		for (int i = 0; i < m1.move.actions().size(); i++)
		{
			final ActionType m1ActionType = m1.move.actions().get(i).actionType();
			final ActionType m2ActionType = m2.move.actions().get(i).actionType();
			if (m1ActionType != null && m2ActionType != null && !m1ActionType.equals(m2ActionType))
				return false;
			else if (m1ActionType == null && m2ActionType != null || m1ActionType != null && m2ActionType == null)
				return false;
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