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
import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.celtic.Celtic;
import game.functions.graph.generators.basis.tri.TriShapeType;
import game.util.graph.Poly;

public class CelticPanel extends OptionPanel implements ItemListener
{
	private Displayer displayer;
	
	private JPanel cards;
	private JComboBox<String> shapeBox;
	private JSpinner rowSpinner;
	private JSpinner colSpinner;
	
	private PolygonView pv;
	private PreviewListener pl;
	
	private Board board;
	
	public CelticPanel(Displayer displayer) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));	
		setPreferredSize(new Dimension(displayer.getParamPanel().getWidth(), displayer.getParamPanel().getHeight()));
		
		this.displayer = displayer;
		pl = new PreviewListener(this, displayer.getPreviewPanel());
		
		add(Box.createVerticalStrut(5));
		
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel((String)displayer.getStrings().get("paramShape"));
		label.setToolTipText((String) displayer.getStrings().get("paramShapeCustomTT"));
		p.add(label);
		
		String[] shapeItems = new String[] {"Rectangle", "Custom"};
		shapeBox = new JComboBox<String>(shapeItems);
		shapeBox.addActionListener(pl);
		shapeBox.addItemListener(this);
		p.add(label);
		p.add(shapeBox);
		add(p);
		
		add(Box.createVerticalStrut(5));
		
		cards = new JPanel(new CardLayout());
		makeDimCard();
		makePolyCard();
		add(cards);
		
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
		JLabel label = new JLabel((String) displayer.getStrings().get("row"));
		label.setToolTipText((String) displayer.getStrings().get("rowTT"));
		p.add(label);
		
		rowSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		rowSpinner.addChangeListener(pl);
		p.add(rowSpinner);
		card.add(p);
		
		card.add(Box.createVerticalStrut(5));
		
		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel((String) displayer.getStrings().get("col"));
		label.setToolTipText((String) displayer.getStrings().get("colTT"));
		p.add(label);
		
		colSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		colSpinner.addChangeListener(pl);
		p.add(colSpinner);
		card.add(p);
		
		card.add(Box.createVerticalGlue());
		
		cards.add(card,"dim");
	}

	@Override
	public void createBoard()
	{
		if (((String)shapeBox.getSelectedItem()).equals("Rectangle")) {
			DimConstant dimA = new DimConstant((int)rowSpinner.getValue());
			DimConstant dimB = new DimConstant((int)colSpinner.getValue());
			GraphFunction graph = new Celtic(dimA, (dimB.eval() == 0) ? null : dimB);
			board = new Board(graph, null, null, null, null, null, null);
		} else if (((String)shapeBox.getSelectedItem()).equals("Custom")) {
			// Need this check to avoid an error when creating the celtic graph
			if (pv.getPoly().size() > 2) {
				Poly poly = pv.makePoly();
				GraphFunction graph = new Celtic(poly, null);
				board = new Board(graph, null, null, null, null, null, null);
			} else {
				board = null;
			}
		}
	}

	@Override
	public Board board()
	{
		return board;
	}

	@Override
	public void itemStateChanged(ItemEvent e)
	{
		CardLayout cl = (CardLayout) cards.getLayout();
		if (((String)shapeBox.getSelectedItem()).equals("Custom")) {
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
