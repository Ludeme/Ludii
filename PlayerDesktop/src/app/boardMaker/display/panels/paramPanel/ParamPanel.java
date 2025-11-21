package app.boardMaker.display.panels.paramPanel;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;

import app.boardMaker.handlers.Displayer;

/**
 * A class containing the parameters when creating boards
 */

public class ParamPanel extends JPanel
{
	private Displayer displayer;
	
	private boolean visible;
	
	private JTabbedPane tab;
	private JScrollPane scroll;
	private JPanel tiling;
	
	public ParamPanel(Displayer displayer) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.displayer = displayer;
		
		displayer.setParamPanel(this);
		
		tiling = new JPanel();
		tiling.add(new JLabel("Placeholder"));
		
		scroll = new JScrollPane(tiling,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		tab = new JTabbedPane();
		tab.addTab("Board parameters", scroll);
		
		add(tab);
	}
	
	public void setPanel(JPanel p) {
		tiling = p;
		scroll.setViewportView(p);

		revalidate();
		repaint();
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	public boolean visible() {
		return visible;
	}
}
