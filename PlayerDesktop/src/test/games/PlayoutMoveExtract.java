package test.games;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import game.Game;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;
import utils.LudiiAI;
import utils.RandomAI;

/**
 * -
 * 
 * @author matthew.stephenson
 */
public class PlayoutMoveExtract
{

	@Test
	public void test()
	{
		final File gameFile = new File("../Common/res/lud/board/space/line/Tic-Tac-Toe.lud");

		for(int i = 0; i<1; i++) 
		{
			System.out.println("Game " + i);
			final Game game = GameLoader.loadGameFromFile(gameFile);

			final Trial trial = new Trial(game);
			final Context context = new Context(game, trial);

			game.start(context);

			final List<AI> ai_players = new ArrayList<AI>();

			final AI ai1 = new LudiiAI();
			final AI ai2 = new RandomAI();

			ai1.initAI(game, 0);
			ai2.initAI(game, 1);

			ai_players.add(null);
			ai_players.add(ai1);
			ai_players.add(ai2);

			final Trial output = game.playout(context, ai_players, 1.0, null, 0, -1, ThreadLocalRandom.current());

			final List<Move> move_list = output.generateCompleteMovesList();

			for (final Move move : move_list) 
			{
				if (move.mover() != 0) 
				{
					if(move.actions().size() > 1) 
					{
						System.out.print("Extra ");
						System.out.println(move.toTurnFormat(context, true));

					} 
					else 
					{
						System.out.print("Move ");
						System.out.println(move.toTurnFormat(context, true));
					}
				}
			}
			System.out.println(context.winners() + "\n");
		}
	}

}
