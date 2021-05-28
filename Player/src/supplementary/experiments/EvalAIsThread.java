package supplementary.experiments;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.IntStream;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.collections.ListUtils;
import manager.Manager;
import manager.Referee;
import manager.ai.AIDetails;
import other.AI;
import other.RankUtils;
import other.context.Context;
import utils.experiments.ResultsSummary;

/**
 * Thread in which multiple AI players can be evaluated when playing
 * against each other.
 * 
 * @author Dennis Soemers
 */
public class EvalAIsThread extends Thread
{
	/** Our runnable */
	private final EvalAIsThreadRunnable runnable;

	//-------------------------------------------------------------------------

	/**
	 * @param ref
	 * @param aiPlayers 
	 * @return Constructs a thread for AIs to be evaluated in
	 */
	public static EvalAIsThread construct
	(
		final Referee ref,
		final List<AI> aiPlayers,
		final Manager manger
	)
	{
		final EvalAIsThreadRunnable runnable = 
				new EvalAIsThreadRunnable
				(
					ref,
					aiPlayers,
					manger
				);

		return new EvalAIsThread(runnable);
	}

	/**
	 * Constructor
	 * @param runnable
	 */
	protected EvalAIsThread(final EvalAIsThreadRunnable runnable)
	{
		super(runnable);
		this.runnable = runnable;
	}

	//-------------------------------------------------------------------------

	public EvalAIsThreadRunnable getRunnable()
	{
		return runnable;
	}

	/**
	 * Runnable class for Eval AIs Thread
	 * 
	 * @author Dennis Soemers
	 */
	private static class EvalAIsThreadRunnable implements Runnable
	{

		//---------------------------------------------------------------------

		/** Referee */
		protected final Referee ref;
		
		/** AI players */
		protected final List<AI> aiPlayers;
		
		protected Manager manager;

		//---------------------------------------------------------------------

		/**
		 * Constructor 
		 * @param ref 
		 * @param aiPlayers
		 */
		public EvalAIsThreadRunnable
		(
			final Referee ref,
			final List<AI> aiPlayers,
			final Manager manager
		)
		{
			this.ref = ref;
			this.aiPlayers = aiPlayers;
			this.manager = manager;
		}

		//---------------------------------------------------------------------

		@Override
		public void run()
		{
			final int maxNumGames = 100;
			final Game game = ref.context().game();
			final int numPlayers = game.players().count();
			
			final List<String> agentStrings = new ArrayList<String>(numPlayers);
			for (int i = 0; i < numPlayers; ++i)
			{
				final AI ai = aiPlayers.get(i + 1);
				final int playerIdx = i + 1;
				
				if (ai == null)
				{
					try
					{
						EventQueue.invokeAndWait(new Runnable()
						{
							@Override
							public void run()
							{
								manager.getPlayerInterface().addTextToAnalysisPanel
								(
									"Cannot run evaluation; Player " + playerIdx + " is not AI.\n"
								);
							}
						});
					}
					catch (final InvocationTargetException | InterruptedException e)
					{
						e.printStackTrace();
					}
					
					return;
				}
				else if (!ai.supportsGame(game))
				{
					try
					{
						EventQueue.invokeAndWait(new Runnable()
						{
							@Override
							public void run()
							{
								manager.getPlayerInterface().addTextToAnalysisPanel
								(
									"Cannot run evaluation; " + ai.friendlyName + " does not support this game.\n"
								);
							}
						});
					}
					catch (final InvocationTargetException | InterruptedException e)
					{
						e.printStackTrace();
					}
					
					return;
				}
				
				agentStrings.add(manager.aiSelected()[i+1].name());
			}
			
//			final String[] originalPlayerNames = Arrays.copyOf(AIDetails.convertToNameArray(PlayerApp.aiSelected()), PlayerApp.aiSelected().length);
			
			final Context context = ref.context();
			
			final List<TIntArrayList> aiListPermutations = 
					ListUtils.generatePermutations(
							TIntArrayList.wrap(
									IntStream.range(1, numPlayers + 1).toArray()));
			
			final ResultsSummary resultsSummary = new ResultsSummary(game, agentStrings);
			
			final Timer updateGuiTimer = new Timer();
			
			try
			{
				int gameCounter = 0;
				for (/**/; gameCounter < maxNumGames; ++gameCounter)
				{
					// compute list of AIs to use for this game (we rotate every game)
					final List<AI> currentAIList = new ArrayList<AI>(numPlayers);
					final int currentAIsPermutation = gameCounter % aiListPermutations.size();
					
					final TIntArrayList currentPlayersPermutation = 
							aiListPermutations.get(currentAIsPermutation);
					currentAIList.add( null); // 0 index not used

					for (int i = 0; i < currentPlayersPermutation.size(); ++i)
					{
						final AI ai = aiPlayers.get(currentPlayersPermutation.getQuick(i));
						currentAIList.add(ai);
					}
					
					// play a game
					game.start(context);
					
					updateGuiTimer.schedule(new TimerTask()
					{
						@Override
						public void run()
						{
							manager.getPlayerInterface().repaint();
						}
					}, Referee.AI_VIS_UPDATE_TIME , Referee.AI_VIS_UPDATE_TIME 
					);
					
					for (int p = 1; p < currentAIList.size(); ++p)
					{
						currentAIList.get(p).initAI(game, p);
					}
					
					while (!context.trial().over())
					{
						if (manager.settingsManager().agentsPaused())
						{
							final int passedAtGameCounter = gameCounter;
							EventQueue.invokeLater
							(
								new Runnable()
								{

									@Override
									public void run()
									{
										manager.getPlayerInterface().addTextToAnalysisPanel
										(
											"Evaluation interrupted after completing " + passedAtGameCounter + " games."
										);
									}
										
								}
							);
							
							return;
						}
						
						context.model().startNewStep
						(
							context, 
							currentAIList, 
							AIDetails.convertToThinkTimeArray(manager.aiSelected()), 
							-1, -1, 0.3,
							false, false, false, 
							null, null
						);
						
						manager.setLiveAIs(context.model().getLiveAIs());
						
						while (!context.model().isReady())
						{
							Thread.sleep(100L);
						}
						
						manager.setLiveAIs(null);
					}
					
					final double[] utilities = RankUtils.agentUtilities(context);
					final int numMovesPlayed = context.trial().numMoves() - context.trial().numInitialPlacementMoves();
					final int[] agentPermutation = new int[currentPlayersPermutation.size() + 1];
					currentPlayersPermutation.toArray(agentPermutation, 0, 1, currentPlayersPermutation.size());
					for (int p = 1; p < agentPermutation.length; ++p)
					{
						agentPermutation[p] -= 1;
					}

					resultsSummary.recordResults(agentPermutation, utilities, numMovesPlayed);
					
					manager.getPlayerInterface().addTextToAnalysisPanel(resultsSummary.generateIntermediateSummary());
					manager.getPlayerInterface().addTextToAnalysisPanel("\n");
					
					// Close AIs
					for (int p = 1; p < currentAIList.size(); ++p)
					{
						currentAIList.get(p).closeAI();
					}
					
					Thread.sleep(1000);	// wait for a second before starting next game
				}
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				updateGuiTimer.cancel();
				updateGuiTimer.purge();
				
				manager.setLiveAIs(null);
				manager.getPlayerInterface().repaint();
			}
		}

	}

	//-------------------------------------------------------------------------

}
