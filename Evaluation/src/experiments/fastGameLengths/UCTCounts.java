package experiments.fastGameLengths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import game.Game;
import game.rules.play.moves.Moves;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.model.Model;
import other.trial.Trial;
import search.mcts.MCTS;

//-----------------------------------------------------------------------------

/**
 * Experiments to test number of visits per move for low iteration counts.
 * @author cambolbro
 */
public class UCTCounts
{		
	//-------------------------------------------------------------------------

	/**
	 * @param game The game to test.
	 */
	@SuppressWarnings("static-method")
	void runUCT(final Game game) 
	{
		final List<AI> ais = new ArrayList<>();
		ais.add(null);  // null placeholder for player 0
		ais.add(MCTS.createUCT());
		ais.add(MCTS.createUCT());
		for (int p = 1; p <= game.players().count(); ++p)
		{
			((MCTS)ais.get(p)).setTreeReuse(false);
			ais.get(p).initAI(game, p);
		}
			
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
	
		game.start(context);

		final Model model = context.model();
		
		int turn = 0;
		while (!trial.over())
		{
			System.out.println("======================\nTurn " + turn + ":");
			
			final Moves moves = game.moves(context);
			final int bf = moves.count();
			
			System.out.println("State has " + bf + " moves...");

			for (int n = 0; n < 10; n++)
			{
				final int iterations = bf * (int)Math.pow(2, n);
				System.out.print("n=" + n + " (" + iterations + " it.s):");
				model.startNewStep(context, ais, -1, iterations, -1, 0);
			}
			turn++;
		}
		System.out.println("Result is: " + trial);
	}
	
	//-------------------------------------------------------------------------
		
	void test()
	{
		final Game game = GameLoader.loadGameFromName("Breakthrough.lud", Arrays.asList("Board Size/7x7"));
		
		System.out.println("==================================================");
		//System.out.println("Loaded game " + game.name() + ", " + gameName.expected() + " moves expected.");
		
		runUCT(game);
	}
		
	//-------------------------------------------------------------------------
	
	public static void main(final String[] args)
	{
		final UCTCounts app = new UCTCounts();
		app.test();
	}

	//-------------------------------------------------------------------------

}
