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
import java.util.regex.Pattern;

import game.Game;
import game.rules.end.End;
import game.rules.end.EndRule;
import game.rules.phase.Phase;
import game.rules.play.moves.Moves;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.options.Ruleset;
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
import other.trial.Trial;
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
		final String gameName = args.length < 3 ? "" : args[2];

		if (gameName.isEmpty())
		{
			exportConceptCSV();
			exportConceptTypeCSV();
			exportConceptDataTypeCSV();
			exportConceptComputationTypeCSV();
			exportConceptPurposeCSV();
			exportConceptConceptPurposesCSV();
		}

		exportRulesetConceptsCSV(numPlayouts, timeLimit, gameName);
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
	 */
	public static void exportRulesetConceptsCSV(final int numPlayouts, final double timeLimit, final String name)
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
		final String outputRulesetConcepts = "RulesetConcepts" + fileName + ".csv";
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
				if (rulesetsInGame != null && !rulesetsInGame.isEmpty()) // Code for the only default ruleset
				{
					for (int rs = 0; rs < rulesetsInGame.size(); rs++)
					{
						final Ruleset ruleset = rulesetsInGame.get(rs);
						if (!ruleset.optionSettings().isEmpty()) // We check if the ruleset is implemented.
						{
							final Game rulesetGame = GameLoader.loadGameFromName(gameName, ruleset.optionSettings());

							System.out.println("Loading ruleset: " + rulesetGame.getRuleset().heading());
							final Map<String, Double> frequencyPlayouts = (numPlayouts == 0)
									? new HashMap<String, Double>()
									: frequency(rulesetGame, numPlayouts, timeLimit);

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
									//lineToWrite.add("-1"); // the frequency.
									writer.println(StringRoutines.join(",", lineToWrite));
									id++;
								}
								else
								{
									if(concept.type().equals(ConceptType.Metrics)) // Metrics concepts added to the csv.
									{
										System.out.println("TODO metric: " + concept);
									}
									else // Frequency concepts added to the csv.
									{
										final String conceptName = concept.name();
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
														(frequency > 0 ? new DecimalFormat("##.##").format(frequency) + "" : "-1") + ""); // the
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
				else // Code for a specific ruleset
				{
					final Map<String, Double> frequencyPlayouts = (numPlayouts == 0) ? new HashMap<String, Double>()
							: frequency(game, numPlayouts, timeLimit);

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
								//lineToWrite.add("-1"); // the frequency.
								writer.println(StringRoutines.join(",", lineToWrite));
								id++;
							}
						}
						else
						{
							if(concept.type().equals(ConceptType.Metrics)) // Metrics concepts added to the csv.
							{
								final double value = frequencyPlayouts.get(concept.name()) == null ? 0
										: frequencyPlayouts.get(concept.name()).doubleValue();
								System.out.println("TODO metric: " + concept + " value is "  + value);
							}
							else // Frequency concepts added to the csv.
							{
								final String conceptName = concept.name();
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
//										if(frequency > 0)
//											System.out.println(concept + " = " + (frequency * 100) +"%");
										lineToWrite.add(
												(frequency > 0 ? new DecimalFormat("##.##").format(frequency) + "" : "-1") + ""); // the
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

	/**
	 * @param game        The game
	 * @param numPlayouts The number of playouts to run.
	 * @return The frequency of all the boolean concepts in the number of playouts
	 *         set in entry
	 */
	private static Map<String, Double> frequency(final Game game, final int playoutLimit, final double timeLimit)
	{
		final long startTime = System.currentTimeMillis();

		// Used to return the frequency (of each playout concept).
		final Map<String, Double> mapFrequency = new HashMap<String, Double>();
		
		// Used to return the value of each metric.
		final Map<String, Double> mapMetrics = new HashMap<String, Double>();
		for(Concept metricConcept: Concept.values())
			if(metricConcept.type().equals(ConceptType.Metrics))
				mapMetrics.put(metricConcept.name(), 0.0);

		// For now I exclude the matchs, but can be included too after. The deduc puzzle
		// will stay excluded.
		if (game.hasSubgames() || game.isDeductionPuzzle() || game.isSimulationMoveGame()
				|| game.name().contains("Trax") || game.name().contains("Kriegsspiel"))
			return mapFrequency;

		// Frequencies of the moves.
		final TDoubleArrayList frequencyMoveConcepts = new TDoubleArrayList();

		// Frequencies returned by all the playouts.
		final TDoubleArrayList frenquencyPlayouts = new TDoubleArrayList();
		for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
			frenquencyPlayouts.add(0.0);
		
		// Init metric returned by all playouts.
		final Map<String, Double> mapMetricsPlayouts = new HashMap<String, Double>();
		for(Concept metricConcept: Concept.values())
			if(metricConcept.type().equals(ConceptType.Metrics))
				mapMetricsPlayouts.put(metricConcept.name(), 0.0);
		
		int playoutsDone = 0;
		for (int i = 0; i < playoutLimit; i++)
		{
			final List<AI> ais = new ArrayList<AI>();
			ais.add(null);
			for (int p = 1; p <= game.players().count(); ++p)
				ais.add(new utils.RandomAI());

			final Context context = new Context(game, new Trial(game));
			final Trial trial = context.trial();
			game.start(context);

			// Init the ais (here random).
			for (int p = 1; p <= game.players().count(); ++p)
				ais.get(p).initAI(game, p);
			final Model model = context.model();

			// Frequencies returned by that playout.
			final TDoubleArrayList frenquencyPlayout = new TDoubleArrayList();
			for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
				frenquencyPlayout.add(0);
			
			final Map<String, Double> mapMetricsPlayout = new HashMap<String, Double>();
			for(Concept metricConcept: Concept.values())
				if(metricConcept.type().equals(ConceptType.Metrics))
					mapMetricsPlayout.put(metricConcept.name(), 0.0);
			
			// Run the playout.
			int turnWithMoves = 0;
			Context prevContext = null;
			while (!trial.over())
			{
				final int mover = context.state().mover();
				final Phase currPhase = game.rules().phases()[context.state().currentPhase(mover)];
				final Moves moves = currPhase.play().moves().eval(context);

				final TIntArrayList frenquencyTurn = new TIntArrayList();
				for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
					frenquencyTurn.add(0);
				
				final double numLegalMoves = moves.moves().size();
				if (numLegalMoves > 0)
					turnWithMoves++;

				for (final Move legalMove : moves.moves())
				{
					final BitSet moveConcepts = legalMove.moveConcepts(context);
					for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
					{
						final Concept concept = Concept.values()[indexConcept];
						if (moveConcepts.get(concept.id()))
							frenquencyTurn.set(indexConcept, frenquencyTurn.get(indexConcept) + 1);
					}
				}

				// We set the values for the metrics for that single playout.
				mapMetricsPlayout.put(Concept.BranchingFactor.name(), mapMetricsPlayout.get(Concept.BranchingFactor.name()) + numLegalMoves);
				
				// Compute avg for each playout.
				for (int j = 0; j < frenquencyTurn.size(); j++)
					frenquencyPlayout.set(j, frenquencyPlayout.get(j) + (numLegalMoves == 0 ? 0 : frenquencyTurn.get(j) / numLegalMoves));

				prevContext = new Context(context);
				model.startNewStep(context, ais, 1.0);
			}
			
			// Compute avg for all the playouts.
			for (int j = 0; j < frenquencyPlayout.size(); j++)
				frenquencyPlayouts.set(j, frenquencyPlayouts.get(j) + frenquencyPlayout.get(j) / turnWithMoves);

			final int numMoves = trial.numMoves() - trial.numInitialPlacementMoves();

			// Compute avg for all the data used for the metrics for the playout.
			for(Map.Entry<String, Double> entry : mapMetricsPlayout.entrySet())
				mapMetricsPlayouts.put(entry.getKey(), mapMetricsPlayouts.get(entry.getKey()) + (entry.getValue() / numMoves));

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
								frenquencyPlayouts.set(indexConcept, frenquencyPlayouts.get(indexConcept) + 1);
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
							frenquencyPlayouts.set(indexConcept, frenquencyPlayouts.get(indexConcept) + 1);
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
						frenquencyPlayouts.set(indexConcept, frenquencyPlayouts.get(indexConcept) + 1);
						break;
					}
				}
			}

			playoutsDone++;
			final double currentTimeUsed = (System.currentTimeMillis() - startTime) / 1000.0;
			if (currentTimeUsed > timeLimit) // We stop if the limit of time is reached.
				break;
		}

		// Compute avg frequency for the game.
		for (int i = 0; i < frenquencyPlayouts.size(); i++)
			frequencyMoveConcepts.add(frenquencyPlayouts.get(i) / playoutsDone);

		for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
		{
			final Concept concept = Concept.values()[indexConcept];
			mapFrequency.put(concept.name(), Double.valueOf(frequencyMoveConcepts.get(indexConcept)));
		}

		// Compute metrics for the game.
		for(Map.Entry<String, Double> entry : mapMetricsPlayouts.entrySet())
			mapMetrics.put(entry.getKey(), entry.getValue() / playoutsDone);
		
		// We merge the metrics to the frequencies to return one single map.
		for(Map.Entry<String, Double> entry : mapMetrics.entrySet())
			mapFrequency.put(entry.getKey(), entry.getValue());
		
		final double allSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
		final int seconds = (int) (allSeconds % 60.0);
		final int minutes = (int) ((allSeconds - seconds) / 60.0);
		System.out.println("Done in " + minutes + " minutes " + seconds + " seconds. " + playoutsDone + " playouts.");

		return mapFrequency;
	}
}
