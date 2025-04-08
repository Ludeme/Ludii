package app.boardMaker.display.panels.previewPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import app.boardMaker.display.panels.paramPanel.tilings.OptionPanel;

public class PreviewListener implements ActionListener, ChangeListener
{
	private OptionPanel op;
	private PreviewPanel pp;
	
	public PreviewListener(OptionPanel op, PreviewPanel pp) {
		this.op = op;
		this.pp = pp;
	}
	
	@Override
	public void stateChanged(ChangeEvent e)
	{
		op.createBoard();
		pp.setBoard(op.board());
		pp.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		op.createBoard();
		pp.setBoard(op.board());
		pp.repaint();
	}

}
