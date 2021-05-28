package help;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import annotations.Hide;
import gnu.trove.list.array.TIntArrayList;
import grammar.ClassEnumerator;
import grammar.Grammar;
import main.Constants;
import main.StringRoutines;
import main.grammar.Symbol;
import metadata.MetadataItem;
import other.Ludeme;

/**
 * Code to generate a help file for generating auto-complete etc. in Ludii's editor.
 *
 * @author Dennis Soemers
 */
public class GenerateLudiiEditorHelpFile
{
	private static final Grammar grammar = Grammar.grammar();
	
	private static final String HELP_FILE_NAME = "../Common/res/help/EditorHelp.txt";
	
	private static final String DEFS_DIR = "../Common/res/def";
	
	/** Map of pieces of text to replace in documentation */
	private static final Map<String, String> TEXT_TO_REPLACE = new HashMap<String, String>();
	
	/** All the game classes (under the game package) */
	private static List<Class<?>> gameClasses;
	
	/** All the metadata classes (under the metadata package) */
	private static List<Class<?>> metadataClasses;
	
	static
	{
		TEXT_TO_REPLACE.put("MAX_DISTANCE", "" + Constants.MAX_DISTANCE);
		
		TEXT_TO_REPLACE.put("\\\\url", "\\url");
		TEXT_TO_REPLACE.put("\\\\texttt{", "\\texttt{");
		
		TEXT_TO_REPLACE.put("$\\\\leq$", "<=");
		TEXT_TO_REPLACE.put("$\\\\geq$", ">=");
		TEXT_TO_REPLACE.put("$\\\\neq$", "$=/=");
		
		TEXT_TO_REPLACE.put("\\\\frac{", "\\frac{");
		TEXT_TO_REPLACE.put("\\\\exp(", "\\exp(");
		TEXT_TO_REPLACE.put("\\\\tanh", "\\tanh");
		
		try
		{
			gameClasses = ClassEnumerator.getClassesForPackage(Class.forName("game.Game").getPackage());
			metadataClasses = ClassEnumerator.getClassesForPackage(Class.forName("metadata.Metadata").getPackage());
		} 
		catch (final ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Main method
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void generateHelp() throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException
	{
		// First we'll read the XML file we generated
		final File inputFile = new File("../LudiiDocGen/out/xml/jel.xml");
		
		final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		final Document doc = dBuilder.parse(inputFile);
		final Element root = doc.getDocumentElement();
		
		final List<ClassTreeNode> rootPackages = new ArrayList<ClassTreeNode>();
		
		final List<Node> classNodes = xmlChildrenForName(root, "jelclass");
		for (int i = 0; i < classNodes.size(); ++i)
		{
			final Node child = classNodes.get(i);
			
			if (hasAnnotation(child, "Hide") || hasAnnotation(child, "ImpliedRule"))
				continue;
			
			final NamedNodeMap attributes = child.getAttributes();
			
			final String packageStr = attributes.getNamedItem("package").getTextContent();
			String type = attributes.getNamedItem("type").getTextContent();
			
			final String fullType = packageStr + "." + type.replaceAll(Pattern.quote("."), Matcher.quoteReplacement("$"));
			final Class<?> clazz = Class.forName(fullType);
			
			if (!Ludeme.class.isAssignableFrom(clazz) && !MetadataItem.class.isAssignableFrom(clazz) && !clazz.isEnum())
				continue;	// We skip classes that are neither Ludemes nor enums	
					
//			if ((clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) && !clazz.isEnum())	
//				continue;	// We skip interfaces and abstract classes
			
			if (type.contains("."))
			{
				if (!clazz.isEnum())
				{
					System.out.println("Skipping inner class type: " + fullType);
					continue;
				}
				else
				{
					// Need to get rid of outer class
					final String[] typeSplit = type.split(Pattern.quote("."));
					type = typeSplit[typeSplit.length - 1];
				}
			}
			
			boolean grammarSymbolUsed = false;
			boolean metadataSymbolUsed = false;
			final List<Symbol> grammarSymbols = grammar.symbolListFromClassName(type);

			if (grammarSymbols != null)
			{
				for (final Symbol symbol : grammarSymbols)
				{
					if (symbol.usedInDescription())
						grammarSymbolUsed = true;
					
					if (symbol.usedInGrammar())
						grammarSymbolUsed = true;
					
					if (symbol.usedInMetadata())
						metadataSymbolUsed = true;
					
					if (grammarSymbolUsed && metadataSymbolUsed)
						break;
				}
			}
			
			if (!grammarSymbolUsed && !metadataSymbolUsed && !MetadataItem.class.isAssignableFrom(clazz))
			{
				System.out.println("Ignoring type not used in grammar or metadata: " + fullType);
				continue;
			}
			
			final String[] subpackages = packageStr.split(Pattern.quote("."));
			
			// Find matching package in our list of root packages
			ClassTreeNode currentNode = null;
			List<ClassTreeNode> currentPackages = rootPackages;
			
			final List<String> fullTypeStrParts = new ArrayList<String>();
			
			for (int j = 0; j < subpackages.length; ++j)
			{
				boolean foundPackage = false;
				fullTypeStrParts.add(subpackages[j]);
				
				for (final ClassTreeNode pckg : currentPackages)
				{
					if (subpackages[j].equals(pckg.name))
					{
						currentNode = pckg;
						foundPackage = true;
						break;
					}
				}
				
				if (!foundPackage)
				{
					// Didn't find a match, so have to instantiate a new root package
					currentNode = new ClassTreeNode(subpackages[j], true, StringRoutines.join(".", fullTypeStrParts), clazz.isEnum(), child);
					currentPackages.add(currentNode);
				}
				
				currentPackages = currentNode.children;
			}
			
			// Create new node for this type
			fullTypeStrParts.add(type);
			currentPackages.add(new ClassTreeNode(type, false, attributes.getNamedItem("fulltype").getTextContent(), clazz.isEnum(), child));
		}
		
		// Try to nicely sort all packages; first classes in alphabetical order, then subpackages
		sortPackages(rootPackages);
		
		// Create StringBuilder to contain all our help info
		final StringBuilder sb = new StringBuilder();
		
		// Process all our packages in a depth-first manner
		for (final ClassTreeNode node : rootPackages)
		{
			process(node, sb);
		}
		
		// Also process all our def files
		processDefFiles(sb);
		
		// Postprocess our full string to fix type names,
		// and replace other keywords that need replacing
		while (true)
		{
			final int paramStartIdx = sb.indexOf("<PARAM:");

			if (paramStartIdx < 0)	// we're done
				break;

			final int paramEndIdx = sb.indexOf(">", paramStartIdx);
			String paramTypeStr = sb.substring(paramStartIdx, paramEndIdx);
			paramTypeStr = paramTypeStr.substring("<PARAM:".length(), paramTypeStr.length());
			if (paramTypeStr.endsWith("[]"))
				paramTypeStr = paramTypeStr.substring(0, paramTypeStr.length() - "[]".length());
			final String[] paramTypeStrSplit = paramTypeStr.split(Pattern.quote("."));

			final String paramTypeText = typeString(StringRoutines.lowerCaseInitial(paramTypeStrSplit[paramTypeStrSplit.length - 1]));

			sb.replace(paramStartIdx, paramEndIdx + 1, paramTypeText);
		}
		
		// Remove \texttt{} LaTeX commands
		while (true)
		{
			final int textttStartIdx = sb.indexOf("\\texttt{");
			
			if (textttStartIdx < 0)	// we're done
				break;

			final int textttEndIdx = sb.indexOf("}", textttStartIdx);
			sb.replace
			(
				textttStartIdx, 
				textttEndIdx + 1, 
				sb.substring(textttStartIdx + "\\texttt{".length(), textttEndIdx)
			);
		}

		for (final String key : TEXT_TO_REPLACE.keySet())
		{
			while (true)
			{
				final int startIdx = sb.indexOf(key);

				if (startIdx < 0)	// we're done
					break;

				final int endIdx = startIdx + key.length();
				sb.replace(startIdx, endIdx, TEXT_TO_REPLACE.get(key));
			}
		}
		
		// Write our help file
		final File outFile = new File(HELP_FILE_NAME);
		outFile.getParentFile().mkdirs();
		System.out.println("Writing file: " + outFile.getCanonicalPath());
		try (final PrintWriter writer = new PrintWriter(outFile))
		{
			writer.write(sb.toString());
		}
	}
	
	/**
	 * Processes the given node
	 * @param node
	 * @param sb
	 * @throws ClassNotFoundException 
	 */
	private static void process(final ClassTreeNode node, final StringBuilder sb) throws ClassNotFoundException
	{
		if (node.isPackage)
		{
			for (final ClassTreeNode child : node.children)
			{
				process(child, sb);
			}
		}
		else
		{
			if (node.isEnum)
				processEnumNode(node, sb);
			else
				processClassNode(node, sb);
		}
	}
	
	/**
	 * Processes the given class node
	 * @param node
	 * @param sb
	 * @throws ClassNotFoundException 
	 */
	private static void processClassNode(final ClassTreeNode node, final StringBuilder sb) throws ClassNotFoundException
	{
		// We're starting a new class
		final Class<?> clazz = Class.forName(node.fullType);
		final String ludemeName = StringRoutines.lowerCaseInitial(node.name);
		
		appendLine(sb, "TYPE: " + node.fullType);
		
		// Handle @Alias annotation
		String unformattedAlias = null;
		if (hasAnnotation(node.xmlNode, "Alias"))
		{
			final Node annotationsNode = xmlChildForName(node.xmlNode, "annotations");
			
			if (annotationsNode != null)
			{
				final List<Node> annotationNodes = xmlChildrenForName(annotationsNode, "annotation");
				for (final Node annotationNode : annotationNodes)
				{
					final String annotationType = annotationNode.getAttributes().getNamedItem("type").getTextContent();
	
					if (annotationType.equals("Alias"))
					{
						final Node valuesNode = xmlChildForName(annotationNode, "values");
						final Node valueNode = xmlChildForName(valuesNode, "value");
						unformattedAlias = valueNode.getAttributes().getNamedItem("value").getTextContent();
						break;
					}
				}
			}
		}

		// Write the comments on top of class
		final Node classCommentNode = xmlChildForName(node.xmlNode, "comment");
		if (classCommentNode != null)
		{
			final Node descriptionNode = xmlChildForName(classCommentNode, "description");
			if (descriptionNode != null)
				appendLine(sb, "TYPE JAVADOC: " + descriptionNode.getTextContent().replaceAll(Pattern.quote("\n"), " ").trim());
		}
		
		if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()))	
		{
			// Interface or abstract class
			final List<Class<?>> potentialSubclasses;
			if (node.fullType.startsWith("game"))
			{
				potentialSubclasses = gameClasses;
			}
			else if (node.fullType.startsWith("metadata"))
			{
				potentialSubclasses = metadataClasses;
			}
			else
			{
				potentialSubclasses = null;
				System.err.println("Interface / abstract class starts with neither game nor metadata!");
			}
			
			final List<String> subclassTypes = new ArrayList<String>();
			for (final Class<?> cls : potentialSubclasses)
			{
				if (clazz.isAssignableFrom(cls))
				{
					if (cls.isAssignableFrom(clazz))
						continue;		// Same class, skip this
					
					if (cls.isAnnotationPresent(Hide.class))
						continue;		// Skip hidden classes
					
					subclassTypes.add(cls.getName());
				}
			}
			
			for (final String subclassType : subclassTypes)
			{
				appendLine(sb, "SUBCLASS: " + subclassType);
			}
		}
		else
		{
			// Just a normal class
			final Node methodsNode = xmlChildForName(node.xmlNode, "methods");
			final List<Node> constructors = xmlChildrenForName(methodsNode, "constructor");
			
			// also add public static create() methods to the constructors
			final List<Node> createMethods = xmlChildrenForName(methodsNode, "method");
			for (final Node methodNode : createMethods)
			{
				final Node staticAttribute = methodNode.getAttributes().getNamedItem("static");
				
				if 
				(
					staticAttribute != null &&
					staticAttribute.getTextContent().equals("true") &&
					methodNode.getAttributes().getNamedItem("visibility").getTextContent().equals("public") &&
					methodNode.getAttributes().getNamedItem("name").getTextContent().equals("construct")
				)
				{
					constructors.add(methodNode);
				}
			}
			
			// Remove constructors with @Hide annotations
			constructors.removeIf((final Node ctor) -> { return hasAnnotation(ctor, "Hide"); });
	
			for (int ctorIdx = 0; ctorIdx < constructors.size(); ++ctorIdx)
			{
				final Node ctor = constructors.get(ctorIdx);
				if (ctor.getAttributes().getNamedItem("visibility").getTextContent().equals("public"))
				{
					// A public constructor
					appendLine(sb, "NEW CTOR");
	
					final StringBuilder ctorSb = new StringBuilder();
					if (unformattedAlias != null)
						ctorSb.append("(" + unformattedAlias + "");
					else
						ctorSb.append("(" + ludemeName + "");
	
					final Node paramsNode = xmlChildForName(ctor, "params");
					final List<String> paramSymbols = new ArrayList<String>();
					final TIntArrayList singleValueEnumParamIndices = new TIntArrayList();
					
					if (paramsNode != null)
					{
						final List<Node> paramNodes = xmlChildrenForName(paramsNode, "param");
						
						boolean orSequence = false;
						boolean or2Sequence = false;
	
						for (int paramNodeIdx = 0; paramNodeIdx < paramNodes.size(); ++paramNodeIdx)
						{
							final Node paramNode = paramNodes.get(paramNodeIdx);
							
							// Figure out how to write this param
							final String paramName = paramNode.getAttributes().getNamedItem("name").getTextContent();
							String paramFullType = paramNode.getAttributes().getNamedItem("fulltype").getTextContent();
	
							boolean named = false;
							boolean optional = false;
							int numArrayDims = 0;
							boolean startNewOrSequence = false;
							boolean endPreviousOrSequence = (orSequence || or2Sequence);
	
							while (paramFullType.endsWith("[]"))
							{
								paramFullType = paramFullType.substring(0, paramFullType.length() - "[]".length());
								numArrayDims += 1;
							}
	
							final Node annotationsNode = xmlChildForName(paramNode, "annotations");
							if (annotationsNode != null)
							{
								final List<Node> annotationNodes = xmlChildrenForName(annotationsNode, "annotation");
								for (final Node annotationNode : annotationNodes)
								{
									final String annotationType = annotationNode.getAttributes().getNamedItem("type").getTextContent();
									if (annotationType.equals("Name"))
									{
										named = true;
									}
									else if (annotationType.equals("Opt"))
									{
										optional = true;
									}
									else if (annotationType.equals("Or"))
									{
										if (orSequence)
											endPreviousOrSequence = false;
										else
											startNewOrSequence = true;
										
										orSequence = true;
										or2Sequence = false;
									}
									else if (annotationType.equals("Or2"))
									{
										if (or2Sequence)
											endPreviousOrSequence = false;
										else
											startNewOrSequence = true;
										
										orSequence = false;
										or2Sequence = true;
									}
								}
							}
							
							if (endPreviousOrSequence)
							{
								orSequence = false;
								or2Sequence = false;
								ctorSb.append(")");
							}
	
							// Write this param
							ctorSb.append(" ");
							
							if (startNewOrSequence)
								ctorSb.append("(");
							else if (orSequence || or2Sequence)
								ctorSb.append(" | ");
							
							final StringBuilder paramSymbolSb = new StringBuilder();
							
							if (optional)
								paramSymbolSb.append("[");
							if (named)
								paramSymbolSb.append(paramName + ":");
							
							for (int i = 0; i < numArrayDims; ++i)
							{
								paramSymbolSb.append("{");
							}
							
							final Class<?> paramClass = Class.forName(paramFullType);

							if (Enum.class.isAssignableFrom(paramClass))
							{
								if (paramClass.getEnumConstants().length == 1)
								{
									paramSymbolSb.append(paramClass.getEnumConstants()[0].toString());
									singleValueEnumParamIndices.add(paramNodeIdx);
								}
								else
								{
									paramSymbolSb.append("<PARAM:" + paramFullType + ">");
								}
							}
							else
							{
								paramSymbolSb.append("<PARAM:" + paramFullType + ">");
							}
	
							for (int i = 0; i < numArrayDims; ++i)
							{
								paramSymbolSb.append("}");
							}
							
							if (optional)
								paramSymbolSb.append("]");
							
							paramSymbols.add(paramSymbolSb.toString());
							ctorSb.append(paramSymbolSb);
						}
						
						if (orSequence || or2Sequence)	// close final or sequence
							ctorSb.append(")");
					}
	
					ctorSb.append(")");
	
					appendLine(sb, ctorSb.toString());
					
					if (paramSymbols.size() > 0)
					{
						// Add explanations of all the params
						final List<Node> paramNodes = xmlChildrenForName(paramsNode, "param");
						for (int paramNodeIdx = 0; paramNodeIdx < paramNodes.size(); ++paramNodeIdx)
						{
							if (singleValueEnumParamIndices.contains(paramNodeIdx))
								continue;	// Skip this param, it's a single-value enum
							
							final Node paramNode = paramNodes.get(paramNodeIdx);
							final Node commentNode = paramNode.getAttributes().getNamedItem("comment");
							
							if (commentNode == null)
								System.err.println("WARNING: No Javadoc comment for " + (paramNodeIdx + 1) + "th param of " + (ctorIdx + 1) + "th constructor of " + node.fullType);
								
							String paramDescription = commentNode != null ? commentNode.getTextContent() : "";
							paramDescription = StringRoutines.cleanWhitespace(paramDescription);
							appendLine(sb, "PARAM JAVADOC: " + paramSymbols.get(paramNodeIdx) + ": " + paramDescription);
						}
					}
					
					final Node ctorCommentNode = xmlChildForName(ctor, "comment");
					if (ctorCommentNode != null)
					{
						final List<Node> attributes = xmlChildrenForName(ctorCommentNode, "attribute");
						
						// Collect constructor-specific examples
						boolean foundExample = false;
						for (final Node attribute : attributes)
						{
							if (attribute.getAttributes().getNamedItem("name").getTextContent().equals("@example"))
							{
								final Node descriptionNode = xmlChildForName(attribute, "description");
								
								appendLine
								(
									sb, 
									"EXAMPLE: " + StringRoutines.cleanWhitespace(descriptionNode.getTextContent())
								);
								foundExample = true;
							}
						}
						
						if (!foundExample)
							System.err.println("WARNING: Found no example for one of the constructors of ludeme: " + node.fullType);
					}
				}
			}
		}

		if (classCommentNode != null)
		{
			final List<Node> attributes = xmlChildrenForName(classCommentNode, "attribute");
			
			// Write remarks
			for (final Node attribute : attributes)
			{
				if (attribute.getAttributes().getNamedItem("name").getTextContent().equals("@remarks"))
				{
					final Node descriptionNode = xmlChildForName(attribute, "description");
					appendLine(sb, "REMARKS: " + StringRoutines.cleanWhitespace(descriptionNode.getTextContent()));
				}
			}
		}
	}
	
	/**
	 * Processes the given enum node
	 * @param node
	 * @param sb
	 */
	private static void processEnumNode(final ClassTreeNode node, final StringBuilder sb)
	{
		// We're starting a new enum type
		appendLine(sb, "TYPE: " + node.fullType);
		
		// Write the comments on top of enum
		final Node commentNode = xmlChildForName(node.xmlNode, "comment");
		if (commentNode != null)
		{
			final Node descriptionNode = xmlChildForName(commentNode, "description");
			if (descriptionNode != null)
				appendLine(sb, "TYPE JAVADOC: " + StringRoutines.cleanWhitespace(descriptionNode.getTextContent()));
		}
		
		// Write enum value comments
		final Node enumerationNode = xmlChildForName(node.xmlNode, "enumeration");
		
		final List<Node> values = xmlChildrenForName(enumerationNode, "value");
		
		if (!values.isEmpty())
		{
			for (final Node valueNode : values)
			{
				final String valueName = valueNode.getAttributes().getNamedItem("name").getTextContent();
				
				final Node descriptionNode = valueNode.getAttributes().getNamedItem("description");
				final String description = (descriptionNode != null) ? descriptionNode.getTextContent() : null;
				
				if (description == null)
					System.err.println("WARNING: no javadoc for value " + valueName + " of enum: " + node.fullType);
				else if (StringRoutines.cleanWhitespace(description).equals(""))
					System.err.println("WARNING: empty javadoc for value " + valueName + " of enum: " + node.fullType);
				
				if (description != null)
					appendLine(sb, "CONST JAVADOC: " + valueName + ": " + description);
			}
		}
	}
	
	/**
	 * @param xmlNode
	 * @param name
	 * @return Child of given XML node with given name
	 */
	private static final Node xmlChildForName(final Node xmlNode, final String name)
	{
		final NodeList childNodes = xmlNode.getChildNodes();
		
		for (int i = 0; i < childNodes.getLength(); ++i)
		{
			if (name.equals(childNodes.item(i).getNodeName()))
				return childNodes.item(i);
		}
		
		return null;
	}
	
	/**
	 * @param xmlNode
	 * @param name
	 * @return Children of given XML node with given name
	 */
	private static final List<Node> xmlChildrenForName(final Node xmlNode, final String name)
	{
		final List<Node> ret = new ArrayList<Node>();
		final NodeList childNodes = xmlNode.getChildNodes();
		
		for (int i = 0; i < childNodes.getLength(); ++i)
		{
			if (name.equals(childNodes.item(i).getNodeName()))
				ret.add(childNodes.item(i));
		}
		
		return ret;
	}
	
	/**
	 * @param type
	 * @return Generates a String for a given type, as it would also appear in grammar
	 */
	private static final String typeString(final String type)
	{
		final String str = StringRoutines.lowerCaseInitial(type);
		
		if (str.equals("integer"))
			return "int";
		else if (str.equals("intFunction"))
			return "<int>";
		else if (str.equals("booleanFunction"))
			return "<boolean>";
		else if (str.equals("regionFunction"))
			return "<region>";
		
		return "<" + str + ">";
	}
	
	/**
	 * @param node
	 * @param type
	 * @return True if the given node contains an annotation of given type
	 */
	private static final boolean hasAnnotation(final Node node, final String type)
	{
		final Node annotationsNode = xmlChildForName(node, "annotations");
		if (annotationsNode != null)
		{
			final List<Node> annotationNodes = xmlChildrenForName(annotationsNode, "annotation");
			for (final Node annotationNode : annotationNodes)
			{
				final String annotationType = annotationNode.getAttributes().getNamedItem("type").getTextContent();

				if (annotationType.equals(type))
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Processes all our .def files
	 * @param sb
	 */
	private static final void processDefFiles(final StringBuilder sb)
	{
		// Get list of directories
		final List<File> dirs  = new ArrayList<File>();
		final File folder = new File(DEFS_DIR);
		dirs.add(folder);

		for (int i = 0; i < dirs.size(); ++i)
		{
			final File dir = dirs.get(i);
			for (final File file : dir.listFiles())
				if (file.isDirectory())
					dirs.add(file);		
		}
		Collections.sort(dirs);

		// Visit files in each directory
		for (final File dir : dirs)
		{
			for (final File file : dir.listFiles())
				if (!file.isDirectory())
					processDefFile(file, sb);
		}
	}
	
	/**
	 * Processes a single given .def file
	 * @param file
	 * @param sb
	 */
	private static final void processDefFile(final File file, final StringBuilder sb)
	{
		if (!file.getPath().contains(".def"))
		{
			System.err.println("Bad file: " + file.getPath());
			return;
		}
		
		// Read lines in
        final List<String> lines = new ArrayList<String>();
		try 
        (
        	final BufferedReader reader = 
        		new BufferedReader
        		(
        			new InputStreamReader
        			(
        				new FileInputStream(file), 
        				StandardCharsets.UTF_8
        			)
        		)
        )
        {
            String line = reader.readLine();
            while (line != null) 
            {
                lines.add(line);
                line = reader.readLine();
            }
        }
        catch (final IOException e) 
        { 
        	e.printStackTrace(); 
        }
		
		// Write .def name
		final String name = file.getName().substring(0, file.getName().length() - ".def".length());
		appendLine(sb, "DEFINE: " + name);
		
		// Write def comments
		appendLine(sb, "DEFINE JAVADOC: " + commentsFromLines(lines));
		
		// Write examples
		for (final String example : examplesFromLines(lines))
		{
			appendLine(sb, "DEFINE EXAMPLE: " + StringRoutines.cleanWhitespace(example));
		}
	}
	
	//-------------------------------------------------------------------------

	final static String commentsFromLines(final List<String> lines)
	{
		final StringBuilder sb = new StringBuilder();
		
		for (final String line : lines)
		{
			final int c = line.indexOf("//");
			if (c < 0)
				break;  // not a comment
		
			if (line.contains("@example"))
				break;  // don't include examples in comments
			
			sb.append(line.substring(c + 2).trim() + " ");
		}
		
		return sb.toString();
	}

	//-------------------------------------------------------------------------

	final static List<String> examplesFromLines(final List<String> lines)
	{
		final List<String> examples = new ArrayList<String>();
		
		for (final String line : lines)
		{
			final int c = line.indexOf("@example");
			if (c < 0)
				continue;  // not an example
		
			examples.add(line.substring(c + 8).trim());
		}
		
		return examples;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Appends given line to StringBuilder (+ newline character)
	 * @param sb
	 * @param line
	 */
	private static final void appendLine(final StringBuilder sb, final String line)
	{
		sb.append(line + "\n");
	}
	
	/**
	 * Makes sure all packages that are somewhere in the given list (including
	 * subpackages of these packages) are all nicely sorted: first classes,
	 * then subpackages, with each of those being sorted alphabetically.
	 * @param packages
	 */
	private static void sortPackages(final List<ClassTreeNode> packages)
	{
		packages.sort(new Comparator<ClassTreeNode>() 
		{

			@Override
			public int compare(final ClassTreeNode o1, final ClassTreeNode o2)
			{
				if (!o1.isPackage && o2.isPackage)
					return -1;
				
				if (o1.isPackage && !o2.isPackage)
					return 1;
				
				return o1.name.compareTo(o2.name);
			}
			
		});
		
		for (final ClassTreeNode p : packages)
		{
			sortPackages(p.children);
		}
	}
	
	/**
	 * Class for node in our tree of classes (and packages)
	 * 
	 * @author Dennis Soemers
	 */
	private static class ClassTreeNode
	{
		/** Full package + type name */
		public final String fullType;
		/** Name of package or class */
		public final String name;
		/** True if we're a package, false if we're a class */
		public boolean isPackage;
		/** True if this is an enum (instead of a Ludeme class or package) */
		public boolean isEnum;
		/** The XML node */
		public Node xmlNode;
		
		/** List of child nodes (with subpackages and classes) */
		public final List<ClassTreeNode> children = new ArrayList<ClassTreeNode>();
		
		/**
		 * Constructor
		 * @param name
		 * @param isPackage
		 * @param fullType
		 * @param isEnum
		 * @param xmlNode
		 */
		public ClassTreeNode
		(
			final String name, 
			final boolean isPackage, 
			final String fullType, 
			final boolean isEnum,
			final Node xmlNode
		)
		{
			this.name = name;
			this.isPackage = isPackage;
			this.fullType = fullType;
			this.isEnum = isEnum;
			this.xmlNode = xmlNode;
		}
		
		@Override
		public String toString()
		{
			return "[Node: " + fullType + "]";
		}
	}

}
