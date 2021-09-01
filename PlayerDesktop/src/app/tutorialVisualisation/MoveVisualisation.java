package app.tutorialVisualisation;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import app.PlayerApp;
import app.display.util.DesktopGUIUtil;
import app.utils.GameUtil;
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
	
	private final static int numberTrials = 1;
	
	//-------------------------------------------------------------------------
	
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
	
	public static void moveVisualisation(final PlayerApp app)
	{
		// Turn on some settings
		app.settingsPlayer().setShowLastMove(true);
		app.settingsPlayer().setPerformingTutorialVisualisation(true);
		
		final Referee ref = app.manager().ref();
		
		final List<Trial> generatedTrials = new ArrayList<>();
		final List<RandomProviderDefaultState> generatedTrialsRNG = new ArrayList<>();
		
		final List<MoveCompleteInformation> condensedMoveList = new ArrayList<>();
		
		while (generatedTrials.size() < numberTrials)
		{
			app.restartGame();
			ref.randomPlayout(app.manager());
			generatedTrials.add(new Trial(ref.context().trial()));
			generatedTrialsRNG.add(new RandomProviderDefaultState(app.manager().currGameStartRngState().getState()));
		}
		
		for (int trialIndex = 0; trialIndex < generatedTrials.size(); trialIndex++)
		{
			final Trial trial = generatedTrials.get(trialIndex);
			final RandomProviderDefaultState trialRNG = generatedTrialsRNG.get(trialIndex);
			app.manager().setCurrGameStartRngState(trialRNG);
			GameUtil.resetGame(app, true);
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				final Move move = trial.getMove(i);
				
				final int what = getWhatOfMove(ref.context(), move);
				final MoveCompleteInformation newMove = new MoveCompleteInformation(trial, trialRNG, i, what);
				
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
				
				ref.context().game().apply(ref.context(), move);
			}
		}
		
		final Timer screenshotTimer = new Timer();
		screenshotTimer.scheduleAtFixedRate(new TimerTask()
		{
			int index = 0;
			
		    @Override
		    public void run()
		    {
		    	if (index >= condensedMoveList.size())
		    	{
		    		System.out.println("Screenshots complete.");
		    		screenshotTimer.cancel();
		    		screenshotTimer.purge();
		    	}
		    	else
		    	{
			    	final MoveCompleteInformation moveInformation = condensedMoveList.get(index);
			    	
			    	System.out.println("-----------------");
					System.out.println(moveInformation);
					
			    	//takeMoveScreenshot(app, moveInformation);
					takeMoveImage(app, moveInformation, true);
			    	
			    	index++;
		    	}
		    }
		}, 0, 5000);
		
//		for (int i = trial.numInitialPlacementMoves(); i < completeMoveList.size(); i++)
//		{
//			final Move m = completeMoveList.get(i);
//			System.out.println(m.actionType());
//			System.out.println(m.actions());
//			System.out.println(m.direction(ref.context()));
//			System.out.println(m.what());
//		}
	}
	
	//-------------------------------------------------------------------------

	/** 
	 * Takes a screenshot/gif of the app directly after the move is made. 
	 */
	protected final static void takeMoveImage(final PlayerApp app, final MoveCompleteInformation moveInformation, final boolean gif)
	{
		final Referee ref = app.manager().ref();
		
		final Trial trial = moveInformation.trial;
		final RandomProviderDefaultState trialRNG = moveInformation.rng;
		app.manager().setCurrGameStartRngState(trialRNG);
		GameUtil.resetGame(app, true);
		
		for (int i = trial.numInitialPlacementMoves(); i < moveInformation.moveIndex; i++)
		{
			final Move move = trial.getMove(i);
			ref.context().game().apply(ref.context(), move);
		}
		
		app.contextSnapshot().setContext(ref.context());
		app.repaint();
		
		if (gif)
		{
			new java.util.Timer().schedule
			( 
		        new java.util.TimerTask() 
		        {
		            @Override
		            public void run() 
		            {
		            	DesktopGUIUtil.gameGif(moveInformation.toString());
		    			ref.applyHumanMoveToGame(app.manager(), trial.getMove(moveInformation.moveIndex));
		            }
		        }, 
		        1000 
			);	
		}
		else
		{
			ref.applyHumanMoveToGame(app.manager(), trial.getMove(moveInformation.moveIndex));
			new java.util.Timer().schedule
			( 
		        new java.util.TimerTask() 
		        {
		            @Override
		            public void run() 
		            {
		            	DesktopGUIUtil.gameScreenshot("tutorialVisualisation/" + moveInformation.toString());
		            }
		        }, 
		        1000 
			);
		}
	}
	
	//-------------------------------------------------------------------------
	
	private final static boolean movesCanBeMerged(final Context context, final MoveCompleteInformation m1, final MoveCompleteInformation m2)
	{
		if (m1.what != m2.what)
			return false;
		
		if (m1.move().mover() != m2.move().mover())
			return false;
		
		if (m1.move().actions().size() != m2.move().actions().size())
			return false;
		
		for (int i = 0; i < m1.move().actions().size(); i++)
			if (!m1.move().actions().get(i).actionType().equals(m2.move().actions().get(i).actionType()))
				return false;
		
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	private final static int getWhatOfMove(final Context context, final Move move)
	{
		final Location moveFrom = move.getFromLocation();
		final int containerIdFrom = ContainerUtil.getContainerId(context, moveFrom.site(), moveFrom.siteType());
		
		final State state = context.state();
		final ContainerState cs = state.containerStates()[containerIdFrom];
		
		// get the what of the component at the selected location
		int what = cs.what(moveFrom.site(), moveFrom.level(), moveFrom.siteType());
		
		// If adding a piece at the site, get the what of the move (first action that matches selected location) instead.
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