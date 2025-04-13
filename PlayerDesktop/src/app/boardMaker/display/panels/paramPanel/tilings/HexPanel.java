package app.boardMaker.display.panels.paramPanel.tilings;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

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
import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.hex.Hex;
import game.functions.graph.generators.basis.hex.HexShapeType;
import game.functions.graph.generators.basis.tri.TriShapeType;
import game.util.graph.Poly;

public class HexPanel extends OptionPanel implements ItemListener
{
	private Displayer displayer;
	private PolygonView pv;
	private PreviewListener pl;
	
	private JPanel cards;
	
	private JComboBox<HexShapeType> shapeBox;
	private JSpinner primSpinner;
	private JSpinner secSpinner;

	private Board board;
	
	public HexPanel(Displayer displayer) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));	
		setPreferredSize(new Dimension(displayer.getParamPanel().getWidth(), displayer.getParamPanel().getHeight()));
		
		this.displayer = displayer;
		pl = new PreviewListener(this,displayer.getPreviewPanel());
		
		add(Box.createVerticalStrut(5));
		
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel((String)displayer.getStrings().get("paramShape"));
		label.setToolTipText((String) displayer.getStrings().get("paramShapeCustomTT"));
		p.add(label);
		shapeBox = new JComboBox<HexShapeType>(HexShapeType.values());
		shapeBox.removeItem(HexShapeType.NoShape);
		shapeBox.setSelectedItem(HexShapeType.Hexagon);
		shapeBox.addActionListener(pl);
		shapeBox.addItemListener(this);
		p.add(shapeBox);
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
		JLabel label = new JLabel((String)displayer.getStrings().get("htuPoly"));
		label.setToolTipText((String)displayer.getStrings().get("polyExplain"));
		p.add(label);
		card.add(p);
		
		cards.add(card,"custom");
	}

	private void makeDimCard()
	{
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel((String) displayer.getStrings().get("1dim"));
		label.setToolTipText((String) displayer.getStrings().get("1dimTT"));
		p.add(label);
		
		primSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		primSpinner.addChangeListener(pl);
		p.add(primSpinner);
		card.add(p);
		
		card.add(Box.createVerticalStrut(5));
		
		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel((String) displayer.getStrings().get("2dim"));
		label.setToolTipText((String) displayer.getStrings().get("2dimTT"));
		p.add(label);
		
		secSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		secSpinner.addChangeListener(pl);
		p.add(secSpinner);
		card.add(p);
		
		card.add(Box.createVerticalGlue());
		
		cards.add(card,"dim");
	}

	@Override
	public void itemStateChanged(ItemEvent e)
	{
		CardLayout cl = (CardLayout) cards.getLayout();
		if (((HexShapeType)shapeBox.getSelectedItem()).equals(HexShapeType.Custom)) {
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

	@Override
	public void createBoard()
	{
		HexShapeType shape = (HexShapeType) shapeBox.getSelectedItem();
		if (!shape.equals(HexShapeType.Custom)) {
			DimConstant dimA = new DimConstant((int)primSpinner.getValue());
			DimConstant dimB = new DimConstant((int)secSpinner.getValue());
			GraphFunction graph = Hex.construct(shape, dimA, (dimB.eval() == 0) ? null : dimB);
			board = new Board(graph, null, null, null, null, null, null);
		} else {
			Poly poly = pv.makePoly();
			GraphFunction graph = Hex.construct(poly, null);
			board = new Board(graph, null, null, null, null, null, null);
		}
	}

	@Override
	public Board board()
	{
		return board;
	}

}
