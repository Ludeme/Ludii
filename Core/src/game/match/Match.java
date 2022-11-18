package game.match;

import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.equipment.Equipment;
import game.equipment.component.Component;
import game.equipment.container.Container;
import game.equipment.container.board.Board;
import game.equipment.container.other.Dice;
import game.players.Players;
import game.rules.end.End;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import main.Constants;
import main.grammar.Description;
import metadata.Metadata;
import other.AI;
import other.GameLoader;
import other.action.others.ActionNextInstance;
import other.concept.Concept;
import other.concept.ConceptDataType;
import other.context.Context;
import other.move.Move;
import other.playout.PlayoutMoveSelector;
import other.topology.TopologyElement;
import other.trial.Trial;

/**
 * Defines a match made up of a series of subgames.
 * 
 * @author Eric.Piette
 */
public class Match extends Game
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The end condition for the match. */
	private End end;

	/** The difference possible instance for a match. */
	protected Subgame[] instances;

	//-------------------------------------------------------------------------

	/**
	 * @param name    The name of the match.
	 * @param players The players of the match [(players 2)].
	 * @param games   The different subgames that make up the match.
	 * @param end     The end rules of the match.
	 * 
	 * @example (match "Match" (players 2) (games { (subgame "Tic-Tac-Toe" next:1)
	 *          (subgame "Yavalath" next:2) (subgame "Breakthrough" next:0) } ) (end
	 *          { (if (and (= (count Trials) 3) (> (matchScore P1) (matchScore P2)))
	 *          (result P1 Win)) (if (and (= (count Trials) 3) (< (matchScore P1)
	 *          (matchScore P2))) (result P2 Win)) (if (and (= (count Trials) 3) (=
	 *          (matchScore P1) (matchScore P2))) (result P1 Draw)) }) )
	 */
	public Match
	(
		 	 final String name,
		@Opt final Players players,
			 final Games games,
			 final End end
	)
	{
		super(name, players, null, null, null);

		final List<Subgame> subgames = games.games();

		this.instances = new Subgame[subgames.size()];
		for (int i = 0; i < subgames.size(); i++)
			this.instances[i] = subgames.get(i);

		if (instances.length == 0)
			throw new IllegalArgumentException("A match needs at least one game.");

		stateReference = null;
		this.end = end;
		this.end.setMatch(true);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param name            The name of the game.
	 * @param gameDescription The description of the game.
	 */
	@Hide
	public Match(final String name, final Description gameDescription)
	{
		super(name, gameDescription);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public Move apply(final Context context, final Move move, final boolean skipEndRules)
	{
		context.getLock().lock();
		
		try
		{
			if (move.containsNextInstance())
			{
				// We need to move on to next instance, so apply on match context instead of subcontext
				assert (context.subcontext().trial().over()); 
				assert (move.actions().size() == 1 && move.actions().get(0) instanceof ActionNextInstance);
				context.currentInstanceContext().trial().addMove(move);
				context.trial().addMove(move);
				context.advanceInstance();
				return move;
			}
			
			final Context subcontext = context.subcontext();
			final Trial subtrial = subcontext.trial();
			final int numMovesBeforeApply = subtrial.numMoves();
				
			// First just apply the move on the subcontext
			final Move appliedMove = subcontext.game().apply(subcontext, move, skipEndRules);
			
			if (!skipEndRules)
			{
				// Will likely have to append some extra moves to the match-wide trial
				final List<Move> subtrialMoves = subtrial.generateCompleteMovesList();
				final int numMovesAfterApply = subtrialMoves.size();
				final int numMovesToAppend = numMovesAfterApply - numMovesBeforeApply;
				for (int i = 0; i < numMovesToAppend; ++i)
					context.trial().addMove(subtrialMoves.get(subtrialMoves.size() - 1 - i));
			}
				
			return appliedMove;
		}
		finally
		{
			context.getLock().unlock();
		}
	}
	
	@Override
	public Moves moves(final Context context)
	{
		context.getLock().lock();
		
		try
		{
			final Context subcontext = context.subcontext();
			final Moves moves;
			
			if (subcontext.trial().over())
			{
				// Our only action will be to move on to next instance
				moves = new BaseMoves(null);
				
				if (context.trial().over())
					return moves;		// Full match is over
				
				final ActionNextInstance action = new ActionNextInstance();
				action.setDecision(true);
				final Move move = new Move(action);
				move.setDecision(true);
				move.setMover(subcontext.state().mover());
				moves.moves().add(move);
			}
			else
			{
				// Normal moves generation
				moves = subcontext.game().moves(subcontext);
			}
			
			if (context.trial().auxilTrialData() != null)
				context.trial().auxilTrialData().updateNewLegalMoves(moves, context);
			
			return moves;
		}
		finally
		{
			context.getLock().unlock();
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public void setMetadata(final Object md)
	{
		metadata = (Metadata)md;

		// We add the concepts of the metadata to the game.
		if (metadata != null)
		{
			final BitSet metadataConcept = metadata.concepts(this);
			booleanConcepts.or(metadata.concepts(this));
			final boolean stackTypeUsed = booleanConcepts.get(Concept.StackType.id());
			if (stackTypeUsed && !metadataConcept.get(Concept.Stack.id())
					&& booleanConcepts.get(Concept.StackState.id()))
				booleanConcepts.set(Concept.Stack.id(), false);
		}
		else
		{
			metadata = new Metadata(null, null, null, null);
		}
	}
	
	//-------------------------------------------------------------------------

	@Override
	public End endRules()
	{
		return end;
	}

	@Override
	public Subgame[] instances()
	{
		return instances;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean hasSubgames()
	{
		return true;
	}

	@Override
	public boolean hasCustomPlayouts()
	{
		for (final Subgame instance : instances)
		{
			if (instance.getGame().hasCustomPlayouts())
				return true;
		}

		return false;
	}

	@Override
	public void disableMemorylessPlayouts()
	{
		for (final Subgame instance : instances)
		{
			instance.disableMemorylessPlayouts();
		}
	}

	@Override
	public boolean usesNoRepeatPositionalInGame()
	{
		return false;
	}

	@Override
	public boolean usesNoRepeatPositionalInTurn()
	{
		return false;
	}
	
	@Override
	public boolean requiresScore()
	{
		return true;	// Matches always have scores
	}
	
	@Override
	public boolean automove()
	{
		return false;	// this flag should never be true for a Match
	}

	//-------------------------------------------------------------------------
	
	@Override
	public Board board()
	{
		// StringRoutines.stackTrace();
		System.err.println("Match.board() always returns null! Should probably call context.board() instead.");
		return null;
	}
	
	@Override
	public Equipment equipment()
	{
		//StringRoutines.stackTrace();
		System.err.println("Match.equipment() always returns null! Should probably call context.equipment() instead.");
		return null;
	}
	
	@Override
	public boolean hasSharedPlayer()
	{
		//StringRoutines.stackTrace();
		System.err.println("Match.hasSharedPlayer() always returns false! Should probably call context.hasSharedPlayer() instead.");
		return false;
	}
	
	@Override
	public List<Dice> handDice()
	{
		//StringRoutines.stackTrace();
		System.err.println("Match.handDice() always returns null! Should probably call context.handDice() instead.");
		return null;
	}
	
	@Override
	public int numContainers()
	{
		//StringRoutines.stackTrace();
		System.err.println("Match.numContainers() always returns -1! Should probably call context.numContainers() instead.");
		return Constants.UNDEFINED;
	}

	@Override
	public int numComponents()
	{
		//StringRoutines.stackTrace();
		System.err.println("Match.numComponents() always returns -1! Should probably call context.numComponents() instead.");
		return Constants.UNDEFINED;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void create()
	{
		if (finishedPreprocessing)
			System.err.println("Warning! Match.create() has already previously been called on " + name());

		GameLoader.compileInstance(instances()[0]);
		
		finishedPreprocessing = true;
		booleanConcepts = computeBooleanConcepts();
		conceptsNonBoolean = computeNonBooleanConcepts();
		hasMissingRequirement = computeRequirementReport();
		willCrash = computeCrashReport();
	}

	@Override
	public void start(final Context context)
	{
		context.getLock().lock();

		try
		{
			final Context subcontext = context.subcontext();
			final Trial subtrial = subcontext.trial();
			final int numMovesBeforeStart = subtrial.numMoves();
				
			// Start the first instance
			instances()[0].getGame().start(subcontext);
			
			// Will maybe have to append some extra moves to the match-wide trial
			final List<Move> subtrialMoves = subtrial.generateCompleteMovesList();
			final int numMovesAfterStart = subtrialMoves.size();
			final int numMovesToAppend = numMovesAfterStart - numMovesBeforeStart;
			for (int i = 0; i < numMovesToAppend; ++i)
				context.trial().addMove(subtrialMoves.get(subtrialMoves.size() - 1 - i));
			
//			// Let the match-wide trial know how many initial moves there are
//			context.trial().setNumInitialPlacementMoves(subtrial.numInitialPlacementMoves());

			// Make sure our "real" context's RNG actually gets used and progresses
			if (!context.trial().over() && context.game().isStochasticGame())
				context.game().moves(context);
		}
		finally
		{
			context.getLock().unlock();
		}
	}
	
	@Override
	public Trial playout
	(
		final Context context, final List<AI> ais, final double thinkingTime,
		final PlayoutMoveSelector playoutMoveSelector, final int maxNumBiasedActions, 
		final int maxNumPlayoutActions, final Random random
	)
	{
		return context.model().playout
		(
			context, ais, thinkingTime, playoutMoveSelector,
			maxNumBiasedActions, maxNumPlayoutActions,
			random
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public boolean isGraphGame()
	{
		//StringRoutines.stackTrace();
		System.err.println("Match.isGraphGame() always returns false! Should probably call context.isGraphGame() instead.");
		return false;
	}

	@Override
	public boolean isVertexGame()
	{
		//StringRoutines.stackTrace();
		System.err.println("Match.isVertexGame() always returns false! Should probably call context.isVertexGame() instead.");
		return false;
	}

	@Override
	public boolean isEdgeGame()
	{
		//StringRoutines.stackTrace();
		System.err.println("Match.isEdgeGame() always returns false! Should probably call context.isEdgeGame() instead.");
		return false;
	}

	@Override
	public boolean isCellGame()
	{
		//StringRoutines.stackTrace();
		System.err.println("Match.isCellGame() always returns false! Should probably call context.isCellGame() instead.");
		return false;
	}
	
	@Override
	public boolean equipmentWithStochastic()
	{
		final BitSet regionConcept = new BitSet();

		if (instances != null)
			for (final Subgame subgame : instances)
				if (subgame.getGame() != null)
					for (int i = 0; i < subgame.getGame().equipment().regions().length; i++)
						regionConcept.or(subgame.getGame().equipment().regions()[i].concepts(this));
		
		return regionConcept.get(Concept.Stochastic.id());
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet computeBooleanConcepts()
	{
		final BitSet gameConcept = new BitSet();
		if (end != null)
			gameConcept.or(end.concepts(this));

		if (instances != null)
			for (final Subgame subgame : instances)
				gameConcept.or(subgame.concepts(this));

		gameConcept.set(Concept.Match.id(), true);

		return gameConcept;
	}

	@Override
	public BitSet computeWritingEvalContextFlag()
	{
		final BitSet writingEvalContextFlags = new BitSet();
		if (end != null)
			writingEvalContextFlags.or(end.writesEvalContextRecursive());

		if (instances != null)
			for (final Subgame subgame : instances)
				writingEvalContextFlags.or(subgame.writesEvalContextRecursive());

		return writingEvalContextFlags;
	}

	@Override
	public BitSet computeReadingEvalContextFlag()
	{
		final BitSet readingEvalContextFlags = new BitSet();
		if (end != null)
			readingEvalContextFlags.or(end.readsEvalContextRecursive());

		if (instances != null)
			for (final Subgame subgame : instances)
				readingEvalContextFlags.or(subgame.readsEvalContextRecursive());

		return readingEvalContextFlags;
	}

	@Override
	public Map<Integer, String> computeNonBooleanConcepts()
	{
		final Map<Integer, String> nonBooleanConcepts = new HashMap<Integer, String>();

		int countPlayableSites = 0;
		int countPlayableSitesOnBoard = 0;
		int numColumns = 0;
		int numRows = 0;
		int numCorners = 0;
		double avgNumDirection = 0.0;
		double avgNumOrthogonalDirection = 0.0;
		double avgNumDiagonalDirection = 0.0;
		double avgNumAdjacentlDirection = 0.0;
		double avgNumOffDiagonalDirection = 0.0;
		int numOuterSites = 0;
		int numInnerSites = 0;
		int numLayers = 0;
		int numEdges = 0;
		int numCells = 0;
		int numVertices = 0;
		int numPerimeterSites = 0;
		int numTopSites = 0;
		int numBottomSites = 0;
		int numRightSites = 0;
		int numLeftSites = 0;
		int numCentreSites = 0;
		int numConvexCorners = 0;
		int numConcaveCorners = 0;
		int numPhasesBoard = 0;
		int numComponentsType = 0;
		double numComponentsTypePerPlayer = 0.0;
		int numPlayPhase = 0;
		int numDice = 0;
		int numContainers = 0;
		int numStartComponents = 0;
		int numStartComponentsHands = 0;
		int numStartComponentsBoard = 0;
		int numPlayers = 0;

		int numGamesCompiled = 0;
		
		for (final Subgame subGame : instances)
		{
			final Game game = subGame.getGame();

			if(game != null)
			{
			numGamesCompiled++;
			final SiteType defaultSiteType = game.board().defaultSite();
			final List<? extends TopologyElement> elements = game.board().topology().getGraphElements(defaultSiteType);
			final int numDefaultElements = elements.size();
			int totalNumDirections = 0;
			int totalNumOrthogonalDirections = 0;
			int totalNumDiagonalDirections = 0;
			int totalNumAdjacentDirections = 0;
			int totalNumOffDiagonalDirections = 0;
			for (final TopologyElement element : elements)
			{
				totalNumDirections += element.neighbours().size();
				totalNumOrthogonalDirections += element.orthogonal().size();
				totalNumDiagonalDirections += element.diagonal().size();
				totalNumAdjacentDirections += element.adjacent().size();
				totalNumOffDiagonalDirections += element.off().size();
			}

			for (final Concept concept : Concept.values())
				if (!concept.dataType().equals(ConceptDataType.BooleanData))
				{
					switch (concept)
					{
					case NumPlayableSites:
						for (int cid = 0; cid < game.equipment().containers().length; cid++)
						{
							final Container container = game.equipment().containers()[cid];
							if (cid != 0)
								countPlayableSites += container.numSites();
							else
							{
								if (booleanConcepts.get(Concept.Cell.id()))
									countPlayableSites += container.topology().cells().size();

								if (booleanConcepts.get(Concept.Vertex.id()))
									countPlayableSites += container.topology().vertices().size();

								if (booleanConcepts.get(Concept.Edge.id()))
									countPlayableSites += container.topology().edges().size();
							}
						}
						break;
					case NumPlayableSitesOnBoard:
						final Container container = game.equipment().containers()[0];
						if (booleanConcepts.get(Concept.Cell.id()))
							countPlayableSitesOnBoard += container.topology().cells().size();

						if (booleanConcepts.get(Concept.Vertex.id()))
							countPlayableSitesOnBoard += container.topology().vertices().size();

						if (booleanConcepts.get(Concept.Edge.id()))
							countPlayableSitesOnBoard += container.topology().edges().size();
						break;
					case NumColumns:
						numColumns += game.board().topology().columns(defaultSiteType).size();
						break;
					case NumPlayers:
						numPlayers += game.players().count();
						break;
					case NumRows:
						numRows += game.board().topology().rows(defaultSiteType).size();
						break;
					case NumCorners:
						numCorners += game.board().topology().corners(defaultSiteType).size();
						break;
					case NumDirections:
						avgNumDirection += (double) totalNumDirections / (double) numDefaultElements;
						break;
					case NumOrthogonalDirections:
						avgNumOrthogonalDirection += (double) totalNumOrthogonalDirections
								/ (double) numDefaultElements;
						break;
					case NumDiagonalDirections:
						avgNumDiagonalDirection += (double) totalNumDiagonalDirections / (double) numDefaultElements;
						break;
					case NumAdjacentDirections:
						avgNumAdjacentlDirection += (double) totalNumAdjacentDirections / (double) numDefaultElements;
						break;
					case NumOffDiagonalDirections:
						avgNumOffDiagonalDirection += (double) totalNumOffDiagonalDirections
								/ (double) numDefaultElements;
						break;
					case NumOuterSites:
						numOuterSites += game.board().topology().outer(defaultSiteType).size();
						break;
					case NumInnerSites:
						numInnerSites += game.board().topology().inner(defaultSiteType).size();
						break;
					case NumLayers:
						numLayers += game.board().topology().layers(defaultSiteType).size();
						break;
					case NumEdges:
						numEdges += game.board().topology().edges().size();
						break;
					case NumCells:
						numCells += game.board().topology().cells().size();
						break;
					case NumVertices:
						numVertices += game.board().topology().vertices().size();
						break;
					case NumPerimeterSites:
						numPerimeterSites += game.board().topology().perimeter(defaultSiteType).size();
						break;
					case NumTopSites:
						numTopSites += game.board().topology().top(defaultSiteType).size();
						break;
					case NumBottomSites:
						numBottomSites += game.board().topology().bottom(defaultSiteType).size();
						break;
					case NumRightSites:
						numRightSites += game.board().topology().right(defaultSiteType).size();
						break;
					case NumLeftSites:
						numLeftSites += game.board().topology().left(defaultSiteType).size();
						break;
					case NumCentreSites:
						numCentreSites += game.board().topology().centre(defaultSiteType).size();
						break;
					case NumConvexCorners:
						numConvexCorners += game.board().topology().cornersConvex(defaultSiteType).size();
						break;
					case NumConcaveCorners:
						numConcaveCorners += game.board().topology().cornersConcave(defaultSiteType).size();
						break;
					case NumPhasesBoard:
						final List<List<TopologyElement>> phaseElements = game.board().topology()
								.phases(defaultSiteType);
						for (final List<TopologyElement> topoElements : phaseElements)
							if (topoElements.size() != 0)
								numPhasesBoard++;
						break;
					case NumComponentsType:
						numComponentsType += game.equipment().components().length - 1;
						break;
					case NumComponentsTypePerPlayer:
						final int[] componentsPerPlayer = new int[game.players().size()];
						for (int i = 1; i < game.equipment().components().length; i++)
						{
							final Component component = game.equipment().components()[i];
							if (component.owner() > 0 && component.owner() < players().size())
								componentsPerPlayer[component.owner()]++;
						}
						int numOwnerComponent = 0;
						for (int i = 1; i < componentsPerPlayer.length; i++)
							numOwnerComponent += componentsPerPlayer[i];
						String avgNumComponentPerPlayer =  players.count() <= 0 ? "0": new DecimalFormat("##.##")
								.format((double) numOwnerComponent / (double) players.count()) + "";
						avgNumComponentPerPlayer = avgNumComponentPerPlayer.replaceAll(",", ".");
						numComponentsTypePerPlayer += ((double) numOwnerComponent / (double) players.count());
						break;
					case NumPlayPhase:
						numPlayPhase += game.rules().phases().length;
						break;
					case NumDice:
						for (int i = 1; i < game.equipment().components().length; i++)
							if (game.equipment().components()[i].isDie())
								numDice++;
						break;
					case NumContainers:
						numContainers += game.equipment().containers().length;
						break;
					case NumStartComponents:
						nonBooleanConcepts.put(Integer.valueOf(concept.id()), numStartComponents + "");
						break;
					case NumStartComponentsHand:
						nonBooleanConcepts.put(Integer.valueOf(concept.id()), numStartComponentsHands + "");
						break;
					case NumStartComponentsBoard:
						nonBooleanConcepts.put(Integer.valueOf(concept.id()), numStartComponentsBoard + "");
						break;
					case NumStartComponentsPerPlayer:
						nonBooleanConcepts.put(Integer.valueOf(concept.id()), numStartComponents / players().count() + "");
						break;
					case NumStartComponentsHandPerPlayer:
						nonBooleanConcepts.put(Integer.valueOf(concept.id()), numStartComponentsHands / players().count() + "");
						break;
					case NumStartComponentsBoardPerPlayer:
						nonBooleanConcepts.put(Integer.valueOf(concept.id()), numStartComponentsBoard / players().count() + "");
						break;
					default:
						break;
					}
				}
			}
		}

		nonBooleanConcepts.put(Integer.valueOf(Concept.NumPlayableSites.id()), ((double) countPlayableSites / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumPlayableSitesOnBoard.id()), ((double) countPlayableSitesOnBoard / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumColumns.id()), ((double) numColumns / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumRows.id()), ((double) numRows / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumCorners.id()), ((double) numCorners / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumDirections.id()), new DecimalFormat("##.##").format(avgNumDirection / numGamesCompiled).replaceAll(",", ".") + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumOrthogonalDirections.id()), new DecimalFormat("##.##").format(avgNumOrthogonalDirection / numGamesCompiled).replaceAll(",", ".") + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumDiagonalDirections.id()), new DecimalFormat("##.##").format(avgNumDiagonalDirection / numGamesCompiled).replaceAll(",", ".") + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumAdjacentDirections.id()), new DecimalFormat("##.##").format(avgNumAdjacentlDirection / numGamesCompiled).replaceAll(",", ".") + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumOffDiagonalDirections.id()), new DecimalFormat("##.##").format(avgNumOffDiagonalDirection / numGamesCompiled).replaceAll(",", ".") + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumOuterSites.id()), ((double) numOuterSites / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumInnerSites.id()), ((double) numInnerSites / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumLayers.id()), ((double) numLayers / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumEdges.id()), ((double) numEdges / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumCells.id()), ((double) numCells / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumVertices.id()), ((double) numVertices / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumPerimeterSites.id()), ((double) numPerimeterSites / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumTopSites.id()), ((double) numTopSites / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumBottomSites.id()), ((double) numBottomSites / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumRightSites.id()), ((double) numRightSites / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumLeftSites.id()), ((double) numLeftSites / (double) numGamesCompiled) + ""); 
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumCentreSites.id()), ((double) numCentreSites / (double) numGamesCompiled) + ""); 
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumConvexCorners.id()), ((double) numConvexCorners / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumConcaveCorners.id()), ((double) numConcaveCorners / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumPhasesBoard.id()), ((double) numPhasesBoard / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumComponentsType.id()), ((double) numComponentsType / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumComponentsTypePerPlayer.id()), (numComponentsTypePerPlayer / numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumPlayPhase.id()), ((double) numPlayPhase / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumDice.id()), ((double) numDice / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumContainers.id()), ((double) numContainers / (double) numGamesCompiled) + "");
		nonBooleanConcepts.put(Integer.valueOf(Concept.NumPlayers.id()), ((double) numPlayers / (double) numGamesCompiled) + "");
		
		return nonBooleanConcepts;
	}

	@Override
	public boolean computeRequirementReport()
	{
		boolean missingRequirement = false;

		if (end != null)
			missingRequirement |= end.missingRequirement(this);

		if (instances != null)
			for (final Subgame subgame : instances)
				missingRequirement |= subgame.missingRequirement(this);

		return missingRequirement;
	}

	@Override
	public boolean computeCrashReport()
	{
		boolean crash = false;

		if (end != null)
			crash |= end.willCrash(this);

		if (instances != null)
			for (final Subgame subgame : instances)
				crash |= subgame.willCrash(this);

		return crash;
	}
	
	@Override
	public boolean isStacking()
	{
		return false;
	}

}
