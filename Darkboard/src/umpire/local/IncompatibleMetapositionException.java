package umpire.local;

import core.Metaposition;

public class IncompatibleMetapositionException extends Exception {
	
	Metaposition m1, m2;
	
	public IncompatibleMetapositionException(Metaposition mp1, Metaposition mp2)
	{
		m1 = mp1; m2 = mp2;
	}

}
