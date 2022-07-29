package search.flat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JTable;

import game.Game;
import main.collections.FVector;
import main.collections.FastArrayList;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.HeuristicTerm;
import metadata.ai.heuristics.terms.PlayerRegionsProximity;
import metadata.ai.heuristics.terms.RegionProximity;
import other.action.Action;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;
import other.move.MoveScore;
import search.flat.HeuristicSampleAdaptedUtils.HeuristicProportionViewInterface;

/**
 * Include the visualisation of weightings and wait out the thinking time
 * 
 * @author Markus
 *
 */
public class HeuristicSampleAdapted extends HeuristicSampling 
{
	@SuppressWarnings("unused")
	private FastArrayList<Move> moves;
	private FastArrayList<MoveScore> moveScores;
	private boolean waitForUserAction;
	private boolean autoSelect = false;

	private boolean heuristicChanged;
	private boolean randomizingTerm = false;
	private MoveHeuristicEvaluation latestMoveHeuristicEvaluation;
	private HeuristicProportionViewInterface heuristicProportionView;
	private Move userSelectedMove = null;

	@Override
	public void setHeuristics(Heuristics heuristics) {


		super.setHeuristics(heuristics);

		heuristicChanged = true;
	}

	/**
	 * called when playout sampling is turned of or on
	 */
	public void recalculateHeuristics() {
		heuristicChanged = true;
	}

	public void setAutoSelect(boolean autoSelect) {
		this.autoSelect = autoSelect;
	}

	public HeuristicSampleAdapted(Heuristics heuristic) {
		super(heuristic);
		setHeuristics(heuristic);
	}

	public HeuristicSampleAdapted() {
		super();
	}

	public HeuristicSampleAdapted(Heuristics heuristic, int fraction) {
		super(heuristic, fraction);
		setHeuristics(heuristic);
	}

	public HeuristicSampleAdapted(int fraction) {
		super(fraction);
	}

	public MoveHeuristicEvaluation getLatestMoveHeuristicEvaluation() {
		return latestMoveHeuristicEvaluation;
	}

	@Override
	public Move selectAction(final Game game, final Context context, final double maxSeconds, final int maxIterations,
			final int maxDepth) {
		long startTime = System.currentTimeMillis();
		boolean repeatCondition = false;
		waitForUserAction = true;
		userSelectedMove = null;
		Move move = null;
		do {
			heuristicChanged = false;
			final MoveScore moveScore = evaluateMoves(game, context, 1);

			move = moveScore.move();
			if (move == null)
				System.out.println("** No best move.");
			boolean waitingCondition = false;
			do {
				if (wantsInterrupt)
					return null;
				if (!(autoSelect && !heuristicChanged))try {
					Thread.sleep(16);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				long remainingTime = (long) (maxSeconds * 1000 - (System.currentTimeMillis() - startTime));
				waitingCondition = (!heuristicChanged && !autoSelect && waitForUserAction)
						|| (autoSelect && remainingTime > 0 && !heuristicChanged);
			} while (waitingCondition);
			long remainingTime = (long) (maxSeconds * 1000 - (System.currentTimeMillis() - startTime));
			repeatCondition = (autoSelect && heuristicChanged && remainingTime > 0)
					|| (!autoSelect && heuristicChanged && waitForUserAction);
		} while (repeatCondition);
		if (userSelectedMove!=null)return userSelectedMove;
		return move;
	}

	@Override
	public AIVisualisationData aiVisualisationData() {
		FVector aiDistribution = new FVector(moveScores.size());
		FVector valueEstimates = new FVector(moveScores.size());
		FastArrayList<Move> movesList = new FastArrayList<>(moveScores.size());

		float minNegative = 0;
		float maxNegative = Long.MIN_VALUE;

		float minPositive = Long.MAX_VALUE;
		float maxPositive = 0;

		float deltaNegative = 0f;
		float deltaPositive = 0f;
		boolean noPositive = true;
		boolean noNegative = true;
		for (int j = 0; j < moveScores.size(); j++) {
			MoveScore moveScore = moveScores.get(j);
			aiDistribution.set(j, 1.f);
			valueEstimates.set(j, moveScore.score());
			movesList.add(moveScore.move());

			float score = moveScore.score();
			if (score >= 0) {
				noPositive = false;
				if (score < minPositive)
					minPositive = score;
				if (score > maxPositive)
					maxPositive = score;
			}
			if (score < 0) {
				noNegative = false;
				if (score < minNegative)
					minNegative = score;
				if (score > maxNegative)
					maxNegative = score;
			}
		}

		deltaNegative = maxNegative - minNegative;
		deltaPositive = maxPositive - minPositive;
		if (noPositive) {
			minPositive = 0f;
			maxPositive = 0f;
			deltaPositive = 0f;
		}
		if (noNegative) {
			minNegative = 0f;
			maxNegative = 0f;
			deltaNegative = 0f;
		}
		for (int i = 0; i < valueEstimates.dim(); i++) {
			float newVal = valueEstimates.get(i);
			if (newVal == 0)
				continue;
			if (newVal < 0 && deltaNegative != 0) {
				newVal = 0.f - ((maxNegative - newVal) / deltaNegative);
			} else if (newVal < 0 && deltaNegative == 0) {
				newVal = -1;
			}
			if (newVal > 0 && deltaPositive != 0) {
				newVal = 0.f + ((newVal - minPositive) / deltaPositive);
			} else if (newVal > 0 && deltaPositive == 0) {
				newVal = 1;
			}
			valueEstimates.set(i, newVal);
		}

		// FVector f = new FVector(valueEstimates);
		ArrayList<Entry<Float, Integer>> entries = new ArrayList<Entry<Float, Integer>>();

		for (int i = 0; i < valueEstimates.dim(); i++) {
			int indexLoop = i;
			entries.add(new Entry<Float, Integer>() {
				Integer index = Integer.valueOf(indexLoop);

				@Override
				public Float getKey() {
					return Float.valueOf(valueEstimates.get(indexLoop));
				}

				@Override
				public Integer getValue() {

					return index;
				}

				@Override
				public Integer setValue(Integer value) {
					index = value;
					return index;
				}
			});
		}

		Collections.sort(entries, new Comparator<Entry<Float, Integer>>() {
			@Override
			public int compare(Entry<Float, Integer> o1, Entry<Float, Integer> o2) {
				return -o1.getKey().compareTo(o2.getKey());
			}
		});
		if(entries.size() == 0) return null;
		Entry<Float, Integer> array_element = entries.get(0);
		float newVal = ((entries.size() - 0) * 1.f) / (entries.size()) * 1.0f + 0.0f;
		aiDistribution.set(array_element.getValue().intValue(), newVal);
		Entry<Float, Integer> lastEntry = array_element;
		for (int i = 1; i < entries.size(); i++) {
			Entry<Float, Integer> entry = entries.get(i);
			if (lastEntry.getKey().equals(entry.getKey())) {
				aiDistribution.set(entry.getValue().intValue(), aiDistribution.get(lastEntry.getValue().intValue()));
			} else {
				float newValLoop = ((entries.size() - i) * 1.f) / (entries.size()) * 1.0f + 0.0f;
				aiDistribution.set(entry.getValue().intValue(), newValLoop);
			}

			lastEntry = entry;
		}
		/*
		 * for (int i = 1; i < entries.size(); i++) { Entry<Float, Integer>
		 * array_element = entries.get(i);
		 * aiDistribution.set(array_element.getValue().intValue() if
		 * (aiDistribution.get(i-1) == aiDistribution.get(i)) { aiDistribution.set(i,
		 * aiDistribution.get(i-1)); }
		 * 
		 * }
		 */

		return new AIVisualisationData(aiDistribution, valueEstimates, movesList);
	}

	public static class MoveHeuristicEvaluation {

		private FastArrayList<Move> moves;
		private Context context;
		private Heuristics heuristicFunction;
		

		private int mover;
		private int[] opponents;
		private Game game;
		public final static String[] floatNames = new String[] { "finalWeighted", "finalWeightless", "score1Weighted",
				"score1Weightless", "scoreOpponentWeighted", "scoreOpponentWeightLess" };

		public MoveHeuristicEvaluation(Game game, FastArrayList<Move> moves, Context context,
				Heuristics heuristicFunction, int mover, int[] opponents) {
			
			this.moves = addNullMoveAndSort(moves);
			this.context = context;
			this.heuristicFunction = heuristicFunction;
			this.mover = mover;
			this.opponents = opponents;
			this.game = game;
		}

		private static FastArrayList<Move> addNullMoveAndSort(FastArrayList<Move> moves2) {
			List<Action> a = new ArrayList<Action>();
			Move m = new Move(a);
			moves2.add(0,m);
			ArrayList<Move> sorted = new ArrayList<>();
			for (Move move : moves2) {
				sorted.add(move);
			}
			sorted.sort(Comparator.comparing(Move::toString));
			FastArrayList<Move> h = new FastArrayList<>();
			for (Move move : sorted) {
				h.add(move);
			}
			return h;
		}

		public Heuristics getHeuristicFunction() {
			return heuristicFunction;
		}
		
		public HashMap<Move, HashMap<HeuristicTerm, Float[]>> getHashMap() {
			HashMap<Move, HashMap<HeuristicTerm, Float[]>> finalMap = new HashMap<>();
			
			for (Move move : moves) {
				

				HashMap<HeuristicTerm, Float[]> termsToValueMap = calculateMove(move);
				
				finalMap.put(move, termsToValueMap);
			}

			return finalMap;
		}

		private HashMap<HeuristicTerm, Float[]> calculateMove(Move move) {
			final Context contextCopy = new TempContext(context);
			game.apply(contextCopy, move);

			HashMap<HeuristicTerm, Float[]> termsToValueMap = getHeuristicTermsToValueMap(contextCopy);
			return termsToValueMap;
		}

		private HashMap<HeuristicTerm,Float[]> getHeuristicTermsToValueMap( final Context contextCopy) {
			HashMap<HeuristicTerm, Float[]> termsToValueMap = new HashMap<>();
			for (HeuristicTerm ht : heuristicFunction.heuristicTerms()) {

				float score1 = ht.computeValue(contextCopy, mover, ABS_HEURISTIC_WEIGHT_THRESHOLD);
				float score2 = 0;
				for (final int opp : opponents) {
					if (contextCopy.active(opp))
						score2 -= ht.computeValue(contextCopy, opp, ABS_HEURISTIC_WEIGHT_THRESHOLD);
					else if (contextCopy.winners().contains(opp))
						score2 -= PARANOID_OPP_WIN_SCORE;
				}
				float scoreCombined = score1 + score2;
				Float[] scores = new Float[] { Float.valueOf(ht.weight() * scoreCombined),
						Float.valueOf(score1 + score2), Float.valueOf(ht.weight() * score1), Float.valueOf(score1),
						Float.valueOf(ht.weight() * score2), Float.valueOf(score2) };
				termsToValueMap.put(ht, scores);
			}
			return termsToValueMap;
		}

		public JTable getJTable(String valueType) {
			for (int i = 0; i < floatNames.length; i++) {
				String string = floatNames[i];
				if (string.equals(valueType))
					return getJTable(i);
			}
			return null;
		}

		public JTable getJTable(int valueType) {
			HashMap<Move, HashMap<HeuristicTerm, Float[]>> hashMap = this.getHashMap();

			/*
			 * StringBuilder sb = new StringBuilder(); sb.append("Move: " );
			 * Set<HeuristicTerm> terms =
			 * hashMap.entrySet().iterator().next().getValue().keySet(); for (HeuristicTerm
			 * heuristicTerm : terms) { sb.append(heuristicTerm.getClass().getSimpleName() +
			 * " "); for (int i = 1; i < floatNames.length; i++) { sb.append("\t"); } }
			 * sb.append("\n"); sb.append("       "); for (HeuristicTerm heuristicTerm :
			 * terms) { sb.append(heuristicTerm.getClass().getSimpleName() + " "); for (int
			 * i = 1; i < floatNames.length; i++) { sb.append("\t"); } }
			 */
			ArrayList<String> columnNames = new ArrayList<>();
			Set<HeuristicTerm> terms = hashMap.entrySet().iterator().next().getValue().keySet();
			HashMap<String, HeuristicTerm> nameToHT = new HashMap<>();
			for (HeuristicTerm heuristicTerm : terms) {

				String className = heuristicTerm.getClass().getSimpleName();
				if (heuristicTerm instanceof RegionProximity)
					className += " " + ((RegionProximity) heuristicTerm).region();
				if (heuristicTerm instanceof PlayerRegionsProximity)
					className += " " + ((PlayerRegionsProximity) heuristicTerm).regionPlayer();

				columnNames.add(className);
				nameToHT.put(columnNames.get(columnNames.size() - 1), heuristicTerm);
			}
			Collections.sort(columnNames);
			columnNames.add(0, "move: ");
			columnNames.add(1, "totalWeighted: ");
			columnNames.add(2, "QuickHeuristic: ");
			columnNames.add(3, "PlayoutScore: ");
			

			Move move = moves.get(0);
			HashMap<HeuristicTerm, Float[]> hm = hashMap.get(move);
			Object[][] data = new Object[moves.size()][hm.size() * floatNames.length + 1];
			for (int i = 0; i < moves.size(); i++) {
				Move m = moves.get(i);
				hm = hashMap.get(m);
				data[i][0] = m;
				float sum = 0;
				int counter = 4;
				for (int j = 4; j < columnNames.size(); j++) {
					String name = columnNames.get(j);
					HeuristicTerm heuristicTerm = nameToHT.get(name);
					Float[] floats = hm.get(heuristicTerm);
					sum += floats[0].floatValue();
					data[i][counter++] = String.format("%+.2f", floats[valueType]) + "";

				}
				data[i][1] = Float.valueOf(sum);
			}

			String[] columNamesArray = columnNames.toArray(new String[columnNames.size()]);

			JTable table = new JTable(data, columNamesArray);
			return table;

		}

		public int getMover() {

			return mover;
		}

	

		public void recalcMove(int selectedRow) {
			//HashMap<HeuristicTerm, Float[]> termsToValueMap = calculateMove(this.moves.get(selectedRow));
		}

		public Move getMove(int selectedIndex) {
			return moves.get(selectedIndex);
			
		}
	}

	public MoveHeuristicEvaluation getMoveHeuristicEvaluation() {
		return latestMoveHeuristicEvaluation;
	}

	@Override
	MoveScore evaluateMoves(final Game game, final Context context, final int depth) {
		if (randomizingTerm) {
			evaluateMoves(game,context,depth,false);
		}
		return evaluateMoves(game,context,depth,randomizingTerm);
		
	}

	private MoveScore evaluateMoves(Game game, Context context, int depth, boolean useRandomisingTerm) {
		FastArrayList<Move> movesLocal;
		FastArrayList<MoveScore> moveScoresLocal;
		
		movesLocal = selectMoves(game, context, threshold(), depth);
		moveScoresLocal = new FastArrayList<MoveScore>();
		
		
		
		if (!useRandomisingTerm) {
			this.moves = movesLocal;
			this.moveScores = moveScoresLocal;
		}
		

		float bestScore = Float.NEGATIVE_INFINITY;
		Move bestMove = movesLocal.get(0);

		final int mover = context.state().mover();

		// Context contextCurrent = context;

		for (final Move move : movesLocal) {
			final Context contextCopy = new TempContext(context);
			game.apply(contextCopy, move);

			if (!contextCopy.active(mover)) {
				if (contextCopy.winners().contains(mover)) {
					moveScoresLocal.add(new MoveScore(move, WIN_SCORE));
					if (WIN_SCORE > bestScore) {
						bestScore = WIN_SCORE;
						bestMove = move;
					}
					//return new MoveScore(move, WIN_SCORE); // Return winning move immediately
					continue;
				}
				else if (contextCopy.losers().contains(mover)) {
					moveScoresLocal.add(new MoveScore(move, -WIN_SCORE)); 
					continue; // Skip losing move
				}
					
			}

			float score = 0;
			if (continuation() && contextCopy.state().mover() == mover && depth <= 10) {
				// System.out.println("Recursing...");
				return new MoveScore(move, evaluateMoves(game, contextCopy, depth + 1,useRandomisingTerm).score());
			} else {
				score = super.heuristicFunction.computeValue(contextCopy, mover, ABS_HEURISTIC_WEIGHT_THRESHOLD);
				for (final int opp : opponents(mover)) {
					if (contextCopy.active(opp))
						score -= super.heuristicFunction.computeValue(contextCopy, opp, ABS_HEURISTIC_WEIGHT_THRESHOLD);
					else if (contextCopy.winners().contains(opp))
						score -= PARANOID_OPP_WIN_SCORE;
				}
				if (useRandomisingTerm)
					score += (float) (ThreadLocalRandom.current().nextInt(1000) / 1000000.0);
			}
			moveScoresLocal.add(new MoveScore(move, score));
			if (score > bestScore) {
				bestScore = score;
				bestMove = move;
			}

		}
		if (!useRandomisingTerm) {
		latestMoveHeuristicEvaluation = new MoveHeuristicEvaluation(game, movesLocal, context, heuristicFunction, mover,
				opponents(mover));
		if (heuristicProportionView != null) {
			heuristicProportionView.update(latestMoveHeuristicEvaluation, game, context);
		}}
		return new MoveScore(bestMove, bestScore);
	}

	public void makeMove() {
		this.waitForUserAction = false;

	}
	public void makeMove(Move m) {
		this.userSelectedMove  = m;
		this.waitForUserAction = false;
		
	}
	
	
	public void useRandomTerm(boolean useRandomTerm) {
		this.randomizingTerm = useRandomTerm;

	}

	public void setHeuristicProportionView(HeuristicProportionViewInterface heuristicProportionView) {
		this.heuristicProportionView = heuristicProportionView;
		this.heuristicProportionView.addObserver(this);
	}

	

}
