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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import game.equipment.container.Container;
import game.match.Match;
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
import main.collections.ListUtils;
import main.options.Ruleset;
import manager.utils.game_logs.MatchRecord;
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
 *         Concepts.csv (Id, Name, Description, TypeId, DataTypeId,
 *         ComputationTypeId)
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

	/** The move limit to use in the trials used. */
	private static int moveLimit;

	/** The folder with the trials to use. */
	private static String folderTrials;

	/** The trials. */
	private static List<Trial> trials = new ArrayList<Trial>();

	// The RNGs of each trial.
	private static List<RandomProviderState> allStoredRNG = new ArrayList<RandomProviderState>();

	/**
	 * List of games for which the list of trials to use does not have to be more
	 * than a specific number to be able to compute in less than 4 days, due to the
	 * metric computation.
	 */
	private static List<String> lessTrialsGames = new ArrayList<String>();

	/** The limit of trials to use for some games too slow to compute. */
	private static final int smallLimitTrials = 30;
	
	/**
	 * List of games for which the list of trials to use does not have to be more
	 * than an even lower specific number to be able to compute in less than 4 days, due to the
	 * metrics.
	 */
	private static List<String> evenLessTrialsGames = new ArrayList<String>();
	
	/** The limit of trials to use for some games even slower to compute. */
	private static final int smallestLimitTrials = 1;

	// -------------------------------------------------------------------------

	public static void main(final String[] args)
	{
		// Store the games which needs less trials.
		lessTrialsGames.add("Russian Fortress Chess");
		lessTrialsGames.add("Puhulmutu");
		lessTrialsGames.add("Ludus Latrunculorum");
		lessTrialsGames.add("Poprad Game");
		lessTrialsGames.add("Unashogi");
		lessTrialsGames.add("Taikyoku Shogi");
		lessTrialsGames.add("Tai Shogi");
		lessTrialsGames.add("Pagade Kayi Ata (Sixteen-handed)");
		lessTrialsGames.add("Chex");
		lessTrialsGames.add("Poprad Game");
		lessTrialsGames.add("Backgammon"); // Mostly for smart agent (AB), the playouts are too long
		lessTrialsGames.add("Buffa de Baldrac"); // Mostly for smart agent (AB), the playouts are too long
		lessTrialsGames.add("Portes"); // Mostly for smart agent (AB), the playouts are too long
		lessTrialsGames.add("Shatranj al-Kabir"); // Mostly for smart agent (AB), the playouts are too long
		
		// Really slow games (included deduc puzzles because the trials always reach the move limit....)
		evenLessTrialsGames.add("Kriegsspiel");
		evenLessTrialsGames.add("Anti-Knight Sudoku");
		evenLessTrialsGames.add("Fill A Pix");
		evenLessTrialsGames.add("Futoshiki");
		evenLessTrialsGames.add("Hoshi");
		evenLessTrialsGames.add("Kakuro");
		evenLessTrialsGames.add("Killer Sudoku");
		evenLessTrialsGames.add("Latin Square");
		evenLessTrialsGames.add("Magic Hexagon");
		evenLessTrialsGames.add("Magic Square");
		evenLessTrialsGames.add("N Queens");
		evenLessTrialsGames.add("Samurai Sudoku");
		evenLessTrialsGames.add("Slitherlink");
		evenLessTrialsGames.add("Squaro");
		evenLessTrialsGames.add("Sudoku");
		evenLessTrialsGames.add("Sudoku Mine");
		evenLessTrialsGames.add("Sudoku X");
		evenLessTrialsGames.add("Sujiken");
		evenLessTrialsGames.add("Takuzu");
		evenLessTrialsGames.add("Tridoku");

		final Evaluation evaluation = new Evaluation();
		int numPlayouts = args.length == 0 ? 0 : Integer.parseInt(args[0]);
		final double timeLimit = args.length < 2 ? 0 : Double.parseDouble(args[1]);
		final double thinkingTime = args.length < 3 ? 1 : Double.parseDouble(args[2]);
		moveLimit = args.length < 4 ? Constants.DEFAULT_MOVES_LIMIT : Integer.parseInt(args[3]);
		final String agentName = args.length < 5 ? "Random" : args[4];
		folderTrials = args.length < 6 ? "" : args[5];
		final String gameName = args.length < 7 ? "" : args[6];
		final String rulesetName = args.length < 8 ? "" : args[7];
		final String agentName2 = args.length < 9 ? "" : args[8];

		if (gameName.isEmpty())
		{
			exportConceptCSV();
			exportConceptTypeCSV();
			exportConceptDataTypeCSV();
			exportConceptComputationTypeCSV();
			exportConceptPurposeCSV();
			exportConceptConceptPurposesCSV();
		}

		if (evenLessTrialsGames.contains(gameName) && numPlayouts > smallestLimitTrials)
			numPlayouts = smallestLimitTrials;
		else if (lessTrialsGames.contains(gameName) && numPlayouts > smallLimitTrials)
			numPlayouts = smallLimitTrials;

		exportRulesetConceptsCSV(evaluation, numPlayouts, timeLimit, thinkingTime, agentName, gameName, rulesetName, agentName2);
	}

	// -------------------------------------------------------------------------

	/**
	 * To create Concepts.csv (Id, Name, Description, TypeId, DataTypeId)
	 */
	public static void exportConceptCSV()
	{
		List<String> toNotShowOnWebsite = new ArrayList<String>();
		toNotShowOnWebsite.add("Properties");
		toNotShowOnWebsite.add("Format");
		toNotShowOnWebsite.add("Time");
		toNotShowOnWebsite.add("Turns");
		toNotShowOnWebsite.add("Players");
		toNotShowOnWebsite.add("Equipment");
		toNotShowOnWebsite.add("Board");
		toNotShowOnWebsite.add("Container");
		toNotShowOnWebsite.add("Component");
		toNotShowOnWebsite.add("Rules");
		toNotShowOnWebsite.add("Play");
		toNotShowOnWebsite.add("Efficiency");
		toNotShowOnWebsite.add("Implementation");
		toNotShowOnWebsite.add("Visual");
		toNotShowOnWebsite.add("Style");
		toNotShowOnWebsite.add("Math");
		toNotShowOnWebsite.add("Behaviour");
		
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
				lineToWrite.add(toNotShowOnWebsite.contains(concept.name()) ? "0" : "1");
				writer.println(StringRoutines.join(",", lineToWrite));
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		System.out.println("Done.");
	}

	// -------------------------------------------------------------------------

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

	// -------------------------------------------------------------------------

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

	// -------------------------------------------------------------------------

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

	// -------------------------------------------------------------------------

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

	// -------------------------------------------------------------------------

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

	// -------------------------------------------------------------------------

	/**
	 * To create RulesetConcepts.csv (Id, RulesetId, ConceptId, Value)
	 * 
	 * @param numPlayouts     The maximum number of playout.
	 * @param timeLimit       The maximum time to compute the playouts concepts.
	 * @param thinkingTime    The maximum time to take a decision per move.
	 * @param agentName       The name of the agent to use for the playout concepts
	 * @param name            The name of the game.
	 * @param rulesetExpected The name of the ruleset of the game.
	 * @param agentName2	  The name for a different second agent (if not empty string)
	 */
	public static void exportRulesetConceptsCSV
	(
		final Evaluation evaluation, 
		final int numPlayouts,
		final double timeLimit, 
		final double thinkingTime, 
		final String agentName, 
		final String name,
		final String rulesetExpected, 
		final String agentName2
	)
	{
		final List<Concept> ignoredConcepts = new ArrayList<Concept>();
		ignoredConcepts.add(Concept.Behaviour);
		ignoredConcepts.add(Concept.StateRepetition);
		ignoredConcepts.add(Concept.Duration);
		ignoredConcepts.add(Concept.Complexity);
		ignoredConcepts.add(Concept.BoardCoverage);
		ignoredConcepts.add(Concept.GameOutcome);
		ignoredConcepts.add(Concept.StateEvaluation);
		ignoredConcepts.add(Concept.Clarity);
		ignoredConcepts.add(Concept.Decisiveness);
		ignoredConcepts.add(Concept.Drama);
		ignoredConcepts.add(Concept.MoveEvaluation);
		ignoredConcepts.add(Concept.StateEvaluationDifference);
		ignoredConcepts.add(Concept.BoardSitesOccupied);
		ignoredConcepts.add(Concept.BranchingFactor);
		ignoredConcepts.add(Concept.DecisionFactor);
		ignoredConcepts.add(Concept.MoveDistance);
		ignoredConcepts.add(Concept.PieceNumber);
		ignoredConcepts.add(Concept.ScoreDifference);
		
		final List<String> games = new ArrayList<String>();
		final List<String> rulesets = new ArrayList<String>();
		final TIntArrayList ids = new TIntArrayList();
		
		// Get the ids of the rulesets.
		try (final InputStream in = ExportDbCsvConcepts.class.getResourceAsStream(GAME_RULESET_PATH);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));)
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
		final String outputRulesetConcepts = rulesetExpected.isEmpty() ? "RulesetConcepts" + fileName + ".csv"
				: "RulesetConcepts" + fileName + "-" + rulesetExpected.substring(8) + ".csv";
		System.out.println("Writing " + outputRulesetConcepts);
		
		// Do nothing if the files already exist.
		final File file = new File(outputRulesetConcepts);
		if(file.exists())
			return;
		
		// Computation of the concepts
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

			// Check only the games wanted
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

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("reconstruction/pending/"))
					continue;
				
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("reconstruction/validation/"))
					continue;

				if (!name.isEmpty() && !gameName.substring(1).equals(name.replaceAll(Pattern.quote("\\"), "/")))
					continue;

				final Game game = GameLoader.loadGameFromName(gameName);
				game.setMaxMoveLimit(moveLimit);
				game.start(new Context(game, new Trial(game)));

				System.out.println("Loading game: " + game.name());

				final List<Ruleset> rulesetsInGame = game.description().rulesets();
				
				// Code for games with many rulesets
				if (rulesetsInGame != null && !rulesetsInGame.isEmpty()) 
				{
					for (int rs = 0; rs < rulesetsInGame.size(); rs++)
					{
						final Ruleset ruleset = rulesetsInGame.get(rs);

						// We check if we want a specific ruleset.
						if (!rulesetExpected.isEmpty() && !rulesetExpected.equals(ruleset.heading()))
							continue;

						if (!ruleset.optionSettings().isEmpty() && !ruleset.heading().contains("Incomplete")) // We check if the ruleset is implemented.
						{
							final Game rulesetGame = GameLoader.loadGameFromName(gameName, ruleset.optionSettings());
							rulesetGame.setMaxMoveLimit(moveLimit);

							System.out.println("Loading ruleset: " + rulesetGame.getRuleset().heading());
							final Map<String, Double> playoutConcepts = (numPlayouts == 0)
									? new HashMap<String, Double>()
									: playoutsMetrics(rulesetGame, evaluation, numPlayouts, timeLimit, thinkingTime, agentName, agentName2);
							
							final int idRuleset = IdRuleset.get(rulesetGame);
							final BitSet concepts = rulesetGame.booleanConcepts();
							final Map<Integer,String> nonBooleanConceptsValues = rulesetGame.nonBooleanConcepts();
							
							// Boolean concepts
							for (final Concept concept : booleanConcepts)
							{
								final List<String> lineToWrite = new ArrayList<String>();
								lineToWrite.add(id + ""); // id
								lineToWrite.add(idRuleset + ""); // id ruleset
								lineToWrite.add(concept.id() + ""); // id concept
								if(ignoredConcepts.contains(concept))
									lineToWrite.add("NULL");
								else if (concepts.get(concept.id()))
									lineToWrite.add("\"1\"");
								else
									lineToWrite.add("\"0\"");
								writer.println(StringRoutines.join(",", lineToWrite));
								id++;
							}
							
							System.out.println("NON BOOLEAN CONCEPTS");
							// Non Boolean Concepts
							for (final Concept concept : nonBooleanConcepts)
							{
								if (concept.computationType().equals(ConceptComputationType.Compilation))
								{
									final List<String> lineToWrite = new ArrayList<String>();
									lineToWrite.add(id + "");
									lineToWrite.add(idRuleset + "");
									lineToWrite.add(concept.id() + "");
									lineToWrite.add(
											"\"" + nonBooleanConceptsValues.get(Integer.valueOf(concept.id())) + "\"");
									writer.println(StringRoutines.join(",", lineToWrite));
									id++;
								}
								else
								{
									final String conceptName = concept.name();
									if (conceptName.indexOf("Frequency") == Constants.UNDEFINED) // Non Frequency concepts added to the csv.
									{
										final double value = playoutConcepts.get(concept.name()) == null ? Constants.UNDEFINED
												: playoutConcepts.get(concept.name()).doubleValue();
										final List<String> lineToWrite = new ArrayList<String>();
										lineToWrite.add(id + "");
										lineToWrite.add(idRuleset + "");
										lineToWrite.add(concept.id() + "");
										lineToWrite.add(value == Constants.UNDEFINED ? "NULL"
												: "\"" + new DecimalFormat("##.##").format(value) + "\""); // the value of the metric
										writer.println(StringRoutines.join(",", lineToWrite));
										id++;
									}
									else // Frequency concepts added to the csv.
									{
										final String correspondingBooleanConceptName = conceptName.substring(0, conceptName.indexOf("Frequency"));
										for (final Concept correspondingConcept : booleanConcepts)
										{
											if (correspondingConcept.name().equals(correspondingBooleanConceptName))
											{
												final List<String> lineToWrite = new ArrayList<String>();
												lineToWrite.add(id + "");
												lineToWrite.add(idRuleset + "");
												lineToWrite.add(concept.id() + "");
												final double frequency = playoutConcepts
														.get(correspondingConcept.name()) == null ? Constants.UNDEFINED
																: playoutConcepts.get(correspondingConcept.name()).doubleValue();
												if (frequency > 0)
													System.out.println(concept + " = " + (frequency * 100) + "%");
												lineToWrite.add((frequency > 0 ? "\"" + new DecimalFormat("##.##").format(frequency) + "\"" : "0") + ""); // the frequency
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
				else // Code for games with only a single ruleset.
				{
					final Map<String, Double> playoutConcepts = (numPlayouts == 0) ? new HashMap<String, Double>()
							: playoutsMetrics(game, evaluation, numPlayouts, timeLimit, thinkingTime, agentName, agentName2);

					final int idRuleset = IdRuleset.get(game);
					final BitSet concepts = game.booleanConcepts();
					
					for (final Concept concept : booleanConcepts)
					{
						final List<String> lineToWrite = new ArrayList<String>();
						lineToWrite.add(id + "");
						lineToWrite.add(idRuleset + "");
						lineToWrite.add(concept.id() + "");
						if(ignoredConcepts.contains(concept))
							lineToWrite.add("NULL");
						else if (concepts.get(concept.id()))
							lineToWrite.add("\"1\"");
						else
							lineToWrite.add("\"0\"");
						writer.println(StringRoutines.join(",", lineToWrite));
						id++;
					}

					for (final Concept concept : nonBooleanConcepts)
					{
						if (concept.computationType().equals(ConceptComputationType.Compilation))
						{
							final List<String> lineToWrite = new ArrayList<String>();
							lineToWrite.add(id + "");
							lineToWrite.add(idRuleset + "");
							lineToWrite.add(concept.id() + "");
							lineToWrite.add(
									"\"" + game.nonBooleanConcepts().get(Integer.valueOf(concept.id())) + "\"");
							writer.println(StringRoutines.join(",", lineToWrite));
							id++;
						}
						else
						{
							final String conceptName = concept.name();
							if (conceptName.indexOf("Frequency") == Constants.UNDEFINED) // Non Frequency concepts added to the csv.
							{
								final double value = playoutConcepts.get(conceptName) == null ? Constants.UNDEFINED
										: playoutConcepts.get(conceptName).doubleValue();
								final List<String> lineToWrite = new ArrayList<String>();
								lineToWrite.add(id + "");
								lineToWrite.add(idRuleset + "");
								lineToWrite.add(concept.id() + "");
								lineToWrite.add(value == Constants.UNDEFINED ? "NULL"
										: "\"" + new DecimalFormat("##.##").format(value) + "\""); // the value of the metric
								writer.println(StringRoutines.join(",", lineToWrite));
								id++;
//								if(value != 0)
//									System.out.println("metric: " + concept + " value is "  + value);
							}
							else // Frequency concepts added to the csv.
							{
								final String correspondingBooleanConceptName = conceptName.substring(0, conceptName.indexOf("Frequency"));
								for (final Concept correspondingConcept : booleanConcepts)
								{
									if (correspondingConcept.name().equals(correspondingBooleanConceptName))
									{
										final List<String> lineToWrite = new ArrayList<String>();
										lineToWrite.add(id + "");
										lineToWrite.add(idRuleset + "");
										lineToWrite.add(concept.id() + "");
										final double frequency = playoutConcepts
												.get(correspondingConcept.name()) == null ? Constants.UNDEFINED
														: playoutConcepts.get(correspondingConcept.name()).doubleValue();
										if(frequency > 0)
											System.out.println(concept + " = " + (frequency * 100) +"%");
										lineToWrite.add((frequency > 0 ? "\"" + new DecimalFormat("##.##").format(frequency) + "\"" : "0") + ""); // the frequency
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

	// ------------------------------PLAYOUT CONCEPTS-----------------------------------------------------

	/**
	 * @param game         The game
	 * @param playoutLimit The number of playouts to run.
	 * @param timeLimit    The maximum time to use.
	 * @param thinkingTime The maximum time to take a decision at each state.
	 * @return The frequency of all the boolean concepts in the number of playouts
	 *         set in entry
	 */
	private static Map<String, Double> playoutsMetrics(final Game game, final Evaluation evaluation,
			final int playoutLimit, final double timeLimit, final double thinkingTime, final String agentName,
			final String agentName2)
	{
		final long startTime = System.currentTimeMillis();

		// Used to return the frequency (of each playout concept).
		final Map<String, Double> mapFrequency = new HashMap<String, Double>();

		// For now I exclude the matches, but can be included too after. The deduc puzzle
		// will stay excluded.
		if (//game.hasSubgames() || game.isDeductionPuzzle() || game.isSimulationMoveGame())
				 game.name().contains("Kriegsspiel"))
		{
			// We add all the default metrics values corresponding to a concept to the
			// returned map.
			final List<Metric> metrics = new Evaluation().conceptMetrics();
			for (final Metric metric : metrics)
				if (metric.concept() != null)
					mapFrequency.put(metric.concept().name(), null);
			
			// Computation of the p/s and m/s
			mapFrequency.putAll(playoutsEstimationConcepts(game));
			
			return mapFrequency;
		}

		// We run the playouts needed for the computation.

		if (folderTrials.isEmpty())
		{
			// Create list of AI objects to be used in all trials
			final List<AI> aisAllTrials = chooseAI(game, agentName, agentName2, 0);

			for (final AI ai : aisAllTrials)
				if (ai != null)
					ai.setMaxSecondsPerMove(thinkingTime);
			
			final int numPlayers = game.players().count();
			List<TIntArrayList> aiListPermutations = new ArrayList<TIntArrayList>();
			if (numPlayers <= 5)
			{
				// Compute all possible permutations of indices for the list of AIs
				aiListPermutations = ListUtils.generatePermutations(
						TIntArrayList.wrap(IntStream.range(0, numPlayers).toArray()));

				Collections.shuffle(aiListPermutations);
			}
			else
			{
				// Randomly generate some permutations of indices for the list of AIs
				aiListPermutations = ListUtils.samplePermutations(
						TIntArrayList.wrap(IntStream.range(0, numPlayers).toArray()), 120);
			}
			
			int playoutsDone = 0;
			for (int indexPlayout = 0; indexPlayout < playoutLimit; indexPlayout++)
			{
//				final List<AI> ais = chooseAI(game, agentName, agentName2, indexPlayout);
//
//				for (final AI ai : ais)
//					if (ai != null)
//						ai.setMaxSecondsPerMove(thinkingTime);
				
				// Create re-ordered list of AIs for this particular playout
				final List<AI> ais = new ArrayList<AI>();
				ais.add(null);
				final int currentAIsPermutation = indexPlayout % aiListPermutations.size();
				final TIntArrayList currentPlayersPermutation = aiListPermutations.get(currentAIsPermutation);
				for (int i = 0; i < currentPlayersPermutation.size(); ++i)
				{
					ais.add
					(
						aisAllTrials.get(currentPlayersPermutation.getQuick(i) % aisAllTrials.size())
					);
				}

				final Context context = new Context(game, new Trial(game));
				allStoredRNG.add(context.rng().saveState());
				final Trial trial = context.trial();
				game.start(context);

				// Init the ais.
				for (int p = 1; p <= game.players().count(); ++p)
					ais.get(p).initAI(game, p);
				final Model model = context.model();

				while (!trial.over())
					model.startNewStep(context, ais, thinkingTime);

				trials.add(trial);
				playoutsDone++;

				for (int p = 1; p <= game.players().count(); ++p)
					ais.get(p).closeAI();

				final double currentTimeUsed = (System.currentTimeMillis() - startTime) / 1000.0;
				if (currentTimeUsed > timeLimit) // We stop if the limit of time is reached.
					break;
			}

			final double allSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
			final int seconds = (int) (allSeconds % 60.0);
			final int minutes = (int) ((allSeconds - seconds) / 60.0);
			System.out.println(
					"Playouts done in " + minutes + " minutes " + seconds + " seconds. " + playoutsDone + " playouts.");
		}
		else
		{
			getTrials(game);
		}

		// We get the values of the frequencies.
		mapFrequency.putAll(frequencyConcepts(game));
		
		final List<Concept> reconstructionConcepts = new ArrayList<Concept>();
		reconstructionConcepts.add(Concept.DurationTurns);
		reconstructionConcepts.add(Concept.DurationTurnsStdDev);
		reconstructionConcepts.add(Concept.DurationTurnsNotTimeouts);
		reconstructionConcepts.add(Concept.DecisionMoves);
		reconstructionConcepts.add(Concept.BoardCoverageDefault);
		reconstructionConcepts.add(Concept.AdvantageP1);
		reconstructionConcepts.add(Concept.Balance);
		reconstructionConcepts.add(Concept.Completion);
		reconstructionConcepts.add(Concept.Timeouts);
		reconstructionConcepts.add(Concept.Drawishness);
		reconstructionConcepts.add(Concept.PieceNumberAverage);
		reconstructionConcepts.add(Concept.BoardSitesOccupiedAverage);
		reconstructionConcepts.add(Concept.BranchingFactorAverage);
		reconstructionConcepts.add(Concept.DecisionFactorAverage);
		
		// We get the values of the metrics.
		mapFrequency.putAll(metricsConcepts(game, evaluation, reconstructionConcepts));

		// We get the values of the starting concepts.
		mapFrequency.putAll(startsConcepts(game));

		// Computation of the p/s and m/s
		mapFrequency.putAll(playoutsEstimationConcepts(game));
		
		return mapFrequency;
	}

	/**
	 * @param game The game.
	 */
	private static void getTrials(final Game game)
	{
		final File currentFolder = new File(".");
		final File folder = new File(currentFolder.getAbsolutePath() + folderTrials);
		final String gameName = game.name();
		final String rulesetName = game.getRuleset() == null ? "" : game.getRuleset().heading();

//		System.out.println("GAME NAME = " + gameName);
//		System.out.println("RULESET NAME = " + rulesetName);

		String trialFolderPath = folder + "/" + gameName;
		if (!rulesetName.isEmpty())
			trialFolderPath += File.separator + rulesetName.replace("/", "_");

		final File trialFolder = new File(trialFolderPath);

		if (trialFolder.exists())
			System.out.println("TRIALS FOLDER EXIST");
		else
			System.out.println("DO NOT FOUND IT - Path is " + trialFolder);

		int limit = Constants.UNDEFINED;
		if (evenLessTrialsGames.contains(gameName))
			limit = smallestLimitTrials;
		else if (lessTrialsGames.contains(gameName))
				limit = smallLimitTrials;

		int num = 0;
		for (final File trialFile : trialFolder.listFiles())
		{
			System.out.println(trialFile.getName());
			if(trialFile.getName().contains(".txt"))
			{
				MatchRecord loadedRecord;
				try
				{
					loadedRecord = MatchRecord.loadMatchRecordFromTextFile(trialFile, game);
					final Trial loadedTrial = loadedRecord.trial();
					trials.add(loadedTrial);
					allStoredRNG.add(loadedRecord.rngState());
					num++;
					if (num == limit)
						break;
				}
				catch (final FileNotFoundException e)
				{
					e.printStackTrace();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param game         The game.
	 * @param agentName    The name of the agent.
	 * @param agentName2   The name of the second agent (can be empty string if not used).
	 * @param indexPlayout The index of the playout.
	 * @return The list of AIs to play that playout.
	 */
	private static List<AI> chooseAI(final Game game, final String agentName, final String agentName2, final int indexPlayout)
	{
		final List<AI> ais = new ArrayList<AI>();
		//ais.add(null);
		
		if (agentName2.length() > 0)
		{
			// Special case where we have provided two different names
			if (game.players().count() == 2)
			{
				ais.add(AIFactory.createAI(agentName));
				ais.add(AIFactory.createAI(agentName2));
				return ais;
			}
			else
			{
				System.err.println("Provided 2 agent names, but not a 2-player game!");
			}
		}

		// Continue with Eric's original implementation
		for (int p = 1; p <= game.players().count(); ++p)
		{
			if (agentName.equals("UCT"))
			{
				final AI ai = AIFactory.createAI("UCT");
				if (ai.supportsGame(game))
				{
					ais.add(ai);
				}
				else
				{
					ais.add(new utils.RandomAI());
				}
			}
			else if (agentName.equals("Alpha-Beta"))
			{
				AI ai = AIFactory.createAI("Alpha-Beta");
				if (ai.supportsGame(game))
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
			else if (agentName.equals("Alpha-Beta-UCT")) // AB/UCT/AB/UCT/...
			{
				if (indexPlayout % 2 == 0)
				{
					if (p % 2 == 1)
					{
						AI ai = AIFactory.createAI("Alpha-Beta");
						if (ai.supportsGame(game))
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
						if (ai.supportsGame(game))
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
					if (p % 2 == 1)
					{
						final AI ai = AIFactory.createAI("UCT");
						if (ai.supportsGame(game))
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
						if (ai.supportsGame(game))
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
			else if (agentName.equals("ABONEPLY")) // AB/ONEPLY/AB/ONEPLY/...
			{
				if (indexPlayout % 2 == 0)
				{
					if (p % 2 == 1)
					{
						AI ai = AIFactory.createAI("Alpha-Beta");
						if (ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else if (AIFactory.createAI("One-Ply (No Heuristic)").supportsGame(game))
						{
							ai = AIFactory.createAI("One-Ply (No Heuristic)");
							ais.add(ai);
						}
						else
						{
							ais.add(new utils.RandomAI());
						}
					}
					else
					{
						final AI ai = AIFactory.createAI("One-Ply (No Heuristic)");
						if (ai.supportsGame(game))
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
					if (p % 2 == 1)
					{
						final AI ai = AIFactory.createAI("One-Ply (No Heuristic)");
						if (ai.supportsGame(game))
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
						if (ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else if (AIFactory.createAI("One-Ply (No Heuristic)").supportsGame(game))
						{
							ai = AIFactory.createAI("One-Ply (No Heuristic)");
							ais.add(ai);
						}
						else
						{
							ais.add(new utils.RandomAI());
						}
					}
				}
			}
			else if (agentName.equals("UCTONEPLY")) // UCT/ONEPLY/UCT/ONEPLY/...
			{
				if (indexPlayout % 2 == 0)
				{
					if (p % 2 == 1)
					{
						AI ai = AIFactory.createAI("UCT");
						if (ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else if (AIFactory.createAI("One-Ply (No Heuristic)").supportsGame(game))
						{
							ai = AIFactory.createAI("One-Ply (No Heuristic)");
							ais.add(ai);
						}
						else
						{
							ais.add(new utils.RandomAI());
						}
					}
					else
					{
						final AI ai = AIFactory.createAI("One-Ply (No Heuristic)");
						if (ai.supportsGame(game))
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
					if (p % 2 == 1)
					{
						final AI ai = AIFactory.createAI("One-Ply (No Heuristic)");
						if (ai.supportsGame(game))
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
						AI ai = AIFactory.createAI("UCT");
						if (ai.supportsGame(game))
						{
							ais.add(ai);
						}
						else if (AIFactory.createAI("One-Ply (No Heuristic)").supportsGame(game))
						{
							ai = AIFactory.createAI("One-Ply (No Heuristic)");
							ais.add(ai);
						}
						else
						{
							ais.add(new utils.RandomAI());
						}
					}
				}
			}
			else if (agentName.equals("AB-Odd-Even")) // Alternating between AB Odd and AB Even
			{
				if (indexPlayout % 2 == 0)
				{
					if (p % 2 == 1)
					{
						AI ai = new AlphaBetaSearch();
						((AlphaBetaSearch) ai).setAllowedSearchDepths(AllowedSearchDepths.Odd);
						if (ai.supportsGame(game))
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
						if (ai.supportsGame(game))
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
					if (p % 2 == 1)
					{
						final AlphaBetaSearch ai = new AlphaBetaSearch();
						ai.setAllowedSearchDepths(AllowedSearchDepths.Even);
						if (ai.supportsGame(game))
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
						((AlphaBetaSearch) ai).setAllowedSearchDepths(AllowedSearchDepths.Odd);
						if (ai.supportsGame(game))
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
	 * @return The map of playout concepts to the their values for the starting
	 *         ones.
	 */
	private static Map<String, Double> startsConcepts(final Game game)
	{
		final Map<String, Double> mapStarting = new HashMap<String, Double>();
		final long startTime = System.currentTimeMillis();

		final BitSet booleanConcepts = game.booleanConcepts();
		double numStartComponents = 0.0;
		double numStartComponentsHands = 0.0;
		double numStartComponentsBoard = 0.0;

		// Check for each initial state of the game.
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
							final int count = (game.hasSubgames() ? ((Match) game).instances()[0].getGame().isStacking() :  game.isStacking()) 
									? cs.sizeStack(cell, SiteType.Cell)
									: cs.count(cell, SiteType.Cell);
							numStartComponents += count;
							numStartComponentsBoard += count;
						}

					if (booleanConcepts.get(Concept.Vertex.id()))
						for (int vertex = 0; vertex < cont.topology().vertices().size(); vertex++)
						{
							final int count = (game.hasSubgames() ? ((Match) game).instances()[0].getGame().isStacking() :  game.isStacking()) 
									? cs.sizeStack(vertex, SiteType.Vertex)
									: cs.count(vertex, SiteType.Vertex);
							numStartComponents += count;
							numStartComponentsBoard += count;
						}

					if (booleanConcepts.get(Concept.Edge.id()))
						for (int edge = 0; edge < cont.topology().edges().size(); edge++)
						{
							final int count = (game.hasSubgames() ? ((Match) game).instances()[0].getGame().isStacking() :  game.isStacking())  
									? cs.sizeStack(edge, SiteType.Edge)
									: cs.count(edge, SiteType.Edge);
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
							final int count = (game.hasSubgames() ? ((Match) game).instances()[0].getGame().isStacking() :  game.isStacking()) 
									? cs.sizeStack(cell, SiteType.Cell)
									: cs.count(cell, SiteType.Cell);
							numStartComponents += count;
							numStartComponentsHands += count;
						}
				}
			}
		}

		mapStarting.put(Concept.NumStartComponents.name(), Double.valueOf(numStartComponents / allStoredRNG.size()));
		mapStarting.put(Concept.NumStartComponentsHand.name(), Double.valueOf(numStartComponentsHands / allStoredRNG.size()));
		mapStarting.put(Concept.NumStartComponentsBoard.name(), Double.valueOf(numStartComponentsBoard / allStoredRNG.size()));

		mapStarting.put(Concept.NumStartComponentsPerPlayer.name(), Double.valueOf((numStartComponents / allStoredRNG.size()) / (game.players().count() == 0 ? 1 : game.players().count())));
		mapStarting.put(Concept.NumStartComponentsHandPerPlayer.name(), Double.valueOf((numStartComponentsHands / allStoredRNG.size()) / (game.players().count() == 0 ? 1 : game.players().count())));
		mapStarting.put(Concept.NumStartComponentsBoardPerPlayer.name(), Double.valueOf((numStartComponentsBoard / allStoredRNG.size()) / (game.players().count() == 0 ? 1 : game.players().count())));

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

	// ------------------------------Frequency CONCEPTS-----------------------------------------------------

	/**
	 * @param game         The game.
	 * @param trials       The trials.
	 * @param allStoredRNG The RNG for each trial.
	 * @return The map of playout concepts to the their values for the frequency
	 *         ones.
	 */
	private static Map<String, Double> frequencyConcepts(final Game game)
	{
		final Map<String, Double> mapFrequency = new HashMap<String, Double>();
		final long startTime = System.currentTimeMillis();
		// Frequencies of the moves.
		final TDoubleArrayList frequencyMoveConcepts = new TDoubleArrayList();

		// Frequencies returned by all the playouts.
		final TDoubleArrayList frequencyPlayouts = new TDoubleArrayList();
		for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
			frequencyPlayouts.add(0.0);

		// FOR THE MUSEUM GAME
//		final TIntArrayList edgesUsage = new TIntArrayList();	
//		for(int i = 0; i < game.board().topology().edges().size(); i++)
//			edgesUsage.add(0);
		
		for (int trialIndex = 0; trialIndex < trials.size(); trialIndex++)
		{
			final Trial trial = trials.get(trialIndex);
			final RandomProviderState rngState = allStoredRNG.get(trialIndex);

			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);

			// Frequencies returned by that playout.
			final TDoubleArrayList frequencyPlayout = new TDoubleArrayList();
			for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
				frequencyPlayout.add(0);

			// Run the playout.
			int turnWithMoves = 0;
			Context prevContext = null;
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				final Moves legalMoves = context.game().moves(context);
				final TIntArrayList frequencyTurn = new TIntArrayList();
				for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
					frequencyTurn.add(0);

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
							frequencyTurn.set(indexConcept, frequencyTurn.get(indexConcept) + 1);
					}
				}

				for (int j = 0; j < frequencyTurn.size(); j++)
					frequencyPlayout.set(j, frequencyPlayout.get(j)
							+ (numLegalMoves == 0 ? 0 : frequencyTurn.get(j) / numLegalMoves));

				// We keep the context before the ending state for the frequencies of the end
				// conditions.
				if (i == trial.numMoves() - 1)
					prevContext = new Context(context);

				// We go to the next move.
				context.game().apply(context, trial.getMove(i));
				
				// FOR THE MUSEUM GAME
				// To count the frequency/usage of each edge on the board.
//				final Move lastMove = context.trial().lastMove();
//				final int vertexFrom = lastMove.fromNonDecision();
//				// To not take in account moves coming from the hand.
//				if(vertexFrom < 0 || vertexFrom >= game.board().topology().vertices().size())
//					continue;
//				final int vertexTo = lastMove.toNonDecision();
//
//				for(int j = 0; j < game.board().topology().edges().size(); j++)
//				{
//					final Edge edge = game.board().topology().edges().get(j);
//					if((edge.vertices().get(0).index() == vertexFrom && edge.vertices().get(1).index() == vertexTo) ||
//							(edge.vertices().get(0).index() == vertexTo && edge.vertices().get(1).index() == vertexFrom))
//						edgesUsage.set(j, edgesUsage.get(j) + 1);
//				}

				// TO PRINT THE NUMBER OF PIECES PER TRIAL (this was for LL xp)
////				 int countPieces = 0;
////				 int countPiecesP1 = 0;
////				 int countPiecesP2 = 0;
////				 final ContainerState cs = context.containerState(0);
////				 final int numCells = context.topology().cells().size();
////				 for(int j = 0; j < numCells; j++)
////				 {
////					 if(cs.what(j, SiteType.Cell) != 0)
////					 countPieces++;
////					
////					 if(cs.what(j, SiteType.Cell) == 1)
////					 countPiecesP1++;
////					
////					 if(cs.what(j, SiteType.Cell) == 2)
////					 countPiecesP2++;
////				 }
////				
////				 System.out.println(countPieces+","+countPiecesP1+","+countPiecesP2);
			}
			
			// Compute avg for all the playouts.
			for (int j = 0; j < frequencyPlayout.size(); j++)
				frequencyPlayouts.set(j, frequencyPlayouts.get(j) + frequencyPlayout.get(j) / turnWithMoves);

			context.trial().lastMove().apply(prevContext, true);

			boolean noEndFound = true;

			if (context.rules().phases() != null)
			{
				final int mover = context.state().mover();
				final Phase endPhase = context.rules().phases()[context.state().currentPhase(mover)];
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

			final End endRule = context.rules().end();
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
		
		// FOR THE MUSEUM GAME
//		int totalEdgesUsage = 0;
//		for(int i = 0; i < edgesUsage.size(); i++)
//			totalEdgesUsage += edgesUsage.get(i);
//		
//		System.out.println("Total Moves on Edges = " + totalEdgesUsage);
//		for(int i = 0; i < edgesUsage.size(); i++)
//		{
//			final Edge edge = game.board().topology().edges().get(i);
//			final int vFrom = edge.vertices().get(0).index();
//			final int vTo = edge.vertices().get(1).index();
//			if(totalEdgesUsage == 0)
//				System.out.println("Edge " + i + "(" + vFrom + "-" + vTo + ")"+ " is used " + new DecimalFormat("##.##").format(0.0) + "% ("+edgesUsage.get(i) + " times)");
//			else
//				System.out.println("Edge " + i + "(" + vFrom + "-" + vTo + ")"+ " is used " + new DecimalFormat("##.##").format(Double.valueOf(((double)edgesUsage.get(i) / (double)totalEdgesUsage)*100.0)) + "% ("+edgesUsage.get(i) + " times)");
//		}
//		
//		final String outputEdgesResults = "EdgesResult" + game.name() + "-" + game.getRuleset().heading().substring(8) + ".csv";
//		try (final PrintWriter writer = new UnixPrintWriter(new File(outputEdgesResults), "UTF-8"))
//		{
//			for(int i = 0; i < edgesUsage.size(); i++)
//			{
//				if(totalEdgesUsage == 0)
//					writer.println(i + ","+ edgesUsage.get(i) + ","+ new DecimalFormat("##.##").format(0.0*100.0));
//				else
//					writer.println(i + ","+ edgesUsage.get(i) + ","+ new DecimalFormat("##.##").format(Double.valueOf(((double)edgesUsage.get(i) / (double)totalEdgesUsage)*100.0)));
//			}
//		}
//		catch (FileNotFoundException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		catch (UnsupportedEncodingException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// Compute avg frequency for the game.
		for (int i = 0; i < frequencyPlayouts.size(); i++)
			frequencyMoveConcepts.add(frequencyPlayouts.get(i) / trials.size());

		for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
		{
			final Concept concept = Concept.values()[indexConcept];
			mapFrequency.put(concept.name(), Double.valueOf(frequencyMoveConcepts.get(indexConcept)));
			if (mapFrequency.get(concept.name()).doubleValue() != 0)
			{
				final double perc= mapFrequency.get(concept.name()).doubleValue() * 100.0;
				System.out.println("concept = " + concept.name() + " frequency is "
						+ new DecimalFormat("##.##").format(perc) + "%.");
			}
		}

		final double allMilliSecond = System.currentTimeMillis() - startTime;
		final double allSeconds = allMilliSecond / 1000.0;
		final int seconds = (int) (allSeconds % 60.0);
		final int minutes = (int) ((allSeconds - seconds) / 60.0);
		final int milliSeconds = (int) (allMilliSecond - (seconds * 1000));
		System.out.println("Frequency done in " + minutes + " minutes " + seconds + " seconds " + milliSeconds + " ms.");

		return mapFrequency;
	}

	// ------------------------------Metrics CONCEPTS-----------------------------------------------------

	/**
	 * @param game         The game.
	 * @param trials       The trials.
	 * @param allStoredRNG The RNG for each trial.
	 * @return The map of playout concepts to the their values for the metric ones.
	 */
	private static Map<String, Double> metricsConcepts(final Game game, final Evaluation evaluation, final List<Concept> reconstructionConcepts)
	{
		final Map<String, Double> playoutConceptValues = new HashMap<String, Double>();
		// We get the values of the metrics.
		final long startTime = System.currentTimeMillis();
		final Trial[] trialsMetrics = new Trial[trials.size()];
		final RandomProviderState[] rngTrials = new RandomProviderState[trials.size()];
		for (int i = 0; i < trials.size(); i++)
		{
			trialsMetrics[i] = new Trial(trials.get(i));
			rngTrials[i] = allStoredRNG.get(i);
		}

		// We add all the metrics corresponding to a concept to the returned map.
		final List<Metric> metrics = new Evaluation().conceptMetrics();
		for (final Metric metric : metrics)
		{
			if (metric.concept() != null)
			{
				Double value;
				if(reconstructionConcepts.contains(metric.concept()))
				{
					value = metric.apply(game, evaluation, trialsMetrics, rngTrials);
				}
				else
					value = null; // If that's not a reconstruction metrics we put NULL for it.
					
					if(value == null)
						playoutConceptValues.put(metric.concept().name(), null);
					else
					{
						double metricValue = metric.apply(game, evaluation, trialsMetrics, rngTrials).doubleValue();
						metricValue = (Math.abs(metricValue) < Constants.EPSILON) ? 0 : metricValue;
						playoutConceptValues.put(metric.concept().name(), Double.valueOf(metricValue));
						if (metricValue != 0)
							System.out.println(metric.concept().name() + ": " + metricValue);
				}
			}
		}

		final double allMilliSecond = System.currentTimeMillis() - startTime;
		final double allSeconds = allMilliSecond / 1000.0;
		final int seconds = (int) (allSeconds % 60.0);
		final int minutes = (int) ((allSeconds - seconds) / 60.0);
		final int milliSeconds = (int) (allMilliSecond - (seconds * 1000));
		System.out.println("Metrics done in " + minutes + " minutes " + seconds + " seconds " + milliSeconds + " ms.");

		return playoutConceptValues;
	}

	// ------------------------------Playout Estimation CONCEPTS-----------------------------------------------------

	/**
	 * @param game The game.
	 * @return The map of playout concepts to the their values for the p/s and m/s
	 *         ones.
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
		final Random rng = new Random((long) game.name().hashCode() * 2077);

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

		final double allSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
		final int seconds = (int) (allSeconds % 60.0);
		final int minutes = (int) ((allSeconds - seconds) / 60.0);
		System.out.println("p/s = " + rate);
		System.out.println("m/s = " + rateMove);
		System.out.println(
				"Playouts/Moves per second estimation done in " + minutes + " minutes " + seconds + " seconds.");

		return playoutConceptValues;
	}

}
