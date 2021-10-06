package processing.similarity_matrix;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public interface Drawable
{

	static Drawable generatePlaceholder(final String path)
	{
		System.out.println(path);
		return new Drawable()
		{
			
			private BufferedImage image;

			@Override
			public BufferedImage getBufferedImage()
			{
				if (image == null) {
					try
					{
						image = ImageIO.read(new File(path));
					} catch (final IOException e)
					{
						e.printStackTrace();
					}
				}
				return image;
			}

			@Override
			public void clickAt(final Point p, final MouseEvent me)
			{
				final int x = (int)Math.round(p.getX());
				final int y = (int)Math.round(p.getY());
				if (x<0||y<0||x>=image.getWidth()||y>=image.getHeight())return;
				final int rgb = image.getRGB(x, y);
				System.out.println("clickedat " + x + " " + y + ": " + rgb);
			}

			@Override
			public String getName()
			{
				return "";
			}

			
		};
	}

	BufferedImage getBufferedImage();

	void clickAt(Point p, MouseEvent me);

	default int getWidth() {
		return getBufferedImage().getWidth();
	}
	default int getHeight() {
		return getBufferedImage().getHeight();
	}

	default double getAspectRatio() {
		return (getWidth()*1.0)/getHeight();
	}

	String getName();

}
