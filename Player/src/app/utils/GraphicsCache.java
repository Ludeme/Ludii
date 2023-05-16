package app.utils;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.equipment.component.Component;
import game.equipment.container.other.Dice;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import main.Constants;
import other.action.Action;
import other.action.die.ActionUpdateDice;
import other.action.die.ActionUseDie;
import other.context.Context;
import other.move.Move;
import util.ImageInfo;
import view.component.ComponentStyle;

/**
 * GraphicsCache for storing all the images we need.
 * 
 * @author Matthew.Stephenson
 */
public class GraphicsCache
{

	// All component images and sizes for use on the containers.
	private cacheStorage allComponentImages = new cacheStorage();
	
	// All component images and sizes for use on other GUI elements without a container.
	private cacheStorage allComponentImagesSecondary = new cacheStorage();
	
	// Other images we want to store
	private BufferedImage boardImage = null;
	private BufferedImage graphImage = null;
	private BufferedImage connectionsImage = null;
	private BufferedImage[] allToolButtons = new BufferedImage[9];
	private final ArrayList<DrawnImageInfo> allDrawnComponents = new ArrayList<>();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Get the image of a component.
	 */
	public BufferedImage getComponentImage
	(
		final Bridge bridge,
		final int containerId,
		final Component component,
		final int owner,
		final int localState, 
		final int value,
		final int site, 
		final int level,
		final SiteType type,
		final int imageSize,
		final Context context,
		final int hiddenValue,
		final int rotation,
		final boolean secondary
	) 
	{
		final int componentId = component.index();
		final ComponentStyle componentStyle = bridge.getComponentStyle(component.index());

		// Retrieve and initialise the correct graphics cache object.
		cacheStorage componentImageArray;
		if (secondary && component.isTile())
			componentImageArray = allComponentImagesSecondary().setupCache(containerId, componentId, owner, localState, value, hiddenValue, rotation, secondary);
		else
			componentImageArray = allComponentImages().setupCache(containerId, componentId, owner, localState, value, hiddenValue, rotation, secondary);
	
		// Fetch the image and size stored in the graphics cache.
		BufferedImage cacheImage = componentImageArray.getCacheImage(containerId, componentId, owner, localState, value, hiddenValue, rotation);
		int cacheImageSize = componentImageArray.getCacheImageSize(containerId, componentId, owner, localState, value, hiddenValue, rotation).intValue();

		// If the component does not have a stored image for the provided local state, then create one from its SVG string
		if (cacheImage == null || cacheImageSize != imageSize)
		{
			// create the image for the given local state value
			if (containerId > 0 && component.isLargePiece())
				componentStyle.renderImageSVG(context, containerId, bridge.getContainerStyle(0).cellRadiusPixels()*2, localState, value, true, hiddenValue, rotation);
			else if (containerId > 0 && component.isTile())
				componentStyle.renderImageSVG(context, containerId, imageSize, localState, value, true, hiddenValue, rotation);
			else
				componentStyle.renderImageSVG(context, containerId, imageSize, localState, value, secondary, hiddenValue, rotation);
			
			final SVGGraphics2D svg = componentStyle.getImageSVG(localState);
			final BufferedImage componentImage = getComponentBufferedImage(svg, component, componentStyle, context, containerId, imageSize, localState, secondary);

			componentImageArray.setCacheImage(componentImage, containerId, componentId, owner, localState, value, hiddenValue, rotation);
			componentImageArray.setCacheImageSize(imageSize, containerId, componentId, owner, localState, value, hiddenValue, rotation);
			
			cacheImage = componentImage;
			cacheImageSize = imageSize;
		}

		// Check if the component is a die, if so then if the die is used make it transparent.
		if (component.isDie())
			return getDiceImage(containerId, component, localState, site, context, componentId, cacheImage);
		
		return cacheImage;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Creates the (basic) image of the component from its SVG.
	 */
	private static BufferedImage getComponentBufferedImage(final SVGGraphics2D svg, final Component component, final ComponentStyle componentStyle, final Context context, final int containerId, final int imageSize, final int localState, final boolean secondary)
	{		
		// create the BufferedImage based on this SVG image.
		BufferedImage componentImage = null;
		if (svg != null)
		{
			if (component.isLargePiece()) 
			{
				componentImage = SVGUtil.createSVGImage(svg.getSVGDocument(), componentStyle.largePieceSize().x, componentStyle.largePieceSize().y);
				if (containerId != 0)
				{
					final int maxSize = Math.max(componentStyle.largePieceSize().x, componentStyle.largePieceSize().y);
					final double scaleFactor = 0.9 * imageSize / maxSize;
					componentImage = BufferedImageUtil.resize(componentImage, (int)(scaleFactor * componentStyle.largePieceSize().x), (int)(scaleFactor * componentStyle.largePieceSize().y));
				}
			}
			else
			{
				componentImage = SVGUtil.createSVGImage(svg.getSVGDocument(), imageSize, imageSize);
			}
		}
		
		return componentImage;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Gets the image of the component if it's a dice (transparent if already used).
	 */
	private static BufferedImage getDiceImage
	(
		final int containerId,
		final Component component,
		final int localState, 
		final int site, 
		final Context context, 
		final int componentId, 
		final BufferedImage cacheImage
	)
	{
		// get the index of the dice container (usually just one)
		int handDiceIndex = -1;
		for (int j = 0; j < context.handDice().size(); j++)
		{
			final Dice dice = context.handDice().get(j);
			if (dice.index() == containerId)
			{
				handDiceIndex = j;
				break;
			}
		}
		
		// Only grey out dice if they are in a dice hand.
		// TODO ERIC REWRITE THIS INTO INFORMATION CONTEXT
		if (handDiceIndex != -1)
		{
			// Previous value of the dice (looking for if this is zero) (before to apply the prior (now do) moves).
			int previousValue = context.state().currentDice()[handDiceIndex][site - context.sitesFrom()[containerId]];
			
			int stateValue = localState;
			//final Context fullContext = ((InformationContext) context).originalContext();
			final Moves moves = context.moves(context);
			boolean useDieDetected = false;
			if (moves.moves().size() > 0)
			{
				
				// Keep only actions that are the same across all moves.
				final ArrayList<Action> allSameActions = new ArrayList<Action>(moves.moves().get(0).actions());
				for (final Move m : moves.moves())
				{
					boolean differentAction = false;
					for (int j = allSameActions.size()-1; j >= 0; j--)
					{
						if (m.actions().size() <= j)
							break;
						if (allSameActions.get(j) != m.actions().get(j))
							differentAction = true;
						if (differentAction)
							allSameActions.remove(j);
					}
				}
	
				// We check if the moves used an ActionUseDie to know if we need to grey them or not.
				for (final Move m : moves.moves())
				{
					for (final Action action : m.actions())
						if (action instanceof ActionUseDie)
						{
							useDieDetected = true;
							break;
						}
					if (useDieDetected)
						break;
				}
	
				// We keep only the same actions equal to ActionSetStateAndUpdateDice.
				for (int j = 0; j < allSameActions.size(); j++)
				{
					if (!(allSameActions.get(j) instanceof ActionUpdateDice))
						allSameActions.remove(j);
				}
	
				final int loc = context.sitesFrom()[containerId] + site;
				for (final Action a : allSameActions)
				{
					if (a.from() == loc && stateValue != Constants.UNDEFINED)
					{
						stateValue = a.state();
						previousValue = context.components()[component.index()].getFaces()[stateValue];
					}
				}
			}
	
			// Grey the die if the previous value was 0 and this is the same turn and the action useDieDetected is used.
			if (context.state().mover() == context.state().prev() && previousValue == 0 && useDieDetected)
				return BufferedImageUtil.makeImageTranslucent(cacheImage, 0.2);
		}
		
		return cacheImage;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draws the image onto the view, and also saves its information for selection use later.
	 */
	public void drawPiece(final Graphics2D g2d, final Context context, final BufferedImage pieceImage, final Point posn, final int site, final int level, final SiteType type, final double transparency)
	{
		BufferedImage imageToDraw = pieceImage;
		if (transparency != 0)
		{
			imageToDraw = BufferedImageUtil.makeImageTranslucent(pieceImage, transparency);
		}
		final ImageInfo imageInfo = new ImageInfo(posn, site, level, type);	
		allDrawnComponents().add(new DrawnImageInfo(pieceImage,imageInfo));
		g2d.drawImage(imageToDraw, posn.x, posn.y, null);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Clear the graphics cache.
	 */
	public void clearAllCachedImages()
	{
		setAllComponentImages(new cacheStorage());
		setAllComponentImagesSecondary(new cacheStorage());
		setBoardImage(null);
		setGraphImage(null);
		setConnectionsImage(null);
		setAllToolButtons(new BufferedImage[Constants.MAX_PLAYERS+1]);
		allDrawnComponents().clear();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Gets the size of the image for a component.
	 */
	public int getComponentImageSize(final int containerId, final int componentId, final int owner, final int localState, final int value, final int hiddenValue, final int rotation)
	{
		return allComponentImages().getCacheImageSize(containerId, componentId, owner, localState, value, hiddenValue, rotation).intValue();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Nested class for storing the bufferedImages from the component SVGs.
	 * [Container, Component, State, Value, HiddenValue, Rotation]
	 */
	protected static class cacheStorage 
	{
		/** All component images for use on the containers (BufferedImages). */
		protected final ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<BufferedImage>>>>>>> cacheStorageImages = new ArrayList<>();
		
		/** All component image sizes for use on the containers (Integer). */
		protected final ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<Integer>>>>>>> cacheStorageSizes = new ArrayList<>();
		
		//-------------------------------------------------------------------------
		
		protected BufferedImage getCacheImage(final int containerId, final int componentId, final int owner, final int localState, final int value, final int hiddenValue, final int rotation)
		{
			return cacheStorageImages.get(containerId).get(componentId).get(owner).get(localState).get(value).get(hiddenValue).get(rotation);
		}
		
		protected Integer getCacheImageSize(final int containerId, final int componentId, final int owner, final int localState, final int value, final int hiddenValue, final int rotation)
		{
			return cacheStorageSizes.get(containerId).get(componentId).get(owner).get(localState).get(value).get(hiddenValue).get(rotation);
		}
		
		//-------------------------------------------------------------------------
		
		protected BufferedImage setCacheImage(final BufferedImage image, final int containerId, final int componentId, final int owner, final int localState, final int value, final int hiddenValue, final int rotation)
		{
			return cacheStorageImages.get(containerId).get(componentId).get(owner).get(localState).get(value).get(hiddenValue).set(rotation, image);
		}
		
		protected Integer setCacheImageSize(final int size, final int containerId, final int componentId, final int owner, final int localState, final int value, final int hiddenValue, final int rotation)
		{
			return cacheStorageSizes.get(containerId).get(componentId).get(owner).get(localState).get(value).get(hiddenValue).set(rotation, Integer.valueOf(size));
		}
		
		//-------------------------------------------------------------------------
		
		/**
		 * Adds additional empty arrayLists and default values to the graphics cache, for the intermediary values.
		 */
		protected final cacheStorage setupCache
		(
			final int containerId,
			final int componentId,
			final int owner,
			final int localState, 
			final int value, 
			final int hiddenValue,
			final int rotation,
			final boolean secondary
		)
		{
			while (cacheStorageImages.size() <= containerId)
			{
				cacheStorageImages.add(new ArrayList<>());
				cacheStorageSizes.add(new ArrayList<>());
			}
			
			while (cacheStorageImages.get(containerId).size() <= componentId)
			{
				cacheStorageImages.get(containerId).add(new ArrayList<>());
				cacheStorageSizes.get(containerId).add(new ArrayList<>());
			}
			
			while (cacheStorageImages.get(containerId).get(componentId).size() <= owner)
			{
				cacheStorageImages.get(containerId).get(componentId).add(new ArrayList<>());
				cacheStorageSizes.get(containerId).get(componentId).add(new ArrayList<>());
			}
			
			while (cacheStorageImages.get(containerId).get(componentId).get(owner).size() <= localState)
			{
				cacheStorageImages.get(containerId).get(componentId).get(owner).add(new ArrayList<>());
				cacheStorageSizes.get(containerId).get(componentId).get(owner).add(new ArrayList<>());
			}
			
			while (cacheStorageImages.get(containerId).get(componentId).get(owner).get(localState).size() <= value)
			{
				cacheStorageImages.get(containerId).get(componentId).get(owner).get(localState).add(new ArrayList<>());
				cacheStorageSizes.get(containerId).get(componentId).get(owner).get(localState).add(new ArrayList<>());
			}
			
			while (cacheStorageImages.get(containerId).get(componentId).get(owner).get(localState).get(value).size() <= hiddenValue)
			{
				cacheStorageImages.get(containerId).get(componentId).get(owner).get(localState).get(value).add(new ArrayList<>());
				cacheStorageSizes.get(containerId).get(componentId).get(owner).get(localState).get(value).add(new ArrayList<>());
			}
			
			while (cacheStorageImages.get(containerId).get(componentId).get(owner).get(localState).get(value).get(hiddenValue).size() <= rotation)
			{
				cacheStorageImages.get(containerId).get(componentId).get(owner).get(localState).get(value).get(hiddenValue).add(null);
				cacheStorageSizes.get(containerId).get(componentId).get(owner).get(localState).get(value).get(hiddenValue).add(Integer.valueOf(0));
			}
			
			return this;
		}
	}
	
	//-------------------------------------------------------------------------
	
	public BufferedImage boardImage() 
	{
		return boardImage;
	}

	public void setBoardImage(final BufferedImage boardImage) 
	{
		this.boardImage = boardImage;
	}

	public BufferedImage graphImage() 
	{
		return graphImage;
	}

	public void setGraphImage(final BufferedImage graphImage) 
	{
		this.graphImage = graphImage;
	}

	public BufferedImage connectionsImage() 
	{
		return connectionsImage;
	}

	public void setConnectionsImage(final BufferedImage connectionsImage) 
	{
		this.connectionsImage = connectionsImage;
	}

	public BufferedImage[] allToolButtons() 
	{
		return allToolButtons;
	}

	public void setAllToolButtons(final BufferedImage[] allToolButtons) 
	{
		this.allToolButtons = allToolButtons;
	}

	public ArrayList<DrawnImageInfo> allDrawnComponents() 
	{
		return allDrawnComponents;
	}
	
	private cacheStorage allComponentImages() 
	{
		return allComponentImages;
	}

	private void setAllComponentImages(final cacheStorage allComponentImages) 
	{
		this.allComponentImages = allComponentImages;
	}

	private cacheStorage allComponentImagesSecondary() 
	{
		return allComponentImagesSecondary;
	}

	private void setAllComponentImagesSecondary(final cacheStorage allComponentImagesSecondary) 
	{
		this.allComponentImagesSecondary = allComponentImagesSecondary;
	}

	//-------------------------------------------------------------------------

}
