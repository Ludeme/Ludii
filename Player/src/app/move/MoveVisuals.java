package app.move;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import app.PlayerApp;
import game.Game;
import game.rules.end.EndRule;
import game.rules.end.If;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import main.Constants;
import main.collections.FVector;
import main.collections.FastArrayList;
import other.AI;
import other.AI.AIVisualisationData;
import other.context.Context;
import other.location.Location;
import other.move.Move;
import util.ArrowUtil;
import util.ContainerUtil;
import util.HiddenUtil;

/**
 * Functions that deal with specific visualisations of moves
 * 
 * @author Matthew.Stephenson
 */
public class MoveVisuals 
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draw the last move that was performed.
	 */
	public static void drawLastMove(final PlayerApp app, final Graphics2D g2d, final Context context, final Rectangle passLocation, final Rectangle otherLocation)
	{
		final Move lastMove = context.currentInstanceContext().trial().lastMove();
		drawMove(app, g2d, context, passLocation, otherLocation, lastMove, new Color(1.f, 1.f, 0.f, 0.5f));
	}
	
	/**
	 * Draw all possible moves as a set of arrows. Used for tutorial visualisation.
	 */
	public static void drawTutorialVisualisatonArrows(final PlayerApp app, final Graphics2D g2d, final Context context, final Rectangle passLocation, final Rectangle otherLocation)
	{
//		for (final Move m : context.game().moves(context).moves())
//			drawMove(app, g2d, context, passLocation, otherLocation, m, new Color(1.f, 0.f, 0.f, 1.f));
		
		if (app.manager().undoneMoves().size() > 0) 
		{
			final Game game = context.game();
			final Move nextMove = app.manager().undoneMoves().get(0);
			for (final Move legalMove: game.moves(context).moves()) {
				// Only show moves
				if (app.settingsPlayer().tutorialVisualisationMoveType() == "move") {
					if (legalMove.from() == nextMove.from() && context.state().containerStates()[0].isEmptyCell(legalMove.to())) {
						MoveVisuals.drawMove(app, g2d, context, passLocation, otherLocation, legalMove, new Color(1.f, 0.f, 0.f, 1.f));
					}
				// Only show captures
				} else if (app.settingsPlayer().tutorialVisualisationMoveType() == "capture") {
					if (legalMove.from() == nextMove.from() && !context.state().containerStates()[0].isEmptyCell(legalMove.to())) {
						MoveVisuals.drawMove(app, g2d, context, passLocation, otherLocation, legalMove, new Color(1.f, 0.f, 0.f, 1.f));
					}
				}
				// Show all legal moves
				else {
					if (legalMove.from() == nextMove.from()) {
						MoveVisuals.drawMove(app, g2d, context, passLocation, otherLocation, legalMove, new Color(1.f, 0.f, 0.f, 1.f));
					}
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Draw the AI distribution.
	 */
	public static void drawAIDistribution(final PlayerApp app, final Graphics2D g2d, final Context context, final Rectangle passLocation, final Rectangle otherLocation)
	{
		if (!context.trial().over())
		{
			if (app.manager().liveAIs() == null)
				return;
			
			for (final AI visualisationAI : app.manager().liveAIs())
			{
				if (visualisationAI == null)
					continue;

				final AIVisualisationData visData = visualisationAI.aiVisualisationData();

				if (visData != null)
				{
					final FVector aiDistribution = visData.searchEffort();
					final FVector valueEstimates = visData.valueEstimates();
					final FastArrayList<Move> moves = visData.moves();

					final float maxVal = aiDistribution.max();

					for (int i = 0; i < aiDistribution.dim(); ++i)
					{
						final float val = aiDistribution.get(i);
						float probRatio = 0.05f + ((0.95f * val) / maxVal);

						if (probRatio > 1)
							probRatio = 1;
						if (probRatio < -1)
							probRatio = -1;

						final Move move = moves.get(i);
						final int from = move.from();
						final int to = move.to();
						
						final SiteType fromType = move.fromType();
						final SiteType toType = move.toType();
						
						final int fromContainerIdx = ContainerUtil.getContainerId(context, from, fromType);
						final int toContainerIdx = ContainerUtil.getContainerId(context, to, toType);
						
						if (from != to)
						{
							final Point2D fromPosnWorld = app.bridge().getContainerStyle(fromContainerIdx)
									.drawnGraphElement(from, fromType).centroid();
							final Point2D ToPosnWorld = app.bridge().getContainerStyle(toContainerIdx)
									.drawnGraphElement(to, toType).centroid();
							
							final Point fromPosnScreen = app.bridge().getContainerStyle(fromContainerIdx).screenPosn(fromPosnWorld);
							final Point toPosnScreen = app.bridge().getContainerStyle(toContainerIdx).screenPosn(ToPosnWorld);

							final int fromX = fromPosnScreen.x;
							final int fromY = fromPosnScreen.y;
							final int toX = toPosnScreen.x;
							final int toY = toPosnScreen.y;
							
							final int maxRadius = Math.max(app.bridge().getContainerStyle(fromContainerIdx).cellRadiusPixels(), app.bridge().getContainerStyle(toContainerIdx).cellRadiusPixels());
							final int minRadius = maxRadius / 4;
							final int arrowWidth = Math.max((int) ((minRadius + probRatio * (maxRadius - minRadius)) / 2.5), 1);

							if (valueEstimates != null)
							{
								// interpolate between red (losing) and blue (winning)
								g2d.setColor(new Color(0.5f - 0.5f * valueEstimates.get(i), 0.f,
										0.5f + 0.5f * valueEstimates.get(i), 0.5f + 0.2f * probRatio * probRatio));
							}
							else
							{
								// just use red
								g2d.setColor(new Color(1.f, 0.f, 0.f, 0.5f + 0.2f * probRatio * probRatio));
							}

							if (move.isOrientedMove())
							{
								// draw arrow with arrow head
								ArrowUtil.drawArrow(g2d, fromX, fromY, toX, toY, arrowWidth, (Math.max(arrowWidth, 3)),
										(int) (1.75 * (Math.max(arrowWidth, 5))));
							}
							else
							{
								// draw arrow with no head
								ArrowUtil.drawArrow(g2d, fromX, fromY, toX, toY, arrowWidth, 0, 0);
							}
						}
						else if (to != Constants.UNDEFINED)
						{
							final int maxRadius =  app.bridge().getContainerStyle(toContainerIdx).cellRadiusPixels();
							final int minRadius = maxRadius / 4;

							final Point2D ToPosnWorld = app.bridge().getContainerStyle(toContainerIdx)
									.drawnGraphElement(to, toType).centroid();
							final Point toPosnScreen = app.bridge().getContainerStyle(toContainerIdx).screenPosn(ToPosnWorld);
							
							final int midX = toPosnScreen.x;
							final int midY = toPosnScreen.y;

							final int radius = (int) (minRadius + probRatio * (maxRadius - minRadius));

							if (valueEstimates != null)
							{
								// interpolate between red (losing) and blue (winning)
								g2d.setColor(new Color(0.5f - 0.5f * valueEstimates.get(i), 0.f,
										0.5f + 0.5f * valueEstimates.get(i), 0.5f + 0.2f * probRatio));
							}
							else
							{
								// just use red
								g2d.setColor(new Color(1.f, 0.f, 0.f, 0.5f + 0.2f * probRatio));
							}

							g2d.fillOval(midX - radius, midY - radius, 2 * radius, 2 * radius);
						}
						else
						{
							// no positions
							if (move.isPass() || move.isSwap() || move.isOtherMove() || move.containsNextInstance())
							{
								
								Rectangle position = null;
								if (move.isPass() || move.containsNextInstance())
									position = passLocation;
								else
									position = otherLocation;

								if (position != null)
								{
									final int maxRadius = ((Math.min(position.width, position.height))) / 2;
									final int minRadius = maxRadius / 4;

									final int midX = (int) position.getCenterX();
									final int midY = (int) position.getCenterY();

									final int radius = (int) (minRadius + probRatio * (maxRadius - minRadius));

									if (valueEstimates != null)
									{
										// Interpolate between red (losing) and blue (winning)
										g2d.setColor(new Color(0.5f - 0.5f * valueEstimates.get(i), 0.f,
												0.5f + 0.5f * valueEstimates.get(i), 0.5f + 0.2f * probRatio));
									}
									else
									{
										// Just use red
										g2d.setColor(new Color(1.f, 0.f, 0.f, 0.5f + 0.2f * probRatio));
									}

									g2d.fillOval(midX - radius, midY - radius, 2 * radius, 2 * radius);
								}
							}
						}
					}
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draws moves that would lead to a repeated state (Green = legal, Red = illegal).
	 */
	public static void drawRepeatedStateMove(final PlayerApp app, final Graphics2D g2d, final Context context, final Rectangle passLocation, final Rectangle otherLocation)
	{
		final Moves legal = context.moves(context);
		
		// Moves that can be done, but lead to a repeated state (Green)
		final ArrayList<Move> movesThatLeadToRepeatedStates = new ArrayList<>();
		for (final Move m : legal.moves())
		{
			final Context newContext = new Context(context);
			newContext.game().apply(newContext, m);

			if (app.manager().settingsManager().storedGameStatesForVisuals().contains(Long.valueOf(newContext.state().stateHash())))
				movesThatLeadToRepeatedStates.add(m);
		}
		
		for (final Move m : movesThatLeadToRepeatedStates)
			drawMove(app, g2d, context, passLocation, otherLocation, m, new Color(0.f, 1.f, 0.f, 0.5f));
		
		// Moves that cannot be done, because they lead to a repeated state (Red)
		for (final Move m: app.manager().settingsManager().movesAllowedWithRepetition())
			if (!legal.moves().contains(m))
				drawMove(app, g2d, context, passLocation, otherLocation, m, new Color(1.f, 0.f, 0.f, 0.5f));
	}
	//-------------------------------------------------------------------------
	
	/**
	 * Draws ending move when one was just made. 
	 */
	public static void drawEndingMove(final PlayerApp app, final Graphics2D g2d, final Context context)
	{
		final Context copyContext = new Context(context);
		copyContext.state().setMover(context.state().prev());
		copyContext.state().setNext(context.state().mover());
		
		for (final EndRule endRule : context.game().endRules().endRules())
		{
			if (endRule instanceof If)
			{
				if (((If) endRule).result() != null && ((If) endRule).result().result() != null)
				{
					final List<Location> endingLocations = ((If) endRule).endCondition().satisfyingSites(copyContext);
					for (final Location location : endingLocations)
						drawEndingMoveLocation(app, g2d, context, location);
					if (endingLocations.size() > 0)
						break;
				}
			}
		}	
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draws a marker for representing an ending move at a given location, for a given result.
	 */
	private static void drawEndingMoveLocation(final PlayerApp app, final Graphics2D g2d, final Context context, final Location location)
	{
		final int lastMover = context.trial().getMove(context.trial().numMoves()-1).mover();

		Color colour = null;
		if (!context.trial().over())
		{
			if (context.active(lastMover))
				colour = new Color(1.0f, 0.7f, 0.f, 0.7f);
			else if (context.computeNextDrawRank() > context.trial().ranking()[lastMover])
				colour = new Color(0.f, 1.f, 0.f, 0.7f);
			else if (context.computeNextDrawRank() < context.trial().ranking()[lastMover])
				colour = new Color(1.f, 0.f, 0.f, 0.7f);
		}
		else
		{
			if (context.winners().contains(lastMover))
				colour = new Color(0.f, 1.f, 0.f, 0.7f);
			else
				colour = new Color(1.f, 0.f, 0.f, 0.7f);
		}

		final int site = location.site();
		final SiteType type = location.siteType();
		final int containerIdx = ContainerUtil.getContainerId(context, site, type);
	
		final Point2D ToPosnWorld = app.bridge().getContainerStyle(containerIdx).drawnGraphElement(site, type).centroid();
		final Point toPosnScreen = app.bridge().getContainerStyle(containerIdx).screenPosn(ToPosnWorld);

		final int midX = toPosnScreen.x;
		final int midY = toPosnScreen.y;

		g2d.setColor(Color.BLACK);
		int radius = (int) (app.bridge().getContainerStyle(containerIdx).cellRadiusPixels()/2 * 1.1) + 2;
		g2d.fillOval(midX - radius, midY - radius, 2 * radius, 2 * radius);
		
		g2d.setColor(colour);
		radius = app.bridge().getContainerStyle(containerIdx).cellRadiusPixels()/2;
		g2d.fillOval(midX - radius, midY - radius, 2 * radius, 2 * radius);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draw the specified move.
	 * Generic functionality, called by the other MoveVisuals functions.
	 */
	private static void drawMove(final PlayerApp app, final Graphics2D g2d, final Context context, final Rectangle passLocation, final Rectangle otherLocation, final Move move, final Color colour)
	{
		final int currentMover = context.state().mover();
		g2d.setColor(colour);

		if (move != null)
		{
			final int from = move.from();
			final int to = move.to();
			final int fromLevel = move.levelFrom();
			final int toLevel = move.levelTo();
			final SiteType fromType = move.fromType();
			final SiteType toType = move.toType();
			final int fromContainerIdx = ContainerUtil.getContainerId(context, from, fromType);
			final int toContainerIdx = ContainerUtil.getContainerId(context, to, toType);

			if (from != to)
			{
				// Move with two locations.
				final Point2D fromPosnWorld = app.bridge().getContainerStyle(fromContainerIdx)
						.drawnGraphElement(from, fromType).centroid();
				final Point2D ToPosnWorld = app.bridge().getContainerStyle(toContainerIdx).drawnGraphElement(to, toType)
						.centroid();
				
				final Point fromPosnScreen = app.bridge().getContainerStyle(fromContainerIdx).screenPosn(fromPosnWorld);
				final Point toPosnScreen = app.bridge().getContainerStyle(toContainerIdx).screenPosn(ToPosnWorld);

				final int fromX = fromPosnScreen.x;
				final int fromY = fromPosnScreen.y;
				final int toX = toPosnScreen.x;
				final int toY = toPosnScreen.y;
				
				final int maxRadius = Math.max(app.bridge().getContainerStyle(fromContainerIdx).cellRadiusPixels(),
						app.bridge().getContainerStyle(toContainerIdx).cellRadiusPixels());
				final int arrowWidth = Math.max((int) (maxRadius / 2.5), 1);

				boolean arrowHidden = false;
				if (HiddenUtil.siteHiddenBitsetInteger(context, context.state().containerStates()[fromContainerIdx], from, fromLevel, currentMover, fromType) > 0
						|| HiddenUtil.siteHiddenBitsetInteger(context, context.state().containerStates()[toContainerIdx], to, toLevel, currentMover, toType) > 0)
					arrowHidden = true;

				if (!arrowHidden)
				{
					if (move.isOrientedMove())
						ArrowUtil.drawArrow(g2d, fromX, fromY, toX, toY, arrowWidth, (Math.max(arrowWidth, 3)),
								(int) (1.75 * (Math.max(arrowWidth, 5))));
					else
						ArrowUtil.drawArrow(g2d, fromX, fromY, toX, toY, arrowWidth, 0, 0);
				}
			}
			else if (to != Constants.UNDEFINED)
			{
				// Move with just one location.
				final Point2D ToPosnWorld = app.bridge().getContainerStyle(toContainerIdx).drawnGraphElement(to, toType)
						.centroid();
				final Point toPosnScreen = app.bridge().getContainerStyle(toContainerIdx).screenPosn(ToPosnWorld);

				final int midX = toPosnScreen.x;
				final int midY = toPosnScreen.y;

				final int radius = app.bridge().getContainerStyle(toContainerIdx).cellRadiusPixels()/2;
				g2d.fillOval(midX - radius, midY - radius, 2 * radius, 2 * radius);
			}
			else
			{
				// Move with no location.
				Rectangle position = null;
				if (move.isPass() || move.containsNextInstance())
					position = passLocation;
				else if (move.isOtherMove())
					position = otherLocation;
				
				if (position != null)
					g2d.fillOval(position.x, position.y, position.width, position.height);
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
}
