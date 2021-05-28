package utils.concepts;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import game.Game;
import game.rules.end.End;
import game.rules.end.EndRule;
import game.rules.phase.Phase;
import game.rules.play.moves.Moves;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import main.FileHandling;
import other.AI;
import other.GameLoader;
import other.concept.Concept;
import other.concept.ConceptType;
import other.context.Context;
import other.model.Model;
import other.move.Move;
import other.trial.Trial;

/**
 * Method to print the state concepts of a game (run with a certain number of
 * playouts).
 *
 * @author Eric Piette
 */
public class ComputeStateConcepts
{
	/**
	 * Main method.
	 * 
	 * @param args
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static void main(final String[] args) throws IllegalArgumentException, IllegalAccessException
	{
		final long startTime = System.currentTimeMillis();

		// Compilation of all the games.
		final String[] allGameNames = FileHandling.listGames();
		for (int index = 0; index < allGameNames.length; index++)
		{
			final String gameName = allGameNames[index];

			final String name = gameName.substring(gameName.lastIndexOf('/') + 1, gameName.length());

			if (!name.equals("Chex.lud"))
				continue;

			if (FileHandling.shouldIgnoreLudAnalysis(gameName))
				continue;

			System.out.println("Compilation of : " + gameName);
			final Game game = GameLoader.loadGameFromName(gameName);

			final int numPlayouts = 1;

			final TDoubleArrayList frequencyMoveConcepts = new TDoubleArrayList();

			final TDoubleArrayList frenquencyPlayouts = new TDoubleArrayList();
			for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
				frenquencyPlayouts.add(0.0);

			for (int i = 0; i < numPlayouts; i++)
			{
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

				final TDoubleArrayList frenquencyPlayout = new TDoubleArrayList();
				for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
					frenquencyPlayout.add(0);

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

					// Compute avg for each playout.
					for (int j = 0; j < frenquencyTurn.size(); j++)
						frenquencyPlayout.set(j, frenquencyPlayout.get(j) + (numLegalMoves == 0 ? 0 : frenquencyTurn.get(j) / numLegalMoves));

					prevContext = new Context(context);
					model.startNewStep(context, ais, 1.0);
				}

				// Compute avg for all the playouts.
				for (int j = 0; j < frenquencyPlayout.size(); j++)
					frenquencyPlayouts.set(j, frenquencyPlayouts.get(j) + frenquencyPlayout.get(j) / turnWithMoves);

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
			}


			// Compute avg for the game.
			for (int i = 0; i < frenquencyPlayouts.size(); i++)
				frequencyMoveConcepts.add(frenquencyPlayouts.get(i) / numPlayouts);

			System.out.println("RESULTS ARE: \n");

			final StringBuffer sb = new StringBuffer();

			// Print results.
			for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
			{
				final Concept concept = Concept.values()[indexConcept];
				if (frequencyMoveConcepts.get(indexConcept) > 0)
					sb.append("CONCEPT " + concept.name() + ", frequency = "
									+ new DecimalFormat("##.##").format(frequencyMoveConcepts.get(indexConcept) * 100)
							+ "%\n");
			}

			System.out.println(sb.toString());

			final double allSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
			final int seconds = (int) (allSeconds % 60.0);
			final int minutes = (int) ((allSeconds - seconds) / 60.0);
			System.out.println("Done in " + minutes + " minutes " + seconds + " seconds");

		}
	}

}
