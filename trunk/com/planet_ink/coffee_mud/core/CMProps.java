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

import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

/*
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class CMProps extends Properties
{
	private static CMProps props=null;
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
	private static CMProps p(){ return props;}

	public static final long serialVersionUID=0;
	public enum Strings
	{
		PLAYERKILL, PLAYERDEATH, FLEE, DOMAIN, DEFAULTPROMPT, COLORSCHEME, CHARSETINPUT, CHARSETOUTPUT, MUDVER, MUDNAME, 
		//Below do not get their Strings set on reset
		MUDSTATUS, MUDPORTS, INIPATH, MUDBINDADDRESS;
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

	protected DVector newusersByIP=new DVector(2);
	protected HashMap<String,Object[]> listData=new HashMap();

	public boolean loaded=false;

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
	/** retrieve a local .ini file entry as a string
	*
	* <br><br><b>Usage:</b>  String s=getPrivateStr("TAG");
	* @param tagToGet   the property tag to retreive.
	* @return String   the value of the .ini file tag
	*/
	public String getPrivateStr(String tagToGet)
	{
		String s=getProperty(tagToGet);
		if(s==null) return "";
		return s;
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
		for(Enumeration e=propertyNames(); e.hasMoreElements();)
		{
			String propName = (String)e.nextElement();
			if(propName.toUpperCase().startsWith(tagStartersToGet))
			{
				String subPropName = propName.substring(tagStartersToGet.length()).toUpperCase();
				String thisTag=this.getProperty(propName);
				if(thisTag!=null)
					strBag.addElement(subPropName,thisTag);
			}
		}
		String[][] strArray = new String[strBag.size()][2];
		for(int s = 0; s < strBag.size(); s++)
		{
			strArray[s][0] = (String)strBag.elementAt(s,1);
			strArray[s][1] = (String)strBag.elementAt(s,2);
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
		if(thisVal.toUpperCase().startsWith("T"))
			return true;
		return false;
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
	public static int getCountNewUserByIP(String address)
	{
		int count=0;
		DVector DV=p().newusersByIP;
		synchronized(DV)
		{
			for(int i=DV.size()-1;i>=0;i--)
				if(((String)DV.elementAt(i,1)).equalsIgnoreCase(address))
				{
					if(System.currentTimeMillis()>(((Long)DV.elementAt(i,2)).longValue()))
						DV.removeElementAt(i);
					else
						count++;
				}
		}
		return count;
	}
	public static void addNewUserByIP(String address)
	{
		DVector DV=p().newusersByIP;
		synchronized(DV)
		{
			DV.addElement(address,Long.valueOf(System.currentTimeMillis()+TimeManager.MILI_DAY));
		}
	}

	public static String[] getSListVar(String key)
	{
		String[] results=(String[])p().listData.get(key);
		if(results == null)
		{
			String S=getListValue(key);
			if(S==null) return null;
			results=CMParms.toStringArray(CMParms.parseCommas(S,true));
			p().listData.put(key, results);
		}
		return results;
	}

	public static String getListValue(String key)
	{
		final String listFileName=CMProps.p().getProperty("LISTFILE");
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
//		if(CMLib.lang()!=null)
//			CMLib.lang().setLocale(getStr("LANGUAGE"),getStr("COUNTRY"));
		for(Strings e : EnumSet.range(Strings.PLAYERKILL, Strings.CHARSETOUTPUT))
			e.setProperty(getStr(e.toString()));

		for(Ints e : EnumSet.range(Ints.LASTPLAYERLEVEL, Ints.MAXCONNSPERACCOUNT))
			e.setProperty(getInt(e.toString()));

		for(Bools e : EnumSet.range(Bools.INTRODUCTIONSYSTEM, Bools.SHOWDAMAGE))
			e.setProperty(getBoolean(e.toString()));

		if(CMLib.color()!=null) CMLib.color().clearLookups();
		Directions.instance().reInitialize(getInt("DIRECTIONS"));
		resetSecurityVars();
		CMLib.propertiesLoaded();
	}

	public void resetSecurityVars() {
		CMSecurity.setDisableVars(getStr("DISABLE"));
		CMSecurity.setDebugVars(getStr("DEBUG"));
	}

	// this is the sound support method.
	// it builds a valid MSP sound code from built-in web server
	// info, and the info provided.
	public static String msp(String soundName, int volume, int priority)
	{
		if((soundName==null)||(soundName.length()==0)||CMSecurity.isDisabled("MSP")) return "";
		return " !!SOUND("+soundName+" V="+volume+" P="+priority+") ";
	}

	public static String[] mxpImagePath(String fileName)
	{
		if((fileName==null)||(fileName.trim().length()==0))
			return new String[]{"",""};
		if(CMSecurity.isDisabled("MXP"))
			return new String[]{"",""};
		int x=fileName.lastIndexOf('=');
		String preFilename="";
		if(x>=0)
		{
			preFilename=fileName.substring(0,x+1);
			fileName=fileName.substring(x+1);
		}
		x=fileName.lastIndexOf('/');
		if(x>=0)
		{
			preFilename+=fileName.substring(0,x+1);
			fileName=fileName.substring(x+1);
		}
		String domain=Strings.DOMAIN.property();
		if((domain==null)||(domain.length()==0))
			domain="localhost";
		return new String[]{"http://"+domain+":27744/images/mxp/"+preFilename,fileName};
	}

	public static String getHashedMXPImage(String key)
	{
		Hashtable H=(Hashtable)Resources.getResource("MXP_IMAGES");
		if(H==null) getDefaultMXPImage(null);
		H=(Hashtable)Resources.getResource("MXP_IMAGES");
		if(H==null) return "";
		return getHashedMXPImage(H,key);

	}
	public static String getHashedMXPImage(Hashtable H, String key)
	{
		if(H==null) return "";
		String s=(String)H.get(key);
		if(s==null) return null;
		if(s.trim().length()==0) return null;
		if(s.equalsIgnoreCase("NULL")) return "";
		return s;
	}

	public static String getDefaultMXPImage(Object O)
	{
		return "";
	}

	public static String msp(String soundName, int priority)
	{ return msp(soundName,50,CMLib.dice().roll(1,50,priority));}

	public static Vector loadEnumerablePage(String iniFile)
	{
		StringBuffer str=new CMFile(iniFile,null,true).text();
		if((str==null)||(str.length()==0)) return new Vector();
		Vector page=Resources.getFileLineVector(str);
		for(int p=0;p<(page.size()-1);p++)
		{
			String s=((String)page.elementAt(p)).trim();
			if(s.startsWith("#")||s.startsWith("!")) continue;
			if((s.endsWith("\\"))&&(!s.endsWith("\\\\")))
			{
				s=s.substring(0,s.length()-1)+((String)page.elementAt(p+1)).trim();
				page.removeElementAt(p+1);
				page.setElementAt(s,p);
				p=p-1;
			}
		}
		return page;
	}

}
