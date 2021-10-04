
package metrics.groupBased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import common.CountMap;
import common.DistanceMatrix;
import common.EvaluatorDistanceMetric;
import common.LudRul;
import common.Score;
import common.StringCleaner;
import game.Game;
import metrics.DistanceMetric;
import metrics.GroupBased;
import metrics.support.DistanceProgressListener;

//-----------------------------------------------------------------------------

/**
 * Like JensanShannon but weights keywords by their overall occurences
 * 
 * @author Sofia, Markus
 */
public class JensenShannonTFIDFDivergence implements DistanceMetric, GroupBased
{
	public static final double LOG_2 = Math.log(2);
	
	
	private final HashMap<LudRul,TreeMap<String,Double>> idfOfT = new HashMap<>();


	private DistanceMatrix<LudRul, LudRul> distanceMatrix = null;


	private boolean initialized = false; 
	
	
	private JensenShannonTFIDFDivergence()
	{
		// placeHolder
	}

	@Override
	public String getName()
	{
		return "TFIDFDivergence";
	}
	
	@Override
	public Score
			distance(final LudRul candidate, final LudRul gameToCompareWith)
	{
		if (distanceMatrix!=null)return new Score(distanceMatrix.get(candidate, gameToCompareWith));
		final TreeMap<String, Double> distributionA = idfOfT.get(candidate);
		final TreeMap<String, Double> distributionB= idfOfT.get(gameToCompareWith);
		double cs = cosine_similarity(distributionA, distributionB);
		//final double dist = jensenShannonDivergence(distributionA ,
			//	distributionB);

		return new Score(cs);
	}

	
	private double cosine_similarity(
			TreeMap<String, Double> distributionA,
			TreeMap<String, Double> distributionB
	)
	{
		final Set<String> vocabulary = new HashSet<>(distributionA.keySet());
		vocabulary.addAll(distributionB.keySet());
		ArrayList<String> vocabularyIndexable = new ArrayList<>(vocabulary);
		
		double[] a = new double[vocabulary.size()];
		double[] b= new double[vocabulary.size()];
		
		for (int i = 0; i < vocabularyIndexable.size(); i++)
		{
			final String word = vocabularyIndexable.get(i);
			Double valA = distributionA.get(word);
			if (valA!=null)
			a[i] = valA.doubleValue();
			else
			a[i] = 0;
			Double valB = distributionB.get(word);
			if (valB!=null)
				b[i] = valB.doubleValue();
				else
				b[i] = 0;
		}
		
		return 1.0-cosine_similarity(a, b);
	}

	// -------------------------------------------------------------------------

	@Override
	public Score distance(final Game gameA, final Game gameB)
	{
		return null;
	}


	// -------------------------------------------------------------------------

	/**
	 * Cleans the string of special signs and then creates a HashMap of
	 * frequencies <word, count>
	 * 
	 * @param contentData ???
	 * @return A map of word frequency.
	 */
	public static Map<String, Integer> getFrequencies(final String contentData)
	{
		final StringCleaner sc = StringCleaner.cleanAll;
		final String[] words = sc.cleanAndSplit(contentData);
		final CountMap<String> cm = new CountMap<>();
		cm.countUnique(words);

		return cm.getHashMap();
	}

	// -------------------------------------------------------------------------



	// -------------------------------------------------------------------------

	/**
	 * Code inspired from:
	 * https://github.com/mimno/Mallet/blob/master/src/cc/mallet/util/Maths.java
	 * 
	 * Calculates the shannon divergence between both normalised word
	 * distributions Note: Taking the root would lead to shannonDistance
	 * 
	 * Pre: valA + valB will never add to 0. Markus guarantees this.
	 * 
	 * @param distributionA First distribution of words.
	 * @param distributionB Second distribution of words.
	 * 
	 * @return The distance between these distributions.
	 */
	public static double jensenShannonDivergence(
			final TreeMap<String, Double> distributionA,
			final TreeMap<String, Double> distributionB
	)
	{
		final Set<String> vocabulary = new HashSet<>(distributionA.keySet());
		vocabulary.addAll(distributionB.keySet());

		double klDiv1 = 0.0;
		double klDiv2 = 0.0;

		for (final String word : vocabulary)
		{
			Double valA = distributionA.get(word);
			Double valB = distributionB.get(word);

			if (valA == null)
				valA = Double.valueOf(0.0);
			
			if (valB == null)
				valB = Double.valueOf(0.0);

			final double avg = (valA.doubleValue() + valB.doubleValue()) / 2.0;
			assert (avg != 0.0);

			if (valA.doubleValue() != 0.0)
				klDiv1 += valA.doubleValue() * Math.log(valA.doubleValue() / avg);

			if (valB.doubleValue() != 0.0)
				klDiv2 += valB.doubleValue() * Math.log(valB.doubleValue() / avg);
		}
		final double jensonsShannonDivergence = (klDiv1 + klDiv2) / 2.0 / LOG_2;
		return jensonsShannonDivergence;
	}

	@Override
	public Score distance(
			final Game gameA, final List<Game> gameB, final int numberTrials,
			final int maxTurns, final double thinkTime, final String AIName
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
	public DistanceMetric getDefaultInstance()
	{
		return new JensenShannonTFIDFDivergence();
	}

	@Override
	public boolean isInitialized(ArrayList<LudRul> candidates)
	{
		if (!initialized)return false;
		if (idfOfT==null&&distanceMatrix==null) return false;
		if (distanceMatrix==null) {
			Set<LudRul> ks = idfOfT.keySet();
			if (ks.size()!=candidates.size())return false;
			if (!ks.containsAll(candidates))return false;
		}
		if (idfOfT==null){
			Set<LudRul> ks = distanceMatrix.getTargetToIndex().keySet();
			if (ks.size()!=candidates.size())return false;
			if (!ks.containsAll(candidates))return false;
		}
		
		
		return true;
	}

	@Override
	public DistanceMetric getPlaceHolder()
	{
		return new JensenShannonTFIDFDivergence();
	}
	@Override
	public void init(final ArrayList<LudRul> candidates, boolean forceRecaluation,final DistanceProgressListener dpl) {
		final DistanceMatrix<LudRul, LudRul> distanceMatrixLoaded = EvaluatorDistanceMetric.loadDistanceMatrix(candidates,
				this);
		if (distanceMatrixLoaded!=null&&!forceRecaluation) {
			this.distanceMatrix = distanceMatrixLoaded;
			this.initialized=true;
			return;
		}
		this.distanceMatrix = null;
		final CountMap<String> documentFrequency = new CountMap<>();
		final HashMap<LudRul,CountMap<String>> wordFrequency = new HashMap<>();
		for (int i = 0; i < candidates.size(); i++)
		{
			dpl.update(i, candidates.size()*2);
			final LudRul ludRul = candidates.get(i);
			System.out.println("Step1/2: " + i + " " + candidates.size());
			final StringCleaner sc = StringCleaner.cleanAll;
			final String[] words = sc.cleanAndSplit(ludRul.getDescriptionExpanded());
			final CountMap<String> cm = new CountMap<>();
			wordFrequency.put(ludRul, cm);
			cm.countUnique(words);
			for (final String key:cm.getHashMap().keySet()) {
				documentFrequency.addInstance(key);
			}
		}
		
		for (int i = 0; i < candidates.size(); i++)
		{
			dpl.update(candidates.size()+i, candidates.size()*2);
			final LudRul ludRul = candidates.get(i);
			System.out.println("Step2/2: " + i + " " + candidates.size());
			final HashMap<String, Double> tfMap = new HashMap<>();
			final TreeMap<String, Double> tdifSumsTo1 = new TreeMap<>();
			final CountMap<String> cm = wordFrequency.get(ludRul);
			int maxFequency = 0;
			for (final Entry<String, Integer> entry : cm.getHashMap().entrySet()) {
				maxFequency = Math.max(entry.getValue().intValue(),maxFequency);
			}
			double tfidfSum = 0.0;
			for (final Entry<String, Integer> entry : cm.getHashMap().entrySet()) {
				final double tf = entry.getValue().doubleValue()/maxFequency;
				tfMap.put(entry.getKey(), Double.valueOf(tf));
				double docFrequency = documentFrequency.get(entry.getKey()).doubleValue();
				double toLog =candidates.size()/docFrequency;
				final double infidf = Math.log(toLog)/Math.log(2);
				final double tfIdf = tf * infidf;
				tfidfSum += tf * infidf;
				tdifSumsTo1.put(entry.getKey(), Double.valueOf(tfIdf));
			}
			/*for (final Entry<String, Double> endtry : tdifSumsTo1.entrySet())
			{
				tdifSumsTo1.put(endtry.getKey(), Double.valueOf(endtry.getValue().doubleValue()/tfidfSum));
			}*/
			
			idfOfT.put(ludRul, tdifSumsTo1);
		}
		
		dpl.update(candidates.size()*2, candidates.size()*2);
		this.initialized= true;
	}
	
	public static double cosine_similarity(double[] A, double[] B)
	{
		int Vector_Length= A.length;
	    double dot = 0.0, denom_a = 0.0, denom_b = 0.0 ;
	     for(int i = 0; i < Vector_Length; ++i) {
	        dot += A[i] * B[i] ;
	        denom_a += A[i] * A[i] ;
	        denom_b += B[i] * B[i] ;
	    }
	    return dot / (Math.sqrt(denom_a) * Math.sqrt(denom_b)) ;
	}

	@Override
	public boolean typeNeedsToBeInitialized()
	{
		return true;
	}

	public static DistanceMetric createPlaceHolder()
	{
		return new JensenShannonTFIDFDivergence();
	}
	
}
