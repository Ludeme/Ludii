package app.boardMaker.display.tabbedBar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import app.boardMaker.handlers.Maker;
import app.boardMaker.res.StyleType;

public class GameTabListener implements ActionListener, ChangeListener
{	
	private BoardMakerTabbedBar tabbedBar;
	private Maker maker;
	
	public GameTabListener(Maker maker) {
		this.tabbedBar = maker.getDisplayer().getTabbedBar();
		this.maker = maker;
	}
	
	@Override
	public void stateChanged(ChangeEvent e)
	{
		// TODO Auto-generated method stub
		tabbedBar.updateGameInfo();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// TODO Auto-generated method stub
		switch (e.getActionCommand())
		{
		case "Name":
			tabbedBar.updateGameInfo();
			break;
		case "Mode":
			tabbedBar.updateGameInfo();
			break;
		case "Vertex":
			maker.setStyle(StyleType.GraphStyle);
			maker.getDisplayer().getBoardPanel().revalidate();
			maker.getDisplayer().getBoardPanel().repaint();
			break;
		case "Edge":
			maker.setStyle(StyleType.GraphStyle);
			maker.getDisplayer().getBoardPanel().revalidate();
			maker.getDisplayer().getBoardPanel().repaint();
			break;
		case "Cell":
			maker.setStyle(StyleType.BoardStyle);
			maker.getDisplayer().getBoardPanel().revalidate();
			maker.getDisplayer().getBoardPanel().repaint();
			break;
		default:
			break;
		}
	}

}
