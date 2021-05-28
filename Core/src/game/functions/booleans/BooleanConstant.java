package game.functions.booleans;

import annotations.Hide;
import game.Game;
import other.context.Context;

/**
 * Constant boolean value.
 * 
 * @author cambolbro, Dennis Soemers
 */
@Hide
public final class BooleanConstant extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	/** The boolean constant value. */
	private final boolean a;

	//-------------------------------------------------------------------------

//	/** Singleton which returns True. */
//	private static final TrueConstant TRUE_INSTANCE = new TrueConstant();
//	
//	/** Singleton which returns False. */
//	private static final FalseConstant FALSE_INSTANCE = new FalseConstant();
//
//	//-------------------------------------------------------------------------
//
//	/**
//	 * Construct function
//	 * 
//	 * @param a
//	 */
//	@SuppressWarnings("javadoc")
//	public static BaseBooleanFunction construct(final boolean a)
//	{
//		if (a)
//			return TRUE_INSTANCE;
//		else
//			return FALSE_INSTANCE;
//	}
//	
//	/**
//	 * Constructor. NOTE: should not be used anymore!
//	 * 
//	 * @param a
//	 */
//	private BooleanConstant(final boolean a)
//	{
//		this.a = a;
//	}
	
	/**
	 * @param a
	 */
	public BooleanConstant(final boolean a)
	{
		this.a = a;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		return a;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final String str = "" + a;
		return str;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return 0;
	}
	
	@Override
	public void preprocess(final Game game)
	{
		// nothing to do
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Constant boolean function returning True
	 * 
	 * @author Dennis Soemers
	 */
	public final static class TrueConstant extends BaseBooleanFunction
	{
		
		/** */
		private static final long serialVersionUID = 1L;

		//---------------------------------------------------------------------
	
		/**
		 * Constructor.
		 */
		TrueConstant()
		{
			// Do nothing
		}
	
		//---------------------------------------------------------------------
	
		@Override
		public boolean eval(final Context context)
		{
			return true;
		}
	
		//---------------------------------------------------------------------
	
		@Override
		public String toString()
		{
			final String str = "True";
			return str;
		}
	
		//---------------------------------------------------------------------
	
		@Override
		public boolean isStatic()
		{
			return true;
		}
	
		@Override
		public long gameFlags(final Game game)
		{
			return 0;
		}
		
		@Override
		public void preprocess(final Game game)
		{
			// nothing to do
		}
	}
	
	/**
	 * Constant boolean function returning False
	 * 
	 * @author Dennis Soemers
	 */
	public final static class FalseConstant extends BaseBooleanFunction
	{
		
		/** */
		private static final long serialVersionUID = 1L;

		//---------------------------------------------------------------------
	
		/**
		 * Constructor.
		 */
		FalseConstant()
		{
			// Do nothing
		}
	
		//---------------------------------------------------------------------
	
		@Override
		public boolean eval(final Context context)
		{
			return false;
		}
	
		//---------------------------------------------------------------------
	
		@Override
		public String toString()
		{
			final String str = "False";
			return str;
		}
	
		//---------------------------------------------------------------------
	
		@Override
		public boolean isStatic()
		{
			return true;
		}
	
		@Override
		public long gameFlags(final Game game)
		{
			return 0;
		}
		
		@Override
		public void preprocess(final Game game)
		{
			// nothing to do
		}
	}
}
