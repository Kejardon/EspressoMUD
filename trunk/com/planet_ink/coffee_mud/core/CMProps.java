package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import com.planet_ink.coffee_mud.application.MUD;

import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class CMProps extends Properties
{
	private static CMProps props=null;

	//protected DVector newusersByIP=new DVector(2);
	protected static HashMap<String,Object[]> listData=new HashMap();

	public boolean loaded=false;

	public CMProps()
	{
		if(props==null) props=this;
	}
	public static CMProps instance()
	{
		CMProps p=props;
		if(props==null) p=new CMProps();
		return p;
	}

	public static final long serialVersionUID=0;
	public enum Strings
	{
		PLAYERKILL, PLAYERDEATH, FLEE, DOMAIN, DEFAULTPROMPT, COLORSCHEME, CHARSETINPUT, CHARSETOUTPUT, MUDVER, MUDNAME, 
		//Below do not get their Strings set on reset
		MUDSTATUS, MUDPORTS, MUDBINDADDRESS;
		private String property="";
		public void setProperty(String s)
		{
			property=s;
			if(this==PLAYERKILL)
			{
				int x=s.indexOf("-");
				if(x>0)
					CMProps.Ints.PKILLDIFF.setProperty(CMath.s_int(s.substring(x+1)));
			}
		}
		public String property(){return property;}
	}
	public enum Ints
	{
		LASTPLAYERLEVEL, MAXCONNSPERIP, MAXITEMSHOWN, COMMONACCOUNTSYSTEM, MAXCONNSPERACCOUNT,
		//Backlog is an ini-only thing, other three are reset differently than the rest
		//MAXNEWPERIP, this seems silly and unnecessary. CONNSPERIP is probably fine alone.
		PKILLDIFF, BACKLOG, TICKSPERMUDDAY, TICKSPERMUDMONTH;
		private int property;
		public void setProperty(int i){property=i;}
		public int property(){return property;}
	}
	public enum Bools
	{
		INTRODUCTIONSYSTEM, SHOWDAMAGE,
		//Below are only manually accessed, not reset
		MUDSTARTED, MUDSHUTTINGDOWN;
		private boolean property;
		public void setProperty(boolean b){property=b;}
		public boolean property(){return property;}
	}

	private enum MSSPOptions
	{
		NAME{public String[] value(){return new String[]{Strings.MUDNAME.property()};}},
		PLAYERS{public String[] value(){return new String[]{Integer.toString(CMLib.sessions().size())};}},
		UPTIME{public String[] value(){return new String[]{Long.toString(MUD.hosts.firstElement().getUptimeStart())};}},
		CRAWLTIME{public String[] value(){return new String[]{"-1"};} public String toString(){return "CRAWL TIME";}},
		HOSTNME{public String[] value(){return new String[]{Strings.DOMAIN.property()};}},
		PORT{public String[] value(){return new String[]{Integer.toString(MUD.hosts.firstElement().getPort())};}},
		CODEBASE{public String[] value(){return new String[]{"EspressoMUD v"+Strings.MUDVER.property()};}},
		//CONTACT{public String[] value(){return new String[]{""};}},	//should probably be a CMProps call for this. TODO. Also other things below this not marked Optional.
		//CREATED{public String[] value(){return new String[]{"20??"};}},
		//ICON{public String[] value(){return new String[]{""};}},	//Optional
		//IP{public String[] value(){return new String[]{""};}},	//Optional. Dunno if this is a good idea.
		LANGUAGE{public String[] value(){return new String[]{"English"};}},
		//LOCATION{public String[] value(){return new String[]{""};}},
		//MINIMUMAGE{public String[] value(){return new String[]{"13"};} public String toString(){return "MINIMUM AGE";}},	//also should be CMProps
		//WEBSITE{public String[] value(){return new String[]{"http://"+Strings.DOMAIN.property()+":"+CMLib.httpUtils().getWebServerPort()};}},
		FAMILY{public String[] value(){return new String[]{"Custom", "CoffeeMUD"};}},
		GENRE{public String[] value(){return new String[]{"Fantasy", "Modern"};}},
		GAMEPLAY{public String[] value(){return new String[]{"Adventure"};}},
		STATUS{public String[] value(){return new String[]{"Alpha"};}},	//TODO. Eventually.
		GAMESYSTEM{public String[] value(){return new String[]{"Custom"};}},
		//INTERMUD{public String[] value(){return new String[]{};}},
		SUBGENRE{public String[] value(){return new String[]{"None"};}},
		AREAS{public String[] value(){return new String[]{Integer.toString(CMLib.map().numAreas())};}},
		HELPFILES{public String[] value(){return new String[]{Integer.toString(CMLib.help().getHelpFile().size())};}},
		ROOMS{public String[] value(){return new String[]{"-1"};}},
		CLASSES{public String[] value(){return new String[]{"0"};}},
		LEVELS{public String[] value(){return new String[]{"0"};}},
		RACES{public String[] value(){return new String[]{Integer.toString(CMLib.login().raceQualifies().size())};}},
		//SKILLS{public String[] value(){return new String[]{""};}},
		ANSI{public String[] value(){return new String[]{"1"};}},
		GMCP{public String[] value(){return new String[]{"0"};}},
		MCCP{public String[] value(){return new String[]{"0"};}},
		MCP{public String[] value(){return new String[]{"0"};}},
		MSDP{public String[] value(){return new String[]{"1"};}},
		MSP{public String[] value(){return new String[]{"1"};}},
		MXP{public String[] value(){return new String[]{"1"};}},
		PUEBLO{public String[] value(){return new String[]{"0"};}},
		UTF8{public String[] value(){return new String[]{"0"};} public String toString(){return "UTF-8";}},
		VT100{public String[] value(){return new String[]{"0"};}},
		PAY2PLAY{public String[] value(){return new String[]{"0"};} public String toString(){return "PAY TO PLAY";}},
		PAY4PERKS{public String[] value(){return new String[]{"0"};} public String toString(){return "PAY FOR PERKS";}},
		HIRINGBUILDERS{public String[] value(){return new String[]{"0"};} public String toString(){return "HIRING BUILDERS";}},
		HIRINGCODERS{public String[] value(){return new String[]{"0"};} public String toString(){return "HIRING CODERS";}},
		;
		public abstract String[] value();
	}

	public static String getMSSPIAC()
	{
		if(MUD.hosts.size()==0) return "";
		MudHost host = MUD.hosts.firstElement();
		StringBuilder rpt = new StringBuilder((char)Session.TELNET_IAC+(char)Session.TELNET_SB+(char)Session.TELNET_MSSP);
		for(MSSPOptions option : MSSPOptions.values())
		{
			rpt.append(((char)1)+option.toString());
			for(String value : option.value())
				rpt.append(((char)2)+value);
		}
		rpt.append((char)Session.TELNET_IAC+(char)Session.TELNET_SE);
		return rpt.toString();
	}

	public static String getMSSPPacket()
	{
		if(MUD.hosts.size()==0) return "";
		MudHost host = MUD.hosts.firstElement();
		StringBuilder rpt = new StringBuilder("\r\nMSSP-REPLY-START");
		for(MSSPOptions option : MSSPOptions.values())
		{
			rpt.append("\r\n"+option.toString());
			for(String value : option.value())
				rpt.append("\t"+value);
		}
		rpt.append("\r\nMSSP-REPLY-END\r\n");
		return rpt.toString();
	}

	public CMProps(InputStream in)
	{
		if(props==null) props=this;
		try
		{
			this.load(in);
			loaded=true;
		}
		catch(IOException e)
		{
			loaded=false;
		}
	}
	public CMProps(String filename)
	{
		if(props==null) props=this;
		try
		{
			CMFile F=new CMFile(filename,null,false);
			if(F.exists())
			{
				this.load(new ByteArrayInputStream(F.textUnformatted().toString().getBytes()));
				loaded=true;
			}
			else
				loaded=false;
		}
		catch(IOException e)
		{
			loaded=false;
		}
	}

	public boolean load(String filename)
	{
		try
		{
			this.load(new ByteArrayInputStream(new CMFile(filename,null,false).raw()));
			loaded=true;
		}
		catch(IOException e)
		{
			loaded=false;
		}
		return loaded;
	}

	public CMProps(Properties p, String filename)
	{
		super(p);
		if(props==null) props=this;
		try
		{
			this.load(new ByteArrayInputStream(new CMFile(filename,null,false).raw()));
			loaded=true;
		}
		catch(IOException e)
		{
			loaded=false;
		}
	}

	public static CMProps loadPropPage(String iniFile)
	{
		CMProps page=new CMProps(iniFile);
		if(!page.loaded)
			return null;
		return page;
	}

	/** retrieve raw local .ini file entry as a string
	*
	* <br><br><b>Usage:</b>  String s=getRawPrivateStr("TAG");
	* @param tagToGet   the property tag to retreive.
	* @return String   the value of the .ini file tag
	*/
	public String getRawPrivateStr(String tagToGet)
	{
		return getProperty(tagToGet);
	}

	/** retrieve a particular .ini file entry as a string
	*
	* <br><br><b>Usage:</b>  String s=getStr(p,"TAG");
	* @param tagToGet    the property tag to retreive.
	* @return String    the value of the .ini file tag
	*/
	public String getStr(String tagToGet)
	{
		String thisTag=this.getProperty(tagToGet);
		if(thisTag==null) return "";
		return thisTag;
	}

	/** retrieve a particular .ini file entry as a string, or use a default
	*
	* <br><br><b>Usage:</b>  String s=getStr(p,"TAG");
	* @param tagToGet    the property tag to retreive.
	* @return String    the value of the .ini file tag
	*/
	public String getStr(String tagToGet, String defaultVal)
	{
		String thisTag=this.getProperty(tagToGet);
		if((thisTag==null)||(thisTag.length()==0)) return defaultVal;
		return thisTag;
	}

	/** retrieve particular .ini file entrys as a string array
	*
	* <br><br><b>Usage:</b>  String s=getStrsStarting(p,"TAG");
	* @param tagStartersToGet    the property tag to retreive.
	* @return String    the value of the .ini file tag
	*/
	public String[][] getStrsStarting(String tagStartersToGet)
	{
		DVector strBag = new DVector(2);
		tagStartersToGet = tagStartersToGet.toUpperCase();
		for(Enumeration<String> e=(Enumeration<String>)propertyNames(); e.hasMoreElements();)
		{
			String propName = e.nextElement();
			if(propName.toUpperCase().startsWith(tagStartersToGet))
			{
				String subPropName = propName.substring(tagStartersToGet.length()).toUpperCase();
				String thisTag=this.getProperty(propName);
				if(thisTag!=null)
					strBag.addRow(subPropName,thisTag);
			}
		}
		String[][] strArray = new String[strBag.size()][2];
		for(int s = 0; s < strBag.size(); s++)
		{
			strArray[s][0] = (String)strBag.elementAt(s,0);
			strArray[s][1] = (String)strBag.elementAt(s,1);
		}
		return strArray;
	}

	/** retrieve a particular .ini file entry as a boolean
	*
	* <br><br><b>Usage:</b>  boolean i=getBoolean("TAG");
	* @param tagToGet   the property tag to retreive.
	* @return int   the value of the .ini file tag
	*/
	public boolean getBoolean(String tagToGet)
	{
		String thisVal=getStr(tagToGet);
		return ((thisVal.length()>0)&&(Character.toUpperCase(thisVal.charAt(0))=='T'));
	}

	/** retrieve a particular .ini file entry as a double
	*
	* <br><br><b>Usage:</b>  double i=getDouble("TAG");
	* @param tagToGet    the property tag to retreive.
	* @return int    the value of the .ini file tag
	*/
	public double getDouble(String tagToGet)
	{
		try
		{
			return Double.parseDouble(getStr(tagToGet));
		}
		catch(Throwable t)
		{
			return 0.0;
		}
	}

	/** retrieve a particular .ini file entry as an integer
	*
	* <br><br><b>Usage:</b>  int i=getInt("TAG");
	* @param tagToGet    the property tag to retreive.
	* @return int    the value of the .ini file tag
	*/
	public int getInt(String tagToGet)
	{
		try
		{
			return Integer.parseInt(getStr(tagToGet));
		}
		catch(Throwable t)
		{
			return 0;
		}
	}

	//Ints.X.setProperty(CMath.s_int(val));

	public static String[] getSListVar(String key)
	{
		String[] results=(String[])listData.get(key);
		if(results == null)
		{
			String S=getListValue(key);
			if(S==null) return null;
			results=CMParms.toStringArray(CMParms.parseCommas(S,true));
			listData.put(key, results);
		}
		return results;
	}

	public static String getListValue(String key)
	{
		final String listFileName=CMProps.props.getProperty("LISTFILE");
		synchronized(listFileName.intern())
		{
			Properties rawListData=(Properties)Resources.getResource("PROPS: " + listFileName);
			if(rawListData==null)
			{
				rawListData=new Properties();
				CMFile F=new CMFile(listFileName,null,true);
				if(F.exists())
				{
					try{
						rawListData.load(new ByteArrayInputStream(F.raw()));
					} catch(IOException e){}
				}
				Resources.submitResource("PROPS: " + listFileName, rawListData);
			}
			return rawListData.getProperty(key);
		}
	}

	public void resetSystemVars()
	{
		for(Strings e : EnumSet.range(Strings.PLAYERKILL, Strings.CHARSETOUTPUT))
			e.setProperty(getStr(e.toString()));

		for(Ints e : EnumSet.range(Ints.LASTPLAYERLEVEL, Ints.MAXCONNSPERACCOUNT))
			e.setProperty(getInt(e.toString()));

		for(Bools e : EnumSet.range(Bools.INTRODUCTIONSYSTEM, Bools.SHOWDAMAGE))
			e.setProperty(getBoolean(e.toString()));

		//if(CMLib.color()!=null) CMLib.color().clearLookups();
		resetSecurityVars();
		CMLib.propertiesLoaded();
	}

	public void resetSecurityVars() {
		CMSecurity.setDisableVars(getStr("DISABLE"));
		CMSecurity.setDebugVars(getStr("DEBUG"));
	}

	public static Vector<String> loadEnumerablePage(String iniFile)
	{
		StringBuffer str=new CMFile(iniFile,null,true).text();
		if((str==null)||(str.length()==0)) return new Vector();
		Vector<String> page=Resources.getFileLineVector(str);
		for(int p=0;p<(page.size()-1);p++)
		{
			String s=(page.elementAt(p)).trim();
			if(s.startsWith("#")||s.startsWith("!")) continue;
			if((s.endsWith("\\"))&&(!s.endsWith("\\\\")))
			{
				s=s.substring(0,s.length()-1)+(page.elementAt(p+1)).trim();
				page.removeElementAt(p+1);
				page.setElementAt(s,p);
				p=p-1;
			}
		}
		return page;
	}
}