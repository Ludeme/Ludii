package processing.grammarGMLComposer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import grammar.Grammar;
import main.grammar.ebnf.EBNF;
import main.grammar.ebnf.EBNFClause;
import main.grammar.ebnf.EBNFClauseArg;
import main.grammar.ebnf.EBNFRule;

/**
 * Composes graphs for the programm yED. Currently addapted for Tokens of the
 * grammar.
 * 
 * @author Markus
 * @author Zhangyi
 *
 */
public class GraphComposerGrammarToken
{
	String extension = ".gml";
	String testFileName = "Test";
	String GML_HEADER = "Creator \"Small Game Composr\" \nVersion \"2.16\"\ngraph\n"
			+ "[\n"
			+ "\thierarchic\t1\n"
			+ "\tlabel\t\"\"\n"
			+ "\tdirected\t1";
	String NodeTemplate = "s";

	GMLComposerSettingsGrammarToken settings = GMLComposerSettingsGrammarToken
			.getDefaultInstance();

	public GraphComposerGrammarToken(
			final GMLComposerSettingsGrammarToken settings
	)
	{
		this.settings = settings;
	}

	public GraphComposerGrammarToken()
	{
		settings = GMLComposerSettingsGrammarToken.getDefaultInstance();
	}

	private void compose(
			final File folder, final String name, final ArrayList<String> Nodes,
			final ArrayList<String> Edges
	)
	{
		testFileName = name;
		
		final StringBuilder content = new StringBuilder(GML_HEADER);
		for (final String node : Nodes)
		{
			content.append("\n");
			content.append(node);
		}
		for (final String edge : Edges)
		{
			content.append("\n");
			content.append(edge);
		}
		content.append("\n]");

		// If the file doesn't exists, create and write to it
		// If the file exists, truncate (remove all content) and write to it
		if (!folder.exists())
		{
			folder.mkdir();
		}
		final String path = folder.getAbsolutePath() + "/" + testFileName
				+ extension;
		try (FileWriter writer = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(writer))
		{
			bw.write(content.toString());

		} catch (final IOException e)
		{
			System.err.format("IOException: %s%n", e);
		}
		System.out.println("graph written to " + path);
	}

	/**
	 * @param id           ID should be a unique number, is regarded as the
	 *                     target and source for drawing edges
	 * @param fontSize     FontSize
	 * @param x            X coordinate
	 * @param y            Y coordinate
	 * @param w            Width of node
	 * @param h            Height of node
	 * @param label        Don't know what is for yet, can be anything
	 * @param shape        Options including "rectangle", "diamond", "ellipse",
	 *                     and so on
	 * @param fillColor    Color of node
	 * @param outlineColor Color of outline of node
	 * @param elementText  Text displayed in the node
	 * @param fontName     Font
	 * @param anchor       Don't know what is for yet. Default is "c"
	 * @return
	 */
	private String composeNode(
			final int id, final int fontSize, final float x, final float y,
			final float w, final float h, final String label,
			final String shape, final String fillColor,
			final String outlineColor, final String elementText,
			final String fontName, final String anchor
	)
	{
		final String stringID = String.format("\t\t\t id \t %d",
				Integer.valueOf(id));
		final String stringLabel = String.format("\t\t\t label \t \"%s\"",
				label);
		final String stringx = String.format("\t\t\t\t x \t %.0f",
				Float.valueOf(x));
		final String stringy = String.format("\t\t\t\t y \t %.0f",
				Float.valueOf(y));
		final String stringw = String.format("\t\t\t\t w \t %.0f",
				Float.valueOf(w));
		final String stringh = String.format("\t\t\t\t h \t %.0f",
				Float.valueOf(h));
		final String stringShape = String.format("\t\t\t\t type \t \"%s\"",
				shape);
		final String stringFill = String.format("\t\t\t\t fill \t \"%s\"",
				fillColor);
		final String stringOutline = String.format("\t\t\t\t outline \t \"%s\"",
				outlineColor);
		final String stringText = String.format("\t\t\t\t text \t \"%s\"",
				elementText);
		final String stringFontSize = String.format("\t\t\t\t fontSize \t %d",
				Integer.valueOf(fontSize));
		final String stringFontName = String
				.format("\t\t\t\t fontName \t \"%s\"", fontName);
		final String stringAnchor = String.format("\t\t\t\t anchor \t \"%s\"",
				anchor);

		final String node = String.join("\n", "\t node\n\t\t [", stringID,
				stringLabel, "\t\t\t graphics\n\t\t\t[",
				stringx, stringy, stringw, stringh, stringShape,
				"\t\t\t\t raisedBorder \t 0", stringFill, stringOutline,
				"\t\t\t]", "\t\t\t LabelGraphics\n\t\t\t [", stringText,
				stringFontSize, stringFontName, stringAnchor,
				"\t\t\t ]\n\t\t ]");
		return node;
	}

	/**
	 * Width of arrow will be implemented later.
	 * 
	 * @param sourceId    From which node
	 * @param targetId    To which node
	 * @param color       Color of arrow
	 * @param targetArrow TODO
	 * @return
	 */
	private String composeEdge(
			final int sourceId, final int targetId, final int thickness,
			final String color, final Boolean dashedLine,
			final boolean targetArrow
	)
	{
		final String stringSource = String.format("\t\tsource\t%d",
				Integer.valueOf(sourceId));
		final String stringTarget = String.format("\t\ttarget\t%d",
				Integer.valueOf(targetId));
		final String stringThickness = String.format("\t\t\twidth\t%d",
				Integer.valueOf(thickness));
		String stringDashed = "";
		if (dashedLine.booleanValue() == true)
		{
			stringDashed = String.format("\t\t\tstyle\t\"%s\"", dashedLine);
		}
		String stringTargetArrow = "\t\t\ttargetArrow \t \"standard\"";
		if (!targetArrow)
			stringTargetArrow = "";
		final String stringColor = String.format("\t\t\tfill\t\"%s\"", color);
		final String node = String.join("\n", "\tedge\n\t[\n\t\t", stringSource,
				stringTarget, "\t\tgraphics\n\t\t[\n", stringThickness,
				stringDashed, stringColor, stringTargetArrow,
				"\t\t]\n\t]");
		return node;
	}

	/**
	 * composes an gml file to be used with yEd
	 * 
	 * @param folder             folder to export into
	 * @param fileName           the name the file should be. extension will be
	 *                           added
	 * @param suffixTree
	 * @param includeSuffixLinks TODO
	 */
	public void compose(final File folder, final String fileName)
	{
		final ArrayList<ArrayList<String>> nAe = extractNodeEdgeInfos();
		System.out.println("COMPOSING");
		final ArrayList<String> Nodes = nAe.get(0);
		final ArrayList<String> Edges = nAe.get(1);
		compose(folder, fileName, Nodes, Edges);

	}

	/**
	 * used for compose gml graphs transforms the nodes of
	 * 
	 * @param suffixTree
	 * @param includeSuffixLinks including of the suffixLinks, but this clutters
	 *                           the tree and is only used during the building
	 *                           of the suffix tree
	 * @return
	 */
	private ArrayList<ArrayList<String>> extractNodeEdgeInfos()
	{
		final Collection<EBNFRule> rules = Grammar.grammar().ebnf().rules()
				.values();

		// create all nodes

		final HashMap<String, Node> identifierToNodeHashMap = new HashMap<>();

		for (final EBNFRule rule : rules)
		{
			final Node n = new Node(rule);
			identifierToNodeHashMap.put(n.getUniqueIdentifier(), n);
			for (final EBNFClause ebnfClause : rule.rhs())
			{
				if (ebnfClause.isTerminal()&&false)
				{
					final Node tn = Node.createTerminal(ebnfClause);
					if (!identifierToNodeHashMap.containsValue(tn))
						identifierToNodeHashMap.put(tn.getUniqueIdentifier(),
								tn);
				}
				if (ebnfClause.args() == null)
					continue;
				for (final EBNFClauseArg arg : ebnfClause.args())
				{
					
					if (EBNF.isTerminal(arg.token())&&false) {
						if (!identifierToNodeHashMap.containsKey(arg.toString()))
						{
							final Node tn = Node.createTerminal(arg);
							identifierToNodeHashMap.put(tn.getUniqueIdentifier(),
									tn);
						}
					}
				}

			}
		}
		final ArrayList<Node> allNodes = new ArrayList<>(
				identifierToNodeHashMap.values());
		for (final Node n : allNodes)
		{
			if (n.isRule())
			{
				final List<EBNFClause> rhs = n.getRule().rhs();
				for (final EBNFClause ebnfClause : rhs)
				{
					if ((ebnfClause.isTerminal()&&false) || ebnfClause.isRule())
					{
						final Node child = identifierToNodeHashMap
								.get(ebnfClause.token().toString());
						n.connectChild(child);
					}
					if (ebnfClause.args() == null)
						continue;
					for (final EBNFClauseArg arg : ebnfClause.args())
					{
						
						final Node child = identifierToNodeHashMap
									.get(arg.token().toString());
						if (child != null)
							n.connectChild(child);
						
						
					}
				}
			}

		}

		final ArrayList<String> nodes = new ArrayList<String>();
		final ArrayList<String> edges = new ArrayList<String>();

		System.out.println();

		final HashMap<Node, Integer> idMap = new HashMap<>(allNodes.size());
		for (int i = 0; i < allNodes.size(); i++)
		{
			final Node node = allNodes.get(i);
			idMap.put(node, Integer.valueOf(i));
		}

		for (int i = 0; i < allNodes.size(); i++)
		{
			final Node nodeState = allNodes.get(i);
			String newNode;
		
			newNode = composeNode(nodeState, idMap);
			String newEdge;

			final Collection<? extends Node> nl = nodeState.getNodeChildren();
			for (final Node childNode : nl)
			{
				newEdge = composeEdge(idMap, nodeState, childNode, true);
				edges.add(newEdge);
			}

			nodes.add(newNode);

		}

		final ArrayList<ArrayList<String>> nodesAndEdges = new ArrayList<ArrayList<String>>();
		nodesAndEdges.add(nodes);
		nodesAndEdges.add(edges);

		return nodesAndEdges;
	}

	private String composeEdge(
			final HashMap<Node, Integer> idMap, final Node parentNode,
			final Node nodeState, final boolean targetArrow
	)
	{
		final String newEdge = composeEdge(idMap.get(parentNode).intValue(),
				idMap.get(nodeState).intValue(),
				settings.getEdgeThickness(null),
				settings.getEdgeColor(null), Boolean.valueOf(false),
				targetArrow);
		return newEdge;
	}

	private String composeNode(
			final Node nodeState, final HashMap<Node, Integer> idMap
	)
	{
		String newNode;
		final int mover = 0;
		final String label = settings.getNodeLabel(nodeState).replace("\"",
				"'");
		newNode = composeNode(idMap.get(nodeState).intValue(), 12,
				new Random().nextInt(3000), new Random().nextInt(3000),
				settings.getWidth(nodeState),
				settings.getHeight(nodeState, label.split("\\n").length),
				"no label",
				settings.getShapeFromMover(mover, nodeState),
				settings.getFillColorFromMover(mover, nodeState),
				settings.getOutLineColorFromMover(mover, nodeState),
				label, "Dialog", "c");

		return newNode;
	}

}