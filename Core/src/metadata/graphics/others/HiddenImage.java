package metadata.graphics.others;

import java.util.BitSet;

import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Draws a specified image when a piece is hidden.
 * 
 * @author Matthew.Stephenson
 */
public class HiddenImage implements GraphicsItem
{

	/** Hidden image to draw. */
	private final String image;
		
	//-------------------------------------------------------------------------

	/**
	 * @param image		Name of the hidden Image image to draw.
	 * 
	 * @example (hiddenImage "door")
	 */
	public HiddenImage
	(
		final String image
	)
	{
		this.image = image;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return Hidden image to draw.
	 */
	public String hiddenImage()
	{
		return image;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		return concepts;
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long gameFlags = 0l;
		return gameFlags;
	}

	@Override
	public boolean needRedraw()
	{
		return false;
	}

}
