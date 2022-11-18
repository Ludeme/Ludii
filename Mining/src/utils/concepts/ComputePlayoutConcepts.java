package utils.concepts;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import game.equipment.container.Container;
import game.rules.end.End;
import game.rules.end.EndRule;
import game.rules.phase.Phase;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import metrics.Evaluation;
import metrics.Metric;
import metrics.Utils;
import other.AI;
import other.concept.Concept;
import other.concept.ConceptDataType;
import other.concept.ConceptType;
import other.context.Context;
import other.model.Model;
import other.move.Move;
import other.state.container.ContainerState;
import other.trial.Trial;
import search.minimax.AlphaBetaSearch;
import search.minimax.AlphaBetaSearch.AllowedSearchDepths;
import utils.AIFactory;

/**
 * To update a game object with the estimated values of the playout concepts.
 * 
 * @author Eric.Piette
 */
public class ComputePlayoutConcepts
{
	/**
	 * To create RulesetConcepts.csv (Id, RulesetId, ConceptId, Value)
	 * 
	 * @param game             The game to update.
	 * @param numPlayouts      The maximum number of playout.
	 * @param timeLimit        The maximum time to compute the playouts concepts.
	 * @param thinkingTime     The maximum time to take a decision per move.
	 * @param agentName        The name of the agent to use for the playout concepts.
	 * @param portfolioConcept To compute only the concepts for the portfolio.
	 */
	public static void updateGame
	(
		final Game game,
		final Evaluation evaluation,
		final int numPlayouts, 
		final double timeLimit, 
		final double thinkingTime, 
		final String agentName,
		final boolean portfolioConcept
	)
	{
		final List<Concept> nonBooleanConcepts = new ArrayList<Concept>();
		for (final Concept concept : (portfolioConcept) ? Concept.portfolioConcepts() : Concept.values())
		{
			if (!concept.dataType().equals(ConceptDataType.BooleanData))
				nonBooleanConcepts.add(concept);
		}

		final Map<String, Double> frequencyPlayouts = (numPlayouts == 0) ? new HashMap<String, Double>()
			: playoutsMetrics(game, evaluation, numPlayouts, timeLimit, thinkingTime, agentName, portfolioConcept);

		for (final Concept concept : nonBooleanConcepts)
		{
			final double value = frequencyPlayouts.get(concept.name()) == null ? Constants.UNDEFINED
				: frequencyPlayouts.get(concept.name()).doubleValue();
						
			game.nonBooleanConcepts().put(Integer.valueOf(concept.id()), value+"");
		}
	}

	//------------------------------PLAYOUT CONCEPTS-----------------------------------------------------
	
	/**
	 * @param game         The game
	 * @param playoutLimit The number of playouts to run.
	 * @param timeLimit    The maximum time to use.
	 * @param thinkingTime The maximum time to take a decision at each state.
	 * @param portfolioConcept To compute only the concepts for the portfolio.
	 * @return The frequency of all the boolean concepts in the number of playouts
	 *         set in entry
	 */
	private static Map<String, Double> playoutsMetrics
	(
		final Game game, 
		final Evaluation evaluation,
		final int playoutLimit, 
		final double timeLimit,
		final double thinkingTime,
		final String agentName,
		final boolean portfolioConcept
	)
	{
		final long startTime = System.currentTimeMillis();

		// Used to return the frequency (of each playout concept).
		final Map<String, Double> mapFrequency = new HashMap<String, Double>();
		
		// Used to return the value of each metric.
		final List<Trial> trials = new ArrayList<Trial>();
		final List<RandomProviderState> allStoredRNG = new ArrayList<RandomProviderState>();

		// For now I exclude the matchs, but can be included too after. The deduc puzzle
		// will stay excluded.
		if (game.hasSubgames() || game.isDeductionPuzzle() || game.isSimulationMoveGame()
				|| game.name().contains("Trax") || game.name().contains("Kriegsspiel"))
		{
			// We add all the default metrics values corresponding to a concept to the returned map.
			final List<Metric> metrics = new Evaluation().conceptMetrics();
			for(final Metric metric: metrics)
				if(metric.concept() != null)
					mapFrequency.put(metric.concept().name(), null);
			return mapFrequency;
		}
		
		// We run the playouts needed for the computation.
		for (int indexPlayout = 0; indexPlayout < playoutLimit; indexPlayout++)
		{
			final List<AI> ais = chooseAI(game, agentName, indexPlayout);
			
			for(final AI ai : ais)
				if(ai != null)
					ai.setMaxSecondsPerMove(thinkingTime);
			
			final Context context = new Context(game, new Trial(game));
			allStoredRNG.add(context.rng().saveState());
			final Trial trial = context.trial();
			game.start(context);

			// Init the ais (here random).
			for (int p = 1; p <= game.players().count(); ++p)
				ais.get(p).initAI(game, p);
			final Model model = context.model();

			while (!trial.over())
				model.startNewStep(context, ais, thinkingTime);

			trials.add(trial);

			final double currentTimeUsed = (System.currentTimeMillis() - startTime) / 1000.0;
			if (currentTimeUsed > timeLimit) // We stop if the limit of time is reached.
				break;
		}
		
		// We get the values of the starting concepts.
		mapFrequency.putAll(startsConcepts(game, allStoredRNG));
		

		final long startTimeFrequency = System.currentTimeMillis();
		
		// We get the values of the frequencies.
		mapFrequency.putAll(frequencyConcepts(game,trials, allStoredRNG));

		final double ms = (System.currentTimeMillis() - startTimeFrequency);
		System.out.println("Playouts computation done in " + ms + " ms.");

		// We get the values of the metrics.
		if(!portfolioConcept)
			mapFrequency.putAll(metricsConcepts(game, evaluation, trials, allStoredRNG));
		
		// Computation of the p/s and m/s
		if(!portfolioConcept)
			mapFrequency.putAll(playoutsEstimationConcepts(game));
		
		return mapFrequency;
	}
	
	/**
	 * @param game The game.
	 * @param agentName The name of the agent.
	 * @param indexPlayout The index of the playout.
	 * @return The list of AIs to play that playout.
	 */
	private static List<AI> chooseAI(final Game game, final String agentName, final int indexPlayout)
	{
		final List<AI> ais = new ArrayList<AI>();
		ais.add(null);
		
		for (int p = 1; p <= game.players().count(); ++p)
		{
			if(agentName.equals("UCT"))
			{
				final AI ai = AIFactory.createAI("UCT");
				if(ai.supportsGame(game))
				{
					ais.add(ai);
				}
				else
				{
					ais.add(new utils.RandomAI());
				}
			}
			else if(agentName.equals("Alpha-Beta"))
			{
				AI ai = AIFactory.createAI("Alpha-Beta");
				if(ai.supportsGame(game))
				{
					ais.add(ai);
				}
				else if (AIFactory.createAI("UCT").supportsGame(game))
				{
					ai = AIFactory.createAI("UCT");
					ais.add(ai);
				}
				else 
				{
					ais.add(new utils.RandomAI());
				}
			}
			else if(agentName.equals("Alpha-Beta-UCT")) // AB/UCT/AB/UCT/...
			{
				if(indexPlayout % 2 == 0)
				{
					if(p % 2 == 1)
					{
						AI ai = AIFactory.createAI("Alpha-Beta");
						if(ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else if (AIFactory.createAI("UCT").supportsGame(game))
						{
							ai = AIFactory.createAI("UCT");
							ais.add(ai);
						}
						else 
						{
							ais.add(new utils.RandomAI());
						}
					}
					else
					{
						final AI ai = AIFactory.createAI("UCT");
						if(ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else
						{
							ais.add(new utils.RandomAI());
						}
					}
				}
				else
				{
					if(p % 2 == 1)
					{
						final AI ai = AIFactory.createAI("UCT");
						if(ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else
						{
							ais.add(new utils.RandomAI());
						}
					}
					else
					{
						AI ai = AIFactory.createAI("Alpha-Beta");
						if(ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else if (AIFactory.createAI("UCT").supportsGame(game))
						{
							ai = AIFactory.createAI("UCT");
							ais.add(ai);
						}
						else 
						{
							ais.add(new utils.RandomAI());
						}
					}
				}
			}
			else if(agentName.equals("AB-Odd-Even")) // Alternating between AB Odd and AB Even
			{
				if(indexPlayout % 2 == 0)
				{
					if(p % 2 == 1)
					{
						AI ai = new AlphaBetaSearch();
						((AlphaBetaSearch)ai).setAllowedSearchDepths(AllowedSearchDepths.Odd);
						if(ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else if (AIFactory.createAI("UCT").supportsGame(game))
						{
							ai = AIFactory.createAI("UCT");
							ais.add(ai);
						}
						else 
						{
							ais.add(new utils.RandomAI());
						}
					}
					else
					{
						final AlphaBetaSearch ai = new AlphaBetaSearch();
						ai.setAllowedSearchDepths(AllowedSearchDepths.Even);
						if(ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else
						{
							ais.add(new utils.RandomAI());
						}
					}
				}
				else
				{
					if(p % 2 == 1)
					{
						final AlphaBetaSearch ai = new AlphaBetaSearch();
						ai.setAllowedSearchDepths(AllowedSearchDepths.Even);
						if(ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else
						{
							ais.add(new utils.RandomAI());
						}
					}
					else
					{
						AI ai = new AlphaBetaSearch();
						((AlphaBetaSearch)ai).setAllowedSearchDepths(AllowedSearchDepths.Odd);
						if(ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else if (AIFactory.createAI("UCT").supportsGame(game))
						{
							ai = AIFactory.createAI("UCT");
							ais.add(ai);
						}
						else 
						{
							ais.add(new utils.RandomAI());
						}
					}
				}
			}
			else
			{
				ais.add(new utils.RandomAI());
			}
		}
		return ais;
	}

	/**
	 * 
	 * @param game The game.
	 * @param trials The trials.
	 * @param allStoredRNG The RNG for each trial.
	 * @return The map of playout concepts to the their values for the starting ones.
	 */
	private static Map<String, Double> startsConcepts(final Game game, final List<RandomProviderState> allStoredRNG)
	{
		final Map<String, Double> mapStarting = new HashMap<String, Double>();
		
		final BitSet booleanConcepts = game.booleanConcepts();
		double numStartComponents = 0.0;
		double numStartComponentsHands = 0.0;
		double numStartComponentsBoard = 0.0;
		
		for (int index = 0; index < allStoredRNG.size(); index++)
		{
			final RandomProviderState rngState = allStoredRNG.get(index);
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			for (int cid = 0; cid < context.containers().length; cid++)
			{
				final Container cont = context.containers()[cid];
				final ContainerState cs = context.containerState(cid);
				if (cid == 0)
				{
					if (booleanConcepts.get(Concept.Cell.id()))
						for (int cell = 0; cell < cont.topology().cells().size(); cell++)
						{
							final int count = game.isStacking() ? cs.sizeStack(cell, SiteType.Cell) : cs.count(cell, SiteType.Cell);
							numStartComponents += count;
							numStartComponentsBoard += count;
						}
		
					if (booleanConcepts.get(Concept.Vertex.id()))
						for (int vertex = 0; vertex < cont.topology().vertices().size(); vertex++)
						{
							final int count = game.isStacking() ? cs.sizeStack(vertex, SiteType.Vertex) : cs.count(vertex, SiteType.Vertex);
							numStartComponents += count;
							numStartComponentsBoard += count;
						}
		
					if (booleanConcepts.get(Concept.Edge.id()))
						for (int edge = 0; edge < cont.topology().edges().size(); edge++)
						{
							final int count = game.isStacking() ? cs.sizeStack(edge, SiteType.Edge) : cs.count(edge, SiteType.Edge);
							numStartComponents += count;
							numStartComponentsBoard += count;
						}
				}
				else
				{
					if (booleanConcepts.get(Concept.Cell.id()))
						for (int cell = context.sitesFrom()[cid]; cell < context.sitesFrom()[cid]
								+ cont.topology().cells().size(); cell++)
						{
							final int count = game.isStacking() ? cs.sizeStack(cell, SiteType.Cell) : cs.count(cell, SiteType.Cell);
							numStartComponents += count;
							numStartComponentsHands += count;
						}
				}
			}
		}
		
		mapStarting.put(Concept.NumStartComponents.name(), Double.valueOf(numStartComponents / allStoredRNG.size()));
		mapStarting.put(Concept.NumStartComponentsHand.name(), Double.valueOf(numStartComponentsHands / allStoredRNG.size()));
		mapStarting.put(Concept.NumStartComponentsBoard.name(), Double.valueOf(numStartComponentsBoard / allStoredRNG.size()));
		
		mapStarting.put(Concept.NumStartComponentsPerPlayer.name(), Double.valueOf((numStartComponents / allStoredRNG.size()) / game.players().count()));
		mapStarting.put(Concept.NumStartComponentsHandPerPlayer.name(), Double.valueOf((numStartComponentsHands / allStoredRNG.size()) / game.players().count()));
		mapStarting.put(Concept.NumStartComponentsBoardPerPlayer.name(), Double.valueOf((numStartComponentsBoard / allStoredRNG.size()) / game.players().count()));
		
		return mapStarting;
	}
	
	//------------------------------Frequency CONCEPTS-----------------------------------------------------
	
	/**
	 * @param game The game.
	 * @param trials The trials.
	 * @param allStoredRNG The RNG for each trial.
	 * @return The map of playout concepts to the their values for the frequency ones.
	 */
	private static Map<String, Double> frequencyConcepts(final Game game, final List<Trial> trials, final List<RandomProviderState> allStoredRNG)
	{
		final Map<String, Double> mapFrequency = new HashMap<String, Double>();
		// Frequencies of the moves.
		final TDoubleArrayList frequencyMoveConcepts = new TDoubleArrayList();

		// Frequencies returned by all the playouts.
		final TDoubleArrayList frequencyPlayouts = new TDoubleArrayList();
		for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
			frequencyPlayouts.add(0.0);

		for (int trialIndex = 0; trialIndex < trials.size(); trialIndex++)
		{
			final Trial trial = trials.get(trialIndex);
			final RandomProviderState rngState = allStoredRNG.get(trialIndex);
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Frequencies returned by that playout.
			final TDoubleArrayList frenquencyPlayout = new TDoubleArrayList();
			for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
				frenquencyPlayout.add(0);

			// Run the playout.
			int turnWithMoves = 0;
			Context prevContext = null;
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				final Moves legalMoves = context.game().moves(context);
				
				final TIntArrayList frenquencyTurn = new TIntArrayList();
				for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
					frenquencyTurn.add(0);
				
				final double numLegalMoves = legalMoves.moves().size();
				if (numLegalMoves > 0)
					turnWithMoves++;
				
				for (final Move legalMove : legalMoves.moves())
				{
					final BitSet moveConcepts = legalMove.moveConcepts(context);
					for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
					{
						final Concept concept = Concept.values()[indexConcept];
						if (moveConcepts.get(concept.id()))
							frenquencyTurn.set(indexConcept, frenquencyTurn.get(indexConcept) + 1);
					}
				}
				
				for (int j = 0; j < frenquencyTurn.size(); j++)
					frenquencyPlayout.set(j, frenquencyPlayout.get(j) + (numLegalMoves == 0 ? 0 : frenquencyTurn.get(j) / numLegalMoves));
				
				// We keep the context before the ending state for the frequencies of the end conditions.
				if(i == trial.numMoves()-1)
					prevContext = new Context(context);
				
				// We go to the next move.
				context.game().apply(context, trial.getMove(i));
			}
			
			// Compute avg for all the playouts.
			for (int j = 0; j < frenquencyPlayout.size(); j++)
				frequencyPlayouts.set(j, frequencyPlayouts.get(j) + frenquencyPlayout.get(j) / turnWithMoves);

			context.trial().lastMove().apply(prevContext, true);

			boolean noEndFound = true;

			if (game.rules().phases() != null)
			{
				final int mover = context.state().mover();
				final Phase endPhase = game.rules().phases()[context.state().currentPhase(mover)];
				final End EndPhaseRule = endPhase.end();

				// Only check if action not part of setup
				if (context.active() && EndPhaseRule != null)
				{
					final EndRule[] endRules = EndPhaseRule.endRules();
					for (final EndRule endingRule : endRules)
					{
						final EndRule endRuleResult = endingRule.eval(prevContext);
						if (endRuleResult == null)
							continue;

						final BitSet endConcepts = endingRule.stateConcepts(prevContext);

						noEndFound = false;
						for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
						{
							final Concept concept = Concept.values()[indexConcept];
							if (concept.type().equals(ConceptType.End) && endConcepts.get(concept.id()))
							{
								// System.out.println("end with " + concept.name());
								frequencyPlayouts.set(indexConcept, frequencyPlayouts.get(indexConcept) + 1);
							}
						}
						// System.out.println();
						break;
					}
				}
			}

			final End endRule = game.endRules();
			if (noEndFound && endRule != null)
			{
				final EndRule[] endRules = endRule.endRules();
				for (final EndRule endingRule : endRules)
				{
					final EndRule endRuleResult = endingRule.eval(prevContext);
					if (endRuleResult == null)
						continue;
	
					final BitSet endConcepts = endingRule.stateConcepts(prevContext);
	
					noEndFound = false;
					for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
					{
						final Concept concept = Concept.values()[indexConcept];
						if (concept.type().equals(ConceptType.End) && endConcepts.get(concept.id()))
						{
							// System.out.println("end with " + concept.name());
							frequencyPlayouts.set(indexConcept, frequencyPlayouts.get(indexConcept) + 1);
						}
					}
					// System.out.println();
					break;
				}
			}

			if (noEndFound)
			{
				for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
				{
					final Concept concept = Concept.values()[indexConcept];
					if (concept.equals(Concept.Draw))
					{
						frequencyPlayouts.set(indexConcept, frequencyPlayouts.get(indexConcept) + 1);
						break;
					}
				}
			}
		}

		// Compute avg frequency for the game.
		for (int i = 0; i < frequencyPlayouts.size(); i++)
			frequencyMoveConcepts.add(frequencyPlayouts.get(i) / trials.size());

		for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
		{
			final Concept concept = Concept.values()[indexConcept];
			mapFrequency.put(concept.name(), Double.valueOf(frequencyMoveConcepts.get(indexConcept)));
		}
		
		return mapFrequency;
	}
	
	//------------------------------Metrics CONCEPTS-----------------------------------------------------
	
	/**
	 * @param game The game.
	 * @param trials The trials.
	 * @param allStoredRNG The RNG for each trial.
	 * @return The map of playout concepts to the their values for the metric ones.
	 */
	private static Map<String, Double> metricsConcepts(final Game game, final Evaluation evaluation, final List<Trial> trials, final List<RandomProviderState> allStoredRNG)
	{
		final Map<String, Double> playoutConceptValues = new HashMap<String, Double>();
		// We get the values of the metrics.
		final Trial[] trialsMetrics = new Trial[trials.size()];
		final RandomProviderState[] rngTrials = new RandomProviderState[trials.size()];
		for(int i = 0 ; i < trials.size();i++)
		{
			trialsMetrics[i] = new Trial(trials.get(i));
			rngTrials[i] = allStoredRNG.get(i);
		}
		
		// We add all the metrics corresponding to a concept to the returned map.
		final List<Metric> metrics = new Evaluation().conceptMetrics();
		for(final Metric metric: metrics)
			if(metric.concept() != null)
			{
				double metricValue = metric.apply(game, evaluation, trialsMetrics, rngTrials).doubleValue();
				metricValue = (Math.abs(metricValue) < Constants.EPSILON) ? 0 : metricValue;
				playoutConceptValues.put(metric.concept().name(), Double.valueOf(metricValue));
			}

		return playoutConceptValues;
	}
	
	//------------------------------Playout Estimation CONCEPTS-----------------------------------------------------
	
	/**
	 * @param game The game.
	 * @return The map of playout concepts to the their values for the p/s and m/s ones.
	 */
	private static Map<String, Double> playoutsEstimationConcepts(final Game game)
	{
		final Map<String, Double> playoutConceptValues = new HashMap<String, Double>();
		// Computation of the p/s and m/s
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);

		// Warming up
		long stopAt = 0L;
		long start = System.nanoTime();
		final double warmingUpSecs = 1;
		final double measureSecs = 3;
		double abortAt = start + warmingUpSecs * 1000000000.0;
		while (stopAt < abortAt)
		{
			game.start(context);
			game.playout(context, null, 1.0, null, -1, Constants.UNDEFINED, ThreadLocalRandom.current());
			stopAt = System.nanoTime();
		}
		System.gc();

		// Set up RNG for this game, Always with a rng of 2077.
		final Random rng = new Random((long)game.name().hashCode() * 2077);

		// The Test
		stopAt = 0L;
		start = System.nanoTime();
		abortAt = start + measureSecs * 1000000000.0;
		int playouts = 0;
		int moveDone = 0;
		while (stopAt < abortAt)
		{
			game.start(context);
			game.playout(context, null, 1.0, null, -1, Constants.UNDEFINED, rng);
			moveDone += context.trial().numMoves();
			stopAt = System.nanoTime();
			++playouts;
		}

		final double secs = (stopAt - start) / 1000000000.0;
		final double rate = (playouts / secs);
		final double rateMove = (moveDone / secs);
		playoutConceptValues.put(Concept.PlayoutsPerSecond.name(), Double.valueOf(rate));
		playoutConceptValues.put(Concept.MovesPerSecond.name(), Double.valueOf(rateMove));

		return playoutConceptValues;
	}
	
}
