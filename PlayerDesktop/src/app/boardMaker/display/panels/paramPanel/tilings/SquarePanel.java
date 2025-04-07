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
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.LookAndFeel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.plaf.OptionPaneUI;

import app.boardMaker.display.buttons.CancelButton;
import app.boardMaker.display.buttons.CreateButton;
import app.boardMaker.display.panels.polygonView.PolygonView;
import app.boardMaker.handlers.Displayer;
import game.functions.graph.generators.basis.square.DiagonalsType;
import game.functions.graph.generators.basis.square.SquareShapeType;

public class SquarePanel extends JPanel implements ItemListener
{
	private Displayer displayer;
	
	private JPanel cards;
	
	private JComboBox<SquareShapeType> cBox;
	private JSpinner dimSpinner;
	private JComboBox<DiagonalsType> diagBox;
	private JComboBox<DiagonalsType> diagBoxCustom;
	private JComboBox<String> choice;
	
	private boolean pyramidal = false;
	
	public SquarePanel(Displayer displayer) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(displayer.getParamPanel().getWidth(), displayer.getParamPanel().getHeight()));
		
		this.displayer = displayer;
		
		add(Box.createVerticalStrut(5));
		
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel((String)displayer.getStrings().get("paramShape"));
		label.setToolTipText((String) displayer.getStrings().get("paramShapeCustomTT"));
		p.add(label);
		
		cBox = new JComboBox<SquareShapeType>(SquareShapeType.values());
		cBox.removeItem(SquareShapeType.NoShape);
		cBox.setSelectedItem(SquareShapeType.Square);
		cBox.addItemListener(this);
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
	}
	
	public void makeDimCard() {
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel((String) displayer.getStrings().get("dimension"));
		label.setToolTipText((String) displayer.getStrings().get("dimensionTT"));
		p.add(label);
		
		dimSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		p.add(dimSpinner);
		card.add(p);
		
		card.add(Box.createVerticalStrut(5));
		
		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel choicePanel = new JPanel(new CardLayout());
		choice = new JComboBox<String>(new String[] {"Diagonal type: ", "Pyramide stack: "});
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
		diagPanel.add(diagBox);
		choicePanel.add(diagPanel,"diag");
		
		JPanel pyramPanel = new JPanel();
		ButtonGroup group = new ButtonGroup();
		JRadioButton button = new JRadioButton((String)displayer.getStrings().get("on"));
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
		button = new JRadioButton((String)displayer.getStrings().get("off"));
		button.setSelected(true);
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
		label.setToolTipText((String)displayer.getStrings().get("diagTypeTT"));
		p.add(label);
		diagBoxCustom = new JComboBox<DiagonalsType>(DiagonalsType.values());
		diagBoxCustom.setSelectedItem(DiagonalsType.Implied);
		p.add(diagBoxCustom);
		card.add(p);
		
		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel((String)displayer.getStrings().get("htuPoly"));
		label.setToolTipText((String)displayer.getStrings().get("polyExplain"));
		p.add(label);
		card.add(p);
		
		cards.add(card,"custom");
	}

	@Override
	public void itemStateChanged(ItemEvent e)
	{
		CardLayout cl = (CardLayout) cards.getLayout();
		if (((SquareShapeType)cBox.getSelectedItem()).equals(SquareShapeType.Custom)) {
			cl.show(cards, "custom");
			if (displayer.getPreviewPanel().getTabCount() == 1) {
				displayer.getPreviewPanel().addTab("Polygon", new PolygonView(displayer));
			}
		} else {
			cl.show(cards,"dim");
			if (displayer.getPreviewPanel().getTabCount() > 1) {
				displayer.getPreviewPanel().removeTabAt(1);
			}
		}
	}
}
