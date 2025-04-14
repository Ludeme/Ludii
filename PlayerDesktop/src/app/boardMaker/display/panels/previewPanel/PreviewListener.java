package app.boardMaker.display.panels.previewPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import app.boardMaker.display.panels.paramPanel.tilings.MancalaPanel;
import app.boardMaker.display.panels.paramPanel.tilings.OptionPanel;

public class PreviewListener implements ActionListener, ChangeListener
{
	private OptionPanel op;
	private MancalaPanel mp;
	private PreviewPanel pp;
	
	public PreviewListener(OptionPanel op, PreviewPanel pp) {
		this.op = op;
		this.pp = pp;
	}
	
	public PreviewListener(MancalaPanel mp, PreviewPanel pp) {
		this.mp = mp;
		this.pp = pp;
	}
	
	@Override
	public void stateChanged(ChangeEvent e)
	{
		if (op != null) {
			op.createBoard();
			pp.setBoard(op.board());
		} else if (mp != null) {
			mp.createBoard();
			pp.setBoard(mp.board());
		}
		pp.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (op != null) {
			op.createBoard();
			pp.setBoard(op.board());
		} else if (mp != null) {
			mp.createBoard();
			pp.setBoard(mp.board());
		}
		pp.repaint();
	}

}
