package processing.similarity_matrix;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class DrawPanel extends JPanel
{

	private static final double zoomSlowFactor = 0.5;
	private static final double zoomFactorOut = 0.25;
	private static final double zoomFactorIn = 0.2; //zoomfactor in and out work that zooming in and out leads to the original picture
	private final Drawable drawable;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	Point2D scalePointTopLeft = new Point2D.Double(0.0, 0.0);
	Point2D scalePointBottomRight = new Point2D.Double(0.0, 0.0);
	private final ArrayList<DrawPanel> sharedScalingPanels = new ArrayList<>();

	@Override
	public void paint(final Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		final BufferedImage image = drawable.getBufferedImage();
		g.drawImage(image,(int)scalePointTopLeft.getX(),(int)scalePointTopLeft.getY(),
				(int)scalePointBottomRight.getX(),(int)scalePointBottomRight.getY()
				,0,0,image.getWidth(),image.getHeight(),null);
		
		printScaler();
	}
	
	private void printScaler()
	{
		System.out.println("scalerImage: " + (int)scalePointTopLeft.getX() + " " + (int)scalePointTopLeft.getY() + " "+ 
				(int)scalePointBottomRight.getX() + " " + (int)scalePointBottomRight.getY());
		
	}

	@Override
	public String getName() {
		return drawable.getName();
	}
	

	public DrawPanel(final Drawable d)
	{
		drawable = d;
		scalePointTopLeft = new Point2D.Double(0.0, 0.0);
		scalePointBottomRight = new Point2D.Double(800, 600);
		//imageLabel.setIcon(new ImageIcon(d.getBufferedImage()));
		//this.add(imageLabel);
		final MouseAdapter ma = createMouseAdapter(d);
		addMouseListener(ma);
		addMouseWheelListener(ma);
		addMouseMotionListener(ma);
		this.setSize(800, 600);
		setPreferredSize(new Dimension(800, 600));
		resetScaling();
	}

	private MouseAdapter createMouseAdapter(final Drawable d)
	{
		final DrawPanel selfReference = this;
		return new MouseAdapter()
		{	
			
			Point previous;
			
			@Override
			public void mouseReleased(final MouseEvent e)
			{
				System.out.println("released");
			}
			
			@Override
			public void mousePressed(final MouseEvent e)
			{
				if (SwingUtilities.isRightMouseButton(e)) {
					previous = e.getPoint();
				}
				if (SwingUtilities.isMiddleMouseButton(e)) {
					resetScaling();
					
				}
				System.out.println("pressed");
			}
			
			@Override
			public void mouseClicked(final MouseEvent e)
			{
				System.out.println("clicked");
				
				
				final Point p = selfReference.convertScreenToImage(e.getPoint());
				d.clickAt(p, e);
			}
			
			@Override
			public void mouseDragged(final MouseEvent e)
			{
				if (SwingUtilities.isRightMouseButton(e)) {
					drag(previous,e.getPoint(),e.isAltDown());
					previous = e.getPoint();
				}
				System.out.println("drag");
				final Point p = selfReference.convertScreenToImage(e.getPoint());
				d.clickAt(p, e);
			}
			@Override
			public void mouseWheelMoved(final MouseWheelEvent e)
			{
				System.out.println("mousewheel");
				selfReference.changeScaling(e);
				
			}
		};
	}


	

	protected void drag(final Point previous, final Point current, final boolean altDown)
	{
		double dx = current.getX()-previous.getX();
		double dy = current.getY()-previous.getY();
		System.out.println("drag :" + dx + " " + dy);
		if (altDown) {
			dx*=0.5;
			dy*=0.5;
		}
		scalePointTopLeft = new Point2D.Double(scalePointBottomRight.getX() + dx, scalePointBottomRight.getY() + dy);
		scalePointBottomRight = new Point2D.Double(scalePointTopLeft.getX() + dx, scalePointTopLeft.getY() + dy);		
		updateSharedPanels();
		repaint();
	}

	protected void changeScaling(final MouseWheelEvent e)
	{
		final Point panelPoint = e.getPoint();
		final double dx = panelPoint .getX()-scalePointTopLeft.getX(); 
		final double dy = panelPoint.getY()-scalePointTopLeft.getY();
		
		final double dsx = scalePointBottomRight.getX()-scalePointTopLeft.getX();
		final double currentZoom = dsx / drawable.getBufferedImage().getWidth();
		System.out.println("zoom:" + currentZoom);
		final int wheelRotation = e.getWheelRotation();
		double zoomFactor = zoomFactorIn;
		if (wheelRotation<0) zoomFactor = zoomFactorOut;
		if (e.isAltDown())zoomFactor*=zoomSlowFactor;
		final double zx = getScaleWidht()*zoomFactor;
		final double zy = getScaleHeight()*zoomFactor;
		
		final double rtoL = dx / getScaleWidht();
		final double rtoT = dy / getScaleHeight();
		
		if (wheelRotation<0) {
			scalePointTopLeft = new Point2D.Double(scalePointBottomRight.getX() + dx-zx*rtoL, scalePointBottomRight.getY() + -zy*rtoT);
			scalePointBottomRight = new Point2D.Double(scalePointTopLeft.getX() + zx*(1-rtoL), scalePointTopLeft.getY() + zy*(1-rtoT));	
		}
			
		if (wheelRotation>0) {
			scalePointTopLeft = new Point2D.Double(scalePointBottomRight.getX() + zx*rtoL, scalePointBottomRight.getY() + -zy*rtoT);
			scalePointBottomRight = new Point2D.Double(scalePointTopLeft.getX() + -zx*(1-rtoL), scalePointTopLeft.getY() + -zy*(1-rtoT));	
		}
		updateSharedPanels();
			repaint();
	}

	

	private double getScaleWidht()
	{
		return scalePointBottomRight.getX()-scalePointTopLeft.getX();
	}
	private double getScaleHeight()
	{
		return scalePointBottomRight.getY()-scalePointTopLeft.getY();
	}


	protected void resetScaling()
	{
		scalePointTopLeft = new Point2D.Double(0.0, 0.0);
		//this.scalePointBottomRight = new Point2D(drawable.getBufferedImage().getWidth(), drawable.getBufferedImage().getHeight());
		final double ar = drawable.getAspectRatio();
		if (getWidth()/ar>getHeight()) {
			scalePointBottomRight = new Point2D.Double(getHeight()*ar, getHeight());
			
		}else {
			scalePointBottomRight = new Point2D.Double(getWidth(), getWidth()/ar);
		}
		updateSharedPanels();
		repaint();
	}

	private void updateSharedPanels()
	{
		for (final DrawPanel drawPanel : sharedScalingPanels)
		{
			drawPanel.setScaling(scalePointTopLeft,scalePointBottomRight);
		}
		
	}

	private void setScaling(
			final Point2D scalePointTopLeftNew, final Point2D scalePointBottomRightNew
	)
	{
		scalePointTopLeft = scalePointTopLeftNew;
		scalePointBottomRight = scalePointBottomRightNew;
		
	}

	protected Point convertScreenToImage(final Point panelPoint)
	{
		final double dx = panelPoint.getX()-scalePointTopLeft.getX(); 
		final double dy = panelPoint.getY()-scalePointTopLeft.getY();
		final double rx = dx / (scalePointBottomRight.getX() - scalePointTopLeft.getX());
		final double ry = dy / (scalePointBottomRight.getY() - scalePointTopLeft.getY());
		final int width = drawable.getWidth();
		final int height = drawable.getHeight();
		System.out.println("% rx:" + rx + " ry:" + ry);
		return new Point((int)(rx*width),(int)(ry*height));
		
	}

	public void addSharedScaling(final DrawPanel otherDrawPanel)
	{
		sharedScalingPanels.add(otherDrawPanel);
	}

}
