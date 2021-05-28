package metadata.info.database;

import metadata.info.InfoItem;

//-----------------------------------------------------------------------------

/**
 * Specifies the latest Ludii version that this .lud is known to work for.
 * 
 * @author cambolbro
 * 
 * @remarks The version format is (Major version).(Minor version).(Build number).
 *          For example, the first major version for public release is "1.0.0".
 */
public class Version implements InfoItem
{
	private final String version;

	//-------------------------------------------------------------------------
	
	/**
	 * @param version Ludii version in String form.
	 * 
	 * @example (version "1.0.0")
	 */
	public Version(final String version)
	{
		this.version = version;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("    (version \"" + version + "\")\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The version.
	 */
	public String version()
	{
		return version;
	}
	
	//-------------------------------------------------------------------------

}
