package app.boardMaker.display.panels.paramPanel.tilings;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;

import app.boardMaker.display.buttons.CancelButton;
import app.boardMaker.display.buttons.CreateButton;
import app.boardMaker.display.panels.polygonView.PolygonView;
import app.boardMaker.display.panels.previewPanel.PreviewListener;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.square.DiagonalsType;
import game.functions.graph.generators.basis.square.Square;
import game.functions.graph.generators.basis.square.SquareShapeType;
import game.util.graph.Poly;

public class SquarePanel extends OptionPanel implements ItemListener
{
	private Maker maker;
	private Displayer displayer;
	private PolygonView pv;
	private PreviewListener pl;
	
	private JPanel cards;
	private JTabbedPane pvContainer;
	
	private JComboBox<SquareShapeType> cBox;
	private JSpinner dimSpinner;
	private JComboBox<DiagonalsType> diagBox;
	private JComboBox<DiagonalsType> diagBoxCustom;
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
		cBox.setSelectedItem(SquareShapeType.Square);
		cBox.addItemListener(this);
		cBox.addActionListener(pl);
		p.add(cBox);
		add(p);
		
		add(Box.createVerticalStrut(5));
		
		cards = new JPanel(new CardLayout());
		makeDimCard();
		makePolyCard();
		add(cards);
		
		add(Box.createVerticalStrut(5));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(new CreateButton(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				createBoard();
				maker.addBoard();
				if (displayer.getPreviewPanel().getTabCount() > 1) {
					displayer.getPreviewPanel().removeTabAt(1);
				}
				displayer.mainView();	
			}
		}));
		buttonPanel.add(new CancelButton(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (displayer.getPreviewPanel().getTabCount() > 1) {
					displayer.getPreviewPanel().removeTabAt(1);
				}
				displayer.mainView();
			}
		}));
		
		add(buttonPanel);
		
		add(Box.createVerticalGlue());
		
		createBoard();
		displayer.getPreviewPanel().setBoard(board);
	}
	
	public void makeDimCard() {
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel("Dimension: ");
		label.setToolTipText("The number of sites par side.");
		p.add(label);
		
		dimSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		dimSpinner.addChangeListener(pl);
		p.add(dimSpinner);
		card.add(p);
		
		card.add(Box.createVerticalStrut(5));
		
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
		card.add(p);
		
		card.add(Box.createVerticalGlue());
		
		cards.add(card,"dim");
	}
	
	public void makePolyCard() {
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel("Type of diagonals: ");
		label.setToolTipText("How to handle diagonals between opposite corners.");
		p.add(label);
		diagBoxCustom = new JComboBox<DiagonalsType>(DiagonalsType.values());
		diagBoxCustom.setSelectedItem(DiagonalsType.Implied);
		diagBoxCustom.addActionListener(pl);
		p.add(diagBoxCustom);
		card.add(p);
		
		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("How to make a polygon ?");
		label.setToolTipText("<html>Polygons are made in the \"Polygon\" tab"
				+ "<br>You can navigate the grid by pressing the middle button (the wheel) of the mouse and drag it."
				+ "<br>Left click on a dot to add it to the polygon."
				+ "<br>Right click on a dot to remove it from the polygon.</html>");
		p.add(label);
		card.add(p);
		
		cards.add(card,"custom");
	}
	
	@Override
	public void createBoard() {
		SquareShapeType shapeType = (SquareShapeType) cBox.getSelectedItem();
		if (!shapeType.equals(SquareShapeType.Custom)) {
			DimConstant dim = new DimConstant((int)dimSpinner.getValue());
			DiagonalsType diagType = (DiagonalsType) diagBox.getSelectedItem();
			GraphFunction graph = Square.construct(shapeType, dim, choice.getSelectedIndex() == 0 ? diagType : null, choice.getSelectedIndex() == 0 ? null : pyramidal);
			board = new Board(graph,null,null,null,null,maker.getSiteType(),maker.largeStack());
		} else {
			Poly poly = pv.makePoly();
			GraphFunction graph = Square.construct(poly, null, (DiagonalsType) diagBox.getSelectedItem());
			board = new Board(graph,null,null,null,null,maker.getSiteType(),maker.largeStack());
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e)
	{
		CardLayout cl = (CardLayout) cards.getLayout();
		if (((SquareShapeType)cBox.getSelectedItem()).equals(SquareShapeType.Custom)) {
			cl.show(cards, "custom");
			/*if (displayer.getPreviewPanel().getTabCount() == 1) {
				pv = new PolygonView(displayer,this);
				displayer.getPreviewPanel().addTab("Polygon", pv);
			}*/
			pvContainer = new JTabbedPane();
			pvContainer.setPreferredSize(new Dimension(displayer.getBoardPanel().getPreferredSize()));
			pv = new PolygonView(displayer,this);
			pvContainer.addTab("Polygon",pv);
			displayer.creationViewPoly();
			displayer.getBoardMakerPane().add(pvContainer, BorderLayout.CENTER);
		} else {
			cl.show(cards,"dim");
			/*if (displayer.getPreviewPanel().getTabCount() > 1) {
				displayer.getPreviewPanel().removeTabAt(1);
				pv = null;
			}*/
			displayer.creationView();
		}
	}

	@Override
	public Board board()
	{
		return board;
	}
}
