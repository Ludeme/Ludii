package app.display.dialogs.visual_editor.LayoutManagement.GraphFactory;

import app.display.dialogs.visual_editor.model.interfaces.iGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Creates graph by parsing *.txt file
 * @author nic0gin
 */

public class GraphFromFile extends GraphCreator
{

    // DEFAULT FILE PATHS
    private final String DEF_FILE_1 = "../resources/graphs/graph1.txt";
    private final String DEF_FILE_2 = "../resources/graphs/graph2.txt";
    private final String DEF_FILE_3 = "../resources/graphs/graph3.txt";
    private final String DEF_FILE_4 = "../resources/graphs/graph4.txt";
    private final String DEF_FILE_5 = "../resources/graphs/graph5.txt";
    private final String DEF_FILE_6 = "../resources/graphs/graph6.txt";
    private final String DEF_FILE_7 = "../resources/graphs/megagraph.txt";

    //
    private final File file;

    public GraphFromFile()
    {
        this.file = new File(DEF_FILE_6);
    }

    public GraphFromFile(String path)
    {
        this.file = new File(path);
    }

    @Override
    public iGraph createGraph()
    {

        int n = -1;
        int m = -1;

        try
        {
            FileReader fr = new FileReader("src/"+file);
            BufferedReader br = new BufferedReader(fr);

            String record;

            while ((record = br.readLine()) != null)
            {
                if( record.startsWith("//") ) continue;
                break;
            }

            assert record != null;
            if(record.startsWith("VERTICES = ")) {n = Integer.parseInt( record.substring(11) );}
            for (int i = 0; i < n; i++) {graph.addNode();}

            record = br.readLine();

            if (record.startsWith("EDGES = ")) {m = Integer.parseInt( record.substring(8) );}

            for (int d=0; d<m; d++)
            {
                record = br.readLine();
                String[] data = record.split(" ");

                graph.addEdge(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
            }
        }
        catch (IOException ex) {System.out.println(ex.getMessage());}

        return graph;

    }
}
