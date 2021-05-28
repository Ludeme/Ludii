package view.container.styles.board.puzzle;

import bridge.Bridge;
import game.equipment.container.Container;
import other.context.Context;
import view.container.aspects.designs.board.puzzle.HashiDesign;

public class HashiStyle extends PuzzleStyle
{
	public HashiStyle(final Bridge bridge, final Container container, final Context context) 
	{
		super(bridge, container, context);
		containerDesign = new HashiDesign(this, boardPlacement);
	}

	//-------------------------------------------------------------------------

//	@Override
//	protected void drawVertices(final Graphics2D g2d, final Context context, final double vertexRadius)
//	{
//		final double innerCircleScale = 0.9;
//		for (final Cell v : topology().cells())
//		{
//			if (locationValuesVertex.contains(Integer.valueOf(v.index())))
//			{
//				final int x = (int) (v.centroid().getX() * placement.width + 0.5);
//				final int y = placement.width - (int) (v.centroid().getY() * placement.width + 0.5);
//
//				g2d.setColor(colorOuter);
//				final java.awt.Shape ellipseO = new Ellipse2D.Double(x - vertexRadius, y - vertexRadius, 2 * vertexRadius, 2 * vertexRadius);
//				g2d.fill(ellipseO);
//
//				g2d.setColor(Color.WHITE);
//				final java.awt.Shape ellipseI = new Ellipse2D.Double(x - vertexRadius * innerCircleScale, y - vertexRadius * innerCircleScale, 2 * vertexRadius * innerCircleScale, 2 * vertexRadius * innerCircleScale);
//				g2d.fill(ellipseI);
//			}
//		}
//	}

	//-------------------------------------------------------------------------

//	@Override
//	protected void drawComponents(final Graphics2D g2d, final Context context)
//	{
//		final int pixels = placement.width;
//		
//		final int swThin = Math.max(1, (int) (0.0025 * pixels + 0.5));
//		final int swThick = 25;
//		strokeThin = new BasicStroke(swThin, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
//		GeneralPath path = new GeneralPath();
//
//		// Sizes used for drawing pieces and vertex when edge victory conditions
//		final double cellDistance = cellRadius;
//		final double rO = 0.025 * pixels * cellDistance * swThick;
//		final double rI = 0.022 * pixels * cellDistance * swThick;
//
//		final ContainerState ps = (ContainerState) context.trial().state().containerStates()[0];
//
//		// draw edges
//		for (int edgeIndex = 0; edgeIndex < topology().edges().size(); edgeIndex++)
//		{
//			final Edge e = topology().edges().get(edgeIndex);
//			final int num_edges = ps.numberEdge(edgeIndex);
//			if (locationValuesVertex.contains(Integer.valueOf(e.vA().index())) && locationValuesVertex.contains(Integer.valueOf(e.vB().index())))
//			{
//				g2d.setColor(colorInner);
//				path = new GeneralPath();
//
//				final Point2D.Double va = e.vA().centroid();
//				final Point2D.Double vb = e.vB().centroid();
//
//				double ax = 0;
//				double ay = 0;
//				double bx = 0;
//				double by = 0;
//
//				if (!ps.resolvedEdges(edgeIndex))
//				{
//					// draw nothing
//				}		
//				else if (num_edges == 0)
//				{
//					ax = va.x * pixels;
//					ay = pixels - 1 - va.y * pixels;
//
//					bx = vb.x * pixels;
//					by = pixels - 1 - vb.y * pixels;
//					
//					final double midpointX = (ax + bx) / 2.0;
//					final double midpointY = (ay + by) / 2.0;
//					
//					final Font oldFont = g2d.getFont();
//					final Font font = new Font(oldFont.getFontName(), Font.BOLD, (int) (0.45 * cellRadius * pixels));  // * Global.fontScale));
//					g2d.setFont(font);
//					final Rectangle2D bounds = font.getStringBounds("X", g2d.getFontRenderContext());
//					g2d.drawString("X", (int) (midpointX - bounds.getWidth()/2.0), (int) (midpointY + bounds.getHeight()/3.0));
//					
//				}
//				else
//				{
//					// draw the certain lines (or lack thereof)
//					final double lineSpacing = pixels * cellDistance * 0.3;
//					g2d.setStroke(strokeThin);
//
//					if (Math.abs(va.x - vb.x) < Math.abs(va.y - vb.y))
//					{
//						// extra lines need to be shifted horizontally
//						for (int i = 0; i < num_edges; i++)
//						{
//							ax = va.x * pixels + (i * lineSpacing) - ((num_edges-1)/2.0*lineSpacing) + placement.x;
//							ay = pixels - 1 - va.y * pixels + placement.y;
//
//							bx = vb.x * pixels + (i * lineSpacing) - ((num_edges-1)/2.0*lineSpacing) + placement.x;
//							by = pixels - 1 - vb.y * pixels + placement.y;
//
//							if (ax < bx - 0.001)	// line goes right
//							{
//								ax = ax + rO - (rO-rI)/2;
//								bx = bx - rO + (rO-rI)/2;
//							}
//							if (ax > bx + 0.001)	// line goes left
//							{
//								ax = ax - rO + (rO-rI)/2;
//								bx = bx + rO - (rO-rI)/2;
//							}
//							if (ay < by - 0.001)	// line goes down
//							{
//								ay = ay + rO - (rO-rI)/2;
//								by = by - rO + (rO-rI)/2;
//							}
//							if (ay > by + 0.001)	// line goes up
//							{
//								ay = ay - rO + (rO-rI)/2;
//								by = by + rO - (rO-rI)/2;
//							}
//
//							path.moveTo(ax, ay);
//							path.lineTo(bx, by);
//							g2d.draw(path);
//						}
//					}
//					else
//					{
//						// extra lines need to be shifted vertically
//						for (int i = 0; i < num_edges; i++)
//						{
//							ax = va.x * pixels + placement.x;
//							ay = pixels - 1 - va.y * pixels + (i * lineSpacing) - ((num_edges-1)/2.0*lineSpacing) + placement.y;
//
//							bx = vb.x * pixels + placement.x;
//							by = pixels - 1 - vb.y * pixels + (i * lineSpacing) - ((num_edges-1)/2.0*lineSpacing) + placement.y;
//
//							if (ax < bx - 0.001)	// line goes right
//							{
//								ax = ax + rO - (rO-rI)/2;
//								bx = bx - rO + (rO-rI)/2;
//							}
//							if (ax > bx + 0.001)	// line goes left
//							{
//								ax = ax - rO + (rO-rI)/2;
//								bx = bx + rO - (rO-rI)/2;
//							}
//							if (ay < by - 0.001)	// line goes down
//							{
//								ay = ay + rO - (rO-rI)/2;
//								by = by - rO + (rO-rI)/2;
//							}
//							if (ay > by + 0.001)	// line goes up
//							{
//								ay = ay - rO + (rO-rI)/2;
//								by = by + rO - (rO-rI)/2;
//							}
//
//							path.moveTo(ax, ay);
//							path.lineTo(bx, by);
//							g2d.draw(path);
//						}
//					}
//				}
//			}
//		}
//		drawPuzzleHints(g2d, context);
//		//createImage(context,pixels, cells);
//	}

}
