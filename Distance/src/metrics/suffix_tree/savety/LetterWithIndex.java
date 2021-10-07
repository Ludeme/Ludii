package metrics.suffix_tree.savety;

import java.io.Serializable;

import metrics.suffix_tree.Letter;

public class LetterWithIndex implements Serializable
{
	private static final long serialVersionUID = -4233119346421627381L;
	
	Letter l;
	int globalIndex;
}
