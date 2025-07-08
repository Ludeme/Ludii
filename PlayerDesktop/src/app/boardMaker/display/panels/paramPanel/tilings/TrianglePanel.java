package app.boardMaker.display.panels.paramPanel.tilings;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import app.boardMaker.display.buttons.CancelButton;
import app.boardMaker.display.buttons.CreateButton;
import app.boardMaker.display.panels.polygonView.PolygonView;
import app.boardMaker.display.panels.previewPanel.PreviewListener;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.square.SquareShapeType;
import game.functions.graph.generators.basis.tri.Tri;
import game.functions.graph.generators.basis.tri.TriShapeType;
import game.util.graph.Poly;

public class TrianglePanel extends OptionPanel implements ItemListener
{
	private Displayer displayer;
	private PolygonView pv;
	private PreviewListener pl;
	
	private JPanel cards;
	
	private JComboBox<TriShapeType> triBox;
	private JSpinner primSpinner;
	private JSpinner secSpinner;
	
	private GraphFunction board;
	
	public TrianglePanel(Maker maker)
	{
		super();
		this.displayer = maker.getDisplayer();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(displayer.getParamPanel().getWidth(), displayer.getParamPanel().getHeight()));

		maker.setMancala(false);
		maker.setSurakarta(false);

		pl = new PreviewListener(this,displayer.getPreviewPanel());
		
		add(Box.createVerticalStrut(5));
		
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel("Board shape: ");
		label.setToolTipText("This parameter will set the board shape, choosing the CUSTOM option will allow you to drow your own shape.");
		p.add(label);
		
		triBox = new JComboBox<TriShapeType>(TriShapeType.values());
		triBox.removeItem(TriShapeType.NoShape);
		triBox.setSelectedItem(TriShapeType.Triangle);
		triBox.addActionListener(pl);
		triBox.addItemListener(this);
		p.add(triBox);
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
		
		add(Box.createVerticalGlue());
		
		createBoard();
		displayer.getPreviewPanel().setBoard(board);
	}

	private void makePolyCard()
	{
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel("How to make a polygon ?");
		label.setToolTipText("<html>Polygons are made in the \"Polygon\" tab"
				+ "<br>You can navigate the grid by pressing the middle button (the wheel) of the mouse and drag it."
				+ "<br>Left click on a dot to add it to the polygon."
				+ "<br>Right click on a dot to remove it from the polygon.</html>");
		p.add(label);
		card.add(p);
		
		cards.add(card,"custom");
		
	}

	private void makeDimCard()
	{
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel("First dimension");
		label.setToolTipText("Primary dimension of the board.");
		p.add(label);
		
		primSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		primSpinner.addChangeListener(pl);
		p.add(primSpinner);
		card.add(p);
		
		card.add(Box.createVerticalStrut(5));
		
		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("Second dimension");
		label.setToolTipText("Secondary dimension of the board. Length of sides will alternate between primary and secondary dimensions.");
		p.add(label);
		
		secSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		secSpinner.addChangeListener(pl);
		p.add(secSpinner);
		card.add(p);
		
		card.add(Box.createVerticalGlue());
		
		cards.add(card,"dim");
	}

	@Override
	public void createBoard()
	{
		TriShapeType shape = (TriShapeType) triBox.getSelectedItem();
		if (!shape.equals(TriShapeType.Custom)) {
			DimConstant dimA = new DimConstant((int) primSpinner.getValue());
			DimConstant dimB = new DimConstant((int) secSpinner.getValue());
			board = Tri.construct(shape, dimA, (dimB.eval() == 0) ? null : dimB);
		} else {
			Poly poly = pv.makePoly();
			board = Tri.construct(poly, null);
		}
	}

	@Override
	public GraphFunction board()
	{
		return board;
	}

	@Override
	public void itemStateChanged(ItemEvent e)
	{
		CardLayout cl = (CardLayout) cards.getLayout();
		if (((TriShapeType)triBox.getSelectedItem()).equals(TriShapeType.Custom)) {
			cl.show(cards, "custom");
			if (displayer.getPreviewPanel().getTabCount() == 1) {
				pv = new PolygonView(displayer,this);
				displayer.getPreviewPanel().addTab("Polygon", pv);
			}
		} else {
			cl.show(cards,"dim");
			if (displayer.getPreviewPanel().getTabCount() > 1) {
				displayer.getPreviewPanel().removeTabAt(1);
				pv = null;
			}
		}
	}
}
