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
		JLabel label = new JLabel("Game name: ");
		label.setToolTipText("Sets the name of the game.");
		toolbar.add(label);
		toolbar.addSeparator();
		name = new JTextField();
		name.setActionCommand("Name");
		name.setText(maker.getName());
		name.addActionListener(gtl);
		toolbar.add(name);
		
		toolbar.addSeparator();
		
		label = new JLabel("Players: ");
		label.setToolTipText("Sets the number of player of the game.");
		toolbar.add(label);
		toolbar.addSeparator();
		players = new JSpinner(new SpinnerNumberModel(maker.getPlayers(), 1, Integer.MAX_VALUE, 1));
		players.addChangeListener(gtl);
		toolbar.add(players);
		
		toolbar.addSeparator();
		
		label = new JLabel("Mode: ");
		label.setToolTipText("<html>Sets the mode of the game."
				+ "<br>Alternating: turn by turn."
				+ "<br>Simultaneous: players move at the same time."
				+ "<br>Simulation: simulation game.</html>");
		toolbar.add(label);
		toolbar.addSeparator();
		mode = new JComboBox<ModeType>(ModeType.values());
		mode.setActionCommand("Mode");
		mode.setSelectedItem(maker.getMode());
		mode.addActionListener(gtl);
		toolbar.add(mode);
		
		toolbar.addSeparator();
		
		toolbar.add(new JSeparator(SwingConstants.VERTICAL));
		
		toolbar.addSeparator();
		
		JRadioButton button;
		ButtonGroup group = new ButtonGroup();
		for (SiteType type : SiteType.values()) {
			button = new JRadioButton(type.name());
			button.setActionCommand(type.name());
			button.addActionListener(gtl);

			switch (type)
			{
			case Cell:
				button.setSelected(true);
				break;
			case Edge:
				break;
			case Vertex:
				break;
			default:
				break;
			}

			group.add(button);
			toolbar.add(button);
		}
		
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
	
	public void updateGameInfo() {
		maker.setGameInfo(name.getText(), (Integer)players.getValue(), (ModeType)mode.getSelectedItem());
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	public boolean visible() {
		return visible;
	}
}
