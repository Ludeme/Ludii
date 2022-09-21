package core.algorithm;

import ai.player.Darkboard;
import core.EvaluationFunction;
import core.EvaluationTree;
import core.Move;

public class EvaluationTableAlgorithm extends core.algorithm.SelectionAlgorithm {

	Darkboard p;
	int maxPositions = 5000;
	
	/*public void minimax(Metaposition start, Move m, float startingValue, int maxPositions, boolean topLevel, Metaposition precalcChessboard, EvaluationTree father, EvaluationTree current,EvaluationFunction ef)
	{
		Metaposition evolve; 
		Metaposition evolve2;
		float minv;
		EvaluationTree tree = (current!=null? current : (EvaluationGlobals.usingExtendedGameTree? new ExtendedEvaluationTree() : new EvaluationTree()));
		if (EvaluationGlobals.usingExtendedGameTree) EvaluationFunction.currentNode = (ExtendedEvaluationTree)tree;
		//EvaluationFunction ef = findAppropriateFunction(start);
		
		if (ef==null) return;
		
		tree.m = m;
		//tree.sc = precalcChessboard;
		if (!topLevel)
		{
			evolve = (precalcChessboard==null? ef.generateMostLikelyEvolution(start,m) : precalcChessboard);
			evolve2 = evolve.generateMostLikelyOpponentMove(m);
			tree.staticvalue = startingValue;
			tree.sc = precalcChessboard;
		} else
		{
			evolve2 = start;
			startingValue = p.evaluate(evolve2,null,evolve2,ef);
			tree.sc = start;
			//tree.staticvalue = tree.value = startingValue;
		}
		
		Vector v = evolve2.generateMoves(topLevel,p);
		Vector newMoves = new Vector();
		Vector oldMoves = new Vector();
		
		//separate the moves: those which already have a place in the evaluation table
		//and those which don't.
		for (int k=0; k<v.size(); k++)
		{
			Move mv = (Move)v.get(k);
			MoveEvaluation me = p.getEvaluation(mv);
			if (me!=null) {
				oldMoves.add(me); me.firstEvolution = ef.generateMostLikelyEvolution(evolve2,mv); } 
				else 
				{
					ExtendedEvaluationTree t=null;
					if (EvaluationGlobals.usingExtendedGameTree)
					{
						t = new ExtendedEvaluationTree();
						EvaluationFunction.currentNode = t;
					}
					Metaposition next = ef.generateMostLikelyEvolution(evolve2,mv);
					float newMoveEval = p.evaluate(evolve2,mv,next,ef);
					me = p.addEvaluation(mv,newMoveEval-startingValue,startingValue,next);
					me.staticValue = newMoveEval;
					me.tree = t;
					newMoves.add(me);
				} 
		}
		
		//every evaluation is one position we had to compute. Subtract it.
		int newSize = (newMoves.size()>EvaluationGlobals.pruningNewMovesAmount? EvaluationGlobals.pruningNewMovesAmount : newMoves.size());
		int otherPositions = (oldMoves.size()>EvaluationGlobals.pruningOldMovesAmount? EvaluationGlobals.pruningOldMovesAmount : oldMoves.size());
		int totalBranches = newSize + otherPositions;
		maxPositions -= newMoves.size()+otherPositions;
		int positionsForEach = (totalBranches>0? maxPositions / totalBranches : 0);
		Vector chessboardStorage = new Vector();
		
		Collections.sort(oldMoves,p.listSorter);
		Collections.sort(newMoves,p.listSorter);
		
		//put the chessboard evolutions into the vector, or they will be overwritten by recursive calls... found this the hard way...
		for (int k=0; k<newSize; k++) chessboardStorage.add(((MoveEvaluation)newMoves.get(k)).firstEvolution);
		for (int k=0; k<otherPositions; k++) chessboardStorage.add(((MoveEvaluation)oldMoves.get(k)).firstEvolution);

		float value, avgvalue, minvalue, currentMinValue;
		value = avgvalue = -100000000;
		minvalue = 0.0f;
		if (startingValue<minvalue) minvalue = startingValue;
		currentMinValue = minvalue;
		Move bestMove = null;
		
		float minValForExpansion = ef.getMinValueForExpansion();
		float maxValForExpansion = ef.getMaxValueForExpansion();
		
		for (int k=0; k<totalBranches; k++)
		{
			MoveEvaluation eval = (k<newSize? ((MoveEvaluation)newMoves.get(k)) :
			((MoveEvaluation)oldMoves.get(k-newSize)));
			Move testedMove = eval.move;
			//Metaposition nextEvolution = eval.firstEvolution;
			Metaposition nextEvolution = (Metaposition) chessboardStorage.get(k);
			//if it's an old move (already in hash table) we re-evaluate it.
			float staticValue = 0.0f;
			if (k<newSize)
			{
				staticValue = eval.staticValue;
			}
			else
			{
				if (EvaluationGlobals.usingExtendedGameTree)
				{
					eval.tree = new ExtendedEvaluationTree();
					EvaluationFunction.currentNode = eval.tree;
				} 
				staticValue = p.evaluate(evolve2,testedMove,nextEvolution);
				eval.staticValue = staticValue;
			}
			
			if (positionsForEach>1 && staticValue>=minValForExpansion && staticValue<=maxValForExpansion) //don't expand positions that are very bad or very good
			{
				minimax(evolve2,testedMove,staticValue,positionsForEach,false,nextEvolution,tree,eval.tree,ef);
			}
			else
			{
				//EvaluationTree et = new EvaluationTree();
				EvaluationTree et = eval.tree;
				if (et==null) et = new EvaluationTree();
				et.m = testedMove;
				et.staticvalue = staticValue;
				//et.sc = precalcChessboard;
				et.sc = eval.firstEvolution;
				et.value = staticValue;
				tree.addChild(et);	
			}
		}
		
		avgvalue = -1000000;
		for (int k=0; k<tree.getChildNumber(); k++)
		{
			//examine each child in turn...
			EvaluationTree pick = tree.getChild(k);
			float averageValue = pick.value;
			if (averageValue > avgvalue)
			{
				avgvalue = averageValue;
				bestMove = pick.m;
			}
		}
		
		//tree.value = avgvalue;
		tree.value = (tree.staticvalue)*p.alpha+avgvalue*p.oneMinusAlpha;
		tree.bestChild = bestMove;
		if (father!=null) father.addChild(tree);
			
		if (topLevel) 
		{
			p.minimaxBestMove = bestMove;
			p.minimaxPositionValue = avgvalue;
			p.minimaxMinValue = currentMinValue;
			p.bestMoveValue = avgvalue;
			EvaluationGlobals.moveTree = tree;
		} 
	}*/
	
	public Move select(Darkboard pl, EvaluationTree et)
	{
		p = pl;
		pl.globals.evaluatedPositions = 0;
		pl.globals.evaluationTable.clear();
		pl.globals.evaluationTableSize = 0;
		pl.globals.currentEvaluator = (p.isWhite? pl.globals.whiteEvaluator : pl.globals.blackEvaluator);
		p.simplifiedBoard.setAge((byte)0);
		
		EvaluationFunction ef = p.findAppropriateFunction(p.simplifiedBoard);
		maxPositions *= ef.getNodeMultFactor(p.simplifiedBoard);
		
		/*if (ef.useKillerPruning())*/ //minimax(p.simplifiedBoard,null,0.0f,maxPositions,true,null,null,null,ef);
		//else exhaustiveMinimax(p.simplifiedBoard,null,0.0f,maxPositions,true,null,null,null);
		
		if (pl.globals.OUTPUT_MOVE_TREE && pl.globals.moveTree!=null) System.out.println(pl.globals.moveTree.getBestSequence());
		if (pl.globals.OUTPUT_MOVE_TREE && pl.globals.SAVE_MOVE_TREE_TO_DISK && pl.globals.moveTree!=null) pl.globals.moveTree.saveToFile();
		pl.globals.bestMoveValue = p.minimaxPositionValue;
		return p.minimaxBestMove;
	}
	
}
