package main.grammar;

import java.util.List;

/**
 * Convenience class containing ludeme info to store in database.
 * @author cambolbro and Matthew.Stephenson
 */
public class LudemeInfo
{
	/** Ludeme's unique id in the database. 0 means unassigned. */
	private int id = 0;
	
	/** 
	 * Grammar symbol corresponding to this ludeme. 
	 * Use symbol.keyword() to get this ludeme's token as used in game descriptions.
	 * Use symbol.grammarLabel() to get the minimally scoped token used in the grammar.
	 * Use symbol.cls() to get class that generated this ludeme.
	 * Use symbol.type() to get the ludeme type: Ludeme/SuperLudeme/Constant/Predefined/Primitive
	 */
	private final Symbol symbol;

	/** Short description of ludeme (maybe taken from JavaDoc). */
	private String description = "";
	
	private String packagePath = "";
	
	//-------------------------------------------------------------------------
	
	public LudemeInfo(final Symbol symbol)
	{
		this.symbol = symbol;
		packagePath = symbol.cls().getName();
	}
	
	//-------------------------------------------------------------------------

	public int id()
	{
		return id;
	}

	public void setId(final int id)
	{
		this.id = id;
	}

	public Symbol symbol()
	{
		return symbol;
	}

	public String description()
	{
		return description;
	}

	public void setDescription(final String description)
	{
		this.description = description;
	}

	//-------------------------------------------------------------------------

	public String getDBString()
	{
		return 	symbol.name() + "," + 
				packagePath + "," + 
				symbol.ludemeType() + "," + 
				symbol.token() + "," + 
				symbol.grammarLabel() + "," + 
				(symbol.usedInGrammar() ? 1 : 0) + "," + 
				(symbol.usedInMetadata() ? 1 : 0) + "," + 
				(symbol.usedInDescription() ? 1 : 0) + ",\"" + 
				description + "\"";
	}
	
	//-------------------------------------------------------------------------
	
	/** 
	 * @return First LudemeInfo in list that matches a Call object.
	 */
	public static LudemeInfo findLudemeInfo
	(
		final Call call, final List<LudemeInfo> ludemes
	)
	{
		// Pass 1: Check for special constants
		if (call.constant() != null)
			for (final LudemeInfo ludemeInfo : ludemes)
				if (call.constant().equals(ludemeInfo.symbol().name()))
					return ludemeInfo; 
		
		// Pass 2: Check for exact match
		for (final LudemeInfo ludemeInfo : ludemes)
			if (ludemeInfo.symbol().path().equals(call.symbol().atomicLudeme().path()))
				return ludemeInfo;
		
		// **
		// ** TODO: Check that closest compatible type is returned, i.e. 
		// **       superclass, else its superclass, else its superclass, ...?
		// **
		// Pass 3: Check for compatible type
//		for (final LudemeInfo ludemeInfo : ludemes)
//			if (ludemeInfo.symbol().compatibleWith(call.symbol()))
//				return allParentLudemes(ludemeInfo, ludemes);
	
//		// Pass 3: Check for primitive wrappers
//		for (final LudemeInfo ludemeInfo : ludemes)
//			if 
//			(
//				(
//					(
//						call.symbol().name().equals("Integer") 
//						|| 
//						call.symbol().name().equals("IntConstant") 
//						|| 
//						call.symbol().name().equals("DimConstant")
//					) 
//					&& 
//					ludemeInfo.symbol().name().equals("int")
//				)
//				||
//				(
//					(
//						call.symbol().name().equals("Boolean") 
//						|| 
//						call.symbol().name().equals("BooleanConstant") 
//					) 
//					&& 
//					ludemeInfo.symbol().name().equals("boolean")
//				)
//				||
//				(
//					(
//						call.symbol().name().equals("Float") 
//						|| 
//						call.symbol().name().equals("FloatConstant") 
//					) 
//					&& 
//					ludemeInfo.symbol().name().equals("float")
//				)
//			)
//				return allParentLudemes(ludemeInfo, ludemes);		
//		
//		// Pass 4: Check the return type (only for specific cases like Graph and Dim)
//		final List<LudemeInfo> ludemeInfoFound = new ArrayList<>();
//		for (final LudemeInfo ludemeInfo : ludemes)
//			if (ludemeInfo.symbol().returnType().equals(call.symbol().returnType()))
//				ludemeInfoFound.add(ludemeInfo);
//		if (ludemeInfoFound.size() > 1)
//			System.out.println(ludemeInfoFound);
//		if (ludemeInfoFound.size() == 1)
//			return allParentLudemes(ludemeInfoFound.get(0), ludemes);
	
//		System.out.println("\nERROR! Matching Ludeme not found.");
//		//System.out.println("Call: " + call.toString().strip());
//		System.out.println("Symbol name: " + call.symbol().name());
//		System.out.println("Symbol path: " + call.symbol().path());
//		System.out.println("Symbol token: " + call.symbol().token());
//		System.out.println("Symbol return type: " + call.symbol().returnType());
//		
//		System.out.println(call);
		
		return null;
	}
	
	//-------------------------------------------------------------------------
	
//	@SuppressWarnings("unused")
//	public static Set<LudemeInfo> allParentLudemes(final LudemeInfo ludemeinfo, final List<LudemeInfo> ludemeInfos)
//	{
//		final Set<LudemeInfo> parentLudemes = new HashSet<>();
//		parentLudemes.add(ludemeinfo);
//		
//		// JUST A TEMPORARY CHECK FOR CAMERON
////		if (ludemeinfo.symbol().name().equals("N"))
////			System.out.println(ludemeinfo.symbol().ancestors());
//		
////		// Add this symbol's ancestors to the list
////		for (final Symbol ancestor : ludemeinfo.symbol().ancestors())
////			for (final LudemeInfo info : ludemeInfos)
////				if (info.symbol().equals(ancestor))
////					parentLudemes.add(info);
//		
//		return parentLudemes;
//	}
	
}
