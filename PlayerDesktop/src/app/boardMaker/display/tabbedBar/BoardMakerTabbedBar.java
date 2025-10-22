package app.boardMaker.display.tabbedBar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import game.types.board.SiteType;
import game.types.play.ModeType;
import game.types.play.RoleType;

/**
 * Class representing the main tabbed bar of the board maker
 */

public class BoardMakerTabbedBar extends JTabbedPane
{
	private Displayer displayer;
	private Maker maker;
	
	private JToolBar toolbar;
	
	private JSpinner players;
	private JTextField name;
	private JComboBox<ModeType> mode;

	private GameTabListener gtl;
	
	private boolean visible;
	
	public BoardMakerTabbedBar(Maker maker) {
		this.displayer = maker.getDisplayer();
		this.maker = maker;
		
		displayer.setTabbedBar(this);

		gtl = new GameTabListener(maker);
		
		initToolbar();
		makeGameToolbar();
		addTab("Game", toolbar);
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
		JLabel label = new JLabel("Name: " + maker.getName());
		toolbar.add(label);
		
		toolbar.addSeparator();
		
		label = new JLabel("Players: " + maker.getPlayers());
		toolbar.add(label);

		toolbar.addSeparator();
		
		label = new JLabel("Mode: " + maker.getMode());
		toolbar.add(label);
		
		toolbar.addSeparator();

		label = new JLabel("Site: " + maker.getSiteType());
		toolbar.add(label);

		toolbar.addSeparator();
		
		toolbar.add(new JSeparator(SwingConstants.VERTICAL));
		
		toolbar.addSeparator();

		label = new JLabel("Show: ");
		toolbar.add(label);
		
		JRadioButton button;
		ButtonGroup group = new ButtonGroup();
		button = new JRadioButton("Cells");
		button.setActionCommand("Cells");
		button.setSelected(maker.getSiteType() == SiteType.Cell);
		button.addActionListener(gtl);
		group.add(button);
		toolbar.add(button);
		button = new JRadioButton("Graph");
		button.setActionCommand("Graph");
		button.setSelected(maker.getSiteType() != SiteType.Cell);
		button.addActionListener(gtl);
		group.add(button);
		toolbar.add(button);
		
		toolbar.addSeparator();
		
		toolbar.add(new JSeparator(SwingConstants.VERTICAL));
		
		toolbar.addSeparator();
		
		button = new JRadioButton("Large stacks");
		button.setActionCommand("Stack");
		button.setToolTipText("Allows the game to involve stacks higher than 32.");
		button.setSelected(false);
		button.addActionListener(gtl);
		toolbar.add(button);
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	public boolean visible() {
		return visible;
	}
}
