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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import app.boardMaker.display.buttons.CancelButton;
import app.boardMaker.display.buttons.CreateButton;
import app.boardMaker.display.panels.previewPanel.PreviewListener;
import app.boardMaker.handlers.Displayer;
import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.quadhex.Quadhex;

public class QuadhexPanel extends OptionPanel
{
	private Displayer displayer;
	private PreviewListener pl;
	
	private JSpinner layerSpinner;
	private boolean thirds = false;
	
	private GraphFunction board;
	
	public QuadhexPanel(Displayer displayer) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));	
		setPreferredSize(new Dimension(displayer.getParamPanel().getWidth(), displayer.getParamPanel().getHeight()));
		
		this.displayer = displayer;
		pl = new PreviewListener(this, displayer.getPreviewPanel());
		
		add(Box.createVerticalStrut(5));
		
		JPanel panel;
		JLabel label;
		
		label = new JLabel("Layers: ");
		label.setToolTipText("Number of layers. The number of cells per side will be twice this number.");
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(label);
		layerSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		layerSpinner.addChangeListener(pl);
		panel.add(layerSpinner);
		add(panel);
		
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("Split: ");
		label.setToolTipText("Splits the board in 3 subsections.");
		panel.add(label);
		ButtonGroup group = new ButtonGroup();
		JRadioButton buttonYes = new JRadioButton("Enabled");
		buttonYes.setEnabled(false);
		buttonYes.addActionListener(pl);
		buttonYes.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// TODO Auto-generated method stub
				thirds = true;
			}
		});
		group.add(buttonYes);
		panel.add(buttonYes);
		JRadioButton buttonNo = new JRadioButton("Disabled");
		buttonNo.setSelected(true);
		buttonNo.addActionListener(pl);
		buttonNo.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// TODO Auto-generated method stub
				thirds = false;
			}
		});
		group.add(buttonNo);
		panel.add(buttonNo);
		add(panel);
		
		layerSpinner.addChangeListener(new ChangeListener()
		{
			
			@Override
			public void stateChanged(ChangeEvent e)
			{
				// TODO Auto-generated method stub
				if ((Integer)layerSpinner.getValue() == 1) {
					buttonNo.setSelected(true);
					buttonYes.setSelected(false);
					buttonYes.setEnabled(false);
					
					thirds = false;
				} else {
					buttonYes.setEnabled(true);
				}
			}
		});
		
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
		DimConstant dimA = new DimConstant((int)layerSpinner.getValue());
		board = new Quadhex(dimA, thirds);
	}

	@Override
	public GraphFunction board()
	{
		return board;
	}

}
