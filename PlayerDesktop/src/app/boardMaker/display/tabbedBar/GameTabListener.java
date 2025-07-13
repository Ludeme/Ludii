package app.boardMaker.display.tabbedBar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import app.boardMaker.handlers.Maker;
import app.boardMaker.res.StyleType;
import game.types.board.SiteType;

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
			maker.setSiteType(SiteType.Vertex);

			maker.getDisplayer().getCurrentDisplay().revalidate();
			maker.getDisplayer().getCurrentDisplay().repaint();

			break;
		case "Edge":
			maker.setSiteType(SiteType.Edge);

			maker.getDisplayer().getCurrentDisplay().revalidate();
			maker.getDisplayer().getCurrentDisplay().repaint();

			break;
		case "Cell":
			maker.setSiteType(SiteType.Cell);

			maker.getDisplayer().getCurrentDisplay().revalidate();
			maker.getDisplayer().getCurrentDisplay().repaint();

			break;
		case "Stack":
			maker.setStack(((JRadioButton)e.getSource()).isSelected());
			break;
		default:
			break;
		}
	}

}
