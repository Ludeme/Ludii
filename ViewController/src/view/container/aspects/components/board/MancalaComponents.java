package view.container.aspects.components.board;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import bridge.Bridge;
import game.equipment.component.Component;
import game.equipment.container.Container;
import game.equipment.container.board.Board;
import game.equipment.container.board.custom.MancalaBoard;
import game.equipment.other.Map;
import game.types.board.SiteType;
import game.types.board.StoreType;
import graphics.ImageProcessing;
import main.math.MathRoutines;
import metadata.graphics.Graphics;
import other.concept.Concept;
import other.context.Context;
import other.state.State;
import other.state.container.ContainerState;
import other.topology.Topology;
import view.container.aspects.components.ContainerComponents;
import view.container.styles.BoardStyle;
import view.container.styles.board.MancalaStyle;

/**
 * Mancala components properties.
 * 
 * @author Matthew.Stephenson and cambolbro
 */
public class MancalaComponents extends ContainerComponents
{
	private final BoardStyle boardStyle;
	
	//-------------------------------------------------------------------------
	
	public MancalaComponents(final Bridge bridge, final MancalaStyle containerStyle)
	{
		super(bridge, containerStyle);
		boardStyle = containerStyle;
	}
	
	//-------------------------------------------------------------------------

	/** Colour of the seeds. */
	private final Color seedColour = new Color(255, 255, 230);

	/** Piece placements. */
	private final Point2D.Double[][] offsets =
	{
		{},
		{
			// 1 seed
			new Point2D.Double(0, 0),
		},
		{
			// 2 seeds
			new Point2D.Double(-1, 0),
			new Point2D.Double( 1, 0),
		},
		{
			// 3 seeds
			new Point2D.Double(-1.0, -0.8),
			new Point2D.Double( 1.0, -0.8),
			new Point2D.Double( 0.0,  1.0),
		},
		{
			// 4 seed
			new Point2D.Double(-1.0, -1.0),
			new Point2D.Double( 1.0, -1.0),
			new Point2D.Double(-1.0,  1.0),
			new Point2D.Double( 1.0,  1.0),
		},
		{
			// 5 seeds
			new Point2D.Double(-1.0, -1.0),
			new Point2D.Double( 1.0, -1.0),
			new Point2D.Double(-1.0,  1.0),
			new Point2D.Double( 1.0,  1.0),
			new Point2D.Double( 0.0,  0.0),
		},
	};
	
	//-------------------------------------------------------------------------

	@Override
	public void drawComponents(final Graphics2D g2d, final Context context)
	{		
		final Rectangle placement = boardStyle.placement();
		final int cellRadiusPixels = boardStyle.cellRadiusPixels();

		final boolean circleTiling = context.game().booleanConcepts().get(Concept.CircleTiling.id());
		
		// Set the seed size
		final boolean withStore = (context.board() instanceof MancalaBoard)
				? !((MancalaBoard) context.board()).storeType().equals(StoreType.None)
				: true;
		final int indexHoleBL = (withStore) ? 1 : 0;
		final Point ptA = boardStyle.screenPosn(boardStyle.topology().vertices().get(circleTiling ? 0 : indexHoleBL).centroid());
		final Point ptB = boardStyle.screenPosn(
				boardStyle.topology().vertices().get(circleTiling ? 1 : (indexHoleBL + 1)).centroid());
		
		final double unit = MathRoutines.distance(ptA, ptB); 
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		final Board board = context.board();
		final Topology graph = board.topology();
		final Graphics graphics = context.metadata().graphics();
		
		final String label = "Board";
		final Container equip = context.game().mapContainer().get(label);

		final Color shadeBase  = seedColour;
		final Color shadeDark  = MathRoutines.shade(shadeBase, 0.75);
		final Color shadeLight = MathRoutines.shade(shadeBase, 1.5);

		if (equip == null)
			return;

		// This game has a board
		final State state = context.state();
		final ContainerState cs = state.containerStates()[0];

		for (int site = 0; site < graph.vertices().size(); site++)
		{
			final Point pt = boardStyle.screenPosn(graph.vertices().get(site).centroid());
			final int count = cs.count(site, SiteType.Vertex);
			
			final int cx = pt.x;
			final int cy = pt.y;

			final int swRing = (int) (boardStyle.cellRadius() * placement.width / 10.0);
			final BasicStroke strokeRink = new BasicStroke(swRing, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
			g2d.setStroke(strokeRink);

			// Code for drawing the tuz (ring around holes that are coloured based on player value).
			if (context.game().metadata().graphics().showPlayerHoles())
			{
				for (int i = 1; i <= context.game().players().count(); i++)
				{
					if (state.getValue(i) == site)
					{
						final int r = cellRadiusPixels;
						g2d.setColor(bridge.settingsColour().playerColour(context, i));
						g2d.drawArc(cx-r, cy-r, 2*r, 2*r, 0, 360);
					}
				}
			}
			
			// Code for drawing the tuz (ring around holes that are coloured based on local state).
			if (context.game().metadata().graphics().holesUseLocalState())
			{
				for (int i = 1; i <= context.game().players().count(); i++)
				{
					if (i == cs.stateVertex(site))
					{
						final int r = cellRadiusPixels;
						g2d.setColor(bridge.settingsColour().playerColour(context, i));
						g2d.drawArc(cx-r, cy-r, 2*r, 2*r, 0, 360);
					}
				}
			}

			// Code for drawing rings around each kalah based on which player they are for.
			if (context.game().metadata().graphics().showPits())
			{
				if (context.game().equipment().maps().length != 0)
				{
					final Map map = context.game().equipment().maps()[0];
					for (int p = 1; p <= context.game().players().count(); p++)
					{
						final int ownedSite = map.to(p);
	
						if (ownedSite == site)
						{
							final int r = cellRadiusPixels;
							g2d.setColor(bridge.settingsColour().playerColour(context, p));
							g2d.drawArc(cx-r, cy-r, 2*r, 2*r, 0, 360);
						}
					}
				}
			}

			if (count > 0)
			{
				final int what = cs.what(site, SiteType.Vertex);
				final int who = cs.who(site, SiteType.Vertex);

				final Component component = (what > 1) ? context.components()[what] : null;
				final int scale = (component == null) ? 1 : (int) graphics.pieceScale(who, component.name(), context).getX();
				final int seedRadius = Math.max(1*scale, (int) (0.19 * unit* scale)) ;

				// Draw pieces
				final int group = Math.min(count, offsets.length-1);
				for (int s = 0; s < offsets[group].length; s++)
				{
					final Point2D.Double off = offsets[group][s];

					final int x = cx + (int) (off.x * seedRadius + 0.5) - seedRadius + 1;
					final int y = cy - (int) (off.y * seedRadius + 0.5) - seedRadius + 1;

					ImageProcessing.ballImage(g2d, x, y, (seedRadius), new Color(255, 255, 230));
				}

				if (count > 5)
				{
					// Draw piece count
					final Font oldFont = g2d.getFont();
					final Font font = new Font(oldFont.getFontName(), Font.BOLD, (int) (0.45 * boardStyle.cellRadius() * placement.width));
					g2d.setFont(font);

					final String str = Integer.toString(count);
					final Rectangle2D bounds = font.getStringBounds(str, g2d.getFontRenderContext());

					final int tx = cx - (int)(0.5 * bounds.getWidth() + 0.5);
					final int ty = cy + (int)(0.4 * bounds.getHeight() + 0.5);

					g2d.setColor(Color.black);
					g2d.drawString(str, tx, ty-1);

					g2d.setColor(shadeLight);
					g2d.drawString(str, tx, ty+1);

					g2d.setColor(shadeDark);
					g2d.drawString(str, tx, ty);

					g2d.setFont(oldFont);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
}
