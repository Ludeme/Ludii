package app.boardMaker.display.panels.paramPanel.tilings;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import app.boardMaker.display.buttons.CancelButton;
import app.boardMaker.display.buttons.CreateButton;
import app.boardMaker.display.panels.previewPanel.PreviewListener;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.equipment.container.board.custom.SurakartaBoard;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.hex.Hex;
import game.functions.graph.generators.basis.hex.HexShapeType;
import game.functions.graph.generators.basis.tri.Tri;
import game.functions.graph.generators.basis.tri.TriShapeType;
import main.Constants;

public class SurakartaTPanel extends SurakartaPanel
{
	private Maker maker;
	private Displayer displayer;
	private PreviewListener pl;
	
	private JSpinner primDim;
	private JSpinner loop;
	private JSpinner start;
	
	private Board board;
	
	public SurakartaTPanel(Maker maker) {
		super();
		
		this.maker = maker;
		this.displayer = maker.getDisplayer();
		maker.setMancala(false);
		maker.setSurakarta(true);

		pl = new PreviewListener(this, displayer.getPreviewPanel());
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));	
		setPreferredSize(new Dimension(displayer.getParamPanel().getWidth(), displayer.getParamPanel().getHeight()));
		
		add(Box.createVerticalStrut(5));
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel("First dimension");
		label.setToolTipText("Primary dimension of the board.");
		panel.add(label);
		primDim = new JSpinner(new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1));
		primDim.addChangeListener(pl);
		panel.add(primDim);
		add(panel);
		
		add(Box.createVerticalStrut(5));
		
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("Loops: ");
		label.setToolTipText("Number of loops. If 0, then add a loop for every 2 cells on the side ((FirstDim - 1) / 2).");
		panel.add(label);
		loop = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		loop.addChangeListener(pl);
		panel.add(loop);
		add(panel);
		
		add(Box.createVerticalStrut(5));
		
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("From: ");
		label.setToolTipText("Which row to start the loop from.");
		panel.add(label);
		start = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		start.addChangeListener(pl);
		panel.add(start);
		add(panel);
		
		add(Box.createVerticalGlue());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(new CreateButton(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				createBoard();
				displayer.getBoardPanel().setBoard(board);
				displayer.mainView();	
			}
		}));
		buttonPanel.add(new CancelButton(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				displayer.mainView();
			}
		}));
		
		add(buttonPanel);
		
		add(Box.createVerticalStrut(5));
		
		createBoard();
		displayer.getPreviewPanel().setBoard(board);
	}
	
	@Override
	public Board board() {
		return board;
	}
	
	@Override
	public void createBoard() {
		GraphFunction graph = Tri.construct(TriShapeType.Triangle, new DimConstant((Integer)primDim.getValue()), null);
		int nloop = ((Integer)loop.getValue() == 0) ? Constants.UNDEFINED : (Integer)loop.getValue();
		board = new SurakartaBoard(graph, nloop, (Integer)start.getValue(), maker.largeStack());
	}
}
