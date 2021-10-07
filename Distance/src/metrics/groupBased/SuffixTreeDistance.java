package metrics.groupBased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import common.DistanceUtils;
import common.LudRul;
import common.Score;
import game.Game;
import metrics.DistanceMetric;
import metrics.GroupBased;
import metrics.MoveBased;
import metrics.suffix_tree.Letteriser;
import metrics.suffix_tree.SuffixTreeCollapsed;
import metrics.suffix_tree.TreeBuildingIngredients;
import metrics.suffix_tree.TreeDistancer;
import other.trial.Trial;
import utils.data_structures.support.DistanceProgressListener;

public class SuffixTreeDistance implements DistanceMetric,GroupBased,MoveBased
{
	HashMap<LudRul, SuffixTreeCollapsed> lastTwoTrees = new HashMap<>();
	HashMap<LudRul, TreeBuildingIngredients> treeIngredients = new HashMap<>();
	
	private final int numPlayouts;
	private final int numMaxMoves;
	private final Letteriser letteriser;
	private final TreeDistancer td; 
	
	public SuffixTreeDistance(final Letteriser letteriser, final int numPlayouts, final int numMaxMoves)
	{
		td = TreeDistancer.jaccardDistance; 
		this.letteriser = letteriser;
		this.numPlayouts = numPlayouts;
		this.numMaxMoves = numMaxMoves;
	}
	
	@Override
	public void releaseResources() {
		treeIngredients.clear();
		lastTwoTrees.clear();
	}
	
	@Override
	public Score distance(final Game gameA, final Game gameB)
	{
		final SuffixTreeCollapsed treeA = getTreeFromGame(gameA,"",numPlayouts,numMaxMoves, letteriser);
		final SuffixTreeCollapsed treeB = getTreeFromGame(gameB,"",numPlayouts,numMaxMoves, letteriser);
		return distance(treeA, treeB);
	}

	@Override
	public Score distance(final LudRul gameA, final LudRul gameB)
	{
		final SuffixTreeCollapsed stA = getTree(gameA);
		final SuffixTreeCollapsed stB = getTree(gameB);
		lastTwoTrees.clear();
		lastTwoTrees.put(gameA, stA);
		lastTwoTrees.put(gameB, stB);
		
		
		final long t1 = System.currentTimeMillis();
		 final Score d = distance(stA,stB);
		//final Score d = new Score(Math.random());
		final long t2 = System.currentTimeMillis();
		final long dt = t2- t1;
		System.out.println("calc distance took: " + dt + " distance: " + d.score());
		// Get the Java runtime
	    final Runtime runtime = Runtime.getRuntime();
	    // Run the garbage collector
	    runtime.gc();
	    // Calculate the used memory
	    final long memory = runtime.totalMemory() - runtime.freeMemory();
	    System.out.println("Used memory is bytes: " + memory);
	    System.out.println("Used memory is megabytes: "
	                + (memory)/1024/1024);
		return d;
	}
	
	@Override
	public String getName()
	{
		return "SuffixTreeDistance";
	}

	private SuffixTreeCollapsed getTree(final LudRul game)
	{
		SuffixTreeCollapsed tre = lastTwoTrees.get(game);
		if (tre!=null)return tre;
		TreeBuildingIngredients treeIngred = treeIngredients.get(game);
		if (treeIngred==null) {
			treeIngred = getTreeIngredientsFromGame(game,numPlayouts,numMaxMoves, letteriser);
			treeIngredients.put(game, treeIngred);
		}
		tre = new SuffixTreeCollapsed(game.getGameNameIncludingOption(true),treeIngred);
		lastTwoTrees.put(game, tre);
		return tre;
	}

	private static TreeBuildingIngredients getTreeIngredientsFromGame(
			final LudRul game, final int numPlayouts2, final int numMaxMoves2,
			final Letteriser let
	)
	{
		final Trial[] trials = DistanceUtils.generateRandomTrialsFromGame(game.getGame(), numPlayouts2, numMaxMoves2);
		return let.createTreeBuildingIngredients(game.getGame(),trials);
	}

	private Score distance(final SuffixTreeCollapsed treeA, final SuffixTreeCollapsed treeB)
	{
		return new Score(td.distance(treeA, treeB));
		
	}

	public static SuffixTreeCollapsed getTreeFromGame(final Game game, final String name, final int numPlayoutsL, final int numMaxMovesL, final Letteriser let)
	{
		final Trial[] trials = DistanceUtils.generateRandomTrialsFromGame(game, numPlayoutsL, numMaxMovesL);
		final SuffixTreeCollapsed st = new SuffixTreeCollapsed(name,null, trials, let);
		
		return st;
	}

	@Override
	public Score distance(
			final Game gameA, final List<Game> gameB, final int numberTrials, final int maxTurns,
			final double thinkTime, final String AIName
	)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Score distance(final String description1, final String description2)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DistanceMetric showUserSelectionDialog()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DistanceMetric getPlaceHolder()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DistanceMetric getDefaultInstance()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInitialized(ArrayList<LudRul> candidates)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean typeNeedsToBeInitialized()
	{
		
		return true;
	}

	@Override
	public void init(final ArrayList<LudRul> candidates,boolean forceRecalculation, final DistanceProgressListener dpl) {
		// nothing ?
	}
}
