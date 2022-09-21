/*
 * Created on 15-nov-05
 *
 */
package ai.evolve;

import java.io.Serializable;
import java.util.Random;

/**
 * @author Nikola Novarlic
 *
 */
public class SimpleWeightSet implements Serializable {
	
	private static final Random r = new Random();
	
	public static final int WEIGHT_NUMBER = 2048;
	
	public float weights[] = new float[WEIGHT_NUMBER];
	
	//base values
	public static final int W_PAWN_V = 0;
	public static final int W_KNIGHT_V = 1;
	public static final int W_BISHOP_V = 2;
	public static final int W_ROOK_V = 3;
	public static final int W_QUEEN_V = 4;
	
	//increments
	public static final int W_PAWN_I = 5;
	public static final int W_KNIGHT_I = 6;
	public static final int W_BISHOP_I = 7;
	public static final int W_ROOK_I = 8;
	public static final int W_QUEEN_I = 9;	
	
	//pawn bonuses
	public static final int W_PASSED_PAWN_I = 10;
	public static final int W_ADJACENT_PAWN_I = 11;
	public static final int W_ADVANCEMENT_PAWN_I = 12;
	
	//knight bonuses
	public static final int W_MAX_MOVES_KNIGHT_I = 13;
	
	//bishop bonuses
	public static final int W_CENTRAL_BISHOP_I = 14;
	
	//rook bonuses
	public static final int W_CENTRAL_ROOK_I = 15;
	
	//queen bonuses
	public static final int W_CENTRAL_QUEEN_I = 16;
	
	//general risk modifiers
	public static final int W_RISK_MODIFIER_I = 17;
	public static final int W_PAWN_TRY_MODIFIER = 18;
	public static final int W_MOBILITY_MODIFIER = 19;
	
	
	public static final int MAX_INDEX = 20;
	
	public SimpleWeightSet()
	{
		weights[W_PAWN_V] = 1.6f; 
		weights[W_KNIGHT_V] = 2.8f; 
		weights[W_BISHOP_V] = 3.0f; 
		weights[W_ROOK_V] = 4.0f; 
		weights[W_QUEEN_V] = 9.0f; 
		
		/*weights[W_PAWN_I] = 1.03f; 
		weights[W_KNIGHT_I] = 1.03f; 
		weights[W_BISHOP_I] = 1.1f; 
		weights[W_ROOK_I] = 1.1f; 
		weights[W_QUEEN_I] = 1.1f; */
		
		weights[W_PAWN_I] = 1.00f; 
		weights[W_KNIGHT_I] = 1.00f; 
		weights[W_BISHOP_I] = 1.0f; 
		weights[W_ROOK_I] = 1.0f; 
		weights[W_QUEEN_I] = 1.0f; 
		
		weights[W_PASSED_PAWN_I] = 1.4f; 
		weights[W_ADJACENT_PAWN_I] = 1.2f; 
		weights[W_ADVANCEMENT_PAWN_I] = 1.15f; 
		weights[W_MAX_MOVES_KNIGHT_I] = 1.2f; 
		weights[W_CENTRAL_BISHOP_I] = 1.01f; 
		weights[W_CENTRAL_ROOK_I] = 1.01f; 
		weights[W_CENTRAL_QUEEN_I] = 1.01f; 
		
		//weights[W_RISK_MODIFIER_I] = 0.65f;
		weights[W_RISK_MODIFIER_I] = 0.85f;
		weights[W_PAWN_TRY_MODIFIER] = 500.0f;
		weights[W_MOBILITY_MODIFIER] = 0.025f;
	}
	
	public SimpleWeightSet evolve(float percentage, float amount)
	{
		SimpleWeightSet s = new SimpleWeightSet();
		System.arraycopy(weights,0,s.weights,0,MAX_INDEX-1);
		
		for (int k=0; k<MAX_INDEX; k++)
		{
			//change the weight?
			if (r.nextFloat()<=percentage)
			{
				float actualAmount = amount * r.nextInt(1000) / 1000.0f;
				actualAmount += 1.0f;
				if (r.nextBoolean()) actualAmount = 1.0f / actualAmount;
				s.weights[k] *= actualAmount;
			}
		}
		
		return s;
	}
	
	public void blend(SimpleWeightSet s, float intensity)
	{
		if (intensity<0.0f || intensity>1.0f) return;
		for (int k=0; k<MAX_INDEX; k++)
		{
			weights[k] = s.weights[k]*intensity + weights[k]*(1.0f-intensity);
		}
	}
	
	public String toString()
	{
		String result = "";
		for (int k=0; k<MAX_INDEX; k++)
		{
			result+=weights[k]+"\n";
		}
		return result;
	}

}
