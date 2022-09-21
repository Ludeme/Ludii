/*
 * Created on 8-apr-05
 *
 */
package ai.player;

import java.util.Vector;

import core.Metaposition;
import core.Move;

/**
 * Provides the required routines for operating an AI Player.
 * @author Nikola Novarlic
 *
 */
public class AIPlayer extends Player {
	
	public static final int MESSAGE_TYPE_MOVE =		0; 
	public static final int MESSAGE_TYPE_INFO =		1;
	public static final int MESSAGE_TYPE_ERROR = 	2;
	
	
	/**
	 * At the heart of every minimax, even our expectiminimax, is the
	 * evaluation function, which assigns a goodness value to a given
	 * position. AIPlayer's implementation does nothing; this method
	 * will be overriden by its children.
	 * @param c
	 * @return
	 */
	
	public float evaluate(Metaposition start, Move m, Metaposition dest, Vector history) { return 0.0f; }
	
	public boolean usesGlobals()
	{
		return true;
	}

}
