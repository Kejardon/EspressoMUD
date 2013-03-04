package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public interface XMLLibrary extends CMLibrary
{
	public final static String HEX_DIGITS="0123456789ABCDEF";
	
	public static final String FILE_XML_BOUNDARY="<?xml version=\"1.0\"?>";
	
	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 * 
	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, String Data);
	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 * 
	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, int Data);
	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 * 
	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, short Data);
	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 * 
	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, boolean Data);
	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 * 
	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, long Data);
	/**
	 * Return the contents of an XML tag, given the tag to search for
	 * 
	 * <br><br><b>Usage:</b> String XML=returnXMLBlock(Response,"PDIModelErrors");
	 * @param Blob String to searh
	 * @param Tag Tag name to search for
	 * @return String Information corresponding to the tname
	 */
	public String returnXMLBlock(String Blob, String Tag);
	
	/**
	 * 
	 * @param V
	 * @param tag
	 * @return
	 */
	public String getValFromPieces(Vector<XMLpiece> V, String tag);
	/**
	 * 
	 * @param V
	 * @param tag
	 * @return
	 */
	public Vector<XMLpiece> getContentsFromPieces(Vector<XMLpiece> V, String tag);
	/**
	 * 
	 * @param V
	 * @param tag
	 * @return
	 */
	public Vector<XMLpiece> getRealContentsFromPieces(Vector<XMLpiece> V, String tag);
	/**
	 * 
	 * @param V
	 * @param tag
	 * @return
	 */
	public XMLpiece getPieceFromPieces(Vector<XMLpiece> V, String tag);
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
	 * <br><br><b>Usage:</b> String ThisColHead=getBoolFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @return boolean Information from XML block
	 */
	public boolean getBoolFromPieces(Vector<XMLpiece> V, String tag);
	
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
	 * <br><br><b>Usage:</b> String ThisColHead=getShortFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @return short Information from XML block
	 */
	public short getShortFromPieces(Vector<XMLpiece> V, String tag);
	
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
	 * <br><br><b>Usage:</b> String ThisColHead=getIntFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @return int Information from XML block
	 */
	public int getIntFromPieces(Vector<XMLpiece> V, String tag);
	
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
	 * <br><br><b>Usage:</b> String ThisColHead=getLongFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @return long Information from XML block
	 */
	public long getLongFromPieces(Vector<XMLpiece> V, String tag);
	
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
	 * <br><br><b>Usage:</b> String ThisColHead=getDoubleFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @return double Information from XML block
	 */
	public double getDoubleFromPieces(Vector<XMLpiece> V, String tag);
	
	/**
	 * 
	 * @param buf
	 * @return
	 */
	public Vector<XMLpiece> parseAllXML(String buf);
	
	/**
	 * 
	 * @param buf
	 * @return
	 */
	public Vector<XMLpiece> parseAllXML(StringBuffer buf);
	
	/**
	 * 
	 * @param numberedList
	 * @return
	 */
	public Vector<String> parseXMLList(String numberedList);
	
	/**
	 * 
	 * @param V
	 * @return
	 */
	public String getXMLList(Vector<?> V);
	
	/**
	 * Return the data value within the first XML block
	 * <TAG>Data</TAG>
	 * 
	 * <br><br><b>Usage:</b> String ThisColHead=returnXMLValue(ThisRow);
	 * @param Blob String to searh
	 * @return String Information from first XML block
	 */
	public String returnXMLValue(String Blob);
	
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
	 * <br><br><b>Usage:</b> String ThisColHead=returnXMLValue(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return String Information from XML block
	 */
	public String returnXMLValue(String Blob, String Tag);
	
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
	 * <br><br><b>Usage:</b> String ThisColHead=returnXMLValue(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return String Information from XML block
	 */
	public boolean returnXMLBoolean(String Blob, String Tag);

	/**
	 * Return a parameter value within an XML tag
	 * <TAG Parameter="VALUE">
	 * 
	 * <br><br><b>Usage:</b> String ThisColHead=getParmValue(parmSet,"TD");
	 * @param parmSet set of parms to search
	 * @param Tag Tag to search for
	 * @return String Parameter value
	 */
	public String getParmValue(Hashtable<String,String> parmSet, String Tag);
	
	/**
	 * parse a tag value for safety
	 * 
	 * <br><br><b>Usage:</b> String val=parseOutAngleBrackets(ThisValue);
	 * @param s String to parse
	 * @return String parsed value
	 */
	public String parseOutAngleBrackets(String s);
	/**
	 * restore a tag value parsed for safety
	 * 
	 * <br><br><b>Usage:</b> String val=restoreAngleBrackets(ThisValue);
	 * @param s String to parse
	 * @return String unparsed value
	 */
	public String restoreAngleBrackets(String s);
	
	/**
	 * 
	 * @author Bo Zimmerman
	 *
	 */
	public static class XMLpiece implements Cloneable
	{
		public String tag="";
		public String value="";
		public Vector<XMLpiece> contents=new Vector<XMLpiece>();
		public Hashtable<String,String> parms=new Hashtable<String,String>();
		public XMLpiece parent=null;
		public int outerStart=-1;
		public int innerStart=-1;
		public int innerEnd=-1;
		public int outerEnd=-1;
		
		public XMLpiece copyOf() {
			try {
				XMLpiece piece2=(XMLpiece)this.clone();
				piece2.contents=(Vector<XMLpiece>)contents.clone();
				piece2.parms=(Hashtable<String,String>)parms.clone();
				return piece2;
			} catch(Exception e) {
				return this;
			}
		}
		
		public void addContent(XMLpiece x)
		{
			if (x == null) return;
			if (contents == null) contents = new Vector<XMLpiece>();
			x.parent=this;
			contents.add(x);
		}
	}
}
