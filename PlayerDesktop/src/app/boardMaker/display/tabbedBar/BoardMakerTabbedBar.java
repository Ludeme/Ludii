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
	
	private final BoardButtonListener bl;
	private GameTabListener gtl;
	
	private boolean visible;
	
	public BoardMakerTabbedBar(Maker maker) {
		this.displayer = maker.getDisplayer();
		this.maker = maker;
		
		displayer.setTabbedBar(this);
		
		bl = new BoardButtonListener(maker);
		gtl = new GameTabListener(maker);
		
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
		JLabel label = new JLabel("Game name: ");
		label.setToolTipText((String)displayer.getStrings().get("gamenameTT"));
		toolbar.add(label);
		toolbar.addSeparator();
		name = new JTextField();
		name.setActionCommand("Name");
		name.setText(maker.getName());
		name.addActionListener(gtl);
		toolbar.add(name);
		
		toolbar.addSeparator();
		
		label = new JLabel("Players: ");
		label.setToolTipText((String)displayer.getStrings().get("playersTT"));
		toolbar.add(label);
		toolbar.addSeparator();
		players = new JSpinner(new SpinnerNumberModel(maker.getPlayers(), 1, Integer.MAX_VALUE, 1));
		players.addChangeListener(gtl);
		toolbar.add(players);
		
		toolbar.addSeparator();
		
		label = new JLabel("Mode: ");
		label.setToolTipText((String)displayer.getStrings().get("modeTT"));
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
			button.setToolTipText((String)displayer.getStrings().get(type.name()));
			if (type.name().equals("Cell")) {
				button.setSelected(true);
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
	
	/**
	 * Creates the board tab toolbar
	 */
	private void makeBoardToolbar() {
		JButton button;
		
		button = new JButton("New Board");
		button.setToolTipText("Adds a new board tab.");
		button.setActionCommand("New");
		button.addActionListener(bl);
		toolbar.add(button);
		
		button = new JButton("Delete");
		button.setToolTipText("Deletes the current board tab. (Min. 1)");
		button.setActionCommand("Remove");
		button.addActionListener(bl);
		toolbar.add(button);
		
		toolbar.addSeparator();
		
		toolbar.add(new JSeparator(SwingConstants.VERTICAL));
		
		toolbar.addSeparator();
		
		for (BoardTiling tiling : BoardTiling.values()) {
			button = new JButton(tiling.name());
			button.setToolTipText((String) displayer.getStrings().get(tiling.name()));
			button.setActionCommand(tiling.name());
			button.addActionListener(bl);
			toolbar.add(button);
		}
		
		toolbar.addSeparator();
		
		toolbar.add(new JSeparator(SwingConstants.VERTICAL));
		
		toolbar.addSeparator();
		
		button = new JButton("Mancala");
		button.setToolTipText("Creates a mancala board.");
		button.setActionCommand("Mancala");
		button.addActionListener(bl);
		toolbar.add(button);
		
		button = new JButton("Surakarta");
		button.setToolTipText("Creates a surakarta board. (Only for square, rectangle, hex and triangle board shapes)");
		button.setActionCommand("Surakarta");
		button.addActionListener(bl);
		toolbar.add(button);
	}
	
	/**
	 * Creates the function tab toolbar
	 */
	private void makeFuncToolbar() {
		JLabel label = new JLabel("Functions placeholder");
		toolbar.add(label);
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
