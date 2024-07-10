package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.json.JSONObject;
import org.json.JSONTokener;

import game.Game;
import main.FileHandling;
import main.grammar.Report;
import metadata.ai.Ai;
import metadata.ai.agents.Agent;
import metadata.ai.agents.BestAgent;
import metadata.ai.agents.mcts.Mcts;
import metadata.ai.agents.minimax.AlphaBeta;
import other.AI;
import policies.GreedyPolicy;
import policies.ProportionalPolicyClassificationTree;
import policies.softmax.SoftmaxPolicyLinear;
import policies.softmax.SoftmaxPolicyLogitTree;
import search.flat.FlatMonteCarlo;
import search.flat.HeuristicSampling;
import search.flat.OnePlyNoHeuristic;
import search.mcts.MCTS;
import search.mcts.MCTS.QInit;
import search.mcts.backpropagation.AlphaGoBackprop;
import search.mcts.backpropagation.MonteCarloBackprop;
import search.mcts.backpropagation.QualitativeBonus;
import search.mcts.finalmoveselection.RobustChild;
import search.mcts.playout.MAST;
import search.mcts.playout.NST;
import search.mcts.playout.RandomPlayout;
import search.mcts.selection.McBRAVE;
import search.mcts.selection.McGRAVE;
import search.mcts.selection.ProgressiveBias;
import search.mcts.selection.ProgressiveHistory;
import search.mcts.selection.UCB1;
import search.mcts.selection.UCB1GRAVE;
import search.mcts.selection.UCB1Tuned;
import search.minimax.AlphaBetaSearch;
import search.minimax.BRSPlus;
import search.minimax.BiasedUBFM;
import search.minimax.HybridUBFM;
import search.minimax.LazyUBFM;
import search.minimax.UBFM;

/**
 * Can create AI agents based on strings / files
 * 
 * @author Dennis Soemers
 */
public class AIFactory 
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Map from absolute paths of JAR files to lists of loaded, third-party AI
	 * classes
	 */
	private static Map<String, List<Class<?>>> thirdPartyAIClasses = new HashMap<>();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor should not be used.
	 */
	private AIFactory()
	{
		// not intended to be used
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param string String representation of agent, or filename from which to load agent
	 * @return Created AI
	 */
	public static AI createAI(final String string)
	{
		if (string.equalsIgnoreCase("Random"))
			return new RandomAI();
		
		if (string.equalsIgnoreCase("Monte Carlo (flat)") || string.equalsIgnoreCase("Flat MC"))
			return new FlatMonteCarlo();
		
		if (string.equalsIgnoreCase("Alpha-Beta") || string.equalsIgnoreCase("AlphaBeta"))
			return AlphaBetaSearch.createAlphaBeta();
		
		if (string.equalsIgnoreCase("BRS+") || string.equalsIgnoreCase("Best-Reply Search+"))
			return new BRSPlus();
		
		if (string.equalsIgnoreCase("UBFM"))
			return new UBFM();

		if (string.equalsIgnoreCase("Hybrid UBFM"))
			return new HybridUBFM();

		if (string.equalsIgnoreCase("Lazy UBFM"))
			return new LazyUBFM();
		
		if (string.equalsIgnoreCase("Biased UBFM"))
			return new BiasedUBFM();
		
		if (string.equalsIgnoreCase("UCT") || string.equalsIgnoreCase("MCTS"))
			return MCTS.createUCT();
		
		if (string.equalsIgnoreCase("MC-GRAVE"))
		{
			final MCTS mcGRAVE =
					new MCTS
					(
						new McGRAVE(),
						new RandomPlayout(200),
						new MonteCarloBackprop(),
						new RobustChild()
					);
			mcGRAVE.setQInit(QInit.INF);
			mcGRAVE.setFriendlyName("MC-GRAVE");
			return mcGRAVE;
		}
		
		if (string.equalsIgnoreCase("MC-BRAVE"))
		{
			final MCTS mcBRAVE =
					new MCTS
					(
						new McBRAVE(),
						new RandomPlayout(200),
						new MonteCarloBackprop(),
						new RobustChild()
					);
			mcBRAVE.setQInit(QInit.INF);
			mcBRAVE.setFriendlyName("MC-BRAVE");
			return mcBRAVE;
		}
		
		if (string.equalsIgnoreCase("UCB1Tuned"))
		{
			final MCTS ucb1Tuned =
					new MCTS
					(
						new UCB1Tuned(),
						new RandomPlayout(200),
						new MonteCarloBackprop(),
						new RobustChild()
					);
			ucb1Tuned.setQInit(QInit.PARENT);
			ucb1Tuned.setFriendlyName("UCB1Tuned");
			return ucb1Tuned;
		}
		
		if (string.equalsIgnoreCase("Score Bounded MCTS") || string.equalsIgnoreCase("ScoreBoundedMCTS"))
		{
			final MCTS sbMCTS =
					new MCTS
					(
						new UCB1(),
						new RandomPlayout(200),
						new MonteCarloBackprop(),
						new RobustChild()
					);
			sbMCTS.setQInit(QInit.PARENT);
			sbMCTS.setUseScoreBounds(true);
			sbMCTS.setFriendlyName("Score Bounded MCTS");
			return sbMCTS;
		}
		
		if (string.equalsIgnoreCase("Progressive History") || string.equalsIgnoreCase("ProgressiveHistory"))
		{
			final MCTS progressiveHistory =
					new MCTS
					(
						new ProgressiveHistory(),
						new RandomPlayout(200),
						new MonteCarloBackprop(),
						new RobustChild()
					);
			progressiveHistory.setQInit(QInit.PARENT);
			progressiveHistory.setFriendlyName("Progressive History");
			return progressiveHistory;
		}
		
		if (string.equalsIgnoreCase("Progressive Bias") || string.equalsIgnoreCase("ProgressiveBias"))
		{
			final MCTS progressiveBias =
					new MCTS
					(
						new ProgressiveBias(),
						new RandomPlayout(200),
						new MonteCarloBackprop(),
						new RobustChild()
					);
			progressiveBias.setQInit(QInit.INF);	// This is probably important for sufficient exploration
			progressiveBias.setWantsMetadataHeuristics(true);
			progressiveBias.setFriendlyName("Progressive Bias");
			return progressiveBias;
		}
		
		if (string.equalsIgnoreCase("MAST"))
		{
			final MCTS mast =
					new MCTS
					(
						new UCB1(),
						new MAST(200, 0.1),
						new MonteCarloBackprop(),
						new RobustChild()
					);
			mast.setQInit(QInit.PARENT);
			mast.setFriendlyName("MAST");
			return mast;
		}
		
		if (string.equalsIgnoreCase("NST"))
		{
			final MCTS nst =
					new MCTS
					(
						new UCB1(),
						new NST(200, 0.1),
						new MonteCarloBackprop(),
						new RobustChild()
					);
			nst.setQInit(QInit.PARENT);
			nst.setFriendlyName("NST");
			return nst;
		}
		
		if (string.equalsIgnoreCase("UCB1-GRAVE"))
		{
			final MCTS ucb1GRAVE =
					new MCTS
					(
						new UCB1GRAVE(),
						new RandomPlayout(200),
						new MonteCarloBackprop(),
						new RobustChild()
					);
			ucb1GRAVE.setFriendlyName("UCB1-GRAVE");
			return ucb1GRAVE;
		}
		
		if (string.equalsIgnoreCase("Ludii AI"))
		{
			return new LudiiAI();
		}
		
		if (string.equalsIgnoreCase("Biased MCTS"))
			return MCTS.createBiasedMCTS(0.0);
		
		if (string.equalsIgnoreCase("Biased MCTS (Uniform Playouts)") || string.equalsIgnoreCase("MCTS (Biased Selection)"))
			return MCTS.createBiasedMCTS(1.0);
		
		if (string.equalsIgnoreCase("MCTS (Hybrid Selection)"))
			return MCTS.createHybridMCTS();
		
		if (string.equalsIgnoreCase("Bandit Tree Search"))
			return MCTS.createBanditTreeSearch();
		
		if (string.equalsIgnoreCase("EPT"))
		{
			final MCTS ept = 
				new MCTS
				(
					new UCB1(Math.sqrt(2.0)), 
					new RandomPlayout(4),
					new AlphaGoBackprop(),
					new RobustChild()
				);

			ept.setWantsMetadataHeuristics(true);
			ept.setPlayoutValueWeight(1.0);
			ept.setFriendlyName("EPT");
			return ept;
		}
		
		if (string.equalsIgnoreCase("EPT-QB"))
		{
			final MCTS ept = 
				new MCTS
				(
					new UCB1(Math.sqrt(2.0)), 
					new RandomPlayout(4),
					new QualitativeBonus(),
					new RobustChild()
				);

			ept.setWantsMetadataHeuristics(true);
			ept.setFriendlyName("EPT-QB");
			return ept;
		}
		
		if (string.equalsIgnoreCase("Heuristic Sampling"))
		{
			return new HeuristicSampling();
		}
		else if (string.equalsIgnoreCase("Heuristic Sampling (1)"))
		{
			return new HeuristicSampling(1);
		}
		
		if (string.equalsIgnoreCase("One-Ply (No Heuristic)"))
			return new OnePlyNoHeuristic();
		
		// See if this AI was registered
		final AI registeredAI = AIRegistry.fromRegistry(string);
		if (registeredAI != null)
			return registeredAI;
		
		// Try to interpret the given string as a resource or some other 
		// kind of file
		final URL aiURL = AIFactory.class.getResource(string);
		File aiFile = null;
		
		if (aiURL != null)
			aiFile = new File(aiURL.getFile());
		else
			aiFile = new File(string);
		
		String[] lines = new String[0];
		
		if (aiFile.exists())
		{
			try (final BufferedReader reader = new BufferedReader(new FileReader(aiFile)))
			{
				final List<String> linesList = new ArrayList<String>();
				
				String line = reader.readLine();
				while (line != null)
				{
					linesList.add(line);
				}
				
				lines = linesList.toArray(lines);
			} 
			catch (final IOException e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			// assume semicolon-separated lines directly passed as 
			// command line arg
			lines = string.split(";");
		}
		
		final String firstLine = lines[0];
		if (firstLine.startsWith("algorithm="))
		{
			final String algName = firstLine.substring("algorithm=".length());
			
			if 
			(
				algName.equalsIgnoreCase("MCTS") 
				|| 
				algName.equalsIgnoreCase("UCT")
			)
			{
				// UCT is the default implementation of MCTS, 
				// so both cases are the same
				return MCTS.fromLines(lines);
			}
			else if 
			(
				algName.equalsIgnoreCase("AlphaBeta") 
				||
				algName.equalsIgnoreCase("Alpha-Beta")
			)
			{
				return AlphaBetaSearch.fromLines(lines);
			}
			else if (algName.equalsIgnoreCase("BRS+"))
			{
				return BRSPlus.fromLines(lines);
			}
			else if (algName.equalsIgnoreCase("HeuristicSampling"))
			{
				return HeuristicSampling.fromLines(lines);
			}
			else if 
			(
				algName.equalsIgnoreCase("Softmax") 
				|| 
				algName.equalsIgnoreCase("SoftmaxPolicy")
			)
			{
				return SoftmaxPolicyLinear.fromLines(lines);
			}
			else if 
			(
				algName.equalsIgnoreCase("Greedy") 
				||
				algName.equalsIgnoreCase("GreedyPolicy")
			)
			{
				return GreedyPolicy.fromLines(lines);
			}
			else if (algName.equalsIgnoreCase("ProportionalPolicyClassificationTree"))
			{
				return ProportionalPolicyClassificationTree.fromLines(lines);
			}
			else if (algName.equalsIgnoreCase("SoftmaxPolicyLogitTree"))
			{
				return SoftmaxPolicyLogitTree.fromLines(lines);
			}
			else if (algName.equalsIgnoreCase("Random"))
			{
				return new RandomAI();
			}
			else
			{
				System.err.println("Unknown algorithm name: " + algName);
			}
		}
		else
		{
			System.err.println
			(
				"Expecting AI file to start with \"algorithm=\", "
				+ "but it starts with " + firstLine
			);
		}
		
		System.err.println
		(
			String.format
			(
				"Warning: cannot convert string \"%s\" to AI; defaulting to random.", 
				string
			)
		);
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param file
	 * @return AI created based on configuration in given JSON file
	 */
	public static AI fromJsonFile(final File file)
	{
		try (final InputStream inputStream = new FileInputStream(file))
		{
			final JSONObject json = new JSONObject(new JSONTokener(inputStream));
			return fromJson(json);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		
		System.err.println("WARNING: Failed to construct AI from JSON file: " + file.getAbsolutePath());
		return null;
	}
	
	/**
	 * @param json
	 * @return AI created based on configuration in given JSON object
	 */
	public static AI fromJson(final JSONObject json)
	{
		if (json.has("constructor"))
		{
			final AIConstructor constructor = (AIConstructor) json.get("constructor");
			if (constructor != null)
				return constructor.constructAI();
		}
		
		final JSONObject aiObj = json.getJSONObject("AI");
		final String algName = aiObj.getString("algorithm");

		if 
		(
			algName.equalsIgnoreCase("Ludii") ||
			algName.equalsIgnoreCase("Ludii AI") ||
			algName.equalsIgnoreCase("Default")
		)
		{
			return new LudiiAI();
		}
		else if (algName.equalsIgnoreCase("Random"))
		{
			return new RandomAI();
		}
		else if (algName.equalsIgnoreCase("Lazy UBFM"))
		{
			return new LazyUBFM();
		}
		else if (algName.equalsIgnoreCase("Hybrid UBFM"))
		{
			return new HybridUBFM();
		}
		else if (algName.equalsIgnoreCase("Biased UBFM"))
		{
			return new BiasedUBFM();
		}
		else if (algName.equalsIgnoreCase("UBFM"))
		{
			return UBFM.createUBFM();
		}
		else if (algName.equalsIgnoreCase("Monte Carlo (flat)") || algName.equalsIgnoreCase("Flat MC"))
		{
			return new FlatMonteCarlo();
		}
		else if (algName.equalsIgnoreCase("UCT"))
		{
			return MCTS.createUCT();
		}
		else if (algName.equalsIgnoreCase("UCT (Uncapped)"))
		{
			final MCTS uct = 
				new MCTS
				(
					new UCB1(Math.sqrt(2.0)), 
					new RandomPlayout(),
					new MonteCarloBackprop(),
					new RobustChild()
				);
		
			uct.setFriendlyName("UCT (Uncapped)");
			return uct;
		}
		else if (algName.equalsIgnoreCase("MCTS"))
		{
			return MCTS.fromJson(aiObj);
		}
		else if (algName.equalsIgnoreCase("MC-GRAVE"))
		{
			final MCTS mcGRAVE = new MCTS(new McGRAVE(), new RandomPlayout(200), new MonteCarloBackprop(), new RobustChild());
			mcGRAVE.setQInit(QInit.INF);
			mcGRAVE.setFriendlyName("MC-GRAVE");
			return mcGRAVE;
		}
		else if (algName.equalsIgnoreCase("MC-BRAVE"))
		{
			final MCTS mcBRAVE = new MCTS(new McBRAVE(), new RandomPlayout(200), new MonteCarloBackprop(), new RobustChild());
			mcBRAVE.setQInit(QInit.INF);
			mcBRAVE.setFriendlyName("MC-BRAVE");
			return mcBRAVE;
		}
		else if (algName.equalsIgnoreCase("UCB1Tuned"))
		{
			return createAI("UCB1Tuned");
		}
		else if (algName.equalsIgnoreCase("Score Bounded MCTS") || algName.equalsIgnoreCase("ScoreBoundedMCTS"))
		{
			return createAI("Score Bounded MCTS");
		}
		else if (algName.equalsIgnoreCase("Progressive History") || algName.equalsIgnoreCase("ProgressiveHistory"))
		{
			final MCTS progressiveHistory =
					new MCTS
					(
						new ProgressiveHistory(),
						new RandomPlayout(200),
						new MonteCarloBackprop(),
						new RobustChild()
					);
			progressiveHistory.setQInit(QInit.PARENT);
			progressiveHistory.setFriendlyName("Progressive History");
			return progressiveHistory;
		}
		else if (algName.equalsIgnoreCase("Progressive Bias") || algName.equalsIgnoreCase("ProgressiveBias"))
		{
			return AIFactory.createAI("Progressive Bias");
		}
		else if (algName.equalsIgnoreCase("MAST"))
		{
			final MCTS mast =
					new MCTS
					(
						new UCB1(),
						new MAST(200, 0.1),
						new MonteCarloBackprop(),
						new RobustChild()
					);
			mast.setQInit(QInit.PARENT);
			mast.setFriendlyName("MAST");
			return mast;
		}
		else if (algName.equalsIgnoreCase("NST"))
		{
			final MCTS nst =
					new MCTS
					(
						new UCB1(),
						new NST(200, 0.1),
						new MonteCarloBackprop(),
						new RobustChild()
					);
			nst.setQInit(QInit.PARENT);
			nst.setFriendlyName("NST");
			return nst;
		}
		else if (algName.equalsIgnoreCase("UCB1-GRAVE"))
		{
			final MCTS ucb1GRAVE = new MCTS(new UCB1GRAVE(), new RandomPlayout(200), new MonteCarloBackprop(), new RobustChild());
			ucb1GRAVE.setFriendlyName("UCB1-GRAVE");
			return ucb1GRAVE;
		}
		else if (algName.equalsIgnoreCase("Biased MCTS"))
		{
			return MCTS.createBiasedMCTS(0.0);
		}
		else if (algName.equalsIgnoreCase("Biased MCTS (Uniform Playouts)") || algName.equalsIgnoreCase("MCTS (Biased Selection)"))
		{
			return MCTS.createBiasedMCTS(1.0);
		}
		else if (algName.equalsIgnoreCase("MCTS (Hybrid Selection)"))
		{
			return MCTS.createHybridMCTS();
		}
		else if (algName.equalsIgnoreCase("Bandit Tree Search"))
		{
			return MCTS.createBanditTreeSearch();
		}
		else if (algName.equalsIgnoreCase("EPT"))
		{
			return createAI("EPT");
		}
		else if (algName.equalsIgnoreCase("EPT-QB"))
		{
			return createAI("EPT-QB");
		}
		else if (algName.equalsIgnoreCase("Alpha-Beta") || algName.equalsIgnoreCase("AlphaBeta"))
		{
			return AlphaBetaSearch.createAlphaBeta();
		}
		else if (algName.equalsIgnoreCase("BRS+") || algName.equalsIgnoreCase("Best-Reply Search+"))
		{
			return new BRSPlus();
		}
		else if (algName.equalsIgnoreCase("Heuristic Sampling"))
		{
			return new HeuristicSampling();
		}
		else if (algName.equalsIgnoreCase("Heuristic Sampling (1)"))
		{
			return new HeuristicSampling(1);
		}
		else if (algName.equalsIgnoreCase("One-Ply (No Heuristic)"))
		{
			return new OnePlyNoHeuristic();
		}
		else if (algName.equalsIgnoreCase("From JAR"))
		{
			final File jarFile = new File(aiObj.getString("JAR File"));
			final String className = aiObj.getString("Class Name");
			
			try
			{
				for (final Class<?> clazz : loadThirdPartyAIClasses(jarFile))
				{
					if (clazz.getName().equals(className))
					{
						return (AI) clazz.getConstructor().newInstance();
					}
				}
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (algName.equalsIgnoreCase("From AI.DEF"))
		{
			try
			{
				final String aiMetadataStr = FileHandling.loadTextContentsFromFile(aiObj.getString("AI.DEF File"));
				final Ai aiMetadata = 
						(Ai)compiler.Compiler.compileObject
						(
							aiMetadataStr, 
							"metadata.ai.Ai",
							new Report()
						);
				
				return fromDefAgent(aiMetadata.agent());
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
		
		System.err.println("WARNING: Failed to construct AI from JSON: " + json.toString(4));
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @return AI constructed from given game's metadata
	 */
	public static AI fromMetadata(final Game game)
	{
		// Try to find the best agent type
		String bestAgent;
		
		if (!game.isAlternatingMoveGame())
			bestAgent = "Flat MC";
		else
			bestAgent = "UCT";
		
		if (game.metadata().ai().agent() != null)
		{
			return fromDefAgent(game.metadata().ai().agent());
		}
		
		final AI ai = createAI(bestAgent);
		return ai;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param agent
	 * @return AI constructed from agent metadata in some .def file
	 */
	public static AI fromDefAgent(final Agent agent)
	{
		if (agent instanceof BestAgent)
			return fromDefBestAgent((BestAgent) agent);
		else if (agent instanceof AlphaBeta)
			return fromDefAlphaBetaAgent((AlphaBeta) agent);
		else if (agent instanceof Mcts)
			return fromDefMctsAgent((Mcts) agent);
		
		System.err.println("AIFactory failed to load from def agent: " + agent);
		return null;
	}
	
	/**
	 * @param agent
	 * @return AI built from a best-agent string
	 */
	public static AI fromDefBestAgent(final BestAgent agent)
	{
		return createAI(agent.agent());
	}
	
	/**
	 * @param agent
	 * @return AlphaBeta AI built from AlphaBeta metadata
	 */
	public static AlphaBetaSearch fromDefAlphaBetaAgent(final AlphaBeta agent)
	{
		if (agent.heuristics() == null)
			return new AlphaBetaSearch();
		else
			return new AlphaBetaSearch(agent.heuristics());
	}
	
	/**
	 * @param agent
	 * @return MCTS AI built from Mcts metadata
	 */
	public static MCTS fromDefMctsAgent(final Mcts agent)
	{
		System.err.println("Loading MCTS from def not yet implemented!");
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param jarFile
	 * @return List of all AI classes in the given JAR file
	 */
	public static List<Class<?>> loadThirdPartyAIClasses(final File jarFile)
	{
		List<Class<?>> classes = null;
		
		try
		{
			if (thirdPartyAIClasses.containsKey(jarFile.getAbsolutePath()))
			{
				classes = thirdPartyAIClasses.get(jarFile.getAbsolutePath());
			}
			else
			{
				classes = new ArrayList<Class<?>>();

				// search the full jar file for all classes that extend our AI abstract class
				final URL[] urls =
					{ new URL("jar:file:" + jarFile.getAbsolutePath() + "!/") };

				try (final URLClassLoader classLoader = URLClassLoader.newInstance(urls))
				{
					try (final JarFile jar = new JarFile(jarFile))
					{
						final Enumeration<? extends JarEntry> entries = jar.entries();
						while (entries.hasMoreElements())
						{
							final ZipEntry entry = entries.nextElement();

							try
							{
								if (entry.getName().endsWith(".class"))
								{
									final String className = entry.getName().replace(".class", "").replace("/", ".");
									final Class<?> clazz = classLoader.loadClass(className);

									if (AI.class.isAssignableFrom(clazz))
										classes.add(clazz);
								}
							}
							catch (final NoClassDefFoundError exception)
							{
								continue;
							}
						}
					}
				}

				classes.sort(new Comparator<Class<?>>()
				{
					@Override
					public int compare(final Class<?> o1, final Class<?> o2)
					{
						return o1.getName().compareTo(o2.getName());
					}
				});

				thirdPartyAIClasses.put(jarFile.getAbsolutePath(), classes);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		return classes;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Interface for a functor that constructs AIs
	 *
	 * @author Dennis Soemers
	 */
	public static interface AIConstructor
	{
		/**
		 * @return Constructed AI object
		 */
		public AI constructAI();
	}
	
	//-------------------------------------------------------------------------

}
