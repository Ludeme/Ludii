package app.display.dialogs.visual_editor.recs.gzip;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * Found on 3/14/22 at https://www.geeksforgeeks.org/compressing-decompressing-files-using-gzip-format-java/?ref=lbp
 */
public class GZIPCompression {

    public static void compress(String inputPath, String outputPath)
    {
        byte[] buffer = new byte[1024];
        try
        {
            GZIPOutputStream os =
                    new GZIPOutputStream(new FileOutputStream(outputPath));

            FileInputStream in =
                    new FileInputStream(inputPath);

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
}
