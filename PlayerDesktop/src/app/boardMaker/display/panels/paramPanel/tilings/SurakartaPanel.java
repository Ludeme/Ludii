package app.boardMaker.display.panels.paramPanel.tilings;

import javax.swing.JPanel;

import app.boardMaker.dataStruct.board.ContainerInfo;
import game.equipment.container.board.Board;

public abstract class SurakartaPanel extends JPanel
{
	public abstract Board board();
	
	public abstract void createBoard();

	public abstract ContainerInfo createInfo();
}
