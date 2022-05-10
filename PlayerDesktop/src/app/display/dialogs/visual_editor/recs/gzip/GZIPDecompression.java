package app.display.dialogs.visual_editor.recs.gzip;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * Found on 3/14/22 at https://www.geeksforgeeks.org/compressing-decompressing-files-using-gzip-format-java/?ref=lbp
 */
public class GZIPDecompression {

    public static void decompress(String inputPath, String outputPath)
    {
        byte[] buffer = new byte[1024];
        try
        {
            GZIPInputStream is =
                    new GZIPInputStream(new FileInputStream(inputPath));

            FileOutputStream out =
                    new FileOutputStream(outputPath);

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
