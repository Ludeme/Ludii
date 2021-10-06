package metrics.suffix_tree.savety;

import java.io.Serializable;

import metrics.suffix_tree.Letter;

public class LetterWithIndex implements Serializable
{
	Letter l;
	int globalIndex;
}
