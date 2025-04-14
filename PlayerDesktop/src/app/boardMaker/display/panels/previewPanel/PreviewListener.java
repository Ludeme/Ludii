package app.boardMaker.display.panels.previewPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import app.boardMaker.display.panels.paramPanel.tilings.MancalaPanel;
import app.boardMaker.display.panels.paramPanel.tilings.OptionPanel;
import app.boardMaker.display.panels.paramPanel.tilings.SurakartaPanel;
import game.equipment.container.board.custom.SurakartaBoard;

public class PreviewListener implements ActionListener, ChangeListener
{
	private OptionPanel op;
	private MancalaPanel mp;
	private PreviewPanel pp;
	private SurakartaPanel sp;
	
	public PreviewListener(OptionPanel op, PreviewPanel pp) {
		this.op = op;
		this.pp = pp;
	}
	
	public PreviewListener(MancalaPanel mp, PreviewPanel pp) {
		this.mp = mp;
		this.pp = pp;
	}
	
	public PreviewListener(SurakartaPanel sp, PreviewPanel pp) {
		this.sp = sp;
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
		} else if (sp != null) {
			sp.createBoard();
			pp.setBoard(sp.board());
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
		} else if (sp != null) {
			sp.createBoard();
			pp.setBoard(sp.board());
		}
		pp.repaint();
	}

}
