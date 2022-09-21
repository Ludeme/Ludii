package reader;

import java.io.InputStream;
import java.util.Vector;

/**
 * Provides minimal XML-like reading facilities
 * @author Nikola Novarlic
 *
 */
public class MiniReader {
	
	public class ReaderTag
	{
		public String tag="";
		public String value="";
		public Vector<ReaderTag> subtags = new Vector();
	}
	
	String source;
	int mark=0;
	
	public MiniReader(InputStream is)
	{
		byte b[] = new byte[32768];
		source = "";
		try
		{
			while (true)
			{
				int read = is.read(b);
				if (read<=0) break;
				String part = new String(b);
				source += part;
			}
		} catch (Exception e) {}
	}
	
	public MiniReader(String s)
	{
		source = s;
	}
	
	public String getNextToken(boolean moveMark)
	{
		int mymark = mark;
		
		while (mymark<source.length() && 
				(source.charAt(mymark)==' ' || source.charAt(mymark)=='\n' || source.charAt(mymark)=='\r' || source.charAt(mymark)=='\t')) mymark++;
		
		if (mymark>=source.length()) 
		{
			if (moveMark) mark=mymark;
			return "";
		}
		
		int i1 = source.indexOf('<', mymark+1);
		int i2 = source.indexOf('>', mymark+1);
		
		if (source.charAt(mymark)=='<')
		{
			if (i2<0)
			{
				if (moveMark) mark=mymark;
				return "";
			}
			String result = source.substring(mymark, i2+1);
			if (moveMark) mark = i2+1;
			return result;
		} else
		{
			if (i1<0)
			{
				if (moveMark) mark=mymark;
				return "";
			}
			String result = source.substring(mymark, i1);
			result = result.trim();
			if (moveMark) mark = i1;
			return result;
		}		
	}
	
	public String viewNextToken()
	{
		return getNextToken(false);
	}
	
	public String getNextToken()
	{
		return getNextToken(true);
	}
	
	public void resetMark()
	{
		mark = 0;
	}
	
	public ReaderTag parse()
	{
		Vector<ReaderTag> tagStack = new Vector();
		String input = "";
		do
		{
			input = getNextToken();
			if (input.startsWith("<"))
			{
				String input2 = input.substring(1, input.length()-1);
				if (input2.startsWith("/"))
				{
					//end token
					ReaderTag rt = tagStack.lastElement();
					String orig = input2.substring(1, input2.length());
					if (!orig.equals(rt.tag)) return null; //malformed string, unmatched tag
					tagStack.remove(rt);
					if (tagStack.size()<1) return rt;
					tagStack.lastElement().subtags.add(rt);
				} else
				{
					//begin token
					ReaderTag rt = new ReaderTag();
					rt.tag = input2;
					tagStack.add(rt);
				}
			} else
			{
				if (tagStack.size()<1) return null; //malformed string, tagless text
				tagStack.lastElement().value = input;
			}
		} while (!input.equals(""));
		
		return null;
	}

}
