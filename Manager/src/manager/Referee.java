package manager;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;

import game.Game;
import game.rules.play.moves.Moves;
import game.types.play.ModeType;
import main.Constants;
import manager.ai.AIDetails;
import manager.ai.AIUtil;
import other.AI;
import other.context.Context;
import other.model.Model;
import other.model.Model.AgentMoveCallback;
import other.model.Model.MoveMessageCallback;
import other.move.Move;
import other.trial.Trial;
import utils.AIUtils;
import utils.DoNothingAI;

/**
 * The Referee class coordinates all aspects of game play including self-play
 * tournaments.
 *
 * @author cambolbro and Eric.Piette and Matthew.Stephenson
 */
public class Referee
{
	//---------------------------------------------------------
	
	/** The game instance. */
	private Context context;

	/** True if human input is allowed to cause a new step to start */
	private final AtomicBoolean allowHumanBasedStepStart = new AtomicBoolean(true);
	
	/** Set to true if we want a new nextMove(false) call */
	private final AtomicBoolean wantNextMoveCall = new AtomicBoolean(false);

	/** Update visualisation of what AI is thinking every x milliseconds */
	public static final int AI_VIS_UPDATE_TIME = 40;
	
	/** Thread for checking model to make moves. */
	private RefereeStepThread moveThread = null;

	//-------------------------------------------------------------------------

	/**
	 * @return Current context
	 */
	public Context context()
	{
		return context;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param game
	 * @return The current game object.
	 */
	public Referee setGame(final Manager manager, final Game game)
	{
		context = new Context(game, new Trial(game));
		manager.updateCurrentGameRngInternalState();
		return this;
	}

	//-------------------------------------------------------------------------

	/**
	 * Apply a saved move to the game. Used only when viewing prior states. No
	 * validity checks.
	 */
	public void makeSavedMoves(final Manager manager, final List<Move> moves)
	{
		Move move = null;
		
		for (int i = context.trial().numMoves(); i < moves.size(); i++)
		{
			move = moves.get(i);
			preMoveApplication(manager, move);
			context.game().apply(context, move);
		}

		if (move != null)
			postMoveApplication(manager, move, true);
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Apply human move to game.
	 */
	public synchronized void applyHumanMoveToGame(final Manager manager, final Move move)
	{
		final Model model = context.model();
		
		if (model.isReady())
			if (!nextMove(manager, true))
				return;

		final boolean autoPass = move.isPass() && context.game().moves(context).moves().isEmpty();
		if (!autoPass)
		{
			while (model.isReady() || !model.isRunning())
			{
				try
				{
					Thread.sleep(10L);
				}
				catch (final InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
			
		// Wrap the code that we want to run in a Runnable.
		final Runnable runnable = () -> 
		{
			preMoveApplication(manager, move);
			final Move appliedMove = model.applyHumanMove(context(), move, move.mover());
			
			if (model.movesPerPlayer() != null)
			{
				// We might be waiting for the moves of other players.
				final ArrayList<Integer> playerIdsWaitingFor = new ArrayList<>();
				
				for (int i = 1; i < model.movesPerPlayer().length; i++)
				{
					final Move m = model.movesPerPlayer()[i];
					if (m == null)
						playerIdsWaitingFor.add(Integer.valueOf(i));
				}
				
				if (playerIdsWaitingFor.size() > 0)
				{
					String tempMessage = "Waiting for moves from";
					for (final int index : playerIdsWaitingFor)
						tempMessage += " P" + index + " and";
					tempMessage = tempMessage.substring(0, tempMessage.length()-4);
					tempMessage += ".\n";
					manager.getPlayerInterface().addTextToStatusPanel(tempMessage);
				}
			}

			if (appliedMove != null)
				postMoveApplication(manager, appliedMove, false);
		};
		
		runnable.run();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Apply remote move that we received from the network.
	 */
	public synchronized boolean applyNetworkMoveToGame(final Manager manager, final Move move)
	{
		// Check that the model is ready to apply this move.
		final Model model = context.model();
		if (model.isReady() && !nextMove(manager, true))
		{
			System.out.println("Waiting on the model: " + move);
			return false;
		}
		
		// Get the real move from the set of legal moves that matches the fetched move information.
		final Move realMoveToApply = context.game().getMatchingLegalMove(context, move);
		
		// If the move was not valid, tell the user and try again
		if (realMoveToApply == null)
		{
			manager.getPlayerInterface().addTextToStatusPanel("received move was not legal: " + move + "\n");
			manager.getPlayerInterface().addTextToStatusPanel("currentTrialLength: " + context.trial().moveNumber() + "\n");
			return false;
		}

		// If the move was valid, try and apply it.
		applyHumanMoveToGame(manager, realMoveToApply);
		
		return true;
	}

	//-------------------------------------------------------------------------

	/**
	 * Plays a random move in the current position.
	 */
	public void randomMove(final Manager manager)
	{		
		final Moves legal = context.game().moves(context);
		
		if (legal.moves().size() > 0)
		{
			final int moveIndex = ThreadLocalRandom.current().nextInt(0, legal.moves().size());
	        final Move randomMove = legal.get(moveIndex);
	        applyHumanMoveToGame(manager, randomMove);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Time random playouts.
	 *
	 * @return Average number of playouts per second.
	 */
	public double timeRandomPlayouts()
	{
		// Use a copy of our context for all the playouts
		final Context timingContext = new Context(context);
		final Game game = timingContext.game();

		// Warming
		long stopAt = 0;
		long start = System.nanoTime();
		double abortAt = start + 10 * 1000000000.0;

		while (stopAt < abortAt)
		{
			game.start(timingContext);
			game.playout(timingContext, null, 1.0, null, 0, -1, ThreadLocalRandom.current());
			stopAt = System.nanoTime();
		}

		stopAt = 0;
		System.gc();
		start = System.nanoTime();
		abortAt = start + 30 * 1000000000.0;
		int playouts = 0;
		int moveDone = 0;
		while (stopAt < abortAt)
		{
			game.start(timingContext);
			game.playout(timingContext, null, 1.0, null, 0, -1, ThreadLocalRandom.current());
			stopAt = System.nanoTime();
			moveDone += timingContext.trial().numMoves();
			playouts++;
		}

		final double secs = (stopAt - start) / 1000000000.0;
		final double rate = (playouts / secs);
		final double rateMove = (moveDone / secs);

		System.out.println(String.format(Locale.US, "%.2f", Double.valueOf(rate)) + "p/s");
		System.out.println(String.format(Locale.US, "%.2f", Double.valueOf(rateMove)) + "m/s");

		return rate;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Perform a random playout.
	 */
	public void randomPlayout(final Manager manager)
	{
		interruptAI(manager);
		
		final Game gameToPlayout = context.game();
		gameToPlayout.playout(context, null, 1.0, null, 0, -1, ThreadLocalRandom.current());
		
		EventQueue.invokeLater(() -> 
		{
			manager.getPlayerInterface().postMoveUpdates(context.trial().lastMove(), true);
		});
	}
	
	/**
	 * Perform a random playout only for the current instance within a Match
	 */
	public void randomPlayoutSingleInstance(final Manager manager)
	{
		final Context instanceContext = context.currentInstanceContext();
		final Trial instanceTrial = instanceContext.trial();
		
		if (!instanceTrial.over())
		{
			interruptAI(manager);

			final Trial startInstanceTrial = context.currentInstanceContext().trial();
			int currentMovesMade = startInstanceTrial.numMoves();
			
//			if (manager.savedTrial() != null)
//			{
//				final List<Move> tempActions = context.trial().generateCompleteMovesList();
//				manager.getPlayerInterface().restartGame();
//				makeSavedMoves(manager, tempActions);
//			}

			final Game gameToPlayout = instanceContext.game();
			gameToPlayout.playout(instanceContext, null, 1.0, null, 0, -1, ThreadLocalRandom.current());
			
			// Will likely have to append some extra moves to the match-wide trial
			final List<Move> subtrialMoves = instanceContext.trial().generateCompleteMovesList();
			final int numMovesAfterPlayout = subtrialMoves.size();
			final int numMovesToAppend = numMovesAfterPlayout - currentMovesMade;
			
			for (int i = 0; i < numMovesToAppend; ++i)
				context.trial().addMove(subtrialMoves.get(subtrialMoves.size() - numMovesToAppend + i));
			
			// If the instance we over, we have to advance here in this Match
			if (instanceTrial.over())
			{
				final Moves legalMatchMoves = context.game().moves(context);
				assert (legalMatchMoves.moves().size() == 1);
				assert (legalMatchMoves.moves().get(0).containsNextInstance());
				context.game().apply(context, legalMatchMoves.moves().get(0));
			}

			// We only want to print moves in moves tab from the last trial
			if (context().currentInstanceContext().trial() != startInstanceTrial)
				currentMovesMade = context().currentInstanceContext().trial().numInitialPlacementMoves();
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Triggers a move from the next player. If the next player is human then
	 * control drops through to wait for input.
	 *
	 * @param humanBasedStepStart True if the caller responds to human input
	 * @return True if we started a new step in the model
	 */
	public synchronized boolean nextMove(final Manager manager, final boolean humanBasedStepStart)
	{
		wantNextMoveCall.set(false);

		if (!allowHumanBasedStepStart.get() && humanBasedStepStart)
			return false;

		try
		{
			if (!context().trial().over())
			{
				final Model model = context.model();

				// In case of a simulation.
				if (context.game().mode().mode().equals(ModeType.Simulation))
				{
					final List<AI> list = new ArrayList<AI>();
					list.add(new DoNothingAI());
					model.unpauseAgents
					(
						context, 
						list, 
						new double[]{ manager.settingsManager().tickLength() }, 
						Constants.UNDEFINED, 
						Constants.UNDEFINED, 
						0.0, 
						null, 
						null, 
						true,
						new MoveMessageCallback()
						{
							@Override
							public void call(final String message)
							{
								manager.getPlayerInterface().addTextToStatusPanel(message);
							}
						}
					);
					postMoveApplication(manager, context.trial().lastMove(), false);
				}

				if (!model.isReady() && model.isRunning() && !manager.settingsManager().agentsPaused())
				{
					final double[] thinkTime = AIDetails.convertToThinkTimeArray(manager.aiSelected());
					List<AI> agents = null;
					
					if (!manager.settingsManager().agentsPaused())
						agents = AIDetails.convertToAIList(manager.aiSelected());
					if (agents != null)
						AIUtil.checkAISupported(manager, context);
					
					model.unpauseAgents
					(
						context,
						agents,
						thinkTime,
						-1, -1,
						0.4,
						new AgentMoveCallback()
						{
							@Override
							public long call(final Move move)
							{
								preMoveApplication(manager, move);
								return 0L;
							}
						},
						new AgentMoveCallback()
						{

							@Override
							public long call(final Move move)
							{
								postMoveApplication(manager, move, false);
								return -1L;
							}
						},
						true,
						new MoveMessageCallback()
						{
							@Override
							public void call(final String message)
							{
								manager.getPlayerInterface().addTextToStatusPanel(message);
							}
						}
					);
				}
				else
				{
					allowHumanBasedStepStart.set(model.expectsHumanInput());
					
					if (moveThread != null && moveThread.isAlive())
						moveThread.runnable.shouldTerminate = true;
					
					moveThread = new RefereeStepThread(new RefereeStepRunnable()
					{
						@Override
						public void run()
						{
							final double[] thinkTime = AIDetails.convertToThinkTimeArray(manager.aiSelected());
	
							List<AI> agents = null;
							if (!manager.settingsManager().agentsPaused())
							{
								agents = AIDetails.convertToAIList(manager.aiSelected());
							}
	
							// make sure any AIs are initialised
							if (agents != null)
							{
								for (int p = 1; p <= context.game().players().count(); ++p)
								{
									if (agents.get(p) == null)
										continue;
	
									if (!agents.get(p).supportsGame(context.game()))
									{
	
										final AI oldAI = manager.aiSelected()[p].ai();
										final AI newAI = AIUtils.defaultAiForGame(context.game());
	
										final JSONObject json = new JSONObject()
													.put("AI", new JSONObject()
													.put("algorithm", newAI.friendlyName())
												);
	
										manager.aiSelected()[p] = new AIDetails(manager, json, p, "Ludii AI");
	
										EventQueue.invokeLater(() -> 
										{
											manager.getPlayerInterface().addTextToStatusPanel
											(
												oldAI.friendlyName() + " does not support this game. Switching to default AI for this game: " 
												+ 
												newAI.friendlyName() + ".\n"
											);
										});
									}
	
									agents.get(p).initIfNeeded(context.game(), p);
								}
							}
							
							final Trial startInstanceTrial = context.currentInstanceContext().trial();
	
							model.startNewStep
							(
								context,
								agents,
								thinkTime,
								-1, -1,
								manager.settingsManager().minimumAgentThinkTime(),
								false, 		// don't block
								true, 		// force use of threads
								false,		// don't force use of no threads
								new AgentMoveCallback()
								{
									@Override
									public long call(final Move move)
									{
										preMoveApplication(manager, move);
										return 0L;
									}
								},
								new AgentMoveCallback()
								{
	
									@Override
									public long call(final Move move)
									{
										postMoveApplication(manager, move, false);
										return -1L;
									}
								},
								true,
								new MoveMessageCallback()
								{
									@Override
									public void call(final String message)
									{
										manager.getPlayerInterface().addTextToStatusPanel(message);
									}
								}
							);
	
							while (!model.isReady() && !shouldTerminate)
							{
								manager.setLiveAIs(model.getLiveAIs());
								allowHumanBasedStepStart.set(model.expectsHumanInput());
								try
								{
									final List<AI> liveAIs = manager.liveAIs();
									if (liveAIs != null && !liveAIs.isEmpty())
									{
										EventQueue.invokeAndWait(() ->
										{
											manager.getPlayerInterface().repaint();
										});
									}
	
									Thread.sleep(AI_VIS_UPDATE_TIME);
								}
								catch (final InterruptedException | InvocationTargetException e)
								{
									e.printStackTrace();
								}
							}
							
							if (shouldTerminate)
								return;
	
							EventQueue.invokeLater(() -> 
							{
								manager.getPlayerInterface().repaint();
							});
							
							allowHumanBasedStepStart.set(false);
							manager.setLiveAIs(null);
							
							// If we transitioned to new instance, we need to pause
							if (startInstanceTrial != context.currentInstanceContext().trial())
								manager.settingsManager().setAgentsPaused(manager, true);
	
							if (!manager.settingsManager().agentsPaused())
							{
								final List<AI> ais = model.getLastStepAIs();
								
								EventQueue.invokeLater(() ->
								{
									for (int i = 0; i < ais.size(); ++i)
									{
										final AI ai = ais.get(i);
	
										if (ai != null)
										{
											final String analysisReport = ai.generateAnalysisReport();
											if (analysisReport != null)
												manager.getPlayerInterface().addTextToAnalysisPanel(analysisReport + "\n");
										}
									}
								});
	
								if (!context().trial().over())
								{
									wantNextMoveCall.set(true);
									nextMove(manager, false);
								}
								else
								{
									allowHumanBasedStepStart.set(true);
								}
							}
							else
							{
								allowHumanBasedStepStart.set(true);
							}
						}
					});

					moveThread.setDaemon(true);
					moveThread.start();

					// Don't return until the model has at least started running (or maybe instantly completed)
					while (!wantNextMoveCall.get() && moveThread != null && moveThread.isAlive() && (model.isReady() || !model.isRunning()))
					{
						// Don't return from call yet, keep calling thread occupied until
						// model is at least properly running (or maybe already finished)
					}
				}
			}
			else
			{
				return false;
			}

			return true;
		}
		finally
		{
			if (!humanBasedStepStart && !context.model().isRunning())
			{
				allowHumanBasedStepStart.set(true);
			}
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Callback to call prior to application of AI-chosen moves
	 */
	void preMoveApplication(final Manager manager, final Move move)
	{
		if (manager.settingsManager().showRepetitions())
		{
			final Context newContext = new Context(context);
			newContext.trial().previousState().clear();
			newContext.trial().previousStateWithinATurn().clear();
			newContext.game().apply(newContext, move);
			manager.settingsManager().setMovesAllowedWithRepetition(newContext.game().moves(newContext).moves());
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Handle miscellaneous stuff we need to do after applying a move
	 */
	public void postMoveApplication(final Manager manager, final Move move, final boolean savedMove)
	{
		// Store the hash of each state encountered.
		if (manager.settingsManager().showRepetitions() && !manager.settingsManager().storedGameStatesForVisuals().contains(Long.valueOf(context.state().stateHash())))
			manager.settingsManager().storedGameStatesForVisuals().add(Long.valueOf(context.state().stateHash()));
		
		if (!savedMove)
		{
			manager.undoneMoves().clear();
			
			if (manager.settingsNetwork().getActiveGameId() != 0)
			{
				String scoreString = "";
				if (context.game().requiresScore())
					for (int i = 1; i <= context.game().players().count(); i++)
						scoreString += context.score(context.state().playerToAgent(i)) + ",";
				
				final int moveNumber = context.currentInstanceContext().trial().numMoves() - context.currentInstanceContext().trial().numInitialPlacementMoves();
				
				manager.databaseFunctionsPublic().sendMoveToDatabase(manager, move, context.state().mover(), scoreString, moveNumber);
				manager.databaseFunctionsPublic().checkNetworkSwap(manager, move);
			}
			
			// Check if need to apply instant Pass move.
			checkInstantPass(manager);
		}
		
		manager.getPlayerInterface().setTemporaryMessage("");
		manager.getPlayerInterface().postMoveUpdates(move, savedMove);
	}

	//-------------------------------------------------------------------------
	
	/** 
	 * Checks if a pass move should be applied instantly, if its the only legal move and the game is stochastic. 
	 */
	private void checkInstantPass(final Manager manager) 
	{
		final Moves legal = context.game().moves(context);
		final Move firstMove = legal.moves().get(0);
		
		if 
		(
			manager.aiSelected()[manager.moverToAgent()].ai() == null 			// Don't check instant pass if an AI is selected. Can potentially cause GUI threading issues.
			&& 
			legal.moves().size() == 1 && firstMove.isPass() && firstMove.isForced()
			&& 
			(!context.game().isStochasticGame() || manager.settingsManager().alwaysAutoPass())
			&&
			manager.settingsNetwork().getActiveGameId() == 0					// Instant pass can cause problems for remote games.
		)
		{
			applyHumanMoveToGame(manager, firstMove);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Attempts to interrupt any AI that is currently running, and returns only once there no longer is any AI thinking thread alive.
	 */
	public void interruptAI(final Manager manager)
	{
		context.model().interruptAIs();
		manager.setLiveAIs(null);
		allowHumanBasedStepStart.set(true);
	}
    
    //-------------------------------------------------------------------------
	
	/**
	 * Runnable for per-step threads in Referee
	 * 
	 * @author Dennis Soemers
	 */
	protected static abstract class RefereeStepRunnable implements Runnable
	{
		/** We can set this to true to indicate that we want to terminate, for example when loading a new game */
		public boolean shouldTerminate = false;
	}
	
	/**
	 * Per-step thread for referee
	 * 
	 * @author Dennis Soemers
	 */
	protected static class RefereeStepThread extends Thread
	{
		
		/** Our runnable */
		public final RefereeStepRunnable runnable;
		
		/**
		 * Constructor
		 * @param runnable
		 */
		public RefereeStepThread(final RefereeStepRunnable runnable)
		{
			super(runnable);
			this.runnable = runnable;
		}
	}
	
	//-------------------------------------------------------------------------

}