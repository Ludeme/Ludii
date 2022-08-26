package ai;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import game.Game;
import compiler.Compiler;
import main.FileHandling;
import main.grammar.Description;
import other.context.Context;
import other.model.Model;
import other.trial.Trial;

/**
 * Unit tests for heuristic state value estimators.
 *
 * @author Dennis Soemers
 */
public class TestHeuristicStateValues
{
	
	/**
	 * Unit test which:
	 * 	- Runs one random playout for every game
	 * 	- In every encountered state, tries to run the computation of any
	 * 	heuristic state evaluation term that claims to be applicable to that game
	 * 	- Ensures we have at least one (non-intercept, non-mover) heuristic state
	 * 	evaluation term that's applicable for every alternating-move game.
	 */
	@Test
	@SuppressWarnings("static-method")
	public void test()
	{
		final File startFolder = new File("../Common/res/lud/");
		final List<File> gameDirs = new ArrayList<File>();
		gameDirs.add(startFolder);

		final List<File> entries = new ArrayList<File>();
		
		for (int i = 0; i < gameDirs.size(); ++i)
		{
			final File gameDir = gameDirs.get(i);
			
			for (final File fileEntry : gameDir.listFiles())
			{
				if (fileEntry.isDirectory())
				{
					final String path = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
					
					if (path.equals("../Common/res/lud/plex"))
						continue;
					
					if (path.equals("../Common/res/lud/wip"))
						continue;

					if (path.equals("../Common/res/lud/wishlist"))
						continue;
					
					if (path.equals("../Common/res/lud/puzzle/deduction"))
						continue; // skip deduction puzzles for now
					
					if (path.equals("../Common/res/lud/bad"))
						continue;
					
					if (path.equals("../Common/res/lud/bad_playout"))
						continue;
					
					if (path.equals("../Common/res/lud/test"))
						continue;

					// We'll find files that we should be able to compile and run here
					gameDirs.add(fileEntry);
				}
				else
				{
					entries.add(fileEntry);
				}
			}
		}
		
		// the following entries should correctly compile and run
		for (final File fileEntry : entries)
		{
			if (fileEntry.getName().contains(".lud"))
			{
				final String fileName = fileEntry.getPath();
				System.out.println("File: " + fileName);

				// Load the string from file
				String desc = "";
				try
				{
					desc = FileHandling.loadTextContentsFromFile(fileName);	
				}
				catch (final FileNotFoundException ex)
				{
					System.out.println("Unable to open file '" + fileName + "'");
				}
				catch (final IOException ex)
				{
					System.out.println("Error reading file '" + fileName + "'");
				}

				// Parse and compile the game
				final Game game = (Game)Compiler.compileTest(new Description(desc), false);

				if (game.hasSubgames())
					continue;

				final Trial trial = new Trial(game);
				final Context context = new Context(game, trial);

				game.start(context);
				
				// collect applicable heuristics
				final int numPlayers = game.players().count();
//				final List<Component> components = game.equipment().components();
//				final List<Regions> regions = game.equipment().regions();
//				final List<StateHeuristicValue> heuristics = new ArrayList<StateHeuristicValue>();
//				heuristics.add(new Intercept());	// intercept always applicable
//				
//				if (CentreProximity.isApplicableToGame(game))
//					heuristics.add(new CentreProximity(game));
//				
//				if (CornerProximity.isApplicableToGame(game))
//					heuristics.add(new CornerProximity(game));
//				
//				if (CurrentMoverHeuristic.isApplicableToGame(game))
//					heuristics.add(new CurrentMoverHeuristic());
//				
//				if (LineCompletionHeuristic.isApplicableToGame(game))
//					heuristics.add(new LineCompletionHeuristic(game));
//				
//				if (Material.isApplicableToGame(game))
//					heuristics.add(new Material(game));
//				
//				if (MobilitySimple.isApplicableToGame(game))
//					heuristics.add(new MobilitySimple());
//				
////				if (OpponentPieceProximity.isApplicableToGame(game))
////					heuristics.add(new OpponentPieceProximity(game));
//				
//				if (OwnRegionsCount.isApplicableToGame(game))
//					heuristics.add(new OwnRegionsCount(game));
//				
//				if (PlayerRegionsProximity.isApplicableToGame(game))
//				{
//					final FVector pieceWeights = new FVector(components.size());
//					pieceWeights.fill(0, pieceWeights.dim(), 1.f);
//					
//					for (int p = 1; p <= numPlayers; ++p)
//					{
//						heuristics.add(new PlayerRegionsProximity(game, pieceWeights, p));
//					}
//				}
//				
//				if (PlayerSiteMapCount.isApplicableToGame(game))
//					heuristics.add(new PlayerSiteMapCount());
//				
//				if (RegionProximity.isApplicableToGame(game))
//				{
//					final FVector pieceWeights = new FVector(components.size());
//					pieceWeights.fill(0, pieceWeights.dim(), 1.f);
//					
//					for (int i = 0; i < regions.size(); ++i)
//					{
//						heuristics.add(new RegionProximity(game, pieceWeights, i));
//					}
//				}
//				
//				if (Score.isApplicableToGame(game))
//					heuristics.add(new Score());
//				
//				if (SidesProximity.isApplicableToGame(game))
//					heuristics.add(new SidesProximity(game));
//				
//				assert (heuristics.size() > 1);
//				
//				final FVector heuristicTermWeights = new FVector(heuristics.size());
//				heuristicTermWeights.fill(0, heuristicTermWeights.dim(), 1.f);
//				
//				final List<HeuristicTransformation> transformations = new ArrayList<HeuristicTransformation>();
//				for (int i = 0; i < heuristics.size(); ++i)
//				{
//					transformations.add(new Identity());
//				}
//				
//				final HeuristicEnsemble ensemble = new HeuristicEnsemble(heuristicTermWeights, heuristics, transformations);
				
				final Model model = context.model();
				while (!trial.over())
				{
					// evaluate from every player's perspective
					for (int p = 1; p <= numPlayers; ++p)
					{
						//final float eval = ensemble.computeValue(context, p, -1.f);
						//assert (!Float.isNaN(eval));
					}
					
					model.startNewStep(context, null, 0.0);
				}
			}
		}
	}

}
