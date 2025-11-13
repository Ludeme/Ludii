package app.boardMaker.display.panels.paramPanel.tilings;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;

import app.boardMaker.dataStruct.board.BoardInfo;
import app.boardMaker.dataStruct.board.ContainerInfo;
import app.boardMaker.dataStruct.board.graphFunction.generator.SquareInfo;
import app.boardMaker.display.components.buttons.CancelButton;
import app.boardMaker.display.components.buttons.CreateButton;
import app.boardMaker.display.panels.previewPanel.PreviewListener;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.square.DiagonalsType;
import game.functions.graph.generators.basis.square.Square;
import game.functions.graph.generators.basis.square.SquareShapeType;
import game.types.board.SiteType;

public class SquarePanel extends OptionPanel {
	private Maker maker;
	private Displayer displayer;
	private PreviewListener pl;

	private JComboBox<SquareShapeType> cBox;
	private JSpinner dimSpinner;
	private JComboBox<DiagonalsType> diagBox;
	private JComboBox<String> choice;
	
	private boolean pyramidal = false;
	
	private Board board;
	
	public SquarePanel(Maker maker) {
		super();
		this.maker = maker;
		this.displayer = maker.getDisplayer();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(displayer.getParamPanel().getWidth(), displayer.getParamPanel().getHeight()));

		pl = new PreviewListener(this,displayer.getPreviewPanel());
		
		add(Box.createVerticalStrut(5));
		
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel("Board shape: ");
		label.setToolTipText("This parameter will set the board shape, choosing the CUSTOM option will allow you to drow your own shape.");
		p.add(label);
		
		cBox = new JComboBox<SquareShapeType>(SquareShapeType.values());
		cBox.removeItem(SquareShapeType.NoShape);
		cBox.removeItem(SquareShapeType.Rectangle);
		cBox.setSelectedItem(SquareShapeType.Square);
		cBox.addActionListener(pl);
		p.add(cBox);
		add(p);
		
		add(Box.createVerticalStrut(5));

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("Dimension: ");
		label.setToolTipText("The number of sites par side.");
		p.add(label);

		dimSpinner = maker.getSiteType() == SiteType.Cell ? new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1))
						: new JSpinner(new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1));
		dimSpinner.addChangeListener(pl);
		p.add(dimSpinner);
		add(p);

		add(Box.createVerticalStrut(5));

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel choicePanel = new JPanel(new CardLayout());
		choice = new JComboBox<String>(new String[] {"Diagonal type: ", "Pyramide stack: "});
		choice.addActionListener(pl);
		choice.addItemListener(new ItemListener()
		{

			@Override
			public void itemStateChanged(ItemEvent e)
			{
				CardLayout cl = (CardLayout) choicePanel.getLayout();
				if (((String)choice.getSelectedItem()).equals("Diagonal type: ")) {
					cl.show(choicePanel, "diag");
				} else {
					cl.show(choicePanel, "pyram");
				}
			}
		});
		p.add(choice);

		JPanel diagPanel = new JPanel();
		diagBox = new JComboBox<DiagonalsType>(DiagonalsType.values());
		diagBox.setSelectedItem(DiagonalsType.Implied);
		diagBox.addActionListener(pl);
		diagPanel.add(diagBox);
		choicePanel.add(diagPanel,"diag");

		JPanel pyramPanel = new JPanel();
		ButtonGroup group = new ButtonGroup();
		JRadioButton button = new JRadioButton("Enabled");
		button.addActionListener(pl);
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				pyramidal = true;
			}
		});
		group.add(button);
		pyramPanel.add(button);
		button = new JRadioButton("Disabled");
		button.setSelected(true);
		button.addActionListener(pl);
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				pyramidal = false;
			}
		});
		group.add(button);
		pyramPanel.add(button);
		choicePanel.add(pyramPanel,"pyram");

		p.add(choicePanel);
		add(p);
		
		add(Box.createVerticalStrut(5));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(new CreateButton(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				maker.setBoard(createInfo());
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
		
		add(Box.createVerticalGlue());

		displayer.getPreviewPanel().setBoard(createInfo());
	}
	
	@Override
	public void createBoard() {
		SquareShapeType shapeType = (SquareShapeType) cBox.getSelectedItem();
		DimConstant dim = new DimConstant((int)dimSpinner.getValue());
		DiagonalsType diagType = (DiagonalsType) diagBox.getSelectedItem();
		GraphFunction graph = Square.construct(shapeType, dim, choice.getSelectedIndex() == 0 ? diagType : null, choice.getSelectedIndex() == 0 ? null : pyramidal);
		board = new Board(graph,null,null,null,null,maker.getSiteType(),maker.largeStack());
	}

	@Override
	public Board board()
	{
		return board;
	}

	@Override
	public ContainerInfo createInfo() {
		SquareInfo info;
		if (choice.getSelectedItem().equals("Diagonal type: ")) {
			info = new SquareInfo((SquareShapeType)cBox.getSelectedItem(),(int)dimSpinner.getValue(),
					(DiagonalsType)diagBox.getSelectedItem(),null);
		} else {
			info = new SquareInfo((SquareShapeType)cBox.getSelectedItem(),(int)dimSpinner.getValue(),
					null,pyramidal);
		}
		return new BoardInfo(maker,info);
	}
}
