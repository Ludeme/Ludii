package ai.mc;

public class MCStrategyNode extends MCNode {
	
	public static final double UCT_CONSTANT_C = 0.5;
	public static final double WEIGHT_DECAY = 0.5;
	
	double timesCompleted = 0.0;
	MCNode bestImplementingNode; //lower tier node implementing this best
	MCStrategy strategy;


	
	public MCStrategyNode(MCStrategy s, MCStrategyNode cus, MCStrategyNode bcus)
	{
		strategy = s;
		currentUpperStrategy = cus;
		beginningOfCurrentUpperStrategy = bcus;
		nextStrategy = null;
	}
	
	//public boolean needsExpansion() { return (!expanded); }
	
	public MCNode select() 
	{
		//if (currentUpperStrategy==null) return this;
		
		if (timesCompleted<=0.0 && parent!=null) return this; //do not look further if you never got past here anyways.
		
		if (parent!=null && currentUpperStrategy!=null && strategyComplete())
		{
			currentUpperStrategy = currentUpperStrategy.nextStrategy;
			beginningOfCurrentUpperStrategy = this;
			if (currentUpperStrategy==null) return this;
		}
		
		MCStrategy cont[] = (currentUpperStrategy!=null && currentUpperStrategy.strategy!=null? currentUpperStrategy.strategy.continuations(getCurrentStrategyProgress()) : null);
		MCLink nodes[] = links;
		int best = -1;
		double bestval = Double.NEGATIVE_INFINITY;
		
		if (cont!=null)
		{
			nodes = new MCLink[cont.length];
			
			int additional = 0;
			for (int k=0; k<cont.length; k++)
			{
				MCStrategy s = cont[k];
				boolean exists = false;
				if (links!=null)
					for (int j=0; j<links.length; j++)
						if (links[j].child!=null && ((MCStrategyNode)links[j].child).strategy.equals(s)) 
						{ 
							nodes[k] = links[j];
							exists = true; break; 
						}
				if (exists) cont[k] = null; else additional++;
			}
			if (additional>0)
			{
				
				if (links==null)
				{
					int k=0;
					links = new MCLink[additional];
					for (int j=0; j<cont.length; j++)
						if (cont[j]!=null) 
						{
							MCLink m = new MCLink(this);
							m.setChild(new MCStrategyNode(cont[j],null,null));
							links[k++] = m;
							nodes[j] = m;
						}
				} else
				{
					MCLink l[] = new MCLink[links.length+additional];
					System.arraycopy(links, 0, l, 0, links.length);
					int k=links.length; links = l;
					for (int j=0; j<cont.length; j++)
					{
						if (cont[j]!=null) 
						{
							MCLink m = new MCLink(this);
							m.setChild(new MCStrategyNode(cont[j],null,null));
							links[k++] = m;
							nodes[j] = m;
						}
					}
				}
			}
		}
		double log = Math.log(visits+1);
		if (nodes==null)
		{
			return this;
		}
		for (int k=0; k<nodes.length; k++)
		{
			MCNode n = nodes[k].child;
			double visits = (n==null? 1.0 : n.visits+1);
			double v = (n==null? this.value : n.value);
			
			double val = links[k].heuristicBias+v+UCT_CONSTANT_C*Math.sqrt(log/visits);
			if (val>bestval)
			{
				bestval = val;
				best = k;
			}
		}
		
		if (best<0) return this;
		
		MCStrategyNode b = (MCStrategyNode)nodes[best].child;
		if (b==null) return this;
		
		this.nextStrategy = b;
		b.currentUpperStrategy = this.currentUpperStrategy;
		b.beginningOfCurrentUpperStrategy = this.beginningOfCurrentUpperStrategy;
		
		return b.select(); 
	}

	
	
	public MCNode expand() { return null; }
	
	
	public MCStrategy[] getCurrentStrategyProgress()
	{
		int size = 1;
		if (beginningOfCurrentUpperStrategy!=null && ((MCStrategyNode)beginningOfCurrentUpperStrategy).strategy==null) size--;
		MCStrategyNode mc = this;
		while (mc!=null && mc!=beginningOfCurrentUpperStrategy)
		{
			mc = (MCStrategyNode)mc.parent;
			size++;
		}
		
		MCStrategy[] out = new MCStrategy[size];
		if (size==0) return out;
		int k=out.length;
		mc = this; out[k-1] = this.strategy;
		while (mc!=null && mc!=beginningOfCurrentUpperStrategy)
		{
			k--;
			mc = (MCStrategyNode)mc.parent;
			out[k] = mc.strategy;
		}
		return out;
	}
	
	public boolean strategyComplete()
	{
		if (currentUpperStrategy==null || beginningOfCurrentUpperStrategy==null) return true;
		
		if (currentUpperStrategy.strategy==null) return false;
		
		MCStrategy prog[] = getCurrentStrategyProgress();
		
		MCStrategy cont[] = currentUpperStrategy.strategy.continuations(prog);
		
		//if continuations include null, it means this is (also) a final state.
		if (cont==null)
		{
			return false;
		}
		for (int k=0; k<cont.length; k++) if (cont[k]==null) return true;
		
		return false;
	}
	
	public void upPropagation(MCNode where, boolean completed, boolean alsoBack, double value)
	{
		if (completed) 
		{
			timesCompleted += 1.0;
			if (bestImplementingNode==null) bestImplementingNode = where;
			else
			{
				if (bestImplementingNode.value<where.value) bestImplementingNode = where;
			}
		}
		
		if (alsoBack) this.backpropagate(value, 1.0);
		if (currentUpperStrategy!=null) currentUpperStrategy.upPropagation(this, completed && strategyComplete(), alsoBack, value);
		
	}
	
	public void backpropagate(double d, double weight)
	{
		value = (value*visits+d*weight)/(visits+weight);
		visits+=1.0;
		if (parent!=null) 
		{
			parent.backpropagate(d,weight*WEIGHT_DECAY);
		}
	}

}
