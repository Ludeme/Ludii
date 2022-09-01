package app.display.dialogs.visual_editor.recs.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author filreh
 */
public class GZIPController{
    /**
     * This method reads in a .csv file, compresses it and writes the contents to a .gz file.
     * Credit: Found on 3/14/22 at
     * https://www.geeksforgeeks.org/compressing-decompressing-files-using-gzip-format-java/?ref=lbp
     * @param inputPath location of .csv file
     * @param outputPath   location of .gz file
     */
    public static void compress(String inputPath, String outputPath) {
        byte[] buffer = new byte[1024];
        try
        (
        	GZIPOutputStream os = new GZIPOutputStream(new FileOutputStream(outputPath));
            FileInputStream in = new FileInputStream(inputPath);		
        )
        {
            int totalSize;
            while((totalSize = in.read(buffer)) > 0 )
            {
                os.write(buffer, 0, totalSize);
            }

            in.close();
            os.finish();
            os.close();

            System.out.println("File Successfully compressed");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This method reads in a .gz file, decompresses it and writes the contents to a .csv file.
     * Credit: Found on 3/14/22 at
     * https://www.geeksforgeeks.org/compressing-decompressing-files-using-gzip-format-java/?ref=lbp
     * @param inputPath location of .csv file
     * @param outputPath   location of .gz file
     */
    public static void decompress(String inputPath, String outputPath) {
        byte[] buffer = new byte[1024];
        try
        (
            GZIPInputStream is = new GZIPInputStream(new FileInputStream(inputPath));
            FileOutputStream out = new FileOutputStream(outputPath);        		
        )
        {
            int totalSize;
            while((totalSize = is.read(buffer)) > 0 )
            {
                out.write(buffer, 0, totalSize);
            }

            out.close();
            is.close();

            System.out.println("File Successfully decompressed");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
