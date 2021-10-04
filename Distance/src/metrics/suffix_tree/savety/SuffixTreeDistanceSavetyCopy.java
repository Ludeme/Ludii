//package metrics.suffix_tree.savety;
//
//import java.util.HashMap;
//import java.util.List;
//
//import common.DistanceUtils;
//import common.LudRul;
//import common.Score;
//import game.Game;
//import metrics.DistanceMetric;
//import metrics.suffix_tree.Alphabet;
//import metrics.suffix_tree.Letteriser;
//import metrics.suffix_tree.SuffixTree;
//import metrics.suffix_tree.SuffixTreeCollapsed;
//import metrics.suffix_tree.TreeDistancer;
//import util.Trial;
//
//public class SuffixTreeDistanceSavetyCopy implements DistanceMetric
//{
//	HashMap<LudRul, SuffixTree> trees = new HashMap<>();
//	HashMap<LudRul, String[]> words = new HashMap<>();
//	HashMap<LudRul, Alphabet> alphabets = new HashMap<>();
//	private final int numPlayouts;
//	private final int numMaxMoves;
//	private final Letteriser letteriser;
//	private final TreeDistancer td; 
//	
//	public SuffixTreeDistanceSavetyCopy(final Letteriser letteriser, final int numPlayouts, final int numMaxMoves)
//	{
//		this.td = TreeDistancer.naiveDistance; 
//		this.letteriser = letteriser;
//		this.numPlayouts = numPlayouts;
//		this.numMaxMoves = numMaxMoves;
//	}
//	
//	@Override
//	public void releaseResources() {
//		trees.clear();
//	}
//	
//	@Override
//	public Score distance(final Game gameA, final Game gameB)
//	{
//		final SuffixTree treeA = getTreeFromGame(gameA,"",numPlayouts,numMaxMoves, letteriser);
//		final SuffixTree treeB = getTreeFromGame(gameB,"",numPlayouts,numMaxMoves, letteriser);
//		return distance(treeA, treeB);
//	}
//
//	@Override
//	public Score distance(final LudRul gameA, final LudRul gameB)
//	{
//		SuffixTreeExpanded treeA = trees.get(gameA);
//		if (treeA==null) {
//			treeA = getTreeFromGame(gameA.getGame(),gameA.getGameNameIncludingOption(true),numPlayouts,numMaxMoves, letteriser);
//			
//			trees.put(gameA, treeA);
//		}
//		SuffixTreeExpanded treeB = trees.get(gameB);
//		if (treeB==null) {
//			treeB = getTreeFromGame(gameB.getGame(),gameB.getGameNameIncludingOption(true),numPlayouts,numMaxMoves, letteriser);
//			//DistanceUtils.serialise(treeB, DistanceUtils.resourceTmpFolder.getAbsolutePath() +"/"+ treeB.getName() + ".ser", true);
//			trees.put(gameB, treeB);
//		}
//		final long t1 = System.currentTimeMillis();
//		final Score d = distance(treeA,treeB);
//		final long t2 = System.currentTimeMillis();
//		final long dt = t2- t1;
//		System.out.println("calc distance took: " + dt + " distance: " + d.score());
//		return d;
//	}
//
//	private Score distance(final SuffixTreeExpanded treeA, final SuffixTreeExpanded treeB)
//	{
//		return new Score(td.distance(treeA, treeB));
//		
//	}
//
//	public static SuffixTree getTreeFromGame(final Game game, final String name, final int numPlayoutsL, final int numMaxMovesL, final Letteriser let)
//	{
//		final Trial[] trials = DistanceUtils.generateRandomTrialsFromGame(game, numPlayoutsL, numMaxMovesL);
//		final SuffixTree st = new SuffixTreeCollapsed(name,trials, let);
//		
//		return st;
//	}
//
//	@Override
//	public Score distance(
//			final Game gameA, final List<Game> gameB, final int numberTrials, final int maxTurns,
//			final double thinkTime, final String AIName
//	)
//	{
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
