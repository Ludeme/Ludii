package app.boardMaker.display.tabbedBar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import app.boardMaker.handlers.Displayer;

/**
 * Class representing the main tabbed bar of the board maker
 */

public class BoardMakerTabbedBar extends JTabbedPane
{
	private Displayer displayer;
	private JToolBar toolbar;
	
	public BoardMakerTabbedBar(Displayer displayer) {
		this.displayer = displayer;
		
		initToolbar();
		makeGameToolbar();
		addTab("Game", toolbar);
		
		initToolbar();
		makeBoardToolbar();
		addTab("Boards", toolbar);
		
		initToolbar();
		makeFuncToolbar();
		addTab("Functions", toolbar);
	}
	
	private void initToolbar() {
		toolbar = new JToolBar();
		toolbar.setRollover(true);
		toolbar.setFloatable(false);
	}
	
	/**
	 * Creates the game tab toolbar
	 */
	private void makeGameToolbar() {
		JLabel label = new JLabel("Game parameters placeholder");
		toolbar.add(label);
	}
	
	/**
	 * Creates the board tab toolbar
	 */
	private void makeBoardToolbar() {
		JButton button = new JButton("Create board placeholder");
		button.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				displayer.creationView();
			}
		});
		toolbar.add(button);
	}
	
	/**
	 * Creates the function tab toolbar
	 */
	private void makeFuncToolbar() {
		JLabel label = new JLabel("Functions placeholder");
		toolbar.add(label);
	}
}
