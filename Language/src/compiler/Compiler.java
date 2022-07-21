package compiler; 

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import compiler.exceptions.CantDecomposeException;
import compiler.exceptions.CompilerErrorWithMessageException;
import compiler.exceptions.CompilerException;
import compiler.exceptions.CreationErrorWithMessageException;
import compiler.exceptions.NullGameException;
import grammar.Grammar;
import main.grammar.Call;
import main.grammar.Call.CallType;
import main.grammar.Description;
import main.grammar.Report;
import main.grammar.Symbol;
import main.grammar.Symbol.LudemeType;
import main.grammar.Token;
import main.options.UserSelections;
import parser.Parser;

//-----------------------------------------------------------------------------

/**
 * Compiles game descriptions in the Ludii class grammar format to executable 
 * Game objects, if possible.
 * 
 * @author cambolbro
 */
public class Compiler
{		
	// Avoid warning about failure to close resource
	private static ClassLoader classLoader;

	//-------------------------------------------------------------------------

	/**
	 * Default private constructor; can't be called directly.
	 */
	private Compiler()
	{
	}

	//-------------------------------------------------------------------------

	/**
	 * Compile option for testing purposes. Does not interact with UI settings. Not
	 * for release! Create and throws away GameOptions each time.
	 * 
	 * Cast the returned object to (Game) in order to use it!
	 * 
	 * @param description The description.
	 * @param isVerbose   True if this is verbose.
	 * @return Executable Game object if can be compiled, else null.
	 */
	public static Object compileTest
	(
		final Description description, 
		final boolean     isVerbose
	)
	{
		final Object game = 	compile
							(
								description, 
								new UserSelections(new ArrayList<String>()), 
								new Report(),
								isVerbose
							);
		if (game == null)
			System.out.println("** Compiler.compileTest(): Game compiled but returned null after initialisation.");
		
		return game;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Cast the returned object to (Game) in order to use it!
	 * 
	 * @param description    The description.
	 * @param userSelections The user selections.
	 * @param report         The report.
	 * @param isVerbose      True if this is verbose.
	 * @return Executable Game object if can be compiled, else null.
	 */
	public static Object compile
	(
		final Description    description,
		final UserSelections userSelections,
		final Report         report,
		final boolean        isVerbose
	)
	{
		try
		{
			return compileActual(description, userSelections, report, isVerbose);
		} 
		catch (final CompilerException e)
		{
			//if (isVerbose)
				e.printStackTrace();
			throw new CompilerException(e.getMessageBody(description.raw()), e);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Compiled Game object else null.  
	 */
	private static Object compileActual
	(
		final Description    description,
		final UserSelections userSelections,
		final Report         report,
		final boolean        isVerbose
	) 
	{
		if (isVerbose)
		{
			//System.out.println("+++++++++++++++++++++\nCompiling:\n" + description.raw());
			report.addLogLine("+++++++++++++++++++++\nCompiling:\n" + description.raw());
		}

//		System.out.println("\nAt Compiler.compiler(), current option selections are:\n  " 
//							+ options.currentSelectionsAsString() + " => " + options.toStrings());

		//final Parser parser = Parser.getParser();  //new Parser();

//		if (!parser.parse(description, userSelections, isVerbose))
//			throw new CompilerErrorWithMessageException("Failed to parse game description.");
		Parser.expandAndParse(description, userSelections, report, true, isVerbose);	
		if (report.isError())
		{
			System.out.println("Failed to parse game description:");
			for (final String error : report.errors())
			{
				System.out.println("* " + error);
				report.addLogLine("* " + error);
			}
					
			final StringBuilder sb = new StringBuilder();
			
			//sb.append(description.expanded());
			
			for (final String error : report.errors())
				sb.append(error + "\n");
			
			for (final String warning : report.warnings())
				sb.append("Warning: " + warning + "\n");
			
			for (final String note : report.notes())
				sb.append("Note: " + note + "\n");
			
			throw new CompilerErrorWithMessageException(sb.toString());
			//throw new RuntimeException(sb.toString());
//			return null;
		}
		
//		System.out.println("Expanded description:\n" + gameDescription.expandedDescription());
//		System.out.println("Metadata description:\n" + gameDescription.metadataDescription());
		
//		boolean isMatch = false;
//		final int matchAt = gameDescription.expandedDescription().indexOf("(match");
//		if (matchAt >= 0)
//		{
//			final char ch = gameDescription.expandedDescription().charAt(matchAt + 6);
//			if (!StringRoutines.isTokenChar(ch))  // avoid superstrings e.g. "(matchScore"
//				isMatch = true;  
//		}
		
		//Game game = null;
//		Match match = null;
//		if (isMatch)
//		{
//			// Compile match
//			match = (Match)compileTask
//					(
//						gameDescription.expandedDescription(), "match", "game.Match", isVerbose
//					);
//		}
//		else
//		{
			final Object game = compileTask
								(
									description.expanded(), 
									"game", 
									"game.Game", 
									report, 
									isVerbose, 
									description
								);
			if (game == null)
			{
				System.out.println("** Compiler.compiler(): Could not compile game.");
				return null;
			}
				
			try
			{
				// If the report object has a messenger attached, print this message using it.
//				final Method gameName = game.getClass().getMethod("name");
//			
//				if (report.getReportMessageFunctions() != null)
//					report.getReportMessageFunctions().printMessageInStatusPanel
//				(
////					"Compiled " + game.name() + " successfully.\n"
//					"Compiled " + gameName.invoke(game) + " successfully.\n"
//				);
			
				// Associate the game with its description
				//game.setDescription(description);	
				final Method gameSetDescription = game.getClass().getMethod("setDescription", Description.class);
				gameSetDescription.invoke(game, description);
			} 
			catch (final Exception e)
			{
				e.printStackTrace();
			} 

			// Check that the game can be created
			try 
			{
				//game.create();
				final Method gameCreate = game.getClass().getMethod("create");
				gameCreate.invoke(game);
			}
			catch (final Error e)
			{
				String msg = "Error during game creation: " + e;
				final boolean isStackOverflow = e.getClass().getName().contains("StackOverflowError");
				if (isStackOverflow)
				{
					msg = 	"Error: Stack overflow during game creation.\n" + 
							"Check for recursive rules, e.g. (forEach Piece ...) within a piece.";
				}
				report.addError(msg);
				throw new CreationErrorWithMessageException(msg);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				final String msg = "Exception during game creation: " + e;
				report.addError(msg);
				throw new CreationErrorWithMessageException(msg);
			}
//		}		

//		System.out.println("Game created. Compiling metadata...");

		// Compile metadata
		//System.out.println("Compiling metadata string:\n" + parser.metadataString());
		Object md = null;
		if (description.metadata() != null && description.metadata() != "")
		{
			md = compileTask
				 (
				    description.metadata(), "metadata", "metadata.Metadata", report, isVerbose, null
				 );
		}	
		
		if (md == null)
		{
			//md = new Metadata(null, null, null);
			try
			{
				md = loadExternalClass
					 (
						"../Core/src/metadata/",
						"metadata.Metadata"
					 )
					 .getDeclaredConstructor()
					 .newInstance();
			} catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
		
		try
		{
			//game.setMetadata(md);
			final Method gameSetMetadata = game.getClass().getMethod("setMetadata", Object.class);
			gameSetMetadata.invoke(game, md);
		
			//game.setOptions(userSelections.selectedOptionStrings());
			final Method gameSetOptions = game.getClass().getMethod("setOptions", List.class);
			gameSetOptions.invoke(game, userSelections.selectedOptionStrings());
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
//		System.out.println("Game is: " + game.name());
//		System.out.print("Aliases are: ");
//		for (final String alias : game.metadata().info().getAliases())
//			System.out.print(" " + alias);
//		System.out.println();
				
//		if (game != null)
//			return game;
//		
//		return match;
		return game;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Compiles an object from an arbitrary class from given string (in ludeme-like
	 * format).
	 * 
	 * This method should not be used for full game objects, but only for smaller
	 * objects that may typically be found deeper inside Game or Metadata objects.
	 * 
	 * Does not support any more advanced mechanisms like options, defines, etc.
	 * 
	 * @param strIn
	 * @param className Fully qualified name of type of Object that we're expecting
	 * @param report    The report.
	 * @return Compiled object, or null if failed to compile.
	 */
	public static Object compileObject
	(
		final String strIn, 
		final String className,
		final Report report 
	)
	{
		final String[] classNameSplit = className.split(Pattern.quote("."));
		//final String symbolName = StringRoutines.lowerCaseInitial(classNameSplit[classNameSplit.length - 1]);
		final String symbolName = classNameSplit[classNameSplit.length - 1];
		
		return compileObject(strIn, symbolName, className, report);
	}
	
	/**
	 * Compiles an object from an arbitrary class from given string (in ludeme-like
	 * format).
	 * 
	 * This method should not be used for full game objects, but only for smaller
	 * objects that may typically be found deeper inside Game or Metadata objects.
	 * 
	 * Does not support any more advanced mechanisms like options, defines, etc.
	 * 
	 * @param strIn
	 * @param symbolName The symbol name (usually last part of className, or an
	 *                   alias)
	 * @param className  Fully qualified name of type of Object that we're expecting
	 * @param report     The report.
	 * @return Compiled object, or null if failed to compile.
	 */
	public static Object compileObject
	(
		final String strIn, 
		final String symbolName,
		final String className,
		final Report report
	)
	{		
		return compileTask(strIn, symbolName, className, report, false, null);
//		final Object result = compileTask(strIn, symbolName, className, false);
//		if (result == null)
//			compileTask(strIn, symbolName, className, true);
//		return result;
	}

	//-------------------------------------------------------------------------

	/**
	 * Compile either the Game object or the Metadata object.
	 * @return Compiled object, else null if error.
	 */
	private static Object compileTask
	(
		final String  strIn,	
		final String  symbolName, 
		final String  className, 
		final Report  report,
		final boolean isVerbose,
		final Description description
		
	) 
	{
//		/**
//		 * List of ludemes to ignore if found not to compile. These are typically
//		 * ludemes automatically generated as defaults for enclosing ludemes
//		 * that can but do not actually appear in the game description. 
//		 */
//		final Map<String, String> ignoreNonCompiledLudemes = new HashMap<String, String>();
//		{
////			ignoreNonCompiledLudemes.put("java.lang.Integer", "java.lang.Integer");
////			ignoreNonCompiledLudemes.put("java.lang.String",  "java.lang.String");
////			ignoreNonCompiledLudemes.put("game.rules.meta.Meta",    "game.rules.meta.Meta");
////			ignoreNonCompiledLudemes.put("game.util.moves.Player",  "game.util.moves.Player");
////			ignoreNonCompiledLudemes.put("game.util.moves.From",    "game.util.moves.From");
////			ignoreNonCompiledLudemes.put("game.util.moves.To",      "game.util.moves.To");
////			ignoreNonCompiledLudemes.put("game.util.moves.Between", "game.util.moves.Between");
////			ignoreNonCompiledLudemes.put("game.util.moves.Flips",   "game.util.moves.Flips");
////			ignoreNonCompiledLudemes.put("game.util.moves.Piece",   "game.util.moves.Piece");
////			ignoreNonCompiledLudemes.put("game.rules.start.Start",  "game.rules.start.Start");
////			ignoreNonCompiledLudemes.put("game.mode.Mode",          "game.mode.Mode");
////			ignoreNonCompiledLudemes.put("game.functions.intArray.state.Rotations", "game.functions.intArray.state.Rotations");
////			ignoreNonCompiledLudemes.put("metadata.graphics.Graphics",        "metadata.graphics.Graphics");
////			ignoreNonCompiledLudemes.put("metadata.ai.heuristics.Heuristics", "metadata.ai.heuristics.Heuristics");
////			ignoreNonCompiledLudemes.put("game.equipment.container.board.Track", "game.equipment.container.board.Track");
//			//ignoreNonCompiledLudemes.put("",  "");
//			//ignoreNonCompiledLudemes.put("",  "");
//			//ignoreNonCompiledLudemes.put("",  "");
//		};
	
		//System.out.println("parser.metadataString:\n" + parser.metadataString());
		
//		System.out.println("\nCompiling description starting: " + strIn.substring(0, 20));
//		System.out.println("symbolName is: " + symbolName);
		
		final Token tokenTree = new Token(strIn, report);
		if (isVerbose)
		{
			//System.out.println("\nCompiler.compileTask() token tree:\n" + tokenTree);
			report.addLogLine("\nCompiler.compileTask() token tree:\n" + tokenTree);
		}

		if (tokenTree.type() == null)
		{
			//System.out.println("** Compiler.compileTask(): Null token tree.");
			report.addLogLine("** Compiler.compileTask(): Null token tree.");
			
			if (symbolName.equals("game"))  // || symbolName.equals("match"))
				throw new CantDecomposeException("CompilercompileTask()");  // must have a game object!
			else
				return null;  // may be no metadata
		}
		
		final ArgClass rootClass = (ArgClass)Arg.createFromToken(Grammar.grammar(), tokenTree);
		if (rootClass == null)
			throw new NullGameException();

		if (isVerbose)
		{
			//System.out.println("\nRoot:" + rootClass);
			report.addLogLine("\nRoot:" + rootClass);
		}

		// Eric: commented for the match
//		if (!rootClass.symbolName().equals(symbolName))
//		{
//			System.out.println("rootClass is: " + rootClass.symbolName() + ", symbolName is: " + symbolName);
//			throw new BadRootException(rootClass.symbolName(), symbolName);
//		}

		final Grammar grammar = Grammar.grammar();
		if (!rootClass.matchSymbols(grammar, report))
		{
			System.out.println("Compiler.compileTask(): Failed to match symbols.");
			report.addLogLine("Compiler.compileTask(): Failed to match symbols.");
			
			throw new CompilerErrorWithMessageException("Failed to match symbols when compiling.");
		}

		// Attempt to compile the game
		Class<?> clsRoot = null;
		try
		{
			clsRoot = Class.forName(className);
		} catch (final ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		// Create call tree with dummy root
		Call callTree = (description == null) ? null : new Call(CallType.Null);
			
		final Map<String, Boolean> hasCompiled = new HashMap<>();
		
		final Object result = rootClass.compile(clsRoot, (isVerbose ? 0 : -1), report, callTree, hasCompiled);
				
		// Check fragments that did not compile
		for (final Map.Entry<String, Boolean> entry : hasCompiled.entrySet()) 
		{
			//System.out.println("key: " + entry.getKey());
			
			if (entry.getValue() == Boolean.TRUE)
				continue;  // item did compile
			
			final String path = entry.getKey();
			
//			if (ignoreNonCompiledLudemes.containsKey(path))
//				continue;  // ignore certain classes
			
			final Symbol symbol = grammar.findSymbolByPath(path);
			if (symbol == null)  // || !symbol.usedInDescription())
				continue;  // is probably a primitive type or structural <rule> ludeme

			if (symbol.ludemeType() == LudemeType.Structural)
				continue;  // don't include structural ludemes
			
			//System.out.println(path);
			//System.out.println("symbol: " + symbol + ", name: " + symbol.name());
			
//			final Boolean check = hasCompiled.get(symbol.returnType().cls().getName());
//			if (check == Boolean.TRUE)
//				continue;  // symbol's return type compiled successfully
			
			System.out.println("** Could not compile " + path + ".");
			
			report.addWarning("Could not compile " + path + ".");
			//report.addError("Could not compile " + path + ".");
			//report.addLogLine("Could not compile " + path + ".");
			
			if (report.getReportMessageFunctions() != null)
				report.getReportMessageFunctions().printMessageInStatusPanel("WARNING: Could not compile " + path + ".\n");
		}
		
		if (description != null)
		{
			// Remove dummy root from call tree and store it
			if (callTree == null || callTree.args().isEmpty())
				System.out.println("Compiler.compileTask: Bad call tree.");
	
			callTree = callTree.args().isEmpty() ? null : callTree.args().get(0);
			description.setCallTree(callTree);
			//System.out.println(description.callTree());
			
//			callTree.export("call-tree-" + ((Game)result).name() + ".txt");
		}
		
		if (isVerbose)
			System.out.println(report.log());
		
		if (result == null)
		{
			//System.out.println("Compiler.compileTask(): Null result from compiling root ArgClass object.");
			report.addLogLine("Compiler.compileTask(): Null result from compiling root ArgClass object.");
			throw new NullGameException();
		}
		
		//System.out.println("Compiled task:\n" + result.toString());

		return result;
	}

	//-------------------------------------------------------------------------
	
	public static Class<?> loadExternalClass(final String folderPath, final String classPath)
	{
		Class<?> cls = null;
		
		final String relPath = folderPath.replace('.', '/');
		
		// Create a File object on the root of the directory containing the class file
		final File file = new File(relPath);  //folderPath);
		
		try 
		{
			// Convert File to a URL
			final URL url = file.toURI().toURL();
			final URL[] urls = new URL[] { url };

			classLoader = new URLClassLoader(urls, Compiler.class.getClassLoader());

			// Load in the class; MyClass.class should be located in
			// the directory file:/c:/myclasses/com/mycompany
			cls = classLoader.loadClass(classPath);
		} 
		catch (final MalformedURLException e) 
		{
			e.printStackTrace();
		} 
		catch (final ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		return cls;
	}
	
	//-------------------------------------------------------------------------
	
}
