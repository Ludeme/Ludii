package view.component.custom;

import bridge.Bridge;
import game.equipment.component.Component;

/**
 * Implementation of card component style. 
 * 
 * @author cambolbro
 */
public class CardStyle extends PieceStyle
{
//	/**
//	 * Temporary storage for base card images when a card deck is loaded.
//	 */
//	private final BaseCardImages baseCardImages = new BaseCardImages();
//	
	//----------------------------------------------------------------------------

	public CardStyle(final Bridge bridge, final Component component) 
	{
		super(bridge, component);
	}

	//----------------------------------------------------------------------------
//	
//	@Override
//	protected SVGGraphics2D getSVGImageFromFilePath(final SVGGraphics2D g2dOriginal, final Context context, final int imageSize, 
//			final String filePath, final int localState, final int value, final int hiddenValue, final int rotation, final boolean secondary)
//	{
//		final Point2D.Double[][] pts =
//		{
//			{	// Joker
//			},
//			{	// Ace
//				new Point2D.Double(0.5, 0.5)
//			},
//			{ 	// 2
//				new Point2D.Double(0.5, 0.225), new Point2D.Double(0.5, 0.775)
//			},
//			{ 	// 3
//				new Point2D.Double(0.5, 0.225), new Point2D.Double(0.5, 0.5),
//				new Point2D.Double(0.5, 0.775)
//			},
//			{ 	// 4
//				new Point2D.Double(0.31, 0.225), new Point2D.Double(0.69, 0.225),
//				new Point2D.Double(0.31, 0.775), new Point2D.Double(0.69, 0.775)
//			},
//			{ 	// 5
//				new Point2D.Double(0.31, 0.225), new Point2D.Double(0.69, 0.225),
//				new Point2D.Double(0.5, 0.5),
//				new Point2D.Double(0.31, 0.775), new Point2D.Double(0.69, 0.775)
//			},
//			{ 	// 6
//				new Point2D.Double(0.31, 0.225), new Point2D.Double(0.69, 0.225),
//				new Point2D.Double(0.31, 0.5), 	 new Point2D.Double(0.69, 0.5),
//				new Point2D.Double(0.31, 0.775), new Point2D.Double(0.69, 0.775)
//			},
//			{ 	// 7
//				new Point2D.Double(0.31, 0.225), new Point2D.Double(0.69, 0.225),
//				new Point2D.Double(0.5, 0.35),
//				new Point2D.Double(0.31, 0.5), 	 new Point2D.Double(0.69, 0.5),
//				new Point2D.Double(0.31, 0.775), new Point2D.Double(0.69, 0.775)
//			},
//			{ 	// 8
//				new Point2D.Double(0.31, 0.225), new Point2D.Double(0.69, 0.225),
//				new Point2D.Double(0.5, 0.35),
//				new Point2D.Double(0.31, 0.5),   new Point2D.Double(0.69, 0.5),
//				new Point2D.Double(0.5, 0.65),
//				new Point2D.Double(0.31, 0.775), new Point2D.Double(0.69, 0.775)
//			},
//			{ 	// 9
//				new Point2D.Double(0.31, 0.225), new Point2D.Double(0.69, 0.225),
//				new Point2D.Double(0.31, 0.41),  new Point2D.Double(0.69, 0.41),
//				new Point2D.Double(0.5, 0.5),
//				new Point2D.Double(0.31, 0.59),  new Point2D.Double(0.69, 0.59),
//				new Point2D.Double(0.31, 0.775), new Point2D.Double(0.69, 0.775)
//			},
//			{ 	// 10
//				new Point2D.Double(0.31, 0.225), new Point2D.Double(0.69, 0.225),
//				new Point2D.Double(0.5, 0.31),
//				new Point2D.Double(0.31, 0.41),  new Point2D.Double(0.69, 0.41),
//				new Point2D.Double(0.31, 0.59),  new Point2D.Double(0.69, 0.59),
//				new Point2D.Double(0.5, 0.69),
//				new Point2D.Double(0.31, 0.775), new Point2D.Double(0.69, 0.775)
//			},
//			{ 	// Jack
//				new Point2D.Double(0.81, 0.135)
//			},
//			{ 	// Queen
//				new Point2D.Double(0.81, 0.135)
//			},
//			{ 	// King
//				new Point2D.Double(0.81, 0.135)
//			},
//		};
//
//		final int cardSize = imageSize;
//
//		if (!baseCardImages.areLoaded())
//			baseCardImages.loadImages(cardSize);
//
//		final int ht = cardSize;
//		final int wd = (int)(55 / 85.0 * ht + 0.5) / 2 * 2;
//
//		// Load the relevant large suit image from file
//		final String suitSVGLarge = baseCardImages.getPath(BaseCardImages.SUIT_LARGE, getCardSuitValue(context));
//
//		final Rectangle2D suitRectLarge = SVGtoImage.getBounds(suitSVGLarge, baseCardImages.getSuitSizeBig());
//		final int lx = (int) suitRectLarge.getWidth();
//		final int ly = (int) suitRectLarge.getHeight();
//
//		// Create the small suit image from the large one
//		final String suitSVGSmall = baseCardImages.getPath(BaseCardImages.SUIT_SMALL, getCardSuitValue(context));
//
//		final Rectangle2D suitRectSmall = SVGtoImage.getBounds(suitSVGSmall, baseCardImages.getSuitSizeSmall());
//		final int sy = (int) suitRectSmall.getHeight();  //suitSizeSmall;
//		final int sx = (int)(sy * lx / (double)ly + 0.5);
//
//		// Assemble the card image
//		final SVGGraphics2D g2d = new SVGGraphics2D(cardSize, cardSize);
//		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//
//		Color cardFillColour = Color.white;
//		if (hiddenValue == 1)
//			cardFillColour = new Color(180,0,0);
//
//		// Draw and fill card outline
//		final int round = (int)(0.1 * cardSize + 0.5);
//
//		g2d.setColor(cardFillColour);
//		g2d.fillRoundRect(cardSize/2-wd/2, 1, wd, ht-2, round, round);
//
//		g2d.setStroke(new BasicStroke());
//		g2d.setColor(Color.black);
//		g2d.drawRoundRect(cardSize/2-wd/2, 1, wd, ht-2, round, round);
//
//		// don't draw the images on the card if its masked
//		if (hiddenValue == 0)
//		{
//			// Margin offset from the edges
//			final int off = (int)(0.03 * cardSize + 0.5);
//	
//			// Draw the left margin label
//			final int fontSize = (int)(0.11 * cardSize + 0.5);
//			final Font cardFont = new Font("Arial", Font.BOLD, fontSize);
//			g2d.setFont(cardFont);
//	
//			final String label = component.cardType().label();
//			final Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(label, g2d);
//	
//			final int tx = cardSize / 2 - wd / 2 + 2 * off - (int)(bounds.getWidth()/2 + 0.5);
//			final int ty = 5 * off;
//	
//			final Color color = isBlack(context) ? Color.BLACK : Color.RED;
//			g2d.setColor(color);
//			g2d.drawString(label, tx, ty);
//	
//			// Draw the left margin suit image (small)
////			SVGtoImage.loadFromString
////			(
////				g2d, suitSVGSmall, baseCardImages.getSuitSizeSmall(cardSize), 
////				baseCardImages.getSuitSizeSmall(cardSize), 
////				cardSize/2-wd/2+2*off-sx/2, 6*off, color, null, false, 
////				0, false, false
////			);
//	
//			final int number = component.cardType().number();  //ordinal();
//	
//			if (component.cardType().isRoyal())  //id > 10)
//			{
//				// Is a royal card
//				final int ry = (int)(0.6 * cardSize + 0.5);
//				
//				final String royalSVG =
//						isBlack(context)
//								? baseCardImages.getPath(BaseCardImages.BLACK_ROYAL, number)
//								:
//						baseCardImages.getPath(BaseCardImages.RED_ROYAL, number);
//	
//				// Draw the royal image
//				if (royalSVG != null)
//				{
////					SVGtoImage.loadFromString
////					(
////						g2d, royalSVG, ry, ry, cardSize/2-ry/2, cardSize/2-ry/2, color, null, true, 
////						0, false, false
////					);
//				}
//			}
//			
//			// Draw the interior suit images (large)
//			if (number < pts.length)
//			{
//				for (int n = 0; n < pts[number].length; n++)
//				{
//					final Point2D.Double pt = pts[number][n];
//					final int x = cardSize / 2 - wd / 2 + (int)(pt.x * wd + 0.5);
//					final int y = (int)(pt.y * ht + 0.5);
////					SVGtoImage.loadFromString
////					(
////						g2d, suitSVGLarge, baseCardImages.getSuitSizeBig(cardSize), 
////						baseCardImages.getSuitSizeBig(cardSize), x-lx/2, y-ly/2, color, null, false, 
////						0, false, false
////					);
//				}
//			}
//		}
//		
//		return g2d;
//	}
//
//	//----------------------------------------------------------------------------
//	
//	/**
//	 * Returns True if this card should be black.
//	 */
//	private boolean isBlack(final Context context)
//	{
//		if (SuitType.values()[getCardSuitValue(context)-1] == SuitType.Diamonds || SuitType.values()[getCardSuitValue(context)-1] == SuitType.Hearts)
//			return false;
//		return true;
//	}
//	
//	//----------------------------------------------------------------------------
//	
//	/**
//	 * Returns the value of this card's suit (considering the metadata ranking).
//	 */
//	private int getCardSuitValue(final Context context)
//	{
//		if (!component.isCard())
//			return Constants.UNDEFINED;
//		
//		final Card card = (Card)component;
//		if (context.game().metadata().graphics().suitRanking() == null)
//			return card.suit();
//		
//		final int initValue = card.suit();
//		int cardValue = 0;
//		for (final SuitType suit : context.game().metadata().graphics().suitRanking())
//		{
//			cardValue++;
//			if (suit == SuitType.Clubs && initValue == SuitType.Clubs.value)
//				return cardValue;
//			if (suit == SuitType.Spades && initValue == SuitType.Spades.value)
//				return cardValue;
//			if (suit == SuitType.Diamonds && initValue == SuitType.Diamonds.value)
//				return cardValue;
//			if (suit == SuitType.Hearts && initValue == SuitType.Hearts.value)
//				return cardValue;
//		}
//		return card.suit();
//	}
	
	//----------------------------------------------------------------------------

}
