package utils.concepts.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

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
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.options.Ruleset;
import metrics.Evaluation;
import metrics.Metric;
import metrics.Utils;
import other.AI;
import other.GameLoader;
import other.concept.Concept;
import other.concept.ConceptComputationType;
import other.concept.ConceptDataType;
import other.concept.ConceptPurpose;
import other.concept.ConceptType;
import other.context.Context;
import other.model.Model;
import other.move.Move;
import other.state.container.ContainerState;
import other.trial.Trial;
import search.minimax.AlphaBetaSearch;
import search.minimax.AlphaBetaSearch.AllowedSearchDepths;
import utils.AIFactory;
import utils.IdRuleset;

/**
 * To export the necessary CSV to build the tables in the database for the
 * concepts.
 * 
 * @author Eric.Piette
 * 
 *         Structure for the db:
 * 
 *         Concepts.csv (Id, Name, Description, TypeId, DataTypeId, ComputationTypeId)
 * 
 *         ConceptTypes.csv (Id, Name)
 * 
 *         ConceptDataTypes.csv (Id, Name)
 *         
 *         ConceptComputationTypes.csv (Id, Name)
 * 
 *         ConceptKeywords.csv (Id, Name, Description)
 * 
 *         ConceptPurposes.csv (Id, Name)
 * 
 *         ConceptConceptKeywords.csv (Id, ConceptId, KeywordId)
 * 
 *         ConceptConceptPurposes.csv (Id, ConceptId, PurposeId)
 * 
 *         RulesetConcepts.csv (Id, RulesetId, ConceptId, Value)
 */
public class ExportDbCsvConcepts
{
	/** The path of the csv with the id of the rulesets for each game. */
	private static final String GAME_RULESET_PATH = "/concepts/input/GameRulesets.csv";

	//-------------------------------------------------------------------------

	public static void main(final String[] args)
	{
		final int numPlayouts = args.length == 0 ? 0 : Integer.parseInt(args[0]);
		final double timeLimit = args.length < 2 ? 0 : Double.parseDouble(args[1]);
		final double thinkingTime = args.length < 3 ? 1 : Double.parseDouble(args[2]);
		final String agentName = args.length < 4 ? "Random" : args[3];
		final String gameName = args.length < 5 ? "" : args[4];
		final String rulesetName = args.length < 6 ? "" : args[5];

		if (gameName.isEmpty())
		{
			exportConceptCSV();
			exportConceptTypeCSV();
			exportConceptDataTypeCSV();
			exportConceptComputationTypeCSV();
			exportConceptPurposeCSV();
			exportConceptConceptPurposesCSV();
		}

		exportRulesetConceptsCSV(numPlayouts, timeLimit, thinkingTime, agentName, gameName, rulesetName);
	}

	//-------------------------------------------------------------------------

	/**
	 * To create Concepts.csv (Id, Name, Description, TypeId, DataTypeId)
	 */
	public static void exportConceptCSV()
	{
		final String outputConcept = "Concepts.csv";
		System.out.println("Writing Concepts.csv");
		try (final PrintWriter writer = new UnixPrintWriter(new File(outputConcept), "UTF-8"))
		{
			for (final Concept concept : Concept.values())
			{
				final List<String> lineToWrite = new ArrayList<String>();
				lineToWrite.add(concept.id() + "");
				lineToWrite.add("\"" + concept.name() + "\"");
				lineToWrite.add("\"" + concept.description() + "\"");
				lineToWrite.add(concept.type().id() + "");
				lineToWrite.add(concept.dataType().id() + "");
				lineToWrite.add(concept.computationType().id() + "");
				lineToWrite.add("\"" + concept.taxonomy() + "\"");
				lineToWrite.add(concept.isleaf() ? "1" : "0");
				writer.println(StringRoutines.join(",", lineToWrite));
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		System.out.println("Done.");
	}

	//-------------------------------------------------------------------------

	/**
	 * To create ConceptTypes.csv (Id, Name)
	 */
	public static void exportConceptTypeCSV()
	{
		final String outputConceptType = "ConceptTypes.csv";
		System.out.println("Writing ConceptTypes.csv");
		try (final PrintWriter writer = new UnixPrintWriter(new File(outputConceptType), "UTF-8"))
		{
			for (final ConceptType conceptType : ConceptType.values())
			{
				final List<String> lineToWrite = new ArrayList<String>();
				lineToWrite.add(conceptType.id() + "");
				lineToWrite.add("\"" + conceptType.name() + "\"");
				writer.println(StringRoutines.join(",", lineToWrite));
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		System.out.println("Done.");
	}

	//-------------------------------------------------------------------------

	/**
	 * To create ConceptDataTypes.csv (Id, Name)
	 */
	public static void exportConceptDataTypeCSV()
	{
		final String outputDataType = "ConceptDataTypes.csv";
		System.out.println("Writing ConceptDataTypes.csv");
		try (final PrintWriter writer = new UnixPrintWriter(new File(outputDataType), "UTF-8"))
		{
			for (final ConceptDataType dataType : ConceptDataType.values())
			{
				final List<String> lineToWrite = new ArrayList<String>();
				lineToWrite.add(dataType.id() + "");
				lineToWrite.add("\"" + dataType.name() + "\"");
				writer.println(StringRoutines.join(",", lineToWrite));
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		System.out.println("Done.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * To create ConceptComputationTypes.csv (Id, Name)
	 */
	public static void exportConceptComputationTypeCSV()
	{
		final String outputComputationType = "ConceptComputationTypes.csv";
		System.out.println("Writing ConceptComputationTypes.csv");
		try (final PrintWriter writer = new UnixPrintWriter(new File(outputComputationType), "UTF-8"))
		{
			for (final ConceptComputationType dataType : ConceptComputationType.values())
			{
				final List<String> lineToWrite = new ArrayList<String>();
				lineToWrite.add(dataType.id() + "");
				lineToWrite.add("\"" + dataType.name() + "\"");
				writer.println(StringRoutines.join(",", lineToWrite));
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		System.out.println("Done.");
	}

	//-------------------------------------------------------------------------

	/**
	 * To create ConceptPurposes.csv (Id, Name)
	 */
	public static void exportConceptPurposeCSV()
	{
		final String outputConceptPurposes = "ConceptPurposes.csv";
		System.out.println("Writing ConceptPurposes.csv");
		try (final PrintWriter writer = new UnixPrintWriter(new File(outputConceptPurposes), "UTF-8"))
		{
			for (final ConceptPurpose purpose : ConceptPurpose.values())
			{
				final List<String> lineToWrite = new ArrayList<String>();
				lineToWrite.add(purpose.id() + "");
				lineToWrite.add("\"" + purpose.name() + "\"");
				writer.println(StringRoutines.join(",", lineToWrite));
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		System.out.println("Done.");
	}

	//-------------------------------------------------------------------------

	/**
	 * To create ConceptConceptPurposes.csv (Id, ConceptId, PurposeId)
	 */
	public static void exportConceptConceptPurposesCSV()
	{
		final String outputConceptConceptPurposes = "ConceptConceptPurposes.csv";
		System.out.println("Writing ConceptConceptPurposes.csv");
		try (final PrintWriter writer = new UnixPrintWriter(new File(outputConceptConceptPurposes), "UTF-8"))
		{
			int id = 1;
			for (final Concept concept : Concept.values())
			{
				for (final ConceptPurpose purpose : concept.purposes())
				{
					final List<String> lineToWrite = new ArrayList<String>();
					lineToWrite.add(id + "");
					lineToWrite.add(concept.id() + "");
					lineToWrite.add(purpose.id() + "");
					writer.println(StringRoutines.join(",", lineToWrite));
					id++;
				}
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		System.out.println("Done.");
	}

	//-------------------------------------------------------------------------

	/**
	 * To create RulesetConcepts.csv (Id, RulesetId, ConceptId, Value)
	 * 
	 * @param numPlayouts     The maximum number of playout.
	 * @param timeLimit       The maximum time to compute the playouts concepts.
	 * @param thinkingTime    The maximum time to take a decision per move.
	 * @param agentName       The name of the agent to use for the playout concepts
	 * @param name            The name of the game.
	 * @param rulesetExpected The name of the ruleset of the game. 
	 */
	public static void exportRulesetConceptsCSV
	(
		final int numPlayouts, 
		final double timeLimit, 
		final double thinkingTime, 
		final String agentName, 
		final String name, 
		final String rulesetExpected
	)
	{
		final List<String> games = new ArrayList<String>();
		final List<String> rulesets = new ArrayList<String>();
		final TIntArrayList ids = new TIntArrayList();
		try
		(
			final InputStream in = ExportDbCsvConcepts.class.getResourceAsStream(GAME_RULESET_PATH);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));	
		)
		{
			String line = reader.readLine();
			while (line != null)
			{
				String lineNoQuote = line.replaceAll(Pattern.quote("\""), "");

				int separatorIndex = lineNoQuote.indexOf(',');
				final String gameName = lineNoQuote.substring(0, separatorIndex);
				games.add(gameName);
				lineNoQuote = lineNoQuote.substring(gameName.length() + 1);

				separatorIndex = lineNoQuote.indexOf(',');
				final String rulesetName = lineNoQuote.substring(0, separatorIndex);
				rulesets.add(rulesetName);
				lineNoQuote = lineNoQuote.substring(rulesetName.length() + 1);
				final int id = Integer.parseInt(lineNoQuote);
				ids.add(id);
				// System.out.println(gameName + " --- " + rulesetName + " --- " + id);
				line = reader.readLine();
			}
			reader.close();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}

		final String fileName = name.isEmpty() ? ""
				: name.substring(name.lastIndexOf('/') + 1, name.length() - 4).replace(" ", "");
		final String outputRulesetConcepts = rulesetExpected.isEmpty() ? "RulesetConcepts" + fileName + ".csv" : "RulesetConcepts" + fileName +"-" + rulesetExpected.substring(8) + ".csv";
		System.out.println("Writing " + outputRulesetConcepts);
		try (final PrintWriter writer = new UnixPrintWriter(new File(outputRulesetConcepts), "UTF-8"))
		{
			final List<Concept> booleanConcepts = new ArrayList<Concept>();
			final List<Concept> nonBooleanConcepts = new ArrayList<Concept>();
			for (final Concept concept : Concept.values())
			{
				if (concept.dataType().equals(ConceptDataType.BooleanData))
					booleanConcepts.add(concept);
				else 
					nonBooleanConcepts.add(concept);
			}

			int id = 1;

			final String[] gameNames = FileHandling.listGames();

			for (int index = 0; index < gameNames.length; index++)
			{
				final String gameName = gameNames[index];
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/"))
					continue;

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/"))
					continue;

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/"))
					continue;

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/"))
					continue;

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("subgame"))
					continue;

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("reconstruction"))
					continue;

				if (!name.isEmpty() && !gameName.substring(1).equals(name.replaceAll(Pattern.quote("\\"), "/")))
					continue;

				final Game game = GameLoader.loadGameFromName(gameName);
				game.start(new Context(game, new Trial(game)));

				System.out.println("Loading game: " + game.name());

				final List<Ruleset> rulesetsInGame = game.description().rulesets();
				if (rulesetsInGame != null && !rulesetsInGame.isEmpty()) // Code for games with many rulesets
				{
					for (int rs = 0; rs < rulesetsInGame.size(); rs++)
					{
						final Ruleset ruleset = rulesetsInGame.get(rs);
						
						// We check if we want a specific ruleset.
						if(!rulesetExpected.isEmpty() && !rulesetExpected.equals(ruleset.heading()))
							continue;
						
						if (!ruleset.optionSettings().isEmpty()) // We check if the ruleset is implemented.
						{
							final Game rulesetGame = GameLoader.loadGameFromName(gameName, ruleset.optionSettings());

							System.out.println("Loading ruleset: " + rulesetGame.getRuleset().heading());
							final Map<String, Double> frequencyPlayouts = (numPlayouts == 0)
									? new HashMap<String, Double>()
									: playoutsMetrics(rulesetGame, numPlayouts, timeLimit, thinkingTime, agentName);

							final int idRuleset = IdRuleset.get(rulesetGame);
							final BitSet concepts = rulesetGame.booleanConcepts();
							for (final Concept concept : booleanConcepts)
							{
								if (concepts.get(concept.id()))
								{
									final List<String> lineToWrite = new ArrayList<String>();
									lineToWrite.add(id + ""); // id 
									lineToWrite.add(idRuleset + ""); // id ruleset
									lineToWrite.add(concept.id() + ""); // id concept
									lineToWrite.add("\"1\"");
									writer.println(StringRoutines.join(",", lineToWrite));
									id++;
								}
							}
							for (final Concept concept : nonBooleanConcepts)
							{
								if(concept.computationType().equals(ConceptComputationType.Compilation)) 
								{
									final List<String> lineToWrite = new ArrayList<String>();
									lineToWrite.add(id + "");
									lineToWrite.add(idRuleset + "");
									lineToWrite.add(concept.id() + "");
									lineToWrite.add("\"" + game.nonBooleanConcepts().get(Integer.valueOf(concept.id())) + "\"");
									writer.println(StringRoutines.join(",", lineToWrite));
									id++;
								}
								else
								{
									final String conceptName = concept.name();
									if(conceptName.indexOf("Frequency") == Constants.UNDEFINED) // Non Frequency concepts added to the csv.
									{
										final double value = frequencyPlayouts.get(concept.name()) == null ? 0
												: frequencyPlayouts.get(concept.name()).doubleValue();
										final List<String> lineToWrite = new ArrayList<String>();
										lineToWrite.add(id + "");
										lineToWrite.add(idRuleset + "");
										lineToWrite.add(concept.id() + "");
										lineToWrite.add(new DecimalFormat("##.##").format(value)); // the value of the metric
										writer.println(StringRoutines.join(",", lineToWrite));
										id++;
										//System.out.println("metric: " + concept);
									}
									else // Frequency concepts added to the csv.
									{
										final String correspondingBooleanConceptName = conceptName.substring(0,conceptName.indexOf("Frequency"));
										for (final Concept correspondingConcept : booleanConcepts)
										{
											if(correspondingConcept.name().equals(correspondingBooleanConceptName))
											{
												final List<String> lineToWrite = new ArrayList<String>();
												lineToWrite.add(id + "");
												lineToWrite.add(idRuleset + "");
												lineToWrite.add(concept.id() + "");
												final double frequency = frequencyPlayouts.get(correspondingConcept.name()) == null ? 0
														: frequencyPlayouts.get(correspondingConcept.name()).doubleValue();
//												if(frequency > 0)
//													System.out.println(concept + " = " + (frequency * 100) +"%");
												lineToWrite.add(
														(frequency > 0 ? new DecimalFormat("##.##").format(frequency) + "" : "0") + ""); // the frequency
												writer.println(StringRoutines.join(",", lineToWrite));
												id++;
											}
										}
									}
								}
							}
						}
					}
				}
				else // Code for the default ruleset.
				{
					final Map<String, Double> frequencyPlayouts = (numPlayouts == 0) ? new HashMap<String, Double>()
							: playoutsMetrics(game, numPlayouts, timeLimit, thinkingTime, agentName);

					final int idRuleset = IdRuleset.get(game);
					final BitSet concepts = game.booleanConcepts();
					for (final Concept concept : booleanConcepts)
					{
						if (concepts.get(concept.id()))
						{
							final List<String> lineToWrite = new ArrayList<String>();
							lineToWrite.add(id + "");
							lineToWrite.add(idRuleset + "");
							lineToWrite.add(concept.id() + "");
							lineToWrite.add("\"1\"");
							writer.println(StringRoutines.join(",", lineToWrite));
							id++;
						}
					}
					
					for (final Concept concept : nonBooleanConcepts)
					{
						if(concept.computationType().equals(ConceptComputationType.Compilation)) 
						{
							if (!game.nonBooleanConcepts().get(Integer.valueOf(concept.id())).equals("0"))
							{
								final List<String> lineToWrite = new ArrayList<String>();
								lineToWrite.add(id + "");
								lineToWrite.add(idRuleset + "");
								lineToWrite.add(concept.id() + "");
								lineToWrite.add("\"" + game.nonBooleanConcepts().get(Integer.valueOf(concept.id())) + "\"");
								writer.println(StringRoutines.join(",", lineToWrite));
								id++;
							}
						}
						else
						{
							if(concept.type().equals(ConceptType.Behaviour) || !concept.name().contains("Frequency")) // Non Frequency concepts added to the csv.
							{
								final double value = frequencyPlayouts.get(concept.name()) == null ? Constants.UNDEFINED
										: frequencyPlayouts.get(concept.name()).doubleValue();
								final List<String> lineToWrite = new ArrayList<String>();
								lineToWrite.add(id + "");
								lineToWrite.add(idRuleset + "");
								lineToWrite.add(concept.id() + "");
								lineToWrite.add(value == Constants.UNDEFINED ? "NULL" : new DecimalFormat("##.##").format(value)); // the value of the metric
								writer.println(StringRoutines.join(",", lineToWrite));
								id++;
								//System.out.println("metric: " + concept + " value is "  + value);
							}
							else // Frequency concepts added to the csv.
							{
								final String conceptName = concept.name();
								final String correspondingBooleanConceptName = conceptName.substring(0, conceptName.indexOf("Frequency"));
								for (final Concept correspondingConcept : booleanConcepts)
								{
									if(correspondingConcept.name().equals(correspondingBooleanConceptName))
									{
										final List<String> lineToWrite = new ArrayList<String>();
										lineToWrite.add(id + "");
										lineToWrite.add(idRuleset + "");
										lineToWrite.add(concept.id() + "");
										final double frequency = frequencyPlayouts.get(correspondingConcept.name()) == null ? 0
												: frequencyPlayouts.get(correspondingConcept.name()).doubleValue();
//										if(frequency > 0)
//											System.out.println(concept + " = " + (frequency * 100) +"%");
										lineToWrite.add(
												(frequency > 0 ? new DecimalFormat("##.##").format(frequency) + "" : "0") + ""); // the frequency
										writer.println(StringRoutines.join(",", lineToWrite));
										id++;
									}
								}
							}
						}
					}
				}
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		System.out.println("Done.");
	}

	//------------------------------PLAYOUT CONCEPTS-----------------------------------------------------
	
	/**
	 * @param game         The game
	 * @param playoutLimit The number of playouts to run.
	 * @param timeLimit    The maximum time to use.
	 * @param thinkingTime The maximum time to take a decision at each state.
	 * @return The frequency of all the boolean concepts in the number of playouts
	 *         set in entry
	 */
	private static Map<String, Double> playoutsMetrics
	(
		final Game game, 
		final int playoutLimit, 
		final double timeLimit,
		final double thinkingTime,
		final String agentName
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
			for(Metric metric: metrics)
				if(metric.concept() != null)
					mapFrequency.put(metric.concept().name(), null);
			return mapFrequency;
		}
		
		// We run the playouts needed for the computation.
		int playoutsDone = 0;
		for (int indexPlayout = 0; indexPlayout < playoutLimit; indexPlayout++)
		{
			final List<AI> ais = chooseAI(game, agentName, indexPlayout);
			
			for(AI ai : ais)
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

			System.out.println("\nNEW TRIAL\n");
			while (!trial.over())
			{
				model.startNewStep(context, ais, thinkingTime);
				// TO PRINT THE NUMBER OF PIECES PER TRIAL
				int countPieces = 0;
				int countPiecesP1 = 0;
				int countPiecesP2 = 0;
				final ContainerState cs = context.containerState(0);
				final int numCells = context.topology().cells().size();
				for(int i = 0; i < numCells; i++)
				{
					if(cs.what(i, SiteType.Cell) != 0)
						countPieces++;

					if(cs.what(i, SiteType.Cell) == 1)
						countPiecesP1++;

					if(cs.what(i, SiteType.Cell) == 2)
						countPiecesP2++;
				}
				
				System.out.println(countPieces+","+countPiecesP1+","+countPiecesP2);
			}

			trials.add(trial);
			playoutsDone++;

			final double currentTimeUsed = (System.currentTimeMillis() - startTime) / 1000.0;
			if (currentTimeUsed > timeLimit) // We stop if the limit of time is reached.
				break;
		}
		
		final double allSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
		final int seconds = (int) (allSeconds % 60.0);
		final int minutes = (int) ((allSeconds - seconds) / 60.0);
		System.out.println("Playouts done in " + minutes + " minutes " + seconds + " seconds. " + playoutsDone + " playouts.");

		// We get the values of the starting concepts.
		mapFrequency.putAll(startsConcepts(game, allStoredRNG));
		
		// We get the values of the frequencies.
		mapFrequency.putAll(frequencyConcepts(game,trials, allStoredRNG));
		
		// We get the values of the metrics.
		mapFrequency.putAll(metricsConcepts(game,trials, allStoredRNG));
		
		// Computation of the p/s and m/s
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
				AI ai = AIFactory.createAI("UCT");
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
						AI ai = AIFactory.createAI("UCT");
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
						AI ai = AIFactory.createAI("UCT");
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
						AlphaBetaSearch ai = new AlphaBetaSearch();
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
						AlphaBetaSearch ai = new AlphaBetaSearch();
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
		final long startTime = System.currentTimeMillis();
		
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
		
		mapStarting.put(Concept.NumStartComponents.name(), numStartComponents / allStoredRNG.size());
		mapStarting.put(Concept.NumStartComponentsHand.name(), numStartComponentsHands / allStoredRNG.size());
		mapStarting.put(Concept.NumStartComponentsBoard.name(), numStartComponentsBoard / allStoredRNG.size());
		
//		System.out.println(Concept.NumStartComponents.name() + " = " + mapStarting.get(Concept.NumStartComponents.name()));
//		System.out.println(Concept.NumStartComponentsHand.name() + " = " + mapStarting.get(Concept.NumStartComponentsHand.name()));
//		System.out.println(Concept.NumStartComponentsBoard.name() + " = " + mapStarting.get(Concept.NumStartComponentsBoard.name()));
		
		final double allMilliSecond = System.currentTimeMillis() - startTime;
		final double allSeconds = allMilliSecond / 1000.0;
		final int seconds = (int) (allSeconds % 60.0);
		final int minutes = (int) ((allSeconds - seconds) / 60.0);
		final int milliSeconds = (int) (allMilliSecond - (seconds * 1000));
		System.out.println("Starting concepts done in " + minutes + " minutes " + seconds + " seconds " + milliSeconds + " ms.");
		
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
		final long startTime = System.currentTimeMillis();
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
			if(mapFrequency.get(concept.name()) != 0)
				System.out.println("concept = " + concept.name() + " frequency is " + new DecimalFormat("##.##").format(Double.valueOf(mapFrequency.get(concept.name()))*100) +"%.");
		}

		final double allMilliSecond = System.currentTimeMillis() - startTime;
		final double allSeconds = allMilliSecond / 1000.0;
		final int seconds = (int) (allSeconds % 60.0);
		final int minutes = (int) ((allSeconds - seconds) / 60.0);
		final int milliSeconds = (int) (allMilliSecond - (seconds * 1000));
		System.out.println("Frequency done in " + minutes + " minutes " + seconds + " seconds " + milliSeconds + " ms.");
		
		return mapFrequency;
	}
	
	//------------------------------Metrics CONCEPTS-----------------------------------------------------
	
	/**
	 * @param game The game.
	 * @param trials The trials.
	 * @param allStoredRNG The RNG for each trial.
	 * @return The map of playout concepts to the their values for the metric ones.
	 */
	private static Map<String, Double> metricsConcepts(final Game game, final List<Trial> trials, final List<RandomProviderState> allStoredRNG)
	{
		final Map<String, Double> playoutConceptValues = new HashMap<String, Double>();
		// We get the values of the metrics.
		final long startTime = System.currentTimeMillis();
		final Trial[] trialsMetrics = new Trial[trials.size()];
		final RandomProviderState[] rngTrials = new RandomProviderState[trials.size()];
		for(int i = 0 ; i < trials.size();i++)
		{
			trialsMetrics[i] = trials.get(i);
			rngTrials[i] = allStoredRNG.get(i);
		}
		
		// We add all the metrics corresponding to a concept to the returned map.
		final List<Metric> metrics = new Evaluation().conceptMetrics();
		for(final Metric metric: metrics)
			if(metric.concept() != null)
			{
				double metricValue = metric.apply(game, trialsMetrics, rngTrials);
				metricValue = (Math.abs(metricValue) < Constants.EPSILON) ? 0 : metricValue;
				playoutConceptValues.put(metric.concept().name(),  metricValue);
			}

		final double allMilliSecond = System.currentTimeMillis() - startTime;
		final double allSeconds = allMilliSecond / 1000.0;
		final int seconds = (int) (allSeconds % 60.0);
		final int minutes = (int) ((allSeconds - seconds) / 60.0);
		final int milliSeconds = (int) (allMilliSecond - (seconds * 1000));
		System.out.println("Metrics done in " + minutes + " minutes " + seconds + " seconds " + milliSeconds + " ms.");
		
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
		final long startTime = System.currentTimeMillis();
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);

		// Warming up
		long stopAt = 0L;
		long start = System.nanoTime();
		final double warmingUpSecs = 10;
		final double measureSecs = 30;
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
		playoutConceptValues.put(Concept.PlayoutsPerSecond.name(), rate);
		playoutConceptValues.put(Concept.MovesPerSecond.name(), rateMove);

		final double allSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
		final int seconds = (int) (allSeconds % 60.0);
		final int minutes = (int) ((allSeconds - seconds) / 60.0);
		System.out.println("p/s = " + rate);
		System.out.println("m/s = " + rateMove);
		System.out.println("Playouts/Moves per second estimation done in " + minutes + " minutes " + seconds + " seconds.");
		
		return playoutConceptValues;
	}
	
}
