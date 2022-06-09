package supplementary.experiments.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import game.equipment.component.Component;
import game.equipment.other.Regions;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.StringRoutines;
import main.grammar.Report;
import main.options.Ruleset;
import metadata.ai.Ai;
import metadata.ai.agents.BestAgent;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.CentreProximity;
import metadata.ai.heuristics.terms.ComponentValues;
import metadata.ai.heuristics.terms.CornerProximity;
import metadata.ai.heuristics.terms.HeuristicTerm;
import metadata.ai.heuristics.terms.Influence;
import metadata.ai.heuristics.terms.LineCompletionHeuristic;
import metadata.ai.heuristics.terms.Material;
import metadata.ai.heuristics.terms.MobilitySimple;
import metadata.ai.heuristics.terms.NullHeuristic;
import metadata.ai.heuristics.terms.OwnRegionsCount;
import metadata.ai.heuristics.terms.PlayerRegionsProximity;
import metadata.ai.heuristics.terms.PlayerSiteMapCount;
import metadata.ai.heuristics.terms.RegionProximity;
import metadata.ai.heuristics.terms.Score;
import metadata.ai.heuristics.terms.SidesProximity;
import metadata.ai.misc.Pair;
import other.GameLoader;
import search.minimax.AlphaBetaSearch;
import utils.DBGameInfo;
import utils.analysis.BestBaseAgents;
import utils.analysis.BestStartingHeuristics;

/**
 * Script to initialise directory of best-agents data
 *
 * @author Dennis Soemers
 */
public class InitBestAgentsData
{
	
	/**
	 * Constructor
	 */
	private InitBestAgentsData()
	{
		// Should not construct
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Initialises our best agents data directory
	 * @param argParse
	 */
	private static void initBestAgentsData(final CommandLineArgParse argParse)
	{
		// Load our tables of best base agents and heuristics
		final BestBaseAgents bestBaseAgents = BestBaseAgents.loadData();
		final BestStartingHeuristics bestStartingHeuristics = BestStartingHeuristics.loadData();
		
		// Create the output directory
		String bestAgentsDataDirPath = argParse.getValueString("--best-agents-data-dir");
		bestAgentsDataDirPath = bestAgentsDataDirPath.replaceAll(Pattern.quote("\\"), "/");
		if (!bestAgentsDataDirPath.endsWith("/"))
			bestAgentsDataDirPath += "/";
		final File bestAgentsDataDir = new File(bestAgentsDataDirPath);
		if (!bestAgentsDataDir.exists())
			bestAgentsDataDir.mkdirs();
		
		// Find our files of starting heuristics
		String startingHeuristicsDir = argParse.getValueString("--starting-heuristics-dir");
		startingHeuristicsDir = startingHeuristicsDir.replaceAll(Pattern.quote("\\"), "/");
		if (!startingHeuristicsDir.endsWith("/"))
			startingHeuristicsDir += "/";
		
		final String[] allGameNames = Arrays.stream(FileHandling.listGames()).filter(s -> (
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wishlist/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/simulation/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/proprietary/"))
			)).toArray(String[]::new);
		
		for (final String fullGamePath : allGameNames)
		{
			final String[] gamePathParts = fullGamePath.replaceAll(Pattern.quote("\\"), "/").split(Pattern.quote("/"));
			final String gameName = gamePathParts[gamePathParts.length - 1].replaceAll(Pattern.quote(".lud"), "");
			final Game gameNoRuleset = GameLoader.loadGameFromName(gameName + ".lud");
			final List<Ruleset> gameRulesets = new ArrayList<Ruleset>(gameNoRuleset.description().rulesets());
			gameRulesets.add(null);
			boolean foundRealRuleset = false;
			
			for (final Ruleset ruleset : gameRulesets)
			{
				final Game game;
				String fullRulesetName = "";
				if (ruleset == null && foundRealRuleset)
				{
					// Skip this, don't allow game without ruleset if we do have real implemented ones
					continue;
				}
				else if (ruleset != null && !ruleset.optionSettings().isEmpty())
				{
					fullRulesetName = ruleset.heading();
					foundRealRuleset = true;
					game = GameLoader.loadGameFromName(gameName + ".lud", fullRulesetName);
				}
				else if (ruleset != null && ruleset.optionSettings().isEmpty())
				{
					// Skip empty ruleset
					continue;
				}
				else
				{
					game = gameNoRuleset;
				}
				
				if (game.hasSubgames())
					continue;
				
				final String filepathsGameName = StringRoutines.cleanGameName(gameName);
				final String filepathsRulesetName = StringRoutines.cleanRulesetName(fullRulesetName.replaceAll(Pattern.quote("Ruleset/"), ""));
				
				final File bestAgentsDataDirForGame = new File(bestAgentsDataDir.getAbsolutePath() + "/" + filepathsGameName + filepathsRulesetName);
				initBestAgentsDataDir(bestAgentsDataDirForGame, game, bestBaseAgents, bestStartingHeuristics, DBGameInfo.getUniqueName(game), startingHeuristicsDir);
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Populates a best agents data directory with initial data.
	 * 
	 * @param bestAgentsDataDirForGame
	 * @param game
	 * @param bestBaseAgents
	 * @param bestStartingHeuristics
	 * @param gameRulesetName
	 * @param startingHeuristicsDir
	 */
	private static void initBestAgentsDataDir
	(
		final File bestAgentsDataDirForGame, 
		final Game game,
		final BestBaseAgents bestBaseAgents,
		final BestStartingHeuristics bestStartingHeuristics,
		final String gameRulesetName,
		final String startingHeuristicsDir
	)
	{
		final Ai aiMetadata = game.metadata().ai();
		
		bestAgentsDataDirForGame.mkdirs();
		final File bestAgentFile = new File(bestAgentsDataDirForGame.getAbsolutePath() + "/BestAgent.txt");
		final File bestFeaturesFile = new File(bestAgentsDataDirForGame.getAbsolutePath() + "/BestFeatures.txt");
		final File bestHeuristicsFile = new File(bestAgentsDataDirForGame.getAbsolutePath() + "/BestHeuristics.txt");
		
		final BestBaseAgents.Entry baseAgentEntry = bestBaseAgents.getEntry(gameRulesetName);
		if (baseAgentEntry != null)
		{
			final BestAgent bestAgent = new BestAgent(baseAgentEntry.topAgent());
			try (final PrintWriter writer = new PrintWriter(bestAgentFile, "UTF-8"))
			{
				writer.println(bestAgent.toString());
			}
			catch (final FileNotFoundException | UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}
		else if (aiMetadata.agent() != null)
		{
			try (final PrintWriter writer = new PrintWriter(bestAgentFile, "UTF-8"))
			{
				writer.println(aiMetadata.agent().toString());
			}
			catch (final FileNotFoundException | UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}
		
		if (aiMetadata.features() != null)
		{
			try (final PrintWriter writer = new PrintWriter(bestFeaturesFile))
			{
				writer.println(aiMetadata.features().toString());
			}
			catch (final FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}
		
		if (baseAgentEntry != null && baseAgentEntry.topAgent().equals("AlphaBetaMetadata"))
		{
			// Take heuristics as we have them in metadata right now
			if (aiMetadata.heuristics() != null)
			{
				try (final PrintWriter writer = new PrintWriter(bestHeuristicsFile))
				{
					writer.println(aiMetadata.heuristics().toString());
				}
				catch (final FileNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}
		else if (new AlphaBetaSearch().supportsGame(game))
		{
			// Load our top starting heuristic
			final BestStartingHeuristics.Entry startingHeuristicEntry = bestStartingHeuristics.getEntry(gameRulesetName);
			
			final Regions[] regions = game.equipment().regions();
			final List<Regions> staticRegions = game.equipment().computeStaticRegions();
			
			boolean skipCentreProximity = false;
			boolean skipComponentValues = false;
			boolean skipCornerProximity = false;
			boolean skipLineCompletionHeuristic = false;
			boolean skipMaterial = false;
			boolean skipMobilitySimple = false;
			boolean skipNullHeuristic = false;
			boolean skipInfluence = false;
			boolean skipOwnRegionCount = false;
			final boolean[] skipPlayerRegionsProximity = new boolean[game.players().count() + 1];
			boolean skipPlayerSiteMapCount = false;
			final boolean[] skipRegionProximity = new boolean[regions.length];
			boolean skipScore = false;
			boolean skipSidesProximity = false;
			
			// We'll always skip proximity to non-static regions
			for (int i = 0; i < regions.length; ++i)
			{
				if (!staticRegions.contains(regions[i]))
					skipRegionProximity[i] = true;
			}

			// And we'll always skip player region proximity for players who don't own any regions
			for (int p = 1; p <= game.players().count(); ++p)
			{
				boolean foundOwned = false;

				for (final Regions region : staticRegions)
				{
					if (region.owner() == p)
					{
						foundOwned = true;
						break;
					}
				}

				if (!foundOwned)
					skipPlayerRegionsProximity[p] = true;
			}
			
			final List<HeuristicTerm> heuristicTerms = new ArrayList<HeuristicTerm>();
			
			if (startingHeuristicEntry != null)
			{
				try
				{
					final Heuristics startingHeuristics = 
							(Heuristics)compiler.Compiler.compileObject
							(
								FileHandling.loadTextContentsFromFile
								(
									startingHeuristicsDir + startingHeuristicEntry.topHeuristic() + ".txt"
								), 
								"metadata.ai.heuristics.Heuristics",
								new Report()
							);
					
					for (final HeuristicTerm term : startingHeuristics.heuristicTerms())
					{
						heuristicTerms.add(term);
						if (term instanceof CentreProximity)
						{
							skipCentreProximity = true;
						}
						else if (term instanceof ComponentValues)
						{
							skipComponentValues = true;
						}
						else if (term instanceof CornerProximity)
						{
							skipCornerProximity = true;
						}
						else if (term instanceof LineCompletionHeuristic)
						{
							skipLineCompletionHeuristic = true;
						}
						else if (term instanceof Material)
						{
							skipMaterial = true;
						}
						else if (term instanceof MobilitySimple)
						{
							skipMobilitySimple = true;
						}
						else if (term instanceof NullHeuristic)
						{
							skipNullHeuristic = true;
						}
						else if (term instanceof Influence)
						{
							skipInfluence = true;
						}
						else if (term instanceof OwnRegionsCount)
						{
							skipOwnRegionCount = true;
						}
						else if (term instanceof PlayerRegionsProximity)
						{
							final PlayerRegionsProximity playerRegionsProximity = (PlayerRegionsProximity) term;
							skipPlayerRegionsProximity[playerRegionsProximity.regionPlayer()] = true;
						}
						else if (term instanceof PlayerSiteMapCount)
						{
							skipPlayerSiteMapCount = true;
						}
						else if (term instanceof RegionProximity)
						{
							final RegionProximity regionProximity = (RegionProximity) term;
							skipRegionProximity[regionProximity.region()] = true;
						}
						else if (term instanceof Score)
						{
							skipScore = true;
						}
						else if (term instanceof SidesProximity)
						{
							skipSidesProximity = true;
						}
						else
						{
							System.err.println("Did not recognise class for heuristic term: " + term);
						}
					}
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
			
			// Add 0-weight heuristics for all the unused terms (so we can train them)
			if (!skipCentreProximity)
			{
				if (CentreProximity.isApplicableToGame(game))
					heuristicTerms.add(new CentreProximity(null, Float.valueOf(1.f), zeroWeightPairsArray(game)));
			}
			
			if (!skipComponentValues)
			{
				if (ComponentValues.isApplicableToGame(game))
					heuristicTerms.add(new ComponentValues(null, Float.valueOf(1.f), zeroWeightPairsArray(game), null));
			}
			
			if (!skipCornerProximity)
			{
				if (CornerProximity.isApplicableToGame(game))
					heuristicTerms.add(new CornerProximity(null, Float.valueOf(1.f), zeroWeightPairsArray(game)));
			}
			
			if (!skipLineCompletionHeuristic)
			{
				if (LineCompletionHeuristic.isApplicableToGame(game))
					heuristicTerms.add(new LineCompletionHeuristic(null, Float.valueOf(0.f), null));
			}
			
			if (!skipMaterial)
			{
				if (Material.isApplicableToGame(game))
					heuristicTerms.add(new Material(null, Float.valueOf(1.f), zeroWeightPairsArray(game), null));
			}
			
			if (!skipMobilitySimple)
			{
				if (MobilitySimple.isApplicableToGame(game))
					heuristicTerms.add(new MobilitySimple(null, Float.valueOf(0.f)));
			}
			
			if (!skipNullHeuristic)
			{
				if (NullHeuristic.isApplicableToGame(game))
					heuristicTerms.add(new NullHeuristic());
			}
			
			if (!skipInfluence)
			{
				if (Influence.isApplicableToGame(game))
					heuristicTerms.add(new Influence(null, Float.valueOf(0.f)));
			}
			
			if (!skipOwnRegionCount)
			{
				if (OwnRegionsCount.isApplicableToGame(game))
					heuristicTerms.add(new OwnRegionsCount(null, Float.valueOf(0.f)));
			}
			
			for (int p = 1; p <= game.players().count(); ++p)
			{
				if (!skipPlayerRegionsProximity[p])
				{
					if (PlayerRegionsProximity.isApplicableToGame(game))
						heuristicTerms.add(new PlayerRegionsProximity(null, Float.valueOf(1.f), Integer.valueOf(p), zeroWeightPairsArray(game)));
				}
			}
			
			if (!skipPlayerSiteMapCount)
			{
				if (PlayerSiteMapCount.isApplicableToGame(game))
					heuristicTerms.add(new PlayerSiteMapCount(null, Float.valueOf(0.f)));
			}
			
			for (int i = 0; i < regions.length; ++i)
			{
				if (!skipRegionProximity[i])
					heuristicTerms.add(new RegionProximity(null, Float.valueOf(1.f), Integer.valueOf(i), zeroWeightPairsArray(game)));
			}
			
			if (!skipScore)
			{
				if (Score.isApplicableToGame(game))
					heuristicTerms.add(new Score(null, Float.valueOf(0.f)));
			}
			
			if (!skipSidesProximity)
			{
				if (SidesProximity.isApplicableToGame(game))
					heuristicTerms.add(new SidesProximity(null, Float.valueOf(1.f), zeroWeightPairsArray(game)));
			}
			
			try (final PrintWriter writer = new PrintWriter(bestHeuristicsFile))
			{
				writer.println(new Heuristics(heuristicTerms.toArray(new HeuristicTerm[heuristicTerms.size()])).toString());
			}
			catch (final FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Helper method to generate an array of Pairs for 0-piece-weight heuristics
	 * @param game
	 * @return
	 */
	private static Pair[] zeroWeightPairsArray(final Game game)
	{
		final Component[] components = game.equipment().components();
		final List<Pair> pairs = new ArrayList<Pair>(components.length);
		
		for (final Component comp : components)
		{
			if (comp != null)
				pairs.add(new Pair(comp.name(), Float.valueOf(0.f)));
		}
		
		return pairs.toArray(new Pair[pairs.size()]);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method to generate all our scripts
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Initialises a directory with best-agents data."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--best-agents-data-dir")
				.help("Base directory in which we want to store data about the best agents per game.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--starting-heuristics-dir")
				.help("Directory with our starting heuristic files.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		initBestAgentsData(argParse);
	}
	
	//-------------------------------------------------------------------------
}
