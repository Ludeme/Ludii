package app.boardMaker.display.panels.paramPanel.tilings;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import app.boardMaker.dataStruct.board.BoardInfo;
import app.boardMaker.dataStruct.board.ContainerInfo;
import app.boardMaker.dataStruct.board.graphFunction.generator.SpiralInfo;
import app.boardMaker.display.components.buttons.CancelButton;
import app.boardMaker.display.components.buttons.CreateButton;
import app.boardMaker.display.panels.previewPanel.PreviewListener;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.shape.Spiral;

public class SpiralPanel extends OptionPanel
{
	private Maker maker;
	private Displayer displayer;
	private PreviewListener pl;
	
	private JSpinner turns;
	private JSpinner sites;
	private boolean clock = true;
	
	private Board board;
	
	public SpiralPanel(Maker maker) {
		super();
		this.maker = maker;
		this.displayer = maker.getDisplayer();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(displayer.getParamPanel().getWidth(), displayer.getParamPanel().getHeight()));

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
		sites = new JSpinner(new SpinnerNumberModel(4, 4, Integer.MAX_VALUE, 10));
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
				maker.addBoard(createInfo());
				//maker.getCurrentBoard().setContainerInfo(createInfo());
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

		displayer.getPreviewPanel().setBoard(createInfo());
	}

	@Override
	public void createBoard()
	{
		GraphFunction graph = new Spiral(new DimConstant((int) turns.getValue()), new DimConstant((int) sites.getValue()), clock);
		board = new Board(graph,null,null,null,null,maker.getSiteType(),maker.largeStack());
	}

	@Override
	public Board board()
	{
		return board;
	}

	@Override
	public ContainerInfo createInfo() {
		SpiralInfo info = new SpiralInfo((int)turns.getValue(), (int)sites.getValue(),clock);
		return new BoardInfo(maker,info);
	}

}
