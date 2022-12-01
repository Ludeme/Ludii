package other.concept;

import java.util.BitSet;

import game.Game;
import game.functions.booleans.BooleanFunction;
import game.rules.end.Result;
import game.types.play.ResultType;
import game.types.play.RoleType;
import other.context.Context;

/**
 * Utilities class used to compute the end concepts from the concepts of the
 * condition which is true.
 * 
 * @author Eric.Piette
 */
public class EndConcepts
{

	/**
	 * @param condition  The condition
	 * @param context    The context
	 * @param game       The game
	 * @param result     The result
	 * @return The ending concepts
	 */
	public static BitSet get
	(
		final BooleanFunction condition, 
		final Context context,
		final Game game,
		final Result result
	)
	{
		final ResultType resultType = (result != null) ? result.result() : null;
		final RoleType who = (result != null) ? result.who() : null;
		final BitSet condConcepts = (context == null) ? condition.concepts(game) : condition.stateConcepts(context);
		final BitSet endConcepts = new BitSet();

		// ------------------------------------------- Legal Moves End ----------------------------------------------------
		
		if (condConcepts.get(Concept.NoMoves.id()))
		{
			endConcepts.set(Concept.NoMovesEnd.id(), true);
			if(resultType != null && who != null)
			{
				if(resultType.equals(ResultType.Win))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.NoMovesWin.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.NoMovesLoss.id(), true);
				}
				else if(resultType.equals(ResultType.Loss))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.NoMovesLoss.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.NoMovesWin.id(), true);
				}
				else if(resultType.equals(ResultType.Draw))
					endConcepts.set(Concept.NoMovesDraw.id(), true);
			}
		}
		
		// ------------------------------------------- Time End ----------------------------------------------------
		
		if (condConcepts.get(Concept.ProgressCheck.id()))
		{
			endConcepts.set(Concept.NoProgressEnd.id(), true);
			if(resultType != null && who != null)
			{
				if(resultType.equals(ResultType.Win))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.NoProgressWin.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.NoProgressLoss.id(), true);
				}
				else if(resultType.equals(ResultType.Loss))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.NoProgressLoss.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.NoProgressWin.id(), true);
				}
				else if(resultType.equals(ResultType.Draw))
					endConcepts.set(Concept.NoProgressDraw.id(), true);
			}
		}
		
		// ------------------------------------------- Scoring End ----------------------------------------------------
		
		// Scoring End
		if (condConcepts.get(Concept.Scoring.id()))
		{
			endConcepts.set(Concept.ScoringEnd.id(), true);	
			if(resultType != null && who != null)
			{
				if(resultType.equals(ResultType.Win))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.ScoringWin.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.ScoringLoss.id(), true);
				}
				else if(resultType.equals(ResultType.Loss))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.ScoringLoss.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.ScoringWin.id(), true);
				}
				else if(resultType.equals(ResultType.Draw))
					endConcepts.set(Concept.ScoringDraw.id(), true);
			}
		}
		
		// ------------------------------------------- Race End ----------------------------------------------------
		
		// No Own Pieces End
		if (condConcepts.get(Concept.NoPieceMover.id()))
		{
			endConcepts.set(Concept.NoOwnPiecesEnd.id(), true);	
			if(resultType != null && who != null)
			{
				if(resultType.equals(ResultType.Win))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.NoOwnPiecesWin.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.NoOwnPiecesLoss.id(), true);
				}
				else if(resultType.equals(ResultType.Loss))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.NoOwnPiecesLoss.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.NoOwnPiecesWin.id(), true);
				}
				else if(resultType.equals(ResultType.Draw))
					endConcepts.set(Concept.NoOwnPiecesDraw.id(), true);
			}
			
			if(result.concepts(game).get(Concept.Scoring.id))
					endConcepts.set(Concept.NoOwnPiecesWin.id(), true);
		}

		// Fill End
		if (condConcepts.get(Concept.Fill.id()))
		{
			endConcepts.set(Concept.FillEnd.id(), true);
			if(resultType != null && who != null)
			{
				if(resultType.equals(ResultType.Win))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.FillWin.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.FillLoss.id(), true);
				}
				else if(resultType.equals(ResultType.Loss))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.FillLoss.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.FillWin.id(), true);
				}
				else if(resultType.equals(ResultType.Draw))
					endConcepts.set(Concept.FillDraw.id(), true);
			}
		}
		
		// Reach End
		if (condConcepts.get(Concept.Contains.id()))
		{
			endConcepts.set(Concept.ReachEnd.id(), true);
			if(resultType != null && who != null)
			{
				if(resultType.equals(ResultType.Win))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.ReachWin.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.ReachLoss.id(), true);
				}
				else if(resultType.equals(ResultType.Loss))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.ReachLoss.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.ReachWin.id(), true);
				}
				else if(resultType.equals(ResultType.Draw))
					endConcepts.set(Concept.ReachDraw.id(), true);
			}
		}

		// ---------------------------------------------- Capture End ------------------------------------------------
		
		// Checkmate end.
		if (condConcepts.get(Concept.CanNotMove.id()) && condConcepts.get(Concept.Threat.id()))
		{
			endConcepts.set(Concept.Checkmate.id(), true);
			if(resultType != null && who != null)
			{
				if(resultType.equals(ResultType.Win))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.CheckmateWin.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.CheckmateLoss.id(), true);
				}
				else if(resultType.equals(ResultType.Loss))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.CheckmateLoss.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.CheckmateWin.id(), true);
				}
				else if(resultType.equals(ResultType.Draw))
					endConcepts.set(Concept.CheckmateDraw.id(), true);
			}
		}

		// No Target piece End
		if (condConcepts.get(Concept.NoTargetPiece.id()))
		{
			endConcepts.set(Concept.NoTargetPieceEnd.id(), true);	
			if(resultType != null && who != null)
			{
				if(resultType.equals(ResultType.Win))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.NoTargetPieceWin.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.NoTargetPieceLoss.id(), true);
				}
				else if(resultType.equals(ResultType.Loss))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.NoTargetPieceLoss.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.NoTargetPieceWin.id(), true);
				}
				else if(resultType.equals(ResultType.Draw))
					endConcepts.set(Concept.NoTargetPieceDraw.id(), true);
			}
		}
		
		// Eliminate Pieces End
		if (condConcepts.get(Concept.NoPieceNext.id()) || condConcepts.get(Concept.CountPiecesNextComparison.id()) || 
				(condConcepts.get(Concept.NoPiece.id()) && !condConcepts.get(Concept.NoPieceMover.id())))
		{
			endConcepts.set(Concept.EliminatePiecesEnd.id(), true);	
			if(resultType != null && who != null)
			{
				if(resultType.equals(ResultType.Win))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.EliminatePiecesWin.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.EliminatePiecesLoss.id(), true);
				}
				else if(resultType.equals(ResultType.Loss))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.EliminatePiecesLoss.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.EliminatePiecesWin.id(), true);
				}
				else if(resultType.equals(ResultType.Draw))
					endConcepts.set(Concept.EliminatePiecesDraw.id(), true);
			}
		}

		//----------------------------------- Space End -----------------------------------------
		
		// Line End
		if (condConcepts.get(Concept.Line.id()))
		{
			endConcepts.set(Concept.LineEnd.id(), true);
			if(resultType != null && who != null)
			{
				if(resultType.equals(ResultType.Win))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.LineWin.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.LineLoss.id(), true);
				}
				else if(resultType.equals(ResultType.Loss))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.LineLoss.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.LineWin.id(), true);
				}
				else if(resultType.equals(ResultType.Draw))
					endConcepts.set(Concept.LineDraw.id(), true);
			}
		}
		
		// Connection End
		if (condConcepts.get(Concept.Connection.id()))
		{
			endConcepts.set(Concept.ConnectionEnd.id(), true);
			if(resultType != null && who != null)
			{
				if(resultType.equals(ResultType.Win))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.ConnectionWin.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.ConnectionLoss.id(), true);
				}
				else if(resultType.equals(ResultType.Loss))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.ConnectionLoss.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.ConnectionWin.id(), true);
				}
				else if(resultType.equals(ResultType.Draw))
					endConcepts.set(Concept.ConnectionDraw.id(), true);
			}
		}

		// Group End
		if (condConcepts.get(Concept.Group.id()))
		{
			endConcepts.set(Concept.GroupEnd.id(), true);
			if(resultType != null && who != null)
			{
				if(resultType.equals(ResultType.Win))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.GroupWin.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.GroupLoss.id(), true);
				}
				else if(resultType.equals(ResultType.Loss))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.GroupLoss.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.GroupWin.id(), true);
				}
				else if(resultType.equals(ResultType.Draw))
					endConcepts.set(Concept.GroupDraw.id(), true);
			}
		}

		// Loop End
		if (condConcepts.get(Concept.Loop.id()))
		{
			endConcepts.set(Concept.LoopEnd.id(), true);
			if(resultType != null && who != null)
			{
				if(resultType.equals(ResultType.Win))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.LoopWin.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.LoopLoss.id(), true);
				}
				else if(resultType.equals(ResultType.Loss))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.LoopLoss.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.LoopWin.id(), true);
				}
				else if(resultType.equals(ResultType.Draw))
					endConcepts.set(Concept.LoopDraw.id(), true);
			}
		}

		// Pattern End
		if (condConcepts.get(Concept.Pattern.id()))
		{
			endConcepts.set(Concept.PatternEnd.id(), true);
			if(resultType != null && who != null)
			{
				if(resultType.equals(ResultType.Win))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.PatternWin.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.PatternLoss.id(), true);
				}
				else if(resultType.equals(ResultType.Loss))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.PatternLoss.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.PatternWin.id(), true);
				}
				else if(resultType.equals(ResultType.Draw))
					endConcepts.set(Concept.PatternDraw.id(), true);
			}
		}

		// Territory End
		if (condConcepts.get(Concept.Territory.id()))
		{
			endConcepts.set(Concept.TerritoryEnd.id(), true);
			if(resultType != null && who != null)
			{
				if(resultType.equals(ResultType.Win))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.TerritoryWin.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.TerritoryLoss.id(), true);
				}
				else if(resultType.equals(ResultType.Loss))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.TerritoryLoss.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.TerritoryWin.id(), true);
				}
				else if(resultType.equals(ResultType.Draw))
					endConcepts.set(Concept.TerritoryDraw.id(), true);
			}
		}

		// PathExtent End
		if (condConcepts.get(Concept.PathExtent.id()))
		{
			endConcepts.set(Concept.PathExtentEnd.id(), true);
			if(resultType != null && who != null)
			{
				if(resultType.equals(ResultType.Win))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.PathExtentWin.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.PathExtentLoss.id(), true);
				}
				else if(resultType.equals(ResultType.Loss))
				{
					if(who.equals(RoleType.Mover))
						endConcepts.set(Concept.PathExtentLoss.id(), true);
					else if(who.equals(RoleType.Next) && game.players().count() == 2)
						endConcepts.set(Concept.PathExtentWin.id(), true);
				}
				else if(resultType.equals(ResultType.Draw))
					endConcepts.set(Concept.PathExtentDraw.id(), true);
			}
		}
		
		//----------------------------- Misere -------------------------------------------------
		
		if(game.players().count() == 2)
		{
			if(resultType != null && who != null)
			{
				if (
					(resultType.equals(ResultType.Win) && who.equals(RoleType.Next)) 
					|| 
					resultType.equals(ResultType.Loss) && who.equals(RoleType.Mover)
				   )
					endConcepts.set(Concept.Misere.id(), true);
			}
		}

		if (result != null)
			endConcepts.or(result.concepts(game));

		return endConcepts;
	}

}
