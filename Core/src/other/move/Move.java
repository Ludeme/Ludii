package other.move;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import annotations.Hide;
import game.Game;
import game.equipment.container.other.Dice;
import game.rules.play.moves.Moves;
import game.types.board.RelationType;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.Direction;
import game.util.directions.DirectionFacing;
import game.util.graph.Radial;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.hash.TIntHashSet;
import main.Constants;
import main.Status;
import main.collections.FastArrayList;
import main.collections.FastTIntArrayList;
import other.UndoData;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.action.cards.ActionSetTrumpSuit;
import other.action.die.ActionSetDiceAllEqual;
import other.action.die.ActionUpdateDice;
import other.action.die.ActionUseDie;
import other.action.graph.ActionSetCost;
import other.action.graph.ActionSetPhase;
import other.action.hidden.ActionSetHidden;
import other.action.hidden.ActionSetHiddenCount;
import other.action.hidden.ActionSetHiddenRotation;
import other.action.hidden.ActionSetHiddenState;
import other.action.hidden.ActionSetHiddenValue;
import other.action.hidden.ActionSetHiddenWhat;
import other.action.hidden.ActionSetHiddenWho;
import other.action.move.ActionAdd;
import other.action.move.ActionCopy;
import other.action.move.ActionInsert;
import other.action.move.ActionMoveN;
import other.action.move.ActionPromote;
import other.action.move.ActionSelect;
import other.action.move.ActionSubStackMove;
import other.action.move.move.ActionMove;
import other.action.move.remove.ActionRemove;
import other.action.others.ActionForfeit;
import other.action.others.ActionNextInstance;
import other.action.others.ActionNote;
import other.action.others.ActionPass;
import other.action.others.ActionPropose;
import other.action.others.ActionSetValueOfPlayer;
import other.action.others.ActionSwap;
import other.action.others.ActionVote;
import other.action.puzzle.ActionReset;
import other.action.puzzle.ActionSet;
import other.action.puzzle.ActionToggle;
import other.action.state.ActionAddPlayerToTeam;
import other.action.state.ActionBet;
import other.action.state.ActionForgetValue;
import other.action.state.ActionRememberValue;
import other.action.state.ActionSetAmount;
import other.action.state.ActionSetCount;
import other.action.state.ActionSetCounter;
import other.action.state.ActionSetNextPlayer;
import other.action.state.ActionSetPending;
import other.action.state.ActionSetPot;
import other.action.state.ActionSetRotation;
import other.action.state.ActionSetScore;
import other.action.state.ActionSetState;
import other.action.state.ActionSetTemp;
import other.action.state.ActionSetValue;
import other.action.state.ActionSetVar;
import other.action.state.ActionStoreStateInContext;
import other.action.state.ActionTrigger;
import other.concept.Concept;
import other.context.Context;
import other.context.TempContext;
import other.location.FullLocation;
import other.state.State;
import other.state.container.ContainerState;
import other.state.owned.Owned;
import other.state.track.OnTrackIndices;
import other.topology.Topology;
import other.topology.TopologyElement;
import other.trial.Trial;

/**
 * Move made up of a list of actions for modifying the game state.
 *
 * @author Eric.Piette and cambolbro
 *
 */
@Hide
public class Move extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * The data of the move set during the computation whatever if this is a
	 * decision or not.
	 */
	private int from;
	private int to;
	private TIntArrayList between = new TIntArrayList();
	private int state = -1;
	private boolean oriented = true;
	private int edge = -1;

	/** The player making this move. */
	private int mover = 0;

	/** The levels to move a piece on Stacking game */
	private int levelMin = 0;
	private int levelMax = 0;

	/** Sequence of actions making up this move. */
	private final List<Action> actions;

	/** Subsequents of the move. */
	private final List<Moves> then = new ArrayList<>();

	//-------------------------------------------------------------------------

	/** The "Moves" where comes from the move. */
	private transient Moves movesLudeme;

	//-------------------------------------------------------------------------

	/**
	 * @param actions
	 */
	public Move(final List<Action> actions)
	{
		this.actions = actions;
	}

	/**
	 * @param a The action to convert to a move.
	 */
	public Move(final Action a)
	{
		assert(!(a instanceof Move));
		actions = new ArrayList<>(1);
		actions.add(a);

		from = a.from();
		to = a.to();
	}

	/**
	 * @param a The first action of the move.
	 * @param b The second and last action of the move.
	 */
	public Move(final Action a, final Action b)
	{
		actions = new ArrayList<>(2);
		actions.add(a);
		actions.add(b);

		from = a.from();
		to = a.to();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other The move to copy.
	 */
	public Move(final Move other)
	{
		from = other.from;
		to = other.to;
		between = new TIntArrayList(other.between);
		actions = new ArrayList<Action>(other.actions);
		mover = other.mover;
	}

	/**
	 * To add an action at the start of the list of moves.
	 * 
	 * @param a    The action
	 * @param list The list of moves.
	 */
	public Move(final Action a, final FastArrayList<Move> list)
	{
		actions = new ArrayList<>(list.size() + 1);
		actions.add(a);
		for (final Action b : list)
			actions.add(b);

		from = a.from();
		to = a.to();
		if (!list.isEmpty())
			between = new TIntArrayList(list.get(0).betweenNonDecision());
	}

	/**
	 * To add an action at the end of the list of moves.
	 * 
	 * @param list The list of moves.
	 * @param b    The action.
	 */
	public Move(final FastArrayList<Move> list, final Action b)
	{
		actions = new ArrayList<>(list.size() + 1);
		for (final Action a : list)
			actions.add(a);
		actions.add(b);

		from = actions.get(0).from();
		to = actions.get(0).to();
		between = new TIntArrayList(list.get(0).betweenNonDecision());
	}

	/**
	 * To add a move with a specific list of moves.
	 * 
	 * @param list The list of moves.
	 */
	public Move(final FastArrayList<Move> list)
	{
		actions = new ArrayList<>(list.size());
		boolean hasDecision = false;
		for (final Action a : list)
		{
			if (a.isDecision()) // to have only one decision max in the result.
				if (!hasDecision)
					hasDecision = true;
				else
					a.setDecision(false);
			actions.add(a);
		}

		if (actions.size() > 0) 
		{
			from = actions.get(0).from();
			to = actions.get(0).to();
			between = new TIntArrayList(list.get(0).betweenNonDecision());
		}

	}

	/**
	 * Reconstructs a Move object from a detailed String
	 * (generated using toDetailedString())
	 * @param detailedString
	 */
	public Move(final String detailedString)
	{
		assert(detailedString.startsWith("[Move:"));

		final String strBeforeActions = detailedString.substring(0, detailedString.indexOf("actions="));

		final String strFrom = Action.extractData(strBeforeActions, "from");
		from = (strFrom.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strFrom);

		final String strTo = Action.extractData(strBeforeActions, "to");
		to = (strTo.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strTo);

		final String strState = Action.extractData(strBeforeActions, "state");
		state = (strState.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strState);

		final String strOriented = Action.extractData(strBeforeActions, "oriented");
		oriented = (strOriented.isEmpty()) ? true : Boolean.parseBoolean(strOriented);

		final String strEdge = Action.extractData(strBeforeActions, "edge");
		edge = (strEdge.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strEdge);

		final String strMover = Action.extractData(strBeforeActions, "mover");
		mover = Integer.parseInt(strMover);

		final String strLvlMin = Action.extractData(strBeforeActions, "levelMin");
		levelMin = (strLvlMin.isEmpty()) ? Constants.GROUND_LEVEL : Integer.parseInt(strLvlMin);

		final String strLvlMax = Action.extractData(strBeforeActions, "levelMax");
		levelMax = (strLvlMax.isEmpty()) ? Constants.GROUND_LEVEL : Integer.parseInt(strLvlMax);

		String str = detailedString;
		actions = new ArrayList<>();
		str = str.substring(str.indexOf(",actions=") + ",actions=".length());

		while (true)
		{
			if (str.isEmpty())
				break;

			// find matching closing square bracket
			int numOpenBrackets = 1;
			int idx = 1;

			while (numOpenBrackets > 0 && idx < str.length())
			{
				if (str.charAt(idx) == '[')
					++numOpenBrackets;
				else if (str.charAt(idx) == ']')
					--numOpenBrackets;

				++idx;
			}

			if (numOpenBrackets > 0)
			{
				// we reached end of string
				break;
			}

			final int closingIdx = idx;

			final String actionStr = str.substring(0, closingIdx);

			if (actionStr.startsWith("[Move:") && actionStr.contains("actions="))
				actions.add(new Move(actionStr));
			else if (actionStr.startsWith("[Add:"))
				actions.add(new ActionAdd(actionStr));
			else if (actionStr.startsWith("[Insert:"))
				actions.add(new ActionInsert(actionStr));
			else if (actionStr.startsWith("[SetStateAndUpdateDice:"))
				actions.add(new ActionUpdateDice(actionStr));
			else if (actionStr.startsWith("[Move:") && actionStr.contains("count="))
				actions.add(new ActionMoveN(actionStr));
			else if (actionStr.startsWith("[Move:"))
				actions.add(ActionMove.construct(actionStr));
			else if (actionStr.startsWith("[StackMove:"))
				actions.add(new ActionSubStackMove(actionStr));
			else if (actionStr.startsWith("[SetValueOfPlayer:"))
				actions.add(new ActionSetValueOfPlayer(actionStr));
			else if (actionStr.startsWith("[SetAmount:"))
				actions.add(new ActionSetAmount(actionStr));
			else if (actionStr.startsWith("[Note:"))
				actions.add(new ActionNote(actionStr));
			else if (actionStr.startsWith("[SetPot:"))
				actions.add(new ActionSetPot(actionStr));
			else if (actionStr.startsWith("[Bet:"))
				actions.add(new ActionBet(actionStr));
			else if (actionStr.startsWith("[Pass:"))
				actions.add(new ActionPass(actionStr));
			else if (actionStr.startsWith("[SetTrumpSuit:"))
				actions.add(new ActionSetTrumpSuit(actionStr));
			else if (actionStr.startsWith("[SetPending:"))
				actions.add(new ActionSetPending(actionStr));
			else if (actionStr.startsWith("[Promote:"))
				actions.add(new ActionPromote(actionStr));
			else if (actionStr.startsWith("[Reset:"))
				actions.add(new ActionReset(actionStr));
			else if (actionStr.startsWith("[SetScore:"))
				actions.add(new ActionSetScore(actionStr));
			else if (actionStr.startsWith("[SetCost:"))
				actions.add(new ActionSetCost(actionStr));
			else if (actionStr.startsWith("[Propose:"))
				actions.add(new ActionPropose(actionStr));
			else if (actionStr.startsWith("[SetDiceAllEqual:"))
				actions.add(new ActionSetDiceAllEqual(actionStr));
			else if (actionStr.startsWith("[Vote:"))
				actions.add(new ActionVote(actionStr));
			else if (actionStr.startsWith("[Select:"))
				actions.add(new ActionSelect(actionStr));
			else if (actionStr.startsWith("[Set:"))
				actions.add(new ActionSet(actionStr));
			else if (actionStr.startsWith("[SetPhase:"))
				actions.add(new ActionSetPhase(actionStr));
			else if (actionStr.startsWith("[SetCount:"))
				actions.add(new ActionSetCount(actionStr));
			else if (actionStr.startsWith("[SetCounter:"))
				actions.add(new ActionSetCounter(actionStr));
			else if (actionStr.startsWith("[SetTemp:"))
				actions.add(new ActionSetTemp(actionStr));
			else if (actionStr.startsWith("[SetState:"))
				actions.add(new ActionSetState(actionStr));
			else if (actionStr.startsWith("[SetValue:"))
				actions.add(new ActionSetValue(actionStr));
			else if (actionStr.startsWith("[Toggle:"))
				actions.add(new ActionToggle(actionStr));
			else if (actionStr.startsWith("[Trigger:"))
				actions.add(new ActionTrigger(actionStr));
			else if (actionStr.startsWith("[Remove:"))
				actions.add(ActionRemove.construct(actionStr));
			else if (actionStr.startsWith("[SetNextPlayer:"))
				actions.add(new ActionSetNextPlayer(actionStr));
			else if (actionStr.startsWith("[Forfeit:"))
				actions.add(new ActionForfeit(actionStr));
			else if (actionStr.startsWith("[Swap:"))
				actions.add(new ActionSwap(actionStr));
			else if (actionStr.startsWith("[SetRotation:"))
				actions.add(new ActionSetRotation(actionStr));
			else if (actionStr.startsWith("[UseDie:"))
				actions.add(new ActionUseDie(actionStr));
			else if (actionStr.startsWith("[StoreStateInContext:"))
				actions.add(new ActionStoreStateInContext(actionStr));
			else if (actionStr.startsWith("[AddPlayerToTeam:"))
				actions.add(new ActionAddPlayerToTeam(actionStr));
			else if (actionStr.startsWith("[NextInstance"))
				actions.add(new ActionNextInstance(actionStr));
			else if (actionStr.startsWith("[Copy"))
				actions.add(new ActionCopy(actionStr));
			else if (actionStr.startsWith("[ForgetValue"))
				actions.add(new ActionForgetValue(actionStr));
			else if (actionStr.startsWith("[RememberValue"))
				actions.add(new ActionRememberValue(actionStr));
			else if (actionStr.startsWith("[SetVar"))
				actions.add(new ActionSetVar(actionStr));
			else if (actionStr.startsWith("[SetHiddenWho"))
				actions.add(new ActionSetHiddenWho(actionStr));
			else if (actionStr.startsWith("[SetHiddenWhat"))
				actions.add(new ActionSetHiddenWhat(actionStr));
			else if (actionStr.startsWith("[SetHiddenState"))
				actions.add(new ActionSetHiddenState(actionStr));
			else if (actionStr.startsWith("[SetHiddenRotation"))
				actions.add(new ActionSetHiddenRotation(actionStr));
			else if (actionStr.startsWith("[SetHiddenValue"))
				actions.add(new ActionSetHiddenValue(actionStr));
			else if (actionStr.startsWith("[SetHiddenCount"))
				actions.add(new ActionSetHiddenCount(actionStr));
			else if (actionStr.startsWith("[SetHidden"))
				actions.add(new ActionSetHidden(actionStr));
			else
				System.err.println("Move constructor does not recognise action: " + str);

			str = str.substring(closingIdx + 1);	// +1 to skip over comma
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The mover.
	 */
	public int mover()
	{
		return mover;
	}

	/**
	 * Set the mover.
	 * 
	 * @param who The new mover.
	 */
	public void setMover(final int who)
	{
		mover = who;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param context
	 * @return A list of all the actions that would be applied by this Move to the given Context, including consequents
	 */
	public List<Action> getActionsWithConsequences(final Context context)
	{
		return getMoveWithConsequences(context).actions();
	}
	
	/**
	 * @param context
	 * @return A Move with all actions that would be applied by this Move to the given Context, including consequents
	 */
	public Move getMoveWithConsequences(final Context context)
	{
		final Context contextCopy = new TempContext(context);

		// we need to fix the RNG internal state for the full actions,
		// including nondeterministic consequents, to match perfectly
		final RandomProviderDefaultState realRngState = (RandomProviderDefaultState) context.rng().saveState();
		contextCopy.rng().restoreState(realRngState);
		
		// We pass true for skipping the computation of end rules, don't need to waste time on that
		return contextCopy.game().apply(contextCopy, this, true);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Sequence of sequence.
	 */
	public List<Action> actions()
	{
		return actions;
	}

	/**
	 * @return The subsequents of the move.
	 */
	public List<Moves> then()
	{
		return then;
	}

	//-------------------------------------------------------------------------

	@Override
	public final Action apply(final Context context, final boolean store)
	{
		final List<Action> returnActions = new ArrayList<>(actions.size());
		final Trial trial = context.trial();

		// Apply the list of actions
		for (final Action action : actions)
		{
			final Action returnAction = action.apply(context, false);
			if (returnAction instanceof Move)
				returnActions.addAll(((Move) returnAction).actions);
			else
				returnActions.add(returnAction);
		}

		// Store the move if necessary
		if (store)
			trial.addMove(this);

		// Apply the possible subsequents
		// Create a list of actions to store in the trial
		for (final Moves consequent : then)
		{
			final FastArrayList<Move> postActions = consequent.eval(context).moves();
			
			for (final Move m : postActions)
			{
				// Remove the last stored action and store a list with subsequents
				final Action innerApplied = m.apply(context, false);

				if (innerApplied instanceof Move)
				{
					returnActions.addAll(((Move) innerApplied).actions);
				}
				else
				{
					returnActions.add(innerApplied);
				}

//				context.trial().actions().remove(context.trial().actions().size() - 1);
//				context.trial().addMove(moveToStore);

			}
		}

		if (store && context.game().hasSequenceCapture())
		{
			if (!containsReplayAction(returnActions))
			{
				final TIntArrayList sitesToRemove = context.state().sitesToRemove();
				if(context.game().isStacking())
				{
					final ContainerState cs = context.containerState(0);
					final SiteType defaultSiteType = context.board().defaultSite();
					final int[] numRemoveBySite = new int[context.board().topology().getGraphElements(defaultSiteType).size()];
					for (int i = sitesToRemove.size() - 1; i >= 0 ; i--)
						numRemoveBySite[sitesToRemove.get(i)]++;
					
					for(int site = 0; site < numRemoveBySite.length; site++)
					{
						if(numRemoveBySite[site] > 0)
						{
							final int numToRemove = Math.min(numRemoveBySite[site], cs.sizeStack(site, defaultSiteType));
							if(numToRemove > 0)
								for(int level = numToRemove - 1; level >=0 ; level--)
								{
									final Action remove = other.action.move.remove.ActionRemove.construct(
											context.board().defaultSite(), site, level, true);
									remove.apply(context, false);
									returnActions.add(0, remove);
								}
						}
					}
				}
				else
				{
					for (int i = 0; i < sitesToRemove.size();i++)
					{
						final int site = sitesToRemove.get(i);
						final Action remove = other.action.move.remove.ActionRemove.construct(
								context.board().defaultSite(), site, Constants.UNDEFINED,
									true);
	
						if (!returnActions.contains(remove))
						{
							remove.apply(context, false);
							returnActions.add(0, remove);
						}
					}
				}
				
				context.state().reInitCapturedPiece();
			}
		}

		final Move returnMove = new Move(returnActions);
		returnMove.setFromNonDecision(from);
		returnMove.setBetweenNonDecision(new TIntArrayList(betweenNonDecision()));
		returnMove.setToNonDecision(to);
		returnMove.setStateNonDecision(state);
		returnMove.setOrientedMove(oriented);
		returnMove.setEdgeMove(edge);
		returnMove.setMover(mover);
		returnMove.setLevelMaxNonDecision(levelMax);
		returnMove.setLevelMinNonDecision(levelMin);
		
		if (store)
		{
			// we applied extra consequents actions, so replace move in trial
			trial.replaceLastMove(returnMove);
		}

		return returnMove;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context, boolean discard)
	{
		if(discard)
		{
			final Trial trial = context.trial();
			final State currentState = context.state();
			final Game game = context.game();
			
			// Step 2: Restore the data modified by the last end rules or nextPhase.
			// Get the previous end data.
			final UndoData undoData = trial.endData().isEmpty() ? null : trial.endData().get(trial.endData().size()-1);
			final double[] ranking = undoData == null ? new double[game.players().size()] : undoData.ranking();
			final int[] phases = undoData == null ? new int[game.players().size()] : undoData.phases();
			final Status status = undoData == null ? null : undoData.status();
			final TIntArrayList winners = undoData == null ? new TIntArrayList(game.players().count()) : undoData.winners();
			final TIntArrayList losers = undoData == null ? new TIntArrayList(game.players().count()) : undoData.losers();
			final TIntHashSet pendingValues = undoData == null ? new TIntHashSet() : undoData.pendingValues();
			final int previousCounter = undoData == null ? Constants.UNDEFINED : undoData.counter();
			final TLongArrayList previousStateWithinATurn = undoData == null ? null : undoData.previousStateWithinATurn();
			final TLongArrayList previousState = undoData == null ? null : undoData.previousState();
			final int prev = undoData == null ? 1 : undoData.prev();
			final int currentMover = undoData == null ? 1 : undoData.mover();
			final int next = undoData == null ? 1 : undoData.next();
			final int numTurn = undoData == null ? 0 : undoData.numTurn();
			final int numTurnSamePlayer = undoData == null ? 0 : undoData.numTurnSamePlayer();
			final int numConsecutivePasses = undoData == null ? 0 : undoData.numConsecutivePasses();
			final FastTIntArrayList remainingDominoes = undoData == null ? null : undoData.remainingDominoes();
			final BitSet visited = undoData == null ? null : undoData.visited();
			final TIntArrayList sitesToRemove = undoData == null ? null : undoData.sitesToRemove();
			final OnTrackIndices onTrackIndices = undoData == null ? null : undoData.onTrackIndices();
			final Owned owned = undoData == null ? null : undoData.owned();
			final int isDecided = undoData == null ? Constants.UNDEFINED : undoData.isDecided();
			
			int active = 0;
			if(undoData != null)
				active = undoData.active();
			else
			{
				for (int p = 1; p <= game.players().count(); ++p)
				{
					final int whoBit = (1 << (p - 1));
					active |= whoBit;
				}
			}
			
			final int[] scores = undoData == null ? new int[game.players().size()] : undoData.scores();
			final double[] payoffs = undoData == null ? new double[game.players().size()] : undoData.payoffs();
			final int numLossesDecided = undoData == null ? 0: undoData.numLossesDecided();
			final int numWinsDecided = undoData == null ? 0: undoData.numWinsDecided();
			
			// Restore the previous end data.
			for(int i = 0; i < ranking.length; i++)
				trial.ranking()[i] = ranking[i];
			trial.setStatus(status);
			if(winners != null)
			{
				context.winners().clear();
				for(int i = 0; i < winners.size(); i++)
					context.winners().add(winners.get(i));
			}
			
			if(losers != null)
			{
				context.losers().clear();
				for(int i = 0; i < losers.size(); i++)
					context.losers().add(losers.get(i));
			}
			context.setActive(active);
			
			if(context.scores() != null)
				for(int i = 0; i < context.scores().length; i++)
					context.scores()[i] = scores[i];
			
			if(context.payoffs() != null)
				for(int i = 0; i < context.payoffs().length; i++)
					context.payoffs()[i] = payoffs[i];
			
			context.setNumLossesDecided(numLossesDecided);
			context.setNumWinsDecided(numWinsDecided);
	
			for(int pid = 1; pid < phases.length; pid++)
				context.state().setPhase(pid, phases[pid]);
			trial.removeLastEndData();
			
			// Step 3: update the state data.
			if(previousStateWithinATurn != null)
			{
				trial.previousStateWithinATurn().clear();
				for(int i = 0; i < previousStateWithinATurn.size(); i++)
					trial.previousStateWithinATurn().add(previousStateWithinATurn.get(i));
			}
			if(previousState != null)
			{
				trial.previousState().clear();
				for(int i = 0; i < previousState.size(); i++)
					trial.previousState().add(previousState.get(i));
			}
			if(remainingDominoes != null)
			{
				currentState.remainingDominoes().clear();
				for(int i = 0; i < remainingDominoes.size(); i++)
					currentState.remainingDominoes().add(remainingDominoes.get(i));
			}
			
			if(visited != null)
			{
				currentState.reInitVisited();
				for(int site = 0; site < visited.size(); site++)
					if(visited.get(site))
						currentState.visit(site);
			}
	
			if(sitesToRemove != null)
			{
				currentState.sitesToRemove().clear();
				for(int i = 0; i < sitesToRemove.size(); i++)
					currentState.sitesToRemove().add(sitesToRemove.get(i));
			}
			
			// Undo the list of actions
			for (int i = actions.size() - 1; i >= 0; i--)
			{
				final Action action = actions.get(i);
				action.undo(context, false);
			}
			
			// Discard the move if necessary
			trial.removeLastMove();
	
			currentState.setPrev(prev);
			currentState.setMover(currentMover);
			currentState.setNext(next);
			currentState.setNumTurn(numTurn);
			currentState.setTurnSamePlayer(numTurnSamePlayer);
			currentState.setNumConsecutivesPasses(numConsecutivePasses);
			currentState.setOnTrackIndices(onTrackIndices);
			currentState.setOwned(owned);
			currentState.setIsDecided(isDecided);
			
			// Step 5: To update the sum of the dice container.
			if (game.hasHandDice())
			{
				for (int i = 0; i < game.handDice().size(); i++)
				{
					final Dice dice = game.handDice().get(i);
					final ContainerState cs = context.containerState(dice.index());
	
					final int siteFrom = context.sitesFrom()[dice.index()];
					final int siteTo = context.sitesFrom()[dice.index()] + dice.numSites();
					int sum = 0;
					for (int site = siteFrom; site < siteTo; site++)
					{
						sum += context.components()[cs.whatCell(site)].getFaces()[cs.stateCell(site)];
						// context.state().currentDice()[i][site - siteFrom] =
						// context.components().get(cs.what(site))
						// .getFaces()[cs.state(site)];
					}
					currentState.sumDice()[i] = sum;
				}
			}
			
			// Step 6: restore some data in the state.
			currentState.restorePending(pendingValues);
			currentState.setCounter(previousCounter);
			trial.clearLegalMoves();
			
			// Make sure our "real" context's RNG actually gets used and progresses
			// For temporary copies of context, we need not do this
			if (!(context instanceof TempContext) && !trial.over() && context.game().isStochasticGame())
				context.game().moves(context);
		}
		else
		{
			// Undo the list of actions
			for (int i = actions.size() - 1; i >= 0; i--)
			{
				final Action action = actions.get(i);
				action.undo(context, false);
			}
		}
		
		return this;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param actionsList
	 * @return True if the list of actions to apply contains SetNextPlayer.
	 */
	@SuppressWarnings("static-method")
	public boolean containsReplayAction(final List<Action> actionsList)
	{
		for (final Action action : actionsList)
			if (action instanceof ActionSetNextPlayer)
				return true;

		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isPass()
	{
		boolean foundPass = false;

		for (final Action a : actions)
		{
			if (a.isDecision() && !a.isPass())
				return false;
			else
				foundPass = foundPass || a.isPass();
		}

		return foundPass;
	}

	@Override
	public boolean isForfeit()
	{
		boolean foundForfeit = false;

		for (final Action a : actions)
		{
			if (a.isDecision() && !a.isForfeit())
				return false;
			else
				foundForfeit = foundForfeit || a.isForfeit();
		}

		return foundForfeit;
	}

	@Override
	public boolean isSwap()
	{
		boolean foundSwap = false;

		for (final Action a : actions)
		{
			if (a.isDecision() && !a.isSwap())
				return false;
			else
				foundSwap = foundSwap || a.isSwap();
		}

		return foundSwap;
	}
	
	@Override
	public boolean isVote()
	{
		boolean foundVote = false;

		for (final Action a : actions)
		{
			if (a.isDecision() && !a.isVote())
				return false;
			else
				foundVote = foundVote || a.isVote();
		}

		return foundVote;
	}
	
	@Override
	public boolean isPropose()
	{
		boolean foundPropose = false;

		for (final Action a : actions)
		{
			if (a.isDecision() && !a.isPropose())
				return false;
			else
				foundPropose = foundPropose || a.isPropose();
		}

		return foundPropose;
	}
	
	@Override
	public boolean isAlwaysGUILegal()
	{
		for (final Action a : actions)
			if (a.isDecision() && a.isAlwaysGUILegal())
				return true;

		return false;
	}

	@Override
	public int playerSelected()
	{
		for (final Action a : actions)
			if (a.isDecision())
				return a.playerSelected();

		return Constants.UNDEFINED;
	}

	@Override
	public boolean isOtherMove()
	{
		boolean foundOtherMove = false;

		for (final Action a : actions)
		{
			if (a.isDecision() && !a.isOtherMove())
				return false;
			else
				foundOtherMove = foundOtherMove || a.isOtherMove();
		}

		return foundOtherMove;
	}
	
	@Override
	public boolean containsNextInstance()
	{
		for (final Action a : actions)
		{
			if (a.containsNextInstance())
				return true;
		}
		
		return false;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();

		for (final Action a : actions)
		{
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(a.toString());
		}
		if (actions.size() > 1)
		{
			sb.insert(0, '[');
			sb.append(']');
		}
		else if (actions.isEmpty())
		{
			sb.append("[Empty Move]");
		}
		if (then.size() > 0)
		{
			sb.append(then.toString());
		}
		return sb.toString();
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		for (final Action a : actions)
		{
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(a.toTurnFormat(context, useCoords));
		}
		if (actions.size() > 1)
		{
			sb.insert(0, '[');
			sb.append(']');
		}
		else if (actions.isEmpty())
		{
			sb.append("[Empty Move]");
		}
		if (then.size() > 0)
		{
			sb.append(then.toString());
		}
		return sb.toString();
	}

	//-------------------------------------------------------------------------

	@Override
	public int from()
	{
		for (final Action a : actions)
			if (a.isDecision())
				return a.from();
		return Constants.UNDEFINED;
	}
	
	@Override
	public ActionType actionType()
	{
		for (final Action a : actions)
			if (a.isDecision())
				return a.actionType();
		return null;
	}

	/**
	 * @return The from site after applying the subsequents.
	 */
	public int fromAfterSubsequents()
	{
		if (actions.size() > 0)
		{
			int i = 1;
			while (actions.get(actions.size() - i).from() == Constants.OFF)
			{
				i++;
				if ((actions.size() - i) < 0)
					return Constants.OFF;
			}
			return actions.get(actions.size() - i).from();
		}
		return Constants.UNDEFINED;
	}
	
	/**
	 * @return The from site after applying the subsequents.
	 */
	public int levelFromAfterSubsequents()
	{
		if (actions.size() > 0)
		{
			int i = 1;
			while (actions.get(actions.size() - i).from() == Constants.OFF)
			{
				i++;
				if ((actions.size() - i) < 0)
					return Constants.OFF;
			}
			return actions.get(actions.size() - i).levelFrom();
		}
		return Constants.UNDEFINED;
	}

	@Override
	public int levelFrom()
	{
		for (final Action a : actions)
			if (a.isDecision())
				return a.levelFrom();
		return 0;
	}

	@Override
	public int to()
	{
		for (final Action a : actions)
			if (a.isDecision())
				return a.to();

		return Constants.UNDEFINED;
	}

	/**
	 * @return The to site after applying the subsequents.
	 */
	public int toAfterSubsequents()
	{
		if (actions.size() > 0)
		{
			int i = 1;
			while (actions.get(actions.size() - i).to() == Constants.OFF)
			{
				i++;
				if ((actions.size() - i) < 0)
					return Constants.OFF;
			}
			return actions.get(actions.size() - i).to();
		}
		return Constants.UNDEFINED;
	}
	
	/**
	 * @return The to site after applying the subsequents.
	 */
	public int levelToAfterSubsequents()
	{
		if (actions.size() > 0)
		{
			int i = 1;
			while (actions.get(actions.size() - i).to() == Constants.OFF)
			{
				i++;
				if ((actions.size() - i) < 0)
					return Constants.OFF;
			}
			return actions.get(actions.size() - i).levelTo();
		}
		return Constants.UNDEFINED;
	}

	@Override
	public int levelTo()
	{
		for (final Action a : actions)
			if (a.isDecision())
				return a.levelTo();
		return 0;
	}

	@Override
	public int what()
	{
		for (final Action a : actions)
			if (a.isDecision())
				return a.what();
		return Constants.NO_PIECE;
	}

	@Override
	public int state()
	{
		for (final Action a : actions)
			if (a.isDecision())
				return a.state();
		return 0;
	}

	@Override
	public int count()
	{
		for (final Action a : actions)
			if (a.isDecision())
				return a.count();
		return 1;
	}

	@Override
	public boolean isStacking()
	{
		for (final Action a : actions)
			if (a.isDecision())
				return a.isStacking();
		return false;
	}

	@Override
	public boolean[] hidden()
	{
		for (final Action a : actions)
			if (a.isDecision())
				return a.hidden();
		return null;
	}

	@Override
	public int who()
	{
		for (final Action a : actions)
			if (a.isDecision())
				return a.who();
		return 0;
	}

	@Override
	public boolean isDecision()
	{
		for (final Action a : actions)
			if (a.isDecision())
				return true;
		return false;
	}
	
	@Override
	public boolean isForced()
	{
		for (final Action a : actions)
			if (a.isForced())
				return true;
		return false;
	}

	@Override
	public final void setDecision(final boolean decision)
	{
		for(final Action a : actions())
			a.setDecision(decision);
	}

	@Override
	public final Move withDecision(final boolean dec)
	{
		return this;
	}

	@Override
	public boolean matchesUserMove(final int siteA, final int levelA, final SiteType graphElementTypeA, final int siteB,
			final int levelB, final SiteType graphElementTypeB)
	{
		return (fromNonDecision() == siteA && levelFrom() == levelA && fromType() == graphElementTypeA && toNonDecision() == siteB
				&& levelTo() == levelB && toType() == graphElementTypeB);
	}

	/**
	 * @return The from site of the move whatever the decision action.
	 */
	public int fromNonDecision()
	{
		return from;
	}

	/**
	 * To set the from site of the move what the decision action.
	 * 
	 * @param from The new from site.
	 */
	public void setFromNonDecision(final int from)
	{
		this.from = from;
	}

	/**
	 * @return The to site of the move whatever the decision action.
	 */
	public int toNonDecision()
	{
		return to;
	}

	/**
	 * To set the to site of the move whatever the decision action.
	 * 
	 * @param to The new to site.
	 */
	public void setToNonDecision(final int to)
	{
		this.to = to;
	}

	/**
	 * @return The between sites of the move whatever the decision action.
	 */
	public TIntArrayList betweenNonDecision()
	{
		return between;
	}

	/**
	 * To set the between sites of the move whatever the decision action.
	 * 
	 * @param between The new between sites.
	 */
	public void setBetweenNonDecision(final TIntArrayList between)
	{
		this.between = between;
	}

	/**
	 * @param fromW The new from
	 * @return The move.
	 */
	public Move withFrom(final int fromW)
	{
		from = fromW;
		return this;
	}

	/**
	 * @param toW The new to.
	 * @return The move.
	 */
	public Move withTo(final int toW)
	{
		to = toW;
		return this;
	}
	
	/**
	 * @param m New mover
	 * @return The move.
	 */
	public Move withMover(final int m)
	{
		mover = m;
		return this;
	}

	/**
	 * @return The minimum level of the move whatever the decision action.
	 */
	public int levelMinNonDecision()
	{
		return levelMin;
	}

	/**
	 * Set the minimum level of the move whatever the decision action.
	 * 
	 * @param levelMin The minimum level.
	 */
	public void setLevelMinNonDecision(final int levelMin)
	{
		this.levelMin = levelMin;
	}

	/**
	 * @return The maximum level of the move whatever the decision action.
	 */
	public int levelMaxNonDecision()
	{
		return levelMax;
	}

	/**
	 * Set the maximum level of the move whatever the decision action.
	 * 
	 * @param levelMax The maximum level.
	 */
	public void setLevelMaxNonDecision(final int levelMax)
	{
		this.levelMax = levelMax;
	}

	/**
	 * @return True if the move is oriented.
	 */
	public boolean isOrientedMove()
	{
		return oriented;
	}

	/**
	 * To set the move to be an edge move.
	 * 
	 * @param edge The value.
	 */
	public void setEdgeMove(final int edge)
	{
		this.edge = edge;
	}

	/**
	 * @return True if the move is an edge move.
	 */
	public int isEdgeMove()
	{
		return edge;
	}

	/**
	 * Set the state value of the move whatever the decision action.
	 * 
	 * @param state The new value.
	 */
	public void setStateNonDecision(final int state)
	{
		this.state = state;
	}

	/**
	 * @return The state value of the move whatever if the decision action.
	 */
	public int stateNonDecision()
	{
		return state;
	}

	/**
	 * Set the orientation of the move.
	 * 
	 * @param oriented True if the move has to be oriented.
	 */
	public void setOrientedMove(final boolean oriented)
	{
		this.oriented = oriented;
	}

	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actions == null) ? 0 : actions.hashCode());
		result = prime * result + ((then == null) ? 0 : then.hashCode());
		result = prime * result + (from + to);	// from and to added here due to use of oriented in equals()
		result = prime * result + levelMax;
		result = prime * result + levelMin;
		result = prime * result + state;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof Move))
			return false;

		final Move other = (Move) obj;

		if (actions == null)
		{
			if (other.actions != null)
				return false;
		}
		else
		{
			if (!actions.equals(other.actions))
			{
				return false;
			}
		}

		if (then == null)
		{
			if (other.then != null)
				return false;
		}
		else if (!then.equals(other.then))
		{
			return false;
		}

		if (oriented || other.oriented)
		{
			return (from == other.from &&
					levelMax == other.levelMax &&
					levelMin == other.levelMin &&
					to == other.to &&
					state == other.state);
		}
		else
		{
			return ((from == other.from || from == other.to) &&
					levelMax == other.levelMax &&
					levelMin == other.levelMin &&
					(to == other.from || to == other.to) &&
					state == other.state);
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[Move:");

		sb.append("mover=" + mover);

		if (from != Constants.UNDEFINED)
			sb.append(",from=" + from);

		if (to != Constants.UNDEFINED)
			sb.append(",to=" + to);

		if (state != Constants.UNDEFINED)
			sb.append(",state=" + state);

		if (oriented == false)
			sb.append(",oriented=" + oriented);

		if (edge != Constants.UNDEFINED)
			sb.append(",edge=" + edge);

		if (levelMin != Constants.GROUND_LEVEL && levelMax != Constants.GROUND_LEVEL)
		{
			sb.append(",levelMin=" + levelMin);
			sb.append(",levelMax=" + levelMax);
		}

		final List<Action> allActions = (context == null) ? actions : getActionsWithConsequences(context);
		sb.append(",actions=");

		for (int i = 0; i < allActions.size(); ++i)
		{
			sb.append(allActions.get(i).toTrialFormat(context));
			if (i < allActions.size() - 1)
				sb.append(",");
		}

		sb.append(']');

		return sb.toString();
	}
	
	@Override
	public String getDescription() 
	{
		return "Move";
	}

	//-------------------------------------------------------------------------

	@Override
	public int rotation()
	{
		for (final Action a : actions)
			if (a.isDecision())
				return a.rotation();
		return 0;
	}
	
	@Override
	public SiteType fromType()
	{
		for (final Action a : actions)
			if (a.isDecision())
				return a.fromType();

		for (final Action a : actions)
			if (a.fromType() != null)
				return a.fromType();
		
		return SiteType.Cell;
	}

	@Override
	public SiteType toType()
	{
		for (final Action a : actions)
			if (a.isDecision())
				return a.toType();
		
		for (final Action a : actions)
			if (a.toType() != null)
				return a.toType();

		return SiteType.Cell;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return The from full location.
	 */
	public FullLocation getFromLocation() 
	{
		return new FullLocation(from, levelFrom(), fromType());
	}
	
	/**
	 * @return The to full location.
	 */
	public FullLocation getToLocation() 
	{
		return new FullLocation(to, levelTo(), toType());
	}

	//-------------------------------------------------------------------------

	/**
	 * @param topo The topology.
	 * @return The direction of the move if that move has one.
	 */
	public Direction direction(final Topology topo)
	{
		// No direction if in the same site, or in no site, or not the site type.
		if (from == to || from == Constants.UNDEFINED || to == Constants.UNDEFINED || !fromType().equals(toType()))
			return null;

		final SiteType type = toType();

		// No direction if not in the board.
		if (from >= topo.getGraphElements(type).size())
			return null;

		// No direction if not in the board.
		if (to >= topo.getGraphElements(type).size())
			return null;

		final TopologyElement fromElement = topo.getGraphElement(type, from);
		final TopologyElement toElement = topo.getGraphElement(type, to);

		final List<DirectionFacing> directionsSupported = topo.supportedDirections(RelationType.All, type);

		for (final DirectionFacing direction : directionsSupported)
		{
			final AbsoluteDirection absDirection = direction.toAbsolute();
			final List<Radial> radials = topo.trajectories().radials(type, fromElement.index(), absDirection);

			for (final Radial radial : radials)
			{
				for (int toIdx = 1; toIdx < radial.steps().length; toIdx++)
				{
					final int toRadial = radial.steps()[toIdx].id();
					if (toRadial == toElement.index())
					{
						return absDirection;
					}
				}
			}
		}

		return null;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The Moves concepts computed thanks to the action concepts.
	 */
	@Override
	public BitSet concepts(final Context context, final Moves ludeme)
	{
		final BitSet moveConcepts = new BitSet();
		for (final Action action : getActionsWithConsequences(context))
			moveConcepts.or(action.concepts(context, ludeme));
		return moveConcepts;
	}

	/**
	 * @param context The Context.
	 * @return The Moves concepts computed thanks to the action/move concepts.
	 */
	public BitSet moveConcepts(final Context context)
	{
		final BitSet moveConcepts = new BitSet();
		for (final Action action : getActionsWithConsequences(context))
			moveConcepts.or(action.concepts(context, movesLudeme));
		return moveConcepts;
	}
	
	/**
	 * @param context The Context.
	 * @return The values associated with the Moves concepts computed thanks to the action/move concepts.
	 */
	public TIntArrayList moveConceptsValue(final Context context)
	{
		final TIntArrayList moveConceptsValues = new TIntArrayList();
		
		for(int i = 0; i < Concept.values().length; i++)
			moveConceptsValues.add(0);
		
		for (final Action action : getActionsWithConsequences(context))
		{
			BitSet actionConcepts = action.concepts(context, movesLudeme);
			for(int i = 0; i < Concept.values().length; i++)
				if(actionConcepts.get(i))
					moveConceptsValues.set(i, moveConceptsValues.get(i) + 1);
		}
//		
//		System.out.println("Move is " + this);
//
//		for(int i = 0; i < Concept.values().length; i++)
//			if(moveConceptsValues.get(i) != 0)
//				System.out.println("Concept = " + Concept.values()[i] + " value = " + moveConceptsValues.get(i));
//		
//		System.out.println();
		
		return moveConceptsValues;
	}

	/**
	 * @return The move ludemes.
	 */
	public Moves movesLudeme()
	{
		return movesLudeme;
	}

	/**
	 * To set the moves ludemes.
	 * 
	 * @param movesLudeme The moves.
	 */
	public void setMovesLudeme(final Moves movesLudeme)
	{
		this.movesLudeme = movesLudeme;
	}
	
	//-------------------------------------------------------------------------
	
	/** 
	 * @return A short string representation of this move's action descriptions. 
	 */
	public String actionDescriptionStringShort()
	{
		String actionString = "";
		for (final Action a : actions())
			actionString += a.getDescription() + ", ";
		actionString = actionString.substring(0, actionString.length()-2);
		return actionString;
	}
	
	/** 
	 * @param context 
	 * @param useCoords 
	 * @return A long string representation of this move's action descriptions. 
	 */
	public String actionDescriptionStringLong(final Context context, final boolean useCoords)
	{
		String actionString = "";
		for (final Action a : actions())
		{
			String moveLocations = a.toTurnFormat(context, useCoords);
			if (moveLocations.endsWith("-"))
				moveLocations = moveLocations.substring(0, moveLocations.length()-1);
			actionString += a.getDescription() + " (" + moveLocations + "), ";
		}
		actionString = actionString.substring(0, actionString.length()-2);
		return actionString;
	}
	
	//-------------------------------------------------------------------------

}
