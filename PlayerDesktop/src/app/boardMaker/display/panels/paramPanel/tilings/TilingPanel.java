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

import app.boardMaker.display.components.buttons.CancelButton;
import app.boardMaker.display.components.buttons.CreateButton;
import app.boardMaker.display.panels.previewPanel.PreviewListener;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.tiling.Tiling;
import game.functions.graph.generators.basis.tiling.TilingType;

public class TilingPanel extends OptionPanel {
	private Maker maker;
	private Displayer displayer;
	private PreviewListener pl;

	private JComboBox<TilingType> tBox;
	private JSpinner pSpinner;
	private JSpinner sSpinner;
	
	private Board board;
	
	public TilingPanel(Maker maker) {
		super();
		this.maker = maker;
		this.displayer = maker.getDisplayer();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(displayer.getParamPanel().getWidth(), displayer.getParamPanel().getHeight()));

		pl = new PreviewListener(this,displayer.getPreviewPanel());
		
		add(Box.createVerticalStrut(5));
		
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel("Tiling: ");
		label.setToolTipText("Sets the type of tiling.");
		p.add(label);
		tBox = new JComboBox<TilingType>(TilingType.values());
		tBox.addActionListener(pl);
		p.add(tBox);
		add(p);
		
		add(Box.createVerticalStrut(5));

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("First dimension");
		label.setToolTipText("Primary dimension of the board.");
		p.add(label);

		pSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		pSpinner.addChangeListener(pl);
		p.add(pSpinner);
		add(p);

		add(Box.createVerticalStrut(5));

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("Second dimension");
		label.setToolTipText("Secondary dimension of the board. Length of sides will alternate between primary and secondary dimensions.");
		p.add(label);

		sSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		sSpinner.addChangeListener(pl);
		p.add(sSpinner);
		add(p);
		
		add(Box.createVerticalStrut(5));
		
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
		
		add(Box.createVerticalGlue());
		
		createBoard();
		displayer.getPreviewPanel().setBoard(board);
	}

	@Override
	public void createBoard()
	{
		TilingType type = (TilingType) tBox.getSelectedItem();
		DimConstant dimA = new DimConstant((int)pSpinner.getValue());
		DimConstant dimB = new DimConstant((int)sSpinner.getValue());
		GraphFunction graph = Tiling.construct(type, dimA, (dimB.eval() == 0) ? null : dimB);
		board = new Board(graph,null,null,null,null,maker.getSiteType(),maker.largeStack());
	}

	@Override
	public Board board()
	{
		return board;
	}

}
