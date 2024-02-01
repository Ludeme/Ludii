package games;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import game.Game;
import game.rules.phase.Phase;
import game.rules.play.moves.Moves;
import main.FileHandling;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.model.Model;
import other.move.Move;
import other.trial.Trial;

/**
 * Check if in running a pre-determined number of playouts over all our games,
 * if some duplicates are present.
 * 
 * @author Eric.Piette
 */
public class DuplicateMovesTest
{
	/** The number of playouts to run. */
	final int NUM_PLAYOUTS = 1;

	@Test
	public void test()
	{
		// Compilation of all the games.
		boolean duplicateMove = false;
		final String[] allGameNames = FileHandling.listGames();
		for (int index = 0; index < allGameNames.length; index++)
		{
			final String gameName = allGameNames[index];

			final String name = gameName.substring(gameName.lastIndexOf('/') + 1, gameName.length());

			// if (!name.equals("Chex.lud"))
			// continue;

			if (FileHandling.shouldIgnoreLudAnalysis(gameName))
				continue;

			System.out.println("Compilation of : " + gameName);
			final Game game = GameLoader.loadGameFromName(gameName);

			if (game.hasSubgames() || game.isDeductionPuzzle() || game.isSimulationMoveGame())
				continue;

			for (int i = 0; i < NUM_PLAYOUTS; i++)
			{
				boolean duplicateMoveInPlayout = false;
				final List<AI> ais = new ArrayList<AI>();
				ais.add(null);
				for (int p = 1; p <= game.players().count(); ++p)
					ais.add(new utils.RandomAI());

				final Context context = new Context(game, new Trial(game));
				final Trial trial = context.trial();
				game.start(context);

				for (int p = 1; p <= game.players().count(); ++p)
					ais.get(p).initAI(game, p);

				final Model model = context.model();

				while (!trial.over())
				{
					final int mover = context.state().mover();
					final Phase currPhase = game.rules().phases()[context.state().currentPhase(mover)];
					final Moves legal = currPhase.play().moves().eval(context);

					for (int j = 0; j < legal.moves().size(); j++)
					{
						final Move m1 = legal.moves().get(j);

						for (int k = j + 1; k < legal.moves().size(); k++)
						{

							final Move m2 = legal.moves().get(k);

							if (Model.movesEqual(m1, m2, context))
							{
								duplicateMove = true;
								if (!duplicateMoveInPlayout)
									System.err.println("DUPLICATE move in " + name);
								duplicateMoveInPlayout = true;
							}
						}
					}

					model.startNewStep(context, ais, 1.0);
				}
			}
		}

		if (duplicateMove)
			fail();
	}

}
