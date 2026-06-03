package app.boardMaker.display.panels.paramPanel.tilings;

import javax.swing.JPanel;

import app.boardMaker.dataStruct.board.ContainerInfo;
import game.equipment.container.board.Board;

public abstract class OptionPanel extends JPanel
{
	public abstract void createBoard();
	
	public abstract Board board();

	public abstract ContainerInfo createInfo();
}
