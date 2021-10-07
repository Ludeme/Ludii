package metrics.individual;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import common.LudRul;
import common.Score;
import game.Game;
import main.grammar.Description;
import metrics.DistanceMetric;
import utils.data_structures.support.zhang_shasha.Tree;

/**
 * Returns Zhang-Shasha tree edit distance.
 * 
 * @author cambolbro
 */
public class ZhangShasha implements DistanceMetric
{
	static HashMap<LudRul, Tree> trees = new HashMap<>();
	static HashMap<LudRul, Integer> numTokens = new HashMap<>();

	@Override
	public Score distance(final Game gameA, final Game gameB)
	{
		final int numTokensA = gameA.description().tokenForest().tokenTree()
				.count();
		final int numTokensB = gameB.description().tokenForest().tokenTree()
				.count();
		final int maxTokens = Math.max(numTokensA, numTokensB);
		// System.out.println("numTokensA=" + numTokensA + ", numTokensB=" +
		// numTokensB
		// + ".");

		String strA = gameA.description().tokenForest().tokenTree()
				.formatZhangShasha("", 0, false, true);
		strA = format(strA);
		// System.out.println("strA:\n" + strA);

		String strB = gameB.description().tokenForest().tokenTree()
				.formatZhangShasha("", 0, false, true);
		strB = format(strB);
		// System.out.println("strB:\n" + strB);

		if (strA.equals(strB))
			return new Score(0); // same string

		Tree treeA = null;
		Tree treeB = null;
		try
		{
			treeA = new Tree(strA);
			treeB = new Tree(strB);
		} catch (final IOException e)
		{
			e.printStackTrace();
		}

		final int edits = Tree.ZhangShasha(treeA, treeB);

//		final int edits = (numTokensA > numTokensB)
//						  ? Tree.ZhangShasha(treeA, treeB)
//						  : Tree.ZhangShasha(treeB, treeA);
		// System.out.println("edits=" + edits + ".");

//		final int minLength =   Math.min
//								(
//									gameA.gameDescription().expandedDescription().length(), 
//									gameB.gameDescription().expandedDescription().length()
//								);		

		final double score = Math.min(1, (double) edits / maxTokens); // minLength;

		return new Score(score);
	}

	private static String format(final String strIn)
	{
		String str = new String(strIn);

		str = str.replaceAll("==\\(", "equals\\(");
		str = str.replaceAll("!=\\(", "notEquals\\(");
		str = str.replaceAll("<=\\(", "le\\(");
		str = str.replaceAll(">=\\(", "ge\\(");
		str = str.replaceAll("<\\(", "lt\\(");
		str = str.replaceAll(">\\(", "gt\\(");
		str = str.replaceAll("=\\(", "setEquals\\(");
		// str = str.replaceAll(":", "_");
		str = str.replaceAll(":", "");

		while (str.indexOf("( ") >= 0)
			str = str.replaceAll("\\( ", "\\(");

		while (str.indexOf(" )") >= 0)
			str = str.replaceAll(" \\)", "\\)");

		while (str.indexOf("()") >= 0)
			str = str.replaceAll("\\(\\)", "");

		return str;
	}

	@Override
	public Score distance(final LudRul candidate, final LudRul target)
	{
		Tree treeA = trees.get(candidate);
		int tokenCountA = numTokens.get(candidate).intValue();
		Tree treeB = trees.get(target);
		int tokenCountB = numTokens.get(target).intValue();

		if (treeA == null)
		{
			final Description desc = candidate.getDescription();
			tokenCountA = desc.tokenForest().tokenTree().count();
			String strA = desc.tokenForest().tokenTree().formatZhangShasha("",
					0, false, true);
			strA = format(strA);
			try
			{
				treeA = new Tree(strA);
			} catch (final IOException e)
			{

				e.printStackTrace();
			}
			trees.put(candidate, treeA);
			numTokens.put(candidate, Integer.valueOf(tokenCountA));
		}
		if (treeB == null)
		{
			final Description desc = target.getDescription();
			tokenCountB = desc.tokenForest().tokenTree().count();
			String strB = desc.tokenForest().tokenTree().formatZhangShasha("",
					0, false, true);
			strB = format(strB);
			try
			{
				treeB = new Tree(strB);
			} catch (final IOException e)
			{

				e.printStackTrace();
			}
			trees.put(target, treeB);
			numTokens.put(target, Integer.valueOf(tokenCountB));
		}
		final int edits = Tree.ZhangShasha(treeA, treeB);

		final double maxTokens = Math.max(tokenCountA, tokenCountB);

		final double score = Math.min(1, edits / maxTokens); // minLength;

		return new Score(score);
	}

	@Override
	public void releaseResources()
	{
		trees.clear();
		numTokens.clear();
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
	public String getName()
	{
		return "ZhangShasha";
	}

	@Override
	public Score distance(final String description1, final String description2)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DistanceMetric getDefaultInstance()
	{
		return new ZhangShasha();
	}
}
