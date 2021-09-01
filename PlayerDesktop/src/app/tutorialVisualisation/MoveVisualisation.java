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
	private final static int numberTrials = 1;
	
	/** If gif animations should be saved rather than static screenshots. */
	private final static boolean gifAnimations = true;
	
	//-------------------------------------------------------------------------
	
	/***
	 * All necessary information to recreate a specific move from a specific trial.
	 * 
	 * @author Matthew.Stephenson
	 */
	public static class MoveCompleteInformation
	{
		Trial trial;
		RandomProviderDefaultState rng;
		int moveIndex;
		int what;
		
		MoveCompleteInformation(final Trial trial, final RandomProviderDefaultState rng, final int moveIndex, final int what)
		{
			this.trial = new Trial(trial);
			this.rng = new RandomProviderDefaultState(rng.getState());
			this.moveIndex = moveIndex;
			this.what = what;
		}
		
		Move move()
		{
			return trial.getMove(moveIndex);
		}
		
		@Override
		public String toString()
		{
			return move().toString() + "_what_" + what + "_mover_" + move().mover();
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
				final MoveCompleteInformation newMove = new MoveCompleteInformation(trial, trialRNG, i, what);
				
				// Determine if the move should be added to the condensed list.
				boolean addMove = true;
				for (final MoveCompleteInformation priorMove : condensedMoveList)
				{
					if (movesCanBeMerged(ref.context(), newMove, priorMove))
					{
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
		
		System.out.println("Total of " + condensedMoveList.size() + " condensed moves found.");
		
		// TODO
		// Check the trials for the state where the most of a specific condensed move are legal.
		
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
		    		System.out.println("Process complete.");
		    		screenshotTimer.cancel();
		    		screenshotTimer.purge();
		    	}
		    	else
		    	{
			    	final MoveCompleteInformation moveInformation = condensedMoveList.get(condensedMoveIndex);
					takeMoveImage(app, moveInformation);
					condensedMoveIndex++;
		    	}
		    }
		}, 0, 5000);
	}
	
	//-------------------------------------------------------------------------

	/** 
	 * Takes a screenshot/gif of the app directly after the move is made. 
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
		app.repaint();
		
		if (gifAnimations)	// Record a short gif animation of the move.				&& MoveAnimation.getMoveAnimationType(app, trial.getMove(moveInformation.moveIndex)) != AnimationType.NONE
		{
			new java.util.Timer().schedule
			( 
		        new java.util.TimerTask() 
		        {
		            @Override
		            public void run() 
		            {
		            	ScreenCapture.gameGif("tutorialVisualisation/gif/" + moveInformation.toString());
		    			ref.applyHumanMoveToGame(app.manager(), trial.getMove(moveInformation.moveIndex));
		            }
		        }, 
		        1000 
			);	
		}
		else	// Take a screenshot before and after.
		{
			new java.util.Timer().schedule
			( 
		        new java.util.TimerTask() 
		        {
		            @Override
		            public void run() 
		            {
		            	ScreenCapture.gameScreenshot("tutorialVisualisation/screenshot/BEFORE_" + moveInformation.toString());
		            }
		        }, 
		        1000 
			);

			new java.util.Timer().schedule
			( 
		        new java.util.TimerTask() 
		        {
		            @Override
		            public void run() 
		            {
		            	ref.applyHumanMoveToGame(app.manager(), trial.getMove(moveInformation.moveIndex));
		            }
		        }, 
		        2000 
			);
			
			new java.util.Timer().schedule
			( 
		        new java.util.TimerTask() 
		        {
		            @Override
		            public void run() 
		            {
		            	ScreenCapture.gameScreenshot("tutorialVisualisation/screenshot/AFTER_" + moveInformation.toString());
		            }
		        }, 
		        3000 
			);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Determines if two moves can be merged due to them containing the same key information.
	 */
	private final static boolean movesCanBeMerged(final Context context, final MoveCompleteInformation m1, final MoveCompleteInformation m2)
	{
		if (m1.what != m2.what)
			return false;
		
		if (m1.move().mover() != m2.move().mover())
			return false;
		
		if (m1.move().actions().size() != m2.move().actions().size())
			return false;
		
		for (int i = 0; i < m1.move().actions().size(); i++)
		{
			final ActionType m1ActionType = m1.move().actions().get(i).actionType();
			final ActionType m2ActionType = m2.move().actions().get(i).actionType();
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
	 * Returns the what value of a given move at the current point in the context.
	 */
	private final static int getWhatOfMove(final Context context, final Move move)
	{
		final Location moveFrom = move.getFromLocation();
		final int containerIdFrom = ContainerUtil.getContainerId(context, moveFrom.site(), moveFrom.siteType());
		
		final State state = context.state();
		final ContainerState cs = state.containerStates()[containerIdFrom];
		
		// Get the what of the component at the move's from location
		int what = cs.what(moveFrom.site(), moveFrom.level(), moveFrom.siteType());
		
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
		
		return what;
	}
	
	//-------------------------------------------------------------------------
	
}