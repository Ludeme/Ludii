/*
 * Created on 12-mar-06
 *
 */
package ai.planner;

import java.util.Vector;

import ai.player.Darkboard;
import core.Metaposition;
import core.Move;

/**
 * @author Nikola Novarlic
 *
 */
public class Plan {
	
	public int priority=3;
	public boolean active=true;
	public int age=0;
	public int ttl=-1;
	float falloff=1.0f; //plan benefit may fade over time.
	public float modifier=1.0f; //current plan intensity
	protected Vector subplans = new Vector();
	
	public Darkboard owner;
	
	public static String description = ""; //after a call to evaluate, contains info
	
	public Plan(Darkboard own)
	{
		owner = own;
	}
	
	/**
	 * Called when a plan is activated and put into a Dashboard.
	 *
	 */
	public void isAdded()
	{
		
	}
	
	public float evaluate(Metaposition start, Metaposition end, Move m, Vector history)
	{
		float v = fitness(start,end,m,history);
		return v*modifier;
	}
	
	protected float fitness(Metaposition start, Metaposition end, Move m, Vector history)
	{
		return 0.0f;
	}
	
	public String toString()
	{
		return "Generic plan";
	}
	
	/**
	 * Returns the move that triggers the plan's execution, if any.
	 * @return
	 */
	public float isApplicable(Metaposition start, Move m)
	{
		return 0.0f;
	}
	
	public void evolveAfterMove(Metaposition root, Metaposition ev, Move m,
			int cap, int capx, int capy, int check1, int check2, int tries)
	{
		
	}
	
	public void evolveAfterOpponentMove(Metaposition root, Metaposition ev,
			int capx, int capy, int check1, int check2, int tries)
	{
		
	}
	
	public void evolveAfterIllegalMove(Metaposition root, Metaposition ev, Move m/*, int capx, int capy, int c1, int c2, int tries*/)
	{
		
	}

}
