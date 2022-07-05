package bridge;

import controllers.BaseController;
import controllers.container.BasicController;
import controllers.container.PyramidalController;
import game.equipment.component.Component;
import game.equipment.container.Container;
import metadata.graphics.util.ComponentStyleType;
import metadata.graphics.util.ContainerStyleType;
import metadata.graphics.util.ControllerType;
import other.context.Context;
import view.component.ComponentStyle;
import view.component.custom.CardStyle;
import view.component.custom.DieStyle;
import view.component.custom.ExtendedShogiStyle;
import view.component.custom.ExtendedXiangqiStyle;
import view.component.custom.NativeAmericanDiceStyle;
import view.component.custom.PieceStyle;
import view.component.custom.large.DominoStyle;
import view.component.custom.large.LargePieceStyle;
import view.component.custom.large.TileStyle;
import view.container.ContainerStyle;
import view.container.styles.BoardStyle;
import view.container.styles.HandStyle;
import view.container.styles.board.BackgammonStyle;
import view.container.styles.board.BoardlessStyle;
import view.container.styles.board.ChessStyle;
import view.container.styles.board.Connect4Style;
import view.container.styles.board.ConnectiveGoalStyle;
import view.container.styles.board.GoStyle;
import view.container.styles.board.HoundsAndJackalsStyle;
import view.container.styles.board.IsometricStyle;
import view.container.styles.board.JanggiStyle;
import view.container.styles.board.LascaStyle;
import view.container.styles.board.MancalaStyle;
import view.container.styles.board.ShibumiStyle;
import view.container.styles.board.ShogiStyle;
import view.container.styles.board.SnakesAndLaddersStyle;
import view.container.styles.board.SpiralStyle;
import view.container.styles.board.SurakartaStyle;
import view.container.styles.board.TableStyle;
import view.container.styles.board.TaflStyle;
import view.container.styles.board.UltimateTicTacToeStyle;
import view.container.styles.board.XiangqiStyle;
import view.container.styles.board.graph.GraphStyle;
import view.container.styles.board.graph.PenAndPaperStyle;
import view.container.styles.board.puzzle.FutoshikiStyle;
import view.container.styles.board.puzzle.HashiStyle;
import view.container.styles.board.puzzle.KakuroStyle;
import view.container.styles.board.puzzle.PuzzleStyle;
import view.container.styles.board.puzzle.SudokuStyle;
import view.container.styles.hand.DeckStyle;
import view.container.styles.hand.DiceStyle;

/**
 * Factory for creating specified graphics Style for an Item.
 * @author matthew.stephenson and cambolbro
 */
public class ViewControllerFactory
{
	public static ContainerStyle createStyle
	(
		final Bridge bridge, final Container container, final ContainerStyleType type, final Context context
	)
	{
		if (type == null)
			return new BoardStyle(bridge, container);
	
		switch(type)
		{
		
		// core types
		case Board:
			return new BoardStyle(bridge, container);
		case Hand:
			return new HandStyle(bridge, container);
		case Deck:
			return new DeckStyle(bridge, container);
		case Dice:
			return new DiceStyle(bridge, container);
			
		// puzzle types
		case Puzzle:
			return new PuzzleStyle(bridge, container, context);
		case Sudoku:
			return new SudokuStyle(bridge, container, context);
		case Kakuro:
			return new KakuroStyle(bridge, container, context);
		case Futoshiki:
			return new FutoshikiStyle(bridge, container, context);
		case Hashi:
			return new HashiStyle(bridge, container, context);
			
		// graph types
		case Graph:
			return new GraphStyle(bridge, container, context);
		case PenAndPaper:
			return new PenAndPaperStyle(bridge, container, context);
		
		// custom types
		case Backgammon:
			return new BackgammonStyle(bridge, container);
		case Boardless:
			return new BoardlessStyle(bridge, container);
		case Chess:
			return new ChessStyle(bridge, container, context);
		case ConnectiveGoal:
			return new ConnectiveGoalStyle(bridge, container);
		case Go:
			return new GoStyle(bridge, container);
		case HoundsAndJackals:
			return new HoundsAndJackalsStyle(bridge, container);
		case Janggi:
			return new JanggiStyle(bridge, container);
		case Lasca:
			return new LascaStyle(bridge, container);
		case Mancala:
			return new MancalaStyle(bridge, container);
		case Shibumi:
			return new ShibumiStyle(bridge, container);
		case Shogi:
			return new ShogiStyle(bridge, container);
		case SnakesAndLadders:
			return new SnakesAndLaddersStyle(bridge, container);
		case Tafl:
			return new TaflStyle(bridge, container);
		case Xiangqi:
			return new XiangqiStyle(bridge, container);
		case Connect4:
			return new Connect4Style(bridge, container);
		case Spiral:
			return new SpiralStyle(bridge, container);
		case Surakarta:
			return new SurakartaStyle(bridge, container);
		case UltimateTicTacToe:
			return new UltimateTicTacToeStyle(bridge, container);
		case Isometric:
			return new IsometricStyle(bridge, container);
		case Table:
			return new TableStyle(bridge, container);
		default:
			return new BoardStyle(bridge, container);
		}
	}
	
	//-------------------------------------------------------------------------
	
	public static ComponentStyle createStyle
	(
		final Bridge bridge, final Component component, final ComponentStyleType type
	)
	{
		if (type == null)
			return new PieceStyle(bridge, component);
	
		switch(type)
		{
		case Piece:
			return new PieceStyle(bridge, component);
		case Card:
			return new CardStyle(bridge, component);
		case Die:
			return new DieStyle(bridge, component);
		case Domino:
			return new DominoStyle(bridge, component);
		case Tile:
			return new TileStyle(bridge, component);
		case LargePiece:
			return new LargePieceStyle(bridge, component);
		case ExtendedShogi:
			return new ExtendedShogiStyle(bridge, component);
		case ExtendedXiangqi:
			return new ExtendedXiangqiStyle(bridge, component);
		case NativeAmericanDice:
			return new NativeAmericanDiceStyle(bridge, component);
		
		default:
			return new PieceStyle(bridge, component);
		}
	}
	
	//-------------------------------------------------------------------------

	public static BaseController createController(final Bridge bridge, final Container container, final ControllerType type) 
	{
		if (type == null)
			return new BasicController(bridge, container);
	
		switch(type)
		{
		case BasicController:
			return new BasicController(bridge, container);
		case PyramidalController:
			return new PyramidalController(bridge, container);
		default:
			return new BasicController(bridge, container);
		}
	}
	
	//-------------------------------------------------------------------------
	
}
