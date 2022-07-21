package tex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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

import gnu.trove.list.array.TIntArrayList;
import grammar.Grammar;
import graphics.svg.SVGLoader;
import main.Constants;
import main.StringRoutines;
import main.grammar.Description;
import main.grammar.Report;
import main.grammar.Symbol;
import main.grammar.Token;
import main.options.UserSelections;
import metadata.MetadataItem;
import other.Ludeme;
import parser.Parser;

//-----------------------------------------------------------------------------

/**
 * Code to generate .tex files for Ludii Language Reference
 *
 * @author Dennis Soemers and cambolbro
 */
public class GenerateLudiiDocTex
{
	private static final Grammar grammar = Grammar.grammar();
	
	private static final int CHAPTER_GAME = 0;
	private static final int CHAPTER_EQUIPMENT = 1;
	private static final int CHAPTER_GRAPH_FUNCS = 2;
	private static final int CHAPTER_DIM_FUNCS = 3;
	private static final int CHAPTER_FLOAT_FUNCS = 4;
	private static final int CHAPTER_RULES = 5;
	private static final int CHAPTER_MOVES = 6;
	private static final int CHAPTER_BOOLEAN_FUNCS = 7;
	private static final int CHAPTER_INT_FUNCS = 8;
	private static final int CHAPTER_INT_ARRAY_FUNCS = 9;
	private static final int CHAPTER_REGION_FUNCS = 10;
	private static final int CHAPTER_DIRECTION_FUNCS = 11;
	private static final int CHAPTER_RANGE_FUNCS = 12;
	private static final int CHAPTER_UTILITIES = 13;
	private static final int CHAPTER_TYPES = 14;
	
	private static final int CHAPTER_INFO_METADATA = 15;
	private static final int CHAPTER_GRAPHICS_METADATA = 16;
	private static final int CHAPTER_AI_METADATA = 17;

//	private static final int CHAPTER_DEFINES = 15;
//	private static final int CHAPTER_OPTIONS = 16;
//	private static final int CHAPTER_RULESETS = 17;
//	private static final int CHAPTER_RANGES = 18;
//	
//	private static final int APPENDIX_KNOWN_DEFINES = 19;
//	private static final int APPENDIX_LUDII_GRAMMAR = 14;
	
	private static final String[] CHAPTER_FILENAMES = new String[]
			{
				"out/tex/Chapter2GameLudemes.tex",
				"out/tex/Chapter3Equipment.tex",
				"out/tex/Chapter4GraphFuncs.tex",
				"out/tex/Chapter5DimFuncs.tex",
				"out/tex/Chapter6FloatFuncs.tex",
				"out/tex/Chapter7Rules.tex",
				"out/tex/Chapter8Moves.tex",
				"out/tex/Chapter9BooleanFuncs.tex",
				"out/tex/Chapter10IntFuncs.tex",
				"out/tex/Chapter11IntArrayFuncs.tex",
				"out/tex/Chapter12RegionFuncs.tex",
				"out/tex/Chapter13DirectionFuncs.tex",
				"out/tex/Chapter14RangeFuncs.tex",
				"out/tex/Chapter15Utilities.tex",
				"out/tex/Chapter16Types.tex",
				
				"out/tex/Chapter17InfoMetadata.tex",
				"out/tex/Chapter18GraphicsMetadata.tex",
				"out/tex/Chapter19AIMetadata.tex",

				// No! Overwrites source files
				//"out/tex/ChapterXDefines.tex",
				//"out/tex/ChapterXOptions.tex",
				//"out/tex/ChapterXRulesets.tex",
				//"out/tex/ChapterXRanges.tex",
				//"out/tex/ChapterXConstants.tex", 

				//"out/tex/AppendixAKnownDefines.tex",
				//"out/tex/AppendixBLudiiGrammar.tex",
			};
	
	private static final String[] CHAPTER_NAMES = new String[]
			{
				"Game",
				"Equipment",
				"Graph Functions",
				"Dimension Functions",
				"Float Functions",
				"Rules",
				"Moves",
				"Boolean Functions",
				"Integer Functions",
				"Integer Array Functions",
				"Region Functions",
				"Direction Functions",
				"Range Functions",
				"Utilities",
				"Types",
				
				"Info Metadata",
				"Graphics Metadata",
				"AI Metadata",

				"Defines",
				"Options",
				"Rulesets",
				"Ranges",
				"Constants",

				"B -- Known Defines",
				"A -- Ludii Grammar",
			};
	
	private static final String[] CHAPTER_LABELS = new String[]
			{
				"\\label{Chapter:GameLudemes}",
				"\\label{Chapter:EquipmentLudemes}",
				"\\label{Chapter:GraphFunctions}",
				"\\label{Chapter:DimFunctions}",
				"\\label{Chapter:FloatFunctions}",
				"\\label{Chapter:RuleLudemes}",
				"\\label{Chapter:MoveLudemes}",
				"\\label{Chapter:BooleanLudemes}",
				"\\label{Chapter:IntegerLudemes}",
				"\\label{Chapter:IntegerArrayLudemes}",
				"\\label{Chapter:RegionLudemes}",
				"\\label{Chapter:DirectionLudemes}",
				"\\label{Chapter:RangeLudemes}",
				"\\label{Chapter:Utilities}",
				"\\label{Chapter:Types}",
				
				"\\label{Chapter:InfoMetadata}",
				"\\label{Chapter:GraphicsMetadata}",
				"\\label{Chapter:AIMetadata}",
				
				"\\label{Chapter:Defines}",
				"\\label{Chapter:Options}",
				"\\label{Chapter:Rulesets}",
				"\\label{Chapter:Ranges}",
				"\\label{Chapter:Constants}",
	
				"\\label{Appendix:KnownDefines}",
				"\\label{Appendix:LudiiGrammar}",
			};
	
	/** Map from packages to chapters in which they should be included */
	private static final Map<String, Integer> PACKAGE_TO_CHAPTER = new HashMap<String, Integer>();
	
	static
	{
		PACKAGE_TO_CHAPTER.put("game", Integer.valueOf(CHAPTER_GAME));
		PACKAGE_TO_CHAPTER.put("game.equipment", Integer.valueOf(CHAPTER_EQUIPMENT));
		PACKAGE_TO_CHAPTER.put("game.functions.graph", Integer.valueOf(CHAPTER_GRAPH_FUNCS));
		PACKAGE_TO_CHAPTER.put("game.functions.dim", Integer.valueOf(CHAPTER_DIM_FUNCS));
		PACKAGE_TO_CHAPTER.put("game.functions.floats", Integer.valueOf(CHAPTER_FLOAT_FUNCS));
		PACKAGE_TO_CHAPTER.put("game.functions.booleans", Integer.valueOf(CHAPTER_BOOLEAN_FUNCS));
		PACKAGE_TO_CHAPTER.put("game.functions.intArray", Integer.valueOf(CHAPTER_INT_ARRAY_FUNCS));
		PACKAGE_TO_CHAPTER.put("game.functions.ints", Integer.valueOf(CHAPTER_INT_FUNCS));
		PACKAGE_TO_CHAPTER.put("game.functions.region", Integer.valueOf(CHAPTER_REGION_FUNCS));
		PACKAGE_TO_CHAPTER.put("game.rules", Integer.valueOf(CHAPTER_RULES));
		PACKAGE_TO_CHAPTER.put("game.rules.play.moves", Integer.valueOf(CHAPTER_MOVES));
		PACKAGE_TO_CHAPTER.put("game.functions.directions", Integer.valueOf(CHAPTER_DIRECTION_FUNCS));
		PACKAGE_TO_CHAPTER.put("game.functions.range", Integer.valueOf(CHAPTER_RANGE_FUNCS));
		PACKAGE_TO_CHAPTER.put("game.types", Integer.valueOf(CHAPTER_TYPES));
		PACKAGE_TO_CHAPTER.put("game.util", Integer.valueOf(CHAPTER_UTILITIES));
		
		PACKAGE_TO_CHAPTER.put("metadata", Integer.valueOf(CHAPTER_INFO_METADATA));
		PACKAGE_TO_CHAPTER.put("metadata.graphics", Integer.valueOf(CHAPTER_GRAPHICS_METADATA));
		PACKAGE_TO_CHAPTER.put("metadata.ai", Integer.valueOf(CHAPTER_AI_METADATA));
	}
	
	/** Map from type descriptions in documentation strings to labels they should link to */
	private static final Map<String, String> TYPE_TO_LABEL = new HashMap<String, String>();
	
	static
	{
		TYPE_TO_LABEL.put("java.lang.Integer", "Sec:Introduction.Integers");
		TYPE_TO_LABEL.put("java.lang.Boolean", "Sec:Introduction.Booleans");
		TYPE_TO_LABEL.put("java.lang.Float", "Sec:Introduction.Floats");
		TYPE_TO_LABEL.put("java.lang.String", "Sec:Introduction.Strings");
		
		TYPE_TO_LABEL.put("game.equipment.container.board.shape.Shape", "Sec:game.equipment.container.board.shape");
		TYPE_TO_LABEL.put("game.equipment.container.board.tiling.Tiling", "Sec:game.equipment.container.board.tiling.flat");
		TYPE_TO_LABEL.put("game.equipment.container.board.tiling.flat.FlatTiling", "Sec:game.equipment.container.board.tiling.flat");
		TYPE_TO_LABEL.put("game.equipment.container.board.tiling.pyramidal.PyramidalTiling", "Sec:game.equipment.container.board.tiling.pyramidal");
		TYPE_TO_LABEL.put("game.equipment.container.board.modify.Modify", "Sec:game.equipment.container.board.modify");
		TYPE_TO_LABEL.put("game.equipment.Item", "Chapter:EquipmentLudemes");
		TYPE_TO_LABEL.put("game.functions.ints.IntFunction", "Chapter:IntegerLudemes");
		TYPE_TO_LABEL.put("game.functions.intArray.IntArrayFunction", "Chapter:IntegerArrayLudemes");
		TYPE_TO_LABEL.put("game.functions.booleans.BooleanFunction", "Chapter:BooleanLudemes");
		TYPE_TO_LABEL.put("game.functions.range.RangeFunction", "Chapter:RangeLudemes");
		TYPE_TO_LABEL.put("game.functions.region.RegionFunction", "Chapter:RegionLudemes");
		TYPE_TO_LABEL.put("game.functions.directions.DirectionsFunction", "Chapter:DirectionLudemes");
		TYPE_TO_LABEL.put("game.functions.graph.GraphFunction", "Chapter:GraphFunctions");
		TYPE_TO_LABEL.put("game.functions.floats.FloatFunction", "Chapter:FloatFunctions");
		TYPE_TO_LABEL.put("game.functions.dim.DimFunction", "Chapter:DimFunctions");
		TYPE_TO_LABEL.put("game.rules.end.EndRule", "Sec:game.rules.end");
		TYPE_TO_LABEL.put("game.rules.meta.MetaRule", "Sec:game.rules.meta");
		TYPE_TO_LABEL.put("game.rules.play.moves.effect.Effects", "Sec:game.rules.play.moves.effect");
		TYPE_TO_LABEL.put("game.rules.play.moves.Moves", "Chapter:MoveLudemes");
		TYPE_TO_LABEL.put("game.rules.play.moves.nonDecision.NonDecision", "Chapter:MoveLudemes");
		TYPE_TO_LABEL.put("game.rules.start.StartRule", "Sec:game.rules.start");
		TYPE_TO_LABEL.put("game.util.directions.Direction", "Sec:game.util.directions");
		
		TYPE_TO_LABEL.put("metadata.ai.heuristics.transformations.HeuristicTransformation", "Sec:metadata.ai.heuristics.transformations");
		TYPE_TO_LABEL.put("metadata.ai.heuristics.terms.HeuristicTerm", "Sec:metadata.ai.heuristics.terms");
		
		TYPE_TO_LABEL.put("metadata.graphics.Graphics", "Chapter:GraphicsMetadata");
		TYPE_TO_LABEL.put("metadata.info.Info", "Chapter:InfoMetadata");
		TYPE_TO_LABEL.put("metadata.info.InfoItem", "Sec:metadata.info.database");
	}
	
	/** Map of pieces of text to replace in documentation */
	private static final Map<String, String> TEXT_TO_REPLACE = new HashMap<String, String>();
	
	static
	{
		TEXT_TO_REPLACE.put("MAX_DISTANCE", "$" + Constants.MAX_DISTANCE + "$");
		TEXT_TO_REPLACE.put("\\section{Ai}", "\\section{AI}");
		
		TEXT_TO_REPLACE.put("\\\\url", "\\url");
		TEXT_TO_REPLACE.put("\\\\texttt{", "\\texttt{");
		
		TEXT_TO_REPLACE.put("$\\\\leq$", "$\\leq$");
		TEXT_TO_REPLACE.put("$\\\\geq$", "$\\geq$");
		TEXT_TO_REPLACE.put("$\\\\neq$", "$\\neq$");
		
		TEXT_TO_REPLACE.put("\\\\frac{", "\\frac{");
		TEXT_TO_REPLACE.put("\\\\exp(", "\\exp(");
		TEXT_TO_REPLACE.put("\\\\tanh", "\\tanh");
	}
	
	/**
	 * For every package with package-info.java comments, a string containing its comments
	 * with @chapter annotation.
	 */
	private static final Map<String, String> CHAPTER_PACKAGE_INFOS = new HashMap<String, String>();
	
	/**
	 * For every package with package-info.java comments, a string containing its comments
	 * without any specific annotations (or with @section annotations).
	 */
	private static final Map<String, String> SECTION_PACKAGE_INFOS = new HashMap<String, String>();
	
	/** The Section that we're currently writing in (per chapter) */
	private static String[] currentSections = new String[CHAPTER_NAMES.length];
	
	/**
	 * Main method
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void generateTex() throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException
	{
		// First we'll read the XML file we generated
		final File inputFile = new File("out/xml/jel.xml");
		
		final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		final Document doc = dBuilder.parse(inputFile);
		final Element root = doc.getDocumentElement();
		
		final List<ClassTreeNode> rootPackages = new ArrayList<ClassTreeNode>();
		
		final List<Node> packageInfoNodes = xmlChildrenForName(root, "jelpackage");
		for (final Node packageInfoNode : packageInfoNodes)
		{
			final Node commentNode = xmlChildForName(packageInfoNode, "comment");
			
			if (commentNode != null)
			{
				final String type = packageInfoNode.getAttributes().getNamedItem("type").getTextContent();
				final Node descriptionNode = xmlChildForName(commentNode, "description");
				
				final List<String> sectionComments = new ArrayList<String>();
				final List<String> chapterComments = new ArrayList<String>();
				
				if (descriptionNode != null)
					sectionComments.add(descriptionNode.getTextContent());
				
				final List<Node> attributeNodes = xmlChildrenForName(commentNode, "attribute");
				for (final Node attributeNode : attributeNodes)
				{
					final String attributeName = attributeNode.getAttributes().getNamedItem("name").getTextContent();
					final String attributeDescription = xmlChildForName(attributeNode, "description").getTextContent();
					
					if (attributeName.equals("@chapter"))
						chapterComments.add(attributeDescription);
					else if (attributeName.equals("@section"))
						sectionComments.add(attributeDescription);
				}
				
				SECTION_PACKAGE_INFOS.put(type, StringRoutines.join("\n\n", sectionComments));
				CHAPTER_PACKAGE_INFOS.put(type, StringRoutines.join("\n\n", chapterComments));
			}
		}
		
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
			
			if ((clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) && !clazz.isEnum())
				continue;	// We skip interfaces and abstract classes
			
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
				
			final List<Symbol> grammarSymbols = grammar.symbolListFromClassName(type);
			if (grammarSymbols != null)
			{
				for (final Symbol symbol : grammarSymbols)
				{
					if 
					(
						symbol.usedInDescription() 
						|| 
						symbol.usedInMetadata()
						|| 
						(clazz.isEnum() && symbol.usedInGrammar())
					)
					{
						grammarSymbolUsed = true;
						break;
					}
				}
			}
			
			if (!grammarSymbolUsed && !MetadataItem.class.isAssignableFrom(clazz))
			{
				System.out.println("Ignoring type not used in grammar: " + fullType);
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
		
		// Create StringBuilders in which to collect text for all our chapters
		final StringBuilder[] chapterStrings = new StringBuilder[CHAPTER_FILENAMES.length];
		for (int i = 0; i < chapterStrings.length; ++i)
		{
			chapterStrings[i] = new StringBuilder();
			appendLine(chapterStrings[i], "\\chapter{" + CHAPTER_NAMES[i] + "} " + CHAPTER_LABELS[i]);
			
			// Write package info for this chapter
			String chapterPackageStr = null;
			
			for (final String key : PACKAGE_TO_CHAPTER.keySet())
			{
				if (PACKAGE_TO_CHAPTER.get(key).intValue() == i)
				{
					chapterPackageStr = key;
					break;
				}
			}
			
			final String packageInfo = CHAPTER_PACKAGE_INFOS.get(chapterPackageStr);
			
			if (packageInfo != null)
				appendLine(chapterStrings[i], packageInfo);
		}
		
		// Process all our packages in a depth-first manner
		for (final ClassTreeNode node : rootPackages)
		{
			process(node, chapterStrings);
		}
		
		// Postprocess all our full chapter strings to set up hyperlinks, fix type names,
		// and replace other keywords that need replacing
		for (int i = 0; i < chapterStrings.length; ++i)
		{
			final StringBuilder sb = chapterStrings[i];
			
			while (true)
			{
				final int paramStartIdx = sb.indexOf("<TO_LINK:");
				
				if (paramStartIdx < 0)	// we're done with this chapter
					break;
				
				final int paramEndIdx = sb.indexOf(">", paramStartIdx);
				String paramTypeStr = sb.substring(paramStartIdx, paramEndIdx);
				paramTypeStr = paramTypeStr.substring("<TO_LINK:".length(), paramTypeStr.length());
				if (paramTypeStr.endsWith("[]"))
					paramTypeStr = paramTypeStr.substring(0, paramTypeStr.length() - "[]".length());
				final String[] paramTypeStrSplit = paramTypeStr.split(Pattern.quote("."));
				
				final String paramTypeText = typeString(StringRoutines.lowerCaseInitial(paramTypeStrSplit[paramTypeStrSplit.length - 1]));
				final String labelToLink = TYPE_TO_LABEL.get(paramTypeStr);
				
				final String replaceStr;
				
				if (labelToLink == null)	
				{
					// No hyperlink
					replaceStr = paramTypeText;
					System.out.println("no hyperlink for: " + paramTypeStr);
				}
				else
				{
					replaceStr = "\\hyperref[" + labelToLink + "]{" + paramTypeText + "}";
				}
				
				sb.replace(paramStartIdx, paramEndIdx + 1, replaceStr);
			}
			
			for (final String key : TEXT_TO_REPLACE.keySet())
			{
				while (true)
				{
					final int startIdx = sb.indexOf(key);
					
					if (startIdx < 0)	// we're done with this key in this chapter
						break;
					
					final int endIdx = startIdx + key.length();
					sb.replace(startIdx, endIdx, TEXT_TO_REPLACE.get(key));
				}
			}
		}
		
		// Write all our chapter files
		for (int i = 0; i < chapterStrings.length; ++i)
		{
			try (final PrintWriter writer = new PrintWriter(new File(CHAPTER_FILENAMES[i]), "UTF-8"))
			{
				writer.write(chapterStrings[i].toString());
			}
		}
		
//		for (final ClassTreeNode p : rootPackages)
//		{
//			p.print(0);
//		}
		
		// Write our appendix with list of images
		writeImageList();
		
		// Write our appendix of known defines
		writeKnownDefines();
		
		// Write out appendix containing grammar
		writeGrammar();
	}
	
	/**
	 * Processes the given node
	 * @param node
	 * @param chapterStrings
	 */
	private static void process(final ClassTreeNode node, final StringBuilder[] chapterStrings)
	{
		if (node.isPackage)
		{
			for (final ClassTreeNode child : node.children)
			{
				process(child, chapterStrings);
			}
		}
		else
		{
			if (node.isEnum)
				processEnumNode(node, chapterStrings);
			else
				processClassNode(node, chapterStrings);
		}
	}
	
	/**
	 * Processes the given class node
	 * @param node
	 * @param chapterStrings
	 */
	private static void processClassNode(final ClassTreeNode node, final StringBuilder[] chapterStrings)
	{
		// First figure out index of chapter in which we should write for this node
		int chapter = -1;
		String chapterPackages = node.fullType;
		
		while (chapter < 0)
		{
			if (PACKAGE_TO_CHAPTER.containsKey(chapterPackages))
				chapter = PACKAGE_TO_CHAPTER.get(chapterPackages).intValue();
			else
				chapterPackages = chapterPackages.substring(0, chapterPackages.lastIndexOf("."));
		}

		final StringBuilder sb = chapterStrings[chapter];
		
		// Section headers will be based on what we didn't use for identifying chapter
		final String sectionHeader = sectionHeader(chapterPackages + ".", node.fullType, chapter);

		if (!sectionHeader.equals(currentSections[chapter]))
		{
			// We need to start writing a new section
			currentSections[chapter] = sectionHeader;
			
			final String packageStr = node.fullType.substring(0, node.fullType.length() - node.name.length() - 1);
			final String packageInfo = SECTION_PACKAGE_INFOS.get(packageStr);
			
			final String secLabelStr;
			if (packageStr.contains(".") && !PACKAGE_TO_CHAPTER.containsKey(packageStr))
				secLabelStr = packageStr;
			else
				secLabelStr = node.fullType + "." + node.name;
			
//			System.out.println();
//			System.out.println("node.fullType = " + node.fullType);
//			System.out.println("node.name = " + node.name);
//			System.out.println("packageStr = " + packageStr);
//			System.out.println("sectionHeader = " + sectionHeader);
//			System.out.println("secLabelStr = " + secLabelStr);
//			System.out.println("chapter = " + chapter);
//			System.out.println();
			
			appendLine(sb, "");
			
			//ruledLine(sb, 2);
			appendLine(sb, "\\newpage");
			
			appendLine(sb, "\\section{" + sectionHeader + "} \\label{Sec:" + secLabelStr + "}");
			
			// Insert package info
			if (packageInfo != null)
				appendLine(sb, SECTION_PACKAGE_INFOS.get(packageStr));
			else
				System.err.println("null package info for: " + packageStr);
		}

		ruledLine(sb, 0.5);
		//appendLine(sb, "\\newpage");

		// Start a new subsection for specific ludeme
		final String ludemeName = StringRoutines.lowerCaseInitial(node.name);
		String subsectionHeader = ludemeName;
		
		String alias = null;
		String unformattedAlias = null;
		
		// Check if we should modify subsection header due to @Alias annotation
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
						alias = valueNode.getAttributes().getNamedItem("value").getTextContent();
						unformattedAlias = alias;
						alias = "$" + alias + "$";
						alias = alias.replaceAll(Pattern.quote("%"), Matcher.quoteReplacement("\\%"));
						alias = alias.replaceAll(Pattern.quote("^"), Matcher.quoteReplacement("\\wedge{}"));
						subsectionHeader = "\\texorpdfstring{" + alias + "}{} (" + ludemeName + ")";
						break;
					}
				}
			}
		}
		
		appendLine(sb, "");
		appendLine(sb, "\\subsection{" + subsectionHeader + "} \\label{Subsec:" + node.fullType + "}");
		TYPE_TO_LABEL.put(node.fullType, "Subsec:" + node.fullType);

		// First write the comments on top of class
		final Node classCommentNode = xmlChildForName(node.xmlNode, "comment");
		if (classCommentNode != null)
		{
			final Node descriptionNode = xmlChildForName(classCommentNode, "description");
			if (descriptionNode != null)
			{
				if (descriptionNode.getTextContent().charAt(descriptionNode.getTextContent().length() - 1) != '.')
					System.err.println("WARNING: comment with no full stop for the description of " + ludemeName);
				appendLine(sb, descriptionNode.getTextContent());
			}
		}
		
		// Add a separating line before "Format" box
//		sb.append("\n\\phantom{}\n\n");

		// Write the constructors
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
		
		// Remove non-public constructors
		constructors.removeIf((final Node ctor) -> 
		{ 
			return !ctor.getAttributes().getNamedItem("visibility").getTextContent().equals("public"); 
		});
		
		if (constructors.size() >= 1)
		{
			appendLine(sb, "\\vspace{-1mm}");
			appendLine(sb, "\\subsubsection*{Format}");
		}
		
		// This will collect examples over all constructors
		final List<Node> exampleDescriptionNodes = new ArrayList<Node>();

		for (int ctorIdx = 0; ctorIdx < constructors.size(); ++ctorIdx)
		{
			final Node ctor = constructors.get(ctorIdx);
			appendLine(sb, "\\begin{formatbox}");
			
			if (constructors.size() > 1)
			{
				// Need to add constructor-level javadoc
				final Node commentNode = xmlChildForName(ctor, "comment");
				if (commentNode != null)
				{
					final Node descriptionNode = xmlChildForName(commentNode, "description");
					
					if (descriptionNode != null)
					{
						final String description = descriptionNode.getTextContent();
						
						if (description.length() > 0)
							appendLine(sb, description + "\n\n\\vspace{.2cm}\n");
						else
							System.err.println("WARNING: No Javadoc comment for " + (ctorIdx + 1) + "th constructor of " + node.fullType);
						
						if (description.toLowerCase().contains("constructor"))
							System.err.println("WARNING: Javadoc comment for " + (ctorIdx + 1) + "th constructor of " + node.fullType + " uses the word Constructor!");
					}
					else
					{
						System.err.println("WARNING: No Javadoc comment for " + (ctorIdx + 1) + "th constructor of " + node.fullType);
					}
				}
				else
				{
					System.err.println("WARNING: No Javadoc comment for " + (ctorIdx + 1) + "th constructor of " + node.fullType);
				}
			}
			
			appendLine(sb, "\\noindent\\begin{minipage}{\\textwidth}");

			final StringBuilder ctorSb = new StringBuilder();
			appendLine(sb, "\\begin{ttquote}");

			//appendLine(sb, "\\noindent");
			appendLine(sb, "\\hspace{-7mm}");

			if (alias != null)
				ctorSb.append("(" + alias + "");
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
						paramSymbolSb.append(StringRoutines.lowerCaseInitial(paramName) + ":");

					for (int i = 0; i < numArrayDims; ++i)
					{
						paramSymbolSb.append("\\{");
					}

					try
					{
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
								paramSymbolSb.append("<TO_LINK:" + paramFullType + ">");
							}
						}
						else
						{
							paramSymbolSb.append("<TO_LINK:" + paramFullType + ">");
						}
					} 
					catch (final ClassNotFoundException e)
					{
						e.printStackTrace();
					}

					for (int i = 0; i < numArrayDims; ++i)
					{
						paramSymbolSb.append("\\}");
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

			appendLine(sb, "\\end{ttquote}");

			if (paramSymbols.size() > 0)
				appendLine(sb, "\\vspace{\\parskip}");

			appendLine(sb, "\\end{minipage}");

			if (paramSymbols.size() - singleValueEnumParamIndices.size() > 0)
			{
				// Add explanations of all the params
				appendLine(sb, "\\noindent where:");
				appendLine(sb, "\\begin{itemize}");

				final List<Node> paramNodes = xmlChildrenForName(paramsNode, "param");
				for (int paramNodeIdx = 0; paramNodeIdx < paramNodes.size(); ++paramNodeIdx)
				{
					if (singleValueEnumParamIndices.contains(paramNodeIdx))
					{
						// Skip this param, it's a single-value enum
						continue;
					}

					final Node paramNode = paramNodes.get(paramNodeIdx);
					final Node commentNode = paramNode.getAttributes().getNamedItem("comment");

					if (commentNode == null)
						System.err.println("WARNING: No Javadoc comment for " + (paramNodeIdx + 1) + "th param of " + (ctorIdx + 1) + "th constructor of " + node.fullType);
					else if (StringRoutines.cleanWhitespace(commentNode.getTextContent()).equals(""))
						System.err.println("WARNING: Empty Javadoc comment for " + (paramNodeIdx + 1) + "th param of " + (ctorIdx + 1) + "th constructor of " + node.fullType);
					else if (commentNode.getTextContent().charAt(commentNode.getTextContent().length() - 1) != '.')
						System.err.println("WARNING: comment with no full stop for " + (paramNodeIdx + 1)
								+ "th param of " + (ctorIdx + 1) + "th constructor of " + node.fullType);

					final String paramDescription = commentNode != null ? commentNode.getTextContent() : "";
					appendLine(sb, "\\item \\texttt{" + paramSymbols.get(paramNodeIdx) + "}: " + paramDescription);

					final int openSquareBracketIdx = paramDescription.indexOf("[");
					if (openSquareBracketIdx >= 0)
					{
						final int closingSquareBracketIdx = StringRoutines.matchingBracketAt(paramDescription, openSquareBracketIdx);
						if (closingSquareBracketIdx < 0)
						{
							System.err.println("WARNING: No closing square bracket in " + (paramNodeIdx + 1) + "th param of " + (ctorIdx + 1) + "th constructor of " + node.fullType);
						}
					}
				}

				appendLine(sb, "\\end{itemize}");
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
						exampleDescriptionNodes.add(descriptionNode);
						foundExample = true;
					}
				}

				if (!foundExample)
					System.err.println("WARNING: Found no example for one of the constructors of ludeme: " + node.fullType);

				// We don't want to support constructor-specific remarks anymore
				for (final Node attribute : attributes)
				{
					if (attribute.getAttributes().getNamedItem("name").getTextContent().equals("@remarks"))
						System.err.println("WARNING: Found constructor-specific remark in ludeme: " + ludemeName);
				}
			}

			appendLine(sb, "\\vspace{.2cm}");
			appendLine(sb, "\\end{formatbox}");

			if (ctorIdx + 1 < constructors.size())
				appendLine(sb, "\\phantom{}");

			appendLine(sb, "");
		}
		
		// Collect nodes with remarks to print here
		final List<Node> remarkNodes = new ArrayList<Node>();
		
		// And same for images
		final List<Node> imageNodes = new ArrayList<Node>();
		
		if (classCommentNode != null)
		{
			final List<Node> attributes = xmlChildrenForName(classCommentNode, "attribute");
			
			// Collect class-wide examples
			for (final Node attribute : attributes)
			{
				if (attribute.getAttributes().getNamedItem("name").getTextContent().equals("@example"))
				{
					final Node descriptionNode = xmlChildForName(attribute, "description");
					exampleDescriptionNodes.add(descriptionNode);
				}
			}
			
			// Collect remarks
			for (final Node attribute : attributes)
			{
				if (attribute.getAttributes().getNamedItem("name").getTextContent().equals("@remarks"))
				{
					final Node descriptionNode = xmlChildForName(attribute, "description");
					if (descriptionNode.getTextContent().charAt(descriptionNode.getTextContent().length() - 1) != '.')
						System.err.println("WARNING: comment with no full stop for the remarks of " + ludemeName);
					remarkNodes.add(descriptionNode);
				}
			}
			
			// Collect images
			for (final Node attribute : attributes)
			{
				if (attribute.getAttributes().getNamedItem("name").getTextContent().equals("@image"))
				{
					final Node descriptionNode = xmlChildForName(attribute, "description");
					imageNodes.add(descriptionNode);
				}
			}
		}
		
		if (exampleDescriptionNodes.size() > 0)
		{
			appendLine(sb, "\\vspace{-2mm}");
			
			if (exampleDescriptionNodes.size() == 1)
				appendLine(sb, "\\subsubsection*{Example}");
			else
				appendLine(sb, "\\subsubsection*{Examples}");
			
			appendLine(sb, "\\begin{formatbox}");
			appendLine(sb, "\\noindent\\begin{minipage}{\\textwidth}");
			for (final Node exampleNode : exampleDescriptionNodes)
			{
				final Report report = new Report();
				final Token token = new Token(exampleNode.getTextContent(), report);
				String exampleCode = token.toString();

				// Try to compile our example code
				Object compiledObject = null;
				try
				{
					final Description description = new Description(exampleCode);
					final String className = node.fullType;
					final String symbolName;
					
					if (unformattedAlias == null)
					{
						final String[] classNameSplit = className.split(Pattern.quote("."));
						symbolName = StringRoutines.lowerCaseInitial(classNameSplit[classNameSplit.length - 1]);
					}
					else
					{
						symbolName = unformattedAlias;
					}
					
					//Expander.expand(description, new UserSelections(new ArrayList<String>()), report, false);
					Parser.expandAndParse
					(
						description,  
						new UserSelections(new ArrayList<String>()), 
						report, 
						true,  // allow examples
						false
					);

					compiledObject = compiler.Compiler.compileObject(description.expanded(), symbolName, className, report);
					
					if (report.isWarning())
					{
						System.err.println("Encountered warnings compiling example code:");
						System.err.println(exampleCode);
						System.err.println();
						
						for (final String warning : report.warnings())
						{
							System.err.println(warning);
						}
					}
					
					if (report.isError())
					{
						System.err.println("Encountered errors compiling example code:");
						System.err.println(exampleCode);
						System.err.println();
						
						for (final String error : report.errors())
						{
							System.err.println(error);
						}
					}
					
					//compiledObject = compiler.Compiler.compileObject(exampleCode, node.fullType);
				}
				catch (final Exception e)
				{
					// Do nothing
				}
				
				if (exampleCode.endsWith("\n"))
					exampleCode = exampleCode.substring(0, exampleCode.lastIndexOf('\n'));
				
				if (compiledObject == null)
				{
					System.err.println("Failed to compile example code in " + ludemeName);
					System.err.println("Expected type = " + node.fullType);
					System.err.println("Example code: \n" + exampleCode);
					System.err.println("exampleNode.getTextContent() = " + exampleNode.getTextContent());
				}

				appendLine(sb, "\\begin{verbatim}");
				// TO PRINT ALL THE EXAMPLES UNCOMMENT THAT.
				// System.out.println(exampleNode.getTextContent() + "\n");
				appendLine(sb, exampleCode);
				appendLine(sb, "\\end{verbatim}");
			}
			
			appendLine(sb, "\\end{minipage}");
			
			if (exampleDescriptionNodes.size() > 1)
				appendLine(sb, "\\vspace{.2cm}");
			else
				appendLine(sb, "\\vspace{.001cm}");
			
			appendLine(sb, "\\end{formatbox}");
			appendLine(sb, "");
		}
		
		for (final Node remarksNode : remarkNodes)
		{
			appendLine(sb, "");
			appendLine(sb, "\\vspace{-2mm}");
			appendLine(sb, "\\subsubsection*{Remarks}");
			appendLine(sb, remarksNode.getTextContent());
		}
		
		if (imageNodes.size() > 0)
		{
			appendLine(sb, "");
			appendLine(sb, "\\subsection*{Images}");
			
			for (final Node imageNode : imageNodes)
			{
				final String contents = imageNode.getTextContent();
				
				final int startIdxFilepath = contents.indexOf("IMAGE=");
				final int startIdxCaption = contents.indexOf("CAPTION=");
				final int startIdxLabel = contents.indexOf("LABEL=");
				
				if (startIdxFilepath < 0)
				{
					System.err.println("WARNING: Image filepath not found for image in ludeme: " + node.fullType);
					continue;
				}
				
				if (startIdxCaption < 0)
				{
					System.err.println("WARNING: Image caption not found for image in ludeme: " + node.fullType);
					continue;
				}
				
				if (startIdxLabel < startIdxCaption || startIdxCaption < startIdxFilepath)
				{
					System.err.println("WARNING: Ignoring image for ludeme: " + node.fullType + ". We require file first, then caption, then (optionally) label.");
					continue;
				}
				
				appendLine(sb, "\\begin{figure} [H]");
				appendLine(sb, "\\centering");
				
				final int endIdxFilepath;
				if (startIdxLabel >= 0)
					endIdxFilepath = Math.min(startIdxCaption, startIdxLabel);
				else
					endIdxFilepath = startIdxCaption;
				
				appendLine(sb, "\\includegraphics[max height=6cm,max width=\\textwidth]{" + contents.substring(startIdxFilepath + "IMAGE=".length(), endIdxFilepath).trim() + "}");
				
				final int endIdxCaption = (startIdxLabel >= 0) ? startIdxLabel : contents.length();
				appendLine(sb, "\\caption{" + contents.substring(startIdxCaption + "CAPTION=".length(), endIdxCaption).trim() + "}");
				
				if (startIdxLabel >= 0)
					appendLine(sb, "\\label{" + contents.substring(startIdxLabel + "LABEL=".length(), contents.length()).trim() + "}");
				
				appendLine(sb, "\\end{figure}");
			}
		}
		
//		if (exampleDescriptionNodes.size() > 0 || remarkNodes.size() > 0)
//			ruledLine(sb);
	}
	
	static void ruledLine(final StringBuilder sb, final double width)
	{
		appendLine(sb, "");
		appendLine(sb, "\\vskip10pt");
		appendLine(sb, "\\noindent\\rule{\\textwidth}{" + width + "pt}");
		appendLine(sb, "\\vskip0pt");
		appendLine(sb, "\\vspace{-2mm}");
	}
	
	/**
	 * Processes the given enum node
	 * @param node
	 * @param chapterStrings
	 */
	private static void processEnumNode(final ClassTreeNode node, final StringBuilder[] chapterStrings)
	{
		// First figure out index of chapter in which we should write for this node
		int chapter = -1;
		String chapterPackages = node.fullType;
		
		while (chapter < 0)
		{
			if (PACKAGE_TO_CHAPTER.containsKey(chapterPackages))
				chapter = PACKAGE_TO_CHAPTER.get(chapterPackages).intValue();
			else
				chapterPackages = chapterPackages.substring(0, chapterPackages.lastIndexOf("."));
		}

		final StringBuilder sb = chapterStrings[chapter];
		
		// Section headers will be based on what we didn't use for identifying chapter
		final String sectionHeader = sectionHeader(chapterPackages + ".", node.fullType, chapter);

		final String currentSection = currentSections[chapter];
		if (!sectionHeader.equals(currentSection))
		{
			// We need to start writing a new section
			currentSections[chapter] = sectionHeader;
			appendLine(sb, "");
			
			ruledLine(sb, 0.5);
			//appendLine(sb, "\\newpage");
			
			final String packageStr = node.fullType.substring(0, node.fullType.length() - node.name.length() - 1);
			final String secLabelStr;
			if (packageStr.contains(".") && !PACKAGE_TO_CHAPTER.containsKey(packageStr))
				secLabelStr = packageStr;
			else
				secLabelStr = node.fullType + "." + node.name;

			appendLine(sb, "\\section{" + sectionHeader + "} \\label{Sec:" + secLabelStr + "}");
			
			final String packageInfo = SECTION_PACKAGE_INFOS.get(packageStr);
			
			// Insert package info
			if (packageInfo != null)
				appendLine(sb, SECTION_PACKAGE_INFOS.get(packageStr));
			else
				System.err.println("null package info for: " + packageStr);
		}

		// Start a new subsection for specific enum
		final String ludemeName = StringRoutines.lowerCaseInitial(node.name);
		appendLine(sb, "");
		appendLine(sb, "\\subsection{" + ludemeName + "} \\label{Subsec:" + node.fullType + "}");
		TYPE_TO_LABEL.put(node.fullType, "Subsec:" + node.fullType);

		// First write the comments on top of class
		final Node commentNode = xmlChildForName(node.xmlNode, "comment");
		if (commentNode != null)
		{
			final Node descriptionNode = xmlChildForName(commentNode, "description");
			if (descriptionNode != null)
				appendLine(sb, descriptionNode.getTextContent());
		}

		// Write enum values
		final Node enumerationNode = xmlChildForName(node.xmlNode, "enumeration");
		
		appendLine(sb, "");
//		appendLine(sb, "\\subsubsection*{Type values}");
//		appendLine(sb, "\\vspace{-4mm}");
			
		final List<Node> values = xmlChildrenForName(enumerationNode, "value");
		
		if (!values.isEmpty())
		{
			appendLine(sb, "\\renewcommand{\\arraystretch}{1.3}");
			appendLine(sb, "\\arrayrulecolor{white}");
			appendLine(sb, "\\rowcolors{2}{gray!25}{gray!10}");
			appendLine(sb, "\\begin{longtable}{@{}p{.2\\textwidth} | p{.76\\textwidth}@{}}");
			appendLine(sb, "\\rowcolor{gray!50}");
			appendLine(sb, "\\textbf{Value} & \\textbf{Description} \\\\");
			for (final Node valueNode : values)
			{
				String valueName = valueNode.getAttributes().getNamedItem("name").getTextContent();
				valueName = valueName.replaceAll(Pattern.quote("_"), Matcher.quoteReplacement("\\_"));		// escape underscores
				
				final Node descriptionNode = valueNode.getAttributes().getNamedItem("description");
				final String description = (descriptionNode != null) ? descriptionNode.getTextContent() : null;
				
				if (description == null)
					System.err.println("WARNING: no javadoc for value " + valueName + " of enum: " + node.fullType);
				else if (StringRoutines.cleanWhitespace(description).equals(""))
					System.err.println("WARNING: empty javadoc for value " + valueName + " of enum: " + node.fullType);
				
				if (description == null)
					appendLine(sb, valueName + " & \\\\");
				else
					appendLine(sb, valueName + " & " + description + " \\\\");
			}
			appendLine(sb, "\\end{longtable}");
		}
	}
	
	/**
	 * @param prefixToRemove Prefix from fullType that we'll remove before determining section header
	 * @param fullType
	 * @param chapter
	 * @return Section header for node with given full type in given chapter
	 */
	private static final String sectionHeader(final String prefixToRemove, final String fullType, final int chapter)
	{
		String sectionHeaderStr = fullType.replace(prefixToRemove, "");

		final int lastDotIdx = sectionHeaderStr.lastIndexOf(".");
		if (lastDotIdx >= 0)
			sectionHeaderStr = sectionHeaderStr.substring(0, lastDotIdx);

		final String[] sectionHeaderParts = sectionHeaderStr.split(Pattern.quote("."));
		String sectionHeader = StringRoutines.join(" - ", StringRoutines.upperCaseInitialEach(sectionHeaderParts));

		if (sectionHeader.length() == 0)
		{
			// We'll just reuse the chapter name as section header
			sectionHeader = CHAPTER_NAMES[chapter];
		}
		
		return sectionHeader;
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
	 * Writes our appendix with list of images
	 */
	private static void writeImageList()
	{
		final String imageAppendixFilepath = "out/tex/AppendixAImageList.tex";
		final StringBuilder sb = new StringBuilder();
		
		// Write introductory part
		appendLine(sb, "% WARNING: Do NOT manually modify this file! Instead modify writeImageList() in GenerateLudiiDocTex.java!");
		appendLine(sb, "");
		appendLine(sb, "\\chapter{Image List} \\label{Appendix:ImageList}");
		appendLine(sb, "\\fancyhead[C]{\\leftmark}");
		appendLine(sb, "");
		appendLine(sb, "This Appendix lists the image files provided with the Ludii distribution.");
		appendLine(sb, "These image names can be used in .lud files to associate particular images with named pieces "
				+ "or board symbols.");
		appendLine(sb, "For pieces, the app tries to find the image whose file name is closest to the defined name, e.g. "
				+ "``QueenLeft'' will match the image file ``queen.svg''.");
		appendLine(sb, "For board symbols, an exact name match is required.");
		appendLine(sb, "");
		appendLine(sb, "\\phantom{}");
		appendLine(sb, "");
		appendLine(sb, "\\noindent");
		appendLine(sb, "Credits for images appear in the About dialog for games in which they are used.");
		appendLine(sb, "");
		appendLine(sb, "\\phantom{}");
		appendLine(sb, "");
		appendLine(sb, "\\noindent");
		appendLine(sb, "Image list as of Ludii v" + Constants.LUDEME_VERSION + ".");
		appendLine(sb, "");
		appendLine(sb, "\\vspace{3mm}");
		appendLine(sb, "\\noindent\\rule{\\textwidth}{2pt}");
		appendLine(sb, "\\vspace{-6mm}");
		appendLine(sb, "");
		
		appendLine(sb, "\\begin{multicols}{2}");

		final String[] svgs = SVGLoader.listSVGs();
		String currSubsec = null;
		for (final String svg : svgs)
		{
			final String[] svgSplit = svg.replaceAll(Pattern.quote("/svg/"), "").split(Pattern.quote("/"));
			final String subsec = StringRoutines.join("/", Arrays.copyOf(svgSplit, svgSplit.length - 1));
			
			if (!subsec.equals(currSubsec))
			{
				currSubsec = subsec;
				appendLine(sb, "\\subsection*{" + subsec + "}");
				appendLine(sb, "\\hspace{4mm}");
			}
			
			appendLine(sb, svgSplit[svgSplit.length - 1].replaceAll(Pattern.quote("_"), Matcher.quoteReplacement("\\_")));
			appendLine(sb, "");
		}
		
		appendLine(sb, "\\end{multicols}");
		
		try (final PrintWriter writer = new PrintWriter(new File(imageAppendixFilepath), "UTF-8"))
		{
			writer.write(sb.toString());
		} 
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void writeKnownDefines()
	{
		final String definesAppendixFilepath = "out/tex/AppendixBKnownDefines.tex";
		final StringBuilder sb = new StringBuilder();
		
		// Write introductory part
		appendLine(sb, "% WARNING: Do NOT manually modify this file! Instead modify writeKnownDefines() in "
				+ "GenerateLudiiDocTex.java!");
		appendLine(sb, "");
		appendLine(sb, "\\chapter{Known Defines} \\label{Appendix:KnownDefines}");
		appendLine(sb, "");
		appendLine(sb, "This Appendix lists the known {\\tt define} structures provided with the Ludii distribution, "
				+ "which are available for use by game authors. ");
		appendLine(sb, "Known defines can be found in the ``def'' package area (or below) with file extension *.def.");
		appendLine(sb, "See Chapter~\\ref{Chapter:Defines} for details on the {\\tt define} syntax.");
		appendLine(sb, "");
		appendLine(sb, "\\vspace{3mm}");
		appendLine(sb, "\\noindent\\rule{\\textwidth}{2pt}");
		appendLine(sb, "\\vspace{-7mm}");
		appendLine(sb, "");
		
		appendLine(sb, convertAllDefToTex("../Common/res/def"));
		
		try (final PrintWriter writer = new PrintWriter(new File(definesAppendixFilepath), "UTF-8"))
		{
			writer.write(sb.toString());
		} 
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void writeGrammar()
	{
		final String grammarAppendixFilepath = "out/tex/AppendixCLudiiGrammar.tex";
		final StringBuilder sb = new StringBuilder();
		
		// Write introductory part
		appendLine(sb, "% WARNING: Do NOT manually modify this file! Instead modify writeGrammar() in "
				+ "GenerateLudiiDocTex.java!");
		appendLine(sb, "");
		appendLine(sb, "\\chapter{Ludii Grammar} \\label{Appendix:LudiiGrammar}");
		appendLine(sb, "This Appendix lists the complete Ludii grammar for the current Ludii version.");
		appendLine(sb, "The Ludii grammar is generated automatically from the hierarchy of Java classes that implement "
				+ "the ludemes described in this document, using the {\\it class grammar} approach described in "
				+ "C. Browne ``A Class Grammar for General Games'', {\\it Computers and Games (CG 2016)}, "
				+ "Springer, LNCS 10068, pp.~169--184.");
		appendLine(sb, "");
		appendLine(sb, "Ludii game descriptions (*.lud files) {\\it must} conform to this grammar, but note that "
				+ "conformance does not guarantee compilation. ");
		appendLine(sb, "Many factors can stop game descriptions from compiling, such as attempting to access a "
				+ "component using an undefined name, attempting to modify board sites that do not exist, and so on.");
		appendLine(sb, "");
		appendLine(sb, "%====================================================================");
		appendLine(sb, "");
		appendLine(sb, "\\section{Compilation}");
		appendLine(sb, "");
		appendLine(sb, "The steps for compiling a game according to the grammar, from a given *.lud game description "
				+ "to an executable Java {\\tt Game} object, are as follows:");
		appendLine(sb, "");
		appendLine(sb, "\\begin{enumerate}");
		appendLine(sb, "");
		appendLine(sb, "\\item {\\it Expand}: The {\\it raw} text *.lud game description is expanded according to "
				+ "the metalanguage features described in Part~\\ref{Part:MetalanguageFeatures} "
				+ "(defines, options, rulesets, ranges, constants, etc.) to give an {\\it expanded} text description "
				+ "of the  game with current options and rulesets instantiated.");
		appendLine(sb, "");
		appendLine(sb, "\\item {\\it Tokenise}: The expanded text description is then {\\it tokenised} into a "
				+ "{\\it symbolic expression} in the form of a tree of simple tokens.");
		appendLine(sb, "");
		appendLine(sb, "\\item {\\it Parse}: The token tree is parsed according to the current Ludii grammar for "
				+ "correctness.");
		appendLine(sb, "");
		appendLine(sb, "\\item {\\it Compile}: The names of tokens in the token tree are then matched with known "
				+ "ludeme Java classes and these are compiled with the specified arguments, if possible, to give "
				+ "a {\\tt Game} object.");
		appendLine(sb, "");
		appendLine(sb, "\\item {\\it Create}: The {\\tt Game} object calls its {\\tt create()} method to perform "
				+ "relevant preparations, such as deciding on an appropriate {\tt State} type, allocating required "
				+ "memory, initialising necessary variables, etc. ");
		appendLine(sb, "");
		appendLine(sb, "\\end{enumerate}");
		appendLine(sb, "");
		appendLine(sb, "%====================================================================");
		appendLine(sb, "");
		appendLine(sb, "%\\section{Error Handling}");
		appendLine(sb, "");
		appendLine(sb, "% TODO: Notes on error handling.");
		appendLine(sb, "");
		appendLine(sb, "%====================================================================");
		appendLine(sb, "");
		appendLine(sb, "\\section{Listing}");
		appendLine(sb, "");
		appendLine(sb, "\\vspace{-2mm}");
		appendLine(sb, "");
		appendLine(sb, "\\begingroup");
		appendLine(sb, "\\obeylines");
		appendLine(sb, "{\\tt \\small");
		appendLine(sb, "\\begin{verbatim}");
		appendLine(sb, "");
		
		appendLine(sb, Grammar.grammar().toString());
		
		appendLine(sb, "");
		appendLine(sb, "\\end{verbatim}");
		appendLine(sb, "}");
		appendLine(sb, "\\endgroup");
		
		try (final PrintWriter writer = new PrintWriter(new File(grammarAppendixFilepath), "UTF-8"))
		{
			writer.write(sb.toString());
		} 
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Converts all .def into .tex from the specified folder and below. 
	 * @return
	 */
	private static String convertAllDefToTex(final String folderPath)
	{
		final StringBuilder sb = new StringBuilder();
		
		// Get list of directories
		final List<File> dirs  = new ArrayList<File>();
		final File folder = new File(folderPath);
		dirs.add(folder);

		for (int i = 0; i < dirs.size(); ++i)
		{
			final File dir = dirs.get(i);
			for (final File file : dir.listFiles())
				if (file.isDirectory())
					dirs.add(file);		
		}
		Collections.sort(dirs);

		try
		{
			// Visit files in each directory
			for (final File dir : dirs)
			{
				final String path = dir.getCanonicalPath().replaceAll(Pattern.quote("\\"), "/");
				if (path.indexOf("/def/") != -1)
				{
					// Add this section header
					final String name = path.substring(path.indexOf("def/"));
					sb.append(texSection(name));
				}
				
				for (final File file : dir.listFiles())
					if (!file.isDirectory())
						convertDefToTex(file, sb);
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Converts the specified .def file to .tex.
	 */
	private static void convertDefToTex(final File file, final StringBuilder sb)
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
            	// Process line for .tex safety
            	line = line.replace("&", "\\&");
            	line = line.replace("_", "\\_");
            	
                lines.add(new String(line));
                line = reader.readLine();
            }
        }
        catch (final IOException e) 
        { 
        	e.printStackTrace(); 
        }
		
		// Handle subsection title for this define
		final String name = file.getName().substring(0, file.getName().length() - 4);
		sb.append(texSubsection(name));

		// Handle comments in description
		final String comments = commentsFromLines(lines);
		if (comments == "")
			sb.append("\\phantom{}\n");
		else
			sb.append("\n" + comments + "\n");
		
		// Handle examples
		final List<String> examples = examplesFromLines(lines);
		if (!examples.isEmpty())
			sb.append(texExample(examples));
		
		// Handle define
		final String define = defineFromLines(lines);
		sb.append(texDefine(define));
    }
	
	//-------------------------------------------------------------------------

	private final static String commentsFromLines(final List<String> lines)
	{
		final StringBuilder sb = new StringBuilder();
		
		int commentLinesAdded = 0;
		for (final String line : lines)
		{
			final int c = line.indexOf("//");
			if (c < 0)
				break;  // not a comment
		
			if (line.contains("@example"))
				break;  // don't include examples in comments
			
			if (commentLinesAdded > 0)
				sb.append(" \\\\ ");
			sb.append(line.substring(c + 2).trim() + " ");
		
			commentLinesAdded++;
		}
		
        final String comments = sb.toString().replace("#", "\\#");
		
		return comments;
	}

	//-------------------------------------------------------------------------

	private final static List<String> examplesFromLines(final List<String> lines)
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
	
	private final static String defineFromLines(final List<String> lines)
	{
		final StringBuilder sb = new StringBuilder();
		
		boolean defineFound = false;
		for (final String line : lines)
		{
			final int c = line.indexOf("(define ");
			if (c >= 0)
				defineFound = true;
			
			if (defineFound)
				sb.append(line + "\n");
		}
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	private static String texThinLine()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("\n" + "\\vspace{-1mm}\n");
		sb.append("\\noindent\\rule{\\textwidth}{0.5pt}\n");
		sb.append("\\vspace{-6mm}\n");
		return sb.toString();
	}
	
	private static String texThickLine()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("\n" + "\\vspace{3mm}\n");
		sb.append("\\noindent\\rule{\\textwidth}{2pt}\n");
		sb.append("\\vspace{-7mm}\n");
		return sb.toString();
	}
	
	private static String texSection(final String title)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("\n" + "%==========================================================\n");
		sb.append(texThickLine());
		sb.append("\n" + "\\section{" + title + "}\n");
		//sb.append("\n" + "%---------------------------------\n");
		return sb.toString();
	}
	
	private static String texSubsection(final String name)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("\n" + "%-----------------------------------------\n");
		sb.append(texThinLine());
		sb.append("\n" + "\\subsection{``" + name + "''}");
		sb.append("  \\label{known:" + name + "}\n");
		return sb.toString();
	}
	
	private static String texExample(final List<String> examples)
	{
		final StringBuilder sb = new StringBuilder();
		if (examples.size() < 2)
			sb.append("\n% Example\n");
		else
			sb.append("\n% Examples\n");
		sb.append("\\vspace{-1mm}\n");
		sb.append("\\subsubsection*{Example}\n");
		sb.append("\\vspace{-3mm}\n");
		sb.append("\n" + "\\begin{formatbox}\n");
		sb.append("\\begin{verbatim}\n");
		
		for (final String example : examples)
			sb.append(example + "\n");
				
		sb.append("\\end{verbatim}\n");
		sb.append("\\vspace{-1mm}\n");
		sb.append("\\end{formatbox}\n");
		sb.append("\\vspace{-2mm}\n");
		return sb.toString();
	}
	
	private static String texDefine(final String define)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("\n% Define\n");
		sb.append("{\\tt\n");
		sb.append("\\begin{verbatim}\n");
		sb.append(define + "\n");
		sb.append("\\end{verbatim}\n");
		sb.append("}\n");
		sb.append("\\vspace{-4mm}\n");
		return sb.toString();
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
				
				return o1.name.compareToIgnoreCase(o2.name);
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
		
//		/**
//		 * Prints this package and all subpackages
//		 * @param numIndents
//		 */
//		protected void print(final int numIndents)
//		{
//			final StringBuilder sb = new StringBuilder();
//			for (int i = 0; i < numIndents; ++i)
//			{
//				sb.append("\t");
//			}
//			sb.append(name);
//			System.out.println(sb);
//			
//			for (final ClassTreeNode child : children)
//			{
//				child.print(numIndents + 1);
//			}
//		}
		
		@Override
		public String toString()
		{
			return "[Node: " + fullType + "]";
		}
	}

}
