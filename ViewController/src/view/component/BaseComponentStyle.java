package view.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.equipment.component.Component;
import graphics.ImageConstants;
import graphics.ImageUtil;
import graphics.svg.SVGtoImage;
import metadata.graphics.Graphics;
import metadata.graphics.util.MetadataImageInfo;
import metadata.graphics.util.PieceColourType;
import metadata.graphics.util.ValueDisplayInfo;
import metadata.graphics.util.colour.ColourRoutines;
import other.context.Context;
import util.HiddenUtil;
import util.StringUtil;

/**
 * Base style for drawing components.
 * 
 * @author Matthew.Stephenson
 */
public abstract class BaseComponentStyle implements ComponentStyle
{
	protected Bridge bridge;
	
	protected Component component;
	protected String svgName;
	
	/** Component SVG image. */
	private final ArrayList<SVGGraphics2D> imageSVG = new ArrayList<>();
	
	/** Piece scale. */
	protected double scaleX = 1.0;	// Used as the default scale for cases where a piece cannot be scaled in one direction, e.g. for Ball components.
	protected double scaleY = 1.0;
	protected double maxBackgroundScale = 1.0;
	protected double maxForegroundScale = 1.0;

	/** Fill colour. */
	protected Color fillColour;
	
	/** Edge colour. */
	protected Color edgeColour = Color.BLACK;
	
	/** Secondary colour. E.g. for displaying numbers on pieces. */
	protected Color secondaryColour;
	
	/** If the piece image should be rotated. */
	protected int metadataRotation = 0;
	
	/** If the value or local state of the piece should be shown on it. */
	protected ValueDisplayInfo showValue = new ValueDisplayInfo();
	protected ValueDisplayInfo showLocalState = new ValueDisplayInfo();
	
	// Force all visuals to be drawn as strings (used for N puzzles)
	protected boolean drawStringVisuals = false;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns an SVG image from a given file path.
	 */
	protected abstract SVGGraphics2D getSVGImageFromFilePath(SVGGraphics2D g2d, Context context, int imageSize, String sVGPath, int containerIndex, int localState, int value, int hiddenValue, final int rotation, final boolean secondary);
	
	//-------------------------------------------------------------------------
	
	public BaseComponentStyle(final Bridge bridge, final Component component)
	{
		this.component = component;
		this.bridge = bridge;
	}

	//----------------------------------------------------------------------------

	@Override
	public void renderImageSVG(final Context context, final int containerIndex, final int imageSize, final int localState, final int value, final boolean secondary, final int hiddenValue, final int rotation)
	{
		edgeColour = new Color(0, 0, 0);
		fillColour = null;

		final int g2dSize = (int) (imageSize * scale(context, containerIndex, localState, value));
		SVGGraphics2D g2d = new SVGGraphics2D(g2dSize, g2dSize);
		
		final BitSet hiddenBitset = HiddenUtil.intToBitSet(hiddenValue);
		g2d = hiddenCheck(context, hiddenBitset, g2d);
		
		final int imageState = hiddenStateCheck(context, hiddenBitset, localState);
		final int imageValue = hiddenValueCheck(context, hiddenBitset, value);
		
		String SVGNameLocal = component.getNameWithoutNumber();
		SVGNameLocal = genericMetadataChecks(context, containerIndex, imageState, imageValue);
		String SVGPath = ImageUtil.getImageFullPath(SVGNameLocal);
		
		if (drawStringVisuals)
			SVGPath = null;
		
		SVGPath = hiddenWhatCheck(context, hiddenBitset, SVGPath);
		hiddenWhoCheck(context, hiddenBitset);
		
		final int maxRotation = 360 / context.currentInstanceContext().game().maximalRotationStates();
		final int degreesRotation = rotation * maxRotation + metadataRotation;
		
		while (imageSVG.size() <= localState)
			imageSVG.add(null);

		imageSVG.set(localState, getSVGImageFromFilePath(g2d, context, imageSize, SVGPath, containerIndex, imageState, imageValue, hiddenValue, degreesRotation, secondary));
	}

	//-------------------------------------------------------------------------
	
	private static SVGGraphics2D hiddenCheck(final Context context, final BitSet hiddenBitset, final SVGGraphics2D g2d)
	{
		if (hiddenBitset.get(HiddenUtil.hiddenIndex))
			return new SVGGraphics2D(0, 0);
		
		return g2d;
	}

	private static int hiddenValueCheck(final Context context, final BitSet hiddenBitset, final int value)
	{
		if (hiddenBitset.get(HiddenUtil.hiddenValueIndex))
			return -1;
		return value;
	}

	private static int hiddenStateCheck(final Context context, final BitSet hiddenBitset, final int localState)
	{
		if (hiddenBitset.get(HiddenUtil.hiddenStateIndex))
			return -1;
		return localState;
	}

	private void hiddenWhoCheck(final Context context, final BitSet hiddenBitset) 
	{
		if (hiddenBitset.get(HiddenUtil.hiddenWhoIndex))
		{
			fillColour = Color.GRAY;
			edgeColour = Color.GRAY;
		}
	}

	private String hiddenWhatCheck(final Context context, final BitSet hiddenBitset, final String sVGPath) 
	{
		String filePath = sVGPath;
		if (hiddenBitset.get(HiddenUtil.hiddenWhatIndex))
		{
		 	final String hiddenImageName = context.game().metadata().graphics().pieceHiddenImage();
		 	if (hiddenImageName != null)
		 	{
		 		filePath = ImageUtil.getImageFullPath(hiddenImageName);
		 		svgName = hiddenImageName;
		 	}
		 	else
		 	{
		 		filePath = null;
		 		svgName = "?";
		 	}
		}
		return filePath;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Performs all graphics metadata checks that apply to all piece types.
	 */
	public String genericMetadataChecks(final Context context, final int containerIndex, final int localState, final int value)
	{
		svgName = component.getNameWithoutNumber();

		final Graphics metadataGraphics = context.game().metadata().graphics();
		
		final Point2D.Float scale = metadataGraphics.pieceScale(context, component.owner(), component.name(), containerIndex, localState, value);
		scaleX = scale.getX();
		scaleY = scale.getY();
	
		// Check the .lud metadata for piece name extension
	 	final String nameExtension = metadataGraphics.pieceNameExtension(context, component.owner(), component.name(), containerIndex, localState, value);
	 	if (nameExtension != null)
	 		svgName = svgName + nameExtension;
	 	
	 	final String nameReplacement = metadataGraphics.pieceNameReplacement(context, component.owner(), component.name(), containerIndex, localState, value);
	 	if (nameReplacement != null)
	 		svgName = nameReplacement;
	
	 	final boolean addLocalStateToName = metadataGraphics.addStateToName(context, component.owner(), component.name(), containerIndex, localState, value);
	 	if (addLocalStateToName)
	 		svgName = svgName + localState;
	
	 	// Check the .lud metadata for piece colour
	 	final Color pieceColour = metadataGraphics.pieceColour(context, component.owner(), component.name(), containerIndex, localState, value, PieceColourType.Fill);
	 	if (pieceColour != null)
	  		fillColour = pieceColour;
	 	
	 	final Color pieceEdgeColour = metadataGraphics.pieceColour(context, component.owner(), component.name(), containerIndex, localState, value, PieceColourType.Edge);
	 	if (pieceEdgeColour != null)
	  		edgeColour = pieceEdgeColour;
	 	
 	 	final Color pieceSecondaryColour = metadataGraphics.pieceColour(context, component.owner(), component.name(), containerIndex, localState, value, PieceColourType.Secondary);
 	 	if (pieceSecondaryColour != null)
 	 		secondaryColour = pieceSecondaryColour;
	
	 	metadataRotation = metadataGraphics.pieceRotate(context, component.owner(), component.name(), containerIndex, localState, value);
	 	
	 	showValue = metadataGraphics.displayPieceValue(context, component.owner(), component.name());
	 	showLocalState = metadataGraphics.displayPieceState(context, component.owner(), component.name());
	 	
	 	if (component.isDie())
	 		showLocalState = new ValueDisplayInfo();
	 	
	 	if 
	 	(
	 		!component.isDie()
 			&&
 			(
 				bridge.settingsVC().pieceFamily(context.game().name()).equals(ImageConstants.abstractFamilyKeyword)
	 			||
	 			(
	 				bridge.settingsVC().pieceFamily(context.game().name()).equals("") 
 					&& 
 					metadataGraphics.pieceFamilies() != null
 					&& 
 					Arrays.asList(metadataGraphics.pieceFamilies()).contains(ImageConstants.abstractFamilyKeyword)
	 			)
	 		)
 		)
	 	{
	 		svgName = ImageConstants.customImageKeywords[0];
	 	}
	 	
	 	bridge.settingsVC().setPieceStyleExtension(bridge.settingsVC().pieceFamily(context.game().name()));
	 	
	 	if (bridge.settingsVC().pieceStyleExtension().equals(""))
	 		if (metadataGraphics.pieceFamilies() != null)
	 			bridge.settingsVC().setPieceStyleExtension(metadataGraphics.pieceFamilies()[0]);
	 	
	 	if (!Arrays.asList(ImageConstants.defaultFamilyKeywords).contains(bridge.settingsVC().pieceStyleExtension()) && !bridge.settingsVC().pieceStyleExtension().equals(ImageConstants.abstractFamilyKeyword) && !bridge.settingsVC().pieceStyleExtension().equals(""))
	 		svgName = svgName + "_" + bridge.settingsVC().pieceStyleExtension();
	
	 	if (fillColour == null)
			fillColour = bridge.settingsColour().playerColour(context, component.owner());
	
//		if (svgName.length() == 1)
//	 	{
//			edgeColour = fillColour;
//			fillColour = null;
//	 	}
		
		if (secondaryColour == null)
			secondaryColour = ColourRoutines.getContrastColorFavourDark(fillColour);

		return svgName;
	}

	//----------------------------------------------------------------------------
	
	/**
	 * Returns an SVG image for the component style background.
	 */
	protected SVGGraphics2D getBackground(final SVGGraphics2D g2d, final Context context, final int containerIndex, final int localState, final int value, final int dim)
	{
		final Graphics metadataGraphics = context.game().metadata().graphics();
		
		for (final MetadataImageInfo backgroundImageInfo : metadataGraphics.pieceBackground(context, component.owner(), component.name(), containerIndex, localState, value))
		{
 	 		if (backgroundImageInfo.path() != null)
 	 		{
	 	 		final String backgroundPath = ImageUtil.getImageFullPath(backgroundImageInfo.path());
	 	 		final double backgroundScaleX = backgroundImageInfo.scaleX();
	 		 	final double backgroundScaleY = backgroundImageInfo.scaleY();
	 		 	maxForegroundScale = Math.max(Math.max(backgroundScaleX, backgroundScaleY), maxBackgroundScale);
	 		 	Color backgroundColour = backgroundImageInfo.mainColour();
	 		 	Color backgroundEdgeColour = backgroundImageInfo.secondaryColour();
	 		 	final int rotation = backgroundImageInfo.rotation();
	 		 	final double offsetX = backgroundImageInfo.offestX();
	 		 	final double offsetY = backgroundImageInfo.offestY();
 		 	
	 		 	if (backgroundColour == null)
	 		 		backgroundColour = bridge.settingsColour().playerColour(context, component.owner());
	 		 	if (backgroundEdgeColour == null)
	 		 		backgroundEdgeColour = Color.BLACK;
	 		 	
	 		 	final int tileSizeX = (int) (dim * backgroundScaleX);
	 		 	final int tileOffsetX = (dim-tileSizeX)/2;
	 		 	final int tileSizeY = (int) (dim * backgroundScaleY);
	 		 	final int tileOffsetY = (dim-tileSizeY)/2;
	 			SVGtoImage.loadFromFilePath
	 			(
	 				g2d, backgroundPath, new Rectangle((int) (tileOffsetX + tileOffsetX*offsetX), (int) (tileOffsetY + tileOffsetY*offsetY), tileSizeX, tileSizeY), 
	 				backgroundEdgeColour, backgroundColour, rotation
	 			);
 	 		}
 	 		
 	 		if (backgroundImageInfo.text() != null)
 	 		{
 	 			final Font valueFont = new Font("Arial", Font.BOLD, (int) (dim * backgroundImageInfo.scale()));
 				g2d.setColor(backgroundImageInfo.mainColour());
 				g2d.setFont(valueFont);
 				StringUtil.drawStringAtPoint(g2d, backgroundImageInfo.text(), null, new Point(g2d.getWidth()/2,g2d.getHeight()/2), true);
 	 		}
 	 	}
		
		return g2d;
	}
	
	//----------------------------------------------------------------------------
	
	/**
	 * Returns an SVG image for the component style foreground.
	 */
	protected SVGGraphics2D getForeground(final SVGGraphics2D g2d, final Context context, final int containerIndex, final int localState, final int value, final int dim)
	{
		final Graphics metadataGraphics = context.game().metadata().graphics();

		for (final MetadataImageInfo foregroundImageInfo : metadataGraphics.pieceForeground(context, component.owner(), component.name(), containerIndex, localState, value))
		{
			if (foregroundImageInfo.path() != null)
 	 		{
	 	 		final String foregroundPath = ImageUtil.getImageFullPath(foregroundImageInfo.path());
	 		 	final double foregroundScaleX = foregroundImageInfo.scaleX();
	 		 	final double foregroundScaleY = foregroundImageInfo.scaleY();
	 		 	maxForegroundScale = Math.max(Math.max(foregroundScaleX, foregroundScaleY), maxForegroundScale);
	 		 	Color foregroundColour = foregroundImageInfo.mainColour();
	 		 	Color foregroundEdgeColour = foregroundImageInfo.secondaryColour();
	 		 	final int rotation = foregroundImageInfo.rotation();
	 		 	final double offsetX = foregroundImageInfo.offestX();
	 		 	final double offsetY = foregroundImageInfo.offestY();
	 		 	
	 		 	if (foregroundColour == null)
	 		 		foregroundColour = bridge.settingsColour().playerColour(context, component.owner());
	 		 	if (foregroundEdgeColour == null)
	 		 		foregroundEdgeColour = Color.BLACK;
	 		 	
	 		 	final int tileSizeX = (int) (dim * foregroundScaleX);
	 		 	final int tileOffsetX = (dim-tileSizeX)/2;
	 		 	final int tileSizeY = (int) (dim * foregroundScaleY);
	 		 	final int tileOffsetY = (dim-tileSizeY)/2;
	 			SVGtoImage.loadFromFilePath
	 			(
	 				g2d, foregroundPath, new Rectangle((int) (tileOffsetX + tileOffsetX*offsetX), (int) (tileOffsetY + tileOffsetY*offsetY), tileSizeX, tileSizeY), 
	 				foregroundEdgeColour, foregroundColour, rotation
	 			);
 	 		}

			if (foregroundImageInfo.text() != null)
 	 		{
 	 			final Font valueFont = new Font("Arial", Font.BOLD, (int) (dim * foregroundImageInfo.scale()));
 				g2d.setColor(foregroundImageInfo.mainColour());
 				g2d.setFont(valueFont);
 				StringUtil.drawStringAtPoint(g2d, foregroundImageInfo.text(), null, new Point(g2d.getWidth()/2,g2d.getHeight()/2), true);
 	 		}
 	 	}
		
		return g2d;
	}
	
	//----------------------------------------------------------------------------

	@Override
	public SVGGraphics2D getImageSVG(final int localState)
	{		
		if (localState >= imageSVG.size())
		{
			if (imageSVG.size() > 0)
				return imageSVG.get(0);
			else
				return null;
		}
		return imageSVG.get(localState);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double scale(final Context context, final int containerIndex, final int localState, final int value) 
	{		
		// Need to check metadata for any adjusted piece scales, as the same component style may have different scales based on state and value (e.g. Mig Mang).
		final Graphics metadataGraphics = context.game().metadata().graphics();
		final Point2D.Float scale = metadataGraphics.pieceScale(context, component.owner(), component.name(), containerIndex, localState, value);
		scaleX = scale.getX();
		scaleY = scale.getY();
		
		return Math.max(Math.max(Math.max(scaleX, scaleY), maxBackgroundScale),maxForegroundScale);
	}

	@Override
	public ArrayList<Point> origin() 
	{
		return new ArrayList<>();
	}

	@Override
	public ArrayList<Point2D> getLargeOffsets() 
	{
		return new ArrayList<>();
	}

	@Override
	public Point largePieceSize() 
	{
		return new Point();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Color getSecondaryColour()
	{
		return secondaryColour;
	}
	
	//-------------------------------------------------------------------------
	
}
