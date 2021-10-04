package processing.visualisation_3d_scatter_plot;

import java.util.HashMap;
import java.util.LinkedList;

import common.LudRul;
import processing.visualisation_3d_scatter_plot.WrapperClass.Pos;

public class WrapperClassData
{
	private double temperature;
	private double step;
	private HashMap<LudRul, Pos> bestPossition;
	private LinkedList<Boolean> improvementsLongTerm;
	private int improvementsLongTermCounter;
	private LinkedList<Boolean> acceptedsLongTerm;
	private int acceptedsLongTermCounter;
	private double longTermAvgImprovemt;
	private LinkedList<Boolean> improvementsShortTerm;
	private int improvementsShortTermCounter;
	private LinkedList<Boolean> acceptedsShortTerm;
	private int acceptedsShortTermCounter;
	private double shortTermAvgImprovemt;
	private double longTermAcceptensRate;
	private double longTermImprovementRate;
	private double shortTermAcceptensRate;
	private double shortTermImprovementRate;

	public WrapperClassData(
			double temperature, double step,
			LinkedList<Boolean> improvementsLongTerm,
			int improvementsLongTermCounter,
			LinkedList<Boolean> acceptedsLongTerm, int acceptedsLongTermCounter,
			LinkedList<Boolean> improvementsShortTerm,
			int improvementsShortTermCounter,
			LinkedList<Boolean> acceptedsShortTerm,
			int acceptedsShortTermCounter, double longTermAcceptensRate,
			double longTermImprovementRate, double shortTermAcceptensRate,
			double shortTermImprovementRate
	)
	{
		this.temperature = temperature;
		this.step = step;
		this.improvementsLongTerm = improvementsLongTerm;
		this.improvementsLongTermCounter = improvementsLongTermCounter;
		this.acceptedsLongTerm = acceptedsLongTerm;
		this.acceptedsLongTermCounter = acceptedsLongTermCounter;
		this.improvementsShortTerm = improvementsShortTerm;
		this.improvementsShortTermCounter = improvementsShortTermCounter;
		this.acceptedsShortTerm = acceptedsShortTerm;
		this.acceptedsShortTermCounter = acceptedsShortTermCounter;
		this.longTermAcceptensRate = longTermAcceptensRate;
		this.longTermImprovementRate = longTermImprovementRate;
		this.shortTermAcceptensRate = shortTermAcceptensRate;
		this.shortTermImprovementRate = shortTermImprovementRate;
	}

	public double getTemperature()
	{
		return temperature;
	}

	public void setTemperature(double temperature)
	{
		this.temperature = temperature;
	}

	public double getStep()
	{
		return step;
	}

	public void setStep(double step)
	{
		this.step = step;
	}

	public HashMap<LudRul, Pos> getBestPossition()
	{
		return bestPossition;
	}

	public void setBestPossition(HashMap<LudRul, Pos> bestPossition)
	{
		this.bestPossition = bestPossition;
	}

	public LinkedList<Boolean> getImprovementsLongTerm()
	{
		return improvementsLongTerm;
	}

	public void setImprovementsLongTerm(
			LinkedList<Boolean> improvementsLongTerm
	)
	{
		this.improvementsLongTerm = improvementsLongTerm;
	}

	public int getImprovementsLongTermCounter()
	{
		return improvementsLongTermCounter;
	}

	public void setImprovementsLongTermCounter(int improvementsLongTermCounter)
	{
		this.improvementsLongTermCounter = improvementsLongTermCounter;
	}

	public LinkedList<Boolean> getAcceptedsLongTerm()
	{
		return acceptedsLongTerm;
	}

	public void setAcceptedsLongTerm(LinkedList<Boolean> acceptedsLongTerm)
	{
		this.acceptedsLongTerm = acceptedsLongTerm;
	}

	public int getAcceptedsLongTermCounter()
	{
		return acceptedsLongTermCounter;
	}

	public void setAcceptedsLongTermCounter(int acceptedsLongTermCounter)
	{
		this.acceptedsLongTermCounter = acceptedsLongTermCounter;
	}

	public double getLongTermAvgImprovemt()
	{
		return longTermAvgImprovemt;
	}

	public void setLongTermAvgImprovemt(double longTermAvgImprovemt)
	{
		this.longTermAvgImprovemt = longTermAvgImprovemt;
	}

	public LinkedList<Boolean> getImprovementsShortTerm()
	{
		return improvementsShortTerm;
	}

	public void setImprovementsShortTerm(
			LinkedList<Boolean> improvementsShortTerm
	)
	{
		this.improvementsShortTerm = improvementsShortTerm;
	}

	public int getImprovementsShortTermCounter()
	{
		return improvementsShortTermCounter;
	}

	public void setImprovementsShortTermCounter(
			int improvementsShortTermCounter
	)
	{
		this.improvementsShortTermCounter = improvementsShortTermCounter;
	}

	public LinkedList<Boolean> getAcceptedsShortTerm()
	{
		return acceptedsShortTerm;
	}

	public void setAcceptedsShortTerm(LinkedList<Boolean> acceptedsShortTerm)
	{
		this.acceptedsShortTerm = acceptedsShortTerm;
	}

	public int getAcceptedsShortTermCounter()
	{
		return acceptedsShortTermCounter;
	}

	public void setAcceptedsShortTermCounter(int acceptedsShortTermCounter)
	{
		this.acceptedsShortTermCounter = acceptedsShortTermCounter;
	}

	public double getShortTermAvgImprovemt()
	{
		return shortTermAvgImprovemt;
	}

	public void setShortTermAvgImprovemt(double shortTermAvgImprovemt)
	{
		this.shortTermAvgImprovemt = shortTermAvgImprovemt;
	}

	public double getLongTermAcceptensRate()
	{
		return longTermAcceptensRate;
	}

	public void setLongTermAcceptensRate(double longTermAcceptensRate)
	{
		this.longTermAcceptensRate = longTermAcceptensRate;
	}

	public double getLongTermImprovementRate()
	{
		return longTermImprovementRate;
	}

	public void setLongTermImprovementRate(double longTermImprovementRate)
	{
		this.longTermImprovementRate = longTermImprovementRate;
	}

	public double getShortTermAcceptensRate()
	{
		return shortTermAcceptensRate;
	}

	public void setShortTermAcceptensRate(double shortTermAcceptensRate)
	{
		this.shortTermAcceptensRate = shortTermAcceptensRate;
	}

	public double getShortTermImprovementRate()
	{
		return shortTermImprovementRate;
	}

	public void setShortTermImprovementRate(double shortTermImprovementRate)
	{
		this.shortTermImprovementRate = shortTermImprovementRate;
	}
}