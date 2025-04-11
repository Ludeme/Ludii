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
import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.brick.Brick;
import game.functions.graph.generators.basis.brick.BrickShapeType;

public class BrickPanel extends OptionPanel
{
	private Displayer displayer;
	private PreviewListener pl;
	
	private JComboBox<BrickShapeType> shapeBox;
	private JSpinner rowSpinner;
	private JSpinner colSpinner;
	
	private boolean trim = false;
	
	private Board board;
	
	public BrickPanel(Displayer displayer) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));	
		setPreferredSize(new Dimension(displayer.getParamPanel().getWidth(), displayer.getParamPanel().getHeight()));
		
		this.displayer = displayer;
		pl = new PreviewListener(this, displayer.getPreviewPanel());
		
		add(Box.createVerticalStrut(5));
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel((String) displayer.getStrings().get("paramShape"));
		label.setToolTipText((String) displayer.getStrings().get("paramShapeTT"));
		shapeBox = new JComboBox<>(BrickShapeType.values());
		shapeBox.addActionListener(pl);
		panel.add(label);
		panel.add(shapeBox);
		add(panel);
				
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel((String) displayer.getStrings().get("row"));
		label.setToolTipText((String) displayer.getStrings().get("rowTT"));
		panel.add(label);
		rowSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		rowSpinner.addChangeListener(pl);
		panel.add(rowSpinner);
		add(panel);
		
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel((String) displayer.getStrings().get("col"));
		label.setToolTipText((String) displayer.getStrings().get("colTT"));
		panel.add(label);
		colSpinner = new JSpinner(new SpinnerNumberModel(0,0,Integer.MAX_VALUE,1));
		colSpinner.addChangeListener(pl);
		panel.add(colSpinner);
		add(panel);
		
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("Trim (optional): ");
		panel.add(label);
		ButtonGroup group = new ButtonGroup();
		JRadioButton button = new JRadioButton("Enabled");
		button.addActionListener(pl);
		button.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// TODO Auto-generated method stub
				trim = true;
			}
		});
		panel.add(button);
		group.add(button);
		button = new JRadioButton("Disabled");
		button.setSelected(true);
		button.addActionListener(pl);
		button.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// TODO Auto-generated method stub
				trim = false;
			}
		});
		panel.add(button);
		group.add(button);
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
		BrickShapeType shape = (BrickShapeType) shapeBox.getSelectedItem();
		DimConstant dimA = new DimConstant((int)rowSpinner.getValue());
		DimConstant dimB = new DimConstant((int)colSpinner.getValue());
		GraphFunction graph = Brick.construct(shape, dimA, (dimB.eval() == 0) ? null : dimB, trim);
		board = new Board(graph, null, null, null, null, null, null);
	}

	@Override
	public Board board()
	{
		return board;
	}

}
