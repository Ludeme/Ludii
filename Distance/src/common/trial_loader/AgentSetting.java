package common.trial_loader;

import java.util.List;

import other.AI;

public class AgentSetting
{
	public static AgentSetting random = new AgentSetting();
	
	
	private double thinkingTime;
	private final boolean isRandom;
	private String name;

	private AgentSetting() {
		isRandom = true;
	}
	private AgentSetting(final double thinkingTime) {
		this.thinkingTime = thinkingTime;
		isRandom = true;
		name = "random" + (int)thinkingTime + "ms";
	}
	
	public String getName()
	{
		return name;
	}

	public double getThinkingTime()
	{
		return thinkingTime;
	}

	public List<AI> getAIs(final int numPlayers)
	{
		if (isRandom) return null;
		
		//final ArrayList<AI> agents = new ArrayList<>();
		for (int i = 0; i < numPlayers; i++)
		{
			//agents.add(AIFactory.createAI("MCTS"));
			//no access to aifactory
		}
		return null;
	}

}
