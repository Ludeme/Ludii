package app.boardMaker.display.panels.paramPanel.tilings;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import app.boardMaker.display.buttons.CancelButton;
import app.boardMaker.display.buttons.CreateButton;
import app.boardMaker.display.panels.previewPanel.PreviewListener;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.brick.BrickShapeType;
import game.functions.graph.generators.shape.Spiral;

public class SpiralPanel extends OptionPanel
{
	private Displayer displayer;
	private PreviewListener pl;
	
	private JSpinner turns;
	private JSpinner sites;
	private boolean clock = true;
	
	private GraphFunction board;
	
	public SpiralPanel(Maker maker) {
		super();
		this.displayer = maker.getDisplayer();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(displayer.getParamPanel().getWidth(), displayer.getParamPanel().getHeight()));

		maker.setMancala(false);
		maker.setSurakarta(false);

		pl = new PreviewListener(this, displayer.getPreviewPanel());
		
		add(Box.createVerticalStrut(5));
		
		JPanel panel;
		JLabel label;
		
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("Turns: ");
		label.setToolTipText("The number of times the spiral turns.");
		panel.add(label);
		turns = new JSpinner(new SpinnerNumberModel(3, 3, Integer.MAX_VALUE, 1));
		turns.addChangeListener(pl);
		panel.add(turns);
		add(panel);
		
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("Sites: ");
		label.setToolTipText("Total number of sites.");
		panel.add(label);
		sites = new JSpinner(new SpinnerNumberModel(3, 3, Integer.MAX_VALUE, 1));
		sites.addChangeListener(pl);
		panel.add(sites);
		add(panel);
		
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("Rotation: ");
		label.setToolTipText("Direction of the rotation.");
		panel.add(label);
		ButtonGroup group = new ButtonGroup();
		JRadioButton button = new JRadioButton("Clockwise");
		button.setSelected(true);
		button.addActionListener(pl);
		button.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// TODO Auto-generated method stub
				clock = true;
			}
		});
		group.add(button);
		panel.add(button);
		button = new JRadioButton("Counterclockwise");
		button.addActionListener(pl);
		button.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// TODO Auto-generated method stub
				clock = false;
			}
		});
		group.add(button);
		panel.add(button);
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
	public void createBoard()
	{
		board = new Spiral(new DimConstant((int) turns.getValue()), new DimConstant((int) sites.getValue()), clock);
	}

	@Override
	public GraphFunction board()
	{
		return board;
	}

}
