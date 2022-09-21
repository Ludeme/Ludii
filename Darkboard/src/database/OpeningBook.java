/*
 * Created on 3-ott-05
 *
 */
package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;

/**
 * @author Nikola Novarlic
 *
 */
public class OpeningBook implements Serializable {

    Vector openings;
    
    private static final long serialVersionUID = -8105882071227579591L;
    
    public OpeningBook()
    {
        openings = new Vector();
    }

    public static OpeningBook load(File f)
    {
        try
        {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            OpeningBook ob = (OpeningBook)ois.readObject();
            ois.close();
            fis.close();
            return ob;
        } catch (Exception e) {e.printStackTrace(); return null; }
    }

    public void save(File f)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            fos.close();
        } catch (Exception e) {e.printStackTrace(); }
    }

    public int size()
    {
        return openings.size();
    }

    public Opening getOpening(int k)
    {
        if (k<0 || k>=size()) return null;
        return (Opening)openings.get(k);
    }

    public void addOpening(Opening o)
    {
        //this add function behaves in the following way...
        //it scans the existing opening list, looking for either
        //one that is a subset to it, or a superset to it.
        //The bigger opening gets into the book, the other is discarded...

        for (int k=0; k<size(); k++)
        {
            Opening t = getOpening(k);
            if (t.isOpeningSubset(o,true))
            {
                //o is a subset of t, hence we need add nothing to the book.
                return;
            } else
            if (t.isOpeningSubset(o,false))
            {
                //t is a subset of o, we replace t with the longer opening.
                openings.setElementAt(o,k);
                return;
            }
        }
        //still here, the opening is totally new. Add it as it is.
        openings.add(o);
    }

    public String toString()
    {
        String result;
        result = "Openings in book: "+size()+"\n";
        for (int k=0; k<size(); k++)
        {
            result+="*************\n";
            result+=getOpening(k).toString()+"\n";
        }
        return result;
    }


}
