package main.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//-----------------------------------------------------------------------------

/**
 * Instance of a symbol which may be a compiled object (compile stage) or a list
 * of possible clauses (parse stage).
 */
public class Instance
{
	// Symbol corresponding to a token in the game definition.  
	// Tokens can map to multiple symbols.
	protected final Symbol symbol;
			
	// Possible clauses this symbol might match with.
	private List<Clause> clauses = null;

	// The compiled object (may be created from a class, enum constant, primitive or Java wrapper class). 
	protected Object object = null;

	/** Name of constant if derived from application constant, e.g. "Off", "End", "Repeat", ... */
	private final String constant;
	
	//-------------------------------------------------------------------------

	/** 
	 * Constructor for Compiler specifying actual object compiled.
	 */
	public Instance(final Symbol symbol, final Object object)
	{
		this.symbol   = symbol;
		this.object   = object;
		this.constant = null;
	}
	
	/** 
	 * Constructor for Compiler specifying actual object compiled.
	 */
	public Instance(final Symbol symbol, final Object object, final String constant)
	{
		this.symbol   = symbol;
		this.object   = object;
		this.constant = constant;
	}
	
	//-------------------------------------------------------------------------
	
	public Symbol symbol()
	{
		return symbol;
	}

	public List<Clause> clauses()
	{
		if (clauses == null)
			return null;
		return Collections.unmodifiableList(clauses);
	}
	
	public void setClauses(final List<Clause> list)
	{
		clauses = new ArrayList<Clause>();
		clauses.addAll(list);
	}
	
	public Object object()
	{
		return object;
	}
	
	public void setObject(final Object object)
	{
		this.object = object;
	}
	
	public String constant()
	{
		return constant;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Class for this symbol. Can assume it is unique over all symbols(?)
	 */
	public Class<?> cls()
	{
		if (symbol.cls() == null)
			System.out.println("** Instance: null symbol.cls() for symbol " + symbol.name() + ".");
		
		return symbol.cls();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		if (symbol == null)
			return "Unknown";
		
		return 	symbol.grammarLabel() 
				+ 
				(cls() == null ? " (null)" : ", cls: " + cls().getName())
				+ 
				(object == null ? " (null)" : ", object: " + object);
	}
	
	//-------------------------------------------------------------------------
	
}
