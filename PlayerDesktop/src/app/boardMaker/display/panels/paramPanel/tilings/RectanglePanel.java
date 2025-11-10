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

import app.boardMaker.dataStruct.board.BoardInfo;
import app.boardMaker.dataStruct.board.ContainerInfo;
import app.boardMaker.dataStruct.board.graphFunction.generator.RectangleInfo;
import app.boardMaker.display.components.buttons.CancelButton;
import app.boardMaker.display.components.buttons.CreateButton;
import app.boardMaker.display.panels.previewPanel.PreviewListener;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.square.DiagonalsType;
import game.functions.graph.generators.basis.square.RectangleOnSquare;

public class RectanglePanel extends OptionPanel
{
	private Maker maker;
	private Displayer displayer;
	private PreviewListener pl;
	
	private JSpinner rowSpinner;
	private JSpinner colSpinner;
	private JComboBox<DiagonalsType> diagBox;
	
	private Board board;
	
	public RectanglePanel(Maker maker) {
		super();
		this.maker = maker;
		this.displayer = maker.getDisplayer();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(displayer.getParamPanel().getWidth(), displayer.getParamPanel().getHeight()));

		pl = new PreviewListener(this,displayer.getPreviewPanel());
		
		add(Box.createVerticalStrut(5));
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel("Rows: ");
		label.setToolTipText("Sets the number of rows on the board.");
		panel.add(label);
		rowSpinner = new JSpinner(new SpinnerNumberModel(3, 1, Integer.MAX_VALUE, 1));
		rowSpinner.addChangeListener(pl);
		panel.add(rowSpinner);
		add(panel);
		
		add(Box.createVerticalStrut(5));
		
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("Columns: ");
		label.setToolTipText("Sets the number of columns on the board. If 0, as many columns as rows.");
		panel.add(label);
		colSpinner = new JSpinner(new SpinnerNumberModel(1,1,Integer.MAX_VALUE,1));
		colSpinner.addChangeListener(pl);
		panel.add(colSpinner);
		add(panel);
		
		add(Box.createVerticalStrut(5));
		
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("Type of diagonals: ");
		label.setToolTipText("How to handle diagonals between opposite corners.");
		panel.add(label);
		diagBox = new JComboBox<DiagonalsType>(DiagonalsType.values());
		diagBox.setSelectedItem(DiagonalsType.Implied);
		diagBox.addActionListener(pl);
		panel.add(diagBox);
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
		GraphFunction graph = new RectangleOnSquare(new DimConstant((Integer)rowSpinner.getValue()), new DimConstant((Integer)colSpinner.getValue()),(DiagonalsType) diagBox.getSelectedItem(), null);
		board = new Board(graph,null,null,null,null,maker.getSiteType(),maker.largeStack());
	}

	@Override
	public Board board()
	{
		return board;
	}

	@Override
	public ContainerInfo createInfo() {
		RectangleInfo info = new RectangleInfo((int)rowSpinner.getValue(), (int)colSpinner.getValue(),
				(DiagonalsType)diagBox.getSelectedItem());
		return new BoardInfo(maker,info);
	}

}
