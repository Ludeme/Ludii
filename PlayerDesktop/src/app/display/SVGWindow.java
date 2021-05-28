package app.display;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import main.Constants;

//-----------------------------------------------------------------------------

/**
 * SVG viewer.
 * 
 * @author cambolbro
 */
public class SVGWindow extends JPanel
{
	private static final long serialVersionUID = 1L;

	private final BufferedImage[] images = new BufferedImage[Constants.MAX_PLAYERS+1];
	
	//-------------------------------------------------------------------------

	public void setImages(final BufferedImage img1, final BufferedImage img2)
	{
		images[1] = img1;
		images[2] = img2;
	}
	
	//-------------------------------------------------------------------------

	@Override 
	public void paint(final Graphics g)
	{
		g.setColor(Color.white);
		g.fillRect(0,  0,  getWidth(),  getHeight());
		
		if (images[1] != null && images[2] != null)
		{
			g.drawImage(images[1],  10,  10, null);
			g.drawImage(images[2],  10 + images[1].getWidth() + 10,  10, null);
		}
	}

	//-------------------------------------------------------------------------

}
