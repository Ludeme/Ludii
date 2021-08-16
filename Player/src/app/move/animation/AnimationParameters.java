package app.move.animation;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Parameters needed for animating a move.
 * 
 * @author Matthew.Stephenson
 */
public class AnimationParameters 
{
	public final AnimationType animationType;
	public final List<BufferedImage> pieceImages;
	public final List<Point> fromLocations;
	public final List<Point> toLocations;
	public final long animationTimeMs;
	
	public AnimationParameters()
	{
		animationType = AnimationType.NONE;
		pieceImages = new ArrayList<>();
		fromLocations = new ArrayList<>();
		toLocations = new ArrayList<>();
		animationTimeMs = 0;
	}
	
	public AnimationParameters(final AnimationType animationType, final List<BufferedImage> pieceImages, final List<Point> fromLocations, final List<Point> toLocations, final long animationWaitTime)
	{
		this.animationType = animationType;
		this.pieceImages = pieceImages;
		this.fromLocations = fromLocations;
		this.toLocations = toLocations;
		animationTimeMs = animationWaitTime;
	}
}
