package app.display.dialogs.visual_editor.LayoutManagement.VisualEditor.GrammarModel;

import java.util.*;

public class ClassParser {

    private final HashMap<String, List<String>> SyntaxMap;

    public ClassParser()
    {

        SyntaxMap = null;
    }

    public List<String> getLudImplementation(String reference)
    {
        return SyntaxMap.get(reference);
    }

    public HashMap<String, List<String>> matchReference(String keyword)
    {
        // TODO:
        //  1. get list of keys from SyntaxMap
        //  2. check if key has a keyword
        //  3. return return key-value pairs for all positive cases
        List<String> keyList = new ArrayList<>(SyntaxMap.keySet());
        ListIterator<String> keyListIter = keyList.listIterator();

        HashMap<String, List<String>> matchedKeyValues = new HashMap<>();

        while (keyListIter.hasNext())
        {
            String keyInst = keyListIter.next();
            if (keyInst.contains(keyword))
            {
                matchedKeyValues.put(keyInst, SyntaxMap.get(keyInst));
            }
        }
        return matchedKeyValues;
    }

    public Set<Map.Entry<String, String>> matchSyntax(String keyword)
    {
        // TODO:
        // 1. get list of values of SyntaxMap
        // 2. check values for a keyword
        // 3. return key-values pairs for all positive cases
        return null;
    }

    public Set<Map.Entry<String, String>> matchInputs(List<String> inputs)
    {
        // TODO:
        return null;
    }

    public List<String> terminalReference(String terminal)
    {
        List<String> referenceVariants = new ArrayList<>();
        SyntaxMap.forEach(( k, v) ->
        {
            if(v.contains(terminal)) referenceVariants.add(k);
        });
        return referenceVariants;
    }

}

