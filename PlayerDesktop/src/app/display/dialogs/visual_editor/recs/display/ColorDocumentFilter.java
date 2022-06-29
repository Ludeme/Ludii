package app.display.dialogs.visual_editor.recs.display;

import app.display.dialogs.visual_editor.recs.utils.FileUtils;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorDocumentFilter extends DocumentFilter
{
    private final JTextPane textPane;
    private final StyledDocument styledDocument;
    private final TextEditor textEditor;

    private StyleContext styleContext;
    private AttributeSet ludemeAttributeSetDark, parenthesisAttributeSetDark, attributeAttributeSetDark, defaultAttributeSetDark,
            numberAttributeSetDark, stringAttributeSetDark, mathAttributeSetDark, bracesAttributeSetDark, hashAttributeSetDark,
            optionAttributeSetDark, commentAttributeSetDark;

    private AttributeSet ludemeAttributeSetLight, parenthesisAttributeSetLight, attributeAttributeSetLight, defaultAttributeSetLight,
            numberAttributeSetLight, stringAttributeSetLight, mathAttributeSetLight, bracesAttributeSetLight, hashAttributeSetLight,
            optionAttributeSetLight, commentAttributeSetLight;

    private Pattern ludemePattern, parenthesisPattern, attributePattern, numberPattern, stringPattern, mathPattern,
            bracesPattern, hashPattern, optionPattern,commentPattern;


    public ColorDocumentFilter(TextEditor textEditor) {
        this.textEditor = textEditor;
        this.textPane = textEditor.getTextArea();
        this.styledDocument= textPane.getStyledDocument();
        init();
        updateTextStyles();
    }

    /**
     * Initialization
     */
    private void init() {
        styleContext = StyleContext.getDefaultStyleContext();

        //START LIGHTMODE
        ludemeAttributeSetLight = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.decode("#7C227A"));
        parenthesisAttributeSetLight = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.decode("#392795"));
        attributeAttributeSetLight = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.decode("#7F3F3E"));
        numberAttributeSetLight = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.decode("#455ED0"));
        stringAttributeSetLight = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.decode("#4F7E61"));
        mathAttributeSetLight = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.BLACK);
        bracesAttributeSetLight = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.BLACK);
        hashAttributeSetLight = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.BLACK);
        optionAttributeSetLight = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.decode("#E70000"));
        commentAttributeSetLight= styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.BLACK);

        defaultAttributeSetLight = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.decode("#575757"));
        //END LIGHTMODE

        //START DARKMODE
        ludemeAttributeSetDark = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.decode("#CC7832"));
        parenthesisAttributeSetDark = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.decode("#CC7832"));
        attributeAttributeSetDark = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.decode("#9876AA"));
        numberAttributeSetDark = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.decode("#6897BB"));
        stringAttributeSetDark = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.decode("#6a8759"));
        mathAttributeSetDark = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.decode("#BBB529"));
        bracesAttributeSetDark = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.decode("#BBB529"));
        hashAttributeSetDark = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.decode("#707D95"));
        optionAttributeSetDark = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.decode("#507874"));
        commentAttributeSetDark = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.decode("#7F7F7A"));

        defaultAttributeSetDark = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.WHITE);
        //END DARKMODE

        // Use a regular expression to find the words you are looking for
        parenthesisPattern = Pattern.compile("\\(|\\)");
        ludemePattern = buildPattern(getLudemeRegex());
        attributePattern = buildPattern(getAttributeRegex());
        numberPattern = buildPattern(getNumberRegex());
        stringPattern = Pattern.compile("\"[^\"]*\"|\"[^\"]*|\"");
        mathPattern = Pattern.compile("!=|%|\\*|\\+|-|/|<|<=|=|>|>=|\\^");
        bracesPattern = Pattern.compile("\\{|}");
        hashPattern = Pattern.compile("#.*");
        optionPattern = Pattern.compile("<.*>");
        commentPattern = Pattern.compile("//.*");
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attributeSet) throws BadLocationException {
        super.insertString(fb, offset, text, attributeSet);
        //--------
        handleTextChanged();
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        super.remove(fb, offset, length);

        handleTextChanged();
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attributeSet) throws BadLocationException {
        super.replace(fb, offset, length, text, attributeSet);

        handleTextChanged();
    }

    /**
     * Runs your updates later, not during the event notification.
     */
    private void handleTextChanged()
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateTextStyles();
            }
        });
    }

    /**
     * Build the regular expression that looks for the whole word of each word that you wish to find.  The "\\b" is the beginning or end of a word boundary.  The "|" is a regex "or" operator.
     * @return
     * @param regex
     */
    private Pattern buildPattern(List<String> regex)
    {
        StringBuilder sb = new StringBuilder();
        for (String token : regex) {
            sb.append("\\b"); // Start of word boundary
            sb.append(token);
            sb.append("\\b|"); // End of word boundary and an or for the next word
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1); // Remove the trailing "|"
        }

        Pattern p = Pattern.compile(sb.toString());

        return p;
    }


    private void updateTextStyles()
    {
        // Clear existing styles
        AttributeSet defaultAttributeSet;
        if(textEditor.isLightMode()) {
            defaultAttributeSet = defaultAttributeSetLight;
        } else {
            defaultAttributeSet = defaultAttributeSetDark;
        }
        styledDocument.setCharacterAttributes(0, textPane.getText().length(), defaultAttributeSet, true);

        List<Pattern> patterns = Arrays.asList(new Pattern[]{ludemePattern,parenthesisPattern,attributePattern,
                numberPattern,stringPattern,bracesPattern,hashPattern,optionPattern, commentPattern,mathPattern});
        List<AttributeSet> attributeSetsDark = Arrays.asList(new AttributeSet[]{ludemeAttributeSetDark, parenthesisAttributeSetDark,
                attributeAttributeSetDark, numberAttributeSetDark, stringAttributeSetDark, mathAttributeSetDark, bracesAttributeSetDark,
                hashAttributeSetDark, optionAttributeSetDark, commentAttributeSetDark});
        List<AttributeSet> attributeSetsLight = Arrays.asList(new AttributeSet[]{ludemeAttributeSetLight, parenthesisAttributeSetLight,
                attributeAttributeSetLight, numberAttributeSetLight, stringAttributeSetLight, mathAttributeSetLight, bracesAttributeSetLight,
                hashAttributeSetLight, optionAttributeSetLight, commentAttributeSetLight});
        // Look for tokens and highlight them
        for(int i = 0; i < patterns.size(); i++) {
            Pattern pattern = patterns.get(i);
            AttributeSet attributeSet;
            if(textEditor.isLightMode()) {
                 attributeSet = attributeSetsLight.get(i);
            } else {
                //darkmode
                attributeSet = attributeSetsDark.get(i);
            }
            Matcher matcher = pattern.matcher(textPane.getText());
            while (matcher.find()) {
                // Change the color of recognized tokens
                styledDocument.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), attributeSet, true);
            }
        }
    }

    private List<String> getLudemeRegex() {
        List<String> ludemes = new ArrayList<>();
        Scanner sc = FileUtils.readFile("src/app/display/dialogs/visual_editor/resources/recs/highlighting/allLudemes.txt");
        while (sc.hasNext()) {
            String nextLine = sc.nextLine();
            ludemes.add(nextLine);
        }
        sc.close();
        return ludemes;
    }
    private List<String> getAttributeRegex() {
        List<String> attributes = new ArrayList<>();
        Scanner sc = FileUtils.readFile("src/app/display/dialogs/visual_editor/resources/recs/highlighting/allAttributes.txt");
        while (sc.hasNext()) {
            String nextLine = sc.nextLine();
            attributes.add(nextLine);
        }
        sc.close();
        return attributes;
    }

    private List<String> getNumberRegex() {
        List<String> numbers = new ArrayList<>();
        Scanner sc = FileUtils.readFile("src/app/display/dialogs/visual_editor/resources/recs/highlighting/allAttributes.txt");
        numbers.add("[0-9]*\\.[0-9]*");
        numbers.add("[0-9]*\\.");
        numbers.add("[0-9]*");
        return numbers;
    }
}
