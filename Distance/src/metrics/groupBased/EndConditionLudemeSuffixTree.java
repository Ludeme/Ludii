package metrics.groupBased;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.TreeMap;

import common.DistanceMatrix;
import common.DistanceUtils;
import common.EvaluatorDistanceMetric;
import common.FolderLocations;
import common.LudRul;
import common.Score;
import common.StringCleaner;
import game.Game;
import metrics.DistanceMetric;
import metrics.GroupBased;
import metrics.suffix_tree.Alphabet;
import metrics.suffix_tree.ContainerSeperator;
import metrics.suffix_tree.Letter;
import metrics.suffix_tree.PercentageMap;
import metrics.suffix_tree.QuerryResult;
import metrics.suffix_tree.Seperator;
import metrics.suffix_tree.SuffixTreeCollapsed;
import metrics.support.DistanceProgressListener;
import processing.similarity_matrix.AssignmentSettings;
import processing.similarity_matrix.Visualiser;

/**
 * A distance metric based on the most likely ending condition between two games.
 * It builds a suffix tree of all end condition
 * @author Markus
 *
 */
public class EndConditionLudemeSuffixTree  implements DistanceMetric, GroupBased
{
	//private final SuffixTreeCollapsed suffixTree;
	private final HashMap<LudRul,PercentageMap<String>> probabilties = new HashMap<>();
	private HashMap<LudRul,String[]> endDescriptions;
	private DistanceMatrix<LudRul, LudRul> distanceMatrix;
	private boolean initialized = false;
	
	
	
	public static void main(final String[] args) {
		final ArrayList<LudRul> candidates = DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false,
						FolderLocations.boardFolder, null);
		//while (candidates.size()>20) {
			//candidates.remove(candidates.size()-1);
		//}
		final EndConditionLudemeSuffixTree eclst = new EndConditionLudemeSuffixTree(candidates);
		final DistanceMatrix<LudRul, LudRul> distanceMatrix;
		distanceMatrix = EvaluatorDistanceMetric.getDistanceMatrix(candidates,
				eclst, false, null);
		final AssignmentSettings ass = new AssignmentSettings(3, 3);
		final Visualiser v = new Visualiser(eclst.getName(),candidates, distanceMatrix, ass);
		v.setVisible(true);
		eclst.releaseResources();
	}

	@Override
	public boolean typeNeedsToBeInitialized() {
		return true;
	}
	
	public EndConditionLudemeSuffixTree(final ArrayList<LudRul> gameBase)
	{
		init(gameBase, false,null);
		
	}
	
	@Override
	public String getName()
	{
		return "EndConditionLudemeSuffixTree";
	}
	
	/**
	 * assign every game a likelyhood, which folder it belongs to
	 * @param gameBase
	 * @param stc
	 */
	private void assignProbabilities(final ArrayList<LudRul> gameBase, final SuffixTreeCollapsed stc)
	{
		final HashMap<File,List<LudRul>> sameGameDifferentRulesets = LudRul.getFamilyHashmap(gameBase);
		final HashMap<LudRul,Integer> ludRulIndexes = new HashMap<>();
		for (int i = 0; i < gameBase.size(); i++)
		{
			ludRulIndexes.put(gameBase.get(i),Integer.valueOf(i));
		}
		
		for (int i = 0; i < gameBase.size(); i++)
		{
			final LudRul ludRul = gameBase.get(i);
			final String[] end = endDescriptions.get(ludRul);
			if (end==null) {
				System.out.println("unpossible");
			}
			final ArrayList<Letter> code = stc.getAlphabet().encode(end);
			final TreeMap<Integer,Seperator> sp = stc.getAlphabet().getSeperator();
			
			final Seperator seperator = sp.get(Integer.valueOf(i));
			if (!(seperator instanceof ContainerSeperator<?>)) {
				System.out.println("unpossible");
			}
			@SuppressWarnings("unchecked")
			final
			ContainerSeperator<LudRul> correctSeperator = (ContainerSeperator<LudRul>) seperator;
			final ArrayList<Seperator> seperatorsToIgnor = new ArrayList<>();
			final List<LudRul> liste = sameGameDifferentRulesets.get(correctSeperator.getContainer().getFile());
			for (final LudRul ludRul2 : liste)
			{	
				final Seperator similarSeperator = sp.get(ludRulIndexes.get(ludRul2));
				seperatorsToIgnor.add(similarSeperator);
			}
			
			final boolean ignoreQuotationMarks = true;
			
			
			 ArrayList<QuerryResult> qrs = stc.findNode(code,0,ignoreQuotationMarks,seperatorsToIgnor);
			if (qrs.size()==0) {
				qrs = stc.findNode(code,0,ignoreQuotationMarks,seperatorsToIgnor);
			}
			final QuerryResult qr = qrs.get(0);
			final PercentageMap<String> map = qr.getLeaveTypePercentageMap();
			probabilties.put(ludRul, map);
			System.out.println("should: " + ludRul.getCurrentClassName() + " estimate: " + map.getHashMap().entrySet());
			System.out.print("");
		}
		//this.probabilties = new HashMap<>(,)
		
	}

	
	private SuffixTreeCollapsed buildSuffixTree(final ArrayList<LudRul> gameBaseSrc, DistanceProgressListener dpl)
	{
		endDescriptions = new HashMap<>();
		final ArrayList<LudRul> gameBase = new ArrayList<>(gameBaseSrc); //just to make sure the order stays the same during building. but shouldnt make a difference anyways.
		final StringCleaner cleaner = StringCleaner.cleanKeepUnderScoreAndColon;
		final int nInstances = gameBase.size();
		final String[][] keyWords = new String[nInstances][];
		for (int i = 0; i < nInstances; i++)
		{
			dpl.update(i, nInstances);
			System.out.println(i + " " + nInstances);
			final LudRul ludRul = gameBase.get(i);
			final String[] trimmed =  getToEndConditionDescriptionTrimmed(ludRul,cleaner);
			endDescriptions.put(ludRul, trimmed);
			keyWords[i] = trimmed;
		}
		final HashSet<String> wordList = new HashSet<>();
		for (final String[] strings : keyWords)
		{
			wordList.addAll(Arrays.asList(strings));
		}
		final Alphabet a = new Alphabet(wordList);
		a.overwritteSuffixes(gameBase);
		final SuffixTreeCollapsed st = new SuffixTreeCollapsed();
		st.setAlphabet(a);
		for (int i = 0; i < keyWords.length; i++)
		{
			System.out.println(i + " " + keyWords.length);
			final String[] string2 = keyWords[i];
			st.insertIntoTree(a.encode(string2));
		}
		
		st.assessAllOccurences();
		st.printSizes();
		
		st.exportGml(FolderLocations.outputTmpFolder, "wincondintions", false);
		dpl.update(nInstances, nInstances);
		return st;
	}


	private String[] getToEndConditionDescriptionTrimmed(final LudRul ludRul, final StringCleaner cleaner)
	{
		final String fullexpansion = ludRul.getDescriptionExpanded();
		
		final String fullexpansionFlattened = DistanceUtils.flatten(fullexpansion);
		//System.out.println(fullexpansionFlattened);
		
		final String[] fullFlattened =  cleaner.cleanAndSplit(fullexpansionFlattened);
		
		//search for end_ and _end
		int end_Pos = 0;
		int _endPos = 0;
		
		for (int j = fullFlattened.length-1; j >= 0 ; j--)
		{
			if (fullFlattened[j].equals("_end")) {
				_endPos = j;
			}
			if (fullFlattened[j].equals("end_")) {
				end_Pos = j;
			}
		}
		final int length = _endPos - end_Pos-1;
		final String[] trimmedFlattened = new String[length];
		System.arraycopy(fullFlattened, end_Pos+1, trimmedFlattened, 0, length);
		return trimmedFlattened;
	}

	

	@Override
	public Score distance(final LudRul gameA, final LudRul gameB)
	{
		if (distanceMatrix!=null)return new Score(distanceMatrix.get(gameA, gameB));
		//the distance is the sum of absolute differences between their percentages 
		
		final HashMap<String, Double> probA = probabilties.get(gameA).getHashMap();
		final HashMap<String,Double> probB = probabilties.get(gameB).getHashMap();
		double distance = 0.0;
		
		for (final Entry<String, Double> entry : probA.entrySet())
		{
			final double percentageA = entry.getValue().doubleValue();
			final Double pb = probB.get(entry.getKey());
			final double perceentageB;
			if (pb != null) perceentageB = pb.doubleValue();
			else perceentageB = 0.0;
			distance+=Math.abs(percentageA-perceentageB);
			
		}
		distance /=2.0;
		System.out.println(gameA.getGameNameIncludingOption(true) + "  " + probA.entrySet());
		System.out.println(gameB.getGameNameIncludingOption(true) + "  " + probB.entrySet());
		
		return new Score(distance);
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
	public Score distance(final Game gameA, final Game gameB)
	{
		// TODO Auto-generated method stub
		return null;
	}

	

	@Override
	public DistanceMetric getPlaceHolder()
	{
		EndConditionLudemeSuffixTree e = new EndConditionLudemeSuffixTree();
		
		return e;
	}
	
	private EndConditionLudemeSuffixTree()
	{
		
	}
	
	@Override
	public void init(final ArrayList<LudRul> candidates, boolean forceRecaluation,final DistanceProgressListener dpl) {
		final DistanceMatrix<LudRul, LudRul> distanceMatrixLoaded = EvaluatorDistanceMetric.loadDistanceMatrix(candidates,
				this);
		if (distanceMatrixLoaded!=null&&!forceRecaluation) {
			this.distanceMatrix = distanceMatrixLoaded;
			return;
		}
		this.distanceMatrix = null;
		final SuffixTreeCollapsed stc = buildSuffixTree(candidates,dpl); 
		assignProbabilities(candidates,stc);
		initialized = true;
	}

	@Override
	public boolean isInitialized(ArrayList<LudRul> candidates)
	{
		if (!initialized)return false;
		Set<LudRul> ks = distanceMatrix.getCandidateToIndex().keySet();
		if (ks.size()!=candidates.size())return false;
		if (!ks.containsAll(candidates))return false;
		
		return true;
	}

	@Override
	public DistanceMetric getDefaultInstance()
	{
		return createPlaceHolder();
	}

	public static DistanceMetric createPlaceHolder()
	{
		return new EndConditionLudemeSuffixTree();
	}
}
