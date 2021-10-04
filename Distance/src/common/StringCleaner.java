package common;

public abstract class StringCleaner
{

	public static final StringCleaner cleanAll = getCleanAllStringCleaner();
	public static final StringCleaner cleanKeepUnderScoreAndColon = getKeepUnderScoreAndColonStringCleaner();

	private static StringCleaner getCleanAllStringCleaner() {
		return new StringCleaner()
		{
			
			@Override
			public String[] cleanAndSplit(final String contentData)
			{
				final String dataClean = cleanString(contentData);
				final String[] words = dataClean.split("\\s+");
				return words;
			}
			
			// -------------------------------------------------------------------------

			/**
			 * @param contentData ...
			 * @return String with just single words and no double spaces.
			 */
			@Override
			public String cleanString(final String contentData)
			{
				final String data = contentData;
				final String dataAlphabetic = data.replaceAll("[^A-Za-z0-9 ]", " ");

				final String dataSingleSpace = dataAlphabetic.trim().replaceAll(" +",
						" ");
				final String dataClean = dataSingleSpace.toLowerCase();

				return dataClean;
			}
		};
	}
	
	

	private static StringCleaner getKeepUnderScoreAndColonStringCleaner()
	{
		return new StringCleaner()
		{
			
			@Override
			public String[] cleanAndSplit(final String contentData)
			{
				final String dataClean = cleanString(contentData);
				final String[] words = dataClean.split("\\s+");
				return words;
			}
			
			// -------------------------------------------------------------------------

			/**
			 * @param contentData ...
			 * @return String with just single words and no double spaces.
			 */
			@Override
			public String cleanString(final String contentData)
			{
				// Maybe keep numbers, to ???
				// dataAlphabetic = data.replaceAll("[^A-Za-z ]"," ");
				
				final String data = contentData;
				final String dataAlphabetic = data.replaceAll("[^A-Za-z0-9:_\\+\\-\" ]", " ");

				

				final String dataSingleSpace = dataAlphabetic.trim().replaceAll(" +",
						" ");
				final String dataClean = dataSingleSpace.toLowerCase();

				return dataClean;
			}
		};
	}



	public abstract String[] cleanAndSplit(String contentData);
	public abstract String cleanString(String contentData);

}
