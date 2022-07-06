package search.minimax;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import game.Game;
import main.collections.FVector;
import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;
import other.state.State;
import utils.data_structures.ScoredMove;
import utils.data_structures.transposition_table.TranspositionTableBFS.BFSTTData;

public class NaiveActionBasedSelection extends LazyUBFM
{

	protected Move BFSSelection
	(
			final Game game,
			final Context context,
			final double maxSeconds,
			final int depthLimit
	)
	{
		final long startTime = System.currentTimeMillis();
		long stopTime = (maxSeconds > 0.0) ? startTime + (long) (maxSeconds * 1000) : Long.MAX_VALUE;
		
		final int numPlayers = game.players().count();
		currentRootMoves = new FastArrayList<Move>(game.moves(context).moves());
		final int numRootMoves = currentRootMoves.size();
		
		if (numRootMoves == 1)
		{
			// play faster if we only have one move available anyway
			if (autoPlaySeconds >= 0.0 && autoPlaySeconds < maxSeconds)
				stopTime = startTime + (long) (autoPlaySeconds * 1000);
		}
		
		// Vector for visualisation purposes
		rootValueEstimates = new FVector(currentRootMoves.size());
		rootMovesScores = new float[currentRootMoves.size()];

		final int maximisingPlayer = context.state().playerToAgent(context.state().mover());
		final State state = context.state();
		final int mover = state.playerToAgent(state.mover());
		
		if (numPlayers > 2)
			throw new RuntimeException("BFS not implemented for more than 2 players");
		
		rootAlphaInit = ALPHA_INIT;
		rootBetaInit = BETA_INIT;
		
		// For visualisation purpose:
		minHeuristicEval = rootBetaInit;
		maxHeuristicEval = rootAlphaInit;
		
		// To ouput a visual graph of the search tree:
		searchTreeOutput.setLength(0);
		searchTreeOutput.append("[\n");
		
		// Calling the recursive minimaxBFS strategy:
		long zobrist = context.state().fullHash();
		
		final Context contextCopy = copyContext(context);

		zobrist = context.state().fullHash();
		
		List<Long> initialNodeLabel = new ArrayList<Long>();
		initialNodeLabel.add(contextCopy.state().fullHash());
		if (savingSearchTreeDescription)
			searchTreeOutput.append("("+stringOfNodeLabel(initialNodeLabel)+","+Float.toString(getContextValue(contextCopy,maximisingPlayer,initialNodeLabel,0))+","+Integer.toString((mover==maximisingPlayer)? 1:2)+"),\n");
		
		firstTurn = false;
		estimatedHeuristicScoresRange = 1;
		estimatedActionLogitRange = 1;
		estimatedActionLogitMean = 1;
		// Only one call in this variant
		minimaxBFS(contextCopy,maximisingPlayer,stopTime,1,depthLimit,initialNodeLabel);
		
		zobrist = context.state().fullHash();
		final BFSTTData tableData = transpositionTable.retrieve(zobrist);
		final ScoredMove finalChoice = finalDecision(tableData, mover==maximisingPlayer);
		
		analysisReport = friendlyName + " (player " + maximisingPlayer + ") completed an analysis that reached at some point a depth of " + maxDepthReached + ":\n";
		analysisReport += "best value observed: "+Float.toString(finalChoice.score)+",\n";
		analysisReport += Integer.toString(nbStatesEvaluated)+" different states were evaluated";
		analysisReport += "\n"+Integer.toString(callsOfMinimax)+" calls of minimax";
		
		
		if ((maxSeconds > 0.)&&(System.currentTimeMillis()<stopTime))
				analysisReport += " (finished analysis early) ";
		
		if (resetTTeachTurn) {
			transpositionTable.deallocate();
			System.out.println("deallocated");
		}
		
		if (debugDisplay)
		{
			System.out.print("rootValueEstimates: (");
			for (int i=0; i<currentRootMoves.size(); i++) {
					System.out.print(rootValueEstimates.get(i)+".");
			}
			System.out.println(")");
		}
		
		// To ouput a visual graph of the search tree:
		searchTreeOutput.append("]");
		if (savingSearchTreeDescription)
		{
			try {
		      FileWriter myWriter = new FileWriter("/home/cyprien/Documents/M1/Internship/"+treeSaveFile);
		      myWriter.write(searchTreeOutput.toString());
		      myWriter.close();
		      System.out.println("Successfully saved search tree in a file.");
		    } catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		}
//		try {
//			FileWriter myWriter = new FileWriter("/home/cyprien/Documents/M1/Internship/average_recursive_calls/"+this.getClass().getSimpleName()+".sav");
//			myWriter.write(Double.toString(((double) callsOfMinimax)/callsOfSelectAction));
//		    myWriter.close();
//		} catch (IOException e) {
//		      System.out.println("An error occurred.");
//		      e.printStackTrace();
//		}
		
		return finalChoice.move;
	}
}
