package app;

import java.util.Arrays;

import gameDistance.CompareAllDistanceMetrics;
import kilothon.Kilothon;
import ludemeplexDetection.LudemeplexDetection;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import skillTraceAnalysis.SkillTraceAnalysis;
import supplementary.experiments.concepts.ParallelComputeConceptsMultipleGames;
import supplementary.experiments.debugging.FindCrashingTrial;
import supplementary.experiments.eval.EvalAgents;
import supplementary.experiments.eval.EvalGames;
import supplementary.experiments.eval.EvalGate;
import supplementary.experiments.eval.ParallelEvalMultiGamesMultiAgents;
import supplementary.experiments.feature_importance.IdentifyTopFeatures;
import supplementary.experiments.optim.EvolOptimHeuristics;
import supplementary.experiments.scripts.GenerateBiasedMCTSEvalScripts;
import supplementary.experiments.scripts.GenerateFeatureEvalScripts;
import supplementary.experiments.scripts.GenerateGatingScripts;
import supplementary.experiments.speed.PlayoutsPerSec;
import test.instructionGeneration.TestInstructionGeneration;
import training.expert_iteration.ExpertIteration;
import utils.concepts.db.ExportDbCsvConcepts;
import utils.features.ExportFeaturesDB;
import utils.trials.GenerateTrialsCluster;
import utils.trials.GenerateTrialsClusterParallel;

/**
 * Class with helper method to delegate to various other main methods
 * based on command-line arguments.
 * 
 * @author Dennis Soemers
 */
public class PlayerCLI
{
	/**
	 * @param args The first argument is expected to be a command
	 * to run, with all subsequent arguments being passed onto
	 * the called command.
	 */
	public static void runCommand(final String[] args)
	{
		final String[] commandArg = new String[]{args[0]};
		
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Run one of Ludii's command-line options, followed by the command's arguments.\n"
					+ "Enter a command's name followed by \"-h\" or \"--help\" for "
					+ "more information on the arguments for that particular command."
				);
		
		argParse.addOption(new ArgOption()
				.help("Command to run. For more help, enter a command followed by \" --help\"")
				.setRequired()
				.withLegalVals
				(
					"--time-playouts",
					"--expert-iteration",
					"--eval-agents",
					"--find-crashing-trial",
					"--eval-gate",
					"--eval-games",
					"--evol-optim-heuristics",
					"--ludeme-detection",
					"--generate-gating-scripts",
					"--export-features-db",
					"--export-moveconcept-db",
					"--generate-trials",
					"--generate-trials-parallel",
					"--tutorial-generation",
					"--game-distance",
					"--generate-feature-eval-scripts",
//					"--eval-ubfm",
//					"--learning-with-descent",
					"--generate-biased-mcts-eval-scripts",
					"--kilothon",
					"--identify-top-features",
					"--skill-trace-analysis",
					"--parallel-eval-multi-games-multi-agents",
					"--parallel-compute-concepts-multiple-games"
				)
				.withNumVals(1)
				.withType(OptionTypes.String));
		
		// parse the args
		if (!argParse.parseArguments(commandArg))
			return;
		
		final String command = argParse.getValueString(0);
		final String[] passArgs = Arrays.copyOfRange(args, 1, args.length);
		
		if (command.equalsIgnoreCase("--time-playouts"))
			PlayoutsPerSec.main(passArgs);
		else if (command.equalsIgnoreCase("--expert-iteration"))
			ExpertIteration.main(passArgs);
		else if (command.equalsIgnoreCase("--eval-agents"))
			EvalAgents.main(passArgs);
		else if (command.equalsIgnoreCase("--find-crashing-trial"))
			FindCrashingTrial.main(passArgs);
		else if (command.equalsIgnoreCase("--eval-gate"))
			EvalGate.main(passArgs);
		else if (command.equalsIgnoreCase("--eval-games"))
			EvalGames.main(passArgs);
		else if (command.equalsIgnoreCase("--evol-optim-heuristics"))
			EvolOptimHeuristics.main(passArgs);
		else if (command.equalsIgnoreCase("--ludeme-detection"))
			LudemeplexDetection.main(passArgs);
		else if (command.equalsIgnoreCase("--generate-gating-scripts"))
			GenerateGatingScripts.main(passArgs);
		else if (command.equalsIgnoreCase("--export-features-db"))
			ExportFeaturesDB.main(passArgs);
		else if (command.equalsIgnoreCase("--export-moveconcept-db"))
			ExportDbCsvConcepts.main(passArgs);
		else if (command.equalsIgnoreCase("--generate-trials"))
			GenerateTrialsCluster.main(passArgs);
		else if (command.equalsIgnoreCase("--generate-trials-parallel"))
			GenerateTrialsClusterParallel.main(passArgs);
		else if (command.equalsIgnoreCase("--tutorial-generation"))
			TestInstructionGeneration.main(passArgs);
		else if (command.equalsIgnoreCase("--game-distance"))
			CompareAllDistanceMetrics.main(passArgs);
		else if (command.equalsIgnoreCase("--generate-feature-eval-scripts"))
			GenerateFeatureEvalScripts.main(passArgs);
		else if (command.equalsIgnoreCase("--generate-biased-mcts-eval-scripts"))
			GenerateBiasedMCTSEvalScripts.main(passArgs);
		else if (command.equalsIgnoreCase("--kilothon"))
			Kilothon.main(passArgs);
		else if (command.equalsIgnoreCase("--identify-top-features"))
			IdentifyTopFeatures.main(passArgs);
		else if (command.equalsIgnoreCase("--skill-trace-analysis"))
			SkillTraceAnalysis.main(passArgs);
//		else if (command.equalsIgnoreCase("--eval-ubfm"))
//			EvaluateAllUBFMs.main(passArgs);
//		else if (command.equalsIgnoreCase("--learning-with-descent"))
//		{
//			HeuristicsTraining.main(passArgs);
//			EvaluateAllUBFMs.main(new String[] {passArgs[0], "eval heuristics"});
//		}
		else if (command.equalsIgnoreCase("--parallel-eval-multi-games-multi-agents"))
			ParallelEvalMultiGamesMultiAgents.main(passArgs);
		else if (command.equalsIgnoreCase("--parallel-compute-concepts-multiple-games"))
			ParallelComputeConceptsMultipleGames.main(passArgs);
		else
			System.err.println("ERROR: command not yet implemented: " + command);

	}

}
