package view.container.aspects.components.board;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.List;

import bridge.Bridge;
import game.equipment.container.Container;
import other.context.Context;
import other.state.State;
import other.state.container.ContainerState;
import other.topology.Cell;
import view.container.aspects.components.ContainerComponents;
import view.container.aspects.placement.Board.Connect4Placement;
import view.container.styles.board.Connect4Style;

/**
 * Connect4 components properties.
 * 
 * @author Matthew.Stephenson
 */
public class Connect4Components extends ContainerComponents
{
	private final Connect4Style boardStyle;
	private final Connect4Placement boardPlacement;
	
	//-------------------------------------------------------------------------
	
	public Connect4Components(final Bridge bridge, final Connect4Style containerStyle, final Connect4Placement containerPlacement)
	{
		super(bridge, containerStyle);
		boardStyle = containerStyle;
		boardPlacement = containerPlacement;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void drawComponents(final Graphics2D g2d, final Context context) 
	{
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		final List<Cell> cells = boardStyle.topology().cells();
		final Rectangle placement = boardStyle.placement();
		final String label = "Board";
		final Container equip = context.game().mapContainer().get(label);

		if (cells.isEmpty())
		{
			System.out.println("** Connect4Style.drawStyle(): Board has no cells.");
			return;
		}
	
		final int u = (int) ((cells.get(1).centroid().getX() - cells.get(0).centroid().getX()) * placement.width);
		final int r = (int) (0.425 * u + 0.5);

		if (equip != null)
		{
			// This game has a board
			final State state = context.state();
			final ContainerState cs = state.containerStates()[0];

			for (int site = 0; site < boardStyle.topology().cells().size(); site++)
			{
				final Point2D pixel = cells.get(site).centroid();
				final int levelNumber = cs.sizeStackCell(site);

				for (int level = 0; level < levelNumber; level++)
				{
					final int who = cs.whoCell(site, level);
					if (who == 0)
						break;

					// Draw this piece
					final int cx = (int) (pixel.getX() * placement.width);
					final int cy = (int) (pixel.getY() * placement.width + (boardPlacement.connect4Rows() - 1) * u - level * u
							+ placement.y);

					g2d.setColor(bridge.settingsColour().playerColour(context, who));
					g2d.fillArc(cx - r, cy - r, 2 * r, 2 * r, 0, 360);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
}
