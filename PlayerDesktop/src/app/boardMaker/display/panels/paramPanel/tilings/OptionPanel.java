package app.boardMaker.display.panels.paramPanel.tilings;

import javax.swing.JPanel;

import game.equipment.container.board.Board;
import game.functions.graph.GraphFunction;

public abstract class OptionPanel extends JPanel
{
	public abstract void createBoard();
	
	public abstract GraphFunction board();
}
