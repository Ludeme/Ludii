package app.display.dialogs.visual_editor.view.panels.userGuide;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains a paragraph + image (if any).
 * Displays image
 */

public class UserGuideContent
{

    private final String paragraph;
    private final List<Image> images;

    /**
     * Constructor.
     * @param paragraph
     * @param imageNames Names of images in the folder Common/res/img/visual_editor/user_guide/
     */
    public UserGuideContent(String paragraph, List<String> imageNames)
    {
        this.paragraph = paragraph;
        List<Image> images = new ArrayList<>();
        for(String imageName : imageNames)
        {
            String path = "/visual_editor/user_guide/" + imageName;
            try
            {
                images.add(ImageIO.read(getClass().getResource(path)));
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        this.images = images;
    }

    public UserGuideContent(String paragraph, String image)
    {
        this.paragraph = paragraph;
        List<String> imageNames = new ArrayList<>();
        imageNames.add(image);

        List<Image> images = new ArrayList<>();
        for(String imageName : imageNames)
        {
            String path = "/visual_editor/user_guide/" + imageName;
            try
            {
                images.add(ImageIO.read(getClass().getResource(path)));
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        this.images = images;
    }

    public List<Image> images()
    {
        return images;
    }

    public String paragraph()
    {
        return paragraph;
    }

}
