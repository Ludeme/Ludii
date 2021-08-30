package app.tutorialVisualisation;

import java.util.List;

import app.PlayerApp;
import manager.Referee;
import other.move.Move;
import other.trial.Trial;

public class MoveVisualisation
{
	public static void moveVisualisation(final PlayerApp app)
	{
		app.restartGame();
		final Referee ref = app.manager().ref();
		
		// Carry out a random playout to obtain our trial
		ref.randomPlayout(app.manager());
		final Trial trial = ref.context().trial();
		final List<Move> completeMoveList = trial.generateCompleteMovesList();
		
		for (int i = trial.numInitialPlacementMoves(); i < completeMoveList.size(); i++)
		{
			final Move m = completeMoveList.get(i);
			System.out.println(m.actionType());
			System.out.println(m.actions());
			System.out.println(m.direction(ref.context()));
			System.out.println(m.what());
		}
	}
}
