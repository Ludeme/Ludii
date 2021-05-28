package controllers;

import java.awt.Point;
import java.util.List;

import other.context.Context;
import other.location.Location;

/**
 * Controller interface for controlling piece movement.
 * 
 * @author Matthew.Stephenson
 */
public interface Controller
{
	Location calculateNearestLocation(Context context, Point pt, List<Location> legalLocations);
}
