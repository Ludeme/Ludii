package app.boardMaker.display.panels.paramPanel.tilings;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import app.boardMaker.display.components.buttons.CancelButton;
import app.boardMaker.display.components.buttons.CreateButton;
import app.boardMaker.display.panels.previewPanel.PreviewListener;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.shape.Wedge;

public class WedgePanel extends OptionPanel
{
	private Maker maker;
	private Displayer displayer;
	private PreviewListener pl;
	
	private JSpinner row;
	private JSpinner col;
		
	private Board board;
	
	public WedgePanel(Maker maker) {
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
		label = new JLabel("Rows: ");
		label.setToolTipText("Sets the number of rows on the board.");
		panel.add(label);
		row = new JSpinner(new SpinnerNumberModel(2, 1, Integer.MAX_VALUE, 1));
		row.addChangeListener(pl);
		panel.add(row);
		add(panel);
		
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("Columns: ");
		label.setToolTipText("Sets the number of columns on the board. If 0, as many columns as rows.");
		panel.add(label);
		col = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		col.addChangeListener(pl);
		panel.add(col);
		add(panel);
		
		add(Box.createVerticalGlue());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(new CreateButton(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				createBoard();
				maker.addBoard(displayer.getPreviewPanel().getBoardData());
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
		GraphFunction graph = new Wedge(new DimConstant((int) row.getValue()), ((int)col.getValue() == 0) ? null : new DimConstant((int) col.getValue()));
		board = new Board(graph,null,null,null,null,maker.getSiteType(),maker.largeStack());
	}

	@Override
	public Board board()
	{
		return board;
	}

}
