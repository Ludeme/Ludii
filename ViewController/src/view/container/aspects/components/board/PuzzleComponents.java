package view.container.aspects.components.board;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.BitSet;

import bridge.Bridge;
import game.equipment.component.Piece;
import game.rules.start.StartRule;
import game.rules.start.deductionPuzzle.Set;
import game.types.play.RoleType;
import gnu.trove.list.array.TIntArrayList;
import metadata.graphics.Graphics;
import other.context.Context;
import other.state.State;
import other.state.container.ContainerState;
import other.topology.TopologyElement;
import util.ImageInfo;
import view.component.BaseComponentStyle;
import view.component.custom.PieceStyle;
import view.container.aspects.components.ContainerComponents;
import view.container.aspects.designs.board.puzzle.PuzzleDesign;
import view.container.styles.board.puzzle.PuzzleStyle;

/**
 * Puzzle components properties.
 * 
 * @author Matthew.Stephenson
 */
public class PuzzleComponents extends ContainerComponents
{
	
	/** Initial cells where values are set. */
	protected TIntArrayList initialValues = new TIntArrayList();
	
	//-------------------------------------------------------------------------
	
	protected final PuzzleStyle puzzleStyle;
	private final PuzzleDesign puzzleDesign;
	
	//-------------------------------------------------------------------------
	
	public PuzzleComponents(final Bridge bridge, final PuzzleStyle containerStyle, final PuzzleDesign containerDesign)
	{
		super(bridge, containerStyle);
		puzzleStyle = containerStyle;
		puzzleDesign = containerDesign;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void drawComponents(final Graphics2D g2d, final Context context)
	{			
		// As the game is a deduction puzzle, we need to draw the variables rather than the components.
		if (initialValues.size() == 0 && context.game().rules().start() != null)
		{
			final StartRule[] startRules = context.game().rules().start().rules();
			for (final StartRule startRule : startRules)
			{
				if (startRule.isSet())
				{
					final Set setRule = (Set) startRule;
					for (final Integer site : setRule.vars())
					{
						initialValues.add(site.intValue());
					}
				}
			}
		}

		// This game has a board
		final State state = context.state();
		final ContainerState cs = state.containerStates()[0];

		for (int site = 0; site < puzzleStyle.topology().getGraphElements(context.board().defaultSite()).size(); site++)
		{
			final TopologyElement element = puzzleStyle.topology().getGraphElements(context.board().defaultSite()).get(site);
			final Point2D posn = element.centroid();
			final Point drawPosn = puzzleStyle.screenPosn(posn);

			final BitSet values = cs.values(context.board().defaultSite(), site);

			if (cs.isResolved(site, context.board().defaultSite()))
			{				
				final int value = values.nextSetBit(0);
				
				final int dim = puzzleStyle.topology().rows(context.board().defaultSite()).size();
				final int bigFontSize = (int)(0.75 * puzzleStyle.placement().getHeight() / dim + 0.5);
				final Font bigFont = new Font("Arial", Font.BOLD,  bigFontSize);
				
				g2d.setFont(bigFont);
				if (initialValues.contains(site))
					g2d.setColor(Color.BLACK);
				else
					g2d.setColor(new Color(139,0,0));
				
				final int pieceSize = (int) (puzzleStyle.cellRadiusPixels()*2*pieceScale()*puzzleStyle.containerScale());
				drawPuzzleValue(value, site, context, g2d, drawPosn, pieceSize);
			}
		}
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void drawPuzzleValue(final int value, final int site, final Context context, final Graphics2D g2d, final Point drawPosn, final int imageSize) 
	{		
		final Graphics metadataGraphics = context.game().metadata().graphics();
		final String name = metadataGraphics.pieceNameReplacement(1, String.valueOf(value), context, 0, 0);
	 	if (name != null)
	 	{
	 		// Draw a specific image here instead of the value
			final Piece component = new Piece(name, RoleType.P1, null, null, null, null, null, null);
			component.create(context.game());
			component.setIndex(value);
			final BaseComponentStyle componentStyle = new PieceStyle(bridge, component);
			componentStyle.renderImageSVG(context, imageSize, 0, 0, false, 0, 0);
			bridge.graphicsRenderer().drawSVG(context, g2d, componentStyle.getImageSVG(0), new ImageInfo(drawPosn, site, 0, context.board().defaultSite(), component, 0, 0, 0.0, 0, 1, imageSize, 1));
	 	}
	 	else
	 	{
	 		// Draw the single resolved value
			final String str = "" + value;
			final Rectangle bounds = g2d.getFontMetrics().getStringBounds(str, g2d).getBounds();
			g2d.drawString(str, drawPosn.x - bounds.width/2, drawPosn.y + bounds.height/3);
	 	}
	}

	public PuzzleDesign getPuzzleDesign()
	{
		return puzzleDesign;
	}
	
	//-------------------------------------------------------------------------

}
