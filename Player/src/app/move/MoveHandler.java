package app.move;

import java.awt.EventQueue;
import java.util.ArrayList;

import app.PlayerApp;
import app.utils.PuzzleSelectionType;
import game.equipment.component.Component;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import main.Constants;
import main.collections.FastArrayList;
import other.action.Action;
import other.action.puzzle.ActionReset;
import other.action.puzzle.ActionSet;
import other.action.puzzle.ActionToggle;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.move.Move;
import other.state.container.ContainerState;
import other.topology.Vertex;
import util.ContainerUtil;

/**
 * Functions for handling moves made by humans.
 * 
 * @author Matthew.Stephenson
 */
public class MoveHandler
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Try to make a move for the specified From and To locations.
	 * If more than one possible move is found, the users is asked which they want.
	 * If one possible move is found, that move is applied.
	 * @return True if a matching legal move found, false otherwise
	 */
	public static boolean tryGameMove(final PlayerApp app, final Location locnFromInfo, final Location locnToInfo, final boolean passMove, final int selectPlayerMove)
	{
		final Context context = app.manager().ref().context();
		final Moves legal = context.game().moves(context);
		final FastArrayList<Move> possibleMoves = new FastArrayList<>();
		
		// only used in web app, to force multiple possible moves in some cases.
		boolean forceMultiplePossibleMoves = false;

		// Check if de-selecting a previously selected piece
		if (app.settingsPlayer().componentIsSelected() && app.bridge().settingsVC().lastClickedSite().equals(locnFromInfo))
			return false;
		
		if (app.bridge().settingsVC().selectingConsequenceMove())
		{
			applyConsequenceChosen(app, locnToInfo);
			return true;
		}

		if (passMove)
		{
			for (final Move m : legal.moves())
			{
				if (m.isPass())
					possibleMoves.add(m);
				
				if (m.containsNextInstance())
					possibleMoves.add(m);
			}
		}
		else if (selectPlayerMove != -1)
		{
			for (final Move m : legal.moves())
			{
				if (m.playerSelected() == selectPlayerMove)
					possibleMoves.add(m);
			}
		}
		else
		{			
			for (final Move move : legal.moves())
			{
				if (locnFromInfo.site() == -1)
					return false;
				
				// Check if any other legal moves have fromInfo as their from location.
				if 
				(
					locnFromInfo.equals(locnToInfo) 
					&& 
					move.getFromLocation().equals(locnFromInfo) 
					&& 
					!move.getToLocation().equals(locnToInfo) 
					&& 
					!app.settingsPlayer().componentIsSelected()
				)
				{
					forceMultiplePossibleMoves = true;
				}
				
				// If move matches clickInfo, then store it as a possible move.
				if (MoveHandler.moveMatchesLocation(app, move, locnFromInfo, locnToInfo, context))
				{
					boolean moveAlreadyAvailable = false;
					for (final Move m : possibleMoves)
						if (m.getActionsWithConsequences(context).equals(move.getActionsWithConsequences(context)))
							moveAlreadyAvailable = true;
	
					if (!moveAlreadyAvailable)
						possibleMoves.add(move);
				}
			}
		}
		
		if (app.settingsPlayer().printMoveFeatures() || app.settingsPlayer().printMoveFeatureInstances())
		{
			printMoveFeatures(app, context, possibleMoves);			
			return false;
		}

		if (possibleMoves.size() > 1 || (possibleMoves.size() > 0 && forceMultiplePossibleMoves && !app.settingsPlayer().usingMYOGApp()))
		{
			// If several different moves are possible.
			return handleMultiplePossibleMoves(app, possibleMoves, context);
		}
		else if (possibleMoves.size() == 1)
		{
			if (MoveHandler.moveChecks(app, possibleMoves.get(0)))
			{
				app.manager().ref().applyHumanMoveToGame(app.manager(), possibleMoves.get(0));
				return true; // move found
			}
		}

		return false; // move not found
	}
	
	//-------------------------------------------------------------------------
	
	private static void printMoveFeatures(final PlayerApp app, final Context context, final FastArrayList<Move> possibleMoves)
	{
		// Not supported anymore because decision trees in metadata mess up the implementation
		System.err.println("Printing move features is not currently supported.");
		
		
		// Don't apply move, but print active features for all matching moves
//		final SoftmaxFromMetadataSelection softmax = app.settingsPlayer().featurePrintingSoftmax();
//		softmax.initIfNeeded(context.game(), context.state().mover());
//		
//		final BaseFeatureSet[] featureSets = softmax.featureSets();
//		final BaseFeatureSet featureSet;
//		if (featureSets[0] != null)
//			featureSet = featureSets[0];
//		else
//			featureSet = featureSets[context.state().mover()];
//		
//		if (app.settingsPlayer().printMoveFeatures())
//		{
//			for (final Move move : possibleMoves)
//			{
//				final List<Feature> activeFeatures = featureSet.computeActiveFeatures(context, move);
//				System.out.println("Listing active features for move: " + move);
//				
//				for (final Feature activeFeature : activeFeatures)
//					System.out.println(activeFeature);
//			}
//		}
//			
//		if (app.settingsPlayer().printMoveFeatureInstances())
//		{
//			for (final Move move : possibleMoves)
//			{
//				final List<FeatureInstance> activeFeatureInstances = 
//						featureSet.getActiveSpatialFeatureInstances
//						(
//							context.state(), 
//							FeatureUtils.fromPos(context.trial().lastMove()),
//							FeatureUtils.toPos(context.trial().lastMove()),
//							FeatureUtils.fromPos(move),
//							FeatureUtils.toPos(move), 
//							move.mover()
//						);
//				System.out.println("Listing active feature instances for move: " + move);
//				
//				for (final FeatureInstance activeFeatureInstance : activeFeatureInstances)
//					System.out.println(activeFeatureInstance);
//			}
//		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Try and carry out a move for the puzzle.
	 */
	public static void tryPuzzleMove(final PlayerApp app, final Location locnFromInfo, final Location locnToInfo)
	{
		final Context context = app.contextSnapshot().getContext(app);
		final Moves legal = context.game().moves(context);
		
		for (final Move move : legal.moves())
		{
			if (moveMatchesLocation(app, move, locnFromInfo, locnToInfo, context))
			{
				final ContainerState cs = context.state().containerStates()[0];
				
				final int site = move.from();
				SiteType setType = move.fromType();
				
				int maxValue = 0;
				int puzzleValue = 0;
				boolean valueResolved = false;
				boolean valueFound = false;
			
				maxValue = context.board().getRange(setType).max(context);
				puzzleValue = cs.what(site, setType);
				valueResolved = cs.isResolved(site, setType);
				valueFound = false;
				
				if (!valueResolved)
					puzzleValue = -1;
				
				for (int i = puzzleValue + 1; i < maxValue + 1; i++)
				{
					other.action.puzzle.ActionSet a = null;
					a = new ActionSet(setType, site, i);
					a.setDecision(true);
					final Move m = new Move(a);
					m.setFromNonDecision(move.from());
					m.setToNonDecision(move.to());
					m.setEdgeMove(site);
					m.setDecision(true);
					m.setOrientedMove(false);
					
					if 
					(
						context.game().moves(context).moves().contains(m) 
						||
						(app.settingsPlayer().illegalMovesValid() && i > 0)
					)
					{
						valueFound = true;
						puzzleValue = i;
						break;
					}
				}

				if 
				(
					app.settingsPlayer().puzzleDialogOption() == PuzzleSelectionType.Dialog 
					|| 
					(
						app.settingsPlayer().puzzleDialogOption() == PuzzleSelectionType.Automatic 
						&& 
						maxValue > 3
					)
				)
				{
					app.showPuzzleDialog(site);
				}
				else
				{
					if (!valueFound)
					{
						final Move resetMove = new Move(new ActionReset(context.board().defaultSite(), site, maxValue + 1));
						resetMove.setDecision(true);
						
						if (MoveHandler.moveChecks(app, resetMove))
							app.manager().ref().applyHumanMoveToGame(app.manager(), resetMove);
					}
					else
					{
						puzzleMove(app, site, puzzleValue, true, setType);
						
						// Set all unresolved edges, faces and vertices to the first value.
						if (context.trial().over())
						{
							setType = SiteType.Edge;
							for (int i = 0; i < context.board().topology().edges().size(); i++)
								if (!cs.isResolvedEdges(i))
									puzzleMove(app, i, 0, true, setType);

							setType = SiteType.Cell;
							for (int i = 0; i < context.board().topology().cells().size(); i++)
								if (!cs.isResolvedVerts(i))
									puzzleMove(app, i, 0, true, setType);

							setType = SiteType.Vertex;
							for (int i = 0; i < context.board().topology().vertices().size(); i++)
								if (!cs.isResolvedCell(i))
									puzzleMove(app, i, 0, true, setType);
						}
					}
				}
				
				break;
			}
		}
	}
	
	/**
	 * Move made specifically for a puzzle game.
	 * Involves either selecting a value or toggling a value for a site.
	 */
	public static void puzzleMove(final PlayerApp app, final int site, final int puzzleValue, final boolean leftClick, final SiteType type)
	{
		Action a = null;

		if (leftClick)
			a = new ActionSet(type, site, puzzleValue);			// Set value
		else
			a = new ActionToggle(type, site, puzzleValue);		// Toggle value

		final Move m = new Move(a);
		m.setDecision(true);
		
		if (MoveHandler.moveChecks(app, m))
			app.manager().ref().applyHumanMoveToGame(app.manager(), m);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Handle the cases where several moves are possible for the same from/to ClickInfo.
	 */
	private static boolean handleMultiplePossibleMoves(final PlayerApp app, final FastArrayList<Move> possibleMoves, final Context context)
	{
		app.bridge().settingsVC().possibleConsequenceLocations().clear();		
		app.manager().settingsManager().possibleConsequenceMoves().clear();
		app.bridge().settingsVC().setSelectingConsequenceMove(false);

		int minMoveLength = 9999;
		for (final Move m : possibleMoves)
			if (m.getActionsWithConsequences(context).size() < minMoveLength)
				minMoveLength = m.getActionsWithConsequences(context).size();

		int differentAction = -1;
		for (int i = 0; i < minMoveLength; i++)
		{
			Action sameAction = null;
			boolean allSame = true;
			for (final Move m : possibleMoves)
			{
				if (sameAction == null)
					sameAction = m.getActionsWithConsequences(context).get(i);
				else if (!sameAction.equals(m.getActionsWithConsequences(context).get(i)))
					allSame = false;
			}

			if (!allSame)
			{
				differentAction = i;
				break;
			}
		}		

		if (differentAction == -1)
		{
			app.showPossibleMovesDialog(context, possibleMoves);
			return false;
		}
		else
		{		
			for (final Move m : possibleMoves)
			{
				app.bridge().settingsVC().possibleConsequenceLocations()
						.add(new FullLocation(m.getActionsWithConsequences(context).get(differentAction).to(),
								m.getActionsWithConsequences(context).get(differentAction).levelTo(),
								m.getActionsWithConsequences(context).get(differentAction).toType()));
				
				// ** FIXME: Not thread-safe.
				app.manager().settingsManager().possibleConsequenceMoves().add(m);

				if (m.getActionsWithConsequences(context).get(differentAction).to() < 0)
				{
					app.showPossibleMovesDialog(context, possibleMoves);
					return false;
				}				
			}
			
			// If any of the possibleToLocations are duplicates then need the dialog.
			final ArrayList<Location> checkForDuplicates = new ArrayList<>();
			boolean duplicateFound = false;
			for (int i = 0; i < app.bridge().settingsVC().possibleConsequenceLocations().size(); i++)
			{
				for (final Location location : checkForDuplicates)
				{
					if (location.equals(app.bridge().settingsVC().possibleConsequenceLocations().get(i)))
					{
						duplicateFound = true;
						break;
					}
				}
				if (duplicateFound)
				{
					app.showPossibleMovesDialog(context, possibleMoves);
					return false;
				}
				checkForDuplicates.add(app.bridge().settingsVC().possibleConsequenceLocations().get(i));
			}
			
			app.bridge().settingsVC().setSelectingConsequenceMove(true);
			
			// Need to event queue this message so that it overrides the "invalid move" message.
			app.setTemporaryMessage("Please select a consequence.");
			
			return true;
		}
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Applies the chosen consequence, corresponding with the selected location.
	 */
	private static void applyConsequenceChosen(final PlayerApp app, final Location location)
	{
		boolean moveMade = false;
		for (int i = 0; i < app.bridge().settingsVC().possibleConsequenceLocations().size(); i++)
		{
			if (app.bridge().settingsVC().possibleConsequenceLocations().get(i).site() == location.site())
			{
				if (MoveHandler.moveChecks(app, app.manager().settingsManager().possibleConsequenceMoves().get(i)))
				{
					app.manager().ref().applyHumanMoveToGame(app.manager(), app.manager().settingsManager().possibleConsequenceMoves().get(i));
					moveMade = true;
					break;
				}
			}
		}
		
		if (!moveMade)
			app.setVolatileMessage("That is not a valid move.");

		app.bridge().settingsVC().setSelectingConsequenceMove(false);
		app.bridge().settingsVC().possibleConsequenceLocations().clear();
		
		app.manager().settingsManager().possibleConsequenceMoves().clear();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Checks if Move matches the From and To location information.
	 */
	private static boolean moveMatchesLocation(final PlayerApp app, final Move move, final Location fromInfo, final Location toInfo, final Context context)
	{
		if (checkVertexMoveForEdge(move, fromInfo, toInfo, context))
			return true;
		
		if (move.matchesUserMove(fromInfo.site(), fromInfo.level(), fromInfo.siteType(), toInfo.site(), toInfo.level(), toInfo.siteType()))
			return moveMatchesDraggedPieceRotation(app, move, fromInfo);
		
		return false;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Whether the player dragged between two vertices to indicate an edge move.
	 */
	private static boolean checkVertexMoveForEdge(final Move move, final Location fromInfo, final Location toInfo, final Context context)
	{
		// player can perform an edge move by dragging between its two vertices.
		if (move.fromType() == SiteType.Edge && move.toType() == SiteType.Edge && move.getFromLocation().equals(move.getToLocation()))
		{
			// only works if dragging between vertices, and not dragging to the same vertex.
			if (fromInfo.siteType() == SiteType.Vertex
					&& fromInfo.siteType() == SiteType.Vertex
					&& fromInfo.site() != toInfo.site())
			{
				if (move.from() == move.to())
				{
					final int edgeIndex = move.from();
					final Vertex va = context.board().topology().edges().get(edgeIndex).vA();
					final Vertex vb = context.board().topology().edges().get(edgeIndex).vB();
					
					if (va.index() == fromInfo.site() && vb.index() == toInfo.site())
						return true;
					
					if (!move.isOrientedMove() && vb.index() == fromInfo.site() && va.index() == toInfo.site())
							return true;
				}
			}
			else
			{
				return false;
			}
		}
		
		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/** 
	 * returns true if the rotation of the dragged component matches the move specified. 
	 */
	private static boolean moveMatchesDraggedPieceRotation(final PlayerApp app, final Move move, final Location fromInfo)
	{
		final Context context = app.contextSnapshot().getContext(app);
		
		if (context.game().hasLargePiece() && app.bridge().settingsVC().pieceBeingDragged())
		{
			final int containerId = ContainerUtil.getContainerId(context, fromInfo.site(), fromInfo.siteType());				
			final int componentIndex = context.containerState(containerId).whatCell(fromInfo.site());
			
			if 
			(
				componentIndex > 0 
				&& 
				move.what() == Constants.NO_PIECE 
				&& 
				context.components()[componentIndex].isLargePiece() 
				&& 
				move.state() != app.settingsPlayer().dragComponentState()
			)
				return false;
		}

		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Applies the legal move that matches the direction (if only one match)
	 */
	public static void applyDirectionMove(final PlayerApp app, final AbsoluteDirection direction)
	{
		final Context context = app.manager().ref().context();
		final Moves legal = context.game().moves(context);
		
		// Find all valid moves for the specified direction.
		final ArrayList<Move> validMovesfound = new ArrayList<>();
		for (final Move m : legal.moves())
			if (direction.equals(m.direction(context.topology())))
				validMovesfound.add(m);
		
		// If only one valid move found, apply it to the game.
		if (validMovesfound.size() == 1)
		{
			if (moveChecks(app, validMovesfound.get(0)))
				app.manager().ref().applyHumanMoveToGame(app.manager(), validMovesfound.get(0));
		}
		else
		{
			if (validMovesfound.size() == 0)
				app.setVolatileMessage("No valid moves found for Direction " + direction.name());
			else
				app.setVolatileMessage("Too many valid moves found for Direction " + direction.name());
			
			EventQueue.invokeLater(() -> 
			{
				app.manager().getPlayerInterface().repaint();
			});
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Checks if any of the legal moves are duplicates or not decisions.
	 */
	public static void checkMoveWarnings(final PlayerApp app) 
	{
		final Context context = app.contextSnapshot().getContext(app);
		
		final Moves legal = context.moves(context);

		for (int i = 0; i < legal.moves().size(); i++)
		{
			final Move m1 = legal.moves().get(i);
			
			if (!context.game().isSimulationMoveGame())
			{	
				// Check if any moves are not decisions.
				if (!m1.isDecision())
					app.manager().getPlayerInterface().addTextToStatusPanel("WARNING: Move " + m1.getActionsWithConsequences(context) + " was not a decision move. If you see this in an official Ludii game, please report it to us.\n");
				
				// Check if any moves have more than one decision.
				int decisionCounter = 0;
				for (final Action a : m1.actions())
					if (a.isDecision())
						decisionCounter++;
				if (decisionCounter > 1)
					app.manager().getPlayerInterface().addTextToStatusPanel("WARNING: Move " + m1.getActionsWithConsequences(context) + " has multiple decision actions. If you see this in an official Ludii game, please report it to us.\n");
				
				// Check if any moves have an illegal mover.
				if (m1.mover() <= 0 || m1.mover() > context.game().players().count())
					app.manager().getPlayerInterface().addTextToStatusPanel("WARNING: Move " + m1.getActionsWithConsequences(context) + " has an illegal mover (" + m1.mover() + "). If you see this in an official Ludii game, please report it to us.\n");
				
				// Check if more than one pass move.
				int passMoveCounter = 0;
				if (m1.isPass())
					passMoveCounter++;
				if (passMoveCounter > 1)
					app.manager().getPlayerInterface().addTextToStatusPanel("WARNING: Multiple Pass moves detected in the legal moves.\n");
				
				// Check if more than one swap move.
				int swapMoveCounter = 0;
				if (m1.isSwap())
					swapMoveCounter++;
				if (swapMoveCounter > 1)
					app.manager().getPlayerInterface().addTextToStatusPanel("WARNING: Multiple Swap moves detected in the legal moves.\n");
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Checks that need to be made before any code specific to moves is performed.
	 */
	public static boolean moveChecks(final PlayerApp app, final Move move)
	{
		final Context context = app.manager().ref().context();

		if (!move.isAlwaysGUILegal() && !context.model().verifyMoveLegal(context, move))
		{
			System.err.println("Selected illegal move: " + move.getActionsWithConsequences(context));
			app.addTextToStatusPanel("Selected illegal move: " + move.getActionsWithConsequences(context) + "\n");
			return false;
		}

		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns the component associated with the to position of the last move.
	 */
	public static Component getLastMovedPiece(final PlayerApp app)
	{
		final Context context = app.manager().ref().context();
		final Move lastMove = context.trial().lastMove();
		if (lastMove != null)
		{
			try
			{
				final int containerId = ContainerUtil.getContainerId(context, lastMove.getToLocation().site(), lastMove.getToLocation().siteType());	
				final int what = context.containerState(containerId).what(lastMove.getToLocation().site(), lastMove.getToLocation().siteType());
				
				// TODO update exhib rules so that you can only drag to correct site on shared hand.s
				if (containerId == 3)
					return null;
				
				if (context.trial().numberRealMoves() <= 0 || what == 0)
					return null;
				
				final Component lastMoveComponent = context.game().equipment().components()[what];
				return lastMoveComponent;
			}
			catch (final Exception e)
			{
				return null;
			}
		}
		return null;
	}
	
	//-------------------------------------------------------------------------
	
}
