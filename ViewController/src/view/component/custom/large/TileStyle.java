package view.component.custom.large;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.equipment.component.Component;
import game.equipment.component.tile.Path;
import game.functions.dim.DimConstant;
import game.functions.graph.generators.shape.Regular;
import game.types.board.SiteType;
import game.util.graph.Face;
import game.util.graph.Graph;
import other.context.Context;
import util.HiddenUtil;
import view.component.custom.PieceStyle;

/**
 * Style for drawing pieces that fill the cells they are on.
 * Can also include paths on the piece between edges, if specified.
 * 
 * @author Matthew.Stephenson
 */
public class TileStyle extends PieceStyle
{
	public TileStyle(final Bridge bridge, final Component component) 
	{
		super(bridge, component);
	}

	//-------------------------------------------------------------------------
	
	@Override
	protected SVGGraphics2D getSVGImageFromFilePath(final SVGGraphics2D g2dOriginal, final Context context, final int imageSize, 
			final String filePath, final int containerIndex, final int localState, final int value, final int hiddenValue, final int rotation, final boolean secondary)
	{		
		SVGGraphics2D g2d = new SVGGraphics2D(imageSize, imageSize);
		
		// Rotate graphics object if needed
		g2d.rotate(Math.toRadians(rotation), imageSize/2, imageSize/2);
		
		if (HiddenUtil.intToBitSet(hiddenValue).get(HiddenUtil.hiddenIndex))
			return new SVGGraphics2D(imageSize, imageSize);
		
		final int numEdges = component.numSides();
		
		// Secondary image for a tile also includes an outline.
		if(secondary)
		{
			final Graph tileGraph = new Regular(null, new DimConstant(numEdges)).eval(context, SiteType.Cell);
			tileGraph.normalise();
			final Face tileGraphFace = tileGraph.faces().get(0);
			
			final GeneralPath path = new GeneralPath();
			for (int i = 0; i < tileGraphFace.vertices().size(); i++)
			{
				final game.util.graph.Vertex v = tileGraphFace.vertices().get(i);
				if (i == 0)
					path.moveTo(v.pt().x() * imageSize, v.pt().y() * imageSize);
				else
					path.lineTo(v.pt().x() * imageSize, v.pt().y() * imageSize);
			}
			path.closePath();
			
			//final Color fillColour = context.game().metadata().graphics().pieceColour(context, component.owner(), component.name(), localState, value, PieceColourType.Fill);
			if (fillColour != null)
				g2d.setColor(fillColour);
			else if (context.game().players().count() >= component.owner())
				g2d.setColor(bridge.settingsColour().playerColour(context, component.owner()));
			else
				g2d.setColor(Color.BLACK);
			g2d.fill(path);
			
			//final Color pieceEdgeColour = context.game().metadata().graphics().pieceColour(context, component.owner(), component.name(), localState, value, PieceColourType.Edge);
		 	if (edgeColour != null)
		 	{
		 		final java.awt.Shape oldClip = g2d.getClip();
		 		g2d.setStroke(new BasicStroke(imageSize/10 + 1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		 		g2d.setColor(edgeColour);
		 		g2d.setClip(path);
		  		g2d.draw(path);
		  		g2d.setClip(oldClip);
		 	}
		}

		if (!HiddenUtil.intToBitSet(hiddenValue).get(HiddenUtil.hiddenWhatIndex))
		{
			// Add in any foreground specified in metadata
			g2d = getForeground(g2d, context, containerIndex, localState, value, imageSize);
			
			// Draw on the terminus lines
			int[] terminus = component.terminus();
			if (component.numTerminus().intValue() > 0)
			{
				terminus = new int[numEdges];
				Arrays.fill(terminus, component.numTerminus().intValue());
				g2d.setColor(Color.RED); // red colour by default
				final double lineThicknessMultiplier = 0.33;
				final int lineThickness = (int)(imageSize * lineThicknessMultiplier);
				g2d.setStroke(new BasicStroke(lineThickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
				drawPathLines(g2d, context, terminus, imageSize, numEdges);
			}
		}
		else
		{
			// If the what of the tile is hidden, draw a question mark.
			final Font valueFont = new Font("Arial", Font.BOLD, (imageSize));
			g2d.setColor(Color.BLACK);
			g2d.setFont(valueFont);
			final Rectangle2D rect = valueFont.getStringBounds("?", g2d.getFontRenderContext());
			g2d.drawString("?", (int)(g2d.getWidth()/2 - rect.getWidth()/2) , (int)(g2d.getHeight()/2 + rect.getHeight()/3));
		}
		
		return g2d;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Draws the path lines on the tile based on the terminus specs.
	 * TODO needs to be done in a more generic manner.
	 */
	private void drawPathLines(final SVGGraphics2D g2d, final Context context, final int[] terminus, final int imageSize, final int numEdges) 
	{
		int terminusSpacing = (imageSize) / (terminus[0] + 1);

		if (numEdges == 4) // TODO only square tile paths supported right now
		{
			if (component.paths() != null)
			{
				terminusSpacing = (imageSize) / (component.numTerminus().intValue() + 1);
				for (final Path tilePath : component.paths())
				{
					int ax = 0;
					int ay = 0;
					int bx = 0;
					int by = 0;

					switch (tilePath.side1().intValue())
					{
					case 0:
						ax = terminusSpacing * (tilePath.terminus1().intValue() + 1);
						ay = 0;
						break;
					case 1:
						ax = imageSize;
						ay = terminusSpacing * (tilePath.terminus1().intValue() + 1);
						break;
					case 2:
						ax = terminusSpacing * (tilePath.terminus1().intValue() + 1);
						ay = imageSize;
						break;
					case 3:
						ax = 0;
						ay = terminusSpacing * (tilePath.terminus1().intValue() + 1);
						break;
					}

					switch (tilePath.side2().intValue())
					{
					case 0:
						bx = terminusSpacing * (tilePath.terminus2().intValue() + 1);
						by = 0;
						break;
					case 1:
						bx = imageSize;
						by = terminusSpacing * (tilePath.terminus2().intValue() + 1);
						break;
					case 2:
						bx = terminusSpacing * (tilePath.terminus2().intValue() + 1);
						by = imageSize;
						break;
					case 3:
						bx = 0;
						by = terminusSpacing * (tilePath.terminus2().intValue() + 1);
						break;
					}

					final double off = 0.666;

					final int aax = ax + (int) (off * (imageSize / 2 - ax));
					final int aay = ay + (int) (off * (imageSize / 2 - ay));

					final int bbx = bx + (int) (off * (imageSize / 2 - bx));
					final int bby = by + (int) (off * (imageSize / 2 - by));

					final GeneralPath path2 = new GeneralPath();
					path2.moveTo(ax, ay);
					path2.curveTo(aax, aay, bbx, bby, bx, by);

					g2d.setColor(bridge.settingsColour().playerColour(context, tilePath.colour().intValue()));
					g2d.draw(path2);
				}
			}
		}
		else
		{
			// Only square done for now
		}
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double scale(final Context context, final int containerIndex, final int localState, final int value) 
	{
		return 1.0;
	}

}
