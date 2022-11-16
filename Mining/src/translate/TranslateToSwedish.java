package translate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import main.UnixPrintWriter;

public class TranslateToSwedish
{
	final static String defaultInputPath        = "./res/Games.csv";
	
	public static void main(String[] args) throws IOException {

		final List<String> CSVlines = new ArrayList<String>();
		StringBuffer tempLine = new StringBuffer();
		try (BufferedReader br = new BufferedReader(new FileReader(defaultInputPath))) 
		{
			String line = br.readLine();
			while (line != null)
			{
//				System.out.println(line);
//				System.out.println("OTHER LINE");
				if(line.length() > 2 && line.charAt(0) == '"' && Character.isDigit(line.charAt(1)))
				{
					if(!tempLine.toString().isEmpty())
						CSVlines.add(tempLine.toString());
					tempLine= new StringBuffer("");
				}
				tempLine.append(line);
				line = br.readLine();
			}
			CSVlines.add(tempLine.toString());
		}

		final List<String> swedishDescriptions = new ArrayList<String>();
		for(int i = 1; i < CSVlines.size(); i++)
		{
			String csvLine = CSVlines.get(i);
			boolean inQuote = false;
			int numColumn = 0;
			StringBuffer descriptionBuffer = new StringBuffer("");
			for(int c = 0; c < csvLine.length(); c++)
			{
				char ch = csvLine.charAt(c);
				if(ch == '"')
					inQuote = !inQuote;
				if(ch == ',' && !inQuote)
					numColumn++;
				else
					if(numColumn == 3)
						descriptionBuffer.append(ch);
				if(numColumn == 4)
					break;
			}

			final String description = descriptionBuffer.toString().substring(1, descriptionBuffer.toString().length()-1);
			String descriptionSwedish = translate("en", "sv", description);
			descriptionSwedish = descriptionSwedish.replaceAll("&quot;&quot;", "\"\"");
			descriptionSwedish = descriptionSwedish.replaceAll("&quot;", "\"\"");
			swedishDescriptions.add(descriptionSwedish);
			System.out.println("Line " + i + " done.");
		}
		
		final String output = "Games.csv";
		
		// Write the new CSV.
		try (final PrintWriter writer = new UnixPrintWriter(new File(output), "UTF-8"))
		{
			writer.println(CSVlines.get(0));
			for(int i = 1; i < CSVlines.size(); i++)
			{
				writer.print(CSVlines.get(i).substring(0, CSVlines.get(i).length() - 4));
				writer.println("\"" + swedishDescriptions.get(i-1) + "\"");
			}
		}
		catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
		}
		catch (UnsupportedEncodingException e1)
		{
			e1.printStackTrace();
		}
    }

    private static String translate(String langFrom, String langTo, String text) throws IOException {
        // INSERT YOU URL HERE
        String urlStr = "https://script.google.com/macros/s/AKfycbxohIwAG-uJMG864Rc_yoDkZvJhJe3vpxcWSGNomLC62LNK1xaY89-UU2b7RddEYq8HXA/exec" +
                "?q=" + URLEncoder.encode(text, "UTF-8") +
                "&target=" + langTo +
                "&source=" + langFrom;
        URL url = new URL(urlStr);
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        @SuppressWarnings("resource")
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }
}