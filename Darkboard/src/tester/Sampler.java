package tester;

public class Sampler {
	
	public class Sample
	{
		double accumulatedValue = 0.0;
		int samples = 0;
		
		double getValue()
		{
			return (accumulatedValue/samples);
		}
		
		void addSample(double val)
		{
			accumulatedValue += val;
			samples++;
		}
		
		public String toString()
		{
			return (""+getValue());
		}
	}
	
	Sample samples[];
	
	public Sampler(int sampleSet)
	{
		samples = new Sample[sampleSet];
		for (int k=0; k<sampleSet; k++) samples[k] = new Sample();
	}
	
	public void addSample(int k, double val)
	{
		if (k>=0 && k<samples.length) samples[k].addSample(val);
	}
	
	public String toString()
	{
		String s = "Samples: "+samples[0].samples+"\n";
		for (int k=0; k<samples.length; k++) s+= samples[k].toString()+"\n";
		return s;
	}

}
