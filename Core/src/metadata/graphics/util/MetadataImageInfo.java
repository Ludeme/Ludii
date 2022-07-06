package metadata.graphics.util;

import java.awt.Color;

import game.types.board.SiteType;

/**
 * The Metadata Image Info.
 * Generic object for holding various bits of information when applying metadata graphics
 * 
 * @author matthew.stephenson
 */
public class MetadataImageInfo
{
	/**
	 * The vertices to draw line through.
	 */
	private Integer[] line;
	
	/**
	 * The site.
	 */
	private int site = -1;

	/**
	 * The type of the graph element.
	 */
	private SiteType siteType;

	/**
	 * The path.
	 */
	private String path;
	
	/**
	 * text string.
	 */
	private String text;

	/**
	 * The image scale along x-axis.
	 */
	private float scaleX;
	
	/**
	 * The image scale along y-axis.
	 */
	private float scaleY;

	/**
	 * The main colour.
	 */
	private Color mainColour; // typically the fill colour

	/**
	 * The secondary colour.
	 */
	private Color secondaryColour; // typically the edge colour

	/**
	 * The BoardGraphicsType.
	 */
	private BoardGraphicsType boardGraphicsType;
	
	/**
	 * The site type of the region.
	 */
	private SiteType regionSiteType;
	
	/**
	 * The offset distance in pixels to the right.
	 */
	private float offestX = 0;

	/**
	 * The offset distance in pixels downwards.
	 */
	private float offestY = 0;

	/**
	 * The rotation.
	 */
	private int rotation = 0;
	
	/**
	 * The curved values.
	 */
	private Float[] curve;
	
	/**
	 * The type of curve.
	 */
	private CurveType curveType = CurveType.Spline;
	
	/**
	 * LineStyle.
	 */
	private LineStyle lineStyle = LineStyle.Thin;
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param site    The site.
	 * @param element The type of the graph element.
	 * @param path    The path.
	 * @param scale   The scale of the image.
	 */
	public MetadataImageInfo(final int site, final SiteType element, final String path, final float scale)
	{
		setSite(site);
		setSiteType(element);
		setPath(path);
		setScaleX(scale);
		setScaleY(scale);
	}
	
	/**
	 * @param line       The line between two vertices.
	 * @param element The type of the graph element.
	 * @param mainColour The colour of the edge.
	 * @param scale      The scale of the edges.
	 */
	public MetadataImageInfo(final Integer[] line, final SiteType element, final Color mainColour, final float scale)
	{
		setLine(line);
		setMainColour(mainColour);
		setScaleX(scale);
		setScaleY(scale);
		setSiteType(element);
	}
	
	/**
	 * @param line       The line between two vertices.
	 * @param mainColour The colour of the edge.
	 * @param scale      The scale of the edges.
	 * @param curve      The curved values.
	 * @param siteType   The type of the graph element.
	 * @param curveType  The type of curve.
	 * @param lineStyle  The line style.
	 */
	public MetadataImageInfo(final Integer[] line, final Color mainColour, final float scale, final Float[] curve, final SiteType siteType, final CurveType curveType, final LineStyle lineStyle)
	{
		setLine(line);
		setMainColour(mainColour);
		setScaleX(scale);
		setScaleY(scale);
		setCurve(curve);
		setSiteType(siteType);
		setCurveType(curveType);
		setLineStyle(lineStyle);
	}
	
	/**
	 * @param site       The site.
	 * @param element    The type of the graph element.
	 * @param path       The path.
	 * @param scale      The scale of the image.
	 * @param mainColour The main colour.
	 */
	public MetadataImageInfo(final int site, final SiteType element, final String path, final float scale, final Color mainColour)
	{
		setSite(site);
		setSiteType(element);
		setPath(path);
		setScaleX(scale);
		setScaleY(scale);
		setMainColour(mainColour);
	}
	
	/**
	 * @param site            The site.
	 * @param element         The type of the graph element.
	 * @param path            The path.
	 * @param scale           The scale of the image.
	 * @param mainColour      The main colour.
	 * @param secondaryColour The secondary colour.
	 */
	public MetadataImageInfo(final int site, final SiteType element, final String path, final float scale, final Color mainColour, final Color secondaryColour)
	{
		setSite(site);
		setSiteType(element);
		setPath(path);
		setScaleX(scale);
		setScaleY(scale);
		setMainColour(mainColour);
		setSecondaryColour(secondaryColour);
	}
	
	/**
	 * @param site            The site.
	 * @param element         The type of the graph element.
	 * @param path            The path.
	 * @param scale           The scale of the image.
	 * @param mainColour      The main colour.
	 * @param secondaryColour The secondary colour.
	 * @param rotation        The rotation.
	 */
	public MetadataImageInfo(final int site, final SiteType element, final String path, final float scale, final Color mainColour, final Color secondaryColour, final int rotation)
	{
		setSite(site);
		setSiteType(element);
		setPath(path);
		setScaleX(scale);
		setScaleY(scale);
		setMainColour(mainColour);
		setSecondaryColour(secondaryColour);
		setRotation(rotation);
	}
	
	/**
	 * @param site            The site.
	 * @param element         The type of the graph element.
	 * @param path            The path.
	 * @param scale           The scale of the image.
	 * @param mainColour      The main colour.
	 * @param secondaryColour The secondary colour.
	 * @param rotation        The rotation.
	 * @param offsetX		  The offset to the right.
	 * @param offsetY		  The offset downwards.
	 */
	public MetadataImageInfo(final int site, final SiteType element, final String path, final float scale, final Color mainColour, final Color secondaryColour, final int rotation, final float offsetX, final float offsetY)
	{
		setSite(site);
		setSiteType(element);
		setPath(path);
		setScaleX(scale);
		setScaleY(scale);
		setMainColour(mainColour);
		setSecondaryColour(secondaryColour);
		setRotation(rotation);
		setOffestX(offsetX);
		setOffestY(offsetY);
	}
	
	/**
	 * @param site            The site.
	 * @param element         The type of the graph element.
	 * @param path            The path.
	 * @param scaleX          The scale of the image along x-axis.
	 * @param scaleY          The scale of the image along y-axis.
	 * @param mainColour      The main colour.
	 * @param secondaryColour The secondary colour.
	 * @param rotation        The rotation.
	 * @param offsetX		  The offset to the right.
	 * @param offsetY		  The offset downwards.
	 */
	public MetadataImageInfo(final int site, final SiteType element, final String path, final float scaleX, final float scaleY, final Color mainColour, final Color secondaryColour, final int rotation, final float offsetX, final float offsetY)
	{
		setSite(site);
		setSiteType(element);
		setPath(path);
		setScaleX(scaleX);
		setScaleY(scaleY);
		setMainColour(mainColour);
		setSecondaryColour(secondaryColour);
		setRotation(rotation);
		setOffestX(offsetX);
		setOffestY(offsetY);
	}
	
	/**
	 * @param site            The site.
	 * @param element         The type of the graph element.
	 * @param path            The path.
	 * @param text			  The text string.
	 * @param scaleX          The scale of the image along x-axis.
	 * @param scaleY          The scale of the image along y-axis.
	 * @param mainColour      The main colour.
	 * @param secondaryColour The secondary colour.
	 * @param rotation        The rotation.
	 * @param offsetX		  The offset to the right.
	 * @param offsetY		  The offset downwards.
	 */
	public MetadataImageInfo(final int site, final SiteType element, final String path, final String text, final float scaleX, final float scaleY, final Color mainColour, final Color secondaryColour, final int rotation, final float offsetX, final float offsetY)
	{
		setSite(site);
		setSiteType(element);
		setPath(path);
		setText(text);
		setScaleX(scaleX);
		setScaleY(scaleY);
		setMainColour(mainColour);
		setSecondaryColour(secondaryColour);
		setRotation(rotation);
		setOffestX(offsetX);
		setOffestY(offsetY);
	}
	
	/**
	 * @param site              The site.
	 * @param element           The type of the graph element.
	 * @param boardGraphicsType The BoardGraphicsType.
	 * @param mainColour        The main colour of the board.
	 */
	public MetadataImageInfo(final int site, final SiteType element, final BoardGraphicsType boardGraphicsType, final Color mainColour)
	{
		setSite(site);
		setSiteType(element);
		setMainColour(mainColour);
		setBoardGraphicsType(boardGraphicsType);
	}
	
	/**
	 * @param site           The site.
	 * @param element        The type of the graph element.
	 * @param regionSiteType The type of the region.
	 * @param mainColour     The main colour of the board.
	 * @param scale          The scale.
	 */
	public MetadataImageInfo(final int site, final SiteType element, final SiteType regionSiteType, final Color mainColour, final float scale)
	{
		setSite(site);
		setSiteType(element);
		setMainColour(mainColour);
		setRegionSiteType(regionSiteType);
		setScaleX(scale);
		setScaleY(scale);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return max scale along X and Y axis.
	 */
	public float scale()
	{
		return Math.max(scaleX(), scaleY());
	}

	/**
	 * @return The line indices.
	 */
	public Integer[] line() 
	{
		return line;
	}

	/**
	 * @param line The line indices.
	 */
	public void setLine(final Integer[] line) 
	{
		this.line = line;
	}

	/**
	 * @return The site.
	 */
	public int site() 
	{
		return site;
	}

	/**
	 * @param site The site.
	 */
	public void setSite(final int site) 
	{
		this.site = site;
	}

	/**
	 * @return The siteType.
	 */
	public SiteType siteType() 
	{
		return siteType;
	}

	/**
	 * @param siteType The siteType.
	 */
	public void setSiteType(final SiteType siteType) 
	{
		this.siteType = siteType;
	}

	/**
	 * @return The path.
	 */
	public String path() 
	{
		return path;
	}
	
	/**
	 * @return The text.
	 */
	public String text() 
	{
		return text;
	}

	/**
	 * @param path The path.
	 */
	public void setPath(final String path) 
	{
		this.path = path;
	}
	
	/**
	 * @param path The text.
	 */
	public void setText(final String text) 
	{
		this.text = text;
	}

	/**
	 * @return The X scale.
	 */
	public float scaleX() 
	{
		return scaleX;
	}

	/**
	 * @param scaleX The X scale.
	 */
	public void setScaleX(final float scaleX) 
	{
		this.scaleX = scaleX;
	}

	/**
	 * @return The Y scale.
	 */
	public float scaleY() 
	{
		return scaleY;
	}

	/**
	 * @param scaleY The Y scale.
	 */
	public void setScaleY(final float scaleY) 
	{
		this.scaleY = scaleY;
	}

	/**
	 * @return The main colour.
	 */
	public Color mainColour() 
	{
		return mainColour;
	}

	/**
	 * @param mainColour The main colour.
	 */
	public void setMainColour(final Color mainColour) 
	{
		this.mainColour = mainColour;
	}

	/**
	 * @return The secondary colour.
	 */
	public Color secondaryColour() 
	{
		return secondaryColour;
	}

	/**
	 * @param secondaryColour The secondary colour.
	 */
	public void setSecondaryColour(final Color secondaryColour) 
	{
		this.secondaryColour = secondaryColour;
	}

	/**
	 * @return The graphic type of the board.
	 */
	public BoardGraphicsType boardGraphicsType() 
	{
		return boardGraphicsType;
	}

	/**
	 * @param boardGraphicsType The graphic type of the board.
	 */
	public void setBoardGraphicsType(final BoardGraphicsType boardGraphicsType) 
	{
		this.boardGraphicsType = boardGraphicsType;
	}

	/**
	 * @return The type of the region.
	 */
	public SiteType regionSiteType() 
	{
		return regionSiteType;
	}

	/**
	 * @param regionSiteType The type of the region.
	 */
	public void setRegionSiteType(final SiteType regionSiteType) 
	{
		this.regionSiteType = regionSiteType;
	}

	/**
	 * @return The X offset.
	 */
	public float offestX() 
	{
		return offestX;
	}

	/**
	 * @param offestX The X offset.
	 */
	public void setOffestX(final float offestX) 
	{
		this.offestX = offestX;
	}

	/**
	 * @return The Y offset.
	 */
	public float offestY() 
	{
		return offestY;
	}

	/**
	 * @param offestY The Y offset.
	 */
	public void setOffestY(final float offestY) 
	{
		this.offestY = offestY;
	}

	/**
	 * @return The rotation value.
	 */
	public int rotation() 
	{
		return rotation;
	}

	/**
	 * @param rotation The rotation value.
	 */
	public void setRotation(final int rotation) 
	{
		this.rotation = rotation;
	}

	/**
	 * @return The curve parameters.
	 */
	public Float[] curve() 
	{
		return curve;
	}

	/**
	 * @param curve The curve parameters.
	 */
	public void setCurve(final Float[] curve) 
	{
		this.curve = curve;
	}

	/**
	 * @return The type of the curve.
	 */
	public CurveType curveType() 
	{
		return curveType;
	}

	/**
	 * @param curveType The type of the curve.
	 */
	public void setCurveType(final CurveType curveType) 
	{
		this.curveType = curveType;
	}
	
	/**
	 * @return The line style.
	 */
	public LineStyle lineStyle() 
	{
		return lineStyle;
	}

	/**
	 * @param lineStyle The line style.
	 */
	public void setLineStyle(final LineStyle lineStyle) 
	{
		this.lineStyle = lineStyle;
	}

}